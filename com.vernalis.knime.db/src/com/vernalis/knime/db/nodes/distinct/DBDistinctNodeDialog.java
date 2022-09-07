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

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBDataPortObjectSpec;

/**
 * {@link NodeDialogPane} implementation for the DB Distinct node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBDistinctNodeDialog extends NodeDialogPane {

	private DialogComponentColumnFilter2 colFilt;

	/**
	 * Constructor
	 */
	DBDistinctNodeDialog() {
		colFilt = new DialogComponentColumnFilter2(createColNamesModel(), 0,
				false);
		super.addTab("DISTINCT Columns", colFilt.getComponentPanel());
	}

	/**
	 * @return model for the DISTINCT Columns setting
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@SuppressWarnings("unchecked")
	static SettingsModelColumnFilter2 createColNamesModel() {
		return new SettingsModelColumnFilter2("Columns", StringValue.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public void loadSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {

		try {
			DBDataPortObjectSpec dbSpec =
					PortObjectSpecHelper.asDBDataPortObjectSpec(specs[0], true);
			final DataTableSpec[] dts =
					new DataTableSpec[] { dbSpec.getDataTableSpec() };
			colFilt.loadSettingsFrom(settings, dts);
		} catch (InvalidSettingsException e) {
			throw new NotConfigurableException(e.getMessage(), e);
		}

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		colFilt.saveSettingsTo(settings);

	}
}
