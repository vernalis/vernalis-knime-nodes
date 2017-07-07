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
package com.vernalis.knime.mmp.nodes.pairgen.abstrct;

import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createAllowHiliteModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createAllowSelfTransformsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createFilterByDeltaHACModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createForceSingleAcyclicsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createFragKeyModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createFragValueModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createGraphDistDblFilterCutoffModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createGraphDistFpColnameModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createGraphDistIntFilterCutoffModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createHACDeltaRangeModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createIDModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createIncludeGraphSimilarityModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createLMinusRModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createLdbRModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createLeftPassThroughColsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createOutputChangingHACountsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createOutputHARatiosModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createOutputKeyModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createRMinusLModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createRdbLModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createRightPassThroughColsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createShowDeltaHACModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createShowReverseTransformsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createShowSmartsTransformsModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createSortedKeysModel;
import static com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeDialog.createValueGraphDistFilterModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.knime.chem.types.SmartsCellFactory;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.ComplexNumberValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.ComplexNumberCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.data.vector.bytevector.ByteVectorValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.property.hilite.DefaultHiLiteMapper;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteTranslator;
import org.knime.core.util.DuplicateChecker;
import org.knime.core.util.DuplicateKeyException;
import org.knime.core.util.MultiThreadWorker;
import org.knime.core.util.Pair;

import com.vernalis.knime.data.datarow.KnowsParentRowIDsDataRow;
import com.vernalis.knime.data.datatable.GroupIterableDataTable;
import com.vernalis.knime.dialog.components.SettingsModelDoubleBoundedRerangable;
import com.vernalis.knime.dialog.components.SettingsModelIntegerBoundedRerangable;
import com.vernalis.knime.dialog.components.SettingsModelIntegerRange;
import com.vernalis.knime.iterators.PairwiseIterable;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.frags.simple.SimpleFragmentKey;
import com.vernalis.knime.mmp.frags.simple.SimpleFragmentValue;
import com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage;
import com.vernalis.knime.mmp.transform.TransformUtils;
import com.vernalis.knime.parallel.SingleTableParallelResult;

/**
 * The node model for the Abstract matched pairs from fragments node
 * 
 * @author s.roughley
 * 
 */
abstract public class AbstractMatchedPairsFromFragmentsNodeModel extends NodeModel {

	protected final SettingsModelString m_FragKeyColName = createFragKeyModel();
	protected final SettingsModelBoolean m_keysAreSorted;
	protected final SettingsModelString m_IDColName = createIDModel();
	protected final SettingsModelString m_FragValColName = createFragValueModel();
	protected final SettingsModelBoolean m_hiliteMdl = createAllowHiliteModel();
	protected final SettingsModelBoolean m_filterByDeltaHACMdl = createFilterByDeltaHACModel();
	protected final SettingsModelIntegerRange m_hacDeltaRangeMdl = createHACDeltaRangeModel();
	protected final SettingsModelBoolean m_showHACDeltaMdl = createShowDeltaHACModel();
	protected final SettingsModelString m_GraphDistCutoffType = createValueGraphDistFilterModel();
	protected final SettingsModelDoubleBoundedRerangable m_GraphDistDblCutOff =
			createGraphDistDblFilterCutoffModel();
	protected final SettingsModelIntegerBoundedRerangable m_GraphDistIntCutOff =
			createGraphDistIntFilterCutoffModel();
	protected final SettingsModelString m_graphDistFpColName = createGraphDistFpColnameModel();
	protected final SettingsModelBoolean m_inclGraphDistDists = createIncludeGraphSimilarityModel();
	protected final SettingsModelBoolean m_includeUnchangingPortions = createOutputKeyModel();
	protected final SettingsModelBoolean m_includeHACount = createOutputChangingHACountsModel();
	protected final SettingsModelBoolean m_includeHARatio = createOutputHARatiosModel();
	protected final SettingsModelBoolean m_showReverseTransforms =
			createShowReverseTransformsModel();
	protected final SettingsModelBoolean m_includeReactionSMARTS =
			createShowSmartsTransformsModel();
	protected final SettingsModelBoolean m_requireAcyclicSMARTS = createForceSingleAcyclicsModel();
	protected final SettingsModelBoolean m_AllowSelfTransforms = createAllowSelfTransformsModel();
	protected final SettingsModelColumnFilter2 m_LeftMinusRightColumns = createLMinusRModel();
	protected final SettingsModelColumnFilter2 m_RightMinusLeftColumns = createRMinusLModel();
	protected final SettingsModelColumnFilter2 m_LeftDivbyRightColumns = createLdbRModel();
	protected final SettingsModelColumnFilter2 m_RightDivbyLeftColumns = createRdbLModel();
	protected final SettingsModelColumnFilter2 m_LeftPassThroughColumns =
			createLeftPassThroughColsModel();
	protected final SettingsModelColumnFilter2 m_RightPassThroughColumns =
			createRightPassThroughColsModel();

	/** The NodeLogger Instance */
	static final protected NodeLogger m_logger =
			NodeLogger.getLogger(AbstractMatchedPairsFromFragmentsNodeModel.class);

	/** The output table spec */
	protected DataTableSpec m_outSpec;
	protected int numCols;
	protected int[] leftKeepColIdxs, rightWithLeftKeepColIdxs, rightOnlyKeepColIdxs, lMinRColIdxs,
			rMinLColIdxs, lDivRColIdxs, rDivLColIdxs;

	protected IPreferenceStore prefStore = null;

	protected boolean verboseLogging = false;
	protected Integer numThreads, queueSize;
	protected final boolean presortTableByKey;
	protected ValueGraphDistanceFingerprintComparisonType graphDistFPComparisonType;
	protected final boolean hasTwoInputs;
	protected final boolean showBothKeys;

	// Highlighting fields
	/** Mapping from the input row to fragments */
	protected final HiLiteTranslator topHiLiteTranslator = new HiLiteTranslator();
	protected final HiLiteTranslator bottomHiLiteTranslator;
	protected static final String HILITE_KEY = "HiLiteMapping";
	protected static final String SETTINGS_FILE_NAME = "internals";

	/**
	 * Constructor for the node model class with 1 in and 1 out port, showing
	 * only 1 key
	 * 
	 * @param presortTableByKey
	 *            Should the table be presorted by key
	 */
	public AbstractMatchedPairsFromFragmentsNodeModel(boolean presortTableByKey) {
		this(presortTableByKey, false);
	}

	/**
	 * Constructor for the node model class showing only 1 key
	 * 
	 * @param presortTableByKey
	 *            Should the table be presorted by key
	 * @param hasTwoInputs
	 *            Should the node have two input ports?
	 */
	public AbstractMatchedPairsFromFragmentsNodeModel(boolean presortTableByKey,
			boolean hasTwoInputs) {
		this(presortTableByKey, hasTwoInputs, false);
	}

