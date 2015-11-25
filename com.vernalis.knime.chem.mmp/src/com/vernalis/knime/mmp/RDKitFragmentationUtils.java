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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.RDKit.Atom;
import org.RDKit.Atom.ChiralType;
import org.RDKit.Bond.BondType;
import org.RDKit.Bond_Vect;
import org.RDKit.ChemicalReaction;
import org.RDKit.Match_Vect;
import org.RDKit.Match_Vect_Vect;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.node.NodeModel;

import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

/**
 * Class providing utility methods around the use of RDKit for fragmentation
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RDKitFragmentationUtils {
	private RDKitFragmentationUtils() {
		// Dont instantiate
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
			return "rSMARTS mapping indices need to be the same for reactants (Here: " + rctMapId_0
					+ ", " + rctMapId_1 + ") and products (Here: " + prodMapId_0 + ", "
					+ prodMapId_1 + ")";
		}

		// We got there!
		return null;
	}

	/**
	 * <p>
	 * Method to cut all bonds in inMol with match bondMatch
	 * </p>
	 * <p>
	 * The bond is removed, and the stub atoms are marked by setting their
	 * isotope to 500. If an atom is the stub atom more than once, then its
	 * isotope is set to 1000
	 * </p>
	 * 
	 * @param inMol
	 *            The {@link ROMol} test molecule
	 * @param bondMatch
	 *            The {@link ROMol} query molecule to match a single bond
	 * @return A Single String for the multicomponent smiles of the bond-deleted
	 *         molecule
	 */
	public static String cutAllMatchingBonds(ROMol inMol, ROMol bondMatch) {
		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();

		RWMol editableMol = swigGC.markForCleanup(new RWMol(inMol));
		Match_Vect_Vect bondMatches = swigGC.markForCleanup(inMol.getSubstructMatches(bondMatch));
		for (int i = 0; i < bondMatches.size(); i++) {
			Match_Vect bond = swigGC.markForCleanup(bondMatches.get(i));
			// Remove the breakable bond
			editableMol.removeBond(swigGC.markForCleanup(bond.get(0)).getSecond(),
					swigGC.markForCleanup(bond.get(1)).getSecond());
			// Mark the atoms at the end with isotope 500 or 1000
			Atom at = swigGC.markForCleanup(
					editableMol.getAtomWithIdx(swigGC.markForCleanup(bond.get(0)).getSecond()));
			if (at.getIsotope() < 500) {
				at.setIsotope(500);
			} else {
				at.setIsotope(1000);
			}
			at = swigGC.markForCleanup(
					editableMol.getAtomWithIdx(swigGC.markForCleanup(bond.get(1)).getSecond()));
			if (at.getIsotope() < 500) {
				at.setIsotope(500);
			} else {
				at.setIsotope(1000);
			}
		}
		String retVal = editableMol.MolToSmiles(true);
		swigGC.cleanupMarkedObjects();
		return retVal;

	}

	/**
	 * <p>
	 * Method to mark all bonds in inMol with match bondMatch
	 * </p>
	 * <p>
	 * The atoms flanking the matching bonds are marked by setting their isotope
	 * to 500
	 * </p>
	 * 
	 * @param inMol
	 *            The {@link ROMol} test molecule
	 * @param bondMatch
	 *            The {@link ROMol} query molecule to match a single bond
	 * @return A Single String for the smiles of the bond-marked molecule
	 */
	public static String markAllMatchingBonds(ROMol inMol, ROMol bondMatch) {
		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();

		RWMol editableMol = swigGC.markForCleanup(new RWMol(inMol));
		Match_Vect_Vect bondMatches = swigGC.markForCleanup(inMol.getSubstructMatches(bondMatch));
		for (int i = 0; i < bondMatches.size(); i++) {
			Match_Vect bond = swigGC.markForCleanup(bondMatches.get(i));
			// Mark the atoms at the end with isotope 500
			swigGC.markForCleanup(
					editableMol.getAtomWithIdx(swigGC.markForCleanup(bond.get(0)).getSecond()))
					.setIsotope(500);
			swigGC.markForCleanup(
					editableMol.getAtomWithIdx(swigGC.markForCleanup(bond.get(1).getSecond())))
					.setIsotope(500);
		}
		String retVal = editableMol.MolToSmiles(true);
		swigGC.cleanupMarkedObjects();
		return retVal;
	}

	/**
	 * <p>
	 * Method to identify (by start/end atom idxs wrapped as
	 * {@link BondIdentifier}) all bonds in a test molecule which match the
	 * bondMatch query
	 * </p>
	 * <p>
	 * NB some bonds might not be cuttable to give viable fragmentations
	 * </p>
	 * 
	 * 
	 * @param inMol
	 *            The {@link ROMol} test molecule
	 * @param bondMatch
	 *            The {@link ROMol} query molecule to match a single bond
	 * @return A Set of {@link BondIdentifier}s for matching bonds
	 */
	public static Set<BondIdentifier> identifyAllMatchingBonds(ROMol inMol, ROMol bondMatch) {
		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();
		HashSet<BondIdentifier> retVal = new LinkedHashSet<>();
		Match_Vect_Vect bondMatches = swigGC.markForCleanup(inMol.getSubstructMatches(bondMatch));
		for (int i = 0; i < bondMatches.size(); i++) {
			Match_Vect bond = swigGC.markForCleanup(bondMatches.get(i));
			retVal.add(new BondIdentifier(bond, inMol));
		}
		swigGC.cleanupMarkedObjects();
		return retVal;
	}

	/**
	 * <p>
	 * Method to identify (by start/end atom idxs wrapped as
	 * {@link BondIdentifier}) all bonds in a test molecule which match the
	 * bondMatch query, and remove those from the list which may not be cut and
	 * still allow for the specified number of cuts to be made to give a valid
	 * Key/Value pair
	 * </p>
	 * 
	 * @param inMol
	 *            The {@link ROMol} test molecule
	 * @param bondMatch
	 *            The {@link ROMol} query molecule to match a single bond
	 * @param numCuts
	 *            The number of bond breaks to be made (1-
	 *            {@value MMPConstants#MAXIMUM_NUMBER_OF_CUTS})
	 * @return A list of viable cutable bonds
	 * @throws IllegalArgumentException
	 *             if numCuts is out of range 1-
	 *             {@value MMPConstants#MAXIMUM_NUMBER_OF_CUTS}
	 */
	public static Set<BondIdentifier> identifyAllCuttableBonds(ROMol inMol, ROMol bondMatch,
			int numCuts) throws IllegalArgumentException {
		if (numCuts < 1 || numCuts > MMPConstants.MAXIMUM_NUMBER_OF_CUTS) {
			throw new IllegalArgumentException(
					"numCuts must be in range 1-" + MMPConstants.MAXIMUM_NUMBER_OF_CUTS);
		}

		ArrayList<BondIdentifier> bonds = new ArrayList<>(
				identifyAllMatchingBonds(inMol, bondMatch));

		if (numCuts <= 2) {
			// Any matching bond or pair of bonds is cuttable!
			return new LinkedHashSet<BondIdentifier>(bonds);
		}

		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();

		// Iterate using ListIterator so we can remove entries
		ListIterator<BondIdentifier> iter = bonds.listIterator(bonds.size());
		while (iter.hasPrevious()) {
			BondIdentifier bondId = iter.previous();
			// RWMol rwMol = RWMol.MolFromSmiles(markedBondsSmi, 0, false);
			RWMol rwMol = swigGC.markForCleanup(new RWMol(inMol));

			rwMol.removeBond(bondId.getStartIdx(), bondId.getEndIdx());
			// And Mark the end atoms so they dont get counted as leaves in the
			// call to canCutNTimes
			swigGC.markForCleanup(rwMol.getAtomWithIdx(bondId.getStartIdx())).setIsotope(500);
			swigGC.markForCleanup(rwMol.getAtomWithIdx(bondId.getEndIdx())).setIsotope(500);
			String[] comps = rwMol.MolToSmiles(true).split("\\.");
			boolean keepBond = false;
			for (String comp : comps) {
				RWMol frag = swigGC.markForCleanup(RWMol.MolFromSmiles(comp, 0, false));
				// frag.sanitizeMol();
				frag.findSSSR();
				if (canCutNTimes(frag, bondMatch, numCuts - 1)) {
					keepBond = true;
					break;
				}
			}
			if (!keepBond) {
				iter.remove();
			}
			swigGC.cleanupMarkedObjects();
		}
		return new LinkedHashSet<BondIdentifier>(bonds);
	}

	/**
	 * Overloaded method, not worrying about possibility of 2 cuts giving a
	 * single bond as a value
	 * 
	 * @see #canCutNTimes(ROMol, ROMol, int, boolean)
	 */
	public static boolean canCutNTimes(ROMol inMol, ROMol bondMatch, int numCuts) {
		return canCutNTimes(inMol, bondMatch, numCuts, false);
	}

	/**
	 * Method to test whether a way exists of cutting a given number of times
	 * using a specified bond cut type
	 * 
	 * @param inMol
	 *            The {@link ROMol} test molecule
	 * @param bondMatch
	 *            The {@link ROMol} query molecule to match a single bond
	 * @param numCuts
	 *            The number of bond breaks to be made (1-
	 *            {@value MMPConstants#MAXIMUM_NUMBER_OF_CUTS})
	 * @return {@code true} if the molecule can be cut
	 * @throws IllegalArgumentException
	 *             if numCuts is out of range 1-
	 *             {@value MMPConstants#MAXIMUM_NUMBER_OF_CUTS}
	 */
	public static boolean canCutNTimes(ROMol inMol, ROMol bondMatch, int numCuts,
			boolean allowBondValueForTwoCuts) throws IllegalArgumentException {
		if (numCuts < 1 || numCuts > MMPConstants.MAXIMUM_NUMBER_OF_CUTS) {
			throw new IllegalArgumentException(
					"numCuts must be in range 1-" + MMPConstants.MAXIMUM_NUMBER_OF_CUTS);
		}

		// As we dont return any RDKit objects, we can handle the C++ object
		// garbage collection within the method
		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();

		// Find the matching bonds
		Match_Vect_Vect matches = swigGC.markForCleanup(inMol.getSubstructMatches(bondMatch));

		// First up - quick check - we need enough cuttable bonds
		if (matches.size() < numCuts
				&& !(allowBondValueForTwoCuts && numCuts == 2 && matches.size() == 1)) {
			swigGC.cleanupMarkedObjects();
			return false;
		}

		// 1 and 2 cuts are trivial too
		if (numCuts <= 2 && matches.size() >= numCuts) {
			swigGC.cleanupMarkedObjects();
			return true;
		}

		// Special case for 2 when a bond can be returned as a value
		if (allowBondValueForTwoCuts && numCuts == 2 && matches.size() == 1) {
			// This also allowed
			swigGC.cleanupMarkedObjects();
			return true;
		}

		// Now we have to look more carefully
		String[] fragments = cutAllMatchingBonds(inMol, bondMatch).split("\\.");
		int monoAttachPointFragsCnt = 0;
		Pattern p = Pattern.compile("\\[(5|10)00[A-Za-z]");
		for (String frag : fragments) {
			int numAPs = 0;
			Matcher m = p.matcher(frag);
			while (m.find() && numAPs <= 2) {
				// Dont go beyond 2 APs (attachment points), there's no need -
				// it's not a leaf!
				// NB as a multiply attached atom is \\[1000[A-Za-z] we will not
				// count these, but that doesnt matter
				numAPs++;
				if (m.group(1).equals("10")) {
					numAPs++;
				}
			}
			if (numAPs == 1) {
				monoAttachPointFragsCnt++;
				if (monoAttachPointFragsCnt >= numCuts) {
					// Dont keep counting if we dont need to!
					swigGC.cleanupMarkedObjects();
					return true;
				}
			}
		}
		// This should never change anything - I think this should be equivalent
		// to return false!
		swigGC.cleanupMarkedObjects();
		return monoAttachPointFragsCnt >= numCuts;
	}

	/**
	 * <p>
	 * Method to calculate the maximum number of cuts.
	 * </p>
	 * <p>
	 * The for 1 or 2 cuts the methods counts the number of matching bonds; for
	 * higher cuts, the molecule has all matching bonds broken using
	 * {@link #cutAllMatchingBonds(ROMol, ROMol)}, and the number of 'leaves'
	 * (i.e. fragments with only a single attachment point) are counted.
	 * </p>
	 * <p>
	 * In the event that no matching bonds are found, when H-adding for 1 cut is
	 * specified, hydrogens are added, and the matches counted again, resulting
	 * in 0 or 1 possible cuts.
	 * <p>
	 * 
	 * @param inMol
	 *            The {@link ROMol} test molecule
	 * @param bondMatch
	 *            The {@link ROMol} query molecule to match a single bond
	 * @param addHsFor1Cut
	 *            {@code true} if Hydrogens are to be added if neede to test for
	 *            1 cut possibility
	 * @return The maximum number of cuts for the given schema
	 */
	public static int maxNumCuts(ROMol inMol, ROMol bondMatch, boolean addHsFor1Cut,
			boolean allowBondValueForTwoCuts) {

		// As we dont return any RDKit objects, we can handle the C++ object
		// garbage collection within the method
		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();

		// Find the number of matching bonds
		Match_Vect_Vect matches = swigGC.markForCleanup(inMol.getSubstructMatches(bondMatch, true));

		int numCuts = 0;
		// Firstly, need to check whether adding Hs is relevant and makes a
		// difference. Only the case if no matching bonds were found and we are
		// adding Hs for the 1 cut case
		if (matches.size() == 0 && addHsFor1Cut) {
			// First, no matching bonds - we need to see if we are allowed to
			// add
			// H's, and whether doing so helps...
			ROMol inMolH = swigGC.markForCleanup(inMol.addHs(false, false));
			Match_Vect_Vect matches2 = swigGC
					.markForCleanup(inMolH.getSubstructMatches(bondMatch, true));
			if (matches2.size() > 0) {
				numCuts = 1;
				// We return here, because we dont allow A-H >> key:
				// A-[1*].[H][2*]; value: [1*]-[2*]
				swigGC.cleanupMarkedObjects();
				return numCuts;
			}
		} else if (matches.size() == 1) {
			// 1 matching bond, then obviously we can cut once
			numCuts = 1;
		}
		if (allowBondValueForTwoCuts && numCuts == 1) {
			// If we are allowed to cut 1 bond twice, and we have decided that
			// we can cut once, then actually, we can cut twice
			numCuts = 2;
		}

		// First deal with 0, 1 and 2 cut cases
		if (matches.size() <= 2) {
			// And for 2 or fewer matching bonds, then the number of cuts is now
			// the larger of the number of matching bonds or the numCuts already
			// determined
			int retVal = (int) (numCuts > (int) matches.size() ? numCuts : matches.size());
			swigGC.cleanupMarkedObjects();
			return retVal;
		}

		// For 3 or more matches, me need to be more careful
		String[] fragments = cutAllMatchingBonds(inMol, bondMatch).split("\\.");
		int monoAttachPointFragsCnt = 0;
		Pattern p = Pattern.compile("\\[(5|10)00[A-Za-z]");
		// Pattern p2 = Pattern.compile("\\[1000[A-Za-z]");
		for (String frag : fragments) {
			int numAPs = 0;
			Matcher m = p.matcher(frag);
			while (m.find() && numAPs <= 2) {
				numAPs++;
				if (m.group(1).equals("10")) {
					numAPs++;
				}
			}
			if (numAPs == 1) {
				monoAttachPointFragsCnt++;
			}
		}
		swigGC.cleanupMarkedObjects();
		return monoAttachPointFragsCnt;
	}

	/**
	 * Failed cut triplets (i.e. 3 bonds which cut to R*.*R*.*R*.*R) are the
	 * cause of all higher order cut patterns which subsequently fail. This
	 * method cuts the molecule in all 3 places specified and checks that the
	 * above pattern is not returned
	 * 
	 * @param mol
	 *            The molecule
	 * @param bond0
	 *            The first bond
	 * @param bond1
	 *            The second bond
	 * @param bond2
	 *            The third bond
	 * @return {@code true} if this gives 3 leaves and 1 core
	 */
	public static boolean isValidCutTriplet(RWMol mol, BondIdentifier bond0, BondIdentifier bond1,
			BondIdentifier bond2) {

		Set<BondIdentifier> bonds = new HashSet<>();
		bonds.add(bond0);
		bonds.add(bond1);
		bonds.add(bond2);
		return isValidCutTriplet(mol, bonds);
	}

	/**
	 * Failed cut triplets (i.e. 3 bonds which cut to R*.*R*.*R*.*R) are the
	 * cause of all higher order cut patterns which subsequently fail. This
	 * method cuts the molecule in all 3 places specified and checks that the
	 * above pattern is not returned
	 * 
	 * @param mol
	 *            The molecule
	 * @param bonds
	 *            The set of bonds making up the 3-bond cut
	 * @return {@code true} if this gives 3 leaves and 1 core
	 * @throws IllegalArgumentException
	 *             if the number of bonds specified is not 3
	 */
	public static boolean isValidCutTriplet(ROMol mol, Collection<BondIdentifier> bonds)
			throws IllegalArgumentException {
		if (bonds.size() != 3) {
			throw new IllegalArgumentException("Exactly 3 bonds must be supplied");
		}
		SWIGObjectGarbageCollector swigGC = new SWIGObjectGarbageCollector();
		RWMol editableMol = swigGC.markForCleanup(new RWMol(mol));

		// Remove the specified bonds
		for (BondIdentifier bond : bonds) {

			// Remove the breakable bond
			editableMol.removeBond(bond.getStartIdx(), bond.getEndIdx());
			// Mark the atoms at the end with isotope 500 or 1000
			Atom at = swigGC.markForCleanup(editableMol.getAtomWithIdx(bond.getStartIdx()));
			if (at.getIsotope() < 500) {
				at.setIsotope(500);
			} else {
				at.setIsotope(1000);
			}
			at = swigGC.markForCleanup(editableMol.getAtomWithIdx(bond.getEndIdx()));
			if (at.getIsotope() < 500) {
				at.setIsotope(500);
			} else {
				at.setIsotope(1000);
			}
		}

		// Count the smiles components with 2 or more APs
		String[] fragments = editableMol.MolToSmiles(true).split("\\.");
		swigGC.cleanupMarkedObjects();

		int monoAttachPointFragsCnt = 0;

		Pattern p = Pattern.compile("\\[(5|10)00[A-Za-z]");
		for (String frag : fragments) {
			int numAPs = 0;
			Matcher m = p.matcher(frag);
			while (m.find() && numAPs <= 2) {
				// Dont go beyond 2 APs (attachment points), there's no need -
				// it's not a leaf!
				// NB as a multiply attached atom is \\[1000[A-Za-z] we will not
				// count these, but that doesnt matter
				numAPs++;
				if (m.group(1).equals("10")) {
					numAPs++;
				}
			}
			if (numAPs == 1) {
				monoAttachPointFragsCnt++;
				if (monoAttachPointFragsCnt >= 3) {
					// Dont keep counting if we dont need to!
					return true;
				}
			}
		}

		// Should only be 1 if this is a valid triplet of cuts
		return monoAttachPointFragsCnt == 3;
	}

	/**
	 * Break a single bond identified by the start and end atom IDs of the
	 * supplied {@link BondIdentifier}. Dummy atoms are added with the indicated
	 * index as isotope. In other words, A-B becomes A-[n*].B-[n*] where n is
	 * the index.
	 * 
	 * @param mol
	 *            The input molecule
	 * @param bond
	 *            The bond to break
	 * @param index
	 *            The index of the attachment point dummy atoms. If
	 *            {@code index} < 500, 500 is added
	 * @param swigGC
	 *            An instance of the {@link SWIGObjectGarbageCollector} to pass
	 *            RDKit objects generated along the way to for later clean-up
	 * @param gcWrapLayer
	 *            The wrap layer index for the garbage collection object
	 * @return A multi-component molecule with a bond broken and dummy atoms
	 *         added to each end of the broken bond
	 */
	public static RWMol breakBond(RWMol mol, BondIdentifier bond, int index,
			SWIGObjectGarbageCollector swigGC, int gcWrapLayer) {
		if (index < 500) {
			// Ensure index is 500+ to avoid clashes on canonicalisation
			index += 500;
		}

		// Get the atom index which will point to the first new atom
		long newAtmIdx = swigGC.markForCleanup(mol.getAtoms(), gcWrapLayer).size();

		// Fix the stereochemistry at each end of the bond
		// NB We do this before anything else as it is the order of BONDS (not
		// ATOMS!) which counts. Once we remove a bond, the bond indices will
		// change
		for (int atom : bond) {
			mol = swigGC.markForCleanup(
					fixAtomChirality(mol, atom, bond.getBondIdx(mol), swigGC, gcWrapLayer),
					gcWrapLayer);
		}

		// Break the bond
		mol.removeBond(bond.getStartIdx(), bond.getEndIdx());

		// Add a dummy atom to the start
		Atom at0 = swigGC.markForCleanup(new Atom(0), gcWrapLayer);
		at0.setIsotope(index);
		mol.addAtom(at0, true);
		mol.addBond(swigGC.markForCleanup(mol.getAtomWithIdx(bond.getStartIdx()), gcWrapLayer),
				swigGC.markForCleanup(mol.getAtomWithIdx(newAtmIdx++), gcWrapLayer),
				BondType.SINGLE);

		// Add a dummy atom to the end
		Atom at1 = swigGC.markForCleanup(new Atom(0), gcWrapLayer);
		at1.setIsotope(index);
		mol.addAtom(at1);
		mol.addBond(swigGC.markForCleanup(mol.getAtomWithIdx(bond.getEndIdx()), gcWrapLayer),
				swigGC.markForCleanup(mol.getAtomWithIdx(newAtmIdx++), gcWrapLayer),
				BondType.SINGLE);

		// TODO: DOES THIS BREAK IT???
		// RDKFuncs.cleanUp(mol);

		return mol;
	}

	/**
	 * Break a single bond identified by the start and end atom IDs of the
	 * supplied {@link BondIdentifier}, adding a sequential pair of IDs to the
	 * dummy atoms, and a pair of singly-bonded dummy atoms as a separate
	 * component with the same IDs. Dummy atoms are added with the indicated
	 * index as isotope. In other words, A-B becomes
	 * A-[n*].B-[(n+1)*].[n*]-[(n+1)*] where n is the index.
	 * 
	 * @param mol
	 *            The input molecule
	 * @param bond
	 *            The bond to break
	 * @param index
	 *            The index of the attachment point dummy atoms. If
	 *            {@code index} < 500, 500 is added
	 * @param swigGC
	 *            An instance of the {@link SWIGObjectGarbageCollector} to pass
	 *            RDKit objects generated along the way to for later clean-up
	 * @param gcWrapLayer
	 *            The wrap layer index for the garbage collection object
	 * @return A multi-component molecule with a bond broken and dummy atoms
	 *         added to each end of the broken bond
	 */
	public static RWMol breakOneBondTwice(RWMol mol, BondIdentifier bond, int index,
			SWIGObjectGarbageCollector swigGC, int gcWrapLayer) {
		if (index < 500) {
			// Ensure index is 500+ to avoid clashes on canonicalisation
			index += 500;
		}

		// Get the atom index which will point to the first new atom
		long newAtmIdx = mol.getAtoms().size();

		// Fix the stereochemistry at each end of the bond
		// NB We do this before anything else as it is the order of BONDS (not
		// ATOMS!) which counts. Once we remove a bond, the bond indices will
		// change
		for (int atom : bond) {
			mol = swigGC.markForCleanup(
					fixAtomChirality(mol, atom, bond.getBondIdx(mol), swigGC, gcWrapLayer),
					gcWrapLayer);
		}

		// Break the bond
		mol.removeBond(bond.getStartIdx(), bond.getEndIdx());

		// Add a dummy atom to the start
		Atom at0 = swigGC.markForCleanup(new Atom(0), gcWrapLayer);
		at0.setIsotope(index);
		mol.addAtom(at0, true);

		mol.addBond(swigGC.markForCleanup(mol.getAtomWithIdx(bond.getStartIdx()), gcWrapLayer),
				swigGC.markForCleanup(mol.getAtomWithIdx(newAtmIdx++), gcWrapLayer),
				BondType.SINGLE);

		// Add a dummy atom to the end - with an incremented index
		Atom at1 = swigGC.markForCleanup(new Atom(0), gcWrapLayer);
		at1.setIsotope(index + 1);
		mol.addAtom(at1);
		mol.addBond(swigGC.markForCleanup(mol.getAtomWithIdx(bond.getEndIdx()), gcWrapLayer),
				swigGC.markForCleanup(mol.getAtomWithIdx(newAtmIdx++), gcWrapLayer),
				BondType.SINGLE);

		// Now we need to add two further atoms and bond them together too
		Atom at2 = swigGC.markForCleanup(new Atom(0), gcWrapLayer);
		at2.setIsotope(index);
		mol.addAtom(at2, true);

		Atom at3 = swigGC.markForCleanup(new Atom(0), gcWrapLayer);
		at3.setIsotope(index + 1);
		mol.addAtom(at3, true);

		mol.addBond(swigGC.markForCleanup(mol.getAtomWithIdx(newAtmIdx++), gcWrapLayer),
				swigGC.markForCleanup(mol.getAtomWithIdx(newAtmIdx++), gcWrapLayer),
				BondType.SINGLE);
		return mol;
	}

	/**
	 * As the bond priority order for SMILES (and RDKit objects is specifierd by
	 * CW / CCW order of bonds around the stereocentre, when a bond is broken
	 * and replaced with a new bond, in order to retain the true
	 * stereochemistry, it may be necessary to invert a stereocentre's CW/ CCW
	 * designation. The RDKit {@link ChemicalReaction} class implements this
	 * directly by a slightly complex method. Here, we compose the old order of
	 * bond priorities, and look for the number of 'swaps' to get the new bond
	 * (which will be added at the end) back to the correct position. An odd
	 * number of swaps requires inversion. This method iterates through the
	 * atoms at either end of the bond. <b>It does not fix double bond E/Z
	 * geometry</b>
	 * 
	 * @param mol
	 *            The molecule to fix
	 * @param atomIdx
	 *            The atom ID of the atom to fix
	 * @param bondIdx
	 *            The bond ID of the bond to be replaced
	 * @return The molecule with the stereochemistry fixed
	 */
	public static RWMol fixBondChirality(RWMol mol, BondIdentifier bond,
			SWIGObjectGarbageCollector swigGC, int gcWrapLayer) {
		for (int atom : bond) {
			mol = fixAtomChirality(mol, atom, bond.getBondIdx(mol), swigGC, gcWrapLayer);
		}
		return mol;
	}

	/**
	 * As the bond priority order for SMILES (and RDKit objects is specifierd by
	 * CW / CCW order of bonds around the stereocentre, when a bond is broken
	 * and replaced with a new bond, in order to retain the true
	 * stereochemistry, it may be necessary to invert a stereocentre's CW/ CCW
	 * designation. The RDKit {@link ChemicalReaction} class implements this
	 * directly by a slightly complex method. Here, we compose the old order of
	 * bond priorities, and look for the number of 'swaps' to get the new bond
	 * (which will be added at the end) back to the correct position. An odd
	 * number of swaps requires inversion
	 * 
	 * @param mol
	 *            The molecule to fix
	 * @param atomIdx
	 *            The atom ID of the atom to fix
	 * @param bondIdx
	 *            The bond ID of the bond to be replaced
	 * @return The molecule with the stereochemistry fixed
	 */
	public static RWMol fixAtomChirality(RWMol mol, long atomIdx, long bondIdx,
			SWIGObjectGarbageCollector swigGC, int gcWrapLayer) {

		ChiralType atmChirality = swigGC.markForCleanup(mol.getAtomWithIdx(atomIdx), gcWrapLayer)
				.getChiralTag();
		if (atmChirality == ChiralType.CHI_TETRAHEDRAL_CCW
				|| atmChirality == ChiralType.CHI_TETRAHEDRAL_CW) {
			// We need to fix the chirality of the chiral attachment point atom

			// Firstly, we need to check if the atom is the start of the SMILES
			// String
			// boolean atomStartsSMILES = mol.getAtomWithIdx(atomIdx).hasProp(
			// "_SmilesStart")
			// && mol.getAtomWithIdx(atomIdx).getProp("_SmilesStart") == "1";
			// And also whether it has any ring closure bonds (these are the '1'
			// in e.g. C1CCC1)
			boolean atomHasRingClosures = swigGC
					.markForCleanup(mol.getAtomWithIdx(atomIdx), gcWrapLayer)
					.hasProp("_RingClosures");
			List<Long> ringClosureBonds = new ArrayList<>();
			if (atomHasRingClosures) {
				String prop = swigGC.markForCleanup(mol.getAtomWithIdx(atomIdx), gcWrapLayer)
						.getProp("_RingClosures").replace("[", "").replace(",]", "");
				String[] idx;
				if (prop.indexOf(",") >= 0) {
					idx = prop.split(",");
				} else {
					idx = new String[] { prop };
				}
				for (String id : idx) {
					ringClosureBonds.add(Long.parseLong(id.trim()));
				}
			}

			List<Long> attachedBonds = new ArrayList<>();
			Bond_Vect bonds = swigGC.markForCleanup(
					swigGC.markForCleanup(mol.getAtomWithIdx(atomIdx), gcWrapLayer).getBonds(),
					gcWrapLayer);
			for (int i = 0; i < bonds.size(); i++) {
				// Loop through the remaining bonds attached to the atom with
				// the broken bond
				attachedBonds.add(swigGC.markForCleanup(bonds.get(i), gcWrapLayer).getIdx());
			}

			// Ring closure bonds will have the wrong priorities based on id if
			// they are at the start of the ring closure bond definition.
			// If they are at the start, they will have the highest bond index
			// boolean removed=true;
			// while(removed){
			// List<Long> tmp = new ArrayList<>();
			// Collections.copy(tmp, attachedBonds);
			// tmp.removeAll(ringClosureBonds);
			// }
			attachedBonds.removeAll(ringClosureBonds);
			Collections.sort(attachedBonds);
			// if (atomStartsSMILES) {
			// attachedBonds.addAll(0, ringClosureBonds);
			// } else {
			// attachedBonds.addAll(1, ringClosureBonds);
			// }

			// if (mol.getAtomWithIdx(atomIdx).getDegree() == 3) {
			// // Implicit H atom
			// if (atomStartsSMILES) {
			// attachedBonds.add(0, -1L);
			// } else {
			// attachedBonds.add(1, -1L);
			// }
			// }
			// Now we need to calculate the number of swaps needed to get the
			// bond back to the right place in the priority list, as it will be
			// added at the end
			int nSwaps = attachedBonds.size() - attachedBonds.indexOf(bondIdx) - 1;

			// Finally, for unknown reasons, if we have only 1 ring closure
			// bond, we need 1 less swap
			nSwaps += ringClosureBonds.size();

			if (nSwaps % 2 != 0) {
				swigGC.markForCleanup(mol.getAtomWithIdx(atomIdx), gcWrapLayer).invertChirality();
			}

		}
		return mol;
	}

	/**
	 * Filter the fragment pair according to filters
	 * 
	 * @param key
	 *            The Key part of the fragmented molecule
	 * @param value
	 *            The Value part of the fragmented molecule
	 * @param maxNumVarAtoms
	 *            The maximum number of changing atoms ({@code null} if no
	 *            filter)
	 * @param minCnstToVarAtmRatio
	 *            The minimum ration of constant to changing atoms ({@code null}
	 *            if not filter)
	 * @return {@code true} if all specified filters are passed
	 */
	public static boolean filterFragment(FragmentKey2 key, FragmentValue2 value,
			Integer maxNumVarAtoms, Double minCnstToVarAtmRatio) {

		// if (maxNumVarAtoms == null && minCnstToVarAtmRatio == null) {
		// // No filtering applied
		// return true;
		// }

		if (maxNumVarAtoms != null && value.getNumberChangingAtoms() > maxNumVarAtoms) {
			// There is a Maximum number of changing atoms filter, and it is
			// violated
			return false;
		}

		if (minCnstToVarAtmRatio != null
				&& key.getConstantToVaryingAtomRatio(value) < minCnstToVarAtmRatio) {
			// There is a Minumum const/varying atom ratio filter, and it is
			// violated
			return false;
		}

		// We passed all checks
		return true;

	}

	/**
	 * <p>
	 * String parsing method to remove hydrogens from a SMILES String
	 * </p>
	 * <p>
	 * Chirality and double bond geometry is preserved, taking [H] inside the @]
	 * where required
	 * </p>
	 * 
	 * @param smiles
	 *            The input SMILES String
	 * @return The SMILES String with Hydrogen atoms removed
	 */
	public static String removeHydrogens(String smiles) {
		// NB Tried to do with a backreference to '(' before [H] but
		// couldnt
		// get it working
		smiles = smiles.replaceAll("@\\]([\\d]*)\\[H\\]", "@H]$1")
				.replaceAll("@\\]([\\d]*)\\(\\[H\\]\\)", "@H]$1");
		// smi = smi.replace("@][H]", "@H]").replace("@]([H])", "@H]");
		smiles = smiles.replace("[H]", "").replace("()", "");
		smiles = smiles.replace("(/)", "\\").replace("(\\)", "/");
		return smiles;
	}

	/**
	 * Get the transfroms for a {@link TreeSet} of {@link FragmentValue2}s,
	 * optionally including the number of changing heavy atoms. This is the
	 * simple method not requiring access to the {@link FragmentKey}
	 * 
	 * @param fragmentValues
	 *            A {@link TreeSet} of {@link FragmentValue2}s from which to
	 *            derive transforms
	 * @param numNewCols
	 *            The number of new columns - supplied so only calculated once
	 *            during the {@link NodeModel} <code>#configure</code> method.
	 * @param removeExplicitHs
	 *            If true, then explicit hydrogens are removed from the outputs
	 * @param includeNumChangingHAs
	 *            If true, then the number of changing heavy atoms are added for
	 *            each fragment
	 * @param showReverseTransforms
	 *            If <code>true<code>, then the output includes transformations
	 *            in both directions
	 * @param allowSelfTransforms
	 *            If {@code true}, then transforms arising from two different
	 *            cuts to a molecule giving the same key are allowed
	 * @return An {@link ArrayList} of {@link DataCell}s for each new transform
	 *         to be added derived from the {@link TreeSet} of
	 *         {@link FragmentValue}s
	 * @see #getTransforms(TreeSet, FragmentKey, int, boolean, boolean, boolean,
	 *      boolean)
	 */
	public static ArrayList<DataCell[]> getTransforms(TreeSet<FragmentValue2> fragmentValues,
			int numNewCols, boolean removeExplicitHs, boolean includeNumChangingHAs,
			boolean showReverseTransforms, boolean allowSelfTransforms) {

		ArrayList<DataCell[]> retVal = new ArrayList<DataCell[]>();
		TreeSet<FragmentValue2> orderedFrags = new TreeSet<FragmentValue2>(fragmentValues);
		for (FragmentValue2 leftFrag : orderedFrags) {
			for (FragmentValue2 rightFrag : orderedFrags.tailSet(leftFrag, false)) {
				if ((allowSelfTransforms || !leftFrag.getID().equals(rightFrag.getID()))
						&& !leftFrag.isSameSMILES(rightFrag)) {
					DataCell[] transform = buildSimpleTransform(numNewCols, removeExplicitHs,
							includeNumChangingHAs, leftFrag, rightFrag);
					retVal.add(transform);
					if (showReverseTransforms) {
						transform = buildSimpleTransform(numNewCols, removeExplicitHs,
								includeNumChangingHAs, rightFrag, leftFrag);
						retVal.add(transform);
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * Actually builds the transformation cells array from the Left and Right
	 * {@link FragmentValue}s. Called by
	 * {@link #getTransforms(TreeSet, int, boolean, boolean, boolean)}. Called
	 * twice if reverse transforms are also to be shown, with the Left and Right
	 * {@link FragmentValue}s transposed
	 * 
	 * @param numNewCols
	 *            The number of new columns - supplied so only calculated once
	 *            during the {@link NodeModel} <code>#configure</code> method.
	 * @param removeExplicitHs
	 *            If true, then explicit hydrogens are removed from the outputs
	 * @param includeNumChangingHAs
	 *            If true, then the number of changing heavy atoms are added for
	 *            each fragment
	 * @param leftFrag
	 *            The {@link FragmentValue2} for the 'Left' molecule
	 * @param rightFrag
	 *            The {@link FragmentValue2} for the 'Right' molecule
	 * @return The {@link DataCell}s for the new row representing the tranform
	 */
	private static DataCell[] buildSimpleTransform(int numNewCols, boolean removeExplicitHs,
			boolean includeNumChangingHAs, FragmentValue2 leftFrag, FragmentValue2 rightFrag) {
		DataCell[] retVal = new DataCell[numNewCols];
		int i = 0;
		retVal[i++] = SmilesCellFactory.create(leftFrag.getSMILES(removeExplicitHs) + ">>"
				+ rightFrag.getSMILES(removeExplicitHs));
		retVal[i++] = leftFrag.getIDCell();
		retVal[i++] = rightFrag.getIDCell();
		retVal[i++] = leftFrag.getSMILESCell(removeExplicitHs);
		retVal[i++] = rightFrag.getSMILESCell(removeExplicitHs);
		if (includeNumChangingHAs) {
			retVal[i++] = leftFrag.getNumberChangingAtomsCell();
			retVal[i++] = rightFrag.getNumberChangingAtomsCell();
		}
		return retVal;
	}

	/**
	 * Overloaded method to get the transforms for a {@link TreeSet} of
	 * {@link FragmentValue}s, optionally including the number of changing heavy
	 * atoms, and properties relating to the {@link FragmentKey2}.
	 * 
	 * @param fragmentValues
	 *            A {@link TreeSet} of {@link FragmentValue2}s from which to
	 *            derive transforms
	 * @param fragmentKey
	 *            The {@link FragmentKey2} from which to derive the unchanging
	 *            atoms and HA ratios
	 * @param numNewCols
	 *            The number of new columns - supplied so only calculated once
	 *            during the {@link NodeModel} <code>#configure</code> method.
	 * @param removeExplicitHs
	 *            If true, then explicit hydrogens are removed from the outputs
	 * @param includeKeySMILES
	 *            If true, then the SMILES representation of the unchanging
	 *            atoms is included in the output
	 * @param includeNumChangingHAs
	 *            If true, then the number of changing heavy atoms are added for
	 *            each fragment
	 * @param includeRatioHAs
	 *            If true, then the rations of changing / unchanging heavy atoms
	 *            are added for each transformation
	 * @param showReverseTransforms
	 *            If <code>true<code>, then the output includes transformations
	 *            in both directions
	 * @param allowSelfTransforms
	 *            If {@code true}, then transforms arising from two different
	 *            cuts to a molecule giving the same key are allowed
	 * @return An {@link ArrayList} of {@link DataCell}s for each new transform
	 *         to be added derived from the {@link TreeSet} of
	 *         {@link FragmentValue}s
	 * @see #getTransforms(TreeSet, int, boolean, boolean)
	 */
	public static ArrayList<DataCell[]> getTransforms(TreeSet<FragmentValue2> fragmentValues,
			FragmentKey2 fragmentKey, int numNewCols, boolean removeExplicitHs,
			boolean includeKeySMILES, boolean includeNumChangingHAs, boolean includeRatioHAs,
			boolean showReverseTransforms, boolean allowSelfTransforms) {

		ArrayList<DataCell[]> retVal = new ArrayList<DataCell[]>();
		TreeSet<FragmentValue2> orderedFrags = new TreeSet<FragmentValue2>(fragmentValues);
		for (FragmentValue2 leftFrag : orderedFrags) {
			for (FragmentValue2 rightFrag : orderedFrags.tailSet(leftFrag, false)) {
				if ((allowSelfTransforms || !leftFrag.getID().equals(rightFrag.getID()))
						&& !leftFrag.isSameSMILES(rightFrag)) {
					DataCell[] transform = buildTransform(fragmentKey, removeExplicitHs,
							includeKeySMILES, includeNumChangingHAs, includeRatioHAs, leftFrag,
							rightFrag, numNewCols);
					retVal.add(transform);
					if (showReverseTransforms) {
						transform = buildTransform(fragmentKey, removeExplicitHs, includeKeySMILES,
								includeNumChangingHAs, includeRatioHAs, rightFrag, leftFrag,
								numNewCols);
						retVal.add(transform);
					}
				}
			}
		}
		return retVal;
	}

	/**
	 * Actually builds the transformation cells array from the Left and Right
	 * {@link FragmentValue2}s. Called by
	 * {@link #getTransforms(TreeSet, FragmentKey2, int, boolean, boolean, boolean, boolean, boolean)}
	 * . Called twice if reverse transforms are also to be shown, with the Left
	 * and Right {@link FragmentValue}s transposed
	 * 
	 * @param fragmentKey
	 *            The {@link FragmentKey2} for the transformation
	 * @param removeExplicitHs
	 *            If true, then explicit hydrogens are removed from the outputs
	 * @param includeKeySMILES
	 *            If true, then include the smiles of the Key in the output
	 * @param includeNumChangingHAs
	 *            If true, then the number of changing heavy atoms are added for
	 *            each fragment * @param includeRatioHAs
	 * @param leftFrag
	 *            The {@link FragmentValue2} for the 'Left' molecule
	 * @param rightFrag
	 *            The {@link FragmentValue2} for the 'Right' molecule
	 * @param numNewCols
	 *            The number of new columns - supplied so only calculated once
	 *            during the {@link NodeModel} <code>#configure</code> method.
	 *            * @return The {@link DataCell}s for the new row representing
	 *            the tranform
	 */
	private static DataCell[] buildTransform(FragmentKey2 fragmentKey, boolean removeExplicitHs,
			boolean includeKeySMILES, boolean includeNumChangingHAs, boolean includeRatioHAs,
			FragmentValue2 leftFrag, FragmentValue2 rightFrag, int numNewCols) {
		DataCell[] retVal = new DataCell[numNewCols];

		int i = 0;
		retVal[i++] = SmilesCellFactory.create(leftFrag.getSMILES(removeExplicitHs) + ">>"
				+ rightFrag.getSMILES(removeExplicitHs));
		retVal[i++] = leftFrag.getIDCell();
		retVal[i++] = rightFrag.getIDCell();
		retVal[i++] = leftFrag.getSMILESCell(removeExplicitHs);
		retVal[i++] = rightFrag.getSMILESCell(removeExplicitHs);
		if (includeKeySMILES) {
			retVal[i++] = fragmentKey.getKeyAsDataCell(removeExplicitHs);
		}
		if (includeNumChangingHAs) {
			retVal[i++] = leftFrag.getNumberChangingAtomsCell();
			retVal[i++] = rightFrag.getNumberChangingAtomsCell();
		}
		if (includeRatioHAs) {
			retVal[i++] = fragmentKey.getConstantToVaryingAtomRatioCell(leftFrag);
			retVal[i++] = fragmentKey.getConstantToVaryingAtomRatioCell(rightFrag);
		}
		return retVal;
	}

}
