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
package com.vernalis.knime.misc.blobs.nodes.compress.formats;

import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Compress;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Concatenation;
import static com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities.Expand;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.vernalis.knime.misc.blobs.nodes.compress.CompressFormat;
import com.vernalis.knime.misc.blobs.nodes.compress.CompressorCapabilities;
import com.vernalis.knime.streams.DataCellCollectors;

/**
 * {@link NodeModel} implementation for the List Compression Formats node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class ListCompressionFormatsNodeModel extends NodeModel {

    private static final DataTableSpec OUT_SPEC =
            new DataTableSpecCreator().addColumns(
                    new DataColumnSpecCreator("Format", StringCell.TYPE)
                            .createSpec(),
                    new DataColumnSpecCreator("Description", StringCell.TYPE)
                            .createSpec(),
                    new DataColumnSpecCreator("Supports Compression",
                            BooleanCellFactory.TYPE).createSpec(),
                    new DataColumnSpecCreator("Compression Description",
                            StringCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("Supports Decompression",
                            BooleanCellFactory.TYPE).createSpec(),
                    new DataColumnSpecCreator("Decompression Description",
                            StringCell.TYPE).createSpec(),
                    new DataColumnSpecCreator("Supports Concatenation",
                            BooleanCellFactory.TYPE).createSpec(),
                    new DataColumnSpecCreator("Additional Capabilities",
                            ListCell.getCollectionType(StringCell.TYPE))
                                    .createSpec(),
                    new DataColumnSpecCreator(
                            "Additional Output Columns (Decompression)",
                            ListCell.getCollectionType(StringCell.TYPE))
                                    .createSpec())
                    .createSpec();

    private static final Set<CompressorCapabilities> DEFAULT_CAPABILITIES =
            EnumSet.of(Compress, Expand, Concatenation);

    /**
     * Constructor
     */
    ListCompressionFormatsNodeModel() {

        super(0, 1);
    }

    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        return new DataTableSpec[] { OUT_SPEC };
    }

    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData,
            ExecutionContext exec) throws Exception {

        BufferedDataContainer bdc = exec.createDataContainer(OUT_SPEC);
        final CompressFormat[] formats = CompressFormat.values();
        double progPerRow = 1.0 / formats.length;

        for (CompressFormat cf : formats) {
            exec.checkCanceled();
            exec.setProgress(cf.ordinal() * progPerRow,
                    String.format("Processed %d of %d formats", cf.ordinal(),
                            formats.length));

            RowKey key = RowKey.createRowKey((long) cf.ordinal());
            DataCell[] cells = new DataCell[OUT_SPEC.getNumColumns()];
            int colIdx = 0;
            cells[colIdx++] = new StringCell(cf.getText());
            cells[colIdx++] =
                    cf.hasDescription() ? new StringCell(cf.getDescription())
                            : DataType.getMissingCell();
            cells[colIdx++] =
                    BooleanCellFactory.create(cf.supportsCompression());
            cells[colIdx++] = cf.hasCompressDescription()
                    ? new StringCell(cf.getCompressDescription())
                    : DataType.getMissingCell();
            cells[colIdx++] =
                    BooleanCellFactory.create(cf.supportsDecompression());
            cells[colIdx++] = cf.hasExpandDescription()
                    ? new StringCell(cf.getExpandDescription())
                    : DataType.getMissingCell();
            cells[colIdx++] =
                    BooleanCellFactory.create(cf.supportsConcatenation());
            cells[colIdx++] = cf.getCapabilities().stream()
                    .filter(cap -> !DEFAULT_CAPABILITIES.contains(cap))
                    .map(cap -> cap.name()).map(n -> new StringCell(n))
                    .collect(DataCellCollectors.toListCellOrMissing());
            cells[colIdx++] = cf.getAdditionalDecompressionOutputColumnSpecs()
                    .stream().map(colSpec -> colSpec.getName())
                    .map(n -> new StringCell(n))
                    .collect(DataCellCollectors.toListCellOrMissing());
            bdc.addRowToTable(new DefaultRow(key, cells));

        }
        bdc.close();
        return new BufferedDataTable[] { bdc.getTable() };
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
    protected void saveSettingsTo(NodeSettingsWO settings) {

        //
    }

    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        //
    }

    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
            throws InvalidSettingsException {

        //
    }

    @Override
    protected void reset() {

        //
    }

}
