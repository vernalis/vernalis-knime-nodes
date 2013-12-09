/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.QueryParam;

/**
 * QueryOptionDialog class.
 *
 * A QueryOptionDialog defines the user interface dialog components for a single query option.
 * It contains:
 * <UL>
 * <LI>The query option (and the list of child query parameters) that are represented by the dialog</LI>
 * <LI>A Boolean settings model to control selection of the query option</LI>
 * <LI>A checkbox to act as the dialog component for the selection settings model</LI>
 * <LI>A collection of dialog components to represent the query parameters</LI>
 * </UL>
 *
 * @see QueryOption
 * @see QueryParam
 * @see QueryOptionModel
 */
@SuppressWarnings("serial")
public class QueryOptionDialog extends JPanel {
	private final QueryOption m_queryOption;
	private SettingsModelBoolean m_selected;
	private JCheckBox m_checkBox = null;
	private final List<QueryParam> m_params;
	/** The dialog components.
	 *
	 * Inner list contains the dialog components for a single query param.
	 * Outer list is for each query param in m_params.
	 */
	private final List<List<DialogComponent>> m_dlgs = new ArrayList<List<DialogComponent>>();

	/**
	 * Instantiates a new query option dialog for a given query option.
	 *
	 * @param queryOption the query option
	 */
	public QueryOptionDialog(final QueryOption queryOption) {
		m_queryOption = queryOption;
		super.setLayout(new GridBagLayout());
		super.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), m_queryOption.getLabel()));
		m_selected = ComponentFactory.createSelectionSettingsModel(m_queryOption);
		boolean isSelected = m_selected.getBooleanValue();
		//Could use a DialogComponentBoolean here, but the checkbox is lighter (no surrounding JPanel)
		m_checkBox = new JCheckBox("Selected");
		m_checkBox.setSelected(isSelected);
		m_checkBox.addItemListener(new ItemListener() {
			@Override
            public void itemStateChanged(final ItemEvent e) {
				m_selected.setBooleanValue(m_checkBox.isSelected());
			}
		});
		m_selected.addChangeListener(new ChangeListener() {
			@Override
            public void stateChanged(final ChangeEvent e) {
				final boolean isSelected = m_selected.getBooleanValue();
				if (isSelected != m_checkBox.isSelected()) {
					m_checkBox.setSelected(isSelected);
				}
				for (final List<DialogComponent> dlgs : m_dlgs) {
					for (final DialogComponent dlg : dlgs) {
						dlg.getModel().setEnabled(isSelected);
						final JPanel dlgPanel = dlg.getComponentPanel();
						dlgPanel.setEnabled(isSelected);
						//Make sure all child components of the dialog are also enabled/disabled...
						for (final Component com : ComponentFactory.getAllSubComponents(dlgPanel,null)) {
							com.setEnabled(isSelected);
						}
					}
				}
			}
		});
		int iRow = 0;
		int iCol = 0;
		//Add checkBox to occupy whole of first column, anchored to the centre left (WEST)
		GridBagConstraints cChk = new GridBagConstraints();
		cChk.fill = GridBagConstraints.NONE;//do not resize component
		cChk.weightx = 1.0;//add any spare width to this column
		cChk.gridx = iCol;//x cell coord
		cChk.gridy = iRow;//y cell coord
		cChk.gridwidth = 1;//occupy one column
		cChk.gridheight = GridBagConstraints.REMAINDER;//occupy all rows
		cChk.anchor = GridBagConstraints.WEST;//anchor to centre left
		super.add(m_checkBox,cChk);
		//Create dialog components for each parameter, with enabled/disabled status in sync with the
		//selection checkbox.
		iCol += cChk.gridwidth;
		m_params = m_queryOption.getParams();
		Iterator<QueryParam> iter = m_params.iterator();
		while (iter.hasNext()) {
			final QueryParam param = iter.next();
			final List<DialogComponent> dlgs = ComponentFactory.createDialogComponents(param, isSelected);
			m_dlgs.add(dlgs);
			//Add all components for this param to a JPanel
			final JPanel subPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			for (DialogComponent dlg : dlgs) {
				final JPanel dlgPanel = dlg.getComponentPanel();
				dlgPanel.setEnabled(isSelected);
				dlgPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
				subPanel.add(dlgPanel);
				//Make sure all child components of the dialog are also enabled/disabled...
				for (final Component com : ComponentFactory.getAllSubComponents(dlgPanel,null)) {
					com.setEnabled(isSelected);
					//add mouse click listener unless this is a label
					if (!(com instanceof JLabel)) {
						com.addMouseListener(new MouseListener() {
							@Override
							public void mouseClicked(final MouseEvent arg0) {
								if (!m_selected.getBooleanValue()) {
									m_selected.setBooleanValue(true);
									arg0.getComponent().requestFocusInWindow();
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
					}
				}
			}
			//Determine how many columns the subpanel should occupy, based on its preferred width
			int panelWidth = (int)subPanel.getPreferredSize().getWidth();
			int gridWidth = (panelWidth + Properties.QUERY_LAYOUT_COL_WIDTH - 1) / Properties.QUERY_LAYOUT_COL_WIDTH;
			int ipadx = gridWidth * Properties.QUERY_LAYOUT_COL_WIDTH - panelWidth;
			//Start new line if we are going to exceed maximum number of columns
			if ((iCol + gridWidth - 1) > Properties.QUERY_LAYOUT_COLUMNS) {
				iRow++;
				iCol = 1;
			}
			//Check if this panel alone exceeds max number of columns
			boolean isTooWide = (gridWidth > Properties.QUERY_LAYOUT_COLUMNS);
			//Check for orphaned last panel and ensure is right justified (by using remainder of grid width)
			boolean isOrphan = !iter.hasNext() && (iCol == 1);
			GridBagConstraints cDlg = new GridBagConstraints();
			cDlg.fill = GridBagConstraints.NONE;
			cDlg.weightx = 0.0;
			cDlg.gridx = iCol;
			cDlg.gridy = iRow;
			cDlg.gridwidth = (isTooWide || isOrphan) ? GridBagConstraints.REMAINDER : gridWidth;
			cDlg.gridheight = 1;
			cDlg.anchor = GridBagConstraints.EAST;
			cDlg.ipadx = isTooWide ? 0 : ipadx;
			super.add(subPanel,cDlg);
			iCol += gridWidth;
			if (iCol > Properties.QUERY_LAYOUT_COLUMNS) {
				iRow++;
				iCol = 1;
			}
		}
	}

	/**
	 * Gets the query option.
	 *
	 * @return the query option
	 */
	public final QueryOption getQueryOption() {
		return m_queryOption;
	}

	/**
	 * Sets the selection status.
	 *
	 * @param isSelected the new selection status
	 */
	public void setSelected(final boolean isSelected) {
		m_selected.setBooleanValue(isSelected);
	}

	/**
	 * Reset reset all dialog parameters to default values.
	 */
	public void resetParams() {
		Iterator<QueryParam> paramIter = m_params.iterator();
		Iterator<List<DialogComponent>> dlgsIter = m_dlgs.iterator();
		while (paramIter.hasNext() && dlgsIter.hasNext()) {
			final QueryParam param = paramIter.next();
			final List<DialogComponent> dlgs = dlgsIter.next();
			ComponentFactory.resetDialogComponents(param, dlgs);
		}
	}

	/**
	 * Loads dialog settings.
	 *
	 * Calls {@link DialogComponent#loadSettingsFrom(NodeSettingsRO, PortObjectSpec[])}
	 * on each dialog component, and {@link SettingsModel#loadSettingsFrom(NodeSettingsRO)}
	 * on the selection settings model.
	 *
	 * @param settings the settings
	 * @param specs the specs
	 * @throws NotConfigurableException
	 */
	public final void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		assert settings != null;
		assert specs != null;
		try {
			m_selected.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
		}

		for (List<DialogComponent> dlgs : m_dlgs) {
			for (DialogComponent dlg : dlgs) {
				dlg.loadSettingsFrom(settings, specs);
			}
		}
	}

	/**
	 * Saves dialog settings.
	 *
	 * Calls {@link DialogComponent#saveSettingsTo(NodeSettingsWO)} on each dialog component,
	 * and {@link SettingsModel#saveSettingsTo(NodeSettingsWO)} on the selection
	 * settings model.
	 *
	 * @param settings the settings
	 * @throws InvalidSettingsException
	 */
	public final void saveSettingsTo(final NodeSettingsWO settings)
	throws InvalidSettingsException {
		m_selected.saveSettingsTo(settings);
		for (List<DialogComponent> dlgs : m_dlgs) {
			for (DialogComponent dlg : dlgs) {
				dlg.saveSettingsTo(settings);
			}
		}
	}
}
