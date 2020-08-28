/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.core.os;

/**
 * Exception thrown when the current operating system is not supported
 * 
 * @author s.roughley
 *
 */
public class UnsupportedOperatingSystemException extends Exception {

	private static final long serialVersionUID = -4306049211115428163L;

	public UnsupportedOperatingSystemException() {
		super();

	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnsupportedOperatingSystemException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 */
	public UnsupportedOperatingSystemException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public UnsupportedOperatingSystemException(Throwable cause) {
		super(cause);

	}

}
