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

import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationException;
import com.vernalis.knime.mmp.fragmentors.UnenumeratedStereochemistryException;

/**
 * <p>
 * Takes a multi-component smiles string, with n+1 components. 1 component must
 * have n attachment point and the other n components only 1 attachment point.
 * </p>
 * <p>
 * The parser implements comparable, and sorting is as follows:
 * <ol>
 * <li>Sorted by increasing number of cuts</li>
 * <li>Sorted by the canonicalised SMILES</li>
 * </ol>
 * </p>
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class MulticomponentSmilesFragmentParser
		implements Comparable<MulticomponentSmilesFragmentParser> {
	private final FragmentKey2 key;
	private FragmentValue2 value = null;
	private final String smiles; // The input smiles
	private final int numCuts;
	private final String canonicalisedSMILES;
	private final boolean removeHs;

	/**
	 * Constructor. The supplied SMILES string is parsed to a fragment value and
	 * key and stored in a canonicalised form. If the SMILES is a 2 component,
	 * then the first is treated as the key and the second as the value, and the
	 * reverse can be obtained using the {@link #getReverse()} method
	 * 
	 * @param smiles
	 *            The SMILES string to parse
	 * @throws MoleculeFragmentationException
	 *             Thrown if the SMILES is not a valid fragmentation
	 * @throws UnenumeratedStereochemistryException
	 *             If the SMILES contains a dative bond ('{@code >}') which
	 *             needs to be enumerated to {@code /} and {@code \}
	 */
	public MulticomponentSmilesFragmentParser(String smiles, boolean removeHs)
			throws MoleculeFragmentationException, UnenumeratedStereochemistryException {

		if (smiles.indexOf(">") >= 0 || smiles.indexOf("<") >= 0) {
			// Unenumerated double bond geometries to parse
			throw new UnenumeratedStereochemistryException(
					"Need to enumerate double bond isomer combos first...", smiles, removeHs);
		}

		this.removeHs = removeHs;

		String[] comps = smiles.split("\\.");
		if (comps.length < 2) {
			throw new MoleculeFragmentationException(
					"The supplied SMILES string must be multicomponent");
		}

		this.smiles = smiles;

		key = new FragmentKey2(removeHs);

		// Deal with the 1 cut special case first
		if (comps.length == 2) {
			key.addLeaf(new Leaf(comps[0], removeHs));
			if ((" " + comps[1] + " ").split("\\*").length != 2) {
				throw new MoleculeFragmentationException(
						"Wrong number of attachment points on FragmentValue: '" + comps[1] + "'");
			}
			value = new FragmentValue2(comps[1]);
		} else {

			// Now loop through
			for (String comp : comps) {
				try {
					key.addLeaf(new Leaf(comp, removeHs));
				} catch (IllegalArgumentException e) {
					// Not a key
					if (value != null) {
						// We can only have 1 value,
						throw new MoleculeFragmentationException("Can only have one fragment key");
					} else {
						if ((" " + comp + " ").split("\\*").length != comps.length) {
							// and it must have n attachment points
							throw new MoleculeFragmentationException(
									"Wrong number of attachment points on FragmentValue '" + comp
											+ "'");
						}
						value = new FragmentValue2(comp);
					}
				}
			}
		}
		// finally, set the indices and canonicalise
		value.setAttachmentPointIndices(key);
		numCuts = key.getNumComponents();
		// track connectivity for the canonicalisation to avoid an issue
		// with duplicate leaves in the key (No longer case as
		// ROMolFragmentFactory fixes this

		canonicalisedSMILES = key.getKeyAsString(false, true) + "." + value.getSMILES(false, true); // .getCanonicalIndexNaiveSMILES();
		// System.out.println("INPUT SMILES:\t\t"+smiles);
		// System.out.println("CANON SMILES:\t\t"+canonicalisedSMILES);
	}

	/**
	 * @return The fragment key component
	 */
	public FragmentKey2 getKey() {
		return key;
	}

	/**
	 * @return The fragment value component
	 */
	public FragmentValue2 getValue() {
		return value;
	}

	/**
	 * @return The number of cuts the fragmentation represents
	 */
	public int getNumCuts() {
		return numCuts;
	}

	/**
	 * @return The canonicalised SMILES string
	 */
	public String getCanonicalSMILES() {
		return canonicalisedSMILES;
	}

	/**
	 * Returns the reverse key/value pair for a single cut fragmentation
	 * 
	 * @return {@code null} if not a single cut, or the reverse key/value pair
	 * @throws MoleculeFragmentationException
	 *             If the reversal cannot be processed
	 * @throws UnenumeratedStereochemistryException
	 */
	public MulticomponentSmilesFragmentParser getReverse()
			throws MoleculeFragmentationException, UnenumeratedStereochemistryException {
		if (key.getNumComponents() == 1 && value != null) {
			return new MulticomponentSmilesFragmentParser(
					smiles.split("\\.")[1] + "." + smiles.split("\\.")[0], removeHs);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(MulticomponentSmilesFragmentParser o) {
		// Initially, we sort by increasing number of cuts
		if (numCuts > o.numCuts) {
			return 1;
		} else if (numCuts < o.numCuts) {
			return -1;
		} else {
			// Same number of cuts, so we compare the strings
			return canonicalisedSMILES.compareTo(o.canonicalisedSMILES);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((canonicalisedSMILES == null) ? 0 : canonicalisedSMILES.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MulticomponentSmilesFragmentParser other = (MulticomponentSmilesFragmentParser) obj;
		if (canonicalisedSMILES == null) {
			if (other.canonicalisedSMILES != null) {
				return false;
			}
		} else if (!canonicalisedSMILES.equals(other.canonicalisedSMILES)) {
			return false;
		}
		return true;
	}

}
