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
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.Pair;

import static com.vernalis.nodes.misc.distance.AbstractDistanceNodeDialog.createSquareDistanceModel;

/**
 * Base node model class for calculating geometric distance between points.
 * Points can either have coordinates specified in pairs of columns, e.g. (x0,
 * y0, z0) and (x1, y1, z1) requires 6 columns, or as a pair of columns, each
 * containing collection cells, in which case the pairs of cells must contain
 * the same number of cells
 * 
 * @author s.roughley
 *
 */
public abstract class AbstractDistanceNodeModel
		extends AbstractVectorDistanceNodeModel {

	protected final SettingsModelBoolean sqrDistanceMdl =
			registerSettingsModel(createSquareDistanceModel());

	/**
	 * Constructor for a non-collection-based implementation. At least one
	 * dimension name must be supplied
	 * 
	 * @param firstDimension
	 *            The name of the first dimension
	 * @param extraDimensions
	 *            The name of any additional dimensions
	 */
	public AbstractDistanceNodeModel(char firstDimension,
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
	public AbstractDistanceNodeModel(boolean isCollectionBased) {
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
	private AbstractDistanceNodeModel(boolean isCollectionBased,
			char firstDimension, char... extraDimensions) {
		super(isCollectionBased, firstDimension, extraDimensions);
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
		DataColumnSpec distColSpec = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						(isCollectionBased ? "n" : numDims) + "-D "
								+ (sqrDistanceMdl.getBooleanValue() ? "Squared-"
										: "")
								+ "Distance"),
				DoubleCell.TYPE).createSpec();
		if (isCollectionBased) {
			rearranger
					.append(new AbstractCellFactory(true, distColSpec,
							new DataColumnSpecCreator(DataTableSpec
									.getUniqueColumnName(spec, "Dimensions"),
									IntCell.TYPE).createSpec()) {

						@Override
						public DataCell[] getCells(DataRow row) {
							double delta = 0.0;
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
								Iterator<DataCell> startIter =
										startLV.iterator();
								Iterator<DataCell> endIter = endLV.iterator();
								while (startIter.hasNext()
										&& endIter.hasNext()) {
									DataCell startCell = startIter.next();
									if (startCell.isMissing()) {
										return retVal;
									}
									DataCell endCell = endIter.next();
									if (endCell.isMissing()) {
										return retVal;
									}
									double startDbl = ((DoubleValue) startCell)
											.getDoubleValue();
									double endDbl = ((DoubleValue) endCell)
											.getDoubleValue();
									delta += (startDbl - endDbl)
											* (startDbl - endDbl);
								}

							}
							retVal[0] = new DoubleCell(
									sqrDistanceMdl.getBooleanValue() ? delta
											: Math.sqrt(delta));

							return retVal;
						}
					});
		} else {
			rearranger.append(new SingleCellFactory(true, distColSpec) {

				@Override
				public DataCell getCell(DataRow row) {
					double delta = 0.0;

					for (IntPair dim : colIdx.values()) {
						DataCell start = row.getCell(dim.getFirst());
						if (start.isMissing()) {
							return DataType.getMissingCell();
						}
						DataCell end = row.getCell(dim.getSecond());
						if (end.isMissing()) {
							return DataType.getMissingCell();
						}
						double startDbl =
								((DoubleValue) start).getDoubleValue();
						double endDbl = ((DoubleValue) end).getDoubleValue();
						delta += (startDbl - endDbl) * (startDbl - endDbl);
					}

					return new DoubleCell(sqrDistanceMdl.getBooleanValue()
							? delta : Math.sqrt(delta));
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
	@Override
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
	@Override
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

}
