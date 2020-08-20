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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;

import com.vernalis.io.MultilineTextObject;

/**
 * @author S.Roughley
 *
 */
public class FastaRecord implements MultilineTextObject {

	private FastaTypes fType;
	private String headerLine;
	private String comments;
	private String sequence;

	private final List<String> lines = new ArrayList<>();

	/**
	 * No arg constructor for spec generation
	 */
	public FastaRecord() {
		this(FastaTypes.getDefault());
	}

	public FastaRecord(FastaTypes type) {
		headerLine = null;
		comments = null;
		sequence = null;
		fType = type;
	}

	/**
	 * @param fType
	 */
	public FastaRecord(FastaTypes fType, List<String> lines) {
		this.fType = fType;
		if (lines.isEmpty()) {
			throw new IllegalArgumentException(
					"FASTA record must have at least a header line!");
		}
		Iterator<String> iter = lines.iterator();
		headerLine = iter.next();
		if (!headerLine.startsWith(">")) {
			throw new IllegalArgumentException(
					"FASTA record must start with a header line starting with '>'");
		}
		StringBuilder comm = new StringBuilder();
		StringBuilder seq = new StringBuilder();
		while (iter.hasNext()) {
			String line = iter.next();
			if (line.startsWith(";")) {
				comm.append(line.substring(1));
			} else {
				seq.append(line.trim());
			}
		}
		comments = comm.length() > 0 ? comm.toString() : null;
		sequence = seq.length() > 0 ? seq.toString() : null;
		this.lines.addAll(lines);
	}

	@Override
	public DataCell[] getNewCells(String lineSeparator) {
		List<DataCell> newCells = new ArrayList<>();
		newCells.add(new StringCell(
				lines.stream().collect(Collectors.joining(lineSeparator))));
		newCells.add(new StringCell(headerLine));
		newCells.add(comments == null ? DataType.getMissingCell()
				: new StringCell(comments));
		newCells.add(sequence == null ? DataType.getMissingCell()
				: new StringCell(sequence));
		Collections.addAll(newCells, fType.getCells(headerLine));
		return newCells.toArray(new DataCell[newCells.size()]);
	}

	@Override
	public DataColumnSpec[] getNewColumnSpecs() {
		List<DataColumnSpec> newColSpecs = new ArrayList<>();
		newColSpecs.add(createColSpec("FASTA Record"));
		newColSpecs.add(createColSpec("Header Line"));
		newColSpecs.add(createColSpec("Comments"));
		newColSpecs.add(createColSpec("Sequence"));
		Collections.addAll(newColSpecs,
				fType.getColumnSpecs(new DataTableSpec()));
		return newColSpecs.toArray(new DataColumnSpec[newColSpecs.size()]);
	}

	public void setFastaType(FastaTypes type) {
		this.fType = type;
	}

	/**
	 * @param newColName
	 *            TODO
	 * @return
	 */
	DataColumnSpec createColSpec(String newColName) {
		return new DataColumnSpecCreator(newColName, StringCell.TYPE)
				.createSpec();
	}

}
