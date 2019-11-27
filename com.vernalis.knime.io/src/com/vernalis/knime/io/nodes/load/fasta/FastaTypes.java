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

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.misc.ArrayUtils;

public enum FastaTypes implements ButtonGroupEnumInterface {
	GenBank("GenBank", "gb\\|(.*)\\|(.*)", "GI number", "Accession", "Locus"),
	EMBL("EMBL", "emb\\|(.*)\\|(.*)", "GI number", "Accession", "Locus"),
	DDBJ("DDBJ (DNA Database of Japan)", "dbj\\|(.*)\\|(.*)", "GI number",
			"Accession", "Locus"),
	NBRF("NBRF", "pir\\|\\|(.*)", "Entry"),
	Protein_Research_Foundation("Protein Research Foundation", "prf\\|\\|(.*)",
			"Entry"),
	SWISS_PROT("SWISS-PROT", "(?:sp|tr)\\|(.*)\\|(.*)", "Accession", "Name"),
	PDB("PDB", "([A-Za-z0-9]{4}):([A-Za-z0-9])\\|PDBID\\|CHAIN\\|SEQUENCE",
			"PDB ID", "Chain"),
	Patents("Patents", "pat\\|(.*)\\|(.*)", "Country", "number"),
	GenInfo_Backbone_Id("GenInfo Backbone Id", "bbs\\|(.*)", "Number"),
	General_Database_Identifier("General Database Identifier",
			"gnl\\|(.*)\\|(.*)", "Database", "Identifier"),
	NCBI_Reference_Sequence("NCBI Reference Sequence", "ref\\|(.*)\\|(.*)",
			"Accession", "Locus"),
	Local_Sequence_Identifier("Local Sequence Identifier", "lcl\\|(.*)",
			"Identifier"),
	Other("Other (No fields extracted from header)", "");

	private final String legacyName;
	private final Pattern headerPattern;
	private final String[] columnNames;

	private FastaTypes(String name, String headerPattern,
			String... columnNames) {
		this.legacyName = name;
		this.headerPattern = Pattern.compile(headerPattern);
		this.columnNames = columnNames;
	}

	@Override
	public String getText() {
		return legacyName;
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return getText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public DataColumnSpec[] getColumnSpecs(DataTableSpec inSpec) {
		return Arrays.stream(getColumnNames())
				.map(x -> DataTableSpec.getUniqueColumnName(inSpec, x))
				.map(x -> new DataColumnSpecCreator(x, StringCell.TYPE)
						.createSpec())
				.toArray(DataColumnSpec[]::new);
	}

	public DataCell[] getCells(String fastaheader) {
		int numCols = getColumnNames().length;
		DataCell[] retVal = ArrayUtils.fill(new DataCell[numCols],
				DataType.getMissingCell());
		Matcher m = headerPattern.matcher(fastaheader);
		if (m.find()) {
			for (int i = 0; i < m.groupCount(); i++) {
				final String str = m.group(i + 1);
				if (str != null && !str.isEmpty()) {
					retVal[i] = new StringCell(str);
				}
			}
		}
		return retVal;
	}

	public static FastaTypes getDefault() {
		return Other;
	}

	public static FastaTypes resolveLegacyName(String legacyName) {
		if (legacyName == null) {
			throw new NullPointerException("A legacyName must be supplied!");
		}
		Optional<FastaTypes> any = Arrays.stream(values())
				.filter(x -> legacyName.equals(x.getActionCommand())).findAny();
		if (any.isPresent()) {
			return any.get();
		}
		throw new IllegalArgumentException(
				"Legacy name '" + legacyName + "' not recognised");
	}
}
