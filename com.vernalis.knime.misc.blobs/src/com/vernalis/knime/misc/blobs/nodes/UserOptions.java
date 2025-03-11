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
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.flowvars.FlowVariableStackSpec;

/**
 * This interface defines a set of user options which can be displayed in a node
 * dialog, along with methods to load, validate and save their settings
 * 
 * @author S.Roughley knime@vernalis.com
 */
public interface UserOptions {

    /**
     * Method to allow any required validation during the node #configure()
     * method to take place. Most implementations are unlikely to need anything
     * here, but may, for example be used to validate or guess a default column
     * selection
     * 
     * @param specs
     *            the incoming specs
     * @return a warning message if there is one to display, otherwise
     *             {@code null}
     * @throws InvalidSettingsException
     *             if there is an error with the settings configuration
     */
    public default String validateInConfigure(PortObjectSpec[] specs)
            throws InvalidSettingsException {

        // Do nothing
        return null;
    }

    /**
     * Method to load settings in the dialog component when the components need
     * access to the incoming {@link PortObjectSpec}s, e.g. for a column name
     * selection dropdown. The default implementation defers to
     * {@link #loadFromSettings(NodeSettingsRO)}. Most implementations do not
     * need to override this method.
     * 
     * @implNote Any class requiring this method <strong>must</strong> be
     *               initialised during node dialog construction and
     *               saved/loaded in order to populate the required internal
     *               sport specs, or have a call to
     *               {@link #saveToSettings(NodeSettingsWO)} followed by a call
     *               to this method via a dummy settings object and cache'd
     *               {@link PortObjectSpec}s
     * @param settings
     *            the saved settings
     * @param portSpecs
     *            the incoming port specs
     * @param flowVarsSpec
     *            the flow variables spec if required (maybe left as
     *            {@code null} if not required) as the port specs do not provide
     *            access to the variable stack
     * @throws InvalidSettingsException
     *             if there is a problem during load
     * @throws NotConfigurableException
     *             if there is a problem which prevents the dialog from opening
     * @see #loadFromSettings(NodeSettingsRO)
     * @see #saveToSettings(NodeSettingsWO)
     */
    public default void loadFromSettings(NodeSettingsRO settings,
            PortObjectSpec[] portSpecs, FlowVariableStackSpec flowVarsSpec)
            throws InvalidSettingsException, NotConfigurableException {

        // Default implementation just defers to the model method
        loadFromSettings(settings);
    }

    /**
     * Method to load the saved settings into the model.
     * 
     * @param settings
     *            the saved settings
     * @throws InvalidSettingsException
     *             if there is a problem with the
     *             settings
     * @see #saveToSettings(NodeSettingsWO)
     * @see #loadFromSettings(NodeSettingsRO,
     *          PortObjectSpec[],
     *          FlowVariableStackSpec)
     */
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException;

    /**
     * Method to validate the settings. This moethod should check that all
     * supplied settings are valid, but should not change the state of the
     * object in any way
     * 
     * @param settings
     *            the settings to test
     * @throws InvalidSettingsException
     *             if there is a problem with the
     *             settings
     * @see #saveToSettings(NodeSettingsWO)
     */
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException;

    /**
     * Method to save the settings into the supplied settings object. Almost
     * certainly, a sub-model should be created and the individual setting(s)
     * saved into that
     * 
     * @param settings
     *            the parent settings object
     */
    public void saveToSettings(NodeSettingsWO settings);

    /**
     * Method to populate a parent {@link JPanel} with the required user
     * components.
     * <p>
     * Implementations should always clear the parent panel first as there is no
     * guarantee that a calling class will have done so
     * </p>
     * <p>
     * Implementations should not change the parent panel in any way (e.g. by
     * setting a different {@link java.awt.LayoutManager} or
     * {@link javax.swing.border.Border}) If such things are required then they
     * should be placed in a top-level panel within this method which is then
     * added to the parent panel - nesting Swing components is cheap!
     * </p>
     * 
     * @param parent
     *            the parent panel
     * @param flowVarsStackSpec
     *            flow variable stack in case needed e.g. to
     *            create
     *            {@link org.knime.core.node.FlowVariableModel}s.
     *            Maybe {@code null}
     */
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec);

    /**
     * Method to set a warning consumer using the settings, in case it is
     * needed e.g. for warning messages when new settings are added to an
     * implementation
     * 
     * @param warningConsumer
     *            the warning consumer
     * @implNote <strong>NB</strong> The default implementation does nothing
     */
    public default void setWarningConsumer(Consumer<String> warningConsumer) {

        // The default does nothing
    }

}
