/*******************************************************************************
 * Copyright (c) 2016, 2020 Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
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
package com.vernalis.pdbconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;

import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.ReportField2;
import com.vernalis.pdbconnector.config.ReportOverflowException;
import com.vernalis.pdbconnector.config.XmlDataParsingException;
import com.vernalis.rest.GetRunner;
import com.vernalis.rest.PostRunner;
import com.vernalis.rest.RestClient;

/**
 * Static helper functions for PdbConnectorNodeModel.
 * 
 * <p>
 * SDR 07-Jul-2016 - Added Support for ListCells in
 * {@link #getDataCell(ReportField2, String, String)} and
 * {@link #getDataType(ReportField2)}
 * </p>
 * 
 * <p>
 * SDR 14-Jul-2016 - Added improved handling of 'null' query returns, and
 * cancellation checks. Added direct return of query results to data table and
 * removed deprecated methods which remain in the deprecated
 * {@link ModelHelperFunctions} class
 * </p>
 * <p>
 * SDR 15-Jul-2016 - Moved all service requests (GET & POST) to separate Threads
 * to enable cancellation during requests
 * </p>
 * 
 * @see PdbConnectorNodeModel2
 */
public class ModelHelperFunctions2 {

	/** DateFormat for yyyy-MM-dd. */
	private static final DateFormat ymdFormat =
			new SimpleDateFormat(Properties.YMD_FORMAT);

	/** DateFormat for yyyy-MM. */
	private static final DateFormat ymFormat =
			new SimpleDateFormat(Properties.YM_FORMAT);

