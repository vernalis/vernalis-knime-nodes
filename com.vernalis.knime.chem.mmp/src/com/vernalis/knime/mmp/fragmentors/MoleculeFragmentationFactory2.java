/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
import java.util.Collections;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractMulticomponentFragmentationParser;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifier;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifierSelfpairSet;

/**
 * Interface defining the API for a 3rd or 4th Generation molecule fragmentation
 * factory. Implementations should handle caching of e.g. stereochemistry
 * information from the input molecule. Most likely, implementations should
 * subclass {@link AbstractFragmentationFactory}
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 *
 * @param <T>
 *            The type of the molecule to fragment
 * @param <U>
 *            The type of the matcher object
 */
public interface MoleculeFragmentationFactory2<T, U> {

	/**
	 * This is the method to fragment a molecule by breaking a single bond.
	 * 
	 * @param bond
	 *            The bond to be broken
	 * @return The fragmentation result
	 * @throws MoleculeFragmentationException
	 *             If unable to correctly fragment
	 * @throws IllegalArgumentException
	 *             If the bond is {@code null}
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 */
	public default AbstractMulticomponentFragmentationParser<T> fragmentMolecule(
			BondIdentifier bond) throws MoleculeFragmentationException, IllegalArgumentException,
			ToolkitException, ClosedFactoryException {
		return fragmentMolecule(Collections.singleton(bond));
	}

	/**
	 * The main method for fragmentations. This method delegates single bond
	 * breaks to {@link #fragmentMolecule(BondIdentifier)}, and then for 2 or
	 * more breaks determines whether any atom is shared by two breaking bonds.
	 * 
	 * @param bonds
	 *            The bond or bonds to be broken
	 * @return The fragmentation result
	 * @throws MoleculeFragmentationException
	 *             If unable to correctly fragment
	 * @throws IllegalArgumentException
	 *             if no bonds are supplied
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 */
	public abstract AbstractMulticomponentFragmentationParser<T> fragmentMolecule(
			Set<BondIdentifier> bonds) throws IllegalArgumentException,
			MoleculeFragmentationException, ToolkitException, ClosedFactoryException;

	/**
	 * This is the method to fragment a molecule by breaking a single bond
	 * 'twice'. The result is the outcome of the transformation:
	 * 
	 * <pre>
	 * A-B &gt;&gt; A-[*:1] . B-[*:2] . [*:1]-[*:2]
	 * </pre>
	 * 
	 * @param bond
	 *            The bond to be broken
	 * @return The fragmentation result
	 * @throws MoleculeFragmentationException
	 *             If unable to correctly fragment
	 * @throws IllegalArgumentException
	 *             If the bond is {@code null}
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 */
	public abstract AbstractMulticomponentFragmentationParser<T> fragmentMoleculeWithBondInsertion(
			BondIdentifier bond) throws IllegalArgumentException, MoleculeFragmentationException,
			ToolkitException, ClosedFactoryException;

	/**
	 * Set the verboseLogging parameter which should influence logging output
	 * 
	 * @param verboseLogging
	 *            {@code true} for verbose logging
	 */
	public abstract void setVerboseLogging(boolean verboseLogging);

	/**
	 * Identify all bonds in the molecule which match the supplied substructure
	 * 
	 * @return A set of {@link BondIdentifier}s of the bonds which match the
	 *         query
	 */
	public abstract Set<BondIdentifier> identifyAllMatchingBonds() throws ClosedFactoryException;

	/**
	 * Check whether the supplied set of {@link BondIdentifier}s (which must be
	 * of length 3) is a valid triplet
	 * 
	 * @param triplet
	 *            Set of three bonds to cut. NB an exception will be thrown if
	 *            this is not the case.
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 */
	public abstract boolean isValidCutTriplet(Set<BondIdentifier> triplet)
			throws ToolkitException, ClosedFactoryException;

