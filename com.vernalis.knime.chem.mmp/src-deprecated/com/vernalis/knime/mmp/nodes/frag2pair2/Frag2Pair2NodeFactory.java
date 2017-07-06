/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.frag2pair2;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * Node Factory class for the Fragment to MMP node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class Frag2Pair2NodeFactory extends NodeFactory<Frag2Pair2NodeModel> {

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public Frag2Pair2NodeModel createNodeModel() {
		return new Frag2Pair2NodeModel();
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeView(int, org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<Frag2Pair2NodeModel> createNodeView(int viewIndex,
			Frag2Pair2NodeModel nodeModel) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new Frag2Pair2NodeDialog();
	}

}
