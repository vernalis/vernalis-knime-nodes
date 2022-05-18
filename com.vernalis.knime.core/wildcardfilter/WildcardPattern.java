/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.wildcardfilter;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * This class provides some analogy to the {@link Pattern} class for Regex
 * matching, by providing methods to match wildcard strings where '?' and '*'
 * represent one or many unknown characters.
 * </p>
 * <p>
 * The wildcard pattern is converted to a regex, with grouping added around each
 * wildcard
 * </p>
 * <p>
 * Similar to the {@link Pattern} class for regex, the object is constructed
 * using either {@link #compile(String)} or {@link #compile(String, int)}
 * methods. In the simplest case, a call to {@link #matches(CharSequence)} will
 * test for a match. If access to the regex {@link Matcher} object is required,
 * e.g. to access the substrings matched by the wildcards, then
 * {@link #matcher(CharSequence)} should be used
 * </p>
 * 
 * @see WildcardPattern#UNICODE_CASE
 * @see WildcardPattern#DOTALL
 * @see WildcardPattern#CASE_INSENSITIVE
 * @see WildcardPattern#CANON_EQ
 * @see Pattern
 * 
 * @author S.Roughley
 *
 */
public class WildcardPattern {

	private final String wcPattern;
	private final Pattern regexPattern;

	// The flags - directly from Pattern
	/**
	 * Enables Unicode-aware case folding for {@link #CASE_INSENSITIVE}
	 * matching.
	 * 
	 * @see Pattern#UNICODE_CASE
	 * @see WildcardPattern#CASE_INSENSITIVE
	 */
	public static int UNICODE_CASE = Pattern.UNICODE_CASE;

	/**
	 * In DOTALL mode, the characters represented by '?' and '*' can include
	 * line-terminator characters. By default, wildcard characters will not
	 * match line-terminators
	 * 
	 * @see Pattern.DOTALL
	 */
	public static int DOTALL = Pattern.DOTALL;

	/**
	 * Enable case-insensitive matching. By default, matching is
	 * case-insensitive. If unicode-aware case-insensitive matching is require,
	 * then {@link #UNICODE_CASE} also needs to be set
	 * 
	 * @see WildcardPattern#UNICODE_CASE
	 * @see Pattern#CASE_INSENSITIVE
	 */
	public static int CASE_INSENSITIVE = Pattern.CASE_INSENSITIVE;

	/**
	 * @see Pattern#CANON_EQ
	 */
	public static int CANON_EQ = Pattern.CANON_EQ;

	private WildcardPattern(String pattern, int flags)
			throws PatternSyntaxException {
		wcPattern = pattern;
		regexPattern = Pattern.compile(convertWildcardToRegex(pattern), flags);
	}

	/**
	 * Static compile method to create a pattern with default flags
	 * 
	 * @param pattern
	 *            The wildcard pattern
	 * 
	 * @throws PatternSyntaxException
	 *             If the resulting regex could not be compiled
	 */
	public static WildcardPattern compile(String pattern)
			throws PatternSyntaxException {
		return compile(pattern, 0);
	}

	/**
	 * Static compile method to create a pattern matcher with user supplid flags
	 * 
	 * @param pattern
	 *            The wildcard pattern
	 * @param flags
	 *            The flags
	 * 
	 * @throws PatternSyntaxException
	 *             If the resulting regex could not be compiled
	 * 
	 * @see WildcardPattern#UNICODE_CASE
	 * @see WildcardPattern#DOTALL
	 * @see WildcardPattern#CASE_INSENSITIVE
	 * @see WildcardPattern#CANON_EQ
	 */
	public static WildcardPattern compile(String pattern, int flags)
			throws PatternSyntaxException {
		return new WildcardPattern(pattern, flags);
	}

	/**
	 * A simple method to convert a wildcard pattern matche ('*' - any
	 * characters, '?' - single character) to s Regular Expression to perform
	 * the match. Based on the SO answer at
	 * https://stackoverflow.com/questions/24337657/wildcard-matching-in-java/
	 * 24337711#24337711 Wildcards are grouped in the resulting regex
	 * 
	 * @param pattern
	 *            The wildcard pattern to match
	 * 
	 * @return A regex string representation of the character
	 */
	public static String convertWildcardToRegex(String pattern) {
		Pattern regex = Pattern.compile("[^*?]+|(\\*)|(\\?)");
		Matcher match = regex.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (match.find()) {
			if (match.group(1) != null) {
				match.appendReplacement(sb, "(.*)");
			} else if (match.group(2) != null) {
				match.appendReplacement(sb, "(.)");
			} else {
				match.appendReplacement(sb, "\\\\Q" + match.group(0) + "\\\\E");
			}
		}
		match.appendTail(sb);
		return sb.toString();
	}

	/**
	 * @return The wildcard match pattern supplied
	 */
	public String getPattern() {
		return wcPattern;
	}

	/**
	 * @return The regex version of the wildcard match
	 */
	public String getRegex() {
		return regexPattern.pattern();
	}

	/**
	 * @return The compiled {@link Pattern} object
	 */
	public Pattern getRegexPattern() {
		return regexPattern;
	}

	/**
	 * @return A Predicate which can be used to check for matches
	 */
	public Predicate<String> asPredicate() {
		return regexPattern.asPredicate();
	}

	/**
	 * @return The specified flags
	 * 
	 * @see WildcardPattern#UNICODE_CASE
	 * @see WildcardPattern#DOTALL
	 * @see WildcardPattern#CASE_INSENSITIVE
	 * @see WildcardPattern#CANON_EQ
	 */
	public int flags() {
		return regexPattern.flags();
	}

	/**
	 * Method to check whether the supplied argument is matched by the wildcard
	 * pattern
	 * 
	 * @param input
	 *            Test character sequence
	 * 
	 * @return {@code true} if the input is matched
	 */
	public boolean matches(CharSequence input) {
		return regexPattern.matcher(input).matches();
	}

	/**
	 * Method to return the {@link Matcher} object for the input character
	 * sequence. The wildcards are grouped in the underlying regex, so the value
	 * of the sub-strings they match can be extracted *
	 * 
	 * @param input
	 *            Test character sequence
	 * 
	 * @return The {@link Matcher} object
	 */
	public Matcher matcher(CharSequence input) {
		return regexPattern.matcher(input);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WildcardPattern [wcPattern=");
		builder.append(wcPattern);
		builder.append("]");
		return builder.toString();
	}
}