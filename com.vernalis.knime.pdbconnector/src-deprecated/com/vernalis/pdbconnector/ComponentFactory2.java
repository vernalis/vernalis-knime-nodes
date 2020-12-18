/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2016 Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 *  
 *  Copyright (C) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.pdbconnector;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JSpinner;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.QueryParam;
import com.vernalis.pdbconnector.config.ReportField2;

/**
 * Factory class for creating KNIME SettingsModel and DialogComponent objects
 * dynamically from PdbConnectorConfig definition objects (QueryOption,
 * QueryParam and ReportField).
 * 
 * @see QueryOption
 * @see QueryParam
 * @see ReportField2
 * @deprecated since 1.28.0
 */
@Deprecated
public class ComponentFactory2 {

	/**
	 * Creates a new Boolean settings model to control the selection status of a
	 * query option.
	 *
	 * The SettingsModel name is defined as the QueryOption id with a
	 * ".SELECTED" suffix.
	 * 
	 * @param query
	 *            the query
	 * @return the Boolean settings model.
	 */
	public static SettingsModelBoolean
			createSelectionSettingsModel(QueryOption query) {
		return (query != null)
				? new SettingsModelBoolean(query.getId() + ".SELECTED",
						query.getDefault())
				: null;
	}

	/**
	 * Creates a new Boolean settings model to control the selection status of a
	 * report field.
	 *
	 * The SettingsModel name is equal to the ReportField id.
	 * 
	 * @param field
	 *            the field
	 * @return the Boolean settings model
	 */
	public static SettingsModelBoolean
			createSelectionSettingsModel(ReportField2 field) {
		return (field != null)
				? new SettingsModelBoolean(field.getId(), field.getDefault())
				: null;
	}

	/**
	 * Creates a new Boolean dialog component to control the selection status of
	 * a query option.
	 *
	 * This creates a new underlying Boolean SettingsModel as well.
	 * 
	 * @param query
	 *            the query
	 * @return the Boolean dialog component.
	 */
	public static DialogComponentBoolean
			createSelectionDialogComponent(QueryOption query) {
		return (query != null)
				? new DialogComponentBoolean(
						createSelectionSettingsModel(query), "Selected")
				: null;
	}

	/**
	 * Creates a new Boolean dialog component to control the selection status of
	 * a report field.
	 *
	 * This creates a new underlying Boolean SettingsModel as well.
	 * 
	 * @param field
	 *            the field
	 * @return the Boolean dialog component.
	 */
	public static DialogComponentBoolean
			createSelectionDialogComponent(ReportField2 field) {
		return (field != null)
				? new DialogComponentBoolean(
						createSelectionSettingsModel(field), field.getLabel())
				: null;
	}

	/**
	 * Creates the settings model(s) required to represent a single query param.
	 *
	 * All settings models are enabled.
	 * 
	 * @param param
	 *            the query param
	 * @return the list of settings model(s)
	 */
	public static List<SettingsModel> createSettingsModel(QueryParam param) {
		return createSettingsModels(param, true);
	}

	/**
	 * Creates the settings model(s) required to represent a single query param.
	 *
	 * The enabled status of the settings models is controlled by the isEnabled
	 * argument.
	 * 
	 * @param param
	 *            the query param
	 * @param isEnabled
	 *            settings model enabled status
	 * @return the list of settings model(s)
	 */
	public static List<SettingsModel> createSettingsModels(QueryParam param,
			boolean isEnabled) {
		List<SettingsModel> retVal = new ArrayList<>();
		if (param != null) {
			switch (param.getType()) {
				case STRING:
				case BIG_STRING:
				case STRING_COND:
					retVal.add(new SettingsModelString(param.getId(), ""));
					break;
				case INTEGER:
					retVal.add(new SettingsModelIntegerBounded(param.getId(),
							(int) param.getDefault(), (int) param.getMin(),
							(int) param.getMax()));
					break;
				case DOUBLE:
					retVal.add(new SettingsModelDoubleBounded(param.getId(),
							param.getDefault(), param.getMin(),
							param.getMax()));
					break;
				case INTEGER_RANGE:
				case INTEGER_RANGE_COND:
					retVal.add(new SettingsModelIntegerBounded(
							param.getId() + ".MIN", (int) param.getMin(),
							(int) param.getMin(), (int) param.getMax()));
					retVal.add(new SettingsModelIntegerBounded(
							param.getId() + ".MAX", (int) param.getMax(),
							(int) param.getMin(), (int) param.getMax()));
					break;
				case DOUBLE_RANGE:
				case DOUBLE_RANGE_COND:
					retVal.add(new SettingsModelDoubleBounded(
							param.getId() + ".MIN", param.getMin(),
							param.getMin(), param.getMax()));
					retVal.add(new SettingsModelDoubleBounded(
							param.getId() + ".MAX", param.getMax(),
							param.getMin(), param.getMax()));
					break;
				case STRING_LIST:
					retVal.add(new SettingsModelString(param.getId(),
							param.getValues().getDefaultLabel()));
					break;
				case DATE:
					// Separate bounded integer fields for YYYY-MM-DD
					Calendar now = Calendar.getInstance();
					int year = now.get(Calendar.YEAR);
					retVal.add(new SettingsModelIntegerBounded(
							param.getId() + ".YYYY", year, 1900, year));
					retVal.add(new SettingsModelIntegerBounded(
							param.getId() + ".MM", now.get(Calendar.MONTH) + 1,
							1, 12));
					retVal.add(new SettingsModelIntegerBounded(
							param.getId() + ".DD",
							now.get(Calendar.DAY_OF_MONTH), 1, 31));
					break;
				default:
					break;
			}
		}
		for (SettingsModel model : retVal) {
			model.setEnabled(isEnabled);
		}
		return retVal;
	}

