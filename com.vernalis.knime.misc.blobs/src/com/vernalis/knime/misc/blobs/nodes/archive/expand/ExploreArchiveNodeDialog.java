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
package com.vernalis.knime.misc.blobs.nodes.archive.expand;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createFailOnExpansionExplosionModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createMaxCompressionRatioModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createMaxExpandedEntriesModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createMaxExpandedSizeModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createSecurityOptionsPanel;

import java.awt.BorderLayout;
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
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.VariableType;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer;
import com.vernalis.knime.misc.blobs.nodes.UserOptions;
import com.vernalis.knime.misc.blobs.nodes.archive.ArchiveFormat;

/**
 * Node Dialog implementation for the List Archive and Extract Archive nodes
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class ExploreArchiveNodeDialog extends NodeDialogPane
        implements SecurityOptionsContainer {

    private static final String CASE_SENSITIVE = "Case Sensitive";
    private static final String FILTER_PATHS = "Filter Paths";
    private static final String KEEP_DIRECTORIES = "Keep Directories";
    private static final String REMOVE_INPUT_COLUMN = "Remove input column";
    private static final String BINARY_OBJECTS_COLUMN = "Binary Objects Column";



    // Settings models for load/save
    // Model for the 'Remove input column' option
    private final SettingsModelBoolean removeInputColumnMdl;
    // Model for the name of the archive format
    private final SettingsModelString archiveFormatMdl;
    private final SettingsModelBoolean keepDirectoriesMdl;
    private final SettingsModelBoolean filterPathsMdl;
    private final SettingsModelString patternMdl;
    private final SettingsModelString patternTypeMdl;
    private final SettingsModelBoolean caseSensitiveMdl;

    private final SettingsModelLongBounded maxExpandedSizeMdl;
    private final SettingsModelDoubleBounded maxCompressionRatioMdl;

    private final SettingsModelIntegerBounded maxExpandedEntriesMdl;
    private final SettingsModelBoolean failOnExpansionExplosionMdl;

    // DIsaplyed components/panels
    private JButton restoreDefaultsButton;
    private JPanel archiveFormatOptionsPanel;
    // The dropdown for the Blob column - needed so we can access it in
    // loadSettingsFrom()
    private DialogComponentColumnNameSelection colNameDiac;

    private ArchiveFormat currentArchiveFormat = ArchiveFormat.getDefault();
    private final Map<ArchiveFormat, InputStreamWrapperOptions> archiveFormatOpts =
            new EnumMap<>(ArchiveFormat.class);

    private PortObjectSpec[] lastSpecs;

    /**
     * Constructor
     * 
     * @param includeExpansionSecurityOptions
     *            TODO
     */
    ExploreArchiveNodeDialog(boolean includeExpansionSecurityOptions) {

        super();

        // Start by initialising the required settings models
        archiveFormatMdl = createArchiveFormatMdl();
        archiveFormatMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                updateComponentsOnArchiveFormatChange();
            }

        });
        keepDirectoriesMdl = createKeepDirectoriesModel();
        removeInputColumnMdl = createRemoveInputColumnModel();

        filterPathsMdl = createFilterPathsModel();
        patternMdl = createPatternModel();
        patternTypeMdl = createPatternTypeModel();
        caseSensitiveMdl = createCaseSensitiveModel();
        filterPathsMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                patternMdl.setEnabled(filterPathsMdl.getBooleanValue());
                patternTypeMdl.setEnabled(filterPathsMdl.getBooleanValue());
                caseSensitiveMdl.setEnabled(filterPathsMdl.getBooleanValue());
            }

        });
        patternMdl.setEnabled(filterPathsMdl.getBooleanValue());
        patternTypeMdl.setEnabled(filterPathsMdl.getBooleanValue());
        caseSensitiveMdl.setEnabled(filterPathsMdl.getBooleanValue());

        // Security models
        if (includeExpansionSecurityOptions) {
            maxExpandedSizeMdl = createMaxExpandedSizeModel();
            maxExpandedSizeMdl.addChangeListener(this);
            maxCompressionRatioMdl = createMaxCompressionRatioModel();
            maxCompressionRatioMdl.addChangeListener(this);
        } else {
            maxExpandedSizeMdl = null;
            maxCompressionRatioMdl = null;
        }
        maxExpandedEntriesMdl = createMaxExpandedEntriesModel();
        if (maxExpandedEntriesMdl != null) {
            maxExpandedEntriesMdl.addChangeListener(this);
        }
        failOnExpansionExplosionMdl = createFailOnExpansionExplosionModel();

        // The base panel in which all components in the tab are placed
        Box basePanel = new Box(BoxLayout.Y_AXIS);

        // Add a panel for the Column selector
        basePanel.add(createBinaryObjectsColumnPanel());

        // Create a panel for the options for the archive format with an etched
        // border
        JPanel archiveOptionsPanel = createArchiveOptionsPanel();
        basePanel.add(archiveOptionsPanel);

        // Finally, a panel for optional path filtering
        JPanel pathFilterOptionsPanel = createPathFilterOptionsPanel();
        basePanel.add(pathFilterOptionsPanel);

        // And finally, the 'Security Options' panel
        JPanel securityOptionsPanel = createSecurityOptionsPanel(this);
        basePanel.add(securityOptionsPanel);

        addTab("Options", basePanel);
        updateComponentsOnArchiveFormatChange();
        updateCanTrapExplodingExpansion();
    }

    private JPanel createPathFilterOptionsPanel() {

        JPanel pathFilterOptionsPanel = new JPanel();
        pathFilterOptionsPanel.setLayout(
                new BoxLayout(pathFilterOptionsPanel, BoxLayout.Y_AXIS));

        pathFilterOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Path Filter Options"));
        JPanel row0 = new JPanel(new GridLayout(1, 3));
        row0.add(new DialogComponentBoolean(filterPathsMdl, FILTER_PATHS)
                .getComponentPanel());
        row0.add(new DialogComponentButtonGroup(patternTypeMdl, null, false,
                PathFilterType.values()).getComponentPanel());
        row0.add(new DialogComponentBoolean(caseSensitiveMdl, CASE_SENSITIVE)
                .getComponentPanel());
        pathFilterOptionsPanel.add(row0);
        pathFilterOptionsPanel
                .add(new DialogComponentString(patternMdl, "Pattern", false, 60)
                        .getComponentPanel());
        return pathFilterOptionsPanel;
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
        archiveFormatOptionsPanel.setPreferredSize(new Dimension(600, 200));
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
                                        .createInputStreamOptions()));

                if (currentArchiveFormat.supportsDirectories()) {
                    // By default we keep directories
                    keepDirectoriesMdl.setBooleanValue(true);
                }
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
                Arrays.stream(ArchiveFormat.values())
                        .map(ArchiveFormat::getText).toList(),
                false, createFlowVariableModel(archiveFormatMdl.getKey(),
                        VariableType.StringType.INSTANCE)).getComponentPanel());

        archiveFmtPanel.add(
                new DialogComponentBoolean(keepDirectoriesMdl, KEEP_DIRECTORIES)
                        .getComponentPanel());
        return archiveFmtPanel;
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
                currentArchiveFormat, k -> k.createInputStreamOptions()))
                        .createSettingsPane(archiveFormatOptionsPanel,
                                FlowVariableStackSpec
                                        .createFromNodeDialog(this));

        restoreDefaultsButton
                .setEnabled(archiveFormatOptionsPanel.getComponentCount() > 0);

        archiveFormatOptionsPanel.validate();
        archiveFormatOptionsPanel.repaint();
    }

    private void updateComponentsOnArchiveFormatChange() {

        currentArchiveFormat = ArchiveFormat
                .valueOf(archiveFormatMdl.getStringValue().replace(' ', '_'));

        keepDirectoriesMdl
                .setEnabled(currentArchiveFormat.supportsDirectories());

        if (getMaxExpandedSizeModel() != null) {
            getMaxExpandedSizeModel()
                    .setEnabled(currentArchiveFormat.includesCompression());
        }
        if (getMaxCompressionRatioModel() != null) {
            getMaxCompressionRatioModel()
                    .setEnabled(currentArchiveFormat.includesCompression());
        }
        updateArchiveFormatOptionsPanel();

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

    private JPanel createBinaryObjectsColumnPanel() {

        JPanel blobColPanel = new JPanel();
        blobColPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Column Options"));
        colNameDiac =
                new DialogComponentColumnNameSelection(createBlobColNameMdl(),
                        BINARY_OBJECTS_COLUMN, 0, BLOB_COLUMN_FILTER);
        blobColPanel.add(colNameDiac.getComponentPanel());
        blobColPanel.add(new DialogComponentBoolean(removeInputColumnMdl,
                REMOVE_INPUT_COLUMN).getComponentPanel());
        return blobColPanel;
    }

    /**
     * @return model for the 'Keep directories' option
     */
    static final SettingsModelBoolean createKeepDirectoriesModel() {

        return new SettingsModelBoolean(KEEP_DIRECTORIES, true);
    }

    /**
     * @return settings model for the 'Remove Input Column' option
     */
    static final SettingsModelBoolean createRemoveInputColumnModel() {

        return new SettingsModelBoolean(REMOVE_INPUT_COLUMN, true);
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

    /**
     * @return settings model for the 'Case Sensitive' option
     */
    static final SettingsModelBoolean createCaseSensitiveModel() {

        return new SettingsModelBoolean(CASE_SENSITIVE, false);
    }

    /**
     * @return settings model for the 'Path Filter Type' option
     */
    static final SettingsModelString createPatternTypeModel() {

        return new SettingsModelString("Path Filter Type",
                PathFilterType.getDefault().getActionCommand());
    }

    /**
     * @return settings model for the 'Path Pattern' option
     */
    static final SettingsModelString createPatternModel() {

        return new SettingsModelString("Path Pattern", "");
    }

    /**
     * @return settings model for the 'Filter Paths' option
     */
    static final SettingsModelBoolean createFilterPathsModel() {

        return new SettingsModelBoolean(FILTER_PATHS, false);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings)
            throws InvalidSettingsException {

        colNameDiac.saveSettingsTo(settings);
        removeInputColumnMdl.saveSettingsTo(settings);
        keepDirectoriesMdl.saveSettingsTo(settings);
        archiveFormatMdl.saveSettingsTo(settings);
        archiveFormatOpts
                .computeIfAbsent(currentArchiveFormat,
                        k -> k.createInputStreamOptions())
                .saveToSettings(settings);

        filterPathsMdl.saveSettingsTo(settings);
        patternMdl.saveSettingsTo(settings);
        patternTypeMdl.saveSettingsTo(settings);
        caseSensitiveMdl.saveSettingsTo(settings);

        if (maxCompressionRatioMdl != null) {
            maxCompressionRatioMdl.saveSettingsTo(settings);
        }
        if (maxExpandedSizeMdl != null) {
            maxExpandedSizeMdl.saveSettingsTo(settings);
        }
        maxExpandedEntriesMdl.saveSettingsTo(settings);
        failOnExpansionExplosionMdl.saveSettingsTo(settings);

    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings,
            DataTableSpec[] specs) throws NotConfigurableException {

        lastSpecs = Arrays.copyOf(specs, specs.length);

        try {
            // We need this to load in the values for the column selection
            colNameDiac.loadSettingsFrom(settings, specs);
            removeInputColumnMdl.loadSettingsFrom(settings);
            keepDirectoriesMdl.loadSettingsFrom(settings);
            archiveFormatMdl.loadSettingsFrom(settings);
            currentArchiveFormat = ArchiveFormat.valueOf(
                    archiveFormatMdl.getStringValue().replace(' ', '_'));
            archiveFormatOpts
                    .computeIfAbsent(currentArchiveFormat,
                            k -> k.createInputStreamOptions())
                    .loadFromSettings(settings, specs,
                            FlowVariableStackSpec.createFromNodeDialog(this));
            filterPathsMdl.loadSettingsFrom(settings);
            patternMdl.loadSettingsFrom(settings);
            patternTypeMdl.loadSettingsFrom(settings);
            caseSensitiveMdl.loadSettingsFrom(settings);
        } catch (InvalidSettingsException | IllegalArgumentException
                | NullPointerException e) {
            // We ignore them here and load the panel
        }
        // These two added later - don't break the others by putting in same
        // try-catch
        try {
            if (maxCompressionRatioMdl != null) {
                maxCompressionRatioMdl.loadSettingsFrom(settings);
            }
            if (maxExpandedSizeMdl != null) {
                maxExpandedSizeMdl.loadSettingsFrom(settings);
            }
            maxExpandedEntriesMdl.loadSettingsFrom(settings);
            failOnExpansionExplosionMdl.loadSettingsFrom(settings);
        } catch (InvalidSettingsException | IllegalArgumentException
                | NullPointerException e) {
            // We ignore them here and load the panel
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxExpandedSizeModel()
     */
    @Override
    public SettingsModelLongBounded getMaxExpandedSizeModel() {

        return maxExpandedSizeMdl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxCompressionRatioModel()
     */
    @Override
    public SettingsModelDoubleBounded getMaxCompressionRatioModel() {

        return maxCompressionRatioMdl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getMaxEntriesModel()
     */
    @Override
    public SettingsModelIntegerBounded getMaxEntriesModel() {

        return maxExpandedEntriesMdl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.SecurityOptionsContainer#
     * getFailOnExpansionExplosionModel()
     */
    @Override
    public SettingsModelBoolean getFailOnExpansionExplosionModel() {

        return failOnExpansionExplosionMdl;
    }

}
