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
package com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarConditionRegistry;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;

/**
 * {@link SettingsModel} for a flow variable condition, storing the condition,
 * output variable status, variable name, inversion status, and the components
 * and values for any reference values and configuration values
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class SettingsModelFlowVarCondition extends SettingsModel {

	private static final String CFGKEY_CONDITION_UID = "Condition_UID";
	private static final String CFGKEY_INVERT = "NOT";
	private static final String CFGKEY_VARIABLE = "Variable";
	private boolean invert;
	private final String key;
	private String variableName;
	private FlowVarCondition<?> condition;
	private List<ComponentWrapper<?, ?, ?>> components = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param configName
	 *            the settings key
	 */
	public SettingsModelFlowVarCondition(String configName) {
		this.key = configName;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SettingsModelFlowVarCondition createClone() {
		final SettingsModelFlowVarCondition flowVarConditionModel2 =
				new SettingsModelFlowVarCondition(key);
		List<ComponentWrapper<?, ?, ?>> comps = components.stream()
				.map(c -> c.createClone()).collect(Collectors.toList());

		flowVarConditionModel2.setValue(getVariableName(), isInverted(),
				getCondition(), comps);
		return flowVarConditionModel2;
	}

	@Override
	protected String getModelTypeID() {
		return "SMID Flow Variable Condition";
	}

	@Override
	protected String getConfigName() {
		return key;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			NodeSettingsRO mySettings =
					settings.getNodeSettings(getConfigName());
			// Get the values from the model, keeping existing where not
			// available
			String varName =
					mySettings.getString(CFGKEY_VARIABLE, variableName);
			boolean isNot = mySettings.getBoolean(CFGKEY_INVERT, invert);
			String condUID = mySettings.getString(CFGKEY_CONDITION_UID,
					condition == null ? null : condition.getUniqueID());

			// Get the components for this condition
			final FlowVarCondition<?> cond = FlowVarConditionRegistry
					.getInstance().getCondition(condUID);
			List<ComponentWrapper<?, ?, ?>> comps =
					cond == null ? Collections.emptyList()
							: cond.getReferenceComponents();
			for (ComponentWrapper<?, ?, ?> comp : comps) {
				try {
					comp.loadFromSettings(mySettings);
				} catch (InvalidSettingsException e) {
					// nothing
				}
			}
			setValue(varName, isNot, cond, comps);
		} catch (InvalidSettingsException e) {
			// Do nothing - we just keep the existing store values
		}

	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings)
			throws InvalidSettingsException {
		saveSettingsForModel(settings);

	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {
		NodeSettingsRO mySettings = settings.getNodeSettings(getConfigName());
		mySettings.getString(CFGKEY_VARIABLE);
		mySettings.getBoolean(CFGKEY_INVERT);
		String condUID = mySettings.getString(CFGKEY_CONDITION_UID);
		final FlowVarCondition<?> cond =
				FlowVarConditionRegistry.getInstance().getCondition(condUID);
		if (cond == null) {
			throw new InvalidSettingsException("Condition with UID '" + condUID
					+ "' not found in registry!");
		}
		for (ComponentWrapper<?, ?, ?> comp : cond.getReferenceComponents()) {
			comp.loadFromSettings(mySettings);
		}
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {
		NodeSettingsRO mySettings = settings.getNodeSettings(getConfigName());
		String varName = mySettings.getString(CFGKEY_VARIABLE);
		boolean isNot = mySettings.getBoolean(CFGKEY_INVERT);
		String condUID = mySettings.getString(CFGKEY_CONDITION_UID);
		final FlowVarCondition<?> cond =
				FlowVarConditionRegistry.getInstance().getCondition(condUID);
		if (cond == null) {
			throw new InvalidSettingsException("Condition with UID '" + condUID
					+ "' not found in registry!");
		}
		final List<ComponentWrapper<?, ?, ?>> comps =
				cond.getReferenceComponents();
		for (ComponentWrapper<?, ?, ?> comp : comps) {
			comp.loadFromSettings(mySettings);
		}

		setValue(varName, isNot, cond, comps);

	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		NodeSettingsWO mySettings = settings.addNodeSettings(getConfigName());
		mySettings.addString(CFGKEY_VARIABLE, getVariableName());
		mySettings.addBoolean(CFGKEY_INVERT, invert);
		mySettings.addString(CFGKEY_CONDITION_UID,
				condition == null ? null : condition.getUniqueID());
		for (ComponentWrapper<?, ?, ?> comp : components) {
			try {
				comp.saveToSettings(mySettings);
			} catch (InvalidSettingsException e) {
				// nothing
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModel#
	 * prependChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	protected void prependChangeListener(ChangeListener l) {
		// Make it visible to the corresponding component
		super.prependChangeListener(l);
	}

	@Override
	public String toString() {
		return "Flow Variable Condition Model (Var=" + variableName
				+ "; Condition=" + getConditionName() + "; Invert=" + invert
				+ "; Reference:" + components.toString() + ")";
	}

	/**
	 * @return the selected variable name
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * @return the selected condition name, or {@code null} if the condition is
	 *         null
	 */
	public String getConditionName() {
		return condition == null ? null : condition.getDisplayName();
	}

	/**
	 * @return the condition - maybe {@code null}
	 */
	private FlowVarCondition<?> getCondition() {
		return condition;
	}

	/**
	 * @return the inverted state
	 */
	public boolean isInverted() {
		return invert;
	}

	/**
	 * Method to set the value
	 * 
	 * @param varName
	 *            the variable name
	 * @param isInverted
	 *            the inverted status
	 * @param condition
	 *            the condition
	 * @param comps
	 *            the components for the condition
	 */
	public void setValue(String varName, boolean isInverted,
			FlowVarCondition<?> condition,
			List<ComponentWrapper<?, ?, ?>> comps) {
		boolean changed = false;
		if (!Objects.equals(varName, getVariableName())) {
			this.variableName = varName;
			changed = true;
		}
		if (isInverted != invert) {
			this.invert = isInverted;
			changed = true;
		}
		if (!Objects.equals(condition, this.condition)) {
			this.condition = condition;
			changed = true;
		}
		if (!Objects.equals(comps, this.components)) {
			this.components.clear();
			this.components.addAll(comps);
			changed = true;
		}
		if (changed) {
			notifyChangeListeners();
		}
	}

	/**
	 * Method to set the variable name, leaving all other settings unchanged
	 * 
	 * @param varName
	 *            the new variable name
	 */
	public void setVariableName(String varName) {
		setValue(varName, isInverted(), getCondition(), getComponents());
	}

	/**
	 * @return an unmodifiable copy of the components list
	 */
	public List<ComponentWrapper<?, ?, ?>> getComponents() {
		return Collections.unmodifiableList(components);
	}

	/**
	 * Method to set the inversion status, leaving all other settings unchanged
	 * 
	 * @param invert
	 *            the new inversion status
	 */
	public void setInvert(boolean invert) {
		setValue(getVariableName(), invert, getCondition(), getComponents());
	}

	/**
	 * Method to set the condition, leaving all other settings unchanged
	 * 
	 * @param condition
	 *            the new condition
	 */
	public void setCondition(FlowVarCondition<?> condition) {
		setValue(getVariableName(), isInverted(), condition, getComponents());
	}

	/**
	 * Method to set the components, leaving all other settings unchanged
	 * 
	 * @param comps
	 *            the components
	 */
	public void setComponents(List<ComponentWrapper<?, ?, ?>> comps) {
		setValue(getVariableName(), isInverted(), getCondition(), comps);
	}

}
