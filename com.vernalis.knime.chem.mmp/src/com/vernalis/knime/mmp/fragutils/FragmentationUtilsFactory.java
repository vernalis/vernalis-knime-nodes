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
package com.vernalis.knime.mmp.fragutils;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.InvalidSettingsException;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.IncomingMoleculeException;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.ClosedFactoryException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;

/**
 * A utility factory to allow all the components required for a
 * matched-molecular pairs node using a {@link MoleculeFragmentationFactory2} to
 * be created. The factory implementation should also handle any native object
 * cleanup, overriding {@link #postExecuteCleanup()}, {@link #nodeReset()},
 * {@link #rowCleanup(long)} and {@link #nodeDispose()} as required
 * 
 * NB This API is possibly unstable in the current version and future versions
 * may change
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type parameter for the molecule object
 * @param <U>
 *            The type parameter for the matcher object
 */
public interface FragmentationUtilsFactory<T, U> {

	/**
	 * @return An array of the {@link DataValue} classes accepted as incoming
	 *         molecules
	 */
	Class<? extends DataValue>[] getInputColumnTypes();

	/**
	 * Returns a molecule from a {@link DataCell} using the appropriate method
	 * for the cell type
	 * 
	 * @param molCell
	 *            Input DataCell (should be RDKit, MOL, SDF or SMILES)
	 * @param rowIndex
	 *            The rowIndex - needed for some implementations to perform
	 *            cleanup
	 * @param removeExplicitHs
	 *            Should any explicit H's in the incoming molecule be removed?
	 * @return Molecule object containing the molecule
	 * @throws InvalidSettingsException
	 *             Thrown if the cell is not of the correct type
	 * @throws RowExecutionException
	 */
	T getMolFromCell(DataCell molCell, long rowIndex, boolean removeExplicitHs)
			throws ToolkitException;

	/**
	 * This method returns a copy of the molecule with explicit hydrogens added
	 * (There is no corresponding method for removal of expict H's as this is
	 * handled by the fragmentation factory)
	 * 
	 * @param mol
	 *            The molecule
	 * @param rowIndex
	 *            The row index - maybe needed for garbage cleanup
	 * @return A copy with explict H's added
	 * @throws IncomingMoleculeException
	 *             If there was a problem adding H's to the molecule
	 */
	T createHAddedMolecule(T mol, long rowIndex) throws ToolkitException;

	/**
	 * Abstract method to check the at the molecule argument contains 1 or more
	 * atoms
	 * 
	 * @param mol
	 *            The molecule
	 * @return {@code true} if the molecule contains no atoms
	 */
	boolean moleculeIsEmpty(T mol);

	/**
	 * Abstract method to check whether the molecule is multicomponent
	 * 
	 * @param mol
	 *            The molecule
	 * @return {@code true} If the molecule has 2 or more disconnected
	 *         components
	 */
	boolean moleculeIsMultiComponent(T mol);

	/**
	 * Method which returns the Matcher object from the specified SMARTS.
	 * 
	 * @param SMARTS
	 *            The SMARTS string - should match two atoms connected by a
	 *            single, acyclic bond
	 * @return The matcher object
	 * @throws Exception
	 */
	U getMatcher(String SMARTS) throws ToolkitException;

	/**
	 * Method to check that the supplied SMARTS is valid. For backwards
	 * compatibility, if it contains '&gt;&gt;', it should check that it
	 * contains two atoms before the arrow only, otherwise it should check that
	 * it contains two atoms connected by a single, acyclic bond. The method may
	 * need to generate a local copy of the matcher object using a call to
	 * {@link #getMatcher(String)}
	 * 
	 * @param SMARTS
	 *            The SMARTS String to validate
	 * @return <code>null</code> if the SMARTS is valid, otherwise an
	 *         informative error message describing the problem
	 */
	String validateMatcherSmarts(String SMARTS);

	/**
	 * This method returns the fragmentation factory
	 * 
	 * @param mol
	 *            The molecule to fragment
	 * @param bondMatch
	 *            The bond matcher
	 * @param stripHsAtEnd
	 *            Should H's be removed post-fragmentation?
	 * @param isHAdded
	 *            Whether H's have been added (in which case the factory should
	 *            only break bonds to H)
	 * @param verboseLog
	 *            Use verbose logging?
	 * @param prochiralAsChiral
	 *            Treat prochiral atoms as chiral
	 * @param maxNumVarAtm
	 *            The maximum number of varying heavy atoms
	 * @param minCnstToVarAtmRatio
	 *            The minimum ratio of constant to varying heavy atoms
	 * @param maxLeafCacheSize
	 *            The maximum number of Leaf objects to be cached during
	 *            fragmentation
	 * @return The fragmentation factory
	 * @throws ToolkitException
	 */
	MoleculeFragmentationFactory2<T, U> createFragmentationFactory(T mol, U bondMatch,
			boolean stripHsAtEnd, boolean isHAdded, boolean verboseLog, boolean prochiralAsChiral,
			Integer maxNumVarAtm, Double minCnstToVarAtmRatio, int maxLeafCacheSize)
			throws ClosedFactoryException, ToolkitException;

	/**
	 * Convenience method to create the H-added
	 * {@link MoleculeFragmentationFactory2} from the normal molecule
	 * 
	 * @param mol
	 *            The molecule to fragment
	 * @param bondMatch
	 *            The bond matcher
	 * @param stripHsAtEnd
	 *            Should H's be removed post-fragmentation?
	 * @param verboseLog
	 *            Use verbose logging?
	 * @param prochiralAsChiral
	 *            Treat prochiral atoms as chiral
	 * @param maxNumVarAtm
	 *            The maximum number of varying heavy atoms
	 * @param minCnstToVarAtmRatio
	 *            The minimum ratio of constant to varying heavy atoms
	 * @param rowIndex
	 *            The rowIndex for native object garbage collection
	 * @return The fragmentation factory
	 * @throws ToolkitException
	 * @throws ClosedFactoryException
	 */
	default MoleculeFragmentationFactory2<T, U> createHAddedFragmentationFactory(T mol, U bondMatch,
			boolean stripHsAtEnd, boolean verboseLog, boolean prochiralAsChiral,
			Integer maxNumVarAtm, Double minCnstToVarAtmRatio, long rowIndex)
			throws ToolkitException, ClosedFactoryException {
		// We don't cache in H-added case as there is no point in doing so!
		return createFragmentationFactory(createHAddedMolecule(mol, rowIndex), bondMatch,
				stripHsAtEnd, true, verboseLog, prochiralAsChiral, maxNumVarAtm,
				minCnstToVarAtmRatio, 0);
	}

	/**
	 * @return Whether the toolkit can handle the extended fingerprint options
	 */
	boolean hasExtendedFingerprintOptions();

	/**
	 * @return A user friendly name to suffix the node type with in the KNIME
	 *         GUI
	 */
	String getToolkitName();

	/**
	 * Method called at the end of the node execution step to clean up any
	 * stored native objects
	 */
	default void postExecuteCleanup() {

	}

	/**
	 * Method called on node reset to quarantine any stored native objects for
	 * cleanup
	 */
	default void nodeReset() {

	}

	/**
	 * Method called on row completion to cleanup any stored native objects for
	 * the row
	 * 
	 * @param index
	 *            The row index
	 */
	default void rowCleanup(long index) {

	}

	/**
	 * Method called on node disposal or finalization to cleanup any stored
	 * native objects
	 */
	default void nodeDispose() {

	}

	/**
	 * @return The rendering cell type if the factory supports it or
	 *         <code>null</code> if rendering is not supported
	 */
	DataType getRendererType();

}
