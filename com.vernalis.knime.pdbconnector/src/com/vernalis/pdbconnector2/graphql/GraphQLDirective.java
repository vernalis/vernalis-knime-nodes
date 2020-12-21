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
 * A class representing a {@link GraphQLDirective}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class GraphQLDirective implements GraphQL {

	private final GraphQLDirectiveType type;
	private String valueExpression;

	/**
	 * Constructor with type and boolean value
	 * 
	 * @param type
	 *            The type of directive
	 * @param value
	 *            The boolean value for the directive
	 */
	public GraphQLDirective(GraphQLDirectiveType type, boolean value) {
		this(type, Boolean.valueOf(value).toString());
	}

	/**
	 * Constructor with type and an valid GraphQL value evaluating to boolean
	 * 
	 * @param type
	 *            The type of directive
	 * @param valueExpression
	 *            The expression
	 */
	public GraphQLDirective(GraphQLDirectiveType type, String valueExpression) {
		this.type = type;
		this.valueExpression = valueExpression;
	}

	// TODO: Add constructors for variable declaration-based expressions
	// (boolean variables or variable + expression)

	@Override
	public String getGraphQL() {
		return String.format("@%s(if: %s) ", type.getGraphQL(),
				valueExpression);
	}

}
