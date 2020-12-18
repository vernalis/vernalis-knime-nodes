/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
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
package com.vernalis.pdbconnector.config;

/**
 * A simple exception to capture the report generation over-running and
 * returning non-XML
 * 
 * @author S.Roughley
 *
 */
@SuppressWarnings("serial")
@Deprecated
public class ReportOverflowException extends Exception {

	public ReportOverflowException() {

	}

	public ReportOverflowException(String message) {
		super(message);
	}

	public ReportOverflowException(Throwable cause) {
		super(cause);
	}

	public ReportOverflowException(String message, Throwable cause) {
		super(message, cause);
	}

}
