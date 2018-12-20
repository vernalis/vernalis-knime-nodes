/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.rcsb.io.nodes.manip;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.vernalis.rcsb.io.helpers.RCSBFileTypes;

/**
 * <code>NodeDialog</code> for the "RCSBmultiDownload" Node. Node to allow
 * download of multiple RCSB PDB filetypes from a column of RCSB Structure IDs
 */
public class RCSBmultiDownload2NodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the RCSBmultiDownload node.
	 */
	@SuppressWarnings("unchecked")
	protected RCSBmultiDownload2NodeDialog() {
		super();
		createNewGroup("PDB ID Column");
		addDialogComponent(new DialogComponentColumnNameSelection(createColumnNameModel(),
				"Select a column containing the PDB IDs:", 0, true, StringValue.class));

		closeCurrentGroup();

		createNewGroup("Downloaded Files");
		// The properties component
		final List<String> fTypeNames = new ArrayList<>();
		for (RCSBFileTypes fType : RCSBFileTypes.values()) {
			fTypeNames.add(fType.getName());
		}

		final SettingsModelStringArray selectedProperties = createFiletypesModel();
		final DialogComponentStringListSelection propertyListDlg =
				new DialogComponentStringListSelection(selectedProperties, null, fTypeNames,
						ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true,
						Math.min(fTypeNames.size(), 10));

		addDialogComponent(propertyListDlg);

		final DialogComponentButton selectAll = new DialogComponentButton("Select All");
		selectAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				selectedProperties.setStringArrayValue(fTypeNames.toArray(new String[0]));
			}
		});
		final DialogComponentButton selectNone = new DialogComponentButton("Clear");
		// NB - we cannot set no properties
		selectNone.setToolTipText("Selects only the 1st property");
		selectNone.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectedProperties
						.setStringArrayValue(new String[] { RCSBFileTypes.getDefault().getName() });
			}
		});

		setHorizontalPlacement(true);
		addDialogComponent(selectAll);
		addDialogComponent(selectNone);
		setHorizontalPlacement(false);
		closeCurrentGroup();

		closeCurrentGroup();
	}

	/**
	 * @return Settings model for the PDB ID Column name
	 */
	static SettingsModelString createColumnNameModel() {
		return new SettingsModelString("PDB_column_name", null);
	}

	/**
	 * @return Settings model for the selected filetypes
	 */
	static SettingsModelStringArray createFiletypesModel() {
		return new SettingsModelStringArray("Selected filetypes",
				new String[] { RCSBFileTypes.getDefault().getName() });
	}
}
