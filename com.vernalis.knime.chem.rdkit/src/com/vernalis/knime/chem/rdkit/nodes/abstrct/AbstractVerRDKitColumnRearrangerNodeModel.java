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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.RDKit.ROMol;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

import static com.vernalis.knime.chem.rdkit.nodes.abstrct.AbstractVerRDKitNodeDialog.createColumnNameModel;
import static com.vernalis.knime.chem.rdkit.nodes.abstrct.AbstractVerRDKitNodeDialog.createRemoveInputColModel;

/**
 * The abstract node model class for internal webservices.
 * <p>
 * Subclasses need to provide the {@link NodeLogger} instance, and arrays
 * containing the new column name(s) and {@link DataType}s, along with
 * {@link #m_colFormats} - the acceptable input column formats.
 * </p>
 * <p>
 * The default {@link #configure(DataTableSpec[])} method checks the column type
 * is selected matching the allowed formats
 * </p>
 * <p>
 * The {@link #execute(BufferedDataTable[], ExecutionContext)} method generates
 * the output table using a column rearranger, which gets its results from the
 * {@link #getResultsFromRDKitObject(Object, long)} method, which needs to be
 * implemented.
 * </p>
 * <p>
 * During construction, the following must be initialised:
 * <ul>
 * <li>{@link #m_colFormats} - The acceptable column formats</li>
 * <li>{@link #m_newColNames} - The new column names</li>
 * <li>{@link #m_newColTypes} - The new column {@link DataType}s</li>
 * </ul>
 * 
 * @author Stephen Roughley knime@vernalis.com
 * 
 * @since v1.34.0
 */
