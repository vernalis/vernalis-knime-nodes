/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
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
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.RDKit.EBV_Vect;
import org.RDKit.ExplicitBitVect;
import org.RDKit.Int_Vect;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.RWMol;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVector;

/**
 * Utility class containing static methods for converting RDKit types to Java
 * Types
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RdkitTypeConvertors {

	private RdkitTypeConvertors() {
		// Do not instantiate
	}

	/**
	 * Convert a {@link ROMol_Vect} object to an array of SMILES strings. It is
	 * the calling function's responsibility to dispose of the
	 * {@link ROMol_Vect} input.
	 */
	public static String[] molVectToSMILESArray(ROMol_Vect molVect) {
		ROMol tmp = null;
		String[] retVal = new String[(int) molVect.size()];
		for (int i = 0; i < molVect.size(); i++) {
			tmp = molVect.get(i);
			String tmpSmi = tmp.MolToSmiles(true);
			retVal[i] = tmpSmi;
			tmp.delete();
			tmp = null;
		}
		// tmp.delete();
		return retVal;
	}

	/**
	 * Convert a Set of SMARTS Strings to an {@link ROMol_Vect} representation
	 * 
	 * @param SMARTS
	 *            The input set of SMARTS Strings
	 * @return The {@link ROMol_Vect} representation
	 */
	public static ROMol_Vect stringsToROMolVect(Set<String> SMARTS) {
		ROMol_Vect retVal = new ROMol_Vect();
		for (String tmp : SMARTS) {
			// TODO: Does the intermediate RWMol need to be deleted?
			retVal.add(RWMol.MolFromSmarts(tmp));
		}
		return retVal;
	}

	/**
	 * Converter for RDKit {@link ExplicitBitVect} to KNIME
	 * {@link SparseBitVector}. It is the calling function's responsibility to
	 * dispose of the {@link EBV_Vect} input.
	 */
	public static SparseBitVector rdkitFpToSparseBitVector(
			ExplicitBitVect rdkitFingerprint) {
		SparseBitVector retVal = new SparseBitVector(
				rdkitFingerprint.getNumBits());
		for (int bit : intVectToListOfInts(rdkitFingerprint.getOnBits())) {
			retVal.set(bit, true);
		}
		return retVal;
	}

	/**
	 * Converter for RDKit {@link ExplicitBitVect} to KNIME
	 * {@link DenseBitVector}. It is the calling function's responsibility to
	 * dispose of the {@link EBV_Vect} input.
	 */
	public static DenseBitVector rdkitFpToDenseBitVector(
			ExplicitBitVect rdkitFingerprint) {
		if (rdkitFingerprint == null) {
			return null;
		}
		DenseBitVector retVal = new DenseBitVector(
				rdkitFingerprint.getNumBits());
		for (int bit : intVectToListOfInts(rdkitFingerprint.getOnBits())) {
			retVal.set(bit, true);
		}
		return retVal;
	}

	/**
	 * Converter for RDKit {@link Int_Vect} to <code>List&lt;Integer&gt;</code>.
	 * It is the calling function's responsibility to dispose of the
	 * {@link Int_Vect} input.
	 */
	public static List<Integer> intVectToListOfInts(Int_Vect intVect) {
		List<Integer> retVal = new ArrayList<Integer>();
		for (int i = 0; i < intVect.size(); i++) {
			retVal.add(intVect.get(i));
		}
		return retVal;
	}

}