	/**
	 * Constructor for the node model class
	 * 
	 * @param presortTableByKey
	 *            Should the table be presorted by key
	 * @param hasTwoInputs
	 *            Should the node have two input ports?
	 * @param showBothKeys
	 *            Should both L & R keys be shown if the 'show unchanging
	 *            portion' option is selected?
	 */
	public AbstractMatchedPairsFromFragmentsNodeModel(boolean presortTableByKey,
			boolean hasTwoInputs, boolean showBothKeys) {
		super(hasTwoInputs ? 2 : 1, 1);
		this.hasTwoInputs = hasTwoInputs;
		this.showBothKeys = showBothKeys;
		m_filterByDeltaHACMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_hacDeltaRangeMdl.setEnabled(m_filterByDeltaHACMdl.getBooleanValue());
				m_showHACDeltaMdl.setBooleanValue(m_filterByDeltaHACMdl.getBooleanValue());
			}
		});
		m_hacDeltaRangeMdl.setEnabled(m_filterByDeltaHACMdl.getBooleanValue());
		m_showHACDeltaMdl.setBooleanValue(m_filterByDeltaHACMdl.getBooleanValue());

		m_includeReactionSMARTS.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_requireAcyclicSMARTS.setEnabled(m_includeReactionSMARTS.getBooleanValue());

			}
		});
		m_requireAcyclicSMARTS.setEnabled(m_includeReactionSMARTS.getBooleanValue());

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

		updateGraphDistModels();
		m_GraphDistCutoffType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateGraphDistModels();

			}
		});

		verboseLogging = prefStore.getBoolean(MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING);
		queueSize = MatchedPairPreferencePage.getQueueSize();
		numThreads = MatchedPairPreferencePage.getThreadsCount();

		this.presortTableByKey = presortTableByKey;
		if (presortTableByKey) {
			m_keysAreSorted = createSortedKeysModel();
		} else {
			m_keysAreSorted = null;
		}

		bottomHiLiteTranslator = hasTwoInputs ? new HiLiteTranslator() : null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable [], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		graphDistFPComparisonType = ValueGraphDistanceFingerprintComparisonType
				.valueOf(m_GraphDistCutoffType.getStringValue());

		// Do some generic stuff
		// Buffered datacontainer
		final BufferedDataContainer dc0 = exec.createDataContainer(m_outSpec);

		// Empty Tables
		if (inData[0].size() == 0 || (inData.length > 1 && inData[1].size() == 0)) {
			dc0.close();
			return new BufferedDataTable[] { dc0.getTable() };
		}

		final BufferedDataTable[] tables;
		// May need to pre-sort
		ExecutionContext exec1;
		if (presortTableByKey && !m_keysAreSorted.getBooleanValue()) {
			tables = new BufferedDataTable[inData.length];
			exec.setMessage("Pre-Sorting table(s) by fragmentation keys");
			for (int i = 0; i < inData.length; i++) {
				double tableProg = 0.25 / inData.length;
				ExecutionContext exec0 = exec.createSubExecutionContext(tableProg);
				tables[i] = getSortedTable(inData[i], exec0);
			}
			exec1 = exec.createSubExecutionContext(0.75);
		} else {
			tables = inData;
			exec1 = exec.createSubExecutionContext(1.0);
		}

		// Col IDxs
		DataTableSpec inSpec = inData[0].getDataTableSpec();
		final int keyColIdx = inSpec.findColumnIndex(m_FragKeyColName.getStringValue());
		final int valColIdx = inSpec.findColumnIndex(m_FragValColName.getStringValue());
		final int idColIdx = inSpec.findColumnIndex(m_IDColName.getStringValue());
		final int graphFpColIdx = m_graphDistFpColName.isEnabled()
				? inSpec.findColumnIndex(m_graphDistFpColName.getStringValue()) : -1;

		// If we are a presorting table implementation then we are going to
		// process the table in chunks of matching keys, otherwise we are going
		// row-wise
		GroupIterableDataTable[] groupTables = new GroupIterableDataTable[tables.length];
		for (int i = 0; i < tables.length; i++) {
			groupTables[i] =
					new GroupIterableDataTable(tables[i], presortTableByKey ? keyColIdx : -1);
		}

		Map<RowKey, Set<RowKey>> topMapping = new HashMap<>();
		Map<RowKey, Set<RowKey>> bottomMapping = hasTwoInputs ? new HashMap<>() : topMapping;
		DuplicateChecker dupCheck = new DuplicateChecker();

		exec.setMessage(
				"Generating pairs using " + numThreads + " threads (queue size " + queueSize + ")");
		m_logger.info(
				"Generating pairs using " + numThreads + " threads (queue size " + queueSize + ")");
		if (!presortTableByKey || groupTables.length == 1) {
			final long numRows = inData[groupTables.length - 1].size();
			MultiThreadWorker<List<DataRow>, List<SingleTableParallelResult>> processor =
					new MultiThreadWorker<List<DataRow>, List<SingleTableParallelResult>>(queueSize,
							numThreads) {
						long rowsDone = 0;
						double numComparisons = numRows * (numRows - 1);

						@Override
						protected void processFinished(ComputationTask task)
								throws ExecutionException, CancellationException,
								InterruptedException {

							// task.get().stream().forEachOrdered(x ->
							// x.addRowsToTable(dc0));

							for (SingleTableParallelResult outTable : task.get()) {
								for (DataRow outRow : outTable) {
									RowKey toKey = outRow.getKey();
									int idx = 0;
									boolean changed = false;
									while (true) {
										try {
											dupCheck.addKey(toKey.getString());
											break;
										} catch (DuplicateKeyException e) {
											toKey = new RowKey(idx == 0
													? toKey.getString() + "_(#" + (idx++) + ")"
													: toKey.getString().replaceAll(
															"(.*_\\(#)[\\d]+\\)",
															"$1" + (idx++) + ")"));
											changed = true;
										} catch (IOException e) {
											throw new ExecutionException(e);
										}
									}
									if (changed) {
										dc0.addRowToTable(new DefaultRow(toKey, outRow));
									} else {
										dc0.addRowToTable(outRow);
									}
									if (m_hiliteMdl.getBooleanValue()) {
										RowKey lKey, rKey;
										if (outRow instanceof KnowsParentRowIDsDataRow) {
											lKey = ((KnowsParentRowIDsDataRow) outRow).getLeftKey();
											rKey = ((KnowsParentRowIDsDataRow) outRow)
													.getRightKey();
										} else {
											// we have to try to guess, which is
											// dangerous...
											final String[] keyParts = toKey.getString().split("_");
											lKey = new RowKey(keyParts[0]);
											rKey = new RowKey(keyParts[1]);
										}
										HashSet<RowKey> topSet = new HashSet<>();
										topSet.add(lKey);
										topMapping.put(toKey, topSet);
										if (!bottomMapping.containsKey(toKey)) {
											bottomMapping.put(toKey, new HashSet<>());
										}
										bottomMapping.get(toKey).add(rKey);
									}
								}
							}
							rowsDone += task.getInput().size();
							// TODO: Fix this!
							// When we are comparing row-wise we base progress
							// on
							// (n*n-1)/2
							// comparisons needed
							// Otherwise we assume in grouping that the progress
							// is more
							// linear (well, we cant really do much else)
							double progress = presortTableByKey || inData.length > 1
									? 1.0 * rowsDone / numRows
									: rowsDone * (rowsDone - 1) / numComparisons;
							exec1.setProgress(progress,
									"Processed " + rowsDone + " of " + numRows + (presortTableByKey
											? " (" + task.getIndex() + " key sets)" : ""));
							try {
								exec1.checkCanceled();
							} catch (CanceledExecutionException e) {
								throw new CancellationException();
							}
						}

						@Override
						protected List<SingleTableParallelResult> compute(List<DataRow> in,
								long index) throws Exception {
							List<SingleTableParallelResult> retVal = new ArrayList<>();
							if (presortTableByKey && in.size() == 1) {
								// Only 1 row means no pairs possible - ~2.5%
								// time
								// saving
								return retVal;
							}
							for (DataRow row : in) {
								// Process each row in group in turn - either
								// against
								// rest
								// of group or whole table
								retVal.add(processRow(row, presortTableByKey ? in : tables[0],
										keyColIdx, valColIdx, idColIdx, graphFpColIdx, exec));
								exec1.checkCanceled();
							}
							return retVal;
						}
					};

			try {
				processor.run(groupTables[groupTables.length - 1]);
			} catch (InterruptedException e) {
				CanceledExecutionException cee = new CanceledExecutionException(e.getMessage());
				cee.initCause(e);
				throw cee;
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause == null) {
					cause = e;
				}
				if (cause instanceof RuntimeException) {
					throw (RuntimeException) cause;
				}
				throw new RuntimeException(cause);
			}
		} else {
			// When we have 2 presorted table we have a special case where we
			// can enhance efficiency
			final long numRows = inData[1].size();
			MultiThreadWorker<Pair<List<DataRow>, List<DataRow>>, List<SingleTableParallelResult>> processor =
					new MultiThreadWorker<Pair<List<DataRow>, List<DataRow>>, List<SingleTableParallelResult>>(
							queueSize, numThreads) {
						long rowsDone = 0;

						@Override
						protected List<SingleTableParallelResult> compute(
								Pair<List<DataRow>, List<DataRow>> in, long index)
								throws Exception {
							List<SingleTableParallelResult> retVal = new ArrayList<>();
							for (DataRow row : in.getFirst()) {
								// Process query table rows (1st in pair, 2nd
								// input) against reference table(2nd in pair,
								// 1st input)
								retVal.add(processRow(row, in.getSecond(), keyColIdx, valColIdx,
										idColIdx, graphFpColIdx, exec));
								exec1.checkCanceled();
							}
							return retVal;
						}

						@Override
						protected void processFinished(
								MultiThreadWorker<Pair<List<DataRow>, List<DataRow>>, List<SingleTableParallelResult>>.ComputationTask task)
								throws ExecutionException, CancellationException,
								InterruptedException {
							for (SingleTableParallelResult outTable : task.get()) {
								for (DataRow outRow : outTable) {
									RowKey toKey = outRow.getKey();
									int idx = 0;
									boolean changed = false;
									while (true) {
										try {
											dupCheck.addKey(toKey.getString());
											break;
										} catch (DuplicateKeyException e) {
											toKey = new RowKey(idx == 0
													? toKey.getString() + "_(#" + (idx++) + ")"
													: toKey.getString().replaceAll(
															"(.*_\\(#)[\\d]+\\)",
															"$1" + (idx++) + ")"));
											changed = true;
										} catch (IOException e) {
											throw new ExecutionException(e);
										}
									}
									if (changed) {
										dc0.addRowToTable(new DefaultRow(toKey, outRow));
									} else {
										dc0.addRowToTable(outRow);
									}
									if (m_hiliteMdl.getBooleanValue()) {
										RowKey lKey, rKey;
										if (outRow instanceof KnowsParentRowIDsDataRow) {
											lKey = ((KnowsParentRowIDsDataRow) outRow).getLeftKey();
											rKey = ((KnowsParentRowIDsDataRow) outRow)
													.getRightKey();
										} else {
											// we have to try to guess, which is
											// dangerous...
											final String[] keyParts = toKey.getString().split("_");
											lKey = new RowKey(keyParts[0]);
											rKey = new RowKey(keyParts[1]);
										}
										HashSet<RowKey> topSet = new HashSet<>();
										topSet.add(lKey);
										topMapping.put(toKey, topSet);
										if (!bottomMapping.containsKey(toKey)) {
											bottomMapping.put(toKey, new HashSet<>());
										}
										bottomMapping.get(toKey).add(rKey);
									}
								}
							}

							rowsDone += task.getInput().getFirst().size();
							double progress = 1.0 * rowsDone / numRows;
							exec1.setProgress(progress,
									"Processed " + rowsDone + " of " + numRows + (presortTableByKey
											? " (" + task.getIndex() + " key sets)" : ""));
							try {
								exec1.checkCanceled();
							} catch (CanceledExecutionException e) {
								throw new CancellationException();
							}
						}

					};
			try {
				// We define a comparator which performs comparison on the
				// Smiles String of the Fragmentation key of the first entry in
				// two lists
				Comparator<List<DataRow>> comp = new Comparator<List<DataRow>>() {

					@Override
					public int compare(List<DataRow> leftRows, List<DataRow> rightRows) {
						// We want to know that they have the same Fragment Key.
						// As they are both grouping iterators we check the
						// first row
						// By definition neither list can be empty so we can
						// safely use get(0)
						DataCell leftKeyCell = leftRows.get(0).getCell(keyColIdx);
						DataCell rightKeyCell = rightRows.get(0).getCell(keyColIdx);
						if (leftKeyCell.isMissing() && rightKeyCell.isMissing()) {
							return 0;
						}
						if (leftKeyCell.isMissing()) {
							// missing is high - we used sortMissingToEnd!
							return +1;
						}
						if (rightKeyCell.isMissing()) {
							return -1;
						}
						// Now just compare be SMILES
						return ((SmilesValue) leftKeyCell).getSmilesValue()
								.compareTo(((SmilesValue) rightKeyCell).getSmilesValue());
					}
				};
				// Create a pairwise iterator which 'locks' the two incoming
				// table iterators together by their fragmentation keys
				PairwiseIterable<List<DataRow>> pairs =
						new PairwiseIterable<>(groupTables[1], groupTables[0], comp);
				processor.run(pairs);
			} catch (InterruptedException e) {
				CanceledExecutionException cee = new CanceledExecutionException(e.getMessage());
				cee.initCause(e);
				throw cee;
			} catch (ExecutionException e) {
				Throwable cause = e.getCause();
				if (cause == null) {
					cause = e;
				}
				if (cause instanceof RuntimeException) {
					throw (RuntimeException) cause;
				}
				throw new RuntimeException(cause);
			}
		}

		dc0.close();
		if (m_hiliteMdl.getBooleanValue()) {
			topHiLiteTranslator.setMapper(new DefaultHiLiteMapper(topMapping));
			if (hasTwoInputs) {
				bottomHiLiteTranslator.setMapper(new DefaultHiLiteMapper(bottomMapping));
			}
		}
		return new BufferedDataTable[] { dc0.getTable() };
	}

	/**
	 * This method is called if the incoming table is to be sorted (the node is
	 * a presort-by-keys node) and the keys-are-sorted setting is 'false'. This
	 * handle allows implementations to control the sorting process
	 * 
	 * @param inDataTable
	 *            The incoming table
	 * @param exec
	 *            The node execution context - allocated 25% of execution time
	 * @return The sorted table
	 * @throws CanceledExecutionException
	 *             if the sorting was cancelled by the user
	 */
	protected BufferedDataTable getSortedTable(final BufferedDataTable inDataTable,
			final ExecutionContext exec) throws CanceledExecutionException {
		exec.setMessage("Pre-Sorting table by Fragment key");
		BufferedDataTableSorter sorter = new BufferedDataTableSorter(inDataTable,
				Collections.singleton(m_FragKeyColName.getStringValue()), new boolean[] { true },
				false);
		return sorter.sort(exec);
	}

	/**
	 * Process a row from the incoming table. All rows prior to the current row
	 * are compared according to the similarity comparison rules, and any
	 * transforms generated are returned
	 * 
	 * @param leftRow
	 *            The current ('left') row
	 * @param rightRows
	 *            The full {@link DataTable} or group of rows to from pairs with
	 * @param keyColIdx
	 *            The column index of the key
	 * @param valColIdx
	 *            The column index of the value
	 * @param idColIdx
	 *            The column index of the ID
	 * @param graphFPColIdx
	 *            The column index of the graph distance fingerprint (-1 if
	 *            there isnt one required)
	 * @param exec
	 *            The {@link ExecutionContext} to allow cancelling
	 * @return A {@link SingleTableParallelResult} container with a row for each
	 *         pair from the current leftRow data row to all preceding rows in
	 *         rightRows
	 * @throws CanceledExecutionException
	 */
	protected SingleTableParallelResult processRow(DataRow leftRow, Iterable<DataRow> rightRows,
			int keyColIdx, int valColIdx, int idColIdx, int graphFPColIdx, ExecutionContext exec)
			throws CanceledExecutionException {
		SingleTableParallelResult retVal = new SingleTableParallelResult(exec, m_outSpec);

		DataCell leftKeyCell = leftRow.getCell(keyColIdx);
		DataCell leftValCell = leftRow.getCell(valColIdx);
		DataCell leftIDCell = leftRow.getCell(idColIdx);

		if (leftKeyCell.isMissing() || leftValCell.isMissing() || leftIDCell.isMissing()) {
			// Skip rows with missing keys or values
			return retVal;
		}

		ByteVectorValue leftGraphFP = null;
		if (m_GraphDistCutoffType.isEnabled()
				&& graphDistFPComparisonType != ValueGraphDistanceFingerprintComparisonType.NONE) {
			DataCell leftGraphDistFPCell = leftRow.getCell(graphFPColIdx);
			if (leftGraphDistFPCell.isMissing()) {
				return retVal;
			}
			leftGraphFP = (ByteVectorValue) leftGraphDistFPCell;
		}

		// Get the Left ID and key
		String leftID = ((StringValue) leftIDCell).getStringValue();
		SimpleFragmentKey leftKey =
				new SimpleFragmentKey(((SmilesValue) leftKeyCell).getSmilesValue());

		applyFingerprintToKey(leftKey, leftRow);

		SimpleFragmentValue leftVal =
				new SimpleFragmentValue(((SmilesValue) leftValCell).getSmilesValue(), leftID, true);

		int leftHAC = leftVal.getNumberChangingAtoms();

		// Now loop through all the preceding rows of the table again,
		// looking for matches
		Iterator<DataRow> iter = rightRows.iterator();
		DataRow rightRow;
		// Loop through the right rows (the reference table in 2 table versions)
		while (iter.hasNext()
				&& (!(rightRow = iter.next()).getKey().equals(leftRow.getKey()) || hasTwoInputs)) {
			DataCell rightKeyCell = rightRow.getCell(keyColIdx);
			DataCell rightValCell = rightRow.getCell(valColIdx);
			DataCell rightIDCell = rightRow.getCell(idColIdx);

			if (rightKeyCell.isMissing() || rightValCell.isMissing() || rightIDCell.isMissing()) {
				// Skip rows with missing keys or values
				continue;
			}

			// Get the Right ID, key and Fingerprints
			String rightID = ((StringValue) rightIDCell).getStringValue();

			if (leftID.equals(rightID) && !m_AllowSelfTransforms.getBooleanValue()) {
				continue;
			}

			SimpleFragmentKey rightKey =
					new SimpleFragmentKey(((SmilesValue) rightKeyCell).getSmilesValue());

			if (leftKey.getNumComponents() != rightKey.getNumComponents()) {
				continue;
			}

			if (presortTableByKey && !rightKey.equals(leftKey)) {
				// We can skip fast because if we presort by key it is assumed
				// we must need a matching key
				continue;
			}

			SimpleFragmentValue rightVal = new SimpleFragmentValue(
					((SmilesValue) rightValCell).getSmilesValue(), rightID, true);
			if (leftVal.equals(rightVal)) {
				// Everything is valid, but this is not a 'transform'!
				continue;
			}

			int rightHAC = rightVal.getNumberChangingAtoms();
			if (m_filterByDeltaHACMdl.getBooleanValue()) {
				if (!m_showReverseTransforms.getBooleanValue()) {
					int deltaHAC = rightHAC - leftHAC;
					if (deltaHAC > m_hacDeltaRangeMdl.getMaxRange()
							|| deltaHAC < m_hacDeltaRangeMdl.getMinRange()) {
						continue;
					}
				} else {
					// If reverse transforms are allowed we need to check fully
					// later
					int deltaHAC = Math.abs(rightHAC - leftHAC);
					if (deltaHAC > Math.max(Math.abs(m_hacDeltaRangeMdl.getMaxRange()),
							Math.abs(m_hacDeltaRangeMdl.getMinRange()))) {
						// Too big a change in HAC - cant pass
						continue;
					}
				}
			}
			ByteVectorValue rightGraphFP = null;
			if (m_GraphDistCutoffType.isEnabled()
					&& graphDistFPComparisonType != ValueGraphDistanceFingerprintComparisonType.NONE) {
				DataCell rightGraphDistFPCell = rightRow.getCell(graphFPColIdx);
				if (rightGraphDistFPCell.isMissing()) {
					continue;
				}
				rightGraphFP = (ByteVectorValue) rightGraphDistFPCell;
				Number dOrS = graphDistFPComparisonType.calculateDistSim(leftGraphFP, rightGraphFP);
				if (graphDistFPComparisonType.isDistance()) {
					// Distance
					if (graphDistFPComparisonType.isInteger()) {
						// Int
						if (dOrS.intValue() > m_GraphDistIntCutOff.getIntValue()) {
							continue;
						}
					} else {
						// Double
						if (dOrS.doubleValue() > m_GraphDistDblCutOff.getDoubleValue()) {
							continue;
						}
					}
				} else {
					// Similarity
					if (graphDistFPComparisonType.isInteger()) {
						// Int
						if (dOrS.intValue() < m_GraphDistIntCutOff.getIntValue()) {
							continue;
						}
					} else {
						// Double
						if (dOrS.doubleValue() < m_GraphDistDblCutOff.getDoubleValue()) {
							continue;
						}
					}
				}
			}

			// If we have got here, we have two rows with no missing
			// cells in the right places, different IDs if
			// required by the user settings, and so we can consider
			// whether the two rows are a pair
			applyFingerprintToKey(rightKey, rightRow);
			if (rowsArePair(leftRow, rightRow, leftKey, rightKey, leftVal, rightVal, leftID,
					rightID)) {

				int deltaHAC = rightHAC - leftHAC;
				if ((!m_filterByDeltaHACMdl.getBooleanValue())
						|| !(deltaHAC > m_hacDeltaRangeMdl.getMaxRange()
								|| deltaHAC < m_hacDeltaRangeMdl.getMinRange())) {
					retVal.addRowToTable(createOutRow(leftRow, rightRow, leftVal, rightVal, leftKey,
							rightKey, leftGraphFP, rightGraphFP));
				}

				if (m_showReverseTransforms.getBooleanValue()) {
					deltaHAC *= -1;
					if (!m_filterByDeltaHACMdl.getBooleanValue()
							|| !(deltaHAC > m_hacDeltaRangeMdl.getMaxRange()
									|| deltaHAC < m_hacDeltaRangeMdl.getMinRange())) {
						retVal.addRowToTable(createOutRow(rightRow, leftRow, rightVal, leftVal,
								rightKey, leftKey, rightGraphFP, leftGraphFP));
					}
				}
			}

			exec.checkCanceled();
		}
		if (iter instanceof CloseableRowIterator) {
			((CloseableRowIterator) iter).close();
		}
		return retVal;
	}

	/**
	 * This method should be over-ridden in nodes which require the fragment key
	 * to contain it's fingerprint information. The fingerprint information
	 * should be gathered from the row and applied to the
	 * {@link SimpleFragmentKey} argument using
	 * {@link SimpleFragmentKey#setLeafFingerprints(DataCell[])}
	 * 
	 * @param key
	 *            The {@link SimpleFragmentKey}
	 * @param row
	 *            The datarow containing the fingerprints
	 */
	protected void applyFingerprintToKey(SimpleFragmentKey key, DataRow row) {
		// Nothing in default implementation
	}

	/**
	 * Check with a Left/Right row pair are a matched pair. By the time this
	 * call has been reached, checks will have been made that both rows have
	 * non-missing Key, Values and IDs, and in the case of a Graph Distance
	 * fingerprint check, that will also have been performed. If the node is a
	 * pre-sort-by-Key node, then a check will also have been performed to
	 * ensure the keys are identical. If the leaf fingerprints are required then
	 * {@link #applyFingerprintToKey(SimpleFragmentKey, DataRow)} should be
	 * overridden for efficiency
	 * 
	 * @param leftRow
	 *            The left row
	 * @param rightRow
	 *            The right row
	 * @param leftKey
	 *            The left fragment key
	 * @param rightKey
	 *            The right fragment key
	 * @param leftVal
	 *            The left fragment value
	 * @param rightVal
	 *            The right fragment value
	 * @param leftID
	 *            The left ID
	 * @param rightID
	 *            The right ID
	 * @return <code>true</code> if the rows constitute a pair
	 */
	protected abstract boolean rowsArePair(DataRow leftRow, DataRow rightRow,
			SimpleFragmentKey leftKey, SimpleFragmentKey rightKey, SimpleFragmentValue leftVal,
			SimpleFragmentValue rightVal, String leftID, String rightID);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {

		if (inSpecs.length > 1) {
			if (!inSpecs[0].equalStructure(inSpecs[1])) {
				throw new InvalidSettingsException("The two input tables must have the same spec");
			}
		}

		// Try autoguessing the 3 column names and validating selection
		m_FragKeyColName
				.setStringValue(guessColumnName(inSpecs[0], m_FragKeyColName.getStringValue(),
						SmilesCell.TYPE, "'Key'", inSpecs[0].getNumColumns() - 1));
		m_IDColName.setStringValue(guessColumnName(inSpecs[0], m_IDColName.getStringValue(),
				StringCell.TYPE, "ID", inSpecs[0].getNumColumns() - 1));

		// Start looking from the fragment before
		m_FragValColName
				.setStringValue(guessColumnName(inSpecs[0], m_FragValColName.getStringValue(),
						SmilesCell.TYPE, "'Value'", inSpecs[0].getNumColumns() - 1));

		// Now check the 2 SMILES columns are different
		if (m_FragKeyColName.getStringValue().equals(m_FragValColName.getStringValue())) {
			// If they are not, then try looking from column before the key for
			// a new value
			m_FragValColName.setStringValue(guessColumnName(inSpecs[0],
					m_FragValColName.getStringValue(), SmilesCell.TYPE, "'Value'",
					inSpecs[0].findColumnIndex(m_FragKeyColName.getStringValue()) - 1));
		}

		graphDistFPComparisonType = ValueGraphDistanceFingerprintComparisonType
				.valueOf(m_GraphDistCutoffType.getStringValue());

		if (m_keysAreSorted != null && m_keysAreSorted.getBooleanValue()) {
			setWarningMessage(
					"Ensure incoming table is sorted by the fragment key column otherwise pairs may be missed");
		}

		m_outSpec = createOutSpec(inSpecs[0]);
		numCols = m_outSpec.getNumColumns();

		return new DataTableSpec[] { m_outSpec };
	}

	/**
	 * Checks a column name exists. If not, tries to autoguess, and matches as
	 * substring (if non-'null'), and DataType. Starts at supplied column index
	 * and works back through the table
	 * 
	 * @param spec
	 *            The input data table spec
	 * @param nameFromSettingsModel
	 *            The name supplied from the settings model
	 * @param type
	 *            The type of column
	 * @param substringMatch
	 *            A substring to match - ignored if null
	 * @param startColIdx
	 *            The start column index
	 * @return The column name - either the validated name from the settings
	 *         model, or a guessed name of the correct type
	 * @throws InvalidSettingsException
	 */
	protected String guessColumnName(DataTableSpec spec, String nameFromSettingsModel,
			DataType type, String substringMatch, int startColIdx) throws InvalidSettingsException {
		DataColumnSpec colSpec = spec.getColumnSpec(nameFromSettingsModel);
		String retVal = nameFromSettingsModel;
		if (colSpec == null) {
			if (startColIdx < 0 || startColIdx >= spec.getNumColumns()) {
				// Run out of columns of the appropriate type, or supplied too
				// high an index
				m_logger.error("Not enough columns of the required type in the input table");
				throw new InvalidSettingsException(
						"Not enough columns of the required type in the input table");
			}
			// No column selected, or selected column not found - autoguess!
			for (int i = startColIdx; i >= 0; i--) {
				// Reverse order to select most recently added
				if (spec.getColumnSpec(i).getType().isCompatible(type.getPreferredValueClass())
						&& (substringMatch == null
								|| spec.getColumnSpec(i).getName().indexOf(substringMatch) >= 0)) {
					retVal = (spec.getColumnSpec(i).getName());
					m_logger.warn("No column selected. " + retVal + " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					m_logger.error(
							"No molecule column of the accepted" + " input formats was found.");
					throw new InvalidSettingsException(
							"No molecule column of the accepted" + " input formats was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(type.getPreferredValueClass())) {
				// The column is not compatible with one of the accepted types
				m_logger.error(
						"The column " + retVal + " is not one of the accepted" + " input formats");
				throw new InvalidSettingsException(
						"The column " + retVal + " is not one of the accepted" + " input formats");
			}
		}
		return retVal;
	}

	/** Create output table spec, accounting for settings models */
	private final DataTableSpec createOutSpec(DataTableSpec inTableSpec)
			throws InvalidSettingsException {

		final List<DataColumnSpec> specs = new ArrayList<>();

		specs.add(createColSpec("Transformation", SmilesCellFactory.TYPE, null));
		specs.add(createColSpec("ID (Left)", SmilesCellFactory.TYPE, null));
		specs.add(createColSpec("ID (Right)", SmilesCellFactory.TYPE, null));
		specs.add(createColSpec("Left Fragment", SmilesCellFactory.TYPE, null));
		specs.add(createColSpec("Right Fragment", SmilesCellFactory.TYPE, null));
		if (m_includeUnchangingPortions.getBooleanValue()) {
			if (showBothKeys) {
				specs.add(createColSpec("Key (Left)", SmilesCellFactory.TYPE, null));
				specs.add(createColSpec("Key (Right)", SmilesCellFactory.TYPE, null));
			} else {
				specs.add(createColSpec("Key", SmilesCellFactory.TYPE, null));
			}
		}
		if (m_includeHACount.getBooleanValue()) {
			specs.add(createColSpec("Changing Heavy Atoms (Left)", IntCell.TYPE, null));
			specs.add(createColSpec("Changing Heavy Atoms (Right)", IntCell.TYPE, null));
		}
		if (m_includeHARatio.getBooleanValue()) {
			specs.add(createColSpec("Ratio of Changing / Unchanging Heavy Atoms (Left)",
					DoubleCell.TYPE, null));
			specs.add(createColSpec("Ratio of Changing / Unchanging Heavy Atoms (Right)",
					DoubleCell.TYPE, null));
		}
		if (m_includeReactionSMARTS.getBooleanValue()) {
			specs.add(
					createColSpec("Transformation Reaction SMARTS", SmartsCellFactory.TYPE, null));
		}

		if (m_filterByDeltaHACMdl.getBooleanValue() && m_showHACDeltaMdl.getBooleanValue()) {
			specs.add(createColSpec("Delta HAC", IntCell.TYPE, null));
		}
		if (m_GraphDistCutoffType.isEnabled()
				&& graphDistFPComparisonType != ValueGraphDistanceFingerprintComparisonType.NONE
				&& m_inclGraphDistDists.getBooleanValue()) {
			specs.add(createColSpec(
					graphDistFPComparisonType.getText() + " ("
							+ m_graphDistFpColName.getStringValue() + ")",
					graphDistFPComparisonType.getDataType(), null));
		}

		List<String> rightPassThroughNames = new ArrayList<>(
				Arrays.asList(m_RightPassThroughColumns.applyTo(inTableSpec).getIncludes()));
		rightPassThroughNames.remove(m_FragKeyColName.getStringValue());
		rightPassThroughNames.remove(m_FragValColName.getStringValue());
		rightPassThroughNames.remove(m_IDColName.getStringValue());
		for (String name : m_LeftPassThroughColumns.applyTo(inTableSpec).getIncludes()) {
			// Dont pass through ID and Frag Key/Value cols
			if (!name.equals(m_FragKeyColName.getStringValue())
					&& !name.equals(m_IDColName.getStringValue())
					&& !name.equals(m_FragValColName.getStringValue())) {
				final DataColumnSpec inSpec = inTableSpec.getColumnSpec(name);
				specs.add(createColSpec(name + " (L)", inSpec.getType(), inSpec.getProperties()));
				if (rightPassThroughNames.contains(name)) {
					// Keep L/R pairs together
					specs.add(
							createColSpec(name + " (R)", inSpec.getType(), inSpec.getProperties()));
					rightPassThroughNames.remove(name);
				}
			}
		}

		rightPassThroughNames.stream().map(x -> inTableSpec.getColumnSpec(x)).forEach(x -> specs
				.add(createColSpec(x.getName() + " (R)", x.getType(), x.getProperties())));

		Arrays.stream(m_LeftMinusRightColumns.applyTo(inTableSpec).getIncludes())
				.map(x -> inTableSpec.getColumnSpec(x)).forEachOrdered(
						x -> specs.add(createColSpec(x.getName() + " (L-R)", x.getType(), null)));
		Arrays.stream(m_RightMinusLeftColumns.applyTo(inTableSpec).getIncludes())
				.forEachOrdered(x -> specs.add(
						createColSpec(x + " (R-L)", inTableSpec.getColumnSpec(x).getType(), null)));
		Arrays.stream(m_LeftDivbyRightColumns.applyTo(inTableSpec).getIncludes())
				.forEachOrdered(x -> specs.add(
						createColSpec(x + " (L/R)", inTableSpec.getColumnSpec(x).getType(), null)));
		Arrays.stream(m_RightDivbyLeftColumns.applyTo(inTableSpec).getIncludes())
				.forEachOrdered(x -> specs.add(
						createColSpec(x + " (R/L)", inTableSpec.getColumnSpec(x).getType(), null)));

		populatePassThroughIndices(inTableSpec);

		addImplentationColSpecs(inTableSpec, specs);
		uniquifyNames(specs);
		return new DataTableSpec(specs.toArray(new DataColumnSpec[specs.size()]));
	}

	private final void populatePassThroughIndices(DataTableSpec inTableSpec) {
		leftKeepColIdxs = Arrays.stream(m_LeftPassThroughColumns.applyTo(inTableSpec).getIncludes())
				.filter(name -> !name.equals(m_FragKeyColName.getStringValue())
						&& !name.equals(m_IDColName.getStringValue())
						&& !name.equals(m_FragValColName.getStringValue()))
				.mapToInt(x -> inTableSpec.findColumnIndex(x)).sorted().toArray();
		rightWithLeftKeepColIdxs = Arrays
				.stream(m_RightPassThroughColumns.applyTo(inTableSpec).getIncludes())
				.filter(name -> !name.equals(m_FragKeyColName.getStringValue())
						&& !name.equals(m_IDColName.getStringValue())
						&& !name.equals(m_FragValColName.getStringValue()))
				.mapToInt(x -> inTableSpec.findColumnIndex(x))
				.filter(x -> Arrays.binarySearch(leftKeepColIdxs, x) >= 0).sorted().toArray();
		rightOnlyKeepColIdxs = Arrays
				.stream(m_RightPassThroughColumns.applyTo(inTableSpec).getIncludes())
				.filter(name -> !name.equals(m_FragKeyColName.getStringValue())
						&& !name.equals(m_IDColName.getStringValue())
						&& !name.equals(m_FragValColName.getStringValue()))
				.mapToInt(x -> inTableSpec.findColumnIndex(x))
				.filter(x -> Arrays.binarySearch(leftKeepColIdxs, x) < 0).sorted().toArray();
		lMinRColIdxs = Arrays.stream(m_LeftMinusRightColumns.applyTo(inTableSpec).getIncludes())
				.mapToInt(x -> inTableSpec.findColumnIndex(x)).sorted().toArray();
		rMinLColIdxs = Arrays.stream(m_RightMinusLeftColumns.applyTo(inTableSpec).getIncludes())
				.mapToInt(x -> inTableSpec.findColumnIndex(x)).sorted().toArray();
		lDivRColIdxs = Arrays.stream(m_LeftDivbyRightColumns.applyTo(inTableSpec).getIncludes())
				.mapToInt(x -> inTableSpec.findColumnIndex(x)).sorted().toArray();
		rDivLColIdxs = Arrays.stream(m_RightDivbyLeftColumns.applyTo(inTableSpec).getIncludes())
				.mapToInt(x -> inTableSpec.findColumnIndex(x)).sorted().toArray();
	}

	private final void uniquifyNames(List<DataColumnSpec> specs) {
		List<String> names = new ArrayList<>();
		List<DataColumnSpec> retVal = new ArrayList<>();
		boolean changedAny = false;
		for (DataColumnSpec spec : specs) {
			if (names.contains(spec.getName())) {
				String trimedName = spec.getName().trim();
				int uniquifier = 1;
				String result = trimedName;
				while (names.contains(result)) {
					result = trimedName + " (#" + uniquifier + ")";
					uniquifier++;
				}
				names.add(result);
				retVal.add(createColSpec(result, spec.getType(), spec.getProperties()));
				m_logger.info("Duplicate name '" + spec.getName() + "' encountered; Uniquified to '"
						+ result + "'");
				changedAny = true;
			} else {
				names.add(spec.getName());
				retVal.add(spec);
			}
		}
		// specs is final to use in streams so we clear it and replace it if any
		// changes were made
		if (changedAny) {
			specs.clear();
			specs.addAll(retVal);
		}
	}

	/**
	 * This method should add any additional columns to the <code>specs</code>
	 * argument list, which will be pre-populated with the framework columns
	 * (e.g. key/value, ID, transform, pass-through and numeric difference/ratio
	 * columns). The method does not need to ensure uniqueness, as any duplicate
	 * names will be fixed after the call to this method
	 * 
	 * @param inTableSpec
	 *            The incoming table spec
	 * @param specs
	 *            The list of output column specs to which new columns should be
	 *            added
	 * @throws InvalidSettingsException
	 *             If there is a problem applying implementation-specific
	 *             settings
	 */
	protected abstract void addImplentationColSpecs(DataTableSpec inTableSpec,
			List<DataColumnSpec> specs) throws InvalidSettingsException;

	/**
	 * Creates a column spec from the name and type
	 * 
	 * @param colName
	 *            The column name
	 * @param colType
	 *            The column {@link DataType}
	 * @return A {@link DataColumnSpec}
	 */
	protected static final DataColumnSpec createColSpec(String colName, DataType colType,
			DataColumnProperties colProps) {

		final DataColumnSpecCreator dataColumnSpecCreator =
				new DataColumnSpecCreator(colName, colType);
		if (colProps != null) {
			dataColumnSpecCreator.setProperties(colProps);
		}
		return dataColumnSpecCreator.createSpec();
	}

	/**
	 * Create output table row, accounting for settings models
	 * 
	 * @param leftRowKey
	 *            The left row key
	 * @param rightRowKey
	 *            The right row key
	 * @param leftVal
	 *            The left 'value'
	 * @param rightVal
	 *            The right 'value'
	 * @param leftKey
	 *            The left 'key'
	 * @param rightKey
	 *            The right 'key'
	 * @param rightGraphFP
	 * @param leftGraphFP
	 * @param leftFingerprintCells
	 *            The left fingerprint cells
	 * @param rightFingerprintCells
	 *            The right fingerprint cells
	 * @param numColumns
	 *            The number of columns in the output
	 * @param stripHsAtEnd
	 *            Should H's be removed from the outputs?
	 * @param includeUnchangingPortions
	 *            Should the 'unchanging' (key) portion be included?
	 * @param includeHACount
	 *            Should the Heavy Atom Count be included?
	 * @param includeHARatio
	 *            Should the Heavy Atom Ratio be included?
	 * @param outputSimilarities
	 *            Should the smilarities be included?
	 * @param compType
	 *            The comparison type
	 * @param simType
	 *            The similarity type
	 * @param alpha
	 *            The Tversky alpha parameter
	 * @param beta
	 *            The Tversky beta parameter
	 * @param includeReactionSMARTS
	 *            Should the reaction SMARTS be included?
	 * @param numCutsColIdx
	 *            The index of the number of cuts column
	 * @return The data row for the transform
	 */
	protected DataRow createOutRow(DataRow leftRow, DataRow rightRow, SimpleFragmentValue leftVal,
			SimpleFragmentValue rightVal, SimpleFragmentKey leftKey, SimpleFragmentKey rightKey,
			ByteVectorValue leftGraphFP, ByteVectorValue rightGraphFP) {

		List<DataCell> cells = new ArrayList<>();
		cells.add(SmilesCellFactory
				.createAdapterCell(leftVal.getSMILES() + ">>" + rightVal.getSMILES()));
		cells.add(leftVal.getIDCell());
		cells.add(rightVal.getIDCell());
		cells.add(leftVal.getSMILESCell());
		cells.add(rightVal.getSMILESCell());
		if (m_includeUnchangingPortions.getBooleanValue()) {
			cells.add(leftKey.getKeyAsDataCell());
			if (showBothKeys) {
				cells.add(rightKey.getKeyAsDataCell());
			}
		}
		if (m_includeHACount.getBooleanValue()) {
			cells.add(leftVal.getNumberChangingAtomsCell());
			cells.add(rightVal.getNumberChangingAtomsCell());
		}
		if (m_includeHARatio.getBooleanValue()) {
			cells.add(leftKey.getConstantToVaryingAtomRatioCell(leftVal));
			cells.add(rightKey.getConstantToVaryingAtomRatioCell(rightVal));
		}
		if (m_includeReactionSMARTS.getBooleanValue()) {
			cells.add(SmartsCellFactory
					.createAdapterCell(TransformUtils.convertSmirksToReactionSmarts(
							leftVal.getSMILES() + ">>" + rightVal.getSMILES(),
							m_requireAcyclicSMARTS.getBooleanValue())));
		}

		if (m_filterByDeltaHACMdl.getBooleanValue() && m_showHACDeltaMdl.getBooleanValue()) {
			cells.add(new IntCell(
					rightVal.getNumberChangingAtoms() - leftVal.getNumberChangingAtoms()));
		}
		if (m_GraphDistCutoffType.isEnabled()
				&& graphDistFPComparisonType != ValueGraphDistanceFingerprintComparisonType.NONE
				&& m_inclGraphDistDists.getBooleanValue()) {
			cells.add(
					graphDistFPComparisonType.getDistanceSimilarityCell(leftGraphFP, rightGraphFP));
		}

		// Handle pass-through and diff/div cols

		for (int lIdx : leftKeepColIdxs) {
			cells.add(leftRow.getCell(lIdx));
			if (Arrays.binarySearch(rightWithLeftKeepColIdxs, lIdx) >= 0) {
				// Keep L/R pairs together
				cells.add(rightRow.getCell(lIdx));
			}
		}

		Arrays.stream(rightOnlyKeepColIdxs)
				.forEachOrdered(rIdx -> cells.add(rightRow.getCell(rIdx)));

		Arrays.stream(lMinRColIdxs).forEachOrdered(
				idx -> cells.add(substractCells(leftRow.getCell(idx), rightRow.getCell(idx))));
		Arrays.stream(rMinLColIdxs).forEachOrdered(
				idx -> cells.add(substractCells(rightRow.getCell(idx), leftRow.getCell(idx))));
		Arrays.stream(rDivLColIdxs).forEachOrdered(
				idx -> cells.add(divideCells(leftRow.getCell(idx), rightRow.getCell(idx))));
		Arrays.stream(lDivRColIdxs).forEachOrdered(
				idx -> cells.add(divideCells(rightRow.getCell(idx), leftRow.getCell(idx))));

		addImplementationColCells(cells, leftRow, rightRow, leftVal, rightVal, leftKey, rightKey);
		RowKey newRowKey =
				new RowKey(leftRow.getKey().getString() + "_" + rightRow.getKey().getString());
		return new KnowsParentRowIDsDataRow(new DefaultRow(newRowKey, cells), leftRow.getKey(),
				rightRow.getKey());
	}

	/**
	 * This method should add any additional cells to the <code>cells</code>
	 * argument list, which will be pre-populated with the framework cells (e.g.
	 * key/value, ID, transform, pass-through and numeric difference/ratio
	 * columns).
	 * 
	 * @param cells
	 *            The list of cells to add additional cells to
	 * @param leftRow
	 *            The left datarow of the Matched-Pair
	 * @param rightRow
	 *            The right datarow of the matched pair
	 * @param leftVal
	 *            The left fragment value
	 * @param rightVal
	 *            The right fragment value
	 * @param leftKey
	 *            The left fragment key
	 * @param rightKey
	 *            The right fragment key
	 */
	protected abstract void addImplementationColCells(List<DataCell> cells, DataRow leftRow,
			DataRow rightRow, SimpleFragmentValue leftVal, SimpleFragmentValue rightVal,
			SimpleFragmentKey leftKey, SimpleFragmentKey rightKey);

	private final DataCell divideCells(DataCell dividendCell, DataCell divisorCell) {
		if (dividendCell.isMissing() || divisorCell.isMissing()) {
			return DataType.getMissingCell();
		}
		// Only available for doubles!
		return new DoubleCell(((DoubleValue) dividendCell).getDoubleValue()
				/ ((DoubleValue) divisorCell).getDoubleValue());

	}

	private final DataCell substractCells(DataCell minuendCell, DataCell subtrahendCell) {
		if (minuendCell.isMissing() || subtrahendCell.isMissing()) {
			return DataType.getMissingCell();
		}
		if (minuendCell instanceof LongCell) {
			return new LongCell(((LongValue) minuendCell).getLongValue()
					- ((LongValue) subtrahendCell).getLongValue());
		} else if (minuendCell instanceof IntCell) {
			return new IntCell(((IntValue) minuendCell).getIntValue()
					- ((IntValue) subtrahendCell).getIntValue());
		} else if (minuendCell instanceof DoubleCell) {
			return new DoubleCell(((DoubleValue) minuendCell).getDoubleValue()
					- ((DoubleValue) subtrahendCell).getDoubleValue());
		} else if (minuendCell instanceof ComplexNumberCell) {
			return new ComplexNumberCell(
					((ComplexNumberValue) minuendCell).getRealValue()
							- ((ComplexNumberValue) subtrahendCell).getRealValue(),
					((ComplexNumberValue) minuendCell).getImaginaryValue()
							- ((ComplexNumberValue) subtrahendCell).getImaginaryValue());
		}
		m_logger.error("Something went wrong - unimplented cell type in subtraction");
		return DataType.getMissingCell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File internDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		File settingsFile = new File(internDir, SETTINGS_FILE_NAME);
		FileInputStream in = new FileInputStream(settingsFile);
		NodeSettingsRO settings = NodeSettings.loadFromXML(in);
		try {
			if (m_hiliteMdl.getBooleanValue()) {
				NodeSettingsRO mapSet = settings.getNodeSettings(HILITE_KEY + "_top");
				topHiLiteTranslator.setMapper(DefaultHiLiteMapper.load(mapSet));
				if (hasTwoInputs) {
					mapSet = settings.getNodeSettings(HILITE_KEY + "_bottom");
					bottomHiLiteTranslator.setMapper(DefaultHiLiteMapper.load(mapSet));
				}
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File internDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		NodeSettings settings = new NodeSettings("FragmentToPair");
		if (m_hiliteMdl.getBooleanValue()) {
			NodeSettingsWO mapSet = settings.addNodeSettings(HILITE_KEY + "_top");
			((DefaultHiLiteMapper) topHiLiteTranslator.getMapper()).save(mapSet);
			if (hasTwoInputs) {
				mapSet = settings.addNodeSettings(HILITE_KEY + "_bottom");
				((DefaultHiLiteMapper) bottomHiLiteTranslator.getMapper()).save(mapSet);
			}
		}
		File f = new File(internDir, SETTINGS_FILE_NAME);
		FileOutputStream fos = new FileOutputStream(f);
		settings.saveToXML(fos);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.
	 * NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_FragKeyColName.saveSettingsTo(settings);
		if (m_keysAreSorted != null) {
			m_keysAreSorted.saveSettingsTo(settings);
		}
		m_hiliteMdl.saveSettingsTo(settings);
		m_IDColName.saveSettingsTo(settings);
		m_FragValColName.saveSettingsTo(settings);
		m_filterByDeltaHACMdl.saveSettingsTo(settings);
		m_hacDeltaRangeMdl.saveSettingsTo(settings);
		m_GraphDistCutoffType.saveSettingsTo(settings);
		m_GraphDistDblCutOff.saveSettingsTo(settings);
		m_GraphDistIntCutOff.saveSettingsTo(settings);
		m_graphDistFpColName.saveSettingsTo(settings);
		m_inclGraphDistDists.saveSettingsTo(settings);
		m_includeUnchangingPortions.saveSettingsTo(settings);
		m_includeHACount.saveSettingsTo(settings);
		m_includeHARatio.saveSettingsTo(settings);
		m_showReverseTransforms.saveSettingsTo(settings);
		m_includeReactionSMARTS.saveSettingsTo(settings);
		m_AllowSelfTransforms.saveSettingsTo(settings);
		m_requireAcyclicSMARTS.saveSettingsTo(settings);
		m_LeftMinusRightColumns.saveSettingsTo(settings);
		m_RightMinusLeftColumns.saveSettingsTo(settings);
		m_LeftDivbyRightColumns.saveSettingsTo(settings);
		m_RightDivbyLeftColumns.saveSettingsTo(settings);
		m_LeftPassThroughColumns.saveSettingsTo(settings);
		m_RightPassThroughColumns.saveSettingsTo(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.
	 * NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {

		m_FragKeyColName.validateSettings(settings);
		if (m_keysAreSorted != null) {
			m_keysAreSorted.validateSettings(settings);
		}
		m_hiliteMdl.validateSettings(settings);
		m_IDColName.validateSettings(settings);
		m_FragValColName.validateSettings(settings);
		m_filterByDeltaHACMdl.validateSettings(settings);
		m_hacDeltaRangeMdl.validateSettings(settings);
		m_GraphDistCutoffType.validateSettings(settings);
		m_GraphDistDblCutOff.validateSettings(settings);
		m_GraphDistIntCutOff.validateSettings(settings);
		m_graphDistFpColName.validateSettings(settings);
		m_inclGraphDistDists.validateSettings(settings);
		m_includeUnchangingPortions.validateSettings(settings);
		m_includeHACount.validateSettings(settings);
		m_includeHARatio.validateSettings(settings);
		m_showReverseTransforms.validateSettings(settings);
		m_includeReactionSMARTS.validateSettings(settings);
		m_AllowSelfTransforms.validateSettings(settings);
		m_requireAcyclicSMARTS.validateSettings(settings);
		m_LeftMinusRightColumns.validateSettings(settings);
		m_RightMinusLeftColumns.validateSettings(settings);
		m_LeftDivbyRightColumns.validateSettings(settings);
		m_RightDivbyLeftColumns.validateSettings(settings);
		m_LeftPassThroughColumns.validateSettings(settings);
		m_RightPassThroughColumns.validateSettings(settings);
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
		m_FragKeyColName.loadSettingsFrom(settings);
		if (m_keysAreSorted != null) {
			m_keysAreSorted.loadSettingsFrom(settings);
		}
		m_hiliteMdl.loadSettingsFrom(settings);
		m_IDColName.loadSettingsFrom(settings);
		m_FragValColName.loadSettingsFrom(settings);
		m_filterByDeltaHACMdl.loadSettingsFrom(settings);
		m_hacDeltaRangeMdl.loadSettingsFrom(settings);
		m_GraphDistCutoffType.loadSettingsFrom(settings);
		m_GraphDistDblCutOff.loadSettingsFrom(settings);
		m_GraphDistIntCutOff.loadSettingsFrom(settings);
		m_graphDistFpColName.loadSettingsFrom(settings);
		m_inclGraphDistDists.loadSettingsFrom(settings);
		m_includeUnchangingPortions.loadSettingsFrom(settings);
		m_includeHACount.loadSettingsFrom(settings);
		m_includeHARatio.loadSettingsFrom(settings);
		m_showReverseTransforms.loadSettingsFrom(settings);
		m_includeReactionSMARTS.loadSettingsFrom(settings);
		m_AllowSelfTransforms.loadSettingsFrom(settings);
		m_requireAcyclicSMARTS.loadSettingsFrom(settings);
		m_LeftMinusRightColumns.loadSettingsFrom(settings);
		m_RightMinusLeftColumns.loadSettingsFrom(settings);
		m_LeftDivbyRightColumns.loadSettingsFrom(settings);
		m_RightDivbyLeftColumns.loadSettingsFrom(settings);
		m_LeftPassThroughColumns.loadSettingsFrom(settings);
		m_RightPassThroughColumns.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		topHiLiteTranslator.setMapper(null);
		if (hasTwoInputs) {
			bottomHiLiteTranslator.setMapper(null);
		}

	}

	/**
	 * 
	 */
	private void updateGraphDistModels() {
		graphDistFPComparisonType = ValueGraphDistanceFingerprintComparisonType
				.valueOf(m_GraphDistCutoffType.getStringValue());
		if (graphDistFPComparisonType == ValueGraphDistanceFingerprintComparisonType.NONE) {
			m_GraphDistDblCutOff.setEnabled(false);
			m_GraphDistIntCutOff.setEnabled(false);
			m_graphDistFpColName.setEnabled(false);
			m_inclGraphDistDists.setEnabled(false);
		} else {
			m_graphDistFpColName.setEnabled(true);
			m_inclGraphDistDists.setEnabled(true);
			if (graphDistFPComparisonType.getDataType() == IntCell.TYPE) {

				m_GraphDistDblCutOff.setEnabled(false);
				m_GraphDistIntCutOff.setEnabled(true);
				m_GraphDistIntCutOff.setBounds(graphDistFPComparisonType.getMinimum().intValue(),
						graphDistFPComparisonType.getMaximum().intValue());
			} else {
				m_GraphDistIntCutOff.setEnabled(false);
				m_GraphDistDblCutOff.setEnabled(true);
				m_GraphDistDblCutOff.setBounds(graphDistFPComparisonType.getMinimum().doubleValue(),
						graphDistFPComparisonType.getMaximum().doubleValue());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#setInHiLiteHandler(int,
	 * org.knime.core.node.property.hilite.HiLiteHandler)
	 */
	@Override
	protected void setInHiLiteHandler(int inIndex, HiLiteHandler hiLiteHdl) {
		switch (inIndex) {
		case 0:
			topHiLiteTranslator.removeAllToHiliteHandlers();
			topHiLiteTranslator.addToHiLiteHandler(hiLiteHdl);
			break;

		case 1:
			if (hasTwoInputs) {
				bottomHiLiteTranslator.removeAllToHiliteHandlers();
				bottomHiLiteTranslator.addToHiLiteHandler(hiLiteHdl);
				break;
			}

		default:
			super.setInHiLiteHandler(inIndex, hiLiteHdl);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutHiLiteHandler(int)
	 */
	@Override
	protected HiLiteHandler getOutHiLiteHandler(int outIndex) {
		switch (outIndex) {
		case 0:
			return topHiLiteTranslator.getFromHiLiteHandler();

		case 1:
			if (hasTwoInputs) {
				return bottomHiLiteTranslator.getFromHiLiteHandler();
			}

		default:
			return super.getOutHiLiteHandler(outIndex);
		}
	}

}
