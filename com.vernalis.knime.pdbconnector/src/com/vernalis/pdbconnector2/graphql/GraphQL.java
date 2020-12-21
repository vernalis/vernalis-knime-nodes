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
package com.vernalis.pdbconnector2.graphql;

/**
 * An interface defining part of a GraphQL query
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public interface GraphQL {

	/**
	 * @return A string representation of the query component, which must be
	 *         suitable for using in a query request
	 */
	public String getGraphQL();
}
