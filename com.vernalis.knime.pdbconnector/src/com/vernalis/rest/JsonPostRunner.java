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
package com.vernalis.rest;

import java.net.URL;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple Callable task which allows running of a POST request in a separate
 * thread. Calling #call() returns a {@link JsonNode} with the response data
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 */
public class JsonPostRunner implements Callable<JsonNode> {

	private final URL url;
	private final String data;
	private final boolean isInputJson;

	/**
	 * Constructor from a {@link JsonNode} request body
	 *
	 * @param url
	 *            The url to send the request to
	 * @param data
	 *            The data to POST in the body
	 * @throws JsonProcessingException
	 *             If there was an error parsing the data returned by the
	 *             request
	 */
	public JsonPostRunner(URL url, JsonNode data)
			throws JsonProcessingException {
		this(url, new ObjectMapper().writeValueAsString(data), true);
	}

	/**
	 * Constructor
	 * 
	 * @param url
	 *            The url to send the request to
	 * @param data
	 *            The data for the POST body
	 * @param isInputJson
	 *            whether the input data is JSON
	 */
	public JsonPostRunner(URL url, String data, boolean isInputJson) {
		this.url = url;
		this.data = data;
		this.isInputJson = isInputJson;
	}

	@Override
	public JsonNode call() throws Exception {
		return JsonRestClient.getResult(url, data, isInputJson);
	}

}
