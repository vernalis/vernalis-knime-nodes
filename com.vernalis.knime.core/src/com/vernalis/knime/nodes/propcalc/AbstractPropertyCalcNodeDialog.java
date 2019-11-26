/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.nodes.propcalc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.ColumnFilter;

/**
 * Node Dialog pane for {@link CalculatedPropertyInterface} calculation nodes.
 * Shows a column chooser and property chooser panel
 * 
 * @author s.roughley
 * 
 */
public class AbstractPropertyCalcNodeDialog<T> extends DefaultNodeSettingsPane {

	public static final int MAX_VISIBLE_PROPERTIES = 15;
	private boolean m_isShowingAll;
	private final SettingsModelStringArray selectedProperties;

	/**
	 * Constructor
	 * 
	 * @param propertyLabel
	 *            The label for the property chooser in the node dialog. Will
	 *            also be used as settings key
	 * @param columnLabel
	 *            The label for the column chooser in the node dialog. Will also
	 *            be used as the settings key
	 * @param propertySet
	 *            The properties which can be calculated by the node
	 * @param classFilter
	 *            The possible input column types
	 */
	public AbstractPropertyCalcNodeDialog(String propertyLabel,
			String columnLabel, CalculatedPropertyInterface<T>[] propertySet,
			ColumnFilter classFilter) {

		addDialogComponent(new DialogComponentColumnNameSelection(
				createColumnNameModel(columnLabel), columnLabel, 0,
				classFilter));

		createNewGroup("Calculated Properties");
		// The properties component
		final List<String> propertyNames = new ArrayList<>();
		for (CalculatedPropertyInterface<T> prop : propertySet) {
			propertyNames.add(prop.getName());
		}

		selectedProperties =
				createPropertiesModel(propertyLabel, propertyNames);
		final DialogComponentStringListSelection propertyListDlg =
				new DialogComponentStringListSelection(selectedProperties,
						propertyLabel, propertyNames,
						ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true,
						Math.min(propertyNames.size(), MAX_VISIBLE_PROPERTIES));

		m_isShowingAll = propertyNames.size() <= MAX_VISIBLE_PROPERTIES;
		addDialogComponent(propertyListDlg);

		final DialogComponentButton selectAll =
				new DialogComponentButton("Select All");
		selectAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				selectedProperties.setStringArrayValue(
						propertyNames.toArray(new String[0]));
			}
		});
		final DialogComponentButton selectNone =
				new DialogComponentButton("Clear");
		// NB - we cannot set no properties
		selectNone.setToolTipText("Selects only the 1st property");
		selectNone.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectedProperties.setStringArrayValue(
						new String[] { propertyNames.get(0) });
			}
		});

		final DialogComponentButton showAll =
				new DialogComponentButton("Show all");
		showAll.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!m_isShowingAll) {
					propertyListDlg.setVisibleRowCount(propertyNames.size());
					showAll.setText("Show 1st " + MAX_VISIBLE_PROPERTIES);
					m_isShowingAll = true;
				} else {
					propertyListDlg.setVisibleRowCount(MAX_VISIBLE_PROPERTIES);
					showAll.setText("Show all");
					m_isShowingAll = false;
				}
				// Now make it resize
				for (Component comp : propertyListDlg.getComponentPanel()
						.getComponents()) {
					if (comp instanceof JScrollPane) {
						((JScrollPane) comp).getViewport().revalidate();
						// ((JScrollPane) comp).getViewport().repaint();
					}
				}
			}
		});
		setHorizontalPlacement(true);
		if (propertyNames.size() > 1) {
			// Only if there are more than 1 property
			addDialogComponent(selectAll);
			addDialogComponent(selectNone);
			if (propertyNames.size() > MAX_VISIBLE_PROPERTIES) {
				// Only if more than 15 properties
				addDialogComponent(showAll);
			}
		}
		setHorizontalPlacement(false);
		closeCurrentGroup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.data.DataTableSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		try {
			selectedProperties.loadSettingsFrom(settings);
			String[] newProps =
					new String[selectedProperties.getStringArrayValue().length];
			for (int i = 0; i < newProps.length; i++) {
				newProps[i] = selectedProperties.getStringArrayValue()[i]
						.replace("_", " ");
			}
			selectedProperties.setStringArrayValue(newProps);
		} catch (InvalidSettingsException e) {
			// Let the node sort itself out...
		}

		super.loadAdditionalSettingsFrom(settings, specs);
	}

	/**
	 * Create the properties model
	 * 
	 * @param propertyLabel
	 *            The property label in the node dialog
	 * @param propertyNames
	 *            Collection of the property names
	 * 
	 * @return The settings model, with all properties selected by default
	 */
	static SettingsModelStringArray createPropertiesModel(String propertyLabel,
			Collection<String> propertyNames) {
		return new SettingsModelStringArray(propertyLabel,
				propertyNames.toArray(new String[0]));
	}

	/**
	 * @param columnLabel
	 *            The column label in the node dialog
	 * @return The column name settings model
	 */
	static SettingsModelString createColumnNameModel(String columnLabel) {
		return new SettingsModelString(columnLabel, null);
	}
}
