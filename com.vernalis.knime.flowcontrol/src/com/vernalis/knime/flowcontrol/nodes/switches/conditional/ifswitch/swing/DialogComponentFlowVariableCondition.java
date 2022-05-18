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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.FlowVariableListCellRenderer;
import org.knime.core.node.util.FlowVariableListCellRenderer.FlowVariableCell;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Scope;

import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarCondition;
import com.vernalis.knime.flowcontrol.flowvarcond.FlowVarConditionRegistry;
import com.vernalis.knime.flowcontrol.flowvarcond.compwrapper.ComponentWrapper;
import com.vernalis.knime.flowcontrol.nodes.switches.conditional.ifswitch.swing.DialogComponentFlowVariableCondition.FlowVariableConditionCellRenderer.FlowVariableConditionCell;

/**
 * A DialogComponent implementation which allows the setting of an individual
 * flow variable condition.
 * 
 * <p>
 * The component comprises:
 * <ul>
 * <li>A checkbox to change the output port to flow variable instead of matching
 * the input port type</li>
 * <li>A dropdown to select the variable to test. Only variables of types with
 * conditions registered in the extension point will be present in the
 * dropdown</li>
 * <li>A checkbox 'NOT' to invert the comparison result</li>
 * <li>A dropdown to select the condition. This will be dynamically repopulated
 * with the conditions available for the selected flow variable type</li>
 * <li>A Box ('referenceValueBox') which contains any components for the
 * reference value(s) - maybe empty</li>
 * <li>A Box ('optionValueBox') which contains any components for comparison
 * settings - maybe empty</li>
 * </ul>
 * </p>
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class DialogComponentFlowVariableCondition extends DialogComponent {

	private static final int ENTRY_COMPONENT_MAXWIDTH = 300;
	private static final int BOX_SPACING = 10;
	private final JComboBox<FlowVariableCell> varNameChooser;
	private final ItemListener varNameListener;
	private final Supplier<Map<String, FlowVariable>> availableFlowVarsSupplier;
	private final Set<Scope> scopes;
	private final Predicate<FlowVariable> scopeFilter;

	private final JCheckBox invertCheckbox;
	private final ItemListener invertListener;

	private final JComboBox<FlowVariableConditionCell> conditionChooser;
	private final ItemListener conditionListener;

	private final Box referenceValueBox;
	private List<ComponentWrapper<?, ?, ?>> referenceComponents =
			Collections.emptyList();
	private final Box optionValueBox;

	private final JCheckBox outputVariableCheckBox;

	/**
	 * Constructor for all available flow variable scopes
	 * 
	 * @param model
	 *            The settings model
	 * @param title
	 *            The title, displayed in a border round the component
	 * @param availableFlowVarsSupplier
	 *            a supplier which will provide the current available flow
	 *            variables
	 */
	public DialogComponentFlowVariableCondition(
			SettingsModelFlowVarCondition model, String title,
			Supplier<Map<String, FlowVariable>> availableFlowVarsSupplier) {
		this(model, title, availableFlowVarsSupplier, Scope.values());
	}

	/**
	 * Full Constructor
	 * 
	 * @param model
	 *            The settings model
	 * @param title
	 *            The title, displayed in a border round the component
	 * @param availableFlowVarsSupplier
	 *            a supplier which will provide the current available flow
	 *            variables
	 * @param scopes
	 *            The scopes from which variables will be included
	 */
	public DialogComponentFlowVariableCondition(
			SettingsModelFlowVarCondition model, String title,
			Supplier<Map<String, FlowVariable>> availableFlowVarsSupplier,
			Scope... scopes) {
		super(model);
		if (scopes == null || scopes.length == 0) {
			throw new IllegalArgumentException(
					"At least one variable scope must be supplied!");
		}
		this.scopes = EnumSet.noneOf(Scope.class);
		for (Scope scope : scopes) {
			this.scopes.add(scope);
		}
		scopeFilter = x -> this.scopes.contains(x.getScope());

		this.availableFlowVarsSupplier = availableFlowVarsSupplier;

		JPanel componentPanel = getComponentPanel();

		// Handle the optional title / border
		if (title != null) {
			Border border = new EtchedBorder();
			if (!title.isEmpty()) {
				border = BorderFactory.createTitledBorder(border, title);
			}
			componentPanel.setBorder(border);
		}

		// We are going to use a grid bag layout, with the 2 rows and 6 columns:
		// @formatter:off
		//
		//  /---------------------------------------------------------------------------------------------------------\
		//  | 'Variable Outport' | 'Variable:' | Variable dropdown | 'NOT' | Condition dropdown | Reference value box |
		//  |---------------------------------------------------------------------------------------------------------|
		//  |                                 <-----------  Options Box  ----------->                                 |
		//  \---------------------------------------------------------------------------------------------------------/
		//
        // @@formatter:on
		componentPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		// A bit of space around components
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets =
				new Insets(BOX_SPACING, BOX_SPACING, BOX_SPACING, BOX_SPACING);

		// A checkbox to output as a variable
		// NB The value for this comes from the node configuration spec, not the
		// settings
		outputVariableCheckBox = new JCheckBox("Variable Outport");
		Dimension outputVariableCheckBoxSize = new Dimension(200,
				outputVariableCheckBox.getPreferredSize().height);
		outputVariableCheckBox.setPreferredSize(outputVariableCheckBoxSize);
		outputVariableCheckBox.setMinimumSize(outputVariableCheckBoxSize);

		// This strange clunkiness is to ensure a little bit of space before the
		// checkbox, as JCheckBox does strange things to the Border / Insets of
		// the component resulting in it overlapping the component etched border
		Box checkBox = new Box(BoxLayout.X_AXIS);
		checkBox.add(Box.createHorizontalStrut(BOX_SPACING));
		checkBox.add(outputVariableCheckBox);
		componentPanel.add(checkBox, gbc);
		gbc.gridx++;

		// Add a label for the variable dropdown
		componentPanel.add(new JLabel("Variable:"), gbc);
		gbc.gridx++;

		// The variable name dropdown
		varNameListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// We updated the model
					updateModel();
				}

			}
		};
		varNameChooser = new JComboBox<>();
		varNameChooser.setEditable(false);
		varNameChooser.setRenderer(new FlowVariableListCellRenderer());
		varNameChooser.setPrototypeDisplayValue(
				new FlowVariableCell("knime.workspace.and.a.bit"));
		varNameChooser.addItemListener(varNameListener);
		componentPanel.add(varNameChooser, gbc);
		gbc.gridx++;

		// 'NOT' checkbox
		invertCheckbox = new JCheckBox("NOT");
		invertListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				updateModel();
			}
		};
		invertCheckbox.addItemListener(invertListener);
		componentPanel.add(invertCheckbox, gbc);
		gbc.gridx++;

		// The conditions dropdown
		conditionListener = new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// We updated the selection
					updateModel();
				}
			}
		};
		conditionChooser = new JComboBox<>();
		conditionChooser.setEditable(false);
		conditionChooser.addItemListener(conditionListener);
		conditionChooser.setRenderer(new FlowVariableConditionCellRenderer());
		conditionChooser.setPrototypeDisplayValue(new FlowVariableConditionCell(
				FlowVarConditionRegistry.getInstance().getLongestDisplayName()
						+ "  "));
		componentPanel.add(conditionChooser, gbc);

		gbc.gridx++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(0, 0, 0, 0);

		// Finally, a box for the reference value component to go in
		referenceValueBox = new Box(BoxLayout.X_AXIS);
		referenceValueBox.setPreferredSize(new Dimension(325, 25));
		referenceValueBox.setMinimumSize(referenceValueBox.getPreferredSize());
		componentPanel.add(referenceValueBox, gbc);

		// 2nd Row in panel
		// A box 6 grid units wide for options
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 6;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		optionValueBox = new Box(BoxLayout.X_AXIS);
		optionValueBox.setPreferredSize(
				new Dimension(componentPanel.getPreferredSize().width, 45));
		optionValueBox.setMinimumSize(componentPanel.getPreferredSize());

		componentPanel.add(optionValueBox, gbc);
		componentPanel.setMinimumSize(componentPanel.getPreferredSize());

		((SettingsModelFlowVarCondition) getModel())
				.prependChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {
						updateComponent();

					}
				});
		// Finally update the component to match the model
		updateComponent();
	}

	/**
	 * Method to add a listener to the 'Output Variable' checkbox
	 * 
	 * @param l
	 *            the listener
	 */
	public void addOutputVariableListener(ItemListener l) {
		outputVariableCheckBox.addItemListener(l);
	}

	/**
	 * @return whether the 'Output Variable' checkbox selected ({@code true}) or
	 *         not ({@code false})
	 */
	public boolean isOutputVariable() {
		return outputVariableCheckBox.isSelected();
	}

	/**
	 * Set the 'Output Variable' checkbox stated
	 * 
	 * @param isOutputVariable
	 *            the state
	 */
	public void setOutputVariable(boolean isOutputVariable) {
		outputVariableCheckBox.setSelected(isOutputVariable);
	}

	/**
	 * Set the visibility of the 'Output Variable' checkbox
	 * 
	 * @param isHidden
	 *            {@code true} if the checkbox should be hidden
	 */
	public void hideOutputVariable(boolean isHidden) {
		outputVariableCheckBox.setVisible(!isHidden);
	}

	/**
	 * @return A map of the currently available flow variables which are also
	 *         within the defined scope(s)
	 */
	private Map<String, FlowVariable> getInScopeAvailableVariables() {
		return availableFlowVarsSupplier.get().entrySet().stream()
				.filter(ent -> scopeFilter.test(ent.getValue()))
				.collect(Collectors.toMap(ent -> ent.getKey(),
						ent -> ent.getValue()));
	}

	/**
	 * Update the settings model to reflect the state of the dialog component
	 */
	protected void updateModel() {
		// Transfer all the settings to the model
		SettingsModelFlowVarCondition model =
				(SettingsModelFlowVarCondition) getModel();
		String varName = getSelectedVariableName();
		boolean isInverted = invertCheckbox.isSelected();
		FlowVarCondition<?> condition =
				conditionChooser.getSelectedItem() == null ? null
						: ((FlowVariableConditionCell) conditionChooser
								.getSelectedItem()).getCondition();
		model.setValue(varName, isInverted, condition, referenceComponents);
	}

	/**
	 * @return
	 */
	private String getSelectedVariableName() {
		return varNameChooser.getSelectedItem() == null ? null
				: ((FlowVariableCell) varNameChooser.getSelectedItem())
						.getName();
	}

	@Override
	protected void updateComponent() {
		// Rather than call the individual 'update'models, here we turn off all
		// the listeners, and make the components display whatever the model
		// contains. We might need to add a 'dummy' vairable name entry and
		// condition
		// if those arent currently matched to preserve the settings for when
		// the flow variables that are available updates at execution
		varNameChooser.removeItemListener(varNameListener);
		invertCheckbox.removeItemListener(invertListener);
		conditionChooser.removeItemListener(conditionListener);
		boolean updateModel = false;

		SettingsModelFlowVarCondition model =
				(SettingsModelFlowVarCondition) getModel();
		Map<String, FlowVariable> vars = getInScopeAvailableVariables();

		// Update the variable dropdown
		String varName = model.getVariableName();
		FlowVariable selectedVariable = null;
		varNameChooser.removeAllItems();
		for (Entry<String, FlowVariable> variable : vars.entrySet()) {
			final FlowVariableCell fvc =
					new FlowVariableCell(variable.getValue());
			varNameChooser.addItem(fvc);
			if (selectedVariable == null && variable.getKey().equals(varName)) {
				varNameChooser.setSelectedItem(fvc);
				selectedVariable = variable.getValue();
			}
		}

		// Add a dummy if we need to to preserve the settings when no vars are
		// available
		if (selectedVariable == null) {
			if (varName != null && !varName.isEmpty()) {
				// Add an 'invalid' flow variable
				FlowVariableCell fvc = new FlowVariableCell(varName);
				varNameChooser.addItem(fvc);
				varNameChooser.setSelectedItem(fvc);
			} else {
				// No variable selected
				if (varNameChooser.getItemCount() > 0) {
					varNameChooser.setSelectedIndex(0);
					selectedVariable = ((FlowVariableCell) varNameChooser
							.getSelectedItem()).getFlowVariable();
				} else {
					varNameChooser.setSelectedIndex(-1);
				}
			}
		}

		updateModel |= (selectedVariable == null && varName != null)
				|| (selectedVariable != null
						&& !selectedVariable.getName().equals(varName));

		// Simple - the 'NOT' checkbox
		invertCheckbox.setSelected(model.isInverted());

		// Now the condition chooser
		List<FlowVarCondition<?>> conds = selectedVariable == null ? null
				: FlowVarConditionRegistry.getInstance()
						.getConditions(selectedVariable.getVariableType());
		String condName = model.getConditionName();
		FlowVarCondition<?> selectedCondition = null;
		conditionChooser.removeAllItems();

		if (conds != null) {
			for (FlowVarCondition<?> cond : conds) {
				FlowVariableConditionCell fvcc =
						new FlowVariableConditionCell(cond);
				conditionChooser.addItem(fvcc);
				if (selectedCondition == null
						&& cond.getDisplayName().equals(condName)) {
					conditionChooser.setSelectedItem(fvcc);
					selectedCondition = cond;
				}
			}
		}

		// Again, we need to add an 'invalid' dummy condition to preserve the
		// settings if no vars are available, or the condition wasnt found for
		// some reason
		if (selectedCondition == null) {
			if (condName != null && !condName.isEmpty()) {
				FlowVariableConditionCell fvcc =
						new FlowVariableConditionCell(condName);
				conditionChooser.addItem(fvcc);
				conditionChooser.setSelectedItem(fvcc);
			} else {
				// No selection
				if (conditionChooser.getItemCount() > 0) {
					conditionChooser.setSelectedIndex(0);
					selectedCondition =
							((FlowVariableConditionCell) conditionChooser
									.getSelectedItem()).getCondition();
				} else {
					conditionChooser.setSelectedIndex(-1);
				}
			}
		}

		updateModel |= (selectedCondition == null && condName != null)
				|| (selectedCondition != null && !selectedCondition
						.getDisplayName().equals(condName));

		// Finally, the reference value
		if (selectedCondition == null) {
			// We can only actually display anything here if we have a valid
			// variable and condition selection
			// However, we dont change the model or component so that it can be
			// restored valid later
			for (Component c : referenceValueBox.getComponents()) {
				c.setEnabled(false);
			}
			if (referenceValueBox.getComponentCount() > 0) {
				referenceValueBox
						.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
			for (Component c : optionValueBox.getComponents()) {
				c.setEnabled(false);
			}
		} else {
			// Otherwise, we have a condition selected, and we have 2 possible
			// scenarios
			// 1. The model contains the same components as the condition
			// required, in which case,
			// we simply make sure they are displayed, and show the model values
			// 2. They are different from the model - in which case, we remove
			// the old ones, and use the new ones
			// Either way, we update the model to ensure it stores the same
			// objects as are displayed
			List<ComponentWrapper<?, ?, ?>> newRefComps =
					selectedCondition.getReferenceComponents();
			final boolean referenceCompsDiffers =
					referenceCompsDiffers(model, newRefComps);
			referenceComponents =
					referenceCompsDiffers ? newRefComps : model.getComponents();

			updateModel |= referenceCompsDiffers;
			referenceValueBox.removeAll();
			optionValueBox.removeAll();
			for (ComponentWrapper<?, ?, ?> comp : referenceComponents) {
				comp.setEnabled(true);
				Dimension size = comp.getComponent()
						.getPreferredSize().width > ENTRY_COMPONENT_MAXWIDTH
								? new Dimension(ENTRY_COMPONENT_MAXWIDTH,
										comp.getComponent()
												.getPreferredSize().height)
								: comp.getComponent().getPreferredSize();
				comp.getComponent().setPreferredSize(size);
				comp.getComponent().setMinimumSize(size);
				comp.getComponent().setMaximumSize(size);
				Box container =
						comp.isOption() ? optionValueBox : referenceValueBox;
				if (container.getComponentCount() > 0) {
					container.add(Box.createHorizontalStrut(BOX_SPACING));
				}
				container.add(comp.getComponent());

				comp.registerValueChangeComponentListener(e -> updateModel());
			}
			referenceValueBox.repaint();
			referenceValueBox.setBorder(BorderFactory.createEmptyBorder());
			optionValueBox.setBorder(optionValueBox.getComponentCount() == 0
					|| selectedVariable == null
							? BorderFactory.createEmptyBorder()
							: BorderFactory.createTitledBorder(
									BorderFactory.createEtchedBorder(),
									String.format("%s Compare Options",
											selectedVariable.getVariableType()
													.getIdentifier())));
		}

		// put the listeners back...
		varNameChooser.addItemListener(varNameListener);
		invertCheckbox.addItemListener(invertListener);
		conditionChooser.addItemListener(conditionListener);
		setEnabledComponents(getModel().isEnabled());

		// finally, update the model if anything changed
		if (updateModel) {
			updateModel();
		}
	}

	/**
	 * @param model
	 * @param newRefComps
	 * 
	 * @return
	 */
	private boolean referenceCompsDiffers(SettingsModelFlowVarCondition model,
			List<ComponentWrapper<?, ?, ?>> newRefComps) {
		if (model.getComponents() == null) {
			return newRefComps != null;
		}
		if (newRefComps == null) {
			return true;
		}
		if (model.getComponents().size() != newRefComps.size()) {
			return true;
		}
		for (int i = 0; i < model.getComponents().size(); i++) {
			if (!model.getComponents().get(i)
					.isSameComponent(newRefComps.get(i))) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		updateModel();

	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Should be OK

	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		varNameChooser.setEnabled(enabled);
		invertCheckbox.setEnabled(enabled);
		conditionChooser.setEnabled(enabled);
		for (ComponentWrapper<?, ?, ?> comp : referenceComponents) {
			comp.setEnabled(enabled);
		}

	}

	@Override
	public void setToolTipText(String text) {
		// We set the tooltip to the parent panel
		getComponentPanel().setToolTipText(text);
	}

	/**
	 * Cell renderer for the FlowVariable Condition
	 * 
	 * @author S.Roughley <s.roughley@vernalis.com>
	 *
	 */
	@SuppressWarnings("serial")
	static class FlowVariableConditionCellRenderer
			extends DefaultListCellRenderer {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.DefaultListCellRenderer#getListCellRendererComponent(
		 * javax.swing.JList, java.lang.Object, int, boolean, boolean)
		 */
		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index,
					isSelected, cellHasFocus);
			if (value instanceof FlowVariableConditionCell) {
				FlowVariableConditionCell fvcc =
						(FlowVariableConditionCell) value;
				setText(fvcc.getText());
				if (!fvcc.isValid()) {
					setBorder(BorderFactory.createLineBorder(Color.RED));
					super.setToolTipText(null);
				} else if (fvcc.getCondition().getDescription() != null
						&& !fvcc.getCondition().getDescription().isEmpty()) {
					super.setToolTipText(fvcc.getCondition().getDescription());
				} else {
					super.setToolTipText(null);
				}
			}
			return c;
		}

		/**
		 * A holding class for a flow variable condition to allow mapping from
		 * displayed name to actual condition
		 * 
		 * @author S.Roughley <s.roughley@vernalis.com>
		 *
		 */
		static class FlowVariableConditionCell {

			private final String text;
			private final FlowVarCondition<?> condition;

			/**
			 * Constructor with name but no condition - e.g. when a saved
			 * condition name is not present in the registry
			 * 
			 * @param text
			 *            The name
			 */
			FlowVariableConditionCell(String text) {
				this(text, null);
			}

			/**
			 * Constructor from a condition
			 * 
			 * @param condition
			 *            the condition. The name is that returned by
			 *            {@link FlowVarCondition#getDisplayName()}
			 */
			FlowVariableConditionCell(FlowVarCondition<?> condition) {
				this(condition.getDisplayName(), condition);
			}

			private FlowVariableConditionCell(String text,
					FlowVarCondition<?> condition) {
				this.text = text;
				this.condition = condition;
			}

			/**
			 * @return the text
			 */
			final String getText() {
				return text;
			}

			/**
			 * @return the valid
			 */
			final boolean isValid() {
				return condition != null;
			}

			/**
			 * @return the condition
			 */
			final FlowVarCondition<?> getCondition() {
				return condition;
			}

		}

	}

}
