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

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An Abstract base class representing a GraphQL query argument. Individual
 * argument types are accessible via the {@code #of()} static factory methods
 * for the relevant argument type, including from a
 * {@link GraphQlVariableDeclaration}, and {@link #ofEnum(String, String)} and
 * {@link #ofID(String, String)} for the corresponding types
 * 
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public abstract class GraphQLArgument implements GraphQL {

	private final String name;
	private final Object value;
	private final GraphQLScalarTypes type;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The name of the argument
	 * @param type
	 *            The type of the argument - must be a
	 *            {@link GraphQLScalarTypes}
	 * @param value
	 *            The value
	 */
	protected GraphQLArgument(String name, GraphQLScalarTypes type,
			Object value) {
		this.name = Objects.requireNonNull(name);
		this.type = type;
		this.value = value;
	}

	/**
	 * @return the value of the argument
	 */
	protected Object getValue() {
		return value;
	}

	/**
	 * @return {@code true} if the argument is of an Int
	 */
	public boolean isInt() {
		return type == GraphQLScalarTypes.Int;
	}

	/**
	 * @return {@code true} if the argument is of an Float
	 */
	public boolean isFloat() {
		return type == GraphQLScalarTypes.Float;
	}

	/**
	 * @return {@code true} if the argument is of an String
	 */
	public boolean isString() {
		return type == GraphQLScalarTypes.String;
	}

	/**
	 * @return {@code true} if the argument is of an Boolean
	 */
	public boolean isBoolean() {
		return type == GraphQLScalarTypes.Boolean;
	}

	/**
	 * @return {@code true} if the argument is of an ID
	 */
	public boolean isID() {
		return type == GraphQLScalarTypes.ID;
	}

	/**
	 * @return {@code true} if the argument is of an Enum
	 */
	public boolean isEnum() {
		return type == GraphQLScalarTypes.Enum;
	}

	/**
	 * @return {@code true} if the argument is of an Int Array
	 */
	public boolean isIntArray() {
		return type == GraphQLScalarTypes.IntArray;
	}

	/**
	 * @return {@code true} if the argument is of a Float Array
	 */
	public boolean isFloatArray() {
		return type == GraphQLScalarTypes.FloatArray;
	}

	/**
	 * @return {@code true} if the argument is of a String Array
	 */
	public boolean isStringArray() {
		return type == GraphQLScalarTypes.StringArray;
	}

	/**
	 * @return {@code true} if the argument is of an Enum Array
	 */
	public boolean isEnumArray() {
		return type == GraphQLScalarTypes.EnumArray;
	}

	/**
	 * @return {@code true} if the argument is of a Boolean Array
	 */
	public boolean isBooleanArray() {
		return type == GraphQLScalarTypes.BooleanArray;
	}

	@Override
	public String getGraphQL() {
		return String.format("%s: %s", getName(), getValueString());
	}

	/**
	 * @return The value as the correct String representation for inclusion in
	 *         the {@link #getGraphQL()} method
	 */
	protected String getValueString() {
		return getValue().toString();
	}

	/**
	 * @return The name of the argument
	 */
	public String getName() {
		return name;
	}

	/**
	 * Static factory method to create a Int argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, int value) {
		return new IntArgument(name, value);
	}

	/**
	 * Static factory method to create a Float argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, float value) {
		return new FloatArgument(name, value);
	}

	/**
	 * Static factory method to create a String argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, String value) {
		return new StringArgument(name, value);
	}

	/**
	 * Static factory method to create a Boolean argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, boolean value) {
		return new BooleanArgument(name, value);
	}

	/**
	 * Static factory method to create an Enum argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument ofEnum(String name, String value) {
		return new EnumArgument(name, value);
	}

	/**
	 * Static factory method to create an ID argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument ofID(String name, String value) {
		return new IDArgument(name, value);
	}

	/**
	 * Static factory method to create an Int array argument
	 * 
	 * @param name
	 *            The name
	 * @param values
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, int[] values) {
		return new IntArrayArgument(name, values);
	}

	/**
	 * Static factory method to create a Float array argument
	 * 
	 * @param name
	 *            The name
	 * @param values
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, float[] values) {
		return new FloatArrayArgument(name, convertToDouble(values));
	}

	private static double[] convertToDouble(float[] values) {
		if (values == null) {
			return null;
		}
		double[] retVal = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			retVal[i] = values[i];
		}
		return retVal;
	}

	/**
	 * Static factory method to create a Float array argument
	 * 
	 * @param name
	 *            The name
	 * @param values
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, double[] values) {
		return new FloatArrayArgument(name, values);
	}

	/**
	 * Static factory method to create a String array argument
	 * 
	 * @param name
	 *            The name
	 * @param values
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, String[] values) {
		return new StringArrayArgument(name, values);
	}

	/**
	 * Static factory method to create a Enum array argument
	 * 
	 * @param name
	 *            The name
	 * @param values
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument ofEnum(String name, String[] values) {
		return new EnumArrayArgument(name, values);
	}

	/**
	 * Static factory method to create a Boolean array argument
	 * 
	 * @param name
	 *            The name
	 * @param values
	 *            The value
	 * @return The argument
	 */
	public static GraphQLArgument of(String name, boolean[] values) {
		return new BooleanArrayArgument(name, values);
	}

	/**
	 * Static factory method to create a {@link GraphQlVariableDeclaration}
	 * argument
	 * 
	 * @param name
	 *            The name
	 * @param value
	 *            The variable declaration to use
	 * @return The argument
	 */
	public static GraphQLArgument of(String name,
			GraphQlVariableDeclaration value) {
		return new VariableArgument(name, value);
	}

	/*
	 * Here follow the concrete implementations
	 */
	private final static class IntArgument extends GraphQLArgument {

		protected IntArgument(String name, Integer value) {
			super(name, GraphQLScalarTypes.Int, value);

		}

	}

	private final static class FloatArgument extends GraphQLArgument {

		protected FloatArgument(String name, float value) {
			super(name, GraphQLScalarTypes.Float, value);

		}

	}

	private final static class StringArgument extends GraphQLArgument {

		protected StringArgument(String name, String value) {
			super(name, GraphQLScalarTypes.String, value);
		}

		@Override
		protected String getValueString() {
			return String.format("\"%s\"", getValue());
		}

	}

	private final static class BooleanArgument extends GraphQLArgument {

		protected BooleanArgument(String name, boolean value) {
			super(name, GraphQLScalarTypes.Boolean, value);
		}

	}

	private final static class IDArgument extends GraphQLArgument {

		protected IDArgument(String name, String value) {
			super(name, GraphQLScalarTypes.ID, value);
		}

		@Override
		protected String getValueString() {
			return String.format("\"%s\"", getValue());
		}

	}

	private final static class EnumArgument extends GraphQLArgument {

		protected EnumArgument(String name, String value) {
			super(name, GraphQLScalarTypes.Enum, value);
		}

	}

	/*
	 * Superclass for all the Array variable types - we dont support default for
	 * these
	 */
	private abstract static class ArrayArgument extends GraphQLArgument {

		protected ArrayArgument(String name, GraphQLScalarTypes type,
				Object[] value) {
			super(name, type, Objects.requireNonNull(value));
			if (value.length == 0) {
				throw new IllegalArgumentException();
			}
		}

		@Override
		protected String getValueString() {
			return "[" + Arrays.stream((Object[]) getValue())
					.map(x -> getArrValString(x))
					.collect(Collectors.joining(", ")) + "]";
		}

		protected String getArrValString(Object o) {
			return o.toString();
		}
	}

	/*
	 * Array variable concrete implementations
	 */
	private final static class IntArrayArgument extends ArrayArgument {

		protected IntArrayArgument(String name, int... values) {
			super(name, GraphQLScalarTypes.IntArray,
					Arrays.stream(values).boxed().toArray());
		}

	}

	private final static class FloatArrayArgument extends ArrayArgument {

		protected FloatArrayArgument(String name, double... values) {
			super(name, GraphQLScalarTypes.FloatArray,
					Arrays.stream(values).boxed().toArray());
		}

	}

	private final static class StringArrayArgument extends ArrayArgument {

		protected StringArrayArgument(String name, String... values) {
			super(name, GraphQLScalarTypes.StringArray, values);
		}

		@Override
		protected String getArrValString(Object o) {
			return String.format("\"%s\"", o.toString());
		}

	}

	private final static class EnumArrayArgument extends ArrayArgument {

		protected EnumArrayArgument(String name, String... values) {
			super(name, GraphQLScalarTypes.EnumArray, values);
		}

	}

	private final static class BooleanArrayArgument extends ArrayArgument {

		protected BooleanArrayArgument(String name, boolean... values) {
			super(name, GraphQLScalarTypes.BooleanArray, convertArray(values));
		}

		private static Object[] convertArray(boolean[] values) {
			if (values == null) {
				return null;
			}
			Object[] oArr = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				oArr[i] = values[i];
			}
			return oArr;
		}

	}

	private final static class VariableArgument extends GraphQLArgument {

		protected VariableArgument(String name,
				GraphQlVariableDeclaration value) {
			super(name, value.getType(), value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.pdbconnector2.graphql.GraphQLArgument#
		 * getValueString()
		 */
		@Override
		protected String getValueString() {
			return String.format("$%s",
					((GraphQlVariableDeclaration) getValue()).getName());
		}

	}
}
