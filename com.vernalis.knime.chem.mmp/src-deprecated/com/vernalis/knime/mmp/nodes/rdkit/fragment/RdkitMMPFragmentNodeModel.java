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
/**
 * 
 */
package com.vernalis.knime.mmp.nodes.rdkit.fragment;

import static com.vernalis.knime.mmp.RDKitFragment.doRDKitFragmentation;
import static com.vernalis.knime.mmp.RDKitFragment.filterFragments;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.RDKit.ROMol;
import org.knime.base.data.append.column.AppendedColumnRow;
import org.knime.chem.types.SmilesCell;
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
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;

import com.vernalis.knime.mmp.FragmentKey;
import com.vernalis.knime.mmp.FragmentValue;
import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.RowExecutionException;
import com.vernalis.knime.mmp.nodes.rdkit.abstrct.AbstractRdkitMatchedPairsMultipleCutsNodeModel;

/**
 * The {@link NodeModel} implementation for the MMP Molecule Fragment node
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public class RdkitMMPFragmentNodeModel extends
		AbstractRdkitMatchedPairsMultipleCutsNodeModel {

	/**
	 * Constructor
	 */
	public RdkitMMPFragmentNodeModel() {
		super();
		m_Logger = NodeLogger.getLogger(RdkitMMPFragmentNodeModel.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

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
		String fragSMIRKS = FragmentationTypes.valueOf(
				m_fragSMIRKS.getStringValue()).getSMARTS();
		if ((fragSMIRKS == null || "".equals(fragSMIRKS))
				&& FragmentationTypes.valueOf(m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED) {
			fragSMIRKS = m_customSmarts.getStringValue();
		}

		// Now do some final setting up
		int numCuts = m_numCuts.getIntValue();
		// Only add H's is numCuts ==1 and addH's flag set
		boolean addHs = (numCuts == 1) ? m_AddHs.getBooleanValue() : false;

		// These two can both be null
		Integer maxNumVarAtm = (m_hasChangingAtoms.getBooleanValue()) ? m_maxChangingAtoms
				.getIntValue() : null;
		Double minCnstToVarAtmRatio = (m_hasHARatioFilter.getBooleanValue()) ? m_minHARatioFilter
				.getDoubleValue() : null;

		boolean trackCutConnectivity = m_trackCutConnectivity.isEnabled()
				&& m_trackCutConnectivity.getBooleanValue();

		m_Logger.info("Starting fragmentation");
		m_Logger.info("Fragmentation SMIRKS: " + fragSMIRKS + " (" + numCuts
				+ " cuts)");
		if (addHs) {
			m_Logger.info("Adding Hydrogens before fragmentation");
		}

		double progress = 0.0;
		int rowCnt = 0;
		int newRowCnt = 0;

		// Now parse the input molecules...
		for (final DataRow row : table) {
			DataCell molCell = row.getCell(molIdx);
			if (molCell.isMissing()) {
				// Deal with missing mols
				dc_1.addRowToTable((m_addFailReasons.getBooleanValue()) ? new AppendedColumnRow(
						row, new StringCell("Missing value in Molecule Column"))
						: row);
				continue;
			}

			String molID = ((StringValue) row.getCell(idIdx)).getStringValue();
			ROMol roMol;
			try {
				roMol = getROMolFromCell(row.getCell(molIdx));
			} catch (RowExecutionException e) {
				// Log the failed row
				m_Logger.info("Error parsing molecule (Row: "
						+ row.getKey().getString() + ") " + e.getMessage());
				// And add it to the second output
				dc_1.addRowToTable((m_addFailReasons.getBooleanValue()) ? new AppendedColumnRow(
						row, new StringCell(e.getMessage())) : row);
				continue;
			}

			if (roMol == null || "".equals(roMol.MolToSmiles(true))) {
				// Deal with when we cannot get an ROMol object - e.g. for 'No
				// Structure' Mol files
				// And add it to the second output
				dc_1.addRowToTable((m_addFailReasons.getBooleanValue()) ? new AppendedColumnRow(
						row, new StringCell("'No Structure' input molecule"))
						: row);
				continue;
			}

			if (addHs) {
				roMol = roMol.addHs(false, false);
			}

			// Do the fragmentation
			HashMap<FragmentKey, TreeSet<FragmentValue>> newFrags = doRDKitFragmentation(
					roMol, molID, numCuts, fragSMIRKS, trackCutConnectivity,
					exec);

			// Clean up the new fragments (apply max change etc settings -
			newFrags = filterFragments(newFrags, maxNumVarAtm,
					minCnstToVarAtmRatio);

			// Now, add the new fragments to the table
			for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : newFrags
					.entrySet()) {

				DataCell keySmiles = ent.getKey().getKeyAsDataCell(
						m_stripHsAtEnd.getBooleanValue());

				for (FragmentValue fVal : ent.getValue()) {
					RowKey rowId = new RowKey("Row_" + newRowCnt++);

					DataCell[] cells = new DataCell[numCols];
					int i = 0;
					cells[i++] = fVal.getIDCell();
					cells[i++] = keySmiles;
					cells[i++] = fVal.getSMILESCell(m_stripHsAtEnd
							.getBooleanValue());
					if (m_outputNumChgHAs.getBooleanValue()) {
						cells[i++] = fVal.getNumberChangingAtomsCell();
					}
					if (m_outputHARatio.getBooleanValue()) {
						cells[i++] = ent.getKey()
								.getConstantToVaryingAtomRatioCell(fVal);
					}
					if (m_apFingerprints.isEnabled()
							&& m_apFingerprints.getBooleanValue()) {
						cells = addAPFPs(cells, ent.getKey());
					}
					dc_0.addRowToTable(new DefaultRow(rowId, cells));
				}
			}
			roMol.delete();
			newFrags.clear();
			progress = (double) ++rowCnt / (double) numRows;
			exec.checkCanceled();
			exec.setProgress(progress, "Fragmented Row " + rowCnt + " of "
					+ numRows);
		}
		dc_0.close();
		dc_1.close();
		return new BufferedDataTable[] { dc_0.getTable(), dc_1.getTable() };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec createSpec_0(DataTableSpec spec) {
		int numCols = 3;
		if (m_outputNumChgHAs.getBooleanValue()) {
			numCols++;
		}
		if (m_outputHARatio.getBooleanValue()) {
			numCols++;
		}

		DataColumnSpec[] specs = new DataColumnSpec[numCols];
		int i = 0;
		specs[i++] = createColSpec("ID", StringCell.TYPE);
		specs[i++] = createColSpec(
				"Fragmentation 'Key' (" + m_numCuts.getIntValue()
						+ " bond cuts)", SmilesCell.TYPE);
		specs[i++] = createColSpec("Fragmentation 'Value'", SmilesCell.TYPE);

		if (m_outputNumChgHAs.getBooleanValue()) {
			specs[i++] = createColSpec("Changing Heavy Atoms", IntCell.TYPE);
		}
		if (m_outputHARatio.getBooleanValue()) {
			specs[i++] = createColSpec(
					"Ratio of Changing / Unchanging Heavy Atoms",
					DoubleCell.TYPE);
		}

		return new DataTableSpec(specs);
	}

}
