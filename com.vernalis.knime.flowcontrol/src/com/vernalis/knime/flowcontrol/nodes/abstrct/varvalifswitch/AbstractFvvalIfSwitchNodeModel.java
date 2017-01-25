/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.abstrct.varvalifswitch;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.FlowVariable;

import com.vernalis.knime.flowcontrol.FlowControlHelpers;

// TODO: Auto-generated Javadoc
/**
 * This is shared NodeModel implementation of the if switch flow var value nodes
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class AbstractFvvalIfSwitchNodeModel extends NodeModel {

	/** The logger instance */
	private static final NodeLogger logger = NodeLogger
			.getLogger(AbstractFvvalIfSwitchNodeModel.class);

	/** The number of out ports. */
	private static final int m_outPorts = 2;

	/** The variable name settings model */
	private final SettingsModelString m_fvname1 = AbstractFvvalIfSwitchNodeDialog
			.createFlowVarSelectionModel();

	/** The comparison property model. */
	private final SettingsModelString m_property = AbstractFvvalIfSwitchNodeDialog
			.createCompValModel();

	/** The comparison operator. */
	private final SettingsModelString m_Comp = AbstractFvvalIfSwitchNodeDialog
			.createComparatorSelectionModel();

	/** The ignore case settings model. */
	private final SettingsModelBoolean m_ignCase = AbstractFvvalIfSwitchNodeDialog
			.createIgnoreCaseModel();

	/** The ignore white space settings model. */
	private final SettingsModelBoolean m_ignWhiteSpace = AbstractFvvalIfSwitchNodeDialog
			.createIgnoreWhiteSpaceModel();

	/** The double comparison tolerance model. */
	private final SettingsModelDouble m_dblTol = AbstractFvvalIfSwitchNodeDialog
			.createDoubleToleranceModel();

	/**
	 * Constructor for the node model.
	 * 
	 * @param portType
	 *            The {@link PortType} of the node
	 */
	public AbstractFvvalIfSwitchNodeModel(PortType portType) {

		super(FlowControlHelpers.createStartInPort(portType),
				FlowControlHelpers.createStartOutPorts(portType, m_outPorts));

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
		// If the comparison is true the top port is active
		int activeOutPort = (compareVariableValue(m_fvname1.getStringValue(),
				m_Comp.getStringValue(), m_property.getStringValue())) ? 0 : 1;
		return FlowControlHelpers.createStartOutputPortObject(inData,
				m_outPorts, activeOutPort);
	}

	/**
	 * Method to perform the comparison between flow variable and value.
	 * 
	 * @param varName
	 *            The name of the {@link FlowVariable}
	 * @param comparitor
	 *            The comparison operation
	 * @param compValue
	 *            The value to be used in the comparison
	 * @return The {@link boolean} result of the comparison
	 */
	private boolean compareVariableValue(String varName, String comparitor,
			String compValue) {
		FlowVariable fvar = getAvailableFlowVariables().get(varName);
		Double comparisonValue = null;
		Integer comparisonIntValue = null;

		switch (fvar.getType()) {
		case STRING:
			// Do string comparison
			String fvarStringVal = fvar.getStringValue();
			if (m_ignCase.getBooleanValue()) {
				fvarStringVal = fvarStringVal.toLowerCase();
				compValue = compValue.toLowerCase();
			}
			if (m_ignWhiteSpace.getBooleanValue()) {
				fvarStringVal = fvarStringVal.trim();
				compValue = compValue.trim();
			}
			// Lexicographical comparison - > 0 means compValue is
			// lexicographically first (i.e. compValue > fvarStringVal)
			comparisonIntValue = fvarStringVal.compareTo(compValue);
			break;

		case DOUBLE:
			// Do Double comparison
			double fvarDblVal = fvar.getDoubleValue();
			double compDblVal = Double.parseDouble(compValue.trim());
			comparisonValue = compDblVal - fvarDblVal;
			break;

		case INTEGER:
			// Do integer comparison
			int fvarIntVal = fvar.getIntValue();
			int compIntVal = Integer.parseInt(compValue.trim());
			comparisonIntValue = compIntVal - fvarIntVal;
		}
		// Now parse the comparisonValue appropriately for the operator
		if (comparisonValue == null && comparisonIntValue == null) {
			return false;
		}

		if (fvar.getType() != FlowVariable.Type.DOUBLE) {
			if ("=".equals(comparitor) && comparisonIntValue == 0) {
				return true;
			}
			if (">".equals(comparitor) && comparisonIntValue < 0) {
				return true;
			}
			if (">=".equals(comparitor) && comparisonIntValue <= 0) {
				return true;
			}
			if ("!=".equals(comparitor) && comparisonIntValue != 0) {
				return true;
			}
			if ("<".equals(comparitor) && comparisonIntValue > 0) {
				return true;
			}
			if ("<=".equals(comparitor) && comparisonIntValue >= 0) {
				return true;
			}
		} else {
			// Do the Double comparison, accounting for the tolerance
			double tolerance = m_dblTol.getDoubleValue();
			if ("=".equals(comparitor)
					&& Math.abs(comparisonValue) <= tolerance) {
				return true;
			}
			if (">".equals(comparitor) && comparisonValue < 0.0) {
				return true;
			}
			if (">=".equals(comparitor)
					&& (comparisonValue <= 0.0 || Math.abs(comparisonValue) <= tolerance)) {
				return true;
			}
			if ("!=".equals(comparitor)
					&& Math.abs(comparisonValue) > tolerance) {
				return true;
			}
			if ("<".equals(comparitor) && comparisonValue > 0.0) {
				return true;
			}
			if ("<=".equals(comparitor)
					&& (comparisonValue >= 0.0 || Math.abs(comparisonValue) <= tolerance)) {
				return true;
			}
		}
		return false;
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
		if (getAvailableFlowVariables().get(m_fvname1.getStringValue()) == null) {
			logger.warn("No valid variable selected");
			throw new InvalidSettingsException("No valid variable selected");
		}

		// Now check a meaningful comparison is possible
		if (getAvailableFlowVariables().get(m_fvname1.getStringValue())
				.getType() != FlowVariable.Type.STRING) {

			if ("".equals(m_Comp.getStringValue())) {
				// First up, we cannot compare an empty string to a number...
				logger.warn("Cannot compare a numeric variable with an empty string");
				throw new InvalidSettingsException(
						"Cannot compare a numeric variable with an empty string");
			}
			try {
				// Check to see if we can convert the string to a numeric value
				// Double.parseDouble is more tolerant, and will handle e.g.
				// '1', '1.0'
				Double.parseDouble(m_property.getStringValue().trim());
			} catch (Exception e) {
				logger.warn("Cannot convert comparison value to numeric value for comparison");
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
					logger.warn("Remove trailing '.0' for integer comparison");
					throw new InvalidSettingsException(
							"Remove trailing '.0' for integer comparison");
				} else {
					logger.warn("Cannot convert comparison value to numeric integer value for comparison."
							+ "  Ensure there is no decimal point.");
					throw new InvalidSettingsException(
							"Cannot convert comparison value to numeric integer value for comparison."
									+ "  Ensure there is no decimal point.");
				}
			}
		}

		// Now we believe we can sensible try to perform the comparison
		// If the comparison is true the top port is active
		int activeOutPort = (compareVariableValue(m_fvname1.getStringValue(),
				m_Comp.getStringValue(), m_property.getStringValue())) ? 0 : 1;
		return FlowControlHelpers.createStartOutputPortObjectSpec(inSpecs,
				m_outPorts, activeOutPort);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_Comp.saveSettingsTo(settings);
		m_fvname1.saveSettingsTo(settings);
		m_property.saveSettingsTo(settings);
		m_dblTol.saveSettingsTo(settings);
		m_ignCase.saveSettingsTo(settings);
		m_ignWhiteSpace.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_Comp.loadSettingsFrom(settings);
		m_fvname1.loadSettingsFrom(settings);
		m_property.loadSettingsFrom(settings);
		m_dblTol.loadSettingsFrom(settings);
		m_ignCase.loadSettingsFrom(settings);
		m_ignWhiteSpace.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_Comp.validateSettings(settings);
		m_fvname1.validateSettings(settings);
		m_property.validateSettings(settings);
		m_dblTol.validateSettings(settings);
		m_ignCase.validateSettings(settings);
		m_ignWhiteSpace.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec) throws IOException,
			CanceledExecutionException {

	}

}
