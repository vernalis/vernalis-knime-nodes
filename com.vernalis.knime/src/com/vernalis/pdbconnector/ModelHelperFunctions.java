/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.pdbconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.knime.base.node.io.tablecreator.prop.SmilesTypeHelper;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.util.tokenizer.Tokenizer;
import org.knime.core.util.tokenizer.TokenizerSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vernalis.core.RestClient;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.ReportField;

/**
 * Static helper functions for PdbConnectorNodeModel.
 * 
 * @see PdbConnectorNodeModel
 */
public class ModelHelperFunctions {
	/** DateFormat for yyyy-MM-dd. */
	private static final DateFormat ymdFormat = new SimpleDateFormat(
			Properties.YMD_FORMAT);

	/** DateFormat for yyyy-MM. */
	private static final DateFormat ymFormat = new SimpleDateFormat(
			Properties.YM_FORMAT);

	/** DateFormat for yyyy. */
	private static final DateFormat yearFormat = new SimpleDateFormat(
			Properties.YEAR_FORMAT);

	/** UTC Calendar. */
	private static final Calendar cal = DateAndTimeCell.getUTCCalendar();

	/**
	 * Constructs the overall xml query string to post to the PDB Advanced
	 * Search web service.
	 * 
	 * The query models are first combined with the specified conjunction (AND
	 * or OR) The composite query from the first step is then combined with the
	 * similarity query (always using an AND conjunction).
	 * 
	 * @param queryModels
	 *            the query models
	 * @param simModel
	 *            the similarity query model
	 * @param conjunction
	 *            the conjunction type
	 * @return the xml query string
	 */
	public static String getXmlQuery(final List<QueryOptionModel> queryModels,
			final QueryOptionModel simModel, final String conjunction) {
		List<QueryOptionModel> simModels = new ArrayList<QueryOptionModel>();
		simModels.add(simModel);
		// inner query string is for the selected queries themselves.
		// outer query string is combined with similarity query (conjunction is
		// always AND).
		return ModelHelperFunctions.getXmlQuery(
				ModelHelperFunctions.getXmlQuery("", queryModels, conjunction),
				simModels, Properties.CONJUNCTION_AND);
	}

	/**
	 * Constructs a partial xml query string from a list of query models and a
	 * conjunction operator.
	 * 
	 * The return string is preseeded with an optional initial query string.
	 * 
	 * @param initialQuery
	 *            the initial query
	 * @param queryModels
	 *            the query models
	 * @param conjunction
	 *            the conjunction (used only if there is more than one query
	 *            model)
	 * @return the xml query string
	 */
	public static String getXmlQuery(final String initialQuery,
			final List<QueryOptionModel> queryModels, final String conjunction) {
		StringBuffer retVal = new StringBuffer();
		List<String> queries = new ArrayList<String>();
		// Pre-seed with initial query string (if defined)
		if (!initialQuery.isEmpty()) {
			queries.add(initialQuery);
		}
		// Get list of all selected query models
		List<QueryOptionModel> selected = new ArrayList<QueryOptionModel>();
		for (QueryOptionModel queryModel : queryModels) {
			if (queryModel.isSelected()) {
				selected.add(queryModel);
				queries.add(queryModel.getXmlQuery());
			}
		}

		switch (queries.size()) {
		case 0:
			break;
		case 1:
			retVal.append(queries.get(0));
			break;
		default:
			retVal.append(Properties.COMPOSITE_START);
			int N = queries.size();
			for (int i = 0; i < N; ++i) {
				retVal.append(Properties.REFINEMENT_START);
				retVal.append(Properties.LEVEL_START);
				retVal.append(i);
				retVal.append(Properties.LEVEL_END);
				if (i > 0) {
					retVal.append(conjunction);
				}
				retVal.append(queries.get(i));
				retVal.append(Properties.REFINEMENT_END);
			}
			retVal.append(Properties.COMPOSITE_END);
			break;
		}
		return retVal.toString();
	}

	/**
	 * Gets the KNIME data type for a given ReportField type.
	 * 
	 * Performs dynamic check that Smiles support is available in KNIME. If so,
	 * SMILES report fields are returned as Smiles DataType, else returned as
	 * StringCell DataType.
	 * 
	 * @param field
	 *            the report field
	 * @return the KNIME data type
	 */
	public static DataType getDataType(final ReportField field) {
		switch (field.getType()) {
		case STRING:
			return StringCell.TYPE;
		case INTEGER:
			return IntCell.TYPE;
		case DOUBLE:
			return DoubleCell.TYPE;
		case DATE:
			return DateAndTimeCell.TYPE;
		case SMILES:
			return SmilesTypeHelper.INSTANCE.isSmilesAvailable() ? SmilesTypeHelper.INSTANCE
					.getSmilesType() : StringCell.TYPE;
		case PNG_URL:
			return StringCell.TYPE;
		default:
			return StringCell.TYPE;
		}
	}

