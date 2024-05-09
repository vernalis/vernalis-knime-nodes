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

import static com.vernalis.nodes.collection.size.CollectionSizeNodeDialog.createMissingValuesModel;
import static com.vernalis.nodes.collection.size.CollectionSizeNodeDialog.createOutputTypeModel;
import static com.vernalis.nodes.collection.size.CollectionSizeNodeDialog.createUniqueValuesModel;

import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;
/**
 * NodeModel for the Collection Size node
 * 
 * @author S.Roughley knime@vernalis.com
 */
public class CollectionSizeNodeModel extends AbstractMultiCollectionNodeModel {

    private final SettingsModelString missingValuesMdl = registerSettingsModel(
            createMissingValuesModel(), 3, mdl -> mdl.setStringValue(
                    MissingValueOptions.All.getActionCommand()));
    private final SettingsModelBoolean uniqueValuesMdl = registerSettingsModel(
            createUniqueValuesModel(), 3, mdl -> mdl.setBooleanValue(false));

    // This model is not exposed in the configuration dialogue
    // It simply serves to preserve the legacy 'int' counts for existing nodes
    // whilst providing 'long' counts for new nodes
    private final SettingsModelBoolean useLongsMdl = registerSettingsModel(
            createOutputTypeModel(), 3,
            mdl -> mdl.setBooleanValue(false));

    private MissingValueOptions missingValueOpt;

    /**
     * Constructor
     *
     * @since 10-Jan-2023
     */
    protected CollectionSizeNodeModel() {

        // Version 3 - NB Version 2 is the current global default for the AMCNM
        // - see super class
        super(false, 3);
    }

    @Override
    protected void doConfigure(DataTableSpec spec)
            throws InvalidSettingsException {

        try {
            missingValueOpt = MissingValueOptions
                    .valueOf(missingValuesMdl.getStringValue());
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidSettingsException(
                    "Unknown missing value handling option'"
                            + missingValuesMdl.getStringValue() + "' - "
                            + e.getMessage(),
                    e);
        }
        super.doConfigure(spec);

    }

    @Override
    protected DataCell[] getCells(int[] idx, DataRow row,
            DataColumnSpec[] newColSpecs) throws RuntimeException {

        DataCell[] retVal = ArrayUtils.fill(new DataCell[idx.length],
                DataType.getMissingCell());
        for (int i = 0; i < idx.length; i++) {
            int colIdx = idx[i];
            DataCell cell = row.getCell(colIdx);
            if (cell.isMissing()) {
                continue;
            }
            long l = missingValueOpt.getCount((CollectionDataValue) cell,
                    uniqueValuesMdl.getBooleanValue());
            if (useLongsMdl.getBooleanValue()) {
                retVal[i] = new LongCell(l);
            } else {
                // We need to use IntCell here for backwards compatibility
                retVal[i] = new IntCell((int) Math.min(Integer.MAX_VALUE, l));
            }

        }
        return retVal;
    }

    @Override
    protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
            int[] idx) {

        return Arrays.stream(idx)
                .mapToObj(i -> new DataColumnSpecCreator(
                        DataTableSpec.getUniqueColumnName(spec,
                                spec.getColumnSpec(i).getName() + " Size"),
                        useLongsMdl.getBooleanValue() ? LongCell.TYPE
                                : IntCell.TYPE).createSpec())
                .toArray(DataColumnSpec[]::new);
    }

    @Override
    protected boolean isReplaceInputCols() {

        // We never replace the input columns for this node
        return false;
    }

}
