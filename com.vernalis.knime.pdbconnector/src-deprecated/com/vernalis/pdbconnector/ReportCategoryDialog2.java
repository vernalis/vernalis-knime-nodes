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
package com.vernalis.pdbconnector;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.ReportCategory2;
import com.vernalis.pdbconnector.config.ReportField2;
import com.vernalis.pdbconnector.config.StandardReport;

/**
 * ReportCategoryDialog class.
 *
 * A ReportCategoryDialog defines the user interface dialog components for a
 * single report category. It contains:
 * <UL>
 * <LI>The report category (and the list of child report fields) that are
 * represented by the dialog</LI>
 * <LI>A checkbox to control selection/deselection of all report fields in the
 * category</LI>
 * <LI>A collection of Boolean dialog components to represent the report
 * fields</LI>
 * </UL>
 */
@SuppressWarnings("serial")
@Deprecated
public class ReportCategoryDialog2 extends JPanel {

	private final ReportCategory2 m_reportCategory;
	private JCheckBox m_checkBox;
	private final List<ReportField2> m_fields;
	private final List<DialogComponentBoolean> m_dlgs = new ArrayList<>();

	/**
	 * Locks listener when in use to prevent both listeners firing in sequence.
	 */
	private boolean m_lock = false;

	/**
	 * Instantiates a new report category dialog for a given report category.
	 *
	 * @param reportCategory
	 *            the report category
	 */
	public ReportCategoryDialog2(final ReportCategory2 reportCategory) {
		m_reportCategory = reportCategory;
		super.setLayout(new GridBagLayout());
		super.setBorder(BorderFactory.createEtchedBorder());
		// create category selection checkbox
		m_checkBox = new JCheckBox(m_reportCategory.getLabel());
		m_checkBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				selectFields(m_checkBox.isSelected());
			}
		});
		int iRow = 0;
		int iCol = 0;
		// Add checkBox to occupy whole of first column, anchored to the centre
		// left (WEST)
		GridBagConstraints cChk = new GridBagConstraints();
		cChk.fill = GridBagConstraints.NONE;// do not resize component
		cChk.weightx = 1.0;
		cChk.gridx = iCol;// x cell coord
		cChk.gridy = iRow;// y cell coord
		cChk.gridwidth = 1;// occupy one column
		cChk.gridheight = GridBagConstraints.REMAINDER;// occupy all rows
		cChk.anchor = GridBagConstraints.WEST;// anchor to centre left
		// Horizontal padding to fill preferred column width if necessary
		cChk.ipadx = Math.max(0, Properties.REPORT_LAYOUT_COL_WIDTH
				- (int) m_checkBox.getPreferredSize().getWidth());
		super.add(m_checkBox, cChk);
		// Create boolean dialog components for each field
		m_fields = m_reportCategory.getReportFields();
		for (final ReportField2 field : m_fields) {
			DialogComponentBoolean dlg =
					ComponentFactory2.createSelectionDialogComponent(field);
			m_dlgs.add(dlg);
			((SettingsModelBoolean) dlg.getModel())
					.addChangeListener(new ChangeListener() {

						@Override
						public void stateChanged(final ChangeEvent e) {
							updateCheckBox();
						}
					});
			++iCol;
			if (iCol > Properties.REPORT_LAYOUT_COLUMNS) {
				iRow++;
				iCol = 1;
			}
			JPanel comp = dlg.getComponentPanel();
			comp.setLayout(new FlowLayout(FlowLayout.LEFT));
			GridBagConstraints cDlg = new GridBagConstraints();
			cDlg.fill = GridBagConstraints.NONE;
			cDlg.weightx = 1.0;
			cDlg.gridx = iCol;
			cDlg.gridy = iRow;
			cDlg.gridwidth = 1;
			cDlg.gridheight = 1;
			cDlg.anchor = GridBagConstraints.WEST;
			// Horizontal padding to fill preferred column width if necessary
			cDlg.ipadx = Math.max(0, Properties.REPORT_LAYOUT_COL_WIDTH
					- (int) comp.getPreferredSize().getWidth());
			super.add(comp, cDlg);
		}
		updateCheckBox();
	}

	/**
	 * Loads dialog settings.
	 *
	 * Calls
	 * {@link DialogComponent#loadSettingsFrom(NodeSettingsRO, PortObjectSpec[])}
	 * on each dialog component.
	 *
	 * @param settings
	 *            the settings
	 * @param specs
	 *            the specs
	 * @throws NotConfigurableException
	 *             the not configurable exception
	 */
	public final void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		assert settings != null;
		assert specs != null;
		for (DialogComponent comp : m_dlgs) {
			comp.loadSettingsFrom(settings, specs);
		}
		updateCheckBox();
	}

	/**
	 * Saves dialog settings.
	 *
	 * Calls {@link DialogComponent#saveSettingsTo(NodeSettingsWO)} on each
	 * dialog component.
	 *
	 * @param settings
	 *            the settings
	 * @throws InvalidSettingsException
	 *             the invalid settings exception
	 */
	public final void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		for (DialogComponent comp : m_dlgs) {
			comp.saveSettingsTo(settings);
		}
	}

	/**
	 * Applies a standard report to the report field selection.
	 *
	 * The selection state of each report field is updated to reflect the
	 * standard report that is applied.
	 *
	 * @param stdReport
	 *            the standard report to apply
	 */
	public void applyStandardReport(final StandardReport stdReport) {
		if (!m_lock && (stdReport != null)) {
			boolean isCustomTable = stdReport.isCustom();
			m_lock = true;
			Iterator<ReportField2> fieldIter = m_fields.iterator();
			Iterator<DialogComponentBoolean> dlgIter = m_dlgs.iterator();
			while (fieldIter.hasNext() && dlgIter.hasNext()) {
				final ReportField2 field = fieldIter.next();
				final DialogComponentBoolean dlg = dlgIter.next();
				final SettingsModelBoolean model =
						(SettingsModelBoolean) dlg.getModel();
				// 1) Update field selections if this is not a customizable
				// table
				if (!isCustomTable) {
					model.setBooleanValue(field.isTriggered(stdReport));
				}
				// 2) Set enabled state of field selection checkboxes.
				// (greyed out for standard report; enabled for customizable
				// table)
				model.setEnabled(isCustomTable);
				final JPanel dlgPanel = dlg.getComponentPanel();
				dlgPanel.setEnabled(isCustomTable);
				for (Component comp : ComponentFactory2
						.getAllSubComponents(dlgPanel, null)) {
					comp.setEnabled(isCustomTable);
				}
			}
			m_lock = false;
			updateCheckBox();
			m_checkBox.setEnabled(isCustomTable);
		}
	}

	/**
	 * Updates state of report category check box.
	 *
	 * Checkbox is selected if all child fields are selected, else is not
	 * selected.
	 *
	 */
	public void updateCheckBox() {
		if (!m_lock) {
			m_lock = true;
			boolean isSelected = true;
			Iterator<DialogComponentBoolean> iter = m_dlgs.iterator();
			while (isSelected && iter.hasNext()) {
				isSelected = iter.next().isSelected();
			}
			if (isSelected != m_checkBox.isSelected()) {
				m_checkBox.setSelected(isSelected);
			}
			m_lock = false;
		}
	}

	/**
	 * Sets the selection state of all report fields.
	 *
	 * @param isSelected
	 *            the selection state to set
	 */
	public void selectFields(final boolean isSelected) {
		if (!m_lock) {
			m_lock = true;
			for (DialogComponentBoolean dlg : m_dlgs) {
				final SettingsModelBoolean model =
						(SettingsModelBoolean) dlg.getModel();
				model.setBooleanValue(isSelected);
			}
			m_lock = false;
		}
	}
}
