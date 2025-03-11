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
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * {@link InputStreamWrapperOptions} for the 'arj' archiving format. Also serves
 * as a base class for a number of other formats which require a character set
 * encoding
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class ArjInstreamOptions implements InputStreamWrapperOptions {

    private static final String SYSTEM_DEFAULT = "<-- System Default -->";
    private final SettingsModelString encoding;

    private final String rootKey;

    /**
     * Constructor for 'arj' instance
     */
    public ArjInstreamOptions() {

        this("ARJ_OPTIONS", SYSTEM_DEFAULT);
    }

    /**
     * Constructor for subclasses
     * 
     * @param key
     *            The root settings key
     * @param defaultCharsetName
     *            the default charset name, {@code null} for the system default
     */
    protected ArjInstreamOptions(final String key, String defaultCharsetName) {

        rootKey = Objects.requireNonNull(key);
        encoding = new SettingsModelString("ENCODING",
                defaultCharsetName == null ? SYSTEM_DEFAULT
                        : defaultCharsetName);
    }

    /**
     * @return the key of the root settings object
     */
    protected final String getRootSettingsKey() {

        return rootKey;
    }

    @Override
    public final void loadFromSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        final NodeSettingsRO mySettings =
                settings.getNodeSettings(getRootSettingsKey());
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
    protected void loadSubsettings(final NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        // hook
    }

    @Override
    public final void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {

        final NodeSettingsRO mySettings =
                settings.getNodeSettings(getRootSettingsKey());
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
    protected void validateSubsettings(final NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        // hook
    }

    @Override
    public final void saveToSettings(final NodeSettingsWO settings) {

        final NodeSettingsWO mySettings =
                settings.addNodeSettings(getRootSettingsKey());
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
    protected void saveSubsettings(final NodeSettingsWO mySettings) {

        // hook
    }

    @Override
    public void createSettingsPane(final JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();

        parent.add(new DialogComponentStringSelection(encoding,
                "Filename Encoding",
                Stream.concat(Stream.of(SYSTEM_DEFAULT),
                        Charset.availableCharsets().keySet().stream()).toList())
                                .getComponentPanel());

    }

    /**
     * @return the selected charset encoding name, or {@code null} for the
     *             system default
     */
    public final String getEncoding() {

        return SYSTEM_DEFAULT.equals(encoding.getStringValue()) ? null
                : encoding.getStringValue();

    }

}
