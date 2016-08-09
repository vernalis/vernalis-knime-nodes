/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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

import javax.swing.JCheckBox;

import org.knime.base.node.meta.looper.AbstractLoopEndNodeDialog;

/**
 * <code>NodeDialog</code> for the "AbstractMultiPortLoopEnd" Node. Loop end
 * node to handle optional input ports
 * 
 * This node dialog derives from {@link AbstractLoopEndNodeDialog}.
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 */
public class AbstractMultiPortLoopEndNodeDialog extends
		AbstractLoopEndNodeDialog<AbstractMultiPortLoopEndSettings> {

	/** The number of input ports. */
	private final int m_NumPorts;

	/** The ignore empty tables checkboxes. */
	private final JCheckBox[] m_ignoreEmptyTables;

	/** The inactivate disconnected inputs checkbox. */
	private final JCheckBox m_inactivateDisconnecteds;

	/**
	 * New pane for configuring AbstractMultiPortLoopEnd node dialog. Default
	 * implementation, assuming optional inports.
	 * 
	 * @param numPorts
	 *            the num ports
	 */
	public AbstractMultiPortLoopEndNodeDialog(final int numPorts) {
		super(new AbstractMultiPortLoopEndSettings(numPorts));
		m_NumPorts = numPorts;
		m_ignoreEmptyTables = new JCheckBox[numPorts];
		for (int i = 0; i < m_NumPorts; i++) {
			m_ignoreEmptyTables[i] = new JCheckBox(
					"Ignore empty input tables at port " + i);
			addComponent(m_ignoreEmptyTables[i]);
		}
		m_inactivateDisconnecteds = new JCheckBox(
				"Return inactive branches for disconnected optional ports");
		addComponent(m_inactivateDisconnecteds);

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
	public AbstractMultiPortLoopEndNodeDialog(final int numPorts,
			final boolean optionalInPorts) {
		super(new AbstractMultiPortLoopEndSettings(numPorts));
		m_NumPorts = numPorts;
		m_ignoreEmptyTables = new JCheckBox[numPorts];
		for (int i = 0; i < m_NumPorts; i++) {
			m_ignoreEmptyTables[i] = new JCheckBox(
					"Ignore empty input tables at port " + i);
			addComponent(m_ignoreEmptyTables[i]);
		}
		m_inactivateDisconnecteds = new JCheckBox(
				"Return inactive branches for disconnected optional ports");
		if (optionalInPorts) {
			addComponent(m_inactivateDisconnecteds);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.base.node.meta.looper.AbstractLoopEndNodeDialog#addToSettings
	 * (org.knime.base.node.meta.looper.AbstractLoopEndNodeSettings)
	 */
	@Override
	protected void addToSettings(final AbstractMultiPortLoopEndSettings settings) {
		// We use the addToSettings which is called by the superclass as being
		// simpler to implement
		for (int i = 0; i < m_NumPorts; i++) {
			settings.ignoreEmptyTables(i, m_ignoreEmptyTables[i].isSelected());
		}
		settings.setInactivatedDisconnectedBranches(m_inactivateDisconnecteds
				.isSelected());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.base.node.meta.looper.AbstractLoopEndNodeDialog#loadFromSettings
	 * (org.knime.base.node.meta.looper.AbstractLoopEndNodeSettings)
	 */
	@Override
	protected void loadFromSettings(
			final AbstractMultiPortLoopEndSettings settings) {
		// Again we use the loadFromSettings as being simpler to implement
		for (int i = 0; i < m_NumPorts; i++) {
			m_ignoreEmptyTables[i].setSelected(settings.ignoreEmptyTables(i));
		}
		m_inactivateDisconnecteds.setSelected(settings
				.inactivateDisconnectedBranches());
	}

}
