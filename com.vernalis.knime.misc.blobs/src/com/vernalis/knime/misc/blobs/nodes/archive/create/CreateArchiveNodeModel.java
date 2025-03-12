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
package com.vernalis.knime.misc.blobs.nodes.archive.create;

import static com.vernalis.knime.misc.blobs.nodes.archive.create.CreateArchiveNodeDialog.createArchiveFormatMdl;
import static com.vernalis.knime.misc.blobs.nodes.archive.create.CreateArchiveNodeDialog.createBlobColNameMdl;
import static com.vernalis.knime.misc.blobs.nodes.archive.create.CreateArchiveNodeDialog.createPathColNameMdl;
import static com.vernalis.knime.misc.blobs.nodes.archive.create.CreateArchiveNodeDialog.createTimestampTypeModel;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.date.DateAndTimeValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.time.localdatetime.LocalDateTimeValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.knime.misc.blobs.nodes.BlobConstants;
import com.vernalis.knime.misc.blobs.nodes.LastModifiedDateTimeOptions;
import com.vernalis.knime.misc.blobs.nodes.LastModifiedDateTimeOptions.ColumnTimeOptions;
import com.vernalis.knime.misc.blobs.nodes.LastModifiedDateTimeOptions.FixedTimeOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapper.OutputStreamWrapException;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.UserOptions;
import com.vernalis.knime.misc.blobs.nodes.archive.ArchiveFormat;

