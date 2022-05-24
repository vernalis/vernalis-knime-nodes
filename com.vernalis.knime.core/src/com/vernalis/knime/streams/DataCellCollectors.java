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
package com.vernalis.knime.streams;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.SetCell;

/**
 * Class providing Stream {@link Collector} implementations to convert a stream
 * of DataCells to a collection cell
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since v1.34.0
 *
 */
public class DataCellCollectors {

	private DataCellCollectors() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param emptyListToMissing
	 *            Should an empty list be replaced with a Missing Cell?
	 * @param onlyMissingListToMissing
	 *            Should a list comprising only Missing Cells be replaced with a
	 *            Missing Cell?
	 * 
	 * @return A {@link Collector} generating a ListCell according to the
	 *         supplied parameters
	 *
	 * @since v1.34.0
	 */
	public static Collector<DataCell, ?, DataCell> toListCell(
			boolean emptyListToMissing, boolean onlyMissingListToMissing) {
		return new ToListCellCollector(emptyListToMissing,
				onlyMissingListToMissing);
	}

	/**
	 * @return A {@link Collector} generating a {@link ListCell} from a Stream
	 *         of {@link DataCell}s
	 */
	public static Collector<DataCell, ?, DataCell> toListCell() {
		return TO_LIST_CELL_COLLECTOR;
	}

	/**
	 * @return A {@link Collector} generating a {@link ListCell} from a Stream
	 *         of {@link DataCell}s. If the stream is empty, a missing value
	 *         cell is returned'
	 *
	 * @since v1.34.0
	 */
	public static Collector<DataCell, ?, DataCell> toListCellOrMissing() {
		return TO_LIST_CELL_OR_MISSING_COLLECTOR;
	}

	/**
	 * @return A {@link Collector} generating a {@link SetCell} from a Stream of
	 *         {@link DataCell}s
	 */
	public static Collector<DataCell, ?, SetCell> toSetCell() {
		return TO_SET_CELL_COLLECTOR;
	}

	/**
	 * @return A {@link Collector} generating a {@link SetCell} from a Stream of
	 *         {@link DataCell}s. If the stream is empty, a missing value cell
	 *         is returned'
	 *
	 * @since v1.34.0
	 */
	public static Collector<DataCell, ?, DataCell> toSetCellOrMissing() {
		return TO_SET_CELL_OR_MISSING_COLLECTOR;
	}

	/**
	 * A collector to collect a Stream of objects into an array of DataCells,
	 * where each array member is a ListCell. A Function must be supplied for
	 * each array member to obtain a {@link DataCell} from the incoming object,
	 * which will then be aggregated over the Stream into individual List Cells.
	 * This allows an object to return multiple columns of ListCells without
	 * looping over the incoming collection of objects repeatedly for each
	 * column
	 * 
	 * @param <T>
	 *            The incoming object type from the Stream
	 * @param missingforEmptyLists
	 *            Should missing values be returned in place of empty lists?
	 * @param toCellConvertors
	 *            One or more non-null functions to convert the incoming object
	 *            to DataCells
	 * 
	 * @return A collector which will convert a stream of objects to an array of
	 *         List cells
	 *
	 * @since v1.34.0
	 */
	@SafeVarargs
	public static final <T> Collector<T, ?, DataCell[]> toArrayOfListCells(
			boolean missingforEmptyLists,
			Function<T, DataCell>... toCellConvertors) {
		return new ArrayOfListCellCollector<>(missingforEmptyLists,
				toCellConvertors);
	}

	/**
	 * A collector to collect a Stream of objects into an array of DataCells,
	 * where each array member is a ListCell. A Function must be supplied for
	 * each array member to obtain a {@link DataCell} from the incoming object,
	 * which will then be aggregated over the Stream into individual List Cells.
	 * This allows an object to return multiple columns of ListCells without
	 * looping over the incoming collection of objects repeatedly for each
	 * column
	 * 
	 * @param <T>
	 *            The incoming object type from the Stream
	 * @param missingforEmptyLists
	 *            Should missing values be returned in place of empty lists?
	 * @param missingForAllMissingLists
	 *            Should a list containing only missing values be replaced with
	 *            a missing value cell
	 * @param toCellConvertors
	 *            One or more non-null functions to convert the incoming object
	 *            to DataCells
	 * 
	 * @return A collector which will convert a stream of objects to an array of
	 *         List cells
	 *
	 * @since v1.34.0
	 */
	@SafeVarargs
	public static final <T> Collector<T, ?, DataCell[]> toArrayOfListCells(
			boolean missingforEmptyLists, boolean missingForAllMissingLists,
			Function<T, DataCell>... toCellConvertors) {
		return new ArrayOfListCellCollector<>(missingforEmptyLists,
				missingForAllMissingLists, toCellConvertors);
	}

	/**
	 * A {@link Collector} generating a {@link ListCell} from a Stream of
	 * {@link DataCell}s
	 */
	public static final Collector<DataCell, ?, DataCell> TO_LIST_CELL_COLLECTOR =
			new ToListCellCollector(false);

	/**
	 * A {@link Collector} generating a {@link SetCell} from a Stream of
	 * {@link DataCell}s
	 */
	public static final Collector<DataCell, Set<DataCell>, SetCell> TO_SET_CELL_COLLECTOR =
			new Collector<>() {

				@Override
				public Supplier<Set<DataCell>> supplier() {
					return HashSet::new;
				}

				@Override
				public Function<Set<DataCell>, SetCell> finisher() {
					return CollectionCellFactory::createSetCell;
				}

				@Override
				public BinaryOperator<Set<DataCell>> combiner() {
					return (l, r) -> {
						l.addAll(r);
						return l;
					};
				}

				@Override
				public Set<Characteristics> characteristics() {
					return Collections.unmodifiableSet(
							EnumSet.of(Characteristics.UNORDERED));
				}

				@Override
				public BiConsumer<Set<DataCell>, DataCell> accumulator() {
					return (l, c) -> l.add(c);
				}
			};

	/**
	 * A {@link Collector} generating a {@link ListCell} from a Stream of
	 * {@link DataCell}s, or a missing value cell if the stream is empty
	 * 
	 * @since v1.34.0
	 */
	public static final Collector<DataCell, List<DataCell>, DataCell> TO_LIST_CELL_OR_MISSING_COLLECTOR =
			new ToListCellCollector(true);

	/**
	 * A {@link Collector} generating a {@link SetCell} from a Stream of
	 * {@link DataCell}s, or a missing value cell if the stream is empty
	 * 
	 * @since v1.34.0
	 */
	public static final Collector<DataCell, ?, DataCell> TO_SET_CELL_OR_MISSING_COLLECTOR =
			new Collector<DataCell, Set<DataCell>, DataCell>() {

				@Override
				public Supplier<Set<DataCell>> supplier() {
					return HashSet::new;
				}

				@Override
				public Function<Set<DataCell>, DataCell> finisher() {
					return l -> l.isEmpty() ? DataType.getMissingCell()
							: CollectionCellFactory.createSetCell(l);
				}

				@Override
				public BinaryOperator<Set<DataCell>> combiner() {
					return (l, r) -> {
						l.addAll(r);
						return l;
					};
				}

				@Override
				public Set<Characteristics> characteristics() {
					return Collections.unmodifiableSet(
							EnumSet.of(Characteristics.UNORDERED));
				}

				@Override
				public BiConsumer<Set<DataCell>, DataCell> accumulator() {
					return (l, c) -> l.add(c);
				}
			};

}
