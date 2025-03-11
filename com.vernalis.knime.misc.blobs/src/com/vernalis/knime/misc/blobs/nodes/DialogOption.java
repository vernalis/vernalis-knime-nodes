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

import java.util.function.Function;
import java.util.function.Supplier;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;

/**
 * Dialog option for additional format-specific settings
 * 
 * @author S.Roughley knime@vernalis.com
 * @param <D>
 *            The type of dialog component
 * @param <S>
 *            The type of settings model
 * @param <V>
 *            The value type to return from the model
 */
public interface DialogOption<D extends DialogComponent, S extends SettingsModel, V>
        extends Supplier<S>, Function<S, D> {

    /**
     * @return the option name
     */
    String getName();

    /**
     * Method to return the value from the model instance
     * 
     * @param model
     *            the model
     * @return the value
     */
    V getValueFromModel(S model);

}