	/**
	 * Method to break the supplied molecule along all matching bonds. Results
	 * are filtered by any specified heavy atom filters
	 * 
	 * @param exec
	 *            The {@link ExecutionContext} to check for cancelling
	 * @param breakingBondColour
	 *            The color to highlight breaking bonds
	 * @param keyColour
	 *            The color to highlight 'key' atoms and bonds
	 * @param valueColour
	 *            The color to highlight 'value' atoms and bonds
	 * @return The fragmentations as a set
	 * @throws CanceledExecutionException
	 *             if the user cancels during execution
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws IllegalArgumentException
	 *             If one of the bonds is {@code null}
	 */
	Set<AbstractMulticomponentFragmentationParser<T>> breakMoleculeAlongMatchingBonds(
			ExecutionContext exec, Color breakingBondColour, Color keyColour, Color valueColour)
			throws CanceledExecutionException, IllegalArgumentException, ToolkitException,
			ClosedFactoryException;

	/**
	 * Method to break the supplied molecule along all matching bonds, inserting
	 * a '*-*' bond into the broken bond as the value. Results are filtered by
	 * any specified heavy atom filters
	 * 
	 * @param exec
	 *            The {@link ExecutionContext} to check for cancelling
	 * @param bondColour
	 *            The color to highlight breaking bonds
	 * @param keyColour
	 *            The color to highlight 'key' atoms and bonds
	 * @param valueColour
	 *            The color to highlight 'value' atoms and bonds
	 * @return The fragmentations as a set
	 * @throws CanceledExecutionException
	 *             if the user cancels during execution
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws IllegalArgumentException
	 *             If one of the bonds is {@code null}
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	Set<AbstractMulticomponentFragmentationParser<T>> breakMoleculeAlongMatchingBondsWithBondInsertion(
			ExecutionContext exec, Color bondColour, Color keyColour, Color valueColour)
			throws CanceledExecutionException, IllegalArgumentException, ToolkitException,
			ClosedFactoryException;

	/**
	 * Method to find combinations of cuttable bonds. For combinations of 3 or
	 * more cuts, any combinations including an invalid triplet of binds are
	 * removed.
	 * 
	 * @param minNumCuts
	 *            The minimum number of cuts (must be &ge; 2 - 1 cut should be
	 *            handled separately)
	 * @param maxNumCuts
	 *            The maximum number of cuts (must be &ge; minNumCuts)
	 * @return A Set of Sets of bonds
	 * @throws IllegalArgumentException
	 *             If the arguments do not fulfil the above criteria
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	Set<Set<BondIdentifier>> generateCuttableBondCombos(int minNumCuts, int maxNumCuts)
			throws IllegalArgumentException, ToolkitException, ClosedFactoryException;

	/**
	 * Method to find combinations of cuttable bonds. For combinations of 3 or
	 * more cuts, any combinations including an invalid triplet of binds are
	 * removed.
	 * 
	 * @param numCuts
	 *            The number of cuts (must be &ge; 2 - 1 cut should be handled
	 *            separately)
	 * @return A Set of Sets of bonds
	 * @throws IllegalArgumentException
	 *             If the argument does not fulfil the above criterion
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	Set<Set<BondIdentifier>> generateCuttableBondCombos(int numCuts)
			throws IllegalArgumentException, ToolkitException, ClosedFactoryException;

	/**
	 * Method to identify all the bonds which may be part of a valid cut pattern
	 * for the given number of cuts
	 * 
	 * @param numCuts
	 *            The number of cuts
	 * @return A set containing the bonds
	 * @throws IllegalArgumentException
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	Set<BondIdentifier> identifyAllCuttableBonds(int numCuts)
			throws ClosedFactoryException, IllegalArgumentException, ToolkitException;

	/**
	 * Method to break the supplied molecule along the indicated bond
	 * combinations supplied in {@code bondCombos}. For each combination
	 * supplied, if the fragmentation is valid and passes the filters, then it
	 * is added to the fragment output table.
	 * 
	 * @param bondCombos
	 *            A {@link Set} of {@link Set}s of {@link BondIdentifier}. Each
	 *            inner set is a combination of matching bonds to cut.
	 * @param prochiralAsChiral
	 *            Should prochiral centres be assigned chirality if there are no
	 *            known or unknown centres?
	 * @param exec
	 *            The {@link ExecutionContext} to check for cancelling
	 * @param bondColour
	 *            The color to highlight breaking bonds
	 * @param keyColour
	 *            The color to highlight 'key' atoms and bonds
	 * @param valueColour
	 *            The color to highlight 'value' atoms and bonds
	 * @param logger
	 *            {@link NodeLogger} instance for reporting
	 * @param verboseLogging
	 *            Whether verbose logging is enabled
	 * @return The fragmentations as a set
	 * @throws CanceledExecutionException
	 *             if the user cancels during execution
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	Set<AbstractMulticomponentFragmentationParser<T>> breakMoleculeAlongBondCombos(
			Set<Set<BondIdentifier>> bondCombos, boolean prochiralAsChiral, ExecutionContext exec,
			Color bondColour, Color keyColour, Color valueColour, NodeLogger logger,
			boolean verboseLogging) throws CanceledExecutionException, ClosedFactoryException;

	/**
	 * @param Should
	 *            a single bond be allowed to be cut twice if 2 cuts are being
	 *            made?
	 * @return The maximum number of cuts that can be made
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	int getMaximumNumberOfCuts(boolean allowTwoCutsToSingleBond)
			throws ClosedFactoryException, ToolkitException;

	/**
	 * @return The cell type for the rendered cells created by the factory or
	 *         <code>null</code> if the factory does not support rendering
	 */
	default DataType getRendererCellType() {
		return null;
	};

