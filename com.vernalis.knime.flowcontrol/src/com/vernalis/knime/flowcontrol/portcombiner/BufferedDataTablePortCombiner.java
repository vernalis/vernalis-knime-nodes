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
package com.vernalis.knime.flowcontrol.portcombiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.append.AppendedRowsTable;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.ConcatenatingTablesCollector;
import com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend.RowPolicies;
import com.vernalis.knime.flowcontrol.portcombiner.api.PortTypeCombiner;
import com.vernalis.knime.flowcontrol.portcombiner.api.Warnable;

/**
 * {@link PortTypeCombiner} to handle comibne {@link BufferedDataTable}s
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class BufferedDataTablePortCombiner implements PortTypeCombiner {

	private static final String ALLOW_CHANGING_TABLE_SPECS =
			"Allow changing table specs";
	private static final String ALLOW_CHANGING_COLUMN_TYPES =
			"Allow changing column types";
	private static final String IGNORE_EMPTY_INPUT_TABLES =
			"Ignore empty input tables";
	private static final String ADD_PORT_INDEX_COLUMN = "Add port index column";
	private static final String ROW_KEY_POLICY = "Row key policy";

	/**
	 * Private constructor
	 */
	private BufferedDataTablePortCombiner() {
		//
	}

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static BufferedDataTablePortCombiner INSTANCE =
				new BufferedDataTablePortCombiner();
	}

	/**
	 * Method to get the singleton instance
	 * 
	 * @return The singleton instance of BufferedDataTablePortCombiner
	 */
	public static BufferedDataTablePortCombiner getInstance() {
		return HoldingClass.INSTANCE;
	}

	@Override
	public Map<String, SettingsModel> getCombinerModels() {
		Map<String, SettingsModel> retVal = new HashMap<>();
		retVal.put(ROW_KEY_POLICY, createRowKeyPolicyModel());
		retVal.put(ADD_PORT_INDEX_COLUMN, createAddPortIndexColumnModel());
		retVal.put(IGNORE_EMPTY_INPUT_TABLES, createIgnoreEmptyInputsModel());
		retVal.put(ALLOW_CHANGING_COLUMN_TYPES,
				createAllowChangingColumnTypesModel());
		retVal.put(ALLOW_CHANGING_TABLE_SPECS, createAllowChangingSpecsModel());
		return retVal;
	}

	@Override
	public void createDialog(DefaultNodeSettingsPane dialog,
			Map<String, SettingsModel> models) {
		dialog.addDialogComponent(new DialogComponentButtonGroup(
				(SettingsModelString) models.get(ROW_KEY_POLICY),
				ROW_KEY_POLICY, true, RowPolicies.values()));
		dialog.setHorizontalPlacement(true);
		dialog.addDialogComponent(new DialogComponentBoolean(
				(SettingsModelBoolean) models.get(ADD_PORT_INDEX_COLUMN),
				ADD_PORT_INDEX_COLUMN));
		dialog.addDialogComponent(new DialogComponentBoolean(
				(SettingsModelBoolean) models.get(IGNORE_EMPTY_INPUT_TABLES),
				IGNORE_EMPTY_INPUT_TABLES));
		dialog.setHorizontalPlacement(false);
		dialog.setHorizontalPlacement(true);
		dialog.addDialogComponent(new DialogComponentBoolean(
				(SettingsModelBoolean) models.get(ALLOW_CHANGING_COLUMN_TYPES),
				ALLOW_CHANGING_COLUMN_TYPES));
		dialog.addDialogComponent(new DialogComponentBoolean(
				(SettingsModelBoolean) models.get(ALLOW_CHANGING_TABLE_SPECS),
				ALLOW_CHANGING_TABLE_SPECS));
		dialog.setHorizontalPlacement(false);
	}

	@Override
	public boolean hasDialogOptions() {
		return true;
	}

	@Override
	public PortObjectSpec createOutputPortObjectSpec(
			List<? extends PortObjectSpec> activePorts,
			Map<String, SettingsModel> models, Warnable warnable)
			throws InvalidSettingsException {
		// We only know the spec if we have only 1 input spec,
		// or we have multiple input and dont allow any changes
		List<DataTableSpec> uniqueSpecs = new ArrayList<>();
		PortObjectSpec outSpec;
		for (int i = 0; i < activePorts.size(); i++) {
			DataTableSpec inSpec = (DataTableSpec) activePorts.get(i);
			boolean alreadyPresent = false;
			for (DataTableSpec spec : uniqueSpecs) {
				if (inSpec.equalStructure(spec)) {
					alreadyPresent = true;
					break;
				}
			}
			if (!alreadyPresent) {
				uniqueSpecs.add(inSpec);
			}
		}
		if (uniqueSpecs.size() == 1) {
			outSpec = uniqueSpecs.get(0);

		} else if (!(((SettingsModelBoolean) models
				.get(ALLOW_CHANGING_TABLE_SPECS)).getBooleanValue()
				|| ((SettingsModelBoolean) models
						.get(ALLOW_CHANGING_COLUMN_TYPES)).getBooleanValue())) {
			if (((SettingsModelBoolean) models.get(IGNORE_EMPTY_INPUT_TABLES))
					.getBooleanValue()) {
				// Multiple specs, but we may be ok, but we dont know which
				// will
				// be correct until runtime...
				outSpec = null;
				warnable.setWarning(
						"Node execution may fail - multiple incompatible active specs "
								+ "found but may be ok if some are empty tables!");
			} else {
				// Fail - Multiple active specs, but not
				// allowing changes?
				throw new InvalidSettingsException(
						"Multiple incompatible active table specs encountered");
			}
		} else {
			// Generate the merged spec
			outSpec = AppendedRowsTable.generateDataTableSpec(
					uniqueSpecs.toArray(new DataTableSpec[0]));
		}
		// We might have a port index column...
		if (outSpec != null
				&& ((SettingsModelBoolean) models.get(ADD_PORT_INDEX_COLUMN))
						.getBooleanValue()) {
			outSpec =
					createPortSpecIndexColumnRearranger((DataTableSpec) outSpec,
							-1).createSpec();
		}

		String rowKeyPolicy = ((SettingsModelString) models.get(ROW_KEY_POLICY))
				.getStringValue();
		try {
			RowPolicies.valueOf(rowKeyPolicy);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException("The row policy '" + rowKeyPolicy
					+ "' is not a valid value (One of '"
					+ Arrays.stream(RowPolicies.values())
							.map(map -> map.getActionCommand())
							.collect(Collectors.joining("', '"))
					+ "')");
		}
		return outSpec;
	}

	@Override
	public PortObject createOutputPortObject(int[] activePortIndices,
			ExecutionContext exec, Map<String, SettingsModel> models,
			PortObject[] inPorts) throws Exception {

		final boolean addPortIndex =
				((SettingsModelBoolean) models.get(ADD_PORT_INDEX_COLUMN))
						.getBooleanValue();

		// Handle the simple single active input case first
		if (activePortIndices.length == 1) {
			// Only one active port...
			BufferedDataTable inTable =
					(BufferedDataTable) inPorts[activePortIndices[0]];
			if (addPortIndex) {
				// Add the port index column
				return exec.createColumnRearrangeTable(inTable,
						createPortSpecIndexColumnRearranger(
								inTable.getDataTableSpec(),
								activePortIndices[0]),
						exec);
			} else {
				// Just return the active port
				return inTable;
			}
		}
		// We have multiple active
		RowPolicies rowPolicy = RowPolicies
				.valueOf(((SettingsModelString) models.get(ROW_KEY_POLICY))
						.getStringValue());
		ConcatenatingTablesCollector tableColl =
				new ConcatenatingTablesCollector(
						((SettingsModelBoolean) models
								.get(IGNORE_EMPTY_INPUT_TABLES))
										.getBooleanValue(),
						false /*
								 * We handle this option above - the
								 * ConcatenatingTablesCollector will always call
								 * this 'Iteration', which we dont want here!
								 */,
						((SettingsModelBoolean) models
								.get(ALLOW_CHANGING_COLUMN_TYPES))
										.getBooleanValue(),
						((SettingsModelBoolean) models
								.get(ALLOW_CHANGING_TABLE_SPECS))
										.getBooleanValue(),
						Optional.of(rowPolicy), Optional.empty());
		tableColl.setAppendSuffixPrefix("Port");
		for (int i = 0; i < activePortIndices.length; i++) {
			BufferedDataTable table =
					(BufferedDataTable) inPorts[activePortIndices[i]];
			if (addPortIndex) {
				table = exec.createColumnRearrangeTable(table,
						createPortSpecIndexColumnRearranger(
								table.getDataTableSpec(), activePortIndices[i]),
						exec);
			}
			try {
				tableColl.appendTable(table, exec);
			} catch (IllegalArgumentException iae) {
				// Error appending table
				throw new IllegalArgumentException("Error appending inport " + i
						+ " - " + iae.getMessage(), iae);
			}
		}
		return tableColl.createTable(exec);
	}

	private ColumnRearranger createPortSpecIndexColumnRearranger(
			DataTableSpec inSpec, int portIndex) {
		ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		rearranger.append(new SingleCellFactory(new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(inSpec, "Port Index"),
				IntCell.TYPE).createSpec()) {

			@Override
			public DataCell getCell(DataRow row) {
				return new IntCell(portIndex);
			}
		});
		return rearranger;
	}

	private static SettingsModelBoolean createAllowChangingSpecsModel() {
		return new SettingsModelBoolean(ALLOW_CHANGING_TABLE_SPECS, false);
	}

	private static SettingsModelBoolean createAllowChangingColumnTypesModel() {
		return new SettingsModelBoolean(ALLOW_CHANGING_COLUMN_TYPES, false);
	}

	private static SettingsModelBoolean createIgnoreEmptyInputsModel() {
		return new SettingsModelBoolean(IGNORE_EMPTY_INPUT_TABLES, true);
	}

	private static SettingsModelBoolean createAddPortIndexColumnModel() {
		return new SettingsModelBoolean(ADD_PORT_INDEX_COLUMN, true);
	}

	private static SettingsModelString createRowKeyPolicyModel() {
		return new SettingsModelString(ROW_KEY_POLICY,
				RowPolicies.getDefault().getActionCommand());
	}

}
