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
package com.vernalis.knime.mmp.frags.abstrct;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple convenience class to create a set wrapper for a
 * {@link BondIdentifier} which iterates through the supplied argument and its
 * reverse - analogous to the singleton static methods in the collections class.
 * Used to denote in depiction methods that a fragmentation pattern is a double
 * cut to 1 bond with insertion
 * 
 * @author s.roughley
 * 
 *
 */
public class BondIdentifierSelfpairSet extends AbstractSet<BondIdentifier> {
	private final BondIdentifier member;

	/**
	 * @param member
	 *            The {@link BondIdentifier} to be the basis of the set
	 */
	public BondIdentifierSelfpairSet(BondIdentifier member) {
		super();
		this.member = member;
	}

	/**
	 * Static factory method
	 * 
	 * @param member
	 *            The member to be the basis of the set
	 * @return The set
	 */
	public static BondIdentifierSelfpairSet create(BondIdentifier member) {
		return new BondIdentifierSelfpairSet(member);
	}

	@Override
	public boolean add(BondIdentifier e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends BondIdentifier> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean contains(Object o) {
		if (o instanceof BondIdentifier) {
			return ((BondIdentifier) o).equals(member);
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c instanceof BondIdentifierSelfpairSet) {
			return ((BondIdentifierSelfpairSet) c).member.equals(member);
		}
		if (c.size() == 2) {
			for (Object elem : c) {
				if (!elem.equals(member)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Iterator<BondIdentifier> iterator() {
		return new ReversibleBondIdentifierIterator();
	}

	private class ReversibleBondIdentifierIterator implements Iterator<BondIdentifier> {
		int counter = 0;

		@Override
		public boolean hasNext() {
			return counter < 2;
		}

		@Override
		public BondIdentifier next() {
			switch (counter++) {
			case 0:
				return member;
			case 1:
				return member.getReverse();

			default:
				throw new NoSuchElementException();
			}
		}

	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return 2;
	}

	@Override
	public Object[] toArray() {
		return new Object[] { member, member.getReverse() };
	}

}
