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
package com.vernalis.knime.misc.blobs.nodes.archive.create;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.VariableType;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.BlobConstants;
import com.vernalis.knime.misc.blobs.nodes.LastModifiedDateTimeOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.UserOptions;
import com.vernalis.knime.misc.blobs.nodes.archive.ArchiveFormat;

/**
 * Node Dialog implementation for the List Archive and Extract Archive nodes
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class CreateArchiveNodeDialog extends NodeDialogPane {

    private static final String ARCHIVE_PATH_COLUMN = "Archive Path Column";
    private static final String BINARY_OBJECTS_COLUMN = "Binary Objects Column";

    // Settings models for load/save
    // Model for the name of the archive format
    private final SettingsModelString archiveFormatMdl;

    private final SettingsModelString timestampTypeMdl;
    // The dropdown for the Blob column - needed so we can access it in
    // loadSettingsFrom()
    private DialogComponentColumnNameSelection blobColNameDiac;
    // The dropdown for the archive path column - needed so we can access it in
    // loadSettingsFrom()
    private DialogComponentColumnNameSelection pathColNameDiac;

    // DIsaplyed components/panels
    private JButton restoreDefaultsButton;
    private JPanel archiveFormatOptionsPanel;
    private JPanel timestampOptionsPanel;

    private ArchiveFormat currentArchiveFormat = ArchiveFormat.getDefault();
    private final Map<ArchiveFormat, OutputStreamWrapperOptions> archiveFormatOpts =
            new EnumMap<>(ArchiveFormat.class);

    private LastModifiedDateTimeOptions currentTimestampOption =
            LastModifiedDateTimeOptions.getDefault();
    private final Map<LastModifiedDateTimeOptions, UserOptions> timestampOpts =
            new EnumMap<>(LastModifiedDateTimeOptions.class);

    private PortObjectSpec[] lastSpecs;

    /**
     * Constructor
     */
    CreateArchiveNodeDialog() {

        super();

        // Start by initialising the required settings models
        archiveFormatMdl = createArchiveFormatMdl();
        archiveFormatMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                updateComponentsOnArchiveFormatChange();
            }

        });

        timestampTypeMdl = createTimestampTypeModel();
        timestampTypeMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                updateComponentsOnTimestampTypeChange();

            }

        });

        // The base panel in which all components in the tab are placed
        Box basePanel = new Box(BoxLayout.Y_AXIS);

        // Add a panel for the Column selector
        basePanel.add(createBinaryObjectsColumnPanel());

        basePanel.add(createTimestampsPanel());

        // Create a panel for the options for the archive format with an etched
        // border
        JPanel archiveOptionsPanel = createArchiveOptionsPanel();
        basePanel.add(archiveOptionsPanel);

        addTab("Options", basePanel);
        updateComponentsOnArchiveFormatChange();
        updateComponentsOnTimestampTypeChange();

    }

    private Component createTimestampsPanel() {

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Archive Entry Last Modified Time Options"));

        outerPanel.add(new DialogComponentButtonGroup(timestampTypeMdl, null,
                false, LastModifiedDateTimeOptions.values())
                        .getComponentPanel());
        timestampOptionsPanel = new JPanel();
        timestampOptionsPanel.setPreferredSize(new Dimension(600, 50));
        outerPanel.add(timestampOptionsPanel);

        return outerPanel;
    }

    private JPanel createArchiveOptionsPanel() {

        JPanel archiveOptionsPanel = new JPanel();
        archiveOptionsPanel.setLayout(new BorderLayout(5, 5));
        archiveOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Archive Options"));

        // Add the archive format panel, containing a format dropdown and keep
        // directories checkbox, across the top of the 'Archive Options' panel
        JPanel archiveFmtPanel = createArchiveFormatPanel();
        archiveOptionsPanel.add(archiveFmtPanel, BorderLayout.NORTH);

        // Create an inner panel for the options for the selected format - at
        // this stage we dont populate it
        archiveFormatOptionsPanel = new JPanel();
        archiveFormatOptionsPanel.setPreferredSize(new Dimension(600, 300));
        archiveOptionsPanel.add(archiveFormatOptionsPanel);

        // Create a button for restore defaults
        JPanel btnBox = createRestoreDefaultsButton();
        archiveOptionsPanel.add(btnBox, BorderLayout.SOUTH);
        return archiveOptionsPanel;
    }

    private JPanel createRestoreDefaultsButton() {

        JPanel btnBox = new JPanel();
        restoreDefaultsButton = new JButton("Restore Defaults");
        restoreDefaultsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Replace the settings for the current format with a new
                // instance
                archiveFormatOpts.put(currentArchiveFormat,
                        applyLatestPortSpecsToNewUserOptions(
                                currentArchiveFormat
                                        .createOutputStreamOptions()));

                updateArchiveFormatOptionsPanel();

            }

        });
        btnBox.add(restoreDefaultsButton);
        return btnBox;
    }

    private JPanel createArchiveFormatPanel() {

        JPanel archiveFmtPanel = new JPanel(new GridLayout(1, 2));
        archiveFmtPanel.add(new DialogComponentStringSelection(archiveFormatMdl,
                "Archive Format",
                Arrays.stream(ArchiveFormat.getArchiveFormats())
                        .map(ArchiveFormat::getText).toList(),
                        false, createFlowVariableModel(archiveFormatMdl.getKey(),
                                VariableType.StringType.INSTANCE))
                                .getComponentPanel());

        return archiveFmtPanel;
    }

    private JPanel createBinaryObjectsColumnPanel() {

        // We have 2 rows, each with a selector
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Column Options"));

        // Row 0 - the Binary Objects column
        blobColNameDiac =
                new DialogComponentColumnNameSelection(createBlobColNameMdl(),
                        BINARY_OBJECTS_COLUMN, 0, BlobConstants.BLOB_COLUMN_FILTER);
        outerPanel.add(blobColNameDiac.getComponentPanel());

        // Row 1 - the Archive Path column
        pathColNameDiac =
                new DialogComponentColumnNameSelection(createPathColNameMdl(),
                        ARCHIVE_PATH_COLUMN, 0, BlobConstants.PATH_COLUMN_FILTER);
        outerPanel.add(pathColNameDiac.getComponentPanel());

        return outerPanel;
    }

    /**
     * Method to ensure the archiveFormatOptionsPanel matches the current
     * settings
     */
    private void updateArchiveFormatOptionsPanel() {

        // Just in case the options is badly behaved and doesn't clear the panel
        // first..
        archiveFormatOptionsPanel.removeAll();
        applyLatestPortSpecsToNewUserOptions(archiveFormatOpts.computeIfAbsent(
                currentArchiveFormat, k -> k.createOutputStreamOptions()))
                        .createSettingsPane(archiveFormatOptionsPanel,
                                FlowVariableStackSpec
                                        .createFromNodeDialog(this));

        restoreDefaultsButton
                .setEnabled(archiveFormatOptionsPanel.getComponentCount() > 0);

        archiveFormatOptionsPanel.validate();
        archiveFormatOptionsPanel.repaint();
    }

    private void updateComponentsOnArchiveFormatChange() {

        currentArchiveFormat =
                ArchiveFormat.valueOf(
                        archiveFormatMdl.getStringValue().replace(' ', '_'));

        updateArchiveFormatOptionsPanel();

    }

    private void updateComponentsOnTimestampTypeChange() {

        currentTimestampOption =
                LastModifiedDateTimeOptions.valueOf(timestampTypeMdl.getStringValue());
        // Just in case the options is badly behaved and doesn't clear the panel
        // first..
        timestampOptionsPanel.removeAll();
        // Force any components to acquired the latest specs
        applyLatestPortSpecsToNewUserOptions(timestampOpts.computeIfAbsent(
                currentTimestampOption, k -> k.get())).createSettingsPane(
                        timestampOptionsPanel,
                        FlowVariableStackSpec.createFromNodeDialog(this));

        timestampOptionsPanel.validate();
        timestampOptionsPanel.repaint();

    }

    private <T extends UserOptions> T applyLatestPortSpecsToNewUserOptions(
            T userOption) {

        if (lastSpecs != null) {
            NodeSettings tmp = new NodeSettings("temp");
            userOption.saveToSettings(tmp);

            try {
                userOption.loadFromSettings(tmp, lastSpecs,
                        FlowVariableStackSpec.createFromNodeDialog(this));
            } catch (InvalidSettingsException | NotConfigurableException e) {
                // Ignore for now - really should be an issue!
            }
        }
        return userOption;
    }

    /**
     * @return settings model for the 'Timestamp type' option
     */
    static final SettingsModelString createTimestampTypeModel() {

        return new SettingsModelString("Timestamp Type",
                LastModifiedDateTimeOptions.getDefault().getActionCommand());

    }

    /**
     * @return settings model for the 'Path Column' option
     */
    static final SettingsModelColumnName createPathColNameMdl() {

        final SettingsModelColumnName retVal =
                new SettingsModelColumnName(ARCHIVE_PATH_COLUMN, null);
        retVal.setSelection(null, true);
        return retVal;
    }

    /**
     * @return settings model for the 'Archive Format' option
     */
    static final SettingsModelString createArchiveFormatMdl() {

        return new SettingsModelString("Archive format",
                ArchiveFormat.getDefault().getActionCommand());
    }

    /**
     * @return settings model for the 'Binary Objects Column' option
     */
    static final SettingsModelString createBlobColNameMdl() {

        return new SettingsModelString(BINARY_OBJECTS_COLUMN, null);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException {

        blobColNameDiac.saveSettingsTo(settings);
        pathColNameDiac.saveSettingsTo(settings);
        archiveFormatMdl.saveSettingsTo(settings);
        archiveFormatOpts
                .computeIfAbsent(currentArchiveFormat,
                        k -> k.createOutputStreamOptions())
                .saveToSettings(settings);
        timestampTypeMdl.saveSettingsTo(settings);
        timestampOpts.computeIfAbsent(currentTimestampOption, l -> l.get())
                .saveToSettings(settings);

    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings,
            DataTableSpec[] specs) throws NotConfigurableException {

        lastSpecs = Arrays.copyOf(specs, specs.length);

        try {
            // We need this to load in the values for the column selection
            blobColNameDiac.loadSettingsFrom(settings, specs);
            pathColNameDiac.loadSettingsFrom(settings, specs);
            archiveFormatMdl.loadSettingsFrom(settings);
            currentArchiveFormat =
                    ArchiveFormat.valueOf(archiveFormatMdl.getStringValue()
                            .replace(' ', '_'));
            archiveFormatOpts
                    .computeIfAbsent(currentArchiveFormat,
                            k -> k.createOutputStreamOptions())
                    .loadFromSettings(settings, specs,
                            FlowVariableStackSpec.createFromNodeDialog(this));

            timestampTypeMdl.loadSettingsFrom(settings);
            currentTimestampOption = LastModifiedDateTimeOptions
                    .valueOf(timestampTypeMdl.getStringValue());
            timestampOpts.computeIfAbsent(currentTimestampOption, k -> k.get())
                    .loadFromSettings(settings, specs,
                            FlowVariableStackSpec.createFromNodeDialog(this));

        } catch (InvalidSettingsException | IllegalArgumentException
                | NullPointerException e) {
            // WE ignore them here and load the panel
        }
    }

}
