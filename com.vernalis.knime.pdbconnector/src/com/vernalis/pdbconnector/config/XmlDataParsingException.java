/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd.
 * 
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
package com.vernalis.pdbconnector.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;

/**
 * This class is used to record any errors generated during XML parsing. As the
 * datacell generated is still required to be returned by the method, this
 * exception stores both the cell and any messages. Errors can be stored at two
 * levels, enumerated in {@link Level}
 * <p>
 * The value of the stored data cell must be set with a call to
 * {@link #setCell(DataCell)}, as it's value may not be known at exception time
 * 
 * @author S.Roughley
 *
 */
@SuppressWarnings("serial")
public class XmlDataParsingException extends Exception {
	List<DataCell> errors;
	DataCell cell;

	/**
	 * An enum listing the supported error levels, which roughly correspond to
	 * those in the NodeLogger. If an ERROR is encountered, a different
	 * exception should be thrown indicating that node execution cannot
	 * continue.
	 * 
	 * @author S.Roughley
	 *
	 */
	public enum Level {
		INFO, WARN;
	}

	/**
	 * Constructor
	 */
	public XmlDataParsingException() {
		errors = new ArrayList<>();
	}

	/**
	 * Add a message to the exception
	 * 
	 * @param level
	 *            The {@link Level} of the message
	 * @param message
	 *            The error message
	 */
	public void addMessage(Level level, String message) {
		errors.add(new StringCell(level.toString() + ": " + message));
	}

	/**
	 * Add a message to the exception at the {@link Level#INFO} error level
	 * 
	 * @param info
	 *            The message
	 */
	public void addInfo(String info) {
		addMessage(Level.INFO, info);
	}

	/**
	 * Add a message to the exception at the {@link Level#WARN} error level
	 * 
	 * @param info
	 *            The message
	 */
	public void addWarning(String warning) {
		addMessage(Level.WARN, warning);
	}

	/**
	 * @return the cell Stored in the exception. Will be {@code null} if a call
	 *         to {@link #setCell(DataCell)} has not been made
	 */
	public DataCell getCell() {
		return cell;
	}

	/**
	 * Set the value of the data cell generated
	 * 
	 * @param cell
	 *            the cell to set
	 */
	public void setCell(DataCell cell) {
		this.cell = cell;
	}

	/**
	 * Check if there are any error messages recorded
	 * 
	 * @see #hasLevelMessage(Level)
	 */
	public boolean hasMessages() {
		return errors.size() > 0;
	}

	/**
	 * Check if there are any error messages recorded at the indicated
	 * {@link Level}
	 * 
	 * @param level
	 *            The {@link Level} to check
	 * @see #hasInfo()
	 * @see #hasWarning()
	 */
	public boolean hasLevelMessage(Level level) {
		return errors.stream()
				.anyMatch(a -> ((StringValue) cell).getStringValue().startsWith(level.toString()));
	}

	/**
	 * Check if there are any messages at the {@link Level#INFO} level
	 */
	public boolean hasInfo() {
		return hasLevelMessage(Level.INFO);
	}

	/**
	 * Check if there are any messages at the {@link Level#WARN} level
	 */
	public boolean hasWarning() {
		return hasLevelMessage(Level.WARN);
	}

	/**
	 * Return a list of all the error messages
	 * 
	 * @see #getErrors(Level)
	 */
	public List<DataCell> getMessages() {
		return errors;
	}

	/**
	 * Get any error messages recorded at the indicated {@link Level}
	 * 
	 * @param level
	 *            The {@link Level} to check
	 * @see #getInfos()
	 * @see #getWarnings()
	 */
	public List<DataCell> getErrors(Level level) {
		return errors.stream()
				.filter(x -> ((StringValue) x).getStringValue().startsWith(level.toString()))
				.collect(Collectors.toList());
	}

	/**
	 * @return A list of all the {@link Level#INFO} error level messages
	 */
	public List<DataCell> getInfos() {
		return getErrors(Level.INFO);
	}

	/**
	 * @return A list of all the {@link Level#WARN} error level messages
	 */
	public List<DataCell> getWarnings() {
		return getErrors(Level.WARN);
	}
}
