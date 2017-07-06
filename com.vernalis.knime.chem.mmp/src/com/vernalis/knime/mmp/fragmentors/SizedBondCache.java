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

import java.util.LinkedHashMap;

import com.vernalis.knime.mmp.frags.abstrct.BondIdentifier;

/**
 * A size-limited caching for leafs. The map is keyed on the
 * {@link BondIdentifier} which gives rise to the leaf
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type of the Leaf
 */
public class SizedBondCache<T> extends LinkedHashMap<BondIdentifier, T> {
	private static final long serialVersionUID = 1L;
	private final int maxEntries;

	/**
	 * Cache constructor
	 * 
	 * @param maxEntries
	 *            The maximum cache size to use
	 */
	public SizedBondCache(int maxEntries) {
		super(maxEntries, 1, true);
		this.maxEntries = maxEntries;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<BondIdentifier, T> eldest) {
		return size() > maxEntries;
	}

}
