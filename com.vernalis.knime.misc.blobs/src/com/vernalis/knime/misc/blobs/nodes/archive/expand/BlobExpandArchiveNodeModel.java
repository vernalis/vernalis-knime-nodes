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
import org.knime.core.data.DataType;
import org.knime.core.data.blob.BinaryObjectCellFactory;
import org.knime.core.data.blob.BinaryObjectDataCell;
import org.knime.core.data.blob.BinaryObjectDataValue;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;

import com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils;

/**
 * Node Model implementation for Expand Archives node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class BlobExpandArchiveNodeModel
        extends AbstractExploreArchiveNodeModel {

    private final SettingsModelLongBounded maxExpandedSizeMdl =
            smr.registerSettingsModel(
                    ExpansionSecurityUtils.createMaxExpandedSizeModel(), 2,
                    mdl -> mdl.setLongValue(-1));
    private final SettingsModelDoubleBounded maxCompressionRatioMdl =
            smr.registerSettingsModel(
                    ExpansionSecurityUtils.createMaxCompressionRatioModel(), 2,
                    mdl -> mdl.setDoubleValue(-1.0));

    /**
     * Constructor
     */
    BlobExpandArchiveNodeModel() {

        super();
        maxExpandedSizeMdl.addChangeListener(this);
        maxCompressionRatioMdl.addChangeListener(this);
        updateCanTrapExplodingExpansion();
    }

    @Override
    protected List<DataColumnSpec> createAdditionalNewColumnSpecs() {

        return Collections.singletonList(new DataColumnSpecCreator(
                blobColNameMdl.getStringValue() + " (Expanded)",
                        BinaryObjectDataCell.TYPE).createSpec());

    }

    @Override
    protected void createNewCells(DataCell[] extraCells, BinaryObjectCellFactory cellFact,
            NonClosableInputStream ncis, ArchiveEntry archiveEntry) throws IOException {


        if (ncis != null && archiveEntry != null
                && archiveEntry.getSize() != 0) {
            // Could be -1, meaning unknown size
            DataCell blobCell = cellFact.create(ncis);

            if (((BinaryObjectDataValue) blobCell).length() == 0) {
                blobCell = DataType.getMissingCell();
            }
            extraCells[0] = blobCell;
        }

    }

    @Override
    protected NonClosableInputStream wrapArchiveInputStream(
            ArchiveInputStream<?> ais) {

        return new NonClosableInputStream(ais);
    }

    @Override
    protected BinaryObjectCellFactory createBlobCellFactory(
            ExecutionContext exec) {

        return new BinaryObjectCellFactory(exec);
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

}