	/**
	 * Creates the dialog component(s) required to represent a single query
	 * param.
	 *
	 * All settings models and dialog components are enabled.
	 *
	 * @param param
	 *            the query param
	 * @return the list of dialog component(s)
	 */
	public static List<DialogComponent>
			createDialogComponents(QueryParam param) {
		return createDialogComponents(param, true);
	}

	/**
	 * Creates the dialog component(s) required to represent a single query
	 * param.
	 *
	 * The enabled status of the settings models and dialog components is
	 * controlled by the isEnabled argument.
	 * 
	 * @param param
	 *            the query param
	 * @param isEnabled
	 *            settings model enabled status
	 * @return the list of dialog components
	 */
	public static List<DialogComponent> createDialogComponents(QueryParam param,
			boolean isEnabled) {
		List<DialogComponent> retVal = new ArrayList<>();
		if (param != null) {
			List<SettingsModel> models = createSettingsModels(param, isEnabled);
			switch (param.getType()) {
				case STRING:
				case STRING_COND:
					assert models.size() == 1;
					retVal.add(new DialogComponentString(
							(SettingsModelString) models.get(0),
							param.getLabel(), false, param.getWidth()));
					break;
				case INTEGER:
					assert models.size() == 1;
					retVal.add(new DialogComponentNumber(
							(SettingsModelIntegerBounded) models.get(0),
							param.getLabel(), 1, param.getWidth()));
					break;
				case DOUBLE:
					assert models.size() == 1;
					retVal.add(new DialogComponentNumber(
							(SettingsModelDoubleBounded) models.get(0),
							param.getLabel(), 1.0, param.getWidth()));
					break;
				case INTEGER_RANGE:
				case INTEGER_RANGE_COND:
					assert models.size() == 2;
					retVal.add(new DialogComponentNumber(
							(SettingsModelIntegerBounded) models.get(0),
							param.getLabel() + ": min", 1, param.getWidth()));
					retVal.add(new DialogComponentNumber(
							(SettingsModelIntegerBounded) models.get(1), "max",
							1, param.getWidth()));
					break;
				case DOUBLE_RANGE:
				case DOUBLE_RANGE_COND:
					assert models.size() == 2;
					retVal.add(new DialogComponentNumber(
							(SettingsModelDoubleBounded) models.get(0),
							param.getLabel() + ": min", 1.0, param.getWidth()));
					retVal.add(new DialogComponentNumber(
							(SettingsModelDoubleBounded) models.get(1), "max",
							1.0, param.getWidth()));
					break;
				case STRING_LIST:
					assert models.size() == 1;
					retVal.add(new DialogComponentStringSelection(
							(SettingsModelString) models.get(0),
							param.getLabel(), param.getValues().getLabels()));
					break;
				case DATE:
					// Separate bounded integer fields for YYYY-MM-DD
					assert models.size() == 3;
					DialogComponentNumber yearDlg = new DialogComponentNumber(
							(SettingsModelIntegerBounded) models.get(0),
							param.getLabel() + ": YYYY", 1);
					DialogComponentNumber monthDlg = new DialogComponentNumber(
							(SettingsModelIntegerBounded) models.get(1), "MM",
							1);
					DialogComponentNumber dayDlg = new DialogComponentNumber(
							(SettingsModelIntegerBounded) models.get(2), "DD",
							1);
					// remove thousands separator from year fields and set
					// leading
					// zeros on month and day fields.
					setNumberFormat(yearDlg, "#");
					setNumberFormat(monthDlg, "#00");
					setNumberFormat(dayDlg, "#00");
					retVal.add(yearDlg);
					retVal.add(monthDlg);
					retVal.add(dayDlg);
					break;
				case BIG_STRING:
					assert models.size() == 1;
					retVal.add(new DialogComponentMultiLineString(
							(SettingsModelString) models.get(0),
							param.getLabel(), false, param.getWidth(),
							Properties.QUERY_PRM_BIGSTRING_ROWS));
					break;
				default:
					break;
			}
		}
		return retVal;
	}

