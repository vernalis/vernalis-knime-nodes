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
/**
 * 
 */
package com.vernalis.knime.mmp.transform;

import java.util.List;
import java.util.Set;

import org.knime.chem.types.SmartsValue;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.vector.bitvector.DenseBitVector;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

/**
 * Interface defining the {@link TransformUtilityFactory} API, which extends the
 * {@link FragmentationUtilsFactory} for toolkits which also can apply chemical
 * transforms to molecules. Some methods contain default 'sensible'
 * implementations
 * 
 * NB This API is possibly unstable in the current version and future versions
 * may change
 * 
 * @author S.Roughley
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the query molecule object
 * @param <V>
 *            The type of the reaction/transform object
 *
 */
public interface TransformUtilityFactory<T, U, V> extends FragmentationUtilsFactory<T, U> {

	/**
	 * @param cell
	 *            Incoming DataCell (which may be an adaptor cell. The default
	 *            implementation assumes only SMARTS cells will be supplied
	 * @return The rSMARTS string from the cell
	 */
	default String getReactionSMARTSFromCell(DataCell cell) {
		DataType cellType = cell.getType();
		if (cellType.isAdaptable(SmartsValue.class)) {
			return ((AdapterValue) cell).getAdapter(SmartsValue.class).getSmartsValue();
		}
		if (cellType.isCompatible(SmartsValue.class)) {
			return ((SmartsValue) cell).getSmartsValue();
		}
		return null;
	}

	/**
	 * Method for the toolkit to generate a reaction object from the rSMARTS
	 * string
	 * 
	 * @param rSMARTS
	 *            The rSMARTS string
	 * @param allowAdditionalSubstitutionPositions
	 *            Should additional substitution positions be allowed
	 * @param rowIndex
	 *            The row index, used for GC of native objects
	 * @return The reaction object
	 * @throws ToolkitException
	 *             If the toolkit could not generate a reaction object from the
	 *             rSMARTS
	 */
	V generateReactionFromRSmarts(String rSMARTS, boolean allowAdditionalSubstitutionPositions,
			long rowIndex) throws ToolkitException;

	/**
	 * Overloaded method for the toolkit to generate a reaction object from the
	 * rSMARTS string, with a default that additional substitution should not be
	 * allowed
	 * 
	 * @param rSMARTS
	 *            The rSMARTS string
	 * @param rowIndex
	 *            The row index, used for GC of native objects
	 * @return The reaction object
	 * @throws ToolkitException
	 *             If the toolkit could not generate a reaction object from the
	 *             rSMARTS
	 * @see #generateReactionFromRSmarts(String, boolean, long)
	 */
	default V generateReactionFromRSmarts(String rSMARTS, long rowIndex) throws ToolkitException {
		return generateReactionFromRSmarts(rSMARTS, false, rowIndex);
	}

	/**
	 * Method to obtain a SMARTS matching string from a query object
	 * 
	 * @param queryMol
	 *            The query molecule object
	 * @return The SMARTS string
	 * @throws ToolkitException
	 *             If the toolkit could not generate a SMARTS from the query
	 *             molecule
	 */
	String getSMARTSFromMolecule(U queryMol) throws ToolkitException;

	/**
	 * Method to add H's to all positions of a query molecule such that only
	 * exact matching results
	 * 
	 * @param queryMol
	 *            The query Molecule
	 * @param rowIndex
	 *            The row index, used for GC of native objects
	 * @return The a new, modified query molecule
	 * @throws ToolkitException
	 *             If the toolkit could not effect the change
	 */
	U addHsToQueryMolecule(U queryMol, long rowIndex) throws ToolkitException;

	U generateQueryMoleculeFromSMARTS(String SMARTS, long rowIndex);

	/**
	 * @return The acceptable incoming reaction cell types. The default
	 *         implementation assumes only SMARTS as this is the output from the
	 *         pair generation nodes
	 */
	@SuppressWarnings("unchecked")
	default Class<? extends DataValue>[] getReactionCellTypes() {
		return new Class[] { SmartsValue.class };
	}

	/**
	 * Method to check whether the molecule object matches the supplied query
	 * object
	 * 
	 * @param mol
	 *            The test molecule
	 * @param matcher
	 *            The matcher object
	 * @return <code>true</code> if the molecule matches
	 */
	boolean molMatchesQuery(T mol, U matcher);

