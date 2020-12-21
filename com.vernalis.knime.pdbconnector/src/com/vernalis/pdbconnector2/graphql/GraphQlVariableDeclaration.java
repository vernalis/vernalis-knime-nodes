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

import java.util.Objects;

/**
 * A GraphQL variable declaration. Can only be of default scalar types and their
 * corresponding arrays. Variable declarations should be created using one of
 * the many 'of' factory methods
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public abstract class GraphQlVariableDeclaration implements GraphQL {

	private final String name;
	private final GraphQLScalarTypes type;
	private final boolean isOptional;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The variable name
	 * @param type
	 *            The variable type
	 * @param isOptional
	 *            whether the variable value is optional of can be null
	 */
	protected GraphQlVariableDeclaration(String name, GraphQLScalarTypes type,
			boolean isOptional) {
		this.name = name;
		this.type = type;
		this.isOptional = isOptional;
	}

	/**
	 * @return whether the variable is an Int
	 */
	public boolean isInt() {
		return type == GraphQLScalarTypes.Int;
	}

	/**
	 * @return whether the variable is a Float
	 */
	public boolean isFloat() {
		return type == GraphQLScalarTypes.Float;
	}

	/**
	 * @return whether the variable is a String
	 */
	public boolean isString() {
		return type == GraphQLScalarTypes.String;
	}

	/**
	 * @return whether the variable is a Boolean
	 */
	public boolean isBoolean() {
		return type == GraphQLScalarTypes.Boolean;
	}

	/**
	 * @return whether the variable is a GraphQL ID
	 */
	public boolean isID() {
		return type == GraphQLScalarTypes.ID;
	}

	/**
	 * @return whether the variable is a GraphQL Enum
	 */
	public boolean isEnum() {
		return type == GraphQLScalarTypes.Enum;
	}

	/**
	 * @return whether the variable is an Int Array
	 */
	public boolean isIntArray() {
		return type == GraphQLScalarTypes.IntArray;
	}

	/**
	 * @return whether the variable is a Float Array
	 */
	public boolean isFloatArray() {
		return type == GraphQLScalarTypes.FloatArray;
	}

	/**
	 * @return whether the variable is a String Array
	 */
	public boolean isStringArray() {
		return type == GraphQLScalarTypes.StringArray;
	}

	/**
	 * @return whether the variable is a GraphQL Enum Array
	 */
	public boolean isEnumArray() {
		return type == GraphQLScalarTypes.EnumArray;
	}

	/**
	 * @return whether the variable is a Boolean Array
	 */
	public boolean isBooleanArray() {
		return type == GraphQLScalarTypes.BooleanArray;
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public int defaultInt() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public float defaultFloat() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public String defaultString() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public boolean defaultBoolean() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public String defaultID() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public String defaultEnum() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public int[] defaultIntArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public float[] defaultFloatArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public String[] defaultStringArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public String[] defaultEnumArray() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The default value
	 * @throws UnsupportedOperationException
	 *             if this is not of the right type
	 */
	public boolean[] defaultBooleanArray()
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getGraphQL() {
		StringBuilder sb = new StringBuilder().append('$');
		sb.append(name).append(':').append(' ').append(type.name());
		if (!isOptional()) {
			sb.append('!');
		}
		if (hasDefault()) {
			sb.append(" = ").append(getDefaultString());
		}
		return sb.toString();
	}

	/**
	 * @return The variable type
	 */
	public GraphQLScalarTypes getType() {
		return type;
	}

	/**
	 * @return The GraphQL string representation of the default value
	 */
	protected abstract String getDefaultString();

	/**
	 * @return whether the variable declaration has a default value
	 */
	public abstract boolean hasDefault();

	/**
	 * For arrays, whether the array members themselves are optional
	 * 
	 * @param b
	 *            whether the array members are optional
	 */
	public void setIsOfOptionals(boolean b) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The variable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return whether variable value is optional
	 */
	protected boolean isOptional() {
		return isOptional;
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofInt(String name,
			boolean isOptional) {
		return new IntVariable(name, isOptional, null);
	}

	/**
	 * Variable factory method with default value
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @param defaultValue
	 *            The default value (or {@code null} if there is no default
	 *            value
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration of(String name, boolean isOptional,
			Integer defaultValue) {
		return new IntVariable(Objects.requireNonNull(name), isOptional,
				defaultValue);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofFloat(String name,
			boolean isOptional) {
		return new FloatVariable(name, isOptional, null);
	}

	/**
	 * Variable factory method with default value
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @param defaultValue
	 *            The default value (or {@code null} if there is no default
	 *            value
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration of(String name, boolean isOptional,
			Float defaultValue) {
		return new FloatVariable(Objects.requireNonNull(name), isOptional,
				defaultValue);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofString(String name,
			boolean isOptional) {
		return new StringVariable(name, isOptional, null);
	}

	/**
	 * Variable factory method with default value
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @param defaultValue
	 *            The default value (or {@code null} if there is no default
	 *            value
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration of(String name, boolean isOptional,
			String defaultValue) {
		return new StringVariable(Objects.requireNonNull(name), isOptional,
				defaultValue);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofBoolean(String name,
			boolean isOptional) {
		return new BooleanVariable(name, isOptional, null);
	}

	/**
	 * Variable factory method with default value
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @param defaultValue
	 *            The default value (or {@code null} if there is no default
	 *            value
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration of(String name, boolean isOptional,
			Boolean defaultValue) {
		return new BooleanVariable(Objects.requireNonNull(name), isOptional,
				defaultValue);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofEnum(String name,
			boolean isOptional) {
		return ofEnum(name, isOptional, null);
	}

	/**
	 * Variable factory method with default value
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @param defaultValue
	 *            The default value (or {@code null} if there is no default
	 *            value
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofEnum(String name,
			boolean isOptional, String defaultValue) {
		return new EnumVariable(Objects.requireNonNull(name), isOptional,
				defaultValue);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofID(String name,
			boolean isOptional) {
		return ofID(name, isOptional, null);
	}

	/**
	 * Variable factory method with default value
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @param defaultValue
	 *            The default value (or {@code null} if there is no default
	 *            value
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofID(String name,
			boolean isOptional, String defaultValue) {
		return new IDVariable(Objects.requireNonNull(name), isOptional,
				defaultValue);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofIntArray(String name,
			boolean isOptional) {
		return new IntArrayVariable(name, isOptional);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofFloatArray(String name,
			boolean isOptional) {
		return new FloatArrayVariable(name, isOptional);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofStringArray(String name,
			boolean isOptional) {
		return new StringArrayVariable(name, isOptional);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofEnumArray(String name,
			boolean isOptional) {
		return new EnumArrayVariable(name, isOptional);
	}

	/**
	 * Variable factory method
	 * 
	 * @param name
	 *            The name of the variable
	 * @param isOptional
	 *            Whether it is optional
	 * @return A variable declaration of the correct type
	 */
	public static GraphQlVariableDeclaration ofBooleanArray(String name,
			boolean isOptional) {
		return new BooleanArrayVariable(name, isOptional);
	}

	/*
	 * Here follow the concrete implementations
	 */
	private final static class IntVariable extends GraphQlVariableDeclaration {

		private final Integer defaultValue;

		protected IntVariable(String name, boolean isOptional,
				Integer defaultValue) {
			super(name, GraphQLScalarTypes.Int, isOptional);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean hasDefault() {
			return defaultValue != null;
		}

		@Override
		protected String getDefaultString() {
			return String.format("%d", defaultInt());
		}

		@Override
		public int defaultInt() throws UnsupportedOperationException {
			if (!hasDefault()) {
				throw new UnsupportedOperationException();
			}
			return defaultValue.intValue();
		}

	}

	private final static class FloatVariable
			extends GraphQlVariableDeclaration {

		private final Float defaultValue;

		protected FloatVariable(String name, boolean isOptional,
				Float defaultValue) {
			super(name, GraphQLScalarTypes.Float, isOptional);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean hasDefault() {
			return defaultValue != null;
		}

		@Override
		protected String getDefaultString() {
			// Keep full stored info - dont truncate - String.format("%f",float)
			// rounds to ~6 dp
			return defaultValue.toString();
		}

		@Override
		public float defaultFloat() throws UnsupportedOperationException {
			if (!hasDefault()) {
				throw new UnsupportedOperationException();
			}
			return defaultValue.floatValue();
		}

	}

	private final static class StringVariable
			extends GraphQlVariableDeclaration {

		private final String defaultValue;

		protected StringVariable(String name, boolean isOptional,
				String defaultValue) {
			super(name, GraphQLScalarTypes.String, isOptional);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean hasDefault() {
			return defaultValue != null;
		}

		@Override
		protected String getDefaultString() {
			return String.format("\"%s\"", defaultString());
		}

		@Override
		public String defaultString() throws UnsupportedOperationException {
			if (!hasDefault()) {
				throw new UnsupportedOperationException();
			}
			return defaultValue;
		}

	}

	private final static class BooleanVariable
			extends GraphQlVariableDeclaration {

		private final Boolean defaultValue;

		protected BooleanVariable(String name, boolean isOptional,
				Boolean defaultValue) {
			super(name, GraphQLScalarTypes.Boolean, isOptional);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean hasDefault() {
			return defaultValue != null;
		}

		@Override
		protected String getDefaultString() {
			return defaultValue.toString();
		}

		@Override
		public boolean defaultBoolean() throws UnsupportedOperationException {
			if (!hasDefault()) {
				throw new UnsupportedOperationException();
			}
			return defaultValue.booleanValue();
		}

	}

	private final static class IDVariable extends GraphQlVariableDeclaration {

		private final String defaultValue;

		protected IDVariable(String name, boolean isOptional,
				String defaultValue) {
			super(name, GraphQLScalarTypes.ID, isOptional);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean hasDefault() {
			return defaultValue != null;
		}

		@Override
		protected String getDefaultString() {
			return String.format("\"%s\"", defaultValue);
		}

		@Override
		public String defaultID() throws UnsupportedOperationException {
			if (!hasDefault()) {
				throw new UnsupportedOperationException();
			}
			return defaultValue;
		}

	}

	private final static class EnumVariable extends GraphQlVariableDeclaration {

		private final String defaultValue;

		protected EnumVariable(String name, boolean isOptional,
				String defaultValue) {
			super(name, GraphQLScalarTypes.Enum, isOptional);
			this.defaultValue = defaultValue;
		}

		@Override
		public boolean hasDefault() {
			return defaultValue != null;
		}

		@Override
		protected String getDefaultString() {
			return defaultValue;
		}

		@Override
		public String defaultEnum() throws UnsupportedOperationException {
			if (!hasDefault()) {
				throw new UnsupportedOperationException();
			}
			return defaultValue;
		}

	}

	/*
	 * Superclass for all the Array variable types - we dont support default for
	 * these
	 */
	private abstract static class ArrayVariable
			extends GraphQlVariableDeclaration {

		/** Are the array members optional? */
		boolean isOfOptionals;

		protected ArrayVariable(String name, GraphQLScalarTypes type,
				boolean isOptional) {
			super(name, type, isOptional);
		}

		@Override
		public boolean hasDefault() {
			return false;
		}

		@Override
		protected String getDefaultString() {
			return null;
		}

		@Override
		public void setIsOfOptionals(boolean b) {
			this.isOfOptionals = b;
		}

		@Override
		public String getGraphQL() {
			StringBuilder sb = new StringBuilder().append('$');
			sb.append(getName()).append(':').append(' ').append('[')
					.append(getType().name().replace("Array", ""));
			if (!isOfOptionals) {
				sb.append('!');
			}
			sb.append(']');
			if (!isOptional()) {
				sb.append('!');
			}
			if (hasDefault()) {
				sb.append(" = ").append(getDefaultString());
			}
			return sb.toString();
		}
	}

	/*
	 * Array variable concrete implementations
	 */
	private final static class IntArrayVariable extends ArrayVariable {

		protected IntArrayVariable(String name, boolean isOptional) {
			super(name, GraphQLScalarTypes.IntArray, isOptional);
		}

	}

	private final static class FloatArrayVariable extends ArrayVariable {

		protected FloatArrayVariable(String name, boolean isOptional) {
			super(name, GraphQLScalarTypes.FloatArray, isOptional);
		}

	}

	private final static class StringArrayVariable extends ArrayVariable {

		protected StringArrayVariable(String name, boolean isOptional) {
			super(name, GraphQLScalarTypes.StringArray, isOptional);
		}

	}

	private final static class EnumArrayVariable extends ArrayVariable {

		protected EnumArrayVariable(String name, boolean isOptional) {
			super(name, GraphQLScalarTypes.EnumArray, isOptional);
		}

	}

	private final static class BooleanArrayVariable extends ArrayVariable {

		protected BooleanArrayVariable(String name, boolean isOptional) {
			super(name, GraphQLScalarTypes.BooleanArray, isOptional);
		}

	}

}
