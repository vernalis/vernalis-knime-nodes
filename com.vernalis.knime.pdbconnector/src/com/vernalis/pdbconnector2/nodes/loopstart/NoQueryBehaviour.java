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
package com.vernalis.pdbconnector2.nodes.loopstart;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum presenting the options when no valid query is found in the XML column
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
enum NoQueryBehaviour implements ButtonGroupEnumInterface {
	/**
	 * Skip the row
	 */
	Skip,

	/**
	 * Fail node execution
	 */
	Fail;

	@Override
	public String getText() {
		return name().replace('_', ' ');
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
	 * @return The default behaviour
	 */
	public static NoQueryBehaviour getDefault() {
		return Skip;
	}
}