	/**
	 * Method to get a List of match atom IDs. Each list member contains a set
	 * of atom IDs for that Substructure match
	 * 
	 * @param mol
	 *            The test molecule
	 * @param matcher
	 *            The matcher object
	 * @return A list of matching set of atom indices
	 * @throws ToolkitException
	 *             If the toolkit was unable to perform the test
	 */
	List<Set<Integer>> getMatchAtomSets(T mol, U matcher) throws ToolkitException;

	/**
	 * Method to get the environment FPs of a molecule. The molecule needs to be
	 * 'exploded' to a set of Leaf objects using the supplied
	 * {@code toLeafTransform} parameter, and then the individual leaf AP
	 * fingerprints generated
	 * 
	 * @param mol
	 *            The molecule
	 * @param toLeafTransform
	 *            The transform to 'explode' the molecule to it's leaf parts
	 *            from the transfrom
	 * @param index
	 *            The index to use for GC of native objects
	 * @param fpLength
	 *            The fingerprint length to generate
	 * @param radius
	 *            The fingerprint radius
	 * @param useBondTypes
	 *            Should bond types be used in the fingerprint?
	 * @param useChirality
	 *            Should atom chirality be used in the fingerprint?
	 * @param concatenate
	 *            Should the individual AP fingerprints be concatenated into a
	 *            single FP?
	 * @return A List of fingerprint arrays. Each list member represents one
	 *         leaf set (in case multiple transform matches are possible), and
	 *         each array contains the fingerprints for each attachment point
	 *         for the given match
	 * @throws ToolkitException
	 *             If the toolkit was unable to apply the transform or generate
	 *             the fingerprints
	 */
	List<DenseBitVector[]> getEnvironmentFPs(T mol, V toLeafTransform, long index, int fpLength,
			int radius, boolean useBondTypes, boolean useChirality, boolean concatenate)
			throws ToolkitException;

	/**
	 * Method to generate the AP fingerprint for a given 'Leaf' molecule object
	 * 
	 * <p>
	 * TODO NB This method may be removed in subsequent versions if the
	 * leaf/value/key generation methods are moved to the supertype from the
	 * fragmentation containers
	 * </p>
	 * 
	 * @param mol
	 *            The leaf molecule object
	 * @param fpLength
	 *            The fingerprint length to generate
	 * @param radius
	 *            The fingerprint radius
	 * @param useBondTypes
	 *            Should bond types be used in the fingerprint?
	 * @param useChirality
	 *            Should atom chirality be used in the fingerprint?
	 * @return The AP fingerprint for the leaf
	 * @throws ToolkitException
	 *             If the toolkit was unable to generate the fingerprint
	 * 
	 * 
	 */
	DenseBitVector getLeafFingerprint(T mol, int fpLength, int radius, boolean useBondTypes,
			boolean useChirality) throws ToolkitException;

	/**
	 * Method to extract the AP index from a leaf SMILES string
	 * 
	 * @param leafSmiles
	 *            the leaf SMILES string (assumes that an AP is in the simple
	 *            form eg [3*])
	 * @return The index
	 */
	default int getAttachmentPointIndex(String leafSmiles) {
		return Integer.parseInt(leafSmiles.replaceAll(".*\\[(\\d+)\\*\\].*", "$1"));
	}

	/**
	 * Method to apply a transform to a molecule and return a set of unique
	 * product SMILES strings
	 * 
	 * @param mol
	 *            The molecule to apply the transform too
	 * @param transform
	 *            The transform to apply
	 * @param okMatchAtoms
	 *            The set of atoms which are OK to match (NB may be replaced by
	 *            a BitSet in future implementations) because they pass any AP
	 *            FP similarity criteria
	 * @param cleanChirality
	 *            Should chirality cleaning be attempted?
	 * @param index
	 *            The index to use for native object GC
	 * @return A Set of SMILES String(s) for the resulting molecules
	 * @throws ToolkitException
	 *             If the toolkit was unable to apply the transform or conver
	 *             the products to SMILES
	 */
	Set<String> getTransformedMoleculesSmiles(T mol, V transform, Set<Integer> okMatchAtoms,
			boolean cleanChirality, long index) throws ToolkitException;
}
