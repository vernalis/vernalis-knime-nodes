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
package com.vernalis.knime.io.nodes.load.bruker.amix;

import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

import com.vernalis.io.AbstractMultilineTextObjectReader;

/**
 * A reader for an Amix Peak List file
 * 
 * @author s.roughley
 *
 */
public class AmixPeakListReader
		extends AbstractMultilineTextObjectReader<AmixPeakList> {

	/**
	 * The deliminator pattern signalling the start of a new spectrum peak list
	 */
	private static final Pattern DELIMINATOR_PATTERN =
			Pattern.compile("! TITLE = ");

	public AmixPeakListReader(Reader reader) {
		super(reader, DELIMINATOR_PATTERN, false, true, false);
	}

	@Override
	public AmixPeakList getObjectFromLines(List<String> lines)
			throws ParseException, IllegalArgumentException {
		return new AmixPeakList(lines);
	}

}
