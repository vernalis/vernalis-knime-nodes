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
package com.vernalis.nodes.collection.singleton;

import java.util.Arrays;
import java.util.Collections;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.collection.singleton.SingletonCollectionNodeDialog.COLUMN_FILTER;
import static com.vernalis.nodes.collection.singleton.SingletonCollectionNodeDialog.createAllowCollectionsModel;
import static com.vernalis.nodes.collection.singleton.SingletonCollectionNodeDialog.createCollectionTypeModel;
import static com.vernalis.nodes.collection.singleton.SingletonCollectionNodeDialog.createColumnFilterModel;
import static com.vernalis.nodes.collection.singleton.SingletonCollectionNodeDialog.createMissingModel;

/**
 * Node Model implementation for the column to singleton collection node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class SingletonCollectionNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelColumnFilter2 colsMdl =
			registerSettingsModel(createColumnFilterModel());
	private final SettingsModelBoolean wrapMissingMdl =
			registerSettingsModel(createMissingModel());
	private final SettingsModelString collectionTypeMdl =
			registerSettingsModel(createCollectionTypeModel());
	private final SettingsModelBoolean allowCollectionsMdl =
			createAllowCollectionsModel();

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		// Firstly check we have any columns, and any selected
		if (!allowCollectionsMdl.getBooleanValue() && spec.stream()
				.noneMatch(colSpec -> COLUMN_FILTER.include(colSpec))) {
			throw new InvalidSettingsException(
					"No non-collection columns in the incoming table");
		}

		final int[] colIdx = Arrays.stream(colsMdl.applyTo(spec).getIncludes())
				.mapToInt(colName -> spec.findColumnIndex(colName)).toArray();
		if (colIdx.length == 0) {
			throw new InvalidSettingsException("No columns selected!");
		}

		// Now generate the output spec

		final DataColumnSpec[] newColSpecs = new DataColumnSpec[colIdx.length];
		for (int i = 0; i < newColSpecs.length; i++) {
			final DataColumnSpec inColSpec = spec.getColumnSpec(colIdx[i]);
			newColSpecs[i] = new DataColumnSpecCreator(inColSpec.getName(),
					"Set".equalsIgnoreCase(collectionTypeMdl.getStringValue())
							? SetCell.getCollectionType(inColSpec.getType())
							: ListCell.getCollectionType(inColSpec.getType()))
									.createSpec();
		}

		// Create the cellfactory
		CellFactory cellFact = new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] retVal = new DataCell[newColSpecs.length];
				for (int i = 0; i < retVal.length; i++) {
					DataCell inCell = row.getCell(colIdx[i]);
					DataCell outCell;
					if (inCell.isMissing()
							&& !wrapMissingMdl.getBooleanValue()) {
						outCell = DataType.getMissingCell();
					} else {
						outCell = "Set".equalsIgnoreCase(
								collectionTypeMdl.getStringValue())
										? CollectionCellFactory.createSetCell(
												Collections.singleton(inCell))
										: CollectionCellFactory
												.createListCell(Collections
														.singletonList(inCell));
					}
					retVal[i] = outCell;
				}
				return retVal;
			}
		};

		// Create the rearranger, and apply the cell factory appropriately
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.replace(cellFact, colIdx);
		return rearranger;
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		allowCollectionsMdl.saveSettingsTo(settings);
	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.validateSettings(settings);
		// Dont validate allowCollectionsMdl
	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		try {
			allowCollectionsMdl.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			setWarningMessage(
					"Using default legacy value to the 'Allow Collections' option");
			allowCollectionsMdl.setBooleanValue(false);
		}
	}

}
