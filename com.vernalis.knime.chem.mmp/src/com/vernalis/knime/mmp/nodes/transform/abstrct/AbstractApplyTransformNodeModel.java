/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.transform.abstrct;

import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createComparisonTypeModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createFilterByKeySimilarityModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createFirstFPColNameModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createGenerateChiralProductsModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createMolColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createMoleculePassThroughColsModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createRxnColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createSimilarityThresholdModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createSimilarityTypeModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createTransformPassThroughColsModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createTransformsSortedModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createTverskyAlphaModel;
import static com.vernalis.knime.mmp.nodes.transform.abstrct.AbstractApplyTransformNodeDialog.createTverskyBetaModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.knime.base.data.filter.column.FilterColumnRow;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.data.vector.bitvector.DenseBitVector;
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
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.NodeProgressEvent;
import org.knime.core.node.workflow.NodeProgressListener;
import org.knime.core.util.MultiThreadWorker;

import com.vernalis.knime.data.datatable.GroupIterableDataTable;
import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.nodes.pairgen.abstrct.AttachmentPointFingerprintComparisonType2;
import com.vernalis.knime.mmp.nodes.pairgen.abstrct.AttachmentPointFingerprintSimilarityType;
import com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage;
import com.vernalis.knime.mmp.transform.TransformUtilityFactory;
import com.vernalis.knime.mmp.transform.TransformUtils;
import com.vernalis.knime.parallel.SingleTableParallelResult;

/**
 * The node model for the Apply transforms nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the query molecule object
 * @param <V>
 *            The type of the reaction/transform object
 */
public class AbstractApplyTransformNodeModel<T, U, V> extends NodeModel {
	protected final SettingsModelString molColNameMdl = createMolColumnSettingsModel();
	protected final SettingsModelColumnFilter2 molPassThroughColsMdl =
			createMoleculePassThroughColsModel();
	protected final SettingsModelString rxnColNameMdl = createRxnColumnSettingsModel();
	protected final SettingsModelBoolean rxnSortedMdl = createTransformsSortedModel();
	protected final SettingsModelBoolean tryChiralProductsMdl = createGenerateChiralProductsModel();
	protected final SettingsModelColumnFilter2 rxnPassThroughColsMdl =
			createTransformPassThroughColsModel();

	protected final SettingsModelBoolean filterByRxnEnvironmentMdl =
			createFilterByKeySimilarityModel();
	protected final SettingsModelString keyAPFPColNameMdl = createFirstFPColNameModel();
	protected final SettingsModelString similarityTypeMdl = createSimilarityTypeModel();
	protected final SettingsModelDoubleBounded alphaMdl = createTverskyAlphaModel();
	protected final SettingsModelDoubleBounded betaMdl = createTverskyBetaModel();
	protected final SettingsModelDoubleBounded thresholdMdl = createSimilarityThresholdModel();
	protected final SettingsModelString comparisonTypeMdl = createComparisonTypeModel();

	protected final NodeLogger logger = NodeLogger.getLogger(this.getClass());
	protected final TransformUtilityFactory<T, U, V> transformUtilFactory;

	/** The column indices of any fingerprint columns */
	private int[] m_fpColIdx;

	/** The similarity type */
	private AttachmentPointFingerprintSimilarityType m_simType;

	/** The comparison type */
	private AttachmentPointFingerprintComparisonType2 m_compType;

	private DataTableSpec outSpec;

	// Fingerprint settings
	private int fpLength;
	private int fpMorganRadius;
	private boolean fpUseBondTypes;
	private boolean fpUseChirality;

	// Columns to keep
	private int[] rxnKeepColIdxs, molKeepColIdxs;

	// Some fields for the view to access
	protected TreeMap<Long, Double> transformProgress = new TreeMap<>();
	protected TreeMap<Long, String> transformSMARTSMap = new TreeMap<>();
	protected double totalProgress = -1;

	/*
	 * Preferences
	 */
	protected IPreferenceStore prefStore = null;
	protected boolean verboseLogging = false;
	protected Integer numThreads, queueSize;
	public ProgressTableModel transformProgressModel = new ProgressTableModel();

