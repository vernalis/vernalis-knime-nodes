/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.switches.endifcase;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Policy controling behaviour of the End IF/Case node when multiple active
 * ports are present in the absence of a PortTypeCombiner for the port type
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
enum MultiActivesPolicy implements ButtonGroupEnumInterface {

	/** Use the first active input port */
	FIRST("Use first active branch"),
	/** Use the last active input port */
	LAST("Use last active branch"),
	/** Fail node execution */
	FAIL("Fail execution");

	private final String displayName;

	private MultiActivesPolicy(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getText() {
		return displayName;
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return getText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return the default value
	 */
	static MultiActivesPolicy getDefault() {
		return FAIL;
	}
}
