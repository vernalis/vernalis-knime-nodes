/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.urlencode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.function.Function;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.urlencode.UrlDEncodeNodeDialog.TYPE_FILTER;
import static com.vernalis.nodes.urlencode.UrlDEncodeNodeDialog.createCharsetNameModel;
import static com.vernalis.nodes.urlencode.UrlDEncodeNodeDialog.createColNameModel;
import static com.vernalis.nodes.urlencode.UrlDEncodeNodeDialog.createOutputTypeModel;
import static com.vernalis.nodes.urlencode.UrlDEncodeNodeDialog.createRemoveInputColModel;

public class UrlEncodeNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColNameModel());
	private final SettingsModelBoolean removedInputColMdl =
			registerSettingsModel(createRemoveInputColModel());
	private final SettingsModelString outputTypeMdl =
			registerSettingsModel(createOutputTypeModel());
	private final SettingsModelString charsetNameMdl =
			registerSettingsModel(createCharsetNameModel());

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		final int colIdx = getValidatedColumnSelectionModelColumnIndex(
				colNameMdl, TYPE_FILTER, spec, getLogger());
		final DataType inputType = spec.getColumnSpec(colIdx).getType();
		final OutputType outType;
		try {
			outType = OutputType.valueOf(outputTypeMdl.getStringValue());
		} catch (Exception e) {
			throw new InvalidSettingsException(
					"Unrecognised output type option - '"
							+ outputTypeMdl.getStringValue() + "'",
					e);
		}

		final DataType returnType = outType.getOutputType(inputType);
		final Function<String, DataCell> cellFactFunc =
				UrlDEncodeNodeDialog.RETURN_FUNCTIONS.get(returnType);

		if (!Charset.availableCharsets()
				.containsKey(charsetNameMdl.getStringValue())) {
			throw new InvalidSettingsException(
					"Charset '" + charsetNameMdl.getStringValue()
							+ "' not available on this machine");
		}
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		if (removedInputColMdl.getBooleanValue()) {
			// We remove rather than replace so new column is at end, and some
			// auto-selected by a downstream node
			rearranger.remove(colIdx);
		}

		DataColumnSpec newColSpec = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(rearranger
						.createSpec()/*
										 * We use rearranger to allow same name
										 * at output when removing input
										 */,
						removedInputColMdl.getBooleanValue()
								? colNameMdl.getStringValue()
								: (colNameMdl.getStringValue() + " (Encoded)")),
				outType.getOutputType(inputType)).createSpec();

		rearranger.append(new SingleCellFactory(true, newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell inCell = row.getCell(colIdx);
				if (inCell.isMissing()) {
					return DataType.getMissingCell();
				}

				// All the types implement StringValue...
				String input = ((StringValue) inCell).getStringValue();
				try {
					return cellFactFunc.apply(URLEncoder.encode(input,
							charsetNameMdl.getStringValue()));
				} catch (UnsupportedEncodingException e) {
					getLogger().info("Error encoding '" + input + "' (Row "
							+ row.getKey().getString() + ") - "
							+ e.getMessage());
					if (getWarningMessage() == null) {
						setWarningMessage(
								"Not all rows coule be correctly encoded; See log for details");
					}
					return DataType.getMissingCell();
				}

			}
		});
		return rearranger;
	}

}
