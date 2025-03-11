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

import static com.vernalis.knime.misc.blobs.nodes.ExpansionBombProof.DEFAULT_MAX_COMPRESSION_RATIO;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionBombProof.DEFAULT_MAX_ENTRIES;
import static com.vernalis.knime.misc.blobs.nodes.ExpansionBombProof.DEFAULT_MAX_EXPANDED_SIZE;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;

/**
 * Utility class containing helper methods for shared security code between
 * archive expansion / exploration and decompression
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public class ExpansionSecurityUtils {

    /** Key for the 'Fail on explosive expansion' option */
    public static final String FAIL_ON_EXPLOSIVE_EXPANSION =
            "Fail on explosive expansion";

    /** Key for the 'Maximum number of expanded entries' option */
    public static final String MAXIMUM_NUMBER_OF_EXPANDED_ENTRIES =
            "Maximum number of expanded entries";

    /** Key for the 'Maximum compression ration' option */
    public static final String MAXIMUM_COMPRESSION_RATIO =
            "Maximum Compression Ratio";

    /** Key for the 'Maximum expanded size (bytes)' option */
    public static final String MAXIMUM_EXPANDED_SIZE_BYTES =
            "Maximum expanded size (bytes)";

    private ExpansionSecurityUtils() {

        // Do not instantiate!
        throw new UnsupportedOperationException();
    }

    /**
     * Utility method to build a security options dialog panel
     * 
     * @param secOptsContainer
     *            the options container - almost certainly the Node Dialog Pane
     * @return a panel containing the required options
     */
    public static JPanel createSecurityOptionsPanel(
            SecurityOptionsContainer secOptsContainer) {

        JPanel securityPanel = new JPanel();
        securityPanel.setLayout(new BoxLayout(securityPanel, BoxLayout.Y_AXIS));
        securityPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Security Options"));

        if (secOptsContainer.getMaxExpandedSizeModel() != null
                || secOptsContainer.getMaxCompressionRatioModel() != null
                || secOptsContainer.getMaxEntriesModel() != null) {
            Box row0 = Box.createHorizontalBox();
            if (secOptsContainer.getMaxExpandedSizeModel() != null) {
                row0.add(new DialogComponentNumber(
                        secOptsContainer.getMaxExpandedSizeModel(),
                        MAXIMUM_EXPANDED_SIZE_BYTES, 1000, 14)
                                .getComponentPanel());
            }
            if (secOptsContainer.getMaxCompressionRatioModel() != null) {
                row0.add(new DialogComponentNumber(
                        secOptsContainer.getMaxCompressionRatioModel(),
                        MAXIMUM_COMPRESSION_RATIO, 0.5, 5).getComponentPanel());
            }
            if (secOptsContainer.getMaxEntriesModel() != null) {
                row0.add(new DialogComponentNumber(
                        secOptsContainer.getMaxEntriesModel(),
                        MAXIMUM_NUMBER_OF_EXPANDED_ENTRIES, 1000)
                                .getComponentPanel());
            }
            securityPanel.add(row0);
        }
        if (secOptsContainer.getFailOnExpansionExplosionModel() != null) {
            securityPanel.add(new DialogComponentBoolean(
                    secOptsContainer.getFailOnExpansionExplosionModel(),
                    FAIL_ON_EXPLOSIVE_EXPANSION).getComponentPanel());
        }
        return securityPanel;
    }

    /**
     * @return settings model for the 'Fail on explosive expansions' option
     * @since 1.38.0
     */
    public static final SettingsModelBoolean createFailOnExpansionExplosionModel() {

        return new SettingsModelBoolean(
                ExpansionSecurityUtils.FAIL_ON_EXPLOSIVE_EXPANSION, true);
    }

    /**
     * @return settings model for the 'Maximum expanded size' option
     * @since 1.38.0
     */
    public static final SettingsModelLongBounded createMaxExpandedSizeModel() {

        return new SettingsModelLongBounded(
                ExpansionSecurityUtils.MAXIMUM_EXPANDED_SIZE_BYTES,
                DEFAULT_MAX_EXPANDED_SIZE, -1, 1000L * Integer.MAX_VALUE);
    }

    /**
     * @return settings model for the 'Maximum number of expanded entries'
     *             option
     * @since 1.38.0
     */
    public static final SettingsModelIntegerBounded createMaxExpandedEntriesModel() {

        return new SettingsModelIntegerBounded(
                ExpansionSecurityUtils.MAXIMUM_NUMBER_OF_EXPANDED_ENTRIES,
                DEFAULT_MAX_ENTRIES, -1, Integer.MAX_VALUE);
    }

    /**
     * @return settings model for the 'Maximum compression ratio' option
     * @since 1.38.0
     */
    public static final SettingsModelDoubleBounded createMaxCompressionRatioModel() {

        return new SettingsModelDoubleBounded(
                ExpansionSecurityUtils.MAXIMUM_COMPRESSION_RATIO,
                DEFAULT_MAX_COMPRESSION_RATIO, -1.0, 1000);
    }
}
