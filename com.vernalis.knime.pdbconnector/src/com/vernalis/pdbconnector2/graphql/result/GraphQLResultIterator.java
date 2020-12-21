/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.graphql.result;

import java.util.Iterator;

/**
 * An {@link Iterator} which wraps a {@link GraphQLResult} iterator, along with
 * the remaining query path
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class GraphQLResultIterator implements Iterator<GraphQLResult> {

	private final Iterator<? extends GraphQLResult> iter;
	private final String remainingPath;

	/**
	 * Constructor from an Iterator
	 * 
	 * @param iter
	 *            The iterator
	 * @param remainingPath
	 *            the remaining path
	 */
	public GraphQLResultIterator(Iterator<? extends GraphQLResult> iter,
			String remainingPath) {
		this.iter = iter;
		this.remainingPath = remainingPath;
	}

	/**
	 * Constructor from an {@link Iterable}
	 * 
	 * @param iter
	 *            The iterable
	 * @param remainingPath
	 *            The remaining path
	 */
	public GraphQLResultIterator(Iterable<? extends GraphQLResult> iter,
			String remainingPath) {
		this(iter.iterator(), remainingPath);
	}

	/**
	 * @return the remainingPath
	 */
	public final String getRemainingPath() {
		return remainingPath;
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public GraphQLResult next() {
		return iter.next();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GraphQLResultIterator [remainingPath=");
		builder.append(remainingPath);
		builder.append(", iter=");
		builder.append(iter);
		builder.append("]");
		return builder.toString();
	}

}
