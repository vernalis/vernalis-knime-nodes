/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.knime.perfmon.nodes.loopstart.timing;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingStartNodeDialog;
import com.vernalis.knime.perfmon.nodes.timing.abstrct.AbstractPerfMonTimingStartNodeModel;

/**
 * <code>NodeFactory</code> for the "TimingStart" Node.
 * Loop start for execution timing
 *
 * @author S. Roughley
 */
public class PerfMonTiming3PortStartNodeFactory 
        extends NodeFactory<AbstractPerfMonTimingStartNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPerfMonTimingStartNodeModel createNodeModel() {
		return new AbstractPerfMonTimingStartNodeModel(BufferedDataTable.TYPE,
				3);
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
    public NodeView<AbstractPerfMonTimingStartNodeModel> createNodeView(final int viewIndex,
            final AbstractPerfMonTimingStartNodeModel nodeModel) {
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
		return new AbstractPerfMonTimingStartNodeDialog();
    }

}

