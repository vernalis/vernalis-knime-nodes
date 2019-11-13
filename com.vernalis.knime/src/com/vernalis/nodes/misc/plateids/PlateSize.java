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
package com.vernalis.nodes.misc.plateids;

import java.util.EnumSet;
import java.util.stream.IntStream;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * A simple enum for the standard screening plate sizes
 * 
 * @author s.roughley
 *
 */
/**
 * @author s.roughley
 *
 */
/**
 * @author s.roughley
 *
 */
public enum PlateSize implements ButtonGroupEnumInterface {
	SIX(2, 3) {

		@Override
		public PlateSize getNextExpansionSize() {
			return TWENTY_FOUR;
		}
	},
	TWELVE(3, 4) {

		@Override
		public PlateSize getNextExpansionSize() {
			return FORTY_EIGHT;
		}
	},
	TWENTY_FOUR(4, 6) {

		@Override
		public PlateSize getNextExpansionSize() {
			return NINETY_SIX;
		}
	},
	FORTY_EIGHT(6, 8) {

		@Override
		public PlateSize getNextExpansionSize() {
			return null;
		}
	},
	NINETY_SIX(8, 12) {

		@Override
		public PlateSize getNextExpansionSize() {
			return THREE_EIGHT_FOUR;
		}
	},
	THREE_EIGHT_FOUR(16, 24) {

		@Override
		public PlateSize getNextExpansionSize() {
			return ONE_FIVE_THREE_SIX;
		}
	},
	ONE_FIVE_THREE_SIX(32, 48) {

		@Override
		public PlateSize getNextExpansionSize() {
			return null;
		}
	};

	private final int rows, cols;

	private PlateSize(int r, int c) {
		rows = r;
		cols = c;
	}

	/**
	 * @return The total number of wells
	 */
	public int getWells() {
		return getRows() * getCols();
	}

	/**
	 * @return The number of columns
	 */
	public int getCols() {
		return cols;
	}

	/**
	 * @return The number of rows
	 */
	public int getRows() {
		return rows;
	}

	/**
	 * @param r
	 *            The row index (1 - {@link #getRows()})
	 * @param c
	 *            The column index (1 - {@link #getCols()})
	 * @param direction
	 *            The direction the plate is being filled
	 * @return The index within the plate of a specified well (by row/column)
	 *         according to the current plate size, and the direction of filling
	 * @throws IllegalArgumentException
	 *             If r or c are out of bounds
	 */
	public int wellIndexFromRowCol(int r, int c, PlateDirection direction)
			throws IllegalArgumentException {
		if (r < 1 || r > getRows() || c < 1 || c > getCols()) {
			throw new IllegalArgumentException(
					"Row and Column must be within the plate size");
		}
		int retVal = -1;
		switch (direction) {
		case COLUMN_WISE:
			retVal = (c - 1) * getRows();// full columns
			retVal += r;
			return retVal;
		case ROW_WISE:
			retVal = (r - 1) * getCols();// full columns
			retVal += c;
			return retVal;
		default:
			return retVal;
		}
	}

	/**
	 * @param well
	 *            The index of the well within the plate
	 * @param direction
	 *            The fill direction
	 * @return The row index
	 */
	public int getRowFromWellIndex(int well, PlateDirection direction) {
		switch (direction) {
		case COLUMN_WISE:
			final int r = well % getRows();
			return r == 0 ? getRows() : r;
		case ROW_WISE:
			return 1 + (well - 1) / getCols();
		default:
			return -1;
		}
	}

	/**
	 * @param well
	 *            The index of the well within the plate
	 * @param direction
	 *            The fill direction
	 * @return The column index
	 */
	public int getColFromWellIndex(int well, PlateDirection direction) {
		switch (direction) {
		case COLUMN_WISE:
			return 1 + (well - 1) / getRows();
		case ROW_WISE:
			int c = well % getCols();
			return c == 0 ? getCols() : c;
		default:
			return -1;
		}
	}

	/**
	 * @return The maximum length a row index can be
	 */
	public int getMaxRowStringWidth() {
		return rowIDFromRowIndex(getRows()).length();
	}

	/**
	 * @return The maximum length a column index can be
	 */
	public int getMaxColStringWidth() {
		return ("" + getCols()).length();
	}

