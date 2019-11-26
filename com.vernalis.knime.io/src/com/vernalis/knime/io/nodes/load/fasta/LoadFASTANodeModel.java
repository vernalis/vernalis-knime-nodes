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

import java.io.BufferedReader;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.io.MultilineTextObjectReader;
import com.vernalis.knime.io.nodes.abstrct.AbstractMultiLineObjectLoadFilesNodeModel;

import static com.vernalis.knime.io.nodes.load.fasta.LoadFASTANodeDialog.createFASTATypeModel;

public class LoadFASTANodeModel
		extends AbstractMultiLineObjectLoadFilesNodeModel<FastaRecord> {

	private final SettingsModelString fastaTypeModel = createFASTATypeModel();

	protected LoadFASTANodeModel() {
		super(new FastaRecord());
		fastaTypeModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				nonReadableObject.setFastaType(
						FastaTypes.valueOf(fastaTypeModel.getStringValue()));

			}
		});
		nonReadableObject.setFastaType(
				FastaTypes.valueOf(fastaTypeModel.getStringValue()));
	}

	@Override
	protected MultilineTextObjectReader<FastaRecord> getObjectReader(
			BufferedReader br) {
		return new FastaFileReader(br,
				FastaTypes.valueOf(fastaTypeModel.getStringValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.misc.io.nodes.abstrct.
	 * AbstractMultiLineObjectLoadFilesNodeModel#saveSettingsTo(org.knime.core.
	 * node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		fastaTypeModel.saveSettingsTo(settings);
		super.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.misc.io.nodes.abstrct.
	 * AbstractMultiLineObjectLoadFilesNodeModel#loadValidatedSettingsFrom(org.
	 * knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		fastaTypeModel.loadSettingsFrom(settings);
		super.loadValidatedSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.misc.io.nodes.abstrct.
	 * AbstractMultiLineObjectLoadFilesNodeModel#validateSettings(org.knime.core
	 * .node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		fastaTypeModel.validateSettings(settings);
		super.validateSettings(settings);
	}

}
