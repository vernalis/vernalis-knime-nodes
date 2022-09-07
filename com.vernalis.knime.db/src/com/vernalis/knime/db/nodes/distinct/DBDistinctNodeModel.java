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
package com.vernalis.knime.db.nodes.distinct;

import java.sql.SQLType;

import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.database.DBDataObject;
import org.knime.database.SQLQuery;
import org.knime.database.agent.metadata.DBMetadataReader;
import org.knime.database.dialect.DBSQLDialect;
import org.knime.database.node.DBDataNodeModel;
import org.knime.database.port.DBDataPortObjectSpec;
import org.knime.database.session.DBSession;
import org.knime.datatype.mapping.DataTypeMappingConfiguration;

import static com.vernalis.knime.db.nodes.distinct.DBDistinctNodeDialog.createColNamesModel;

/**
 * {@link DBDataNodeModel} implementation for the DB Distinct node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBDistinctNodeModel extends DBDataNodeModel {

	private final SettingsModelColumnFilter2 filtMdl = createColNamesModel();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected DBDataPortObjectSpec configure(DBDataPortObjectSpec inSpec)
			throws InvalidSettingsException {
		if (filtMdl.applyTo(inSpec.getDataTableSpec())
				.getIncludes().length < 1) {
			throw new InvalidSettingsException("No columns kept!");
		}
		return super.configure(inSpec);
	}

	@Override
	protected void saveSettingsToInternal(NodeSettingsWO settings) {
		filtMdl.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettingsInternal(NodeSettingsRO settings)
			throws InvalidSettingsException {
		filtMdl.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFromInternal(NodeSettingsRO settings)
			throws InvalidSettingsException {
		filtMdl.loadSettingsFrom(settings);
	}

	@Override
	protected DBDataObject createDataObject(ExecutionMonitor exec,
			DBSession session, DBDataObject data,
			DataTypeMappingConfiguration<SQLType> externalToKnime)
			throws Exception {

		final DBSQLDialect dialect = session.getDialect();

		final SQLQuery newQuery = dialect.dataManipulation()
				.createDistinctSelectFromQuery(data.getQuery(), filtMdl
						.applyTo(data.getKNIMETableSpec()).getIncludes());
		return session.getAgent(DBMetadataReader.class).getDBDataObject(exec,
				newQuery, externalToKnime);
	}

}
