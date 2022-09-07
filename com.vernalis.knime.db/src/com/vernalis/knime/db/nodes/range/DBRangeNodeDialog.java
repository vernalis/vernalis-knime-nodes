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

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.database.function.aggregation.impl.DefaultDBAggregationFunctionRegistry;
import org.knime.database.function.aggregation.impl.InvalidDBAggregationFunction;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBDataPortObjectSpec;

/**
 * {@link NodeDialogPane} for the DB Numeric Range node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBRangeNodeDialog extends NodeDialogPane {

	private DialogComponentColumnFilter2 colFilt;
	private JLabel warningLabel;

	/**
	 * 
	 */
	protected DBRangeNodeDialog() {
		colFilt = new DialogComponentColumnFilter2(createColNamesModel(), 0,
				false);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(colFilt.getComponentPanel());
		warningLabel = new JLabel();
		p.add(warningLabel);
		super.addTab("Numeric Range Columns", p);
	}

	/**
	 * @return model for the Numeric Range Columns setting
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@SuppressWarnings("unchecked")
	static SettingsModelColumnFilter2 createColNamesModel() {
		return new SettingsModelColumnFilter2("Columns", DoubleValue.class,
				IntValue.class, LongValue.class);
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
			warningLabel.setText("");
			if (dbSpec.isSessionExists()) {
				boolean noMinFunction = DefaultDBAggregationFunctionRegistry
						.getInstance()
						.getAggregationFunctions(
								dbSpec.getDBSession().getDBType())
						.getFunction(
								"MIN") instanceof InvalidDBAggregationFunction;
				boolean noMaxFunction = DefaultDBAggregationFunctionRegistry
						.getInstance()
						.getAggregationFunctions(
								dbSpec.getDBSession().getDBType())
						.getFunction(
								"MAX") instanceof InvalidDBAggregationFunction;
				if (noMinFunction) {
					warningLabel.setText(
							"No 'MIN' function for current connection");
				}
				if (noMaxFunction) {
					warningLabel.setText(
							"No 'MAX' function for current connection");
				}
				if (noMinFunction && noMaxFunction) {
					throw new NotConfigurableException(
							"Current connection does not support MIN or MAX");
				}
			}
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
