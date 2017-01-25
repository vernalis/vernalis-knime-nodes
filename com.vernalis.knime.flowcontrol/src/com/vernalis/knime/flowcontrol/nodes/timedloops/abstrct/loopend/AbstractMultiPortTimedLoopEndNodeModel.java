/*******************************************************************************
 * Copyright (c) 2014, 2016 Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopend;

import java.io.IOException;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.util.DuplicateKeyException;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;
import com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.AbstractMultiPortLoopEndNodeModel;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeModel;

/**
 * This is the model implementation of AbstractMultiPortLoopEnd. Loop end node
 * to handle optional input ports n
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class AbstractMultiPortTimedLoopEndNodeModel extends AbstractMultiPortLoopEndNodeModel
		implements LoopEndNode {

	/**
	 * Single argument constructor for the model, specifying the number of
	 * ports. If there are more than two ports, all except the first are
	 * optional.
	 * 
	 * @param numPorts
	 *            The number of dataports
	 */
	public AbstractMultiPortTimedLoopEndNodeModel(final int numPorts) {

		super(FlowControlHelpers.createTimedEndOptionalInPorts(BufferedDataTable.TYPE, numPorts),
				FlowControlHelpers.createTimedEndPorts(BufferedDataTable.TYPE, numPorts));
	}

	/**
	 * Constructor for the model, specifying the number of ports and whether any
	 * are optional. If there are more than two ports, and
	 * {@link optionalInPorts} are specified, then all except the first are
	 * optional
	 * 
	 * @param numPorts
	 *            The number of dataports
	 * @param optionalInPorts
	 *            Boolean specifying whether the in-ports are optional when
	 *            there {@link numPorts} > 2. Omitting this argument treats it
	 *            as 'true'
	 */
	public AbstractMultiPortTimedLoopEndNodeModel(final int numPorts,
			final boolean optionalInPorts) {

		super((optionalInPorts)
				? FlowControlHelpers.createTimedEndOptionalInPorts(BufferedDataTable.TYPE, numPorts)
				: FlowControlHelpers.createTimedEndInPorts(BufferedDataTable.TYPE, numPorts),
				FlowControlHelpers.createTimedEndPorts(BufferedDataTable.TYPE, numPorts));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.
	 * AbstractMultiPortLoopEndNodeModel#validateLoopStartNode()
	 */
	@Override
	protected void validateLoopStartNode() throws IllegalStateException {
		// Check for a loop start node of some sort
		super.validateLoopStartNode();

		// Now need to check that the loop start is a timed loop start
		if (!(this.getLoopStartNode() instanceof AbstractTimedLoopStartNodeModel)) {
			throw new IllegalStateException("Start loop is of wrong type - must be a "
					+ "'Run-to-time' or 'Run-for-time' Loop Start node");
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		PortObjectSpec[] superSpecs = super.configure(inSpecs);
		m_specs = new PortObjectSpec[m_NumPorts + 1];
		int portId = 0;
		for (PortObjectSpec portSpec : superSpecs) {
			m_specs[portId++] = portSpec;
		}
		// Get the spec for the unprocessed rows
		// Now need to check that the loop start is a timed loop start
		if (this.getLoopStartNode() instanceof AbstractTimedLoopStartNodeModel) {
			m_specs[portId] = ((AbstractTimedLoopStartNodeModel) this.getLoopStartNode())
					.getInSpec();
		}

		pushFlowVariableInt("Last Iteration", 0);
		pushFlowVariableString("End Time", "");
		return m_specs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.
	 * AbstractMultiPortLoopEndNodeModel#continueLoopExecution()
	 */
	@Override
	protected PortObject[] continueLoopExecution() {
		super.continueLoopExecution();
		return new BufferedDataTable[m_NumPorts + 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.
	 * AbstractMultiPortLoopEndNodeModel#endLoopExecution(boolean[],
	 * org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected PortObject[] endLoopExecution(boolean[] inConnected, ExecutionContext exec)
			throws CanceledExecutionException, DuplicateKeyException, IOException {
		final PortObject[] outTables = new PortObject[m_NumPorts + 1];
		int portId = 0;
		for (PortObject pObj : super.endLoopExecution(inConnected, exec)) {
			outTables[portId++] = pObj;
		}

		// Now add the unprocessed rows from the loop start
		AbstractTimedLoopStartNodeModel loopStartModel = (AbstractTimedLoopStartNodeModel) this
				.getLoopStartNode();
		outTables[portId] = loopStartModel.getUnprocessedRows(exec);
		// And add the iteration as a flow variable - the iteration counter
		// will be one higher!

		pushFlowVariableString("End Time", loopStartModel.getEndTime().toString());

		// Finally, do a reset to restore the interaction counter and result
		// container
		reset();
		return outTables;
	}

}
