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
 * {@link #getSMARTS()} method provides the RDKit Reaction SMARTS string.
 * Attachment points should be included as '[*]' to allow correct tagging.
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public enum FragmentationTypes implements ButtonGroupEnumInterface {
	ALL_ACYCLIC("All acyclic single bonds",
			"Break all acyclic single bonds during fragmentation",
			"[*:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	RING_ACYCLIC("Only acyclic single bonds to rings",
			"Break only single acyclic bonds with at least one ring atom",
			"[*;R:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	EXTENDED_RING_ACYCLIC(
			"Only acyclic single bonds to either rings or to double bonds exocyclic to rings",
			"Break only single acyclic bonds with at least one atom either in "
					+ "a ring or in a double bond, the other end of which is in a ring",
			"[*:1]!@!=!#[*;!R0,$(*=!@[*!R0]):2]>>[*:1]-[*].[*:2]-[*]"),

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
	private final String m_SMARTS;

	/**
	 * Constructor to set properties
	 * 
	 * @param name
	 *            The name
	 * @param tooltip
	 *            The tooltip
	 * @param rSMARTS
	 *            The rSMARTS
	 */
	private FragmentationTypes(String name, String tooltip, String rSMARTS) {
		this.m_name = name;
		this.m_tooltip = tooltip;
		this.m_SMARTS = rSMARTS;
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
	public String getSMARTS() {
		return m_SMARTS;
	}

	@Override
	public boolean isDefault() {
		return this.equals(FragmentationTypes.getDefaultMethod());
	}

	/**
	 * @return The default option
	 */
	public static FragmentationTypes getDefaultMethod() {
		return ALL_ACYCLIC;
	}

	/** @return Return the SMARTS for the bond match to be broken */
	public String getBondSMARTS() {
		if (m_SMARTS == null) {
			return null;
		}
		return m_SMARTS.split(">>")[0].replaceAll(".*\\](.*?)\\[.*", "$1");
	}

	/** @return Return the SMARTS for atom at the start of the bond to be broken */
	public String getFirstAtomTypeSMARTS() {
		if (m_SMARTS == null) {
			return null;
		}
		return m_SMARTS.split(">>")[0].replaceAll(".*?(\\[.*?\\]).*", "$1");
	}

	/** @return Return the SMARTS for atom at the end of the bond to be broken */
	public String getSecondAtomTypeSMARTS() {
		if (m_SMARTS == null) {
			return null;
		}
		return m_SMARTS.split(">>")[0].replaceAll(
				".*?\\[.*?\\].*?(\\[.*?\\]).*", "$1");
	}
}
