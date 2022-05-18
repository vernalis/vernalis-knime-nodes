/*******************************************************************************
 * Copyright (c) 2018,2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.abstrct.crossover;

import org.knime.core.data.DataRow;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.workflow.FlowVariable;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;
import com.vernalis.knime.flowcontrol.nodes.abstrct.varvalifswitch.AbstractFvvalIfSwitchNodeModel2;
import com.vernalis.knime.misc.ArrayUtils;


/**
 * This is shared NodeModel implementation of the if switch flow var value nodes
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 */
@Deprecated
public class AbstractCrossoverNodeModel
		extends AbstractFvvalIfSwitchNodeModel2 {

	public AbstractCrossoverNodeModel(PortType portType) {
		super(FlowControlHelpers.createStartOutPorts(portType, 2),
				FlowControlHelpers.createStartOutPorts(portType, 2));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getInputPortRoles()
	 */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return ArrayUtils.of(InputPortRole.DISTRIBUTED_STREAMABLE, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return ArrayUtils.of(OutputPortRole.DISTRIBUTED, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(
			PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new StreamableOperator() {

			boolean swap = !compareVariableValue(m_fvname1.getStringValue(),
					m_Comp.getStringValue(), m_property.getStringValue());

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {
				// Handle the first input
				RowOutput ro0 = (RowOutput) outputs[swap ? 1 : 0];
				DataRow row;
				while ((row = ((RowInput) inputs[0]).poll()) != null) {
					ro0.push(row);
				}
				ro0.close();

				// And the second input
				RowOutput ro1 = (RowOutput) outputs[swap ? 0 : 1];
				while ((row = ((RowInput) inputs[1]).poll()) != null) {
					ro1.push(row);
				}
				ro1.close();
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData,
			final ExecutionContext exec) throws Exception {
		// The validity of performing the comparison has already been checked in
		// the #configure method, so now we just need to pass the input port to
		// the active output port based on the comparison
		// If the comparison is true the ports are not swapped, if it is false,
		// they are swapped
		boolean swap = !compareVariableValue(m_fvname1.getStringValue(),
				m_Comp.getStringValue(), m_property.getStringValue());
		return swap ? new PortObject[] { inData[1], inData[0] } : inData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		// Start by checking a variable has been selected
		if (getAvailableFlowVariables()
				.get(m_fvname1.getStringValue()) == null) {
			getLogger().warn("No valid variable selected");
			throw new InvalidSettingsException("No valid variable selected");
		}

		// Now check a meaningful comparison is possible
		if (getAvailableFlowVariables().get(m_fvname1.getStringValue())
				.getType() != FlowVariable.Type.STRING) {

			if ("".equals(m_Comp.getStringValue())) {
				// First up, we cannot compare an empty string to a number...
				getLogger().warn(
						"Cannot compare a numeric variable with an empty string");
				throw new InvalidSettingsException(
						"Cannot compare a numeric variable with an empty string");
			}
			try {
				// Check to see if we can convert the string to a numeric value
				// Double.parseDouble is more tolerant, and will handle e.g.
				// '1', '1.0'
				Double.parseDouble(m_property.getStringValue().trim());
			} catch (Exception e) {
				getLogger().warn(
						"Cannot convert comparison value to numeric value for comparison");
				throw new InvalidSettingsException(
						"Cannot convert comparison value to numeric value for comparison");
			}
		}

		if (getAvailableFlowVariables().get(m_fvname1.getStringValue())
				.getType() == FlowVariable.Type.INTEGER) {
			// For integers, Integer.parseInt is less tolerant, and fails with a
			// decimal point being present, even if it is '1.0'
			try {
				// Check to see if we can convert the string to a numeric value
				Integer.parseInt(m_property.getStringValue().trim());
			} catch (Exception e) {
				// Throw an informative error message!
				if (m_property.getStringValue().endsWith(".0")) {
					getLogger().warn(
							"Remove trailing '.0' for integer comparison");
					throw new InvalidSettingsException(
							"Remove trailing '.0' for integer comparison");
				} else {
					getLogger()
							.warn("Cannot convert comparison value to numeric integer value for comparison."
									+ "  Ensure there is no decimal point.");
					throw new InvalidSettingsException(
							"Cannot convert comparison value to numeric integer value for comparison."
									+ "  Ensure there is no decimal point.");
				}
			}
		}

		// Now we believe we can sensible try to perform the comparison
		// If the comparison is true the top port is active
		boolean swap = !compareVariableValue(m_fvname1.getStringValue(),
				m_Comp.getStringValue(), m_property.getStringValue());
		return swap ? new PortObjectSpec[] { inSpecs[1], inSpecs[0] } : inSpecs;
	}

}
