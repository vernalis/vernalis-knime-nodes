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

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Simple Callable task which allows running of a GET request in a separate
 * thread. Calling #call() returns a List of the results
 * 
 * @author S.Roughley
 *
 */
public class GetRunner implements Callable<List<String>> {
	private URL url;

	public GetRunner(URL url) {
		this.url = url;
	}

	@Override
	public List<String> call() throws Exception {
		return RestClient.getResultList(url);
	}

}
