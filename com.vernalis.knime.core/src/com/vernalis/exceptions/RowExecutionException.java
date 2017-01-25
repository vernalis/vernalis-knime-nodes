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
package com.vernalis.exceptions;

/**
 * Runtime Exception to allow passing of the error upwards within a
 * ColumnRearranger so that the Row information may be attached
 * 
 * @author Stephen Roughley knime@vernalis.com
 * 
 */
@SuppressWarnings("serial")
public class RowExecutionException extends Exception {

	public RowExecutionException() {

	}

	/**
	 * We only provide a message, which when passed upwards should be re-used
	 * with the row added
	 * 
	 * @param message
	 */
	public RowExecutionException(String message) {
		super(message);
	}

	public RowExecutionException(String message, Throwable cause) {
		// TODO Auto-generated constructor stub
	}

}
