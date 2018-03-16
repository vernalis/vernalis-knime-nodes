/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.maxcuts;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.RDKit.GenericRDKitException;
import org.RDKit.MolSanitizeException;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.rdkit.knime.types.RDKitMolValue;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.RDKitFragmentationUtils;
import com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

import static com.vernalis.knime.mmp.MolFormats.isColTypeRDKitCompatible;
import static com.vernalis.knime.mmp.nodes.maxcuts.MmpMaxCutsNodeDialog.createAddHModel;
import static com.vernalis.knime.mmp.nodes.maxcuts.MmpMaxCutsNodeDialog.createAllowTwoCutsToBondValueModel;
import static com.vernalis.knime.mmp.nodes.maxcuts.MmpMaxCutsNodeDialog.createCustomSMARTSModel;
import static com.vernalis.knime.mmp.nodes.maxcuts.MmpMaxCutsNodeDialog.createMolColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.maxcuts.MmpMaxCutsNodeDialog.createSMIRKSModel;

/**
 * Node model for the Max Number of cuts node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
@Deprecated
public class MmpMaxCutsNodeModel extends NodeModel {

	/** The node logger */
	protected NodeLogger m_Logger = NodeLogger.getLogger(this.getClass());

	/*
	 * Settings Models
	 */
	protected final SettingsModelString m_molColName = createMolColumnSettingsModel();
	protected final SettingsModelString m_fragSMIRKS = createSMIRKSModel();
	protected final SettingsModelString m_customSmarts = createCustomSMARTSModel();
	protected final SettingsModelBoolean m_AddHs = createAddHModel();
	protected final SettingsModelBoolean m_allowTwoCutsBondValue =
			createAllowTwoCutsToBondValueModel();

	/**
	 * The wave index for GC. As we plan to run in parallelisation, we need this
	 * to be atomic. It starts at 2 as wave 0 is reserved for non-assigned
	 * waves, and 1 for the reaction type
	 */
	protected final AtomicInteger gcWave = new AtomicInteger(2);

	/** The SDWIG garbage collector */
	protected final SWIGObjectGarbageCollector gc = new SWIGObjectGarbageCollector();

	private ROMol bondMatch;

	IPreferenceStore prefStore = null;

	boolean verboseLogging = false;
	Integer numThreads, queueSize;

	/**
	 * Model constructor
	 */
	public MmpMaxCutsNodeModel() {
		super(1, 1);
		m_customSmarts.setEnabled(FragmentationTypes
				.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED);

		prefStore = MatchedPairsMultipleCutsNodePlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				// Re-load the settings
				verboseLogging =
						prefStore.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
				queueSize = MatchedPairPreferencePage.getQueueSize();
				numThreads = MatchedPairPreferencePage.getThreadsCount();

			}
		});

		verboseLogging = prefStore.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
		queueSize = MatchedPairPreferencePage.getQueueSize();
		numThreads = MatchedPairPreferencePage.getThreadsCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
			throws Exception {

		return new BufferedDataTable[] { exec.createColumnRearrangeTable(inData[0],
				createColumnRearranger(inData[0].getDataTableSpec()), exec) };
	}

	/**
	 * Create a Column Rearranger to parallel process the input table to
	 * calculate the maximum number of cuts for the Schema
	 * 
	 * @param inSpec
	 *            The incoming table spec
	 * @return The {@link ColumnRearranger} to calculate the output table
	 */
	private ColumnRearranger createColumnRearranger(DataTableSpec inSpec) {
		ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		DataColumnSpec newColSpec = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, "Max Number of Cuts"), IntCell.TYPE)
						.createSpec();

		final int molColIdx = inSpec.findColumnIndex(m_molColName.getStringValue());
		rearranger.append(new SingleCellFactory(true, numThreads, queueSize, newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell retVal = DataType.getMissingCell();
				if (row.getCell(molColIdx).isMissing()) {
					return retVal;
				}

				int currentWaveID = gcWave.getAndIncrement();

				ROMol mol = null;
				try {
					mol = gc.markForCleanup(getROMolFromCell(row.getCell(molColIdx)),
							currentWaveID);
				} catch (RowExecutionException e) {
					return retVal;
				}

				int maxCuts = RDKitFragmentationUtils.maxNumCuts(mol, bondMatch,
						m_AddHs.getBooleanValue(), m_allowTwoCutsBondValue.getBooleanValue());

				gc.cleanupMarkedObjects(currentWaveID);

				return new IntCell(maxCuts);
			}
		});

		return rearranger;
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
	protected ROMol getROMolFromCell(DataCell cell) throws RowExecutionException {
		ROMol mol = null;
		DataType type = cell.getType();
		try {
			if (type.isCompatible(RDKitMolValue.class)) {
				mol = ((RDKitMolValue) cell).readMoleculeValue();
			} else if (type.isCompatible(SmilesValue.class)) {
				RWMol rwMol = RWMol.MolFromSmiles(((SmilesValue) cell).getSmilesValue(), 0, false);
				RDKFuncs.sanitizeMol(rwMol);
				mol = rwMol;
				// rwMol.delete();
			} else if (type.isCompatible(MolValue.class)) {
				mol = RWMol.MolFromMolBlock(((MolValue) cell).getMolValue(), true, false);
			} else if (type.isCompatible(SdfValue.class)) {
				mol = RWMol.MolFromMolBlock(((SdfValue) cell).getSdfValue(), true, false);
			} else {
				throw new RowExecutionException("Cell is not a recognised molecule type");
			}
		} catch (MolSanitizeException e) {
			// MolSanitizeException returns null for #getMessage()
			throw new RowExecutionException("Error in sanitizing molecule: "
					+ ((StringValue) cell).getStringValue() + " : " + e.message());
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null || "".equals(msg)) {
				// Try to do something useful if we have a different RDKit
				// Exception - at least try to report the error type!
				msg = e.getClass().getSimpleName();
				try {
					msg += " : " + ((GenericRDKitException) e).message();
				} catch (Exception e1) {
					// Do nothing
				}
			}
			if (msg.equals("Cell is not a recognised molecule type")) {
				throw new RowExecutionException(msg);
			} else {
				throw new RowExecutionException("Error in parsing molecule: "
						+ ((StringValue) cell).getStringValue() + " : " + msg);
			}
		}
		return mol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		// Check the molCol is a molecule
		DataColumnSpec colSpec = inSpecs[0].getColumnSpec(m_molColName.getStringValue());

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (isColTypeRDKitCompatible(inSpecs[0].getColumnSpec(i).getType())) {
					m_molColName.setStringValue(inSpecs[0].getColumnSpec(i).getName());
					m_Logger.warn("No column selected. " + m_molColName.getStringValue()
							+ " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_Logger.error("No molecule column of the accepted"
							+ " input formats (SDF, Mol, SMILES) was found.");
					throw new InvalidSettingsException("No molecule column of the accepted"
							+ " input formats (SDF, Mol, SMILES) was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!isColTypeRDKitCompatible(colSpec.getType())) {
				// The column is not compatible with one of the accepted types
				m_Logger.error("The column " + m_molColName.getStringValue()
						+ " is not one of the accepted" + " input formats (SDF, Mol, SMILES)");
				throw new InvalidSettingsException("The column " + m_molColName.getStringValue()
						+ " is not one of the accepted" + " input formats (SDF, Mol, SMILES)");
			}
		}

		if (FragmentationTypes
				.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED) {
			if (m_customSmarts.getStringValue() == null
					|| "".equals(m_customSmarts.getStringValue())) {
				m_Logger.error("A reaction SMARTS string must be provided "
						+ "for user-defined fragmentation patterns");
				throw new InvalidSettingsException("A reaction SMARTS string must be provided "
						+ "for user-defined fragmentation patterns");
			}
			String rSMARTSCheck =
					RDKitFragmentationUtils.validateReactionSmarts(m_customSmarts.getStringValue());
			if (rSMARTSCheck != null) {
				m_Logger.error("Error parsing rSMARTS: " + rSMARTSCheck);
				throw new InvalidSettingsException("Error parsing rSMARTS: " + rSMARTSCheck);
			}
		}

		// Sort out the reaction
		String fragSMIRKS = FragmentationTypes.valueOf(m_fragSMIRKS.getStringValue()).getSMARTS();
		if ((fragSMIRKS == null || "".equals(fragSMIRKS)) && FragmentationTypes
				.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED) {
			fragSMIRKS = m_customSmarts.getStringValue();
		}
		bondMatch = gc.markForCleanup(RWMol.MolFromSmarts(fragSMIRKS.split(">>")[0]), 1);

		return new DataTableSpec[] { createColumnRearranger(inSpecs[0]).createSpec() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_molColName.saveSettingsTo(settings);
		m_fragSMIRKS.saveSettingsTo(settings);
		m_customSmarts.saveSettingsTo(settings);
		m_AddHs.saveSettingsTo(settings);
		m_allowTwoCutsBondValue.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_molColName.validateSettings(settings);
		m_fragSMIRKS.validateSettings(settings);
		m_customSmarts.validateSettings(settings);
		m_AddHs.validateSettings(settings);
		m_allowTwoCutsBondValue.validateSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_molColName.loadSettingsFrom(settings);
		m_fragSMIRKS.loadSettingsFrom(settings);
		m_customSmarts.loadSettingsFrom(settings);
		m_AddHs.loadSettingsFrom(settings);
		m_allowTwoCutsBondValue.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		gc.cleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		super.onDispose();
		gc.cleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		gc.cleanupMarkedObjects();
	}

}
