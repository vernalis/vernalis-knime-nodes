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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the top level GraphQL query container and is the entry point to most
 * query construction.
 * 
 * For the simple query
 * 
 * <pre>
 * query TestQuery {
 *   human(id: "1000"){
 *     name
 *     height
 *   }
 * }
 * </pre>
 * 
 * The following code:
 * 
 * <pre>
 * GraphQLQuery query = new GraphQLQuery("TestQuery");
 * query.addField("human.name");
 * query.addField("human.height");
 * query.getField("human").addArgument(GraphQLArgument.of("id", "1000"));
 * </pre>
 * 
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class GraphQLQuery implements GraphQL {

	/**
	 * The unit indent to be used in output
	 */
	static final String INDENT = "    ";
	private final String name;
	private final Map<String, GraphQlVariableDeclaration> variables =
			new LinkedHashMap<>();
	private final Map<String, GraphQLField> fields = new LinkedHashMap<>();
	private String currentIndent = "";

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of the query
	 */
	public GraphQLQuery(String name) {
		this.name = name;
	}

	/**
	 * Constructor for a no-name query
	 */
	public GraphQLQuery() {
		this(null);
	}

	/**
	 * Method to clear any variable declarations
	 */
	public void clearVariables() {
		variables.clear();
	}

	/**
	 * @param name
	 *            The name of the variable
	 * @return whether the query has a variable declared with the given name
	 */
	public boolean hasVariable(String name) {
		return variables.containsKey(name);
	}

	/**
	 * @param variable
	 *            A {@link GraphQlVariableDeclaration} to add to the query
	 */
	public void addVariable(GraphQlVariableDeclaration variable) {
		variables.put(variable.getName(), variable);
	}

	/**
	 * Method to clear any fields in the query
	 */
	public void clearFields() {
		fields.clear();
	}

	/**
	 * @param name
	 *            The name of the field
	 * @return whether the query contains a field with the given name
	 */
	public boolean hasField(String name) {
		// TODO: Allow dot-separated paths
		return fields.containsKey(name);
	}

	/**
	 * @param name
	 *            the name of the field
	 * @return The field with the given name, or {@code null} if no such field
	 *         is present
	 */
	public GraphQLField getField(String name) {
		// TODO: Allow dot-separated paths
		return fields.get(name);
	}

	/**
	 * @param fieldPath
	 *            A '.'-delimited path to a field to add to the query
	 */
	public void addField(String fieldPath) {
		String[] parts = fieldPath.split("\\.", 2);
		String thisLevel = parts[0];
		String subPath = parts.length > 1 ? parts[1] : null;
		GraphQLField f = fields.computeIfAbsent(thisLevel,
				k -> new GraphQLField(k, this));
		if (subPath != null) {
			f.addChildField(subPath);
		}
	}

	/**
	 * @return The current indent for pretty-printing the query via
	 *         {@link #getGraphQL()}
	 */
	String getIndent() {
		return currentIndent;
	}

	/**
	 * Method for fields to increase the indent during pretty printing via
	 * {@link #getGraphQL()}
	 */
	void increaseIndent() {
		currentIndent += INDENT;
	}

	/**
	 * Method for fields to decrease the indent during pretty printing via
	 * {@link #getGraphQL()}
	 */
	void decreaseIndent() {
		currentIndent = currentIndent.replaceFirst("\\Q" + INDENT + "\\E", "");

	}

	@Override
	public String getGraphQL() {
		if (fields.isEmpty()) {
			throw new IllegalStateException("No query present!");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("query ");
		if (name != null) {
			sb.append(name).append(' ');
		}
		if (!variables.isEmpty()) {
			sb.append('(');
			sb.append(variables.values().stream().map(v -> v.getGraphQL())
					.collect(Collectors.joining(", ")));
			sb.append(')').append(' ');
		}
		sb.append('{').append('\n');
		increaseIndent();
		sb.append(fields.values().stream().map(f -> f.getGraphQL())
				.collect(Collectors.joining("\n")));
		decreaseIndent();
		sb.append('\n').append(getIndent()).append('}');
		return sb.toString();
	}
}
