/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.graphql.result;

/**
 * This Exception represents some sort of error whilst trying to retrieve a
 * result from a GraphQLResult
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class GraphQLResultException extends Exception {

	private static final long serialVersionUID = 1L;
	private final String path;
	private final GraphQLResultException nested;

	/**
	 * Constructor
	 * 
	 * @param path
	 *            The path of the query
	 */
	public GraphQLResultException(String path) {
		this(path, null);
	}

	/**
	 * @param path
	 *            The path of the query
	 * @param nested
	 *            Any nested queries
	 */
	public GraphQLResultException(String path, GraphQLResultException nested) {
		// Propagate the 'cause'
		super(nested);
		this.path = path;
		this.nested = nested;
	}

	/**
	 * @return The path to the query
	 */
	public String getGraphQlPath() {
		StringBuilder retVal = new StringBuilder(path);
		if (nested != null) {
			retVal.append('.').append(nested.getGraphQlPath());
		}
		return retVal.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return String.format("Error retrieving result for query path '%s'",
				getGraphQlPath());
	}

}
