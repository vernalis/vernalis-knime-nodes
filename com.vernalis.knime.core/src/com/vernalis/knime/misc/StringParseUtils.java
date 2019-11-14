/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc;

/**
 * A helper class containing various convenience methods for parsing
 * {@link String}s
 * 
 * @author S.Roughley
 *
 */
public class StringParseUtils {

	private StringParseUtils() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * 'Safe' substring method, which returns null or empty string respectively
	 * if that is the input, and if 'end' is beyond the end of the string, the
	 * string to the end from 'start'. If 'start' is beyond the end of the
	 * string, then {@code null} is returned. Otherwise it behaves like
	 * {@link String#substring(int, int)}
	 * 
	 * @param str
	 *            The string
	 * @param start
	 *            The (inclusive) start index
	 * @param end
	 *            The (exclusive) end index
	 * @return The designated substring or {@code null}
	 */
	public static String subString(String str, int start, int end) {
		if (str == null) {
			return null;
		}
		if (str.isEmpty()) {
			return str;
		}
		if (str.length() > end) {
			return str.substring(start, end);
		}
		if (str.length() > start) {
			return str.substring(start);
		}
		return null;
	}

	/**
	 * An equivalent of {@link String#lastIndexOf(int, int)}, allowing multiple
	 * characters to be supplied. The returned value is the index of the last
	 * occurence of any of the specified characters before or at the 'end'
	 * value. If no matches are found, -1 is returned
	 * 
	 * @param str
	 *            The String to test in
	 * @param end
	 *            The last allowed match position
	 * @param chars
	 *            The characters to test for
	 * @return
	 */
	public static int lastIndexOf(CharSequence str, int end, char... chars) {
		for (int i = end; i >= 0; i--) {
			char c = str.charAt(i);
			for (char c0 : chars) {
				if (c == c0) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Removed space characters from the right end of string
	 * 
	 * @param str
	 *            The String
	 * @return The right-trimmed String
	 */
	public static CharSequence trimRight(CharSequence str) {
		int len = str.length();
		while (str.charAt(len - 1) <= ' ' && len > 0) {
			len--;
		}
		return str.subSequence(0, len);
	}

	/**
	 * 'Safe' integer parsing, returning '0' as a default if no value could
	 * correctly be parsed
	 * 
	 * @param string
	 *            The input string
	 * @return 0 if the string could not be parsed
	 * @see #parseInt(String, int)
	 */
	public static int parseInt(String string) {
		return parseInt(string, 0);
	}

	/**
	 * 'Safe' integer parsing. If the string is {@code null} or empty (see
	 * {@link #nullify(String)}), then the default is returned. Non-parsable
	 * text will return the default. If a {@code null} return is required, use
	 * {@link #parseNullableInteger(String)}
	 * 
	 * @param string
	 *            The input string
	 * @param defaultValue
	 *            The default value to return in the event of a non-numeric
	 *            string
	 * @return The parsed int, or the default value
	 * @see #nullify(String)
	 * @see #parseNullableInteger(String)
	 */
	public static int parseInt(String string, int defaultValue) {
		final String nStr = nullify(string);
		if (nStr == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(nStr);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 'Safe' integer parsing. If the string is {@code null} or empty (see
	 * {@link #nullify(String)}), or cannot be parsed to an integer, then
	 * {@code null} is returned.
	 * 
	 * @param string
	 *            The input string
	 * @param defaultValue
	 *            The default value to return in the event of a non-numeric
	 *            string
	 * @return The parsed int, or the {@code null}
	 * @see #nullify(String)
	 * @see #parseInt(String, int)
	 */
	public static Integer parseNullableInteger(String string) {
		final String nStr = nullify(string);
		if (nStr == null) {
			return null;
		}
		try {
			return Integer.parseInt(nStr);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 'Safe' double parsing, returning '0.0' as a default if no value could
	 * correctly be parsed
	 * 
	 * @param string
	 *            The input string
	 * @return 0.0 if the string could not be parsed
	 * @see #parseDouble(String, double)
	 */
	public static double parseDouble(String string) {
		return parseDouble(string, 0.0);
	}

	/**
	 * 'Safe' double parsing. If the string is {@code null} or empty (see
	 * {@link #nullify(String)}), then the default is returned. Non-parsable
	 * text will return the default. If a {@code null} return is required, use
	 * {@link #parseNullableDouble(String)}
	 * 
	 * @param string
	 *            The input string
	 * @param defaultValue
	 *            The default value to return in the event of a non-numeric
	 *            string
	 * @return The parsed double, or the default value
	 * @see #nullify(String)
	 * @see #parseNullableDouble(String)
	 */
	public static double parseDouble(String string, double defaultValue) {
		final String nStr = nullify(string);
		if (nStr == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(nStr);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 'Safe' double parsing. If the string is {@code null} or empty (see
	 * {@link #nullify(String)}), or cannot be parsed to a double, then
	 * {@code null} is returned.
	 * 
	 * @param string
	 *            The input string
	 * @param defaultValue
	 *            The default value to return in the event of a non-numeric
	 *            string
	 * @return The parsed double, or {@code null}
	 * @see #nullify(String)
	 * @see #parseDouble(String, double)
	 */
	public static Double parseNullableDouble(String string) {
		final String nStr = nullify(string);
		if (nStr == null) {
			return null;
		}
		try {
			return Double.parseDouble(nStr);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Convenience method which converts any empty strings or strings containing
	 * only whitespace to {@code null}
	 * 
	 * @param string
	 *            The input string
	 * @return {@code null} or the non-empty {@link String#trim()}ed string
	 * @see #defaultify(String, String)
	 */
	public static String nullify(String string) {
		return string == null ? null
				: string.trim().isEmpty() ? null : string.trim();
	}

	/**
	 * Convenience method to replace a {@code null}, empty, or whitespace-only
	 * string with a default value
	 * 
	 * @param string
	 *            The input string
	 * @param defaultValue
	 *            The default to return if the input is {@code null}, empty or
	 *            contains only whitespace
	 * @return The default or the non-empty {@link String#trim()}ed string
	 * @see #nullify(String)
	 */
	public static String defaultify(String string, String defaultValue) {
		final String nStr = nullify(string);
		return nStr == null ? defaultValue : nStr;
	}

	/**
	 * Add space characters to the right end of string until it is of the
	 * indicated length
	 * 
	 * @param string
	 *            The string
	 * @param length
	 *            The target length
	 * @return The right-padded string, or the unchanged string if it is already
	 *         longer than the indicated length
	 */
	public static String rightPad(String string, int length) {
		if (string.length() >= length) {
			return string;
		}
		return string
				+ new String(ArrayUtils.of(' ', length - string.length()));

	}
}
