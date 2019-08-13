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
package com.vernalis.knime.chem.speedysmiles.helpers;

import com.vernalis.exceptions.RowExecutionException;

/**
 * Exception thrown when chiral isomer enumeration is expected to overrun
 * available memory
 * 
 * @author S.Roughley
 *
 */
@SuppressWarnings("serial")
public class ChiralityOverflowException extends RowExecutionException {

	/**
	 * Default constructor
	 */
	public ChiralityOverflowException() {

	}

	/**
	 * Exception-wrapping contructor
	 * 
	 * @param message
	 *            message
	 * @param cause
	 *            cause
	 */
	public ChiralityOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor with message
	 * 
	 * @param message
	 *            the message
	 */
	public ChiralityOverflowException(String message) {
		super(message);
	}

}
