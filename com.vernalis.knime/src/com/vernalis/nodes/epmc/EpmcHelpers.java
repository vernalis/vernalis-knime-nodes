/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.nodes.epmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

/**
 * Set of helper functions for accessing and retrieving results from the ePMC
 * RESTful webservices
 * 
 * @author s.roughley
 *
 */
public class EpmcHelpers {
	/*
	 * Helper functions for the ePubMedCentral query nodes
	 */

	/**
	 * Function to build correct format url object for ePMC RESTful query
	 * 
	 * @param queryString
	 *            The query string for the required query
	 * @param resultType
	 *            The results type (lite, idlist, lite)
	 * @param pageNo
	 *            The page number of the results
	 * @return URL object for the RESTful query
	 * @deprecated Service no longer uses pageNo
	 */
	@Deprecated
	public static URL buildQueryURL(String queryString, String resultType, int pageNo) {
		/*
		 * This builds a query URL from query string
		 */
		String url = "http://www.ebi.ac.uk/europepmc/webservices/rest/search/query=";
		url += queryString;
		url += "&resulttype=" + resultType;
		url += "&page=" + pageNo;
		url = url.replace(" ", "%20").replace("\"", "%22").replace("[", "%5B").replace("]", "%5D")
				.replace("+", "%20");
		URL url2 = null;
		try {
			url2 = new URL(url);
		} catch (MalformedURLException e) {
			// do nothing
		}
		return url2;
	}

	/**
	 * Overloaded Function to build correct format url object for ePMC RESTful
	 * query with default page size, return format and no email registration
	 * 
	 * @param queryString
	 *            The query string for the required query
	 * @param resultType
	 *            The results type (core, idlist, lite)
	 * @param nextCursor
	 *            The next cursor
	 * @return URL object for the RESTful query
	 */
	public static URL buildQueryURL(String queryString, String resultType, String nextCursor) {
		return buildQueryURL(queryString, resultType, nextCursor, 25, null, null);
	}

	/**
	 * Function to build correct format url object for ePMC RESTful query
	 * 
	 * @param queryString
	 *            The query string for the required query
	 * @param resultType
	 *            The results type (core, idlist, lite)
	 * @param nextCursor
	 *            The next cursor
	 * @param pageSize
	 *            The number of hits per page
	 * @param format
	 *            The format of the return type (<code>null</code> returns XML)
	 * @param email
	 *            An email address to register. <code>null</code> registers no
	 *            email address
	 * @return URL object for the RESTful query
	 */
	public static URL buildQueryURL(String queryString, String resultType, String nextCursor,
			int pageSize, EpmcFormats format, String email) {
		/*
		 * This builds a query URL from query string
		 */
		StringBuilder url = new StringBuilder(
				"http://www.ebi.ac.uk/europepmc/webservices/rest/search/query=");
		url.append(queryString.replace(" ", "%20").replace("\"", "%22").replace("[", "%5B")
				.replace("]", "%5D").replace("+", "%20"));
		url.append("&resulttype=").append(resultType);
		url.append("&cursorMark=").append(nextCursor == null ? "*" : nextCursor);
		url.append("&pageSize=").append(pageSize);
		if (format != null) {
			url.append("&format=").append(format.toString().toLowerCase());
		}
		if (email != null && !email.isEmpty()) {
			url.append("&email=").append(format.toString().toLowerCase());
		}
		URL url2 = null;
		try {
			url2 = new URL(url.toString());
		} catch (MalformedURLException e) {
			// do nothing
		}
		return url2;
	}

