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
package com.vernalis.knime.mmp.fragmentors;

import java.awt.Color;
import java.io.IOException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.mmp.CombinationFinder;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractMulticomponentFragmentationParser;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifier;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifierSelfpairSet;

/**
 * This is the default abstract implementation of
 * {@link MoleculeFragmentationFactory2}. Many components are lazy-initialised.
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 *
 * @param <T>
 *            The type of the molecule to fragment
 * @param <U>
 *            The type of the matcher object
 */
public abstract class AbstractFragmentationFactory<T, U>
		implements MoleculeFragmentationFactory2<T, U> {

	/**
	 * Property name to flag incomping possible but not stereocentres
	 */
	protected static final String UNSPECIFIED_STEREOCENTRE =
			"UNSPECIFIED_STEREOCENTRE";

	/**
	 * Property name to flag incoming defined stereocentres
	 */
	protected static final String DEFINED_STEREOCENTRE = "DEFINED_STEREOCENTRE";

	/**
	 * Property name to flag incoming possible but not stereo double bonds
	 */
	protected static final String UNSPECIFIED_DOUBLE_BOND =
			"UNSPECIFIED_DOUBLE_BOND";
	/**
	 * Property name to flag incoming assigned stereo double bonds
	 */
	protected static final String SPECIFIED_DOUBLE_BOND =
			"SPECIFIED_DOUBLE_BOND";
	/**
	 * Property name to hold isotopic label for attachment point until needed
	 */
	protected static final String AP_ISOTOPIC_LABEL = "AP_ISOTOPIC_LABEL";

	protected static final String BOND_ONLY_VALUE_SMILES = "[501*][500*]";
	protected T mol;
	protected U bondMatch;
	protected boolean isChiral;
	protected boolean hasUndefinedChirality;
	protected boolean hasNonflaggedDoubleBonds;
	protected final NodeLogger logger = NodeLogger.getLogger(this.getClass());
	protected boolean verboseLogging;
	protected boolean removeHs;
	protected boolean treatProchiralAsChiral;
	protected boolean isClosed;

	protected Set<BondIdentifier> matchingBonds;
	protected Map<Integer, Set<BondIdentifier>> cuttableBonds = new HashMap<>();
	protected Map<Set<BondIdentifier>, Boolean> triplets = new HashMap<>();
	protected Map<Set<BondIdentifier>, BitSet> valueAtomIDsLookup =
			new HashMap<>();
	protected Map<BondIdentifier, BitSet> startLeafAtomIDsLookup =
			new HashMap<>();
	protected Map<BondIdentifier, BitSet> endLeafAtomIDsLookup =
			new HashMap<>();
	protected Map<Integer, Set<Set<BondIdentifier>>> cuttableBondCombos =
			new HashMap<>();
	protected Map<Integer, Set<Set<BondIdentifier>>> invalidTriplets =
			new HashMap<>();
	protected SizedBondCache<T> leafLookup;
	protected BitSet possibleCreatedStereos;
	protected BitSet atomsOfPossibleCreatedStereoDoubleBonds;

	/**
	 * The maximum number of Changing HAs ({@code null} if not filter- not used
	 * for 1 cut
	 */
	protected Integer maxNumberChangingHAs;
	/**
	 * The minimum ratio of constant to changing atoms ({@code null} if not
	 * filter) - not used for 1 cut
	 */
	protected Double minCnstToVarAtmRatio;
	/**
	 * The number of heavy atoms
	 */
	protected int HAC;

	/**
	 * Lazy instantiated maximum number of cuts count
	 */
	protected Integer maxCuts = null;

	/**
	 * List of atom neighbour indices for faster graph walking
	 */
	protected final int[][] graphNeighbours;

	/**
	 * A mask of which atoms are heavy atoms
	 */
	protected final BitSet heavyAtomMask;

	/**
	 * Convenience simple fragments to save time
	 */
	protected final T bondOnlyValueComponent, hydrogenOnlyLeaf;

	protected final boolean isHAdded;

	protected static int leafGenCount = 0;

	protected static int leaflookupCount = 0;

	/**
	 * Constructor. Calls in order:
	 * <ul>
	 * <li>{@link #setMolValue(Object)}</li>
	 * <li>{@link #setMatchValue(Object)}</li>
	 * <li>{@link #getNumHeavyAtoms(Object)}</li>
	 * <li>{@link #markUnassignedPossibleDoubleBonds()}</li>
	 * <li>{@link #markUnassignedPossibleChiralCentres()}</li>
	 * <li>{@link #initialiseGraphNeighbours()}</li>
	 * <li>{@link #initialiseHeavyAtomMask()}</li>
	 * <li>{@link #identifyAllMatchingBonds()}</li>
	 * </ul>
	 * 
	 * Calls are also made to {@link #getComponentFromSmiles(String, boolean)}
	 * for a bond-only and *-H fragment
	 * 
	 * @param mol
	 *            The molecule to be fragmented
	 * @param bondMatch
	 * @param removeHs
	 *            Should explicit H atoms be removed after fragmentation?
	 * @param verboseLogging
	 *            Should the logger be used
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 *             This should never be thrown unless {@link #close()} is
	 *             somehow called during instantiation
	 */
	public AbstractFragmentationFactory(T mol, U bondMatch, boolean removeHs,
			boolean isHAdded, boolean verboseLogging,
			boolean treatProchiralAsChiral, Integer maxNumberChangingHAs,
			Double minCnstToVarAtmRatio, int maxLeafCacheSize)
			throws ClosedFactoryException, ToolkitException {

		this.removeHs = removeHs;
		this.mol = setMolValue(mol);
		this.bondMatch = setMatchValue(bondMatch);
		this.verboseLogging = verboseLogging;
		this.maxNumberChangingHAs = maxNumberChangingHAs;
		this.minCnstToVarAtmRatio = minCnstToVarAtmRatio;
		this.HAC = getNumHeavyAtoms(this.mol);
		this.treatProchiralAsChiral = treatProchiralAsChiral;
		this.isHAdded = isHAdded;
		isClosed = false;
		isChiral = false;
		hasUndefinedChirality = false;
		hasNonflaggedDoubleBonds = false;
		markUnassignedPossibleDoubleBonds();
		markUnassignedPossibleChiralCentres();
		graphNeighbours = initialiseGraphNeighbours();
		heavyAtomMask = initialiseHeavyAtomMask();
		matchingBonds = identifyAllMatchingBonds();
		bondOnlyValueComponent = getComponentFromSmiles("[501*][500*]", false);
		leafLookup = new SizedBondCache<>(maxLeafCacheSize);
		// If we are H-added always use index 500
		hydrogenOnlyLeaf = this.isHAdded
				? getComponentFromSmiles("[500*][H]", false) : null;
	}

	/**
	 * Method to convert SMILES to the molecule of type T
	 * 
	 * @param smiles
	 *            The SMILES String
	 * @param moveAPIsotopesToAtomProperties
	 *            should isotopic labels associated with '*' atoms be moved to
	 *            an atom property?
	 * @return The molecule
	 * @throws ToolkitException
	 *             If the toolkit could not instantiate a molecule from the
	 *             SMILES string
	 */
	protected abstract T getComponentFromSmiles(String smiles,
			boolean moveAPIsotopesToAtomProperties) throws ToolkitException;

	/**
	 * @return a {@link BitSet} with the positions corresponding to heavy atoms
	 *         in the molecule set
	 */
	protected abstract BitSet initialiseHeavyAtomMask();

	/**
	 * @return An array of arrays of the neighbour indices. NB TODO: An edge
	 *         reduction here could speed things up later
	 */
	protected abstract int[][] initialiseGraphNeighbours();

	/**
	 * @return The SMILES String of the stored molecule
	 */
	protected String getMolSmiles() {
		return molToSmiles(mol, false);
	}

	/**
	 * Method to convert a molecule of type T to the requisite SMILES string
	 * 
	 * @param mol
	 *            The molecule
	 * @param removeIsotopicLabelsFromAP
	 *            Should the SMILES string have isotopic labels removed from the
	 *            '*' atoms?
	 * @return The SMILES string
	 */
	protected abstract String molToSmiles(T mol,
			boolean removeIsotopicLabelsFromAP);

	/**
	 * This method is called during instantiation to calculate the number of
	 * heavy atoms
	 * 
	 * @param mol
	 *            The molecule
	 * @return The HAC
	 */
	protected abstract int getNumHeavyAtoms(T mol);

	/**
	 * This method is called during instantiation to set the molecule object, to
	 * allow gc-tagging where required
	 * 
	 * @param mol
	 *            The molecule
	 * @return The molecule (GC tagged if required)
	 */
	protected abstract T setMolValue(T mol);

	/**
	 * This method is called during instantiation to set the bondmatch object,
	 * to allow gc-tagging where required
	 * 
	 * @param bondMatch
	 *            The matcher object
	 * 
	 * @return The molecule (GC tagged if required)
	 */
	protected abstract U setMatchValue(U bondMatch);

	/**
	 * Method to mark the unassigned possible chiral centres to prevent them
	 * becoming erroneously assigned later
	 */
	protected abstract void markUnassignedPossibleChiralCentres();

	/**
	 * Method to mark the unassigned possible double bonds to prevent them
	 * becoming erroneously assigned later
	 */
	protected abstract void markUnassignedPossibleDoubleBonds();

	/*
	 * Methods intended to be called from nodes
	 */

	@Override
	public void setVerboseLogging(boolean verboseLogging) {
		this.verboseLogging = verboseLogging;

	}

	@Override
	public Set<AbstractMulticomponentFragmentationParser<T>> breakMoleculeAlongBondCombos(
			Set<Set<BondIdentifier>> bondCombos, boolean prochiralAsChiral,
			ExecutionContext exec, Color bondColour, Color keyColour,
			Color valueColour, NodeLogger logger, boolean verboseLogging)
			throws CanceledExecutionException, ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (isHAdded) {
			throw new UnsupportedOperationException();
		}
		int count = 0;
		Set<AbstractMulticomponentFragmentationParser<T>> retVal =
				new TreeSet<>();
		for (Set<BondIdentifier> bondSet : bondCombos) {
			exec.checkCanceled();
			count++;
			if (verboseLogging) {
				if (count % 50 == 0) {
					logger.info("Fragmenting molecule: " + count + " of "
							+ bondCombos.size() + " fragmentations tried");
				}
			}
			try {
				// Collect the fragmentation
				final AbstractMulticomponentFragmentationParser<T> fragment =
						rawFragmentMoleculeAlongBondCombos(bondSet);
				if (fragment != null) {
					if (bondColour != null || keyColour != null
							|| valueColour != null) {
						try {
							fragment.setRenderingCell(
									renderFragmentation(bondSet, bondColour,
											keyColour, valueColour));
						} catch (IOException
								| MoleculeFragmentationException e) {
							// Leave it as a missing cell - dont think we should
							// be
							// able to get here
						}
					}
					retVal.add(fragment);
				}
			} catch (IllegalArgumentException e) {
				// Strange - shouldnt be here...
				logger.warn("Something strange happened... " + e.getMessage());
			} catch (ToolkitException e) {
				if (verboseLogging) {
					logger.info("Unable to complete fragmentation: "
							+ e.getMessage() == null ? "" : e.getMessage());
				}
			}
		}
		return retVal;
	}

	@Override
	public Set<AbstractMulticomponentFragmentationParser<T>> breakMoleculeAlongMatchingBonds(
			ExecutionContext exec, Color breakingBondColour, Color keyColour,
			Color valueColour) throws CanceledExecutionException,
			IllegalArgumentException, ToolkitException, ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		Set<AbstractMulticomponentFragmentationParser<T>> retVal =
				new TreeSet<>();
		for (BondIdentifier bond : matchingBonds) {
			exec.checkCanceled();

			AbstractMulticomponentFragmentationParser<T> fragment =
					rawFragmentMoleculeAlongBond(bond);
			if (fragment != null) {
				if (breakingBondColour != null || keyColour != null
						|| valueColour != null) {
					try {
						fragment.setRenderingCell(renderFragmentation(
								Collections.singleton(bond), breakingBondColour,
								keyColour, valueColour));
					} catch (IOException | MoleculeFragmentationException e) {
						// Leave it as a missing cell - dont think we should be
						// able to get here
					}
				}
				retVal.add(fragment);
			}
			// Now the reverse
			fragment = rawFragmentMoleculeAlongBond(bond.getReverse());
			if (fragment != null) {
				if (breakingBondColour != null || keyColour != null
						|| valueColour != null) {
					try {
						fragment.setRenderingCell(renderFragmentation(
								Collections.singleton(bond.getReverse()),
								breakingBondColour, keyColour, valueColour));
					} catch (IOException | MoleculeFragmentationException e) {
						// Leave it as a missing cell - dont think we should be
						// able to get here
					}
				}
				retVal.add(fragment);
			}
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * breakMoleculeAlongBondsWithBondInsertion(org.knime.core.node.
	 * ExecutionContext)
	 */
	@Override
	public Set<AbstractMulticomponentFragmentationParser<T>> breakMoleculeAlongMatchingBondsWithBondInsertion(
			ExecutionContext exec, Color bondColour, Color keyColour,
			Color valueColour) throws CanceledExecutionException,
			IllegalArgumentException, ToolkitException, ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (isHAdded) {
			throw new UnsupportedOperationException();
		}
		Set<AbstractMulticomponentFragmentationParser<T>> retVal =
				new TreeSet<>();
		for (BondIdentifier bond : matchingBonds) {
			exec.checkCanceled();
			final AbstractMulticomponentFragmentationParser<T> fragment =
					rawFragmentMoleculeWithBondInsertion(bond);
			if (fragment != null) {
				if (bondColour != null || keyColour != null
						|| valueColour != null) {
					try {
						fragment.setRenderingCell(renderFragmentation(
								BondIdentifierSelfpairSet.create(bond),
								bondColour, keyColour, valueColour));
					} catch (IOException | MoleculeFragmentationException e) {
						// Leave it as a missing cell - dont think we should be
						// able to get here
					}
				}
				retVal.add(fragment);
			}
		}
		return retVal;

	}

	@Override
	public final AbstractMulticomponentFragmentationParser<T> fragmentMolecule(
			Set<BondIdentifier> bonds)
			throws IllegalArgumentException, MoleculeFragmentationException,
			ToolkitException, ClosedFactoryException {
		AbstractMulticomponentFragmentationParser<T> retVal =
				rawFragmentMoleculeAlongBondCombos(bonds);
		if (retVal == null) {
			throw new MoleculeFragmentationException();
		}
		return retVal;
	}

	/**
	 * Method to perform a 1-bond cut and '*-*' insertion without throwing
	 * {@link MoleculeFragmentationException} if not possible (for performance
	 * reasons) - called by
	 * {@link #fragmentMoleculeWithBondInsertion(BondIdentifier)}.
	 * 
	 * @param bond
	 *            The bond to break
	 * @return The fragmentation object or <code>null</code> if a problem with
	 *         fragmentation
	 * @throws ToolkitException
	 *             If there was a problem from the toolkit during fragmentation
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} method has already been called
	 */
	protected AbstractMulticomponentFragmentationParser<T> rawFragmentMoleculeWithBondInsertion(
			BondIdentifier bond)
			throws ToolkitException, ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (isHAdded) {
			throw new UnsupportedOperationException();
		}
		if (bond == null) {
			throw new IllegalArgumentException("A bond must be supplied");
		}

		Set<T> leafs = new LinkedHashSet<>();
		long localGCWave = getGCWave();

		// Do the first leaf
		T firstLeafComponent;
		BitSet leafAtomIDs = null;

		if (!leafLookup.containsKey(bond)) {
			leafGenCount++;
			try {
				leafAtomIDs = listLeafAtomIds(bond, true);
			} catch (MoleculeFragmentationException e) {
				return null;
			}
			firstLeafComponent = createLeaf(leafAtomIDs, bond, 1L);
			if (isChiral
					|| (!hasUndefinedChirality && treatProchiralAsChiral)) {
				if (leafAtomIDs.intersects(possibleCreatedStereos)) {
					assignAPChirality(firstLeafComponent, 1L);
				}
			}
			if (hasNonflaggedDoubleBonds) {
				if (leafAtomIDs
						.intersects(atomsOfPossibleCreatedStereoDoubleBonds)) {
					assignCreatedDblBondGeometry(firstLeafComponent, 1L);
				}
			}
			applyAPIsotopicLabels(firstLeafComponent, -1, localGCWave);
			leafLookup.put(bond, firstLeafComponent);
		}
		leaflookupCount++;
		firstLeafComponent = cloneComponent(leafLookup.get(bond), localGCWave);
		applyAPIsotopicLabels(firstLeafComponent, 500, localGCWave);
		leafs.add(firstLeafComponent);

		T secondLeafComponent;

		BondIdentifier bond2 = bond.getReverse();
		if (!leafLookup.containsKey(bond2)) {
			leafGenCount++;
			try {
				if (leafAtomIDs != null) {
					leafAtomIDs.flip(0, graphNeighbours.length);
				} else {
					leafAtomIDs = listLeafAtomIds(bond2, true);
				}
			} catch (MoleculeFragmentationException e) {
				return null;
			}
			secondLeafComponent = createLeaf(leafAtomIDs, bond2, 1L);
			if (isChiral
					|| (!hasUndefinedChirality && treatProchiralAsChiral)) {
				if (leafAtomIDs.intersects(possibleCreatedStereos)) {
					assignAPChirality(secondLeafComponent, 1L);
				}
			}
			if (hasNonflaggedDoubleBonds) {
				if (leafAtomIDs
						.intersects(atomsOfPossibleCreatedStereoDoubleBonds)) {
					assignCreatedDblBondGeometry(secondLeafComponent, 1L);
				}
			}
			applyAPIsotopicLabels(secondLeafComponent, -1, localGCWave);
			leafLookup.put(bond2, secondLeafComponent);
		}
		leaflookupCount++;

		secondLeafComponent =
				cloneComponent(leafLookup.get(bond2), localGCWave);
		applyAPIsotopicLabels(secondLeafComponent, 501, localGCWave);
		leafs.add(secondLeafComponent);

		AbstractMulticomponentFragmentationParser<T> retVal =
				createFragmentationParserFromComponents(leafs,
						bondOnlyValueComponent, localGCWave);
		doCleanup(localGCWave);
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * fragmentMoleculeWithBondInsertion(com.vernalis.knime.internal.mmp.frags.
	 * abstrct.BondIdentifier)
	 */
	@Override
	public final AbstractMulticomponentFragmentationParser<T> fragmentMoleculeWithBondInsertion(
			BondIdentifier bond)
			throws IllegalArgumentException, MoleculeFragmentationException,
			ToolkitException, ClosedFactoryException {
		AbstractMulticomponentFragmentationParser<T> retVal =
				rawFragmentMoleculeWithBondInsertion(bond);
		if (retVal == null) {
			throw new MoleculeFragmentationException();
		}
		return retVal;
	}

	/**
	 * Method to perform a 1-bond cut without throwing
	 * {@link MoleculeFragmentationException} if not possible (for performance
	 * reasons) - called by {@link #fragmentMolecule(BondIdentifier)}
	 * 
	 * @param bond
	 *            The bond to break
	 * @return The fragmentation object or <code>null</code> if a problem with
	 *         fragmentation
	 * @throws ToolkitException
	 *             If there was a problem from the toolkit during fragmentation
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} method has already been called
	 */
	protected AbstractMulticomponentFragmentationParser<T> rawFragmentMoleculeAlongBond(
			BondIdentifier bond) throws IllegalArgumentException,
			ToolkitException, ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (bond == null) {
			throw new IllegalArgumentException("A bond must be supplied");
		}

		// Check the fragmentation will pass the filter
		// Start by listing the 'value' atom IDs - always go from start of bond
		// - calling method should handle reverse
		BitSet valueAtomIDs;
		try {
			valueAtomIDs = listLeafAtomIds(bond, true);
		} catch (MoleculeFragmentationException e) {
			return null;
		}
		int valueHAC;
		if (heavyAtomMask.cardinality() == heavyAtomMask.size()) {
			// Everything is a heavy atom
			valueHAC = valueAtomIDs.cardinality();
		} else {
			BitSet valueHAs = (BitSet) valueAtomIDs.clone();
			valueHAs.and(valueAtomIDs);
			valueHAC = valueHAs.cardinality();
		}
		int keyHAC = HAC - valueHAC;

		if (maxNumberChangingHAs != null && valueHAC > maxNumberChangingHAs) {
			// There is a Maximum number of changing atoms filter, and it is
			// violated
			return null;
		}

		if (minCnstToVarAtmRatio != null
				&& (1.0 * keyHAC / valueHAC) < minCnstToVarAtmRatio) {
			// There is a Minumum const/varying atom ratio filter, and it is
			// violated
			return null;
		}

		// check which fragmentation route is required
		long localGCWave = getGCWave();
		T value;
		if (!leafLookup.containsKey(bond)) {
			leafGenCount++;
			if (isHAdded && !valueAtomIDs.intersects(heavyAtomMask)) {
				// We have a leaf to H - which is cached to save deleting many
				// other H's and heavy atoms from a big clone
				value = hydrogenOnlyLeaf;
				// It cannot be chiral, or have dbl bond geometry and should
				// not be h-removed. All fragmentaiton are perfomed with idx 500
				// in this case too
			} else {
				value = createLeaf(valueAtomIDs, bond, 1L);
				if (isChiral
						|| (!hasUndefinedChirality && treatProchiralAsChiral)) {
					if (valueAtomIDs.intersects(possibleCreatedStereos)) {
						assignAPChirality(value, 1L);
					}
				}
				if (hasNonflaggedDoubleBonds) {
					if (valueAtomIDs.intersects(
							atomsOfPossibleCreatedStereoDoubleBonds)) {
						assignCreatedDblBondGeometry(value, 1L);
					}
				}
				if (removeHs) {
					value = removeHs(value, localGCWave);
				}
				applyAPIsotopicLabels(value, -1, localGCWave);
				if (!isHAdded) {
					// Dont cache if H-added , as the leaf will never be
					// re-used!
					leafLookup.put(bond, value);
				}
			}
		} else {
			leaflookupCount++;
			value = leafLookup.get(bond);
		}

		T leaf;
		// The value is also a leaf - check for caching, with reversed
		// BondIdentifier

		BitSet leafAtomIDs = (BitSet) valueAtomIDs.clone();
		leafAtomIDs.flip(0, graphNeighbours.length);
		if (!leafLookup.containsKey(bond.getReverse())) {
			leafGenCount++;
			if (isHAdded && !leafAtomIDs.intersects(heavyAtomMask)) {
				// We have a leaf to H - which is cached
				leaf = hydrogenOnlyLeaf;
				// It cannot be chiral, or have dbl bond geometry and should
				// not be h-removed, so just needs the correct AP index
				// applied
				// applyAPIsotopicLabels(leaf, -1, 1L);
			} else {
				leaf = createLeaf(leafAtomIDs, bond.getReverse(), 1L);
				if (isChiral
						|| (!hasUndefinedChirality && treatProchiralAsChiral)) {
					if (leafAtomIDs.intersects(possibleCreatedStereos)) {
						assignAPChirality(leaf, 1L);
					}
				}
				if (hasNonflaggedDoubleBonds) {
					if (leafAtomIDs.intersects(
							atomsOfPossibleCreatedStereoDoubleBonds)) {
						assignCreatedDblBondGeometry(leaf, 1L);
					}
				}
				if (removeHs) {
					leaf = removeHs(leaf, localGCWave);
				}
				applyAPIsotopicLabels(leaf, -1, localGCWave);
				if (!isHAdded) {
					// Dont cache if H-added , as the leaf will never be
					// re-used!
					leafLookup.put(bond, value);
				}
			}
		} else {
			leaflookupCount++;
			leaf = leafLookup.get(bond.getReverse());
		}

		AbstractMulticomponentFragmentationParser<T> retVal =
				createFragmentationParserFromComponents(
						Collections.singleton(leaf), value, localGCWave);

		doCleanup(localGCWave);
		return retVal;
	}

	/**
	 * Method to perform a multi-bond cut without throwing
	 * {@link MoleculeFragmentationException} if not possible (for performance
	 * reasons) - called by {@link #fragmentMolecule(Set)}
	 * 
	 * @param bond
	 *            The bond to break
	 * @return The fragmentation object or <code>null</code> if a problem with
	 *         fragmentation
	 * @throws ToolkitException
	 *             If there was a problem from the toolkit during fragmentation
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} method has already been called
	 */
	protected AbstractMulticomponentFragmentationParser<T> rawFragmentMoleculeAlongBondCombos(
			Set<BondIdentifier> bonds) throws IllegalArgumentException,
			ToolkitException, ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (isHAdded) {
			throw new UnsupportedOperationException();
		}
		if (bonds == null || bonds.isEmpty()) {
			throw new IllegalArgumentException(
					"At least one bond must be supplied");
		}

		if (bonds.size() == 1) {
			return rawFragmentMoleculeAlongBond(bonds.iterator().next());
		}

		// Check the fragmentation will pass the filter
		// Start by listing the 'value' atom IDs
		BitSet valueAtomIDs = rawListValueAtomIds(bonds);
		if (valueAtomIDs == null) {
			return null;
		}
		int valueHAC;
		if (heavyAtomMask.cardinality() == heavyAtomMask.size()) {
			// Everything is a heavy atom
			valueHAC = valueAtomIDs.cardinality();
		} else {
			BitSet valueHAs = (BitSet) valueAtomIDs.clone();
			valueHAs.and(valueAtomIDs);
			valueHAC = valueHAs.cardinality();
		}
		int keyHAC = HAC - valueHAC;

		if (maxNumberChangingHAs != null && valueHAC > maxNumberChangingHAs) {
			// There is a Maximum number of changing atoms filter, and it is
			// violated
			return null;
		}

		if (minCnstToVarAtmRatio != null
				&& (1.0 * keyHAC / valueHAC) < minCnstToVarAtmRatio) {
			// There is a Minumum const/varying atom ratio filter, and it is
			// violated
			return null;
		}

		// check which fragmentation route is required
		Set<T> leafs = new LinkedHashSet<>();
		long localGCWave = getGCWave();
		BitSet leafAtomIDs = null;
		for (BondIdentifier bond : bonds) {
			T leafComponent;
			BondIdentifier bond2 = !valueAtomIDs.get(bond.getStartIdx()) ? bond
					: bond.getReverse();
			if (!leafLookup.containsKey(bond2)) {
				leafGenCount++;
				try {
					leafAtomIDs = listLeafAtomIds(bond2, true);
				} catch (MoleculeFragmentationException e) {
					return null;
				}
				leafComponent = createLeaf(leafAtomIDs, bond2, 1L);
				if (isChiral
						|| (!hasUndefinedChirality && treatProchiralAsChiral)) {
					if (leafAtomIDs.intersects(possibleCreatedStereos)) {
						assignAPChirality(leafComponent, 1L);
					}
				}
				if (hasNonflaggedDoubleBonds) {
					if (leafAtomIDs.intersects(
							atomsOfPossibleCreatedStereoDoubleBonds)) {
						assignCreatedDblBondGeometry(leafComponent, 1L);
					}
				}
				applyAPIsotopicLabels(leafComponent, -1, localGCWave);
				leafLookup.put(bond2, leafComponent);
			} else {
				leaflookupCount++;
				leafComponent = leafLookup.get(bond2);
			}
			leafs.add(leafComponent);
		}

		T valueComponent =
				createValueComponent(valueAtomIDs, bonds, localGCWave);
		if (isChiral || (!hasUndefinedChirality && treatProchiralAsChiral)) {
			if (valueAtomIDs.intersects(possibleCreatedStereos)) {
				assignAPChirality(valueComponent, 1L);
			}
		}
		if (hasNonflaggedDoubleBonds) {
			if (valueAtomIDs
					.intersects(atomsOfPossibleCreatedStereoDoubleBonds)) {
				assignCreatedDblBondGeometry(valueComponent, 1L);
			}
		}

		if (hasDuplicateKeys(leafs)) {
			canonicaliseDuplicateKeyComponents(valueComponent, leafs,
					localGCWave);
		}

		applyAPIsotopicLabels(valueComponent, -1, localGCWave);

		AbstractMulticomponentFragmentationParser<T> retVal =
				createFragmentationParserFromComponents(leafs, valueComponent,
						localGCWave);
		doCleanup(localGCWave);
		return retVal;
	}

	/**
	 * @param leafs
	 *            The set of leafs to check
	 * @return <code>true</code> if the set of leafs has duplicates (i.e.
	 *         identical SMILES)
	 */
	protected boolean hasDuplicateKeys(Set<T> leafs) {
		LinkedHashSet<String> leafSmiles = new LinkedHashSet<>();
		for (T leaf : leafs) {
			if (!leafSmiles.add(molToSmiles(leaf, true))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method to optionally clean-up native objects generated by toolkits with
	 * the specified waveID
	 * 
	 * @param localGCWave
	 *            The Wave index to use for stored native objects
	 */
	protected void doCleanup(long localGCWave) {

	}

	/**
	 * Method to create the {@link AbstractMulticomponentFragmentationParser}
	 * from the fragmentation components
	 * 
	 * @param leafs
	 *            The leaves
	 * @param valueComponent
	 *            the value
	 * @param localGCWave
	 *            The wave index to use for stored native objects
	 * @return The fragmentation container
	 */
	protected abstract AbstractMulticomponentFragmentationParser<T> createFragmentationParserFromComponents(
			Set<T> leafs, T valueComponent, long localGCWave);

	/**
	 * Method to create a 'value' given a {@link BitSet} containing the relevant
	 * IDs of atoms to keep
	 * 
	 * @param valueAtomIDs
	 *            The values to keep
	 * @param bonds
	 *            The bonds to break
	 * @param localGCWave
	 *            The wave index to use for stored native objects
	 * @return The value Component
	 */
	protected abstract T createValueComponent(BitSet valueAtomIDs,
			Set<BondIdentifier> bonds, long localGCWave);

	/**
	 * Method to canonicalise duplicate key components
	 * 
	 * @param valueComponent
	 *            The value component
	 * @param leafs
	 *            The key component
	 * @param localGCWave
	 *            The wave index to use for stored native objects
	 */
	protected abstract void canonicaliseDuplicateKeyComponents(T valueComponent,
			Set<T> leafs, long localGCWave);

	/**
	 * Method to remove explicit H's from a leaf
	 * 
	 * @param leaf
	 *            The leaf
	 * @param localGCWave
	 *            The wave index to use for stored native objects
	 * @return The H-removed leaf
	 */
	protected abstract T removeHs(T leaf, long localGCWave);

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
	 * @param apIndex
	 *            The index of the applied AP, or -1 to use that stored in the
	 *            atom property
	 * @param localGCWave
	 *            The GC Wave for garbage collection
	 */
	protected abstract void applyAPIsotopicLabels(T component, int apIndex,
			long localGCWave);

	/**
	 * Assigns double bond geometry to newly created asymmetric double bonds
	 * which are not flagged as {@value #UNSPECIFIED_DOUBLE_BOND}
	 * 
	 * @param component
	 *            The molecule to fix
	 * @param localGCWave
	 *            The gc wave index
	 */
	protected abstract void assignCreatedDblBondGeometry(T component,
			long localGCWave);

	/**
	 * This assigns chirality to unassigned potentially chiral C atoms which are
	 * not flagged as {@value #UNSPECIFIED_STEREOCENTRE}
	 * 
	 * @param component
	 *            The molecule to fix
	 * @param localGcWave
	 *            The GC wave index for garbage collection
	 */
	protected abstract void assignAPChirality(T component, long localGCWave);

	/**
	 * @return The wave index to use for stored native objects
	 */
	protected abstract long getGCWave();

	/**
	 * Method to create a Leaf from a {@link BitSet} containing the Atom IDs to
	 * keep
	 * 
	 * @param listLeafAtomIds
	 *            The atom IDs to keep
	 * @param bond
	 *            the bond to break to form the leaf
	 * @param localGCWave
	 *            The wave index to use for stored native objects
	 * @return The Leag
	 */
	protected abstract T createLeaf(BitSet listLeafAtomIds, BondIdentifier bond,
			long localGCWave);

	/**
	 * Method to create a cloned copy of a molecule
	 * 
	 * @param t
	 *            The molecule to clone
	 * @param localGCWave
	 *            The wave index to use for stored native objects
	 * @return The cloned molecule
	 */
	protected abstract T cloneComponent(T t, long localGCWave);

	/**
	 * An efficient, toolkit-independent routine to generate a list of atom IDs
	 * for the fragmentation value walking the graphNeighbours object
	 * 
	 * @param bonds
	 *            The bond(s) to break
	 * @return A Set containing the value atom IDs
	 * @throws IllegalArgumentException
	 *             If no bonds are supplied
	 * @throws MoleculeFragmentationException
	 *             if the set does not comprise a valid fragmentation
	 */
	protected BitSet listValueAtomIds(Set<BondIdentifier> bonds)
			throws IllegalArgumentException, MoleculeFragmentationException {
		if (bonds == null || bonds.size() == 0) {
			throw new IllegalArgumentException(
					"At least one bond must be supplied");
		}
		BitSet retVal = rawListValueAtomIds(bonds);
		if (retVal == null) {
			throw new MoleculeFragmentationException();
		}
		return retVal;
	}

	/**
	 * An efficient, toolkit-independent routine to generate a list of atom IDs
	 * and cache the result for later reuse if needed for the fragmentation
	 * value walking the graphNeighbours object, called by
	 * {@link #listValueAtomIds(Set)}
	 * 
	 * @param bonds
	 *            The bond(s) to break
	 * @return A Set containing the value atom IDs of <code>null</code> if the
	 *         set does not comprise a valid fragmentation
	 * @throws IllegalArgumentException
	 *             If no bonds are supplied
	 */
	protected BitSet rawListValueAtomIds(Set<BondIdentifier> bonds) {
		if (!valueAtomIDsLookup.containsKey(bonds)) {
			// NB we have no method for getting number of *all* atoms (not HAC)
			int numAtoms = graphNeighbours.length;
			BitSet apatoms = new BitSet(numAtoms);
			for (BondIdentifier bond : bonds) {
				apatoms.set(bond.getStartIdx());
				apatoms.set(bond.getEndIdx());
			}

			int apCount = 0;

			BitSet visitedAtomIDs = new BitSet(numAtoms);
			BitSet atomLayer = new BitSet(numAtoms);
			atomLayer.set(bonds.iterator().next().getStartIdx());
			BitSet nextAtomLayer = new BitSet(numAtoms);
			while (atomLayer.cardinality() > 0) {
				visitedAtomIDs.or(atomLayer);
				for (int atIdx = atomLayer.nextSetBit(0); atIdx >= 0; atIdx =
						atomLayer.nextSetBit(atIdx + 1)) {
					for (int bondedAtomIdx : graphNeighbours[atIdx]) {
						if (apatoms.get(atIdx)) {
							if (!apatoms.get(bondedAtomIdx)) {
								// Dont traverse bonds to be broken!
								nextAtomLayer.set(bondedAtomIdx);
							} else {
								// Both atoms are in bonds to break - need to
								// check if is in same bond
								boolean traverse = true;
								for (BondIdentifier bond : bonds) {
									if (bond.isToAtomWithIdx(atIdx) && bond
											.isToAtomWithIdx(bondedAtomIdx)) {
										// Same bond - dont traverse
										traverse = false;
										apCount++;
										break;
									}
								}
								if (traverse) {
									nextAtomLayer.set(bondedAtomIdx);
								}
							}
						} else {
							nextAtomLayer.set(bondedAtomIdx);
						}
					}
				}
				nextAtomLayer.andNot(visitedAtomIDs);
				atomLayer.clear();
				atomLayer.or(nextAtomLayer);
				nextAtomLayer.clear();
			}

			// If we are here and apCount is neither 1 (we were on a leaf) or
			// bonds.size() (we were on the core) then this is a broken
			// fragmentation
			if (apCount != 1 && apCount != bonds.size()) {
				return null;
			}
			if (apCount == bonds.size()) {
				valueAtomIDsLookup.put(bonds, visitedAtomIDs);
			} else {
				// We were on a leaf (or n=1, which is degenerate) and need to
				// try
				// again
				apCount = 0;
				visitedAtomIDs = new BitSet(numAtoms);
				atomLayer = new BitSet(numAtoms);
				atomLayer.set(bonds.iterator().next().getEndIdx());
				nextAtomLayer = new BitSet(numAtoms);
				while (atomLayer.cardinality() > 0) {
					visitedAtomIDs.or(atomLayer);
					for (int atIdx =
							atomLayer.nextSetBit(0); atIdx >= 0; atIdx =
									atomLayer.nextSetBit(atIdx + 1)) {
						for (int bondedAtomIdx : graphNeighbours[atIdx]) {
							if (apatoms.get(atIdx)) {
								if (!apatoms.get(bondedAtomIdx)) {
									// Dont traverse bonds to be broken!
									nextAtomLayer.set(bondedAtomIdx);
								} else {
									// Both atoms are in bonds to break - need
									// to
									// check if is in same bond
									boolean traverse = true;
									for (BondIdentifier bond : bonds) {
										if (bond.isToAtomWithIdx(atIdx)
												&& bond.isToAtomWithIdx(
														bondedAtomIdx)) {
											// Same bond - dont traverse
											traverse = false;
											apCount++;
											break;
										}
									}
									if (traverse) {
										nextAtomLayer.set(bondedAtomIdx);
									}
								}
							} else {
								nextAtomLayer.set(bondedAtomIdx);
							}
						}
					}
					nextAtomLayer.andNot(visitedAtomIDs);
					atomLayer.clear();
					atomLayer.or(nextAtomLayer);
					nextAtomLayer.clear();
				}
			}
			// If we are here and apCount is neither 1 (we were on a leaf) or
			// bonds.size() (we were on the core) then this is a broken
			// fragmentation
			if (apCount != 1 && apCount != bonds.size()) {
				return null;
			}
			valueAtomIDsLookup.put(bonds, visitedAtomIDs);
		}
		return valueAtomIDsLookup.get(bonds);
	}

	/**
	 * An efficient, toolkit-independent routine to generate a list of atom IDs
	 * for a fragmentation leaf walking the graphNeighbours object
	 * 
	 * @param bond
	 *            The bond being broken to give the current leaf
	 * @param isFromStartAtom
	 *            Is the leaf attached to the start atom (<code>true</code>) or
	 *            the end atom (<code>false</code>) of the bond
	 * @return A Set of the atom IDs of the current leaf
	 * @throws IllegalArgumentException
	 *             If no bonds are supplied
	 * @throws MoleculeFragmentationException
	 *             If more then one attachment point is found in the graph walk
	 *             from the indicated bond end
	 */
	protected BitSet listLeafAtomIds(BondIdentifier bond,
			boolean isFromStartAtom)
			throws IllegalArgumentException, MoleculeFragmentationException {
		if (bond == null) {
			throw new IllegalArgumentException("A bond must be supplied");
		}

		if ((isFromStartAtom && !startLeafAtomIDsLookup.containsKey(bond))
				|| !endLeafAtomIDsLookup.containsKey(bond)) {
			int apCount = 0;
			int numAtoms = graphNeighbours.length;
			BitSet visitedAtomIDs = new BitSet(numAtoms);
			BitSet atomLayer = new BitSet(numAtoms);
			atomLayer.set(
					isFromStartAtom ? bond.getStartIdx() : bond.getEndIdx());
			BitSet nextAtomLayer = new BitSet(numAtoms);
			while (atomLayer.cardinality() > 0) {
				visitedAtomIDs.or(atomLayer);
				for (int atIdx = atomLayer.nextSetBit(0); atIdx >= 0; atIdx =
						atomLayer.nextSetBit(atIdx + 1)) {
					boolean isAP = false;
					if (bond.isToAtomWithIdx(atIdx)) {
						apCount++;
						if (apCount > 1) {
							throw new MoleculeFragmentationException();
						}
						isAP = true;
					}
					for (int bondedAtomIdx : graphNeighbours[atIdx]) {
						if (isAP) {
							if (!bond.isToAtomWithIdx(bondedAtomIdx)) {
								// Dont traverse bonds to be broken!
								nextAtomLayer.set(bondedAtomIdx);
							}
						} else {
							nextAtomLayer.set(bondedAtomIdx);
						}
					}
				}
				nextAtomLayer.andNot(visitedAtomIDs);
				atomLayer.clear();
				atomLayer.or(nextAtomLayer);
				nextAtomLayer.clear();
			}

			// If we are here and apCount is neither 1 (we were on a leaf) or
			// bonds.size() (we were on the core) then this is a broken
			// fragmentation
			if (apCount != 1) {
				throw new MoleculeFragmentationException();
			}
			if (isFromStartAtom) {
				startLeafAtomIDsLookup.put(bond, visitedAtomIDs);
			} else {
				endLeafAtomIDsLookup.put(bond, visitedAtomIDs);
			}
		}
		return isFromStartAtom ? startLeafAtomIDsLookup.get(bond)
				: endLeafAtomIDsLookup.get(bond);

	}

	/**
	 * Method to calculate those bond triplets which are not a valid cut.
	 * 
	 * @return The set of invalid triplets
	 * @throws ClosedFactoryException
	 *             If the factory has been closed
	 * @throws ToolkitException
	 *             If the underlying toolkit throws an exception
	 */
	protected synchronized Set<Set<BondIdentifier>> getInvalidTriplets(
			int numCuts) throws ClosedFactoryException, ToolkitException {
		if (numCuts < 3) {
			return Collections.emptySet();
		}
		if (!invalidTriplets.containsKey(numCuts)) {
			// Initialise the list of invalid triplets
			Set<Set<BondIdentifier>> testTriplets = null;
			if (numCuts == 3) {
				if (cuttableBondCombos.containsKey(2)) {
					testTriplets = getAddOneCombinations(matchingBonds,
							cuttableBondCombos.get(2));
				} else {
					testTriplets = getNCombinations(3, matchingBonds);
				}
				Iterator<Set<BondIdentifier>> tripletIter =
						testTriplets.iterator();
				while (tripletIter.hasNext()) {
					Set<BondIdentifier> triplet = tripletIter.next();
					if (isValidCutTriplet(triplet)) {
						// We only want invalid triplets
						tripletIter.remove();
					}
				}
				invalidTriplets.put(numCuts, testTriplets);
			} else {
				// Find the most advanced set of invalid triplets and cuttable
				// bonds
				Set<BondIdentifier> allowedBonds = null;
				boolean allowedBondsAreNMinusOne = false;
				for (int i = numCuts - 1; i > 0 && (allowedBonds == null
						|| testTriplets == null); i--) {
					if (testTriplets != null
							&& invalidTriplets.containsKey(i)) {
						testTriplets = new HashSet<>(invalidTriplets.get(i));

					}
					if (allowedBonds != null && cuttableBonds.containsKey(i)) {
						allowedBonds = cuttableBonds.get(i);
						if (i == numCuts - 1) {
							allowedBondsAreNMinusOne = true;
						}
					}
				}

				if (testTriplets == null) {
					// just use the basic invalid triplets
					testTriplets = getInvalidTriplets(3);
				}

				if (allowedBonds != null) {
					Iterator<Set<BondIdentifier>> tripletIter =
							testTriplets.iterator();
					while (tripletIter.hasNext()) {
						if (allowedBonds.containsAll(tripletIter.next())) {
							tripletIter.remove();
						}
					}
					if (allowedBondsAreNMinusOne) {
						invalidTriplets.put(numCuts, testTriplets);
					}
				}
			}
			// May not have been cached, so just return it here
			return testTriplets;
		}
		return invalidTriplets.get(numCuts);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * isValidCutTriplet(java.util.Set)
	 */
	@Override
	public boolean isValidCutTriplet(Set<BondIdentifier> triplet)
			throws ToolkitException, ClosedFactoryException {
		return rawListValueAtomIds(triplet) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * generateCuttableBondCombos(int, int)
	 */
	@Override
	public Set<Set<BondIdentifier>> generateCuttableBondCombos(int minNumCuts,
			int maxNumCuts) throws IllegalArgumentException, ToolkitException,
			ClosedFactoryException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (minNumCuts < 2) {
			throw new IllegalArgumentException(
					"minNumCuts should be 2 or higher");
		}
		if (maxNumCuts < minNumCuts) {
			throw new IllegalArgumentException(
					"maxNumCuts should not be less than minNumCuts");
		}
		if (isHAdded && maxNumCuts > 1) {
			throw new IllegalArgumentException(
					"Only 1 cut should be made to H-added molecules");
		}

		Set<Set<BondIdentifier>> bondCombos = new LinkedHashSet<>();
		Map<Integer, Set<Set<BondIdentifier>>> bondCombosToCheck =
				new TreeMap<>();
		// Generate the combinations of upto numCuts bonds. NB we start at 2 as
		// 1 is handled separately
		for (int n = minNumCuts; n <= maxNumCuts
				&& n <= matchingBonds.size(); n++) {
			if (cuttableBondCombos.containsKey(n)) {
				// We already have the combos, pre-checked for validity, for
				// n-cuts, so simply use them
				bondCombos.addAll(cuttableBondCombos.get(n));
				continue;
			}

			// We need to generate the n-Combos, in the most efficient manner
			// possible
			// Start by finding the smallest number of bonds we can get away
			// with using
			Set<BondIdentifier> bondsToCombinate = getBondsToCombinate(n);
			if (bondsToCombinate.isEmpty()) {
				// No bonds to combinate means no combos at this or higher level
				break;
			}

			// If n=1, then the 'combinations' are simply the singleton sets, so
			// generate them and cache them
			if (n == 1) {
				Set<Set<BondIdentifier>> newCombos =
						createSetOfSingletonSets(bondsToCombinate);
				cuttableBondCombos.put(1, newCombos);
				bondCombos.addAll(newCombos);
				continue;
			} else if (n == 2) {
				// We need to find all pairwise combinations
				// No need to check them - generate them and cache them
				Set<Set<BondIdentifier>> newCombos =
						getNCombinations(n, bondsToCombinate);
				bondCombos.addAll(newCombos);
				cuttableBondCombos.put(2, newCombos);
				continue;
			}

			// Now, do we need to generate all combos from scratch, or do we
			// have the n-1 set stored?
			assert (n > 2);
			Set<Set<BondIdentifier>> nMinusOneCombos = null;
			if (cuttableBondCombos.containsKey(n - 1)) {
				nMinusOneCombos = cuttableBondCombos.get(n - 1);
			} else if (bondCombosToCheck.containsKey(n - 1)) {
				nMinusOneCombos = bondCombosToCheck.get(n - 1);
			}

			// Now do we need to run a quick clean-up of the combinations to
			// save time / memory?
			if (n == 4 || numCombinations(bondsToCombinate.size(), n) > 10000) {
				// Filter also if n==4 to remove invalid triples before we
				// proceed
				// Filter here if the number of
				// combinations is going to be too big, to update the caches
				// with any computed combos so far. Ensure ascending order
				// to not recompute when finding cuttable combos
				checkInvalidTriplets(bondCombos, bondCombosToCheck);
				// And now, those n-1 combos are on the stack, so go and get
				// them, along with the cuttablebonds
				nMinusOneCombos = cuttableBondCombos.get(n - 1);
				bondsToCombinate = identifyAllCuttableBonds(n - 1);
				if (bondsToCombinate.isEmpty()) {
					// No bonds to combinate means no combos at this or higher
					// level
					break;
				}
			}

			Set<Set<BondIdentifier>> newCombos;
			if (nMinusOneCombos == null) {
				// if minCuts was 3 or more, we could still get here
				newCombos = getNCombinations(n, bondsToCombinate);
			} else {
				// Generate the new combos from the n-1 combos
				newCombos = getAddOneCombinations(bondsToCombinate,
						nMinusOneCombos);
			}
			if (newCombos.isEmpty()) {
				break;
			} else {
				bondCombosToCheck.put(n, newCombos);
			}
		}
		checkInvalidTriplets(bondCombos, bondCombosToCheck);
		// Finally, check that the new combos are all valid
		return bondCombos;
	}

	/**
	 * Method to check a Map of Bond Combinations for validity
	 * 
	 * @param bondCombos
	 *            The result container - valid combinations are put here!
	 * @param bondCombosToCheck
	 *            The combinations to check. K = the number of cuts, V = the
	 *            combinations
	 * @throws ClosedFactoryException
	 * @throws ToolkitException
	 */
	protected void checkInvalidTriplets(Set<Set<BondIdentifier>> bondCombos,
			Map<Integer, Set<Set<BondIdentifier>>> bondCombosToCheck)
			throws ClosedFactoryException, ToolkitException {
		for (Integer i : bondCombosToCheck.keySet()) {
			final Set<Set<BondIdentifier>> uncheckedCombos =
					bondCombosToCheck.get(i);
			removeInvalidTriplets(uncheckedCombos, i);
			// Add them to the result and to the cache so that the
			// next call of
			bondCombos.addAll(uncheckedCombos);
			cuttableBondCombos.put(i, uncheckedCombos);
			cuttableBonds.put(i, uncheckedCombos.stream()
					.flatMap(x -> x.stream()).collect(Collectors.toSet()));
		}
		bondCombosToCheck.clear();
	}

	/**
	 * Method to generate a new set of bond combinations by adding one bond to
	 * each of the existing combinations
	 * 
	 * @param bondsToCombinate
	 *            Bonds to add
	 * @param nMinusOneCombos
	 *            Existing combinations
	 * @return The new combinations
	 */
	protected Set<Set<BondIdentifier>> getAddOneCombinations(
			Set<BondIdentifier> bondsToCombinate,
			Set<Set<BondIdentifier>> nMinusOneCombos) {
		Set<Set<BondIdentifier>> newCombos;
		newCombos = new HashSet<>();
		for (BondIdentifier newBond : bondsToCombinate) {
			for (Set<BondIdentifier> parentCombo : nMinusOneCombos) {
				if (!parentCombo.contains(newBond)) {
					Set<BondIdentifier> newCombo = new TreeSet<>(parentCombo);
					newCombo.add(newBond);
					newCombos.add(newCombo);
				}
			}
		}
		return newCombos;
	}

	/**
	 * Convenience method to get combinations via {@link CombinationFinder}
	 * 
	 * @param n
	 *            The number of bonds in each combination
	 * @param bondsToCombinate
	 *            The list of bonds to draw combinations from
	 * @return The combinations
	 */
	protected Set<Set<BondIdentifier>> getNCombinations(int n,
			Set<BondIdentifier> bondsToCombinate) {
		return CombinationFinder.getCombinationsFor(bondsToCombinate, n);
	}

	/**
	 * Method to generate 1-bond 'combinations' by wrapping each bond into a
	 * singleton set
	 * 
	 * @param bondsToCombinate
	 *            The bonds
	 * @return A set of singleton sets
	 */
	protected Set<Set<BondIdentifier>> createSetOfSingletonSets(
			Set<BondIdentifier> bondsToCombinate) {
		Set<Set<BondIdentifier>> newCombos = new HashSet<>();
		for (BondIdentifier bond : bondsToCombinate) {
			newCombos.add(Collections.singleton(bond));
		}
		return newCombos;
	}

	/**
	 * Method to return the combinatable bonds. This is either the cuttable
	 * bonds for n-1 cuts if already cached, or failing that, all the matching
	 * bonds
	 * 
	 * @param n
	 *            The number of cuts
	 * @return The bonds to combinate
	 * @throws ClosedFactoryException
	 */
	protected Set<BondIdentifier> getBondsToCombinate(int n)
			throws ClosedFactoryException {
		Set<BondIdentifier> bondsToCombinate;
		if (n > 1 && cuttableBonds.containsKey(n - 1)) {
			// We already know the cuttable bonds for n-1, so only use them
			bondsToCombinate = cuttableBonds.get(n - 1);
		} else {
			// Use all the matching bonds
			bondsToCombinate = matchingBonds;
		}
		return bondsToCombinate;
	}

	/**
	 * Method to remove any combinations containing any invalid triplets
	 * 
	 * @param combos
	 *            A Set of BondCombos (Set&lt;BondIdentifier&gt;)
	 * @param numCuts
	 *            The number of cuts
	 * @throws ToolkitException
	 * @throws ClosedFactoryException
	 */
	protected void removeInvalidTriplets(Set<Set<BondIdentifier>> combos,
			Integer numCuts) throws ClosedFactoryException, ToolkitException {
		for (Set<BondIdentifier> triplet : getInvalidTriplets(numCuts)) {
			// Now check none of the combos contain that triplet
			Iterator<Set<BondIdentifier>> combosIter = combos.iterator();
			while (combosIter.hasNext()) {
				if (combosIter.next().containsAll(triplet)) {
					combosIter.remove();
				}
			}
			if (combos.isEmpty()) {
				break;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * generateCuttableBondCombos(int)
	 */
	@Override
	public synchronized Set<Set<BondIdentifier>> generateCuttableBondCombos(
			int numCuts) throws ToolkitException, ClosedFactoryException {
		if (isHAdded && numCuts > 1) {
			throw new IllegalArgumentException(
					"Only 1 cut should be made to H-added molecules");
		}
		if (!cuttableBondCombos.containsKey(numCuts)) {

			Set<Set<BondIdentifier>> newBondCombos = new HashSet<>();
			if (numCuts == 1) {
				for (BondIdentifier bond : matchingBonds) {
					newBondCombos.add(Collections.singleton(bond));
				}
			} else if (numCuts > 1) {
				// If it's not then there are no bonds so return empty!
				Set<Set<BondIdentifier>> nMinusOneCombos =
						generateCuttableBondCombos(numCuts - 1);
				Set<BondIdentifier> cuttableBonds =
						identifyAllCuttableBonds(numCuts - 1);
				for (BondIdentifier newBond : cuttableBonds) {
					for (Set<BondIdentifier> parentCombo : nMinusOneCombos) {
						if (!parentCombo.contains(newBond)) {
							Set<BondIdentifier> newCombo =
									new HashSet<>(parentCombo);
							newCombo.add(newBond);
							newBondCombos.add(newCombo);
						}
					}
				}
				if (numCuts >= 3) {
					// Now work through each invalid triplet in turn...
					for (Set<BondIdentifier> triplet : getInvalidTriplets(
							numCuts)) {
						// Now check none of the combos contain that triplet
						Iterator<Set<BondIdentifier>> newCombosIter =
								newBondCombos.iterator();
						while (newCombosIter.hasNext()) {
							if (newCombosIter.next().containsAll(triplet)) {
								newCombosIter.remove();
							}
						}
						if (newBondCombos.isEmpty()) {
							break;
						}
					}
				}
			}
			cuttableBondCombos.put(numCuts, newBondCombos);
		}
		return cuttableBondCombos.get(numCuts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * identifyAllCuttableBonds(int)
	 */
	@Override
	public synchronized Set<BondIdentifier> identifyAllCuttableBonds(
			int numCuts) throws ClosedFactoryException, ToolkitException {
		if (!cuttableBonds.containsKey(numCuts)) {
			cuttableBonds.put(numCuts,
					generateCuttableBondCombos(numCuts).stream()
							.flatMap(x -> x.stream()).distinct()
							.collect(Collectors.toSet()));
		}
		return cuttableBonds.get(numCuts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * getMaximumNumberOfCuts()
	 */
	@Override
	public synchronized int getMaximumNumberOfCuts(
			boolean allowTwoCutsToSingleBond)
			throws ClosedFactoryException, ToolkitException {
		if (isClosed) {
			throw new ClosedFactoryException();
		}
		if (maxCuts == null) {
			Set<BondIdentifier> bonds = matchingBonds;
			if (bonds.isEmpty()) {
				// No matching bonds
				maxCuts = 0;
			} else if (isHAdded && !bonds.isEmpty()) {
				// H-Added factories should only perform 1 cut
				maxCuts = 1;
			} else if (bonds.size() == 1) {
				// Only 1 matching bond, so this is another special case, of 1
				maxCuts = 1;
			} else {
				// We need to blow the molecule apart and count leafs
				long wave = getGCWave();
				List<T> components = breakAllMatchingBonds(wave);
				int leafCount = 0;
				for (T comp : components) {
					if (isLeaf(comp, wave)) {
						leafCount++;
					}
				}
				doCleanup(wave);
				maxCuts = leafCount;
				// for (int i = isHAdded && !bonds.isEmpty() ? 1 : bonds.size();
				// i
				// >= 0
				// && maxCuts == null; i--) {
				// // Start with the most possible - the number of matching
				// bonds,
				// // or 1 if hadded and has any matching bonds
				// // and
				// // work down
				// if (!generateCuttableBondCombos(i).isEmpty()) {
				// // If only 1 or two bonds, all possibilities are cuttable
				// if (i == 1 && allowTwoCutsToSingleBond) {
				// maxCuts = 2;
				// } else {
				// maxCuts = i;
				// }
				// }
				// }
			}
			if (!isHAdded && allowTwoCutsToSingleBond && maxCuts == 1) {
				maxCuts = 2;
			}

			if (maxCuts == null) {
				maxCuts = 0;
			}
		}
		return maxCuts;
	}

	/**
	 * Method to check whether a marked component generated from
	 * {@link #breakAllMatchingBonds(long)} is a leaf
	 * 
	 * @param component
	 *            The component
	 * @param wave
	 *            TODO
	 * @return {@code true} if component is a leaf (has only 1 attachment point)
	 */
	protected abstract boolean isLeaf(T component, long wave);

	/**
	 * @param wave
	 *            TODO
	 * @return A list of individual components resulting from marking and
	 *         breaking every matching bond
	 * @throws ClosedFactoryException
	 */
	protected abstract List<T> breakAllMatchingBonds(long wave)
			throws ClosedFactoryException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.mmp.fragmentors.MoleculeFragmentationFactory#
	 * getMatchingBonds()
	 */
	@Override
	public Set<BondIdentifier> getMatchingBonds()
			throws ClosedFactoryException {
		if (matchingBonds == null) {
			matchingBonds = identifyAllMatchingBonds();
		}
		return matchingBonds;
	}

	/**
	 * {@inheritDoc} Subclasses should call super.close() if they override this
	 * method
	 */
	@Override
	public void close() {

		mol = null;
		bondMatch = null;
		matchingBonds = null;
		cuttableBonds = null;
		triplets = null;
		invalidTriplets = null;
		valueAtomIDsLookup = null;
		startLeafAtomIDsLookup = null;
		endLeafAtomIDsLookup = null;
		isClosed = true;
		cuttableBondCombos = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + (isClosed ? "[Closed]"
				: " [mol=" + getMolSmiles() + ", isChiral=" + isChiral
						+ ", hasUndefinedChirality=" + hasUndefinedChirality
						+ ", hasNonflaggedDoubleBonds="
						+ hasNonflaggedDoubleBonds + ", verboseLogging="
						+ verboseLogging + ", removeHs=" + removeHs
						+ ", maxNumberChangingHAs=" + maxNumberChangingHAs
						+ ", minCnstToVarAtmRatio=" + minCnstToVarAtmRatio
						+ ", HAC=" + HAC + "]");
	}

	/**
	 * Convenience method to return efficiently possibly large numbers of
	 * combinations as a long integer whilst trying to avoid numeric overflow
	 * 
	 * @param numObjects
	 *            The number of objects to draw combinations from
	 * @param numSample
	 *            The number of objects in each combination
	 * @return The number of combinations
	 */
	public static long numCombinations(int numObjects, int numSample) {
		if (numSample > numObjects) {
			throw new IllegalArgumentException(
					"Cant have more objects in sample than in the set!");
		}
		if (numObjects < 1 || numSample < 1) {
			throw new IllegalArgumentException(
					"Need at least one object and at least one in the sample");
		}
		long retVal = 1L;
		// numObjects! / numSample!(numSample-numObjects)!
		if (numSample > (numObjects - numSample)) {
			for (int i = numObjects; i > numSample; i--) {
				// numObjects!/numSample!, e.g 5x4x3x2x1/(3x2x1)= 5x4
				retVal *= i;
			}
			for (int i = 1; i <= (numObjects - numSample); i++) {
				retVal /= i;
			}
		} else {
			for (int i = numObjects; i > (numObjects - numSample); i--) {
				// numObjects!/(numObjects-numSample)!, e.g 5x4x3x2x1/(2x1)=
				// 5x4x3
				retVal *= i;
			}
			for (int i = 1; i <= numSample; i++) {
				retVal /= i;
			}
		}
		return retVal;

	}
}
