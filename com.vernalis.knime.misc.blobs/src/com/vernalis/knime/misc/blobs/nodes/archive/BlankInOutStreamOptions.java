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

import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.InputStreamWrapperOptions;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;

/**
 * A blank {@link InputStreamWrapperOptions} and
 * {@link OutputStreamWrapperOptions} implementation. Callers should use the
 * singleton instance from {@link #getInstance()}
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class BlankInOutStreamOptions
        implements InputStreamWrapperOptions, OutputStreamWrapperOptions {

    private static BlankInOutStreamOptions INSTANCE;

    private BlankInOutStreamOptions() {

    }

    /**
     * @return the singleton instance
     */
    public static BlankInOutStreamOptions getInstance() {

        if (null == INSTANCE) {
            INSTANCE = new BlankInOutStreamOptions();
        }
        return INSTANCE;

    }

    @Override
    public void loadFromSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        // nothing to do

    }

    @Override
    public void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {

        // nothing to do

    }

    @Override
    public void saveToSettings(NodeSettingsWO settings) {

        // nothing to do

    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        parent.removeAll();

    }

}
