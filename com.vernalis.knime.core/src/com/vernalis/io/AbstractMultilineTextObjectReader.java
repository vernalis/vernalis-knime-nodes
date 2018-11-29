/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Abstract implementation class for a {@link MultilineTextObjectReader}.
 * Implementing subclasses need to implement {@link #getObjectFromLines(List)}
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the {@link MultilineTextObject} to be return by the
 *            reader
 */
public abstract class AbstractMultilineTextObjectReader<T extends MultilineTextObject>
		implements MultilineTextObjectReader<T> {

	private final BufferedReader br;
	private final boolean inclDelimAtStart, inclDelimAtEnd, requireFinalDelim;
	private final Pattern delimPattern;
	private List<String> lines =
			Collections.synchronizedList(new ArrayList<>());
	private boolean isFirst = true;
	private long bytesRead = 0;

	/**
	 * Constructor
	 * 
	 * @param reader
	 *            The reader to reads the object(s) from
	 * @param deliminatorPattern
	 *            The object deliminator
	 * @param includeDeliminatorAtEnd
	 *            Should the deliminator be included at the end of the object?
	 * @param includeDeliminatorAtStart
	 *            Should the deliminator be included at the start of the object?
	 * @param requireFinalDeliminatorAtEnd
	 *            Is a final deliminator required at the end of the last object
	 *            in the file?
	 * @throws IllegalArgumentException
	 *             if the reader supplied is {@code null}
	 */
	public AbstractMultilineTextObjectReader(Reader reader,
			Pattern deliminatorPattern, boolean includeDeliminatorAtEnd,
			boolean includeDeliminatorAtStart,
			boolean requireFinalDeliminatorAtEnd)
			throws IllegalArgumentException {
		if (reader == null) {
			throw new IllegalArgumentException("No Reader Supplied!");
		}
		br = (reader instanceof BufferedReader) ? (BufferedReader) reader
				: new BufferedReader(reader);

		inclDelimAtStart = includeDeliminatorAtStart;
		inclDelimAtEnd = includeDeliminatorAtEnd;
		delimPattern = deliminatorPattern;
		requireFinalDelim = requireFinalDeliminatorAtEnd;
	}

	@Override
	public synchronized T readNext()
			throws IOException, ParseException, UnsupportedOperationException {
		if (br == null) {
			throw new UnsupportedOperationException("No Reader available!");
		}

		String line;

		while ((line = br.readLine()) != null) {
			bytesRead += line.length();
			if (delimPattern != null
					&& delimPattern.matcher(line.trim()).find()) {
				// We have a deliminator line, so handle object creation and
				// clear the list
				if (inclDelimAtEnd) {
					lines.add(line);
				}
				if (isFirst && inclDelimAtStart) {
					// Special case - the first line!
					lines.add(line);
					isFirst = false;
					continue;
				}
				T retVal = getObjectFromLines(lines);
				lines.clear();
				if (inclDelimAtStart) {
					lines.add(line);
				}

				return retVal;
			} else {
				lines.add(line);
			}
		}

		if (lines.isEmpty()) {
			return null;
		}
		// We got to the end and didnt find an object deliminator
		if (inclDelimAtEnd && requireFinalDelim) {
			throw new EOFException(
					"End of file reached without required final deliminator");
		}
		T retVal = getObjectFromLines(lines);
		lines.clear();
		return retVal;
	}

	/**
	 * Method to convert a {@link List} of lines from the {@link Reader} to a
	 * single object of the required type
	 * 
	 * @param lines
	 *            The lines from the reader representing the object
	 * @return The object
	 * @throws ParseException
	 *             If there was an error parsing the lines to an object
	 * @throws IllegalArgumentException
	 *             If there was an error parsing the lines to an object
	 */
	public abstract T getObjectFromLines(List<String> lines)
			throws ParseException, IllegalArgumentException;

	@Override
	public final BufferedReader getReader() {
		return br;
	}

	@Override
	public final Pattern getDeliminatorPattern() {
		return delimPattern;
	}

	@Override
	public final boolean includeDeliminatorAtEnd() {
		return inclDelimAtEnd;
	}

	@Override
	public final boolean requireFinalDeliminatorAtEnd() {
		return requireFinalDelim;
	}

	@Override
	public final boolean includeDeliminatorAtStart() {
		return inclDelimAtStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 5;
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractMultilineTextObjectReader [");
		if (br != null) {
			builder.append("br=");
			builder.append(br);
			builder.append(", ");
		}
		builder.append("inclDelimAtStart=");
		builder.append(inclDelimAtStart);
		builder.append(", inclDelimAtEnd=");
		builder.append(inclDelimAtEnd);
		builder.append(", requireFinalDelim=");
		builder.append(requireFinalDelim);
		builder.append(", ");
		if (delimPattern != null) {
			builder.append("delimPattern=");
			builder.append(delimPattern);
			builder.append(", ");
		}
		if (lines != null) {
			builder.append("lines=");
			builder.append(lines.subList(0, Math.min(lines.size(), maxLen)));
			builder.append(", ");
		}
		builder.append("isFirst=");
		builder.append(isFirst);
		builder.append("]");
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.io.MultilineTextObjectReader#getBytesRead()
	 */
	@Override
	public long getBytesRead() {
		return bytesRead;
	}

}
