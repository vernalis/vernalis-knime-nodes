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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides basic connectivity to a RESTful web service via a POST request
 * returning a JSON object
 * 
 * @since 1.28.0
 */
public class JsonRestClient {

	/**
	 * Gets the result of a POST operation as an InputStream.
	 *
	 * @param url
	 *            the url
	 * @param data
	 *            the data to post
	 * @param isInputJson
	 *            whether the POST body data is JSON format
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static InputStream doPOST(URL url, String data, boolean isInputJson)
			throws IOException {
		// Send data
		final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (isInputJson) {
			conn.addRequestProperty("Content-Type",
					"application/json; charset=utf-8");
		}
		conn.setDoOutput(true);
		final OutputStreamWriter wr =
				new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		final int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
			// No hits
			return null;
		}
		if (responseCode >= 300) {
			final StringBuilder err =
					new StringBuilder("Error retrieving query - Response code ")
							.append(responseCode);
			final InputStream errorStream = conn.getErrorStream();
			if (errorStream != null) {
				err.append(" - '");
				final BufferedReader br =
						new BufferedReader(new InputStreamReader(errorStream));
				String line;
				while ((line = br.readLine()) != null) {
					err.append(line).append('\n');
				}
			}
			throw new IOException(err.toString().trim() + "'");
		}
		return conn.getInputStream();
	}

	/**
	 * @param url
	 *            The url to post the request to
	 * @param query
	 *            The request body
	 * @param isInputJson
	 *            Whether the input is JSON
	 * @return The {@link JsonNode} result of calling the service
	 * @throws IOException
	 *             If there was an error calling the service
	 */
	public static JsonNode getResult(URL url, String query, boolean isInputJson)
			throws IOException {
		final InputStream resultStream = doPOST(url, query, isInputJson);
		if (resultStream == null) {
			return null;
		}
		return new ObjectMapper().readTree(resultStream);
	}

	/**
	 * @param url
	 *            The url to post the request to
	 * @param query
	 *            The request body (JSON)
	 * @return The {@link JsonNode} result of calling the service
	 * @throws IOException
	 *             If there was an error calling the service
	 */
	public static JsonNode getResult(URL url, JsonNode query)
			throws IOException {
		return getResult(url, new ObjectMapper().writeValueAsString(query),
				true);
	}
}
