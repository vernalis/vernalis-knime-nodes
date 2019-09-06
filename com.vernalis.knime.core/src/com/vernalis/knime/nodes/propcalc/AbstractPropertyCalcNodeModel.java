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
package com.vernalis.knime.nodes.propcalc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.knime.nodes.propcalc.AbstractPropertyCalcNodeDialog.createColumnNameModel;
import static com.vernalis.knime.nodes.propcalc.AbstractPropertyCalcNodeDialog.createPropertiesModel;

/**
 * Abstract NodeModel to calculate properties based on a
 * {@link CalculatedPropertyInterface} implementation. The node will calculated
 * the properties via a {@link ColumnRearranger} in parallel. The user needs to
 * supply an array of the possible property values at construction, and to
 * implement the abstract {@link #getContainerObjFromCell(DataCell)} method,
 * returning the object in the appropriate format.
 * 
 * @author s.roughley knime@vernalis.com
 * @param <T>
 *            The type of the object required by the
 *            {@link CalculatedPropertyInterface} implementation's
 *            {@code #calculate(T input)} method.
 */
public abstract class AbstractPropertyCalcNodeModel<T>
		extends AbstractSimpleStreamableFunctionNodeModel {

	// Property Keys for column properties
	private static final String UNITS_KEY = "Units";
	private static final String MAXIMUM_VALUE_KEY = "Maximum Value";
	private static final String MINIMUM_VALUE_KEY = "Minimum Value";
	private static final String ALIAS_KEY = "Alias";
	private static final String REFERENCE_KEY = "Reference";
	private static final String DESCRIPTION_KEY = "Description";

	// Minimum z coordinate for non-2D coordinates
	protected static final double MAX_ZERO_Z = 0.0001;

	// Settings models
	protected final SettingsModelString colNameMdl;
	protected final SettingsModelStringArray selectedPropertiesMdl;
	protected CalculatedPropertyInterface<T>[] possProps;
	protected final ColumnFilter acceptedColumnsFilter;

	protected int colIdx;

	/**
	 * Main constructor. Provides a 1-in 1-out node with column rearranger. The
	 * user must supply the full list of possible values. Assuming that
	 * {@link CalculatedPropertyInterface} is implemented as an {@link Enum},
	 * this can be obtained by calling the #values() method.
	 * 
	 * @param columnLabel
	 *            The name of the column label to be used as the settings model
	 *            key
	 * @param propertyLabel
	 *            The name properties label to be used as the settings model key
	 * @param possibleProps
	 *            The properties to be calculated
	 * @param acceptedColumns
	 *            A {@link ColumnFilter} for the acceptable input types
	 * 
	 */
	protected AbstractPropertyCalcNodeModel(String columnLabel,
			String propertyLabel,
			CalculatedPropertyInterface<T>[] possibleProps,
			ColumnFilter acceptedColumns) {
		super();
		possProps = possibleProps;
		List<String> propNames = new ArrayList<>();
		for (CalculatedPropertyInterface<T> prop : possProps) {
			propNames.add(prop.getName());
		}
		selectedPropertiesMdl = registerSettingsModel(
				createPropertiesModel(propertyLabel, propNames));

		colNameMdl = registerSettingsModel(createColumnNameModel(columnLabel));
		acceptedColumnsFilter = acceptedColumns;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.nodes.
	 * AbstractSimpleStreamableFunctionNodeModel#validateSettings(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			super.validateSettings(settings);
		} catch (InvalidSettingsException e) {
			try {
				// Check it is the right model causing a problem
				selectedPropertiesMdl.validateSettings(settings);
			} catch (InvalidSettingsException e1) {
				if (!settings.containsKey("Selected properties")) {
					throw e1;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.nodes.
	 * AbstractSimpleStreamableFunctionNodeModel#loadValidatedSettingsFrom(org.
	 * knime.core.node.NodeSettingsRO)
	 */
	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			super.loadValidatedSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			try {
				// Check it is the right model causing a problem
				selectedPropertiesMdl.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e1) {
				// Return the legacy value
				selectedPropertiesMdl.setStringArrayValue(
						settings.getStringArray("Selected properties"));
				setWarningMessage("Loaded legacy settings...");

			}
		}
	}

	/**
	 * Column Rearranger method to generate the output tables
	 * 
	 * @param inSpec
	 *            The incoming table spec
	 * @return A {@link ColumnRearranger} to generate the output table
	 * @throws InvalidSettingsException
	 *             if there is a problem with the configuration
	 */
	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec)
			throws InvalidSettingsException {

		colIdx = getValidatedColumnSelectionModelColumnIndex(colNameMdl,
				acceptedColumnsFilter, inSpec, getLogger());

		// List the selected properties
		final List<CalculatedPropertyInterface<T>> propMembers =
				getSelectedProperties();

		// Now generate the new column specs
		ColumnRearranger rearranger = new ColumnRearranger(inSpec);

		rearranger.append(getCellFactory(propMembers,
				createNewColumnSpecs(inSpec, propMembers)));
		return rearranger;
	}

	/**
	 * Method to actually get the cell factory to create the property cells.
	 * Subclasses may override if for example a post-calculation clean-up is
	 * required after each row
	 * 
	 * @param selectedProperties
	 *            The properties to calculate
	 * @param newColSpecs
	 *            The output column specs
	 * @return A CellFactory
	 */
	protected CellFactory getCellFactory(
			final List<CalculatedPropertyInterface<T>> selectedProperties,
			DataColumnSpec[] newColSpecs) {
		return new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] retVal =
						ArrayUtils.fill(new DataCell[selectedProperties.size()],
								DataType.getMissingCell());

				DataCell objCell = row.getCell(colIdx);
				if (objCell.isMissing()) {
					return retVal;
				}
				T obj;
				try {
					obj = getContainerObjFromCell(objCell);
				} catch (RowExecutionException e1) {
					obj = null;
					getLogger().info("Unable to convert object in row '"
							+ row.getKey().getString() + "' - "
							+ e1.getMessage());
				}

				if (obj != null) {
					for (int i = 0; i < selectedProperties.size(); i++) {
						try {
							retVal[i] =
									selectedProperties.get(i).calculate(obj);
						} catch (Exception e) {
							// We do nothing - the property was undefined for
							// the molecule
						}
					}
				}
				return retVal;
			}
		};
	}

	/**
	 * @return The selected properties
	 */
	protected List<CalculatedPropertyInterface<T>> getSelectedProperties() {
		List<CalculatedPropertyInterface<T>> retVal = new ArrayList<>();
		for (String selectedPropName : selectedPropertiesMdl
				.getStringArrayValue()) {
			for (CalculatedPropertyInterface<T> possProp : possProps) {
				// This clunky workaround is because I cant figure how to get to
				// T.valueOf(props[i])
				if (possProp.getName().equals(selectedPropName)
						|| /* Handle also older settings */possProp.getName()
								.replace(" ", "_").equals(selectedPropName)) {
					retVal.add(possProp);
					break;
				}
			}
		}
		return retVal;
	}

	/**
	 * @param inSpec
	 *            The incoming table spec
	 * @param props
	 *            The selected properties
	 * @return The new columns specs
	 */
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec inSpec,
			final List<CalculatedPropertyInterface<T>> props) {
		DataColumnSpec[] newColSpec = new DataColumnSpec[props.size()];
		int colIdx = 0;
		for (CalculatedPropertyInterface<T> prop : props) {
			DataColumnSpecCreator colSpecCreator = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(inSpec,
							prop.getName().replace("_", " ") + " ("
									+ colNameMdl.getStringValue() + ")"),
					getColumnType(prop, inSpec));
			Map<String, String> colPropMap = new TreeMap<>();
			if (prop.getDescription() != null
					&& !prop.getDescription().isEmpty()) {
				colPropMap.put(DESCRIPTION_KEY, prop.getDescription());
			}
			String[] propVals = prop.getReferences();
			if (propVals != null && propVals.length > 0) {
				colPropMap.put(REFERENCE_KEY, propVals[0]);
				for (int i = 1; i < propVals.length; i++) {
					if (propVals[i] != null && !"".equals(propVals[i])) {
						colPropMap.put(
								String.format("%s_%d", REFERENCE_KEY, (i - 1)),
								propVals[i]);
					}
				}
			}

			propVals = prop.getAliases();
			if (propVals != null && propVals.length > 0) {
				colPropMap.put(ALIAS_KEY, propVals[0]);
				for (int i = 1; i < propVals.length; i++) {
					if (propVals[i] != null && !"".equals(propVals[i])) {
						colPropMap.put(
								String.format("%s_%d", ALIAS_KEY, (i - 1)),
								propVals[i]);
					}
				}
			}

			if (prop.getMinimum() != null) {
				colPropMap.put(MINIMUM_VALUE_KEY, "" + prop.getMinimum());
			}
			if (prop.getMaximum() != null) {
				colPropMap.put(MAXIMUM_VALUE_KEY, "" + prop.getMaximum());
			}
			if (prop.getUnits() != null) {
				colPropMap.put(UNITS_KEY, prop.getUnits());
			}
			if (colPropMap.size() > 0) {
				colSpecCreator
						.setProperties(new DataColumnProperties(colPropMap));
			}
			newColSpec[colIdx++] = colSpecCreator.createSpec();
		}
		return newColSpec;
	}

	/**
	 * @param prop
	 *            The property to return the type for
	 * @param inSpec
	 *            The incoming table spec.
	 * @return The column Type for the specified property. The default
	 *         implementation returns <code>null</code> if a call to
	 *         {@link CalculatedPropertyInterface#getType()} returns
	 *         <code>null</code>. Nodes with possible properties which return
	 *         <code>null</code> should override this method. At the time of
	 *         calling this method, {@link #colIdx} will be populated with the
	 *         index of the selected input column
	 */
	protected DataType getColumnType(CalculatedPropertyInterface<T> prop,
			DataTableSpec inSpec) {
		return prop.getType();
	}

	/**
	 * Method to return the correct molecule object given the supplied
	 * {@link DataCell}. The node implementation guarantees that the objCell
	 * will not be a missing cell. Implementations should return {@code null} if
	 * the object could not be parsed into the correct format.
	 * 
	 * @param objCell
	 *            The object cell
	 * @return The object, or null
	 * @throws RowExecutionException
	 */
	protected abstract T getContainerObjFromCell(DataCell objCell)
			throws RowExecutionException;

}
