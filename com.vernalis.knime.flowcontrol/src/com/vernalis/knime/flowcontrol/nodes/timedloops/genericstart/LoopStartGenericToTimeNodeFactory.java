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
package com.vernalis.knime.flowcontrol.nodes.timedloops.genericstart;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedNodeType;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractGenericTimedLoopStartNodeModel;
import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog;

/**
 * <code>NodeFactory</code> for the "LoopStartChunkToTime" Node. Loop start node
 * to chunk table into block and run until a specified time of day has past
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 */
public class LoopStartGenericToTimeNodeFactory
		extends NodeFactory<AbstractGenericTimedLoopStartNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractGenericTimedLoopStartNodeModel createNodeModel() {
		return new AbstractGenericTimedLoopStartNodeModel(TimedNodeType.RUN_TO_TIME);
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
	public NodeView<AbstractGenericTimedLoopStartNodeModel> createNodeView(final int viewIndex,
			final AbstractGenericTimedLoopStartNodeModel nodeModel) {
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
		return new AbstractTimedLoopStartNodeDialog(TimedNodeType.RUN_TO_TIME, false);
	}

}
