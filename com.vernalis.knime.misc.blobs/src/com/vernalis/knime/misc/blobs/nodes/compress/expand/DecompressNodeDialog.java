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
package com.vernalis.knime.misc.blobs.nodes.compress.expand;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.BLOB_COLUMN_FILTER;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createFailOnExpansionExplosionModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createMaxCompressionRatioModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createMaxExpandedEntriesModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createMaxExpandedSizeModel;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionSecurityUtils.createSecurityOptionsPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
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
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
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
import com.vernalis.knime.misc.blobs.nodes.compress.CompressFormat;

/**
 * Node Dialog implementation for the List Archive and Extract Archive nodes
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class DecompressNodeDialog extends NodeDialogPane
        implements SecurityOptionsContainer {

    private static final String REMOVE_INPUT_COLUMN = "Remove input column";
    private static final String BINARY_OBJECTS_COLUMN = "Binary Objects Column";

    // Settings models for load/save
    // Model for the 'Remove input column' option
    private final SettingsModelBoolean removeInputColumnMdl;
    // Model for the name of the compression format
    private final SettingsModelString compressionFormatMdl;
    private final SettingsModelLongBounded maxExpandedSizeMdl;
    private final SettingsModelDoubleBounded maxCompressionRatioMdl;

    private final SettingsModelIntegerBounded maxExpandedEntriesMdl;
    private final SettingsModelBoolean failOnExpansionExplosionMdl;
    // DIsaplyed components/panels
    private JButton restoreDefaultsButton;
    private JPanel compressionFormatOptionsPanel;
    // The dropdown for the Blob column - needed so we can access it in
    // loadSettingsFrom()
    private DialogComponentColumnNameSelection colNameDiac;

    private CompressFormat currentCompressionFormat =
            CompressFormat.getDefault();
    private final Map<CompressFormat, InputStreamWrapperOptions> compressionFormatOpts =
            new EnumMap<>(CompressFormat.class);

    private PortObjectSpec[] lastSpecs;

    /**
     * Constructor
     */
    DecompressNodeDialog() {

        super();

        // Start by initialising the required settings models
        compressionFormatMdl = createCompressFormatMdl();
        compressionFormatMdl.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {

                updateComponentsOnCompressionFormatChange();
            }

        });
        removeInputColumnMdl = createRemoveInputColumnModel();

        // Security models
        maxExpandedSizeMdl = createMaxExpandedSizeModel();
        maxExpandedSizeMdl.addChangeListener(this);

        maxCompressionRatioMdl = createMaxCompressionRatioModel();
        maxCompressionRatioMdl.addChangeListener(this);

        maxExpandedEntriesMdl = createMaxExpandedEntriesModel();
        maxExpandedEntriesMdl.addChangeListener(this);

        failOnExpansionExplosionMdl = createFailOnExpansionExplosionModel();

        // The base panel in which all components in the tab are placed
        Box basePanel = new Box(BoxLayout.Y_AXIS);

        // Add a panel for the Column selector
        basePanel.add(createBinaryObjectsColumnPanel());

        // Create a panel for the options for the compression format with an
        // etched
        // border
        JPanel compressionOptionsPanel = createCompressionOptionsPanel();
        basePanel.add(compressionOptionsPanel);

        // And finally, the 'Security Options' panel
        JPanel securityOptionsPanel = createSecurityOptionsPanel(this);
        basePanel.add(securityOptionsPanel);

        addTab("Options", basePanel);
        updateComponentsOnCompressionFormatChange();
        updateCanTrapExplodingExpansion();
    }

    private JPanel createCompressionOptionsPanel() {

        JPanel compressionOptionsPanel = new JPanel();
        compressionOptionsPanel.setLayout(new BorderLayout(5, 5));
        compressionOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Decompression Options"));

        // Add the compression format panel, containing a format dropdown and
        // keep
        // directories checkbox, across the top of the 'Compression Options'
        // panel
        JPanel compressionsFmtPanel = createCompressionFormatPanel();
        compressionOptionsPanel.add(compressionsFmtPanel, BorderLayout.NORTH);

        // Create an inner panel for the options for the selected format - at
        // this stage we dont populate it
        compressionFormatOptionsPanel = new JPanel();
        compressionFormatOptionsPanel.setPreferredSize(new Dimension(600, 200));
        compressionOptionsPanel.add(compressionFormatOptionsPanel);

        // Create a button for restore defaults
        JPanel btnBox = createRestoreDefaultsButton();
        compressionOptionsPanel.add(btnBox, BorderLayout.SOUTH);
        return compressionOptionsPanel;
    }

    private JPanel createRestoreDefaultsButton() {

        JPanel btnBox = new JPanel();
        restoreDefaultsButton = new JButton("Restore Defaults");
        restoreDefaultsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Replace the settings for the current format with a new
                // instance
                compressionFormatOpts.put(currentCompressionFormat,
                        applyLatestPortSpecsToNewUserOptions(
                                currentCompressionFormat
                                        .createInputStreamOptions()));

                updateCompressionFormatOptionsPanel();

            }

        });
        btnBox.add(restoreDefaultsButton);
        return btnBox;
    }

    private JPanel createCompressionFormatPanel() {

        JPanel compressionFmtPanel = new JPanel();
        compressionFmtPanel.add(new DialogComponentStringSelection(
                compressionFormatMdl, "Compression Format",
                Arrays.stream(CompressFormat.getDecompressionFormats())
                        .map(CompressFormat::getText).toList(),
                false, createFlowVariableModel(compressionFormatMdl.getKey(),
                        VariableType.StringType.INSTANCE)).getComponentPanel());

        return compressionFmtPanel;
    }

    /**
     * Method to ensure the compressionFormatOptionsPanel matches the current
     * settings
     */
    private void updateCompressionFormatOptionsPanel() {

        // Just in case the options is badly behaved and doesn't clear the panel
        // first..
        compressionFormatOptionsPanel.removeAll();
        applyLatestPortSpecsToNewUserOptions(
                compressionFormatOpts.computeIfAbsent(currentCompressionFormat,
                        k -> k.createInputStreamOptions())).createSettingsPane(
                                compressionFormatOptionsPanel,
                                FlowVariableStackSpec
                                        .createFromNodeDialog(this));

        // Do this *before* we add the description!
        restoreDefaultsButton.setEnabled(
                compressionFormatOptionsPanel.getComponentCount() > 0);

        updateDescription();
        compressionFormatOptionsPanel.validate();
        compressionFormatOptionsPanel.repaint();
    }

    private void updateDescription() {

        if (currentCompressionFormat.hasExpandDescription()) {
            String description =
                    currentCompressionFormat.getExpandDescription();
            JPanel descPanel = new JPanel();
            descPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Description"));
            final JTextArea textPanel = new JTextArea(description,
                    Math.min(5, (int) Math.ceil(description.length() / 74.0)),
                    74);
            textPanel.setLineWrap(true);
            textPanel.setWrapStyleWord(true);
            textPanel.setEditable(false);
            textPanel.setCursor(null);
            textPanel.setOpaque(false);
            textPanel.setFocusable(false);

            JScrollPane scroll = new JScrollPane(textPanel,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            // The following appear to both be needed to eliminate the
            // single pixel black border around the viewport
            scroll.setViewportBorder(null);
            scroll.setBorder(null);
            descPanel.add(scroll);
            compressionFormatOptionsPanel.add(descPanel);
        }
    }

    private void updateComponentsOnCompressionFormatChange() {

        currentCompressionFormat = CompressFormat.valueOf(
                compressionFormatMdl.getStringValue().replace(' ', '_'));

        maxExpandedEntriesMdl
                .setEnabled(currentCompressionFormat.supportsConcatenation());

        updateCompressionFormatOptionsPanel();

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
     * @return settings model for the 'Remove Input Column' option
     */
    static final SettingsModelBoolean createRemoveInputColumnModel() {

        return new SettingsModelBoolean(REMOVE_INPUT_COLUMN, true);
    }

    /**
     * @return settings model for the 'Compression Format' option
     */
    static final SettingsModelString createCompressFormatMdl() {

        return new SettingsModelString("Compression format",
                CompressFormat.getDefault().getActionCommand());
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

        colNameDiac.saveSettingsTo(settings);
        removeInputColumnMdl.saveSettingsTo(settings);
        maxCompressionRatioMdl.saveSettingsTo(settings);
        maxExpandedSizeMdl.saveSettingsTo(settings);
        maxExpandedEntriesMdl.saveSettingsTo(settings);
        failOnExpansionExplosionMdl.saveSettingsTo(settings);
        compressionFormatMdl.saveSettingsTo(settings);
        compressionFormatOpts
                .computeIfAbsent(currentCompressionFormat,
                        k -> k.createInputStreamOptions())
                .saveToSettings(settings);

    }

    @Override
    protected void loadSettingsFrom(NodeSettingsRO settings,
            DataTableSpec[] specs) throws NotConfigurableException {

        lastSpecs = Arrays.copyOf(specs, specs.length);

        try {
            // We need this to load in the values for the column selection
            colNameDiac.loadSettingsFrom(settings, specs);
            removeInputColumnMdl.loadSettingsFrom(settings);
            compressionFormatMdl.loadSettingsFrom(settings);
            currentCompressionFormat = CompressFormat.valueOf(
                    compressionFormatMdl.getStringValue().replace(' ', '_'));
            compressionFormatOpts
                    .computeIfAbsent(currentCompressionFormat,
                            k -> k.createInputStreamOptions())
                    .loadFromSettings(settings, specs,
                            FlowVariableStackSpec.createFromNodeDialog(this));

        } catch (InvalidSettingsException | IllegalArgumentException
                | NullPointerException e) {
            // We ignore them here and load the panel
        }
        // These two added later - don't break the others by putting in same
        // try-catch
        try {
            maxCompressionRatioMdl.loadSettingsFrom(settings);
            maxExpandedSizeMdl.loadSettingsFrom(settings);
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
