/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Options for dealing with incoming explicit Hydrogens
 * 
 * @author s.roughley
 *
 */
public enum IncomingExplicitHsOption implements ButtonGroupEnumInterface {
	BEFORE(true, false),

	NOTHING {
		@Override
		public String getText() {
			return "Do Nothing";
		}

	},

	AFTER(false, true);

	private final boolean removeBefore, removeAfter;

	private IncomingExplicitHsOption(boolean removeBefore, boolean removeAfter) {
		this.removeBefore = removeBefore;
		this.removeAfter = removeAfter;
	}

	private IncomingExplicitHsOption() {
		this(false, false);
	}

	@Override
	public String getText() {
		return "Remove " + name().toLowerCase() + " fragmentation"
				+ (isDefault() ? " (Recommended)" : "");
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
	 * @return the default option
	 */
	public static IncomingExplicitHsOption getDefault() {
		return BEFORE;
	}

	/**
	 * @return should H's be removed before fragmentation
	 */
	public boolean getRemoveHsBeforeFragmentation() {
		return removeBefore;
	}

	/**
	 * @return should H's be removed after fragmentation
	 */
	public boolean getRemoveHsAfterFragmentation() {
		return removeAfter;
	}
}
