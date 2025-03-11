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
package com.vernalis.knime.misc.blobs.nodes.compress.expand;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.compress.expand.DecompressNodeDialog.createBlobColNameMdl;
import static com.vernalis.knime.misc.blobs.nodes.compress.expand.DecompressNodeDialog.createCompressFormatMdl;
import static com.vernalis.knime.misc.blobs.nodes.compress.expand.DecompressNodeDialog.createRemoveInputColumnModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.data.datacolumn.EnhancedDataColumnSpecCreator;
import com.vernalis.knime.data.datarow.RemoveColumnsDataRow;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.blobs.nodes.ExpansionBombProof.ExpansionDetonationException;
import com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapper.InputStreamWrapException;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer;
import com.vernalis.knime.misc.blobs.nodes.compress.CompressFormat;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.knime.nodes.SettingsModelRegistryImpl;

/**
 * {@link NodeModel} for the Decompress Binary Objects node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class DecompressNodeModel extends NodeModel
        implements SecurityOptionsContainer {

    private final SettingsModelRegistry smr =
            new SettingsModelRegistryImpl(2, getLogger()) {

                @Override
                public void doSetWarningMessage(String message) {

                    DecompressNodeModel.this.setWarningMessage(message);

                }
            };
    private final SettingsModelString blobColNameMdl =
            smr.registerSettingsModel(createBlobColNameMdl());
    private final SettingsModelBoolean removeInputColumnMdl =
            smr.registerSettingsModel(createRemoveInputColumnModel());
    // Need to handle this manually
    private final SettingsModelString compressFormatMdl =
            createCompressFormatMdl();

    private final SettingsModelLongBounded maxExpandedSizeMdl =
            smr.registerSettingsModel(ExpansionSecurityUtils.createMaxExpandedSizeModel(), 2,
                    mdl -> mdl.setLongValue(-1));
    private final SettingsModelDoubleBounded maxCompressionRatioMdl =
            smr.registerSettingsModel(ExpansionSecurityUtils.createMaxCompressionRatioModel(), 2,
                    mdl -> mdl.setDoubleValue(-1.0));
    private final SettingsModelIntegerBounded maxExpandedEntriesMdl =
            smr.registerSettingsModel(ExpansionSecurityUtils.createMaxExpandedEntriesModel(), 2,
                    mdl -> mdl.setIntValue(-1));
    private final SettingsModelBoolean failOnExpansionExplosionMdl =
            smr.registerSettingsModel(ExpansionSecurityUtils.createFailOnExpansionExplosionModel(), 2,
                    mdl -> mdl.setBooleanValue(false));

    private CompressFormat compressionFmt = CompressFormat.getDefault();
    private InputStreamWrapperOptions expandOptions =
            compressionFmt.createInputStreamOptions();

    /**
     * Constructor
     */
    DecompressNodeModel() {

        super(1, 1);
        compressFormatMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                maxExpandedEntriesMdl
                        .setEnabled(CompressFormat
                                .valueOf(compressFormatMdl.getStringValue()
                                        .replace(' ', '_'))
                                .supportsConcatenation());

            }
        });
        maxExpandedSizeMdl.addChangeListener(this);
        maxCompressionRatioMdl.addChangeListener(this);
        maxExpandedEntriesMdl.addChangeListener(this);
        maxExpandedEntriesMdl.setEnabled(CompressFormat
                .valueOf(compressFormatMdl.getStringValue().replace(' ', '_'))
                .supportsConcatenation());
        updateCanTrapExplodingExpansion();
    }



    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        expandOptions.validateInConfigure(inSpecs);

        return new DataTableSpec[] {
                createColumnRearrange(inSpecs[0], createNewColSpecs(), null)
                        .createSpec() };
    }

    private final List<DataColumnSpec> createNewColSpecs() {

        List<DataColumnSpec> outSpecs = new ArrayList<>();
        outSpecs.add(new DataColumnSpecCreator(
                blobColNameMdl.getStringValue() + " (Expanded)",
                BinaryObjectDataCell.TYPE).createSpec());
        outSpecs.addAll(
                compressionFmt.getAdditionalDecompressionOutputColumnSpecs());
        return outSpecs;
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        BufferedDataTable inTable = inData[0];
        DataTableSpec inSpec = inTable.getDataTableSpec();
        List<DataColumnSpec> newColSpecs = createNewColSpecs();
        ColumnRearranger rearranger =
                createColumnRearrange(inSpec, newColSpecs, exec);
        BufferedDataTable outTable;

        if (compressionFmt.supportsConcatenation()) {
            // We have to do this manually...
            BufferedDataContainer bdc =
                    exec.createDataContainer(rearranger.createSpec());
            int colIdx =
                    inSpec.findColumnIndex(blobColNameMdl.getStringValue());
            BinaryObjectCellFactory cellFact =
                    new BinaryObjectCellFactory(exec);
            long rowCount = 0L;
            double progressPerRow = 1.0 / inTable.size();

            for (DataRow row : inTable) {
                exec.checkCanceled();
                exec.setProgress(rowCount * progressPerRow, "Processing row "
                        + (rowCount++) + " of " + inTable.size());

                DataRow baseOutRow = removeInputColumnMdl.getBooleanValue()
                        ? new RemoveColumnsDataRow(row, colIdx)
                        : row;
                DataCell inCell = row.getCell(colIdx);

                if (inCell.isMissing()) {
                    bdc.addRowToTable(new AppendedColumnRow(baseOutRow,
                            ArrayUtils.fill(new DataCell[newColSpecs.size()],
                                    DataType.getMissingCell())));
                    continue;
                }
                try (InputStream is =
                        ((BinaryObjectDataValue) inCell).openInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is)) {
                    createOutputRowsForCurrentInputRow(exec, newColSpecs, bdc,
                            cellFact, row, baseOutRow, bis);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (InputStreamWrapException e1) {
                    throw e1.unchecked();
                }
            }
            bdc.close();
            outTable = bdc.getTable();
        } else {
            // We have a 1->1 row mapping so use Column Rearranger for
            // efficiency
            outTable =
                    exec.createColumnRearrangeTable(inTable, rearranger, exec);
        }
        return new BufferedDataTable[] { outTable };
    }

    private void createOutputRowsForCurrentInputRow(ExecutionContext exec,
            List<DataColumnSpec> newColSpecs, BufferedDataContainer bdc,
            BinaryObjectCellFactory cellFact, DataRow row, DataRow baseOutRow,
            BufferedInputStream bis) throws IOException,
            CanceledExecutionException, UnsupportedOperationException,
            InputStreamWrapException, UncheckedIOException {

        long subRowIdx = 0L;
        String inKeyStr = baseOutRow.getKey().getString();
        long totalBytesRead = 0L;
        long totalExpandedBytes = 0L;
        boolean addedToTable = false;
        while (hasNextFile(bis)) {
            try (NonClosableInputStream ncis = new NonClosableInputStream(bis);
                    CompressorInputStream cis = compressionFmt
                            .wrapInputStream(ncis, expandOptions)) {
                DataCell[] appendedCells =
                        ArrayUtils.fill(new DataCell[newColSpecs.size()],
                                DataType.getMissingCell());

                final CompressorInputStream bcis = wrapCISbombproof(
                        totalBytesRead, totalExpandedBytes, cis);
                appendedCells[0] = cellFact.create(bcis);
                totalBytesRead += bcis instanceof InputStreamStatistics iss
                        ? iss.getCompressedCount()
                        : bcis.getUncompressedCount();
                totalExpandedBytes += bcis.getUncompressedCount();

                System.arraycopy(
                        compressionFmt
                                .getAdditionalDecompressionOutputCells(cis),
                        0, appendedCells, 1, appendedCells.length - 1);
                RowKey outKey = new RowKey(
                        String.format("%s_%d", inKeyStr, subRowIdx++));
                if (maxExpandedEntriesMdl.getIntValue() > 0
                        && subRowIdx > maxExpandedEntriesMdl.getIntValue()) {
                    if (failOnExpansionExplosionMdl.getBooleanValue()) {
                        throw new ExpansionDetonationException(String.format(
                                "Number of files expanded (%d) > %d", subRowIdx,
                                maxExpandedEntriesMdl.getIntValue()));
                    } else {
                        // Shortcut to not raise exception
                        // unneccessarily
                        getLogger().warnWithFormat(
                                "Explosive expansion detected in row '%s' - 'Number of files expanded (%d) > %d' - Output will be truncated",
                                row.getKey().getString(), subRowIdx,
                                maxExpandedEntriesMdl.getIntValue());
                        setWarningMessage(
                                "Explosive expansion detected in some rows resulting in truncated output- check log for details");
                        // Goto the next input row
                        break;
                    }
                }
                bdc.addRowToTable(new AppendedColumnRow(outKey, baseOutRow,
                        appendedCells));
                addedToTable = true;
                exec.checkCanceled();
            } catch (ExpansionDetonationException e) {
                if (failOnExpansionExplosionMdl.getBooleanValue()) {
                    throw new UncheckedIOException(e);
                } else {
                    getLogger().warnWithFormat(
                            "Explosive expansion detected in row '%s' - '%s' - Output will be truncated",
                            row.getKey().getString(), e.getMessage());
                    setWarningMessage(
                            "Explosive expansion detected in some rows resulting in truncated output- check log for details");
                    // Make sure we preserve the input row in the
                    // table
                    if (!addedToTable) {
                        bdc.addRowToTable(new AppendedColumnRow(baseOutRow,
                                ArrayUtils.fill(
                                        new DataCell[newColSpecs.size()],
                                        new MissingCell(e.getMessage()))));
                    }
                    // Goto the next input row
                    break;
                }
            }
        }
    }

    private CompressorInputStream wrapCISbombproof(long totalBytesRead,
            long totalExpandedBytes, CompressorInputStream cis) {

        if (cis instanceof InputStreamStatistics) {
            return new BombproofCompressorInputStream(cis,
                    maxExpandedSizeMdl.getLongValue(),
                    maxCompressionRatioMdl.getDoubleValue(), totalExpandedBytes,
                    totalBytesRead);
        } else if (failOnExpansionExplosionMdl.isEnabled()) {
            setWarningMessage(
                    "Compression type stream implementation does not support explosion-proofing");
        }
        return cis;
    }

    private boolean hasNextFile(BufferedInputStream bis) throws IOException {

        try {
            bis.mark(1);
            int b = bis.read();
            bis.reset();
            return b != -1;
        } catch (IOException e) {
            return false;
        }
    }

    private ColumnRearranger createColumnRearrange(DataTableSpec inSpec,
            List<DataColumnSpec> newColSpecs, ExecutionContext exec)
            throws InvalidSettingsException {

        List<String> blobNames =
                inSpec.stream().filter(BLOB_COLUMN_FILTER::includeColumn)
                        .map(DataColumnSpec::getName).toList();

        if (blobNames.isEmpty()) {
            throw new InvalidSettingsException(
                    BLOB_COLUMN_FILTER.allFilteredMsg());
        }
        if (blobColNameMdl.getStringValue() != null
                && !blobColNameMdl.getStringValue().isBlank()) {
            if (!blobNames.contains(blobColNameMdl.getStringValue())) {
                throw new InvalidSettingsException("Selectd column '"
                        + blobColNameMdl.getStringValue()
                        + "' is not present in incoming table or is of wrong type");
            }
        } else {
            String guess = blobNames.get(blobNames.size() - 1);
            setWarningMessage("No column selected - guessing '" + guess + "'");
            blobColNameMdl.setStringValue(guess);
        }
        int colIdx = inSpec.findColumnIndex(blobColNameMdl.getStringValue());

        BinaryObjectCellFactory cellFact =
                exec == null ? null : new BinaryObjectCellFactory(exec);

        // Check the selected format supports decompression - for some formats,
        // this is platform dependent!
        if (!compressionFmt.supportsDecompression()) {
            throw new InvalidSettingsException(
                    "The selected compression format '"
                            + compressionFmt.getText()
                            + "' does not support decompression on this system");
        }
        ColumnRearranger rearranger = new ColumnRearranger(inSpec);

        if (removeInputColumnMdl.getBooleanValue()) {
            rearranger.remove(
                    inSpec.findColumnIndex(blobColNameMdl.getStringValue()));
        }
        rearranger.append(new AbstractCellFactory(createNewColSpecs().stream()
                .map(colSpec -> inSpec.containsName(colSpec.getName())
                        ? new EnhancedDataColumnSpecCreator(colSpec)
                                .setName(DataTableSpec.getUniqueColumnName(
                                        inSpec, colSpec.getName()))
                                .createSpec()
                        : colSpec)
                .toArray(DataColumnSpec[]::new)) {

            @Override
            public DataCell[] getCells(DataRow row) {

                DataCell[] retVal =
                        ArrayUtils.fill(new DataCell[newColSpecs.size()],
                                DataType.getMissingCell());
                DataCell inCell = row.getCell(colIdx);

                if (inCell.isMissing()) {
                    return retVal;
                }
                try (InputStream is =
                        ((BinaryObjectDataValue) inCell).openInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        CompressorInputStream cis = compressionFmt
                                .wrapInputStream(bis, expandOptions)) {
                    if (cis != null) {
                        retVal[0] = cellFact.create(wrapCISbombproof(cis));
                    }
                    System.arraycopy(
                            compressionFmt
                                    .getAdditionalDecompressionOutputCells(cis),
                            0, retVal, 1, retVal.length - 1);
                } catch (ExpansionDetonationException e) {
                    if (failOnExpansionExplosionMdl.getBooleanValue()) {
                        throw new UncheckedIOException(e);
                    } else {
                        getLogger().warnWithFormat(
                                "Explosive expansion detected in row '%s' - '%s' - Output will be truncated",
                                row.getKey().getString(), e.getMessage());
                        setWarningMessage(
                                "Explosive expansion detected in some rows resulting in truncated output- check log for details");
                        // Make sure we preserve the input row in the
                        // table
                        return ArrayUtils.fill(new DataCell[newColSpecs.size()],
                                new MissingCell(e.getMessage()));

                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (InputStreamWrapException e1) {
                    throw e1.unchecked();
                }
                return retVal;
            }

        });
        return rearranger;
    }

    private CompressorInputStream wrapCISbombproof(CompressorInputStream cis) {

        if (cis instanceof InputStreamStatistics) {
            return new BombproofCompressorInputStream(cis,
                    maxExpandedSizeMdl.getLongValue(),
                    maxCompressionRatioMdl.getDoubleValue());
        } else if (failOnExpansionExplosionMdl.isEnabled()) {
            setWarningMessage(
                    "Compression type stream implementation does not support explosion-proofing");
        }
        return cis;
    }

    @Override
    public void saveSettingsTo(NodeSettingsWO settings) {

        smr.saveSettingsTo(settings);
        compressFormatMdl.saveSettingsTo(settings);
        expandOptions.saveToSettings(settings);
    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        smr.validateSettings(settings);

        try {
            CompressFormat cf = CompressFormat
                    .valueOf(((SettingsModelString) compressFormatMdl
                            .createCloneWithValidatedValue(settings))
                                    .getStringValue().replace(' ', '_'));
            final InputStreamWrapperOptions tmpOpts =
                    cf.createInputStreamOptions();
            tmpOpts.setWarningConsumer(wrn -> setWarningMessage(wrn));
            tmpOpts.validateSettings(settings);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidSettingsException(e);
        }
    }

    @Override
    public void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {

        smr.loadValidatedSettingsFrom(settings);

        compressFormatMdl.loadSettingsFrom(settings);
        compressionFmt = CompressFormat
                .valueOf(compressFormatMdl.getStringValue().replace(' ', '_'));
        expandOptions = compressionFmt.createInputStreamOptions();
        expandOptions.setWarningConsumer(wrn -> setWarningMessage(wrn));
        expandOptions.loadFromSettings(settings);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxExpandedSizeModel()
     */
    @Override
    public SettingsModelLongBounded getMaxExpandedSizeModel() {

        return maxExpandedSizeMdl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxCompressionRatioModel()
     */
    @Override
    public SettingsModelDoubleBounded getMaxCompressionRatioModel() {

        return maxCompressionRatioMdl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxEntriesModel()
     */
    @Override
    public SettingsModelIntegerBounded getMaxEntriesModel() {

        return maxExpandedEntriesMdl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getFailOnExpansionExplosionModel()
     */
    @Override
    public SettingsModelBoolean getFailOnExpansionExplosionModel() {

        return failOnExpansionExplosionMdl;
    }

}
