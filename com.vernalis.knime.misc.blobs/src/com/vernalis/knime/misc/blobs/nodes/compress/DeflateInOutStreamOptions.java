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

import java.util.zip.Deflater;

import javax.swing.JPanel;

import org.apache.commons.compress.compressors.deflate.DeflateParameters;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * {@link InputStreamWrapperOptions} and {@link OutputStreamWrapperOptions}
 * implementation for the 'Deflate' format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class DeflateInOutStreamOptions
        implements InputStreamWrapperOptions, OutputStreamWrapperOptions {

    private final SettingsModelBoolean zlibHeader =
            new SettingsModelBoolean("zlib_header", true);
    private final SettingsModelInteger compressionLevel =
            new SettingsModelIntegerBounded("COMPRESSION_LEVEL",
                    Deflater.DEFAULT_COMPRESSION, Deflater.DEFAULT_COMPRESSION,
                    Deflater.BEST_COMPRESSION);

    @Override
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings("DEFLATE_OPTIONS");
        zlibHeader.loadSettingsFrom(mySettings);
        compressionLevel.loadSettingsFrom(mySettings);
    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings("DEFLATE_OPTIONS");
        zlibHeader.validateSettings(mySettings);
        compressionLevel.validateSettings(mySettings);

    }

    @Override
    public void saveToSettings(NodeSettingsWO settings) {

        NodeSettingsWO mySettings = settings.addNodeSettings("DEFLATE_OPTIONS");
        zlibHeader.saveSettingsTo(mySettings);
        compressionLevel.saveSettingsTo(mySettings);
    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();
        parent.add(new DialogComponentBoolean(zlibHeader, "zlip Header")
                .getComponentPanel());
        parent.add(new DialogComponentNumber(compressionLevel,
                "Compression Level", 1).getComponentPanel());

    }

    /**
     * @return the {@link DeflateParameters} representing the current stored
     *             settings
     */
    DeflateParameters getParameters() {

        DeflateParameters retVal = new DeflateParameters();
        retVal.setWithZlibHeader(zlibHeader.getBooleanValue());
        retVal.setCompressionLevel(compressionLevel.getIntValue());

        return retVal;
    }

}
