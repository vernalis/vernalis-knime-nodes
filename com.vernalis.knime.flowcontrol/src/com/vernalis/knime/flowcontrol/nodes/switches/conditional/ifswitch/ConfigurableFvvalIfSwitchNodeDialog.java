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
package com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory.ConfigurableNodeDialog;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.context.ModifiableNodeCreationConfiguration;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.context.ports.ExchangeablePortGroup;
import org.knime.core.node.context.ports.ExtendablePortGroup;
import org.knime.core.node.context.ports.ModifiablePortsConfiguration;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import com.vernalis.knime.dialog.components.DialogComponentGroup;
import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarConditionRegistry;
import com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.DialogComponentFlowVariableCondition;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.SettingsModelFlowVarCondition;

/**
 * The shared IF Switch NodeDialog
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 */
public class ConfigurableFvvalIfSwitchNodeDialog extends DefaultNodeSettingsPane
		implements ItemListener, ChangeListener, ConfigurableNodeDialog {

	private ModifiableNodeCreationConfiguration config;
	private boolean configChanged = false;
	private final List<DialogComponentFlowVariableCondition> fvCondDiaCs =
			new ArrayList<>();

	private SettingsModelBoolean elseFlowVarMdl;
	private DialogComponentGroup dcg;

	/**
	 * Instantiates a new node dialog.
	 * 
	 * @param config
	 *            The node configuration with the in and out ports and types
	 */
	public ConfigurableFvvalIfSwitchNodeDialog(
			NodeCreationConfiguration config) {
		super();
		this.config = (ModifiableNodeCreationConfiguration) config;
		PortsConfiguration portConfig = this.config.getPortConfig()
				.orElseThrow(() -> new IllegalArgumentException(
						"A NodeCreationConfiguration with Port Config is required"));
		final int numConditions = portConfig.getOutputPorts().length - 1;

		for (int i = 0; i < numConditions; i++) {
			final DialogComponentFlowVariableCondition diaC =
					new DialogComponentFlowVariableCondition(createCondModel(i),
							(i == 0 ? "IF" : "Else IF") + " (Output port " + i
									+ ")",
							() -> getAvailableFlowVariables(
									FlowVarConditionRegistry.getInstance()
											.getAvailableFilterVariableTypes()));
			diaC.addOutputVariableListener(this);
			fvCondDiaCs.add(diaC);
			if (dcg == null) {
				dcg = new DialogComponentGroup(this, null/* No border */,
						OptionalInt.empty()/* Preserve the orientation */,
						diaC);
			} else {
				dcg.addComponent(diaC);
			}
		}

		// We dont use this SM in the NodeModel
		elseFlowVarMdl = new SettingsModelBoolean("Else FlowVar", false);
		elseFlowVarMdl.addChangeListener(this);
		addDialogComponent(new DialogComponentBoolean(elseFlowVarMdl,
				"ELSE Variable Outport"));

	}

	/**
	 * @param i
	 *            The index of the condition (corresponds to output port)
	 * 
	 * @return model for the Condition (i) setting
	 *
	 * @since 17-May-2022
	 */
	static SettingsModelFlowVarCondition createCondModel(int i) {
		return new SettingsModelFlowVarCondition("Condition " + i);
	}

	@Override
	public void setCurrentNodeCreationConfiguration(
			ModifiableNodeCreationConfiguration config) {
		this.config = config;

	}

	@Override
	public Optional<ModifiableNodeCreationConfiguration>
			getNewNodeCreationConfiguration() {
		if (configChanged) {
			updateNodeCreationConfig();
		}
		return Optional.ofNullable(configChanged ? config : null);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		configChanged = true;

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		configChanged = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		updateConfigComponents();
		configChanged = false;
		super.loadAdditionalSettingsFrom(settings, specs);
	}

	/**
	 * Make sure the Node Config matches the settings models
	 */
	private void updateNodeCreationConfig() {

		final ModifiablePortsConfiguration portsConfig =
				config.getPortConfig().orElse(null);
		if (portsConfig == null) {
			return;
		}

		// The first output
		ExchangeablePortGroup outputPortGroup =
				(ExchangeablePortGroup) portsConfig
						.getGroup(ConfigurableSwitchNodeConstants.OUTPUT_GROUP);
		outputPortGroup
				.setSelectedPortType(fvCondDiaCs.get(0).isOutputVariable()
						? FlowVariablePortObject.TYPE
						: BufferedDataTable.TYPE);

		// The intermediates...
		ExtendablePortGroup moreOutputsGroup = (ExtendablePortGroup) portsConfig
				.getGroup(ConfigurableSwitchNodeConstants.MORE_OUTPUTS_GROUP);
		while (moreOutputsGroup.hasConfiguredPorts()) {
			moreOutputsGroup.removeLastPort();
		}
		for (int i = 1; i < fvCondDiaCs.size(); i++) {
			moreOutputsGroup.addPort(fvCondDiaCs.get(i).isOutputVariable()
					? FlowVariablePortObject.TYPE
					: BufferedDataTable.TYPE);
		}

		// Now need the last 'Else' which comes from a SettingsModelBoolean
		ExchangeablePortGroup elsePortGroup =
				(ExchangeablePortGroup) portsConfig.getGroup(
						ConfigurableSwitchNodeConstants.ELSE_OUTPUT_GROUP);
		elsePortGroup.setSelectedPortType(
				elseFlowVarMdl.getBooleanValue() ? FlowVariablePortObject.TYPE
						: BufferedDataTable.TYPE);
	}

	/**
	 * Here we ensure that the models reflect the port config
	 */
	private void updateConfigComponents() {
		ModifiablePortsConfiguration portConfig =
				config.getPortConfig().orElse(null);
		if (portConfig == null) {
			return;
		}
		PortType[] outTypes = portConfig.getOutputPorts();
		for (int i = 0; i < outTypes.length - 1; i++) {
			fvCondDiaCs.get(i).setOutputVariable(
					outTypes[i].equals(FlowVariablePortObject.TYPE));
		}
		elseFlowVarMdl.setBooleanValue(outTypes[outTypes.length - 1]
				.equals(FlowVariablePortObject.TYPE));
	}

}
