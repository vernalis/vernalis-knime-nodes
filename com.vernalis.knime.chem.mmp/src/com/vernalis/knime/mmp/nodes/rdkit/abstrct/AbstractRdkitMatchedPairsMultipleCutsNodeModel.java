/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp.nodes.rdkit.abstrct;
import static com.vernalis.knime.mmp.MolFormats.isColTypeRDKitCompatible;
import static com.vernalis.knime.mmp.RDKitFragment.validateReactionSmarts;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createAddHModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createCustomSMARTSModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createCutsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createHasHARatioFilterModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createHasMaxChangingAtomsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createIDColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createMaxChangingAtomsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createMolColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createOutputChangingHACountsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createOutputHARatiosModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createRatioModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createSMIRKSModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createStripHModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createTrackCutConnectivityModel;

import java.io.File;
import java.io.IOException;

import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.rdkit.knime.types.RDKitMolValue;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.RowExecutionException;

/**
 * Abstract class for the shared code for the MMP and Molecule fragment nodes.
 * Provides methods to retrieve the molecule from the input cell as an ROMol
 * object, and handles column selection via the
 * {@link #configure(DataTableSpec[])} implementation. Subclasses must intialise
 * the {@link #m_Logger}.
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public abstract class AbstractRdkitMatchedPairsMultipleCutsNodeModel extends
		NodeModel {

	/** The node logger Needs to be initialised in subclasses. */
	protected NodeLogger m_Logger;

	/*
	 * Settings Models
	 */
	protected final SettingsModelString m_molColName = createMolColumnSettingsModel();
	protected final SettingsModelString m_idColName = createIDColumnSettingsModel();
	protected final SettingsModelString m_fragSMIRKS = createSMIRKSModel();
	protected final SettingsModelString m_customSmarts = createCustomSMARTSModel();
	protected final SettingsModelIntegerBounded m_numCuts = createCutsModel();
	protected final SettingsModelBoolean m_AddHs = createAddHModel();
	protected final SettingsModelBoolean m_hasChangingAtoms = createHasMaxChangingAtomsModel();
	protected final SettingsModelBoolean m_hasHARatioFilter = createHasHARatioFilterModel();
	protected final SettingsModelIntegerBounded m_maxChangingAtoms = createMaxChangingAtomsModel();
	protected final SettingsModelDoubleBounded m_minHARatioFilter = createRatioModel();
	protected final SettingsModelBoolean m_stripHsAtEnd = createStripHModel();
	protected final SettingsModelBoolean m_outputNumChgHAs = createOutputChangingHACountsModel();
	protected final SettingsModelBoolean m_outputHARatio = createOutputHARatiosModel();
	protected final SettingsModelBoolean m_trackCutConnectivity = createTrackCutConnectivityModel();

	/** Number of columns in the output table */
	protected int numCols;
	/** First output table spec (2nd matches the input table) */
	protected DataTableSpec m_spec;

	/**
	 * Constructor - creates a 1:2 node
	 */
	public AbstractRdkitMatchedPairsMultipleCutsNodeModel() {
		super(1, 2);
		m_AddHs.setEnabled(m_numCuts.getIntValue() == 1);
		m_stripHsAtEnd.setEnabled(m_numCuts.getIntValue() == 1
				&& m_AddHs.getBooleanValue());
		m_trackCutConnectivity.setEnabled(m_numCuts.getIntValue() > 1);
		m_maxChangingAtoms.setEnabled(m_hasChangingAtoms.getBooleanValue());
		m_minHARatioFilter.setEnabled(m_hasHARatioFilter.getBooleanValue());
		m_customSmarts.setEnabled(FragmentationTypes.valueOf(m_fragSMIRKS
				.getStringValue()) == FragmentationTypes.USER_DEFINED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// Check the molCol is a molecule
		DataColumnSpec colSpec = inSpecs[0].getColumnSpec(m_molColName
				.getStringValue());

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (isColTypeRDKitCompatible(inSpecs[0].getColumnSpec(i)
						.getType())) {
					m_molColName.setStringValue(inSpecs[0].getColumnSpec(i)
							.getName());
					m_Logger.warn("No column selected. "
							+ m_molColName.getStringValue() + " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_Logger.error("No molecule column of the accepted"
							+ " input formats (SDF, Mol, SMILES) was found.");
					throw new InvalidSettingsException(
							"No molecule column of the accepted"
									+ " input formats (SDF, Mol, SMILES) was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!isColTypeRDKitCompatible(colSpec.getType())) {
				// The column is not compatible with one of the accepted types
				m_Logger.error("The column " + m_molColName.getStringValue()
						+ " is not one of the accepted"
						+ " input formats (SDF, Mol, SMILES)");
				throw new InvalidSettingsException("The column "
						+ m_molColName.getStringValue()
						+ " is not one of the accepted"
						+ " input formats (SDF, Mol, SMILES)");
			}
		}

		// Now check the ID column
		colSpec = inSpecs[0].getColumnSpec(m_idColName.getStringValue());

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				DataType colType = inSpecs[0].getColumnSpec(i).getType();
				if (colType.isCompatible(StringValue.class)) {
					m_idColName.setStringValue(inSpecs[0].getColumnSpec(i)
							.getName());
					m_Logger.warn("No column selected. "
							+ m_idColName.getStringValue() + " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_Logger.error("No String-compatible ID column was found.");
					throw new InvalidSettingsException(
							"No String-compatible ID column was found.");
				}
			}
		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				// The column is not compatible with one of the accepted types
				m_Logger.error("The column " + m_idColName.getStringValue()
						+ " is not String-compatible");
				throw new InvalidSettingsException("The column "
						+ m_idColName.getStringValue()
						+ " is not String-compatible");
			}
		}

		if (FragmentationTypes.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED) {
			if (m_customSmarts.getStringValue() == null
					|| "".equals(m_customSmarts.getStringValue())) {
				m_Logger.error("A reaction SMARTS string must be provided "
						+ "for user-defined fragmentation patterns");
				throw new InvalidSettingsException(
						"A reaction SMARTS string must be provided "
								+ "for user-defined fragmentation patterns");
			}
			String rSMARTSCheck = validateReactionSmarts(m_customSmarts
					.getStringValue());
			if (rSMARTSCheck != null) {
				m_Logger.error("Error parsing rSMARTS: " + rSMARTSCheck);
				throw new InvalidSettingsException("Error parsing rSMARTS: "
						+ rSMARTSCheck);
			}
		}
		m_spec = createSpec_0(inSpecs[0]);
		numCols = m_spec.getNumColumns();

		return new DataTableSpec[] { m_spec, inSpecs[0] };
	}

	/**
	 * This method is used to return the output table spec.
	 * 
	 * @param spec
	 *            Argument provided to allow use of {@link ColumnRearranger}s
	 * @return The {@link DataTableSpec} of the first output column
	 */
	protected abstract DataTableSpec createSpec_0(DataTableSpec spec);

	/**
	 * Creates a column spec from the name and type
	 * 
	 * @param colName
	 *            The column name
	 * @param colType
	 *            The column {@link DataType}
	 * @return A {@link DataColumnSpec}
	 */
	protected final DataColumnSpec createColSpec(String colName,
			DataType colType) {
		return (new DataColumnSpecCreator(colName, colType)).createSpec();
	}

	/**
	 * Returns an ROMol from a {@link DataCell} using the appropriate method for
	 * the cell type
	 * 
	 * @param cell
	 *            Input DataCell (should be RDKit, MOL, SDF or SMILES)
	 * @return ROMol object containing the molecule
	 * @throws InvalidSettingsException
	 *             Thrown if the cell is not of the correct type
	 * @throws RowExecutionException
	 */
	protected ROMol getROMolFromCell(DataCell cell)
			throws RowExecutionException {
		ROMol mol = null;
		DataType type = cell.getType();
		try {
			if (type.isCompatible(RDKitMolValue.class)) {
				mol = ((RDKitMolValue) cell).readMoleculeValue();
			} else if (type.isCompatible(SmilesValue.class)) {
				RWMol rwMol = RWMol.MolFromSmiles(
						((SmilesValue) cell).getSmilesValue(), 0, false);
				RDKFuncs.sanitizeMol(rwMol);
				mol = rwMol;
			} else if (type.isCompatible(MolValue.class)) {
				mol = RWMol.MolFromMolBlock(((MolValue) cell).getMolValue(),
						true, false);
			} else if (type.isCompatible(SdfValue.class)) {
				mol = RWMol.MolFromMolBlock(((SdfValue) cell).getSdfValue(),
						true, false);
			} else {
				throw new RowExecutionException(
						"Cell is not a recognised molecule type");
			}
		} catch (Exception e) {
			if (e.getMessage().equals("Cell is not a recognised molecule type")) {
				throw new RowExecutionException(e.getMessage());
			} else {
				throw new RowExecutionException("Error in parsing molecule: "
						+ ((StringValue) cell).getStringValue() + " : "
						+ e.getMessage());
			}
		}
		return mol;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_AddHs.saveSettingsTo(settings);
		m_idColName.saveSettingsTo(settings);
		m_molColName.saveSettingsTo(settings);
		m_fragSMIRKS.saveSettingsTo(settings);
		m_customSmarts.saveSettingsTo(settings);
		m_numCuts.saveSettingsTo(settings);
		m_hasChangingAtoms.saveSettingsTo(settings);
		m_hasHARatioFilter.saveSettingsTo(settings);
		m_maxChangingAtoms.saveSettingsTo(settings);
		m_minHARatioFilter.saveSettingsTo(settings);
		m_stripHsAtEnd.saveSettingsTo(settings);
		m_outputNumChgHAs.saveSettingsTo(settings);
		m_outputHARatio.saveSettingsTo(settings);
		m_trackCutConnectivity.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_AddHs.loadSettingsFrom(settings);
		m_idColName.loadSettingsFrom(settings);
		m_molColName.loadSettingsFrom(settings);
		m_fragSMIRKS.loadSettingsFrom(settings);
		try {
			m_customSmarts.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			m_customSmarts.setStringValue("");
		}
		m_numCuts.loadSettingsFrom(settings);
		m_hasChangingAtoms.loadSettingsFrom(settings);
		m_hasHARatioFilter.loadSettingsFrom(settings);
		m_maxChangingAtoms.loadSettingsFrom(settings);
		m_minHARatioFilter.loadSettingsFrom(settings);
		m_stripHsAtEnd.loadSettingsFrom(settings);
		m_outputNumChgHAs.loadSettingsFrom(settings);
		m_outputHARatio.loadSettingsFrom(settings);
		try {
			m_trackCutConnectivity.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// Backwards compatability
			m_trackCutConnectivity.setBooleanValue(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_AddHs.validateSettings(settings);
		m_idColName.validateSettings(settings);
		m_molColName.validateSettings(settings);
		m_fragSMIRKS.validateSettings(settings);
		m_numCuts.validateSettings(settings);
		m_hasChangingAtoms.validateSettings(settings);
		m_hasHARatioFilter.validateSettings(settings);
		m_maxChangingAtoms.validateSettings(settings);
		m_minHARatioFilter.validateSettings(settings);
		m_stripHsAtEnd.validateSettings(settings);
		m_outputNumChgHAs.validateSettings(settings);
		m_outputHARatio.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// Do nothing
	}
}
