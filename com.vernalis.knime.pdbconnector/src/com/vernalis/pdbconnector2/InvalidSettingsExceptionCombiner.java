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
package com.vernalis.pdbconnector2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.knime.core.node.InvalidSettingsException;

/**
 * A class to collect multiple {@link InvalidSettingsException}s during settings
 * loading or validation, and throw a single {@link InvalidSettingsException}
 * combining their message when {@link #throwAll()} is called
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class InvalidSettingsExceptionCombiner {

	private final List<InvalidSettingsException> exceptions =
			new CopyOnWriteArrayList<>();

	/**
	 * Constructor
	 */
	public InvalidSettingsExceptionCombiner() {
	}

	/**
	 * Method to add an additional {@link InvalidSettingsException}
	 * 
	 * @param ise
	 *            The exception to add
	 */
	public void add(InvalidSettingsException ise) {
		if (!exceptions.contains(ise)) {
			exceptions.add(ise);
		}
	}

	/**
	 * Method to throw an {@link InvalidSettingsException} if any have been
	 * collected. If only one has been collected, that exception is re-thrown,
	 * otherwise a new exception combining all the exception messages is thrown
	 * 
	 * @throws InvalidSettingsException
	 *             if any exceptions have been collected
	 */
	public void throwAll() throws InvalidSettingsException {
		if (isEmpty()) {
			return;
		}
		if (isSingleton()) {
			throw exceptions.get(0);
		}
		throw new InvalidSettingsException("Multiple errors in settings:\n"
				+ exceptions.stream().map(e -> e.getMessage()).distinct()
						.collect(Collectors.joining("\n")));
	}

	/**
	 * @return whether the container contains only a single exception
	 */
	public boolean isSingleton() {
		return exceptions.size() == 1;
	}

	/**
	 * @return whether no exceptions have been collected
	 */
	public boolean isEmpty() {
		return exceptions.isEmpty();
	}
}
