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
package com.vernalis.knime.mmp.nodes.prefilter;

import static com.vernalis.knime.mmp.MolFormats.isColTypeRDKitCompatible;
import static com.vernalis.knime.mmp.nodes.prefilter.MmpPrefilterNodeDialog.createAddHModel;
import static com.vernalis.knime.mmp.nodes.prefilter.MmpPrefilterNodeDialog.createAllowTwoCutsToBondValueModel;
import static com.vernalis.knime.mmp.nodes.prefilter.MmpPrefilterNodeDialog.createCustomSMARTSModel;
import static com.vernalis.knime.mmp.nodes.prefilter.MmpPrefilterNodeDialog.createCutsModel;
import static com.vernalis.knime.mmp.nodes.prefilter.MmpPrefilterNodeDialog.createMolColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.prefilter.MmpPrefilterNodeDialog.createSMIRKSModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

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
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.MultiThreadWorker;
import org.rdkit.knime.types.RDKitMolValue;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.RDKitFragmentationUtils;
import com.vernalis.knime.mmp.RowExecutionException;
import com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

/**
 * Abstract Class for filtering by fragmentability according to a specified
 * fragmentation Schema. The class can be subclassed to give either filter (1
 * output port) or splitter (2 output port) variants
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class AbstractMatchedPairsFilterNodeModel extends NodeModel {
	/** The node logger Needs to be initialised in subclasses. */
	protected NodeLogger m_Logger = NodeLogger.getLogger(this.getClass());

	/*
	 * Settings Models
	 */
	protected final SettingsModelString m_molColName = createMolColumnSettingsModel();
	protected final SettingsModelString m_fragSMIRKS = createSMIRKSModel();
	protected final SettingsModelString m_customSmarts = createCustomSMARTSModel();
	protected final SettingsModelIntegerBounded m_numCuts = createCutsModel();
	protected final SettingsModelBoolean m_AddHs = createAddHModel();
	protected final SettingsModelBoolean m_allowTwoCutsToBondValue = createAllowTwoCutsToBondValueModel();

	/** The number of output ports */
	int numOutPorts;

	/** RDKit object collector */
	SWIGObjectGarbageCollector m_SWIGGC = new SWIGObjectGarbageCollector();

	IPreferenceStore prefStore = null;

	Integer numThreads, queueSize;

	private boolean m_verboseLogging;

	/**
	 * Wrap layer for RDKit objects to be persisted throughout a call to
	 * {@link #execute(BufferedDataTable[], ExecutionContext)}
	 */
	private static final int GC_LEVEL_EXECUTE = 0;

	/**
	 * Constructor for the node model. The number of output ports determines
	 * whether the node is a 'filter' (1 output port) or 'splitter' (2 output
	 * ports)
	 * 
	 * @param outPorts
	 *            The number of ports (1 or 2; Values >2 will return 2 ports, <2
	 *            1 port)
	 */
	public AbstractMatchedPairsFilterNodeModel(int outPorts) {
		super(1, (outPorts >= 2) ? 2 : 1);
		numOutPorts = (outPorts >= 2) ? 2 : 1;
		m_customSmarts.setEnabled(FragmentationTypes
				.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED);
		m_allowTwoCutsToBondValue.setEnabled(m_numCuts.getIntValue() == 2);

		prefStore = MatchedPairsMultipleCutsNodePlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				// Re-load the settings
				m_verboseLogging = prefStore
						.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
				queueSize = MatchedPairPreferencePage.getQueueSize();
				numThreads = MatchedPairPreferencePage.getThreadsCount();
			}
		});

		m_verboseLogging = prefStore.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
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
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		// Do some setting up
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		final int molIdx = spec.findColumnIndex(m_molColName.getStringValue());
		final long numRows = table.size();
		// A table for the passed rows
		final BufferedDataContainer dc_pass = exec.createDataContainer(spec);
		// And optionally a table for the failed rows
		final BufferedDataContainer dc_fail;
		if (numOutPorts > 1) {
			dc_fail = exec.createDataContainer(spec);
		} else {
			dc_fail = null;
		}

		// Deal with the empty table situation
		if (numRows == 0) {
			dc_pass.close();
			if (dc_fail != null) {
				dc_fail.close();
				return new BufferedDataTable[] { dc_pass.getTable(), dc_fail.getTable() };
			}
			return new BufferedDataTable[] { dc_pass.getTable() };
		}

		// Sort out the reaction
		String fragSMIRKS = FragmentationTypes.valueOf(m_fragSMIRKS.getStringValue()).getSMARTS();
		if ((fragSMIRKS == null || "".equals(fragSMIRKS)) && FragmentationTypes
				.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED) {
			fragSMIRKS = m_customSmarts.getStringValue();
		}
		final ROMol bondMatch = m_SWIGGC
				.markForCleanup(RWMol.MolFromSmarts(fragSMIRKS.split(">>")[0]), GC_LEVEL_EXECUTE);

		// Now do some final setting up
		final int numCuts = m_numCuts.getIntValue();
		// Only add H's is numCuts ==1 and addH's flag set
		final boolean addHs = (numCuts == 1) ? m_AddHs.getBooleanValue() : false;

		final boolean allow2Cuts = (numCuts == 2) ? m_allowTwoCutsToBondValue.getBooleanValue()
				: false;

		long rowCnt = 0;
		final AtomicLong keptRowCnt = new AtomicLong(0);

		MultiThreadWorker<DataRow, Boolean> processor = new MultiThreadWorker<DataRow, Boolean>(
				queueSize, numThreads) {

			@Override
			protected Boolean compute(DataRow in, long index) throws Exception {
				return processRow(in, molIdx, m_SWIGGC, index + 1, m_Logger, addHs, bondMatch,
						numCuts, allow2Cuts, m_verboseLogging);
			}

			@Override
			protected void processFinished(ComputationTask task)
					throws ExecutionException, CancellationException, InterruptedException {
				long r = task.getIndex();
				DataRow row = task.getInput();

				if (task.get()) {
					dc_pass.addRowToTable(row);
					keptRowCnt.incrementAndGet();
				} else if (dc_fail != null) {
					dc_fail.addRowToTable(row);
				}
				m_SWIGGC.cleanupMarkedObjects((int) (r + 1));
				exec.setProgress((r + 1.0) / numRows, "Processed row " + (r + 1) + " of " + numRows
						+ "; Kept " + keptRowCnt + " rows");
				try {
					exec.checkCanceled();
				} catch (CanceledExecutionException e) {
					throw new CancellationException();
				}
			}
		};
		processor.run(table);

		dc_pass.close();
		if (dc_fail != null) {
			dc_fail.close();
		}
		m_SWIGGC.cleanupMarkedObjects(GC_LEVEL_EXECUTE);

		BufferedDataTable[] retVal = new BufferedDataTable[numOutPorts];
		retVal[0] = dc_pass.getTable();
		if (dc_fail != null) {
			retVal[1] = dc_fail.getTable();
		}
		m_Logger.info(
				"Checked Row " + rowCnt + " of " + numRows + "; Passed " + keptRowCnt + " rows");
		return retVal;

	}

	protected static boolean processRow(DataRow row, int molIdx, SWIGObjectGarbageCollector swigGC,
			long l, NodeLogger logger, boolean addHs, ROMol bondMatch, int numCuts,
			boolean allow2Cuts, boolean verboseLogging) {
		DataCell molCell = row.getCell(molIdx);
		if (molCell.isMissing()) {
			// Deal with missing mols
			return false;
		}

		ROMol roMol;
		try {
			roMol = swigGC.markForCleanup(getROMolFromCell(row.getCell(molIdx)), (int) l);
		} catch (RowExecutionException e) {
			// Log the failed row
			if (verboseLogging) {
				logger.info("Error parsing molecule (Row: " + row.getKey().getString() + ") "
						+ e.getMessage());
			}
			// And add it to the second output
			return false;
		}

		if (roMol == null || "".equals(roMol.MolToSmiles(true))) {
			// Deal with when we cannot get an ROMol object - e.g. for 'No
			// Structure' Mol files
			// And add it to the second output
			return false;
		}
		// Multicomponent molecules make no sense... (and duplicate salts crash
		// the duplicate key resolver!)
		// Checking the SMILES for a '.' is simpler that garbage collecting the
		// ROMol_Vect from RDKFunc#getComponents()

		if (roMol.MolToSmiles().contains(".")) {
			return false;
		}

		if (addHs) {
			roMol = swigGC.markForCleanup(roMol.addHs(false, false), (int) l);
		}

		return RDKitFragmentationUtils.canCutNTimes(roMol, bondMatch, numCuts, allow2Cuts);

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
	protected static ROMol getROMolFromCell(DataCell cell) throws RowExecutionException {
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
			String rSMARTSCheck = RDKitFragmentationUtils
					.validateReactionSmarts(m_customSmarts.getStringValue());
			if (rSMARTSCheck != null) {
				m_Logger.error("Error parsing rSMARTS: " + rSMARTSCheck);
				throw new InvalidSettingsException("Error parsing rSMARTS: " + rSMARTSCheck);
			}
		}

		// Generate the output spec - they are all the same as the 1st input
		// spec
		DataTableSpec[] retVal = new DataTableSpec[numOutPorts];
		Arrays.fill(retVal, inSpecs[0]);
		return retVal;
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
		m_numCuts.saveSettingsTo(settings);
		m_AddHs.saveSettingsTo(settings);
		m_allowTwoCutsToBondValue.saveSettingsTo(settings);
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
		m_numCuts.validateSettings(settings);
		m_AddHs.validateSettings(settings);
		m_allowTwoCutsToBondValue.validateSettings(settings);
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
		m_numCuts.loadSettingsFrom(settings);
		m_AddHs.loadSettingsFrom(settings);
		m_allowTwoCutsToBondValue.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		m_SWIGGC.quarantineAndCleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		m_SWIGGC.cleanupMarkedObjects();
		super.finalize();
	}

}
