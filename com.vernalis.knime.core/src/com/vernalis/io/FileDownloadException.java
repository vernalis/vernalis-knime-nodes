/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.io;

/**
 * A catch-all class for exceptions relating to downloading files from local or
 * remote servers
 */
public class FileDownloadException extends Exception {

	private static final long serialVersionUID = 648012596916286421L;

	public FileDownloadException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileDownloadException(String message) {
		super(message);
	}

	public FileDownloadException(Throwable cause) {
		super(cause);
	}

}
