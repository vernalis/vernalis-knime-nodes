/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.io.nodes.load.mol;

import org.knime.chem.types.MolCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog;
import com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeModel;

/**
 * <code>NodeFactory</code> for the Load MOL Files node
 * 
 * @author S. Roughley
 */
public class LoadLocalMolFilesNodeFactory extends NodeFactory<AbstractLoadFilesNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractLoadFilesNodeModel createNodeModel() {
		return new AbstractLoadFilesNodeModel(MolCellFactory.TYPE, "mol Files") {

			@Override
			protected DataCell getDataCellFromString(String fileContentString) {
				return MolCellFactory.create(fileContentString);
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
	public NodeView<AbstractLoadFilesNodeModel> createNodeView(final int viewIndex,
			final AbstractLoadFilesNodeModel nodeModel) {
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
		return new AbstractLoadFilesNodeDialog(this.getClass().getName(), "mol");
	}

}
