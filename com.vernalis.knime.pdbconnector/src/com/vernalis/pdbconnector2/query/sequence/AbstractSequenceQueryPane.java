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
package com.vernalis.pdbconnector2.query.sequence;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentLineWrappedStringInput;
import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;
import com.vernalis.pdbconnector2.query.QueryPanel;

/**
 * An abstract base pane for a sequence query, which displays a string input
 * with optional flow variable selector button, and {@link CountClearButtonBox}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 * @param <T>
 *            The type of {@link AbstractSequenceQueryModel} implementation
 */
public abstract class AbstractSequenceQueryPane<T extends AbstractSequenceQueryModel>
		extends Box implements QueryPanel<T> {

	private static final long serialVersionUID = 1L;
	private final T model;
	private final CountClearButtonBox ccButtons;
	private final Box optsBox;
	private final String borderTitle;

	/**
	 * Constructor
	 * 
	 * @param rows
	 *            The number of rows in the sequence input box
	 * @param model
	 *            the {@link AbstractSequenceQueryModel}
	 * @param borderTitle
	 *            The title to display in the component border
	 * @param fvm
	 *            The optional {@link FlowVariableModel} for the sequence
	 */
	public AbstractSequenceQueryPane(int rows, T model, String borderTitle,
			FlowVariableModel fvm) {
		super(BoxLayout.Y_AXIS);
		this.model = model;
		this.model.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				resetCountClearButtons();

			}
		});
		this.borderTitle = borderTitle;
		resetBorder();

		final Box seqBox = addNewRow();
		seqBox.add(new DialogComponentLineWrappedStringInput(
				model.getSequenceModel(), "", rows, 80, false, false, fvm)
						.getComponentPanel());
		ccButtons = new CountClearButtonBox(this, rows > 2);
		seqBox.add(ccButtons);
		seqBox.setMinimumSize(getPreferredSize());
		seqBox.setMaximumSize(getPreferredSize());
		resetCountClearButtons();

		optsBox = addNewRow();
		final JPanel componentPanel = new DialogComponentStringSelection(
				model.getTargetModel(), "Target",
				Arrays.stream(SequenceTarget.values()).map(x -> x.getText())
						.collect(Collectors.toList())).getComponentPanel();
		componentPanel.setMinimumSize(getPreferredSize());
		componentPanel.setMaximumSize(getPreferredSize());
		optsBox.add(componentPanel);
	}

	@Override
	public String getBorderTitle() {
		return borderTitle;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * @return The Box to add options to
	 */
	public Box getOptionsBox() {
		return optsBox;
	}

	/**
	 * Add a new row to the dialog component
	 * 
	 * @return The Box for the new row's components to be added to
	 */
	public Box addNewRow() {
		final Box retVal = new Box(BoxLayout.X_AXIS);
		add(retVal);
		return retVal;
	}

	@Override
	public CountClearButtonBox getButtons() {
		return ccButtons;
	}

	@Override
	public T getQueryModel() {
		return model;
	}

}
