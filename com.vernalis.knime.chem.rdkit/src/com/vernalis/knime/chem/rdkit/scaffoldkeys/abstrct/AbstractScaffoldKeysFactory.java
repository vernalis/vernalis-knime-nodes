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
package com.vernalis.knime.chem.rdkit.scaffoldkeys.abstrct;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectScatterMap;
import com.carrotsearch.hppc.ObjectIntMap;
import com.carrotsearch.hppc.ObjectIntScatterMap;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKey;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKeysFactory;

/**
 * An abstract base implementation of {@link ScaffoldKeysFactory}. Implementing
 * subclasses need only implement the missing methods and add the
 * {@link ScaffoldKey} definitions during construction via repeated calls to
 * {@link #addScaffoldKey(ScaffoldKey)}
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The object type for the molecule
 *
 *
 * @since v1.34.0
 */
public abstract class AbstractScaffoldKeysFactory<T>
		implements ScaffoldKeysFactory<T> {

	private final IntObjectMap<ScaffoldKey<T>> scaffoldKeysLookup =
			new IntObjectScatterMap<>();
	private final ObjectIntMap<String> nameIndexLookup =
			new ObjectIntScatterMap<>();

	@Override
	public int getNumberOfKeys() {
		return scaffoldKeysLookup.size();
	}

	@Override
	public int getKey(int index, Scaffold<T> scaffold)
			throws IndexOutOfBoundsException {
		validateKey(index);
		return scaffoldKeysLookup.get(index).calculateForScaffold(scaffold);
	}

	@Override
	public int getKey(String name, Scaffold<T> scaffold) {
		if (!nameIndexLookup.containsKey(name)) {
			throw new IllegalStateException(
					"No key definition for the name '" + name + "' registered");
		}
		return getKey(nameIndexLookup.get(name), scaffold);
	}

	@Override
	public String getKeyName(int index) {
		validateKey(index);
		return scaffoldKeysLookup.get(index).getName();
	}

	@Override
	public String getKeyDescription(int index) {
		validateKey(index);
		return scaffoldKeysLookup.get(index).getDescription();
	}

	/**
	 * Method to be called by implementing classes to add a key definition.
	 * 
	 * @param key
	 *            The key definition to add
	 * 
	 * @throws NullPointerException
	 *             if {@code key} is {@code null}
	 * @throws IllegalArgumentException
	 *             if a key with the same index or name is already registered
	 * 
	 *
	 * @since v1.34.0
	 */
	protected void addScaffoldKey(ScaffoldKey<T> key) {
		Objects.requireNonNull(key, "'key' must not be null!");
		if (scaffoldKeysLookup.containsKey(key.getIndex())) {
			throw new IllegalArgumentException("A Scaffold key with index '"
					+ key.getIndex() + "' already exists!");
		}
		if (nameIndexLookup.containsKey(key.getName())) {
			throw new IllegalArgumentException("A Scaffold key with the name '"
					+ key.getName() + "' already exists!");
		}
		scaffoldKeysLookup.put(key.getIndex(), key);
		nameIndexLookup.put(key.getName(), key.getIndex());
	}

	@Override
	public void validateKey(int index)
			throws IndexOutOfBoundsException, IllegalStateException {
		if (index < 1 || index > getNumberOfKeys()) {
			throw new IndexOutOfBoundsException(
					"Index must be in range 1 - " + getNumberOfKeys());
		}
		if (!scaffoldKeysLookup.containsKey(index)) {
			throw new IllegalStateException(
					"No key definition found for index " + index);
		}
	}

	@Override
	public List<ScaffoldKey<T>> getScaffoldKeys() {
		List<ScaffoldKey<T>> retVal = new ArrayList<>();
		// Wwe use this method rather than the iterator to ensure the numerical
		// index order
		for (int i = 0; i < getNumberOfKeys(); i++) {
			retVal.add(scaffoldKeysLookup.get(i + 1));
		}
		return retVal;
	}

}
