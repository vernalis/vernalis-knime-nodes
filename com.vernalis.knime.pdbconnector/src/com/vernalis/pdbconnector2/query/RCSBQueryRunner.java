/*******************************************************************************
 * Copyright (c) 2020, 2021 Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector2.query;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.json.JSONCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.rest.JsonPostRunner;
import com.vernalis.rest.PostIOException;
import com.vernalis.knime.misc.ArrayUtils;

/**
 * Class to run an RCSB Query. The class should be instantiated with a query
 * model, and any non-default parameters set via the corresponding setter
 * methods.
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class RCSBQueryRunner {

	/**
	 * An exception thrown during query executions
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.28.0
	 *
	 */
	public static class QueryException extends Exception {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * No argument constructor
		 */
		public QueryException() {
			super();
		}

		/**
		 * Constructor
		 * 
		 * @param message
		 *            Exception Message
		 * @param cause
		 *            Wrapped exception
		 */
		public QueryException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Constructor
		 * 
		 * @param message
		 *            Exception Message
		 */
		public QueryException(String message) {
			super(message);
		}

		/**
		 * Constructor
		 * 
		 * @param cause
		 *            Wrapped exception
		 */
		public QueryException(Throwable cause) {
			super(cause);
		}

	}

	private static final NodeLogger logger =
			NodeLogger.getLogger(RCSBQueryRunner.class);
	private static final String SEARCH_LOCATION_FMT =
			"https://search.rcsb.org/rcsbsearch/v%d/query";
	private static final Calendar SEARCH_API_V2_START;
	static {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2022);
		cal.set(Calendar.MONTH, Calendar.APRIL);
		cal.set(Calendar.DAY_OF_MONTH, 13);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		SEARCH_API_V2_START = cal;
	}
	private static final int[] RETRY_DELAYS =
			new int[] { 1, 2, 5, 10, 30, 60, 120, 300, 600 };
	private final QueryModel model;
	private ScoringType scoringType = ScoringType.getDefault();
	private QueryResultType queryResultType = QueryResultType.getDefault();
	private int pageSize = 10;
	private boolean includeJson = false;
	private Integer maxRowsToReturn = null;
	private boolean includeHitCount = false;
	private boolean verboseOutput = true;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the {@link QueryModel} containing the query to execute
	 *            
	 * @throws QueryException
	 *             If the model has an invalid component
	 */
	public RCSBQueryRunner(QueryModel model) throws QueryException {
		Objects.requireNonNull(model);
		if (model.hasInvalidQuery()) {
			throw new QueryException(
					"Supplied model has invalid queries - check in query builder!");
		}
		this.model = model;

	}

	/**
	 * @param scoringType
	 *            the scoring type to use
	 */
	public final void setScoringType(ScoringType scoringType) {
		this.scoringType = scoringType;
	}

	/**
	 * @param queryResultType
	 *            The result type to return
	 */
	public final void setQueryResultType(QueryResultType queryResultType) {
		this.queryResultType = queryResultType;
	}

	/**
	 * @param pageSize
	 *            The page size to use - larger values will use more memory but
	 *            fewer calls to the webservice
	 */
	public final void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @param includeJson
	 *            Should the json for the query return value be included in the
	 *            output table?
	 */
	public final void setIncludeJson(boolean includeJson) {
		this.includeJson = includeJson;
	}

	/**
	 * Method to set an optional hit limit
	 * 
	 * @param hitLimit
	 *            The maximum number of hits to return
	 * 
	 * @since 1.28.3
	 */
	public final void setReturnedHitsLimit(int hitLimit) {
		this.maxRowsToReturn = hitLimit;
	}

	public final void setVerboseOutput(boolean verboseOutput) {
		this.verboseOutput = verboseOutput;
	}

	/**
	 * Method to clear an optional hit limit
	 * 
	 * @since 1.28.3
	 */
	public final void clearReturnedHitsLimit() {
		this.maxRowsToReturn = null;
	}

	/**
	 * Method to set the includeHitCount setting
	 * 
	 * @param includeHitCount
	 *            The value to set
	 *
	 * @since 1.30.2
	 */
	public final void setIncludeHitCount(boolean includeHitCount) {
		this.includeHitCount = includeHitCount;
	}

	/**
	 * Method to create the output table spec based on the current settings
	 * 
	 * @return The output table spec based on the current state of the runner
	 *
	 * @since 1.30.2
	 */
	public final DataTableSpec getOutputTableSpec() {
		if (model instanceof MultiRCSBQueryModel) {
			DataTableSpec modelSpec =
					((MultiRCSBQueryModel) model).getResultTableSpec();
			DataTableSpecCreator specFact = null;
			if (includeJson) {
				specFact = new DataTableSpecCreator(modelSpec)
						.addColumns(new DataColumnSpecCreator("Raw Json",
								JSONCellFactory.TYPE).createSpec());
			}
			if (includeHitCount) {
				if (specFact == null) {
					specFact = new DataTableSpecCreator(modelSpec);
				}
				specFact.addColumns(
						new DataColumnSpecCreator("Hit Count", LongCell.TYPE)
								.createSpec());
			}
			if (specFact == null) {
				return modelSpec;
			}
			return specFact.createSpec();
		}
		return null;
	}

	/**
	 * @return The number of hits for the query
	 * 
	 * @throws QueryException
	 *             If there was an error running the query
	 * @throws CanceledExecutionException
	 *             (cannot be thrown by this method)
	 */
	public int getHitCount() throws QueryException, CanceledExecutionException {

		final JsonNode q = model.getCountQuery(scoringType, queryResultType);
		final JsonNode r = runQuery(q);
		return r == null ? 0 : r.get("total_count").asInt();
	}

	/**
	 * @return the current search api version
	 *
	 * @since 04-Apr-2022
	 */
	public static int getQueryAPIVersion() {
		return Calendar.getInstance().compareTo(SEARCH_API_V2_START) < 0 ? 1
				: 2;
	}

	/**
	 * @return the key for pagination in the JSON object
	 *
	 * @since 04-Apr-2022
	 */
	public static String getPaginationKey() {
		return getQueryAPIVersion() < 2 ? "pager" : "paginate";
	}

	/**
	 * @return the search location
	 *
	 * @since 04-Apr-2022
	 */
	public static String getSearchLocation() {
		return String.format(SEARCH_LOCATION_FMT, getQueryAPIVersion());
	}

	/**
	 * Method to run the query, adding the resulting rows to the an output table
	 * 
	 * @param bdc
	 *            The {@link BufferedDataContainer} to add result rows to
	 * @param exec
	 *            The {@link ExecutionContext} to allow cancelling and progress
	 *            reporting. Maybe {@code null}
	 * 
	 * @return {@code true} if all hits were returned to the table, or
	 *         {@code false} if the output was truncated (Since 1.28.3)
	 * 
	 * @throws QueryException
	 *             If there was an error running the query
	 * @throws CanceledExecutionException
	 *             If the user cancelled
	 */
	public boolean runQueryToTable(BufferedDataContainer bdc,
			ExecutionContext exec)
			throws QueryException, CanceledExecutionException {
		// query_id appears to be retained for each page in web UI
		final ObjectNode q = model.getQuery(false, scoringType, queryResultType,
				pageSize, verboseOutput);
		int pageStart = 0;
		long hits = -1;
		double progPerRow = 0;
		long rowCnt = 0;
		while (hits < 0 || pageStart < hits) {
			q.with("request_options").with(getPaginationKey()).put("start",
					pageStart);
			final JsonNode r = runQuery(q, exec);
			if (r == null) {
				// response 204 - No Hits returned
				if (includeHitCount) {
					writeHitCountToEmptyTable(bdc, r);
				}
				break;
			}
			if (hits < 0) {
				hits = r.get("total_count").asInt();
				progPerRow = 1.0 / (maxRowsToReturn != null
						? Math.min(maxRowsToReturn, hits)
						: hits);
			}
			if (hits == 0 && includeHitCount) {
				// Special case - we need to add a row containing only the hit
				// count
				return writeHitCountToEmptyTable(bdc, r);
			}

			pageStart += pageSize;
			final Iterator<JsonNode> iter = r.get("result_set").iterator();
			while (iter.hasNext()) {
				final JsonNode rCells = iter.next();
				DataRow row = new DefaultRow(RowKey.createRowKey(rowCnt),
						new DataCell[] {
								new StringCell(
										rCells.get("identifier").asText()),
								new DoubleCell(
										rCells.get("score").asDouble()) });

				if (includeJson) {
					DataCell jsonCell = DataType.getMissingCell();
					try {
						jsonCell = JSONCellFactory.create(
								new ObjectMapper().writeValueAsString(rCells),
								true);
					} catch (IOException e) {
						logger.warn("Error generating json cell for json '"
								+ rCells.toString() + "'");
					}
					row = new AppendedColumnRow(row, jsonCell);
				}

				if (includeHitCount) {
					DataCell countCell = new LongCell(hits);
					row = new AppendedColumnRow(row, countCell);
				}

				bdc.addRowToTable(row);
				if (maxRowsToReturn != null
						&& rowCnt == maxRowsToReturn.intValue() - 1) {
					return rowCnt >= (hits - 1);
				}
				rowCnt++;
				if (rowCnt % 10 == 0) {
					exec.checkCanceled();
					exec.setProgress(rowCnt * progPerRow,
							"Processed " + rowCnt + " of "
									+ (maxRowsToReturn != null
											? Math.min(maxRowsToReturn, hits)
											: hits)
									+ " results");
				}
			}
		}
		return true;
	}

	/**
	 * @param bdc
	 *            DataContainer to add the row to
	 * @param r
	 *            The {@link JsonNode} containing the service result (may be
	 *            {@code null})
	 * 
	 * @return whether the full hitset was added to the table
     * @since 1.30.2
	 */
	private boolean writeHitCountToEmptyTable(BufferedDataContainer bdc,
			final JsonNode r) {
		DataCell[] row = ArrayUtils.fill(
				new DataCell[bdc.getTableSpec().getNumColumns()],
				DataType.getMissingCell());
		if (includeJson && r != null) {
			try {
				row[row.length - 2] = JSONCellFactory
						.create(new ObjectMapper().writeValueAsString(r), true);
			} catch (IOException e) {
				logger.warn("Error generating json cell for json '"
						+ r.toString() + "'");
			}
		}
		row[row.length - 1] = new LongCell(0);
		bdc.addRowToTable(new DefaultRow(RowKey.createRowKey(0L), row));
		return true;
	}

	private JsonNode runQuery(JsonNode q)
			throws QueryException, CanceledExecutionException {
		if (model.hasInvalidQuery()) {
			logger.error(
					"Invalid query item present in model - check query in builder!");
			throw new QueryException(
					"Invalide query present in model - check query in builder!");
		}
		return runQuery(q, null);
	}

	/**
	 * Static method to actually run a JSON query
	 * 
	 * @param query
	 *            The query
	 * @param exec
	 *            The {@link ExecutionContext} to allow cancellation. Maybe
	 *            {@code null}
	 * 
	 * @return The query result JSON
	 * 
	 * @throws QueryException
	 *             If there was an error running the query
	 * @throws CanceledExecutionException
	 *             If the user cancelled
	 */
	public static JsonNode runQuery(JsonNode query, ExecutionContext exec)
			throws QueryException, CanceledExecutionException {
		String searchLocation = getSearchLocation();
		try {
			logger.infoWithFormat("Sending query:%n %s%nto '%s",
					new ObjectMapper().writerWithDefaultPrettyPrinter()
							.writeValueAsString(query),
					searchLocation);
		} catch (final JsonProcessingException e1) {
			// Ignore
		}
		QueryException lastException = null;
		for (int delay : RETRY_DELAYS) {
			try {
				final URL url = new URL(searchLocation);
				// Now send the request in a separate thread, waiting for it to
				// complete
				final ExecutorService pool =
						Executors.newSingleThreadExecutor();
				final Future<JsonNode> future =
						pool.submit(new JsonPostRunner(url, query));
				while (!future.isDone()) {
					// wait a 0.1 seconds
					final long time = System.nanoTime();
					while (System.nanoTime() - time < 100000) {
						// Wait
					}
					if (exec != null) {
						try {
							exec.checkCanceled();
						} catch (final CanceledExecutionException e) {
							future.cancel(true);
							while (!future.isCancelled()) {
								// Wait for the cancel to happen
							}
							throw e;
						}
					}
				}
				final JsonNode retVal = future.get();
				if (retVal != null && !query.get("request_info").get("query_id")
						.asText().equals(retVal.get("query_id").asText())) {
					lastException = new QueryException(
							"Error in results - submitted and returned query IDs do not match!");
				} else {
					return retVal;
				}
			} catch (ExecutionException e) {
				lastException = new QueryException(e);
				Throwable cause = e.getCause();
				if (cause instanceof PostIOException) {
					// Handle fatal response codes here
					PostIOException pioe = (PostIOException) cause;
					if (pioe.getResponseCode() >= 400
							&& pioe.getResponseCode() < 500) {
						// Broken query - no point retrying!
						if (exec != null) {
							exec.setMessage(
									"Error retrieving results - broken query!");
						}
						logger.warn("Error retrieving results - broken query!");
						break;
					}
				}
			} catch (IOException | InterruptedException e) {
				lastException = new QueryException(e);
			}
			if (exec != null) {
				exec.setMessage("Error retrieving results - retrying in "
						+ delay + " seconds...");
			}
			logger.info("Error retrieving results - retrying in " + delay
					+ " seconds...");
			pause(delay, exec);
		}
		throw lastException;
	}

	private static void pause(int seconds, ExecutionMonitor exec)
			throws CanceledExecutionException {
		// simple delay function without using threads
		Date start = new Date();
		while (new Date().getTime() - start.getTime() < seconds * 1000) {
			exec.checkCanceled();
		}
	}
}
