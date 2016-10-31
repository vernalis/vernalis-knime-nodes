/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
/**
 * 
 */
package com.vernalis.knime.mmp.fragmentors;

/**
 * Exception thrown during fragmentation of a molecule if the fragmentation was
 * not 'legal', i.e. does not result in a valid key-value pair
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class MoleculeFragmentationException extends Exception {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 2185142546961291720L;

	/**
	 * Default constructor
	 */
	public MoleculeFragmentationException() {
	}

	/**
	 * Constructor with message
	 * 
	 * @param message
	 *            The message
	 */
	public MoleculeFragmentationException(String message) {
		super(message);
	}

	/**
	 * Constructor with cause
	 * 
	 * @param cause
	 *            The throwable cause
	 */
	public MoleculeFragmentationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with message and cause
	 * 
	 * @param message
	 *            The message
	 * @param cause
	 *            The throwable cause
	 */
	public MoleculeFragmentationException(String message, Throwable cause) {
		super(message, cause);
	}

}
