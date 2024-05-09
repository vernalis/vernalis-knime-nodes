/*******************************************************************************
 * Copyright (c) 2019, 2024, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.nodes.collection.size;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.dialog.components.DialogComponentEmpty;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog;

/**
 * Node Dialog for the Collection Size node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class CollectionSizeNodeDialog
        extends AbstractMultiCollectionNodeDialog {

    private static final String UNIQUE_VALUES_ONLY = "Unique values only";

    /**
     * Constructor
     */
    public CollectionSizeNodeDialog() {

        super(false);

        createNewGroup("Counting options");

        addDialogComponent(
                new DialogComponentButtonGroup(createMissingValuesModel(), null,
                        false, MissingValueOptions.values()));
        addDialogComponent(new DialogComponentBoolean(createUniqueValuesModel(),
                UNIQUE_VALUES_ONLY));
        addDialogComponent(new DialogComponentEmpty(createOutputTypeModel()));
    }

    /**
     * @return settings model for the 'Long outputs' option. This option is
     *             present only to preserve backwards compatibility for existing
     *             nodes, and is not intended to be changed by the user
     */
    static final SettingsModelBoolean createOutputTypeModel() {

        return new SettingsModelBoolean("Long outputs", true);
    }

    /**
     * @return settings model for the 'Unique values only' option
     */
    static final SettingsModelBoolean createUniqueValuesModel() {

        return new SettingsModelBoolean(UNIQUE_VALUES_ONLY, false);
    }

    /**
     * @return settings model for the 'Missing Values' option
     */
    static final SettingsModelString createMissingValuesModel() {

        return new SettingsModelString("Missing Values",
                MissingValueOptions.getDefault().getActionCommand());
    }

}
