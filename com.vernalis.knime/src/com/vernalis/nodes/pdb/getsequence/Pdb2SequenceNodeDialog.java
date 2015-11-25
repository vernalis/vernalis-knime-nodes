/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, 2014, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.pdb.getsequence;

import org.knime.bio.types.PdbValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "Pdb2Sequence" Node. Node to extract
 * sequence(s) from PDB Cell column n
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author SDR
 */
public class Pdb2SequenceNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the Pdb2Sequence node.
	 */
	@SuppressWarnings("unchecked")
	protected Pdb2SequenceNodeDialog() {
		createNewGroup("PDB Column");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(Pdb2SequenceNodeModel.CFG_PDB_COL_NAME, null),
				"Select a column containing the PDB Cells:", 0, true, PdbValue.class));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(Pdb2SequenceNodeModel.CFG_PDB_DEL, false),
				"Remove PDB Column"));
		closeCurrentGroup();

		createNewGroup("SEQRES-Derived Sequence(s)");
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(Pdb2SequenceNodeModel.CFG_SEQRES3, false),
				"'Raw' 3-letter sequence(s) from SEQRES records"));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(Pdb2SequenceNodeModel.CFG_SEQRES1, true),
				"'Sanitized' 1-letter sequence(s) from SEQRES records"));
		closeCurrentGroup();

		createNewGroup("Co-ordinate Block-Derived Sequence(s)");
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(Pdb2SequenceNodeModel.CFG_COORDS3, false),
				"'Raw' 3-letter sequence(s) from Co-ordinate records"));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(Pdb2SequenceNodeModel.CFG_COORDS1, true),
				"'Sanitized' 1-letter sequence(s) from Co-ordinate records"));
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(Pdb2SequenceNodeModel.CFG_INC_HETATM, false),
				"Include HETATM Co-ordinate records in sequence(s)"));

		closeCurrentGroup();

	}

}
