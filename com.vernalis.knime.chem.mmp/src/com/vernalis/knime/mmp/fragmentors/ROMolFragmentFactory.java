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
package com.vernalis.knime.mmp.fragmentors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.RDKit.Atom;
import org.RDKit.Atom.ChiralType;
import org.RDKit.AtomIterator;
import org.RDKit.Bond;
import org.RDKit.Bond.BondDir;
import org.RDKit.Bond.BondType;
import org.RDKit.BondIterator;
import org.RDKit.Bond_Vect;
import org.RDKit.Int_Vect;
import org.RDKit.Match_Vect;
import org.RDKit.MolSanitizeException;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.RWMol;
import org.RDKit.UInt_Vect;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.mmp.BondIdentifier;
import com.vernalis.knime.mmp.MulticomponentSmilesFragmentParser;
import com.vernalis.knime.swiggc.ISWIGObjectGarbageCollector;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

/**
 * <p>
 * This class provides a number of factory methods for fragmenting a molecule. A
 * molecule can be fragmented multiple times by calling the relevant
 * fragmentation method with different {@link BondIdentifier}(s).
 * </p>
 * <p>
 * The main factory method, {@link #fragmentMolecule(Set)}, will attempt to
 * perform the fragmentation in the quickest available manner
 * </p>
 * <p>
 * The class is thread-safe and performs it's own garbage clean-up of RDKit
 * objects. The user must, however, clean up the supplied {@link ROMol} object
 * after use
 * </p>
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class ROMolFragmentFactory implements MoleculeFragmentationFactory {
	/**
	 * Property name to flag incomping possible but not stereocentres
	 */
	private static final String UNSPECIFIED_STEREOCENTRE = "UNSPECIFIED_STEREOCENTRE";

	/**
	 * Property name to flag incoming possible but not stereo double bonds
	 */
	private static final String UNSPECIFIED_DOUBLE_BOND = "UNSPECIFIED_DOUBLE_BOND";

	/**
	 * Property name to hold isotopic label for attachment point until needed
	 */
	private static final String AP_ISOTOPIC_LABEL = "AP_ISOTOPIC_LABEL";

	private final ROMol mol;

	private boolean isChiral, hasUndefinedChirality;

	private final ISWIGObjectGarbageCollector gc = new SWIGObjectGarbageCollector();

	AtomicInteger gcWave = new AtomicInteger(2);

	private final NodeLogger logger = NodeLogger.getLogger(this.getClass());

	private boolean verboseLogging;

	/**
	 * Constructor
	 * 
	 * @param mol
	 *            The molecule to be fragmented
	 * @param verboseLogging
	 *            Should the logger be used
	 */
	public ROMolFragmentFactory(ROMol mol, boolean verboseLogging) {
		this.mol = gc.markForCleanup(new ROMol(mol), 1);
		this.verboseLogging = verboseLogging;
		isChiral = false;
		hasUndefinedChirality = false;
		markUnassignedPossibleDoubleBonds();
		markUnassignedPossibleChiralCentres();
	}

	/**
	 * Double bonds which could have stereoisomerism but have none assigned are
	 * flagged with the property {@value #UNSPECIFIED_DOUBLE_BOND}
	 */
	protected void markUnassignedPossibleDoubleBonds() {
		RDKFuncs.findPotentialStereoBonds(this.mol, false);
		// Now loop through the bonds
		for (BondIterator iter = gc.markForCleanup(this.mol.beginBonds(), 1); iter
				.ne(gc.markForCleanup(this.mol.endBonds(), 1)); gc.markForCleanup(iter.next(), 1)) {

			Bond bd = gc.markForCleanup(iter.getBond(), 1);

			if (bd.getBondType() == BondType.DOUBLE
					&& gc.markForCleanup(bd.getStereoAtoms(), 1).size() > 0) {
				// We only worry about double bonds which could be stereo
				boolean isStereo = false;
				// Check that it *could* be a stereobond - we only
				// care about those that could be but arent
				if (bd.getBondDir() != BondDir.EITHERDOUBLE) {
					Bond_Vect bv = gc
							.markForCleanup(gc.markForCleanup(bd.getBeginAtom(), 1).getBonds(), 1);
					for (int i = 0; i < bv.size(); i++) {
						if (gc.markForCleanup(bv.get(i), 1).getBondDir() != BondDir.NONE && gc
								.markForCleanup(bv.get(i), 1).getBondDir() != BondDir.UNKNOWN) {
							isStereo = true;
							break;
						}
					}
					if (!isStereo) {
						bv = gc.markForCleanup(gc.markForCleanup(bd.getEndAtom(), 1).getBonds(), 1);
						for (int i = 0; i < bv.size(); i++) {
							if (gc.markForCleanup(bv.get(i), 1).getBondDir() != BondDir.NONE && gc
									.markForCleanup(bv.get(i), 1).getBondDir() != BondDir.UNKNOWN) {
								isStereo = true;
								break;
							}
						}
					}
				}
				if (!isStereo) {
					bd.setProp(UNSPECIFIED_DOUBLE_BOND, "1");
				}
			}
		}
	}

	/**
	 * Atoms which could be chiral but are not are flagged with the property
	 * {@value #UNSPECIFIED_STEREOCENTRE}. The {@link #isChiral} property is set
	 * if a defined chiral centre is found
	 */
	protected void markUnassignedPossibleChiralCentres() {
		// Flag possible stereocentres
		RDKFuncs.assignStereochemistry(this.mol, false, true, true);

		// Now loop through the atoms
		for (AtomIterator iter = gc.markForCleanup(this.mol.beginAtoms(), 1); iter
				.ne(gc.markForCleanup(this.mol.endAtoms(), 1)); gc.markForCleanup(iter.next(), 1)) {

			Atom at = gc.markForCleanup(iter.getAtom(), 1);
			if (at.hasProp("_ChiralityPossible") && (at.getChiralTag() == ChiralType.CHI_UNSPECIFIED
					|| at.getChiralTag() == ChiralType.CHI_OTHER)) {
				at.setProp(UNSPECIFIED_STEREOCENTRE, "1");
				hasUndefinedChirality = true;
			} else if (!isChiral && (at.getChiralTag() == ChiralType.CHI_TETRAHEDRAL_CCW
					|| at.getChiralTag() == ChiralType.CHI_TETRAHEDRAL_CW)) {
				isChiral = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.MoleculeFragmentationFactory#
	 * setVerboseLogging(boolean)
	 */
	@Override
	public void setVerboseLogging(boolean verboseLogging) {
		this.verboseLogging = verboseLogging;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.MoleculeFragmentationFactory#
	 * fragmentMolecule (com.vernalis.knime.internal.mmp.BondIdentifier)
	 */
	@Override
	public MulticomponentSmilesFragmentParser fragmentMolecule(BondIdentifier bond,
			boolean treatProchiralAsChiral)
					throws MoleculeFragmentationException, IllegalArgumentException {
		if (bond == null) {
			throw new IllegalArgumentException("A bond must be supplied");
		}

		// Single bond is much simpler!
		int localGcWave = gcWave.getAndIncrement();
		RWMol tmp = gc.markForCleanup(new RWMol(mol), localGcWave);

		if (gc.markForCleanup(tmp.getBondBetweenAtoms(bond.getStartIdx(), bond.getEndIdx()),
				localGcWave) == null) {
			// Not a bond in the molecule
			gc.cleanupMarkedObjects(localGcWave);
			throw new MoleculeFragmentationException("Bond was not part of molecule!");
		}

		// Firstly, we break the bond
		tmp.removeBond(bond.getStartIdx(), bond.getEndIdx());

		// Now start at the first index of the first bond, and list all the atom
		// IDs visited
		List<Long> visitedAtomIDs = new ArrayList<>();
		Set<Long> atomLayer = new HashSet<>();
		atomLayer.add((long) bond.getStartIdx());
		Set<Long> nextAtomLayer = new HashSet<>();
		while (atomLayer.size() > 0) {
			for (Long atIdx : atomLayer) {
				visitedAtomIDs.add(atIdx);
				Atom at0 = gc.markForCleanup(tmp.getAtomWithIdx(atIdx), localGcWave);
				Bond_Vect bnds0 = gc.markForCleanup(at0.getBonds(), localGcWave);
				for (int i = 0; i < bnds0.size(); i++) {
					Bond bnd = gc.markForCleanup(bnds0.get(i), localGcWave);
					nextAtomLayer.add(bnd.getBeginAtomIdx());
					nextAtomLayer.add(bnd.getEndAtomIdx());
				}
			}
			nextAtomLayer.removeAll(visitedAtomIDs);
			atomLayer = new HashSet<>(nextAtomLayer);
			nextAtomLayer.clear();
		}

		// Now, visitedAtomIDs contains a list of the atoms in the 'value' or
		// core
		// On a fresh copy, we delete those atoms not needed

		RWMol value = gc.markForCleanup(new RWMol(mol), localGcWave);
		RWMol key = gc.markForCleanup(new RWMol(mol), localGcWave);

		// Now we need to Iterate down through the List of atoms (start with
		// highest IDX as renumbering will occur
		for (long atIdx = mol.getNumAtoms() - 1; atIdx >= 0; atIdx--) {
			// for (Long atIdx : atomIDs) {
			Atom atVal, atKey;

			// For each atom there are three options for CORE (VALUE)
			// V1. Visited - we keep it
			// V2. Not visited but is AP - we change it
			// V3. Not visited, not AP, so we delete it
			// And for the KEY:
			// K1. Not Visited - we keep it
			// K2. Visited, but is AP - change it
			// k3. Visited, not AP - delete
			if (!visitedAtomIDs.contains(atIdx)) {
				// K1, V2. or V3.
				if (bond.isToAtomWithIdx(atIdx)) {
					// V2 - change it
					// atVal = gc.markForCleanup(value.getAtomWithIdx(atIdx),
					// localGcWave);
					atVal = gc.markForCleanup(new Atom(0), localGcWave);
					// atVal.setAtomicNum(0);
					// atVal.setIsotope(500);
					atVal.setProp(AP_ISOTOPIC_LABEL, "500");
					value.replaceAtom(atIdx, atVal);
				} else {
					// V3 - Zap!
					value.removeAtom(atIdx);
				}
			} else {
				// V1, K2 or K3
				if (bond.isToAtomWithIdx(atIdx)) {
					// K2 - change it
					// atKey = gc.markForCleanup(key.getAtomWithIdx(atIdx),
					// localGcWave);
					atKey = gc.markForCleanup(new Atom(0), localGcWave);
					atKey.setAtomicNum(0);
					// atKey.setIsotope(500);
					atKey.setProp(AP_ISOTOPIC_LABEL, "500");
					key.replaceAtom(atIdx, atKey);
				} else {
					// K3 - Zap!
					key.removeAtom(atIdx);
				}
			}
		}

		// Check for generated chirality & double bond geometry
		if (isChiral || (!hasUndefinedChirality && treatProchiralAsChiral)) {
			assignAPChirality(value, localGcWave);
			assignAPChirality(key, localGcWave);
		}
		assignCreatedDblBondGeometry(key, localGcWave);
		assignCreatedDblBondGeometry(value, localGcWave);
		canonicaliseDuplicateKeyComponents(value, localGcWave, key);
		applyAPIsotopicLabels(key, localGcWave);
		applyAPIsotopicLabels(value, localGcWave);

		String retVal = key.MolToSmiles(true) + "." + getCanonicalValueSMILES(value, localGcWave);
		gc.cleanupMarkedObjects(localGcWave);

		return new MulticomponentSmilesFragmentParser(retVal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.MoleculeFragmentationFactory#
	 * fragmentMolecule (java.util.Set)
	 */
	@Override
	public MulticomponentSmilesFragmentParser fragmentMolecule(Set<BondIdentifier> bonds,
			boolean treatProchiralAsChiral)
					throws IllegalArgumentException, MoleculeFragmentationException {

		if (bonds == null || bonds.size() == 0) {
			throw new IllegalArgumentException("At least one bond must be supplied");
		}

		if (bonds.size() == 1) {
			return fragmentMolecule(bonds.iterator().next(), treatProchiralAsChiral);
		}

		// check which fragmentation route is required
		List<BondIdentifier> orderedBonds = new ArrayList<>(bonds);
		boolean useMultipleFragmentMols = false;
		for (int i = 0; i < bonds.size() - 1; i++) {
			for (int j = i + 1; j < bonds.size(); j++) {
				if (orderedBonds.get(i).hasSharedAtomWith(orderedBonds.get(j))) {
					useMultipleFragmentMols = true;
					break;
				}
			}
			if (useMultipleFragmentMols) {
				break;
			}
		}

		// Now assign the temporary indices
		int index = 500;
		for (BondIdentifier bond : bonds) {
			bond.setFragmentationIndex(index++);
		}

		// Now do the relevant flavour of fragmentation
		if (useMultipleFragmentMols) {
			return fragmentLong(bonds, treatProchiralAsChiral);
		} else {
			return fragmentShort(bonds, treatProchiralAsChiral);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.MoleculeFragmentationFactory#
	 * fragmentMoleculeWithBondInsertion
	 * (com.vernalis.knime.internal.mmp.BondIdentifier)
	 */
	@Override
	public MulticomponentSmilesFragmentParser fragmentMoleculeWithBondInsertion(BondIdentifier bond,
			boolean treatProchiralAsChiral)
					throws IllegalArgumentException, MoleculeFragmentationException {
		if (bond == null) {
			throw new IllegalArgumentException("A bond must be supplied");
		}

		// Single bond is much simpler!
		int localGcWave = gcWave.getAndIncrement();
		RWMol tmp = gc.markForCleanup(new RWMol(mol), localGcWave);

		if (gc.markForCleanup(tmp.getBondBetweenAtoms(bond.getStartIdx(), bond.getEndIdx()),
				localGcWave) == null) {
			// Not a bond in the molecule
			gc.cleanupMarkedObjects(localGcWave);
			throw new MoleculeFragmentationException("Bond was not part of molecule!");
		}

		// Firstly, we break the bond
		tmp.removeBond(bond.getStartIdx(), bond.getEndIdx());

		// Now start at the first index of the first bond, and list all the atom
		// IDs visited and count the APs
		List<Long> visitedAtomIDs = new ArrayList<>();
		Set<Long> atomLayer = new HashSet<>();
		atomLayer.add((long) bond.getStartIdx());
		Set<Long> nextAtomLayer = new HashSet<>();
		while (atomLayer.size() > 0) {
			for (Long atIdx : atomLayer) {
				visitedAtomIDs.add(atIdx);
				Atom at0 = gc.markForCleanup(tmp.getAtomWithIdx(atIdx), localGcWave);
				Bond_Vect bnds0 = gc.markForCleanup(at0.getBonds(), localGcWave);
				for (int i = 0; i < bnds0.size(); i++) {
					Bond bnd = gc.markForCleanup(bnds0.get(i), localGcWave);
					nextAtomLayer.add(bnd.getBeginAtomIdx());
					nextAtomLayer.add(bnd.getEndAtomIdx());
				}
			}
			nextAtomLayer.removeAll(visitedAtomIDs);
			atomLayer = new HashSet<>(nextAtomLayer);
			nextAtomLayer.clear();
		}

		// Now, visitedAtomIDs contains a list of the atoms in the 'value' or
		// core
		// On a fresh copy, we delete those atoms not needed

		RWMol value = gc.markForCleanup(new RWMol(mol), localGcWave);
		RWMol key = gc.markForCleanup(new RWMol(mol), localGcWave);

		// Now we need to Iterate down through the List of atoms (start with
		// highest IDX as renumbering will occur
		for (long atIdx = mol.getNumAtoms() - 1; atIdx >= 0; atIdx--) {
			// for (Long atIdx : atomIDs) {
			Atom atVal, atKey;

			// For each atom there are three options for CORE (VALUE)
			// V1. Visited - we keep it
			// V2. Not visited but is AP - we change it
			// V3. Not visited, not AP, so we delete it
			// And for the KEY:
			// K1. Not Visited - we keep it
			// K2. Visited, but is AP - change it
			// k3. Visited, not AP - delete
			if (!visitedAtomIDs.contains(atIdx)) {
				// K1, V2. or V3.
				if (bond.isToAtomWithIdx(atIdx)) {
					// V2 - change it
					// atVal = gc.markForCleanup(value.getAtomWithIdx(atIdx),
					// localGcWave);
					atVal = gc.markForCleanup(new Atom(0), localGcWave);
					atVal.setAtomicNum(0);
					// atVal.setIsotope(500);
					atVal.setProp(AP_ISOTOPIC_LABEL, "500");
					value.replaceAtom(atIdx, atVal);
				} else {
					// V3 - Zap!
					value.removeAtom(atIdx);
				}
			} else {
				// V1, K2 or K3
				if (bond.isToAtomWithIdx(atIdx)) {
					// K2 - change it
					// atKey = gc.markForCleanup(key.getAtomWithIdx(atIdx),
					// localGcWave);
					atKey = gc.markForCleanup(new Atom(0));
					atKey.setAtomicNum(0);
					// atKey.setIsotope(501);
					atKey.setProp(AP_ISOTOPIC_LABEL, "501");
					key.replaceAtom(atIdx, atKey);
				} else {
					// K3 - Zap!
					key.removeAtom(atIdx);
				}
			}
		}

		if (isChiral || (!hasUndefinedChirality && treatProchiralAsChiral)) {
			assignAPChirality(value, localGcWave);
			assignAPChirality(key, localGcWave);
		}

		assignCreatedDblBondGeometry(key, localGcWave);
		assignCreatedDblBondGeometry(value, localGcWave);
		canonicaliseDuplicateKeyComponents(value, localGcWave, key);
		applyAPIsotopicLabels(key, localGcWave);
		applyAPIsotopicLabels(value, localGcWave);

		String retVal = key.MolToSmiles(true) + "." + value.MolToSmiles(true);
		gc.cleanupMarkedObjects(localGcWave);

		return new MulticomponentSmilesFragmentParser(retVal + ".[501*][500*]");
	}

	/**
	 * Private method to perform fragmentation when attachment point atoms will
	 * collide (i.e. geminal break)
	 */
	private MulticomponentSmilesFragmentParser fragmentLong(Set<BondIdentifier> bonds,
			boolean treatProchiralAsChiral) throws MoleculeFragmentationException {

		int localGcWave = gcWave.getAndIncrement();
		RWMol tmp = gc.markForCleanup(new RWMol(mol), localGcWave);

		// A list of the atom indices used in bond breads
		Set<Long> APLookup = new HashSet<>();
		// Firstly, we break all the marked bonds
		for (BondIdentifier bond : bonds) {
			// Break the bond
			tmp.removeBond(bond.getStartIdx(), bond.getEndIdx());
			for (int atomIdx : bond) {
				APLookup.add((long) atomIdx);
			}
		}

		// Now we need to work through each bond, looking for the leaf and core
		// atom indices
		List<Long> coreIdxs = null;
		List<List<Long>> leafIdxs = new ArrayList<>();
		Set<Long> foundAPs = new HashSet<>();

		for (BondIdentifier bond : bonds) {
			// Now start at the first index of the first bond, and list all the
			// atoms
			// IDs visited and count the APs
			for (long bondEndIndex : bond) {
				if (!foundAPs.contains(bondEndIndex)) {
					int apCount = 0;
					List<Long> visitedAtomIDs = new ArrayList<>();
					Set<Long> atomLayer = new HashSet<>();
					atomLayer.add(bondEndIndex);
					Set<Long> nextAtomLayer = new HashSet<>();
					while (atomLayer.size() > 0) {
						for (Long atIdx : atomLayer) {
							visitedAtomIDs.add(atIdx);
							Atom at0 = gc.markForCleanup(tmp.getAtomWithIdx(atIdx), localGcWave);
							if (APLookup.contains(atIdx)) {
								for (BondIdentifier bond2 : bonds) {
									// Make sure we count all APs to the atom
									if (bond2.isToAtomWithIdx(atIdx)) {
										apCount++;
									}
								}
								foundAPs.add(atIdx);
							}
							Bond_Vect bnds0 = gc.markForCleanup(at0.getBonds(), localGcWave);
							for (int i = 0; i < bnds0.size(); i++) {
								Bond bnd = gc.markForCleanup(bnds0.get(i), localGcWave);
								nextAtomLayer.add(bnd.getBeginAtomIdx());
								nextAtomLayer.add(bnd.getEndAtomIdx());
							}
						}
						nextAtomLayer.removeAll(visitedAtomIDs);
						atomLayer = new HashSet<>(nextAtomLayer);
						nextAtomLayer.clear();
					}

					// If we are here and apCount is neither 1 (we were on a
					// leaf) or
					// bonds.size() (we were on the core) then this is a broken
					// fragmentation
					if (apCount == 1) {
						if (bonds.size() == 1 && coreIdxs == null) {
							coreIdxs = new ArrayList<>(visitedAtomIDs);
						} else {
							leafIdxs.add(new ArrayList<>(visitedAtomIDs));
						}
					} else if (apCount == bonds.size() && coreIdxs == null) {
						coreIdxs = new ArrayList<>(visitedAtomIDs);
					} else {
						gc.cleanupMarkedObjects(localGcWave);
						throw new MoleculeFragmentationException();
					}
				}
			}
		}

		// A final check...
		if (leafIdxs.size() != bonds.size() || coreIdxs == null) {
			// something still managed to go wrong..
			gc.cleanupMarkedObjects(localGcWave);
			throw new MoleculeFragmentationException();
		}

		// Now, for convenience we add the core to the leaf list
		leafIdxs.add(coreIdxs);

		// And generate a new RWMol for each fragmnet
		RWMol[] frags = new RWMol[leafIdxs.size()];
		for (int i = 0; i < leafIdxs.size(); i++) {
			frags[i] = gc.markForCleanup(new RWMol(mol), localGcWave);
		}

		// Now we can loop down the atoms, processing each fragment side-by-side
		// as we go..
		for (long atIdx = mol.getNumAtoms() - 1; atIdx >= 0; atIdx--) {
			// loop through the fragments
			for (int i = 0; i < frags.length; i++) {
				Atom at;

				// For each atom there are three options:
				// 1. Visited - we keep it
				// 2. Not visited but is AP - we change it
				// 3. Not visited, not AP, so we delete it
				if (!leafIdxs.get(i).contains(atIdx)) {
					// 2. or 3.
					if (APLookup.contains(atIdx)) {
						// 2 - change it
						// Find the AP index
						int apIdx = -1;
						for (BondIdentifier bond : bonds) {
							if (bond.isToAtomWithIdx(atIdx)) {
								// Need to check that the bond is to one of the
								// atoms we keep
								for (int terminalAtomId : bond) {
									if (leafIdxs.get(i).contains((long) terminalAtomId)) {
										apIdx = bond.getFragmentationIndex();
										continue;
									}
								}
							}
							if (apIdx > 0) {
								continue;
							}
						}
						if (apIdx > 0) {
							// Change it
							// at = gc.markForCleanup(
							// frags[i].getAtomWithIdx(atIdx), localGcWave);
							at = gc.markForCleanup(new Atom(0), localGcWave);
							at.setAtomicNum(0);

							// at.setIsotope(apIdx);
							at.setProp(AP_ISOTOPIC_LABEL, "" + apIdx);
							frags[i].replaceAtom(atIdx, at);
						} else {
							// Not in this fragment, and not bonded to this
							// fragment
							// ZAP!
							frags[i].removeAtom(atIdx);
						}

					} else {
						// 3 - Zap!
						frags[i].removeAtom(atIdx);
					}
				}
			}
		}

		if (isChiral || (!hasUndefinedChirality && treatProchiralAsChiral)) {
			for (int i = 0; i < frags.length; i++) {
				assignAPChirality(frags[i], localGcWave);
			}
		}

		canonicaliseDuplicateKeyComponents(frags[frags.length - 1], localGcWave,
				Arrays.copyOf(frags, frags.length - 1));

		for (int i = 0; i < frags.length; i++) {
			assignCreatedDblBondGeometry(frags[i], localGcWave);
			if (i < frags.length - 1) {
				applyAPIsotopicLabels(frags[i], localGcWave);
			}
		}

		// Finally put it all together...
		StringBuilder sb = new StringBuilder(frags[0].MolToSmiles(true));
		for (int i = 1; i < frags.length - 1; i++) {
			sb.append(".");
			sb.append(frags[i].MolToSmiles(true));
		}
		sb.append(".").append(getCanonicalValueSMILES(frags[frags.length - 1], localGcWave));
		gc.cleanupMarkedObjects(localGcWave);
		return new MulticomponentSmilesFragmentParser(sb.toString());
	}

	/**
	 * Private method to perform fragmentation when attachment point atoms will
	 * not collide
	 */
	private MulticomponentSmilesFragmentParser fragmentShort(Set<BondIdentifier> bonds,
			boolean treatProchiralAsChiral) throws MoleculeFragmentationException {
		int localGcWave = gcWave.getAndIncrement();
		RWMol tmp = gc.markForCleanup(new RWMol(mol), localGcWave);

		// A lookup of the atom index and the AP index
		Map<Long, Integer> APLookup = new HashMap<>();
		// Firstly, we break all the marked bonds
		for (BondIdentifier bond : bonds) {
			// Break the bond
			tmp.removeBond(bond.getStartIdx(), bond.getEndIdx());
			for (int atomIdx : bond) {
				APLookup.put((long) atomIdx, bond.getFragmentationIndex());
			}
		}

		// Now start at the first index of the first bond, and list all the atom
		// IDs visited and count the APs
		int apCount = 0;
		List<Long> visitedAtomIDs = new ArrayList<>();
		Set<Long> atomLayer = new HashSet<>();
		atomLayer.add((long) bonds.iterator().next().getStartIdx());
		Set<Long> nextAtomLayer = new HashSet<>();
		while (atomLayer.size() > 0) {
			for (Long atIdx : atomLayer) {
				visitedAtomIDs.add(atIdx);
				Atom at0 = gc.markForCleanup(tmp.getAtomWithIdx(atIdx), localGcWave);
				if (APLookup.containsKey(atIdx)) {
					apCount++;
				}
				Bond_Vect bnds0 = gc.markForCleanup(at0.getBonds(), localGcWave);
				for (int i = 0; i < bnds0.size(); i++) {
					Bond bnd = gc.markForCleanup(bnds0.get(i), localGcWave);
					nextAtomLayer.add(bnd.getBeginAtomIdx());
					nextAtomLayer.add(bnd.getEndAtomIdx());
				}
			}
			nextAtomLayer.removeAll(visitedAtomIDs);
			atomLayer = new HashSet<>(nextAtomLayer);
			nextAtomLayer.clear();
		}

		// If we are here and apCount is neither 1 (we were on a leaf) or
		// bonds.size() (we were on the core) then this is a broken
		// fragmentation
		if (apCount != 1 && apCount != bonds.size()) {
			gc.cleanupMarkedObjects(localGcWave);
			throw new MoleculeFragmentationException();
		}

		if (apCount != bonds.size()) {
			// We were on a leaf (or n=1, which is degenerate) and need to try
			// again
			apCount = 0;
			visitedAtomIDs = new ArrayList<>();
			atomLayer = new HashSet<>();
			atomLayer.add((long) bonds.iterator().next().getEndIdx());
			nextAtomLayer = new HashSet<>();
			while (atomLayer.size() > 0) {
				for (Long atIdx : atomLayer) {
					visitedAtomIDs.add(atIdx);
					Atom at0 = gc.markForCleanup(tmp.getAtomWithIdx(atIdx), localGcWave);
					if (APLookup.containsKey(atIdx)) {
						apCount++;
					}
					Bond_Vect bnds0 = gc.markForCleanup(at0.getBonds(), localGcWave);
					for (int i = 0; i < bnds0.size(); i++) {
						Bond bnd = gc.markForCleanup(bnds0.get(i), localGcWave);
						nextAtomLayer.add(bnd.getBeginAtomIdx());
						nextAtomLayer.add(bnd.getEndAtomIdx());
					}
				}
				nextAtomLayer.removeAll(visitedAtomIDs);
				atomLayer = new HashSet<>(nextAtomLayer);
				nextAtomLayer.clear();
			}
		}

		// If we are here and apCount is neither 1 (we were on a leaf) or
		// bonds.size() (we were on the core) then this is a broken
		// fragmentation
		if (apCount != 1 && apCount != bonds.size()) {
			gc.cleanupMarkedObjects(localGcWave);
			throw new MoleculeFragmentationException();
		}

		// Now, visitedAtomIDs contains a list of the atoms in the 'value' or
		// core
		// On a fresh copy, we delete those atoms not needed

		RWMol value = gc.markForCleanup(new RWMol(mol), localGcWave);
		RWMol key = gc.markForCleanup(new RWMol(mol), localGcWave);

		// Now we need to Iterate down through the List of atoms (start with
		// highest IDX as renumbering will occur
		for (long atIdx = mol.getNumAtoms() - 1; atIdx >= 0; atIdx--) {
			// for (Long atIdx : atomIDs) {
			Atom atVal, atKey;

			// For each atom there are three options for CORE (VALUE)
			// V1. Visited - we keep it
			// V2. Not visited but is AP - we change it
			// V3. Not visited, not AP, so we delete it
			// And for the KEY:
			// K1. Not Visited - we keep it
			// K2. Visited, but is AP - change it
			// k3. Visited, not AP - delete
			if (!visitedAtomIDs.contains(atIdx)) {
				// K1, V2. or V3.
				if (APLookup.containsKey(atIdx)) {
					// V2 - change it
					// atVal = gc.markForCleanup(value.getAtomWithIdx(atIdx),
					// localGcWave);
					atVal = gc.markForCleanup(new Atom(0), localGcWave);
					atVal.setAtomicNum(0);
					// atVal.setIsotope(APLookup.get(atIdx));
					atVal.setProp(AP_ISOTOPIC_LABEL, "" + APLookup.get(atIdx));
					value.replaceAtom(atIdx, atVal);
					// If we change it, we need to make sure it isnt bonded to
					// any attachment points with a different id
					// Easiest way to do this is delete all bonds between this
					// atom and any atom in the APLookup with both a different
					// lookup value and a lower index - even if they dont exist!
					int apIndex = APLookup.get(atIdx);
					for (Entry<Long, Integer> ent : APLookup.entrySet()) {
						if (ent.getKey() < atIdx && ent.getValue() != apIndex) {
							value.removeBond(atIdx, ent.getKey());
						}
					}
				} else {
					// V3 - Zap!
					value.removeAtom(atIdx);
				}
			} else {
				// V1, K2 or K3
				if (APLookup.containsKey(atIdx)) {
					// K2 - change it
					// atKey = gc.markForCleanup(key.getAtomWithIdx(atIdx),
					// localGcWave);
					atKey = gc.markForCleanup(new Atom(0), localGcWave);
					atKey.setAtomicNum(0);
					int apIndex = APLookup.get(atIdx);
					// atKey.setIsotope(apIndex);
					atKey.setProp(AP_ISOTOPIC_LABEL, "" + apIndex);
					key.replaceAtom(atIdx, atKey);
					// If we change it, we need to make sure it isnt bonded to
					// any attachment poins with a different id
					// Easiest way to do this is delete all bonds between this
					// atom and any atom in the APLookup with both a different
					// lookup value and a lower index - even if they dont exist!
					for (Entry<Long, Integer> ent : APLookup.entrySet()) {
						if (ent.getKey() < atIdx && ent.getValue() != apIndex) {
							key.removeBond(atIdx, ent.getKey());
						}
					}
				} else {
					// K3 - Zap!
					key.removeAtom(atIdx);
				}
			}
		}

		if (isChiral || (!hasUndefinedChirality && treatProchiralAsChiral)) {
			assignAPChirality(value, localGcWave);
			assignAPChirality(key, localGcWave);
		}

		assignCreatedDblBondGeometry(key, localGcWave);
		assignCreatedDblBondGeometry(value, localGcWave);

		canonicaliseDuplicateKeyComponents(value, localGcWave, key);

		applyAPIsotopicLabels(key, localGcWave);
		// applyAPIsotopicLabels(value, localGcWave);

		String retVal = key.MolToSmiles(true) + "." + getCanonicalValueSMILES(value, localGcWave);// value.MolToSmiles(true);
		gc.cleanupMarkedObjects(localGcWave);

		// RDKit output multicomponents as ':' separated
		return new MulticomponentSmilesFragmentParser(retVal.replace(":", "."));
	}

	// /**
	// * Check whether the molecule argument has any defined chiral centres
	// *
	// * @param mol2
	// * The molecule to check
	// * @param wave
	// * The wave index to use for GC
	// *
	// */
	// protected boolean checkIsChiral(ROMol mol2, int wave) {
	// for (int i = 0; i < mol2.getNumAtoms(); i++) {
	// Atom at = gc.markForCleanup(mol2.getAtomWithIdx(i), wave);
	// if (at.getChiralTag() == ChiralType.CHI_TETRAHEDRAL_CCW
	// || at.getChiralTag() == ChiralType.CHI_TETRAHEDRAL_CW) {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * This assigns chirality to unassigned potentially chiral C atoms which are
	 * not flagged as {@value #UNSPECIFIED_STEREOCENTRE}
	 * 
	 * @param component
	 *            The molecule to fix
	 * @param localGcWave
	 *            The GC wave index for garbage collection
	 */
	private void assignAPChirality(RWMol component, int localGcWave) throws MolSanitizeException {

		try {
			component.sanitizeMol();
			// Flag possible stereocentres
			RDKFuncs.assignStereochemistry(component, false, true, true);
		} catch (MolSanitizeException e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry" + e.message() == null ? ""
						: e.message());
			}
			return;
		} catch (Exception e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry" + e.getMessage() == null ? ""
						: e.getMessage());
			}
			return;
		}

		// Now check all atoms
		for (AtomIterator iter = gc.markForCleanup(component.beginAtoms(), localGcWave); iter
				.ne(gc.markForCleanup(component.endAtoms(), localGcWave)); gc
						.markForCleanup(iter.next(), localGcWave)) {
			Atom at = gc.markForCleanup(iter.getAtom(), localGcWave);
			// At present, only fix Carbon... (Otherwise e.g. MeP(=O)(OH)[*]
			// is chiral!) and
			if (at.getAtomicNum() == 6 && !at.hasProp(UNSPECIFIED_STEREOCENTRE)
					&& at.hasProp("_ChiralityPossible")
					&& at.getChiralTag() == ChiralType.CHI_UNSPECIFIED) {
				// Just need to chose one assignment
				// arbitrarily but consistently
				at.setChiralTag(ChiralType.CHI_TETRAHEDRAL_CW);
			}
		}
	}

	/**
	 * Assigns double bond geometry to newly created asymmetric double bonds
	 * which are not flagged as {@value #UNSPECIFIED_DOUBLE_BOND}
	 * 
	 * @param component
	 *            The molecule to fix
	 * @param localGcWave
	 *            The gc wave index
	 */
	private void assignCreatedDblBondGeometry(RWMol component, int localGcWave) {
		try {
			component.sanitizeMol();
			// This adds a list of stereoatoms to previously unassigned double
			// bonds
			RDKFuncs.findPotentialStereoBonds(component, false);
		} catch (MolSanitizeException e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry" + e.message() == null ? ""
						: e.message());
			}
			return;
		} catch (Exception e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry" + e.getMessage() == null ? ""
						: e.getMessage());
			}
			return;
		}

		// Now loop through the bonds
		for (BondIterator iter = gc.markForCleanup(component.beginBonds(), localGcWave); iter
				.ne(gc.markForCleanup(component.endBonds(), localGcWave)); gc
						.markForCleanup(iter.next(), localGcWave)) {

			Bond bd = gc.markForCleanup(iter.getBond(), localGcWave);

			if (bd.getBondType() == BondType.DOUBLE && !bd.hasProp(UNSPECIFIED_DOUBLE_BOND)) {
				// We only worry about double bonds

				Int_Vect stereoAtoms = gc.markForCleanup(bd.getStereoAtoms(), localGcWave);
				if (stereoAtoms.size() > 0) {
					// And only if they have stereo atoms listed

					// Now we need to find the adjacent bonds from the double
					// bond termini to the listed stereoatoms
					Bond beginStereoBond = null;
					Bond endStereoBond = null;
					for (int i = 0; i < stereoAtoms.size(); i++) {
						if (beginStereoBond == null) {
							beginStereoBond = gc.markForCleanup(component.getBondBetweenAtoms(
									bd.getBeginAtomIdx(), stereoAtoms.get(i)), localGcWave);
						}
						if (endStereoBond == null) {
							endStereoBond = gc.markForCleanup(component.getBondBetweenAtoms(
									bd.getEndAtomIdx(), stereoAtoms.get(i)), localGcWave);
						}
					}

					// And now we need to assign them arbitrarily and
					// consistently if they are not already assigned
					if (beginStereoBond.getBondDir() == BondDir.NONE) {
						beginStereoBond.setBondDir(BondDir.ENDUPRIGHT);
					}
					if (endStereoBond.getBondDir() == BondDir.NONE) {
						endStereoBond.setBondDir(BondDir.ENDUPRIGHT);
					}
				}
			}

		}

	}

	/**
	 * <p>
	 * This method is called finally to apply isotopic labels to the attachment
	 * point '*' atoms, from the stored property.
	 * </p>
	 * <p>
	 * This method is used to avoid arbitrary stereochemistry assignment (bonds
	 * and centres) resulting only from the isotopic labels of the attachment
	 * points. e.g. {@code ClC(Cl)C[C@H](O)C} with 2 Cl-C breaks should give
	 * {@code Cl-[500*].Cl-[501*] [500*]C([501*])C[C@H](O)C} and not
	 * {@code Cl-[500*].Cl-[501*] [500*][C@H]([501*])C[C@H](O)C}
	 * </p>
	 * <p>
	 * The method is called as a final step, after stereochemistry and double
	 * bond geometry assignment
	 * </p>
	 * 
	 * @param component
	 *            The molecule component to apply labels to
	 * @param localGCWave
	 *            The GC Wave for garbage collection
	 */
	private void applyAPIsotopicLabels(RWMol component, int localGcWave) {
		for (AtomIterator iter = gc.markForCleanup(component.beginAtoms(), 1); iter.ne(
				gc.markForCleanup(component.endAtoms(), 1)); gc.markForCleanup(iter.next(), 1)) {
			Atom at = gc.markForCleanup(iter.getAtom(), localGcWave);

			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				int isotope = Integer.parseInt(at.getProp(AP_ISOTOPIC_LABEL).trim());
				at.setIsotope(isotope);
			}
		}
	}

	private void canonicaliseDuplicateKeyComponents(RWMol value, int localGcWave,
			RWMol... keyComponents) {

		// System.out.println("Value:\t" + value.MolToSmiles(true));
		// listAPs(value, localGcWave);
		// System.out.println("Keys:");

		if (keyComponents.length == 1) {
			ROMol_Vect comps = gc.markForCleanup(
					RDKFuncs.getMolFrags(keyComponents[0], true, null, null, false), localGcWave);
			if (comps.size() > 1) {
				// We had a key with multiple components in a single molecule
				RWMol[] comps2 = new RWMol[(int) comps.size()];
				for (int i = 0; i < comps.size(); i++) {
					comps2[i] = gc.markForCleanup(new RWMol(comps.get(i)), localGcWave);
				}

				// Now canonicalise
				canonicaliseDuplicateKeyComponents(value, localGcWave, comps2);

				// and now put them back into keyComponents
				keyComponents[0].clear();
				for (RWMol comp : comps2) {
					keyComponents[0].insertMol(comp);
				}
			}
		}

		boolean hasDuplicateComponent = false;
		Map<String, List<RWMol>> comps = new LinkedHashMap<>();
		for (RWMol keyComp : keyComponents) {
			String smi = keyComp.MolToSmiles(true);
			if (comps.containsKey(smi)) {
				hasDuplicateComponent = true;
			} else {
				comps.put(smi, new ArrayList<>());
			}
			comps.get(smi).add(keyComp);
		}

		if (hasDuplicateComponent) {
			// System.out.print(value.getAtomWithIdx(0).getAtomicNum()+"\t");
			canonicalizeAtomOrder(value);
			// System.out.println(value.getAtomWithIdx(0).getAtomicNum() + "
			// (post-canonicalisation)");

			for (Entry<String, List<RWMol>> ent : comps.entrySet()) {
				if (ent.getValue().size() > 1) {
					// This component duplicates
					TreeMap<Long, RWMol> sortedClashes = new TreeMap<>();
					Set<Integer> availableIndices = new LinkedHashSet<>();
					Map<RWMol, Integer> keyCompIndexLookup = new LinkedHashMap<>();

					for (RWMol comp : ent.getValue()) {
						Integer apIdx = getAPIndex(comp, localGcWave);
						Long atIdx = findIndexOfLabelledAtom(value, apIdx, localGcWave);
						sortedClashes.put(atIdx, comp);
						availableIndices.add(apIdx);
						keyCompIndexLookup.put(comp, apIdx);
					}

					// Sort the available indices
					List<Integer> availableIndices2 = new ArrayList<>(availableIndices);
					java.util.Collections.sort(availableIndices2);

					// Now we need to rejig the indices on the actual molecule
					// objects
					int i = 0;
					for (Entry<Long, RWMol> ent1 : sortedClashes.entrySet()) {
						int nextIndex = availableIndices2.get(i++);
						// Change the index on the value
						gc.markForCleanup(value.getAtomWithIdx(ent1.getKey()), localGcWave)
								.setProp(AP_ISOTOPIC_LABEL, "" + nextIndex);
						// listAPs(value, localGcWave);
						// And on the key component
						gc.markForCleanup(ent1.getValue()
								.getAtomWithIdx(findIndexOfLabelledAtom(ent1.getValue(),
										keyCompIndexLookup.get(ent1.getValue()), localGcWave)),
								localGcWave).setProp(AP_ISOTOPIC_LABEL, "" + nextIndex);
					}
					availableIndices.clear();
					availableIndices2.clear();
					keyCompIndexLookup.clear();
					sortedClashes.clear();
				}
			}
		}

		comps.clear();
	}

	/**
	 * Function to return the canonical SMILES of an input {@link RWMol} object,
	 * and to reorder the atoms of that object to the canonical order present in
	 * the SMILES
	 * 
	 * @param value
	 *            The molecule to canonicalise
	 * @return The canonical SMILES
	 */
	private String canonicalizeAtomOrder(RWMol value) {
		String smi = value.MolToSmiles(true);
		String newIdxs = value.getProp("_smilesAtomOutputOrder");
		// System.out.println(smi + "\t::\t" + newIdxs);
		newIdxs = newIdxs.replace("[", "").replace("]", "");
		UInt_Vect newIdxVect = new UInt_Vect();
		for (String idx : newIdxs.split(",")) {
			if (!"".equals(idx)) {
				newIdxVect.add(Integer.parseInt(idx.trim()));
			}
		}
		ROMol tmp = RDKFuncs.renumberAtoms(value, newIdxVect);
		value.clear();
		value.insertMol(tmp);
		tmp.delete();
		newIdxVect.delete();
		return smi;

	}

	@SuppressWarnings("unused")
	private void listAPs(RWMol value, int localGcWave) {
		for (int atIdx = 0; atIdx < value.getNumAtoms(); atIdx++) {
			Atom at = gc.markForCleanup(value.getAtomWithIdx(atIdx), localGcWave);
			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				System.out.println("\t" + atIdx + "\t" + at.getProp(AP_ISOTOPIC_LABEL));
			}
		}

	}

	private long findIndexOfLabelledAtom(RWMol component, int apIndex, int localGcWave) {
		long retVal = -1; // -1 -> Not fount
		for (AtomIterator iter = gc.markForCleanup(component.beginAtoms(), 1); iter.ne(
				gc.markForCleanup(component.endAtoms(), 1)); gc.markForCleanup(iter.next(), 1)) {
			Atom at = gc.markForCleanup(iter.getAtom(), localGcWave);

			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				int isotope = Integer.parseInt(at.getProp(AP_ISOTOPIC_LABEL).trim());
				if (isotope == apIndex) {
					retVal = at.getIdx();
					break;
				}
			}
		}
		return retVal;
	}

	private int getAPIndex(RWMol component, int localGcWave) {
		int retVal = -1;
		for (AtomIterator iter = gc.markForCleanup(component.beginAtoms(), 1); iter.ne(
				gc.markForCleanup(component.endAtoms(), 1)); gc.markForCleanup(iter.next(), 1)) {
			Atom at = gc.markForCleanup(iter.getAtom(), localGcWave);

			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				retVal = Integer.parseInt(at.getProp(AP_ISOTOPIC_LABEL).trim());
				break;
			}
		}
		return retVal;
	}

	// private List<Long> getOrderedAttachmentPointIndices(RWMol value, int
	// localGcWave) {
	// List<Long> retVal = new ArrayList<>();

	// This was supposed to find the isotope labels, and then apply them to
	// the canonical SMILES once generated. That, however, requires a way of
	// reordering the atoms to the canonical order. RWMol#canonicalize()
	// does not do that!

	// Instead, we turn the smiles back into a query molecule to generate a
	// matcher to use as lookup

	// }

	private String getCanonicalValueSMILES(RWMol value, int localGcWave) {
		String smi = value.MolToSmiles(true);
		// System.out.print(smi + "\t-->\t");
		RWMol querymol = RWMol.MolFromSmarts(smi);
		Match_Vect mVect = value.getSubstructMatch(querymol, true);

		// A lookup between the canonical and non-canonical forms. The 'key' is
		// the index of the ap in the canonical form, and the value is the
		// isotopic label to apply
		Map<Integer, Integer> matchLookup = new TreeMap<>();
		for (int i = 0; i < mVect.size(); i++) {
			Atom at1 = gc.markForCleanup(value.getAtomWithIdx(mVect.get(i).getFirst()),
					localGcWave);
			if (at1.getAtomicNum() == 0) {
				matchLookup.put(mVect.get(i).getSecond(),
						Integer.parseInt(at1.getProp(AP_ISOTOPIC_LABEL).trim()));
			}
		}

		// Now we put the labels into the smi
		for (int isoLabel : matchLookup.values()) {
			smi = smi.replaceFirst("\\[\\*\\]", "[" + isoLabel + "*]");
		}
		querymol.delete();
		mVect.delete();
		// System.out.println(smi);
		return smi;
	}
	// protected static int countInversions(List<Long> list) {
	// List<Long> toSort = new ArrayList<>(list);
	// List<Long> sorted = new ArrayList<>(list);
	// Collections.sort(sorted);
	// int retVal = 0;
	// while (!toSort.equals(sorted)) {
	// for (int i = 0; i < toSort.size() - 1; i++) {
	// if (toSort.get(i) > toSort.get(i + 1)) {
	// Collections.swap(toSort, i, i + 1);
	// retVal++;
	// }
	// }
	// }
	// return retVal;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		gc.cleanupMarkedObjects();
		super.finalize();
	}
}
