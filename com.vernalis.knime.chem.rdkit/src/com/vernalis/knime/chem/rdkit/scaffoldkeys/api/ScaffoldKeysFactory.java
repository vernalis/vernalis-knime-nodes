/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.rdkit.scaffoldkeys.api;

import java.util.List;
import java.util.stream.IntStream;

/**
 * 
 * An interface describing a factory for Ertl Scaffold Keys. Implementations
 * should be designed for reuse to avoid unnecessary query molecule object
 * creation
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The type of Molecule object which the implementation uses
 *
 *
 * @since v1.34.0
 */
public interface ScaffoldKeysFactory<T> {

	/**
	 * @return the number of keys in the implementation
	 *
	 * @since v1.34.0
	 */
	public int getNumberOfKeys();

	/**
	 * @param mol
	 *            The input molecule
	 * @param isMurckoScaffold
	 *            if the molecule should be treated as a Bemis-Murcko scaffold
	 *            ({@code true}~), or needs to be converted to a scaffold
	 *            ({@code false})
	 * 
	 * @return The scaffold, which should have H's removed and be pruned to a
	 *         Bemis-Murcko Scaffold
	 *
	 * @since v1.34.0
	 */
	Scaffold<T> getScaffoldFromMolecule(T mol, boolean isMurckoScaffold);

	/**
	 * @param index
	 *            the key index
	 * @param scaffold
	 *            The scaffold
	 * 
	 * @return the key value
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the range 1 < index <=
	 *             {@link #getNumberOfKeys()}
	 * @throws IllegalStateException
	 *             if there is no key registered for the index
	 *
	 * @since v1.34.0
	 */
	public int getKey(int index, Scaffold<T> scaffold)
			throws IndexOutOfBoundsException, IllegalStateException;

	/**
	 * @param name
	 *            the key name
	 * @param scaffold
	 *            The scaffold
	 * 
	 * @return the key value
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the name corresponds to an index out of the range 1 <
	 *             index <= {@link #getNumberOfKeys()}
	 * @throws IllegalStateException
	 *             if there is no key registered for the name
	 *
	 * @since v1.34.0
	 */
	public int getKey(String name, Scaffold<T> scaffold)
			throws IndexOutOfBoundsException, IllegalStateException;

	/**
	 * @param index
	 *            the key index
	 * 
	 * @return a brief description of the key
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the range 1 < index <=
	 *             {@link #getNumberOfKeys()}
	 * @throws IllegalStateException
	 *             if there is no key registered for the index
	 * 
	 * @since v1.34.0
	 */
	public String getKeyDescription(int index)
			throws IndexOutOfBoundsException, IllegalStateException;

	/**
	 * @param index
	 *            the key index
	 * 
	 * @return a short user-friendly name for the key
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the range 1 < index <=
	 *             {@link #getNumberOfKeys()}
	 * 
	 * @throws IllegalStateException
	 *             if there is no key registered for the index
	 *
	 * @since v1.34.0
	 */
	public String getKeyName(int index)
			throws IndexOutOfBoundsException, IllegalStateException;

	/**
	 * @param scaffold
	 *            The scaffold
	 * 
	 * @return The keys for the scaffold as an array (NB index 0 -> key 1, index
	 *         1 -> key 2 etc...)
	 *
	 * @since v1.34.0
	 */
	public default int[] getKeys(Scaffold<T> scaffold) {
		return IntStream.range(0, getNumberOfKeys())
				.map(idx -> getKey(idx + 1, scaffold)).toArray();
	}

	/**
	 * A method to check that a supplied key index is valid. Getter methods in
	 * implementations should override call this method to validate a key
	 * argument
	 * 
	 * @param index
	 *            the key index
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the range 1 < index <=
	 *             {@link #getNumberOfKeys()}
	 * @throws IllegalStateException
	 *             if there is no key registered for the index
	 *
	 * @since v1.34.0
	 */
	void validateKey(int index)
			throws IndexOutOfBoundsException, IllegalStateException;

	/**
	 * 
	 * @return {@code true} if the underlying {@link Scaffold} implementation
	 *         supports depiction and at least one of the registered keys also
	 *         supports depiction
	 *
	 * @since v1.34.0
	 */
	boolean canDepict();

	/**
	 * @return A List containing the key definitions
	 *
	 * @since v1.34.0
	 */
	List<ScaffoldKey<T>> getScaffoldKeys();
}
