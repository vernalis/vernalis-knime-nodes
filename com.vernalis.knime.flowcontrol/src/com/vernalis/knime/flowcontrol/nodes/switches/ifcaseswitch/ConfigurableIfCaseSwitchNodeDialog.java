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
package com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch;

import java.util.Map;
import java.util.Optional;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ConfigurableNodeFactory.ConfigurableNodeDialog;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.context.ModifiableNodeCreationConfiguration;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.context.ports.ExchangeablePortGroup;
import org.knime.core.node.context.ports.ExtendablePortGroup;
import org.knime.core.node.context.ports.ModifiablePortsConfiguration;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentFlowVariableNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.workflow.FlowVariable;

import static com.vernalis.knime.flowcontrol.nodes.switches.ConfigurableSwitchNodeConstants.MORE_OUTPUTS_GROUP;

/**
 * Node Dialog for the Configurable IF/Case switch node
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 */
@SuppressWarnings("deprecation")
public class ConfigurableIfCaseSwitchNodeDialog extends DefaultNodeSettingsPane
		implements ChangeListener, ConfigurableNodeDialog {

	private static final String COUNT_NEGATIVE_VALUES_FROM_END =
			"Count negative values from end";

	private static final String OUT_OF_BOUNDS_BEHAVIOUR =
			"Out-of-bounds Behaviour";

	private static final String VARIABLE = "Variable";

	/** The logger instance */
	private static final NodeLogger LOGGER =
			NodeLogger.getLogger(ConfigurableIfCaseSwitchNodeDialog.class);

	/** First Output */
	static final String FIRST_OUTPUT = "First Output";

	/** Second Output */
	static final String SECOND_OUTPUT = "Second Output";

	private ModifiableNodeCreationConfiguration config;
	private boolean configChanged = false;

	private SettingsModelBoolean[] fVarOutMdls;

	private final SettingsModelString fVarNameMdl;

	private final DialogComponentFlowVariableNameSelection fVarSelector;

	/**
	 * Instantiates a new node dialog.
	 * 
	 * @param config
	 *            the node creation configuration
	 */
	public ConfigurableIfCaseSwitchNodeDialog(
			NodeCreationConfiguration config) {
		super();

		fVarNameMdl = createVarNameModel();
		fVarSelector = new DialogComponentFlowVariableNameSelection(fVarNameMdl,
				VARIABLE, getAvailableFlowVariables().values(),
				FlowVariable.Type.INTEGER);
		addDialogComponent(fVarSelector);

		addDialogComponent(
				new DialogComponentBoolean(createCountNegativeFromEndModel(),
						COUNT_NEGATIVE_VALUES_FROM_END));

		addDialogComponent(new DialogComponentButtonGroup(
				createOutOfBoundsModel(), OUT_OF_BOUNDS_BEHAVIOUR, true,
				OutOfBoundsBehaviour.values()));

		createNewGroup("Output Port Types");
		this.config = (ModifiableNodeCreationConfiguration) config;
		PortsConfiguration portConfig = this.config.getPortConfig()
				.orElseThrow(IllegalArgumentException::new);
		final int numPorts = portConfig.getOutputPorts().length;
		fVarOutMdls = new SettingsModelBoolean[numPorts];
		for (int i = 0; i < numPorts; i++) {
			fVarOutMdls[i] =
					new NoSaveSettingsModelBoolean("fVarOut_" + i, false);
			fVarOutMdls[i].addChangeListener(this);
			addDialogComponent(new DialogComponentBoolean(fVarOutMdls[i],
					"Variable output at port " + i));
		}
		closeCurrentGroup();
	}

	/**
	 * @return model for the Count negative values from end setting
	 *
	 * @since 17-May-2022
	 */
	static SettingsModelBoolean createCountNegativeFromEndModel() {
		return new SettingsModelBoolean(COUNT_NEGATIVE_VALUES_FROM_END, false);
	}

	/**
	 * A {@link SettingsModelBoolean} subclass which does not save to disc, and
	 * therefore does not allow the user to change the workflow construction
	 * form flow vars
	 * 
	 * @author S.Roughley <s.roughley@vernalis.com>
	 *
	 */
	private static class NoSaveSettingsModelBoolean
			extends SettingsModelBoolean {

		public NoSaveSettingsModelBoolean(String configName,
				boolean defaultValue) {
			super(configName, defaultValue);

		}

		@Override
		protected String getModelTypeID() {
			return "No Save " + super.getModelTypeID();
		}

		@Override
		protected void loadSettingsForDialog(NodeSettingsRO settings,
				PortObjectSpec[] specs) throws NotConfigurableException {
			// Dont store!
		}

		@Override
		protected void saveSettingsForDialog(NodeSettingsWO settings)
				throws InvalidSettingsException {
			// Dont store!
		}

		@Override
		protected void validateSettingsForModel(NodeSettingsRO settings)
				throws InvalidSettingsException {
			// Dont store!
		}

		@Override
		protected void loadSettingsForModel(NodeSettingsRO settings)
				throws InvalidSettingsException {
			// Dont store!
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.knime.core.node.defaultnodesettings.SettingsModelBoolean#
		 * saveSettingsForModel(org.knime.core.node.NodeSettingsWO)
		 */
		@Override
		protected void saveSettingsForModel(NodeSettingsWO settings) {
			// Dont store!
		}

	}

	/**
	 * @return model for the Out of bounds behaviour setting
	 *
	 * @since 17-May-2022
	 */
	static SettingsModelString createOutOfBoundsModel() {
		return new SettingsModelString(OUT_OF_BOUNDS_BEHAVIOUR,
				OutOfBoundsBehaviour.getDefault().getActionCommand());
	}

	/**
	 * @return model for the variable setting
	 *
	 * @since 17-May-2022
	 */
	static SettingsModelString createVarNameModel() {
		return new SettingsModelString(VARIABLE, null);
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
		Map<String, FlowVariable> flowVars = getAvailableFlowVariables();

		// check for selected value
		String flowVar = "";
		try {
			flowVar = ((SettingsModelString) fVarNameMdl
					.createCloneWithValidatedValue(settings)).getStringValue();
		} catch (InvalidSettingsException e) {
			LOGGER.debug("Settings model could not be cloned with given "
					+ "settings!");
		} finally {
			fVarSelector.replaceListItems(flowVars.values(), flowVar);
		}
	}

	private void updateNodeCreationConfig() {

		final ModifiablePortsConfiguration portsConfig = config.getPortConfig()
				.orElseThrow(IllegalArgumentException::new);
		// The first two exchangeable outputs
		ExchangeablePortGroup firstOutput =
				(ExchangeablePortGroup) portsConfig.getGroup(FIRST_OUTPUT);
		firstOutput.setSelectedPortType(
				fVarOutMdls[0].getBooleanValue() ? FlowVariablePortObject.TYPE
						: BufferedDataTable.TYPE);
		ExchangeablePortGroup secondOutput =
				(ExchangeablePortGroup) portsConfig.getGroup(SECOND_OUTPUT);
		secondOutput.setSelectedPortType(
				fVarOutMdls[1].getBooleanValue() ? FlowVariablePortObject.TYPE
						: BufferedDataTable.TYPE);

		// The extendable outputs
		ExtendablePortGroup moreOutputsGroup =
				(ExtendablePortGroup) portsConfig.getGroup(MORE_OUTPUTS_GROUP);
		while (moreOutputsGroup.hasConfiguredPorts()) {
			moreOutputsGroup.removeLastPort();
		}
		for (int i = 2; i < fVarOutMdls.length; i++) {
			moreOutputsGroup.addPort(fVarOutMdls[i].getBooleanValue()
					? FlowVariablePortObject.TYPE
					: BufferedDataTable.TYPE);
		}

	}

	/**
	 * Here we ensure that the
	 */
	private void updateConfigComponents() {
		ModifiablePortsConfiguration portConfig = config.getPortConfig()
				.orElseThrow(IllegalArgumentException::new);
		PortType[] outTypes = portConfig.getOutputPorts();
		for (int i = 0; i < outTypes.length; i++) {
			fVarOutMdls[i].setBooleanValue(
					outTypes[i].equals(FlowVariablePortObject.TYPE));
		}

	}

}
