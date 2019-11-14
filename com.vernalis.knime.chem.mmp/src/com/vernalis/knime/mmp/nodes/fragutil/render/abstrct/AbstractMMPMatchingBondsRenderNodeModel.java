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
package com.vernalis.knime.mmp.nodes.fragutil.render.abstrct;

import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.ClosedFactoryException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractColumnRearrangerFragmentationFactoryNodeModel;

/**
 * The abstract node model for the the render matching bonds nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPMatchingBondsRenderNodeModel<T, U>
		extends AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U> {
	private final SettingsModelColor m_bondColour =
			AbstractMMPMatchingBondsRenderNodeDialog.createBondColourModel();

	/**
	 * Constructor
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 */
	AbstractMMPMatchingBondsRenderNodeModel(FragmentationUtilsFactory<T, U> fragUtilityFactory) {
		super(fragUtilityFactory, false, false, false, false, true,
				new String[] { "Matching Bonds", "Number of Matching Bonds" },
				new DataType[] { fragUtilityFactory.getRendererType(), IntCell.TYPE });
	}

	@Override
	protected DataCell[] getResultColumns(T mol, MoleculeFragmentationFactory2<T, U> fragFactory,
			long rowIndex, int length) throws RowExecutionException {
		try {
			if (m_AddHs.getBooleanValue()) {
				// We need explicit H's, but we also need to use a bit of
				// trickery to ensure we show all matching bonds
				fragFactory.close();
				fragFactory = fragUtilityFactory.createFragmentationFactory(
						fragUtilityFactory.createHAddedMolecule(mol, rowIndex), matcher, false,
						false, verboseLogging, false, 0, 0.0, 0);
			}
			return new DataCell[] { fragFactory.renderMatchingBonds(m_bondColour.getColorValue()),
					new IntCell(fragFactory.getMatchingBonds().size()) };
		} catch (ClosedFactoryException | IOException | ToolkitException e) {
			throw new RowExecutionException("Error evaluting row", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragutil.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#saveSettingsTo(org.knime.core.
	 * node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_bondColour.saveSettingsTo(settings);
		super.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragutil.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#loadValidatedSettingsFrom(org.
	 * knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_bondColour.loadSettingsFrom(settings);
		super.loadValidatedSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragutil.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#validateSettings(org.knime.core.
	 * node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		m_bondColour.validateSettings(settings);
		super.validateSettings(settings);
	}
}
