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
package com.vernalis.knime.core.nodes.props.abstrct;

import java.util.List;
import java.util.function.Function;

import org.knime.bio.types.PdbValue;
import org.knime.chem.types.CtabValue;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.util.points.AbstractPoint;
import com.vernalis.knime.chem.util.points.PointFactory;
import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;

/**
 *
 * Abstract class where an List of Points is the basis for the object used for
 * calculation. Implementing subclasses need only implement
 * {@link #getObjFromPointList(List)}. Z-coordinate checking is always active
 *
 * @author S.Roughley
 *
 * @param <T>
 *            The Type parameter for calculation object
 * @param <U>
 *            The Type parameter for the additional point data parameter
 */
public abstract class AbstractPointListMoleculePropertyCalcNodeModel<T, U>
extends AbstractMoleculePropertyCalcNodeModel<T> {

	protected Function<String, U> pdbPropertyFunction;
	protected Function<String, U> molPropertyFunction;

	/**
	 * Overloaded constructor with {@code null} for the PDB and Mol property
	 * functions
	 *
	 * @param propertyLabel
	 *            The name properties label to be used as the settings model key
	 * @param possibleProps
	 *            The possible properties to calculate
	 * @param acceptedColumns
	 *            The acceptable input column filter
	 */
	protected AbstractPointListMoleculePropertyCalcNodeModel(
			String propertyLabel,
			CalculatedPropertyInterface<T>[] possibleProps,
			ColumnFilter acceptedColumns) {
		this(propertyLabel, possibleProps, acceptedColumns, null, null);
	}

	/**
	 * Full constructor
	 *
	 * @param propertyLabel
	 *            The name properties label to be used as the settings model key
	 * @param possibleProps
	 *            The possible properties to calculate
	 * @param acceptedColumns
	 *            The acceptable input column filter
	 * @param pdbPropertyFunction
	 *            Function to get the point parameter from the atom line string
	 *            in the PDB input
	 * @param molPropertyFunction
	 *            Function to get the point parameter from the atom line string
	 *            in the mol block input
	 */
	protected AbstractPointListMoleculePropertyCalcNodeModel(
			String propertyLabel,
			CalculatedPropertyInterface<T>[] possibleProps,
			ColumnFilter acceptedColumns,
			Function<String, U> pdbPropertyFunction,
			Function<String, U> molPropertyFunction) {
		super(propertyLabel, possibleProps, acceptedColumns, true);
		this.pdbPropertyFunction = pdbPropertyFunction;
		this.molPropertyFunction = molPropertyFunction;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vernalis.knime.core.nodes.props.abstrct.
	 * AbstractMoleculePropertyCalcNodeModel#checkMolZCoord(java.lang.Object)
	 */
	@Override
	protected boolean checkMolZCoord(T mol) {
		// We handle this in #getPointListFromCell()
		return false;
	}

	/**
	 * This method should be used to obtain the calculation object of type T from
	 * the intermediate point list
	 *
	 * @param points The list of points from the incoming molecule
	 * @return The calculation object
	 * @throws RowExecutionException
	 */
	protected abstract T getObjFromPointList(List<AbstractPoint<U>> points) throws RowExecutionException;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.vernalis.knime.core.nodes.props.abstrct.
	 * AbstractMoleculePropertyCalcNodeModel#getMolFromCell(org.knime.core.data.
	 * DataCell)
	 */
	@Override
	protected T getMolContainerFromCell(DataCell molCell) throws RowExecutionException {
		return getObjFromPointList(getPointListFromCell(molCell));
	}

	private final List<AbstractPoint<U>> getPointListFromCell(
			DataCell molCell) {
		// Dont handle Mol2 or SMILES
		final DataType type = molCell.getType();
		List<AbstractPoint<U>> retVal = null;
		if (type.isCompatible(PdbValue.class)) {
			retVal = PointFactory.getPointsFromPDBString((PdbValue) molCell,
					pdbPropertyFunction);
		} else if (type.isCompatible(MolValue.class)) {
			retVal = PointFactory.getPointsFromMolString((MolValue) molCell,
					molPropertyFunction);
		} else if (type.isCompatible(SdfValue.class)) {
			// This will pick up ROMol too...
			retVal = PointFactory.getPointsFromMolString((SdfValue) molCell,
					molPropertyFunction);
		} else if (type.isCompatible(CtabValue.class)) {
			retVal = PointFactory.getPointsFromCTabString((CtabValue) molCell,
					molPropertyFunction);
		}
		if (retVal != null && checkZCoordsfor3D && !hasNonZeroZ) {
			hasNonZeroZ |=
					retVal.stream().mapToDouble(pt -> Math.abs(pt.getZ()))
					.anyMatch(z -> z > MAX_ZERO_Z);
		}

		return retVal;
	}
}
