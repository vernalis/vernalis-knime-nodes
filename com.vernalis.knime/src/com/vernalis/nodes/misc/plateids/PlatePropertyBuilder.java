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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.knime.core.data.DataColumnProperties;

/**
 * A builder class to allow generation of the column properties used by Plate
 * Well ID nodes
 * 
 * @author s.roughley
 *
 */
class PlatePropertyBuilder {

	/**
	 * Property key for the Row-Column deliminator property
	 */
	static final String ROW_COLUMN_DELIMINATOR = "Row:Column Deliminator";
	/**
	 * Property key for the Plate-Row deliminator property
	 */
	static final String PLATE_ROW_DELIMINATOR = "Plate:Row Deliminator";
	/**
	 * Property key for the Plate prefix property
	 */
	static final String PLATE_PREFIX = "Plate Prefix";
	/**
	 * Property key for the zero-pad column IDs property
	 */
	static final String ZERO_PAD_COLUMN_I_DS = "0-pad Column IDs";
	/**
	 * Property key for the space-pad row IDs property
	 */
	static final String SPACE_PAD_ROW_I_DS = "Space-pad Row IDs";
	/**
	 * Property key for the number of wells skipped at the end of the plate
	 */
	static final String WELLS_SKIPPED_AT_END = "Wells skipped at end";
	/**
	 * Property key for the number of wells skipped at the start of the plate
	 */
	static final String WELLS_SKIPPED_AT_START = "Wells skipped at start";
	/**
	 * Property key for the plate direction
	 */
	static final String PLATE_DIRECTION = "Plate Direction";
	/**
	 * Property key for the number of wells in the plate
	 */
	static final String NUMBER_OF_WELLS = "Number of wells";
	/**
	 * Property key for the plate size
	 */
	static final String PLATE_SIZE = "Plate Size";

	private final Map<String, String> propertyMap = new HashMap<>();

	/**
	 * Constructor. Initialises an empty property map
	 */
	PlatePropertyBuilder() {

	}

	/**
	 * Method to create a new builder replicating any of the properties the
	 * {@link PlatePropertyBuilder} uses contained within a
	 * {@link DataColumnProperties} object. Other properties are ignored
	 * 
	 * @param colProps
	 *            The {@link DataColumnProperties} to copy the properties from
	 * @return A new builder
	 */
	static final PlatePropertyBuilder createFromColumnProperties(
			DataColumnProperties colProps) {
		PlatePropertyBuilder retVal = new PlatePropertyBuilder();
		if (colProps.containsProperty(NUMBER_OF_WELLS)) {
			retVal.propertyMap.put(NUMBER_OF_WELLS,
					colProps.getProperty(NUMBER_OF_WELLS));
		}
		if (colProps.containsProperty(ROW_COLUMN_DELIMINATOR)) {
			retVal.setRowColDeliminator(
					colProps.getProperty(ROW_COLUMN_DELIMINATOR));
		}
		if (colProps.containsProperty(PLATE_ROW_DELIMINATOR)) {
			retVal.setPlateRowDeliminator(
					colProps.getProperty(PLATE_ROW_DELIMINATOR));
		}
		if (colProps.containsProperty(PLATE_PREFIX)) {
			retVal.setPlatePrefix(colProps.getProperty(PLATE_PREFIX));
		}
		if (colProps.containsProperty(ZERO_PAD_COLUMN_I_DS)) {
			retVal.propertyMap.put(ZERO_PAD_COLUMN_I_DS,
					colProps.getProperty(ZERO_PAD_COLUMN_I_DS));
		}
		if (colProps.containsProperty(SPACE_PAD_ROW_I_DS)) {
			retVal.propertyMap.put(SPACE_PAD_ROW_I_DS,
					colProps.getProperty(SPACE_PAD_ROW_I_DS));
		}
		if (colProps.containsProperty(WELLS_SKIPPED_AT_END)) {
			retVal.propertyMap.put(WELLS_SKIPPED_AT_END,
					colProps.getProperty(WELLS_SKIPPED_AT_END));
		}
		if (colProps.containsProperty(WELLS_SKIPPED_AT_START)) {
			retVal.propertyMap.put(WELLS_SKIPPED_AT_START,
					colProps.getProperty(WELLS_SKIPPED_AT_START));
		}
		if (colProps.containsProperty(PLATE_DIRECTION)) {
			retVal.propertyMap.put(PLATE_DIRECTION,
					colProps.getProperty(PLATE_DIRECTION));
		}
		if (colProps.containsProperty(PLATE_SIZE)) {
			retVal.propertyMap.put(PLATE_SIZE,
					colProps.getProperty(PLATE_SIZE));
		}
		return retVal;
	}

