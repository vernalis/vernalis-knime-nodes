/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp;

/**
 * This exception is thrown when a molecule is not appropriate for
 * fragmentation. The reason should be provided
 * 
 * @author S.Roughley
 *
 */
public class IncomingMoleculeException extends Exception {

	/**
	 * Serial ID
	 */
	private static final long serialVersionUID = 5971028307905417835L;

	/**
	 * Constructor - this is the normal constructor to use, providing a reason
	 * 
	 * @param message
	 *            The reason the row could not be fragmented
	 */
	public IncomingMoleculeException(String message) {
		super(message);
	}

	/**
	 * Constructor where a cause in addition to a textual reason is required
	 * 
	 * @param message
	 *            The reason the row could not be fragmented
	 * @param cause
	 *            A causing exception
	 */
	public IncomingMoleculeException(String message, Throwable cause) {
		super(message, cause);
	}

}
