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
package com.vernalis.pdbconnector2.graphql;

import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.rest.JsonPostRunner;

/**
 * Builder class to combine a GraphQL query string along with any variable
 * values into the correct JSON format for the RCSB GraphQL webservice
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class RCSBGraphQLRunnerBuilder {

	private final ObjectNode requestBody;
	private final ObjectNode variableNode;
	private final String url;

	/**
	 * Constructor
	 * 
	 * @param url
	 *            The url of the service to POST the query to
	 * @param query
	 *            The query
	 */
	public RCSBGraphQLRunnerBuilder(String url, GraphQLQuery query) {
		this(url, query.getGraphQL());
	}

	/**
	 * Constructor
	 * 
	 * @param url
	 *            The url of the service to POST the query to
	 * @param query
	 *            The query
	 */
	public RCSBGraphQLRunnerBuilder(String url, String query) {
		requestBody = new ObjectMapper().createObjectNode().put("query", query);
		variableNode = requestBody.putObject("variables");
		this.url = url;
	}

	/**
	 * Method to clear any set variables
	 * 
	 * @return the {@link RCSBGraphQLRunnerBuilder} for method daisy-chaining
	 */
	public RCSBGraphQLRunnerBuilder clearVariables() {
		variableNode.removeAll();
		return this;
	}

	/**
	 * Method to clear a set variable with the given name. Does nothing if no
	 * variable is set with that name
	 * 
	 * @param name
	 *            The variable name
	 * @return the {@link RCSBGraphQLRunnerBuilder} for method daisy-chaining
	 */
	public RCSBGraphQLRunnerBuilder clearVariable(String name) {
		variableNode.remove(name);
		return this;
	}

	/**
	 * Method to add a String variable with the given value. An existing
	 * variable with the same name will be over-written
	 * 
	 * @param name
	 *            The variable name
	 * @param value
	 *            The variable value
	 * @return the {@link RCSBGraphQLRunnerBuilder} for method daisy-chaining
	 */
	public RCSBGraphQLRunnerBuilder addStringVariable(String name,
			String value) {
		variableNode.put(name, value);
		return this;
	}

	/**
	 * Method to add a String array variable with the given value(s). An
	 * existing variable with the same name will be over-written
	 * 
	 * @param name
	 *            The variable name
	 * @param values
	 *            The array value(s)
	 * @return the {@link RCSBGraphQLRunnerBuilder} for method daisy-chaining
	 */
	public RCSBGraphQLRunnerBuilder addStringArrayVariable(String name,
			String... values) {
		ArrayNode varArr = variableNode.putArray(name);
		for (String value : values) {
			varArr.add(value);
		}
		return this;
	}

	/**
	 * Method to add a boolean variable with the given value. An existing
	 * variable with the same name will be over-written
	 * 
	 * @param name
	 *            The variable name
	 * @param value
	 *            The variable value
	 * @return the {@link RCSBGraphQLRunnerBuilder} for method daisy-chaining
	 */
	public RCSBGraphQLRunnerBuilder addBooleanVariable(String name,
			boolean value) {
		variableNode.put(name, value);
		return this;
	}

	/**
	 * @return A {@link JsonPostRunner} instance ready to run the current query
	 * @throws JsonProcessingException
	 *             If there is a problem converting the JSON to text
	 * @throws MalformedURLException
	 *             If there is a problem forming the URL
	 */
	public JsonPostRunner getRunner()
			throws JsonProcessingException, MalformedURLException {
		// have a value set
		return new JsonPostRunner(new URL(url), requestBody);
	}

	/**
	 * @return the query text from the request body
	 */
	public String getQuery() {
		return requestBody.get("query").asText();
	}

}