	/**
	 * @return The specified properties as a {@link DataColumnProperties} object
	 *         for inclusion in a Column Spec
	 */
	DataColumnProperties getProperties() {
		return new DataColumnProperties(propertyMap);
	}

	/**
	 * Set the row-column deliminator property
	 */
	PlatePropertyBuilder setRowColDeliminator(String rowColDeliminator) {
		propertyMap.put(ROW_COLUMN_DELIMINATOR, rowColDeliminator);
		return this;
	}

	/**
	 * Set the plate-row deliminator
	 */
	PlatePropertyBuilder setPlateRowDeliminator(String plateRowDeliminator) {
		propertyMap.put(PLATE_ROW_DELIMINATOR, plateRowDeliminator);
		return this;
	}

	/**
	 * Set the plate prefix
	 */
	PlatePropertyBuilder setPlatePrefix(String platePrefix) {
		propertyMap.put(PLATE_PREFIX, platePrefix);
		return this;
	}

	/**
	 * Set the zero-pad column IDs property
	 */
	PlatePropertyBuilder setZeroPadColumnIDs(boolean zeroPad) {
		propertyMap.put(ZERO_PAD_COLUMN_I_DS,
				new StringBuilder().append(zeroPad).toString());
		return this;
	}

	/**
	 * Set the space-pad row IDs property
	 */
	PlatePropertyBuilder setSpacePadRowIDs(boolean spacePad) {
		propertyMap.put(SPACE_PAD_ROW_I_DS,
				new StringBuilder().append(spacePad).toString());
		return this;
	}

	/**
	 * Set the wells skipped at start of plate property
	 */
	PlatePropertyBuilder setWellsSkippedAtStart(int wellsSkipped) {
		propertyMap.put(WELLS_SKIPPED_AT_START,
				new StringBuilder().append(wellsSkipped).toString());
		return this;
	}

	/**
	 * Set the wells skipped at start of plate property only if the number is >0
	 */
	PlatePropertyBuilder setOptionallyWellsSkippedAtStart(int wellsSkipped) {
		if (wellsSkipped > 0) {
			return setWellsSkippedAtStart(wellsSkipped);
		}
		return this;
	}

	/**
	 * Set the wells skipped at end of plate property
	 */
	PlatePropertyBuilder setWellsSkippedAtEnd(int wellsSkipped) {
		propertyMap.put(WELLS_SKIPPED_AT_END,
				new StringBuilder().append(wellsSkipped).toString());
		return this;
	}

	/**
	 * Set the wells skipped at end of plate property only if the number is >0
	 */
	PlatePropertyBuilder setOptionallyWellsSkippedAtEnd(int wellsSkipped) {
		if (wellsSkipped > 0) {
			return setWellsSkippedAtEnd(wellsSkipped);
		}
		return this;
	}

	/**
	 * Set the plate direction property
	 */
	PlatePropertyBuilder setPlateDirection(PlateDirection direction) {
		propertyMap.put(PLATE_DIRECTION, direction.getText());
		return this;
	}

	/**
	 * Set the plate size property. Also sets the number of wells
	 */
	PlatePropertyBuilder setPlateSize(PlateSize size) {
		propertyMap.put(PLATE_SIZE, size.getText());
		propertyMap.put(NUMBER_OF_WELLS,
				new StringBuilder().append(size.getWells()).toString());
		return this;
	}

	/**
	 * Clear the plate prefix property
	 */
	PlatePropertyBuilder clearPlatePrefix() {
		propertyMap.remove(PLATE_PREFIX);
		return this;
	}

	/**
	 * Clear the row-plate deliminator property
	 */
	PlatePropertyBuilder clearPlateRowDeliminator() {
		propertyMap.remove(PLATE_ROW_DELIMINATOR);
		return this;
	}

	/**
	 * Clear the wells skipped at start property
	 */
	PlatePropertyBuilder clearWellsSkippedAtStart() {
		propertyMap.remove(WELLS_SKIPPED_AT_START);
		return this;
	}

	/**
	 * Clear the wells skipped at end property
	 */
	PlatePropertyBuilder clearWellsSkippedAtEnd() {
		propertyMap.remove(WELLS_SKIPPED_AT_END);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PlatePropertyBuilder [");
		builder.append(propertyMap.entrySet().stream()
				.map(x -> new StringBuilder().append(x.getKey()).append('=')
						.append(x.getValue()).toString())
				.collect(Collectors.joining(", ")));
		builder.append("]");
		return builder.toString();
	}
}
