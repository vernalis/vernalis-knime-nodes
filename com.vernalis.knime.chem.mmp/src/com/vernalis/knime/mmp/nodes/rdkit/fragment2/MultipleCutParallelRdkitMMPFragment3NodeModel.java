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
/**
 * 
 */
package com.vernalis.knime.mmp.nodes.rdkit.fragment2;

import static com.vernalis.knime.mmp.MolFormats.isColTypeRDKitCompatible;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createAddFailReasonModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createAddHModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createAllowTwoCutsToBondValueModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createApFingerprintsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createCustomSMARTSModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createCutsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createFpLengthModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createFpUseBondTypesModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createFpUseChiralityModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createHasHARatioFilterModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createHasMaxChangingAtomsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createIDColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createMaxChangingAtomsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createMolColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createMorganRadiusModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createOutputChangingHACountsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createOutputHARatiosModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createProchiralModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createRatioModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createSMIRKSModel;
import static com.vernalis.knime.mmp.nodes.rdkit.fragment2.MultipleCutParallelRdkitMMPFragment3NodeDialog.createStripHModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

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
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.container.DataContainerException;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.MultiThreadWorker;
import org.rdkit.knime.types.RDKitMolValue;

import com.vernalis.knime.mmp.BondIdentifier;
import com.vernalis.knime.mmp.CombinationFinder;
import com.vernalis.knime.mmp.FragmentKey2;
import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.Leaf;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.MulticomponentSmilesFragmentParser;
import com.vernalis.knime.mmp.RDKitFragmentationUtils;
import com.vernalis.knime.mmp.RowExecutionException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory;
import com.vernalis.knime.mmp.fragmentors.ROMolFragmentFactory;
import com.vernalis.knime.mmp.fragmentors.UnenumeratedStereochemistryException;
import com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage;
import com.vernalis.knime.parallel.MultiTableParallelResult;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

