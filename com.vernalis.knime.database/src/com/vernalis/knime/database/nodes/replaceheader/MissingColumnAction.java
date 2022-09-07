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
package com.vernalis.knime.database.nodes.replaceheader;

import java.util.Map;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.node.port.database.StatementManipulator;
import org.knime.core.node.util.ButtonGroupEnumInterface;

public enum MissingColumnAction implements ButtonGroupEnumInterface {
	Fail {

		@Override
		public String getToolTip() {
			return "Node execution will fail if the header table "
					+ "does not contain all the column names in the incoming database";
		}

		@Override
		public String returnSqlSelectComponent(Map<String, String> colNameMap,
				String inColName, StatementManipulator statementManipultor) {
			String newColName = colNameMap.get(inColName);
			if (newColName == null || newColName.isEmpty()) {
				throw new RuntimeException("No new name supplied for column '"
						+ inColName + "' in header table");
			}
			return new StringBuilder(
					statementManipultor.quoteIdentifier(inColName))
							.append(" AS ").append(statementManipultor
									.quoteIdentifier(newColName))
							.toString();
		}

		@Override
		public DataColumnSpec createOutputColumnSpec(
				Map<String, String> colNameMap, DataColumnSpec inColSpec) {
			String newColName = colNameMap.get(inColSpec.getName());
			if (newColName == null || newColName.isEmpty()) {
				throw new RuntimeException("No new name supplied for column '"
						+ inColSpec.getName() + "' in header table");
			}
			return new DataColumnSpecCreator(newColName, inColSpec.getType())
					.createSpec();
		}
	},
	Leave_Unchanged {

		@Override
		public String getToolTip() {
			return "Any columns not present in the header table will "
					+ "be passed through to the output table unchanged";
		}

		@Override
		public String returnSqlSelectComponent(Map<String, String> colNameMap,
				String inColName, StatementManipulator statementManipultor) {
			String newColName = colNameMap.get(inColName);

			StringBuilder sql = new StringBuilder(
					statementManipultor.quoteIdentifier(inColName));
			if (newColName != null && !newColName.isEmpty()) {
				sql.append(" AS ").append(
						statementManipultor.quoteIdentifier(newColName));
			}
			return sql.toString();
		}

		@Override
		public DataColumnSpec createOutputColumnSpec(
				Map<String, String> colNameMap, DataColumnSpec inColSpec) {
			String newColName = colNameMap.get(inColSpec.getName());
			if (newColName == null || newColName.isEmpty()) {
				return inColSpec;
			}
			return new DataColumnSpecCreator(newColName, inColSpec.getType())
					.createSpec();
		}
	},
	Omit {

		@Override
		public String getToolTip() {
			return "Any columns not present in the header table will "
					+ "be omitted from the output table";

		}

		@Override
		public String returnSqlSelectComponent(Map<String, String> colNameMap,
				String inColName, StatementManipulator statementManipultor) {
			String newColName = colNameMap.get(inColName);
			if (newColName == null || newColName.isEmpty()) {
				return null;
			}
			return new StringBuilder(
					statementManipultor.quoteIdentifier(inColName))
							.append(" AS ").append(statementManipultor
									.quoteIdentifier(newColName))
							.toString();
		}

		@Override
		public DataColumnSpec createOutputColumnSpec(
				Map<String, String> colNameMap, DataColumnSpec inColSpec) {
			String newColName = colNameMap.get(inColSpec.getName());
			if (newColName == null || newColName.isEmpty()) {
				return null;
			}
			return new DataColumnSpecCreator(newColName, inColSpec.getType())
					.createSpec();
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

	public static MissingColumnAction getDefault() {
		return Leave_Unchanged;
	}

	public abstract String returnSqlSelectComponent(
			Map<String, String> colNameMap, String inColName,
			StatementManipulator statementManipultor);

	public abstract DataColumnSpec createOutputColumnSpec(
			Map<String, String> colNameMap, DataColumnSpec inColSpec);

}
