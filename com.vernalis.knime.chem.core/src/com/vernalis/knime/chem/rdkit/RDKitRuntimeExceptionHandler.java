/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.rdkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.RDKit.AtomKekulizeException;
import org.RDKit.AtomSanitizeException;
import org.RDKit.AtomValenceException;
import org.RDKit.ChemicalReactionException;
import org.RDKit.ChemicalReactionParserException;
import org.RDKit.ConformerException;
import org.RDKit.GenericRDKitException;
import org.RDKit.KekulizeException;
import org.RDKit.KeyErrorException;
import org.RDKit.MolPicklerException;
import org.RDKit.MolSanitizeException;
import org.RDKit.MolSanitizeException_Vect;
import org.RDKit.SmilesParseException;

/**
 * This class wraps RDKit {@link RuntimeException}s and obtains the message via
 * reflection from either the {@code message} or {@code what} methods. If
 * neither method is found, it falls back on the standard {@link #getMessage()}
 * method. The original 'cause' RDKit exception is available via
 * {@link #getCause()}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since com.vernalis.knime.chem.core 1.27.4
 */
@SuppressWarnings("serial")
public class RDKitRuntimeExceptionHandler extends RuntimeException {

	// The following from RDKitExceptions.i
	/**
	 * Constructor from {@link ChemicalReactionException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(ChemicalReactionException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link ChemicalReactionParserException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(ChemicalReactionParserException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link ConformerException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(ConformerException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link MolPicklerException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(MolPicklerException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link MolSanitizeException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(MolSanitizeException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link SmilesParseException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(SmilesParseException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link KeyErrorException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(KeyErrorException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link GenericRDKitException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(GenericRDKitException e) {
		this((RuntimeException) e);
	}

	// The following subclasses of MolSanitizeException are defined in
	// SanitException.i
	/**
	 * Constructor from {@link AtomSanitizeException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(AtomSanitizeException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link AtomValenceException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(AtomValenceException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link AtomKekulizeException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(AtomKekulizeException e) {
		this((RuntimeException) e);
	}

	/**
	 * Constructor from {@link KekulizeException}
	 * 
	 * @param e
	 *            The cause
	 */
	public RDKitRuntimeExceptionHandler(KekulizeException e) {
		this((RuntimeException) e);
	}

	private RDKitRuntimeExceptionHandler(RuntimeException cause) {
		super(getRDKitMessage(cause), cause);

	}

	/**
	 * @return The class of the initial RDKit Exception
	 */
	public Class<?> getRDKitExceptionClass() {
		return getCause().getClass();
	}

	private static String getRDKitMessage(RuntimeException cause) {
		Class<?> clazz = cause.getClass();
		String msg = null;
		Method mtd = null;
		methodloop: for (Method m : clazz.getMethods()) {
			switch (m.getName()) {
				case "what":
				case "message":
					mtd = m;
					break methodloop;
				default:
					// Nothing to do
			}
		}
		if (mtd != null) {
			mtd.setAccessible(true);
			try {
				msg = (String) mtd.invoke(cause);
			} catch (ClassCastException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				// Do nothing
			}
		}
		if (msg == null) {
			msg = cause.getMessage();
		}
		return msg;
	}

	/**
	 * Static factory method to handle {@link MolSanitizeException_Vect}
	 * 
	 * @param msev
	 *            The cause vector
	 * @return A List of handlers for the individual
	 *         {@link MolSanitizeException}s
	 */
	public static List<RDKitRuntimeExceptionHandler>
			fromMolSanitizeExceptionVect(MolSanitizeException_Vect msev) {
		List<RDKitRuntimeExceptionHandler> retVal = new ArrayList<>();
		for (int i = 0; i < msev.size(); i++) {
			retVal.add(new RDKitRuntimeExceptionHandler(msev.get(i)));
		}
		return retVal;
	}
}
