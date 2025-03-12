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
 * The {@link InputStreamWrapperOptions} for the ZIP format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class ZipInstreamOptions extends JarInstreamOptions
        implements InputStreamWrapperOptions {

    private final SettingsModelBoolean useUnicodeExtraFields =
            new SettingsModelBoolean("USE_UNICODE_FIELDS", true);
    private final SettingsModelBoolean allowStoredEntriesWithDataDescriptor =
            new SettingsModelBoolean(
                    "ALLOW_STORED_ENTRIES_WITH_DATA_DESCRIPTION", false);

    /**
     * Constructor
     */
    public ZipInstreamOptions() {

        super("ZIP_OPTIONS");
    }

    /**
     * @return the use unicode extra fields setting
     */
    public final boolean isUseUnicodeExtraFields() {

        return useUnicodeExtraFields.getBooleanValue();
    }

    /**
     * @return the allow stored intries with data descriptor setting
     */
    public boolean isAllowStoredEntriesWithDataDescriptor() {

        return allowStoredEntriesWithDataDescriptor.getBooleanValue();
    }

    @Override
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.loadSubsettings(mySettings);
        useUnicodeExtraFields.loadSettingsFrom(mySettings);
        allowStoredEntriesWithDataDescriptor.loadSettingsFrom(mySettings);
    }

    @Override
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.validateSubsettings(mySettings);
        useUnicodeExtraFields.validateSettings(mySettings);
        allowStoredEntriesWithDataDescriptor.validateSettings(mySettings);
    }

    @Override
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        super.saveSubsettings(mySettings);
        useUnicodeExtraFields.saveSettingsTo(mySettings);
        allowStoredEntriesWithDataDescriptor.saveSettingsTo(mySettings);
    }

    @Override
    public void createSettingsPane(JPanel parent, FlowVariableStackSpec flowVarsStackSpec) {

        super.createSettingsPane(parent, flowVarsStackSpec);

        parent.add(new DialogComponentBoolean(useUnicodeExtraFields,
                "Use unicode extra fields").getComponentPanel());
        parent.add(
                new DialogComponentBoolean(allowStoredEntriesWithDataDescriptor,
                        "Allow stored entries with data descriptor")
                                .getComponentPanel());
    }

}
