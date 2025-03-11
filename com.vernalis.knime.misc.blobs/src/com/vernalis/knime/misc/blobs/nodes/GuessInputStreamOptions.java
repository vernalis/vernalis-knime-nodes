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

import java.util.function.Consumer;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.knime.nodes.SettingsModelRegistryImpl;

/**
 * {@link InputStreamWrapperOptions} implementation for 'guess' options for
 * archives and compressions
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public class GuessInputStreamOptions implements InputStreamWrapperOptions {

    private Consumer<String> warningConsumer = null;

    private final SettingsModelRegistry smr =
            new SettingsModelRegistryImpl(2, NodeLogger.getLogger(getClass())) {

                @Override
                public void doSetWarningMessage(String message) {

                    if (warningConsumer == null) {
                        getNodeLogger().warn(message);
                    } else {
                        warningConsumer.accept(message);
                    }
                }
            };
    private final SettingsModelString noMatchBehaviourModel =
            smr.registerSettingsModel(
                    new SettingsModelString("No match behaviour",
                            NoMatchBehaviourOptions.getDefault()
                                    .getActionCommand()),
                    2, mdl -> mdl.setStringValue(
                            NoMatchBehaviourOptions.Fail.getActionCommand()));

    private enum NoMatchBehaviourOptions implements ButtonGroupEnumInterface {

        Fail("Fail node execution"),
        Missing_Value("Add a missing value in the output table"),
        Pass_through("Pass through the input binary object unchanged");

        private final String tooltip;

        private NoMatchBehaviourOptions(String tooltip) {

            this.tooltip = tooltip;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.knime.core.node.util.ButtonGroupEnumInterface#getText()
         */
        @Override
        public String getText() {

            return name().replace('_', ' ');
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.knime.core.node.util.ButtonGroupEnumInterface#getActionCommand()
         */
        @Override
        public String getActionCommand() {

            return name();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.knime.core.node.util.ButtonGroupEnumInterface#getToolTip()
         */
        @Override
        public String getToolTip() {

            return tooltip;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.knime.core.node.util.ButtonGroupEnumInterface#isDefault()
         */
        @Override
        public boolean isDefault() {

            return this == getDefault();
        }

        /**
         * @return the default
         */
        static final NoMatchBehaviourOptions getDefault() {

            return Fail;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vernalis.knime.misc.blobs.nodes.UserOptions#
     * setWarningConsumer(java.util.function.Consumer)
     */
    @Override
    public void setWarningConsumer(Consumer<String> warningConsumer) {

        this.warningConsumer = warningConsumer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.UserOptions#loadFromSettings
     * (org.knime.core.node.NodeSettingsRO)
     */
    @Override
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        smr.loadValidatedSettingsFrom(settings);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.UserOptions#validateSettings
     * (org.knime.core.node.NodeSettingsRO)
     */
    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        smr.validateSettings(settings);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.vernalis.knime.misc.blobs.nodes.UserOptions#saveToSettings(
     * org.knime.core.node.NodeSettingsWO)
     */
    @Override
    public void saveToSettings(NodeSettingsWO settings) {

        smr.saveSettingsTo(settings);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vernalis.knime.misc.blobs.nodes.UserOptions#
     * createSettingsPane(javax.swing.JPanel,
     * com.vernalis.knime.flowvars.FlowVariableStackSpec)
     */
    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();
        parent.add(new DialogComponentButtonGroup(noMatchBehaviourModel,
                "No match behaviour", false, NoMatchBehaviourOptions.values())
                        .getComponentPanel());

    }

    /**
     * @return if the selected option should fail node execution
     */
    public boolean isFail() {

        return NoMatchBehaviourOptions.valueOf(noMatchBehaviourModel
                .getStringValue()) == NoMatchBehaviourOptions.Fail;
    }

    /**
     * @return if the selected option should return a missing value cell
     */
    public boolean isMissing() {

        return NoMatchBehaviourOptions.valueOf(noMatchBehaviourModel
                .getStringValue()) == NoMatchBehaviourOptions.Missing_Value;
    }

    /**
     * @return is the selected option should pass the input through unchanged
     */
    public boolean isPassThrough() {

        return NoMatchBehaviourOptions.valueOf(noMatchBehaviourModel
                .getStringValue()) == NoMatchBehaviourOptions.Pass_through;
    }

}
