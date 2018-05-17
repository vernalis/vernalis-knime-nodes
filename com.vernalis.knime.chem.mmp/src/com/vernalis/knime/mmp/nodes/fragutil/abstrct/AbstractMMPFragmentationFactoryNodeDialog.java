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
package com.vernalis.knime.mmp.nodes.fragutil.abstrct;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.IncomingExplicitHsOption;
import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

/**
 * Abstract node dialog for nodes using the {@link FragmentationUtilsFactory}
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type paramter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPFragmentationFactoryNodeDialog<T, U>
		extends DefaultNodeSettingsPane {

	protected FragmentationUtilsFactory<T, U> fragUtilFactory;
	protected final int version;

	// /**
	// * Convenience constructor, for non-multicut nodes
	// *
	// * @param fragUtilityFactory
	// * The {@link FragmentationUtilsFactory} instance
	// */
	// public AbstractMMPFragmentationFactoryNodeDialog(
	// FragmentationUtilsFactory<T, U> fragUtilityFactory) {
	// this(fragUtilityFactory, false, version);
	// }

	/**
	 * Convenience constructor, for nodes with a removeH's option
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Does the node have only 1 cut number or does it do 1-n cuts?
	 * @param version
	 *            TODO
	 */
	public AbstractMMPFragmentationFactoryNodeDialog(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, int version) {
		this(fragUtilityFactory, isMulticut, true, true, true, version);
	}

	/**
	 * Convenience constructor, for nodes with a number of cuts option
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Does the node have only 1 cut number or does it do 1-n cuts?
	 * @param hasRemoveHs
	 *            Does the node have a remove H's option?
	 */
	public AbstractMMPFragmentationFactoryNodeDialog(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs) {
		this(fragUtilityFactory, isMulticut, hasRemoveHs, true);
	}

	/**
	 * Convenience constructor, for nodes with a 'two cuts to single bond'
	 * option
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Does the node have only 1 cut number or does it do 1-n cuts?
	 * @param hasRemoveHs
	 *            Does the node have a remove H's option?
	 * @param hasNumCuts
	 *            Does the node have a 'Number of cuts' option?
	 */
	public AbstractMMPFragmentationFactoryNodeDialog(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts) {
		this(fragUtilityFactory, isMulticut, hasRemoveHs, hasNumCuts, true);
	}

	/**
	 * Full constructor. The node dialog is build by calling the following
	 * methods in order:
	 * <ul>
	 * <li>{@link #addColumnSelectors()}</li>
	 * <li>{@link #addFragmentationTypeSelector()}</li>
	 * <li>{@link #addNumberOfCutsOptions(boolean, boolean, boolean, boolean)}</li>
	 * </ul>
	 * 
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Does the node have only 1 cut number or does it do 1-n cuts?
	 * @param hasRemoveHs
	 *            Does the node have a remove H's option?
	 * @param hasNumCuts
	 *            Does the node have a 'Number of cuts' option?
	 * @param hasTwoCutsToBond
	 *            Does the node have the option to perform two cuts to a single
	 *            bond?
	 */
	public AbstractMMPFragmentationFactoryNodeDialog(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			boolean hasTwoCutsToBond) {
		this(fragUtilityFactory, isMulticut, hasRemoveHs, hasNumCuts,
				hasTwoCutsToBond, -1);
	}

	/**
	 * Full constructor. The node dialog is build by calling the following
	 * methods in order:
	 * <ul>
	 * <li>{@link #addColumnSelectors()}</li>
	 * <li>{@link #addFragmentationTypeSelector()}</li>
	 * <li>{@link #addNumberOfCutsOptions(boolean, boolean, boolean, boolean)}</li>
	 * </ul>
	 * 
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Does the node have only 1 cut number or does it do 1-n cuts?
	 * @param hasRemoveHs
	 *            Does the node have a remove H's option?
	 * @param hasNumCuts
	 *            Does the node have a 'Number of cuts' option?
	 * @param hasTwoCutsToBond
	 *            Does the node have the option to perform two cuts to a single
	 *            bond?
	 */
	public AbstractMMPFragmentationFactoryNodeDialog(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			boolean hasTwoCutsToBond, int version) {
		this.version = version;
		this.fragUtilFactory = fragUtilityFactory;
		renameTab("Options", "Molecule & Fragmentation Options");
		addColumnSelectors();
		addFragmentationTypeSelector();
		addNumberOfCutsOptions(isMulticut, hasRemoveHs, hasNumCuts,
				hasTwoCutsToBond);

	}

	/**
	 * Method to add the column selectors. This method adds a molecule selector
	 * column, and then calls {@link #addAdditionalColumnSelectors()}
	 */
	private final void addColumnSelectors() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				createMolColumnSettingsModel(), "Select Molecule column", 0,
				fragUtilFactory.getInputColumnTypes()));
		addAdditionalColumnSelectors();
	}

	/**
	 * Hook to allow subclasses to add additional column selectors immediately
	 * after the molecule column selector
	 */
	protected void addAdditionalColumnSelectors() {

	}

	/**
	 * This method adds the fragmentation type selector
	 */
	private final void addFragmentationTypeSelector() {
		final SettingsModelString fragmentationTypeMdl = createSMIRKSModel();
		final SettingsModelString customSMARTSMdl = createCustomSMARTSModel();
		fragmentationTypeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				customSMARTSMdl.setEnabled(
						FragmentationTypes.valueOf(fragmentationTypeMdl
								.getStringValue()) == FragmentationTypes.USER_DEFINED);
			}
		});

		customSMARTSMdl
				.setEnabled(FragmentationTypes.valueOf(fragmentationTypeMdl
						.getStringValue()) == FragmentationTypes.USER_DEFINED);

		createNewGroup("Select the fragmentation type");
		addDialogComponent(new DialogComponentButtonGroup(fragmentationTypeMdl,
				null, true, FragmentationTypes.values()));
		addDialogComponent(
				new DialogComponentString(customSMARTSMdl, "User SMARTS:"));
		closeCurrentGroup();
	}

	/**
	 * Method adds the number of cuts options * @param isMulticut Does the node
	 * have only 1 cut number or does it do 1-n cuts?
	 * 
	 * @param hasRemoveHs
	 *            Does the node have a remove H's option?
	 * @param hasNumCuts
	 *            Does the node have a 'Number of cuts' option?
	 * @param hasTwoCutsToBond
	 *            Does the node have the option to perform two cuts to a single
	 *            bond?
	 */
	protected void addNumberOfCutsOptions(boolean isMulticut,
			boolean hasRemoveHs, boolean hasNumCuts, boolean hasTwoCutsToBond) {
		SettingsModelIntegerBounded numCutsMdl =
				hasNumCuts ? createCutsModel() : null;
		SettingsModelBoolean allowTwoCutsToBondValueMdl =
				hasTwoCutsToBond ? createAllowTwoCutsToBondValueModel() : null;
		SettingsModelBoolean addHsMdl = createAddHModel();
		SettingsModelBoolean stripHsMdl =
				hasRemoveHs ? createStripHModel() : null;
		if (hasNumCuts) {
			numCutsMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					if (!isMulticut) {
						addHsMdl.setEnabled(numCutsMdl.getIntValue() == 1);
					}
					if (hasRemoveHs) {
						stripHsMdl.setEnabled(addHsMdl.isEnabled()
								&& addHsMdl.getBooleanValue());
					}
					if (hasTwoCutsToBond) {
						allowTwoCutsToBondValueMdl.setEnabled(
								(isMulticut && numCutsMdl.getIntValue() >= 2)
										|| numCutsMdl.getIntValue() == 2);
					}

				}
			});

			if (!isMulticut) {
				addHsMdl.setEnabled(numCutsMdl.getIntValue() == 1);
			}
		}
		if (hasRemoveHs) {
			addHsMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					stripHsMdl.setEnabled(
							addHsMdl.isEnabled() && addHsMdl.getBooleanValue());
				}
			});
			stripHsMdl.setEnabled(
					addHsMdl.isEnabled() && addHsMdl.getBooleanValue());
		}
		if (hasNumCuts) {
			if (hasTwoCutsToBond) {
				allowTwoCutsToBondValueMdl.setEnabled(
						(isMulticut && numCutsMdl.getIntValue() >= 2)
								|| numCutsMdl.getIntValue() == 2);

			}

			addDialogComponent(new DialogComponentNumber(numCutsMdl,
					(isMulticut ? "Maximum n" : "N") + "umber of cuts", 1));
		}
		if (hasTwoCutsToBond) {
			addDialogComponent(new DialogComponentBoolean(
					allowTwoCutsToBondValueMdl,
					"Allow 2 cuts along single bond giving a single bond as 'value'?"));
		}
		createNewGroup("Explicit Hydrogens");
		addDialogComponent(new DialogComponentLabel(
				"The user is strongly advised to leave these setting at their recommended (default) values"));
		addDialogComponent(new DialogComponentBoolean(addHsMdl,
				"Add H's prior to fragmentation (Recommended for n"
						+ (isMulticut ? ">" : "") + "=1)"));
		if (hasRemoveHs) {
			addDialogComponent(new DialogComponentBoolean(stripHsMdl,
					"Remove Added Explicit H's from output (Recommended)"));
			addDialogComponent(new DialogComponentButtonGroup(createIncomingExplicitHsModel(),
					"Incoming explicit H's treatment", true, IncomingExplicitHsOption.values()));
		}
		closeCurrentGroup();
	}

	/**
	 * @return The settings model for the incoming explicit H's behaviour
	 */
	static SettingsModelString createIncomingExplicitHsModel() {
		return new SettingsModelString(
				MMPConstants.INCOMING_EXPLICIT_H_S_OPTION,
				IncomingExplicitHsOption.getDefault().getActionCommand());
	}

	/**
	 * Create Settings Model for option to allow 2 cuts to generate a single
	 * bond as a value
	 */
	static SettingsModelBoolean createAllowTwoCutsToBondValueModel() {
		return new SettingsModelBoolean(MMPConstants.TWO_CUTS_TO_BOND_VALUE,
				MMPConstants.DEFAULT_ALLOW_2_CUTS_TO_SINGLE_BOND);
	}

	/** Create model for customReaction SMARTS */
	static SettingsModelString createCustomSMARTSModel() {
		return new SettingsModelString(MMPConstants.CUSTOM_R_SMARTS, null);
	}

	/**
	 * Create the settings model for the molecule column
	 */
	static SettingsModelString createMolColumnSettingsModel() {
		return new SettingsModelString(MMPConstants.MOLECULE_COLUMN, null);
	}

	/**
	 * Create the settings model for the number of cuts
	 */
	static SettingsModelIntegerBounded createCutsModel() {
		return new SettingsModelIntegerBounded(MMPConstants.NUMBER_OF_CUTS,
				MMPConstants.MAXIMUM_NUMBER_OF_CUTS,
				MMPConstants.MINIMUM_NUMBER_OF_CUTS,
				MMPConstants.MAXIMUM_NUMBER_OF_CUTS);
	}

	/** Create the settings model for the option to add hydrogens */
	static SettingsModelBoolean createAddHModel() {
		return new SettingsModelBoolean(MMPConstants.ADD_HS,
				MMPConstants.DEFAULT_ADD_H);
	}

	/** Create the settings model for the strip H's at the end option */
	static SettingsModelBoolean createStripHModel() {
		return new SettingsModelBoolean(
				MMPConstants.REMOVE_EXPLICIT_H_S_FROM_OUTPUT,
				MMPConstants.DEFAULT_REMOVE_H_POST_FRAGMENTATION);
	}

	/** Create settings model for the SMIRKS fragmentation */
	static SettingsModelString createSMIRKSModel() {
		return new SettingsModelString(MMPConstants.FRAGMENTATION_SMIRKS,
				FragmentationTypes.getDefaultMethod().getActionCommand());
	}

}
