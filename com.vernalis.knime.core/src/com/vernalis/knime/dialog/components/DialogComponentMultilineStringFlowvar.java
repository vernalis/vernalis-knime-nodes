package com.vernalis.knime.dialog.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * A {@link DialogComponentMultiLineString} wrapper which puts the title and
 * multiline string within an etched border, and adds a Flow Vairable button
 * immediately to the right of the title text
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class DialogComponentMultilineStringFlowvar
		extends DialogComponentMultiLineString {

	/**
	 * Constructor. Empty strings are accepted in the input, and the editable
	 * text panel is of default size
	 * 
	 * @param stringModel
	 *            The Settings Model for the editable content
	 * @param label
	 *            The text label
	 * @param fvm
	 *            The flow variable model
	 */
	public DialogComponentMultilineStringFlowvar(
			SettingsModelString stringModel, String label,
			FlowVariableModel fvm) {
		super(stringModel, label);
		fvm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent evt) {
				getModel().setEnabled(!fvm.isVariableReplacementEnabled());
			}
		});
		getComponentPanel().setBorder(new EtchedBorder());
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		titlePanel.add(getComponentPanel().getComponent(0), BorderLayout.WEST);
		titlePanel.add(new FlowVariableModelButton(fvm), BorderLayout.EAST);
		getComponentPanel().add(titlePanel, BorderLayout.NORTH);
		updateComponent();
	}

	/**
	 * Constructor.
	 * 
	 * @param stringModel
	 *            The Settings Model for the editable content
	 * @param label
	 *            The text label
	 * @param disallowEmptyString
	 *            if set true, the component request a non-empty string from the
	 *            user.
	 * @param cols
	 *            the number of columns.
	 * @param rows
	 *            the number of rows.
	 * @param fvm
	 *            The flow variable model
	 */
	public DialogComponentMultilineStringFlowvar(
			SettingsModelString stringModel, String label,
			boolean disallowEmptyString, int cols, int rows,
			FlowVariableModel fvm) {
		super(stringModel, label, disallowEmptyString, cols, rows);
		fvm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent evt) {
				getModel().setEnabled(!fvm.isVariableReplacementEnabled());
			}
		});
		getComponentPanel().setBorder(new EtchedBorder());
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		titlePanel.add(getComponentPanel().getComponent(0), BorderLayout.WEST);
		titlePanel.add(new FlowVariableModelButton(fvm), BorderLayout.EAST);
		getComponentPanel().add(titlePanel, BorderLayout.NORTH);
		updateComponent();
	}

}
