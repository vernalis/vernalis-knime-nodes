/*******************************************************************************
 * Copyright (c) 2019, 2020, Vernalis (R&D) Ltd
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
package com.vernalis.knime.core.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A {@link SortedSet} implementation with a fixed upper limit on size. If an
 * object is added to the set once the size is reached, then either it will be
 * in the collection and the last entry removed or, if it was added beyond the
 * last entry, then the list will be unchanged. Backed by a {@link TreeSet}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @param <E>
 *            The type of objects in the list
 */
public class LimitedTreeSet<E> implements SortedSet<E> {

	private final TreeSet<E> s;
	private final int limit;

	/**
	 * Constructor with only a limit supplied, and default comparator
	 * 
	 * @param limit
	 *            The size limit
	 */
	public LimitedTreeSet(int limit) {
		if (limit < 0) {
			throw new IllegalArgumentException("Limit must be non-negative");
		}
		this.limit = limit;
		s = new TreeSet<>();

	}

	/**
	 * Constructor specifying a {@link Comparator} in addition to the size limit
	 * 
	 * @param limit
	 *            The size limit
	 * @param comparator
	 *            The comparator for the collection
	 */
	public LimitedTreeSet(int limit, Comparator<? super E> comparator) {
		if (limit < 0) {
			throw new IllegalArgumentException("Limit must be non-negative");
		}
		this.limit = limit;
		s = new TreeSet<>(comparator);
	}

	/**
	 * Constructor from a collection with the default comparator
	 * 
	 * @param limit
	 *            The size limit
	 * @param c
	 *            The collection. Elements are copied from the collection to the
	 *            backing TreeSet of this collection
	 */
	public LimitedTreeSet(int limit, Collection<? extends E> c) {
		this(limit);
		addAll(c);
	}

	/**
	 * Full constructor allowing specification of size limit, an existing
	 * collection and a {@link Comparator}
	 * 
	 * @param limit
	 *            The size limit
	 * @param c
	 *            The collection. Elements are copied from the collection to the
	 *            backing TreeSet of this collection
	 * @param comparator
	 *            The comparator for the collection
	 */
	public LimitedTreeSet(int limit, Collection<? extends E> c,
			Comparator<? super E> comparator) {
		this(limit, comparator);
		addAll(c);
	}

	/**
	 * NB Breaks {@link Collection#add(Object)} contract in that a value might
	 * not end up in the collection due to size limit, but will not throw an
	 * exception in this case
	 * 
	 * @return true if the element was added and remains in the collection
	 */
	@Override
	public boolean add(E e) {
		boolean retVal = s.add(e);
		if (retVal && s.size() > limit) {
			retVal = !s.pollLast().equals(e);
		}
		return retVal;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean retVal = s.addAll(c);

		if (retVal) {
			int removedNewCount = 0;
			while (s.size() > limit) {
				if (c.contains(s.pollLast())) {
					removedNewCount++;
				}
			}
			retVal = removedNewCount >= c.size();
		}
		return false;
	}

	@Override
	public void clear() {
		s.clear();

	}

	@Override
	public boolean contains(Object o) {
		return s.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return s.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return s.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return s.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return s.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return s.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return s.retainAll(c);
	}

	@Override
	public int size() {
		return s.size();
	}

	@Override
	public Object[] toArray() {
		return s.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return s.toArray(a);
	}

	@Override
	public Comparator<? super E> comparator() {
		return s.comparator();
	}

	@Override
	public E first() {
		return s.first();
	}

	@Override
	public SortedSet<E> headSet(E toElement) {
		return s.headSet(toElement);
	}

	@Override
	public E last() {
		return s.last();
	}

	@Override
	public SortedSet<E> subSet(E fromElement, E toElement) {
		return s.subSet(fromElement, toElement);
	}

	@Override
	public SortedSet<E> tailSet(E fromElement) {
		return s.tailSet(fromElement);
	}
}
