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
package com.vernalis.knime.flowvar.nodes.io.read;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * enum for the duplicate variable policy.
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 * 
 */
public enum DuplicateVariablePolicy implements ButtonGroupEnumInterface {

	/** The overwrite. */
	OVERWRITE("Overwrite existing variable with value from file"),
	/** The ignore. */
	IGNORE("Ignore the value from the file"),
	/** The rename. */
	RENAME("Rename variable from file with uniquified name"),
	/** The rename different. */
	RENAME_DIFFERENT("Rename variable from file if different",
			"Only rename variables from the file if they already exist "
					+ "and have a different value or type to that in the file.");

	/** The name. */
	private final String m_name;

	/** The tooltip. */
	private final String m_tooltip;

	/**
	 * Constructor for the duplicate variable options.
	 * 
	 * @param name
	 *            The name to display
	 * @param tooltip
	 *            The tooltip to display
	 */
	DuplicateVariablePolicy(final String name, final String tooltip) {
		m_name = name;
		m_tooltip = tooltip;
	}

	/**
	 * Constructor for the timed loop missing value options.
	 * 
	 * @param name
	 *            The name to display
	 */
	DuplicateVariablePolicy(final String name) {
		m_name = name;
		m_tooltip = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getText()
	 */
	@Override
	public String getText() {
		return m_name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getActionCommand()
	 */
	@Override
	public String getActionCommand() {
		return this.name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getToolTip()
	 */
	@Override
	public String getToolTip() {
		return m_tooltip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#isDefault()
	 */
	@Override
	public boolean isDefault() {
		return this.equals(DuplicateVariablePolicy.getDefaultMethod());
	}

	/**
	 * Gets the default method.
	 * 
	 * @return The default method
	 */
	public static DuplicateVariablePolicy getDefaultMethod() {
		return DuplicateVariablePolicy.RENAME;
	}
}
