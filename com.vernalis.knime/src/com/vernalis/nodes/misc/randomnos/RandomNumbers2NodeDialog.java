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
package com.vernalis.nodes.misc.randomnos;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentDoubleRange;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "RandomNumbers" Node. A node to generate a
 * table with a single column of random numbers
 */
public class RandomNumbers2NodeDialog extends DefaultNodeSettingsPane {

	private static final String RANDOM_SEED = "Random Seed";

	/**
	 * New pane for configuring the RandomNumbers node.
	 */
	protected RandomNumbers2NodeDialog() {
		super();
		addDialogComponent(new DialogComponentString(
				new SettingsModelString(RandomNumbersNodeModel.CFG_COLUMN_NAME,
						"Random Values"),
				"Output Column Name"));

		addDialogComponent(new DialogComponentButtonGroup(
				new SettingsModelString(RandomNumbersNodeModel.CFG_TYPE,
						"Double"),
				false, "Output Type", new String[] { "Double", "Integer" }));

		addDialogComponent(new DialogComponentDoubleRange(
				new SettingsModelDoubleRange(RandomNumbersNodeModel.CFG_MIN_MAX,
						0.0, 100000.0),
				-1000000000.0, 1000000000.0, 1, "Enter value range"));

		addDialogComponent(new DialogComponentNumberEdit(
				new SettingsModelLongBounded(RandomNumbersNodeModel.CFG_N, 100,
						1, Long.MAX_VALUE),
				"Number of values"));

		addDialogComponent(
				new DialogComponentBoolean(
						new SettingsModelBoolean(
								RandomNumbersNodeModel.CFG_UNIQUE, true),
						"Unique values?"));

		addDialogComponent(
				new DialogComponentNumberEdit(createSeedModel(), RANDOM_SEED));

	}

	static SettingsModelLongBounded createSeedModel() {
		return new SettingsModelLongBounded(RANDOM_SEED, -1, -1,
				Long.MAX_VALUE);
	}
}
