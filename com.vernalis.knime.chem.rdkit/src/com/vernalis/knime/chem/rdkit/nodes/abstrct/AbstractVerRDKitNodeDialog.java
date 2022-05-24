/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.rdkit.nodes.abstrct;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;

/**
 * <p>
 * Abstract class for Vernalis RDKit node dialogs. Has several constructors
 * which all add a column selector and an option to remove the input column
 * </p>
 * <p>
 * Also provides helper functions for adding some commonly used dialog
 * components, and their associate Settings models
 * <ul>
 * <li>{@link #addXYZDialog()} An (X, Y, Z) input</li>
 * <li>{@link #addAngleDialog()} An angle input (with option for degrees)</li>
 * <li>{@link #addIgnoreHsDialog()} An ignore hydrogens input</li>
 * </ul>
 * 
 * @author Stephen Roughley knime@vernalis.com
 * 
 * @since v1.34.0
 */
public class AbstractVerRDKitNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * Default constructor - puts a column selector with the caption 'Select
	 * input column' in the node dialog. Adds an option to remove the input
	 * column. Default Molecule types filter is applied
	 * 
	 */
	public AbstractVerRDKitNodeDialog() {
		this("Select input column", RdkitCompatibleColumnFormats.MOL_ANY);
	}

	/**
	 * Puts a column selector with a user-specified caption in the node dialog.
	 * Adds an option to remove the input column.
	 * 
	 * @param colSelectLabel
	 *            The caption for the column selector
	 * @param colFormats
	 *            Optional column formats filter
	 */
	public AbstractVerRDKitNodeDialog(String colSelectLabel,
			ColumnFilter colFormats) {
		this(colSelectLabel, null, colFormats);
	}

	/**
	 * Puts a column selector and option to remove the input column in a group
	 * with the supplier name. RDKit preprocessing options are also included
	 * 
	 * @param colSelectLabel
	 *            The caption for the column selector
	 * @param colSelectGroupName
	 *            The name for the column selection group
	 * @param colFormats
	 *            Optional column formats filter
	 */
	public AbstractVerRDKitNodeDialog(String colSelectLabel,
			String colSelectGroupName, ColumnFilter colFormats) {
		this(colSelectLabel, colSelectGroupName, true, colFormats);
	}

	/**
	 * Puts a column selector and option to remove the input column in an
	 * optional group with the supplier name
	 * 
	 * @param colSelectLabel
	 *            The caption for the column selector
	 * @param colSelectGroupName
	 *            The name for the column selection group. If null, the group is
	 *            not created
	 * @param includeRDKitParsingOptions
	 *            If true, then the keep Hs/sanitize molecule options are
	 *            displayed (this behaviour is the default for other
	 *            constructors)
	 * @param colFormats
	 *            Optional column formats filter
	 */
	public AbstractVerRDKitNodeDialog(String colSelectLabel,
			String colSelectGroupName, boolean includeRDKitParsingOptions,
			ColumnFilter colFormats) {
		this(colSelectLabel, colSelectGroupName, includeRDKitParsingOptions,
				colFormats, false);
	}

	public AbstractVerRDKitNodeDialog(String colSelectLabel,
			String colSelectGroupName, boolean includeRDKitParsingOptions,
			ColumnFilter colFormats, boolean horizontalRemoveCol) {
		super();
		if (colSelectGroupName != null) {
			createNewGroup(colSelectGroupName);
		}
		addMolColumnSelectionDialog(colSelectLabel, includeRDKitParsingOptions,
				colFormats, horizontalRemoveCol);
		if (colSelectGroupName != null) {
			closeCurrentGroup();
		}
	}

	/**
	 * Add a molecule column selection dialog with optional RDKit params
	 * 
	 * @param horizontalRemoveCol
	 *            TODO
	 */
	protected void addMolColumnSelectionDialog(String colSelectLabel,
			boolean includeRDKitParsingOptions, ColumnFilter colFormats,
			boolean horizontalRemoveCol) {

		setHorizontalPlacement(horizontalRemoveCol);

		addDialogComponent(new DialogComponentColumnNameSelection(
				createColumnNameModel(), colSelectLabel, 0, colFormats));
		addDialogComponent(new DialogComponentBoolean(
				createRemoveInputColModel(), "Remove input column"));
		setHorizontalPlacement(false);

		// TODO: Nothing actually using the settings models for these...
		// if (includeRDKitParsingOptions) {
		// addDialogComponent(new DialogComponentBoolean(
		// createSanitizeMolModel(), "Sanitize molecule"));
		// addDialogComponent(new DialogComponentBoolean(createRemoveHsModel(),
		// "Remove hydrogens"));
		// }

	}

	/** Add a dialog to enter (X,Y,Z) parameters */
	protected void addXYZDialog() {
		addDialogComponent(
				new DialogComponentNumber(createXModel(), "X:", 0.1));
		addDialogComponent(
				new DialogComponentNumber(createYModel(), "Y:", 0.1));
		addDialogComponent(
				new DialogComponentNumber(createZModel(), "Z:", 0.1));
	}

	/** Add a dialog to enter an angle and ask if degrees */
	protected void addAngleDialog() {
		addDialogComponent(new DialogComponentNumber(createAngleModel(),
				"Rotation Angle", 10));
		addDialogComponent(new DialogComponentBoolean(createIsDegreesModel(),
				"Angle is Degrees"));
	}

	/** Add a dialog for ignoring hydrogens */
	protected void addIgnoreHsDialog() {
		addDialogComponent(new DialogComponentBoolean(createIgnoreHsModel(),
				"Ignore Hydrogens"));
	}

	/** Add a dialog component for rotating about centroid */
	protected void addRotAboutMoleculeCentroidDialog() {
		addDialogComponent(
				new DialogComponentBoolean(createAbtMolCentroidModel(),
						"Rotate about molecule centroid?"));
	}

	/** Create a model for rotating about centroid */
	public static SettingsModelBoolean createAbtMolCentroidModel() {
		return new SettingsModelBoolean("Rotate about centroid", true);
	}

	/** Create a model for sanitizing the molecule */
	public static SettingsModelBoolean createSanitizeMolModel() {
		return new SettingsModelBoolean("Sanitize molecule", true);
	}

	/** Create a model for removing hydrogens */
	public static SettingsModelBoolean createRemoveHsModel() {
		return new SettingsModelBoolean("Remove hydrogens", false);
	}

	/** Create a model for ignoring hydrogens */
	public static SettingsModelBoolean createIgnoreHsModel() {
		return new SettingsModelBoolean("Ignore H's", false);
	}

	/** Create model for the X-coord */
	public static SettingsModelDouble createXModel() {
		return new SettingsModelDouble("X", 0.0);
	}

	/** Create model for the Y-coord */
	public static SettingsModelDouble createYModel() {
		return new SettingsModelDouble("Y", 0.0);
	}

	/** Create model for the Z-coord */
	public static SettingsModelDouble createZModel() {
		return new SettingsModelDouble("Z", 0.0);
	}

	/** Create the model for isDegrees */
	public static SettingsModelBoolean createIsDegreesModel() {
		return new SettingsModelBoolean("Angle is degrees", true);
	}

	/** Create the model for the angle */
	public static SettingsModelDouble createAngleModel() {
		return new SettingsModelDouble("Rotation angle", 0.0);
	}

	/** Create the model for the column selection name */
	public static final SettingsModelString createColumnNameModel() {
		return new SettingsModelString("Selected Column", "");
	}

	/** Create the model for remove input column */
	public static final SettingsModelBoolean createRemoveInputColModel() {
		return new SettingsModelBoolean("Remove input Column", false);
	}

}
