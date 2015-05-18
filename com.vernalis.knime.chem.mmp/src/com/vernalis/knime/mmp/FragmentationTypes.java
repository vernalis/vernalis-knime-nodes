/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.mmp;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum containing the fragmentation patterns provided in the node dialog. The
 * {@link #getSMIRKS()} method provides the RDKit Reaction SMARTS string.
 * Attachment points should be included as '[*]' to allow correct tagging.
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public enum FragmentationTypes implements ButtonGroupEnumInterface {
	ALL_ACYCLIC("All acyclic single bonds",
			"Break all acyclic single bonds during fragmentation",
			"[*:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	RING_ACYCLIC("Only acyclic single bonds to rings",
			"Break only single acyclic bonds with at least one ring atom",
			"[*;R:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	HETATM_ACYCLIC("Only single bonds to a heteroatom",
			"Break only single acyclic bonds with at least one non-C atom",
			"[!#6:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	HUSSAIN_GSK("Non-functional group single bonds",
			"Break only single bonds which are not part of a functional group",
			"[#6+0;!$(*=,#[!#6]):1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	USER_DEFINED("User defined",
			"Enter a reaction SMARTS transformation below", null);

	private final String m_name;
	private final String m_tooltip;
	private final String m_SMIRKS;

	/**
	 * @param m_name
	 * @param m_tooltip
	 * @param m_SMIRKS
	 */
	private FragmentationTypes(String m_name, String m_tooltip, String m_SMIRKS) {
		this.m_name = m_name;
		this.m_tooltip = m_tooltip;
		this.m_SMIRKS = m_SMIRKS;
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

	/**
	 * @return The RDKit format Reaction SMARTS representing the transformation.
	 *         Attachment points should be included as '[*]' to allow correct
	 *         tagging.
	 */
	public String getSMIRKS() {
		return m_SMIRKS;
	}

	@Override
	public boolean isDefault() {
		return this.equals(FragmentationTypes.getDefaultMethod());
	}

	public static FragmentationTypes getDefaultMethod() {
		return ALL_ACYCLIC;
	}

	
}