	/**
	 * Function to build the query string for the ePMC from a set of query
	 * options
	 * 
	 * @param Title
	 *            Title query text
	 * @param Authors
	 *            Authors query text
	 * @param Affiliations
	 *            Affiliations query text
	 * @param From
	 *            From publication date query
	 * @param To
	 *            To publication date query
	 * @param Journals
	 *            Journal name(s) query
	 * @param MeSHSubjects
	 *            MeSH (Medical Subject Headings) Terms query
	 * @param GeneralQuery
	 *            General query - for any other query items with field
	 *            identifiers
	 * @param sortDate
	 *            Sort by date?
	 * @return Query string for the full query based on the options specified
	 */
	public static String buildQueryString(String Title, String Authors, String Affiliations,
			String From, String To, String Journals, String MeSHSubjects, String GeneralQuery,
			boolean sortDate) {
		/*
		 * Builds the ePMC query string from the query options
		 */
		String queryString = " ";
		queryString += (GeneralQuery != null && !("".equals(GeneralQuery))) ? GeneralQuery + " "
				: "";
		queryString += (Title != null && !("".equals(Title))) ? addField(Title, "TITLE") : "";
		queryString += (Authors != null && !("".equals(Authors))) ? addField(Authors, "AUTH") : "";
		queryString += (Affiliations != null && !("".equals(Affiliations)))
				? addField(Affiliations, "AFF") : "";
		if ((To != null && !("".equals(To))) || (From != null && !("".equals(From)))) {
			// Deal with dates in query
			queryString += "PUB_YEAR:[";
			// Default start year is '0'
			queryString += (From != null) ? From : "0";
			queryString += " TO ";
			// Default end year is now+2
			queryString += (To != null) ? To : defaultYearTo();
			queryString += "] ";
		}
		queryString += (Journals != null && !("".equals(Journals))) ? addField(Journals, "JOURNAL")
				: "";
		queryString += (MeSHSubjects != null && !("".equals(MeSHSubjects)))
				? addField(MeSHSubjects, "KW") : "";

		queryString = queryString.trim();
		// Now deal with sorting order - default is relevance
		queryString += (sortDate) ? " SORT_DATE:y" : "";
		return queryString.trim().replace("  ", " ");
	}

	/**
	 * Function to add a field to a query string
	 * 
	 * @param value
	 *            The query data
	 * @param category
	 *            The ePMC query field tag
	 * @return query string component
	 */
	public static String addField(String value, String category) {
		/*
		 * Creates the added field string to add to a query from the input
		 */
		value = value.toLowerCase().trim();
		String r = "(" + category.toUpperCase() + ":\"";
		r += value;
		// Deal with OR
		r = r.replace(" or ", "\" OR " + category + ":\"");
		// Deal with AND
		r = r.replace(" and ", "\" " + category + ":\"");

		// Tidy up, in case the user was too clever!
		r = r.replace(category + ":" + category + ":", category + ":");
		r = r.replace("::", ":");
		r = r.replace("\"\"", "\"");

		r += "\") ";
		return r;
	}

	/**
	 * Function to mimic the ePMC default year behaviour (which is to search to
	 * current year + 2
	 * 
	 * @return The current year + 2
	 */
	public static String defaultYearTo() {
		/*
		 * ePMC searches the year to to the current year +2 by default if the to
		 * parameter is omitted This function provides a quick way to return
		 * that value
		 */
		int year = Calendar.getInstance().get(Calendar.YEAR);
		year += 2;
		return "" + year;
	}

