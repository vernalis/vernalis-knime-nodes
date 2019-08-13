/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.io.nodes.load.fasta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.io.nodes.abstrct.AbstractMultiLineObjectLoadFilesNodeDialog;

public class LoadFASTANodeDialog
		extends AbstractMultiLineObjectLoadFilesNodeDialog<FastaRecord> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.misc.io.nodes.abstrct.
	 * AbstractMultiLineObjectLoadFilesNodeDialog#getNumProperties(org.knime.
	 * core.data.DataColumnSpec[])
	 */
	@Override
	protected int getNumProperties(DataColumnSpec[] newColumnSpecs) {
		return Arrays.stream(FastaTypes.values())
				.mapToInt(x -> x.getColumnNames().length).max().orElse(0) + 4;
	}

	private static final String FASTA_TYPE = "FASTA Type";
	private final SettingsModelString fastaTypeModel;

	public LoadFASTANodeDialog() {
		super(new FastaRecord(), LoadFASTANodeFactory.class.getName(),
				".fasta|.fasta.gz");
		fastaTypeModel = createFASTATypeModel();
		fastaTypeModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				nonReadableObject.setFastaType(
						FastaTypes.valueOf(fastaTypeModel.getStringValue()));
				final List<String> newOptions =
						Arrays.stream(nonReadableObject.getNewColumnSpecs())
								.map(colSpec -> colSpec.getName())
								.collect(Collectors.toList());
				propertiesDlg.replaceListItems(newOptions);
				propertiesDlg.setVisibleRowCount(
						Math.min(getNumProperties(null), 12));
				getTab("Options").validate();
			}
		});
		nonReadableObject.setFastaType(
				FastaTypes.valueOf(fastaTypeModel.getStringValue()));
		propertiesDlg.replaceListItems(
				Arrays.stream(nonReadableObject.getNewColumnSpecs())
						.map(colSpec -> colSpec.getName())
						.collect(Collectors.toList()));
		propertiesDlg.setVisibleRowCount(Math.min(getNumProperties(null), 12));
		getTab("Options").validate();
		addDialogComponent(new DialogComponentButtonGroup(fastaTypeModel,
				FASTA_TYPE, true, FastaTypes.values()));
	}

	static SettingsModelString createFASTATypeModel() {
		return new SettingsModelString(FASTA_TYPE,
				FastaTypes.getDefault().getActionCommand());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		try {
			fastaTypeModel.loadSettingsFrom(settings);
			nonReadableObject.setFastaType(
					FastaTypes.valueOf(fastaTypeModel.getStringValue()));
			final List<String> newOptions =
					Arrays.stream(nonReadableObject.getNewColumnSpecs())
							.map(colSpec -> colSpec.getName())
							.collect(Collectors.toList());
			propertiesDlg.replaceListItems(newOptions);
			propertiesDlg
					.setVisibleRowCount(Math.min(getNumProperties(null), 12));
			getTab("Options").validate();
			// Also, we lose the setting on first load if we do not
			// call the loadSettings method here
			propertiesDlg.getModel().loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			NodeLogger.getLogger(this.getClass())
					.warn("Unable to load settings to dialog");
		}
	}

}
