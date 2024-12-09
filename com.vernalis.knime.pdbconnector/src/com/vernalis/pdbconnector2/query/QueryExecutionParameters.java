/*******************************************************************************
 * Copyright (c) 2024, Vernalis (R&D) Ltd
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

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Parameter class for parameters to be set at query execution time
 * corresponding to the {@code request_options} and {@code return_type} parts of
 * the query request POST body
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.37.0
 *
 */
public class QueryExecutionParameters {

	private ScoringType scoringType = ScoringType.getDefault();
	private QueryResultType resultType = QueryResultType.getDefault();
	private int pageSize = 10;
	private boolean verboseOutput = true;
	private ResultContentType[] resultContentTypes =
			ResultContentType.getDefaults();

	/**
	 * Constructor, with default initial values
	 */
	public QueryExecutionParameters() {

	}

	/**
	 * Constructor allowing setting of some value during construction.
	 * 
	 * @param scoringType
	 *            the scoring type
	 * @param resultType
	 *            the result type
	 * @param pageSize
	 *            the page size
	 * @param verboseOutput
	 *            output verbosity flag
	 * @deprecated NB This is only provided as a convenience from deprecated
	 *             legacy methods in the
	 *             {@link com.vernalis.internal.pdbconnector2.query.QueryModel}
	 *             interface, and will be removed when those methods are removed
	 */
	@Deprecated(forRemoval = true)
	QueryExecutionParameters(ScoringType scoringType, QueryResultType resultType,
			int pageSize, boolean verboseOutput) {
		this.scoringType = scoringType;
		this.resultType = resultType;
		this.pageSize = pageSize;
		this.verboseOutput = verboseOutput;
	}

	/**
	 * @return the scoring type
	 */
	public ScoringType getScoringType() {
		return scoringType;
	}

	/**
	 * Method to set the scoring type
	 * 
	 * @param scoringType
	 *            the new scoring type
	 * @return this object to allow method daisy-chaining
	 * @throws NullPointerException
	 *             if the supplied argument is {@code null}
	 */
	public QueryExecutionParameters setScoringType(ScoringType scoringType)
			throws NullPointerException {
		this.scoringType = Objects.requireNonNull(scoringType);
		return this;
	}

	/**
	 * @return the query result type
	 */
	public QueryResultType getResultType() {
		return resultType;
	}

	/**
	 * Method to set the result type
	 * 
	 * @param resultType
	 *            the new result type
	 * @return this object to allow method daisy-chaining
	 * @throws NullPointerException
	 *             if the supplied argument is {@code null}
	 */
	public QueryExecutionParameters setResultType(QueryResultType resultType)
			throws NullPointerException {
		this.resultType = Objects.requireNonNull(resultType);
		return this;
	}

	/**
	 * @return the page size
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Method to set the page size
	 * 
	 * @param pageSize
	 *            the new page size
	 * @return this object to allow method daisy-chaining
	 */
	public QueryExecutionParameters setPageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	/**
	 * @return the verbose output flag
	 */
	public boolean isVerboseOutput() {
		return verboseOutput;
	}

	/**
	 * Method to set the verbose output flag
	 * 
	 * @param verboseOutput
	 *            the new value for the verbose output flag
	 * @return this object to allow method daisy-chaining
	 */
	public QueryExecutionParameters setVerboseOutput(boolean verboseOutput) {
		this.verboseOutput = verboseOutput;
		return this;
	}

	/**
	 * @return the result content types. Never {@code null}
	 */
	public ResultContentType[] getResultContentTypes() {
		return resultContentTypes;
	}

	/**
	 * Method to set the result content types
	 * 
	 * @param resultContentTypes
	 *            the new result content types
	 * @return this object to allow method daisy-chaining
	 * @throws NullPointerException
	 *             if the supplied argument is {@code null} or contains any
	 *             {@code null} values
	 */
	public QueryExecutionParameters setResultContentTypes(
			ResultContentType[] resultContentTypes)
			throws NullPointerException {
		if (Arrays.stream(Objects.requireNonNull(resultContentTypes))
				.anyMatch(Objects::isNull)) {
			throw new NullPointerException();
		}
		this.resultContentTypes = resultContentTypes;
		return this;
	}

	/**
	 * Method to insert the values into the query node
	 * 
	 * @param queryNode
	 *            the query node, supplied from the calling method
	 *            {@link com.vernalis.internal.pdbconnector2.query.QueryModel#getQuery(boolean, QueryExecutionParameters)}
	 * @param isCount
	 *            whether a counts only query is being built
	 * @return the incoming query node to allow method daisy-chaining
	 */
	public ObjectNode getQuery(ObjectNode queryNode, boolean isCount) {
		ObjectNode requestOptionsNode = queryNode.putObject("request_options");
		requestOptionsNode.put("return_counts", isCount).put(
				"results_verbosity", isVerboseOutput() ? "verbose" : "minimal");


		ArrayNode rctArr =
				requestOptionsNode.putArray("results_content_type");
		Arrays.stream(resultContentTypes).map(rct -> rct.getActionCommand())
				.forEach(rctArr::add);

		if (!isCount) {
			requestOptionsNode.putArray("sort").addObject()
					.put("sort_by", "score").put("direction", "desc");
			requestOptionsNode.putObject(RCSBQueryRunner.getPaginationKey())
					.put("rows", getPageSize()).put("start", 0);
			requestOptionsNode.put("scoring_strategy",
					getScoringType().getActionCommand());
		}

		queryNode.put("return_type", getResultType().getActionCommand());

		return queryNode;
	}
}