	/**
	 * Gets the KNIME data cell for a given report field value.
	 * 
	 * If field or field value is null or empty, returns MissingCell. Else
	 * attempts to convert the field value string to the appropriately typed
	 * data cell value.
	 * 
	 * @param field
	 *            the report field
	 * @param fieldValue
	 *            the field value (as a string)
	 * @param urlSuffix
	 *            the url suffix (for PNG_URL fields only)
	 * @return the KNIME data cell
	 * @throws Exception
	 *             if field value is invalid
	 */
	public static DataCell getDataCell(final ReportField field,
			final String fieldValue, final String urlSuffix) throws Exception {
		if ((field == null) || (fieldValue == null) || fieldValue.isEmpty()
				|| fieldValue.equalsIgnoreCase("null")) {// check for missing
															// data
			return DataType.getMissingCell();
		} else {
			switch (field.getType()) {
			case STRING:
				return new StringCell(fieldValue);
			case INTEGER:
				try {
					return new IntCell(Integer.parseInt(fieldValue));
				} catch (NumberFormatException e) {
					PdbConnectorNodeModel.logger.warn("Invalid integer value ("
							+ fieldValue + ") in field: " + field.getColName());
					return DataType.getMissingCell();
				}
			case DOUBLE:
				try {
					return new DoubleCell(Double.parseDouble(fieldValue));
				} catch (NumberFormatException e) {
					PdbConnectorNodeModel.logger.warn("Invalid double value ("
							+ fieldValue + ") in field: " + field.getColName());
					return DataType.getMissingCell();
				}
			case DATE:
				try {
					cal.setTime(ymdFormat.parse(fieldValue));
					return new DateAndTimeCell(cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH),
							cal.get(Calendar.DAY_OF_MONTH));
				} catch (ParseException e) {
					PdbConnectorNodeModel.logger.debug("Trying simpler "
							+ Properties.YM_FORMAT + " format in field: "
							+ field.getColName());
					try {// try year-month format
						cal.setTime(ymFormat.parse(fieldValue));
						return new DateAndTimeCell(cal.get(Calendar.YEAR),
								cal.get(Calendar.MONTH), 1);// force to first
															// day of month
					} catch (ParseException e2) {
						PdbConnectorNodeModel.logger.debug("Trying simpler "
								+ Properties.YEAR_FORMAT + " format in field: "
								+ field.getColName());
						try {// try year-only format
							cal.setTime(yearFormat.parse(fieldValue));
							return new DateAndTimeCell(cal.get(Calendar.YEAR),
									1, 1);// force to first of January
						} catch (ParseException e3) {
							PdbConnectorNodeModel.logger
									.warn("Invalid date value (" + fieldValue
											+ ") in field: "
											+ field.getColName());
							return DataType.getMissingCell();
						}
					}
				}
			case SMILES:
				return SmilesTypeHelper.INSTANCE.isSmilesAvailable() ? SmilesTypeHelper.INSTANCE
						.newInstance(fieldValue) : new StringCell(fieldValue);
			case PNG_URL:
				// Concatenate the field value between url prefix and suffix to
				// generate
				// the full image URL
				return new StringCell(Properties.LIGAND_IMG_LOCATION
						+ fieldValue + urlSuffix);
			default:
				throw new IOException("Invalid data type at field="
						+ field.getColName() + ": value=" + fieldValue);
			}
		}
	}

	/**
	 * Posts an XML query string to the PDB Advanced Search RESTful web service.
	 * 
	 * @param xml
	 *            the xml query string to post
	 * @return the result as a list of strings, one per line of output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static List<String> postQuery(final String xml) throws IOException {
		List<String> retVal = new ArrayList<String>();
		URL url = new URL(Properties.SEARCH_LOCATION);
		String encodedXML = URLEncoder.encode(xml, "UTF-8");
		InputStream in = RestClient.doPOST(url, encodedXML);
		BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = rd.readLine()) != null) {
			if (line.length() <= 4) { // skip the query id string
				retVal.add(line);
			}
		}
		rd.close();
		return retVal;
	}

	/**
	 * Gets the custom report in CSV format from the PDB Custom Report RESTful
	 * web service.
	 * 
	 * The custom report is returned as a list of list of field values, where
	 * the inner list represents a single report row (one PDB ID), and the outer
	 * list represents all PDB IDs.
	 * 
	 * <P>
	 * WARNING: This method is unsafe as it does not deal correctly with field
	 * values containing quote characters. Use getCustomReportXml2 instead.
	 * 
	 * @param pdbIds
	 *            the list of PDB IDs to generate report for
	 * @param selected
	 *            the selected report fields to include in the report
	 * @return the custom report
	 * @throws Exception
	 *             the exception
	 * @deprecated
	 * @see ModelHelperFunctions#getCustomReportXml2(List, List)
	 */
	@Deprecated
	public static List<List<String>> getCustomReportCsv(
			final List<String> pdbIds, final List<ReportField> selected)
			throws Exception {
		List<List<String>> retVal = new ArrayList<List<String>>();
		StringBuffer buf = new StringBuffer(Properties.REPORT_LOCATION);
		buf.append(ModelHelperFunctions.getPdbIdUrl(pdbIds));
		buf.append(ModelHelperFunctions.getReportColumnsUrl(selected));
		buf.append(Properties.REPORT_CSV_URL);
		URL url = new URL(buf.toString());
		String urlStr = url.toExternalForm();
		PdbConnectorNodeModel.logger.debug("getCustomReport url=" + urlStr);
		PdbConnectorNodeModel.logger.debug("url length=" + urlStr.length());
		String report = RestClient.getResult(url);

		// Tokenizer settings for report (records separated by <br />)
		TokenizerSettings settings = new TokenizerSettings();
		settings.addDelimiterPattern(Properties.REPORT_CSV_LINE_DELIM, true,
				false, false);
		// Tokenizer settings for each record (field values separated by ,)
		TokenizerSettings recordSettings = new TokenizerSettings();
		recordSettings.addDelimiterPattern(Properties.REPORT_CSV_RECORD_DELIM,
				false, false, false);
		recordSettings.addQuotePattern(Properties.REPORT_CSV_OPEN_QUOTE,
				Properties.REPORT_CSV_CLOSE_QUOTE);

		Tokenizer tokenizer = new Tokenizer(new StringReader(report));
		tokenizer.setSettings(settings);
		String record;
		boolean isFirst = true;
		while ((record = tokenizer.nextToken()) != null) {
			if (!isFirst) {// skip header row
				List<String> recordVals = new ArrayList<String>();
				Tokenizer recordTokenizer = new Tokenizer(new StringReader(
						record));
				recordTokenizer.setSettings(recordSettings);
				String fieldValue;
				while ((fieldValue = recordTokenizer.nextToken()) != null) {
					recordVals.add(fieldValue);
				}
				// work around to check for empty last token
				if ((recordVals.size() + 1) == selected.size()) {
					recordVals.add("");// pad with empty last field
				}
				retVal.add(recordVals);
			}
			isFirst = false;
		}
		return retVal;
	}

	/**
	 * Gets the custom report in XML format from the PDB Custom Report RESTful
	 * web service.
	 * 
	 * The custom report is returned as a list of list of field values, where
	 * the inner list represents a single report row (one PDB ID), and the outer
	 * list represents all PDB IDs.
	 * 
	 * <P>
	 * WARNING: This method, although robust, can be memory intensive as it uses
	 * the heavy-duty JDOM DocumentBuilder to parse the XML output. Use
	 * getCustomReportXml2 instead.
	 * 
	 * @param pdbIds
	 *            the list of PDB IDs to generate report for
	 * @param selected
	 *            the selected report fields to include in the report
	 * @return the custom report
	 * @throws Exception
	 *             the exception
	 * @deprecated
	 * @see ModelHelperFunctions#getCustomReportXml2(List, List)
	 */
	@Deprecated
	public static List<List<String>> getCustomReportXml(
			final List<String> pdbIds, final List<ReportField> selected)
			throws Exception {
		List<List<String>> retVal = new ArrayList<List<String>>();
		StringBuffer buf = new StringBuffer(Properties.REPORT_LOCATION);
		buf.append(ModelHelperFunctions.getPdbIdUrl(pdbIds));
		buf.append(ModelHelperFunctions.getReportColumnsUrl(selected));
		buf.append(Properties.REPORT_XML_URL);
		URL url = new URL(buf.toString());
		String urlStr = url.toExternalForm();
		PdbConnectorNodeModel.logger.debug("getCustomReport url=" + urlStr);
		PdbConnectorNodeModel.logger.debug("url length=" + urlStr.length());
		InputSource source = new InputSource(RestClient.getResultStream(url));
		source.setEncoding("UTF8");

		// Create non-validating document parser to read incoming XML report.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		// Parse the REST input stream, forcing UTF8 encoding
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(source);
		Element root = doc.getDocumentElement();
		if (root == null) {
			throw new IOException("Null " + Properties.REPORT_XML_ROOT
					+ " node");
		} else if (Properties.REPORT_XML_ROOT != root.getNodeName()) {
			throw new IOException("Invalid " + Properties.REPORT_XML_ROOT
					+ " root (" + root.getNodeName() + ")");
		} else {
			NodeList records = root
					.getElementsByTagName(Properties.REPORT_XML_RECORD);
			int numRecords = records.getLength();
			PdbConnectorNodeModel.logger.debug("xml report has " + numRecords
					+ " records");
			for (int i = 0; i < numRecords; ++i) {
				Element record = (Element) records.item(i);
				NodeList fields = record.getElementsByTagName("*");
				int numFields = fields.getLength();
				List<String> recordVals = new ArrayList<String>();
				for (int j = 0; j < numFields; ++j) {
					Element field = (Element) fields.item(j);
					recordVals.add(field.getTextContent());
				}
				retVal.add(recordVals);
			}
		}
		return retVal;
	}

	/**
	 * Gets the custom report in XML format from the PDB Custom Report RESTful
	 * web service, using a POST call.
	 * 
	 * The custom report is returned as a list of list of field values, where
	 * the inner list represents a single report row (one PDB ID), and the outer
	 * list represents all PDB IDs.
	 * 
	 * <P>
	 * Note: this is the preferred PDB custom report parser at present. It uses
	 * a crude, bespoke parser that assumes:
	 * <OL>
	 * <LI>there is one XML element per line</LI>
	 * <LI>fixed XML element names for root, dataset and record elements (as
	 * defined in PdbConnectorConfig)</LI>
	 * <LI>all other XML elements are field values</LI>
	 * </OL>
	 * 
	 * <P>
	 * WARNING: This code is vulnerable to any change in the PDB Custom Report
	 * output format.
	 * 
	 * @param pdbIds
	 *            the list of PDB IDs to generate report for
	 * @param selected
	 *            the selected report fields to include in the report
	 * @param primaryOnly
	 *            pseudo report field to control &primaryOnly=1 suffix for
	 *            primary citations
	 * @return the custom report
	 * @throws Exception
	 *             the exception
	 * @see {@link #getCustomReportXml2}
	 */
	public static List<List<String>> postCustomReportXml2(List<String> pdbIds,
			List<ReportField> selected, ReportField primaryOnly)
			throws Exception {

		// Sort out the data to POST to the service
		StringBuffer buf = new StringBuffer(
				ModelHelperFunctions.getPdbIdUrl(pdbIds));
		buf.append(ModelHelperFunctions.getReportColumnsUrl(selected));
		buf.append(Properties.REPORT_XML_URL);
		if (primaryOnly != null) {
			buf.append(primaryOnly.getValue());
		}
		String postRequestData = buf.toString();

		// Now get the URL
		URL url = new URL(Properties.REPORT_LOCATION);

		// And now run the webservice call
		List<String> report = new ArrayList<String>();
		InputStream in = RestClient.doPOST(url, postRequestData);
		BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = rd.readLine()) != null) {
			report.add(line);
		}
		rd.close();

		// Now manually parse the xml
		return manuallyParseXML(report);
	}

	/**
	 * Gets the custom report in XML format from the PDB Custom Report RESTful
	 * web service, using a GET call.
	 * 
	 * The custom report is returned as a list of list of field values, where
	 * the inner list represents a single report row (one PDB ID), and the outer
	 * list represents all PDB IDs.
	 * 
	 * <P>
	 * Note: this is the preferred PDB custom report parser at present. It uses
	 * a crude, bespoke parser that assumes:
	 * <OL>
	 * <LI>there is one XML element per line</LI>
	 * <LI>fixed XML element names for root, dataset and record elements (as
	 * defined in PdbConnectorConfig)</LI>
	 * <LI>all other XML elements are field values</LI>
	 * </OL>
	 * 
	 * <P>
	 * WARNING: This code is vulnerable to any change in the PDB Custom Report
	 * output format.
	 * 
	 * @param pdbIds
	 *            the list of PDB IDs to generate report for
	 * @param selected
	 *            the selected report fields to include in the report
	 * @param primaryOnly
	 *            pseudo report field to control &primaryOnly=1 suffix for
	 *            primary citations
	 * @return the custom report
	 * @throws Exception
	 *             the exception
	 * @see {@link #postCustomReportXml2}
	 */
	public static List<List<String>> getCustomReportXml2(
			final List<String> pdbIds, final List<ReportField> selected,
			final ReportField primaryOnly) throws Exception {
		StringBuffer buf = new StringBuffer(Properties.REPORT_LOCATION);
		buf.append(ModelHelperFunctions.getPdbIdUrl(pdbIds));
		buf.append(ModelHelperFunctions.getReportColumnsUrl(selected));
		buf.append(Properties.REPORT_XML_URL);
		if (primaryOnly != null) {
			buf.append(primaryOnly.getValue());
		}
		URL url = new URL(buf.toString());
		String urlStr = url.toExternalForm();
		// PdbConnectorNodeModel.logger.debug("getCustomReport url=" + urlStr);
		PdbConnectorNodeModel.logger.debug("url length=" + urlStr.length());

		List<String> report = RestClient.getResultList(url);

		return manuallyParseXML(report);
	}

	/**
	 * Manually parse the XML returned by either the
	 * {@link #getCustomReportXml2} or {@link #postCustomReportXml2} methods.
	 * See calling function javadoc for caveats.
	 * 
	 * @param report
	 *            A list of the report rows to parse
	 * @return The parsed report rows
	 * @throws IOException
	 */
	private static List<List<String>> manuallyParseXML(List<String> report)
			throws IOException {
		List<List<String>> retVal = new ArrayList<List<String>>();
		// Manual parsing of xml (we assume one element per line)
		final String DATASET_START = "<" + Properties.REPORT_XML_ROOT + ">";
		final String DATASET_END = "</" + Properties.REPORT_XML_ROOT + ">";
		// Check for empty dataset
		final String DATASET_EMPTY = "<" + Properties.REPORT_XML_ROOT + " />";
		final String RECORD_START = "<" + Properties.REPORT_XML_RECORD + ">";
		final String RECORD_END = "</" + Properties.REPORT_XML_RECORD + ">";
		Iterator<String> iter = report.iterator();
		iter.next();// skip first line
		List<String> currentRecord = null;
		boolean isEndOfDataSet = false;
		while (iter.hasNext() && !isEndOfDataSet) {
			String line = iter.next().trim();
			if (line.equals(DATASET_START)) {// new dataset
				retVal.clear();
				currentRecord = null;
			} else if (line.equals(DATASET_END)) {// end of dataset
				isEndOfDataSet = true;
			} else if (line.equals(DATASET_EMPTY)) {// empty dataset
				retVal.clear();
				currentRecord = null;
				isEndOfDataSet = true;
			} else if (line.equals(RECORD_START)) {// new record
				currentRecord = new ArrayList<String>();
				retVal.add(currentRecord);
			} else if (line.equals(RECORD_END)) {// end of record
				currentRecord = null;
			} else if (currentRecord != null) {// Must be a field element
				int i1 = 0;// start of open element name
				int i2 = line.indexOf(">", i1 + 1);// end of open element name
				int i3 = line.lastIndexOf("</");// start of close element name
				if ((i2 > i1) && (i3 > i2)) {
					String value = line.substring(i2 + 1, i3);
					currentRecord.add(value);
				} else {
					throw new IOException(
							"getCustomReportXml2: Unexpected line " + line);
				}
			} else {
				throw new IOException("getCustomReportXml2: Unexpected line "
						+ line);
			}
		}
		return retVal;
	}

	/**
	 * Gets the URL string that represents a list of PDB IDs.
	 * 
	 * @param pdbIds
	 *            the list of PDB IDs
	 * @return the URL string (comma-separated values)
	 */
	public static String getPdbIdUrl(final List<String> pdbIds) {
		StringBuffer buf = new StringBuffer(Properties.REPORT_PDBIDS_URL);
		boolean isFirst = true;
		for (String pdbId : pdbIds) {
			if (!isFirst) {
				buf.append(",");
			}
			buf.append(pdbId);
			isFirst = false;
		}
		return buf.toString();
	}

	/**
	 * Gets the URL string that represents a list of report fields.
	 * 
	 * @param fields
	 *            the list of fields
	 * @return the URL string (comma-separated values)
	 */
	public static String getReportColumnsUrl(final List<ReportField> fields) {
		StringBuffer buf = new StringBuffer(Properties.REPORT_COLUMNS_URL);
		boolean isFirst = true;
		for (ReportField field : fields) {
			if (!isFirst) {
				buf.append(",");
			}
			buf.append(field.getValue());
			isFirst = false;
		}
		return buf.toString();
	}

}
