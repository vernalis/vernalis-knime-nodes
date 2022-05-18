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

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum for the options for Row Key policies in loop end
 * 
 * v1.32.0 Updated to implement {@link ButtonGroupEnumInterface}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public enum RowPolicies implements ButtonGroupEnumInterface {

	GENERATE_NEW("Generate new Row IDs"),
	APPEND_SUFFIX("Unique Row IDs by appending suffix"),
	UNMODIFIED("Leave Row IDs unchanged");

	private final String displayText;

	private RowPolicies(String displayText) {
		this.displayText = displayText;
	}

	/**
	 * @return The action command for the button group
	 */
	@Override
	public String getActionCommand() {
		return this.name();
	}

	/**
	 * @return The text to be displayed in the dialog
	 */
	public String getDisplayText() {
		return displayText;
	}

	@Override
	public String getText() {
		return getDisplayText();
	}

	@Override
	public String getToolTip() {
		return getDisplayText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default option
	 */
	public static RowPolicies getDefault() {
		return APPEND_SUFFIX;
	}

	/**
	 * Method to get the correct option based on a legacy 'Generate unique
	 * RowIds' setting
	 * 
	 * @param uniqueRowIds
	 *            The value of the legacy setting
	 * 
	 * @return The required value
	 */
	public static RowPolicies getFromUniqueRowIDs(boolean uniqueRowIds) {
		return uniqueRowIds ? APPEND_SUFFIX : UNMODIFIED;
	}

}
