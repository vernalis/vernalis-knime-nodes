/*******************************************************************************
 * Copyright (c) 2013, 2014, Vernalis (R&D) Ltd
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
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *  
 *******************************************************************************/
package com.vernalis.nodes.epmc;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "EuroPmcAdvancedSearch" Node.
 * Node to run a reference query on the European Pub Med Central webservice and return the results as an XML table
 *
 * @author SDR
 */
public class EuroPmcAdvancedSearchNodeFactory 
        extends NodeFactory<EuroPmcAdvancedSearchNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public EuroPmcAdvancedSearchNodeModel createNodeModel() {
        return new EuroPmcAdvancedSearchNodeModel();
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
    public NodeView<EuroPmcAdvancedSearchNodeModel> createNodeView(final int viewIndex,
            final EuroPmcAdvancedSearchNodeModel nodeModel) {
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
        return new EuroPmcAdvancedSearchNodeDialog();
    }

}

