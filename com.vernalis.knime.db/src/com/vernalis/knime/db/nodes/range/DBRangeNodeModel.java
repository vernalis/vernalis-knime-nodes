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
package com.vernalis.knime.db.nodes.range;

import java.sql.SQLType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.util.Pair;
import org.knime.database.DBDataObject;
import org.knime.database.SQLQuery;
import org.knime.database.agent.metadata.DBMetadataReader;
import org.knime.database.dialect.groupby.GroupByParameters;
import org.knime.database.function.aggregation.DBAggregationFunction;
import org.knime.database.function.aggregation.impl.InvalidDBAggregationFunction;
import org.knime.database.model.impl.DefaultDBTable;
import org.knime.database.node.DBDataNodeModel;
import org.knime.database.port.DBDataPortObjectSpec;
import org.knime.database.session.DBSession;
import org.knime.datatype.mapping.DataTypeMappingConfiguration;

import static com.vernalis.knime.db.nodes.range.DBRangeNodeDialog.createColNamesModel;

/**
 * {@link DBNodeModel} implementation for the DB Numeric Range node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBRangeNodeModel extends DBDataNodeModel {

	private final SettingsModelColumnFilter2 filtMdl = createColNamesModel();
	private DBAggregationFunction minFunction;
	private DBAggregationFunction maxFunction;

	private static final BiFunction<String, DBAggregationFunction, String> columnNameCreator =
			new BiFunction<String, DBAggregationFunction, String>() {

				@Override
				public String apply(String incomingColumnName,
						DBAggregationFunction aggFunction) {
					return String.format("%s(%s)", aggFunction.getColumnName(),
							incomingColumnName);
				}
			};

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBRangeNodeModel() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#configure(org.knime.core.node.port.
	 * PortObjectSpec[])
	 */
	@Override
	protected DBDataPortObjectSpec configure(final DBDataPortObjectSpec inSpec)
			throws InvalidSettingsException {

		if (filtMdl.applyTo(inSpec.getDataTableSpec())
				.getIncludes().length < 1) {
			throw new InvalidSettingsException("No columns kept!");
		}
		if (!validateMinMaxAggregations(inSpec.getDBSession())) {
			throw new InvalidSettingsException("The database type ("
					+ inSpec.getDBSession().getDBType().getDescription()
					+ ") of the current connection does not support the MIN or MAX operations");
		}
		if (minFunction instanceof InvalidDBAggregationFunction) {
			setWarningMessage(
					"MIN not supported for the current database type ("
							+ inSpec.getDBSession().getDBType().getDescription()
							+ ") - only MAX will be returned");
		} else if (maxFunction instanceof InvalidDBAggregationFunction) {
			setWarningMessage(
					"MAX not supported for the current database type ("
							+ inSpec.getDBSession().getDBType().getDescription()
							+ ") - only MIN will be returned");
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

		if (!validateMinMaxAggregations(session)) {
			throw new InvalidSettingsException("The database type ("
					+ session.getDBType().getDescription()
					+ ") of the current connection does not support the MIN or MAX operations");
		}

		List<Pair<String, DBAggregationFunction>> aggregationValues =
				new ArrayList<>();
		for (String colName : filtMdl.applyTo(data.getKNIMETableSpec())
				.getIncludes()) {
			if (!(minFunction instanceof InvalidDBAggregationFunction)) {
				aggregationValues.add(new Pair<>(colName, minFunction));
			}
			if (!(maxFunction instanceof InvalidDBAggregationFunction)) {
				aggregationValues.add(new Pair<>(colName, maxFunction));
			}
		}

		SQLQuery newQuery = session.getDialect().dataManipulation()
				.createGroupByStatement(new GroupByParameters(data.getQuery(),
						new DefaultDBTable(
								"table_" + System.identityHashCode(this)),
						Collections.emptyList(), false, null, aggregationValues,
						columnNameCreator));
		return session.getAgent(DBMetadataReader.class).getDBDataObject(exec,
				newQuery, externalToKnime);
	}

	private boolean validateMinMaxAggregations(DBSession session) {
		minFunction = session.getAggregationFunctions().getFunction("MIN");
		maxFunction = session.getAggregationFunctions().getFunction("MAX");
		return !(minFunction instanceof InvalidDBAggregationFunction
				&& maxFunction instanceof InvalidDBAggregationFunction);
	}

}
