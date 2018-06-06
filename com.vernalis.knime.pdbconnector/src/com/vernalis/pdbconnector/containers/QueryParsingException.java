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
package com.vernalis.pdbconnector.containers;

/**
 * An exception thrown during the parsing of a PDB
 * 
 * @author S.Roughley
 *
 */
public class QueryParsingException extends Exception {

	/**
	 * {@link QueryParsingException}s are thrown by classes processing XML query
	 * results to {@link AbstractHetDetails} subclasses
	 */
	private static final long serialVersionUID = 540748396030618002L;

	public QueryParsingException() {
	}

	public QueryParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueryParsingException(String message) {
		super(message);
	}

	public QueryParsingException(Throwable cause) {
		super(cause);
	}

}
