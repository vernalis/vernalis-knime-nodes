/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc.blobs.nodes.base64;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum for the options of how a node should behave in the event of an error
 * converting to, from or between binary objects
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 1.38.0
 */
enum ConversionFailureBehaviour implements ButtonGroupEnumInterface {

	/** The node should fail */
	Fail,
	/**
	 * The node should return a missing value, and ideally include the error
	 * message and log it, and then continue
	 */
	Skip;

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return the default behaviour
	 *
	 * @since 1.38.0
	 */
	static ConversionFailureBehaviour getDefault() {
		return Fail;
	}
}
