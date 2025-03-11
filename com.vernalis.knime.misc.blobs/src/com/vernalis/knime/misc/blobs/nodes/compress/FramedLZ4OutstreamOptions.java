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
package com.vernalis.knime.misc.blobs.nodes.compress;

import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream.BlockSize;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream.Parameters;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * {@link OutputStreamWrapperOptions} for the Framed LZ4 format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class FramedLZ4OutstreamOptions implements OutputStreamWrapperOptions {

    private static final String FRAMED_LZ4_OPTIONS = "FRAMED_LZ4_OPTIONS";
    private final SettingsModelString blocksizeModel =
            new SettingsModelString("BLOCK_SIZE", BlockSize.M4.name());
    private final SettingsModelBoolean contentChecksum =
            new SettingsModelBoolean("CONTENT_CHECKSUM", true);
    private final SettingsModelBoolean blockChecksum =
            new SettingsModelBoolean("BLOCK_CHECKSUM", false);
    private final SettingsModelBoolean blockDependency =
            new SettingsModelBoolean("BLOCK_DEPENDENCY", false);

    @Override
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings =
                settings.getNodeSettings(FRAMED_LZ4_OPTIONS);
        blocksizeModel.loadSettingsFrom(mySettings);
        contentChecksum.loadSettingsFrom(mySettings);
        blockChecksum.loadSettingsFrom(mySettings);
        blockDependency.loadSettingsFrom(mySettings);

    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings =
                settings.getNodeSettings(FRAMED_LZ4_OPTIONS);
        blocksizeModel.validateSettings(mySettings);
        contentChecksum.validateSettings(mySettings);
        blockChecksum.validateSettings(mySettings);
        blockDependency.validateSettings(mySettings);

    }

    @Override
    public void saveToSettings(NodeSettingsWO settings) {

        NodeSettingsWO mySettings =
                settings.addNodeSettings(FRAMED_LZ4_OPTIONS);
        blocksizeModel.saveSettingsTo(mySettings);
        contentChecksum.saveSettingsTo(mySettings);
        blockChecksum.saveSettingsTo(mySettings);
        blockDependency.saveSettingsTo(mySettings);

    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();
        parent.add(new DialogComponentStringSelection(blocksizeModel,
                "Block Size",
                Arrays.stream(BlockSize.values()).map(BlockSize::name).toList())
                        .getComponentPanel());
        parent.add(
                new DialogComponentBoolean(contentChecksum, "Content Checksum")
                        .getComponentPanel());
        parent.add(new DialogComponentBoolean(blockChecksum, "Block Checksum")
                .getComponentPanel());
        parent.add(new DialogComponentBoolean(blockDependency,
                "Interdependent blocks").getComponentPanel());

    }

    /**
     * @return a {@link Parameters} instance representing the stored settings
     */
    Parameters getParameters() {

        return new Parameters(
                BlockSize.valueOf(blocksizeModel.getStringValue()),
                contentChecksum.getBooleanValue(),
                blockChecksum.getBooleanValue(),
                blockDependency.getBooleanValue());
    }

}
