/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.fingerprint.nodes.convert.fromcountslist;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * @author "Stephen Roughley  knime@vernalis.com"
 *
 */
public class FingerprintFromCountsListNodeFactory extends
		NodeFactory<FingerprintFromCountsListNodeModel> {

	/* (non-Javadoc)
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public FingerprintFromCountsListNodeModel createNodeModel() {
		return new FingerprintFromCountsListNodeModel();
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
	public NodeView<FingerprintFromCountsListNodeModel> createNodeView(int viewIndex,
			FingerprintFromCountsListNodeModel nodeModel) {
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
		return new FingerprintFromCountsListNodeDialog();
	}

}
