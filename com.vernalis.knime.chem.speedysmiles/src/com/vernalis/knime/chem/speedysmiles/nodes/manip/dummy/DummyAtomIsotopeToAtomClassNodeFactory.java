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
package com.vernalis.knime.chem.speedysmiles.nodes.manip.dummy;

import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

import com.vernalis.knime.chem.speedysmiles.nodes.abstrct.AbstractSpeedySmilesNodeDialog;
import com.vernalis.knime.chem.speedysmiles.nodes.manip.abstrct.AbstractSpeedySmilesSingleCellManipNodeModel;

import static com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers.dummyAtomIsotopeToAtomLabel;

/**
 * @author s.roughley
 *
 */
public class DummyAtomIsotopeToAtomClassNodeFactory
		extends NodeFactory<AbstractSpeedySmilesSingleCellManipNodeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public AbstractSpeedySmilesSingleCellManipNodeModel createNodeModel() {
		return new AbstractSpeedySmilesSingleCellManipNodeModel(true) {

			@Override
			protected DataCell getResultCell(String smilesValue) {
				return SmilesCellFactory.createAdapterCell(
						dummyAtomIsotopeToAtomLabel(smilesValue));
			}

			@Override
			protected String getColumnNameSuffix() {
				return "";
			}

			@Override
			protected String getColumnNamePrefix() {
				return "Dummy Atom Atom Class-labelled";
			}

			@Override
			protected DataType getColumnType() {
				return SmilesAdapterCell.RAW_TYPE;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<AbstractSpeedySmilesSingleCellManipNodeModel> createNodeView(
			int viewIndex,
			AbstractSpeedySmilesSingleCellManipNodeModel nodeModel) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractSpeedySmilesNodeDialog(true, false);
	}

}
