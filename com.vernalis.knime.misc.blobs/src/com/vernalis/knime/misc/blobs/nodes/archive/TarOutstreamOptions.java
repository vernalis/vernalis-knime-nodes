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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * The {@link OutputStreamWrapperOptions} for the TAR format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class TarOutstreamOptions extends AbstractTarInOutstreamOptions
        implements OutputStreamWrapperOptions {

    private final SettingsModelString longFilenameMode =
            new SettingsModelString("LONG_FILENAMES_MODE",
                    TarLongFileMode.getDefault().getActionCommand());
    private final SettingsModelString bigNumbersMode =
            new SettingsModelString("BIG_NUMBERS_MODE",
                    TarBigNumericValues.getDefault().getActionCommand());

    @Override
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.loadSubsettings(mySettings);
        longFilenameMode.loadSettingsFrom(mySettings);
        bigNumbersMode.loadSettingsFrom(mySettings);

    }

    @Override
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.validateSubsettings(mySettings);
        longFilenameMode.validateSettings(mySettings);
        bigNumbersMode.validateSettings(mySettings);

    }

    @Override
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        super.saveSubsettings(mySettings);
        longFilenameMode.saveSettingsTo(mySettings);
        bigNumbersMode.saveSettingsTo(mySettings);

    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        super.createSettingsPane(parent, flowVarsStackSpec);

        parent.add(new DialogComponentButtonGroup(longFilenameMode,
                "Long Filename Mode", false, TarLongFileMode.values())
                        .getComponentPanel());
        parent.add(new DialogComponentButtonGroup(bigNumbersMode,
                "Big Numbers Mode", false, TarBigNumericValues.values())
                        .getComponentPanel());

    }

    /**
     * @return the flag for the selected big numbers mode
     */
    public int getBigNumberMode() {

        return TarBigNumericValues.valueOf(bigNumbersMode.getStringValue())
                .getFlag();
    }

    /**
     * @return the flag for the long filenames mode
     */
    public int getLongFileMode() {

        return TarLongFileMode.valueOf(longFilenameMode.getStringValue())
                .getFlag();
    }

}