	/**
	 * Resets dialog components to default values for given query param.
	 *
	 * @param param
	 *            the query param
	 * @param dlgs
	 *            the dialog components
	 */
	public static void resetDialogComponents(QueryParam param,
			List<DialogComponent> dlgs) {
		if (param != null) {
			switch (param.getType()) {
				case STRING:
				case BIG_STRING:
				case STRING_COND:
					assert dlgs.size() == 1;
					((SettingsModelString) dlgs.get(0).getModel())
							.setStringValue("");
					break;
				case INTEGER:
					assert dlgs.size() == 1;
					((SettingsModelIntegerBounded) dlgs.get(0).getModel())
							.setIntValue((int) param.getDefault());
					break;
				case DOUBLE:
					assert dlgs.size() == 1;
					((SettingsModelDoubleBounded) dlgs.get(0).getModel())
							.setDoubleValue(param.getDefault());
					break;
				case INTEGER_RANGE:
				case INTEGER_RANGE_COND:
					assert dlgs.size() == 2;
					((SettingsModelIntegerBounded) dlgs.get(0).getModel())
							.setIntValue((int) param.getMin());
					((SettingsModelIntegerBounded) dlgs.get(1).getModel())
							.setIntValue((int) param.getMax());
					break;
				case DOUBLE_RANGE:
				case DOUBLE_RANGE_COND:
					assert dlgs.size() == 2;
					((SettingsModelDoubleBounded) dlgs.get(0).getModel())
							.setDoubleValue(param.getMin());
					((SettingsModelDoubleBounded) dlgs.get(1).getModel())
							.setDoubleValue(param.getMax());
					break;
				case STRING_LIST:
					assert dlgs.size() == 1;
					((SettingsModelString) dlgs.get(0).getModel())
							.setStringValue(
									param.getValues().getDefaultLabel());
					break;
				case DATE:
					assert dlgs.size() == 3;
					Calendar now = Calendar.getInstance();
					int year = now.get(Calendar.YEAR);
					((SettingsModelIntegerBounded) dlgs.get(0).getModel())
							.setIntValue(year);
					((SettingsModelIntegerBounded) dlgs.get(1).getModel())
							.setIntValue(now.get(Calendar.MONTH) + 1);
					((SettingsModelIntegerBounded) dlgs.get(2).getModel())
							.setIntValue(now.get(Calendar.DAY_OF_MONTH));
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Sets the number format on a number dialog component.
	 *
	 * This method sets the number format on all JSpinner subcomponent instances
	 * in the dialog panel.
	 * 
	 * @param dlg
	 *            the dialog component
	 * @param formatPattern
	 *            the number format pattern
	 * @see ComponentFactory2#getAllSubComponents
	 */
	public static void setNumberFormat(DialogComponentNumber dlg,
			String formatPattern) {
		// Find the JSpinner component in the dialog panel
		for (Component comp : ComponentFactory2
				.getAllSubComponents(dlg.getComponentPanel(), null)) {
			if (comp instanceof JSpinner) {
				JSpinner spinner = (JSpinner) comp;
				spinner.setEditor(
						new JSpinner.NumberEditor(spinner, formatPattern));
			}
		}
	}

	/**
	 * Gets list of all UI subcomponents in UI container (recursive call).
	 *
	 * @param container
	 *            the UI container to traverse
	 * @param retVal
	 *            partial return list (from previous recursive calls)
	 * @return the list of all sub components
	 */
	public static List<Component> getAllSubComponents(Container container,
			List<Component> retVal) {
		if (retVal == null) {
			retVal = new ArrayList<>();
		}
		for (final Component com : container.getComponents()) {
			retVal.add(com);
			if (com instanceof Container) {
				retVal = getAllSubComponents((Container) com, retVal);
			}
		}
		return retVal;
	}
}
