/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.mmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.RDKit.ChemicalReaction;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.ROMol_Vect_Vect;
import org.knime.chem.types.SmilesCell;
import org.knime.core.data.DataCell;
import org.knime.core.node.NodeModel;

/**
 * This class provides some static convenience methods for the fragmenting of
 * molecules and the conversion of fragmented molecules to MMP transforms
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public class RDKitFragment {
	/**
	 * Convenience method to apply the fragmentation to an ROMol input object.
	 * This is the most likely entrance point.
	 * 
	 * @param roMol
	 *            The molecule to fragment
	 * @param ID
	 *            The molecule ID
	 * @param numFragmentations
	 *            The number of rounds of fragmentations to apply (i.e. the
	 *            number of bonds to break)
	 * @param fragSMIRKS
	 *            The RDKit reaction SMARTS (SMIRKS) to use to break the bonds.
	 *            Attachment points in the 'products' should be included as
	 *            '[*]' in order to allow corrected labelling
	 * @param trackCutConnectivity
	 *            If <code>true</code>, then the attachment points are labelled
	 *            to preserve regiochemistry
	 * @return {@link HashMap} of {@link FragmentKey}s linking to
	 *         {@link TreeSet} of {@link FragmentValue}s
	 */
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> doRDKitFragmentation(
			ROMol roMol, String ID, int numFragmentations, String fragSMIRKS,
			boolean trackCutConnectivity) {

		// Load up the ROMol and ID into the relevant formats
		HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<FragmentKey, TreeSet<FragmentValue>>();
		FragmentKey key = new FragmentKey();
		FragmentValue val = new FragmentValue(roMol.MolToSmiles(true), ID);
		TreeSet<FragmentValue> valSet = new TreeSet<FragmentValue>();
		valSet.add(val);
		tmp.put(key, valSet);

		// Send to the recursive method
		return doRDKitFragmentation(tmp, numFragmentations, fragSMIRKS,
				trackCutConnectivity);

	}

	/**
	 * This method performs multiple layers of fragmentation by recursion using
	 * the RDKit toolkit
	 * 
	 * @param fragments
	 *            Fragments from previous fragmentation layer.
	 *            {@link #doRDKitFragmentation(ROMol, String, int, String, boolean)}
	 *            is a convenience method to convert an initial input molecule
	 *            to this format correctly.
	 * @param numFragmentations
	 *            The number of rounds of fragmentations to apply (i.e. the
	 *            number of bonds to break)
	 * @param fragSMIRKS
	 *            The RDKit reaction SMARTS (SMIRKS) to use to break the bonds.
	 *            Attachment points in the 'products' should be included as
	 *            '[*]' in order to allow corrected labelling
	 * @param trackCutConnectivity
	 *            If <code>true</code>, then the attachment points are labelled
	 *            to preserve regiochemistry
	 * @return {@link HashMap} of {@link FragmentKey}s linking to
	 *         {@link TreeSet} of {@link FragmentValue}s
	 * @see {@link #doRDKitFragmentation(ROMol, String, int, String, boolean)}
	 *      for preferred entry point
	 */
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> doRDKitFragmentation(
			HashMap<FragmentKey, TreeSet<FragmentValue>> fragments,
			int numFragmentations, String fragSMIRKS,
			boolean trackCutConnectivity) {
		if (numFragmentations == 0) {
			return fragments;
		} else {
			HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<FragmentKey, TreeSet<FragmentValue>>();
			// Fragment
			for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : fragments
					.entrySet()) {
				// We only fragment the values
				for (FragmentValue fragVal : ent.getValue()) {
					ROMol roMol = fragVal.getROMol();

					HashMap<FragmentKey, TreeSet<FragmentValue>> frags = runRDKitFragment(
							roMol, fragVal.getID(), fragSMIRKS,
							numFragmentations, trackCutConnectivity);

					// Now we need to add the result to the retVal
					for (Entry<FragmentKey, TreeSet<FragmentValue>> ent1 : frags
							.entrySet()) {
						FragmentKey newKey = new FragmentKey(ent.getKey());
						newKey.mergeKeys(ent1.getKey());
						if (!tmp.containsKey(newKey)) {
							tmp.put(newKey, new TreeSet<FragmentValue>());
						}
						tmp.get(newKey).addAll(ent1.getValue());
					}
				}
			}

			// And supply the result to recursion
			HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = doRDKitFragmentation(
					tmp, numFragmentations - 1, fragSMIRKS,
					trackCutConnectivity);
			return retVal;
		}
	}

	/**
	 * This method actually runs a fragmentation on an individual ROMol - public
	 * use is discouraged! It is called repeatedly from
	 * {@link #doRDKitFragmentation(HashMap, int, String, boolean)}.
	 * 
	 * @see #doRDKitFragmentation(HashMap, int, String, boolean)
	 */
	private static HashMap<FragmentKey, TreeSet<FragmentValue>> runRDKitFragment(
			ROMol roMol, String id, String fragSMIRKS, Integer cutIndex,
			boolean trackCutConnectivity) {

		ChemicalReaction rxn;
		if (trackCutConnectivity) {
			// Atom index labels, e.g. [*:1] are lost, but isotopic labels are
			// preserved, e.g. [1*] during RDKit reaction
			rxn = ChemicalReaction.ReactionFromSmarts(fragSMIRKS.replace("[*]",
					"[" + cutIndex.intValue() + "*]"));
		} else {
			rxn = ChemicalReaction.ReactionFromSmarts(fragSMIRKS);
		}
		ROMol_Vect rxnSubstrate = new ROMol_Vect(1);
		ROMol_Vect_Vect rxnProds = null;
		rxnSubstrate.set(0, roMol);
		HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = new HashMap<FragmentKey, TreeSet<FragmentValue>>();

		try {
			rxnProds = rxn.runReactants(rxnSubstrate);
		} catch (Exception e) {
			return retVal;
		}

		for (int i = 0; i < rxnProds.size(); i++) {
			ROMol_Vect prod = rxnProds.get(i);
			String[] smi = molVectToSMILESArray(prod);
			if (smi[0].split("\\[[0-9]*?\\*").length == 2) {
				// smi0 can be a key
				FragmentKey key0 = new FragmentKey(smi[0]);
				if (!retVal.containsKey(key0)) {
					retVal.put(key0, new TreeSet<FragmentValue>());
				}
				retVal.get(key0).add(new FragmentValue(smi[1], id));
			}

			if (smi[1].split("\\[[0-9]*?\\*").length == 2) {
				// smi1 can be a key
				FragmentKey key1 = new FragmentKey(smi[1]);
				if (!retVal.containsKey(key1)) {
					retVal.put(key1, new TreeSet<FragmentValue>());
				}
				retVal.get(key1).add(new FragmentValue(smi[0], id));
			}
		}

		return retVal;
	}

	/**
	 * Get the transfroms for a {@link TreeSet} of {@link FragmentValue}s,
	 * optionally including the number of changing heavy atoms. This is the
	 * simple method not requiring access to the {@link FragmentKey}
	 * 
	 * @param fragmentValues
	 *            A {@link TreeSet} of {@link FragmentValue}s from which to
	 *            derive transforms
	 * @param numNewCols
	 *            The number of new columns - supplied so only calculated once
	 *            during the {@link NodeModel} <code>#configure</code> method.
	 * @param removeExplicitHs
	 *            If true, then explicit hydrogens are removed from the outputs
	 * @param includeNumChangingHAs
	 *            If true, then the number of changing heavy atoms are added for
	 *            each fragment
	 * @return An {@link ArrayList} of {@link DataCell}s for each new transform
	 *         to be added derived from the {@link TreeSet} of
	 *         {@link FragmentValue}s
	 * @see #getTransforms(TreeSet, FragmentKey, int, boolean, boolean, boolean,
	 *      boolean)
	 */
	public static ArrayList<DataCell[]> getTransforms(
			TreeSet<FragmentValue> fragmentValues, int numNewCols,
			boolean removeExplicitHs, boolean includeNumChangingHAs) {

		ArrayList<DataCell[]> retVal = new ArrayList<DataCell[]>();
		TreeSet<FragmentValue> orderedFrags = new TreeSet<FragmentValue>(
				fragmentValues);
		for (FragmentValue leftFrag : orderedFrags) {
			for (FragmentValue rightFrag : orderedFrags
					.tailSet(leftFrag, false)) {
				if (!leftFrag.getID().equals(rightFrag.getID())) {
					DataCell[] transform = new DataCell[numNewCols];
					int i = 0;
					transform[i++] = new SmilesCell(
							leftFrag.getSMILES(removeExplicitHs) + ">>"
									+ rightFrag.getSMILES(removeExplicitHs));
					transform[i++] = leftFrag.getIDCell();
					transform[i++] = rightFrag.getIDCell();
					transform[i++] = leftFrag.getSMILESCell(removeExplicitHs);
					transform[i++] = rightFrag.getSMILESCell(removeExplicitHs);
					if (includeNumChangingHAs) {
						transform[i++] = leftFrag.getNumberChangingAtomsCell();
						transform[i++] = rightFrag.getNumberChangingAtomsCell();
					}
					retVal.add(transform);
				}
			}
		}
		return retVal;
	}

	/**
	 * Overloaded method to get the transforms for a {@link TreeSet} of
	 * {@link FragmentValue}s, optionally including the number of changing heavy
	 * atoms, and properties relating to the {@link FragmentKey}.
	 * 
	 * @param fragmentValues
	 *            A {@link TreeSet} of {@link FragmentValue}s from which to
	 *            derive transforms
	 * @param fragmentKey
	 *            The {@link FragmentKey} from which to derive the unchanging
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
	 * @return An {@link ArrayList} of {@link DataCell}s for each new transform
	 *         to be added derived from the {@link TreeSet} of
	 *         {@link FragmentValue}s
	 * @see #getTransforms(TreeSet, int, boolean, boolean)
	 */
	public static ArrayList<DataCell[]> getTransforms(
			TreeSet<FragmentValue> fragmentValues, FragmentKey fragmentKey,
			int numNewCols, boolean removeExplicitHs, boolean includeKeySMILES,
			boolean includeNumChangingHAs, boolean includeRatioHAs) {

		ArrayList<DataCell[]> retVal = new ArrayList<DataCell[]>();
		TreeSet<FragmentValue> orderedFrags = new TreeSet<FragmentValue>(
				fragmentValues);
		for (FragmentValue leftFrag : orderedFrags) {
			for (FragmentValue rightFrag : orderedFrags
					.tailSet(leftFrag, false)) {
				if (!leftFrag.getID().equals(rightFrag.getID())) {
					DataCell[] transform = new DataCell[numNewCols];
					int i = 0;
					transform[i++] = new SmilesCell(
							leftFrag.getSMILES(removeExplicitHs) + ">>"
									+ rightFrag.getSMILES(removeExplicitHs));
					transform[i++] = leftFrag.getIDCell();
					transform[i++] = rightFrag.getIDCell();
					transform[i++] = leftFrag.getSMILESCell(removeExplicitHs);
					transform[i++] = rightFrag.getSMILESCell(removeExplicitHs);
					if (includeKeySMILES) {
						transform[i++] = fragmentKey
								.getKeyAsDataCell(removeExplicitHs);
					}
					if (includeNumChangingHAs) {
						transform[i++] = leftFrag.getNumberChangingAtomsCell();
						transform[i++] = rightFrag.getNumberChangingAtomsCell();
					}
					if (includeRatioHAs) {
						transform[i++] = fragmentKey
								.getConstantToVaryingAtomRatioCell(leftFrag);
						transform[i++] = fragmentKey
								.getConstantToVaryingAtomRatioCell(rightFrag);
					}
					retVal.add(transform);
				}
			}
		}
		return retVal;
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
	 * Utility function to filter fragments by number and ratio of heavy atoms
	 * 
	 * @param fragments
	 *            The fragments to filter
	 * @param maxNumVarAtoms
	 *            The maximum number of varying heavy atoms (null if filter not
	 *            to be applied)
	 * @param minCnstToVarAtmRatio
	 *            The minimum ration of varying to constant heavy atoms (null if
	 *            filter not to be applied)
	 * @return The filtered list of fragments
	 */
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> filterFragments(
			HashMap<FragmentKey, TreeSet<FragmentValue>> fragments,
			Integer maxNumVarAtoms, Double minCnstToVarAtmRatio) {

		if (maxNumVarAtoms == null && minCnstToVarAtmRatio == null) {
			// No filtering applied
			return fragments;
		}

		HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = new HashMap<FragmentKey, TreeSet<FragmentValue>>();
		for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : fragments
				.entrySet()) {
			TreeSet<FragmentValue> keptVals = new TreeSet<FragmentValue>();
			for (FragmentValue val : ent.getValue()) {
				boolean keepVal = true;
				if (maxNumVarAtoms != null
						&& val.getNumberChangingAtoms() > maxNumVarAtoms) {
					keepVal = false;
				}
				if (minCnstToVarAtmRatio != null && keepVal) {
					// Only check if the val is still 'in'
					keepVal = ent.getKey().getConstantToVaryingAtomRatio(val) >= minCnstToVarAtmRatio;
				}
				if (keepVal) {
					keptVals.add(val);
				}
			}
			if (keptVals.size() > 0) {
				retVal.put(ent.getKey(), keptVals);
			}
		}
		return retVal;
	}

	/**
	 * Convenience method to apply the fragmentation to an ROMol input object
	 * 
	 * @deprecated
	 * @see #doRDKitFragmentation(HashMap, int, String, boolean)
	 */
	@Deprecated
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> doRDKitFragmentationFromRxn(
			ROMol roMol, String ID, int numFragmentations, ChemicalReaction rxn) {

		// Load up the ROMol and ID into the relevant formats
		HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<FragmentKey, TreeSet<FragmentValue>>();
		FragmentKey key = new FragmentKey();
		FragmentValue val = new FragmentValue(roMol.MolToSmiles(true), ID);
		TreeSet<FragmentValue> valSet = new TreeSet<FragmentValue>();
		valSet.add(val);
		tmp.put(key, valSet);

		// Send to the recursive method
		return doRDKitFragmentationFromRxn(tmp, numFragmentations, rxn);

	}

	/**
	 * This method performs multiple layers of fragmentation by recursion
	 * 
	 * @deprecated
	 * @see #doRDKitFragmentation(HashMap, int, String, boolean)
	 */
	@Deprecated
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> doRDKitFragmentationFromRxn(
			HashMap<FragmentKey, TreeSet<FragmentValue>> fragments,
			int numFragmentations, ChemicalReaction rxn) {
		if (numFragmentations == 0) {
			return fragments;
		} else {
			HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<FragmentKey, TreeSet<FragmentValue>>();
			// Fragment
			for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : fragments
					.entrySet()) {
				// We only fragment the values
				for (FragmentValue fragVal : ent.getValue()) {
					ROMol roMol = fragVal.getROMol();

					HashMap<FragmentKey, TreeSet<FragmentValue>> frags = runRDKitFragmentFromRxn(
							roMol, fragVal.getID(), rxn, numFragmentations);

					// Now we need to add the result to the retVal
					for (Entry<FragmentKey, TreeSet<FragmentValue>> ent1 : frags
							.entrySet()) {
						FragmentKey newKey = new FragmentKey(ent.getKey());
						newKey.mergeKeys(ent1.getKey());
						if (!tmp.containsKey(newKey)) {
							tmp.put(newKey, new TreeSet<FragmentValue>());
						}
						tmp.get(newKey).addAll(ent1.getValue());
					}
				}
			}

			// And supply the result to recursion
			HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = doRDKitFragmentationFromRxn(
					tmp, numFragmentations - 1, rxn);
			return retVal;
		}
	}

	/**
	 * This method actually runs a fragmentation - public use is discouraged!
	 * 
	 * @deprecated
	 * @see #runRDKitFragment(ROMol, String, String, Integer, boolean)
	 */
	@Deprecated
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> runRDKitFragmentFromRxn(
			ROMol roMol, String id, ChemicalReaction rxn, Integer cutIndex) {

		ROMol_Vect rxnSubstrate = new ROMol_Vect(1);
		ROMol_Vect_Vect rxnProds = null;
		rxnSubstrate.set(0, roMol);
		HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = new HashMap<FragmentKey, TreeSet<FragmentValue>>();

		try {
			// TODO: runReactants Loses mapping indices present on starting
			// material
			rxnProds = rxn.runReactants(rxnSubstrate);
		} catch (Exception e) {
			return retVal;
		}

		for (int i = 0; i < rxnProds.size(); i++) {
			ROMol_Vect prod = rxnProds.get(i);
			String[] smi = molVectToSMILESArray(prod);
			if (smi[0].split("\\[\\*").length == 2) {
				// smi0 can be a key
				FragmentKey key0 = new FragmentKey(smi[0]);
				if (!retVal.containsKey(key0)) {
					retVal.put(key0, new TreeSet<FragmentValue>());
				}
				// TODO: This is where the cut index needs to be introduced =
				// smi[1].replace("[*]","[*:"+cutIndex+"]")
				// FragmentValue will need to be updated too to cope...
				retVal.get(key0).add(new FragmentValue(smi[1], id));
			}

			if (smi[1].split("\\[\\*").length == 2) {
				// smi1 can be a key
				FragmentKey key1 = new FragmentKey(smi[1]);
				if (!retVal.containsKey(key1)) {
					retVal.put(key1, new TreeSet<FragmentValue>());
				}
				retVal.get(key1).add(new FragmentValue(smi[0], id));
			}
		}

		return retVal;
	}

}