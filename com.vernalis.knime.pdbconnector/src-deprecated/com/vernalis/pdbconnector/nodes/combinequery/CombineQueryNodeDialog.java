/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.pdbconnector.nodes.combinequery;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;

/**
 * Node Dialog Pane for the combine query node
 */
@Deprecated
public class CombineQueryNodeDialog extends DefaultNodeSettingsPane {

	/** The logger instance */
	private static final NodeLogger LOGGER =
			NodeLogger.getLogger(CombineQueryNodeDialog.class);
	protected DialogComponentFlowVariableNameSelection m_leftVarSelector;
	protected DialogComponentFlowVariableNameSelection m_rightVarSelector;
	protected SettingsModelString m_leftVarMdl;
	protected SettingsModelString m_rightVarMdl;

	/**
	 * Constructor for the node dialog pane
	 */
	public CombineQueryNodeDialog() {
		setHorizontalPlacement(true);
		m_leftVarMdl = createLeftVarNameModel();
		List<FlowVariable> strFlowVars = getStrFlowVars();
		m_leftVarSelector =
				new DialogComponentFlowVariableNameSelection(m_leftVarMdl,
						"First Query", strFlowVars, FlowVariable.Type.STRING);
		addDialogComponent(m_leftVarSelector);
		addDialogComponent(new DialogComponentStringSelection(
				createConjunctionModel(), "", "AND", "OR"));
		m_rightVarMdl = createRightVarNameModel();
		m_rightVarSelector =
				new DialogComponentFlowVariableNameSelection(m_rightVarMdl,
						"Second Query", strFlowVars, FlowVariable.Type.STRING);
		addDialogComponent(m_rightVarSelector);
		setHorizontalPlacement(false);
		addDialogComponent(new DialogComponentString(createQueryNameModel(),
				"Output Query Name", true, 20));
	}

	/**
	 * @return a list of the available String Flow Variables
	 */
	protected List<FlowVariable> getStrFlowVars() {
		List<FlowVariable> strFlowVars = new ArrayList<>();
		for (FlowVariable fVar : getAvailableFlowVariables().values()) {
			if (!fVar.isGlobalConstant()
					&& fVar.getType() == FlowVariable.Type.STRING) {
				strFlowVars.add(fVar);
			}
		}
		return strFlowVars;
	}

	/**
	 * @return A settings model for the name of the flow variable for the
	 *         combined query
	 */
	static SettingsModelString createQueryNameModel() {
		return new SettingsModelString("Query Output Name", "xmlQuery");
	}

	/**
	 * @return A settings model for the query conjunction
	 */
	static SettingsModelString createConjunctionModel() {
		return new SettingsModelString("Conjuction", "AND");
	}

	/**
	 * @return A settings model for the 'Left' query variable name
	 */
	static SettingsModelString createLeftVarNameModel() {
		return new SettingsModelString("First Query variable", null);
	}

	/**
	 * @return A settings model for the 'Right' query variable name
	 */
	static SettingsModelString createRightVarNameModel() {
		return new SettingsModelString("Second Query variable", null);
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
		List<FlowVariable> flowVars = getStrFlowVars();

		// check for selected value
		String lVar = "";
		String rVar = "";
		try {
			// Try to maintain the selected values
			lVar = ((SettingsModelString) m_leftVarMdl
					.createCloneWithValidatedValue(settings)).getStringValue();
			rVar = ((SettingsModelString) m_rightVarMdl
					.createCloneWithValidatedValue(settings)).getStringValue();
		} catch (InvalidSettingsException e) {
			LOGGER.debug("Settings models could not be cloned with given "
					+ "settings!");
		} finally {
			m_leftVarSelector.replaceListItems(flowVars, lVar);
			m_rightVarSelector.replaceListItems(flowVars, rVar);
		}
	}
}
