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
package com.vernalis.nodes.smartsviewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SmartsViewer" Node. Retrieves a SMARTSViewer
 * visualisation of a columns of SMARTS strings using the service at
 * www.smartsviewer.de
 */
public class SmartsViewer2NodeDialog extends DefaultNodeSettingsPane {
	// Objects for the resetable fields
	final DialogComponentNumber n1, n2;
	final DialogComponentBoolean bool;
	final SettingsModelIntegerBounded m1, m2;
	final SettingsModelBoolean m_bool;

	/**
	 * New pane for configuring the SmartsViewer node.
	 */
	protected SmartsViewer2NodeDialog() {
		super();
		createNewGroup("Select SMARTS Column");
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(SmartsViewer2NodeModel.CFG_SMARTS, null),
				"Select a column containing the SMARTS Strings:", 0, true,
				SmartsValue.class));
		closeCurrentGroup();

		createNewGroup("Renderer Settings");
		// addDialogComponent (new DialogComponentButtonGroup(
		// new SettingsModelString(SmartsViewer2NodeModel.CFG_IMG_FORMAT,"png"),
		// "Select image format",
		// false, new String[] {"PNG Image","SVG Image"}, new String[]
		// {"png","svg"}));

		addDialogComponent(new DialogComponentButtonGroup(
				new SettingsModelString(SmartsViewer2NodeModel.CFG_LEGEND,
						"both"), "Select legend type", false, new String[] {
						"Both", "None", "Static", "Dynamic" }, new String[] {
						"both", "none", "static", "dynamic" }));

		addDialogComponent(new DialogComponentButtonGroup(
				new SettingsModelString(SmartsViewer2NodeModel.CFG_VIS_MODUS,
						"1"), "Select Visualition modus", false, new String[] {
						"Complete", "Element Symbols" }, new String[] { "1",
						"2" }));

		closeCurrentGroup();

		createNewTab("Communication Error Settings");
		// Create some advanced settings dialog components
		m1 = new SettingsModelIntegerBounded(
				SmartsViewer2NodeModel.CFG_NUM_RETRIES, 10, 0, 20);
		n1 = new DialogComponentNumber(m1,
				"Number of retries to contact server:", 1);

		m2 = new SettingsModelIntegerBounded(SmartsViewer2NodeModel.CFG_DELAY,
				1, 1, 600);
		n2 = new DialogComponentNumber(m2, "Delay between attempts (secs):", 5);

		m_bool = new SettingsModelBoolean(
				SmartsViewer2NodeModel.CFG_IGNORE_ERR, true);
		bool = new DialogComponentBoolean(m_bool, "Ignore server errors?");

		// Add them...
		addDialogComponent(n1);
		addDialogComponent(n2);
		addDialogComponent(bool);

		// Now we add a restore settings button, which runs the 'doTestQuery' on
		// clicking
		setHorizontalPlacement(true);
		DialogComponentButton restoreButton = new DialogComponentButton(
				"Restore Defaults");
		restoreButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doRestore();
			}
		});
		addDialogComponent(restoreButton);
	}

	/**
	 * Restore the default settings
	 */
	protected void doRestore() {
		m1.setIntValue(10);
		m2.setIntValue(1);
		m_bool.setBooleanValue(true);
	}
}
