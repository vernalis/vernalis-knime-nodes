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
package com.vernalis.knime.misc;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A class allowing one of two different types to be provided as an argument for
 * a method. Only one type is ever present. An empty instance is also possible.
 * This is loosely based on the Java 1.8 'Optional<E>' class
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The 'left' type parameter
 * @param <U>
 *            The 'right' type parameter
 */
public final class EitherOr<T, U> {
	private final T left;
	private final U right;

	private EitherOr(T left, U right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Empty Instance
	 */
	private static final EitherOr<?, ?> EMPTY = new EitherOr<>(null, null);

	/**
	 * Create a 'left' object
	 * 
	 * @param value
	 *            The value to hold (non-<code>null</code>)
	 * @return A 'left' container object
	 * @throws NullPointerException
	 *             if a <code>null</code> value was supplied
	 */
	public static <T, U> EitherOr<T, U> ofLeft(T value) throws NullPointerException {
		return new EitherOr<>(Objects.requireNonNull(value), null);
	}

	/**
	 * Create a 'right' object
	 * 
	 * @param value
	 *            The value to hold (non-<code>null</code>)
	 * @return A 'right' container object
	 * @throws NullPointerException
	 *             if a <code>null</code> value was supplied
	 */
	public static <T, U> EitherOr<T, U> ofRight(U value) {
		return new EitherOr<>(null, Objects.requireNonNull(value));
	}

	/**
	 * @return An empty object (containing no values) Note - use
	 *         {@link #isEmpty()} or {@link #isPresent()} to check for
	 *         'emptiness'
	 */
	public static <T, U> EitherOr<T, U> empty() {
		@SuppressWarnings("unchecked")
		EitherOr<T, U> retVal = (EitherOr<T, U>) EMPTY;
		return retVal;
	}

	/**
	 * @return <code>true</code> if the object is a 'left' container
	 */
	public boolean isLeft() {
		return left != null;
	}

	/**
	 * @return <code>true</code> if the object is a 'right' container
	 */
	public boolean isRight() {
		return right != null;
	}

	/**
	 * @return <code>true</code> if the object contains neither left or right
	 *         values
	 */
	public boolean isEmpty() {
		return !isPresent();
	}

	/**
	 * @return <code>true</code> if the object contains either a left or right
	 *         value
	 */
	public boolean isPresent() {
		return isLeft() || isRight();
	}

	/**
	 * @return The non-<code>null</code> left value
	 * @throws NoSuchElementException
	 *             if there is no stored left value
	 */
	public T getLeft() throws NoSuchElementException {
		if (!isLeft()) {
			throw new NoSuchElementException("No left value present");
		}
		return left;
	}

	/**
	 * @return The non-<code>null</code> right value
	 * @throws NoSuchElementException
	 *             if there is no stored right value
	 */
	public U getRight() throws NoSuchElementException {
		if (!isRight()) {
			throw new NoSuchElementException("No left value present");
		}
		return right;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EitherOr)) {
			return false;
		}
		EitherOr<?, ?> other = (EitherOr<?, ?>) obj;
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EitherOr [");
		if (isEmpty()) {
			builder.append("EMPTY");
		} else if (isLeft()) {
			builder.append("Left = ").append(left);
		} else if (isRight()) {
			builder.append("Right = ").append(right);
		}
		builder.append("]");
		return builder.toString();
	}

}
