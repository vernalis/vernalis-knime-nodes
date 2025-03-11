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
package com.vernalis.knime.misc.blobs.nodes;

import static com.vernalis.knime.misc.blobs.nodes.BlobConstants.DATE_COLUMN_FILTER;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.time.util.DialogComponentDateTimeSelection;
import org.knime.time.util.DialogComponentDateTimeSelection.DisplayOption;
import org.knime.time.util.SettingsModelDateTime;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.archive.BlankInOutStreamOptions;

/**
 * Enum listing the available Last Modified Time creation options
 * 
 * @author S.Roughley knime@vernalis.com
 */
public enum LastModifiedDateTimeOptions
        implements ButtonGroupEnumInterface, Supplier<UserOptions> {

    /** Runtime timestamp */
    Runtime {

        @Override
        public UserOptions get() {

            // No further settings for this one
            return BlankInOutStreamOptions.getInstance();
        }

    },
    /** From a column */
    Column {

        @Override
        public UserOptions get() {

            return new ColumnTimeOptions();
        }

    },

    /** A pre-set fixed time */
    Fixed_Time {

        @Override
        public UserOptions get() {

            return new FixedTimeOptions();
        }

    },
    /** Dont set one */
    None {

        @Override
        public UserOptions get() {

            // No further settings for this one
            return BlankInOutStreamOptions.getInstance();
        }
    };

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

    /**
     * @return the default option
     */
    public static LastModifiedDateTimeOptions getDefault() {

        return Runtime;
    }

    /**
     * The {@link UserOptions} implementation for a fixed time
     * 
     * @author S.Roughley knime@vernalis.com
     */
    public static class FixedTimeOptions implements UserOptions {

        private static final String KEY = "FIXED_TIME";
        private final SettingsModelDateTime dateTime =
                new SettingsModelDateTime("DATE_TIME", LocalDateTime.now());

        /**
         * @return the stored date
         */
        public Date getDate() {

            return Date.from(dateTime.getLocalDateTime()
                    .atZone(ZoneId.systemDefault()).toInstant());
        }

        @Override
        public void loadFromSettings(NodeSettingsRO settings)
                throws InvalidSettingsException {

            NodeSettingsRO mySettings = settings.getNodeSettings(KEY);
            dateTime.loadSettingsFrom(mySettings);

        }

        @Override
        public void validateSettings(NodeSettingsRO settings)
                throws InvalidSettingsException {

            NodeSettingsRO mySettings = settings.getNodeSettings(KEY);
            dateTime.validateSettings(mySettings);

        }

        @Override
        public void saveToSettings(NodeSettingsWO settings) {

            NodeSettingsWO mySettings = settings.addNodeSettings(KEY);
            dateTime.saveSettingsTo(mySettings);
        }

        @Override
        public void createSettingsPane(JPanel parent,
                FlowVariableStackSpec flowVarsStackSpec) {

            parent.removeAll();

            parent.add(new DialogComponentDateTimeSelection(dateTime, null,
                    DisplayOption.SHOW_DATE_AND_TIME).getComponentPanel());

        }

    }

    /**
     * The {@link UserOptions} implementation to choose a column contianig the
     * timestamp. NB This must use the
     * {@link #loadFromSettings(NodeSettingsRO, PortObjectSpec[], FlowVariableStackSpec)}
     * method in a node dialog
     * 
     * @author S.Roughley knime@vernalis.com
     */
    public static class ColumnTimeOptions implements UserOptions {

        private static final String KEY = "COLUMN_TIME";

        private SettingsModelString dateColNameModel =
                new SettingsModelString("LM_COLUMN_NAME", null);

        private DialogComponentColumnNameSelection dateColNameDiac =
                new DialogComponentColumnNameSelection(dateColNameModel,
                        "Last Modified Column", 0, DATE_COLUMN_FILTER);

        /**
         * @param inSpec
         *            the incoming table spec
         * @return the column index for the matching name, or -1 if not name
         *             matches. NB This method does not check the column type
         * @see #validateInConfigure(PortObjectSpec[])
         */
        public final int getColumnIndex(DataTableSpec inSpec) {

            return inSpec.findColumnIndex(dateColNameModel.getStringValue());
        }

        @Override
        public String validateInConfigure(PortObjectSpec[] specs)
                throws InvalidSettingsException {

            DataTableSpec spec = (DataTableSpec) specs[0];
            List<String> allowedColumnNames =
                    spec.stream().filter(DATE_COLUMN_FILTER::includeColumn)
                            .map(DataColumnSpec::getName).toList();

            if (allowedColumnNames.isEmpty()) {
                throw new InvalidSettingsException(
                        "No Date-Time columns in input table!");
            }
            String selectedColName = dateColNameModel.getStringValue();

            if (selectedColName != null && !selectedColName.isEmpty()) {

                // We have a column selection
                if (!allowedColumnNames.contains(selectedColName)) {
                    // it isnt valid
                    throw new InvalidSettingsException("The selected column ("
                            + selectedColName
                            + ") is not present in the incoming table or is of the wrong type");
                }
            } else {
                // No selection - guess the last one of the right type
                selectedColName =
                        allowedColumnNames.get(allowedColumnNames.size() - 1);
                dateColNameModel.setStringValue(selectedColName);
                return "No column selected - guessed '" + selectedColName
                        + "'!";

            }
            return null;
        }

        @Override
        public void loadFromSettings(NodeSettingsRO settings,
                PortObjectSpec[] portSpecs, FlowVariableStackSpec flowVarsSpec)
                throws InvalidSettingsException, NotConfigurableException {

            NodeSettingsRO mySettings = settings.getNodeSettings(KEY);
            dateColNameDiac.loadSettingsFrom(mySettings, portSpecs);
        }

        @Override
        public void loadFromSettings(NodeSettingsRO settings)
                throws InvalidSettingsException {

            NodeSettingsRO mySettings = settings.getNodeSettings(KEY);
            dateColNameModel.loadSettingsFrom(mySettings);

        }

        @Override
        public void validateSettings(NodeSettingsRO settings)
                throws InvalidSettingsException {

            NodeSettingsRO mySettings = settings.getNodeSettings(KEY);
            dateColNameModel.validateSettings(mySettings);
        }

        @Override
        public void saveToSettings(NodeSettingsWO settings) {

            NodeSettingsWO mySettings = settings.addNodeSettings(KEY);
            dateColNameModel.saveSettingsTo(mySettings);

        }

        @Override
        public void createSettingsPane(JPanel parent,
                FlowVariableStackSpec flowVarsStackSpec) {

            parent.add(dateColNameDiac.getComponentPanel());

        }

    }

}
