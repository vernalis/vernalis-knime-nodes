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
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * {@link InputStreamWrapperOptions} for TAR format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class TarInstreamOptions extends AbstractTarInOutstreamOptions
        implements InputStreamWrapperOptions {

    private final SettingsModelBoolean lenient =
            new SettingsModelBoolean("LENIENT", false);

    @Override
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        lenient.loadSettingsFrom(mySettings);
    }

    @Override
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        lenient.validateSettings(mySettings);
    }

    @Override
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        lenient.saveSettingsTo(mySettings);
    }

    @Override
    public void createSettingsPane(JPanel parent, FlowVariableStackSpec flowVarsStackSpec) {

        super.createSettingsPane(parent, flowVarsStackSpec);
        parent.add(new DialogComponentBoolean(lenient, "Lenient mode")
                .getComponentPanel());

    }

    /**
     * @return is lenient parsing enabled
     */
    public final boolean isLenient() {

        return lenient.getBooleanValue();
    }

}
