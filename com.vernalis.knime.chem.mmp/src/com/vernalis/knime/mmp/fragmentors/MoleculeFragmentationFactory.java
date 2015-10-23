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

import java.util.Set;

import org.RDKit.RWMol;

import com.vernalis.knime.mmp.BondIdentifier;
import com.vernalis.knime.mmp.MulticomponentSmilesFragmentParser;

/**
 * Interface defining the API for a 3rd Generation molecule fragmentation
 * factory. Implementations should handle caching of e.g. stereochemistry
 * information from the input molecule
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public interface MoleculeFragmentationFactory {

	/**
	 * This is the method to fragment a molecule by breaking a single bond. This
	 * method is called by {@link #fragmentMolecule(Set)} if the set only
	 * contains 1 bond
	 * 
	 * @param bond
	 *            The bond to be broken
	 * @param treatProchiralAsChiral
	 *            If a molecule has not chiral centres (either defined or
	 *            undefined), should chirality be generated at prochiral
	 *            centres?
	 * @return The fragmentation result
	 * @throws MoleculeFragmentationException
	 *             If unable to correctly fragment
	 * @throws IllegalArgumentException
	 *             If the bond is {@code null}
	 */
	public abstract MulticomponentSmilesFragmentParser fragmentMolecule(
			BondIdentifier bond, boolean treatProchiralAsChiral)
			throws MoleculeFragmentationException, IllegalArgumentException;

	/**
	 * The main method for fragmentations. This method delegates single bond
	 * breaks to {@link #fragmentMolecule(BondIdentifier)}, and then for 2 or
	 * more breaks determines whether any atom is shared by two breaking bonds.
	 * In this latter case, {@link #fragmentLong(Set)} is called, as separate
	 * {@link RWMol} copies are required for each component. Otherwise,
	 * {@link #fragmentShort(Set)} is used.
	 * 
	 * @param bonds
	 *            The bond or bonds to be broken
	 * @param treatProchiralAsChiral
	 *            If a molecule has not chiral centres (either defined or
	 *            undefined), should chirality be generated at prochiral
	 *            centres?
	 * @return The fragmentation result
	 * @throws MoleculeFragmentationException
	 *             If unable to correctly fragment
	 * @throws IllegalArgumentException
	 *             if no bonds are supplied
	 */
	public abstract MulticomponentSmilesFragmentParser fragmentMolecule(
			Set<BondIdentifier> bonds, boolean treatProchiralAsChiral)
			throws IllegalArgumentException, MoleculeFragmentationException;

	/**
	 * This is the method to fragment a molecule by breaking a single bond
	 * 'twice'. The result is the outcome of the transformation:
	 * 
	 * <pre>
	 * A-B >> A-[*:1] . B-[*:2] . [*:1]-[*:2]
	 * </pre>
	 * 
	 * @param bond
	 *            The bond to be broken
	 * @param treatProchiralAsChiral
	 *            If a molecule has not chiral centres (either defined or
	 *            undefined), should chirality be generated at prochiral
	 *            centres?
	 * @return The fragmentation result
	 * @throws MoleculeFragmentationException
	 *             If unable to correctly fragment
	 * @throws IllegalArgumentException
	 *             If the bond is {@code null}
	 */
	public abstract MulticomponentSmilesFragmentParser fragmentMoleculeWithBondInsertion(
			BondIdentifier bond, boolean treatProchiralAsChiral)
			throws IllegalArgumentException, MoleculeFragmentationException;

	/**
	 * Set the verboseLogging parameter which should influence logging output
	 * 
	 * @param verboseLogging
	 *            {@code true} for verbose logging
	 */
	public abstract void setVerboseLogging(boolean verboseLogging);

}