/**
 * {@link NodeModel} implementation for the Create Archive node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class CreateArchiveNodeModel extends NodeModel {

    private final SettingsModelString blobColNameMdl = createBlobColNameMdl();
    private final SettingsModelColumnName pathColNameMdl =
            createPathColNameMdl();

    private final SettingsModelString archiveFormatMdl =
            createArchiveFormatMdl();
    private final SettingsModelString timestampTypeMdl =
            createTimestampTypeModel();

    private ArchiveFormat archiveFormat = ArchiveFormat.getDefault();
    private OutputStreamWrapperOptions archiveOptions =
            archiveFormat.createOutputStreamOptions();

    private LastModifiedDateTimeOptions timestampType =
            LastModifiedDateTimeOptions.getDefault();
    private UserOptions timestampOptions = timestampType.get();

    /**
     * Constructor
     */
    CreateArchiveNodeModel() {

        super(1, 1);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // Make sure any additional validation is done on the timestampOptions
        // Most happens in loadSettings()
        String msg = timestampOptions.validateInConfigure(inSpecs);

        if (msg != null) {
            setWarningMessage(msg);
        }
        getValidatedColumnIndex(inSpecs[0], BlobConstants.BLOB_COLUMN_FILTER, blobColNameMdl);

        if (!pathColNameMdl.useRowID()) {
            getValidatedColumnIndex(inSpecs[0], BlobConstants.PATH_COLUMN_FILTER,
                    pathColNameMdl);
        }
        return new DataTableSpec[] { createOutputSpec() };
    }

    private final int getValidatedColumnIndex(DataTableSpec spec,
            ColumnFilter colFilter, SettingsModelString colNameMdl)
            throws InvalidSettingsException {

        List<String> allowedColumnNames =
                spec.stream().filter(colFilter::includeColumn)
                        .map(DataColumnSpec::getName).toList();

        if (allowedColumnNames.isEmpty()) {
            throw new InvalidSettingsException(colFilter.allFilteredMsg());
        }
        String selectedColName = colNameMdl.getStringValue();

        if (selectedColName != null && !selectedColName.isEmpty()) {

            // We have a column selection
            if (!allowedColumnNames.contains(selectedColName)) {
                // it isnt valid
                throw new InvalidSettingsException("The selected column ("
                        + selectedColName
                        + ") is not present in the incoming table or is of the wrong type");
            }
        } else {
            // No selection - guess the last one of the right type
            selectedColName =
                    allowedColumnNames.get(allowedColumnNames.size() - 1);
            setWarningMessage(
                    "No column selected - guessed '" + selectedColName + "'!");
            colNameMdl.setStringValue(selectedColName);
        }
        return spec.findColumnIndex(selectedColName);
    }

    private DataTableSpec createOutputSpec() {

        return new DataTableSpecCreator().addColumns(new DataColumnSpecCreator(
                blobColNameMdl.getStringValue() + " (Archived)",
                BinaryObjectDataCell.TYPE).createSpec()).createSpec();
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        final BufferedDataTable inTable = inData[0];
        DataTableSpec inSpec = inTable.getDataTableSpec();
        int blobColIdx = getValidatedColumnIndex(inSpec, BlobConstants.BLOB_COLUMN_FILTER,
                blobColNameMdl);
        int pathColIdx = pathColNameMdl.useRowID() ? -1
                : getValidatedColumnIndex(inSpec,
                        BlobConstants.PATH_COLUMN_FILTER,
                pathColNameMdl);
        int timestampCol = timestampType == LastModifiedDateTimeOptions.Column
                ? ((ColumnTimeOptions) timestampOptions).getColumnIndex(inSpec)
                : -1;
        Function<DataRow, Date> timestampProvider =
                getTimestampProvider(timestampCol);

        // Once commons-io 2.9.0 is available we can use
        // https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/input/QueueInputStream.html
        PipedOutputStream pipe = new PipedOutputStream();
        PipedInputStream archived = new PipedInputStream(pipe);

        // Pass exception from the thread below back to the user rather than the
        // otherwise rather cryptic pipe end closed error
        // Based on https://stackoverflow.com/a/45510494/6076839
        AtomicReference<Exception> osException = new AtomicReference<>();
        FilterInputStream fis = new FilterInputStream(archived) {

            @Override
            public void close() throws IOException {

                try {
                    Exception e = osException.get();

                    if (e != null) {
                        throw new IOException(
                                "Error in writer - " + e.getMessage(), e);
                    }
                } finally {
                    super.close();
                }
            }

        };

        final Thread thread = new Thread(() -> {
            long rows = inTable.size();
            double progPerRow = 1.0 / rows;
            long rowCnt = 0l;
            // We have to create the output stream in the writer thread
            ArchiveOutputStream<?> deflater;

            try {
                deflater = archiveFormat.wrapOutputStream(pipe, archiveOptions);
            } catch (OutputStreamWrapException e) {
                throw e.unchecked();
            }

            for (DataRow row : inTable) {
                exec.setProgress(rowCnt * progPerRow, "Read " + (rowCnt++)
                        + " of " + rows + " binary objects");

                try {
                    exec.checkCanceled();
                } catch (CanceledExecutionException e1) {
                    osException.set(e1);
                }
                // Get the Path
                String path = getPathFromRow(pathColIdx, row);

                DataCell blobCell = row.getCell(blobColIdx);
                File f = getFileFromBlobCell(timestampProvider, row, path,
                        blobCell);

                try {
                    addEntryToArchive(deflater, path, blobCell, f);
                } catch (IOException e) {
                    osException.set(e);
                }
            }

            try {
                deflater.finish();
                deflater.close();
                pipe.close();
            } catch (IOException e) {
                osException.set(e);
            }
        }, "Archiver Thread");
        thread.start();

        BinaryObjectCellFactory cellFact = new BinaryObjectCellFactory(exec);
        BufferedDataContainer bdc =
                exec.createDataContainer(createOutputSpec());
        bdc.addRowToTable(new DefaultRow(RowKey.createRowKey(0L),
                cellFact.create(archived)));
        fis.close();
        bdc.close();
        return new BufferedDataTable[] { bdc.getTable() };
    }

    private <E extends ArchiveEntry> void addEntryToArchive(
            ArchiveOutputStream<E> deflater, String path,
            DataCell blobCell, File f) throws IOException {

        if (f != null
                && (!f.isDirectory() || archiveFormat.supportsDirectories())) {
            E ent = deflater.createArchiveEntry(f, path);
            deflater.putArchiveEntry(ent);

            if (!blobCell.isMissing()) {

                try (InputStream is =
                        ((BinaryObjectDataValue) blobCell)
                                .openInputStream()) {
                    IOUtils.copy(is, deflater);
                }
            }
            deflater.closeArchiveEntry();
        }
    }

    private String getPathFromRow(int pathColIdx, DataRow row) {

        String path;

        if (pathColNameMdl.useRowID()) {
            path = row.getKey().getString();
        } else {
            DataCell pathCell = row.getCell(pathColIdx);

            if (pathCell.isMissing()) {
                path = null;
            } else {
                path = ((StringValue) pathCell).getStringValue();
            }
        }
        return path;
    }

    private Function<DataRow, Date> getTimestampProvider(int timestampCol) {

        @SuppressWarnings("deprecation")
        Function<DataRow, Date> timestampProvider = row -> {

            switch (timestampType) {
                case Column:
                    DataCell tCell = row.getCell(timestampCol);
                    if (tCell.isMissing()) {
                        return null;
                    }
                    if (tCell instanceof DateAndTimeValue datv) {
                        return datv.getUTCCalendarClone().getTime();
                    } else if (tCell instanceof LocalDateTimeValue ldtv) {
                        return Date.from(ldtv.getLocalDateTime()
                                .atZone(ZoneId.systemDefault()).toInstant());
                    }
                    return null;
                case Fixed_Time:
                    return ((FixedTimeOptions) timestampOptions).getDate();
                case Runtime:
                    return new Date();
                default:
                    return null;
            }
        };
        return timestampProvider;
    }

    private File getFileFromBlobCell(Function<DataRow, Date> timestampProvider,
            DataRow row, String path, DataCell blobCell) {

        if (path == null) {
            return null;
        }
        @SuppressWarnings("serial")
        File f = new File(path) {

            @Override
            public boolean isDirectory() {

                return path.endsWith("/") || path.endsWith("\\");
            }

            @Override
            public long length() {

                return blobCell.isMissing() ? 0
                        : ((BinaryObjectDataValue) blobCell).length();
            }

            @Override
            public long lastModified() {

                final Date date = timestampProvider == null ? null
                        : timestampProvider.apply(row);
                return date == null ? 0L : date.getTime();
            }

        };
        return f;
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {

        blobColNameMdl.saveSettingsTo(settings);
        pathColNameMdl.saveSettingsTo(settings);

        archiveFormatMdl.saveSettingsTo(settings);
        archiveOptions.saveToSettings(settings);

        timestampTypeMdl.saveSettingsTo(settings);
        timestampOptions.saveToSettings(settings);

    }

    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        blobColNameMdl.validateSettings(settings);
        pathColNameMdl.validateSettings(settings);

        try {
            ArchiveFormat af = ArchiveFormat
                    .valueOf(((SettingsModelString) archiveFormatMdl
                            .createCloneWithValidatedValue(settings))
                                    .getStringValue().replace(' ', '_'));
            af.createOutputStreamOptions().validateSettings(settings);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown archive format", e);
        }

        try {
            LastModifiedDateTimeOptions edto = LastModifiedDateTimeOptions
                    .valueOf(((SettingsModelString) timestampTypeMdl
                            .createCloneWithValidatedValue(settings))
                                    .getStringValue());
            edto.get().validateSettings(settings);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown timestamp type", e);
        }
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {

        blobColNameMdl.loadSettingsFrom(settings);
        pathColNameMdl.loadSettingsFrom(settings);
        archiveFormatMdl.loadSettingsFrom(settings);
        timestampTypeMdl.loadSettingsFrom(settings);

        try {
            archiveFormat =
                    ArchiveFormat.valueOf(archiveFormatMdl.getStringValue()
                            .replace(' ', '_'));
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown archive format", e);
        }
        archiveOptions = archiveFormat.createOutputStreamOptions();
        archiveOptions.loadFromSettings(settings);

        try {
            timestampType = LastModifiedDateTimeOptions
                    .valueOf(timestampTypeMdl.getStringValue());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown timestamp type", e);
        }
        timestampOptions = timestampType.get();
        timestampOptions.loadFromSettings(settings);
    }

    @Override
    protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {

        //
    }

    @Override
    protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {

        //
    }

    @Override
    protected void reset() {

        //
    }

}
