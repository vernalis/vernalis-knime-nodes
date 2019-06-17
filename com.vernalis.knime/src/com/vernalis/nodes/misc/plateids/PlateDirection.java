/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.plateids;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * A simple Enum for the Direction in which wells should be filled within a
 * plate
 * 
 * @author s.roughley
 *
 */
public enum PlateDirection implements ButtonGroupEnumInterface {
	ROW_WISE, COLUMN_WISE;

	@Override
	public String getText() {
		String txt = name().split("_")[0];
		txt = txt.substring(0, 1).toUpperCase() + txt.substring(1).toLowerCase()
				+ "-wise";
		return txt;
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return getText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default option
	 */
	public static PlateDirection getDefault() {
		return COLUMN_WISE;
	}

	public static PlateDirection valueOfFromText(String text) {
		try {
			return valueOf(text);
		} catch (Exception e) {
			for (PlateDirection pd : values()) {
				if (pd.getText().equals(text)) {
					return pd;
				}
			}
		}
		throw new IllegalArgumentException("No enum member match found");
	}
}
