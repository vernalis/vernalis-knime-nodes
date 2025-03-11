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

import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.zip.Deflater;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.knime.core.data.DataRow;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.LastModifiedDateTimeOptions;
import com.vernalis.knime.misc.blobs.nodes.LastModifiedDateTimeOptions.FixedTimeOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions.RowSpecificOutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.UserOptions;
import com.vernalis.testing.NoTest;

/**
 * A {@link RowSpecificOutputStreamWrapperOptions} implementation for the GZip
 * format
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class GZipOutstreamOption
        implements RowSpecificOutputStreamWrapperOptions {

    private final SettingsModelInteger compressionLevel =
            new SettingsModelIntegerBounded("COMPRESSION_LEVEL",
                    Deflater.DEFAULT_COMPRESSION, Deflater.DEFAULT_COMPRESSION,
                    Deflater.BEST_COMPRESSION);

    private final SettingsModelString timestampTypeMdl =
            new SettingsModelString("Timestamp Type",
                    LastModifiedDateTimeOptions.getDefault()
                            .getActionCommand());

    private LastModifiedDateTimeOptions currentTimestampOption =
            LastModifiedDateTimeOptions.getDefault();
    private final Map<LastModifiedDateTimeOptions, UserOptions> timestampOpts =
            new EnumMap<>(LastModifiedDateTimeOptions.class);
    private JPanel timestampOptionsPanel = new JPanel();


    /**
     * Constructor
     */
    public GZipOutstreamOption() {

        timestampTypeMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                updateComponentsOnTimestampTypeChange();

            }

        });
        updateComponentsOnTimestampTypeChange();
    }

    private void updateComponentsOnTimestampTypeChange() {

        currentTimestampOption = LastModifiedDateTimeOptions
                .valueOf(timestampTypeMdl.getStringValue());
        // Just in case the options is badly behaved and doesn't clear the panel
        // first..
        timestampOptionsPanel.removeAll();
        timestampOpts.computeIfAbsent(currentTimestampOption, k -> k.get())
                        .createSettingsPane(timestampOptionsPanel,
                                FlowVariableStackSpec.createFromNodeContext());

        timestampOptionsPanel.validate();
        timestampOptionsPanel.repaint();

    }

    private Component createTimestampsPanel() {

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Archive Entry Last Modified Time Options"));

        outerPanel.add(new DialogComponentButtonGroup(timestampTypeMdl, null,
                false,
                Arrays.stream(LastModifiedDateTimeOptions.values()).filter(
                        opt -> opt != LastModifiedDateTimeOptions.Column)
                        .toArray(LastModifiedDateTimeOptions[]::new))
                                .getComponentPanel());
        timestampOptionsPanel = new JPanel();
        timestampOptionsPanel.setPreferredSize(new Dimension(600, 50));
        outerPanel.add(timestampOptionsPanel);

        return outerPanel;
    }



    @Override
    public void loadFromSettings(NodeSettingsRO settings,
            PortObjectSpec[] portSpecs, FlowVariableStackSpec flowVarsSpec)
            throws InvalidSettingsException, NotConfigurableException {

        NodeSettingsRO mySettings = settings.getNodeSettings("GZIP_OPTIONS");
        compressionLevel.loadSettingsFrom(mySettings);

        try {
            timestampTypeMdl.loadSettingsFrom(mySettings);
            currentTimestampOption = LastModifiedDateTimeOptions
                    .valueOf(timestampTypeMdl.getStringValue());
            timestampOpts.computeIfAbsent(currentTimestampOption, k -> k.get())
                    .loadFromSettings(mySettings, portSpecs, flowVarsSpec);
        } catch (InvalidSettingsException | IllegalArgumentException
                | NullPointerException e) {
            // WE ignore them here and load the panel
        }
    }

    @Override
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings("GZIP_OPTIONS");
        compressionLevel.loadSettingsFrom(mySettings);

        try {
            timestampTypeMdl.loadSettingsFrom(mySettings);
            currentTimestampOption = LastModifiedDateTimeOptions
                    .valueOf(timestampTypeMdl.getStringValue());
            timestampOpts.computeIfAbsent(currentTimestampOption, k -> k.get())
                    .loadFromSettings(mySettings);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new InvalidSettingsException(e);
        }
    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        NodeSettingsRO mySettings = settings.getNodeSettings("GZIP_OPTIONS");
        compressionLevel.validateSettings(mySettings);

        try {
            LastModifiedDateTimeOptions lmdto = LastModifiedDateTimeOptions
                    .valueOf(((SettingsModelString) timestampTypeMdl
                            .createCloneWithValidatedValue(mySettings))
                                    .getStringValue());
            lmdto.get().validateSettings(mySettings);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException("Unknown timestamp type", e);
        }
    }

    @Override
    public void saveToSettings(NodeSettingsWO settings) {

        NodeSettingsWO mySettings = settings.addNodeSettings("GZIP_OPTIONS");
        compressionLevel.saveSettingsTo(mySettings);
        timestampTypeMdl.saveSettingsTo(mySettings);
        timestampOpts.get(currentTimestampOption).saveToSettings(mySettings);

    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();
        parent.add(new DialogComponentNumber(compressionLevel,
                "Compression level", 1).getComponentPanel());
        parent.add(createTimestampsPanel());

    }

    @Override
    public String validateInConfigure(PortObjectSpec[] specs)
            throws InvalidSettingsException {

        String retVal = timestampOpts.get(currentTimestampOption)
                .validateInConfigure(specs);

        if (retVal == null) {
            return RowSpecificOutputStreamWrapperOptions.super.validateInConfigure(
                    specs);
        }
        return retVal;
    }

    /**
     * @return a {@link GzipParameters} object representing the currently stored
     *             settings
     */
    GzipParameters getParameters() {

        GzipParameters retVal = new GzipParameters();
        retVal.setCompressionLevel(compressionLevel.getIntValue());

        if (currentTimestampOption != LastModifiedDateTimeOptions.None) {
            retVal.setModificationTime(
                    getModificationTime());
        }
        return retVal;
    }

    private long getModificationTime() {

        UserOptions currentTimeStampOption =
                timestampOpts.get(currentTimestampOption);
        Date lm = switch (currentTimestampOption) {
            case Fixed_Time ->
                    ((FixedTimeOptions) currentTimeStampOption).getDate();
            case Runtime -> new Date();
            case None -> null;
            default -> throw new IllegalArgumentException(
                    "Unexpected value: " + currentTimeStampOption);
        };
        return lm == null ? 0L : lm.getTime();
    }

    @Override
    public OutputStreamWrapperOptions getOptionsForRow(DataRow row) {

        // For now we just return ourself, which is totally not row-specific1
        // FIXME: Implement properly when we figure how to implement Timestamp,
        // filename, comment...
        return this;
    }

}
