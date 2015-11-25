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
package com.vernalis.nodes.pdb.props;

import org.knime.bio.types.PdbValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "PdbProperties" Node. Node to extract
 * properties from a PDB cell
 */
public class PdbPropertiesNodeDialog extends DefaultNodeSettingsPane {
	/**
	 * New pane for configuring the PdbProperites node. TODO: Ideally we would
	 * deal with the possibility that the input column is a PDB Celltype
	 */
	@SuppressWarnings("unchecked")
	protected PdbPropertiesNodeDialog() {
		super();
		createNewGroup("PDB Column");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(PdbPropertiesNodeModel.CFG_PDB_COLUMN_NAME, null),
				"Select a column containing the PDB Cells:", 0, true, PdbValue.class));

		closeCurrentGroup();

		createNewGroup("Data to extract:");
		// Note that we are not going to give the user the option of naming all
		// these columns!
		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_PDB_ID, true), "PDB ID"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_TITLE, true), "Title"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_EXP_METHOD, true),
				"Experimental Method"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_RESOLUTION, true),
				"Resolution"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_MDLCNT, true),
				"Number of Models"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_R, true), "R"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_R_FREE, true), "R Free"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_SPACE_GROUP, true),
				"Space Group"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_REMARK_1, true), "REMARK 1"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_REMARK_2, true), "REMARK 2"));

		addDialogComponent(new DialogComponentBoolean(
				new SettingsModelBoolean(PdbPropertiesNodeModel.CFG_REMARK_3, true), "REMARK 3"));

		closeCurrentGroup();
	}

}
