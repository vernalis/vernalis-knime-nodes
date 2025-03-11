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

import javax.swing.JPanel;

import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.commons.compress.utils.CharsetNames;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * {@link InputStreamWrapperOptions} for the CPIO archive format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class CpioInstreamOptions extends ArjInstreamOptions
        implements InputStreamWrapperOptions {

    private final SettingsModelInteger blockSize =
            new SettingsModelIntegerBounded("BLOCK_SIZE",
                    CpioConstants.BLOCK_SIZE, 64, 65535);

    /**
     * Constructor
     */
    public CpioInstreamOptions() {

        super("CPIO_OPTIONS", CharsetNames.US_ASCII);
    }

    @Override
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.loadSubsettings(mySettings);
        blockSize.loadSettingsFrom(mySettings);
    }

    @Override
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.validateSubsettings(mySettings);
        blockSize.validateSettings(mySettings);
    }

    @Override
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        super.saveSubsettings(mySettings);
        blockSize.saveSettingsTo(mySettings);
    }

    @Override
    public void createSettingsPane(JPanel parent, FlowVariableStackSpec flowVarsStackSpec) {

        super.createSettingsPane(parent, flowVarsStackSpec);
        parent.add(new DialogComponentNumber(blockSize, "Block size",
                CpioConstants.BLOCK_SIZE).getComponentPanel());
    }

    /**
     * @return the stored blocksize
     */
    public final int getBlockSize() {

        return blockSize.getIntValue();
    }
}
