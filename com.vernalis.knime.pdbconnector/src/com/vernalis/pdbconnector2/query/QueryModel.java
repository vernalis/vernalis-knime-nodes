/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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

import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This interface defines a settings model for a PDB query. It provides default
 * implementations for many methods. Implementations may represent either a
 * single node in a query, or a logical grouping of multiple query components
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public interface QueryModel {

	/**
	 * Method to build a JSON query from the object
	 * 
	 * @param isCount
	 *            whether the query should return counts or results
	 * @param scoringType
	 *            the type of scoring to use when running the query
	 * @param resultType
	 *            the type of results to return
	 * @param pageSize
	 *            The number of results to return in each paged query
	 * @return The query JSON
	 */
	public default ObjectNode getQuery(boolean isCount, ScoringType scoringType,
			QueryResultType resultType, int pageSize) {

		final ObjectNode retVal = new ObjectMapper().createObjectNode();

		retVal.putObject("request_info").put("query_id", getUniqueQueryID())
				.put("src", "ui");

		final ObjectNode reqOpt = retVal.putObject("request_options")
				.put("return_counts", isCount);

		if (!isCount) {
			reqOpt.putArray("sort").addObject().put("sort_by", "score")
					.put("direction", "desc");
			reqOpt.putObject("pager").put("rows", pageSize).put("start", 0);
			reqOpt.put("scoring_strategy", scoringType.getActionCommand());
		}

		retVal.put("return_type", resultType.getActionCommand());

		final AtomicInteger nodeId = new AtomicInteger();
		retVal.set("query", getQueryNodes(nodeId));
		return retVal;

	}

	/**
	 * An overloaded method to build a query, with a default result page size of
	 * 10 hits
	 * 
	 * @param isCount
	 *            whether the query should return counts or results
	 * @param scoringType
	 *            the type of scoring to use when running the query
	 * @param resultType
	 *            the type of results to return
	 * @return The query JSON
	 * @see #getQuery(boolean, ScoringType, QueryResultType, int)
	 */
	public default ObjectNode getQuery(boolean isCount, ScoringType scoringType,
			QueryResultType resultType) {
		return getQuery(isCount, scoringType, resultType, 10);

	}

	/**
	 * Overload method to retrieve the count query, which returns the number of
	 * hits rather than the actual hits
	 * 
	 * @param scoringType
	 *            the type of scoring to use when running the query
	 * @param resultType
	 *            the type of results to return
	 * @return The query JSON
	 * @see #getQuery(boolean, ScoringType, QueryResultType)
	 */
	public default ObjectNode getCountQuery(ScoringType scoringType,
			QueryResultType resultType) {
		return getQuery(true, scoringType, resultType);
	}

	/**
	 * Method to get the actual query nodes for the query
	 * 
	 * @param nodeId
	 *            The incremental node id counter
	 * @return The JSON tree of query nodes for the current query
	 */
	public JsonNode getQueryNodes(AtomicInteger nodeId);

	/**
	 * @return whether the current query object actually contains a query
	 */
	boolean hasQuery();

	/**
	 * Clear any stored query back to its default state
	 */
	void clearQuery();

	/**
	 * Method to register a change listener to the model
	 * 
	 * @param l
	 *            The listener
	 */
	public default void addChangeListener(ChangeListener l) {
		if (!getChangeListeners().contains(l)) {
			getChangeListeners().add(l);
		}
	}

	/**
	 * Method to remove a registered change listener
	 * 
	 * @param l
	 *            The listener
	 */
	public default void removeChangeListener(ChangeListener l) {
		getChangeListeners().remove(l);
	}

	/**
	 * @return The registered change listeners. If the return value is
	 *         immutable, e.g. as an unmodifiable List implementation, the
	 *         default implementations of
	 *         {@link #addChangeListener(ChangeListener)} and
	 *         {@link #removeChangeListener(ChangeListener)} need to be
	 *         overridden
	 */
	public List<ChangeListener> getChangeListeners();

	/**
	 * Method to save the query settings
	 * 
	 * @param settings
	 *            The settings object to save to
	 */
	void saveSettingsTo(NodeSettingsWO settings);

	/**
	 * Method to load the settings
	 * 
	 * @param settings
	 *            The settings object to load from
	 * @throws InvalidSettingsException
	 *             if there was an error reading or parsing the stored settings
	 *             into the query
	 */
	void loadSettings(NodeSettingsRO settings) throws InvalidSettingsException;

	/**
	 * Method to ensure the settings are valid prior to loading. <i>Ideally</i>
	 * implementations will validate all settings, and throw an
	 * {@link InvalidSettingsException} highlighting all errors
	 * 
	 * @param settings
	 *            The settings object to validate
	 * @throws InvalidSettingsException
	 *             If there was an error or errors in the settings
	 */
	void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException;

	/**
	 * This method generates a unique query ID. It is based on the KNIME
	 * instance ID and current system time, all Base64 encoded
	 *
	 * @return A unique query ID
	 */
	public default String getUniqueQueryID() {
		final StringBuilder sb =
				new StringBuilder(KNIMEConstants.getKNIMEInstanceID());
		sb.append('-');
		sb.append(System.currentTimeMillis());
		return Base64.getEncoder().encodeToString(sb.toString().getBytes());
	}

	/**
	 * @return Whether the query has any invalid components which would render
	 *         it non-executable
	 */
	public default boolean hasInvalidQuery() {
		return false;
	}

	/**
	 * Method to check whether the query supports the given scoring type
	 * 
	 * @param scoringType
	 *            The scoring type in question
	 * @return {@code true} if the query supports the scoring type TODO: Provide
	 *         default
	 */
	public boolean isScoringTypeValid(ScoringType scoringType);
}