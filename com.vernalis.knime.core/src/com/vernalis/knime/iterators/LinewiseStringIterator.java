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
package com.vernalis.knime.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An iterator to allow iteration through a {@link CharSequence} line-by-line.
 * Lines will be ended with either \n or \r\n. A sequence starting with a
 * linebreak will return an empty string at the start of iteration. A string
 * terminating with a linebreak will not return an empty string following the
 * last linebreak
 * 
 * @author s.roughley
 *
 */
public class LinewiseStringIterator implements Iterator<String> {

	private final CharSequence content;
	int index = 0;
	private String lineBreak = null;

	/**
	 * Constructor
	 * 
	 * @param content
	 *            The sequence to iterate
	 */
	public LinewiseStringIterator(CharSequence content) {
		this.content = content;
	}

	@Override
	public boolean hasNext() {
		return content != null && content.length() > index;
	}

	@Override
	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		// Look for next \n or \r\n
		CharSequence retVal = null;
		for (int i = index; i < content.length(); i++) {
			if (content.charAt(i) == '\n') {
				if (i > 1 && content.charAt(i - 1) == '\r') {
					retVal = content.subSequence(index, i - 1);
					if (lineBreak == null) {
						lineBreak = "\r\n";
					}
				} else {
					retVal = content.subSequence(index, i);
					if (lineBreak == null) {
						lineBreak = "\n";
					}
				}
				index = i + 1;
				break;
			}
		}
		if (retVal == null) {
			// Reached end
			retVal = content.subSequence(index, content.length());
			index = content.length();
		}
		return retVal.toString();
	}

	/**
	 * Returns the linebreak found in the string
	 * 
	 * @return The first linebreak
	 * @throws NoSuchElementException
	 *             Thrown if no linebreaks are found, or no call has been made
	 *             to {@link #next()}
	 */
	public String getLineBreak() throws NoSuchElementException {
		if (lineBreak == null) {
			for (int i = index; i < content.length(); i++) {
				if (content.charAt(i) == '\n') {
					if (i > 1 && content.charAt(i - 1) == '\r') {
						lineBreak = "\r\n";
					} else {
						lineBreak = "\n";
					}
					break;
				}
			}
			if (lineBreak == null) {
				throw new NoSuchElementException();
			}
		}
		return lineBreak;
	}

	/**
	 * A convenience method to get a {@link Stream} of the elements of the
	 * iterator in the order they are supplied
	 * 
	 */
	public Stream<String> getAsStream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this,
				Spliterator.ORDERED | Spliterator.NONNULL), false);
	}

	/**
	 * Convenience method to return a stream containing only the non-empty
	 * string elements
	 */
	public Stream<String> getAsNonEmptyStream() {
		return getAsStream().filter(x -> !x.isEmpty());
	}
}
