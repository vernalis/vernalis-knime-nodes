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
package com.vernalis.knime.mmp.nodes.rdkit.multicut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.RDKit.ROMol;
import org.knime.base.data.append.column.AppendedColumnRow;
import org.knime.chem.types.SmartsCell;
import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.FragmentKey;
import com.vernalis.knime.mmp.FragmentValue;
import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.RDKitUtils;
import com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeModel;

import static com.vernalis.knime.mmp.RDKitFragment.doRDKitFragmentation;
import static com.vernalis.knime.mmp.RDKitFragment.filterFragments;
import static com.vernalis.knime.mmp.RDKitFragment.getTransforms;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createOutputKeyModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createShowReverseTransformsModel;
import static com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeDialog.createShowSmartsTransformsModel;

/**
 * {@link NodeModel} implementation for the MMP node
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
@Deprecated
public class RdkitMatchedPairsNodeModel extends AbstractRdkitMatchedPairsMultipleCutsNodeModel {

	protected final SettingsModelBoolean m_outputKey = createOutputKeyModel();
	private final SettingsModelBoolean m_showReverseTransforms = createShowReverseTransformsModel();
	private final SettingsModelBoolean m_includeReactionSMARTS = createShowSmartsTransformsModel();

	/**
	 * Node Model Constructor
	 */
	public RdkitMatchedPairsNodeModel() {
		super();
		m_Logger = NodeLogger.getLogger(RdkitMatchedPairsNodeModel.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
			throws Exception {

		// Do some setting up
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		final int molIdx = spec.findColumnIndex(m_molColName.getStringValue());
		final int idIdx = spec.findColumnIndex(m_idColName.getStringValue());
		int numRows = table.getRowCount();
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

		// Now do some final setting up
		int numCuts = m_numCuts.getIntValue();
		// Only add H's is numCuts ==1 and addH's flag set
		boolean addHs = (numCuts == 1) ? m_AddHs.getBooleanValue() : false;

		// These two can both be null
		Integer maxNumVarAtm =
				(m_hasChangingAtoms.getBooleanValue()) ? m_maxChangingAtoms.getIntValue() : null;
		Double minCnstToVarAtmRatio =
				(m_hasHARatioFilter.getBooleanValue()) ? m_minHARatioFilter.getDoubleValue() : null;

		boolean trackCutConnectivity =
				m_trackCutConnectivity.isEnabled() && m_trackCutConnectivity.getBooleanValue();

		// Container for the fragmentations
		HashMap<FragmentKey, TreeSet<FragmentValue>> frags =
				new HashMap<>();

		m_Logger.info("Starting fragmentation");
		m_Logger.info("Fragmentation SMIRKS: " + fragSMIRKS + " (" + numCuts + " cuts)");
		if (addHs) {
			m_Logger.info("Adding Hydrogens before fragmentation");
		}

		// Allocate 75% of time for this
		ExecutionMonitor exec_0 = exec.createSubProgress(0.75);
		double progress = 0.0;
		int rowCnt = 0;

		// Now parse the input molecules...
		for (final DataRow row : table) {
			DataCell molCell = row.getCell(molIdx);
			if (molCell.isMissing()) {
				// Deal with missing mols
				dc_1.addRowToTable((m_addFailReasons.getBooleanValue()) ? new AppendedColumnRow(row,
						new StringCell("Missing value in Molecule Column")) : row);
				continue;
			}

			String molID = ((StringValue) row.getCell(idIdx)).getStringValue();
			ROMol roMol;
			try {
				roMol = getROMolFromCell(row.getCell(molIdx));
			} catch (RowExecutionException e) {
				// Log the failed row
				m_Logger.info("Error parsing molecule (Row: " + row.getKey().getString() + ") "
						+ e.getMessage());
				// And add it to the second output
				dc_1.addRowToTable((m_addFailReasons.getBooleanValue())
						? new AppendedColumnRow(row, new StringCell(e.getMessage())) : row);
				continue;
			}

			if (roMol == null || "".equals(roMol.MolToSmiles(true))) {
				// Deal with when we cannot get an ROMol object - e.g. for 'No
				// Structure' Mol files
				// And add it to the second output
				dc_1.addRowToTable((m_addFailReasons.getBooleanValue()) ? new AppendedColumnRow(row,
						new StringCell("'No Structure' input molecule")) : row);
				continue;
			}

			if (addHs) {
				roMol = roMol.addHs(false, false);
			}

			// Do the fragmentation
			HashMap<FragmentKey, TreeSet<FragmentValue>> newFrags = doRDKitFragmentation(roMol,
					molID, numCuts, fragSMIRKS, trackCutConnectivity, exec);

			// Clean up the new fragments (apply max change etc settings -
			newFrags = filterFragments(newFrags, maxNumVarAtm, minCnstToVarAtmRatio);

			// Now, add the new fragments to the library
			for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : newFrags.entrySet()) {
				if (!frags.containsKey(ent.getKey())) {
					frags.put(ent.getKey(), new TreeSet<FragmentValue>());
				}
				frags.get(ent.getKey()).addAll(ent.getValue());
			}
			progress = (double) rowCnt++ / (double) numRows;
			exec.checkCanceled();
			exec_0.setProgress(progress, "Fragmented Row " + rowCnt + " of " + numRows);
		}

		// Now we need to add the rows to the output table
		exec_0 = exec.createSubProgress(0.25);
		progress = 0.0;
		int numKeys = frags.size();
		int keyCnt = 0;
		int rowIdx = 0;
		m_Logger.info("Fragmentation completed.  " + numKeys + " unique keys generated.");

		if (m_outputKey.getBooleanValue() || m_outputHARatio.getBooleanValue()
				|| (m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue())) {
			// We need the overloaded method with the full parameter list, and
			// to
			// iterate through the entrySet()
			for (Entry<FragmentKey, TreeSet<FragmentValue>> kv : frags.entrySet()) {
				ArrayList<DataCell[]> newRows = getTransforms(kv.getValue(), kv.getKey(), numCols,
						m_stripHsAtEnd.isEnabled() && m_stripHsAtEnd.getBooleanValue(),
						m_outputKey.getBooleanValue(), m_outputNumChgHAs.getBooleanValue(),
						m_outputHARatio.getBooleanValue(),
						m_showReverseTransforms.getBooleanValue());
				for (DataCell[] cells : newRows) {
					// Deal with need for rSMARTS
					if (m_includeReactionSMARTS.getBooleanValue()) {
						cells = addReactionSmartsCell(cells);
					}
					if (m_apFingerprints.getBooleanValue()) {
						cells = addAPFPs(cells, kv.getKey());
					}
					RowKey rowKey = new RowKey("Row_" + rowIdx++);
					dc_0.addRowToTable(new DefaultRow(rowKey, cells));
				}
				progress = (double) keyCnt++ / (double) numKeys;
				exec.checkCanceled();
				exec_0.setProgress(progress, "Processed Key " + keyCnt + " of " + numKeys + ".  "
						+ rowIdx + " transforms generated.");
			}

		} else {
			// We can use the method with fewer arguments and only iterate
			// through the values
			for (TreeSet<FragmentValue> vals : frags.values()) {
				ArrayList<DataCell[]> newRows = getTransforms(vals, numCols,
						m_stripHsAtEnd.isEnabled() && m_stripHsAtEnd.getBooleanValue(),
						m_outputNumChgHAs.getBooleanValue(),
						m_showReverseTransforms.getBooleanValue());

				for (DataCell[] cells : newRows) {
					// Deal with need for rSMARTS
					if (m_includeReactionSMARTS.getBooleanValue()) {
						cells = addReactionSmartsCell(cells);
					}

					RowKey rowKey = new RowKey("Row_" + rowIdx++);
					dc_0.addRowToTable(new DefaultRow(rowKey, cells));
				}
				progress = (double) keyCnt++ / (double) numKeys;
				exec.checkCanceled();
				exec_0.setProgress(progress, "Processed Key " + keyCnt + " of " + numKeys + ".  "
						+ rowIdx + " transforms generated.");
			}
		}
		dc_0.close();
		dc_1.close();
		m_Logger.info("Pair-finding complete. " + rowIdx + " transforms added");
		return new BufferedDataTable[] { dc_0.getTable(), dc_1.getTable() };
	}

	/**
	 * Method to add SMARTs cell to DataCell[] of nascent row
	 * 
	 * @param cells
	 *            The Row cells - including that (null) for the SMARTS cell
	 * @returnThe DataCell[] for the row
	 */
	private DataCell[] addReactionSmartsCell(DataCell[] cells) {
		cells[cells.length - 1
				- (m_apFingerprints.isEnabled() && m_apFingerprints.getBooleanValue()
						? m_numCuts.getIntValue() : 0)] =
								new SmartsCell(RDKitUtils.convertSmirksToReactionSmarts(
										((SmilesValue) cells[0]).getSmilesValue()));
		return cells;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec createSpec_0(DataTableSpec spec) {
		int numCols = 5;
		if (m_outputKey.getBooleanValue()) {
			numCols++;
		}
		if (m_outputNumChgHAs.getBooleanValue()) {
			numCols += 2;
		}
		if (m_outputHARatio.getBooleanValue()) {
			numCols += 2;
		}
		if (m_includeReactionSMARTS.getBooleanValue()) {
			numCols++;
		}

		DataColumnSpec[] specs = new DataColumnSpec[numCols];
		int i = 0;
		specs[i++] = createColSpec("Transformation", SmilesCell.TYPE);
		specs[i++] = createColSpec("ID (Left)", StringCell.TYPE);
		specs[i++] = createColSpec("ID (Right)", StringCell.TYPE);
		specs[i++] = createColSpec("Left Fragment", SmilesCell.TYPE);
		specs[i++] = createColSpec("Right Fragment", SmilesCell.TYPE);
		if (m_outputKey.getBooleanValue()) {
			specs[i++] = createColSpec("Unchanging fragment(s)", SmilesCell.TYPE);
		}
		if (m_outputNumChgHAs.getBooleanValue()) {
			specs[i++] = createColSpec("Changing Heavy Atoms (Left)", IntCell.TYPE);
			specs[i++] = createColSpec("Changing Heavy Atoms (Right)", IntCell.TYPE);
		}
		if (m_outputHARatio.getBooleanValue()) {
			specs[i++] = createColSpec("Ratio of Changing / Unchanging Heavy Atoms (Left)",
					DoubleCell.TYPE);
			specs[i++] = createColSpec("Ratio of Changing / Unchanging Heavy Atoms (Right)",
					DoubleCell.TYPE);
		}
		if (m_includeReactionSMARTS.getBooleanValue()) {
			specs[i++] = createColSpec("Transformation Reaction SMARTS", SmartsCell.TYPE);
		}

		return new DataTableSpec(specs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.rdkit.abstrct.
	 * AbstractRdkitMatchedPairsMultipleCutsNodeModel
	 * #saveSettingsTo(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_outputKey.saveSettingsTo(settings);
		m_showReverseTransforms.saveSettingsTo(settings);
		m_includeReactionSMARTS.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.rdkit.abstrct.
	 * AbstractRdkitMatchedPairsMultipleCutsNodeModel
	 * #loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_outputKey.loadSettingsFrom(settings);
		try {
			m_showReverseTransforms.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// Set the backwards compatible value
			m_showReverseTransforms.setBooleanValue(false);
		}
		try {
			m_includeReactionSMARTS.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// Set the backwards compatible value
			m_includeReactionSMARTS.setBooleanValue(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.rdkit.abstrct.
	 * AbstractRdkitMatchedPairsMultipleCutsNodeModel
	 * #validateSettings(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		super.validateSettings(settings);
		m_outputKey.validateSettings(settings);
		// Dont validate new settings m_showReverseTransforms and
		// m_includeReactionSMARTS
	}

}
