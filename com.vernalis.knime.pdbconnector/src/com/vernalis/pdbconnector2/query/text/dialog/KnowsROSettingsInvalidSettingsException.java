/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query.text.dialog;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

/**
 * An {@link InvalidSettingsException} subclass to be used when the calling
 * method needs to know the {@link NodeSettingsRO} object causing the exception
 * to be thrown
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.3
 */
@SuppressWarnings("serial")
public class KnowsROSettingsInvalidSettingsException
		extends InvalidSettingsException {

	private final NodeSettingsRO brokenSettings;

	/**
	 * Full constructor
	 * 
	 * @param msg
	 *            The error message
	 * @param cause
	 *            The causing exception
	 * @param brokenSettings
	 *            The broken settings object
	 */
	public KnowsROSettingsInvalidSettingsException(String msg, Throwable cause,
			NodeSettingsRO brokenSettings) {
		super(msg, cause);
		this.brokenSettings = brokenSettings;
	}

	/**
	 * No cause Constructor
	 * 
	 * @param msg
	 *            The error message
	 * @param brokenSettings
	 *            The broken settings object
	 */
	public KnowsROSettingsInvalidSettingsException(String msg,
			NodeSettingsRO brokenSettings) {
		super(msg);
		this.brokenSettings = brokenSettings;
	}

	/**
	 * No description constructor
	 * 
	 * @param cause
	 *            The causing exception
	 * @param brokenSettings
	 *            The broken settings object
	 */
	public KnowsROSettingsInvalidSettingsException(Throwable cause,
			NodeSettingsRO brokenSettings) {
		super(cause);
		this.brokenSettings = brokenSettings;
	}

	/**
	 * @return The causing NodeSettingsRO object
	 */
	public NodeSettingsRO getBrokenSettings() {
		return brokenSettings;
	}

}
