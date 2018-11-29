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
package com.vernalis.knime.io.nodes.load.sdf;

import java.io.BufferedReader;
import java.util.Arrays;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeView;

import com.vernalis.io.MultilineTextObjectReader;
import com.vernalis.knime.io.nodes.abstrct.AbstractMultiLineObjectLoadFilesNodeDialog;
import com.vernalis.knime.io.nodes.abstrct.AbstractMultiLineObjectLoadFilesNodeModel;

/**
 * <code>NodeFactory</code> for the load Amix Peak Lists Node.
 * 
 * @author S. Roughley
 */
public class LoadSdFilesNodeFactory
		extends NodeFactory<AbstractMultiLineObjectLoadFilesNodeModel<Sdfile>> {

	private static final Sdfile SDFILE = new Sdfile();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMultiLineObjectLoadFilesNodeModel<Sdfile> createNodeModel() {
		return new AbstractMultiLineObjectLoadFilesNodeModel<Sdfile>(SDFILE) {

			@Override
			protected MultilineTextObjectReader<Sdfile> getObjectReader(
					BufferedReader br) {
				return new SdfileReader(br);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.misc.io.nodes.abstrct.
			 * AbstractMultiLineObjectLoadFilesNodeModel#
			 * loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)
			 */
			@Override
			protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
					throws InvalidSettingsException {
				super.loadValidatedSettingsFrom(settings);
				try {
					m_selectedPropertiesModel.loadSettingsFrom(settings);
				} catch (Exception e) {
					// Legacy version did not contain mol block
					// Load up the default set
					m_selectedPropertiesModel.setStringArrayValue(
							Arrays.stream(nonReadableObject.getNewColumnSpecs())
									.map(spec -> spec.getName())
									.filter(name -> !"Mol Block".equals(name))
									.toArray(String[]::new));
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<AbstractMultiLineObjectLoadFilesNodeModel<Sdfile>> createNodeView(
			final int viewIndex,
			final AbstractMultiLineObjectLoadFilesNodeModel<Sdfile> nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return new AbstractMultiLineObjectLoadFilesNodeDialog<>(SDFILE,
				this.getClass().getName(), ".sd|.sdf|.sd.gz|.sdf.gz");
	}

}
