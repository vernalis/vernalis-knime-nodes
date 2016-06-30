/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp;

/**
 * Runtime Exception to allow passing of the error upwards within a
 * ColumnRearranger so that the Row information may be attached. Used in
 * Subclasses where the abstract superclass provides the column rearranger
 * method which calls an abstract getResultFromDataCell(DataCell cell) method
 * 
 * @author Stephen Roughley <s.roughley@vernalis.com>
 * 
 */
@SuppressWarnings("serial")
public class RowExecutionException extends Exception {

	/**
	 * We only provide a message, which when passed upwards should be re-used
	 * with the row added
	 * 
	 * @param message
	 */
	public RowExecutionException(String message) {
		super(message);
	}

}
