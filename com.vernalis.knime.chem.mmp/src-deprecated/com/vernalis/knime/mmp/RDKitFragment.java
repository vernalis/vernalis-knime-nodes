/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeModel;

/**
 * This class provides some static convenience methods for the fragmenting of
 * molecules and the conversion of fragmented molecules to MMP transforms. The
 * preferred entry point is
 * {@link #doRDKitFragmentation(ROMol, String, int, String, boolean, ExecutionContext)}
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * @deprecated All methods are inefficient and replaced with methods in the
 *             {@link RDKitFragmentationUtils} class
 */
@Deprecated
public class RDKitFragment {
	private RDKitFragment() {
		// Dont instantiate
	}

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
	 * @throws CanceledExecutionException
	 */
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> doRDKitFragmentation(ROMol roMol,
			String ID, int numFragmentations, String fragSMIRKS, boolean trackCutConnectivity,
			ExecutionContext exec) throws CanceledExecutionException {

		// Load up the ROMol and ID into the relevant formats
		HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<>();
		FragmentKey key = new FragmentKey();
		FragmentValue val = new FragmentValue(roMol.MolToSmiles(true), ID);
		TreeSet<FragmentValue> valSet = new TreeSet<>();
		valSet.add(val);
		tmp.put(key, valSet);

		// Send to the recursive method
		return doRDKitFragmentation(tmp, numFragmentations, fragSMIRKS, trackCutConnectivity, exec);

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
	 * @param exec
	 * @return {@link HashMap} of {@link FragmentKey}s linking to
	 *         {@link TreeSet} of {@link FragmentValue}s
	 * @throws CanceledExecutionException
	 * @see {@link #doRDKitFragmentation(ROMol, String, int, String, boolean)}
	 *      for preferred entry point
	 */
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> doRDKitFragmentation(
			HashMap<FragmentKey, TreeSet<FragmentValue>> fragments, int numFragmentations,
			String fragSMIRKS, boolean trackCutConnectivity, ExecutionContext exec)
			throws CanceledExecutionException {
		if (numFragmentations == 0) {
			return fragments;
		} else {
			HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<>();
			// Fragment
			for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : fragments.entrySet()) {
				// We only fragment the values
				for (FragmentValue fragVal : ent.getValue()) {
					ROMol roMol = fragVal.getROMol();

					HashMap<FragmentKey, TreeSet<FragmentValue>> frags =
							runRDKitFragment(roMol, fragVal.getID(), fragSMIRKS, numFragmentations,
									trackCutConnectivity, exec);

					// Now we need to add the result to the retVal
					for (Entry<FragmentKey, TreeSet<FragmentValue>> ent1 : frags.entrySet()) {
						FragmentKey newKey = new FragmentKey(ent.getKey());
						newKey.mergeKeys(ent1.getKey());
						if (!tmp.containsKey(newKey)) {
							tmp.put(newKey, new TreeSet<FragmentValue>());
						}
						tmp.get(newKey).addAll(ent1.getValue());
					}

					// Delete the ROMol
					roMol.delete();
					exec.checkCanceled();
				}
			}

			// And supply the result to recursion
			HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = doRDKitFragmentation(tmp,
					numFragmentations - 1, fragSMIRKS, trackCutConnectivity, exec);
			return retVal;
		}
	}

	/**
	 * This method actually runs a fragmentation on an individual ROMol - public
	 * use is discouraged! It is called repeatedly from
	 * {@link #doRDKitFragmentation(HashMap, int, String, boolean, ExecutionContext)}
	 * 
	 * @param exec
	 * @throws CanceledExecutionException
	 * 
	 * @see #doRDKitFragmentation(HashMap, int, String, boolean)
	 */
	private static HashMap<FragmentKey, TreeSet<FragmentValue>> runRDKitFragment(ROMol roMol,
			String id, String fragSMIRKS, Integer cutIndex, boolean trackCutConnectivity,
			ExecutionContext exec) throws CanceledExecutionException {

		ChemicalReaction rxn;
		if (trackCutConnectivity) {
			// Atom index labels, e.g. [*:1] are lost, but isotopic labels are
			// preserved, e.g. [1*] during RDKit reaction
			rxn = ChemicalReaction.ReactionFromSmarts(
					fragSMIRKS.replace("[*]", "[" + cutIndex.intValue() + "*]"));
		} else {
			rxn = ChemicalReaction.ReactionFromSmarts(fragSMIRKS);
		}
		ROMol_Vect rxnSubstrate = new ROMol_Vect(1);
		ROMol_Vect_Vect rxnProds = null;
		rxnSubstrate.set(0, roMol);
		HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = new HashMap<>();

		try {
			rxnProds = rxn.runReactants(rxnSubstrate);
		} catch (Exception e) {
			return retVal;
		}

		for (int i = 0; i < rxnProds.size(); i++) {
			ROMol_Vect prod = rxnProds.get(i);
			String[] smi = RDKitUtils.molVectToSMILESArray(prod);
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
			prod.delete();
			exec.checkCanceled();
		}
		rxnProds.delete();
		rxn.delete();

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
	 * @param showReverseTransforms
	 *            If <code>true<code>, then the output includes transformations
	 *            in both directions
	 * @return An {@link ArrayList} of {@link DataCell}s for each new transform
	 *         to be added derived from the {@link TreeSet} of
	 *         {@link FragmentValue}s
	 * @see #getTransforms(TreeSet, FragmentKey, int, boolean, boolean, boolean,
	 *      boolean)
	 */
	public static ArrayList<DataCell[]> getTransforms(TreeSet<FragmentValue> fragmentValues,
			int numNewCols, boolean removeExplicitHs, boolean includeNumChangingHAs,
			boolean showReverseTransforms) {

		ArrayList<DataCell[]> retVal = new ArrayList<>();
		TreeSet<FragmentValue> orderedFrags = new TreeSet<>(fragmentValues);
		for (FragmentValue leftFrag : orderedFrags) {
			for (FragmentValue rightFrag : orderedFrags.tailSet(leftFrag, false)) {
				if (!leftFrag.getID().equals(rightFrag.getID())) {
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
	 *            The {@link FragmentValue} for the 'Left' molecule
	 * @param rightFrag
	 *            The {@link FragmentValue} for the 'Right' molecule
	 * @return The {@link DataCell}s for the new row representing the tranform
	 */
	private static DataCell[] buildSimpleTransform(int numNewCols, boolean removeExplicitHs,
			boolean includeNumChangingHAs, FragmentValue leftFrag, FragmentValue rightFrag) {
		DataCell[] retVal = new DataCell[numNewCols];
		int i = 0;
		retVal[i++] = new SmilesCell(leftFrag.getSMILES(removeExplicitHs) + ">>"
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
	 * @param showReverseTransforms
	 *            If <code>true<code>, then the output includes transformations
	 *            in both directions
	 * @return An {@link ArrayList} of {@link DataCell}s for each new transform
	 *         to be added derived from the {@link TreeSet} of
	 *         {@link FragmentValue}s
	 * @see #getTransforms(TreeSet, int, boolean, boolean)
	 */
	public static ArrayList<DataCell[]> getTransforms(TreeSet<FragmentValue> fragmentValues,
			FragmentKey fragmentKey, int numNewCols, boolean removeExplicitHs,
			boolean includeKeySMILES, boolean includeNumChangingHAs, boolean includeRatioHAs,
			boolean showReverseTransforms) {

		ArrayList<DataCell[]> retVal = new ArrayList<>();
		TreeSet<FragmentValue> orderedFrags = new TreeSet<>(fragmentValues);
		for (FragmentValue leftFrag : orderedFrags) {
			for (FragmentValue rightFrag : orderedFrags.tailSet(leftFrag, false)) {
				if (!leftFrag.getID().equals(rightFrag.getID())) {
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
	 * {@link FragmentValue}s. Called by
	 * {@link #getTransforms(TreeSet, FragmentKey, int, boolean, boolean, boolean, boolean, boolean)}
	 * . Called twice if reverse transforms are also to be shown, with the Left
	 * and Right {@link FragmentValue}s transposed
	 * 
	 * @param fragmentKey
	 *            The {@link FragmentKey} for the transformation
	 * @param removeExplicitHs
	 *            If true, then explicit hydrogens are removed from the outputs
	 * @param includeKeySMILES
	 *            If true, then include the smiles of the Key in the output
	 * @param includeNumChangingHAs
	 *            If true, then the number of changing heavy atoms are added for
	 *            each fragment * @param includeRatioHAs
	 * @param leftFrag
	 *            The {@link FragmentValue} for the 'Left' molecule
	 * @param rightFrag
	 *            The {@link FragmentValue} for the 'Right' molecule
	 * @param numNewCols
	 *            The number of new columns - supplied so only calculated once
	 *            during the {@link NodeModel} <code>#configure</code> method.
	 *            * @return The {@link DataCell}s for the new row representing
	 *            the tranform
	 */
	private static DataCell[] buildTransform(FragmentKey fragmentKey, boolean removeExplicitHs,
			boolean includeKeySMILES, boolean includeNumChangingHAs, boolean includeRatioHAs,
			FragmentValue leftFrag, FragmentValue rightFrag, int numNewCols) {
		DataCell[] retVal = new DataCell[numNewCols];

		int i = 0;
		retVal[i++] = new SmilesCell(leftFrag.getSMILES(removeExplicitHs) + ">>"
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
			HashMap<FragmentKey, TreeSet<FragmentValue>> fragments, Integer maxNumVarAtoms,
			Double minCnstToVarAtmRatio) {

		if (maxNumVarAtoms == null && minCnstToVarAtmRatio == null) {
			// No filtering applied
			return fragments;
		}

		HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = new HashMap<>();
		for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : fragments.entrySet()) {
			TreeSet<FragmentValue> keptVals = new TreeSet<>();
			for (FragmentValue val : ent.getValue()) {
				boolean keepVal = true;
				if (maxNumVarAtoms != null && val.getNumberChangingAtoms() > maxNumVarAtoms) {
					keepVal = false;
				}
				if (minCnstToVarAtmRatio != null && keepVal) {
					// Only check if the val is still 'in'
					keepVal =
							ent.getKey().getConstantToVaryingAtomRatio(val) >= minCnstToVarAtmRatio;
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
		HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<>();
		FragmentKey key = new FragmentKey();
		FragmentValue val = new FragmentValue(roMol.MolToSmiles(true), ID);
		TreeSet<FragmentValue> valSet = new TreeSet<>();
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
			HashMap<FragmentKey, TreeSet<FragmentValue>> fragments, int numFragmentations,
			ChemicalReaction rxn) {
		if (numFragmentations == 0) {
			return fragments;
		} else {
			HashMap<FragmentKey, TreeSet<FragmentValue>> tmp = new HashMap<>();
			// Fragment
			for (Entry<FragmentKey, TreeSet<FragmentValue>> ent : fragments.entrySet()) {
				// We only fragment the values
				for (FragmentValue fragVal : ent.getValue()) {
					ROMol roMol = fragVal.getROMol();

					HashMap<FragmentKey, TreeSet<FragmentValue>> frags =
							runRDKitFragmentFromRxn(roMol, fragVal.getID(), rxn, numFragmentations);

					// Now we need to add the result to the retVal
					for (Entry<FragmentKey, TreeSet<FragmentValue>> ent1 : frags.entrySet()) {
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
			HashMap<FragmentKey, TreeSet<FragmentValue>> retVal =
					doRDKitFragmentationFromRxn(tmp, numFragmentations - 1, rxn);
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
	public static HashMap<FragmentKey, TreeSet<FragmentValue>> runRDKitFragmentFromRxn(ROMol roMol,
			String id, ChemicalReaction rxn, Integer cutIndex) {

		ROMol_Vect rxnSubstrate = new ROMol_Vect(1);
		ROMol_Vect_Vect rxnProds = null;
		rxnSubstrate.set(0, roMol);
		HashMap<FragmentKey, TreeSet<FragmentValue>> retVal = new HashMap<>();

		try {
			// TODO: runReactants Loses mapping indices present on starting
			// material
			rxnProds = rxn.runReactants(rxnSubstrate);
		} catch (Exception e) {
			return retVal;
		}

		for (int i = 0; i < rxnProds.size(); i++) {
			ROMol_Vect prod = rxnProds.get(i);
			String[] smi = RDKitUtils.molVectToSMILESArray(prod);
			if (smi[0].split("\\[\\*").length == 2) {
				// smi0 can be a key
				FragmentKey key0 = new FragmentKey(smi[0]);
				if (!retVal.containsKey(key0)) {
					retVal.put(key0, new TreeSet<FragmentValue>());
				}

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