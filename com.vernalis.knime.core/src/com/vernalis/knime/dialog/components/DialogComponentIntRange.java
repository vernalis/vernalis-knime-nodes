/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Allows the user to enter an int number range. It shows two spinners labelled
 * "min=" and "max=" which expect each an int. The component requires a
 * SettingsModelIntegerRange with its constructor, that holds the two values
 * entered.
 *
 * @see SettingsModelDoubleRange
 *
 * @author s.roughley
 */
public class DialogComponentIntRange extends DialogComponent {

	private static class RangeDialog extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5415477367833315706L;
		private final JLabel label;
		private final JLabel minLabel, maxLabel;
		private final JSpinner minSpinner, maxSpinner;

		public RangeDialog(String label, int min, int max, int minMin,
				int minStepSize, int minMax, int maxMin, int maxStepSize,
				int maxMax) {
			if (min > max) {
				throw new IllegalArgumentException("Min > max!");
			}
			this.label = new JLabel(label);
			this.minLabel = new JLabel("min=");
			this.maxLabel = new JLabel("max=");
			this.minSpinner = new JSpinner(
					new SpinnerNumberModel(min, minMin, minMax, minStepSize));
			this.maxSpinner = new JSpinner(
					new SpinnerNumberModel(max, maxMin, maxMax, maxStepSize));

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setMinimumSize(new Dimension(400, 60));

			Box box = Box.createHorizontalBox();
			box.add(this.label);
			box.add(Box.createHorizontalStrut(15));
			box.add(minLabel);
			box.add(Box.createHorizontalStrut(5));
			box.add(minSpinner);
			box.add(Box.createHorizontalStrut(10));
			box.add(maxLabel);
			box.add(Box.createHorizontalStrut(5));
			box.add(maxSpinner);
			add(box);
			add(Box.createVerticalStrut(10));
		}

		int getMin() {
			return ((Integer) minSpinner.getValue()).intValue();
		}

		int getMax() {
			return ((Integer) maxSpinner.getValue()).intValue();
		}

		void setMin(int min) {
			if (min <= getMax()) {
				minSpinner.setValue(Integer.valueOf(min));
			}

		}

		void setMax(int max) {
			if (max >= getMin()) {
				maxSpinner.setValue(Integer.valueOf(max));
			}
		}

