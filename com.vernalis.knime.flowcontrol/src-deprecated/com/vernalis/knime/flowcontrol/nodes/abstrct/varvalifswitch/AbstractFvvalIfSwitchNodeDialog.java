/*******************************************************************************
 * Copyright (c) 2014, 2017, Vernalis (R&D) Ltd
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

import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

/**
 * The shared IF Switch NodeDialog
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 */
@Deprecated(since="1.32.0")
public class AbstractFvvalIfSwitchNodeDialog extends DefaultNodeSettingsPane {

	/** The logger instance */
	private static final NodeLogger LOGGER =
			NodeLogger.getLogger(AbstractFvvalIfSwitchNodeModel2.class);

	/** The the flow variable name model. */
	private SettingsModelString m_fvname1;

	/** The ignore whitespace and ignore case models. */
	private SettingsModelBoolean m_ignCase, m_ignWhiteSpace;

	/** The double comparison tolerance model */
	private SettingsModelDouble m_dblTol;

	/** The the flow variable name selection dialog component */
	private DialogComponentFlowVariableNameSelection m_fvs1;

	/**
	 * Instantiates a new node dialog.
	 */
	public AbstractFvvalIfSwitchNodeDialog() {
		super();
		setHorizontalPlacement(true);

		m_fvname1 = createFlowVarSelectionModel();
		m_fvs1 = new DialogComponentFlowVariableNameSelection(m_fvname1, "",
				getAvailableFlowVariables().values(), FlowVariable.Type.DOUBLE,
				FlowVariable.Type.INTEGER, FlowVariable.Type.STRING);

		m_ignCase = createIgnoreCaseModel();
		m_ignWhiteSpace = createIgnoreWhiteSpaceModel();
		m_dblTol = createDoubleToleranceModel();

		m_ignCase.setEnabled(false);
		m_ignWhiteSpace.setEnabled(false);
		m_dblTol.setEnabled(false);

		m_fvname1.addChangeListener(new ChangeListener() {

			// Dialog doesnt allow selection of missing type
			@SuppressWarnings("incomplete-switch")
			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					switch (getAvailableFlowVariables().get(m_fvname1.getStringValue()).getType()) {
					case STRING:
						m_ignCase.setEnabled(true);
						m_ignWhiteSpace.setEnabled(true);
						m_dblTol.setEnabled(false);
						break;
					case DOUBLE:
						m_ignCase.setEnabled(false);
						m_ignWhiteSpace.setEnabled(false);
						m_dblTol.setEnabled(true);
						break;
					case INTEGER:
						m_ignCase.setEnabled(false);
						m_ignWhiteSpace.setEnabled(false);
						m_dblTol.setEnabled(false);
					}
				} catch (Exception e1) {
					// No recognised flow variable selected
					m_ignCase.setEnabled(false);
					m_ignWhiteSpace.setEnabled(false);
					m_dblTol.setEnabled(false);
				}
			}
		});
		addDialogComponent(m_fvs1);

		addDialogComponent(new DialogComponentStringSelection(createComparatorSelectionModel(), "",
				new String[] { "=", "<", "<=", ">", ">=", "!=" }));

		addDialogComponent(new DialogComponentString(createCompValModel(), ""));

		createNewGroup("String Comparison settings");
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(m_ignCase, "Ignore case"));
		addDialogComponent(
				new DialogComponentBoolean(m_ignWhiteSpace, "Ignore leading/trailing whitespace"));

		createNewGroup("Double Comparison settings");
		addDialogComponent(new DialogComponentNumberEdit(m_dblTol, "Equality tolerance:"));
	}

	/**
	 * Method to create settings model containing flow variable.
	 * 
	 * @return The settings model
	 */
	static SettingsModelString createFlowVarSelectionModel() {
		return new SettingsModelString("Flow Variable", null);
	}

	/**
	 * Method to create settings model for comparison value.
	 * 
	 * @return The settings model
	 */
	static SettingsModelString createCompValModel() {
		return new SettingsModelString("Comparison Value", "");
	}

	/**
	 * Method to create settings model for ignore case.
	 * 
	 * @return The settings model
	 */
	static SettingsModelBoolean createIgnoreCaseModel() {
		return new SettingsModelBoolean("Ignore Case", true);
	}

	/**
	 * Methods to create settings model for ignore whitespace.
	 * 
	 * @return The settings model
	 */
	static SettingsModelBoolean createIgnoreWhiteSpaceModel() {
		return new SettingsModelBoolean("Ignore Whitespace", true);
	}

	/**
	 * Method to create settings model for double tolerance.
	 * 
	 * @return The settings model
	 */
	static SettingsModelDouble createDoubleToleranceModel() {
		return new SettingsModelDouble("Double comparison tolerance", 0.00001);
	}

	/**
	 * Method to create settings model for the comparison operator.
	 * 
	 * @return The settings model
	 */
	static SettingsModelString createComparatorSelectionModel() {
		return new SettingsModelString("Comparator", "=");
	}

	/**
	 * List of available string flow variables must be updated since it could
	 * have changed.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();

		// check for selected value
		String flowVar = "";
		try {
			flowVar = ((SettingsModelString) m_fvname1.createCloneWithValidatedValue(settings))
					.getStringValue();
		} catch (InvalidSettingsException e) {
			LOGGER.debug("Settings model could not be cloned with given " + "settings!");
		} finally {
			m_fvs1.replaceListItems(flowVars.values(), flowVar);
		}
	}
}
