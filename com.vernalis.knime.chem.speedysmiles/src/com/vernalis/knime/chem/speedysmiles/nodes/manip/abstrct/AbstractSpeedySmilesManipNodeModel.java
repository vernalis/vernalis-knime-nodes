/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct;

import static com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog.createColumnNameModel;
import static com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog.createRemoveInputColumnModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;
import com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct.AbstractSpeedySmilesCountNodeModel;
import com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct.AbstractSpeedySmilesSingleCountNodeModel;

/**
 * This is the base class for all SpeedySMILES nodes which can use a
 * ColumnRearranger to generate output tables
 * <p>
 * The configure method checks that {@link #getColumnNamePrefixes()} is non-null
 * and has at least 1 item. If {@link #getColumnNameSuffixes()} is non-null,
 * then it also checks that the same number of values are returned by both
 * methods. Finally a check is made the {@link #getColumnTypes()} also returns
 * the same number of values. Finally, a Smiles or Smiles-adapatable column is
 * auto-selected if one is available and none is set
 * </p>
 * 
 * <p>
 * The execute method runs via a ColumnRearranger which calls
 * {@link #getResultColumns(String, int)} for each input SMILES
 * </p>
 * 
 * <p>
 * Implementing classes need only implement {@link #getColumnNamePrefixes()},
 * {@link #getColumnNameSuffixes()}, {@link #getColumnTypes()} and
 * {@link #getResultColumns(String, int)}
 * </p>
 * <p>
 * A number of convenience subclasses are provided for single output columns,
 * and 'counts' nodes, where all results are Integers
 * </p>
 * 
 * @see AbstractSpeedySmilesSingleCellManipNodeModel
 * @see AbstractSpeedySmilesCountNodeModel
 * @see AbstractSpeedySmilesSingleCountNodeModel
 * 
 * @author s.roughley
 *
 */
public abstract class AbstractSpeedySmilesManipNodeModel extends SimpleStreamableFunctionNodeModel {
	/** The node logger instance */
	protected NodeLogger m_logger = NodeLogger.getLogger(this.getClass());

	protected final SettingsModelString m_colName = createColumnNameModel();
	protected final SettingsModelBoolean m_removeInputCol;
	protected final boolean m_hasRemoveInputCol;

	/**
	 * Constructor with no 'Remove Input Column' option
	 */
	public AbstractSpeedySmilesManipNodeModel() {
		this(false);
	}

	/**
	 * Constructor for the node model
	 * 
	 * @param hasRemoveInputCol
	 *            Should the node handle a 'Remove Input Column' setting
	 */
	public AbstractSpeedySmilesManipNodeModel(boolean hasRemoveInputCol) {
		super();
		m_hasRemoveInputCol = hasRemoveInputCol;
		if (m_hasRemoveInputCol) {
			m_removeInputCol = createRemoveInputColumnModel();
		} else {
			m_removeInputCol = null;
		}
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		final int smiColIdx = spec.findColumnIndex(m_colName.getStringValue());

		if (m_hasRemoveInputCol && m_removeInputCol.getBooleanValue()) {
			rearranger.remove(m_colName.getStringValue());
		}

		// Generate the new column specs
		DataColumnSpec[] newSpecs = new DataColumnSpec[getColumnNamePrefixes().length];
		for (int i = 0; i < newSpecs.length; i++) {
			String columnName = getColumnNamePrefixes()[i];
			if (getColumnNameSuffixes() != null) {
				columnName += " (" + m_colName.getStringValue() + ")" + getColumnNameSuffixes()[i];
			}
			DataColumnSpecCreator specFact = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, columnName), getColumnTypes()[i]);
			newSpecs[i] = specFact.createSpec();
		}

		rearranger.append(new AbstractCellFactory(true, newSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell cell = row.getCell(smiColIdx);
				String smi = SmilesHelpers.getSmilesFromCell(cell);
				DataCell[] outCells = new DataCell[newSpecs.length];
				Arrays.fill(outCells, DataType.getMissingCell());
				if (smi != null) {
					try {
						outCells = getResultColumns(smi, newSpecs.length);
					} catch (RowExecutionException ree) {
						m_logger.warn(ree.getMessage() + " (" + row.getKey() + ")");
					} catch (Exception e) {
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						} else {
							throw new RuntimeException(e);
						}
					}
				}
				return outCells;
			}
		});
		return rearranger;
	}

	/**
	 * Method to return the types of the output columns. Must return the same
	 * number of columns as {@link #getColumnNamePrefixes()}
	 */
	protected abstract DataType[] getColumnTypes();

	/**
	 * @return the suffix part of the column names. If null, then the prefixes
	 *         are used as the entire names, otherwise, the names are of the
	 *         form '[prefix] ([Column Name])[Suffix]'. To include the name
	 *         without a suffix, an array of empty strings must be returned
	 */
	protected abstract String[] getColumnNameSuffixes();

	/**
	 * @return the prefix part of the column names
	 * @see #getColumnNameSuffixes()
	 */
	protected abstract String[] getColumnNamePrefixes();

	/**
	 * Method to actually return the result counts. Rows which are
	 * non-calculable show throw a subclass of {@link RowExecutionException} to
	 * allow missing values to be returned and node execution to continue. Other
	 * exceptions will cause node execution failure.
	 * 
	 * @param SMILES
	 *            The SMILES String, guaranteed to be non-null if
	 *            {@link #createColumnRearranger(DataTableSpec)} is not
	 *            over-written
	 * @param numCols
	 *            The number of properties to calculate
	 * @return The output cells
	 */
	protected abstract DataCell[] getResultColumns(String SMILES, int numCols) throws Exception;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		if (getColumnNameSuffixes() != null
				&& getColumnNamePrefixes().length != getColumnNameSuffixes().length) {
			throw new InvalidSettingsException(
					"The implementation is broken! Different number of column name suffixes and prefixes");
		}
		if (getColumnNamePrefixes().length < 1) {
			throw new InvalidSettingsException(
					"The implementation is broken! No output columns are defined");
		}
		if (getColumnTypes().length != getColumnNamePrefixes().length) {
			throw new InvalidSettingsException(
					"The implementation is broken! Different number of column names and types");
		}

		try {
			String msg = SmilesHelpers.findSmilesColumn(inSpecs[0], m_colName);
			if (msg != null) {
				m_logger.warn(msg);
			}
		} catch (InvalidSettingsException e) {
			m_logger.error(e.getMessage());
			throw e;
		}
		return new DataTableSpec[] { createColumnRearranger(inSpecs[0]).createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_colName.saveSettingsTo(settings);
		if (m_hasRemoveInputCol) {
			m_removeInputCol.saveSettingsTo(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_colName.loadSettingsFrom(settings);
		if (m_hasRemoveInputCol) {
			m_removeInputCol.loadSettingsFrom(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_colName.validateSettings(settings);
		if (m_hasRemoveInputCol) {
			m_removeInputCol.validateSettings(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

}
