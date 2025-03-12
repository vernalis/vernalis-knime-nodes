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

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * The {@link OutputStreamWrapperOptions} implementation for the XZ format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class XZOutstreamOptions implements OutputStreamWrapperOptions {

    private static final String XZ_OPTIONS = "XZ_OPTIONS";
    private final SettingsModelInteger preset =
            new SettingsModelIntegerBounded("PRESET", 6, 0, 9);

    @Override
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings(XZ_OPTIONS);
        preset.loadSettingsFrom(mySettings);

    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings(XZ_OPTIONS);
        preset.validateSettings(mySettings);

    }

    @Override
    public void saveToSettings(NodeSettingsWO settings) {

        NodeSettingsWO mySettings = settings.addNodeSettings(XZ_OPTIONS);
        preset.saveSettingsTo(mySettings);

    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();
        parent.add(new DialogComponentNumber(preset, "Preset", 1)
                .getComponentPanel());

    }

    /**
     * @return the 'preset' value stored
     */
    int getPreset() {

        return preset.getIntValue();
    }

}