		void setRange(int min, int max) {
			if (min <= max) {
				minSpinner.setValue(Integer.valueOf(min));
				maxSpinner.setValue(Integer.valueOf(max));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#setEnabled(boolean)
		 */
		@Override
		public void setEnabled(boolean enabled) {
			label.setEnabled(enabled);
			minLabel.setEnabled(enabled);
			maxLabel.setEnabled(enabled);
			minSpinner.setEnabled(enabled);
			maxSpinner.setEnabled(enabled);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.JComponent#setEnabled(boolean)
		 */
		@Override
		public void setToolTipText(String tooltip) {
			label.setToolTipText(tooltip);
			minLabel.setToolTipText(tooltip);
			maxLabel.setToolTipText(tooltip);
			minSpinner.setToolTipText(tooltip);
			maxSpinner.setToolTipText(tooltip);
		}
	}

	// private final JLabel m_label;
	//
	// private final JLabel m_labelMin;
	//
	// private final JLabel m_labelMax;
	//
	// private final JSpinner m_spinnerMin;
	//
	// private final JSpinner m_spinnerMax;
	private final RangeDialog rangeDialog;

	/**
	 * Creates two spinner to enter the lower and upper value of the range.
	 * 
	 * @param model
	 *            stores the double numbers entered
	 * @param lowerMin
	 *            minimum value to be entered
	 * @param upperMax
	 *            maximum value to be entered
	 * @param stepSize
	 *            step size for the spinners
	 * @param label
	 *            label for this component
	 */
	public DialogComponentIntRange(final SettingsModelIntegerRange model,
			final int lowerMin, final int upperMax, final int stepSize,
			final String label) {
		this(model, lowerMin, upperMax, stepSize, lowerMin, upperMax, stepSize,
				label);
	}

	/**
	 * Finegrain constructor to specify minimum and maximum values for the lower
	 * and upper bound and different step sizes for each spinner.
	 *
	 * @param model
	 *            stores the double numbers entered
	 * @param lowerMin
	 *            minimum value for the lower bound spinner
	 * @param lowerMax
	 *            maximum value for the lower bound spinner
	 * @param lowerStepSize
	 *            step size for the lower bound spinner
	 * @param upperMin
	 *            minimum value for the upper bound spinner
	 * @param upperMax
	 *            maximum value for the upper bound spinner
	 * @param upperStepSize
	 *            step size for the upper bound spinner
	 * @param label
	 *            label for this component
	 */
	public DialogComponentIntRange(final SettingsModelIntegerRange model,
			final int lowerMin, final int lowerMax, final int lowerStepSize,
			final int upperMin, final int upperMax, final int upperStepSize,
			final String label) {
		super(model);

		// final JPanel myPanel = getComponentPanel();
		// m_label = new JLabel(label);
		// m_labelMin = new JLabel("min=");
		// m_labelMax = new JLabel("max=");
		// m_spinnerMin = new JSpinner(new
		// SpinnerNumberModel(model.getMinRange(),
		// lowerMin, lowerMax, lowerStepSize));
		// JSpinner.DefaultEditor editor =
		// (JSpinner.DefaultEditor) m_spinnerMin.getEditor();
		// editor.getTextField().setColumns(10);
		// editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
		// m_spinnerMin.addChangeListener(new ChangeListener() {
		//
		// @Override
		// public void stateChanged(final ChangeEvent arg0) {
		// updateMinModel();
		// }
		// });
		//
		// m_spinnerMax = new JSpinner(new
		// SpinnerNumberModel(model.getMaxRange(),
		// upperMin, upperMax, upperStepSize));
		// editor = (JSpinner.DefaultEditor) m_spinnerMax.getEditor();
		// editor.getTextField().setColumns(10);
		// editor.getTextField().setFocusLostBehavior(JFormattedTextField.COMMIT);
		// m_spinnerMax.addChangeListener(new ChangeListener() {
		//
		// @Override
		// public void stateChanged(final ChangeEvent arg0) {
		// updateMaxModel();
		// }
		// });
		//
		// myPanel.add(m_label);
		// myPanel.add(m_labelMin);
		// myPanel.add(m_spinnerMin);
		//
		// myPanel.add(m_labelMax);
		// myPanel.add(m_spinnerMax);

		// model.prependChangeListener(new ChangeListener() {
		//
		// @Override
		// public void stateChanged(final ChangeEvent e) {
		// updateComponent();
		// }
		// });
		//
		// // call this method to be in sync with the settings model
		// updateComponent();
		rangeDialog = new RangeDialog(label, model.getMinRange(),
				model.getMaxRange(), lowerMin, lowerStepSize, lowerMax,
				upperMin, upperStepSize, upperMax);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		// panel.setBorder(BorderFactory
		// .createTitledBorder(BorderFactory.createEtchedBorder(), label));
		panel.add(rangeDialog);
		getComponentPanel().add(panel);

		model.prependChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(final ChangeEvent e) {
				updateComponent();
			}
		});

		// call this method to be in sync with the settings model
		updateComponent();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Nothing to check, we don't care about the specs.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setEnabledComponents(final boolean enabled) {
		rangeDialog.setEnabled(enabled);
		// m_spinnerMin.setEnabled(enabled);
		// m_spinnerMax.setEnabled(enabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setToolTipText(final String text) {
		rangeDialog.setToolTipText(text);
		// m_spinnerMin.setToolTipText(text);
		// m_spinnerMax.setToolTipText(text);
	}

