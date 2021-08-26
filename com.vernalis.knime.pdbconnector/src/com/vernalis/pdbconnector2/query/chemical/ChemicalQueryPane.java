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
package com.vernalis.pdbconnector2.query.chemical;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentLineWrappedStringInput;
import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;
import com.vernalis.pdbconnector2.dialogcomponents.swing.SwingUtils;
import com.vernalis.pdbconnector2.query.QueryPanel;
import com.vernalis.pdbconnector2.query.chemical.ChemicalQueryModel.EventType;

/**
 * A {@link QueryPanel} implementation representing a chemical query, backed by
 * a {@link ChemicalQueryModel}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class ChemicalQueryPane extends Box
		implements QueryPanel<ChemicalQueryModel>, ChangeListener {

	private static final long serialVersionUID = 1L;
	private final ChemicalQueryModel model;
	private final CountClearButtonBox ccButtons;
	private final Box optsBox;
	private final EnumMap<ChemicalQueryType, List<JComponent>> queryOptionsComponents =
			new EnumMap<>(ChemicalQueryType.class);
	private static final String borderTitle = "Chemical Query";

	/**
	 * Constructor
	 * 
	 * @param rows
	 *            The number of rows to display in the text input
	 * @param model
	 *            The settings model
	 * @param fvm
	 *            The optional {@link FlowVariableModel} which will add a button
	 *            to the string input if it is not {@code null}
	 */
	public ChemicalQueryPane(int rows, ChemicalQueryModel model,
			FlowVariableModel fvm) {
		super(BoxLayout.Y_AXIS);
		this.model = model;
		this.model.addChangeListener(this);

		resetBorder();

		final Box seqBox = addNewRow();
		seqBox.add(new DialogComponentLineWrappedStringInput(
				model.getTextInputModel(), "", rows, 80, false, false, fvm)
						.getComponentPanel());
		ccButtons = new CountClearButtonBox(this, rows > 2);
		seqBox.add(ccButtons);
		seqBox.setMinimumSize(getPreferredSize());
		seqBox.setMaximumSize(getPreferredSize());
		resetCountClearButtons();

		optsBox = addNewRow();
		final JPanel componentPanel = new DialogComponentStringSelection(
				model.getQueryTypeModel(), "Query Type",
				Arrays.stream(ChemicalQueryType.values()).map(x -> x.getText())
						.collect(Collectors.toList())).getComponentPanel();
		componentPanel.setMinimumSize(getPreferredSize());
		componentPanel.setMaximumSize(getPreferredSize());
		optsBox.add(componentPanel);

		queryOptionsComponents.put(ChemicalQueryType.Formula,
				new ArrayList<>());
		queryOptionsComponents.get(ChemicalQueryType.Formula)
				.add(SwingUtils.forceToPreferredSize(new DialogComponentBoolean(
						model.getMatchSubsetModel(), "Match Subset")));

		queryOptionsComponents.put(ChemicalQueryType.Descriptor,
				new ArrayList<>());
		queryOptionsComponents.get(ChemicalQueryType.Descriptor).add(SwingUtils
				.forceToPreferredSize(new DialogComponentStringSelection(
						model.getDescriptorTypeModel(), "Descriptor Type",
						Arrays.stream(ChemicalDescriptorType.values())
								.map(x -> x.getText())
								.toArray(String[]::new))));
		queryOptionsComponents.get(ChemicalQueryType.Descriptor)
				.add(SwingUtils.forceToPreferredSize(
						new DialogComponentStringSelection(
								model.getMatchTypeModel(), "Match Type",
								Arrays.stream(ChemicalMatchType.values())
										.map(x -> x.getText())
										.toArray(String[]::new))));
		queryOptionsComponents.values().stream().flatMap(x -> x.stream())
				.forEach(x -> optsBox.add(x));

		setComponentVisibility();
	}

	/**
	 * Overloaded constructor with no flow variable button
	 * 
	 * @param rows
	 *            The number of rows to display in the text input
	 * @param model
	 *            The settings model
	 */
	public ChemicalQueryPane(int rows, ChemicalQueryModel model) {
		this(rows, model, null);
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
	 * @return the {@link Box} containing the options for the query type
	 */
	public Box getOptionsBox() {
		return optsBox;
	}

	/**
	 * Method to add a new row to the component
	 * 
	 * @return the added {@link Box}
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
	public String getBorderTitle() {
		return borderTitle;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public ChemicalQueryModel getQueryModel() {
		return model;
	}

	@Override
	public void stateChanged(ChangeEvent e) {

		switch ((EventType) e.getSource()) {
			case QUERY_TYPE_CHANGE:
				setComponentVisibility();

			case DESCRIPTOR_TYPE_CHANGE:
			case MATCH_SUBSET_CHANGE:
			case MATCH_TYPE_CHANGE:
			case STRING_CHANGE:
				resetCountClearButtons();

			default:
				break;

		}

	}

	private void setComponentVisibility() {
		for (Entry<ChemicalQueryType, List<JComponent>> ent : queryOptionsComponents
				.entrySet()) {
			ent.getValue().forEach(
					x -> x.setVisible(ent.getKey() == model.getQueryType()));
		}

	}

}
