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
package com.vernalis.knime.db.nodes.replaceheader;

import java.util.List;
import java.util.Map;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum with the options for what should happen if the lookup table does not
 * contain a match
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public enum MissingColumnAction implements ButtonGroupEnumInterface {

	/** Node exection will fail */
	Fail {

		@Override
		public String getToolTip() {
			return "Node execution will fail if the header table "
					+ "does not contain all the column names in the incoming database";
		}

		@Override
		public boolean testColumn(String testColName,
				Map<String, String> colNamesMap, List<String> inColumnsToKeep) {
			if (!colNamesMap.containsKey(testColName)) {
				return false;
			}

			inColumnsToKeep.add(testColName);
			return true;

		}

	},

	/** Unmatched columns will be left unchanged */
	Leave_Unchanged {

		@Override
		public String getToolTip() {
			return "Any columns not present in the header table will "
					+ "be passed through to the output table unchanged";
		}

		@Override
		public boolean testColumn(String testColName,
				Map<String, String> colNamesMap, List<String> inColumnsToKeep) {
			inColumnsToKeep.add(testColName);
			return true;
		}

	},

	/** Unmatched columns will be omitted */
	Omit {

		@Override
		public String getToolTip() {
			return "Any columns not present in the header table will "
					+ "be omitted from the output table";

		}

		@Override
		public boolean testColumn(String testColName,
				Map<String, String> colNamesMap, List<String> inColumnsToKeep) {
			if (colNamesMap.containsKey(testColName)) {
				inColumnsToKeep.add(testColName);
			}
			return true;
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
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return the default option
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	public static MissingColumnAction getDefault() {
		return Leave_Unchanged;
	}

	/**
	 * @param testColName
	 *            the name of the test column
	 * @param colNamesMap
	 *            A map of column names (old -> new) which may be modified
	 * @param inColumnsToKeep
	 *            A list of incoming column names to keep which should have the
	 *            testColName added if it is to be mapped in the output table
	 * 
	 * @return {@code true} if node execution should proceed
	 */
	public abstract boolean testColumn(String testColName,
			Map<String, String> colNamesMap, List<String> inColumnsToKeep);

}
