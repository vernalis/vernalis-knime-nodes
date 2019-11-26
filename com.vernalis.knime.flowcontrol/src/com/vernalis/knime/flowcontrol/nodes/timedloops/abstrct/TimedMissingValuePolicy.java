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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * enum for the missing value policy for flow variable based timed loop starts.
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 * 
 */
public enum TimedMissingValuePolicy implements ButtonGroupEnumInterface {
	/** Fail the nodes execution. */
	FAIL("Fail",
			"Node execution fails if row has missing values, or no input rows"),

	/** Assign default values. */
	DEFAULT("Use Defaults",
			"Default values are used in place of missing values"),

	/**
	 * Omit missing values, and add row to unprocessed rows table in loop end
	 * node.
	 */
	SKIP_ADDTOUNPROCESSED(
			"Skip (Add to unprocessed rows table)",
			"The row is skipped during execution, but will be added to the unprocessed rows table in"
					+ " the corresponding loop end node"),

	/** Skip missing values. */
	SKIP("Skip (Don't add to unprocessed rows table)",
			"The row is skipped during execution");

	/** The name. */
	private final String m_name;

	/** The tooltip. */
	private final String m_tooltip;

	/**
	 * Constructor for the timed loop missing value options.
	 * 
	 * @param name
	 *            The name to display
	 * @param tooltip
	 *            The tooltip to display
	 */
	TimedMissingValuePolicy(final String name, final String tooltip) {
		m_name = name;
		m_tooltip = tooltip;
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
		return this.equals(TimedMissingValuePolicy.getDefaultMethod());
	}

	/**
	 * Gets the default method.
	 * 
	 * @return The default method
	 */
	public static TimedMissingValuePolicy getDefaultMethod() {
		return TimedMissingValuePolicy.SKIP_ADDTOUNPROCESSED;
	}
}
