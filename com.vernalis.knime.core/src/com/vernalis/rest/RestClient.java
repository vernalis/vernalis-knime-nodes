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
package com.vernalis.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides basic connectivity to PDB RESTful web service.
 */
public class RestClient {

	/**
	 * Gets the result of a GET operation as a single string.
	 *
	 * @param url
	 *            the url
	 * @return the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String getResult(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(conn.getResponseMessage());
		}
		StringBuffer retVal = new StringBuffer();
		BufferedReader rd =
				new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
		String line;
		while ((line = rd.readLine()) != null) {
			retVal.append(line);
		}
		rd.close();
		conn.disconnect();
		return retVal.toString();
	}

	/**
	 * Gets the result of a GET operation as a list of strings (one per line of
	 * output).
	 *
	 * @param url
	 *            the url
	 * @return the result list
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static List<String> getResultList(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(conn.getResponseMessage());
		}
		List<String> retVal = new ArrayList<>();
		BufferedReader rd =
				new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF8"));
		String line;
		while ((line = rd.readLine()) != null) {
			retVal.add(line);
		}
		rd.close();
		conn.disconnect();
		return retVal;
	}

	/**
	 * Gets the result of a GET operation as an InputStream.
	 *
	 * @param url
	 *            the url
	 * @return the result stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static InputStream getResultStream(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException(conn.getResponseMessage());
		}
		return conn.getInputStream();
	}

	/**
	 * Gets the result of a POST operation as an InputStream.
	 *
	 * @param url
	 *            the url
	 * @param data
	 *            the data to post
	 * @return the input stream
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static InputStream doPOST(URL url, String data) throws IOException {
		// Send data
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		return conn.getInputStream();
	}

}
