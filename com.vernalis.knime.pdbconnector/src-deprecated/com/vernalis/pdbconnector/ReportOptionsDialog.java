/*******************************************************************************
 * Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 ******************************************************************************/
package com.vernalis.pdbconnector;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector.config.PdbConnectorConfig;
import com.vernalis.pdbconnector.config.ReportCategory;
import com.vernalis.pdbconnector.config.StandardCategory;
import com.vernalis.pdbconnector.config.StandardReport;

/**
 * Represents the master Report Options tab.
 * 
 * @deprecated Use {@link ReportOptionsDialog2}
 */
@Deprecated
@SuppressWarnings("serial")
public class ReportOptionsDialog extends JPanel {
	static final NodeLogger logger = NodeLogger.getLogger(ReportOptionsDialog.class);
	private final List<ReportCategoryDialog> m_reportDlgs = new ArrayList<>();
	@SuppressWarnings("rawtypes")
	private JComboBox m_comboBox = null;
	private JButton m_selectAllButton = null;
	private JButton m_clearAllButton = null;
	private StandardReport m_defaultReport = null;
	private StandardReport m_customReport = null;

	/**
	 * Instantiates a new report options dialog.
	 *
	 * @param config
	 *            the configuration
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ReportOptionsDialog(PdbConnectorConfig config) {
		super();
		super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		m_defaultReport = config.getDefaultStandardReport();
		m_customReport = config.getCustomStandardReport();

		// Create Select Report dropdown
		m_comboBox = new JComboBox();
		boolean isFirst = true;
		for (StandardCategory stdCat : config.getStandardCategories()) {
			if (!isFirst) {
				m_comboBox.addItem("--------------------");// category divider
			}
			m_comboBox.addItem(stdCat);
			for (StandardReport stdRep : stdCat.getStandardReports()) {
				m_comboBox.addItem(stdRep);
			}
			isFirst = false;
		}
		// custom renderer to display the category and report labels,
		// and to disable the category and divider entries.
		final ListCellRenderer r = m_comboBox.getRenderer();// default renderer
		m_comboBox.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Component c;
				if (value instanceof StandardCategory) {// disable the category
														// list items
					String label = ((StandardCategory) value).getLabel();
					c = r.getListCellRendererComponent(list, label, index, false, false);
					c.setEnabled(false);
				} else if (value instanceof StandardReport) {
					String label = ((StandardReport) value).getLabel();
					c = r.getListCellRendererComponent(list, label, index, isSelected,
							cellHasFocus);
					c.setEnabled(true);
				} else {// divider (String)
					c = r.getListCellRendererComponent(list, value, index, false, false);
					c.setEnabled(false);
				}
				return c;
			}
		});
		m_comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				Object item = cb.getSelectedItem();
				if (item instanceof StandardReport) {
					StandardReport stdReport = (StandardReport) item;
					for (ReportCategoryDialog dlg : m_reportDlgs) {
						dlg.applyStandardReport(stdReport);
					}
					m_clearAllButton.setEnabled(stdReport.isCustom());
					m_selectAllButton.setEnabled(stdReport.isCustom());
				}
			}
		});

		// Create Select All and Clear All buttons
		m_selectAllButton = createButton("Select All", true);
		m_clearAllButton = createButton("Clear All", false);

		// Create button bar
		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new FlowLayout(FlowLayout.LEFT));
		buttonBar.add(new JLabel("Select report"));
		buttonBar.add(m_comboBox);
		buttonBar.add(m_selectAllButton);
		buttonBar.add(m_clearAllButton);
		super.add(buttonBar);

		// Add the report field dialogs for each report category
		List<ReportCategory> categories = config.getReportCategories();
		for (ReportCategory category : categories) {
			if (!category.isHidden()) {
				ReportCategoryDialog reportDlg = new ReportCategoryDialog(category);
				super.add(reportDlg);
				m_reportDlgs.add(reportDlg);
			}
		}
	}

	/**
	 * Loads dialog settings.
	 *
	 * @param settings
	 *            the settings
	 * @param specs
	 *            the specs
	 * @throws NotConfigurableException
	 */
	public final void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		assert settings != null;
		assert specs != null;
		for (ReportCategoryDialog reportDialog : m_reportDlgs) {
			reportDialog.loadSettingsFrom(settings, specs);
		}
		try {
			updateComponent(settings.getString(PdbConnectorNodeModel.STD_REPORT_KEY));
		} catch (InvalidSettingsException e) {
			logger.warn("Error loading string for key " + PdbConnectorNodeModel.STD_REPORT_KEY);
			updateComponent(null);
		}
	}

	/**
	 * Saves dialog settings.
	 *
	 * @param settings
	 *            the settings
	 * @throws InvalidSettingsException
	 */
	public final void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		for (ReportCategoryDialog reportDialog : m_reportDlgs) {
			reportDialog.saveSettingsTo(settings);
		}
		// Save the standard report ID
		Object o = m_comboBox.getSelectedItem();
		if (o instanceof StandardReport) {
			settings.addString(PdbConnectorNodeModel.STD_REPORT_KEY, ((StandardReport) o).getId());
		}
		// Save the default report ID if the selected item is not a standard
		// report (could be a divider or category heading)
		else if (m_defaultReport != null) {
			settings.addString(PdbConnectorNodeModel.STD_REPORT_KEY, m_defaultReport.getId());
		}
	}

	/**
	 * Sets the selected standard report.
	 *
	 * @param reportId
	 *            the report id to set
	 * @return true, if successful
	 */
	private boolean setSelectedReport(String reportId) {
		boolean retVal = false;
		if (reportId != null) {
			for (int i = 0, length = m_comboBox.getItemCount(); (i < length) && !retVal; i++) {
				Object item = m_comboBox.getItemAt(i);
				if (item instanceof StandardReport) {
					final StandardReport report = (StandardReport) item;
					if (report.getId().equals(reportId)) {
						m_comboBox.setSelectedItem(report);
						retVal = true;
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * Updates the report combobox selection with the given standard report.
	 *
	 * If the report id is invalid, resets selection to the default report.
	 * 
	 * @param reportId
	 *            the report id to set
	 * @return true, if successful
	 */
	private boolean updateComponent(String reportId) {
		boolean retVal = setSelectedReport(reportId);
		//
		if (!retVal && (m_defaultReport != null)) {
			m_comboBox.setSelectedItem(m_defaultReport);
			retVal = true;
		}
		return retVal;
	}

	/**
	 * Creates button to select all or clear all fields.
	 *
	 * On mouse click, the report fields are all selected or cleared and the
	 * custom report is set.
	 * 
	 * @param label
	 *            the button label
	 * @param isSelected
	 *            the selection state to apply
	 * @return the button
	 */
	private JButton createButton(final String label, final boolean isSelected) {
		JButton retVal = new JButton(label);
		// Use MouseListener instead of ActionListener
		// so button will respond to clicks even when disabled.
		retVal.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(final MouseEvent arg0) {
				setSelectedReport(m_customReport.getId());
				for (ReportCategoryDialog dlg : m_reportDlgs) {
					dlg.selectFields(isSelected);
					dlg.updateCheckBox();
				}
			}

			@Override
			public void mouseEntered(final MouseEvent arg0) {
			}

			@Override
			public void mouseExited(final MouseEvent arg0) {
			}

			@Override
			public void mousePressed(final MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(final MouseEvent arg0) {
			}
		});
		return retVal;
	}
}
