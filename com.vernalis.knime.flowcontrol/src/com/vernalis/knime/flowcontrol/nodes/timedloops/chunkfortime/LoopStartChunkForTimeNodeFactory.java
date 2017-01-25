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
package com.vernalis.knime.flowcontrol.nodes.timedloops.chunkfortime;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractChunkTimedLoopStartNodeModel;

/**
 * <code>NodeFactory</code> for the "LoopStartChunkToTime" Node. Loop start node
 * to chunk table into block and run until a specified time of day has past
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class LoopStartChunkForTimeNodeFactory extends
		NodeFactory<AbstractChunkTimedLoopStartNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractChunkTimedLoopStartNodeModel createNodeModel() {
		return new AbstractChunkTimedLoopStartNodeModel(
				TimedNodeType.RUN_FOR_TIME);
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
	public NodeView<AbstractChunkTimedLoopStartNodeModel> createNodeView(
			final int viewIndex,
			final AbstractChunkTimedLoopStartNodeModel nodeModel) {
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
		return new LoopStartChunkForTimeNodeDialog();
	}

}
