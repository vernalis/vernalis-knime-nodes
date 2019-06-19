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

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * A {@link ButtonGroupEnumInterface} to provide file encoding options,
 * including 'guess'
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 * 
 */
public enum FileEncodingWithGuess implements ButtonGroupEnumInterface {
	GUESS("Guess", "Guess the encoding"),

	UTF8("UTF-8"),

	UTF16LE("UTF-16LE", "UTF-16 (Little Endian)"),

	UTF16BE("UTF-16BE", "UTF-16 (Big Endian)"),

	UTF32LE("UTF-32LE", "UTF-32 (Little Endian)"),

	UTF32BE("UTF-32BE", "UTF-32 (Big Endian)"),;

	private final String m_name;
	private final String m_tooltip;

	private FileEncodingWithGuess(String name, String tooltip) {
		m_name = name;
		m_tooltip = tooltip;
	}

	private FileEncodingWithGuess(String name) {
		m_name = name;
		m_tooltip = name;
	}

	@Override
	public String getText() {
		return m_name;
	}

	@Override
	public String getActionCommand() {
		return this.name();
	}

	@Override
	public String getToolTip() {
		return m_tooltip;
	}

	@Override
	public boolean isDefault() {

		return this.equals(getDefaultMethod());
	}

	/** @return The default method */
	public static FileEncodingWithGuess getDefaultMethod() {
		return GUESS;
	}
}
