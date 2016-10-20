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
package com.vernalis.rcsb.io.nodes.manip;

import java.util.Arrays;

import org.knime.bio.types.PdbCell;
import org.knime.bio.types.PdbCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;

import com.vernalis.io.FileDownloadException;
import com.vernalis.io.FileHelpers;
import com.vernalis.rcsb.io.helpers.PDBIOHelperFunctions;

/**
 * This is the model implementation of RCSBmultiDownload. Node to allow download
 * of multiple RCSB PDB filetypes from a column of RCSB Structure IDs
 */
public class RCSBmultiDownloadNodeModel extends SimpleStreamableFunctionNodeModel {

	// the logger instance
	@SuppressWarnings("unused")
	private static final NodeLogger logger = NodeLogger.getLogger(RCSBmultiDownloadNodeModel.class);

	// Settings Keys
	static final String CFG_PDB_COLUMN_NAME = "PDB_column_name";
	static final String CFG_PDB = "PDB";
	static final String CFG_CIF = "mmCIF";
	static final String CFG_SF = "StructureFactor";
	static final String CFG_PDBML = "PDBML";
	static final String CFG_FASTA = "FASTA";

	private final SettingsModelString m_PDBcolumnName = new SettingsModelString(CFG_PDB_COLUMN_NAME,
			null);

	private final SettingsModelBoolean m_PDB = new SettingsModelBoolean(CFG_PDB, true);

	private final SettingsModelBoolean m_CIF = new SettingsModelBoolean(CFG_CIF, false);

	private final SettingsModelBoolean m_SF = new SettingsModelBoolean(CFG_SF, false);

	private final SettingsModelBoolean m_PDBML = new SettingsModelBoolean(CFG_PDBML, false);

	private final SettingsModelBoolean m_FASTA = new SettingsModelBoolean(CFG_FASTA, false);

	/**
	 * Constructor for the node model.
	 */