	/**
	 * Function to run a query on the ePMC RESTful service
	 * 
	 * @param queryUrl
	 *            The query URL
	 * @return XML string containing the query results
	 */
	public static String askEpmc(String queryUrl) {
		/*
		 * Run HTTP query with url supplied as string Simple wrapper to run via
		 * URL
		 */
		try {
			URL url = new URL(queryUrl);
			return askEpmc(url, null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Function to run a query on the ePMC RESTful service
	 * 
	 * @param queryUrl
	 *            The query URL
	 * @return XML string containing the query results
	 */
	public static String askEpmc(URL queryUrl, ExecutionContext exec)
			throws CanceledExecutionException {
		/*
		 * Run HTTP query. xml is returned as a string
		 */

		int[] delays = { 1, 2, 5, 10, 30, 60 };
		for (int delay : delays) {
			try {
				HttpURLConnection conn = (HttpURLConnection) queryUrl.openConnection();
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new IOException(conn.getResponseMessage());
				}
				StringBuffer retVal = new StringBuffer();
				BufferedReader rd = new BufferedReader(
						new InputStreamReader(conn.getInputStream(), "UTF8"));
				String line;
				while ((line = rd.readLine()) != null) {
					retVal.append(line);
					if (exec != null) {
						try {
							exec.checkCanceled();
						} catch (CanceledExecutionException e) {
							rd.close();
							throw e;
						}
					}
				}
				rd.close();
				conn.disconnect();
				return xmlCleanSpecialChars(retVal.toString());
			} catch (CanceledExecutionException e) {
				throw e;
			} catch (Exception e) {
				NodeLogger.getLogger(EpmcHelpers.class).info("Error contacting server, waiting "
						+ delay + " seconds before trying again...");
				NodeLogger.getLogger(EpmcHelpers.class).info("Response message: " + e.getMessage());
				NodeLogger.getLogger(EpmcHelpers.class).info("URL: " + queryUrl.toString());
				pause(delay, exec);
			}
		}
		NodeLogger.getLogger(EpmcHelpers.class)
				.warn("Unable to contact server - please try again later");
		return null;
	}

	/**
	 * Simple delay function
	 * 
	 * @param delay
	 *            the time to wait (in seconds)
	 * @param exec
	 *            The execution context
	 * @throws CanceledExecutionException
	 *             If the user cancelled..
	 */
	private static void pause(int delay, ExecutionContext exec) throws CanceledExecutionException {
		// simple delay function without using threads
		Date start = new Date();
		while (new Date().getTime() - start.getTime() < delay * 1000) {
			if (exec != null) {
				exec.checkCanceled();
			}
		}
	}

	/**
	 * Very simple XML parser which returns the first occurrence of a field from
	 * an XML string as an XML Data Cell object
	 * 
	 * @param xml
	 *            The XML string
	 * @param tag
	 *            The XML field tag (which does not include < and > characters
	 * @return XML Datacell with the relevant field
	 */
	public static DataCell getCellFromXmlField(String xml, String tag) {
		/*
		 * This simple parser assumes only one occurrence of a field in the xml
		 * input (otherwise only the first occurrence will be returned) A
		 * DataCell is returned
		 */
		String field = getStringFromXmlField(xml, tag);
		DataCell cell = DataType.getMissingCell();
		if (!("".equals(field) || field == null)) {
			try {
				cell = XMLCellFactory.create(field);
			} catch (Exception e) {
				cell = new StringCell(field);
			}
		}
		return cell;
	}

	/**
	 * Very simple XML parser which returns the first occurrence of a field from
	 * an XML string as an XML String
	 * 
	 * @param xml
	 *            The XML string
	 * @param tag
	 *            The XML field tag (which does not include < and > characters
	 * @return XML String with the relevant field
	 */
	public static String getStringFromXmlField(String xml, String tag) {
		/*
		 * This simple parser assumes only one occurrence of a field in the xml
		 * input (otherwise only the first occurrence will be returned) A String
		 * is returned
		 */
		String field = xml.split("<" + tag + ">")[1].split("</" + tag + ">")[0].trim();
		return field;
	}

	/**
	 * Function to convert an XML String containing multiple results to a List
	 * of DataCells containing one result per cell
	 * 
	 * @param xml
	 *            The XML string
	 * @param tag
	 *            The tag identifying the result block
	 * @param wrapperTag
	 *            The tag to wrap the results in the new xml cell
	 * @return List of XML DataCells
	 */
	public static List<DataCell> getRecordsFromXml(String xml, String tag, String wrapperTag) {

		String[] fields = xml.split("<" + tag + ">");

		if (fields.length <= 1) {
			return null;
		}
		// Now we need to extract the xml header from the first entry
		String xmlHeader = fields[0].split(">")[0] + ">";
		String newXml;
		List<DataCell> cells = new ArrayList<DataCell>();
		for (int i = 1, length = fields.length; i < length; i++) {
			if (fields[i].startsWith("<")) {
				// xml cell - NB needs to be 'good xml'
				newXml = xmlHeader + "<" + wrapperTag + ">" + fields[i].split("</" + tag + ">")[0]
						+ "</" + wrapperTag + ">";
				try {
					cells.add(XMLCellFactory.create(newXml));
					// cells.add(XMLCellFactory.create(xml));
					// cells.add(new StringCell(newXml));
				} catch (Exception e) {
					cells.add(DataType.getMissingCell());
				}
			} else {
				// String cell
				cells.add(new StringCell(fields[i].split("</" + tag + ">")[0]));
			}
		}
		return cells;
	}

	/**
	 * Function to convert xml special characters (e.g. '&quot') to plain text
	 * equivalents
	 * 
	 * @param xml
	 *            The xml String object
	 * @return 'Cleaned' string
	 */
	public static String xmlCleanSpecialChars(String xml) {
		// Return the result as a string
		String[] xmlEntities = { "quot", "amp", "apos", "lt", "gt" };
		String[] xmlReplacements = { "\"", "&", "'", "<", ">" };
		for (int i = 0; i < xmlEntities.length; i++) {
			xml = xml.replace("&" + xmlEntities[i] + ";", xmlReplacements[i]);
			i++;
		}
		return xml;
	}

}
