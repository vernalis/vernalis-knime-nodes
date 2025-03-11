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
package com.vernalis.knime.misc.blobs.nodes.compress.detect;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.createBlobColNameModel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.knime.core.data.DataCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.blobs.nodes.compress.CompressFormat;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * NodeModel class for the Detect Compression Format node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class DetectCompressionFormatNodeModel
        extends AbstractSimpleStreamableFunctionNodeModel {

    private final SettingsModelString blobColNameMdl =
            registerSettingsModel(createBlobColNameModel());

    @Override
    protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
            throws InvalidSettingsException {

        int colIdx = getValidatedColumnSelectionModelColumnIndex(blobColNameMdl,
                BLOB_COLUMN_FILTER, spec, getLogger());

        ColumnRearranger rearranger = new ColumnRearranger(spec);
        rearranger.append(new SingleCellFactory(new DataColumnSpecCreator(
                DataTableSpec.getUniqueColumnName(spec, "Compression Format ("
                        + blobColNameMdl.getStringValue() + ")"),
                StringCell.TYPE).createSpec()) {

            @Override
            public DataCell getCell(DataRow row) {

                DataCell blobCell = row.getCell(colIdx);

                if (blobCell.isMissing()) {
                    return DataType.getMissingCell();
                }
                BinaryObjectDataValue bodv = (BinaryObjectDataValue) blobCell;

                try (InputStream is = bodv.openInputStream();
                        BufferedInputStream bis =
                                new BufferedInputStream(is);) {
                    return getTypeCellFromInputStream(bis);
                } catch (IOException e1) {
                    throw new UncheckedIOException(e1);
                }
            }

        });
        return rearranger;
    }

    private DataCell getTypeCellFromInputStream(BufferedInputStream bis) {

        try {
            CompressFormat detected = CompressFormat.getFormatForStream(bis);
            return detected == null ? DataType.getMissingCell()
                    : new StringCell(detected.getText());
        } catch (IOException e) {
            setWarningMessage(
                    "Some rows could not be checked - see Missing Cell error messages for details");
            return new MissingCell(e.getMessage());

        }
    }

}