	/** DateFormat for yyyy. */
	private static final DateFormat yearFormat =
			new SimpleDateFormat(Properties.YEAR_FORMAT);

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
		List<QueryOptionModel> simModels = new ArrayList<>();
		simModels.add(simModel);
		// inner query string is for the selected queries themselves.
		// outer query string is combined with similarity query (conjunction is
		// always AND).
		return ModelHelperFunctions2.getXmlQuery(
				ModelHelperFunctions2.getXmlQuery("", queryModels, conjunction),
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
			final List<QueryOptionModel> queryModels,
			final String conjunction) {
		StringBuffer retVal = new StringBuffer();
		List<String> queries = new ArrayList<>();
		// Pre-seed with initial query string (if defined)
		if (!initialQuery.isEmpty()) {
			queries.add(initialQuery);
		}
		// Get list of all selected query models
		List<QueryOptionModel> selected = new ArrayList<>();
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
	 * Method to combine two queries with a specified conjunction (AND or OR).
	 * No validation of queries or conjunction is undertaken
	 * 
	 * @param query0
	 *            The first query
	 * @param query1
	 *            The second query
	 * @param conjunction
	 *            AND / OR (NB Needs to be in the full
	 *            {@link Properties#CONJUNCTION_AND} or
	 *            {@link Properties#CONJUNCTION_OR} form)
	 * @return The combined query
	 */
	public static String combineQueries(String query0, String query1,
			String conjunction) {
		StringBuffer retVal = new StringBuffer();
		retVal.append(Properties.COMPOSITE_START);
		retVal.append(Properties.REFINEMENT_START);
		retVal.append(Properties.LEVEL_START);
		retVal.append(0);
		retVal.append(Properties.LEVEL_END);
		retVal.append(query0);
		retVal.append(Properties.REFINEMENT_END);
		retVal.append(Properties.REFINEMENT_START);
		retVal.append(Properties.LEVEL_START);
		retVal.append(1);
		retVal.append(Properties.LEVEL_END);
		retVal.append(conjunction);
		retVal.append(query1);
		retVal.append(Properties.REFINEMENT_END);
		retVal.append(Properties.COMPOSITE_END);
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
	public static DataType getDataType(final ReportField2 field) {
		DataType type;
		switch (field.getType()) {
		case STRING:
			type = StringCell.TYPE;
			break;
		case INTEGER:
			type = IntCell.TYPE;
			break;
		case DOUBLE:
			type = DoubleCell.TYPE;
			break;
		case DATE:
			type = DateAndTimeCell.TYPE;
			break;
		case SMILES:
			type = SmilesCell.TYPE;
			break;
		case PNG_URL:
			type = StringCell.TYPE;
			break;
		default:
			type = StringCell.TYPE;
		}
		if (field.isList()) {
			return ListCell.getCollectionType(type);
		} else {
			return type;
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
	 * @param structureID
	 * @return the KNIME data cell
	 * @throws IOException
	 *             If there was an error with the returned data which cannot be
	 *             recovered
	 * @throws XmlDataParsingException
	 *             If there is an error converting the data to the returned type
	 * @throws Exception
	 *             if field value is invalid
	 */
	public static DataCell getDataCell(final ReportField2 field,
			final String fieldValue, final String urlSuffix)
			throws IOException, XmlDataParsingException {

		XmlDataParsingException exception = new XmlDataParsingException();

		if ((field == null) || (fieldValue == null) || fieldValue.isEmpty()
				|| fieldValue.equalsIgnoreCase("null")) {// check for missing
															// data
			return DataType.getMissingCell();
		} else if (field.isList()) {
			// Handle collection cells
			String[] fieldValues = fieldValue.split(field.getListDeliminator());
			DataCell[] cells = new DataCell[fieldValues.length];
			for (int i = 0; i < fieldValues.length; i++) {
				if (fieldValues[i] == null || fieldValues[i].isEmpty()) {
					cells[i] = DataType.getMissingCell();
				} else {
					switch (field.getType()) {
					case STRING:
						cells[i] = new StringCell(fieldValues[i]);
						break;
					case INTEGER:
						try {
							cells[i] = new IntCell(
									Integer.parseInt(fieldValues[i]));
						} catch (NumberFormatException e) {
							exception.addWarning("Invalid integer value ("
									+ fieldValues[i] + ") in field: "
									+ field.getColName());
							cells[i] = DataType.getMissingCell();
						}
						break;
					case DOUBLE:
						try {
							cells[i] = new DoubleCell(
									Double.parseDouble(fieldValues[i]));
						} catch (NumberFormatException e) {
							exception.addWarning("Invalid double value ("
									+ fieldValues[i] + ") in field: "
									+ field.getColName());
							cells[i] = DataType.getMissingCell();
						}
						break;
					case DATE:
						try {
							cal.setTime(ymdFormat.parse(fieldValues[i]));
							cells[i] =
									new DateAndTimeCell(cal.get(Calendar.YEAR),
											cal.get(Calendar.MONTH),
											cal.get(Calendar.DAY_OF_MONTH));
						} catch (ParseException e) {
							exception.addInfo(
									"Trying simpler " + Properties.YM_FORMAT
											+ " format in field: "
											+ field.getColName());
							try {// try year-month format
								cal.setTime(ymFormat.parse(fieldValues[i]));
								cells[i] = new DateAndTimeCell(
										cal.get(Calendar.YEAR),
										cal.get(Calendar.MONTH), 1);// force to
																	// first
																	// day of
																	// month
							} catch (ParseException e2) {
								exception.addInfo("Trying simpler "
										+ Properties.YEAR_FORMAT
										+ " format in field: "
										+ field.getColName());
								try {// try year-only format
									cal.setTime(
											yearFormat.parse(fieldValues[i]));
									cells[i] = new DateAndTimeCell(
											cal.get(Calendar.YEAR), 1, 1);// force
																			// to
																			// first
																			// of
																			// January
								} catch (ParseException e3) {
									exception.addWarning("Invalid date value ("
											+ fieldValues[i] + ") in field: "
											+ field.getColName());
									cells[i] = DataType.getMissingCell();
								}
							}
						}
						break;
					case SMILES:
						cells[i] = SmilesCellFactory.create(fieldValues[i]);
						break;
					case PNG_URL:
						// Concatenate the field value between url prefix and
						// suffix to
						// generate
						// the full image URL
						cells[i] = new StringCell(Properties.LIGAND_IMG_LOCATION
								+ fieldValues[i] + urlSuffix);
						break;
					default:
						throw new IOException("Invalid data type at field="
								+ field.getColName() + ": value="
								+ fieldValues[i]);
					}
				}
			}
			if (exception.hasMessages()) {
				exception.setCell(CollectionCellFactory
						.createListCell(Arrays.asList(cells)));
				throw exception;
			}
			return CollectionCellFactory.createListCell(Arrays.asList(cells));
		} else {
			DataCell retVal;
			switch (field.getType()) {
			case STRING:
				retVal = new StringCell(fieldValue);
				break;
			case INTEGER:
				try {
					retVal = new IntCell(Integer.parseInt(fieldValue));
				} catch (NumberFormatException e) {
					exception.addWarning("Invalid integer value (" + fieldValue
							+ ") in field: " + field.getColName());
					retVal = DataType.getMissingCell();
				}
				break;
			case DOUBLE:
				try {
					retVal = new DoubleCell(Double.parseDouble(fieldValue));
				} catch (NumberFormatException e) {
					exception.addWarning("Invalid double value (" + fieldValue
							+ ") in field: " + field.getColName());
					retVal = DataType.getMissingCell();
				}
				break;
			case DATE:
				try {
					cal.setTime(ymdFormat.parse(fieldValue));
					retVal = new DateAndTimeCell(cal.get(Calendar.YEAR),
							cal.get(Calendar.MONTH),
							cal.get(Calendar.DAY_OF_MONTH));
				} catch (ParseException e) {
					exception.addInfo("Trying simpler " + Properties.YM_FORMAT
							+ " format in field: " + field.getColName());
					try {// try year-month format
						cal.setTime(ymFormat.parse(fieldValue));
						retVal = new DateAndTimeCell(cal.get(Calendar.YEAR),
								cal.get(Calendar.MONTH), 1);// force to first
															// day of month
					} catch (ParseException e2) {
						exception.addInfo("Trying simpler "
								+ Properties.YEAR_FORMAT + " format in field: "
								+ field.getColName());
						try {// try year-only format
							cal.setTime(yearFormat.parse(fieldValue));
							retVal = new DateAndTimeCell(cal.get(Calendar.YEAR),
									1, 1);// force
											// to
											// first
											// of
											// January
						} catch (ParseException e3) {
							exception.addWarning("Invalid date value ("
									+ fieldValue + ") in field: "
									+ field.getColName());
							retVal = DataType.getMissingCell();
						}
					}
				}
				break;
			case SMILES:
				retVal = SmilesCellFactory.create(fieldValue);
				break;
			case PNG_URL:
				// Concatenate the field value between url prefix and suffix to
				// generate
				// the full image URL
				retVal = new StringCell(Properties.LIGAND_IMG_LOCATION
						+ fieldValue + urlSuffix);
				break;
			default:
				throw new IOException("Invalid data type at field="
						+ field.getColName() + ": value=" + fieldValue);
			}
			if (exception.hasMessages()) {
				exception.setCell(retVal);
				throw exception;
			}
			return retVal;
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
	 * @see #postQueryToTable(String, BufferedDataContainer, ExecutionMonitor)
	 * @deprecated Only used by test query button. Other implementations should
	 *             use the more memory-efficient
	 *             {@link ModelHelperFunctions2#postQueryToTable(String, BufferedDataContainer, ExecutionMonitor)}
	 *             method
	 */
	@Deprecated
	public static List<String> postQuery(final String xml) throws IOException {
		List<String> retVal = new ArrayList<>();
		URL url = new URL(Properties.SEARCH_LOCATION);
		String encodedXML = URLEncoder.encode(xml, "UTF-8");
		InputStream in = RestClient.doPOST(url, encodedXML);
		BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = rd.readLine()) != null) {
			// if (line.length() <= 4) { // skip the query id string
			if ("null".equals(line)) {
				// returned by some query types with no hits
				break;
			}
			if (line.matches("[A-Za-z0-9]{4}:[\\d]*")) {
				retVal.add(line.split(":")[0]);
			} else if (line.matches("[A-Za-z0-9]{4}")) {
				retVal.add(line);
			}
		}
		rd.close();
		return retVal;
	}

	/**
	 * Posts an XML query string to the PDB Advanced Search RESTful web service.
	 * <p>
	 * This method adds the result directly to a buffered data container to
	 * avoid potential storage issues for long queries
	 * 
	 * @param xml
	 *            The XML Query to post
	 * @param bdc
	 *            The data container to add the output to
	 * @param exec
	 *            The execution context
	 * @return The number of hit rows added to the table
	 * @throws IOException
	 *             If there was a problem accessing the server
	 * @throws CanceledExecutionException
	 *             If the use cancels the query
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public static long postQueryToTable(String xml, BufferedDataContainer bdc,
			ExecutionMonitor exec)
			throws IOException, CanceledExecutionException,
			InterruptedException, ExecutionException {
		URL url = new URL(Properties.SEARCH_LOCATION);
		String encodedXML = URLEncoder.encode(xml, "UTF-8");
		// Now send the request in a separate thread, waiting for it to complete
		ExecutorService pool = Executors.newSingleThreadExecutor();
		Future<InputStream> future =
				pool.submit(new PostRunner(url, encodedXML));
		while (!future.isDone()) {
			// wait a 0.1 seconds
			long time = System.nanoTime();
			while (System.nanoTime() - time < 100000) {
				// Wait
			}
			try {
				exec.checkCanceled();
			} catch (CanceledExecutionException e) {
				future.cancel(true);
				while (!future.isCancelled()) {
					// Wait for the cancel to happen
				}
				throw e;
			}
		}

		BufferedReader rd =
				new BufferedReader(new InputStreamReader(future.get()));
		String line;
		long hitCount = 0;
		while ((line = rd.readLine()) != null) {
			// if (line.length() <= 4) { // skip the query id string
			if ("null".equals(line)) {
				// Some query types return 'null' when there are no hits
				break;
			}
			if (line.matches("[A-Za-z0-9]{4}:[\\d]*")) {
				RowKey key = RowKey.createRowKey(hitCount++);
				bdc.addRowToTable(new DefaultRow(key,
						new StringCell(line.split(":")[0])));
				exec.setProgress("Added " + hitCount + " results to table");
			} else if (line.matches("[A-Za-z0-9]{4}")) {
				RowKey key = RowKey.createRowKey(hitCount++);
				bdc.addRowToTable(new DefaultRow(key, new StringCell(line)));
				exec.setProgress("Added " + hitCount + " results to table");
			}
			try {
				exec.checkCanceled();
			} catch (CanceledExecutionException e) {
				rd.close();
				throw e;
			}
		}
		rd.close();
		return hitCount;
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
	 * @param exec
	 * @return the custom report
	 * @throws Exception
	 *             the exception
	 * @see {@link #getCustomReportXml2}
	 */
	public static Map<String, List<List<String>>> postCustomReportXml3(
			List<String> pdbIds, List<ReportField2> selected,
			ReportField2 primaryOnly, ExecutionMonitor exec) throws Exception {

		// Sort out the data to POST to the service
		StringBuffer buf =
				new StringBuffer(ModelHelperFunctions2.getPdbIdUrl(pdbIds));
		buf.append(ModelHelperFunctions2.getReportColumnsUrl(selected));
		buf.append(Properties.REPORT_XML_URL);
		if (primaryOnly != null) {
			buf.append(primaryOnly.getValue());
		}
		String postRequestData = buf.toString();

		// Now get the URL
		URL url = new URL(Properties.REPORT_LOCATION);

		// And now run the webservice call
		List<String> report = new ArrayList<>();

		// Now send the request in a separate thread, waiting for it to complete
		ExecutorService pool = Executors.newSingleThreadExecutor();
		Future<InputStream> future =
				pool.submit(new PostRunner(url, postRequestData));
		while (!future.isDone()) {
			// wait a 0.1 seconds
			long time = System.nanoTime();
			while (System.nanoTime() - time < 100000) {
				// Wait
			}
			try {
				exec.checkCanceled();
			} catch (CanceledExecutionException e) {
				future.cancel(true);
				while (!future.isCancelled()) {
					// Wait for the cancel to happen
				}
				throw e;
			}
		}

		BufferedReader rd =
				new BufferedReader(new InputStreamReader(future.get()));
		String line;
		while ((line = rd.readLine()) != null) {
			if (line.equals(
					"** Report is too big, please reduce the number of fields or pdb ids.")) {
				throw new ReportOverflowException(
						"** Report is too big, please reduce the number of fields or pdb ids.");
			}
			report.add(line);
			exec.checkCanceled();
		}
		rd.close();

		// Now manually parse the xml
		return manuallyParseXMLToMap(report, exec);
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
	 * @param exec
	 * @return the custom report
	 * @throws Exception
	 *             the exception
	 * @see {@link #postCustomReportXml2}
	 */
	public static Map<String, List<List<String>>> getCustomReportXml3(
			final List<String> pdbIds, final List<ReportField2> selected,
			final ReportField2 primaryOnly, ExecutionMonitor exec)
			throws Exception {
		StringBuffer buf = new StringBuffer(Properties.REPORT_LOCATION);
		buf.append(ModelHelperFunctions2.getPdbIdUrl(pdbIds));
		buf.append(ModelHelperFunctions2.getReportColumnsUrl(selected));
		buf.append(Properties.REPORT_XML_URL);
		if (primaryOnly != null) {
			buf.append(primaryOnly.getValue());
		}
		URL url = new URL(buf.toString());
		String urlStr = url.toExternalForm();
		NodeLogger.getLogger("PDB Connector Helper Functions")
				.debug("url length=" + urlStr.length());
		// Now send the request in a separate thread, waiting for it to complete
		ExecutorService pool = Executors.newSingleThreadExecutor();
		Future<List<String>> future = pool.submit(new GetRunner(url));
		while (!future.isDone()) {
			// wait a 0.1 seconds
			long time = System.nanoTime();
			while (System.nanoTime() - time < 100000) {
				// Wait
			}
			try {
				exec.checkCanceled();
			} catch (CanceledExecutionException e) {
				future.cancel(true);
				while (!future.isCancelled()) {
					// Wait for the cancel to happen
				}
				throw e;
			}
		}
		return manuallyParseXMLToMap(future.get(), exec);
	}

	/**
	 * Parses the report to a Map, keyed on the structure ID
	 * 
	 * @param report
	 *            The report object
	 * @param exec
	 * @throws IOException
	 * @throws CanceledExecutionException
	 */
	private static Map<String, List<List<String>>> manuallyParseXMLToMap(
			List<String> report, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		Map<String, List<List<String>>> retVal = new HashMap<>();
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
			exec.checkCanceled();
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
				currentRecord = new ArrayList<>();
				// retVal.add(currentRecord);
			} else if (line.equals(RECORD_END)) {// end of record
				String id = currentRecord.get(0);
				if (!retVal.containsKey(id)) {
					retVal.put(id, new ArrayList<List<String>>());
				}
				retVal.get(id).add(currentRecord);// First entry
													// is ID
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
				throw new IOException(
						"getCustomReportXml2: Unexpected line " + line);
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
	public static String getReportColumnsUrl(final List<ReportField2> fields) {
		StringBuffer buf = new StringBuffer(Properties.REPORT_COLUMNS_URL);
		boolean isFirst = true;
		for (ReportField2 field : fields) {
			if (!isFirst) {
				buf.append(",");
			}
			buf.append(field.getValue());
			isFirst = false;
		}
		return buf.toString();
	}

}