	@Override
	public String getText() {
		return getWells() + "-Well";
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return String.format("A %d-well plate (%d rows x %d cols", getWells(),
				getRows(), getCols());
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default size
	 */
	public static PlateSize getDefault() {
		return NINETY_SIX;
	}

	/**
	 * @param r
	 *            The row index
	 * @return The string label for the row
	 */
	public static String rowIDFromRowIndex(int r) {
		assert r > 0;
		assert r <= 32;
		StringBuilder sb = new StringBuilder();
		if (r > 26) {
			sb.append('A');
		}
		sb.append((char) ((r - 1) % 26 + 'A'));
		return sb.toString();
	}

	/**
	 * @param rID
	 *            The string row ID
	 * @return The row index
	 */
	public static int rowIndexFromRowID(String rID) {
		assert rID != null;
		String rID0 = rID.trim().toUpperCase();
		assert !rID0.isEmpty();
		assert rID0.length() <= 2;
		int retVal = rID0.charAt(rID0.length() - 1) - '@';
		if (rID0.length() > 1) {
			retVal += 26 * (rID0.charAt(0) - '@');
		}
		return retVal;
	}

	/**
	 * @param size
	 *            The numeric well size
	 * @return The enum value for the numeric size
	 * @throws IllegalArgumentException
	 *             if an invalid size is given
	 */
	public static PlateSize valueOf(int size) throws IllegalArgumentException {
		switch (size) {
		case 6:
			return SIX;
		case 12:
			return TWELVE;
		case 24:
			return TWENTY_FOUR;
		case 48:
			return FORTY_EIGHT;
		case 96:
			return NINETY_SIX;
		case 384:
			return THREE_EIGHT_FOUR;
		case 1536:
			return ONE_FIVE_THREE_SIX;
		default:
			throw new IllegalArgumentException("Invalid plate size");
		}
	}

	/**
	 * @param text
	 *            The text, as return by {@link #getText()}
	 * @return The enum value returning the text
	 * @throws IllegalArgumentException
	 *             if no match is found
	 */
	public static PlateSize valueOfFromText(String text)
			throws IllegalArgumentException {
		try {
			return valueOf(text);
		} catch (Exception e) {
			for (PlateSize ps : values()) {
				if (ps.getText().equals(text)) {
					return ps;
				}
			}
		}

		throw new IllegalArgumentException("Invalid plate size");
	}

	/**
	 * @return A Set of the upwards sizes that the current plate can safely be
	 *         expanded to
	 */
	public EnumSet<PlateSize> getUpwardExpansions() {
		final PlateSize nextExpansionSize = getNextExpansionSize();
		if (nextExpansionSize == null) {
			return EnumSet.noneOf(PlateSize.class);
		}
		EnumSet<PlateSize> retVal = EnumSet.of(nextExpansionSize);
		retVal.addAll(nextExpansionSize.getUpwardExpansions());
		return retVal;
	}

	/**
	 * @return The next size upwards plate that the current size can safely be
	 *         expanded to (e.g., a 96 well plate can be mapped to a 384 well
	 *         plate, as e.g. A1 goes to A1, B1, A2, B2). Returns {@code null}
	 *         if there are no possible upwards expansions
	 */
	public abstract PlateSize getNextExpansionSize();

	public int[] getUpwardMapRowIndicesFromRowIndex(int r, PlateSize newSize) {
		if (newSize == this) {
			return new int[] { r };
		}
		if (!getUpwardExpansions().contains(newSize)) {
			throw new UnsupportedOperationException("Cannot map from "
					+ this.getText() + " to " + newSize.getText());
		}
		int mult = newSize.getRows() / this.getRows();
		return IntStream.range((r - 1) * mult + 1, r * mult + 1).toArray();
	}

	public int[] getUpwardMapColIndicesFromColIndex(int c, PlateSize newSize) {
		if (newSize == this) {
			return new int[] { c };
		}
		if (!getUpwardExpansions().contains(newSize)) {
			throw new UnsupportedOperationException("Cannot map from "
					+ this.getText() + " to " + newSize.getText());
		}
		int mult = newSize.getCols() / this.getCols();
		return IntStream.range((c - 1) * mult + 1, c * mult + 1).toArray();
	}
}
