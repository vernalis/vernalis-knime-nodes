/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.core.system;

/**
 * A simple Exception class for catching exceptions relating to command
 * execution
 * 
 * @author s.roughley
 * 
 */
public class CommandExecutionException extends Exception {

		private static final long serialVersionUID = 4436909848992574086L;

		public CommandExecutionException() {
		super();
	}

	/**
	 * @param message
	 */
	public CommandExecutionException(String message) {
		super(message);

	}

	/**
	 * @param throwable
	 */
	public CommandExecutionException(Throwable throwable) {
		super(throwable);

	}

	/**
	 * @param message
	 * @param throwable
	 */
	public CommandExecutionException(String message, Throwable throwable) {
		super(message, throwable);

	}


}
