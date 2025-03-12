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

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.base64.Blob2Base64NodeDialog.createBlobColNameModel;
import static com.vernalis.knime.misc.blobs.nodes.base64.Blob2Base64NodeDialog.createErrorBehaviourModel;
import static com.vernalis.knime.misc.blobs.nodes.base64.Blob2Base64NodeDialog.createReplaceInputColModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Base64;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 1.38.0
 */
public class Blob2Base64NodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString blobColNameMdl =
			registerSettingsModel(createBlobColNameModel());
	private final SettingsModelBoolean replaceInputColMdl =
			registerSettingsModel(createReplaceInputColModel());
	private final SettingsModelString errorBehaviourMdl =
			registerSettingsModel(createErrorBehaviourModel());

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		int colIdx = getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
				BLOB_COLUMN_FILTER, spec, getLogger());
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
				: DataTableSpec.getUniqueColumnName(spec,
						spec.getColumnSpec(colIdx).getName() + " (Base64)");

		DataColumnSpec newColSpec =
				new DataColumnSpecCreator(newColName, StringCell.TYPE)
						.createSpec();

		SingleCellFactory cellFactory = new SingleCellFactory(newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell in = row.getCell(colIdx);
				if (in.isMissing()) {
					return DataType.getMissingCell();
				}
				BinaryObjectDataValue inBlobVal = (BinaryObjectDataValue) in;
				try (InputStream is = inBlobVal.openInputStream()) {
					return new StringCell(
							Base64.getEncoder().encodeToString(is.readAllBytes()));
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
			rearranger.replace(cellFactory, colIdx);
		} else {
			rearranger.append(cellFactory);
		}
		return rearranger;
	}

}
