/*******************************************************************************
 * Copyright (c) 2015, 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.fragmentors;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.RDKit.Atom;
import org.RDKit.Atom.ChiralType;
import org.RDKit.Atom.HybridizationType;
import org.RDKit.Bond;
import org.RDKit.Bond.BondDir;
import org.RDKit.Bond.BondStereo;
import org.RDKit.Bond.BondType;
import org.RDKit.Bond_Vect;
import org.RDKit.ColourPalette;
import org.RDKit.ConformerException;
import org.RDKit.DrawColour;
import org.RDKit.Int_Vect;
import org.RDKit.Match_Vect;
import org.RDKit.Match_Vect_Vect;
import org.RDKit.MolDraw2DSVG;
import org.RDKit.MolSanitizeException;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.RWMol;
import org.RDKit.UInt_Vect;
import org.knime.base.data.xml.SvgCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractMulticomponentFragmentationParser;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifier;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifierSelfpairSet;
import com.vernalis.knime.mmp.frags.rdkit.RWMolMulticomponentFragmentationParser;
import com.vernalis.knime.mmp.fragutils.RWMolFragmentationUtilsFactory;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

/**
 * RDKit Toolkit implementation of {@link AbstractFragmentationFactory}
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
public class RWMolFragmentationFactory
		extends AbstractFragmentationFactory<RWMol, ROMol> {

	/** Property name for AP Count */
	protected static final String AP_COUNT = "APCount";

	protected static final double BREAK_ATOM_END_RADIUS_SCALE = 1.0;
	/**
	 * Wave ID to be used to objects to be persisted until the factory is closed
	 */
	protected static final long ENDURANCE_WAVE_ID = 1L;
	/**
	 * Encapsulated to allow lazy instantiation - so that the superclass can use
	 * this field
	 */
	private SWIGObjectGarbageCollector2WaveSupplier gc;

	protected ROMol dblBondMatch, tetrahedralCarbonMatch;
	protected static final int RENDERER_WIDTH = 300;
	protected static final int RENDERER_HEIGHT = 300;

	/**
	 * Constructor
	 * 
	 * @param mol
	 *            The molecule to be fragmented
	 * @param bondMatch
	 *            The matcher molecule
	 * @param removeHs
	 *            Should explicit H atoms be removed after fragmentation?
	 * @param verboseLogging
	 *            Should the logger be used
	 * @param treatProchiralAsChiral
	 *            Should prochiral centres be treated as chiral?
	 * @param maxNumberChangingHAs
	 *            The maximum number of changing heavy atoms
	 * @param minCnstToVarAtmRatio
	 *            The minumum ratio of constant to changing heavy atoms
	 * @throws ClosedFactoryException
	 * @throws ToolkitException
	 */
	public RWMolFragmentationFactory(RWMol mol, ROMol bondMatch,
			boolean removeHs, boolean isHAdded, boolean verboseLogging,
			boolean treatProchiralAsChiral, Integer maxNumberChangingHAs,
			Double minCnstToVarAtmRatio, int maxLeafCacheSize)
			throws ClosedFactoryException, ToolkitException {
		super(mol, bondMatch, removeHs, isHAdded, verboseLogging,
				treatProchiralAsChiral, maxNumberChangingHAs,
				minCnstToVarAtmRatio, maxLeafCacheSize);
		dblBondMatch = getGc().markForCleanup(RWMol.MolFromSmarts("*=*"),
				ENDURANCE_WAVE_ID);
		tetrahedralCarbonMatch = getGc().markForCleanup(
				RWMol.MolFromSmarts("[C^3]"), ENDURANCE_WAVE_ID);
	}

	/**
	 * @return the gc object for ensuring cleanup of native objects
	 */
	protected SWIGObjectGarbageCollector2WaveSupplier getGc() {
		if (gc == null) {
			gc = new SWIGObjectGarbageCollector2WaveSupplier();
		}
		return gc;
	}

	@Override
	protected int getNumHeavyAtoms(RWMol mol) {
		return (int) mol.getNumHeavyAtoms();
	}

	@Override
	protected RWMol setMolValue(RWMol mol) {
		return getGc().markForCleanup(new RWMol(mol), ENDURANCE_WAVE_ID);
	}

	@Override
	protected ROMol setMatchValue(ROMol bondMatch) {
		return getGc().markForCleanup(new ROMol(bondMatch), ENDURANCE_WAVE_ID);
	}

	/**
	 * Double bonds which could have stereoisomerism but have none assigned are
	 * flagged with the property {@value #UNSPECIFIED_DOUBLE_BOND}
	 */
	@Override
	protected void markUnassignedPossibleDoubleBonds() {
		// This is to fix breakage from pull request
		// https://github.com/rdkit/rdkit/pull/1202
		Set<Integer> stereoNones = new HashSet<>();
		if (!RWMolFragmentationUtilsFactory.IS_PRE_SMILES_DBL_BOND_GEOMETRY_CHANGE) {
			for (int i = 0; i < mol.getNumBonds(); i++) {
				Bond bd = getGc().markForCleanup(mol.getBondWithIdx(i),
						ENDURANCE_WAVE_ID);
				if (bd.getBondType() == BondType.DOUBLE
						&& bd.getStereo() == BondStereo.STEREONONE) {
					stereoNones.add(i);
				}
			}
		}
		RDKFuncs.findPotentialStereoBonds(this.mol, false);
		atomsOfPossibleCreatedStereoDoubleBonds =
				new BitSet((int) mol.getNumAtoms());
		// Now loop through the bonds
		for (int i = 0; i < mol.getNumBonds(); i++) {
			Bond bd = getGc().markForCleanup(mol.getBondWithIdx(i),
					ENDURANCE_WAVE_ID);
			if (bd.getBondType() == BondType.DOUBLE) {
				if (getGc()
						.markForCleanup(bd.getStereoAtoms(), ENDURANCE_WAVE_ID)
						.size() > 0) {
					// We only worry about double bonds which could be stereo
					boolean isStereo = false;
					// Check that it *could* be a stereobond - we only
					// care about those that could be but arent
					if (bd.getBondDir() != BondDir.EITHERDOUBLE) {
						Bond_Vect bv = getGc().markForCleanup(
								getGc().markForCleanup(bd.getBeginAtom(),
										ENDURANCE_WAVE_ID).getBonds(),
								ENDURANCE_WAVE_ID);
						for (int j = 0; j < bv.size(); j++) {
							if (getGc()
									.markForCleanup(bv.get(j),
											ENDURANCE_WAVE_ID)
									.getBondDir() != BondDir.NONE
									&& getGc()
											.markForCleanup(bv.get(j),
													ENDURANCE_WAVE_ID)
											.getBondDir() != BondDir.UNKNOWN) {
								isStereo = true;
								break;
							}
						}
						if (!isStereo) {
							bv = getGc().markForCleanup(
									getGc().markForCleanup(bd.getEndAtom(),
											ENDURANCE_WAVE_ID).getBonds(),
									ENDURANCE_WAVE_ID);
							for (int j = 0; j < bv.size(); j++) {
								if (getGc()
										.markForCleanup(bv.get(j),
												ENDURANCE_WAVE_ID)
										.getBondDir() != BondDir.NONE
										&& getGc()
												.markForCleanup(bv.get(j),
														ENDURANCE_WAVE_ID)
												.getBondDir() != BondDir.UNKNOWN) {
									isStereo = true;
									break;
								}
							}
						}
					}
					if (!isStereo) {
						bd.setProp(UNSPECIFIED_DOUBLE_BOND, "1");
					} else {
						bd.setProp(SPECIFIED_DOUBLE_BOND, "1");
					}
				} else {
					// In some cases, e.g. the exocyclic ring = in
					// Br/C(=N\N=c1/nn[nH][nH]1)c1ccncc1 CHEMBL1410841
					// bd#getStereoAtoms() does not percieve any as it treats
					// the tetrazole as symmetrical
					// In this case, we need to check that both ends of the bond
					// dont actually have stereomarkers
					boolean startHasFlag = false;
					boolean endHasFlag = false;
					Bond_Vect bv = getGc().markForCleanup(
							getGc().markForCleanup(bd.getBeginAtom(),
									ENDURANCE_WAVE_ID).getBonds(),
							ENDURANCE_WAVE_ID);
					for (int j = 0; j < bv.size() && !startHasFlag; j++) {
						if ((getGc()
								.markForCleanup(bv.get(j), ENDURANCE_WAVE_ID)
								.getBondType() == BondType.SINGLE
								|| getGc()
										.markForCleanup(bv.get(j),
												ENDURANCE_WAVE_ID)
										.getBondType() == BondType.AROMATIC)
								&& (getGc()
										.markForCleanup(bv.get(j),
												ENDURANCE_WAVE_ID)
										.getBondDir() != BondDir.NONE
										&& getGc()
												.markForCleanup(bv.get(j),
														ENDURANCE_WAVE_ID)
												.getBondDir() != BondDir.UNKNOWN)) {
							startHasFlag = true;
						}
					}
					bv = getGc().markForCleanup(
							getGc().markForCleanup(bd.getEndAtom(),
									ENDURANCE_WAVE_ID).getBonds(),
							ENDURANCE_WAVE_ID);
					for (int j = 0; j < bv.size() && !endHasFlag; j++) {
						if ((getGc()
								.markForCleanup(bv.get(j), ENDURANCE_WAVE_ID)
								.getBondType() == BondType.SINGLE
								|| getGc()
										.markForCleanup(bv.get(j),
												ENDURANCE_WAVE_ID)
										.getBondType() == BondType.AROMATIC)
								&& (getGc()
										.markForCleanup(bv.get(j),
												ENDURANCE_WAVE_ID)
										.getBondDir() != BondDir.NONE
										&& getGc()
												.markForCleanup(bv.get(j),
														ENDURANCE_WAVE_ID)
												.getBondDir() != BondDir.UNKNOWN)) {
							endHasFlag = true;
						}
					}
					if (startHasFlag && endHasFlag) {
						bd.setProp(SPECIFIED_DOUBLE_BOND, "1");
					}
				}
				if (!bd.hasProp(UNSPECIFIED_DOUBLE_BOND)
						&& !bd.hasProp(SPECIFIED_DOUBLE_BOND)) {
					atomsOfPossibleCreatedStereoDoubleBonds
							.set((int) bd.getBeginAtomIdx());
					atomsOfPossibleCreatedStereoDoubleBonds
							.set((int) bd.getEndAtomIdx());
					hasNonflaggedDoubleBonds = true;
				}
			}
		}
		// Put stereonones back...
		if (!RWMolFragmentationUtilsFactory.IS_PRE_SMILES_DBL_BOND_GEOMETRY_CHANGE) {
			for (int i : stereoNones) {
				getGc().markForCleanup(mol.getBondWithIdx(i), ENDURANCE_WAVE_ID)
						.setStereo(BondStereo.STEREONONE);
			}
		}
	}

	/**
	 * Atoms which could be chiral but are not are flagged with the property
	 * {@value #UNSPECIFIED_STEREOCENTRE}. The {@link #isChiral} property is set
	 * if a defined chiral centre is found
	 */
	@Override
	protected void markUnassignedPossibleChiralCentres() {
		// Flag possible stereocentres
		RDKFuncs.assignStereochemistry(this.mol, false, true, true);
		possibleCreatedStereos = new BitSet((int) mol.getNumAtoms());
		// Now loop through the atoms
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			Atom at = getGc().markForCleanup(mol.getAtomWithIdx(i),
					ENDURANCE_WAVE_ID);
			if (at.hasProp("_ChiralityPossible")
					&& (at.getChiralTag() == ChiralType.CHI_UNSPECIFIED
							|| at.getChiralTag() == ChiralType.CHI_OTHER)) {
				at.setProp(UNSPECIFIED_STEREOCENTRE, "1");
				hasUndefinedChirality = true;
			} else if (!isChiral && (at
					.getChiralTag() == ChiralType.CHI_TETRAHEDRAL_CCW
					|| at.getChiralTag() == ChiralType.CHI_TETRAHEDRAL_CW)) {
				isChiral = true;
			} else if (at.getAtomicNum() == 6 && !at.getIsAromatic()
					&& at.getHybridization() == HybridizationType.SP3) {
				// Could possible become a stereocentre (NB this is a somewhat
				// crude check
				possibleCreatedStereos.set(i);
			}
		}
	}

	@Override
	protected String molToSmiles(RWMol mol,
			boolean removeIsotopicLabelsFromAP) {
		String smi = mol.MolToSmiles(true);
		if (removeIsotopicLabelsFromAP) {
			smi = smi.replaceAll("\\[\\d+\\*\\]", "[*]");
		}

		return smi;
	}

	@Override
	protected RWMol getComponentFromSmiles(String smiles,
			boolean moveAPIsotopesToAtomProperties) throws ToolkitException {
		final RWMol retVal = getGc().markForCleanup(RWMol.MolFromSmiles(smiles),
				ENDURANCE_WAVE_ID);
		if (moveAPIsotopesToAtomProperties) {
			for (int atIdx = 0; atIdx < retVal.getNumAtoms(); atIdx++) {
				Atom at = getGc().markForCleanup(retVal.getAtomWithIdx(atIdx),
						ENDURANCE_WAVE_ID);
				if (at.getAtomicNum() == 0 && at.getIsotope() != 0) {
					at.setProp(AP_ISOTOPIC_LABEL, "" + at.getIsotope());
					at.setIsotope(0);
				}
			}
		}
		return retVal;
	}

	@Override
	protected BitSet initialiseHeavyAtomMask() {
		BitSet retVal = new BitSet((int) mol.getNumAtoms());
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			if (getGc().markForCleanup(mol.getAtomWithIdx(i), ENDURANCE_WAVE_ID)
					.getAtomicNum() > 1) {
				retVal.set(i);
			}
		}
		return retVal;
	}

	@Override
	protected int[][] initialiseGraphNeighbours() {
		int[][] retVal = new int[(int) mol.getNumAtoms()][];
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			Atom at = getGc().markForCleanup(mol.getAtomWithIdx(i),
					ENDURANCE_WAVE_ID);
			Bond_Vect bonds = at.getBonds();
			int[] connections = new int[(int) bonds.size()];
			for (int j = 0; j < connections.length; j++) {
				connections[j] = (int) bonds.get(j).getOtherAtomIdx(i);
			}
			bonds.delete();
			retVal[i] = connections;
		}

		return retVal;
	}

	/**
	 * This method applies an arbitrary bond label to an unassigned directional
	 * bond attached to a double bond. The stereochemistry direction is applied
	 * to the bond with the lowest indexed atom (not necessarily the bond
	 * identified by getStereoAtoms() as this is canonicalised and removes
	 * isomers!)
	 * 
	 * @param component
	 *            the molecule to modify
	 * @param localGcWave
	 *            The current gc wave index to cleanup afterwards
	 * @param newDirectionalBondBeginAtomIdx
	 *            The start atom (i.e. that of the double bond to be assigned)
	 */
	protected void applyDirectionalBond(RWMol component, long localGcWave,
			int newDirectionalBondBeginAtomIdx) {
		// Get the candidate
		// Only consider single bonds...
		Bond_Vect possibleBonds = getGc().markForCleanup(getGc()
				.markForCleanup(component.getAtomWithIdx(
						newDirectionalBondBeginAtomIdx), localGcWave)
				.getBonds(), localGcWave);
		List<Integer> candidateAtomIds = new ArrayList<>();
		for (int j = 0; j < possibleBonds.size(); j++) {
			Bond bond =
					getGc().markForCleanup(possibleBonds.get(j), localGcWave);
			if (bond.getBondType() == BondType.SINGLE) {
				candidateAtomIds.add((int) bond
						.getOtherAtomIdx(newDirectionalBondBeginAtomIdx));
			}
		}
		// Pick the bond to the lowest indexed atom
		Collections.sort(candidateAtomIds);
		// Set arbitrarily a direction for the unassigned bond
		getGc().markForCleanup(component.getBondBetweenAtoms(
				newDirectionalBondBeginAtomIdx, candidateAtomIds.get(0)),
				localGcWave).setBondDir(BondDir.ENDUPRIGHT);
	}

	/**
	 * This re-sorts any duplicate key AP indices such that they are in order of
	 * appearence in the canonicalised 'value'
	 * 
	 * @param value
	 *            The 'value' of the fragmentation
	 * @param localGcWave
	 *            The gc wave id to use
	 * @param rwMols
	 *            The key components. If only 1 is supplied, then it is checked
	 *            for multiple components, and the method called recursively
	 *            after splitting
	 */
	protected void canonicaliseDuplicateKeyComponents(RWMol value,
			long localGcWave, RWMol... rwMols) {

		if (rwMols.length == 1) {
			ROMol_Vect comps = getGc().markForCleanup(
					RDKFuncs.getMolFrags(rwMols[0], false/* sanitize frags */,
							null/* frags to mol mapping */,
							null/* Atoms in each frag mapping */,
							false/* copy confs */),
					localGcWave);
			if (comps.size() > 1) {
				// We had a key with multiple components in a single molecule
				RWMol[] comps2 = new RWMol[(int) comps.size()];
				for (int i = 0; i < comps.size(); i++) {
					comps2[i] = getGc().markForCleanup(new RWMol(comps.get(i)),
							localGcWave);
				}

				// Now canonicalise
				canonicaliseDuplicateKeyComponents(value, localGcWave, comps2);

				// and now put them back into keyComponents
				rwMols[0].clear();
				for (RWMol comp : comps2) {
					rwMols[0].insertMol(comp);
				}
			}
			return;
		}

		boolean hasDuplicateComponent = false;
		Map<String, List<RWMol>> comps = new LinkedHashMap<>();
		for (RWMol keyComp : rwMols) {
			String smi = keyComp.MolToSmiles(true);
			if (comps.containsKey(smi)) {
				hasDuplicateComponent = true;
			} else {
				comps.put(smi, new ArrayList<>());
			}
			comps.get(smi).add(keyComp);
		}

		if (hasDuplicateComponent) {
			canonicalizeAtomOrder(value);
			for (Entry<String, List<RWMol>> ent : comps.entrySet()) {
				if (ent.getValue().size() > 1) {
					// This component duplicates
					TreeMap<Long, RWMol> sortedClashes = new TreeMap<>();
					Set<Integer> availableIndices = new LinkedHashSet<>();
					Map<RWMol, Integer> keyCompIndexLookup =
							new LinkedHashMap<>();

					for (RWMol comp : ent.getValue()) {
						Integer apIdx = getAPIndex(comp, localGcWave);
						Long atIdx = findIndexOfLabelledAtom(value, apIdx,
								localGcWave);
						sortedClashes.put(atIdx, comp);
						availableIndices.add(apIdx);
						keyCompIndexLookup.put(comp, apIdx);
					}

					// Sort the available indices
					List<Integer> availableIndices2 =
							new ArrayList<>(availableIndices);
					Collections.sort(availableIndices2);

					// Now we need to rejig the indices on the actual molecule
					// objects
					int i = 0;
					for (Entry<Long, RWMol> ent1 : sortedClashes.entrySet()) {
						int nextIndex = availableIndices2.get(i++);
						// Change the index on the value
						getGc().markForCleanup(
								value.getAtomWithIdx(ent1.getKey()),
								localGcWave)
								.setProp(AP_ISOTOPIC_LABEL, "" + nextIndex);
						// And on the key component
						getGc().markForCleanup(
								ent1.getValue()
										.getAtomWithIdx(findIndexOfLabelledAtom(
												ent1.getValue(),
												keyCompIndexLookup
														.get(ent1.getValue()),
												localGcWave)),
								localGcWave)
								.setProp(AP_ISOTOPIC_LABEL, "" + nextIndex);
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
	 * @param valueComponent
	 *            The molecule to canonicalise
	 * @return The canonical SMILES
	 */
	protected String canonicalizeAtomOrder(RWMol valueComponent) {
		// NB We use #renumberAtoms as opposed to value=RWMol.MolFromSmiles(SMI)
		// in order to retain all the atom properties
		String smi = valueComponent.MolToSmiles(true);
		String newIdxs = valueComponent.getProp("_smilesAtomOutputOrder");

		newIdxs = newIdxs.replace("[", "").replace("]", "");
		UInt_Vect newIdxVect = new UInt_Vect();
		for (String idx : newIdxs.split(",")) {
			if (!"".equals(idx)) {
				newIdxVect.add(Integer.parseInt(idx.trim()));
			}
		}
		try {
			valueComponent.sanitizeMol();
		} catch (MolSanitizeException e) {
			logger.warn(
					"Unable to sanitize molecule during atom order canonicalisation");
		}
		ROMol tmp = RDKFuncs.renumberAtoms(valueComponent, newIdxVect);

		valueComponent.clear();
		valueComponent.insertMol(tmp);
		tmp.delete();
		newIdxVect.delete();
		return smi;

	}

	/**
	 * @param valueComponent
	 *            the component to find the correct index
	 * @param apIndex
	 *            The attachment point index required
	 * @param localGcWave
	 *            The GC wave for cleanup
	 * @return The index of the AP atom with the specified index
	 */
	protected long findIndexOfLabelledAtom(ROMol valueComponent, int apIndex,
			long localGcWave) {
		long retVal = -1; // -1 -> Not fount
		for (int i = 0; i < valueComponent.getNumAtoms(); i++) {
			Atom at = getGc().markForCleanup(valueComponent.getAtomWithIdx(i),
					localGcWave);
			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				int isotope =
						Integer.parseInt(at.getProp(AP_ISOTOPIC_LABEL).trim());
				if (isotope == apIndex) {
					retVal = at.getIdx();
					break;
				}
			}
		}
		return retVal;
	}

	/**
	 * @param comp
	 *            the component to find the correct index
	 * @param localGcWave
	 *            The GC wave for cleanup
	 * @return The index of the AP atom
	 */
	protected int getAPIndex(ROMol comp, long localGcWave) {
		int retVal = -1;
		for (int i = 0; i < comp.getNumAtoms(); i++) {
			Atom at =
					getGc().markForCleanup(comp.getAtomWithIdx(i), localGcWave);
			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				retVal = Integer.parseInt(at.getProp(AP_ISOTOPIC_LABEL).trim());
				break;
			}
		}
		return retVal;
	}

	@Override
	public Set<BondIdentifier> identifyAllMatchingBonds()
			throws ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (matchingBonds == null) {
			// Lazy instantiation...
			Set<BondIdentifier> retVal = new TreeSet<>();
			Match_Vect_Vect bondMatches = mol.getSubstructMatches(bondMatch);
			int index = 500;
			for (int i = 0; i < bondMatches.size(); i++) {
				Match_Vect bond = bondMatches.get(i);
				int startId = bond.get(0).getSecond();
				int endId = bond.get(1).getSecond();
				// If this is an H-Added factory, then we only match X-H bonds
				// (any X)
				if (!isHAdded || !(heavyAtomMask.get(startId)
						&& heavyAtomMask.get(endId))) {
					int bondIdx =
							(int) getGc()
									.markForCleanup(mol.getBondBetweenAtoms(
											startId, endId), ENDURANCE_WAVE_ID)
									.getIdx();
					retVal.add(new BondIdentifier(startId, endId, bondIdx,
							isHAdded ? index : (index++)));
				}
			}
			bondMatches.delete();
			matchingBonds = retVal;
		}
		return matchingBonds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * getRendererCellType()
	 */
	@Override
	public DataType getRendererCellType() {
		return RWMolFragmentationUtilsFactory.RDKIT_RENDERER_CELLTYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * renderMatchingBonds(java.awt.Color)
	 */
	@Override
	public DataCell renderMatchingBonds(Color bondColour)
			throws ClosedFactoryException, ToolkitException, IOException {
		String svg = renderBondSetToSVGString(bondColour,
				identifyAllMatchingBonds());
		return svg == null ? DataType.getMissingCell()
				: SvgCellFactory.create(svg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * renderCuttableBonds(java.awt.Color, int)
	 */
	@Override
	public DataCell renderCuttableBonds(Color bondColour, int numCuts)
			throws ToolkitException, ClosedFactoryException, IOException {
		String svg = renderBondSetToSVGString(bondColour,
				identifyAllCuttableBonds(numCuts));
		return svg == null ? DataType.getMissingCell()
				: SvgCellFactory.create(svg);
	}

	/**
	 * Method to actually generate the SVG rendering string
	 * 
	 * @param bondColour
	 *            The colour to use for the bonds
	 * @return The SVG string
	 * @throws ClosedFactoryException
	 * @throws ToolkitException
	 */
	protected String renderBondSetToSVGString(Color bondColour,
			Set<BondIdentifier> bonds) throws ToolkitException {
		Int_Vect mb = new Int_Vect();
		// INT_DRAWCOLOUR_MAP colMap = new INT_DRAWCOLOUR_MAP();
		ColourPalette colMap = new ColourPalette();
		float[] rgb = bondColour.getColorComponents(null);
		float alpha = bondColour.getAlpha() / 255.0f;
		DrawColour col = new DrawColour(rgb[0], rgb[1], rgb[2]);
		bonds.forEach(x -> mb.add((int) x.getBondIdx()));
		bonds.forEach(x -> colMap.set((int) x.getBondIdx(), col));

		if (mol.getNumConformers() < 1) {
			mol.compute2DCoords();
		}
		String svg = null;
		MolDraw2DSVG drawer = null;
		int width = 300;
		int height = 300;
		try {
			drawer = new MolDraw2DSVG(width, height);
			drawer.drawMolecule(mol, null, mb, colMap, colMap);
			drawer.finishDrawing();
			svg = drawer.getDrawingText();
			// This swaps the svg header to match the scaleable version in the
			// Renderer to Image node
			if (svg != null) {
				// Make it scaleable
				svg = svg.replaceAll("(?s)<svg:svg.*?>",
						"<svg:svg contentScriptType=\"text/ecmascript\" zoomAndPan=\"magnify\"\n "
								+ "xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n xmlns:svg=\"http://www.w3.org/2000/svg\" baseProfile=\"full\"\n "
								+ "contentStyleType=\"text/css\" version=\"1.1\" width=\""
								+ width
								+ "px\"\n preserveAspectRatio=\"xMidYMid meet\"\n "
								+ "xmlns:rdkit=\"http://www.rdkit.org/xml\" height=\""
								+ height
								+ "px\"\n xmlns=\"http://www.w3.org/2000/svg\">");
				// Make the highlights have nice rounded ends
				svg = svg.replaceAll(
						"(<(svg:)?path .*?stroke-width:[\\d]+px;stroke-linecap:).*?;",
						"$1round;");
				// Apply transparency
				// Locale.ROOT keeps '.' as decimal as required by SVG standard
				// - see
				// https://forum.knime.com/t/mmp-molecule-fragment-node-output-svg-cells-and-system-locale/10315
				svg = svg.replaceAll(
						"(<(svg:)?path .*?stroke-width:[013456789][\\d]*px;.*?stroke-opacity:)1",
						"$1" + String.format(Locale.ROOT, "%.2f", alpha));
			}
		} catch (ConformerException e) {
			throw new ToolkitException(e.message(), e);
		} finally {
			if (drawer != null) {
				drawer.delete();
			}
			mb.delete();
			colMap.delete();
			col.delete();
		}
		return svg;
	}

	@Override
	public DataCell renderFragmentation(Set<BondIdentifier> bonds,
			Color breakingBondColour, Color keyColour, Color valueColour)
			throws IOException, IllegalArgumentException {

		try {
			return SvgCellFactory.create(renderFragmentationToSVGString(
					breakingBondColour, keyColour, valueColour, bonds));
		} catch (ToolkitException | MoleculeFragmentationException e) {
			return DataType.getMissingCell();
		}
	}

	/**
	 * Method to actually generate the SVG rendering string
	 * 
	 * @param bondColour
	 *            the colour to use for the cut bond(s) NB alpha values will be
	 *            ignored
	 * @param keyColour
	 *            the colour to use for the key
	 * @param valueColour
	 *            the colour to use for the value
	 * @param bonds
	 *            The bond(s) to break. If bonds is an instance of
	 *            {@link BondIdentifierSelfpairSet} then this is treated as two
	 *            cuts to a single bond, and the bond becomes the value and all
	 *            other atoms/bonds the key
	 * @return The SVG
	 * @throws ClosedFactoryException
	 * @throws ToolkitException
	 * @throws MoleculeFragmentationException
	 * @throws IllegalArgumentException
	 */
	protected String renderFragmentationToSVGString(Color bondColour,
			Color keyColour, Color valueColour, Set<BondIdentifier> bonds)
			throws ToolkitException, MoleculeFragmentationException {

		long localGcWave = getGc().getNextWaveIndex();
		Int_Vect bondsToHighlight =
				getGc().markForCleanup(new Int_Vect(), localGcWave);
		Int_Vect atomsToHighlight =
				getGc().markForCleanup(new Int_Vect(), localGcWave);
		// INT_DRAWCOLOUR_MAP bondColourMap =
		// getGc().markForCleanup(new INT_DRAWCOLOUR_MAP(), localGcWave);
		// INT_DRAWCOLOUR_MAP atomColourMap =
		// getGc().markForCleanup(new INT_DRAWCOLOUR_MAP(), localGcWave);

		ColourPalette bondColourMap =
				getGc().markForCleanup(new ColourPalette(), localGcWave);
		ColourPalette atomColourMap =
				getGc().markForCleanup(new ColourPalette(), localGcWave);

		boolean isBreakAlongBond = false;
		if (bonds instanceof BondIdentifierSelfpairSet) {
			isBreakAlongBond = true;
		} else if (bonds.size() == 2) {
			Iterator<BondIdentifier> iter = bonds.iterator();
			if (iter.next().isReverse(iter.next())) {
				isBreakAlongBond = true;
			}
		}

		DrawColour valueColor = convertColorToRdkitColor(
				isBreakAlongBond ? keyColour : valueColour, localGcWave);
		DrawColour keyColor = convertColorToRdkitColor(keyColour, localGcWave);
		DrawColour breakingBondColour = convertColorToRdkitColor(
				isBreakAlongBond ? valueColour : bondColour, localGcWave);

		String svg = null;
		BitSet valueAtomIDs = null;
		BitSet leafAtomIDs = null;
		try {
			// Now deal with the (optional) colour of the value
			valueAtomIDs = listValueAtomIds(bonds);
			if (valueColor != null) {
				if (valueAtomIDs.cardinality() > 1) {
					// Highlighting the bonds will do nicely, but now we need to
					// list the bonds from the atoms
					for (int leftAtomId = valueAtomIDs
							.nextSetBit(0); leftAtomId >= 0; leftAtomId =
									valueAtomIDs.nextSetBit(leftAtomId + 1)) {
						for (int rightAtomId =
								valueAtomIDs.nextSetBit(0); rightAtomId >= 0
										&& rightAtomId < leftAtomId; rightAtomId =
												valueAtomIDs.nextSetBit(
														rightAtomId + 1)) {

							Bond bond = getGc().markForCleanup(
									mol.getBondBetweenAtoms(leftAtomId,
											rightAtomId),
									localGcWave);
							if (bond != null) {
								bondColourMap.set((int) bond.getIdx(),
										valueColor);
								bondsToHighlight.add((int) bond.getIdx());
							}
						}
					}
				}
			}

			// Now deal with the (optional) colour of the key
			if (keyColor != null) {
				for (BondIdentifier bond : bonds) {
					leafAtomIDs = listLeafAtomIds(bond,
							valueAtomIDs.get(bond.getEndIdx()));
					if (leafAtomIDs.cardinality() > 1) {
						// Highlighting the bonds will do nicely, but now we
						// need to
						// list the bonds from the atoms
						for (int leftAtomId = leafAtomIDs
								.nextSetBit(0); leftAtomId >= 0; leftAtomId =
										leafAtomIDs
												.nextSetBit(leftAtomId + 1)) {
							for (int rightAtomId =
									leafAtomIDs.nextSetBit(0); rightAtomId >= 0
											&& rightAtomId < leftAtomId; rightAtomId =
													leafAtomIDs.nextSetBit(
															rightAtomId + 1)) {
								Bond leafBond = getGc().markForCleanup(
										mol.getBondBetweenAtoms(leftAtomId,
												rightAtomId),
										localGcWave);
								if (leafBond != null) {
									bondColourMap.set((int) leafBond.getIdx(),
											keyColor);
									bondsToHighlight
											.add((int) leafBond.getIdx());
								}
							}
						}
					}
					// And ensure all atoms at ends of breaking bonds are
					// highlighted in the appropriate colour - this ensures that
					// single atoms show up, and also that atoms are shown in
					// the colour of the component they are part of, rather than
					// sometimes the opposite part due to overlapping bonds
					for (Integer atomId : bond) {
						if (leafAtomIDs.get(atomId)) {
							// Need to highlight this atom
							// int atomId = leafAtomIDs.nextSetBit(0);
							atomsToHighlight.add(atomId);
							atomColourMap.set(atomId, keyColor);
						} else if (valueAtomIDs != null
								&& valueAtomIDs.get(atomId)) {
							atomsToHighlight.add(atomId);
							atomColourMap.set(atomId, valueColor);
						}
					}
				}
			}

			// Deal with the (optional) colour of the breaking bonds
			if (breakingBondColour != null) {
				for (BondIdentifier bond : bonds) {
					bondsToHighlight.add((int) bond.getBondIdx());
					bondColourMap.set((int) bond.getBondIdx(),
							breakingBondColour);
				}
			}

			if (mol.getNumConformers() < 1) {
				mol.compute2DCoords();
			}

			MolDraw2DSVG drawer = null;
			drawer = getGc().markForCleanup(
					new MolDraw2DSVG(RENDERER_WIDTH, RENDERER_HEIGHT),
					localGcWave);

			drawer.drawMolecule(mol, atomsToHighlight, bondsToHighlight,
					atomColourMap, bondColourMap);
			drawer.finishDrawing();
			svg = drawer.getDrawingText();
			// This swaps the svg header to match the scaleable version in the
			// Renderer to Image node
			if (svg != null) {
				// Make it scaleable
				svg = svg.replaceAll("(?s)<svg:svg.*?>",
						"<svg:svg contentScriptType=\"text/ecmascript\" zoomAndPan=\"magnify\"\n "
								+ "xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n xmlns:svg=\"http://www.w3.org/2000/svg\" baseProfile=\"full\"\n "
								+ "contentStyleType=\"text/css\" version=\"1.1\" width=\""
								+ RENDERER_WIDTH
								+ "px\"\n preserveAspectRatio=\"xMidYMid meet\"\n "
								+ "xmlns:rdkit=\"http://www.rdkit.org/xml\" height=\""
								+ RENDERER_HEIGHT
								+ "px\"\n xmlns=\"http://www.w3.org/2000/svg\">");
				// Make the highlights have nice rounded ends
				svg = svg.replaceAll(
						"(<(svg:)?path.*?stroke-width:[\\d]+px;stroke-linecap:).*?;",
						"$1round;");
				// Lazy qualifier before <svg:path gets us first matching which
				// is in the highlights as they come before the bonds
				// Locale.ROOT keeps '.' as decimal as required by SVG standard
				// - see
				// https://forum.knime.com/t/mmp-molecule-fragment-node-output-svg-cells-and-system-locale/10315
				String radius = String.format(Locale.ROOT, "%.4f",
						BREAK_ATOM_END_RADIUS_SCALE
								* Integer.parseInt(svg.replaceAll(
										"(?s).*?<(?:svg:)?path.*?;stroke:#(?!000000)[0-9A-F]{6};stroke-width:(\\d+)px;.*",
										"$1"))
								/ 2.0);
				svg = svg.replaceAll(
						"(<svg:ellipse cx='.*?' cy='.*?' rx=').*?' ry='.*?'",
						"$1" + radius + "' ry='" + radius + "'");

			}
		} catch (ConformerException e) {
			throw new ToolkitException(e.message(), e);
		} finally {
			getGc().cleanupMarkedObjects(localGcWave);
		}
		return svg;
	}

	/**
	 * Method to convert an AWT Color to the corresponding RDKit colour
	 * 
	 * @param colour
	 *            The AWT {@link Color}
	 * @param localGcWave
	 *            The GC wave for cleanup
	 * @return The RDKit {@link DrawColour}
	 */
	protected DrawColour convertColorToRdkitColor(Color colour,
			long localGcWave) {
		if (colour == null) {
			return null;
		}
		float[] rgb = colour.getColorComponents(null);
		return getGc().markForCleanup(new DrawColour(rgb[0], rgb[1], rgb[2]),
				localGcWave);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.AbstractFragmentationFactory#
	 * close()
	 */
	@Override
	public void close() {
		super.close();
		getGc().cleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		getGc().cleanupMarkedObjects();
		super.finalize();
	}

	@Override
	protected AbstractMulticomponentFragmentationParser<RWMol> createFragmentationParserFromComponents(
			Set<RWMol> leafs, RWMol valueComponent, long localGCWave) {
		try {
			return new RWMolMulticomponentFragmentationParser(valueComponent,
					leafs);
		} catch (MoleculeFragmentationException | ToolkitException e) {
			return null;
		}
	}

	@Override
	protected RWMol createValueComponent(BitSet valueAtomIDs,
			Set<BondIdentifier> bonds, long localGCWave) {
		RWMol retVal = getGc().markForCleanup(new RWMol(mol), localGCWave);

		Map<Integer, Integer> APLookup = new HashMap<>();
		for (BondIdentifier bond : bonds) {
			if (!valueAtomIDs.get(bond.getStartIdx())) {
				// Start is not in value atoms, so is AP atom
				APLookup.put(bond.getStartIdx(), bond.getFragmentationIndex());
			} else {
				APLookup.put(bond.getEndIdx(), bond.getFragmentationIndex());
			}
		}
		// Now we can loop down the atoms, processing each fragment side-by-side
		// as we go..
		for (int atIdx = (int) (mol.getNumAtoms() - 1); atIdx >= 0; atIdx--) {
			// loop through the fragments

			Atom at;
			//@formatter:off
			// For each atom there are three options:
			// 1. Visited - we keep it
			// 2. Not visited but is AP - we change it
			// 3. Not visited, not AP, so we delete it
			//@formatter:on
			if (!valueAtomIDs.get(atIdx)) {
				// 2. or 3.
				if (APLookup.containsKey(atIdx)) {
					// 2 - change it
					at = new Atom(0);
					at.setProp(AP_ISOTOPIC_LABEL, "" + APLookup.get(atIdx));
					retVal.replaceAtom(atIdx, at);
					at.delete();
				} else {
					// 3 - Zap!
					retVal.removeAtom(atIdx);
				}
			}
		}
		return retVal;

	}

	@Override
	protected RWMol createLeaf(BitSet leafAtomIds, BondIdentifier bond,
			long localGCWave) {
		RWMol retVal = getGc().markForCleanup(new RWMol(mol), localGCWave);
		// Now we can loop down the atoms, processing each fragment side-by-side
		// as we go..
		for (int atIdx = (int) (mol.getNumAtoms() - 1); atIdx >= 0; atIdx--) {
			// loop through the fragments

			//@formatter:off
			// For each atom there are three options:
			// 1. Visited - we keep it
			// 2. Not visited but is AP - we change it
			// 3. Not visited, not AP, so we delete it
			//@formatter:on
			if (!leafAtomIds.get(atIdx)) {
				// 2. or 3.
				if (bond.isToAtomWithIdx(atIdx)) {
					// 2 - change it
					Atom at = new Atom(0);
					at.setProp(AP_ISOTOPIC_LABEL,
							"" + bond.getFragmentationIndex());
					retVal.replaceAtom(atIdx, at);
					at.delete();
				} else {
					// 3 - Zap!
					retVal.removeAtom(atIdx);
				}
			}
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.AbstractFragmentationFactory2
	 * #doCleanup(long)
	 */
	@Override
	protected void doCleanup(long localGCWave) {
		getGc().cleanupMarkedObjects(localGCWave);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.AbstractFragmentationFactory2
	 * #getGCWave()
	 */
	@Override
	protected long getGCWave() {
		return getGc().getNextWaveIndex();
	}

	@Override
	protected void canonicaliseDuplicateKeyComponents(RWMol valueComponent,
			Set<RWMol> leafComponents, long localGCWave) {

		// We arrive with a known duplicate somewhere
		// Need to list the groups of clashing key components
		Map<String, List<RWMol>> comps = new LinkedHashMap<>();
		for (RWMol keyComp : leafComponents) {
			String smi = molToSmiles(keyComp, true);
			if (!comps.containsKey(smi)) {
				comps.put(smi, new ArrayList<>());
			}
			comps.get(smi).add(keyComp);
		}

		// The value is a use-once, and so we dont need to clone it if we modify
		// it
		canonicalizeAtomOrder(valueComponent);

		for (Entry<String, List<RWMol>> ent : comps.entrySet()) {
			if (ent.getValue().size() > 1) {
				// This component duplicates
				TreeMap<Long, RWMol> sortedClashes = new TreeMap<>();
				TreeSet<Integer> sortedAvailableIndices = new TreeSet<>();
				boolean needsResort = false;
				for (RWMol comp : ent.getValue()) {
					Integer apIdx = getAPIndex(comp, localGCWave);
					Long atIdx = findIndexOfLabelledAtom(valueComponent, apIdx,
							localGCWave);
					if (!sortedClashes.isEmpty()
							&& atIdx.compareTo(sortedClashes.lastKey()) < 0) {
						needsResort = true;
					}
					sortedClashes.put(atIdx, comp);
					sortedAvailableIndices.add(apIdx);
				}

				if (needsResort) {
					// Now we need to rejig the indices on the actual molecule
					// objects
					// We order the set of AP indices used by by the order that
					// the atoms appear in the canonicalised value
					Iterator<Integer> availableIndexIter =
							sortedAvailableIndices.iterator();
					Iterator<Entry<Long, RWMol>> sortedClashesIter =
							sortedClashes.entrySet().iterator();
					while (availableIndexIter.hasNext()
							&& sortedClashesIter.hasNext()) {
						Integer nextIndex = availableIndexIter.next();
						Entry<Long, RWMol> ent1 = sortedClashesIter.next();
						// Change the index on the value
						getGc().markForCleanup(
								valueComponent.getAtomWithIdx(ent1.getKey()),
								localGCWave)
								.setProp(AP_ISOTOPIC_LABEL, "" + nextIndex);

					}
				}
				sortedAvailableIndices.clear();
				sortedClashes.clear();
			}
		}
	}

	@Override
	protected RWMol removeHs(RWMol leaf, long localGCWave) {
		RDKFuncs.removeHs(leaf);
		return leaf;
	}

	@Override
	protected void applyAPIsotopicLabels(RWMol component, int apIdx,
			long localGCWave) {
		for (int i = 0; i < component.getNumAtoms(); i++) {
			Atom at = getGc().markForCleanup(component.getAtomWithIdx(i),
					localGCWave);
			if (at.getAtomicNum() == 0 && at.hasProp(AP_ISOTOPIC_LABEL)) {
				at.setIsotope(apIdx < 0
						? Integer.parseInt(at.getProp(AP_ISOTOPIC_LABEL).trim())
						: apIdx);
			}
		}
	}

	@Override
	protected void assignCreatedDblBondGeometry(RWMol component,
			long localGCWave) {

		try {
			component.sanitizeMol();
			// This adds a list of stereoatoms to previously unassigned double
			// bonds
			RDKFuncs.findPotentialStereoBonds(component, false);
		} catch (MolSanitizeException e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry"
						+ e.message() == null ? "" : e.message());
			}
			return;
		} catch (Exception e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry"
						+ e.getMessage() == null ? "" : e.getMessage());
			}
			return;
		}

		Match_Vect_Vect alkeneMatches = getGc().markForCleanup(
				component.getSubstructMatches(dblBondMatch), localGCWave);
		for (int i = 0; i < alkeneMatches.size(); i++) {
			Match_Vect bond = alkeneMatches.get(i);
			int startId = bond.get(0).getSecond();
			int endId = bond.get(1).getSecond();
			Bond bd = getGc().markForCleanup(
					component.getBondBetweenAtoms(startId, endId), localGCWave);
			if (!bd.hasProp(UNSPECIFIED_DOUBLE_BOND)
					&& !bd.hasProp(SPECIFIED_DOUBLE_BOND)) {
				// We only worry about double bonds which were not assigned or
				// known unassigned and
				// could be, and are not now assigned
				Int_Vect stereoAtoms = bd.getStereoAtoms();
				if (stereoAtoms.size() > 0) {
					// And only if they have stereo atoms listed
					// Now we need to find the adjacent bonds from the double
					// bond termini to the listed stereoatoms.
					Bond beginStereoBond = null;
					Bond endStereoBond = null;
					for (int j = 0; j < stereoAtoms.size(); j++) {
						if (beginStereoBond == null) {
							beginStereoBond = getGc().markForCleanup(
									component.getBondBetweenAtoms(
											bd.getBeginAtomIdx(),
											stereoAtoms.get(j)),
									localGCWave);
						}
						if (endStereoBond == null) {
							endStereoBond = getGc().markForCleanup(
									component.getBondBetweenAtoms(
											bd.getEndAtomIdx(),
											stereoAtoms.get(j)),
									localGCWave);
						}
					}

					// For each end of the bond in turn we apply a label if none
					// is provided
					if (beginStereoBond.getBondDir() == BondDir.NONE) {
						applyDirectionalBond(component, localGCWave,
								(int) bd.getBeginAtomIdx());
					}
					if (endStereoBond.getBondDir() == BondDir.NONE) {
						applyDirectionalBond(component, localGCWave,
								(int) bd.getEndAtomIdx());
					}
				}
				stereoAtoms.delete();
				// Fix for https://github.com/rdkit/rdkit/pull/1202
				if (!RWMolFragmentationUtilsFactory.IS_PRE_SMILES_DBL_BOND_GEOMETRY_CHANGE) {
					bd.setStereo(BondStereo.STEREONONE);
				}
			}
		}
	}

	@Override
	protected void assignAPChirality(RWMol component, long localGCWave) {

		try {
			component.sanitizeMol();
			// Flag possible stereocentres
			RDKFuncs.assignStereochemistry(component, false, true, true);
		} catch (MolSanitizeException e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry"
						+ e.message() == null ? "" : e.message());
			}
			return;
		} catch (Exception e) {
			if (verboseLogging) {
				logger.info("Problem assigning double bond geometry"
						+ e.getMessage() == null ? "" : e.getMessage());
			}
			return;
		}

		// Now check all atoms
		Match_Vect_Vect cMatches = getGc().markForCleanup(
				component.getSubstructMatches(tetrahedralCarbonMatch),
				localGCWave);
		for (int i = 0; i < cMatches.size(); i++) {
			Match_Vect atom = cMatches.get(i);
			int atomId = atom.get(0).getSecond();
			// for (int i = 0; i < component.getNumAtoms(); i++) {
			Atom at = getGc().markForCleanup(component.getAtomWithIdx(atomId),
					localGCWave);
			// At present, only fix Carbon... (Otherwise e.g. MeP(=O)(OH)[*]
			// is chiral!) and
			if (at.getChiralTag() == ChiralType.CHI_UNSPECIFIED
					&& !at.hasProp(UNSPECIFIED_STEREOCENTRE)
					&& at.hasProp("_ChiralityPossible")) {
				// Just need to chose one assignment
				// arbitrarily but consistently
				at.setChiralTag(ChiralType.CHI_TETRAHEDRAL_CW);
			}
		}

	}

	@Override
	protected RWMol cloneComponent(RWMol mol, long localGCWave) {
		return getGc().markForCleanup(new RWMol(mol), localGCWave);
	}

	@Override
	protected boolean isLeaf(RWMol component, long wave) {
		int apCount = 0;
		for (int i = 0; i < component.getNumAtoms(); i++) {
			final Atom at =
					getGc().markForCleanup(component.getAtomWithIdx(i), wave);
			if (at.hasProp(AP_COUNT)) {
				apCount += Integer.parseInt(at.getProp(AP_COUNT));
			}
		}
		return apCount == 1;
	}

	@Override
	protected List<RWMol> breakAllMatchingBonds(long wave)
			throws ClosedFactoryException {

		RWMol mol0 = getGc().markForCleanup(new RWMol(mol), wave);

		for (BondIdentifier bd : getMatchingBonds()) {
			final Atom at0 = getGc().markForCleanup(
					mol0.getAtomWithIdx(bd.getStartIdx()), wave);
			// All this because set/getIntProp isnt exposed in Java wrapper...
			int count = (at0.hasProp(AP_COUNT))
					? Integer.parseInt(at0.getProp(AP_COUNT)) : 0;
			at0.setProp(AP_COUNT, String.format("%d", count + 1));

			final Atom at1 = getGc()
					.markForCleanup(mol0.getAtomWithIdx(bd.getEndIdx()), wave);
			count = (at1.hasProp(AP_COUNT))
					? Integer.parseInt(at1.getProp(AP_COUNT)) : 0;
			at1.setProp(AP_COUNT, String.format("%d", count + 1));

			mol0.removeBond(bd.getStartIdx(), bd.getEndIdx());

		}

		ROMol_Vect comps = getGc().markForCleanup(RDKFuncs.getMolFrags(mol0,
				false/* sanitize frags */, null/* frags to mol mapping */,
				null/* Atoms in each frag mapping */, false/* copy confs */),
				wave);
		List<RWMol> retVal = new ArrayList<>();
		for (int i = 0; i < comps.size(); i++) {
			retVal.add(getGc().markForCleanup(
					new RWMol(getGc().markForCleanup(comps.get(i), wave)),
					wave));
		}
		return retVal;

	}

}
