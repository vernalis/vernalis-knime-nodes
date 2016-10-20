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
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.RDKit.ExplicitBitVect;
import org.RDKit.Int_Vect;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.RWMol;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.data.vector.bitvector.SparseBitVector;

/**
 * Class providing utility methods around the use of RDKit
 * 
 * @author s.roughley
 * 
 */
public class RDKitUtils {
	private RDKitUtils() {
		// Dont instantiate
	}

	/**
	 * Convertor for RDKit {@link ExplicitBitVect} to KNIME
	 * {@link SparseBitVector}
	 */
	public static SparseBitVector rdkitFpToSparseBitVector(ExplicitBitVect rdkitFingerprint){
		SparseBitVector retVal = new SparseBitVector(
				rdkitFingerprint.getNumBits());
		for (int bit : intVectToListOfInts(rdkitFingerprint.getOnBits())) {
			retVal.set(bit, true);
		}
		return retVal;
	}

	/**
	 * Convertor for RDKit {@link ExplicitBitVect} to KNIME
	 * {@link DenseBitVector}
	 */
	public static DenseBitVector rdkitFpToDenseBitVector(
			ExplicitBitVect rdkitFingerprint) {
		DenseBitVector retVal = new DenseBitVector(
				rdkitFingerprint.getNumBits());
		for (int bit : intVectToListOfInts(rdkitFingerprint.getOnBits())) {
			retVal.set(bit, true);
		}
		return retVal;
	}

	/** Convertor for RDKit {@link Int_Vect} to <code>List&lt;Integer&gt;</code> */
	public static List<Integer> intVectToListOfInts(Int_Vect intVect) {
		List<Integer> retVal = new ArrayList<Integer>();
		for (int i = 0; i < intVect.size(); i++) {
			retVal.add(intVect.get(i));
		}
		return retVal;
	}

	/**
	 * Function to convert the SMIRKS representation, with attachment points
	 * labelled by isotopic value (e.g. [2*]), to Reaction SMARTS, with
	 * attachment points labelled by atom label (e.g. ([*:2]). Unlabelled
	 * attachments are labelled '1'.
	 * 
	 * @param SMIRKS
	 *            The isotopically labelled reaction SMIRKS
	 * @return The Reaction SMARTS with correct attachment point labelling
	 */
	public static String convertSmirksToReactionSmarts(String SMIRKS) {
		String retVal = SMIRKS.replaceAll("\\[([0-9]+)\\*\\]", "[*:$1]");
		// String retVal = SMIRKS;
		return retVal.replace("[*]", "[*:1]");
	}

	/**
	 * Methods to validate reaction SMARTS
	 * <ul>
	 * <li>'>>' is required to separate reactants and products</li>
	 * <li>Products require '[*]' to occur twice, for the attachment points (the
	 * node will handle the tagging of these)</li>
	 * <li>Reactants and products require exactly two atom mappings, e.g. :1]
	 * and :2] (other values could be used).</li>
	 * <li>The atom mappings must be two different values</li>
	 * <li>The same atom mappings must be used for reactants and products</li>
	 * </ul>
	 * 
	 * @param rSMARTS
	 *            The Reaction SMARTS to validate
	 * @return <code>null</code> if the SMARTS was validated, otherwise an error
	 *         message informing of the first problem encountered
	 */
	public static String validateReactionSmarts(String rSMARTS) {
		// Firstly, it must contain ">>" - 10 is [*:1]-[*:2]
		if (rSMARTS.indexOf(">>") < 10) {
			return "rSMARTS must contain the substring '>>' "
					+ "to separate reactants and products";
		}

		// Needs to also contain 2 matching pairs of atom indices before and
		// after '>>', and 2 unmapped attachment points [*]
		String reactants = rSMARTS.split(">>")[0];

		String products = rSMARTS.split(">>")[1];
		if (products.split("\\[\\*\\]").length != 2) {
			return "rSMARTS products need exactly two unmapped attachment points, "
					+ "of the form '[*]' for correct tagging";
		}

		Pattern idMatch = Pattern.compile(".*:(\\d+)\\].*:(\\d+)\\].*");
		Matcher m = idMatch.matcher(reactants);
		int rctMapId_0, rctMapId_1;
		if (m.find()) {
			try {
				rctMapId_0 = Integer.parseInt(m.group(1));
				rctMapId_1 = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
				return "rSMARTS reactants need exactly two mapped atoms, "
						+ "of the form '[{atom match}:n]', where n is a number";
			}
		} else {
			return "rSMARTS reactants need exactly two mapped atoms, "
					+ "of the form '[{atom match}:n]', where n is a number";
		}
		if (rctMapId_0 == rctMapId_1) {
			return "rSMARTS reactants need exactly two *differently* mapped atoms, "
					+ "of the form '[{atom match}:n]', where n is a number (e.g. 1 and 2)";
		}

		m = idMatch.matcher(products);
		int prodMapId_0, prodMapId_1;
		if (m.find()) {
			try {
				prodMapId_0 = Integer.parseInt(m.group(1));
				prodMapId_1 = Integer.parseInt(m.group(2));
			} catch (NumberFormatException e) {
				return "rSMARTS products need exactly two mapped atoms, "
						+ "of the form '[{atom match}:n]', where n is a number";
			}
		} else {
			return "rSMARTS products need exactly two mapped atoms, "
					+ "of the form '[{atom match}:n]', where n is a number";
		}
		if (prodMapId_0 == prodMapId_1) {
			return "rSMARTS products need exactly two *differently* mapped atoms, "
					+ "of the form '[{atom match}:n]', where n is a number (e.g. 1 and 2)";
		}
		// finally, check the same indices occur on both sides.
		if ((rctMapId_0 != prodMapId_0 && rctMapId_0 != prodMapId_1)
				|| (rctMapId_1 != prodMapId_0 && rctMapId_1 != prodMapId_1)) {
			return "rSMARTS mapping indices need to be the same for reactants (Here: "
					+ rctMapId_0
					+ ", "
					+ rctMapId_1
					+ ") and products (Here: "
					+ prodMapId_0 + ", " + prodMapId_1 + ")";
		}

		// We got there!
		return null;
	}

	/** Convert a {@link ROMol_Vect} object to an array of SMILES strings. */
	public static String[] molVectToSMILESArray(ROMol_Vect molVect) {
		ROMol tmp = null;
		String[] retVal = new String[(int) molVect.size()];
		for (int i = 0; i < molVect.size(); i++) {
			tmp = molVect.get(i);
			String tmpSmi = tmp.MolToSmiles(true);
			retVal[i] = tmpSmi;
		}
		tmp.delete();
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
			retVal.add(RWMol.MolFromSmarts(tmp));
		}
		return retVal;
	}
}