/**
 * The {@link NodeModel} implementation for the MMP Molecule Fragment node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class MultipleCutParallelRdkitMMPFragment3NodeModel extends NodeModel {

	/** The node logger instance */
	protected NodeLogger m_Logger = NodeLogger.getLogger(this.getClass());

	/*
	 * Settings Models
	 */
	protected final SettingsModelString m_molColName = createMolColumnSettingsModel();
	protected final SettingsModelString m_idColName = createIDColumnSettingsModel();
	protected final SettingsModelString m_fragSMIRKS = createSMIRKSModel();
	protected final SettingsModelString m_customSmarts = createCustomSMARTSModel();
	protected final SettingsModelIntegerBounded m_numCuts = createCutsModel();
	protected final SettingsModelBoolean m_allowTwoCutsToBondValue = createAllowTwoCutsToBondValueModel();
	protected final SettingsModelBoolean m_AddHs = createAddHModel();
	protected final SettingsModelBoolean m_hasChangingAtoms = createHasMaxChangingAtomsModel();
	protected final SettingsModelBoolean m_hasHARatioFilter = createHasHARatioFilterModel();
	protected final SettingsModelIntegerBounded m_maxChangingAtoms = createMaxChangingAtomsModel();
	protected final SettingsModelDoubleBounded m_minHARatioFilter = createRatioModel();
	protected final SettingsModelBoolean m_stripHsAtEnd = createStripHModel();
	protected final SettingsModelBoolean m_outputNumChgHAs = createOutputChangingHACountsModel();
	protected final SettingsModelBoolean m_outputHARatio = createOutputHARatiosModel();
	protected final SettingsModelBoolean m_addFailReasons = createAddFailReasonModel();
	protected final SettingsModelBoolean m_apFingerprints = createApFingerprintsModel();
	protected final SettingsModelBoolean m_useBondTypes = createFpUseBondTypesModel();
	protected final SettingsModelBoolean m_useChirality = createFpUseChiralityModel();
	protected final SettingsModelIntegerBounded m_fpLength = createFpLengthModel();
	protected final SettingsModelIntegerBounded m_morganRadius = createMorganRadiusModel();
	protected final SettingsModelBoolean m_prochiralAsChiral = createProchiralModel();

	/** Number of columns in the output table */
	protected int numCols;
	/** Output table specs */
	protected DataTableSpec m_spec_0, m_spec_1;

	IPreferenceStore prefStore = null;

	boolean verboseLogging = false;
	Integer numThreads, queueSize;

	/** SWIG Garbage collector for RDKit Objects */
	private final SWIGObjectGarbageCollector m_SWIGGC = new SWIGObjectGarbageCollector();

	/**
	 * Wrap layer for RDKit objects to be persisted throughout a call to
	 * {@link #execute(BufferedDataTable[], ExecutionContext)}
	 */
	private static final int GC_LEVEL_EXECUTE = 0;

	/**
	 * Constructor
	 */
	public MultipleCutParallelRdkitMMPFragment3NodeModel() {
		super(1, 2);

		m_stripHsAtEnd.setEnabled(m_AddHs.getBooleanValue());

		m_maxChangingAtoms.setEnabled(m_hasChangingAtoms.getBooleanValue());
		m_minHARatioFilter.setEnabled(m_hasHARatioFilter.getBooleanValue());
		m_customSmarts.setEnabled(FragmentationTypes
				.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED);
		m_useBondTypes
				.setEnabled(m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue());
		m_useChirality
				.setEnabled(m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue());
		m_fpLength.setEnabled(m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue());
		m_morganRadius
				.setEnabled(m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue());
		m_allowTwoCutsToBondValue.setEnabled(m_numCuts.getIntValue() >= 2);

		prefStore = MatchedPairsMultipleCutsNodePlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				// Re-load the settings
				verboseLogging = prefStore
						.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
				queueSize = MatchedPairPreferencePage.getQueueSize();
				numThreads = MatchedPairPreferencePage.getThreadsCount();

			}
		});

		verboseLogging = prefStore.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
		queueSize = MatchedPairPreferencePage.getQueueSize();
		numThreads = MatchedPairPreferencePage.getThreadsCount();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
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

		// Now check the ID column
		colSpec = inSpecs[0].getColumnSpec(m_idColName.getStringValue());

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				DataType colType = inSpecs[0].getColumnSpec(i).getType();
				if (colType.isCompatible(StringValue.class)) {
					m_idColName.setStringValue(inSpecs[0].getColumnSpec(i).getName());
					m_Logger.warn("No column selected. " + m_idColName.getStringValue()
							+ " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_Logger.error("No String-compatible ID column was found.");
					throw new InvalidSettingsException("No String-compatible ID column was found.");
				}
			}
		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				// The column is not compatible with one of the accepted types
				m_Logger.error(
						"The column " + m_idColName.getStringValue() + " is not String-compatible");
				throw new InvalidSettingsException(
						"The column " + m_idColName.getStringValue() + " is not String-compatible");
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
		m_spec_0 = createSpec_0(inSpecs[0]);
		m_spec_1 = createSpec_1(inSpecs[0]);
		numCols = m_spec_0.getNumColumns();

		return new DataTableSpec[] { m_spec_0, m_spec_1 };
	}

	/**
	 * Method to create the 1st output table spec based on the node settings
	 * 
	 * @param spec
	 *            The incoming data table spec
	 * @return The 1st output table spec
	 */
	protected DataTableSpec createSpec_0(DataTableSpec spec) {
		int numCols = 4;
		if (m_outputNumChgHAs.getBooleanValue()) {
			numCols++;
		}
		if (m_outputHARatio.getBooleanValue()) {
			numCols++;
		}
		if (m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue()) {
			numCols += m_numCuts.getIntValue();
		}

		DataColumnSpec[] specs = new DataColumnSpec[numCols];
		int i = 0;
		specs[i++] = createColSpec("ID", StringCell.TYPE);
		specs[i++] = createColSpec(
				"Fragmentation 'Key' (Upto " + m_numCuts.getIntValue() + " bond cuts)",
				SmilesCell.TYPE);
		specs[i++] = createColSpec("Fragmentation 'Value'", SmilesCell.TYPE);

		if (m_outputNumChgHAs.getBooleanValue()) {
			specs[i++] = createColSpec("Changing Heavy Atoms", IntCell.TYPE);
		}
		if (m_outputHARatio.getBooleanValue()) {
			specs[i++] = createColSpec("Ratio of Changing / Unchanging Heavy Atoms",
					DoubleCell.TYPE);
		}
		if (m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue()) {
			for (int fpIdx = 1; fpIdx <= m_numCuts.getIntValue(); fpIdx++) {
				specs[i++] = new DataColumnSpecCreator(
						DataTableSpec.getUniqueColumnName(spec,
								"Attachment point " + fpIdx + " fingerprint"),
						DenseBitVectorCell.TYPE).createSpec();
			}
		}

		specs[i++] = createColSpec("Number of Cuts", IntCell.TYPE);

		return new DataTableSpec(specs);
	}

	/**
	 * Method to create the 2nd output table spec (failed rows) based on the
	 * node settings
	 * 
	 * @param dataTableSpec
	 *            Incoming DTS
	 * @return DTS with optional fail reason column
	 */
	protected DataTableSpec createSpec_1(DataTableSpec dataTableSpec) {
		if (m_addFailReasons.getBooleanValue()) {
			DataTableSpecCreator retVal = new DataTableSpecCreator(dataTableSpec);
			retVal.addColumns(new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(dataTableSpec, "Failure Reason"),
					StringCell.TYPE).createSpec());
			return retVal.createSpec();
		} else {
			return dataTableSpec;
		}
	}

	/**
	 * Creates a column spec from the name and type
	 * 
	 * @param colName
	 *            The column name
	 * @param colType
	 *            The column {@link DataType}
	 * @return A {@link DataColumnSpec}
	 */
	protected final DataColumnSpec createColSpec(String colName, DataType colType) {
		return (new DataColumnSpecCreator(colName, colType)).createSpec();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		// Do some setting up
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		final int molIdx = spec.findColumnIndex(m_molColName.getStringValue());
		final int idIdx = spec.findColumnIndex(m_idColName.getStringValue());
		final long numRows = table.size();
		// A table for the processed output
		final BufferedDataContainer dc_0 = exec.createDataContainer(m_spec_0);
		// And a table for unprocessed rows
		final BufferedDataContainer dc_1 = exec.createDataContainer(m_spec_1);

		// Deal with the empty table situation
		if (numRows == 0) {
			dc_0.close();
			dc_1.close();
			return new BufferedDataTable[] { dc_0.getTable(), dc_1.getTable() };
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

		final boolean addHs = m_AddHs.getBooleanValue();
		// These two can both be null
		final Integer maxNumVarAtm = (m_hasChangingAtoms.getBooleanValue())
				? m_maxChangingAtoms.getIntValue() : null;
		final Double minCnstToVarAtmRatio = (m_hasHARatioFilter.getBooleanValue())
				? m_minHARatioFilter.getDoubleValue() : null;

		final boolean stripHsAtEnd = m_stripHsAtEnd.isEnabled() && m_stripHsAtEnd.getBooleanValue();

		m_Logger.info("Starting fragmentation");
		m_Logger.info("Fragmentation SMIRKS: " + fragSMIRKS + " (Upto " + numCuts + " cuts)");

		m_Logger.info("Using " + numThreads + " threads and " + queueSize
				+ " queue items to parallel process...");
		// ConcurrentRowProcessor processor = getParallelWorker(queueSize,
		// numThreads, useKNIMEThreads, new BufferedDataContainer[] {
		// dc_0, dc_1 }, exec, numRows, numCols, molIdx,
		// m_addFailReasons.getBooleanValue(), bondMatch, numCuts, addHs,
		// stripHsAtEnd,
		// numCuts == 2 && m_allowTwoCutsToBondValue.getBooleanValue(),
		// maxNumVarAtm, minCnstToVarAtmRatio, idIdx,
		// m_outputNumChgHAs.getBooleanValue(),
		// m_outputHARatio.getBooleanValue(),
		// m_apFingerprints.getBooleanValue(),
		// m_morganRadius.getIntValue(), m_fpLength.getIntValue(),
		// m_useChirality.getBooleanValue(),
		// m_useBondTypes.getBooleanValue(), m_SWIGGC, verboseLogging);

		MultiThreadWorker<DataRow, MultiTableParallelResult> processor = new MultiThreadWorker<DataRow, MultiTableParallelResult>(
				queueSize, numThreads) {

			@Override
			protected MultiTableParallelResult compute(DataRow in, long index) throws Exception {
				try {
					return fragmentRow(in, index + 1, numCols, molIdx,
							m_addFailReasons.getBooleanValue(), bondMatch, numCuts,
							m_prochiralAsChiral.getBooleanValue(), addHs, stripHsAtEnd,
							m_allowTwoCutsToBondValue.getBooleanValue(), maxNumVarAtm,
							minCnstToVarAtmRatio, idIdx, m_outputNumChgHAs.getBooleanValue(),
							m_outputHARatio.getBooleanValue(), m_apFingerprints.getBooleanValue(),
							m_morganRadius.getIntValue(), m_fpLength.getIntValue(),
							m_useChirality.getBooleanValue(), m_useBondTypes.getBooleanValue(),
							m_SWIGGC, exec, m_Logger, verboseLogging);
				} catch (CanceledExecutionException e) {
					throw new CancellationException();
				}
			}

			@Override
			protected void processFinished(ComputationTask task)
					throws ExecutionException, CancellationException, InterruptedException {
				long r = task.getIndex();
				String inKey = task.getInput().getKey().getString();
				long subIdx = 0;
				MultiTableParallelResult result = task.get();

				try {
					for (int tblId = 0; tblId < result.getNumberTables(); tblId++) {
						List<DataRow> rows = result.getRowsForTable(tblId);
						int numRows = rows.size();
						for (DataRow row : rows) {
							// Need to Sort out row IDs!
							if (tblId > 0 && numRows == 1) {
								dc_1.addRowToTable(row);
							} else {
								DataRow row2 = new DefaultRow(new RowKey(inKey + "_" + subIdx++),
										row);
								dc_0.addRowToTable(row2);

							}
						}
					}
				} catch (DataContainerException e) {
					// Cancellation can cause table writing blow-up!
					if (!task.isCancelled()) {
						throw new InterruptedException("Exception encountered during execution: "
								+ e.getClass().getSimpleName() + " '" + e.getMessage() + "'");
					}
				} finally {
					m_SWIGGC.cleanupMarkedObjects((int) (r + 1));
					exec.setProgress((r + 1.0) / numRows,
							"Processed row " + (r + 1) + " of " + numRows);
				}

				try {
					exec.checkCanceled();
				} catch (CanceledExecutionException e) {
					throw new CancellationException();
				}

			}
		};

		try {
			processor.run(table);
		} catch (InterruptedException e) {
			// Quarantine to give time for all threads to cancel
			m_SWIGGC.quarantineAndCleanupMarkedObjects();
			CanceledExecutionException cee = new CanceledExecutionException(e.getMessage());
			cee.initCause(e);
			throw cee;
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause == null) {
				cause = e;
			}
			if (cause instanceof RuntimeException) {
				// Quarantine to give time for all threads to cancel
				m_SWIGGC.quarantineAndCleanupMarkedObjects();
				throw (RuntimeException) cause;
			}
			// Quarantine to give time for all threads to cancel
			m_SWIGGC.quarantineAndCleanupMarkedObjects();
			throw new RuntimeException(cause);
		}

		m_SWIGGC.cleanupMarkedObjects(GC_LEVEL_EXECUTE);
		dc_0.close();
		dc_1.close();
		return new BufferedDataTable[] { dc_0.getTable(), dc_1.getTable() };
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

	/**
	 * Static method to fragment a row according the the settings supplied
	 * 
	 * @param row
	 *            The row to fragment
	 * @param index
	 *            The row index for GC of SWIG objects
	 * @param numCols
	 *            The number of columns in the output table
	 * @param molIdx
	 *            The molecule column index
	 * @param addFailReasons
	 *            Should failure reasons be added to 2nd output table?
	 * @param bondMatch
	 *            The matching structure for the bond to be used for
	 *            fragmentation
	 * @param numCuts
	 *            The Maximum number of cuts to make
	 * @param prochiralAsChiral
	 *            Should prochiral centres be assigned chirality if there are no
	 *            known or unknown centres?
	 * @param addHs
	 *            Should H's be added for 1 cut?
	 * @param stripHsAtEnd
	 *            Should H's be removed from the output?
	 * @param allowTwoCutsToBondValue
	 *            Should a bond be cut twice for 2 cuts?
	 * @param maxNumVarAtm
	 *            The maximum number of changing atoms
	 * @param minCnstToVarAtmRatio
	 *            The minimum ratio of constant to changing atoms
	 * @param idColIdx
	 *            The ID column index
	 * @param outputNumChgHAs
	 *            Output the number of changing atoms?
	 * @param outputHARatio
	 *            Output the HA ratio?
	 * @param addFingerprints
	 *            Add fingerprints to the output?
	 * @param morganRadius
	 *            The morgan fp radius
	 * @param fpLength
	 *            The fp length
	 * @param useChirality
	 *            Use chirality in the fingerprints?
	 * @param useBondTypes
	 *            Use bond types in the fingerprints?
	 * @param swigGC
	 *            The garbage collector object
	 * @param exec
	 *            The execution context
	 * @param logger
	 *            The node logger
	 * @param verboseLogging
	 *            Should verbose logging be enabled?
	 * @return A {@link MultiTableParallelResult} object containing either
	 *         fragmentation rows of a failed row
	 * @throws CanceledExecutionException
	 */
	protected static MultiTableParallelResult fragmentRow(DataRow row, long index, int numCols,
			int molIdx, boolean addFailReasons, ROMol bondMatch, int numCuts,
			boolean prochiralAsChiral, boolean addHs, boolean stripHsAtEnd,
			boolean allowTwoCutsToBondValue, Integer maxNumVarAtm, Double minCnstToVarAtmRatio,
			int idColIdx, boolean outputNumChgHAs, boolean outputHARatio, boolean addFingerprints,
			int morganRadius, int fpLength, boolean useChirality, boolean useBondTypes,
			SWIGObjectGarbageCollector swigGC, ExecutionContext exec, NodeLogger logger,
			boolean verboseLogging) throws CanceledExecutionException {
		MultiTableParallelResult retVal = new MultiTableParallelResult(2);

		/*
		 * Firstly, try getting the molecule cell as an ROMol object Feed to
		 * second table if we fail or have a missing cell
		 */
		DataCell molCell = row.getCell(molIdx);
		if (molCell.isMissing()) {
			// Deal with missing mols
			retVal.addRowToTable((addFailReasons)
					? new AppendedColumnRow(row, new StringCell("Missing value in Molecule Column"))
					: row, 1);
			return retVal;
		}

		if (row.getCell(idColIdx).isMissing()) {
			// Missing ID - causes problems later!
			retVal.addRowToTable((addFailReasons)
					? new AppendedColumnRow(row, new StringCell("Missing value in ID Column"))
					: row, 1);
			return retVal;
		}

		ROMol roMol;
		try {
			roMol = swigGC.markForCleanup(getROMolFromCell(row.getCell(molIdx)), (int) index);
		} catch (RowExecutionException e) {
			// Log the failed row
			if (verboseLogging) {
				logger.info("Error parsing molecule (Row: " + row.getKey().getString() + ") "
						+ e.getMessage());
			}
			// And add it to the second output
			retVal.addRowToTable((addFailReasons)
					? new AppendedColumnRow(row, new StringCell(e.getMessage())) : row, 1);

			return retVal;
		}

		if (roMol == null || "".equals(roMol.MolToSmiles(true))) {
			// Deal with when we cannot get an ROMol object - e.g. for 'No
			// Structure' Mol files
			// And add it to the second output
			retVal.addRowToTable((addFailReasons)
					? new AppendedColumnRow(row, new StringCell("'No Structure' input molecule"))
					: row, 1);
			return retVal;
		}

		// Multicomponent molecules make no sense... (and duplicate salts crash
		// the duplicate key resolver!)
		// Checking the SMILES for a '.' is simpler that garbage collecting the
		// ROMol_Vect from RDKFunc#getComponents()

		if (roMol.MolToSmiles().contains(".")) {
			retVal.addRowToTable(
					(addFailReasons)
							? new AppendedColumnRow(row,
									new StringCell(
											"Multi-component structures cannot be fragmented"))
							: row,
					1);
			return retVal;
		}

		/*
		 * Do the fragmentation and apply filters, adding rows as we go...
		 */

		DataCell idCell = new StringCell(((StringValue) row.getCell(idColIdx)).getStringValue());

		// Build a list of all the valid fragmentations
		Set<MulticomponentSmilesFragmentParser> fragmentations = new TreeSet<>();

		// Identify all the matching bonds (NB - Cuttable combos are picked
		// later
		Set<BondIdentifier> cuttableBonds = RDKitFragmentationUtils.identifyAllMatchingBonds(roMol,
				bondMatch);

		// Deal with 1 cut
		MoleculeFragmentationFactory fragFactory;
		if (addHs) {
			ROMol roMol2 = swigGC.markForCleanup(new ROMol(roMol), (int) index);
			roMol2 = swigGC.markForCleanup(roMol2.addHs(false, false), (int) index);
			fragFactory = new ROMolFragmentFactory(roMol2, stripHsAtEnd, verboseLogging,
					maxNumVarAtm, minCnstToVarAtmRatio);
			fragmentations.addAll(breakMoleculeAlongBonds(fragFactory,
					RDKitFragmentationUtils.identifyAllMatchingBonds(roMol2, bondMatch),
					prochiralAsChiral, exec, logger, verboseLogging));

			// Now return the fragFactory to the main unhydrogenated molecule -
			// and in which case we dont strip Hs
			fragFactory = new ROMolFragmentFactory(roMol, false, verboseLogging, maxNumVarAtm,
					minCnstToVarAtmRatio);
		} else {
			// Otherwise we just cut along every bond
			fragFactory = new ROMolFragmentFactory(roMol, false, verboseLogging, maxNumVarAtm,
					minCnstToVarAtmRatio);
			fragmentations.addAll(breakMoleculeAlongBonds(fragFactory, cuttableBonds,
					prochiralAsChiral, exec, logger, verboseLogging));
		}

		boolean couldCut = fragmentations.size() > 0;
		// Check we have anything to do
		if (!couldCut) {
			// No bonds to cut
			retVal.addRowToTable(
					(addFailReasons)
							? new AppendedColumnRow(row,
									new StringCell("No matching bonds or found, or too few to cut"))
							: row,
					1);
			return retVal;
		}

		// Deal with the special case of 2 cuts, and allowing *-* as a value
		if (numCuts >= 2 && allowTwoCutsToBondValue) {
			fragmentations.addAll(doDoubleCutToSingleBond(fragFactory, cuttableBonds,
					prochiralAsChiral, exec, logger, verboseLogging));
		}

		// Now generate the combinations of bonds to cut for 2 or more cuts,
		// removing higher
		// graphs of invalid triplets where appropriate
		// TODO: Why doesnt this take cuttableBonds as an argument
		Set<Set<BondIdentifier>> bondCombos = generateCuttableBondCombos(roMol, bondMatch, numCuts);
		fragmentations.addAll(breakMoleculeAlongBondCombos(fragFactory, bondCombos,
				prochiralAsChiral, exec, logger, verboseLogging));

		// Now add the fragmentations to output rows:
		boolean addedFragmentations = false;
		for (MulticomponentSmilesFragmentParser smiParser : fragmentations) {
			if (smiParser.getNumCuts() > 1 || RDKitFragmentationUtils.filterFragment(
					smiParser.getKey(), smiParser.getValue(), maxNumVarAtm, minCnstToVarAtmRatio)) {
				addedFragmentations = true;
				addRowToTable(retVal, stripHsAtEnd, idCell, smiParser, numCols, outputNumChgHAs,
						outputHARatio, addFingerprints, morganRadius, fpLength, useChirality,
						useBondTypes);
			}
		}
		if (!addedFragmentations) {
			// There were no valid fragmentatins after filtering
			retVal.addRowToTable(
					(addFailReasons)
							? new AppendedColumnRow(row,
									new StringCell(
											"No fragmentations passed the specified filters"))
							: row,
					1);
		}

		fragmentations.clear();
		cuttableBonds.clear();
		return retVal;
	}

	/**
	 * A collection of bonds which have been identified as cuttable (i.e. match
	 * the required substructure, and ideally, for 3 or more cuts, have had
	 * bonds which can never give a valid cut pattern removed) are parsed into
	 * combinations of {@code numCuts} bonds. When more than 3 cuts are made,
	 * any combinations including an invalid triplet are removed
	 * 
	 * @param roMol
	 *            The molecule to cut
	 * @param cuttableBonds
	 *            A collection of cuttable bonds
	 * @return A Set of Sets of {@code numCuts} bonds
	 * @throws IllegalArgumentException
	 */
	protected static Set<Set<BondIdentifier>> generateCuttableBondCombos(ROMol roMol,
			ROMol bondMatch, int numCuts) throws IllegalArgumentException {

		Collection<BondIdentifier> cuttableBonds;
		// Generate the combinations of upto numCuts bonds. NB we start at 2 as
		// 1 is handled separately
		Set<Set<BondIdentifier>> bondCombos = new LinkedHashSet<Set<BondIdentifier>>();
		for (int i = 2; i <= numCuts; i++) {
			cuttableBonds = RDKitFragmentationUtils.identifyAllCuttableBonds(roMol, bondMatch, i);
			Set<Set<BondIdentifier>> newBondCombos = CombinationFinder
					.getCombinationsFor(cuttableBonds, i);
			if (i >= 3) {
				Set<Set<BondIdentifier>> triplets = CombinationFinder
						.getCombinationsFor(cuttableBonds, 3);

				// TODO: Optimise this ratio
				if ((triplets.size() * 1.0 / bondCombos.size()) < 1.0) {
					for (Set<BondIdentifier> triplet : triplets) {
						if (!RDKitFragmentationUtils.isValidCutTriplet(roMol, triplet)) {
							Iterator<Set<BondIdentifier>> iter = newBondCombos.iterator();
							while (iter.hasNext()) {
								if (iter.next().containsAll(triplet)) {
									iter.remove();
								}
							}
							if (newBondCombos.size() == 0) {
								break;
							}
						}
					}
				}
				if (newBondCombos.size() <= 0) {
					break;
				}
			}
			bondCombos.addAll(newBondCombos);
		}

		return bondCombos;
	}

	/**
	 * Method to break the supplied {@link ROMol} along the indicated bond
	 * combinations supplied in {@code bondCombos}. For each combination
	 * supplied, if the fragmentation is valid and passes the filters, then it
	 * is added to the fragment output table.
	 * 
	 * @param bondCombos
	 *            A {@link Set} of {@link Set}s of {@link BondIdentifier}. Each
	 *            inner set is a combination of matching bonds to cut.
	 * @param prochiralAsChiral
	 *            Should prochiral centres be assigned chirality if there are no
	 *            known or unknown centres?
	 * @param exec
	 *            The {@link ExecutionContext} to check for cancelling
	 * @param roMol
	 *            The molecule to fragment
	 * 
	 * @return The fragmentations as a set
	 * @throws CanceledExecutionException
	 *             if the user cancels during execution
	 */
	protected static Set<MulticomponentSmilesFragmentParser> breakMoleculeAlongBondCombos(
			MoleculeFragmentationFactory fragFactory, Set<Set<BondIdentifier>> bondCombos,
			boolean prochiralAsChiral, ExecutionContext exec, NodeLogger logger,
			boolean verboseLogging) throws CanceledExecutionException {
		int count = 0;
		Set<MulticomponentSmilesFragmentParser> retVal = new TreeSet<>();
		for (Set<BondIdentifier> bondSet : bondCombos) {
			exec.checkCanceled();
			count++;
			if (verboseLogging) {
				if (count % 50 == 0) {
					logger.info("Fragmenting molecule: " + count + " of " + bondCombos.size()
							+ " fragmentations tried");
				}
			}
			MulticomponentSmilesFragmentParser smiParser = null;
			try {
				smiParser = fragFactory.fragmentMolecule(bondSet, prochiralAsChiral);
				// Collect the fragmentation
				retVal.add(smiParser);
			} catch (MoleculeFragmentationException e) {
				if (verboseLogging) {
					logger.info("Discarding Fragmentation: " + e.getMessage() == null ? ""
							: e.getMessage());
				}
				// Goto next fragmentation
				continue;
			} catch (UnenumeratedStereochemistryException e) {
				// Some stereochemistry that could not be applied by the
				// Fragmentation Factory
				try {
					// NB the method handles the n=1 case reversal
					retVal.addAll(
							RDKitFragmentationUtils.enumerateDativeMaskedDoubleBondIsomers(e));
				} catch (MoleculeFragmentationException e1) {
					if (verboseLogging) {
						logger.info("Discarding Fragmentation: " + e.getMessage() == null ? ""
								: e.getMessage());
					}
					// Goto next fragmentation
					continue;
				}
			}

			// Deal with the numCuts=1 special case
			if (smiParser != null && smiParser.getNumCuts() == 1) {
				try {
					smiParser = smiParser.getReverse();
					retVal.add(smiParser);
				} catch (MoleculeFragmentationException e) {
					if (verboseLogging) {
						logger.info("Discarding Fragmentation: " + e.getMessage());
					}
					// Goto next fragmentation
					continue;
				} catch (UnenumeratedStereochemistryException e) {
					// Should never get here...
					logger.warn("Unexpected problem: " + e.getMessage());
				}

			}
		}
		return retVal;
	}

	/**
	 * Method to break the supplied {@link ROMol} along the indicated bond
	 * combinations supplied in {@code bondCombos}. For each combination
	 * supplied, if the fragmentation is valid and passes the filters, then it
	 * is added to the fragment output table.
	 * 
	 * @param bonds
	 *            A {@link Set} of {@link Set}s of {@link BondIdentifier}. Each
	 *            inner set is a combination of matching bonds to cut.
	 * @param prochiralAsChiral
	 *            Should prochiral centres be assigned chirality if there are no
	 *            known or unknown centres?
	 * @param exec
	 *            The {@link ExecutionContext} to check for cancelling
	 * @param roMol
	 *            The molecule to fragment
	 * 
	 * @return The fragmentations as a set
	 * @throws CanceledExecutionException
	 *             if the user cancels during execution
	 */
	protected static Set<MulticomponentSmilesFragmentParser> breakMoleculeAlongBonds(
			MoleculeFragmentationFactory fragFactory, Set<BondIdentifier> bonds,
			boolean prochiralAsChiral, ExecutionContext exec, NodeLogger logger,
			boolean verboseLogging) throws CanceledExecutionException {

		Set<MulticomponentSmilesFragmentParser> retVal = new TreeSet<>();
		for (BondIdentifier bond : bonds) {
			exec.checkCanceled();

			MulticomponentSmilesFragmentParser smiParser = null;
			try {
				smiParser = fragFactory.fragmentMolecule(bond, prochiralAsChiral);
				// Collect the fragmentation
				retVal.add(smiParser);
			} catch (MoleculeFragmentationException e) {
				if (verboseLogging) {
					logger.info("Discarding Fragmentation: " + e.getMessage() == null ? ""
							: e.getMessage());
				}
				// Goto next fragmentation
				continue;
			} catch (UnenumeratedStereochemistryException e) {
				// Some stereochemistry that could not be applied by the
				// Fragmentation Factory
				try {
					// NB the method handles the n=1 case reversal
					retVal.addAll(
							RDKitFragmentationUtils.enumerateDativeMaskedDoubleBondIsomers(e));
				} catch (MoleculeFragmentationException e1) {
					if (verboseLogging) {
						logger.info("Discarding Fragmentation: " + e.getMessage() == null ? ""
								: e.getMessage());
					}
					// Goto next fragmentation
					continue;
				}
			}

			// Deal with the numCuts=1 special case
			if (smiParser != null && smiParser.getNumCuts() == 1) {
				try {
					smiParser = smiParser.getReverse();
					retVal.add(smiParser);
				} catch (MoleculeFragmentationException e) {
					if (verboseLogging) {
						logger.info("Discarding Fragmentation: " + e.getMessage());
					}
					// Goto next fragmentation
					continue;
				} catch (UnenumeratedStereochemistryException e) {
					// Should never get here...
					logger.warn("Unexpected problem: " + e.getMessage());
				}
			}
		}
		return retVal;
	}

	/**
	 * Each bond in {@code cuttableBonds} is broken in turn, and [1*]-[2*]
	 * inserted in each case. If the molecule passes the filters, then it is
	 * added to the table in {@code dc_0}.
	 * 
	 * @param cuttableBonds
	 *            A {@link Set} of {@link BondIdentifier}s indicating which
	 *            bonds can be cut
	 * @param prochiralAsChiral
	 *            Should prochiral centres be assigned chirality if there are no
	 *            known or unknown centres?
	 * @param exec
	 *            The {@link ExecutionContext} to check for cancelling
	 * @param roMol
	 *            The molecule to fragment
	 * @param swigGC
	 *            The SWIG object collector
	 * @param gcLevel
	 *            The level to use for the swigGC
	 * 
	 * @return The fragmentations as a set
	 * @throws CanceledExecutionException
	 *             if the user cancels node execution during this method
	 */
	protected static Set<MulticomponentSmilesFragmentParser> doDoubleCutToSingleBond(
			MoleculeFragmentationFactory fragFactory, Set<BondIdentifier> cuttableBonds,
			boolean prochiralAsChiral, ExecutionContext exec, NodeLogger logger,
			boolean verboseLogging) throws CanceledExecutionException {

		Set<MulticomponentSmilesFragmentParser> fragmentations = new TreeSet<>();
		// Break each bond in turn
		for (BondIdentifier bond : cuttableBonds) {
			exec.checkCanceled();
			MulticomponentSmilesFragmentParser smiParser = null;
			try {
				smiParser = fragFactory.fragmentMoleculeWithBondInsertion(bond, prochiralAsChiral);
				fragmentations.add(smiParser);
			} catch (MoleculeFragmentationException e) {
				if (verboseLogging) {
					logger.info("Discarding Fragmentation: " + e.getMessage() == null
							? bond.toString() : e.getMessage());
				}
				// Goto next fragmentation
				continue;
			} catch (UnenumeratedStereochemistryException e) {
				// Some stereochemistry that could not be applied by the
				// Fragmentation Factory
				try {
					fragmentations.addAll(
							RDKitFragmentationUtils.enumerateDativeMaskedDoubleBondIsomers(e));
				} catch (MoleculeFragmentationException e1) {
					if (verboseLogging) {
						logger.info("Discarding Fragmentation: " + e.getMessage() == null ? ""
								: e.getMessage());
					}
					// Goto next fragmentation
					continue;
				}
			}
		}
		return fragmentations;
	}

	/**
	 * Method to add row to the output table, result
	 * 
	 * @param table
	 *            The table result container
	 * @param stripHsAtEnd
	 *            Remove H's?
	 * @param idCell
	 *            The ID cell
	 * @param smiParser
	 *            The {@link MulticomponentSmilesFragmentParser} containing the
	 *            fragmentation
	 * @param numCols
	 *            The number of columns in the output table
	 * @param outputNumChgHAs
	 *            Output the number of changing atoms?
	 * @param outputHARatio
	 *            Output the HA ratio?
	 * @param addFingerprints
	 *            Add fingerprints to the output?
	 * @param morganRadius
	 *            The morgan fp radius
	 * @param fpLength
	 *            The fp length
	 * @param useChirality
	 *            Use chirality in the fingerprints?
	 * @param useBondTypes
	 *            Use bond types in the fingerprints?
	 */
	protected static void addRowToTable(final MultiTableParallelResult table, boolean stripHsAtEnd,
			DataCell idCell, MulticomponentSmilesFragmentParser smiParser, int numCols,
			boolean outputNumChgHAs, boolean outputHARatio, boolean addFingerprints,
			int morganRadius, int fpLength, boolean useChirality, boolean useBondTypes) {

		// We use a dummy row key - the keys are sorted in the concurrent
		// processor class
		RowKey rowId = new RowKey("Row_");

		DataCell[] cells = new DataCell[numCols];
		Arrays.fill(cells, DataType.getMissingCell());
		int colIdx = 0;
		cells[colIdx++] = idCell;
		FragmentKey2 key = smiParser.getKey();
		// The fragmentation factory should already have removed H's so we dont
		// repeat the effort here!
		cells[colIdx++] = key.getKeyAsDataCell(false);
		cells[colIdx++] = smiParser.getValue().getSMILESCell(false);
		if (outputNumChgHAs) {
			cells[colIdx++] = smiParser.getValue().getNumberChangingAtomsCell();
		}
		if (outputHARatio) {
			cells[colIdx++] = smiParser.getKey()
					.getConstantToVaryingAtomRatioCell(smiParser.getValue());
		}
		if (addFingerprints) {
			for (int i = 0; i < smiParser.getNumCuts(); i++) {
				Leaf l = null;
				try {
					l = key.getLeafWithIdx(i + 1);
				} catch (IndexOutOfBoundsException e) {
					// We have got no more leafs to find
					break;
				}
				cells[colIdx++] = l.getMorganFingerprintCell(morganRadius, fpLength, useChirality,
						useBondTypes);
			}
		}
		cells[numCols - 1] = new IntCell(smiParser.getNumCuts());
		table.addRowToTable(new DefaultRow(rowId, cells), 0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_idColName.saveSettingsTo(settings);
		m_molColName.saveSettingsTo(settings);
		m_fragSMIRKS.saveSettingsTo(settings);
		m_customSmarts.saveSettingsTo(settings);
		m_numCuts.saveSettingsTo(settings);
		m_allowTwoCutsToBondValue.saveSettingsTo(settings);

		m_prochiralAsChiral.saveSettingsTo(settings);
		m_AddHs.saveSettingsTo(settings);
		m_stripHsAtEnd.saveSettingsTo(settings);
		m_hasChangingAtoms.saveSettingsTo(settings);
		m_hasHARatioFilter.saveSettingsTo(settings);
		m_maxChangingAtoms.saveSettingsTo(settings);
		m_minHARatioFilter.saveSettingsTo(settings);

		m_outputNumChgHAs.saveSettingsTo(settings);
		m_outputHARatio.saveSettingsTo(settings);
		m_addFailReasons.saveSettingsTo(settings);

		m_apFingerprints.saveSettingsTo(settings);
		m_fpLength.saveSettingsTo(settings);
		m_morganRadius.saveSettingsTo(settings);
		m_useBondTypes.saveSettingsTo(settings);
		m_useChirality.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_idColName.loadSettingsFrom(settings);
		m_molColName.loadSettingsFrom(settings);
		m_fragSMIRKS.loadSettingsFrom(settings);
		m_customSmarts.loadSettingsFrom(settings);
		m_numCuts.loadSettingsFrom(settings);
		m_allowTwoCutsToBondValue.loadSettingsFrom(settings);

		m_prochiralAsChiral.loadSettingsFrom(settings);
		m_AddHs.loadSettingsFrom(settings);
		m_stripHsAtEnd.loadSettingsFrom(settings);
		m_hasChangingAtoms.loadSettingsFrom(settings);
		m_hasHARatioFilter.loadSettingsFrom(settings);
		m_maxChangingAtoms.loadSettingsFrom(settings);
		m_minHARatioFilter.loadSettingsFrom(settings);

		m_outputNumChgHAs.loadSettingsFrom(settings);
		m_outputHARatio.loadSettingsFrom(settings);
		m_addFailReasons.loadSettingsFrom(settings);

		m_apFingerprints.loadSettingsFrom(settings);
		m_useBondTypes.loadSettingsFrom(settings);
		m_useChirality.loadSettingsFrom(settings);
		m_fpLength.loadSettingsFrom(settings);
		m_morganRadius.loadSettingsFrom(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.nodes.rdkit.abstrct.
	 * AbstractRdkitMatchedPairsMultipleCutsNodeModel
	 * #validateSettings(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_prochiralAsChiral.validateSettings(settings);
		m_AddHs.validateSettings(settings);
		m_idColName.validateSettings(settings);
		m_molColName.validateSettings(settings);
		m_fragSMIRKS.validateSettings(settings);
		m_customSmarts.validateSettings(settings);
		m_numCuts.validateSettings(settings);
		m_allowTwoCutsToBondValue.validateSettings(settings);
		m_hasChangingAtoms.validateSettings(settings);
		m_hasHARatioFilter.validateSettings(settings);
		m_maxChangingAtoms.validateSettings(settings);
		m_minHARatioFilter.validateSettings(settings);
		m_stripHsAtEnd.validateSettings(settings);
		m_outputNumChgHAs.validateSettings(settings);
		m_outputHARatio.validateSettings(settings);
		m_addFailReasons.validateSettings(settings);
		m_apFingerprints.validateSettings(settings);
		m_useBondTypes.validateSettings(settings);
		m_useChirality.validateSettings(settings);
		m_fpLength.validateSettings(settings);
		m_morganRadius.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// Quarantine to give time for all threads to cancel
		m_SWIGGC.quarantineAndCleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		m_SWIGGC.cleanupMarkedObjects();
		super.onDispose();
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
