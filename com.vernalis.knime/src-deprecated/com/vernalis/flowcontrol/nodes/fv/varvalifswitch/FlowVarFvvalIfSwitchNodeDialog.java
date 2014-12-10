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
package com.vernalis.flowcontrol.nodes.fv.varvalifswitch;

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

public class FlowVarFvvalIfSwitchNodeDialog extends DefaultNodeSettingsPane {
	private static final NodeLogger LOGGER = NodeLogger
			.getLogger(FlowVarFvvalIfSwitchNodeModel.class);
	private SettingsModelString m_fvname1;
	private SettingsModelBoolean m_ignCase, m_ignWhiteSpace;
	private SettingsModelDouble m_dblTol;
	private DialogComponentFlowVariableNameSelection m_fvs1;

	protected FlowVarFvvalIfSwitchNodeDialog() {
		super();
		setHorizontalPlacement(true);

		m_fvname1 = createFirstFlowVarSelectionModel();
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

			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					switch (getAvailableFlowVariables().get(
							m_fvname1.getStringValue()).getType()) {
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

		addDialogComponent(new DialogComponentStringSelection(
				createComparatorSelectionModel(), "", new String[] { "=", "<",
						"<=", ">", ">=", "!=" }));

		addDialogComponent(new DialogComponentString(createPropertyModel(), ""));

		createNewGroup("String Comparison settings");
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(m_ignCase, "Ignore case"));
		addDialogComponent(new DialogComponentBoolean(m_ignWhiteSpace,
				"Ignore leading/trailing whitespace"));

		createNewGroup("Double Comparison settings");
		addDialogComponent(new DialogComponentNumberEdit(m_dblTol,
				"Equality tolerance:"));
	}

	static SettingsModelString createFirstFlowVarSelectionModel() {
		return new SettingsModelString("First Flow Variable", null);
	}

	static SettingsModelString createPropertyModel() {
		return new SettingsModelString("Property", "");
	}

	static SettingsModelBoolean createIgnoreCaseModel() {
		return new SettingsModelBoolean("Ignore Case", true);
	}

	static SettingsModelDouble createDoubleToleranceModel() {
		return new SettingsModelDouble("Double comparison tolerance", 0.00001);
	}

	static SettingsModelBoolean createIgnoreWhiteSpaceModel() {
		return new SettingsModelBoolean("Ignore Whitespace", true);
	}

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
			flowVar = ((SettingsModelString) m_fvname1
					.createCloneWithValidatedValue(settings)).getStringValue();
		} catch (InvalidSettingsException e) {
			LOGGER.debug("Settings model could not be cloned with given "
					+ "settings!");
		} finally {
			m_fvs1.replaceListItems(flowVars.values(), flowVar);
		}
	}
}
