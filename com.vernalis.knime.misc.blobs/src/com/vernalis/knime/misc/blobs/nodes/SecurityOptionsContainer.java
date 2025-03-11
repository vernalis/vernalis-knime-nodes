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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;

/**
 * Interface defining key methods shared for archive expansion / decompression
 * operations.
 * <p>
 * This extends {@link ChangeListener}, providing a default implementation of
 * {@link #stateChanged(ChangeEvent)} which calls
 * {@link #updateCanTrapExplodingExpansion()}.
 * This latter method sets the enabled status of the model returned by
 * {@link #getFailOnExpansionExplosionModel()} according to whether any of the
 * other models allow trapping explosive expansion / decompression
 * </p>
 * <p>
 * <strong>NB</strong> The settings models returned by the {@code get...Model()}
 * methods should be the same instance on each call, rather than being a factory
 * for new instances
 * </p>
 * <p>
 * <strong>NB</strong> It is the responsibility of the implementation to
 * register this as a changelistener for the first three settings models
 * ({@link #getMaxExpandedSizeModel()}, {@link #getMaxCompressionRatioModel()}
 * and {@link #getMaxEntriesModel()})
 * </p>
 * <p>
 * If any of the options are not to be included in the node, then the
 * corresponding getter should return {@code null}
 * </p>
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public interface SecurityOptionsContainer extends ChangeListener {

    /**
     * @return the settings model for the maximum allowed expanded size
     */
    public SettingsModelLongBounded getMaxExpandedSizeModel();

    /**
     * @return the settings model for the maximum allowed compression ratio
     */
    public SettingsModelDoubleBounded getMaxCompressionRatioModel();

    /**
     * @return the settings model for the maximum allowed number of entries from
     *             a single archive or concatenated file
     */
    public SettingsModelIntegerBounded getMaxEntriesModel();

    /**
     * @return the settings model indicating whether node execution should fail
     *             if explosive expansion is detected
     */
    public SettingsModelBoolean getFailOnExpansionExplosionModel();

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.
     * ChangeEvent)
     */
    @Override
    default void stateChanged(ChangeEvent e) {

        updateCanTrapExplodingExpansion();

    }

    /**
     * Whether the settings allow trapping explosive expansion
     */
    public default void updateCanTrapExplodingExpansion() {

        if (getFailOnExpansionExplosionModel() != null) {
            getFailOnExpansionExplosionModel()
                    .setEnabled((getMaxExpandedSizeModel() != null
                            && getMaxExpandedSizeModel().isEnabled()
                            && getMaxExpandedSizeModel().getLongValue() > 0)
                            || (getMaxCompressionRatioModel() != null
                                    && getMaxCompressionRatioModel().isEnabled()
                                    && getMaxCompressionRatioModel()
                                            .getDoubleValue() > 0.0)
                            || (getMaxEntriesModel() != null
                                    && getMaxEntriesModel().isEnabled()
                                    && getMaxEntriesModel().getIntValue() > 0));
        }
    }
}
