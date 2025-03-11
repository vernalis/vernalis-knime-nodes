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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;

/**
 * Node Model implementation for the List Archive Contents node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class BlobListArchiveNodeModel extends AbstractExploreArchiveNodeModel {

    /**
     * Constructor
     */
    BlobListArchiveNodeModel() {

        super();
    }

    @Override
    protected List<DataColumnSpec> createAdditionalNewColumnSpecs() {

        return Collections.singletonList(
                new DataColumnSpecCreator("Entry Size", LongCell.TYPE)
                        .createSpec());

    }

    @Override
    protected void createNewCells(DataCell[] extraCells,
            BinaryObjectCellFactory cellFact, NonClosableInputStream ncis,
            ArchiveEntry archiveEntry) throws IOException {

        if (archiveEntry != null
                && archiveEntry.getSize() != ArchiveEntry.SIZE_UNKNOWN) {
            extraCells[0] = new LongCell(archiveEntry.getSize());
        }
    }

    @Override
    protected NonClosableInputStream wrapArchiveInputStream(
            ArchiveInputStream<?> ais) {

        // Not needed
        return null;
    }

    @Override
    protected BinaryObjectCellFactory createBlobCellFactory(
            ExecutionContext exec) {

        // Not needed
        return null;
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

        return null;
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

        return null;
    }

}
