/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp.nodes.frag2pair;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.chem.types.SmilesValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Node Dialog for the Fragment to MMP node
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public class Frag2PairNodeDialog extends DefaultNodeSettingsPane {
	private final SettingsModelBoolean m_areSorted, m_checkSorted;

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public Frag2PairNodeDialog() {
		super();

		addDialogComponent(new DialogComponentColumnNameSelection(
				createFragKeyModel(), "Select the Fragment Key column", 0,
				SmilesValue.class));

		m_areSorted = createSortedKeysModel();
		m_areSorted.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				m_checkSorted.setEnabled(m_areSorted.getBooleanValue());

			}
		});
		m_checkSorted = createCheckSortedModel();

		addDialogComponent(new DialogComponentBoolean(m_areSorted,
				"Keys are sorted"));

		addDialogComponent(new DialogComponentBoolean(m_checkSorted,
				"Check keys are sorted"));

		addDialogComponent(new DialogComponentColumnNameSelection(
				createIDModel(), "Select the ID column", 0, StringValue.class));

		addDialogComponent(new DialogComponentColumnNameSelection(
				createFragValueModel(), "Select the Fragment Value column", 0,
				SmilesValue.class));

		createNewTab("Output Settings");
		addDialogComponent(new DialogComponentBoolean(createStripHModel(),
				"Remove Explicit H's from output"));

		addDialogComponent(new DialogComponentBoolean(createOutputKeyModel(),
				"Show unchanging portion"));

		addDialogComponent(new DialogComponentBoolean(
				createOutputChangingHACountsModel(),
				"Show number of changing atoms"));
		addDialogComponent(new DialogComponentBoolean(
				createOutputHARatiosModel(),
				"Show ratio of constant / changing heavy atoms"));

		addDialogComponent(new DialogComponentBoolean(
				createShowReverseTransformsModel(),
				"Show reverse-direction transforms"));

		addDialogComponent(new DialogComponentBoolean(
				createShowSmartsTransformsModel(), "Include Reaction SMARTS"));
	}

	/** Create model to include Reaction SMARTS */
	protected static SettingsModelBoolean createShowSmartsTransformsModel() {
		return new SettingsModelBoolean("Include Reaction SMARTS", true);
	}

	/** Create model to include reverse transforms in outpu */
	protected static SettingsModelBoolean createShowReverseTransformsModel() {
		return new SettingsModelBoolean("Show Reverse Transforms", true);
	}

	/** Create model for checking sorted keys */
	protected static SettingsModelBoolean createCheckSortedModel() {
		return new SettingsModelBoolean("Check sorted keys", true);
	}

	/** Create model for sorted keys */
	protected static SettingsModelBoolean createSortedKeysModel() {
		return new SettingsModelBoolean("Sorted keys", false);
	}

	/** Create model for fragment value column */
	protected static SettingsModelString createFragValueModel() {
		return new SettingsModelString("Fragment Value Column", "");
	}

	/** Create model for ID column */
	protected static SettingsModelString createIDModel() {
		return new SettingsModelString("ID Column", "");
	}

	/** Create model for Fragment Key */
	protected static SettingsModelString createFragKeyModel() {
		return new SettingsModelString("Fragment Key Column", "");
	}

	/** Create the settings model for the strip H's at the end option */
	protected static SettingsModelBoolean createStripHModel() {
		return new SettingsModelBoolean("Remove Explicit H's from output", true);
	}

	/** Create the settings model for the output fragment key model */
	protected static SettingsModelBoolean createOutputKeyModel() {
		return new SettingsModelBoolean("Output fragment key", false);
	}

	/** Create the settings model for the output changing HAC model */
	protected static SettingsModelBoolean createOutputChangingHACountsModel() {
		return new SettingsModelBoolean("Output changing HA counts", false);
	}

	/** Create the settings model for the output HAC ratio key model */
	protected static SettingsModelBoolean createOutputHARatiosModel() {
		return new SettingsModelBoolean(
				"Output changing / unchanging HA ratios", false);
	}
}
