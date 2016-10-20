/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.frag2pair2;

import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createAllowSelfTransformsModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createCheckSortedModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createFragKeyModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createFragValueModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createIDModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createIgnoreIDsModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createOutputChangingHACountsModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createOutputHARatiosModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createOutputKeyModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createShowReverseTransformsModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createShowSmartsTransformsModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createSortedKeysModel;
import static com.vernalis.knime.mmp.nodes.frag2pair2.Frag2Pair2NodeDialog.createStripHModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.knime.chem.types.SmartsCell;
import org.knime.chem.types.SmartsCellFactory;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.mmp.FragmentKey2;
import com.vernalis.knime.mmp.FragmentValue2;
import com.vernalis.knime.mmp.RDKitFragmentationUtils;

/**
 * {@link NodeModel} for the Fragment to MMP node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class Frag2Pair2NodeModel extends NodeModel {

	private final SettingsModelString m_FragKeyColName = createFragKeyModel();
	private final SettingsModelBoolean m_SortedFragKey = createSortedKeysModel();
	private final SettingsModelBoolean m_CheckSortedKeys = createCheckSortedModel();
	private final SettingsModelString m_IDColName = createIDModel();
	private final SettingsModelString m_FragValColName = createFragValueModel();
	private final SettingsModelBoolean m_includeUnchangingPortion = createOutputKeyModel();
	private final SettingsModelBoolean m_includeHACount = createOutputChangingHACountsModel();
	private final SettingsModelBoolean m_includeHARatio = createOutputHARatiosModel();
	private final SettingsModelBoolean m_stripHsAtEnd = createStripHModel();
	private final SettingsModelBoolean m_showReverseTransforms = createShowReverseTransformsModel();
	private final SettingsModelBoolean m_includeReactionSMARTS = createShowSmartsTransformsModel();
	private final SettingsModelBoolean m_IgnoreIDs = createIgnoreIDsModel();
	private final SettingsModelBoolean m_AllowSelfTransforms = createAllowSelfTransformsModel();

	/** The NodeLogger Instance */
	static final NodeLogger m_logger = NodeLogger.getLogger(Frag2Pair2NodeModel.class);

	/** The output table spec */
	private DataTableSpec m_spec;
	private int numCols;

	/** The column indices of any fingerprint columns */
	private ArrayList<Integer> m_fpColIdx;

	/**
	 * Constructor for the node model class
	 */
	public Frag2Pair2NodeModel() {
		super(1, 1);
		m_CheckSortedKeys.setEnabled(m_SortedFragKey.getBooleanValue());
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

		// Do some generic stuff
		// Buffered datacontainer
		BufferedDataContainer dc0 = exec.createDataContainer(m_spec);
		final long numRows = inData[0].size();

		// Empty Tables
		if (numRows == 0) {
			dc0.close();
			return new BufferedDataTable[] { dc0.getTable() };
		}

		// Col IDxs
		DataTableSpec inSpec = inData[0].getDataTableSpec();
		final int keyColIdx = inSpec.findColumnIndex(m_FragKeyColName.getStringValue());
		final int valColIdx = inSpec.findColumnIndex(m_FragValColName.getStringValue());
		final int idColIdx = inSpec.findColumnIndex(m_IDColName.getStringValue());

		if (m_SortedFragKey.getBooleanValue()) {
			// Load until key changes
			double progress = 0.0;
			long inRow = 0;
			long outRow = 0;
			Set<FragmentKey2> keys = new HashSet<>();
			FragmentKey2 currentKey = null;
			TreeSet<FragmentValue2> vals = new TreeSet<>();
			DataCell[] fpCells = null;
			for (final DataRow row : inData[0]) {
				DataCell keyCell = row.getCell(keyColIdx);
				DataCell valCell = row.getCell(valColIdx);
				DataCell idCell = row.getCell(idColIdx);
				inRow++;
				if (keyCell.isMissing() || valCell.isMissing() || idCell.isMissing()) {
					// Skip rows with missing keys or values
					continue;
				}
				String ID = ((StringValue) idCell).getStringValue();
				FragmentKey2 newKey = new FragmentKey2(((SmilesValue) keyCell).getSmilesValue());

				if (!newKey.equals(currentKey)) {
					// We have a new key
					if (m_CheckSortedKeys.getBooleanValue()) {
						// If applicable, check for sortedness
						if (!keys.add(newKey)) {
							// The new key, whilst different from the previous
							// key is not new to the table, and so execution
							// fails
							throw new InvalidSettingsException(
									"The input table is not correctly sorted by key");
						}
					}

					// Take a copy of any fingerprint columns - they will only
					// change with a new key
					fpCells = new DataCell[m_fpColIdx.size()];
					int i = 0;
					for (int idx : m_fpColIdx) {
						fpCells[i++] = row.getCell(idx);
					}

					// Now add the output for the currentKey to the output table
					exec.setMessage("Adding matched molecular pairs to output");
					ArrayList<DataCell[]> newRows;

					// Generate the new rows
					if (m_includeUnchangingPortion.getBooleanValue()
							|| m_includeHARatio.getBooleanValue()) {
						// Need the overloaded method with key and value
						newRows = RDKitFragmentationUtils.getTransforms(vals, currentKey, numCols,
								m_stripHsAtEnd.getBooleanValue(),
								m_includeUnchangingPortion.getBooleanValue(),
								m_includeHACount.getBooleanValue(),
								m_includeHARatio.getBooleanValue(),
								m_showReverseTransforms.getBooleanValue(),
								m_AllowSelfTransforms.getBooleanValue());
					} else {
						newRows = RDKitFragmentationUtils.getTransforms(vals, numCols,
								m_stripHsAtEnd.isEnabled() && m_stripHsAtEnd.getBooleanValue(),
								m_includeHACount.getBooleanValue(),
								m_showReverseTransforms.getBooleanValue(),
								m_AllowSelfTransforms.getBooleanValue());
					}
					for (DataCell[] cells : newRows) {
						// Deal with need for rSMARTS
						if (m_includeReactionSMARTS.getBooleanValue()) {
							cells = addReactionSmartsCell(cells);
						}
						cells = applyFingerprintCells(cells, fpCells);
						RowKey rowKey = new RowKey("Row_" + outRow++);
						dc0.addRowToTable(new DefaultRow(rowKey, cells));
					}
					exec.setMessage("Reading key-values");
					// And now reset the keys and values
					currentKey = newKey;
					vals.clear();
				}

				// Now we need to add the current value to the list of values
				vals.add(new FragmentValue2(((SmilesValue) valCell).getSmilesValue(), ID,
						m_IgnoreIDs.getBooleanValue()));

				exec.checkCanceled();
				progress = (double) inRow / numRows;
				exec.setProgress(progress, "Processed " + inRow + " of " + numRows + ". Created "
						+ outRow + " Matched pairs");
			}
			// After the final row we need to output the last round of MMPs
			exec.setMessage("Adding matched molecular pairs to output");
			ArrayList<DataCell[]> newRows;

			// Generate the new rows
			if (m_includeUnchangingPortion.getBooleanValue()
					|| m_includeHARatio.getBooleanValue()) {
				// Need the overloaded method with key and value
				newRows = RDKitFragmentationUtils.getTransforms(vals, currentKey, numCols,
						m_stripHsAtEnd.getBooleanValue(),
						m_includeUnchangingPortion.getBooleanValue(),
						m_includeHACount.getBooleanValue(), m_includeHARatio.getBooleanValue(),
						m_showReverseTransforms.getBooleanValue(),
						m_AllowSelfTransforms.getBooleanValue());
			} else {
				newRows = RDKitFragmentationUtils.getTransforms(vals, numCols,
						m_stripHsAtEnd.isEnabled() && m_stripHsAtEnd.getBooleanValue(),
						m_includeHACount.getBooleanValue(),
						m_showReverseTransforms.getBooleanValue(),
						m_AllowSelfTransforms.getBooleanValue());
			}
			for (DataCell[] cells : newRows) {
				// Deal with need for rSMARTS
				if (m_includeReactionSMARTS.getBooleanValue()) {
					cells = addReactionSmartsCell(cells);
				}
				cells = applyFingerprintCells(cells, fpCells);
				RowKey rowKey = new RowKey("Row_" + outRow++);
				dc0.addRowToTable(new DefaultRow(rowKey, cells));
			}
		} else {
			// We have to load the entire table into memory and then process
			// Container for the fragmentations
			Map<FragmentKey2, TreeSet<FragmentValue2>> frags = new HashMap<FragmentKey2, TreeSet<FragmentValue2>>();
			// And for the fingerprints
			Map<FragmentKey2, DataCell[]> keyFps = new HashMap<>();

			m_logger.info("Compiling fragment dictionary");
			exec.setMessage("Compiling fragment dictionary");
			// Allocate 75% of time for this
			ExecutionMonitor exec_0 = exec.createSubProgress(0.75);
			double progress = 0.0;
			long rowCnt = 0;

			// Now parse the input molecules...
			for (final DataRow row : inData[0]) {
				DataCell keyCell = row.getCell(keyColIdx);
				DataCell valCell = row.getCell(valColIdx);
				DataCell idCell = row.getCell(idColIdx);

				if (keyCell.isMissing() || valCell.isMissing() || idCell.isMissing()) {
					// Skip rows with missing keys or values
					continue;
				}

				String ID = ((StringValue) idCell).getStringValue();
				FragmentKey2 key = new FragmentKey2(((SmilesValue) keyCell).getSmilesValue());
				if (!frags.containsKey(key)) {
					frags.put(key, new TreeSet<FragmentValue2>());
				}
				FragmentValue2 value = new FragmentValue2(((SmilesValue) valCell).getSmilesValue(),
						ID, m_IgnoreIDs.getBooleanValue());
				frags.get(key).add(value);
				if (!keyFps.containsKey(key)) {
					// Take a copy of any fingerprint columns - they will only
					// change with a new key
					DataCell[] fpCells = new DataCell[m_fpColIdx.size()];
					int i = 0;
					for (int idx : m_fpColIdx) {
						fpCells[i++] = row.getCell(idx);
					}
					keyFps.put(key, fpCells);
				}
				exec_0.checkCanceled();
				progress = (double) rowCnt++ / (double) numRows;
				exec_0.setProgress(progress,
						"Processed " + rowCnt + " of " + numRows + " key-value pairs");
			}
			int numKeys = frags.size();
			m_logger.info("Fragment dictionary compiled. " + numKeys + " unique keys found");

			// Now generate the output
			exec.setMessage("Generating MMP transforms");
			exec_0 = exec.createSubProgress(0.25);
			progress = 0.0;
			long rowIdx = 0;
			long keyCnt = 0;
			if (m_includeUnchangingPortion.getBooleanValue() || m_includeHARatio.getBooleanValue()
					|| m_fpColIdx.size() > 0) {
				// We need the overloaded method with the full paramter list,
				// and to
				// iterate through the entrySet()
				for (Entry<FragmentKey2, TreeSet<FragmentValue2>> kv : frags.entrySet()) {
					ArrayList<DataCell[]> newRows = RDKitFragmentationUtils.getTransforms(
							kv.getValue(), kv.getKey(), numCols, m_stripHsAtEnd.getBooleanValue(),
							m_includeUnchangingPortion.getBooleanValue(),
							m_includeHACount.getBooleanValue(), m_includeHARatio.getBooleanValue(),
							m_showReverseTransforms.getBooleanValue(),
							m_AllowSelfTransforms.getBooleanValue());
					for (DataCell[] cells : newRows) {
						// Deal with need for rSMARTS
						if (m_includeReactionSMARTS.getBooleanValue()) {
							cells = addReactionSmartsCell(cells);
						}
						cells = applyFingerprintCells(cells, keyFps.get(kv.getKey()));
						RowKey rowKey = new RowKey("Row_" + rowIdx++);

						dc0.addRowToTable(new DefaultRow(rowKey, cells));
					}
					progress = (double) keyCnt++ / (double) numKeys;
					exec_0.checkCanceled();
					exec_0.setProgress(progress, "Processed Key " + keyCnt + " of " + numKeys
							+ ".  " + rowIdx + " transforms generated.");
				}

			} else {
				// We can use the method with fewer arguments and only iterate
				// through the values
				for (TreeSet<FragmentValue2> vals : frags.values()) {
					ArrayList<DataCell[]> newRows = RDKitFragmentationUtils.getTransforms(vals,
							numCols, m_stripHsAtEnd.isEnabled() && m_stripHsAtEnd.getBooleanValue(),
							m_includeHACount.getBooleanValue(),
							m_showReverseTransforms.getBooleanValue(),
							m_AllowSelfTransforms.getBooleanValue());

					for (DataCell[] cells : newRows) {
						// Deal with need for rSMARTS
						if (m_includeReactionSMARTS.getBooleanValue()) {
							cells = addReactionSmartsCell(cells);
						}

						RowKey rowKey = new RowKey("Row_" + rowIdx++);
						dc0.addRowToTable(new DefaultRow(rowKey, cells));
					}
					progress = (double) keyCnt++ / (double) numKeys;
					exec.checkCanceled();
					exec_0.setProgress(progress, "Processed Key " + keyCnt + " of " + numKeys
							+ ".  " + rowIdx + " transforms generated.");
				}
			}

		}

		dc0.close();
		return new BufferedDataTable[] { dc0.getTable() };
	}

	/**
	 * Method to add fingerprint cells to DataCell[] of nascent row. NB will now
	 * also add the number of cuts if present
	 * 
	 * @param cells
	 *            The Row cells - including those (null) for the fingerprint
	 * @param fpCells
	 *            The fingerprint cells
	 * @return The DataCell[] for the row
	 */
	private DataCell[] applyFingerprintCells(DataCell[] cells, DataCell[] fpCells) {
		for (int i = 0; i < fpCells.length; i++) {
			cells[cells.length - fpCells.length + i] = fpCells[i];
		}
		return cells;
	}

	/**
	 * Method to add SMARTs cell to DataCell[] of nascent row
	 * 
	 * @param cells
	 *            The Row cells - including that (null) for the SMARTS cell
	 * @returnThe DataCell[] for the row
	 */
	private DataCell[] addReactionSmartsCell(DataCell[] cells) {
		String SMIRKS = ((SmilesValue) cells[0]).getSmilesValue();
		String rSMARTS = RDKitFragmentationUtils.convertSmirksToReactionSmarts(SMIRKS);
		cells[cells.length - 1 - m_fpColIdx.size()] = SmartsCellFactory.create(rSMARTS);
		return cells;
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

		m_spec = createOutSpec(inSpecs[0]);
		return new DataTableSpec[] { m_spec };
	}

	/** Create output table spec, accounting for settings models */
	protected DataTableSpec createOutSpec(DataTableSpec dataTableSpec) {
		numCols = 5;
		if (m_includeUnchangingPortion.getBooleanValue()) {
			numCols++;
		}
		if (m_includeHACount.getBooleanValue()) {
			numCols += 2;
		}
		if (m_includeHARatio.getBooleanValue()) {
			numCols += 2;
		}
		if (m_includeReactionSMARTS.getBooleanValue()) {
			numCols++;
		}

		// Deal with possibility of incoming AP FPs
		String[] colNames = dataTableSpec.getColumnNames();
		ArrayList<String> fpColNames = new ArrayList<>();
		m_fpColIdx = new ArrayList<>();
		// TODO: This could be modified to pass through ANY columns other than
		// ID, key and value
		for (String colName : colNames) {
			if (colName.matches("Attachment point \\d+ fingerprint.*")) {
				fpColNames.add(colName);
				m_fpColIdx.add(dataTableSpec.findColumnIndex(colName));
			}
			if (colName.startsWith("Number of Cuts")) {
				fpColNames.add(colName);
				m_fpColIdx.add(dataTableSpec.findColumnIndex(colName));
			}
		}
		numCols += fpColNames.size();

		DataColumnSpec[] specs = new DataColumnSpec[numCols];
		int i = 0;
		specs[i++] = createColSpec("Transformation", SmilesCell.TYPE);
		specs[i++] = createColSpec("ID (Left)", StringCell.TYPE);
		specs[i++] = createColSpec("ID (Right)", StringCell.TYPE);
		specs[i++] = createColSpec("Left Fragment", SmilesCell.TYPE);
		specs[i++] = createColSpec("Right Fragment", SmilesCell.TYPE);
		if (m_includeUnchangingPortion.getBooleanValue()) {
			specs[i++] = createColSpec("Unchanging fragment(s)", SmilesCell.TYPE);
		}
		if (m_includeHACount.getBooleanValue()) {
			specs[i++] = createColSpec("Changing Heavy Atoms (Left)", IntCell.TYPE);
			specs[i++] = createColSpec("Changing Heavy Atoms (Right)", IntCell.TYPE);
		}
		if (m_includeHARatio.getBooleanValue()) {
			specs[i++] = createColSpec("Ratio of Changing / Unchanging Heavy Atoms (Left)",
					DoubleCell.TYPE);
			specs[i++] = createColSpec("Ratio of Changing / Unchanging Heavy Atoms (Right)",
					DoubleCell.TYPE);
		}
		if (m_includeReactionSMARTS.getBooleanValue()) {
			specs[i++] = createColSpec("Transformation Reaction SMARTS", SmartsCell.TYPE);
		}

		for (String fpColName : fpColNames) {
			if (fpColName.startsWith("Attachment point")) {
				specs[i++] = createColSpec(fpColName, DenseBitVectorCell.TYPE);
			} else {
				// Number of cuts
				specs[i++] = createColSpec(fpColName, IntCell.TYPE);
			}
		}

		return new DataTableSpec(specs);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing

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
		// Nothing

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
		m_SortedFragKey.saveSettingsTo(settings);
		m_CheckSortedKeys.saveSettingsTo(settings);
		m_IDColName.saveSettingsTo(settings);
		m_IgnoreIDs.saveSettingsTo(settings);
		m_AllowSelfTransforms.saveSettingsTo(settings);
		m_FragValColName.saveSettingsTo(settings);
		m_includeUnchangingPortion.saveSettingsTo(settings);
		m_includeHACount.saveSettingsTo(settings);
		m_includeHARatio.saveSettingsTo(settings);
		m_stripHsAtEnd.saveSettingsTo(settings);
		m_showReverseTransforms.saveSettingsTo(settings);
		m_includeReactionSMARTS.saveSettingsTo(settings);
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
		m_SortedFragKey.validateSettings(settings);
		m_CheckSortedKeys.validateSettings(settings);
		m_IDColName.validateSettings(settings);
		m_IgnoreIDs.validateSettings(settings);
		m_AllowSelfTransforms.validateSettings(settings);
		m_FragValColName.validateSettings(settings);
		m_includeUnchangingPortion.validateSettings(settings);
		m_includeHACount.validateSettings(settings);
		m_includeHARatio.validateSettings(settings);
		m_stripHsAtEnd.validateSettings(settings);
		m_showReverseTransforms.validateSettings(settings);
		m_includeReactionSMARTS.validateSettings(settings);
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
		m_SortedFragKey.loadSettingsFrom(settings);
		m_CheckSortedKeys.loadSettingsFrom(settings);
		m_IDColName.loadSettingsFrom(settings);
		m_IgnoreIDs.loadSettingsFrom(settings);
		m_AllowSelfTransforms.loadSettingsFrom(settings);
		m_FragValColName.loadSettingsFrom(settings);
		m_includeUnchangingPortion.loadSettingsFrom(settings);
		m_includeHACount.loadSettingsFrom(settings);
		m_includeHARatio.loadSettingsFrom(settings);
		m_stripHsAtEnd.loadSettingsFrom(settings);
		m_showReverseTransforms.loadSettingsFrom(settings);
		m_includeReactionSMARTS.loadSettingsFrom(settings);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
		// nothing

	}

}
