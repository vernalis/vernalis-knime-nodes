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
package com.vernalis.knime.misc.blobs.nodes.archive.formats;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
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

import com.vernalis.knime.misc.blobs.nodes.archive.ArchiveFormat;

/**
 * {@link NodeModel} implementation for the List Archive Formats node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class ListArchiveFormatsNodeModel extends NodeModel {

    private static final DataTableSpec OUT_SPEC =
            new DataTableSpecCreator()
                    .addColumns(
                            new DataColumnSpecCreator("Format", StringCell.TYPE)
                                    .createSpec(),
                            new DataColumnSpecCreator("Description",
                                    StringCell.TYPE).createSpec(),
                            new DataColumnSpecCreator("Supports Archiving",
                                    BooleanCellFactory.TYPE).createSpec(),
                            new DataColumnSpecCreator("Supports Directories",
                                    BooleanCellFactory.TYPE).createSpec(),
                            new DataColumnSpecCreator("Includes Compression",
                                    BooleanCellFactory.TYPE).createSpec())
                    .createSpec();

    /**
     * Constructor
     */
    ListArchiveFormatsNodeModel() {

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
        final ArchiveFormat[] formats = ArchiveFormat.values();
        double progPerRow = 1.0 / formats.length;

        for (ArchiveFormat af : formats) {
            exec.checkCanceled();
            exec.setProgress(af.ordinal() * progPerRow,
                    String.format("Processed %d of %d formats", af.ordinal(),
                            formats.length));
            RowKey key = RowKey.createRowKey((long) af.ordinal());
            DataCell[] cells = new DataCell[OUT_SPEC.getNumColumns()];
            int colIdx = 0;
            cells[colIdx++] = new StringCell(af.getText());
            cells[colIdx++] =
                    af.getDescription() == null ? DataType.getMissingCell()
                            : new StringCell(af.getDescription());
            cells[colIdx++] = BooleanCellFactory.create(af.supportsArchiving());
            cells[colIdx++] =
                    af == ArchiveFormat.guess ? DataType.getMissingCell() :
                    BooleanCellFactory.create(af.supportsDirectories());
            cells[colIdx++] = af == ArchiveFormat.guess
                    ? DataType.getMissingCell()
                    :
                    BooleanCellFactory.create(af.includesCompression());
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
