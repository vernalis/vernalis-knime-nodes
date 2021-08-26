/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query.text.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector2.dialogcomponents.AbstractSettingsModelRangeBounded;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentNestedDropdown;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelDateBounded;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelDateRangeBounded;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelDoubleRangeBounded;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelIntegerRangeBounded;
import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;
import com.vernalis.pdbconnector2.dialogcomponents.swing.RemoveMeButton;
import com.vernalis.pdbconnector2.dialogcomponents.swing.SwingUtils;
import com.vernalis.pdbconnector2.query.QueryPanel;
import com.vernalis.pdbconnector2.query.RemovableQueryPanel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel.QueryFieldEventType;
import com.vernalis.pdbconnector2.query.text.fields.QueryFieldRegistry;

/**
 * A Swing {@link Box} implementation of {@link QueryPanel} to allow entering a
 * query field, the smallest unit of a text query. A field is always part of a
 * parent Query Group. The panel comprises:
 * <ul>
 * <li>A 'X' button to remove the field from the parent group</li>
 * <li>A nested dropdown menu to select the field type</li>
 * <li>Optionally, an operator dropdown for the match type</li>
 * <li>Optionally, an inversion 'NOT'operator to invert the match</li>
 * <li>Optionally, a value(s) input component
 * <li>
 * <li>A count/clear button pair, to count the results for the field alone, and
 * to clear the query in the field</li>
 * </ul>
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldPanel extends Box implements ChangeListener,
		RemovableQueryPanel<QueryFieldModel, QueryGroupPanel> {

	private static final Color BACKGROUND = new JFrame().getBackground();

	private static final int OPERATOR_MAX_WIDTH =
			new DialogComponentStringSelection(
					new SettingsModelString("test",
							QueryFieldOperator.getDefault().getDisplayName()),
					"",
					Arrays.stream(QueryFieldOperator.values())
							.map(x -> x.getDisplayName())
							.collect(Collectors.toList())).getComponentPanel()
									.getPreferredSize().width;

	private static final long serialVersionUID = 1L;
	private final QueryGroupPanel parent;
	private final QueryFieldModel model;

	private final QueryFieldRegistry fieldRegistry =
			QueryFieldRegistry.getInstance();

	private final JPanel operatorPanel = new JPanel(new GridBagLayout());
	private final Box queryFieldValuesBox = new Box(BoxLayout.X_AXIS);

	private final DialogComponentNestedDropdown queryTypeChooser;
	private final DialogComponentStringSelection operatorChooser;
	private final CountClearButtonBox countButton;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            The model for the field
	 * @param parent
	 *            The parent Query Group
	 */
	public QueryFieldPanel(QueryFieldModel model, QueryGroupPanel parent) {
		super(BoxLayout.X_AXIS);
		// Because we are in a white background potentially, we need to set
		// these:
		setOpaque(true);
		setBackground(BACKGROUND);

		this.parent = parent;
		this.model = model;
		this.model.addChangeListener(this);
		resetBorder();

		// 'X' button to remove query
		add(new RemoveMeButton(this));

		// The query type chooser
		queryTypeChooser = DialogComponentNestedDropdown.fromMenuActions(
				this.model.getQueryTypeModel(), "Query Type",
				fieldRegistry.getActionMap());
		add(SwingUtils.forceToPreferredSize(queryTypeChooser));

		// Operator chooser
		add(operatorPanel);
		final GridBagConstraints opConstraint = new GridBagConstraints();
		opConstraint.anchor = GridBagConstraints.WEST;
		opConstraint.fill = GridBagConstraints.HORIZONTAL;
		opConstraint.weightx = 1.0;
		opConstraint.gridx = 0;
		opConstraint.gridy = 0;
		final Set<QueryFieldOperator> operators =
				model.getQueryField().getOperators();
		operatorChooser = new DialogComponentStringSelection(
				this.model.getQueryOperatorModel(), null,
				operators == null
						? Collections.singleton(QueryFieldOperator.getDefault()
								.getDisplayName())
						: operators.stream().map(x -> x.getDisplayName())
								.collect(Collectors.toList()));

		operatorPanel.add(SwingUtils.forceToPreferredSize(operatorChooser),
				opConstraint);

		opConstraint.gridy++;
		operatorPanel.add(
				SwingUtils.forceToPreferredSize(
						new DialogComponentBoolean(model.getIsNotMdl(), "NOT")),
				opConstraint);
		SwingUtils.forceToSize(operatorPanel,
				new Dimension(
						Math.max(OPERATOR_MAX_WIDTH,
								operatorPanel.getPreferredSize().width),
						operatorPanel.getPreferredSize().height));
		if (operators == null) {
			hideOperator();
		} else {
			showOperator();
		}

		// Initialise the Query Field Values input from the model
		add(queryFieldValuesBox);

		countButton = new CountClearButtonBox(this, true);
		add(countButton);
		// add(createHorizontalStrut(5));

		setFieldValueComponent(model.getQueryField().getDialogComponent(
				model.getOperator(), model.getQueryFieldValueModel()));

		SwingUtils.forceToSize(this,
				new Dimension(600, this.getPreferredSize().height));
		SwingUtils.keepHeightFillWidth(this);
		// }
		resetCountClearButtons();

	}

	/**
	 * @return The parent {@link QueryGroupPanel} to which the field belongs
	 */
	@Override
	public QueryGroupPanel getParentGroup() {
		return parent;
	}

	@Override
	public QueryFieldModel getQueryModel() {
		return model;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// This is called when there is a change to the query type - NOT when
		// there is a
		// change to the operator
		// The query type has changed - we always reset the option field value
		// component
		// and model
		// model submodels need updated
		// Operator chooser needs changed
		// Query Value components / models need updated

		boolean isTypeChange = false;
		switch ((QueryFieldEventType) e.getSource()) {
			case QueryTypeChange:
				// We need to flag that this is a new query - so the field value
				// model uses
				// defaults for the new field type
				isTypeChange = true;
				// And set the operators accordingly
				setOperatorsDropdownValuesForCurrentField();
				// And keep going...
			case OperatorChange:
				// Need to check the field value component/model is still
				// correct
				// Behaviour depends on whether we have a new field type or
				// not...
				setQueryFieldValuesForCurrentField(isTypeChange);
				// And keep going...
			case FieldValueModelChange:
				// FVMC Can only be made to happen via operator change so we
				// shouldn't ever be
				// here directly...
				setQueryFieldValuesEnabled(
						model.getOperator() != QueryFieldOperator.exists);
				// And keep going...
			case InversionChange:
			case FieldValueChange:
				// The query has changed so counting is possible again
				// This is automatically handled
				// by the
				// CountClearButtonBox which adds a
				// listener to the model and resets itself on change
				// resetCountClearButtons();
				break;

			// We dont need to do anything else here
		}

	}

	private void setQueryFieldValuesForCurrentField(boolean isFieldChanged) {

		// Start by getting the existing field value model and the possible new
		// one
		final SettingsModel oldModel = model.getQueryFieldValueModel();
		// The default operator is never exists, so this is safe - otherwise
		// there would
		// be a potential route into transferring values between query types via
		// an
		// exists operator
		final SettingsModel newModel =
				model.getOperator() == QueryFieldOperator.exists ? oldModel
						: model.getQueryField()
								.getValueSettingsModel(model.getOperator());

		if (isFieldChanged) {
			// We discard the old model completely
			final DialogComponent newDiaC = model.getQueryField()
					.getDialogComponent(model.getOperator(), newModel);
			setFieldValueComponent(newDiaC);
			return;
		}

		if (!valueModelChanged(newModel, oldModel)) {
			// We havent changed query field and the components dont change
			// either, so we
			// finish here..
			return;
		} else {
			// If we havent changed the query field type then we have got here
			// by changing
			// the operator and should transfer values across...
			// We will need to update the component type
			transferModelRangeValues(newModel, oldModel);
			final DialogComponent newDiaC = model.getQueryField()
					.getDialogComponent(model.getOperator(), newModel);
			setFieldValueComponent(newDiaC);
			return;
		}

	}

	private boolean valueModelChanged(final SettingsModel newModel,
			final SettingsModel oldModel) {
		if (newModel == null) {
			return oldModel != null;
		}
		if (oldModel == null) {
			return true;
		}
		return newModel.getClass() != oldModel.getClass();
	}

	private void transferModelRangeValues(final SettingsModel newModel,
			final SettingsModel oldModel) {
		// Null check not required as that forms part of instanceof checks

		if (oldModel instanceof AbstractSettingsModelRangeBounded<?>) {
			// We currently have a range, and so we keep the minimum value in
			// the new model
			if (newModel instanceof SettingsModelIntegerBounded) {
				((SettingsModelIntegerBounded) newModel).setIntValue(
						((SettingsModelIntegerRangeBounded) oldModel)
								.getLowerValue());
			} else if (newModel instanceof SettingsModelDoubleBounded) {
				((SettingsModelDoubleBounded) newModel).setDoubleValue(
						((SettingsModelDoubleRangeBounded) oldModel)
								.getLowerValue());
			} else if (newModel instanceof SettingsModelDateBounded) {
				((SettingsModelDateBounded) newModel)
						.setDate(((SettingsModelDateRangeBounded) oldModel)
								.getLowerValue());
			} else {
				assert false : "There shouldnt be any other sort of model in this case";
			}
		} else if (newModel instanceof AbstractSettingsModelRangeBounded<?>) {
			// We move to a range, so we set the lower value to the current
			// value
			if (newModel instanceof SettingsModelIntegerRangeBounded) {
				((SettingsModelIntegerRangeBounded) newModel).setLowerValue(
						((SettingsModelIntegerBounded) oldModel).getIntValue());
			} else if (newModel instanceof SettingsModelDoubleRangeBounded) {
				((SettingsModelDoubleRangeBounded) newModel)
						.setLowerValue(((SettingsModelDoubleBounded) oldModel)
								.getDoubleValue());
			} else if (newModel instanceof SettingsModelDateRangeBounded) {
				((SettingsModelDateRangeBounded) newModel).setLowerValue(
						((SettingsModelDateBounded) oldModel).getDate());
			} else {
				assert false : "There shouldnt be any other sort of model in this case";
			}
		}
		// Otherwise, either one of the models was null or neither was a range
	}

	/**
	 * Method to replace the current field value component and model
	 *
	 * @param newDiaC
	 */
	private void setFieldValueComponent(DialogComponent newDiaC) {
		queryFieldValuesBox.removeAll();
		if (newDiaC != null) {
			queryFieldValuesBox.add(SwingUtils.forceToPreferredSize(newDiaC));

			// Update the model
			model.setQueryFieldValueModel(newDiaC.getModel());
			setQueryFieldValuesEnabled(
					model.getOperator() != QueryFieldOperator.exists);
		} else {
			model.clearQueryFieldValueModel();
		}

		// Now revalidate the component
		revalidate();

	}

	private void setQueryFieldValuesEnabled(boolean enabled) {

		final SettingsModel fieldValueModel = model.getQueryFieldValueModel();
		if (fieldValueModel != null) {
			fieldValueModel.setEnabled(enabled);
		}
	}

	/**
	 * Updates the operator display based on the value of the current QueryField
	 * in the model. Only expected to be called if the saved field has changed.
	 * <ul>
	 * <li>Hides the operator panel if the current QueryField.getOperators()
	 * returns null</li>
	 * <li>Otherwise, shows the panel and replaces the operators in the
	 * dropdown</li>
	 * <li>If visible, sets the selected operator to the current field
	 * default</li>
	 * <li>Updates the value of the operator/operator model in the
	 * {@link QueryFieldModel}</li>
	 * <li>And consequently fires the change listener on that too</li>
	 * </ul>
	 */
	private void setOperatorsDropdownValuesForCurrentField() {

		if (model.getQueryField().getOperators() == null) {
			// Need to update the model manually and hide the operator component
			model.setOperator(null);
			hideOperator();
		} else {
			// Will update the model
			operatorChooser.replaceListItems(
					model.getQueryField().getOperators().stream()
							.map(op -> op.getDisplayName())
							.collect(Collectors.toList()),
					model.getQueryField().getDefaultOperator()
							.getDisplayName());
			// Show the operator component
			showOperator();
		}
	}

	private void showOperator() {
		operatorPanel.setVisible(true);
	}

	private void hideOperator() {
		operatorPanel.setVisible(false);
	}

	@Override
	public CountClearButtonBox getButtons() {
		return countButton;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void removeMe() {
		if (!isRoot()) {
			getParentGroup().removeQueryField(getQueryModel());
		}

	}
}
