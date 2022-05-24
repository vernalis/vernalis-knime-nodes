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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;

import com.vernalis.knime.misc.ArrayUtils;

/**
 * A collector to collect a Stream of objects into an array of DataCells, where
 * each array member is a ListCell. A Function must be supplied for each array
 * member to obtain a {@link DataCell} from the incoming object, which will then
 * be aggregated over the Stream into individual List Cells. This allows an
 * object to return multiple columns of ListCells without looping over the
 * incoming collection of objects repeatedly for each column
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @param <T>
 *            The incoming object type from the Stream
 *
 *
 * @since v1.34.0
 */
class ArrayOfListCellCollector<T>
		implements Collector<T, List<List<DataCell>>, DataCell[]> {

	private final Function<T, DataCell>[] toCellConvertors;
	private final ToListCellCollector[] collectors;

	/**
	 * Constructor with lists of missing cells returned when all list values are
	 * missing cells
	 *
	 * @param missingForEmptyLists
	 *            Should missing values be returned in place of empty lists?
	 * @param toCellConvertors
	 *            One or more non-null functions to convert the incoming object
	 *            to DataCells
	 * 
	 * @since v1.34.0
	 */
	@SafeVarargs
	public ArrayOfListCellCollector(boolean missingForEmptyLists,
			Function<T, DataCell>... toCellConvertors) {
		this(missingForEmptyLists, false, toCellConvertors);
	}

	/**
	 * Full Constructor
	 * 
	 * @param missingForEmptyLists
	 *            Should an empty list be replaced with a Missing Value cell
	 * @param missingForAllMissingLists
	 *            Should a list containing only missing values be replaced with
	 *            a missing value cell
	 * @param toCellConvertors
	 *            One or more non-null functions to convert the incoming object
	 *            to DataCells
	 *
	 * @since v1.34.0
	 */
	@SafeVarargs
	public ArrayOfListCellCollector(boolean missingForEmptyLists,
			boolean missingForAllMissingLists,
			Function<T, DataCell>... toCellConvertors) {

		if (Objects.requireNonNull(toCellConvertors,
				"The convertors must be non-null").length < 1) {
			throw new IllegalArgumentException(
					"At least one object to cell convertor function must be supplied");
		}
		this.toCellConvertors = Arrays.stream(toCellConvertors)
				.filter(Objects::nonNull).toArray(Function[]::new);
		if (this.toCellConvertors.length == 0) {
			throw new IllegalArgumentException(
					"At least one non-null object to cell convertor "
							+ "function must be supplied");
		}
		collectors = ArrayUtils.of(
				() -> new ToListCellCollector(missingForEmptyLists,
						missingForAllMissingLists),
				this.toCellConvertors.length);
	}

	@Override
	public Supplier<List<List<DataCell>>> supplier() {
		return () -> Arrays.stream(collectors).map(c -> c.supplier().get())
				.collect(Collectors.toList());
	}

	@Override
	public BiConsumer<List<List<DataCell>>, T> accumulator() {
		return new BiConsumer<>() {

			@Override
			public void accept(List<List<DataCell>> t, T u) {
				for (int i = 0; i < collectors.length; i++) {
					collectors[i].accumulator().accept(t.get(i),
							toCellConvertors[i].apply(u));
				}

			}
		};
	}

	@Override
	public BinaryOperator<List<List<DataCell>>> combiner() {
		return new BinaryOperator<>() {

			@Override
			public List<List<DataCell>> apply(List<List<DataCell>> t,
					List<List<DataCell>> u) {
				List<List<DataCell>> retVal = new ArrayList<>();
				for (int i = 0; i < collectors.length; i++) {
					retVal.add(
							collectors[i].combiner().apply(t.get(i), u.get(i)));
				}
				return retVal;
			}
		};
	}

	@Override
	public Function<List<List<DataCell>>, DataCell[]> finisher() {
		return new Function<>() {

			@Override
			public DataCell[] apply(List<List<DataCell>> t) {
				DataCell[] retVal = new DataCell[collectors.length];
				for (int i = 0; i < collectors.length; i++) {
					retVal[i] = collectors[i].finisher().apply(t.get(i));
				}
				return retVal;
			}
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Collections.unmodifiableSet(Collections.emptySet());
	}

}
