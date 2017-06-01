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

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Simple Callable task which allows running of a POST request in a separate
 * thread. Calling #call() returns an InputStream with the response data
 * 
 * @author S.Roughley
 *
 */
public class PostRunner implements Callable<InputStream> {
	private URL url;
	private String data;

	/**
	 * Constructor for the task
	 * 
	 * @param url
	 *            The url to send the request to
	 * @param data
	 *            The data to POST
	 */
	public PostRunner(URL url, String data) {
		this.url = url;
		this.data = data;
	}

	@Override
	public InputStream call() throws Exception {
		return RestClient.doPOST(url, data);
	}

}
