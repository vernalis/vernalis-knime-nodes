/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.plot.nodes.kerneldensity;

/**
 * An exception during kernel calculation
 * 
 * @author s.roughley
 *
 */
@SuppressWarnings("serial")
public class KernelException extends Exception {

	/**
	 * {@inheritDoc}
	 */
	public KernelException() {
	}

	/**
	 * {@inheritDoc}
	 */
	public KernelException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	/**
	 * {@inheritDoc}
	 */
	public KernelException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	public KernelException(String arg0) {
		super(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public KernelException(Throwable arg0) {
		super(arg0);
	}

}
