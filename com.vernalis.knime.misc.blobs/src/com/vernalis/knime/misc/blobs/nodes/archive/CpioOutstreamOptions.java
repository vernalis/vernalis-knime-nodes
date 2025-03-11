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
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * The {@link OutputStreamWrapperOptions} implementation for the CPIO format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class CpioOutstreamOptions extends CpioInstreamOptions
        implements OutputStreamWrapperOptions {

    private enum CpioFormats implements ButtonGroupEnumInterface {

        New(CpioConstants.FORMAT_NEW),
        New_CRC(CpioConstants.FORMAT_NEW_CRC),
        Binary(CpioConstants.FORMAT_OLD_BINARY),
        ASCII(CpioConstants.FORMAT_OLD_ASCII);

        private final short flag;

        private CpioFormats(short flag) {

            this.flag = flag;
        }

        @Override
        public String getText() {

            return name().replace('_', ' ');
        }

        @Override
        public String getActionCommand() {

            return name();
        }

        @Override
        public String getToolTip() {

            return null;
        }

        @Override
        public boolean isDefault() {

            return this == getDefault();
        }

        static CpioFormats getDefault() {

            return New;
        }

    }

    private final SettingsModelString format = new SettingsModelString("FORMAT",
            CpioFormats.getDefault().getActionCommand());

    /**
     * Constructor
     */
    public CpioOutstreamOptions() {

        super();
    }

    /**
     * @return the flag for the format
     */
    public final short getFormatFlag() {

        return CpioFormats.valueOf(format.getStringValue()).flag;
    }

    @Override
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.loadSubsettings(mySettings);
        format.loadSettingsFrom(mySettings);
    }

    @Override
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.validateSubsettings(mySettings);
        format.validateSettings(mySettings);
    }

    @Override
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        super.saveSubsettings(mySettings);
        format.saveSettingsTo(mySettings);
    }

    @Override
    public void createSettingsPane(JPanel parent, FlowVariableStackSpec flowVarsStackSpec) {

        super.createSettingsPane(parent, flowVarsStackSpec);
        parent.add(new DialogComponentButtonGroup(format, "Format", false,
                CpioFormats.values()).getComponentPanel());
    }

}