	/**
	 * Constructor
	 * 
	 * @param transformUtilFactory
	 *            The {@link TransformUtilityFactory} instance for the node
	 */
	protected AbstractApplyTransformNodeModel(
			TransformUtilityFactory<T, U, V> transformUtilFactory) {
		super(2, 1);
		this.transformUtilFactory = transformUtilFactory;

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

		filterByRxnEnvironmentMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateEnablements();
			}
		});

		similarityTypeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateTverskyParametersEnabledStatus();
			}
		});

		updateEnablements();
	}

	/**
	 * Method to update the enabled status of the Tversky parameters
	 */
	protected void updateTverskyParametersEnabledStatus() {
		alphaMdl.setEnabled(filterByRxnEnvironmentMdl.getBooleanValue()
				&& AttachmentPointFingerprintSimilarityType.valueOf(similarityTypeMdl
						.getStringValue()) == AttachmentPointFingerprintSimilarityType.TVERSKY);
		betaMdl.setEnabled(filterByRxnEnvironmentMdl.getBooleanValue()
				&& AttachmentPointFingerprintSimilarityType.valueOf(similarityTypeMdl
						.getStringValue()) == AttachmentPointFingerprintSimilarityType.TVERSKY);
	}

	/**
	 * Update the similarity type models
	 */
	protected void updateEnablements() {
		similarityTypeMdl.setEnabled(filterByRxnEnvironmentMdl.getBooleanValue());
		keyAPFPColNameMdl.setEnabled(filterByRxnEnvironmentMdl.getBooleanValue());
		thresholdMdl.setEnabled(filterByRxnEnvironmentMdl.getBooleanValue());
		comparisonTypeMdl.setEnabled(filterByRxnEnvironmentMdl.getBooleanValue());
		updateTverskyParametersEnabledStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable[], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
			throws Exception {
		totalProgress = 0.0;
		// Do some generic stuff
		// Buffered datacontainer
		final BufferedDataContainer dc0 = exec.createDataContainer(outSpec);

		// Empty Tables
		if (inData[0].size() == 0 || inData[1].size() == 0) {
			dc0.close();
			return new BufferedDataTable[] { dc0.getTable() };
		}

		// Col IDxs
		DataTableSpec molTableSpec = inData[0].getDataTableSpec();
		DataTableSpec rxnTableSpec = inData[1].getDataTableSpec();
		final int rxnColIdx = rxnTableSpec.findColumnIndex(rxnColNameMdl.getStringValue());
		final int molColIdx = molTableSpec.findColumnIndex(molColNameMdl.getStringValue());
		notifyViews(true);// Reinitialise to catch latest preferences

		final GroupIterableDataTable rxnTable;
		// May need to pre-sort
		ExecutionContext exec1;
		if (!rxnSortedMdl.getBooleanValue()) {
			exec.setMessage("Pre-Sorting transforms");
			double tableProg = 0.25;
			ExecutionContext exec0 = exec.createSubExecutionContext(tableProg);
			exec.getProgressMonitor().addProgressListener(new NodeProgressListener() {

				@Override
				public void progressChanged(NodeProgressEvent pe) {
					Double progress = pe.getNodeProgress().getProgress();
					totalProgress = progress == null ? totalProgress : progress;
					// notifyViews(false);
				}
			});
			exec1 = exec.createSubExecutionContext(0.75);
			rxnTable = new GroupIterableDataTable(getSortedTransformTable(inData[1], exec0),
					rxnColIdx);
			totalProgress = tableProg;
			notifyViews(false);

		} else {
			exec1 = exec.createSubExecutionContext(1.0);
			rxnTable = new GroupIterableDataTable(inData[1], rxnColIdx);
		}

		transformProgressModel.setNumThreads(numThreads);
		exec.setMessage("Applying transforms using " + numThreads + " threads and " + queueSize
				+ " queue items");
		// To save potential massive memory issues we return 'Void' type, having
		// added rows to the output table as we go
		MultiThreadWorker<List<DataRow>, SingleTableParallelResult> processor =
				new MultiThreadWorker<List<DataRow>, SingleTableParallelResult>(queueSize,
						numThreads) {
					long rxnRowsDone = 0;
					double progPerRxnRow = 1.0 / inData[1].size();

					@Override
					protected void processFinished(
							MultiThreadWorker<List<DataRow>, SingleTableParallelResult>.ComputationTask task)
							throws ExecutionException, CancellationException, InterruptedException {
						SingleTableParallelResult singleTableParallelResult = task.get();
						if (singleTableParallelResult != null) {
							try {
								singleTableParallelResult.addRowsToTable(dc0);
							} catch (CanceledExecutionException e) {
								throw new CancellationException();
							}
						}
						rxnRowsDone += task.getInput().size();
						double progress = 1.0 * rxnRowsDone / inData[1].size();
						exec1.setProgress(progress, "Processed " + rxnRowsDone + " of "
								+ inData[1].size() + " transforms");
						// totalProgress =
						// exec.getProgressMonitor().getProgress();
						notifyViews(false);
					}

					@Override
					protected SingleTableParallelResult compute(List<DataRow> in, long index)
							throws Exception {
						SingleTableParallelResult retVal;
						try {
							retVal = findTransformedStructures(in, rxnColIdx, inData[0], molColIdx,
									index + 1L, exec1, progPerRxnRow);
						} catch (CanceledExecutionException e) {
							throw new CancellationException();
						} finally {
							transformUtilFactory.rowCleanup(index + 1L);
						}
						return retVal;
					}
				};
		try {
			processor.run(rxnTable);
		} catch (InterruptedException e) {
			totalProgress = -1;
			CanceledExecutionException cee = new CanceledExecutionException(e.getMessage());
			cee.initCause(e);
			throw cee;
		} catch (ExecutionException e) {
			totalProgress = -1;
			Throwable cause = e.getCause();
			if (cause == null) {
				cause = e;
			}
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new RuntimeException(cause);
		} finally {
			transformProgress.clear();
			transformSMARTSMap.clear();
			transformProgressModel.clear();
			notifyViews(false);
			transformUtilFactory.postExecuteCleanup();
		}

		dc0.close();
		return new BufferedDataTable[] { dc0.getTable() };
	}

	/**
	 * Run the molecule table against a set of rows all containing the same
	 * transform
	 * 
	 * @param in
	 *            A list of rows all sharing the same transform
	 * @param rxnColIdx
	 *            The index of the column containing the reaction transform
	 * 
	 * @param molDataTable
	 *            The molecule table
	 * @param molColIdx
	 *            The index of the column containing the incoming molecules
	 * @param index
	 *            The index of the current thread for the node progress views
	 * @param exec
	 *            The node execution context
	 * @param progressPerTransformRow
	 *            The amount of total execution progress for a single row in the
	 *            transform table
	 *            {@code (=1.0/[nuumber of transform table rows])}
	 * @return <code>null</code> if the transform could not be handled,
	 *         otherwise a temporary table object containing the rows for the
	 *         node output table from the current transform
	 * @throws CanceledExecutionException
	 *             If the user cancelled
	 */
	protected SingleTableParallelResult findTransformedStructures(List<DataRow> in, int rxnColIdx,
			BufferedDataTable molDataTable, int molColIdx, long index, ExecutionContext exec,
			double progressPerTransformRow) throws CanceledExecutionException {

		ExecutionContext exec0 =
				exec.createSubExecutionContext(in.size() * progressPerTransformRow);

		String transformSMARTS =
				transformUtilFactory.getReactionSMARTSFromCell(in.get(0).getCell(rxnColIdx));
		String leafFinderSMARTS = TransformUtils.convertRSmartsToLeafGeneration(transformSMARTS);
		String replacementMatchSMARTS = TransformUtils.convertRSmartsToMatchSmarts(transformSMARTS);

		Set<V> transforms;
		V toLeafTransform;
		U matcher;
		List<DenseBitVector[]> rxnFPs = new ArrayList<>();// Outer list =
															// rows; inner array
															// =
															// cols for each
															// row
		transformProgress.put(index, 0.0);
		transformSMARTSMap.put(index, transformSMARTS);
		transformProgressModel.addTransform(index, transformSMARTS);
		try {
			if (tryChiralProductsMdl.getBooleanValue()) {
				Set<String> transformsrSMARTS =
						TransformUtils.convertRSmartsToChiralProducts(transformSMARTS);
				transforms = new LinkedHashSet<>();
				for (String rSMARTS : transformsrSMARTS) {
					transforms.add(transformUtilFactory.generateReactionFromRSmarts(rSMARTS, false,
							index));
				}
			} else {
				transforms = Collections.singleton(transformUtilFactory
						.generateReactionFromRSmarts(transformSMARTS, false, index));
			}
			if (filterByRxnEnvironmentMdl.getBooleanValue()) {
				toLeafTransform = transformUtilFactory.generateReactionFromRSmarts(leafFinderSMARTS,
						false, index);
				for (DataRow rxnRow : in) {
					List<DenseBitVector> fps = new ArrayList<>();
					for (int i : m_fpColIdx) {
						if (rxnRow.getCell(i).isMissing()) {
							break;
						}
						fps.add(((DenseBitVectorCell) rxnRow.getCell(i)).getBitVectorCopy());
					}
					if (m_compType == AttachmentPointFingerprintComparisonType2.CONCATENATED
							&& fps.size() > 1) {
						DenseBitVector dbv = fps.get(0);
						for (int i = 1; i < fps.size(); i++) {
							dbv = dbv.concatenate(fps.get(i));
						}
						fps = Collections.singletonList(dbv);
					}
					rxnFPs.add(fps.toArray(new DenseBitVector[fps.size()]));
				}
			} else {
				toLeafTransform = null;
			}
			matcher = transformUtilFactory.generateQueryMoleculeFromSMARTS(replacementMatchSMARTS,
					index);
		} catch (ToolkitException e) {
			transformProgress.remove(index);
			transformSMARTSMap.remove(index);
			transformProgressModel.removeTransfrom(index);
			notifyViews(false);
			return null;
		}

		DataCell[] rxnCells = null;// only instantiate if needed
		SingleTableParallelResult retVal = new SingleTableParallelResult(exec0, outSpec);
		double progressPerMolRow = 1.0 / molDataTable.size();
		long molRowIdx = 0;
		for (DataRow molRow : molDataTable) {
			exec0.checkCanceled();

			if ((++molRowIdx) % 100 == 0) {
				double progress = molRowIdx * progressPerMolRow;
				transformProgress.put(index, progress);
				transformProgressModel.updateProgress(index, progress);
				exec0.setProgress(progress, "Applying transform " + index + " to molecule row "
						+ molRowIdx + " of " + molDataTable.size());
				notifyViews(false);
			}
			T mol;
			try {
				mol = transformUtilFactory.getMolFromCell(molRow.getCell(molColIdx), index,
						false/* Dont take any incoming explicit H's off */);
			} catch (ToolkitException e) {
				// skip the row
				continue;
			}

			if (transformUtilFactory.moleculeIsEmpty(mol)
					|| transformUtilFactory.moleculeIsMultiComponent(mol)
					|| !transformUtilFactory.molMatchesQuery(mol, matcher)) {
				// Skip
				continue;
			}

			// Now see if we need to check the fp environment..
			List<Set<Integer>> molMatchAtomSets;
			try {
				molMatchAtomSets = transformUtilFactory.getMatchAtomSets(mol, matcher);
			} catch (ToolkitException e) {
				continue;
			}
			boolean[] passes = new boolean[molMatchAtomSets.size()];
			if (filterByRxnEnvironmentMdl.isEnabled()
					&& filterByRxnEnvironmentMdl.getBooleanValue()) {
				List<DenseBitVector[]> molFps;
				try {
					molFps = transformUtilFactory.getEnvironmentFPs(mol, toLeafTransform, index,
							fpLength, fpMorganRadius, fpUseBondTypes, fpUseChirality,
							m_compType == AttachmentPointFingerprintComparisonType2.CONCATENATED);
				} catch (ToolkitException e) {
					continue;
				}
				for (int i = 0; i < molFps.size(); i++) {
					for (int j = 0; j < rxnFPs.size() && !passes[i]; j++) {
						passes[i] = m_compType.checkPasses(rxnFPs.get(j), molFps.get(i), m_simType,
								thresholdMdl.getDoubleValue(), alphaMdl.getDoubleValue(),
								betaMdl.getDoubleValue());
					}
				}
			} else {
				Arrays.fill(passes, true);
			}

			Set<Integer> okMatchAtoms = new TreeSet<>();
			int passCount = 0;
			for (int i = 0; i < molMatchAtomSets.size(); i++) {
				if (passes[i]) {
					okMatchAtoms.addAll(molMatchAtomSets.get(i));
					passCount++;
				}
			}
			if (passCount == 0) {
				// No matches pass filter
				continue;
			}

			Set<String> productSmiles;
			try {
				productSmiles = new TreeSet<>();
				for (V trans : transforms) {
					productSmiles.addAll(transformUtilFactory.getTransformedMoleculesSmiles(mol,
							trans, okMatchAtoms, tryChiralProductsMdl.getBooleanValue(), index));
				}
			} catch (ToolkitException e) {
				continue;
			}

			long subRowIdx = 0;
			// Sort the SMILES to create some semblance of order
			for (String prod : new TreeSet<>(productSmiles)) {
				RowKey newKey = new RowKey(
						molRow.getKey().getString() + "_Transform" + index + "_" + (subRowIdx++));
				if (rxnCells == null) {
					rxnCells = new DataCell[rxnKeepColIdxs.length];
					for (int i = 0; i < rxnCells.length; i++) {
						if (rxnKeepColIdxs[i] == rxnColIdx) {
							rxnCells[i] = in.get(0).getCell(rxnColIdx);
						} else {
							List<DataCell> cells = new ArrayList<>();
							for (DataRow rxnRow : in) {
								cells.add(rxnRow.getCell(rxnKeepColIdxs[i]));
							}
							rxnCells[i] = CollectionCellFactory.createListCell(cells);
						}
					}
				}
				DataRow molCells = new FilterColumnRow(molRow, molKeepColIdxs);
				DataRow outRow = new DefaultRow(newKey,
						new DataCell[] { SmilesCellFactory.createAdapterCell(prod),
								molRow.getCell(molColIdx), in.get(0).getCell(rxnColIdx) });
				outRow = new AppendedColumnRow(newKey, outRow, molCells);
				outRow = new AppendedColumnRow(outRow, rxnCells);
				retVal.addRowToTable(outRow);
			}

		}
		transformProgress.remove(index);
		transformSMARTSMap.remove(index);
		transformProgressModel.removeTransfrom(index);
		notifyViews(false);
		return retVal;

	}

	/**
	 * Method to sort the reaction table by the reaction column
	 * 
	 * @param inTransformTable
	 *            The incoming reaction table
	 * @param exec
	 *            The node execution context
	 * @return The sorted table
	 * @throws CanceledExecutionException
	 *             If the user cancels
	 */
	private BufferedDataTable getSortedTransformTable(BufferedDataTable inTransformTable,
			ExecutionContext exec) throws CanceledExecutionException {
		BufferedDataTableSorter sorter = new BufferedDataTableSorter(inTransformTable,
				Collections.singleton(rxnColNameMdl.getStringValue()), new boolean[] { true },
				false);
		return sorter.sort(exec);
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

		DataTableSpec molTableSpec = inSpecs[0];
		DataTableSpec rxnTableSpec = inSpecs[1];

		// Check the molCol is a molecule
		DataColumnSpec molColSpec = molTableSpec.getColumnSpec(molColNameMdl.getStringValue());

		if (molColSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = molTableSpec.getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (isColTypeMolCompatible(molTableSpec.getColumnSpec(i).getType())) {
					molColNameMdl.setStringValue(molTableSpec.getColumnSpec(i).getName());
					molColSpec = molTableSpec.getColumnSpec(molColNameMdl.getStringValue());
					logger.warn("No molecule column selected. " + molColNameMdl.getStringValue()
							+ " auto-selected.");
					break;
				}
			}

			// If we are here and still null, then no suitable column found
			if (molColSpec == null) {
				logger.error("No molecule column of the accepted"
						+ " input formats (SDF, Mol, SMILES) was found.");
				throw new InvalidSettingsException("No molecule column of the accepted"
						+ " input formats (SDF, Mol, SMILES) was found.");
			}
		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!isColTypeMolCompatible(molColSpec.getType())) {
				// The column is not compatible with one of the accepted types
				logger.error("The column " + molColNameMdl.getStringValue()
						+ " is not one of the accepted" + " input formats (SDF, Mol, SMILES)");
				throw new InvalidSettingsException("The column " + molColNameMdl.getStringValue()
						+ " is not one of the accepted" + " input formats (SDF, Mol, SMILES)");
			}
		}

		// Check the rxnCol is a reaction
		DataColumnSpec rxnColSpec = rxnTableSpec.getColumnSpec(rxnColNameMdl.getStringValue());

		if (rxnColSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = rxnTableSpec.getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (isColTypeReactionCompatible(rxnTableSpec.getColumnSpec(i).getType())) {
					rxnColNameMdl.setStringValue(rxnTableSpec.getColumnSpec(i).getName());
					rxnColSpec = rxnTableSpec.getColumnSpec(rxnColNameMdl.getStringValue());
					logger.warn("No molecule column selected. " + rxnColNameMdl.getStringValue()
							+ " auto-selected.");
					break;
				}

			}
			// If we are here and still, then no suitable column found
			if (rxnColSpec == null) {
				logger.error("No molecule column of the accepted"
						+ " input formats (SMARTS, RXN) was found.");
				throw new InvalidSettingsException("No molecule column of the accepted"
						+ " input formats (SMARTS, RXN) was found.");
			}
		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!isColTypeReactionCompatible(rxnColSpec.getType())) {
				// The column is not compatible with one of the accepted types
				logger.error("The column " + molColNameMdl.getStringValue()
						+ " is not one of the accepted" + " input formats (SMARTS, RXN)");
				throw new InvalidSettingsException("The column " + molColNameMdl.getStringValue()
						+ " is not one of the accepted" + " input formats (SMARTS, RXN)");
			}
		}

		if (filterByRxnEnvironmentMdl.isEnabled() && filterByRxnEnvironmentMdl.getBooleanValue()) {

			// Check the fpCol is a f
			DataColumnSpec fpColSpec =
					rxnTableSpec.getColumnSpec(keyAPFPColNameMdl.getStringValue());

			if (fpColSpec == null) {
				// No column selected, or selected column not found - autoguess!
				for (int i = rxnTableSpec.getNumColumns() - 1; i >= 0; i--) {
					// Reverse order to select most recently added
					if (isColTypeFpCompatible(rxnTableSpec.getColumnSpec(i).getType())) {
						keyAPFPColNameMdl.setStringValue(rxnTableSpec.getColumnSpec(i).getName());
						logger.warn("No fingerprint column selected. "
								+ keyAPFPColNameMdl.getStringValue() + " auto-selected.");
						break;
					}
					// If we are here when i = 0, then no suitable column found
					if (i == 0) {
						logger.error(
								"No fingerpring column of the accepted" + " formats was found.");
						throw new InvalidSettingsException(
								"No fingerprint column of the accepted" + " formats was found.");
					}
				}

			} else {
				// We had a selected column, now lets see if it is a compatible
				// type
				if (!isColTypeFpCompatible(fpColSpec.getType())) {
					// The column is not compatible with one of the accepted
					// types
					logger.error("The column " + keyAPFPColNameMdl.getStringValue()
							+ " is not one of the accepted input formats");
					throw new InvalidSettingsException(
							"The column " + keyAPFPColNameMdl.getStringValue()
									+ " is not one of the accepted input formats");
				}
			}

			DataColumnProperties fpColProps = fpColSpec.getProperties();
			if (fpColProps.containsProperty("Toolkit")) {
				if (!fpColProps.getProperty("Toolkit")
						.equals(transformUtilFactory.getToolkitName())) {
					throw new InvalidSettingsException(
							"Fingerprints were generated with a different toolkit ("
									+ fpColProps.getProperty("Toolkit") + ")");
				}
				fpLength = Integer.parseInt(fpColProps.getProperty("Length"));
				fpMorganRadius = Integer.parseInt(fpColProps.getProperty("Radius"));
				fpUseBondTypes = fpColProps.containsProperty("Use bond types")
						? Boolean.parseBoolean(fpColProps.getProperty("Use bond types"))
						: MMPConstants.DEFAULT_USE_BOND_TYPES;
				fpUseChirality = fpColProps.containsProperty("Use chirality")
						? Boolean.parseBoolean(fpColProps.getProperty("Use chirality"))
						: MMPConstants.DEFAULT_USE_CHIRALITY;
			} else {
				setWarningMessage("No fingerprint header - using defaults");
				fpLength = (int) MMPConstants.DEFAULT_FP_LENGTH;
				fpMorganRadius = (int) MMPConstants.DEFAULT_FP_RADIUS;
				fpUseBondTypes = MMPConstants.DEFAULT_USE_BOND_TYPES;
				fpUseChirality = MMPConstants.DEFAULT_USE_CHIRALITY;
			}

			// Find the indices of the correct set of fingerprint columns
			Pattern fpColPattern =
					Pattern.compile(
							"^Attachment point \\d+ fingerprint"
									+ keyAPFPColNameMdl.getStringValue().replaceAll(
											"^Attachment point 1 fingerprint(.*)", "\\\\Q$1\\\\E")
									+ "$");
			m_fpColIdx = rxnTableSpec.stream().map(spec -> spec.getName())
					.filter(colName -> fpColPattern.matcher(colName).matches())
					.mapToInt(name -> rxnTableSpec.findColumnIndex(name)).toArray();
			if (m_fpColIdx.length == 0) {
				throw new InvalidSettingsException("No fingerprint columns found");
			}
			m_simType = AttachmentPointFingerprintSimilarityType
					.valueOf(similarityTypeMdl.getStringValue());
			m_compType = AttachmentPointFingerprintComparisonType2
					.valueOf(comparisonTypeMdl.getStringValue());

		} else {
			m_simType = null;
			m_compType = null;
		}

		int colCnt = 3;// Transformed molecule, parent molecule and transform
		String[] molColNames = Arrays
				.stream(molPassThroughColsMdl.applyTo(molTableSpec).getIncludes())
				.filter(x -> !x.equals(molColNameMdl.getStringValue())).toArray(x -> new String[x]);
		String[] rxnColNames = Arrays
				.stream(rxnPassThroughColsMdl.applyTo(rxnTableSpec).getIncludes())
				.filter(x -> !x.equals(rxnColNameMdl.getStringValue())).toArray(x -> new String[x]);
		colCnt += molColNames.length + rxnColNames.length;

		DataColumnSpec[] outSpecs = new DataColumnSpec[colCnt];
		int colIdx = 0;
		outSpecs[colIdx++] =
				new DataColumnSpecCreator("Transformed Molecule", SmilesCellFactory.TYPE)
						.createSpec();
		if (molColNameMdl.getStringValue().equals(rxnColNameMdl.getStringValue())) {
			DataColumnSpecCreator specFact = new DataColumnSpecCreator(
					molTableSpec.getColumnSpec(molColNameMdl.getStringValue()));
			specFact.setName(molColNameMdl.getStringValue() + " (Molecule Table)");
			outSpecs[colIdx++] = specFact.createSpec();
			specFact = new DataColumnSpecCreator(
					rxnTableSpec.getColumnSpec(rxnColNameMdl.getStringValue()));
			specFact.setName(rxnColNameMdl.getStringValue() + " (Transform Table)");
			outSpecs[colIdx++] = specFact.createSpec();
		} else {
			outSpecs[colIdx++] = molTableSpec.getColumnSpec(molColNameMdl.getStringValue());
			outSpecs[colIdx++] = rxnTableSpec.getColumnSpec(rxnColNameMdl.getStringValue());
		}
		for (String molColName : molColNames) {
			DataColumnSpecCreator specFact =
					new DataColumnSpecCreator(molTableSpec.getColumnSpec(molColName));
			specFact.setName(molColName + " (Molecule)");
			outSpecs[colIdx++] = specFact.createSpec();
		}

		rxnKeepColIdxs = Arrays.stream(rxnColNames).mapToInt(x -> rxnTableSpec.findColumnIndex(x))
				.sorted().toArray();
		molKeepColIdxs = Arrays.stream(molColNames).mapToInt(x -> molTableSpec.findColumnIndex(x))
				.sorted().toArray();

		for (String rxnColName : rxnColNames) {
			DataColumnSpecCreator specFact = new DataColumnSpecCreator(rxnColName + " (Transform)",
					ListCell.getCollectionType(rxnTableSpec.getColumnSpec(rxnColName).getType()));
			specFact.setProperties(rxnTableSpec.getColumnSpec(rxnColName).getProperties());
			outSpecs[colIdx++] = specFact.createSpec();
		}

		outSpec = new DataTableSpec(outSpecs);
		return new DataTableSpec[] { outSpec };
	}

	/**
	 * @param type
	 *            The column {@link DataType}
	 * @return <code>true</code> if the column is a dense bit vector
	 */
	private boolean isColTypeFpCompatible(DataType type) {
		return type == DenseBitVectorCell.TYPE;
	}

	/**
	 * @param type
	 *            The column {@link DataType}
	 * @return <code>true</code> if the column is one of those accepted by the
	 *         {@link TransformUtilityFactory} for incoming molecules
	 */
	private boolean isColTypeMolCompatible(DataType type) {
		for (Class<? extends DataValue> molType : transformUtilFactory.getInputColumnTypes()) {
			if (type.isCompatible(molType) || type.isAdaptable(molType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param type
	 *            The column {@link DataType}
	 * @return <code>true</code> if the column is one of those accepted by the
	 *         {@link TransformUtilityFactory} for incoming transforms
	 */
	protected boolean isColTypeReactionCompatible(DataType type) {
		for (Class<? extends DataValue> rxnType : transformUtilFactory.getReactionCellTypes()) {
			if (type.isCompatible(rxnType) || type.isAdaptable(rxnType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		molColNameMdl.saveSettingsTo(settings);
		molPassThroughColsMdl.saveSettingsTo(settings);
		tryChiralProductsMdl.saveSettingsTo(settings);
		rxnColNameMdl.saveSettingsTo(settings);
		rxnSortedMdl.saveSettingsTo(settings);
		rxnPassThroughColsMdl.saveSettingsTo(settings);
		filterByRxnEnvironmentMdl.saveSettingsTo(settings);
		keyAPFPColNameMdl.saveSettingsTo(settings);
		similarityTypeMdl.saveSettingsTo(settings);
		alphaMdl.saveSettingsTo(settings);
		betaMdl.saveSettingsTo(settings);
		thresholdMdl.saveSettingsTo(settings);
		comparisonTypeMdl.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		molColNameMdl.validateSettings(settings);
		molPassThroughColsMdl.validateSettings(settings);
		tryChiralProductsMdl.validateSettings(settings);
		rxnColNameMdl.validateSettings(settings);
		rxnSortedMdl.validateSettings(settings);
		rxnPassThroughColsMdl.validateSettings(settings);
		filterByRxnEnvironmentMdl.validateSettings(settings);
		keyAPFPColNameMdl.validateSettings(settings);
		similarityTypeMdl.validateSettings(settings);
		alphaMdl.validateSettings(settings);
		betaMdl.validateSettings(settings);
		thresholdMdl.validateSettings(settings);
		comparisonTypeMdl.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		molColNameMdl.loadSettingsFrom(settings);
		molPassThroughColsMdl.loadSettingsFrom(settings);
		tryChiralProductsMdl.loadSettingsFrom(settings);
		rxnColNameMdl.loadSettingsFrom(settings);
		rxnSortedMdl.loadSettingsFrom(settings);
		rxnPassThroughColsMdl.loadSettingsFrom(settings);
		filterByRxnEnvironmentMdl.loadSettingsFrom(settings);
		keyAPFPColNameMdl.loadSettingsFrom(settings);
		similarityTypeMdl.loadSettingsFrom(settings);
		alphaMdl.loadSettingsFrom(settings);
		betaMdl.loadSettingsFrom(settings);
		thresholdMdl.loadSettingsFrom(settings);
		comparisonTypeMdl.loadSettingsFrom(settings);

	}

	@Override
	protected void reset() {
		transformUtilFactory.nodeReset();
		transformSMARTSMap.clear();
		transformProgress.clear();
		transformProgressModel.clear();
		totalProgress = -1.0;
		notifyViews(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		transformUtilFactory.nodeDispose();
		super.onDispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		transformUtilFactory.nodeDispose();
		super.finalize();
	}

}
