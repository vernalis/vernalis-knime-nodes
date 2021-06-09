/*******************************************************************************
 * Copyright (c) 2018, 2021 Vernalis (R&D) Ltd
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
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * A reader class to provide one or more objects from a Reader, where each
 * object is represented by multiple lines of text. Most implementations should
 * extend {@link AbstractMultilineTextObjectReader}
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the {@link MTOM} to return
 * @see AbstractMultilineTextObjectReader
 */
public interface MultilineTextObjectReader<T extends MultilineTextObject> {

	/**
	 * Method to read the next {@link MultilineTextObject} from the Reader.
	 * Returns {@code null} then the reader is exhausted
	 * 
	 * @return The object, or {@code null}
	 * @throws IOException
	 *             If there is an error reading the reader. EOFException if
	 *             {@link #requireFinalDeliminatorAtEnd()} is {@code true} and
	 *             none is found for last object in file
	 * @throws ParseException
	 *             If there is an error parsing the lines to the next object
	 */
	T readNext() throws IOException, ParseException;

	/**
	 * @return A {@link BufferedReader} wrapping the incoming reader
	 */
	BufferedReader getReader();

	/**
	 * @return The deliminator pattern for the individual objects
	 */
	Pattern getDeliminatorPattern();

	/**
	 * @return {@code true} if the deliminator line should be included as the
	 *         last line of an object for parsing
	 */
	boolean includeDeliminatorAtEnd();

	/**
	 * @return {@code true} if the deliminator line should be included as the
	 *         first line of an object for parsing
	 */
	boolean includeDeliminatorAtStart();

	/**
	 * @return {@code true} if a final deliminator is required to end the last
	 *         object read (otherwise the end of the reader is assumed to
	 *         represent the end of the last object)
	 */
	boolean requireFinalDeliminatorAtEnd();

	/**
	 * @return the number of bytes read from the underlying stream
	 */
	long getBytesRead();
}
