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
 *******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.count.dummyatoms;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;
import com.vernalis.knime.chem.speedysmiles.nodes.count.abstrct.AbstractSpeedySmilesSingleCountNodeModel;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.countDummyAtoms;

/**
 * <code>NodeFactory</code> for the SpeedySMILES HAC Node
 *
 * @author S Roughley
 */
public class SmilesDummyAtomCountNodeFactory
		extends NodeFactory<AbstractSpeedySmilesSingleCountNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractSpeedySmilesSingleCountNodeModel createNodeModel() {
		return new AbstractSpeedySmilesSingleCountNodeModel() {

			@Override
			protected Integer getResultCount(String smilesValue) {
				return countDummyAtoms(smilesValue);
			}

			@Override
			protected String getColumnNameSuffix() {
				return "";
			}

			@Override
			protected String getColumnNamePrefix() {
				return "Dummy Atom Count";
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
	public NodeView<AbstractSpeedySmilesSingleCountNodeModel> createNodeView(
			final int viewIndex,
			final AbstractSpeedySmilesSingleCountNodeModel nodeModel) {
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
		return new AbstractSpeedySmilesNodeDialog();
	}

}
