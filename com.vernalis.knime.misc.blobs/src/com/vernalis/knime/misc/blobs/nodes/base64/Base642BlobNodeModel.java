/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc.blobs.nodes.base64;

import static com.vernalis.knime.misc.blobs.nodes.base64.Base642BlobNodeDialog.STRING_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.base64.Base642BlobNodeDialog.createErrorBehaviourModel;
import static com.vernalis.knime.misc.blobs.nodes.base64.Base642BlobNodeDialog.createReplaceInputColModel;
import static com.vernalis.knime.misc.blobs.nodes.base64.Base642BlobNodeDialog.createStringColNameModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * NodeModel for the Base64 to Binary Objects node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class Base642BlobNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString stringColNameMdl =
			registerSettingsModel(createStringColNameModel());
	private final SettingsModelBoolean replaceInputColMdl =
			registerSettingsModel(createReplaceInputColModel());
	private final SettingsModelString errorBehaviourMdl =
			registerSettingsModel(createErrorBehaviourModel());
	private ExecutionContext exec;

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		this.exec = exec;
		return super.execute(inData, exec);
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		int colIdx = getValidatedColumnSelectionModelColumnIndex(
				stringColNameMdl, STRING_COLUMN_FILTER, spec, getLogger());
		ConversionFailureBehaviour cfb;
		try {
			cfb = ConversionFailureBehaviour
					.valueOf(errorBehaviourMdl.getStringValue());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"Unable to parse error behaviour '"
							+ errorBehaviourMdl.getStringValue() + "'",
					e);
		}

		String newColName = replaceInputColMdl.getBooleanValue()
				? spec.getColumnSpec(colIdx).getName()
				: DataTableSpec.getUniqueColumnName(spec, "Binary Objects");

		DataColumnSpec newColSpec =
				new DataColumnSpecCreator(newColName, BinaryObjectDataCell.TYPE)
						.createSpec();
		BinaryObjectCellFactory bocf =
				exec == null ? null : new BinaryObjectCellFactory(exec);

		SingleCellFactory scf = new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell inCell = row.getCell(colIdx);
				if (inCell.isMissing()) {
					return DataType.getMissingCell();
				}
				String base64 = ((StringValue) inCell).getStringValue();
				try {
					return bocf.create(new ByteArrayInputStream(
							Base64.getDecoder().decode(base64)));
				} catch (IOException e) {
					switch (cfb) {
						case Fail:
							throw new UncheckedIOException(e);
						case Skip:
						default:
							setWarningMessage(
									"Error converting some rows - see log for details");
							getLogger().warn("Error converting row '"
									+ row.getKey().getString() + "' - "
									+ e.getMessage());
							return new MissingCell(e.getMessage());
					}
				}

			}
		};

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		if (replaceInputColMdl.getBooleanValue()) {
			rearranger.replace(scf, colIdx);
		} else {
			rearranger.append(scf);
		}
		return rearranger;
	}

}