	/**
	 * Method to render the matching bonds
	 * 
	 * @param bondColour
	 *            The colour to highlight the matching bonds
	 * @return A rendered cell of the appropriate type (see
	 *         {@link #getRendererCellType()})
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 * @throws IOException
	 *             If there was an error with certain cell types
	 */
	default DataCell renderMatchingBonds(Color bondColour)
			throws ClosedFactoryException, ToolkitException, IOException {
		return DataType.getMissingCell();
	}

	/**
	 * Method to render the cuttable matching bonds for the given number of cuts
	 * 
	 * @param bondColour
	 *            The colour to highlight the matching bonds
	 * @return A rendered cell of the appropriate type (see
	 *         {@link #getRendererCellType()})
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 * @throws IOException
	 *             If there was an error with certain cell types
	 */
	default DataCell renderCuttableBonds(Color bondColour, int numCuts)
			throws ToolkitException, ClosedFactoryException, IOException {
		return DataType.getMissingCell();
	}

	/**
	 * Method to render a fragmentaiton pattern
	 * 
	 * @param bonds
	 *            The bond(s) to cut. If a bond is to be cut twice to give a
	 *            'bond' as a 'value', then this should be a
	 *            {@link BondIdentifierSelfpairSet}
	 * @param breakingBondColour
	 *            The color to highlight breaking bonds
	 * @param keyColour
	 *            The color to highlight 'key' atoms and bonds
	 * @param valueColour
	 *            The color to highlight 'value' atoms and bonds
	 * @return A rendered cell of the appropriate type (see
	 *         {@link #getRendererCellType()})
	 * @throws IOException
	 *             If there was an error with certain cell types
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 * @throws MoleculeFragmentationException
	 *             if an invalid pattern was supplied
	 */
	default DataCell renderFragmentation(Set<BondIdentifier> bonds, Color breakingBondColour,
			Color keyColour, Color valueColour) throws IOException, IllegalArgumentException,
			ToolkitException, MoleculeFragmentationException {
		return DataType.getMissingCell();
	}

	/**
	 * Method to check if the molecule can be cut n-times
	 * 
	 * @param n
	 *            The number of cuts
	 * @param allowTwoCutsToSingleBond
	 *            Should one bond be allowed to be cut twice to give a bond as
	 *            'value'
	 * @return <code>true</code> if the molecule can be cut n-times
	 * @throws ToolkitException
	 *             Thrown if the underlying toolkit throws an exception
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	default boolean canCutNTimes(int n, boolean allowTwoCutsToSingleBond)
			throws ClosedFactoryException, ToolkitException {
		return getMaximumNumberOfCuts(allowTwoCutsToSingleBond) >= n;
	}

	/**
	 * This method should be implemented to ensure any objects are disposed of
	 * as soon as the factory is no longer required.
	 */
	void close();

	/**
	 * @return A set containing the matching bonds
	 * @throws ClosedFactoryException
	 *             If the {@link #close()} methods has previously been called
	 */
	public abstract Set<BondIdentifier> getMatchingBonds() throws ClosedFactoryException;

}