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

import java.util.Arrays;

import org.knime.base.node.meta.looper.AbstractLoopEndNodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * NodeSettings class for the multiport loop end nodes
 */
public class AbstractMultiPortLoopEndSettings extends
		AbstractLoopEndNodeSettings {

	/** The m_num ports. */
	Integer m_numPorts;

	/** The m_ignore empty tables. */
	private boolean[] m_ignoreEmptyTables;

	/** The m_inactivate disconnected branches. */
	private boolean m_inactivateDisconnectedBranches;

	/**
	 * Constructor - requires the number of ports to be specified.
	 * 
	 * @param numPorts
	 *            the num ports
	 */
	public AbstractMultiPortLoopEndSettings(Integer numPorts) {
		this.m_numPorts = numPorts;
		m_ignoreEmptyTables = new boolean[numPorts];
		Arrays.fill(m_ignoreEmptyTables, true);
		this.m_inactivateDisconnectedBranches = true;
	}

	/**
	 * Set the node to return active branches for missing inputs.
	 */
	public void activateDisconnectedBranches() {
		this.m_inactivateDisconnectedBranches = false;
	}

	/**
	 * Set the node to return inactive branches for missing inputs.
	 */
	public void deactivateDisconnectedBranches() {
		this.m_inactivateDisconnectedBranches = true;
	}

	/**
	 * Check whether to ignore empty tables at the specified Port.
	 * 
	 * @param portId
	 *            The port index
	 * @return true, if successful
	 */
	public boolean ignoreEmptyTables(final int portId) {
		return m_ignoreEmptyTables[portId];
	}

	/**
	 * Set whether to ignore empty tables at the specified Port.
	 * 
	 * @param portId
	 *            The port index
	 * @param ignore
	 *            true to ignore, false not to ignore
	 */
	public void ignoreEmptyTables(final int portId, final boolean ignore) {
		m_ignoreEmptyTables[portId] = ignore;
	}

	/**
	 * Check whether to return inactive ports for disconnected branches.
	 * 
	 * @return true, if successful
	 */
	public boolean inactivateDisconnectedBranches() {
		return m_inactivateDisconnectedBranches;

	}

	/**
	 * Loads the settings from the node settings object.
	 * 
	 * @param settings
	 *            a node settings object
	 */
	@Override
	public void loadSettings(final NodeSettingsRO settings) {
		super.loadSettings(settings);
		for (int i = 0; i < m_numPorts; i++) {
			m_ignoreEmptyTables[i] = settings.getBoolean("ignoreEmptyTables"
					+ i, true);
		}
		m_inactivateDisconnectedBranches = settings.getBoolean(
				"inactivateDisconnectedBranches", true);
	}

	/**
	 * Save the settings.
	 * 
	 * @param settings
	 *            the settings
	 */
	@Override
	public void saveSettings(final NodeSettingsWO settings) {
		super.saveSettings(settings);
		for (int i = 0; i < m_numPorts; i++) {
			settings.addBoolean("ignoreEmptyTables" + i, m_ignoreEmptyTables[i]);
		}
		settings.addBoolean("inactivateDisconnectedBranches",
				m_inactivateDisconnectedBranches);
	}

	/**
	 * Set the behaviour for disconnected ports.
	 * 
	 * @param b
	 *            true to return inactive branches, false to return empty tables
	 */
	public void setInactivatedDisconnectedBranches(final boolean b) {
		this.m_inactivateDisconnectedBranches = b;

	}

}
