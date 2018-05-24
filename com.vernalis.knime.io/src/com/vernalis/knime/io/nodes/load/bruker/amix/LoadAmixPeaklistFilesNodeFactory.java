/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.io.nodes.load.bruker.amix;

import java.io.BufferedReader;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.io.MultilineTextObjectReader;
import com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog;
import com.vernalis.knime.io.nodes.abstrct.AbstractMultiLineObjectLoadFilesNodeModel;

/**
 * <code>NodeFactory</code> for the load Amix Peak Lists Node.
 * 
 * @author S. Roughley
 */
public class LoadAmixPeaklistFilesNodeFactory extends
		NodeFactory<AbstractMultiLineObjectLoadFilesNodeModel<AmixPeakList>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractMultiLineObjectLoadFilesNodeModel<AmixPeakList> createNodeModel() {
		return new AbstractMultiLineObjectLoadFilesNodeModel<AmixPeakList>(
				new AmixPeakList()) {

			@Override
			protected MultilineTextObjectReader<AmixPeakList> getObjectReader(
					BufferedReader br) {
				return new AmixPeakListReader(br);
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
	public NodeView<AbstractMultiLineObjectLoadFilesNodeModel<AmixPeakList>> createNodeView(
			final int viewIndex,
			final AbstractMultiLineObjectLoadFilesNodeModel<AmixPeakList> nodeModel) {
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
		return new AbstractLoadFilesNodeDialog(this.getClass().getName(),
				".txt");
	}

}
