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
package com.vernalis.knime.misc.blobs.nodes.compress.contract;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.compress.contract.CompressNodeDialog.createBlobColNameMdl;
import static com.vernalis.knime.misc.blobs.nodes.compress.contract.CompressNodeDialog.createCompressFormatMdl;
import static com.vernalis.knime.misc.blobs.nodes.compress.contract.CompressNodeDialog.createRemoveInputColumnModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.blobs.nodes.KnowsSizeOutputStream;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapper.OutputStreamWrapException;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions.RowSpecificOutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.compress.CompressFormat;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

/**
 * NodeModel Implementation for the Compress Binary Objects node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class CompressNodeModel
        extends AbstractSimpleStreamableFunctionNodeModel {

    private final SettingsModelString blobColNameMdl =
            registerSettingsModel(createBlobColNameMdl());
    private final SettingsModelBoolean removeInputColumnMdl =
            registerSettingsModel(createRemoveInputColumnModel());
    private final SettingsModelString compressFormatMdl =
            createCompressFormatMdl();

    private CompressFormat compressionFmt = CompressFormat.getDefault();
    private OutputStreamWrapperOptions compressOptions =
            compressionFmt.createOutputStreamOptions();

    private BinaryObjectCellFactory cellFact;

    /**
     * Constructor
     */
    CompressNodeModel() {

        super();
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        cellFact = new BinaryObjectCellFactory(exec);
        return super.execute(inData, exec);
    }

    @Override
    protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
            throws InvalidSettingsException {

        compressOptions.validateInConfigure(new DataTableSpec[] { spec });
        int blobColIdx = getValidatedColumnSelectionModelColumnIndex(
                blobColNameMdl, BLOB_COLUMN_FILTER, spec, getLogger());

        List<DataColumnSpec> outSpecs = new ArrayList<>();
        outSpecs.add(
                new DataColumnSpecCreator(
                        DataTableSpec.getUniqueColumnName(spec,
                                blobColNameMdl.getStringValue()
                                        + " (Compressed)"),
                        BinaryObjectDataCell.TYPE).createSpec());
        outSpecs.add(new DataColumnSpecCreator(
                DataTableSpec.getUniqueColumnName(spec, "Compression Ratio"),
                DoubleCell.TYPE).createSpec());

        ColumnRearranger rearranger = new ColumnRearranger(spec);

        if (removeInputColumnMdl.getBooleanValue()) {
            rearranger.remove(blobColIdx);
        }
        rearranger.append(new AbstractCellFactory(
                outSpecs.toArray(DataColumnSpec[]::new)) {

            @Override
            public DataCell[] getCells(DataRow row) {

                DataCell[] retVal = ArrayUtils.fill(new DataCell[2],
                        DataType.getMissingCell());
                DataCell inCell = row.getCell(blobColIdx);

                if (inCell.isMissing()) {
                    return retVal;
                }
                BinaryObjectDataValue blob = (BinaryObjectDataValue) inCell;
                double compressionRatio = 1.0 / blob.length();

                try (InputStream is = blob.openInputStream()) {
                    // These cannot go in the try-with-resources block,
                    // or they
                    // break!
                    PipedInputStream gzipped = new PipedInputStream();
                    PipedOutputStream pipe = new PipedOutputStream(gzipped);
                    new Thread(() -> {

                        try (OutputStream deflater =
                                compressionFmt.wrapOutputStream(
                                        new KnowsSizeOutputStream(pipe,
                                                blob.length()),
                                        compressOptions instanceof RowSpecificOutputStreamWrapperOptions rowswo
                                                ? rowswo.getOptionsForRow(row)
                                                : compressOptions)) {
                            IOUtils.copy(is, deflater);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        } catch (OutputStreamWrapException e1) {
                            throw e1.unchecked();
                        }
                    }).start();

                    retVal[0] = cellFact.create(gzipped);
                    retVal[1] = new DoubleCell(
                            ((BinaryObjectDataValue) retVal[0]).length()
                                    * compressionRatio);
                } catch (IOException e1) {
                    throw new UncheckedIOException(e1);
                }
                return retVal;
            }

        });
        return rearranger;
    }

    @Override
    public void saveSettingsTo(NodeSettingsWO settings) {

        super.saveSettingsTo(settings);
        compressFormatMdl.saveSettingsTo(settings);
        compressOptions.saveToSettings(settings);
    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        super.validateSettings(settings);

        try {
            CompressFormat cf = CompressFormat
                    .valueOf(((SettingsModelString) compressFormatMdl
                            .createCloneWithValidatedValue(settings))
                                    .getStringValue().replace(' ', '_'));
            cf.createOutputStreamOptions().validateSettings(settings);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidSettingsException(e);
        }
    }

    @Override
    public void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {

        super.loadValidatedSettingsFrom(settings);
        compressFormatMdl.loadSettingsFrom(settings);
        compressionFmt = CompressFormat
                .valueOf(compressFormatMdl.getStringValue().replace(' ', '_'));
        compressOptions = compressionFmt.createOutputStreamOptions();
        compressOptions.loadFromSettings(settings);
    }

    @Override
    protected void reset() {

        cellFact = null;
        super.reset();
    }
}
