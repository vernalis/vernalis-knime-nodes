/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.trimtable;

import org.knime.core.data.DataRow;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * An enum with the row filter behaviour
 * 
 * @author s.roughley
 *
 */
enum TrimBehaviour implements ButtonGroupEnumInterface {
	Require_All {

		@Override
		boolean testRow(DataRow row, int[] colIdx) {
			for (int i : colIdx) {
				if (row.getCell(i).isMissing()) {
					return false;
				}
			}
			return true;
		}
	},
	Require_Any {

		@Override
		boolean testRow(DataRow row, int[] colIdx) {
			for (int i : colIdx) {
				if (!row.getCell(i).isMissing()) {
					return true;
				}
			}
			return false;
		}
	};

	@Override
	public String getText() {
		return name().replace("_", " ");
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return getText() + " cells to be non-missing";
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public static TrimBehaviour getDefault() {
		return Require_Any;
	}

	/**
	 * @param row
	 *            The row to test
	 * @param colIdx
	 *            The column indices to test
	 * @return {@code true} if the table should be included in the table
	 */
	abstract boolean testRow(DataRow row, int[] colIdx);
}
