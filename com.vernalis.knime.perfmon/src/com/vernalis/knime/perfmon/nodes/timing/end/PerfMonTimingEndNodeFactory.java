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
package com.vernalis.knime.perfmon.nodes.timing.end;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingEndNodeModel;

/**
 * <code>NodeFactory</code> for the "TimingStart" Node.
 * Loop start for execution timing
 *
 * @author S. Roughley
 */
public class PerfMonTimingEndNodeFactory 
 extends
		NodeFactory<AbstractPerfMonTimingEndNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
	public AbstractPerfMonTimingEndNodeModel createNodeModel() {
		return new AbstractPerfMonTimingEndNodeModel(BufferedDataTable.TYPE, 1);
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
	public NodeView<AbstractPerfMonTimingEndNodeModel> createNodeView(
			final int viewIndex,
			final AbstractPerfMonTimingEndNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }

}

