/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A simple FIFO queue with a limited number of members. Adding member(s) to a
 * full object will remove members until the list is within the limit
 * 
 * @author s.roughley
 * @since 1.29.0
 * @param <E>
 *            The type of member
 */
public class FixedLengthQueue<E> implements Queue<E>, Iterable<E> {

	private final int limit;
	private final Queue<E> list = new LinkedList<>();

	/**
	 * Constructor
	 * 
	 * @param limit
	 *            The upper size limit
	 * @throws IllegalArgumentException
	 *             If the size limit is 0 or negative
	 */
	public FixedLengthQueue(int limit) throws IllegalArgumentException {
		if (limit < 1) {
			throw new IllegalArgumentException(
					"A FixedLengthQueue must have at least one member");
		}
		this.limit = limit;
	}

	/**
	 * Constructor from an existing collection
	 * 
	 * @param limit
	 *            The upper size limit
	 * @param c
	 *            the collection to copy members from. If the size of c is
	 *            greater than the limit, only the last members of c in iterator
	 *            order will be kept
	 * @throws IllegalArgumentException
	 *             If the size limit is 0 or negative
	 */
	public FixedLengthQueue(int limit, Collection<? extends E> c)
			throws IllegalArgumentException {
		this(limit);
		addAll(c);
	}

	/**
	 * Constructor from another {@link FixedLengthQueue} instance, with the same
	 * limit and members. If a change in limit is required, then use
	 * {@link #FixedLengthQueue(int, Collection)}
	 * 
	 * @param flq
	 *            The other {@link FixedLengthQueue}
	 */
	public FixedLengthQueue(FixedLengthQueue<? extends E> flq) {
		this(flq.getLimit(), flq.list);
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @return the list
	 */
	public Queue<E> getList() {
		return (Queue<E>) Collections.unmodifiableCollection(list);
	}

	/**
	 * @see java.util.Queue#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e) {
		final boolean retVal = list.add(e);
		trim();
		return retVal;
	}

	/**
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		final boolean retVal = list.addAll(c);
		trim();
		return retVal;
	}

	/**
	 * NB This method will not fail if the collections size limit is met, as in
	 * this case, the head of the queue is removed
	 * 
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(E e) {
		final boolean retVal = list.offer(e);
		trim();
		return retVal;
	}

	/**
	 * 
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		list.clear();
	}

	/**
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return list.contains(o);
	}

	/**
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll(c);
	}

	/**
	 * @see java.util.Queue#element()
	 */
	@Override
	public E element() {
		return list.element();
	}

	/**
	 * @see java.util.Collection#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return list.equals(o);
	}

	/**
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	@Override
	public void forEach(Consumer<? super E> arg0) {
		list.forEach(arg0);
	}

	/**
	 * @see java.util.Collection#hashCode()
	 */
	@Override
	public int hashCode() {
		return list.hashCode();
	}

	/**
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	/**
	 * @see java.util.Collection#parallelStream()
	 */
	@Override
	public Stream<E> parallelStream() {
		return list.parallelStream();
	}

	/**
	 * @see java.util.Queue#peek()
	 */
	@Override
	public E peek() {
		return list.peek();
	}

	/**
	 * @see java.util.Queue#poll()
	 */
	@Override
	public E poll() {
		return list.poll();
	}

	/**
	 * @see java.util.Queue#remove()
	 */
	@Override
	public E remove() {
		return list.remove();
	}

	/**
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return list.remove(o);
	}

	/**
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	/**
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return list.removeIf(filter);
	}

	/**
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}

	/**
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return list.size();
	}

	/**
	 * @see java.util.Collection#spliterator()
	 */
	@Override
	public Spliterator<E> spliterator() {
		return list.spliterator();
	}

	/**
	 * @see java.util.Collection#stream()
	 */
	@Override
	public Stream<E> stream() {
		return list.stream();
	}

	/**
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	private void trim() {
		while (list.size() > limit) {
			list.remove();
		}
	}

}
