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
package com.vernalis.nodes.io.rcsb.source;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * <code>NodeDialog</code> for the "RCSBGrabber" Node.
 * Node to allow download of multiple RCSB PDB filetypes from a column of RCSB Structure IDs
 */
public class RCSBsDownloadNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the RCSBGrabber node.
     */
	protected RCSBsDownloadNodeDialog() {
		super();
		createNewGroup("PDB ID Column");
		addDialogComponent (new DialogComponentString(
				new SettingsModelString(RCSBsDownloadNodeModel.CFG_PDB_ID,null),
				"Enter a valid PDB ID:"));

		closeCurrentGroup();

		createNewGroup("Download formats:");
		//Note that we are not going to give the user the option of naming all these columns!
		addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(
				RCSBsDownloadNodeModel.CFG_PDB, true), "PDB File"));

		addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(
				RCSBsDownloadNodeModel.CFG_CIF, true), "mmCIF File"));

		addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(
				RCSBsDownloadNodeModel.CFG_SF, true), "Structure Factors"));

		addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(
				RCSBsDownloadNodeModel.CFG_PDBML, true), "PDBML/XML (PDBx) File"));

		addDialogComponent (new DialogComponentBoolean(new SettingsModelBoolean(
				RCSBsDownloadNodeModel.CFG_FASTA, true), "FASTA Sequence File"));

		closeCurrentGroup();
	}
}

