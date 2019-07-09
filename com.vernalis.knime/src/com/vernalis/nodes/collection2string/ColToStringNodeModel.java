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
package com.vernalis.nodes.collection2string;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.COLLECTION_FILTER;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createCellPrefixModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createCellSuffixModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createColumnsModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createJoinerModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createPrefixModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createReplaceInputColumnsModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createSkipMissingValuesModel;
import static com.vernalis.nodes.collection2string.ColToStringNodeDialog.createSuffixModel;

public class ColToStringNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelColumnFilter2 collColsMdl =
			registerSettingsModel(createColumnsModel());
	private final SettingsModelBoolean replaceInputsMdl =
			registerSettingsModel(createReplaceInputColumnsModel());
	private final SettingsModelBoolean skipMissingsMdl =
			registerSettingsModel(createSkipMissingValuesModel());
	private final SettingsModelString prefixMdl =
			registerSettingsModel(createPrefixModel());
	private final SettingsModelString cellPrefixMdl =
			registerSettingsModel(createCellPrefixModel());
	private final SettingsModelString joinerMdl =
			registerSettingsModel(createJoinerModel());
	private final SettingsModelString cellSuffixMdl =
			registerSettingsModel(createCellSuffixModel());
	private final SettingsModelString suffixMdl =
			registerSettingsModel(createSuffixModel());

	public ColToStringNodeModel() {

	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		// Firstly check we have any columns, and any selected
		if (!spec.stream().filter(colSpec -> COLLECTION_FILTER.include(colSpec))
				.findAny().isPresent()) {
			throw new InvalidSettingsException(
					"No collection columns in the incoming table");
		}

		final int[] colIdx =
				Arrays.stream(collColsMdl.applyTo(spec).getIncludes())
						.mapToInt(name -> spec.findColumnIndex(name)).toArray();
		if (colIdx.length == 0) {
			throw new InvalidSettingsException(
					"No collection columns selected!");
		}

		// Now generate the output spec
		String colNameSuffix =
				replaceInputsMdl.getBooleanValue() ? "" : " (String)";
		final DataColumnSpec[] newColSpecs = new DataColumnSpec[colIdx.length];
		for (int i = 0; i < newColSpecs.length; i++) {
			String newColName =
					spec.getColumnSpec(colIdx[i]).getName() + colNameSuffix;
			if (!replaceInputsMdl.getBooleanValue()) {
				newColName =
						DataTableSpec.getUniqueColumnName(spec, newColName);
			}
			newColSpecs[i] =
					new DataColumnSpecCreator(newColName, StringCell.TYPE)
							.createSpec();
		}

		// Get the various connecting/wrapping strings - we use
		// getJavaUnescapedString to allow e.g. newlines
		String cellPrefix = cellPrefixMdl.getJavaUnescapedStringValue() == null
				? "" : cellPrefixMdl.getJavaUnescapedStringValue();
		String cellSuffix = cellSuffixMdl.getJavaUnescapedStringValue() == null
				? "" : cellSuffixMdl.getJavaUnescapedStringValue();
		String joiner = joinerMdl.getJavaUnescapedStringValue() == null ? ""
				: joinerMdl.getJavaUnescapedStringValue();
		String prefix = prefixMdl.getJavaUnescapedStringValue() == null ? ""
				: prefixMdl.getJavaUnescapedStringValue();
		String suffix = suffixMdl.getJavaUnescapedStringValue() == null ? ""
				: suffixMdl.getJavaUnescapedStringValue();

		// Create the cellfactory
		CellFactory cellFact = new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {

				DataCell[] retVal =
						ArrayUtils.fill(new DataCell[newColSpecs.length],
								DataType.getMissingCell());
				int cellIndex = 0;
				for (int col : colIdx) {
					DataCell colCell = row.getCell(col);
					if (!colCell.isMissing()) {
						retVal[cellIndex] = new StringCell(
								((CollectionDataValue) colCell).stream().filter(
										x -> !(skipMissingsMdl.getBooleanValue()
												&& x.isMissing()))
										.map(cell -> cellPrefix
												+ cell.toString() + cellSuffix)
										.collect(Collectors.joining(joiner,
												prefix, suffix)));
					}
					cellIndex++;
				}
				return retVal;
			}
		};

		// Create the rearranger, and apply the cell factory appropriately
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		if (replaceInputsMdl.getBooleanValue()) {
			rearranger.replace(cellFact, colIdx);
		} else {
			rearranger.append(cellFact);
		}
		return rearranger;
	}

}
