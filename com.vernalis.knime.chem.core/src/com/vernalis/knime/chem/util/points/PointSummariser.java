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
package com.vernalis.knime.chem.util.points;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.DoubleSummary;

/**
 * A {@link Consumer} to summarise a stream of {@link Point}s on a
 * dimension-by-dimension basis. The object is initialised on the first call to
 * {@link #accept(Point)}
 * 
 * @author s.roughley
 *
 * @param <E>
 *            The type of {@link Point} class implementation
 */
public class PointSummariser<E extends Point<E>> implements Consumer<E> {

	private DoubleSummary[] summaries = null;

	@Override
	public void accept(E t) {
		if (summaries == null) {
			summaries = ArrayUtils.of(new Supplier<DoubleSummary>() {

				@Override
				public DoubleSummary get() {
					return new DoubleSummary();
				}
			}, t.getNumDimensions());
		} else if (t.getNumDimensions() != summaries.length) {
			throw new IllegalArgumentException("Dimensionality mismatch");
		}
		for (int d = 0; d < t.getNumDimensions(); d++) {
			summaries[d].accept(t.getCoordinate(d));
		}

	}

	/**
	 * @param dim
	 *            The dimension
	 * @return The {@link DoubleSummary} for the indicated dimension
	 */
	public DoubleSummary getSummary(int dim) {
		return summaries[dim];
	}

	/**
	 * Combines this with a second summariser
	 * 
	 * @param o
	 *            The other {@link PointSummariser}
	 * @return
	 */
	protected PointSummariser<E> combine(PointSummariser<E> o) {
		if (summaries.length < o.summaries.length) {
			throw new IllegalArgumentException("Dimensionality mismatch");
		}
		for (int d = 0; d < summaries.length; d++) {
			summaries[d].combine(o.summaries[d]);
		}
		return this;
	}

	/**
	 * @param dim
	 *            The dimension
	 * @return The {@link DoubleSummary} for the dimension
	 */
	protected DoubleSummary getDimensionSummary(int dim) {
		if (dim < 0 || dim > summaries.length) {
			throw new IllegalArgumentException("Dimension out of range (0 - "
					+ (summaries.length - 1) + ")");
		}
		return summaries[dim];
	}

	/**
	 * @return The number of dimensions
	 */
	protected int getNumDimensions() {
		return summaries.length;
	}
}
