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
package com.vernalis.knime.streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;

/**
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
final class ToListCellCollector
		implements Collector<DataCell, List<DataCell>, DataCell> {

	private final boolean missingForEmptyLists;
	private final boolean missingForAllMissingLists;
	private boolean hasContent = false;

	/**
	 * Constructor. List comprising only missing cells are returned as a list of
	 * missing cells
	 * 
	 * @param missingForEmptyLists
	 *            Should an empty list be replaced with a Missing Value cell
	 *
	 * @since v1.34.0
	 */
	ToListCellCollector(boolean missingForEmptyLists) {
		this(missingForEmptyLists, false);
	}

	/**
	 * Full constructor
	 * 
	 * @param missingForEmptyLists
	 *            Should an empty list be replaced with a Missing Value cell
	 * @param missingForAllMissingLists
	 *            Should a list containing only missing values be replace with a
	 *            missing value cell
	 *
	 * @since v1.34.0
	 */
	ToListCellCollector(boolean missingForEmptyLists,
			boolean missingForAllMissingLists) {
		this.missingForEmptyLists = missingForEmptyLists;
		this.missingForAllMissingLists = missingForAllMissingLists;
	}

	@Override
	public Supplier<List<DataCell>> supplier() {
		return ArrayList::new;
	}

	@Override
	public Function<List<DataCell>, DataCell> finisher() {
		return l -> (missingForEmptyLists && l.isEmpty())
				|| (missingForAllMissingLists && !hasContent)
						? DataType.getMissingCell()
						: CollectionCellFactory.createListCell(l);
	}

	@Override
	public BinaryOperator<List<DataCell>> combiner() {
		return (l, r) -> {
			l.addAll(r);
			return l;
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(Collections.emptySet());
	}

	@Override
	public BiConsumer<List<DataCell>, DataCell> accumulator() {

		return (l, c) -> {
			if (missingForAllMissingLists && !hasContent && !c.isMissing()) {
				hasContent = true;
			}
			l.add(c);
		};
	}
}