	// /**
	// * Transfers the value from the spinner into the model. Colors the spinner
	// * red, if the number is not accepted by the settings model. And throws an
	// * exception then.
	// *
	// */
	// private void updateMinModel() {
	// try {
	// m_spinnerMin.commitEdit();
	// if (getModel() instanceof SettingsModelIntegerRange) {
	// final SettingsModelIntegerRange model =
	// (SettingsModelIntegerRange) getModel();
	// model.setMinRange(
	// ((Integer) m_spinnerMin.getValue()).intValue());
	// }
	// } catch (final ParseException e) {
	// final JComponent editorMin = m_spinnerMin.getEditor();
	// if (editorMin instanceof DefaultEditor) {
	// showError(((DefaultEditor) editorMin).getTextField());
	// }
	// }
	// }
	//
	// private void updateMaxModel() {
	// try {
	// m_spinnerMax.commitEdit();
	// if (getModel() instanceof SettingsModelIntegerRange) {
	// final SettingsModelIntegerRange model =
	// (SettingsModelIntegerRange) getModel();
	// model.setMaxRange(
	// ((Integer) m_spinnerMax.getValue()).intValue());
	// }
	// } catch (final ParseException e) {
	// final JComponent editorMax = m_spinnerMax.getEditor();
	// if (editorMax instanceof DefaultEditor) {
	// showError(((DefaultEditor) editorMax).getTextField());
	// }
	// }
	// }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateComponent() {

		// // clear any possible error indication
		// JComponent editor = m_spinnerMin.getEditor();
		// if (editor instanceof DefaultEditor) {
		// clearError(((DefaultEditor) editor).getTextField());
		// }
		// editor = m_spinnerMax.getEditor();
		// if (editor instanceof DefaultEditor) {
		// clearError(((DefaultEditor) editor).getTextField());
		// }
		//
		// // update the spinners
		// final SettingsModelIntegerRange model =
		// (SettingsModelIntegerRange) getModel();
		// try {
		// m_spinnerMin.commitEdit();
		// m_spinnerMax.commitEdit();
		//
		// final int valMin = ((Integer) m_spinnerMin.getValue()).intValue();
		// if (valMin != model.getMinRange()) {
		// m_spinnerMin.setValue(Integer.valueOf(model.getMinRange()));
		// }
		// final int valMax = ((Integer) m_spinnerMax.getValue()).intValue();
		// if (valMax != model.getMaxRange()) {
		// m_spinnerMax.setValue(Integer.valueOf(model.getMaxRange()));
		// }
		// } catch (ParseException e) {
		// m_spinnerMin.setValue(model.getMinRange());
		// m_spinnerMax.setValue(model.getMaxRange());
		// }

		final SettingsModelIntegerRange model =
				(SettingsModelIntegerRange) getModel();
		rangeDialog.setRange(model.getMinRange(), model.getMaxRange());

		// update enable status
		setEnabledComponents(model.isEnabled());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		final SettingsModelIntegerRange model =
				(SettingsModelIntegerRange) getModel();
		model.setMinRange(rangeDialog.getMin());
		model.setMaxRange(rangeDialog.getMax());
		// int newMin;
		// int newMax;
		// // try to commit Minimum
		// try {
		// m_spinnerMin.commitEdit();
		// newMin = ((Integer) m_spinnerMin.getValue()).intValue();
		// } catch (final ParseException e) {
		// final JComponent editor = m_spinnerMin.getEditor();
		// if (editor instanceof DefaultEditor) {
		// showError(((DefaultEditor) editor).getTextField());
		// }
		// String errMsg = "Invalid number format. ";
		// errMsg += "Please enter a valid minimum.";
		// throw new InvalidSettingsException(errMsg);
		// }
		// // try to commit Maximum
		// try {
		// m_spinnerMax.commitEdit();
		// newMax = ((Integer) m_spinnerMax.getValue()).intValue();
		// } catch (final ParseException e) {
		// final JComponent editor = m_spinnerMax.getEditor();
		// if (editor instanceof DefaultEditor) {
		// showError(((DefaultEditor) editor).getTextField());
		// }
		// String errMsg = "Invalid number format. ";
		// errMsg += "Please enter a valid maximum.";
		// throw new InvalidSettingsException(errMsg);
		// }
		//
		// try {
		// new SettingsModelIntegerRange(model.getConfigName(), newMin,
		// newMax);
		// } catch (final IllegalArgumentException iae) {
		// JComponent editor = m_spinnerMax.getEditor();
		// if (editor instanceof DefaultEditor) {
		// showError(((DefaultEditor) editor).getTextField());
		// }
		// editor = m_spinnerMin.getEditor();
		// if (editor instanceof DefaultEditor) {
		// showError(((DefaultEditor) editor).getTextField());
		// }
		// throw new InvalidSettingsException(iae.getMessage());
		// }
	}

}
