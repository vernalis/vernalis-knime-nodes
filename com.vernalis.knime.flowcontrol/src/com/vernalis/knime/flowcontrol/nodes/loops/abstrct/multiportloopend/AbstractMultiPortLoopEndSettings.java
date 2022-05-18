/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.util.Arrays;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * NodeSettings class for the multiport loop end nodes, allowing setting all
 * properties except the inactivate disconnected branches property on a
 * port-by-port basis. The settings maintain backwards compatibility with legacy
 * workflows.
 * 
 * @version 1.29.0 Settings keys made package visible for possible future
 *          NodeMigrationRule
 * @author S.Roughley knime@vernalis.com
 */
public class AbstractMultiPortLoopEndSettings {

	private static final String CFG_INACTIVATE_DISCONNECTED_BRANCHES =
			"inactivateDisconnectedBranches";

	/** The key for the row key policy setting */
	static final String CFG_ROW_KEY_POLICY = "rowKeyPolicy";

	/** The key for the add iteration column setting */
	static final String CFG_ADD_ITERATION_COLUMN = "addIterationColumn";

	/** The key for the allow changing column types setting */
	static final String CFG_ALLOW_CHANGING_COL_TYPES = "allowChangingColTypes";

	/** The key for the allow changins specs setting */
	static final String CFG_ALLOW_CHANGING_SPECS = "allowChangingSpecs";

	/** The key for the ignore empty tables setting */
	static final String CFG_IGNORE_EMPTY_TABLES = "ignoreEmptyTables";

	/** The key for the unique row ids setting */
	static final String CFG_UNIQUE_ROW_IDS = "uniqueRowIDs";

	/** The m_num ports. */
	Integer m_numPorts;

	/** The m_ignore empty tables. */
	private boolean[] m_ignoreEmptyTables;

	/** The m_inactivate disconnected branches. */
	private boolean m_inactivateDisconnectedBranches;

	/** Allow table specs at each port to change or not */
	private boolean[] m_allowChangingTableSpecs;

	/** Allow column types (but not names!) to change */
	private boolean[] m_allowChangingColumnTypes;

	/** Row key policies */
	private RowPolicies[] rowKeyPolicies;

	private boolean[] m_addIterationColumns;

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
		m_allowChangingTableSpecs = new boolean[numPorts];
		Arrays.fill(m_allowChangingTableSpecs, false);
		m_allowChangingColumnTypes = new boolean[numPorts];
		Arrays.fill(m_allowChangingColumnTypes, false);
		rowKeyPolicies = new RowPolicies[numPorts];
		Arrays.fill(rowKeyPolicies, RowPolicies.getDefault());
		m_addIterationColumns = new boolean[numPorts];
		Arrays.fill(m_addIterationColumns, true);
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
	 * Check whether to return inactive ports for disconnected branches.
	 * 
	 * @return true, if successful
	 */
	public boolean inactivateDisconnectedBranches() {
		return m_inactivateDisconnectedBranches;

	}

	/**
	 * Check whether to ignore empty tables at the specified Port.
	 * 
	 * @param portId
	 *            The port index
	 * @return true, if successful
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public boolean ignoreEmptyTables(final int portId)
			throws IllegalArgumentException {
		validatePortID(portId);
		return m_ignoreEmptyTables[portId];
	}

	/**
	 * Set whether to ignore empty tables at the specified Port.
	 * 
	 * @param portId
	 *            The port index
	 * @param ignore
	 *            true to ignore, false not to ignore
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public void ignoreEmptyTables(final int portId, final boolean ignore)
			throws IllegalArgumentException {
		validatePortID(portId);
		m_ignoreEmptyTables[portId] = ignore;
	}

	/**
	 * @param portId
	 *            The port index
	 * @return Whether the table spec is allowed to change
	 * @throws IllegalArgumentException
	 *             If the port ID is not valid
	 */
	public boolean allowChangingTableSpecs(final int portId)
			throws IllegalArgumentException {
		validatePortID(portId);
		return m_allowChangingTableSpecs[portId];
	}

	/**
	 * Set the allow changing specs value for the port
	 * 
	 * @param portId
	 *            The port index
	 * @param allowChangingTableSpec
	 *            The value to set
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public void setAllowChangingTableSpecs(final int portId,
			boolean allowChangingTableSpec) throws IllegalArgumentException {
		validatePortID(portId);
		m_allowChangingTableSpecs[portId] = allowChangingTableSpec;
	}

	/**
	 * @param portId
	 *            The port index
	 * @return Whether the column types are allowed to change
	 * @throws IllegalArgumentException
	 *             If the port ID is not valid
	 */
	public boolean allowChangingColumnTypes(final int portId)
			throws IllegalArgumentException {
		validatePortID(portId);
		return m_allowChangingColumnTypes[portId];
	}

