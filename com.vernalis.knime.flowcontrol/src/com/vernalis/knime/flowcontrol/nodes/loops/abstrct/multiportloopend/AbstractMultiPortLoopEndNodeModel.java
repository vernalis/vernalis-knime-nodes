/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
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
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.core.node.workflow.LoopEndNode;
import org.knime.core.node.workflow.LoopStartNodeTerminator;
import org.knime.core.util.DuplicateKeyException;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

/**
 * This is the model implementation of AbstractMultiPortLoopEnd. Loop end node
 * to handle optional input ports n
 * 
 * @version 1.29.0 Added {@link PortsConfiguration} constructor
 * @author S. Roughley
 */
public class AbstractMultiPortLoopEndNodeModel extends NodeModel
		implements LoopEndNode {

	/** the logger instance. */
	protected final NodeLogger logger = NodeLogger.getLogger(getClass());

	// Settings Keys
	/** The Constant NON_CONNECTED_OPTION. */
	static final String NON_CONNECTED_OPTION = "Non-Connected_Branch_Behaviour";

	/** The m_ num ports. */
	protected final int m_NumPorts;

	/** The m_result container. */
	protected ConcatenatingTablesCollector[] m_resultContainer;

	/** The m_current iteration. */
	protected int m_currentIteration = 0;

	/** The m_settings. */
	protected final AbstractMultiPortLoopEndSettings m_settings;

	/** The m_specs. */
	protected PortObjectSpec[] m_specs;

	/**
	 * Single argument constructor for the model, specifying the number of
	 * ports. If there are more than two ports, all except the first are
	 * optional.
	 * 
	 * @param numPorts
	 *            The number of dataports
	 */
	public AbstractMultiPortLoopEndNodeModel(final int numPorts) {
		this(numPorts, true);
	}

	/**
	 * Constructor for the model, specifying the number of ports and whether any
	 * are optional. If there are more than two ports, and {code
	 * optionalInPorts} are specified, then all except the first are optional
	 * 
	 * @param numPorts
	 *            The number of dataports
	 * @param optionalInPorts
	 *            Boolean specifying whether the in-ports are optional when
	 *            there {@code numPorts} > 2. Omitting this argument treats it
	 *            as 'true'
	 */
	public AbstractMultiPortLoopEndNodeModel(final int numPorts,
			final boolean optionalInPorts) {
		this((optionalInPorts)
				? FlowControlHelpers.createEndInPorts(BufferedDataTable.TYPE,
						numPorts)
				: FlowControlHelpers.createStartOutPorts(BufferedDataTable.TYPE,
						numPorts),
				FlowControlHelpers.createStartOutPorts(BufferedDataTable.TYPE,
						numPorts));
	}

	/**
	 * Constructor specifying the port types
	 * 
	 * @param inPortTypes
	 *            The incoming port types
	 * @param outPortTypes
	 *            The outgoing port types
	 */
	protected AbstractMultiPortLoopEndNodeModel(final PortType[] inPortTypes,
			final PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		m_NumPorts = inPortTypes.length;
		m_resultContainer =
				new ConcatenatingTablesCollector[inPortTypes.length];
		m_settings = new AbstractMultiPortLoopEndSettings(inPortTypes.length);

	}

	/**
	 * Constructor from a {@link PortsConfiguration} object
	 * 
	 * @param portsConfiguration
	 *            The port configuration
	 * @since v1.29.0
	 */
	public AbstractMultiPortLoopEndNodeModel(
			PortsConfiguration portsConfiguration) {
		super(portsConfiguration.getInputPorts(),
				portsConfiguration.getOutputPorts());
		m_NumPorts = getNrInPorts();
		m_resultContainer = new ConcatenatingTablesCollector[m_NumPorts];
		m_settings = new AbstractMultiPortLoopEndSettings(m_NumPorts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		validateLoopStartNode();

		// Handle whatever data tables we find in the current iteration
		for (int i = 0; i < m_NumPorts; i++) {
			if (m_currentIteration == 0 || m_resultContainer[i] == null) {
				m_resultContainer[i] = new ConcatenatingTablesCollector(
						m_settings.ignoreEmptyTables(i),
						m_settings.getAddIterationColumn(i),
						m_settings.allowChangingColumnTypes(i),
						m_settings.allowChangingTableSpecs(i),
						Optional.of(m_settings.getRowKeyPolicy(i)),
						Optional.of(i));
			}

			if (inData[i] != null) {
				try {
					m_resultContainer[i]
							.appendTable((BufferedDataTable) inData[i], exec);

				} catch (IllegalArgumentException e) {
					for (String errorLine : e.getMessage().split(";[\\s]*")) {
						// Report the problem(s) to the console in a
						// multi-line format
						logger.error(errorLine);
					}
					// And throw the error
					throw e;

				}
			}
		}

		// Now check whether we are at the end of the loop
		if (((LoopStartNodeTerminator) this.getLoopStartNode())
				.terminateLoop()) {
			boolean[] portsConnected = new boolean[inData.length];
			for (int i = 0; i < inData.length; i++) {
				portsConnected[i] = inData[i] != null;
			}
			return endLoopExecution(portsConnected, exec);
		} else {
			return continueLoopExecution();
		}

	}

	/**
	 * Method to end loop iteration and trigger next loop iteration
	 * 
	 * @return An empty set of tables as we are in an intermediate iteration
	 */
	protected PortObject[] continueLoopExecution() {
		continueLoop();
		m_currentIteration++;
		return new BufferedDataTable[m_NumPorts];
	}

	/**
	 * @throws IllegalStateException
	 *             if the loop start node is incorrect
	 */
	protected void validateLoopStartNode() throws IllegalStateException {
		if (!(this.getLoopStartNode() instanceof LoopStartNodeTerminator)) {
			throw new IllegalStateException(
					"No matching loop start node found!");
		}
	}

	/**
	 * Method called after incoming tables have been processed for what is then
	 * found to be the final time
	 * 
	 * @param inConnected
	 *            Are the incoming ports connected?
	 * @param exec
	 *            The execution context
	 * @return The out-going port objects for the final loop output
	 * @throws CanceledExecutionException
	 *             If the user cancels during execution of this method
	 * @throws DuplicateKeyException
	 *             If duplicate row keys are encountered
	 * @throws IOException
	 *             IF there is an IOException whilst checking for duplicate
	 *             RowKeys
	 */
	protected PortObject[] endLoopExecution(final boolean[] inConnected,
			final ExecutionContext exec) throws CanceledExecutionException,
			DuplicateKeyException, IOException {
		final PortObject[] outTables = new PortObject[m_NumPorts];
		for (int i = 0; i < m_NumPorts; i++) {
			if (!inConnected[i]) {
				if (m_settings.inactivateDisconnectedBranches()) {
					outTables[i] = InactiveBranchPortObject.INSTANCE;
				} else {
					outTables[i] = m_resultContainer[i].createTable(exec);
				}
			} else {
				outTables[i] = m_resultContainer[i].createTable(exec);
			}
		}

		// Finally, do a reset to restore the interation counter and result
		// container
		reset();
		return outTables;
	}

	/**
	 * Utility function to generate output table {@link DataTableSpec} from a
	 * {@link PortObjectSpec}, taking into account the addIterationColumn
	 * setting.
	 * 
	 * @param inSpec
	 *            the in spec
	 * @param portId
	 * @return The {@link DataTableSpec} for the output port
	 */
	private DataTableSpec createOutputTableSpec(final PortObjectSpec inSpec,
			int portId) {
		return (m_settings.getAddIterationColumn(portId))
				? new DataTableSpec((DataTableSpec) inSpec,
						new DataTableSpec((new DataColumnSpecCreator(
								DataTableSpec.getUniqueColumnName(
										((DataTableSpec) inSpec), "Iteration"),
								IntCell.TYPE)).createSpec()))
				: ((DataTableSpec) inSpec);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// restore the interation counter and result container
		m_resultContainer = new ConcatenatingTablesCollector[m_NumPorts];
		m_currentIteration = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		m_specs = new PortObjectSpec[m_NumPorts];
		Arrays.fill(m_specs, null);
		for (int i = 0; i < m_NumPorts; i++) {
			if (inSpecs[i] != null) {
				if (!m_settings.ignoreEmptyTables(i)) {
					m_specs[i] = createOutputTableSpec(inSpecs[i], i);
				}
			} else {
				m_specs[i] = (m_settings.inactivateDisconnectedBranches())
						? InactiveBranchPortObjectSpec.INSTANCE
						: new DataTableSpec();
			}
		}
		return m_specs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_settings.saveSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_settings.loadSettings(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		// We just try loading
		AbstractMultiPortLoopEndSettings s =
				new AbstractMultiPortLoopEndSettings(m_NumPorts);
		s.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
