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
package com.vernalis.knime.chem.pmi.util.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.RDKit.Conformer;
import org.RDKit.Point3D;
import org.RDKit.ROMol;
import org.RDKit.Transform3D;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.util.math.Cubic;
import com.vernalis.knime.util.math.TransformUtil;

/**
 * A convenience class to provide various static methods used by some or all of
 * the PMI nodes
 * 
 * @author S. Roughley knime@vernalis.com
 * 
 */
public abstract class PmiUtils {

	public static final String CFG_PMIs = "Report_PMIs";
	public static final String CFG_nPMIs = "Report_Normalised_PMIs";

	/** Create the return PMIs settings model */
	public static final SettingsModelBoolean createPmisModel() {
		return new SettingsModelBoolean(CFG_PMIs, false);
	}

	/** Create the return normalised PMIs settings model */
	public static final SettingsModelBoolean createNormalisedPmisModel() {
		return new SettingsModelBoolean(CFG_nPMIs, true);
	}

	/**
	 * Check whether a column is selected. If not, or the given name does not
	 * exist in the {@link DataTableSpec}, then the last column of the require
	 * type (Mol or Sdf) is selected. If a column is selected, then a check is
	 * made to ensure that it is of the correct type.
	 * 
	 * @param colName
	 *            The name of the selected column
	 * @param tableSpec
	 *            The {@link DataTableSpec} of the input table
	 * @param logger
	 *            {@link NodeLogger} instance to record output
	 * @return The name of a selected column of the correct type
	 * @throws InvalidSettingsException
	 *             Thrown is the pre-selected column is not a Mol/Sdf column, or
	 *             if no column of these types was found.
	 */
	public static String checkColumnNameAndAutoPick(String colName,
			DataTableSpec tableSpec, NodeLogger logger)
			throws InvalidSettingsException {
		// Check the selection for the sdf or mol column
		DataColumnSpec colSpec = tableSpec.getColumnSpec(colName);

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = tableSpec.getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (tableSpec.getColumnSpec(i).getType()
						.isCompatible(MolValue.class)
						|| tableSpec.getColumnSpec(i).getType()
								.isCompatible(SdfValue.class)) {
					// We select this column
					logger.warn("No column selected. "
							+ tableSpec.getColumnSpec(i).getName()
							+ " auto-selected.");
					return tableSpec.getColumnSpec(i).getName();
				}
			}
			// If we are here then no suitable column found
			logger.error("No molecule column of the accepted"
					+ " input formats (SDF, Mol) was found.");
			throw new InvalidSettingsException(
					"No molecule column of the accepted"
							+ " input formats (SDF, Mol) was found.");

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(MolValue.class)
					&& !colSpec.getType().isCompatible(SdfValue.class)) {
				// The column is not compatible with one of the accepted types
				logger.error("The column " + colName
						+ " is not one of the accepted" + " input formats");
				throw new InvalidSettingsException("The column " + colName
						+ " is not one of the accepted" + " input formats");
			}
			// otherwise, the column name is ok
			return colName;
		}
	}

	/**
	 * Calculate the 3 PMIs in ascending order from an ROMol
	 * 
	 * @param mol
	 *            The molecule
	 * @return The 3 PMIs in ascending order
	 */
	public static List<Double> getPMIs(ROMol mol) {

		// First we need to find the centre of gravity
		Point3D geoCentroid = getCentreOfGravity(mol);

		// Now the inertial Tensor:
		double[][] inertialTensor = getInertialTensor(mol, geoCentroid);
		geoCentroid.delete();

		// And now the PMIs
		List<Double> PMIs = calcPMIs(mol, inertialTensor);

		while (PMIs.size() < 3) {
			// Duplicate the first element of the list
			PMIs.add(PMIs.get(0));
		}
		// Sort Low to High
		Collections.sort(PMIs);
		return PMIs;
	}

	/**
	 * Hidden method to actually calculate the PMIs from the inertial tensor
	 * 
	 * @param mol
	 *            The molecule
	 * @param inertialTensor
	 *            The inertial tensor
	 * @return The PMIs
	 */
	protected static List<Double> calcPMIs(ROMol mol,
			double[][] inertialTensor) {
		List<Double> PMIs = new ArrayList<>();

		Double a = -1.0 * (inertialTensor[0][0] + inertialTensor[1][1]
				+ inertialTensor[2][2]);
		Double b = inertialTensor[0][0] * inertialTensor[1][1]
				+ inertialTensor[0][0] * inertialTensor[2][2]
				+ inertialTensor[1][1] * inertialTensor[2][2]
				- inertialTensor[0][1] * inertialTensor[0][1]
				- inertialTensor[0][2] * inertialTensor[0][2]
				- inertialTensor[1][2] * inertialTensor[1][2];
		Double c = inertialTensor[0][0] * inertialTensor[1][2]
				* inertialTensor[1][2]
				+ inertialTensor[0][1] * inertialTensor[0][1]
						* inertialTensor[2][2]
				+ inertialTensor[0][2] * inertialTensor[0][2]
						* inertialTensor[1][1]
				- 2 * inertialTensor[0][1] * inertialTensor[1][2]
						* inertialTensor[0][2]
				- inertialTensor[0][0] * inertialTensor[1][1]
						* inertialTensor[2][2];
		Cubic cubic = new Cubic(a, b, c);
		PMIs = cubic.getRoots();
		return PMIs;
	}

	/**
	 * Hidden method to calculate the inertial tensor
	 * 
	 * @param mol
	 *            the molecule
	 * @param geoCentroid
	 *            The centre of gravity
	 * @return The inertial tensor
	 */
	protected static double[][] getInertialTensor(ROMol mol,
			Point3D geoCentroid) {
		Conformer conf = mol.getConformer();
		double Ixx = 0, Iyy = 0, Izz = 0, Ixy = 0, Ixz = 0, Iyz = 0;
		for (long i = 0; i < conf.getNumAtoms(); i++) {
			Double atomMass = mol.getAtomWithIdx(i).getMass();
			Point3D coords = conf.getAtomPos(i).minus(geoCentroid);
			Ixx += atomMass * (coords.getY() * coords.getY()
					+ coords.getZ() * coords.getZ());
			Iyy += atomMass * (coords.getX() * coords.getX()
					+ coords.getZ() * coords.getZ());
			Izz += atomMass * (coords.getX() * coords.getX()
					+ coords.getY() * coords.getY());
			Ixy -= atomMass * coords.getX() * coords.getY();
			Ixz -= atomMass * coords.getX() * coords.getZ();
			Iyz -= atomMass * coords.getY() * coords.getZ();
			coords.delete();
		}
		double[][] retVal = new double[3][3];
		retVal[0][0] = Ixx;
		retVal[1][1] = Iyy;
		retVal[2][2] = Izz;
		retVal[0][1] = Ixy;
		retVal[1][0] = Ixy;
		retVal[0][2] = Ixz;
		retVal[2][0] = Ixz;
		retVal[1][2] = Iyz;
		retVal[2][1] = Iyz;
		return retVal;
	}

	/**
	 * Method to calculate the centre of gravity
	 * 
	 * @param mol
	 *            The molecule
	 * @return The centre of gravity
	 */
	public static Point3D getCentreOfGravity(ROMol mol) {
		Double[] cOfG = new Double[] { 0.0, 0.0, 0.0 };
		Double M = 0.0;
		Conformer conf = mol.getConformer();

		for (long i = 0; i < conf.getNumAtoms(); i++) {
			Double atomMass = mol.getAtomWithIdx(i).getMass();
			Point3D coords = conf.getAtomPos(i);
			cOfG[0] += coords.getX() * atomMass;
			cOfG[1] += coords.getY() * atomMass;
			cOfG[2] += coords.getZ() * atomMass;
			M += atomMass;
		}
		Point3D geoCentroid =
				new Point3D(cOfG[0] / M, cOfG[1] / M, cOfG[2] / M);
		return geoCentroid;
	}

	/**
	 * Method to align a molecule to it's principal inertial axes
	 * 
	 * @param mol
	 *            The molecule
	 */
	public static void alignToPrincipalAxes(ROMol mol) {

		// Build the transforms
		// Start with the translation to the centre of gravity
		Point3D cOfG = getCentreOfGravity(mol);

		// Find the rotation
		double[][] inertialTensor = getInertialTensor(mol, cOfG);
		double[] rotationMatrix =
				TransformUtil.makeRotationMatrix(inertialTensor);

		// Now set up the transforms (NB RDKit does rotate-then-transform so do
		// separately
		// Translation first - need to negate the C of G for the
		// transform
		cOfG.setX(-cOfG.getX());
		cOfG.setY(-cOfG.getY());
		cOfG.setZ(-cOfG.getZ());
		Transform3D trans = new Transform3D();
		trans.SetTranslation(cOfG);
		mol.getConformer().transformConformer(trans);
		trans.delete();

		// Now do the rotation - this is the easiest method to set it up
		Transform3D rot = new Transform3D();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				rot.setVal(i, j, rotationMatrix[3 * i + j]);
			}
		}
		mol.getConformer().transformConformer(rot);
		rot.delete();
		cOfG.delete();
	}

}
