/*******************************************************************************
 * Copyright (c) 2014, 2016 Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.knime.base.node.meta.looper.AbstractLoopEndNodeDialog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * <code>NodeDialog</code> for the "AbstractMultiPortLoopEnd" Node. Loop end
 * node to handle optional input ports
 * 
 * This node dialog derives from {@link AbstractLoopEndNodeDialog}.
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class AbstractMultiPortLoopEndNodeDialog extends NodeDialogPane {
	/** The node settings */
	protected final AbstractMultiPortLoopEndSettings m_settings;

	/** The number of input ports */
	private final int m_NumPorts;

	/** The ignore empty tables checkboxes */
	private final JCheckBox[] m_ignoreEmptyTables;

	/** The allow changing table specs checkboxes */
	private final JCheckBox[] m_allowChangingSpecs;
	private final JCheckBox[] m_allowChangingColTypes;

	/** The inactivate disconnected inputs checkbox */
	private final JCheckBox m_inactivateDisconnecteds;

	/** The add iteration columns checkboxes */
	private final JCheckBox[] m_addIterationCols;

	/** The row key policy button groups */
	private final ButtonGroup[] m_rowKeyPolicies;

	/** The layout constraints for the components panel */
	private GridBagConstraints m_gbc;

	/** The panel for the components */
	private JPanel m_panel;

	/**
	 * New pane for configuring AbstractMultiPortLoopEnd node dialog. Default
	 * implementation, assuming optional inports.
	 * 
	 * @param numPorts
	 *            the num ports
	 */
	public AbstractMultiPortLoopEndNodeDialog(final int numPorts) {
		this(numPorts, true);

	}

	/**
	 * New pane for configuring AbstractMultiPortLoopEnd node dialog. Allows the
	 * setting of optional inports. Omission of this argument assumes optional
	 * inports.
	 * 
	 * @param numPorts
	 *            the num ports
	 * @param optionalInPorts
	 *            the optional in ports
	 */
	public AbstractMultiPortLoopEndNodeDialog(final int numPorts, final boolean optionalInPorts) {

		m_panel = new JPanel(new GridBagLayout());
		m_gbc = new GridBagConstraints();

		m_gbc.gridx = 0;
		m_gbc.gridy = 0;
		m_gbc.anchor = GridBagConstraints.WEST;
		m_gbc.insets = new Insets(0, 0, 5, 0);

		m_settings = new AbstractMultiPortLoopEndSettings(numPorts);
		m_NumPorts = numPorts;
		m_ignoreEmptyTables = new JCheckBox[numPorts];
		m_allowChangingSpecs = new JCheckBox[numPorts];
		m_allowChangingColTypes = new JCheckBox[numPorts];
		m_addIterationCols = new JCheckBox[numPorts];
		m_rowKeyPolicies = new ButtonGroup[numPorts];

		for (int i = 0; i < m_NumPorts; i++) {
			// A new panel for the current port
			JPanel portPanel = new JPanel();
			if (m_NumPorts > 1) {
				portPanel.setBorder(new TitledBorder("Port " + i));
			} else {
				portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.PAGE_AXIS));
			}

			// A but group for the row policy in its own panel
			m_rowKeyPolicies[i] = new ButtonGroup();
			JPanel rkPolicyPanel = new JPanel(new GridLayout(RowPolicies.values().length, 1));
			rkPolicyPanel.setBorder(new TitledBorder("Row key policy"));
			for (RowPolicies p : RowPolicies.values()) {
				JRadioButton rButton = new JRadioButton(p.getDisplayText());
				rButton.setActionCommand(p.getActionCommand());
				m_rowKeyPolicies[i].add(rButton);
				rkPolicyPanel.add(rButton);
			}
			portPanel.add(rkPolicyPanel);

			// A new panel for the boolean parameters for the port
			JPanel booleans = new JPanel(new GridLayout(0, 2));

			m_addIterationCols[i] = new JCheckBox("Add iteration column");
			booleans.add(m_addIterationCols[i]);

			m_ignoreEmptyTables[i] = new JCheckBox("Ignore empty input tables");
			booleans.add(m_ignoreEmptyTables[i]);

			m_allowChangingColTypes[i] = new JCheckBox("Allow changing column types");
			booleans.add(m_allowChangingColTypes[i]);

			m_allowChangingSpecs[i] = new JCheckBox("Allow changing table specs");
			booleans.add(m_allowChangingSpecs[i]);
			portPanel.add(booleans);
			addComponent(portPanel);
		}
		m_inactivateDisconnecteds = new JCheckBox(
				"Return inactive branches for disconnected optional ports");
		if (optionalInPorts) {
			addComponent(m_inactivateDisconnecteds);
		}
		addTab("Standard Settings", m_panel);
	}

	protected void addComponent(final Component component) {
		m_gbc.gridy++;
		m_panel.add(component, m_gbc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.base.node.meta.looper.AbstractLoopEndNodeDialog#addToSettings
	 * (org.knime.base.node.meta.looper.AbstractLoopEndNodeSettings)
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {

		for (int i = 0; i < m_NumPorts; i++) {
			m_settings.ignoreEmptyTables(i, m_ignoreEmptyTables[i].isSelected());
			m_settings.setAllowChangingTableSpecs(i, m_allowChangingSpecs[i].isSelected());
			m_settings.setAllowChangingColumnTypes(i, m_allowChangingColTypes[i].isSelected());
			m_settings.setAddIterationColumn(i, m_addIterationCols[i].isSelected());
			m_settings.setRowKeyPolicyName(i,
					m_rowKeyPolicies[i].getSelection().getActionCommand());
		}
		m_settings.setInactivatedDisconnectedBranches(m_inactivateDisconnecteds.isSelected());
		m_settings.saveSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.base.node.meta.looper.AbstractLoopEndNodeDialog#
	 * loadFromSettings
	 * (org.knime.base.node.meta.looper.AbstractLoopEndNodeSettings)
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
			throws NotConfigurableException {
		m_settings.loadSettings(settings);
		for (int i = 0; i < m_NumPorts; i++) {
			m_ignoreEmptyTables[i].setSelected(m_settings.ignoreEmptyTables(i));
			m_allowChangingSpecs[i].setSelected(m_settings.allowChangingTableSpecs(i));
			m_allowChangingColTypes[i].setSelected(m_settings.allowChangingColumnTypes(i));
			m_addIterationCols[i].setSelected(m_settings.getAddIterationColumn(i));
			String rkp = m_settings.getRowKeyPolicy(i).getActionCommand();
			for (Enumeration<AbstractButton> e = m_rowKeyPolicies[i].getElements(); e
					.hasMoreElements();) {
				AbstractButton b = e.nextElement();
				b.setSelected(b.getActionCommand().equals(rkp));
				b.setEnabled(specs[i] != null);
			}
			// Handle non-connected optional ports
			if (specs[i] == null) {
				m_ignoreEmptyTables[i].setEnabled(false);
				m_allowChangingSpecs[i].setEnabled(false);
				m_allowChangingColTypes[i].setEnabled(false);
				m_addIterationCols[i].setEnabled(false);
			} else {
				m_ignoreEmptyTables[i].setEnabled(true);
				m_allowChangingSpecs[i].setEnabled(true);
				m_allowChangingColTypes[i].setEnabled(true);
				m_addIterationCols[i].setEnabled(true);
			}
		}
		m_inactivateDisconnecteds.setSelected(m_settings.inactivateDisconnectedBranches());
	}

}
