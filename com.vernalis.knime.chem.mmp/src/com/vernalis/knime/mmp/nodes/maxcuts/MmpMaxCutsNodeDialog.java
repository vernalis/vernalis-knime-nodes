/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.maxcuts;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.MolFormats;


/**
 * The node dialog pane for the maximum cuts node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class MmpMaxCutsNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelBoolean m_AddHs;
	SettingsModelString m_fragmentationType, m_customRSMARTS;

	/**
	 * Constructor for the node dialog pane
	 */
	@SuppressWarnings("unchecked")
	public MmpMaxCutsNodeDialog() {
		super();
		addDialogComponent(new DialogComponentColumnNameSelection(
				createMolColumnSettingsModel(), "Select Molecule column", 0,
				MolFormats.m_RDKitmolFormats.toArray(new Class[0])));

		m_fragmentationType = createSMIRKSModel();
		m_customRSMARTS = createCustomSMARTSModel();
		m_fragmentationType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				m_customRSMARTS.setEnabled(FragmentationTypes
						.valueOf(m_fragmentationType.getStringValue()) == FragmentationTypes.USER_DEFINED);
			}
		});

		m_customRSMARTS
				.setEnabled(FragmentationTypes.valueOf(m_fragmentationType
						.getStringValue()) == FragmentationTypes.USER_DEFINED);

		createNewGroup("Select the fragmentation type");
		addDialogComponent(new DialogComponentButtonGroup(m_fragmentationType,
				null, true, FragmentationTypes.values()));
		addDialogComponent(new DialogComponentString(m_customRSMARTS,
				"User rSMARTS:"));
		closeCurrentGroup();

		addDialogComponent(new DialogComponentBoolean(
				createAllowTwoCutsToBondValueModel(),
				"Allow 2 cuts along single bond giving a single bond as 'value'?"));

		m_AddHs = createAddHModel();
		addDialogComponent(new DialogComponentBoolean(m_AddHs,
				"Add H's for n=1?"));
	}

	/*
	 * Methods for Settings Model Creation
	 */
	/**
	 * Create Settings Model for option to allow 2 cuts to generate a single
	 * bond as a value
	 */
	public static SettingsModelBoolean createAllowTwoCutsToBondValueModel() {
		return new SettingsModelBoolean("Two Cuts to Bond Value",
				MMPConstants.DEFAULT_ALLOW_2_CUTS_TO_SINGLE_BOND);
	}

	/** Create the settings model for the option to add hydrogens */
	public static SettingsModelBoolean createAddHModel() {
		return new SettingsModelBoolean("Add Hs", MMPConstants.DEFAULT_ADD_H);
	}

	/** Create model for customReaction SMARTS */
	public static SettingsModelString createCustomSMARTSModel() {
		return new SettingsModelString("Custom rSMARTS", null);
	}

	/**
	 * Create the settings model for the molecule column
	 */
	public static SettingsModelString createMolColumnSettingsModel() {
		return new SettingsModelString("Molecule Column", null);
	}

	/** Create settings model for the SMIRKS fragmentation */
	public static SettingsModelString createSMIRKSModel() {
		return new SettingsModelString("Fragmentation SMIRKS",
				FragmentationTypes.getDefaultMethod().getActionCommand());
	}

}
