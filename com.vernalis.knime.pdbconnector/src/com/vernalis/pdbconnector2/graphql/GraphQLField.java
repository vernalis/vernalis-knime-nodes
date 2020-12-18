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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class to represent any data field in a GraphQL query. Fields should be
 * constructed indirectly using the {@link GraphQLQuery#addField(String)} method
 * method
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class GraphQLField implements GraphQL {

	private final GraphQLQuery query;
	private final String fieldName;
	private String alias;
	private GraphQLDirective directive;
	private Map<String, GraphQLArgument> arguments = new HashMap<>();

	private final Map<String, GraphQLField> subFields = new LinkedHashMap<>();

	/**
	 * Constructor Fields should be constructed indirectly using the
	 * {@code GraphQLQuery#addField} method
	 * 
	 * @param fieldName
	 *            The name of the field
	 * @param alias
	 *            field alias
	 * @param query
	 *            The query to which the field belongs
	 */
	GraphQLField(String fieldName, String alias, GraphQLQuery query) {
		this.fieldName = fieldName;
		this.alias = alias;
		this.query = query;
	}

	/**
	 * Constructor Fields should be constructed indirectly using the
	 * {@code GraphQLQuery#addField} method
	 * 
	 * @param fieldName
	 *            The name of the field
	 * @param query
	 *            The query to which the field belongs
	 */
	GraphQLField(String fieldName, GraphQLQuery query) {
		this(fieldName, null, query);
	}

	/**
	 * @return the alias
	 */
	public final String getAlias() {
		return alias;
	}

	/**
	 * @param alias
	 *            the alias to set
	 */
	public final void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String getGraphQL() {
		StringBuilder sb = new StringBuilder();
		sb.append(query.getIndent());
		if (alias != null) {
			sb.append(alias).append(':').append(' ');
		}
		sb.append(fieldName);
		if (directive != null) {
			sb.append(directive.getGraphQL());
			sb.append(' ');
		}
		if (!arguments.isEmpty()) {
			sb.append('(');
			sb.append(arguments.values().stream().map(x -> x.getGraphQL())
					.collect(Collectors.joining(", ")));
			sb.append(')').append(' ');
		}
		if (!subFields.isEmpty()) {
			sb.append(' ').append('{').append('\n');
			query.increaseIndent();
			sb.append(subFields.values().stream().map(x -> x.getGraphQL())
					.collect(Collectors.joining("\n")));
			query.decreaseIndent();
			sb.append('\n').append(query.getIndent()).append('}');
		}
		return sb.toString();
	}

	/**
	 * @return The field name
	 */
	public String getName() {
		return fieldName;
	}

	/**
	 * Method to clear any sub-fields
	 */
	public void clearFields() {
		subFields.clear();
	}

	/**
	 * @param name
	 *            The name of the field to look for
	 * @return {@code true} if this field contains a child field with the given
	 *         name
	 */
	public boolean hasField(String name) {
		return subFields.containsKey(name);
	}

	/**
	 * Method to add a child field. The field path should be a '.'-delimited
	 * path to the field, with each segment representing another 'layer'
	 * 
	 * @param fieldPath
	 *            The path to the field
	 */
	public void addChildField(String fieldPath) {
		String[] parts = fieldPath.split("\\.", 2);
		String thisLevel = parts[0];
		String subPath = parts.length > 1 ? parts[1] : null;
		GraphQLField f = subFields.computeIfAbsent(thisLevel,
				k -> new GraphQLField(k, this.query));
		if (subPath != null) {
			f.addChildField(subPath);
		}
	}

	/**
	 * @return the directive
	 */
	public GraphQLDirective getDirective() {
		return directive;
	}

	/**
	 * @param directive
	 *            the directive to set
	 */
	public void setDirective(GraphQLDirective directive) {
		this.directive = directive;
	}

	/**
	 * Method to clear all field arguments
	 */
	public void clearArguments() {
		arguments.clear();
	}

	/**
	 * Method to add an argument to the field
	 * 
	 * @param argument
	 *            the field to add
	 */
	public void addArgument(GraphQLArgument argument) {
		arguments.put(argument.getName(), argument);
	}

}