public abstract class AbstractVerRDKitColumnRearrangerNodeModel<T>
		extends AbstractSimpleStreamableFunctionNodeModel {

	/* Settings Models */
	protected final SettingsModelString m_colName =
			registerSettingsModel(createColumnNameModel());
	protected final SettingsModelBoolean m_removeInputCol =
			registerSettingsModel(createRemoveInputColModel());

	// The output spec
	// protected DataTableSpec m_spec;

	/** The acceptable molecule input formats */
	protected ColumnFilter m_colFormats;

	/** The new column names */
	protected String[] m_newColNames;

	/** The new column types */
	protected DataType[] m_newColTypes;

	protected final boolean failOnError;

	private final SWIGObjectGarbageCollector2WaveSupplier m_swigGC =
			new SWIGObjectGarbageCollector2WaveSupplier();
	/**
	 * Map of properties for added columns.
	 * <p>
	 * The Key of the {@link HashMap} is the new column name. (If this does not
	 * exist in the output table, then it will be ignored)
	 * </p>
	 * <p>
	 * The {@link TreeMap} contains the Property names as its keys, and property
	 * values as it's keys
	 * </p>
	 * 
	 */
	protected HashMap<String, TreeMap<String, String>> m_columnProperties =
			new HashMap<>();

	/**
	 * Complex constructor allowing direct used of Abstract NodeModel as
	 * anonymous inner type in node factory
	 * 
	 * @param newColNames
	 *            The name of the output column(s)
	 * @param newColTypes
	 *            The {@link DataType} of the new column(s)
	 * @param colFormats
	 *            The optional column type filters
	 */
	public AbstractVerRDKitColumnRearrangerNodeModel(String[] newColNames,
			DataType[] newColTypes, ColumnFilter colFormats) {
		this(newColNames, newColTypes, colFormats, 1);
	}

	public AbstractVerRDKitColumnRearrangerNodeModel(String[] newColNames,
			DataType[] newColTypes, ColumnFilter colFormats,
			int nodeSettingsVersion) {
		this(newColNames, newColTypes, Collections.emptyMap(), colFormats,
				nodeSettingsVersion);
	}

	public AbstractVerRDKitColumnRearrangerNodeModel(String[] newColNames,
			DataType[] newColTypes,
			Map<String, Map<String, String>> newColProps,
			ColumnFilter colFormats) {
		this(newColNames, newColTypes, newColProps, colFormats, 1);
	}

	public AbstractVerRDKitColumnRearrangerNodeModel(String[] newColNames,
			DataType[] newColTypes,
			Map<String, Map<String, String>> newColProps,
			ColumnFilter colFormats, int nodeSettingsVersion) {
		this(newColNames, newColTypes, newColProps, colFormats, false,
				nodeSettingsVersion);
	}

	public AbstractVerRDKitColumnRearrangerNodeModel(String[] newColNames,
			DataType[] newColTypes,
			Map<String, Map<String, String>> newColProps,
			ColumnFilter colFormats, boolean failOnError) {
		this(newColNames, newColTypes, newColProps, colFormats, failOnError, 1);
	}

	public AbstractVerRDKitColumnRearrangerNodeModel(String[] newColNames,
			DataType[] newColTypes,
			Map<String, Map<String, String>> newColProps,
			ColumnFilter colFormats, boolean failOnError,
			int nodeSettingsVersion) {
		super(nodeSettingsVersion);
		m_colFormats = colFormats;
		m_newColNames = newColNames;
		m_newColTypes = newColTypes;
		this.failOnError = failOnError;
		m_columnProperties = new HashMap<>();
		for (Entry<String, Map<String, String>> colProp : newColProps
				.entrySet()) {
			final TreeMap<String, String> propMap = new TreeMap<>();
			for (Entry<String, String> prop : colProp.getValue().entrySet()) {
				if (prop.getValue() != null) {
					propMap.put(prop.getKey(), prop.getValue());
				}
			}
			if (!propMap.isEmpty()) {
				m_columnProperties.put(colProp.getKey(), propMap);
			}
		}
	}

	/**
	 * Method to create the column rearranger. Handles the adding of columns and
	 * their types. Results are added based on the
	 * {@link #getResultsFromRDKitObject(ROMol, long)} method, which needs to be
	 * implemented by subclasses.
	 * 
	 * @param spec
	 *            The input data table spec
	 * 
	 * @return The column rearranger
	 * 
	 * @throws InvalidSettingsException
	 */
	@Override
	protected ColumnRearranger createColumnRearranger(final DataTableSpec spec)
			throws InvalidSettingsException {

		String msg = checkSettings(spec);
		if (msg != null) {
			throw new InvalidSettingsException(msg);
		}

		ColumnRearranger result = new ColumnRearranger(spec);
		DataColumnSpec[] newSpecs = new DataColumnSpec[m_newColNames.length];
		for (int i = 0; i < m_newColNames.length; i++) {
			DataColumnSpecCreator appendSpecCreator = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, m_newColNames[i]),
					m_newColTypes[i]);
			final TreeMap<String, String> colProps =
					m_columnProperties.get(m_newColNames[i]);
			appendSpecCreator.setProperties(colProps == null ? null
					: new DataColumnProperties(colProps));
			newSpecs[i] = appendSpecCreator.createSpec();
		}
		if (m_removeInputCol.getBooleanValue()) {
			result.remove(new String[] { m_colName.getStringValue() });
		}

		result.append(getCellFactory(spec, newSpecs));
		return result;
	}

	/**
	 * Method to check the node settings. Any column selections should be
	 * checked a suitable guess made if there is no selection. The default
	 * implementation checks the column name setting
	 * 
	 * @param spec
	 *            The incoming table spec
	 * 
	 * @return <code>null</code> if there are no errors in the settings,
	 *         otherwise a meaningful error message
	 */
	protected String checkSettings(final DataTableSpec spec) {
		DataColumnSpec colSpec = spec.getColumnSpec(m_colName.getStringValue());
		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = spec.getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (m_colFormats.includeColumn(spec.getColumnSpec(i))) {
					m_colName.setStringValue(spec.getColumnSpec(i).getName());
					getLogger().warn("No column selected. "
							+ m_colName.getStringValue() + " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					return "No molecule column of the accepted"
							+ " input formats was found.";
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!m_colFormats.includeColumn(colSpec)) {
				// The column is not compatible with one of the accepted types
				return "The column " + m_colName.getStringValue()
						+ " is not one of the accepted" + " input formats";
			}
		}
		return null;
	}

	/**
	 * The default implementation returns a cell factory which adds cells based
	 * on an RDKit object of type <T>, which is obtained from a single cell in
	 * the row via a call to {@link #getRDKitObjectFromCell(DataCell, long)},
	 * and subsequently {@link #getResultsFromRDKitObject(Object, long)}
	 * 
	 * @param spec
	 *            The incoming table spec
	 * @param newSpecs
	 *            The new column specs
	 * 
	 * @return an {@link AbstractCellFactory} implementation to return the new
	 *         cells for the row
	 */
	protected AbstractCellFactory getCellFactory(final DataTableSpec spec,
			DataColumnSpec[] newSpecs) {
		return new AbstractCellFactory(newSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell inCell = row.getCell(
						spec.findColumnIndex(m_colName.getStringValue()));
				DataCell[] outCells = new DataCell[m_newColNames.length];
				Arrays.fill(outCells, DataType.getMissingCell());
				if (inCell.isMissing()) {
					return outCells;
				}

				long wave = getGC().getNextWaveIndex();
				try {
					T mol = getRDKitObjectFromCell(inCell, wave);
					outCells = getResultsFromRDKitObject(mol, wave);
				} catch (RowExecutionException e) {
					// Append the row ID and pass the error onwards and upwards
					String errMsg = e.getMessage() + " (Row: "
							+ row.getKey().toString() + ")";
					setWarningMessage(errMsg);
					if (failOnError) {
						throw new RuntimeException(errMsg);
					}
				} finally {
					getGC().cleanupMarkedObjects(wave);
				}
				return outCells;
			}
		};
	}

	/**
	 * This method needs to return the result of the processing the molecule
	 * (never {@code null})
	 * 
	 * @param mol
	 *            The input cell from the selected column
	 * @param wave
	 *            Wave index, should it be required for native object clean-up
	 *            (use {@link #getGC()} for garbage collector)
	 * 
	 * @return The result in a DataCell
	 */
	protected abstract DataCell[] getResultsFromRDKitObject(T mol, long wave)
			throws RowExecutionException;

	/**
	 * Returns an ROMol from a {@link DataCell} using the appropriate method for
	 * the cell type
	 * 
	 * @param cell
	 *            Input DataCell (should be RDKit, MOL, SDF or SMILES)
	 * @param wave
	 *            Wave index, should it be required for native object clean-up
	 *            (use {@link #getGC()} for garbage collector)
	 * 
	 * @return ROMol object containing the molecule
	 * 
	 * @throws RowExecutionException
	 *             Thrown if molecule parsing fails
	 * 
	 * @see #getChemicalReactionFromCell(DataCell)
	 */
	protected abstract T getRDKitObjectFromCell(DataCell cell, long wave)
			throws RowExecutionException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		getGC().cleanupMarkedObjects();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		getGC().cleanupMarkedObjects();
		super.onDispose();
	}

	/**
	 * @return the m_swigGC
	 */
	protected SWIGObjectGarbageCollector2WaveSupplier getGC() {
		return m_swigGC;
	}

}
