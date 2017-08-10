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
package com.vernalis.knime.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A peeking iterator which allows the next item to be previewed without calling
 * next. Elements cannot be removed once a call to {@link #peek()} has been made
 * 
 * @author s.roughley
 *
 * @param <E>
 *            The generic type parameter
 */
public class PeekingIterator<E> implements Iterator<E> {
	E peek;
	Iterator<E> iter;

	/**
	 * Constructor
	 * 
	 * @param iterator
	 *            The iterator to wrap
	 */
	public PeekingIterator(Iterator<E> iterator) {
		peek = null;
		iter = iterator;
	}

	/**
	 * Method to preview the next element of the iterator without actually
	 * moving the iterator pointer to it. repeated calls will retrieve the same
	 * element. A call to {@link #next()} will return it and move the pointer
	 * 
	 * @return The next element if there is one available
	 * @throws NoSuchElementException
	 *             If there is no next element
	 */
	public E peek() throws NoSuchElementException {
		if (peek != null) {
			return peek;
		} else {
			peek = iter.next();
			return peek;
		}
	}

	@Override
	public E next() {
		if (peek != null) {
			E next = peek;
			peek = null;
			return next;
		} else {
			return iter.next();
		}
	}

	@Override
	public boolean hasNext() {
		if (peek != null) {
			return true;
		}
		return iter.hasNext();
	}

	@Override
	public void remove() throws IllegalStateException {
		if (peek == null) {
			iter.remove();
		} else {
			throw new IllegalStateException("Iterator has been peeked - unable to remove element");
		}
	}
}
