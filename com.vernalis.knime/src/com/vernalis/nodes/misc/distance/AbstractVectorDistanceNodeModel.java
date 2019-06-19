/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.distance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;
import org.knime.core.util.Pair;

import static com.vernalis.nodes.misc.distance.AbstractVectorDistanceNodeDialog.LIST_OF_NUMBERS;
import static com.vernalis.nodes.misc.distance.AbstractVectorDistanceNodeDialog.createEndModel;
import static com.vernalis.nodes.misc.distance.AbstractVectorDistanceNodeDialog.createStartModel;

/**
 * Base node model class for calculating vector distances between points. Points
 * can either have coordinates specified in pairs of columns, e.g. (x0, y0, z0)
 * and (x1, y1, z1) requires 6 columns, or as a pair of columns, each containing
 * collection cells, in which case the pairs of cells must contain the same
 * number of cells. Distances are endPoint-startPoint
 * 
 * @author s.roughley
 *
 */
public abstract class AbstractVectorDistanceNodeModel
		extends SimpleStreamableFunctionNodeModel {

	/**
	 * A primitive implementation of Pair<T,U> to store a pair of ints
	 * 
	 * @author s.roughley
	 *
	 */
	static final class IntPair {

		private final int first;
		private final int second;

		/**
		 * Constructor
		 * 
		 * @param first
		 *            the first value
		 * @param second
		 *            the second value
		 */
		IntPair(int first, int second) {
			this.first = first;
			this.second = second;
		}

		/**
		 * @return the first value
		 */
		public int getFirst() {
			return first;
		}

		/**
		 * @return the second value
		 */
		public int getSecond() {
			return second;
		}

	}

	protected final Map<String, Pair<SettingsModelString, SettingsModelString>> dimensionModels =
			new TreeMap<>();
	protected final ColumnFilter colFilter;
	protected final boolean isCollectionBased;

	/**
	 * Constructor for a non-collection-based implementation. At least one
	 * dimension name must be supplied
	 * 
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	public AbstractVectorDistanceNodeModel(char firstDimension,
			char... extraDimensions) {
		this(false, firstDimension, extraDimensions);
	}

	/**
	 * Convenience constructor for implementations requiring only a pair of
	 * columns, which may be collection based
	 * 
	 * @param isCollectionBased
	 *            Are the columns collection columns, in which case the node is
	 *            n-dimensional
	 */
	public AbstractVectorDistanceNodeModel(boolean isCollectionBased) {
		this(isCollectionBased, isCollectionBased ? 'n' : 'x');
	}

	/**
	 * Full constructor, exposing all options, some combinations of which may be
	 * invalid
	 * 
	 * @param isCollectionBased
	 *            Are the columns collection columns, in which case the node is
	 *            n-dimensional, and only the first dimension can be supplied
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	@SuppressWarnings("unchecked")
	protected AbstractVectorDistanceNodeModel(boolean isCollectionBased,
			char firstDimension, char... extraDimensions) {
		if (isCollectionBased && extraDimensions.length > 0) {
			throw new IllegalArgumentException(
					"Collection-based implementations should only supply a single dimension name");
		}
		registerDimension(firstDimension);
		for (char dim : extraDimensions) {
			registerDimension(dim);
		}
		colFilter = isCollectionBased ? LIST_OF_NUMBERS
				: new DataValueColumnFilter(DoubleValue.class);
		this.isCollectionBased = isCollectionBased;
	}

	/**
	 * Method to generate and register for loading/saving the settings models
	 * for a dimension, which must be a letter
	 * 
	 * @param dim
	 *            The name of the dimension
	 */
	protected final void registerDimension(char dim) {
		if (!Character.isLetter(dim)) {
			throw new IllegalArgumentException("Dimension must be a letter!");
		}
		dimensionModels.put(String.valueOf(dim).toLowerCase(),
				new Pair<>(createStartModel(dim), createEndModel(dim)));
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		if (dimensionModels.isEmpty()) {
			throw new InvalidSettingsException(
					"Implementation problem - no dimensions registered");
		}
		// Deque allows easy removal of last item for guessing
		Deque<String> numericalColNames = new LinkedList<>(
				spec.stream().filter(x -> colFilter.includeColumn(x))
						.map(x -> x.getName()).collect(Collectors.toList()));
		List<String> selectedColNames = new ArrayList<>();
		// Check we have enough columns
		final int numDims = dimensionModels.size();

		if (numericalColNames.size() < numDims * 2) {
			throw new InvalidSettingsException(
					(numDims * 2) + " distinct numerical "
							+ (isCollectionBased ? "collection " : "")
							+ "columns are required");
		}

		// Now check any selected columns are present as numericals, and list
		// those selected, checking that none are duplicated
		checkSelectedIncomingColumns(numericalColNames, selectedColNames);

		numericalColNames.removeAll(selectedColNames);

		// Now we need to assign the last available column to each non-selected
		// column in turn, starting with x1, x0...
		StringBuilder msg = guessMissingColumnNames(numericalColNames);
		if (msg.length() > 0) {
			setWarningMessage(msg.substring(1));
		}

		// Now find the column indices
		Map<String, IntPair> colIdx = new TreeMap<>();
		for (Entry<String, Pair<SettingsModelString, SettingsModelString>> dim : dimensionModels
				.entrySet()) {
			final IntPair cols = new IntPair(
					spec.findColumnIndex(
							dim.getValue().getFirst().getStringValue()),
					spec.findColumnIndex(
							dim.getValue().getSecond().getStringValue()));
			if (cols.getFirst() < 0) {
				throw new InvalidSettingsException("Unable to find column '"
						+ dim.getValue().getFirst().getStringValue()
						+ "' chosen for " + dim.getKey() + "0");
			}
			if (cols.getSecond() < 0) {
				throw new InvalidSettingsException("Unable to find column '"
						+ dim.getValue().getSecond().getStringValue()
						+ "' chosen for " + dim.getKey() + "1");
			}
			colIdx.put(dim.getKey(), cols);
		}

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		DataColumnSpec[] distColSpec =
				new DataColumnSpec[numDims + (isCollectionBased ? 1 : 0)];
		int dimIdx = 0;
		for (Entry<String, Pair<SettingsModelString, SettingsModelString>> dim : dimensionModels
				.entrySet()) {
			String dimName = dim.getKey();
			distColSpec[dimIdx++] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec,
							("d" + dimName.toUpperCase() + " ("
									+ dim.getValue().getSecond()
											.getStringValue()
									+ " - "
									+ dim.getValue().getFirst().getStringValue()
									+ ")")),
					isCollectionBased
							? ListCell.getCollectionType(DoubleCell.TYPE)
							: DoubleCell.TYPE).createSpec();
		}

		if (isCollectionBased) {
			distColSpec[dimIdx++] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, "Dimensions"),
					IntCell.TYPE).createSpec();
			rearranger.append(new AbstractCellFactory(true, distColSpec) {

				@Override
				public DataCell[] getCells(DataRow row) {
					List<DataCell> deltas = new ArrayList<>();
					DataCell[] retVal = new DataCell[2];
					Arrays.fill(retVal, DataType.getMissingCell());
					for (IntPair dim : colIdx.values()) {
						DataCell start = row.getCell(dim.getFirst());
						if (start.isMissing()) {
							return retVal;
						}
						DataCell end = row.getCell(dim.getSecond());
						if (end.isMissing()) {
							return retVal;
						}
						ListDataValue startLV = (ListDataValue) start;
						ListDataValue endLV = (ListDataValue) end;
						if (startLV.size() != endLV.size()) {
							return retVal;
						}
						retVal[1] = new IntCell(endLV.size());
						Iterator<DataCell> startIter = startLV.iterator();
						Iterator<DataCell> endIter = endLV.iterator();
						while (startIter.hasNext() && endIter.hasNext()) {
							DataCell startCell = startIter.next();
							if (startCell.isMissing()) {
								deltas.add(DataType.getMissingCell());
								continue;
							}
							DataCell endCell = endIter.next();
							if (endCell.isMissing()) {
								deltas.add(DataType.getMissingCell());
								continue;
							}
							double startDbl =
									((DoubleValue) startCell).getDoubleValue();
							double endDbl =
									((DoubleValue) endCell).getDoubleValue();
							deltas.add(new DoubleCell(endDbl - startDbl));
						}

					}
					retVal[0] = CollectionCellFactory.createListCell(deltas);

					return retVal;
				}
			});
		} else {
			rearranger.append(new AbstractCellFactory(true, distColSpec) {

				@Override
				public DataCell[] getCells(DataRow row) {
					DataCell[] retVal = new DataCell[colIdx.size()];
					Arrays.fill(retVal, DataType.getMissingCell());
					int dimIdx = 0;
					for (IntPair dim : colIdx.values()) {
						DataCell start = row.getCell(dim.getFirst());
						if (start.isMissing()) {
							dimIdx++;
							continue;
						}
						DataCell end = row.getCell(dim.getSecond());
						if (end.isMissing()) {
							dimIdx++;
							continue;
						}
						double startDbl =
								((DoubleValue) start).getDoubleValue();
						double endDbl = ((DoubleValue) end).getDoubleValue();
						retVal[dimIdx++] = new DoubleCell(endDbl - startDbl);
					}

					return retVal;
				}
			});
		}
		return rearranger;

	}

	/**
	 * Method which handles guessing the column names for all dimension columns
	 * as required
	 * 
	 * @param numericalColNames
	 *            A {@link Deque} containing all the suitable columns to chose
	 *            from
	 * @return A {@link StringBuilder} containing any messages about guesses
	 *         made along the way
	 */
	protected StringBuilder guessMissingColumnNames(
			Deque<String> numericalColNames) {
		StringBuilder retVal = new StringBuilder();
		for (Entry<String, Pair<SettingsModelString, SettingsModelString>> ent : dimensionModels
				.entrySet()) {
			retVal.append(
					guessColumn(ent.getValue().getFirst(), numericalColNames));
			retVal.append(
					guessColumn(ent.getValue().getSecond(), numericalColNames));
		}
		return retVal;
	}

	/**
	 * Method which handles checking that selected column names for all
	 * dimension columns are correct and present
	 * 
	 * @param numericalColNames
	 *            A collection of all the suitable columns to choose from
	 * @param selectedColNames
	 *            A collection of all the selected columns
	 * @throws InvalidSettingsException
	 *             If there is a problem with any of the selections (i.e. the
	 *             column is missing from the table, or is of an incorrect type)
	 */
	protected void checkSelectedIncomingColumns(
			Collection<String> numericalColNames,
			Collection<String> selectedColNames)
			throws InvalidSettingsException {
		for (Entry<String, Pair<SettingsModelString, SettingsModelString>> ent : dimensionModels
				.entrySet()) {
			checkColumn(ent.getValue().getFirst(), ent.getKey(),
					numericalColNames, selectedColNames);
			checkColumn(ent.getValue().getSecond(), ent.getKey(),
					numericalColNames, selectedColNames);
		}
	}

	/**
	 * Method to guess a column name from the incoming table if one is not
	 * already supplied. The method first attempts to use a column with the same
	 * name as the dimensions (e.g. x0, y1 etc), and if that is not present,
	 * chooses the last column in the table which is not one of the other
	 * dimension names, and is not already chosen. Finally, a column will be
	 * used which might match another dimension name. In practice, this shouldnt
	 * happen, as the node should know by the time it is here that there are
	 * sufficient columns to choose from.
	 * 
	 * @param mdl
	 *            The settings model for the current column
	 * @param numericalColNames
	 *            A {@link Deque} of all the suitable columns to choose from
	 * @return A string indicating what guess was made (or an empty string if no
	 *         guessing was performed)
	 */
	protected final String guessColumn(SettingsModelString mdl,
			Deque<String> numericalColNames) {
		String name = mdl.getKey();
		if (mdl.getStringValue() == null || mdl.getStringValue().isEmpty()) {
			if (numericalColNames.contains(name)) {
				mdl.setStringValue(name);
				numericalColNames.removeLastOccurrence(name);
			} else {
				Iterator<String> iter = numericalColNames.descendingIterator();
				guessAgain: while (iter.hasNext()) {
					String nextGuess = iter.next();
					for (Pair<SettingsModelString, SettingsModelString> dimMdl : dimensionModels
							.values()) {
						if (dimMdl.getFirst().getKey().equals(nextGuess)
								|| dimMdl.getSecond().getKey()
										.equals(nextGuess)) {
							continue guessAgain;
						}
					}
					mdl.setStringValue(nextGuess);
					numericalColNames.removeLastOccurrence(nextGuess);
					break;
				}
				if (mdl.getStringValue() == null
						|| mdl.getStringValue().isEmpty()) {
					// We didnt find without a clash, so just take the last
					// column and keep going - dont *think* it should be
					// possible
					// to be here!
					mdl.setStringValue(numericalColNames.pollLast());
				}
			}
			getLogger().info("Auto-guessing column '" + mdl.getStringValue()
					+ "' for " + name);
			return ("\nAuto-guessing column '" + mdl.getStringValue() + "' for "
					+ name);
		}
		return "";
	}

	/**
	 * Method to check that the selected column is present in the table as a
	 * column of the appropriate type, and has not already been selected for
	 * another coordinate column
	 * 
	 * @param mdl
	 *            The settings model for the current column
	 * @param name
	 *            The name of the dimension we are looking for (e.g. 'x0' etc)
	 * @param numericalColNames
	 *            A collection of all the numerical columns in the table
	 * @param selectedColNames
	 *            A collection of all the columns selected so far. Modified to
	 *            include the current selection if it passed the conditions
	 * @throws InvalidSettingsException
	 *             If the column was not present or of the correct type, or was
	 *             selected previously
	 */
	protected final void checkColumn(SettingsModelString mdl, String name,
			Collection<String> numericalColNames,
			Collection<String> selectedColNames)
			throws InvalidSettingsException {
		if (mdl.getStringValue() != null && !mdl.getStringValue().isEmpty()) {
			if (!numericalColNames.contains(mdl.getStringValue())) {
				throw new InvalidSettingsException(name + " column '"
						+ mdl.getStringValue() + "' is not present in table");
			} else if (selectedColNames.contains(mdl.getStringValue())) {
				throw new InvalidSettingsException(
						"Column '" + mdl.getStringValue() + "' selected twice");
			}
			selectedColNames.add(mdl.getStringValue());
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		for (Pair<SettingsModelString, SettingsModelString> mdls : dimensionModels
				.values()) {
			mdls.getFirst().saveSettingsTo(settings);
			mdls.getSecond().saveSettingsTo(settings);
		}
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (Pair<SettingsModelString, SettingsModelString> mdls : dimensionModels
				.values()) {
			mdls.getFirst().validateSettings(settings);
			mdls.getSecond().validateSettings(settings);
		}
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (Pair<SettingsModelString, SettingsModelString> mdls : dimensionModels
				.values()) {
			mdls.getFirst().loadSettingsFrom(settings);
			mdls.getSecond().loadSettingsFrom(settings);
		}
	}

}
