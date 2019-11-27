/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.pmi.nodes.props.pmiderived;

import java.util.List;

import com.vernalis.knime.chem.pmi.props.PMI;
import com.vernalis.knime.chem.pmi.props.PrincipalMomentOfInertiaDerivedProperties;
import com.vernalis.knime.chem.util.points.AbstractPoint;
import com.vernalis.knime.chem.util.points.PointFactory;
import com.vernalis.knime.core.nodes.props.abstrct.AbstractMoleculePropertyCalcNodeFactory;
import com.vernalis.knime.core.nodes.props.abstrct.AbstractPointListMoleculePropertyCalcNodeModel;

/**
 * Node Factory implementation for the PMI-Derived properties node
 * 
 * @author s.roughley
 * 
 */
public class PmiDerivedPropsNodeFactory extends
		AbstractMoleculePropertyCalcNodeFactory<AbstractPointListMoleculePropertyCalcNodeModel<PMI, Double>, PMI> {

	/**
	 * Constructor
	 */
	public PmiDerivedPropsNodeFactory() {
		super(PrincipalMomentOfInertiaDerivedProperties.values(), true, true,
				true, false, true, true,
				"Principal Moment of Intertia (PMI)-Derived Properties",
				"PMI_icon2.png");
	}

	@Override
	public AbstractPointListMoleculePropertyCalcNodeModel<PMI, Double> createNodeModel() {
		return new AbstractPointListMoleculePropertyCalcNodeModel<PMI, Double>(
				getPropertyFilterTitle(), getProperties(),
				getAcceptedColumnsFilter(),
				PointFactory.elementSymbolFromPdbAtom
						.andThen(PointFactory.elementWeightFromSymbol),
				PointFactory.elementSymbolFromMolAtom
						.andThen(PointFactory.elementWeightFromSymbol)) {

			@Override
			protected PMI getObjFromPointList(
					List<AbstractPoint<Double>> points) {
				return PMI.fromPoints(points);
			}

		};

	}

}
