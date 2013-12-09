/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.fasta;

import java.util.Arrays;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "FASTASequence" Node. Extract the chains and
 * sequences from a PDB FASTA sequence file column
 */
public class FASTASequenceNodeDialog_2 extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the FASTASequence node.
	 */
	protected FASTASequenceNodeDialog_2() {
		createNewGroup("FASTA Sequence Column");
		// Group to deal with the column
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(
						FASTASequenceNodeModel_2.CFG_FASTA_COL_NAME, null),
				"Select a column containing the FASTA Sequence Cells:", 0,
				true, StringValue.class));

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				FASTASequenceNodeModel_2.CFG_OVERWRITE, false),
				"Delete FASTA Sequence column"));

		createNewGroup("Extracted Properties");
		// Group to deal with what is extracted

		addDialogComponent(new DialogComponentStringSelection(
				new SettingsModelString(
						FASTASequenceNodeModel_2.CFG_FASTA_TYPE, null),
				"Select the FASTA Sequence source or type:", Arrays.asList(
						"GenBank", "EMBL", "DDBJ (DNA Database of Japan)",
						"NBRF", "Protein Research Foundation", "SWISS-PROT",
						"PDB", "Patents", "GenInfo Backbone Id",
						"General Database Identifier",
						"NCBI Reference Sequence", "Local Sequence Identifier",
						"Other (No fields extracted from header)")));

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				FASTASequenceNodeModel_2.CFG_HEADER, false),
				"Extract full Header Row(s) from FASTA"));

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				FASTASequenceNodeModel_2.CFG_SEQUENCE, true),
				"Extract the Sequence?"));

	}
}