	protected RCSBmultiDownloadNodeModel() {
		super();
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec in) {
		// Actually download the files and build the output
		// The column index of the selected column
		final int colIndexPDBID = in.findColumnIndex(m_PDBcolumnName.getStringValue());

		// Count the new columns to add
		int i = 0;
		i += (m_PDB.getBooleanValue()) ? 1 : 0;
		i += (m_CIF.getBooleanValue()) ? 1 : 0;
		i += (m_SF.getBooleanValue()) ? 1 : 0;
		i += (m_PDBML.getBooleanValue()) ? 1 : 0;
		i += (m_FASTA.getBooleanValue()) ? 1 : 0;

		final int NewCols = i;

		// column spec of the appended columns
		DataColumnSpec[] newColSpec = new DataColumnSpec[NewCols];
		i = 0;
		if (m_PDB.getBooleanValue()) {
			// The PDB cell type will be used if it is available - dynamically
			// checked here
			newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in, "PDB"),
					PdbCell.TYPE).createSpec();
			i++;
		}
		if (m_CIF.getBooleanValue()) {
			newColSpec[i] = new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(in, "CIF"),
					StringCell.TYPE).createSpec();
			i++;
		}
		if (m_SF.getBooleanValue()) {
			newColSpec[i] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(in, "Structure Factor"), StringCell.TYPE)
							.createSpec();
			i++;
		}
		if (m_PDBML.getBooleanValue()) {
			newColSpec[i] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(in, "PDBML"), XMLCell.TYPE).createSpec();
			i++;
		}
		if (m_FASTA.getBooleanValue()) {
			newColSpec[i] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(in, "FASTA"), StringCell.TYPE).createSpec();
			i++;
		}

		ColumnRearranger rearranger = new ColumnRearranger(in);
		rearranger.append(new AbstractCellFactory(true, newColSpec) {
			@Override
			public DataCell[] getCells(final DataRow row) {
				DataCell[] result = new DataCell[NewCols];
				Arrays.fill(result, DataType.getMissingCell());
				DataCell c = row.getCell(colIndexPDBID);
				if (c.isMissing()) {
					return result;
				}
				String pdbid = ((StringValue) c).getStringValue();
				int i = 0;
				String r;
				try {
					if (m_PDB.getBooleanValue()) {
						r = FileHelpers.readURLToString(
								PDBIOHelperFunctions.createRCSBUrl(pdbid, "PDB", true));
						if (r != null) {
							result[i] = PdbCellFactory.create(r);
						}
						i++;
					}
					if (m_CIF.getBooleanValue()) {
						r = FileHelpers.readURLToString(
								PDBIOHelperFunctions.createRCSBUrl(pdbid, "cif", true));
						if (r != null) {
							result[i] = new StringCell(r);
						}
						i++;
					}
					if (m_SF.getBooleanValue()) {
						r = FileHelpers.readURLToString(
								PDBIOHelperFunctions.createRCSBUrl(pdbid, "sf", true));
						if (r != null) {
							result[i] = new StringCell(r);
						}
						i++;
					}
					if (m_PDBML.getBooleanValue()) {
						r = FileHelpers.readURLToString(
								PDBIOHelperFunctions.createRCSBUrl(pdbid, "PDBML", true));
						if (r != null) {
							try {
								result[i] = XMLCellFactory.create(r);
							} catch (Exception e) {
								result[i] = new StringCell(r);
							}
						}
						i++;
					}
					if (m_FASTA.getBooleanValue()) {
						r = FileHelpers.readURLToString(
								PDBIOHelperFunctions.createRCSBUrl(pdbid, "FASTA"));
						if (r != null) {
							result[i] = new StringCell(r);
						}
						i++;
					}
				} catch (FileDownloadException e) {
					throw new IllegalArgumentException(e);
				}
				return result;
			}

		});
		return rearranger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Check that a column is selected
		int colIndex = -1;
		if (m_PDBcolumnName.getStringValue() == null) {
			int i = 0;
			for (DataColumnSpec cs : inSpecs[0]) {
				if (cs.getType().isCompatible(StringValue.class)) {
					if (colIndex != -1) {
						setWarningMessage("No PDB ID column selected");
						throw new InvalidSettingsException("No PDB ID column selected.");
					}
					colIndex = i;
				}
				i++;
			}

			if (colIndex == -1) {
				setWarningMessage("No PDB column selected");
				throw new InvalidSettingsException("No PDB column selected.");
			}
			m_PDBcolumnName.setStringValue(inSpecs[0].getColumnSpec(colIndex).getName());
			setWarningMessage("Column '" + m_PDBcolumnName.getStringValue()
					+ "' auto selected for PDB ID column");
		} else {
			colIndex = inSpecs[0].findColumnIndex(m_PDBcolumnName.getStringValue());
			if (colIndex < 0) {
				setWarningMessage("No such column: " + m_PDBcolumnName.getStringValue());
				throw new InvalidSettingsException(
						"No such column: " + m_PDBcolumnName.getStringValue());
			}

			DataColumnSpec colSpec = inSpecs[0].getColumnSpec(colIndex);
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				setWarningMessage(
						"Column \"" + m_PDBcolumnName + "\" does not contain string values");
				throw new InvalidSettingsException("Column \"" + m_PDBcolumnName
						+ "\" does not contain string values: " + colSpec.getType().toString());
			}
		}
		// Finally we need to find at least one property otherwise we are
		// wasting a node!
		if (!(m_PDB.getBooleanValue() || m_CIF.getBooleanValue() || m_FASTA.getBooleanValue()
				|| m_PDBML.getBooleanValue() || m_SF.getBooleanValue())) {
			setWarningMessage("At least one property must be selected");
			throw new InvalidSettingsException("No properties selected");
		}

		// everything seems to fine
		ColumnRearranger c = createColumnRearranger(inSpecs[0]);
		return new DataTableSpec[] { c.createSpec() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_CIF.saveSettingsTo(settings);
		m_FASTA.saveSettingsTo(settings);
		m_PDB.saveSettingsTo(settings);
		m_PDBcolumnName.saveSettingsTo(settings);
		m_PDBML.saveSettingsTo(settings);
		m_SF.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_CIF.loadSettingsFrom(settings);
		m_FASTA.loadSettingsFrom(settings);
		m_PDB.loadSettingsFrom(settings);
		m_PDBcolumnName.loadSettingsFrom(settings);
		m_PDBML.loadSettingsFrom(settings);
		m_SF.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_CIF.validateSettings(settings);
		m_FASTA.validateSettings(settings);
		m_PDB.validateSettings(settings);
		m_PDBcolumnName.validateSettings(settings);
		m_PDBML.validateSettings(settings);
		m_SF.validateSettings(settings);
	}

}
