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
package com.vernalis.knime.mmp.nodes.fragutil.filter.abstrct;

import java.util.Arrays;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.IncomingMoleculeException;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.ClosedFactoryException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeModel;

/**
 * Abstract Streamable NodeModel for the MMP Filter/Splitter nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPFragmentationFilterSplitterNodeModel<T, U>
		extends AbstractMMPFragmentationFactoryNodeModel<T, U> {

	protected final boolean isSplitter;

	/**
	 * Constructor
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 * @param isSplitter
	 *            Is the node a splitter?
	 */
	public AbstractMMPFragmentationFilterSplitterNodeModel(
			FragmentationUtilsFactory<T, U> fragUtilityFactory, boolean isSplitter) {
		super(1, isSplitter ? 2 : 1, fragUtilityFactory, false, false);
		this.isSplitter = isSplitter;
	}

	@Override
	protected DataTableSpec[] doConfigure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
		DataTableSpec[] retVal = new DataTableSpec[isSplitter ? 2 : 1];
		Arrays.fill(retVal, inSpecs[0]);
		return retVal;
	}

	@Override
	protected BufferedDataTable[] doExecute(BufferedDataTable[] inData, ExecutionContext exec)
			throws Exception {
		BufferedDataTable inTable = inData[0];
		RowInput inputRow = new DataTableRowInput(inTable);

		// Sort out the reaction
		final U bondMatch = getMatcherFromSMARTS(getFragmentationSMARTSMatch());

		// Now do some final setting up
		final int maxNumCuts = m_numCuts.getIntValue();
		final boolean addHs = (maxNumCuts == 1) ? m_AddHs.getBooleanValue() : false;
		final int molColIdx = inData[0].getSpec().findColumnIndex(m_molColName.getStringValue());

		// Create the output containers
		BufferedDataContainer[] bdc = new BufferedDataContainer[isSplitter ? 2 : 1];
		bdc[0] = exec.createDataContainer(inTable.getDataTableSpec());
		RowOutput keeps = new BufferedDataTableRowOutput(bdc[0]);
		RowOutput drops;
		if (isSplitter) {
			bdc[1] = exec.createDataContainer(inTable.getDataTableSpec());
			drops = new BufferedDataTableRowOutput(bdc[1]);
		} else {
			drops = null;
		}
		this.execute(inputRow, keeps, drops, inTable.size(), exec, bondMatch, maxNumCuts, addHs,
				molColIdx);
		return (isSplitter) ? new BufferedDataTable[] { bdc[0].getTable(), bdc[1].getTable() }
				: new BufferedDataTable[] { bdc[0].getTable() };
	}

	protected void execute(final RowInput inRow, RowOutput keeps, RowOutput drop,
			final long numRows, final ExecutionContext exec, U bondMatch, int maxNumCuts,
			boolean addHs, int molColIdx) throws InterruptedException, CanceledExecutionException {

		long rowIdx = 0;
		DataRow row;
		while ((row = inRow.poll()) != null) {
			if (numRows > 0) {
				exec.setProgress((++rowIdx) / (double) numRows,
						"Filtering row " + rowIdx + " of " + numRows);
			} else {
				exec.setProgress("Filtering row " + rowIdx);
			}
			exec.checkCanceled();
			// Get the molecule to fragment, checking for simple upfront errors
			// first
			T mol;
			try {
				mol = getMoleculeFromRow(row, molColIdx, -1, rowIdx + 1L);
			} catch (IllegalArgumentException | IncomingMoleculeException e) {
				if (drop != null) {
					drop.push(row);
				}
				continue;
			}
			if (mol == null || fragUtilityFactory.moleculeIsEmpty(mol)) {
				if (drop != null) {
					drop.push(row);
				}
				continue;
			}
			MoleculeFragmentationFactory2<T, U> fragFactory = null;
			try {
				fragFactory = fragUtilityFactory.createFragmentationFactory(mol, bondMatch, false,
						false, verboseLogging, false, 0, 0.0, cacheSize);
				if (fragFactory.canCutNTimes(m_numCuts.getIntValue(),
						m_allowTwoCutsToBondValue.getBooleanValue())) {
					keeps.push(row);
				} else if (m_numCuts.getIntValue() == 1 && m_AddHs.getBooleanValue()) {
					// Check if we want 1 cut and adding Hs if that doesn't make
					// a difference
					fragFactory.close();
					fragFactory = fragUtilityFactory.createHAddedFragmentationFactory(mol,
							bondMatch, false, verboseLogging, false, 0, 0.0, rowIdx + 1L);
					if (fragFactory.canCutNTimes(1, false)) {
						keeps.push(row);
					}
				} else if (drop != null) {
					drop.push(row);
				}
			} catch (ClosedFactoryException | ToolkitException e) {
				if (drop != null) {
					drop.push(row);
				}
			} finally {
				if (fragFactory != null) {
					fragFactory.close();
				}
			}
			fragUtilityFactory.rowCleanup(rowIdx + 1L);
		}

		keeps.close();
		if (drop != null) {
			drop.close();
		}
	}

	@Override
	public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
			final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs, ExecutionContext exec)
					throws Exception {
				// Sort out the reaction
				String fragSMIRKS =
						FragmentationTypes.valueOf(m_fragSMIRKS.getStringValue()).getSMARTS();
				if ((fragSMIRKS == null || "".equals(fragSMIRKS)) && FragmentationTypes.valueOf(
						m_fragSMIRKS.getStringValue()) == FragmentationTypes.USER_DEFINED) {
					fragSMIRKS = m_customSmarts.getStringValue();
				}

				final U bondMatch = fragUtilityFactory.getMatcher(
						fragSMIRKS.contains(">>") ? fragSMIRKS.split(">>")[0] : fragSMIRKS);

				// Now do some final setting up
				final int maxNumCuts = m_numCuts.getIntValue();
				final boolean addHs = (maxNumCuts == 1) ? m_AddHs.getBooleanValue() : false;
				final int molColIdx =
						((DataTableSpec) inSpecs[0]).findColumnIndex(m_molColName.getStringValue());

				// Run it - dont know row count!
				AbstractMMPFragmentationFilterSplitterNodeModel.this.execute((RowInput) inputs[0],
						(RowOutput) outputs[0], isSplitter ? (RowOutput) outputs[1] : null, -1L,
						exec, bondMatch, maxNumCuts, addHs, molColIdx);
			}

		};
	}

	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	@Override
	public OutputPortRole[] getOutputPortRoles() {
		OutputPortRole[] retVal = new OutputPortRole[isSplitter ? 2 : 1];
		Arrays.fill(retVal, OutputPortRole.DISTRIBUTED);
		return retVal;
	}

}
