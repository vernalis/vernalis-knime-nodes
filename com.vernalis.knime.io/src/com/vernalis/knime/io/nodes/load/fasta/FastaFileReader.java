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
package com.vernalis.knime.io.nodes.load.fasta;

import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

import com.vernalis.io.AbstractMultilineTextObjectReader;

public class FastaFileReader
		extends AbstractMultilineTextObjectReader<FastaRecord> {

	private static Pattern DELIMINATOR_PATTERN = Pattern.compile("^>.*");
	private final FastaTypes fType;

	public FastaFileReader(Reader reader, FastaTypes type)
			throws IllegalArgumentException {
		super(reader, DELIMINATOR_PATTERN, false, true, false);
		this.fType = type;
	}

	@Override
	public FastaRecord getObjectFromLines(List<String> lines)
			throws ParseException, IllegalArgumentException {
		return new FastaRecord(fType, lines);
	}

}
