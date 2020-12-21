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
 * Enum of the Scalar Types allowed in GraphQL queries
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum GraphQLScalarTypes {
	/**
	 * Integer
	 */
	Int,
	/**
	 * Floating-point decimal
	 */
	Float,
	/**
	 * String
	 */
	String,
	/**
	 * Boolean
	 */
	Boolean,
	/**
	 * GraphQL ID type
	 */
	ID,
	/**
	 * GraphQL Enum type
	 */
	Enum,
	/**
	 * Integer Array
	 */
	IntArray,
	/**
	 * Floating-point decimal array
	 */
	FloatArray,
	/**
	 * String Array
	 */
	StringArray,
	/**
	 * Boolean Array
	 */
	BooleanArray,
	/**
	 * GraphQL Enum type Array
	 */
	EnumArray;
}
