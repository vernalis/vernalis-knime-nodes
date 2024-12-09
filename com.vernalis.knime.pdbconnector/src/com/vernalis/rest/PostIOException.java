/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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

import java.io.IOException;

/**
 * A simple {@link IOException} implementation which also tracks the HTTP
 * response code
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@SuppressWarnings("serial")
public class PostIOException extends IOException {

	private final int responseCode;

	/**
	 * Simple constructor with only a response Code
	 * 
	 * @param responseCode
	 *            The response code
	 */
	public PostIOException(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * Full constructor
	 * 
	 * @param responseCode
	 *            The response code
	 * @param message
	 *            The error message
	 * @param cause
	 *            The cause
	 */
	public PostIOException(int responseCode, String message, Throwable cause) {
		super(message, cause);
		this.responseCode = responseCode;
	}

	/**
	 * Constructor for response code and message
	 * 
	 * @param responseCode
	 *            The response code
	 * @param message
	 *            The error message
	 */
	public PostIOException(int responseCode, String message) {
		super(message);
		this.responseCode = responseCode;
	}

	/**
	 * Constructor for response code and cause
	 * 
	 * @param responseCode
	 *            The response code
	 * @param cause
	 *            The cause
	 */
	public PostIOException(int responseCode, Throwable cause) {
		super(cause);
		this.responseCode = responseCode;
	}

	/**
	 * @return the response code
	 */
	public int getResponseCode() {
		return responseCode;
	}

}
