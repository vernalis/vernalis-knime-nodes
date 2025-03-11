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
package com.vernalis.knime.misc.blobs.nodes.archive;

import java.nio.charset.Charset;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.apache.commons.compress.archivers.tar.TarConstants;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;

/**
 * Base implementation for TAR format In and Out stream options
 * 
 * @author S.Roughley knime@vernalis.com
 */
abstract class AbstractTarInOutstreamOptions
        implements OutputStreamWrapperOptions {

    private static final String CFG_SETTINGS_KEY = "TAR_OPTIONS";
    private static final String SYSTEM_DEFAULT = "<-- System Default -->";
    private final SettingsModelInteger blockSize =
            new SettingsModelIntegerBounded("BLOCK_SIZE",
                    TarConstants.DEFAULT_BLKSIZE, 16, 65535);
    private final SettingsModelString encoding =
            new SettingsModelString("ENCODING", SYSTEM_DEFAULT);


    @Override
    public final void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings(CFG_SETTINGS_KEY);
        blockSize.loadSettingsFrom(mySettings);
        encoding.loadSettingsFrom(mySettings);
        loadSubsettings(mySettings);

    }

    /**
     * Method hook for subclasses to load further settings from the submodel
     * 
     * @param mySettings
     *            the submodel
     * @throws InvalidSettingsException
     *             if there is an error loading from the submodel
     */
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        // hook
    }

    @Override
    public final void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings(CFG_SETTINGS_KEY);
        blockSize.validateSettings(mySettings);
        encoding.validateSettings(mySettings);
        validateSubsettings(mySettings);

    }

    /**
     * Method hook for subclasses to validate further settings from the submodel
     * 
     * @param mySettings
     *            the submodel
     * @throws InvalidSettingsException
     *             if there is an error validating from the submodel
     */
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        // hook
    }

    @Override
    public final void saveToSettings(NodeSettingsWO settings) {

        NodeSettingsWO mySettings = settings.addNodeSettings(CFG_SETTINGS_KEY);
        blockSize.saveSettingsTo(mySettings);
        encoding.saveSettingsTo(mySettings);
        saveSubsettings(mySettings);

    }

    /**
     * Method hook for subclasses to save further settings to the submodel
     * 
     * @param mySettings
     *            the submodel
     *            if there is an error loading from the submodel
     */
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        // hook
    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();

        parent.add(new DialogComponentNumber(blockSize, "Block Size", 512)
                .getComponentPanel());
        parent.add(new DialogComponentStringSelection(encoding,
                "Filename Encoding",
                Stream.concat(Stream.of(new String[] { SYSTEM_DEFAULT }),
                        Charset.availableCharsets().keySet().stream()).toList())
                                .getComponentPanel());


    }

    /**
     * @return the blocksize to use
     */
    public final int getBlockSize() {

        return blockSize.getIntValue();

    }

    /**
     * @return the encoding to use for filenames
     */
    public final String getEncoding() {

        return SYSTEM_DEFAULT.equals(encoding.getStringValue()) ? null
                : encoding.getStringValue();

    }

}
