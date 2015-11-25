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

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * This is the model implementation of RandomNumbers. A node to generate a table
 * with a single column of random numbers
 * 
 * @author SDR
 */
public class RandomNumbers2NodeModel extends NodeModel {
	// the logger instance
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger
			.getLogger(RandomNumbersNodeModel.class);

	/**
	 * the settings key which is used to retrieve and store the settings (from
	 * the dialog or from a settings file) (package visibility to be usable from
	 * the dialog).
	 */

	static final String CFG_COLUMN_NAME = "Column_name";
	static final String CFG_TYPE = "Double_or_int";
	static final String CFG_MIN_MAX = "Range";

	static final String CFG_N = "Number_of_values";
	static final String CFG_UNIQUE = "Unique_set";

	private final SettingsModelString m_ColumnName = new SettingsModelString(
			CFG_COLUMN_NAME, "Random Values");

	private final SettingsModelString m_Type = new SettingsModelString(
			CFG_TYPE, "Double");

	private final SettingsModelDoubleRange m_Range = new SettingsModelDoubleRange(
			CFG_MIN_MAX, 0.0, 100000.0);

	private final SettingsModelLongBounded m_n = new SettingsModelLongBounded(
			CFG_N, 100, 1, Integer.MAX_VALUE);

	private final SettingsModelBoolean m_isUnique = new SettingsModelBoolean(
			CFG_UNIQUE, true);

	private BufferedDataContainer m_dc;

	private static DataTableSpec spec;


	/**
	 * Constructor for the node model.
	 */
	protected RandomNumbers2NodeModel() {

		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		// Now create a data container for the new output table

		m_dc = exec.createDataContainer(spec);
		if (m_Type.getStringValue().equals("Integer")) {
			if (m_isUnique.getBooleanValue()) {
				RandomNumbers.addUniqueInts(
						(int) Math.floor(m_Range.getMinRange()),
						(int) Math.floor(m_Range.getMaxRange()),
						m_n.getLongValue(), m_dc, exec);
			} else {
				RandomNumbers.addInts(
						(int) Math.floor(m_Range.getMinRange()),
						(int) Math.floor(m_Range.getMaxRange()),
						m_n.getLongValue(), m_dc, exec);
			}
		} else {
			// Actually get the values
				if (m_isUnique.getBooleanValue()) {
				RandomNumbers.addUniqueDoubles(m_Range.getMinRange(),
						m_Range.getMaxRange(), m_n.getLongValue(), m_dc,exec);
			} else {
				RandomNumbers.addDoubles(m_Range.getMinRange(),
						m_Range.getMaxRange(), m_n.getLongValue(), m_dc, exec);
			}

		}

		m_dc.close();
		return new BufferedDataTable[] { m_dc.getTable() };

	}

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

		// Check the settings
		if (m_ColumnName == null) {
			throw new InvalidSettingsException("No column name enteres");
		}
		if (m_n.getLongValue() <= 0 || m_n == null) {
			throw new InvalidSettingsException(
					"Need to enter a valid number of values");
		}
		if (m_n.getLongValue() > Integer.MAX_VALUE) {
			throw new InvalidSettingsException("Too many values required");
		}
		if (m_Range == null) {
			throw new InvalidSettingsException("Enter a valid range");
		}

		// Create an output table spec
		spec = new DataTableSpec(createDataColumnSpec(
				m_ColumnName.getStringValue(), m_Type.getStringValue()));
		return new DataTableSpec[] { spec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_ColumnName.saveSettingsTo(settings);
		m_isUnique.saveSettingsTo(settings);
		m_n.saveSettingsTo(settings);
		m_Range.saveSettingsTo(settings);
		m_Type.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_ColumnName.loadSettingsFrom(settings);
		m_isUnique.loadSettingsFrom(settings);
		m_n.loadSettingsFrom(settings);
		m_Range.loadSettingsFrom(settings);
		m_Type.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_ColumnName.validateSettings(settings);
		m_isUnique.validateSettings(settings);
		m_n.validateSettings(settings);
		m_Range.validateSettings(settings);
		m_Type.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
	}

	private static DataColumnSpec[] createDataColumnSpec(final String colName,
			final String colType) {
		DataColumnSpec[] dcs = new DataColumnSpec[1];
		if ("Double".equals(colType)) {
			dcs[0] = new DataColumnSpecCreator(colName, DoubleCell.TYPE)
					.createSpec();
		} else {
			dcs[0] = new DataColumnSpecCreator(colName, IntCell.TYPE)
					.createSpec();
		}
		return dcs;
	}
}
