/*******************************************************************************
 * Copyright (C) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 ******************************************************************************/
package com.vernalis.pdbconnector;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.pdbconnector.nodes.pdbconnector.PdbConnectorNodeFactory2;

/**
 * PdbConnectorNode factory class.
 * 
 * @deprecated Use {@link PdbConnectorNodeFactory2}
 */
@Deprecated
public class PdbConnectorNodeFactory extends NodeFactory<PdbConnectorNodeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public PdbConnectorNodeModel createNodeModel() {

        return new PdbConnectorNodeModel(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<PdbConnectorNodeModel> createNodeView(final int viewIndex,
			final PdbConnectorNodeModel nodeModel) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {

        return new PdbConnectorNodeDialog(null);
	}

}
