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

import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import javax.swing.JPanel;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.core.node.workflow.VariableType;

import com.vernalis.knime.dialog.components.DialogComponentMultilineStringFlowvar;
import com.vernalis.knime.dialog.components.SettingsModelMultilineString;
import com.vernalis.knime.flowvars.FlowVariableStackSpec;
import com.vernalis.knime.misc.blobs.nodes.OutputStreamWrapperOptions;
import com.vernalis.testing.NoTest;

/**
 * The {@link OutputStreamWrapperOptions} for the ZIP forma
 * 
 * @author S.Roughley knime@vernalis.com
 */
@NoTest
public class ZipOutstreamOptions extends JarInstreamOptions
        implements OutputStreamWrapperOptions {

    private static final String CFG_KEY_COMMENT = "COMMENT";
    private final SettingsModelMultilineString comment =
            new SettingsModelMultilineString(CFG_KEY_COMMENT, "");
    private final SettingsModelBoolean createUnicodeExtraFields =
            new SettingsModelBoolean("USE_UNICODE_FIELDS", true);
    private final SettingsModelBoolean fallbackToUTF8 =
            new SettingsModelBoolean("FALLBACK_TO_UTF8", false);
    private final SettingsModelInteger level = new SettingsModelIntegerBounded(
            "LEVEL", ZipArchiveOutputStream.DEFAULT_COMPRESSION,
            Deflater.DEFAULT_COMPRESSION, Deflater.BEST_COMPRESSION);
    private final SettingsModelString method = new SettingsModelString("METHOD",
            ZipMethod.getDefault().getActionCommand());
    private final SettingsModelBoolean useLanguageEncodingFlag =
            new SettingsModelBoolean("USE_LANGUAGE_ENCODING_FLAG", true);
    private final SettingsModelString unicodeExtraFieldsPolicy =
            new SettingsModelString("CREATE_UNICODE_EXTRA_FIELDS_POLICY",
                    UnicodeExtraFieldPolicies.getDefault().getActionCommand());

    private final SettingsModelString zip64Mode = new SettingsModelString(
            "ZIP64_MODE", Zip64Mode.getDefault().getActionCommand());

    private enum ZipMethod implements ButtonGroupEnumInterface {

        Stored(ZipEntry.STORED), Deflated(ZipEntry.DEFLATED);

        private final int method;

        private ZipMethod(int method) {

            this.method = method;
        }

        @Override
        public String getText() {

            return name();
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

        private static ZipMethod getDefault() {

            return Deflated;
        }

    }

    private enum Zip64Mode implements ButtonGroupEnumInterface {

        Always(org.apache.commons.compress.archivers.zip.Zip64Mode.Always),
        Never(org.apache.commons.compress.archivers.zip.Zip64Mode.Never),
        As_Needed(org.apache.commons.compress.archivers.zip.Zip64Mode.AsNeeded);

        private final org.apache.commons.compress.archivers.zip.Zip64Mode mode;

        private Zip64Mode(
                org.apache.commons.compress.archivers.zip.Zip64Mode mode) {

            this.mode = mode;
        }

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

        private static Zip64Mode getDefault() {

            return As_Needed;
        }

    }

    private enum UnicodeExtraFieldPolicies implements ButtonGroupEnumInterface {

        Always(UnicodeExtraFieldPolicy.ALWAYS),
        Never(UnicodeExtraFieldPolicy.NEVER),
        Not_Encodable(UnicodeExtraFieldPolicy.NOT_ENCODEABLE);

        private final UnicodeExtraFieldPolicy policy;

        private UnicodeExtraFieldPolicies(UnicodeExtraFieldPolicy policy) {

            this.policy = policy;
        }

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

        private static UnicodeExtraFieldPolicies getDefault() {

            return Never;
        }

    }

    /**
     * Instance Constructor
     */
    public ZipOutstreamOptions() {

        super("ZIP_OPTIONS");
    }

    /**
     * Subclass constructor
     * 
     * @param key
     *            the root settings key
     */
    protected ZipOutstreamOptions(String key) {

        super(key);
    }

    /**
     * Method to apply the stored settings to a {@link ZipArchiveOutputStream}
     * instance
     * 
     * @param zos
     *            the stream
     */
    public void applyToOutputStream(ZipArchiveOutputStream zos) {

        zos.setComment(comment.getStringValue() == null ? ""
                : comment.getStringValue());
        zos.setCreateUnicodeExtraFields(UnicodeExtraFieldPolicies
                .valueOf(unicodeExtraFieldsPolicy.getStringValue()).policy);
        zos.setEncoding(getEncoding());
        zos.setFallbackToUTF8(fallbackToUTF8.getBooleanValue());
        zos.setLevel(level.getIntValue());
        zos.setMethod(ZipMethod.valueOf(method.getStringValue()).method);
        zos.setUseLanguageEncodingFlag(
                useLanguageEncodingFlag.getBooleanValue());
        zos.setUseZip64(Zip64Mode.valueOf(zip64Mode.getStringValue()).mode);
    }

    @Override
    protected void loadSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.loadSubsettings(mySettings);
        comment.loadSettingsFrom(mySettings);
        createUnicodeExtraFields.loadSettingsFrom(mySettings);
        fallbackToUTF8.loadSettingsFrom(mySettings);
        level.loadSettingsFrom(mySettings);
        method.loadSettingsFrom(mySettings);
        unicodeExtraFieldsPolicy.loadSettingsFrom(mySettings);
        useLanguageEncodingFlag.loadSettingsFrom(mySettings);
        zip64Mode.loadSettingsFrom(mySettings);

    }

    @Override
    protected void validateSubsettings(NodeSettingsRO mySettings)
            throws InvalidSettingsException {

        super.validateSubsettings(mySettings);
        comment.validateSettings(mySettings);
        createUnicodeExtraFields.validateSettings(mySettings);
        fallbackToUTF8.validateSettings(mySettings);
        level.validateSettings(mySettings);
        method.validateSettings(mySettings);
        unicodeExtraFieldsPolicy.validateSettings(mySettings);
        useLanguageEncodingFlag.validateSettings(mySettings);
        zip64Mode.validateSettings(mySettings);
    }

    @Override
    protected void saveSubsettings(NodeSettingsWO mySettings) {

        super.saveSubsettings(mySettings);
        comment.saveSettingsTo(mySettings);
        createUnicodeExtraFields.saveSettingsTo(mySettings);
        fallbackToUTF8.saveSettingsTo(mySettings);
        level.saveSettingsTo(mySettings);
        method.saveSettingsTo(mySettings);
        unicodeExtraFieldsPolicy.saveSettingsTo(mySettings);
        useLanguageEncodingFlag.saveSettingsTo(mySettings);
        zip64Mode.saveSettingsTo(mySettings);
    }

    @Override
    public void createSettingsPane(JPanel parent,
            FlowVariableStackSpec flowVarsStackSpec) {

        super.createSettingsPane(parent, flowVarsStackSpec);
        parent.add(
                new DialogComponentBoolean(fallbackToUTF8, "Fallback to UTF-8")
                        .getComponentPanel());

        parent.add(new DialogComponentNumber(level, "Compression level", 1)
                .getComponentPanel());
        parent.add(new DialogComponentMultilineStringFlowvar(
                comment, "Archive Comment", false, 60, 5,
                flowVarsStackSpec.createFlowVariableModel(
                        new String[] { getRootSettingsKey(), CFG_KEY_COMMENT },
                        VariableType.StringType.INSTANCE)).getComponentPanel());
        
        parent.add(new DialogComponentBoolean(createUnicodeExtraFields,
                "Use unicode extra fields").getComponentPanel());
        parent.add(new DialogComponentButtonGroup(unicodeExtraFieldsPolicy,
                "Unicode Extra Fields Policy", false,
                UnicodeExtraFieldPolicies.values()).getComponentPanel());
        parent.add(new DialogComponentButtonGroup(method, "ZIP Method", false,
                ZipMethod.values()).getComponentPanel());
        parent.add(new DialogComponentBoolean(useLanguageEncodingFlag,
                "Use Language encoding flag").getComponentPanel());
        parent.add(new DialogComponentButtonGroup(zip64Mode, "ZIP64 Mode",
                false, Zip64Mode.values()).getComponentPanel());
    }

}
