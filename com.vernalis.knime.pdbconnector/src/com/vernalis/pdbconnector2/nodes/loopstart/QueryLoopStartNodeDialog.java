/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.nodes.loopstart;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.xml.XMLValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

/**
 * {@link DefaultNodeSettingsPane} implementation for the 'Query Loop Start'
 * node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryLoopStartNodeDialog extends DefaultNodeSettingsPane {

	private static final String RCSB_PDB_ADVANCED_QUERY_DEFINITION = "RCSB PDB Advanced Query Definition";
	private static final String IGNORE_INVALID_XML = "Ignore invalid XML";
	private static final String NO_QUERY_BEHAVIOUR = "No Query behaviour";
	private static final String COLUMN_NAME = "Column Name";
	/**
	 * Column filter for XML columns
	 */
	static final ColumnFilter XMLVALUE_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return colSpec.getType().isCompatible(XMLValue.class);
		}

		@Override
		public String allFilteredMsg() {
			return "No XML Columns present in input table";
		}
	};

	/**
	 * Constructor
	 */
	public QueryLoopStartNodeDialog() {
		super();
		addDialogComponent(new DialogComponentColumnNameSelection(
				createColumnNameModel(), COLUMN_NAME, 0, XMLVALUE_FILTER));
		addDialogComponent(
				new DialogComponentButtonGroup(createNoQueryBehaviourModel(),
						NO_QUERY_BEHAVIOUR, false, NoQueryBehaviour.values()));
		addDialogComponent(new DialogComponentBoolean(
				createIgnoreInvalidXMLModel(), IGNORE_INVALID_XML));

	}

	/**
	 * @return The model for the 'Ignore invalid XML' setting
	 */
	static SettingsModelBoolean createIgnoreInvalidXMLModel() {
		return new SettingsModelBoolean(IGNORE_INVALID_XML, true);
	}

	/**
	 * @return The model for the 'No Query Behaviour' setting
	 */
	static SettingsModelString createNoQueryBehaviourModel() {
		return new SettingsModelString(NO_QUERY_BEHAVIOUR,
				NoQueryBehaviour.getDefault().getActionCommand());
	}

	/**
	 * @return The model for the 'Column Name' setting
	 */
	static SettingsModelString createColumnNameModel() {
		return new SettingsModelString(COLUMN_NAME,
				RCSB_PDB_ADVANCED_QUERY_DEFINITION);
	}

}
