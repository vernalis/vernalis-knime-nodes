/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.wait.fortime;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;
import com.vernalis.knime.flowcontrol.nodes.wait.abstrct.AbstractWaitNodeModel;

/**
 * <code>NodeFactory</code> for the Wait For Time node
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class WaitForTimeNodeFactory extends NodeFactory<AbstractWaitNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractWaitNodeModel createNodeModel() {
		return new AbstractWaitNodeModel(TimedNodeType.RUN_FOR_TIME);
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
	public NodeView<AbstractWaitNodeModel> createNodeView(final int viewIndex,
			final AbstractWaitNodeModel nodeModel) {
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
		return new WaitForTimeNodeDialog();
	}

}
