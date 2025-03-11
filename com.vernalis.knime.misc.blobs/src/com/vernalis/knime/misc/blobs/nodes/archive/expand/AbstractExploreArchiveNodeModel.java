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
package com.vernalis.knime.misc.blobs.nodes.archive.expand;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createArchiveFormatMdl;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createBlobColNameMdl;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createCaseSensitiveModel;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createFilterPathsModel;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createKeepDirectoriesModel;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createPatternModel;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createPatternTypeModel;
import static com.vernalis.knime.misc.blobs.nodes.archive.expand.ExploreArchiveNodeDialog.createRemoveInputColumnModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.PatternSyntaxException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.knime.data.datarow.RemoveColumnsDataRow;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.blobs.nodes.ExpansionBombProof.ExpansionDetonationException;
import com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapper.InputStreamWrapException;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer;
import com.vernalis.knime.misc.blobs.nodes.archive.ArchiveFormat;
import com.vernalis.knime.misc.blobs.nodes.archive.PassThroughArchiveInputStream;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.knime.nodes.SettingsModelRegistryImpl;

/**
 * Abstract NodeModel class handling most of the implementation of nodes probing
 * the contents of Archived BLOB cell columns
 * 
 * @author S.Roughley knime@vernalis.com
 */
abstract class AbstractExploreArchiveNodeModel extends NodeModel
        implements SecurityOptionsContainer {

    /**
     * {@link SettingsModelRegistry} instance for handling model saving/loading
     */
    protected final SettingsModelRegistry smr =
            new SettingsModelRegistryImpl(2, getLogger()) {

                @Override
                public void doSetWarningMessage(String message) {

                    AbstractExploreArchiveNodeModel.this
                            .setWarningMessage(message);

                }
            };

    /** The settings model containing the blob column name */
    protected final SettingsModelString blobColNameMdl =
            smr.registerSettingsModel(createBlobColNameMdl());
    private final SettingsModelBoolean removeInputColumnMdl =
            smr.registerSettingsModel(createRemoveInputColumnModel());

    // Handle this one manually
    private final SettingsModelString archiveFormatMdl =
            createArchiveFormatMdl();

    private final SettingsModelBoolean keepDirectoriesMdl =
            smr.registerSettingsModel(createKeepDirectoriesModel());
    private final SettingsModelBoolean filterPathsMdl =
            smr.registerSettingsModel(createFilterPathsModel());
    private final SettingsModelString patternMdl =
            smr.registerSettingsModel(createPatternModel());
    private final SettingsModelString patternTypeMdl =
            smr.registerSettingsModel(createPatternTypeModel());
    private final SettingsModelBoolean caseSensitiveMdl =
            smr.registerSettingsModel(createCaseSensitiveModel());

    // Common security options
    private final SettingsModelIntegerBounded maxExpandedEntriesMdl =
            smr.registerSettingsModel(
                    ExpansionSecurityUtils.createMaxExpandedEntriesModel(), 2,
                    mdl -> mdl.setIntValue(-1));
    private final SettingsModelBoolean failOnExpansionExplosionMdl =
            smr.registerSettingsModel(ExpansionSecurityUtils
                    .createFailOnExpansionExplosionModel(), 2,
                    mdl -> mdl.setBooleanValue(false));

    private ArchiveFormat archiveFormat = ArchiveFormat.getDefault();
    private InputStreamWrapperOptions archiveOptions =
            archiveFormat.createInputStreamOptions();
    private Predicate<String> pathPredicate;

    private int numNewColumns;

    /**
     * Constructor.
     * <p>
     * <strong>NB</strong> Implementing subclasses need to call
     * {@link #updateCanTrapExplodingExpansion()} in constructor
     * </p>
     */
    protected AbstractExploreArchiveNodeModel() {

        super(1, 1);

        archiveFormatMdl.addChangeListener(new ChangeListener() {

            // Dont change the actual format as we handle it manually in the
            // loadSettings stage
            @Override
            public void stateChanged(ChangeEvent e) {

                ArchiveFormat af = ArchiveFormat.valueOf(
                        archiveFormatMdl.getStringValue().replace(' ', '_'));
                keepDirectoriesMdl.setEnabled(af.supportsDirectories());

                if (getMaxExpandedSizeModel() != null) {
                    getMaxExpandedSizeModel()
                            .setEnabled(af.includesCompression());
                }
                if (getMaxCompressionRatioModel() != null) {
                    getMaxCompressionRatioModel()
                            .setEnabled(af.includesCompression());
                }
            }
        });
        keepDirectoriesMdl.setEnabled(archiveFormat.supportsDirectories());
        if (getMaxExpandedSizeModel() != null) {
            getMaxExpandedSizeModel()
                    .setEnabled(archiveFormat.includesCompression());
        }
        if (getMaxCompressionRatioModel() != null) {
            getMaxCompressionRatioModel()
                    .setEnabled(archiveFormat.includesCompression());
        }
        filterPathsMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                patternMdl.setEnabled(filterPathsMdl.getBooleanValue());
                patternTypeMdl.setEnabled(filterPathsMdl.getBooleanValue());
                caseSensitiveMdl.setEnabled(filterPathsMdl.getBooleanValue());
            }

        });
        patternMdl.setEnabled(filterPathsMdl.getBooleanValue());
        patternTypeMdl.setEnabled(filterPathsMdl.getBooleanValue());
        caseSensitiveMdl.setEnabled(filterPathsMdl.getBooleanValue());

        maxExpandedEntriesMdl.addChangeListener(this);

    }

    @Override
    protected final DataTableSpec[] configure(DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // NB The archive format is all handled in the loadSettings method
        // Doing anything with it here will break the archiveOptions
        validateAndSetPathPredicate();

        // Check/guess the column name
        final DataTableSpec inTableSpec = inSpecs[0];
        getValidatedColumnIndex(inTableSpec);

        return new DataTableSpec[] { createOutputSpec(inTableSpec) };
    }

    private final void validateAndSetPathPredicate()
            throws InvalidSettingsException {

        if (filterPathsMdl.getBooleanValue()) {
            if (patternMdl.getStringValue() == null
                    || patternMdl.getStringValue().isBlank()) {
                throw new InvalidSettingsException("No pattern supplied");
            }
            try {
                PathFilterType filtType =
                        PathFilterType.valueOf(patternTypeMdl.getStringValue());
                pathPredicate =
                        filtType.getPathPredicate(patternMdl.getStringValue(),
                                caseSensitiveMdl.getBooleanValue());
            } catch (PatternSyntaxException pse) {
                throw new InvalidSettingsException(
                        "Error parsing path pattern '"
                                + patternMdl.getStringValue() + "' - "
                                + pse.getMessage(),
                        pse);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new InvalidSettingsException("Unknown path pattern type '"
                        + patternTypeMdl.getStringValue() + "'", e);
            }
        } else {
            pathPredicate = str -> true;
        }
    }

    private final int getValidatedColumnIndex(DataTableSpec spec)
            throws InvalidSettingsException {

        List<String> allowedColumnNames =
                spec.stream().filter(BLOB_COLUMN_FILTER::includeColumn)
                        .map(DataColumnSpec::getName).toList();

        if (allowedColumnNames.isEmpty()) {
            throw new InvalidSettingsException(
                    "No Binary Objects columns in input table!");
        }
        String selectedColName = blobColNameMdl.getStringValue();

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
            blobColNameMdl.setStringValue(selectedColName);
        }
        return spec.findColumnIndex(selectedColName);
    }

    private final DataTableSpec createOutputSpec(DataTableSpec spec) {

        DataTableSpecCreator outSpecFact;

        if (removeInputColumnMdl.getBooleanValue()) {
            outSpecFact = new DataTableSpecCreator();
            spec.stream().filter(
                    x -> !x.getName().equals(blobColNameMdl.getStringValue()))
                    .forEachOrdered(outSpecFact::addColumns);
        } else {
            outSpecFact = new DataTableSpecCreator(spec);

        }
        final DataColumnSpec[] newColSpecs = createNewColSpecs(spec);
        outSpecFact.addColumns(newColSpecs);

        Collection<DataColumnSpec> extraCols = createAdditionalNewColumnSpecs();

        if (extraCols != null) {
            for (DataColumnSpec extraCol : extraCols) {
                String name = DataTableSpec.getUniqueColumnName(
                        outSpecFact.createSpec(), extraCol.getName());
                DataColumnSpecCreator colFact =
                        new DataColumnSpecCreator(extraCol);
                colFact.setName(name);
                outSpecFact.addColumns(colFact.createSpec());
            }
        }
        numNewColumns =
                newColSpecs.length + (extraCols == null ? 0 : extraCols.size());
        return outSpecFact.createSpec();
    }

    private final DataColumnSpec[] createNewColSpecs(DataTableSpec spec) {

        List<DataColumnSpec> newColSpecs = new ArrayList<>();

        if (archiveFormat == ArchiveFormat.guess) {
            newColSpecs.add(new DataColumnSpecCreator(
                    DataTableSpec.getUniqueColumnName(spec, "Detected Format"),
                    StringCell.TYPE).createSpec());
        }
        newColSpecs.add(new DataColumnSpecCreator(
                DataTableSpec.getUniqueColumnName(spec, "Archive Path"),
                StringCell.TYPE).createSpec());

        final boolean includeIsDirCol = archiveFormat.supportsDirectories()
                && keepDirectoriesMdl.getBooleanValue();

        if (includeIsDirCol) {
            newColSpecs.add(new DataColumnSpecCreator(
                    DataTableSpec.getUniqueColumnName(spec, "Is Directory?"),
                    BooleanCellFactory.TYPE).createSpec());
        }
        newColSpecs.add(new DataColumnSpecCreator(
                DataTableSpec.getUniqueColumnName(spec, "Entry Last Modified"),
                LocalDateTimeCellFactory.TYPE).createSpec());

        return newColSpecs.toArray(DataColumnSpec[]::new);
    }

    /**
     * This method should add any additional columns to the basic set, which
     * are:
     * <ol>
     * <li>Archive Path (String)</li>
     * <li>Is Directory? (Only present if the {@code includesIsDirCol} is
     * {@code true}</li>
     * <li>Entry Last Modified</li>
     * </ol>
     * 
     * @return any new column specs. Names will be uniquified
     */
    protected abstract List<DataColumnSpec> createAdditionalNewColumnSpecs();

    @Override
    protected final BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        BufferedDataTable inTable = inData[0];
        DataTableSpec inSpec = inTable.getDataTableSpec();
        int colIdx = getValidatedColumnIndex(inSpec);
        BufferedDataContainer bdc =
                exec.createDataContainer(createOutputSpec(inSpec));

        if (inTable.size() > 0) {
            RowOutput rowOut = new BufferedDataTableRowOutput(bdc);
            RowInput rowIn = new DataTableRowInput(inTable);
            doExecute(colIdx, rowIn, rowOut, inTable.size(), exec);
        }
        bdc.close();
        return new BufferedDataTable[] { bdc.getTable() };
    }

    @Override
    public final StreamableOperator createStreamableOperator(
            PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        return new StreamableOperator() {

            @Override
            public void runFinal(PortInput[] inputs, PortOutput[] outputs,
                    ExecutionContext exec) throws Exception {

                doExecute(getValidatedColumnIndex((DataTableSpec) inSpecs[0]),
                        (RowInput) inputs[0], (RowOutput) outputs[0], -1, exec);
            }

        };
    }

    @Override
    public final InputPortRole[] getInputPortRoles() {

        return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
    }

    @Override
    public final OutputPortRole[] getOutputPortRoles() {

        return new OutputPortRole[] { OutputPortRole.DISTRIBUTED };
    }

    private final void doExecute(int colIdx, RowInput rowIn, RowOutput rowOut,
            long size, ExecutionContext exec) throws InterruptedException,
            CanceledExecutionException, IOException, InputStreamWrapException,
            InvalidSettingsException, ArchiveException {

        // Make sure the path predicate is set up
        validateAndSetPathPredicate();

        final boolean hasIsDirCol = archiveFormat.supportsDirectories()
                && keepDirectoriesMdl.getBooleanValue();
        final boolean isGuess = archiveFormat == ArchiveFormat.guess;

        BinaryObjectCellFactory cellFact = createBlobCellFactory(exec);
        long rowCnt = 0;
        double progPerRow = 1.0 / size;
        DataRow inRow;

        while ((inRow = rowIn.poll()) != null) {
            exec.checkCanceled();

            if (size > 0) {
                exec.setProgress(progPerRow * (rowCnt++));
            }
            DataRow baseOutRow = removeInputColumnMdl.getBooleanValue()
                    ? new RemoveColumnsDataRow(inRow, colIdx)
                    : inRow;
            DataCell dc = inRow.getCell(colIdx);

            if (dc.isMissing()) {
                DataRow outRow = new AppendedColumnRow(baseOutRow,
                        ArrayUtils.fill(new DataCell[numNewColumns],
                                DataType.getMissingCell()));
                rowOut.push(outRow);
                continue;
            }
            BinaryObjectDataValue blob = (BinaryObjectDataValue) dc;

            try (InputStream is = blob.openInputStream();
                    InputStream bis = is.markSupported() ? is
                            : new BufferedInputStream(is);
                    ArchiveInputStream<?> ais = archiveFormat
                            .wrapInputStream(bis, archiveOptions);) {
                long subRow = 0;
                boolean addedToTable = false;
                String fmt = isGuess && ais != null
                        && !(ais instanceof PassThroughArchiveInputStream)
                                ? ArchiveStreamFactory.detect(bis)
                                : null;

                if (ais == null) {
                    DataCell[] newCells = createNewCellsForEntry(hasIsDirCol,
                            isGuess, cellFact, null, null, fmt);

                    rowOut.push(new AppendedColumnRow(baseOutRow, newCells));
                } else if (ais instanceof InputStreamStatistics) {
                    expandArchiveWithBombproofStream(rowOut, hasIsDirCol,
                            isGuess, cellFact, inRow, baseOutRow, ais, subRow,
                            addedToTable, fmt, exec);
                } else {
                    // Manually count entries - we can do that at least!
                    expandArchiveWithManualEntryCounting(rowOut, hasIsDirCol,
                            isGuess, cellFact, inRow, baseOutRow, ais, subRow,
                            fmt, exec);
                }
            }
        }
    }

    private void expandArchiveWithBombproofStream(RowOutput rowOut,
            final boolean hasIsDirCol, final boolean isGuess,
            BinaryObjectCellFactory cellFact, DataRow inRow, DataRow baseOutRow,
            ArchiveInputStream<?> ais, long subRow, boolean addedToTable,
            String fmt, ExecutionContext exec)
            throws IOException, InterruptedException, UncheckedIOException,
            CanceledExecutionException {

        BombproofArchiveInputStream<?> bais = new BombproofArchiveInputStream<>(
                ais,
                getMaxExpandedSizeModel() == null ? -1
                        : getMaxExpandedSizeModel().getLongValue(),
                getMaxCompressionRatioModel() == null ? -1.0
                        : getMaxCompressionRatioModel().getDoubleValue(),
                getMaxEntriesModel() == null ? -1
                        : getMaxEntriesModel().getIntValue(),
                keepDirectoriesMdl.getBooleanValue(), pathPredicate,
                getLogger(), msg -> setWarningMessage(msg));
        NonClosableInputStream ncis = wrapArchiveInputStream(bais);
        try {
            ArchiveEntry archiveEntry;
            while ((archiveEntry = bais.getNextEntry()) != null) {
                exec.checkCanceled();

                DataCell[] newCells = createNewCellsForEntry(hasIsDirCol,
                        isGuess, cellFact, ncis, archiveEntry, fmt);

                rowOut.push(new AppendedColumnRow(
                        new RowKey(String.format("%s_%d",
                                baseOutRow.getKey().getString(), subRow++)),
                        baseOutRow, newCells));
                addedToTable = true;
            }
        } catch (ExpansionDetonationException e) {
            if (failOnExpansionExplosionMdl.getBooleanValue()) {
                throw new UncheckedIOException(e);
            } else {
                getLogger().warnWithFormat(
                        "Explosive expansion detected in row '%s' - '%s' - Output will be truncated",
                        inRow.getKey().getString(), e.getMessage());
                setWarningMessage(
                        "Explosive expansion detected in some rows resulting in truncated output- check log for details");
                // Make sure we preserve the input row in the
                // table
                if (!addedToTable) {
                    rowOut.push(new AppendedColumnRow(baseOutRow,
                            ArrayUtils.fill(new DataCell[numNewColumns],
                                    new MissingCell(e.getMessage()))));
                }
            }
        }
    }

    private void expandArchiveWithManualEntryCounting(RowOutput rowOut,
            final boolean hasIsDirCol, final boolean isGuess,
            BinaryObjectCellFactory cellFact, DataRow inRow, DataRow baseOutRow,
            ArchiveInputStream<?> ais, long subRow, String fmt,
            ExecutionContext exec) throws IOException, InterruptedException,
            CanceledExecutionException {

        boolean addedToTable;
        ArchiveEntry archiveEntry;
        if ((getMaxExpandedSizeModel() != null
                && getMaxExpandedSizeModel().isEnabled()
                && getMaxExpandedSizeModel().getLongValue() > 0)
                || (getMaxCompressionRatioModel() != null
                        && getMaxCompressionRatioModel().isEnabled()
                        && getMaxCompressionRatioModel()
                                .getDoubleValue() > 0.0)) {
            setWarningMessage(
                    "Archive type stream implementation does not support explosion-proofing");
        }
        NonClosableInputStream ncis = wrapArchiveInputStream(ais);
        while ((archiveEntry = ais.getNextEntry()) != null) {
            exec.checkCanceled();
            if (!ais.canReadEntryData(archiveEntry)) {
                getLogger().warnWithFormat("Unable to read entry '%s'",
                        archiveEntry.getName());
                setWarningMessage(
                        "Unable to read all entries - see log for details");
            } else if ((keepDirectoriesMdl.getBooleanValue()
                    || !archiveEntry.isDirectory())
                    && pathPredicate.test(archiveEntry.getName())) {
                DataCell[] newCells = createNewCellsForEntry(hasIsDirCol,
                        isGuess, cellFact, ncis, archiveEntry, fmt);

                final RowKey outKey = new RowKey(String.format("%s_%d",
                        baseOutRow.getKey().getString(), subRow++));

                rowOut.push(
                        new AppendedColumnRow(outKey, baseOutRow, newCells));
                addedToTable = true;
                if (maxExpandedEntriesMdl.getIntValue() > 0
                        && subRow > maxExpandedEntriesMdl.getIntValue()) {
                    if (failOnExpansionExplosionMdl.getBooleanValue()) {
                        throw new ExpansionDetonationException(String.format(
                                "Number of entries exxtracted (%d) > %d",
                                subRow, maxExpandedEntriesMdl.getIntValue()));
                    } else {
                        // Shortcut to not raise exception
                        // unneccessarily
                        getLogger().warnWithFormat(
                                "Explosive expansion detected in row '%s' - 'Number of entries extracted (%d) > %d' - Output will be truncated",
                                inRow.getKey().getString(), subRow,
                                maxExpandedEntriesMdl.getIntValue());
                        setWarningMessage(
                                "Explosive expansion detected in some rows resulting in truncated output- check log for details");
                        // Make sure we preserve the input row in
                        // the
                        // table
                        if (!addedToTable) {
                            rowOut.push(new AppendedColumnRow(baseOutRow,
                                    ArrayUtils.fill(new DataCell[numNewColumns],
                                            new MissingCell(String.format(
                                                    "'Number of entries extracted (%d) > %d'",
                                                    subRow,
                                                    maxExpandedEntriesMdl
                                                            .getIntValue())))));
                        }
                        // Goto the next input row
                        break;
                    }
                }
            }
        }
    }

    private final DataCell[] createNewCellsForEntry(final boolean hasIsDirCol,
            final boolean isGuess, BinaryObjectCellFactory cellFact,
            NonClosableInputStream ncis, ArchiveEntry archiveEntry, String fmt)
            throws IOException {

        DataCell[] newCells = new DataCell[numNewColumns];
        int colCnt = 0;

        if (isGuess) {
            newCells[colCnt++] = fmt == null ? DataType.getMissingCell()
                    : new StringCell(fmt);
        }
        final String entName =
                archiveEntry == null ? null : archiveEntry.getName();
        newCells[colCnt++] = entName == null ? DataType.getMissingCell()
                : new StringCell(entName);

        if (hasIsDirCol) {
            newCells[colCnt++] = archiveEntry == null
                    ? DataType.getMissingCell()
                    : BooleanCellFactory.create(archiveEntry.isDirectory());
        }
        final Date lastModifiedDate = archiveEntry == null ? null
                : archiveEntry.getLastModifiedDate();
        final LocalDateTime localDateTime = lastModifiedDate == null ? null
                : lastModifiedDate.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
        newCells[colCnt++] = localDateTime == null ? DataType.getMissingCell()
                : LocalDateTimeCellFactory.create(localDateTime);

        DataCell[] extraCells =
                ArrayUtils.fill(new DataCell[numNewColumns - colCnt],
                        DataType.getMissingCell());
        createNewCells(extraCells, cellFact, ncis, archiveEntry);
        System.arraycopy(extraCells, 0, newCells, colCnt, extraCells.length);
        return newCells;
    }

    /**
     * Fill the extra DataCells corresponding to the additional columns from
     * {@link #createAdditionalNewColumnSpecs()} from the wrapped input stream
     * and the current
     * entry. Only called if the entry is to be included in the output table
     * 
     * @param extraCells
     *            an array of the correct size pre-filled with
     *            {@link DataType#getMissingCell()}s. This array should be
     *            modified <i>in situ</i>
     * @param cellFact
     *            the {@link BinaryObjectCellFactory} -
     *            {@code null} if not required be the node
     * @param ncis
     *            the {@link ArchiveInputStream} wrapped in a
     *            {@link NonClosableInputStream} for BLOB-cell
     *            generation - {@code null} if not required by the
     *            node
     * @param archiveEntry
     *            the current entry in the archive
     * @throws IOException
     *             if there was an error reading or writing from
     *             Input/Output Streams
     */
    protected abstract void createNewCells(DataCell[] extraCells,
            BinaryObjectCellFactory cellFact, NonClosableInputStream ncis,
            ArchiveEntry archiveEntry) throws IOException;

    /**
     * @param ais
     *            the {@link ArchiveInputStream} to wrap
     * @return a {@link NonClosableInputStream} wrapping the supplied
     *             stream, or {@code null} if not required
     */
    protected abstract NonClosableInputStream wrapArchiveInputStream(
            ArchiveInputStream<?> ais);

    /**
     * @param exec
     *            the node {@link ExecutionContext}
     * @return a {@link BinaryObjectCellFactory} for creating output BLOB
     *             Cells, or {@code null} if not required
     */
    protected abstract BinaryObjectCellFactory createBlobCellFactory(
            ExecutionContext exec);

    @Override
    protected final void saveSettingsTo(NodeSettingsWO settings) {

        smr.saveSettingsTo(settings);
        archiveFormatMdl.saveSettingsTo(settings);
        archiveOptions.saveToSettings(settings);

    }

    @Override
    protected final void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        smr.validateSettings(settings);

        try {
            // We validate archiveFormatMdl by creating a clone and
            // 1 - Checking we have a valid value
            // 2 - Use the derived ArchiveFormat to validate the settings there
            // without changing the archiveFormat field
            ArchiveFormat af = ArchiveFormat
                    .valueOf(((SettingsModelString) archiveFormatMdl
                            .createCloneWithValidatedValue(settings))
                                    .getStringValue().replace(' ', '_'));
            final InputStreamWrapperOptions tempOpts =
                    af.createInputStreamOptions();
            tempOpts.setWarningConsumer(wrn -> setWarningMessage(wrn));
            tempOpts.validateSettings(settings);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown archive format", e);
        }
    }

    @Override
    protected final void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {

        smr.loadValidatedSettingsFrom(settings);
        archiveFormatMdl.loadSettingsFrom(settings);

        try {
            archiveFormat = ArchiveFormat.valueOf(
                    archiveFormatMdl.getStringValue().replace(' ', '_'));
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown archive format", e);
        }
        keepDirectoriesMdl.setEnabled(archiveFormat.supportsDirectories());
        archiveOptions = archiveFormat.createInputStreamOptions();
        archiveOptions.setWarningConsumer(wrn -> setWarningMessage(wrn));
        archiveOptions.loadFromSettings(settings);

    }

    @Override
    protected final void loadInternals(File nodeInternDir,
            ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {

        //

    }

    @Override
    protected final void saveInternals(File nodeInternDir,
            ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {

        //

    }

    @Override
    protected final void reset() {

        //

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxEntriesModel()
     */
    @Override
    public final SettingsModelIntegerBounded getMaxEntriesModel() {

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
    public final SettingsModelBoolean getFailOnExpansionExplosionModel() {

        return failOnExpansionExplosionMdl;
    }

}