	/**
	 * Set the allow changing column types value for the port
	 * 
	 * @param portId
	 *            The port index
	 * @param allowChangingColumnTypes
	 *            The value to set
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public void setAllowChangingColumnTypes(final int portId,
			boolean allowChangingColumnTypes) throws IllegalArgumentException {
		validatePortID(portId);
		m_allowChangingColumnTypes[portId] = allowChangingColumnTypes;
	}

	/**
	 * Get the row key policy for the port
	 * 
	 * @param portId
	 *            the port index
	 * @return The row key policy
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public RowPolicies getRowKeyPolicy(int portId)
			throws IllegalArgumentException {
		validatePortID(portId);
		return rowKeyPolicies[portId];
	}

	/**
	 * Set the row key policy for the port
	 * 
	 * @param portId
	 *            The port index
	 * @param policy
	 *            The value to set
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public void setRowKeyPolicy(int portId, RowPolicies policy)
			throws IllegalArgumentException {
		validatePortID(portId);
		rowKeyPolicies[portId] = policy;
	}

	/**
	 * Set the row key policy for the port from a string name
	 * 
	 * @param portId
	 *            The port index
	 * @param policyName
	 *            The value to set
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied or the policy name cannot
	 *             be resolved
	 */
	public void setRowKeyPolicyName(int portId, String policyName)
			throws IllegalArgumentException {
		validateRowKeyPolicyName(policyName);
		setRowKeyPolicy(portId, RowPolicies.valueOf(policyName));
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

	/**
	 * @param portId
	 *            The port index
	 * @return Whether an iteration column should be added to the table
	 * @throws IllegalArgumentException
	 *             If the port ID is not valid
	 */
	public boolean getAddIterationColumn(int portId)
			throws IllegalArgumentException {
		validatePortID(portId);
		return m_addIterationColumns[portId];
	}

	/**
	 * Set the add iteratione column setting for the port
	 * 
	 * @param portId
	 *            The port index
	 * @param addIterationColumn
	 *            The value to set
	 * @throws IllegalArgumentException
	 *             if an invalid port id is supplied
	 */
	public void setAddIterationColumn(int portId, boolean addIterationColumn)
			throws IllegalArgumentException {
		validatePortID(portId);
		m_addIterationColumns[portId] = addIterationColumn;
	}

	/**
	 * Helper method to validate the row key policy name
	 * 
	 * @param policyName
	 *            The policy name
	 * @throws IllegalArgumentException
	 *             If an invalid name is supplied
	 */
	private void validateRowKeyPolicyName(String policyName)
			throws IllegalArgumentException {
		for (RowPolicies p : RowPolicies.values()) {
			if (p.getActionCommand().equals(policyName)) {
				return;
			}
		}
		throw new IllegalArgumentException(
				"Policy must be one of accepted values"
						+ Arrays.toString(RowPolicies.values()));

	}

	/**
	 * Helper method to ensure a valid port index is supplied
	 * 
	 * @param portId
	 *            The port index
	 * @throws IllegalArgumentException
	 *             If an invalid port index is supplied
	 */
	private void validatePortID(int portId) throws IllegalArgumentException {
		if (portId < 0 || portId >= m_numPorts) {
			throw new IllegalArgumentException(
					"Port index must be in range 0 - " + (m_numPorts - 1));
		}
	}

	/**
	 * Loads the settings from the node settings object.
	 * 
	 * @param settings
	 *            a node settings object
	 */
	public void loadSettings(final NodeSettingsRO settings) {
		for (int i = 0; i < m_numPorts; i++) {
			m_ignoreEmptyTables[i] =
					settings.getBoolean(CFG_IGNORE_EMPTY_TABLES + i, true);
			// Following are both false for backwards compatibility
			m_allowChangingTableSpecs[i] =
					settings.getBoolean(CFG_ALLOW_CHANGING_SPECS + i, false);
			m_allowChangingColumnTypes[i] = settings
					.getBoolean(CFG_ALLOW_CHANGING_COL_TYPES + i, false);
			// Try old settings name first for backwards compatability
			m_addIterationColumns[i] =
					settings.containsKey(CFG_ADD_ITERATION_COLUMN)
							? settings.getBoolean(CFG_ADD_ITERATION_COLUMN,
									true)
							: settings.getBoolean(CFG_ADD_ITERATION_COLUMN + i,
									true);
			// old settings -> check for backwards compatibility
			// Complicated as 2 'layers' of back compatibility...
			// use setter to validate strings
			if (settings.containsKey(CFG_UNIQUE_ROW_IDS)) {
				boolean uniqueRowIDs =
						settings.getBoolean(CFG_UNIQUE_ROW_IDS, false);
				setRowKeyPolicy(i,
						RowPolicies.getFromUniqueRowIDs(uniqueRowIDs));
			} else if (settings.containsKey(CFG_ROW_KEY_POLICY)) {
				setRowKeyPolicyName(i, settings.getString(CFG_ROW_KEY_POLICY,
						RowPolicies.getDefault().getActionCommand()));
			} else {
				setRowKeyPolicyName(i,
						settings.getString(CFG_ROW_KEY_POLICY + i,
								RowPolicies.getDefault().getActionCommand()));
			}
		}
		m_inactivateDisconnectedBranches =
				settings.getBoolean(CFG_INACTIVATE_DISCONNECTED_BRANCHES, true);

	}

	/**
	 * Save the settings.
	 * 
	 * @param settings
	 *            the settings
	 */
	public void saveSettings(final NodeSettingsWO settings) {
		for (int i = 0; i < m_numPorts; i++) {
			settings.addBoolean(CFG_IGNORE_EMPTY_TABLES + i,
					m_ignoreEmptyTables[i]);
			settings.addBoolean(CFG_ALLOW_CHANGING_SPECS + i,
					m_allowChangingTableSpecs[i]);
			settings.addBoolean(CFG_ALLOW_CHANGING_COL_TYPES + i,
					m_allowChangingColumnTypes[i]);
			settings.addBoolean(CFG_ADD_ITERATION_COLUMN + i,
					m_addIterationColumns[i]);
			settings.addString(CFG_ROW_KEY_POLICY + i,
					rowKeyPolicies[i].getActionCommand());
		}
		settings.addBoolean(CFG_INACTIVATE_DISCONNECTED_BRANCHES,
				m_inactivateDisconnectedBranches);
	}

}
