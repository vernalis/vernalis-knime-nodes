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
package com.vernalis.knime.chem.speedysmiles.nodes.count.diastereomers;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.countPossibleStereoIsomers;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;
import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

/**
 * <code>NodeFactory</code> for the "SmilesHACLargestComponentDesalt" Node. Node
 * to find largest component of SMILES by Heavy Atom Count
 *
 * @author S Roughley
 */
public class StereoisomerCountNodeFactory
		extends NodeFactory<AbstractSpeedySmilesSingleCellManipNodeModel> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractSpeedySmilesSingleCellManipNodeModel createNodeModel() {
		return new AbstractSpeedySmilesSingleCellManipNodeModel() {

			@Override
			protected String getColumnNameSuffix() {
				return "";
			}

			@Override
			protected String getColumnNamePrefix() {
				return "Possible Stereoisomers";
			}

			@Override
			protected DataType getColumnType() {
				return DoubleCell.TYPE;
			}

			@Override
			protected DataCell getResultCell(String SMILES) throws Exception {
				return new DoubleCell(countPossibleStereoIsomers(SMILES));
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
	public NodeView<AbstractSpeedySmilesSingleCellManipNodeModel> createNodeView(
			final int viewIndex, final AbstractSpeedySmilesSingleCellManipNodeModel nodeModel) {
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
