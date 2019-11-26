/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.plot.nodes.kerneldensity;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

/**
 * The node dialog panel for the Kernel Loop Start Node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelLoopStartNodeDialog extends DefaultNodeSettingsPane {

	static final String IS_MULTI_DIMENSIONAL = "Is Multi-dimensional?";
	static final String KERNEL_SYMMETRIES = "Kernel Symmetries";
	static final String KERNEL_ESTIMATORS = "Kernel Estimators";

	/**
	 * Constructor
	 */
	public KernelLoopStartNodeDialog() {
		super();
		addDialogComponent(new DialogComponentStringListSelection(
				createSelectedKernelEstimatorsModel(), KERNEL_ESTIMATORS,
				Arrays.stream(KernelEstimator.names())
						.collect(Collectors.toList()),
				true, KernelEstimator.names().length));
		final SettingsModelBoolean isMultimensionalMdl =
				createMultimensionalModel();
		final SettingsModelStringArray selectedKernelSymmetriesMdl =
				createSelectedKernelSymmetriesModel();
		isMultimensionalMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				selectedKernelSymmetriesMdl
						.setEnabled(isMultimensionalMdl.getBooleanValue());

			}
		});
		selectedKernelSymmetriesMdl
				.setEnabled(isMultimensionalMdl.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(isMultimensionalMdl,
				IS_MULTI_DIMENSIONAL));
		addDialogComponent(new DialogComponentStringListSelection(
				selectedKernelSymmetriesMdl, KERNEL_SYMMETRIES,
				Arrays.stream(KernelSymmetry.names())
						.collect(Collectors.toList()),
				true, KernelSymmetry.names().length));
	}

	/**
	 * @return Settings Model for the is multidimensional option
	 */
	static SettingsModelBoolean createMultimensionalModel() {
		return new SettingsModelBoolean(IS_MULTI_DIMENSIONAL, true);
	}

	/**
	 * @return Settings Model for the kernel estimators option
	 */
	static SettingsModelStringArray createSelectedKernelEstimatorsModel() {
		return new SettingsModelStringArray(KERNEL_ESTIMATORS,
				KernelEstimator.names());
	}

	/**
	 * @return Settings Model for the kernel symmetries option
	 */
	static SettingsModelStringArray createSelectedKernelSymmetriesModel() {
		return new SettingsModelStringArray(KERNEL_SYMMETRIES,
				KernelSymmetry.names());
	}

}
