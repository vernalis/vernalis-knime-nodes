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
package com.vernalis.knime.chem.speedysmiles.nodes.filter.metals;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct.AbstractSpeedySmilesFilterNodeDialog;
import com.vernalis.knime.chem.speedysmiles.nodes.filter.abstrct.AbstractSpeedySmilesFilterNodeModel;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.hasPBlockMetal;

/**
 * <code>NodeFactory</code> for the SpeedySMILES multi-component molecule
 * splitter node
 *
 * @author S Roughley
 */
public class PBlockMetalSplitterNodeFactory
		extends NodeFactory<AbstractSpeedySmilesFilterNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractSpeedySmilesFilterNodeModel createNodeModel() {
		return new AbstractSpeedySmilesFilterNodeModel(true) {

			@Override
			protected boolean rowMatches(String smilesValue) {
				return !hasPBlockMetal(smilesValue);
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
	public NodeView<AbstractSpeedySmilesFilterNodeModel> createNodeView(
			final int viewIndex,
			final AbstractSpeedySmilesFilterNodeModel nodeModel) {
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
		return new AbstractSpeedySmilesFilterNodeDialog();
	}

}
