/*******************************************************************************
 * Copyright (c) 2014, 2017, Vernalis (R&D) Ltd
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
			"Any acyclic single bonds between any two atoms will be broken. This is the most exhaustive "
					+ "approach, which can generate a large number of pairs",
			"[*:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	RING_ACYCLIC("Only acyclic single bonds to rings",
			"Single acyclic bonds between any atoms will be broken, "
					+ "as long as at least one atom is in a ring",
			"[*;R:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	EXTENDED_RING_ACYCLIC(
			"Only acyclic single bonds to either rings or to double bonds exocyclic to rings",
			"Single acyclic bonds between any atoms will "
					+ "be broken, as long as 1 atom is either in a ring, or in a double "
					+ "bond exocyclic to a ring, with the other end in the ring",
			"[*:1]!@!=!#[*;!R0,$(*=!@[*!R0]):2]>>[*:1]-[*].[*:2]-[*]"),

	HETATM_ACYCLIC("Only single bonds to a heteroatom",
			"Single acyclic bonds between any two atoms, at least one of which "
					+ "is not Carbon will be broken. Included to mirror C-X bond breaking "
					+ "chemistry prevalent in modern drug discovery (e.g. SNAr, Reductive "
					+ "Aminations, Amide formations etc. See Ref. 2)",
			"[!#6:1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	HUSSAIN_GSK("Non-functional group single bonds",
			"This reproduces the fragmentation pattern used in the original "
					+ "Hussein/Rea paper (See footnote 24, Ref. 1), and also used in the "
					+ "RDKit Python implementation (Ref 3) ",
			"[#6+0;!$(*=,#[!#6]):1]!@!=!#[*:2]>>[*:1]-[*].[*:2]-[*]"),

	MATSY("Matsy (One atom in ring, or a non-sp2 C atom bonded to a non-C atom)",
			"This reproduces the fragmentation pattern used by NextMove's 'Matsy', i.e. "
					+ "single acyclic bonds between either a ring atom and any other atom, "
					+ "or a heteroatom bonded to a non-sp2 C atom, as described in the "
					+ "Matched Series paper (Ref 4)",
			"[$([#6!^2]-!@[!#6]),$([*;R]-!@[*]):1]-!@[$([!#6]-!@[#6!^2]),$([*]-!@[*;R]):2]>>"
					+ "[*:1]-[*].[*:2]-[*]"),

	PEPTIDE_SIDECHAIN("Peptide Sidechains",
			"Acyclic single bonds from C\u03B1 to C\u03B2 will be broken.  "
					+ "C-H will only be broken for Glycine, and only when explicit H are present "
					+ "(both CH bonds will be broken in this case)",
			"[C;$(CC(=O)[O,N]);$(CN):1]-!@"
					+ "[$([C]-!@C(C(=O)[N,O])N),$([#1]-!@[CH2](C(=O)[N,O])N):2]>>"
					+ "[*:1]-[*].[*:2]-[*]"),

	NUCLEIC_ACID_SIDECHAIN("Nucleic Acid Sidechains",
			"Acyclic single bonds in the anomeric position between the aromatic base N and "
					+ "sugar will be broken. The minimum requirement is N(Ar)CO(CO)CO to "
					+ "allow for open chain analogues",
			"[n:1]-!@[$(COC(CO)CO):2]>>[*:1]-[*].[*:2]-[*]"),

	USER_DEFINED("User defined", "The user needs to provide their own (r)SMARTS "
			+ "fragmentation definition, following the guidelines below", null);

	private final String m_name;
	private final String m_tooltip;
	private final String m_SMARTS;

	private FragmentationTypes(String name, String tooltip) {
		this(name, tooltip, null);
	}

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

	/**
	 * @return Return the SMARTS for atom at the start of the bond to be broken
	 */
	public String getFirstAtomTypeSMARTS() {
		if (m_SMARTS == null) {
			return null;
		}
		return m_SMARTS.split(">>")[0].replaceAll(".*?(\\[.*?\\]).*", "$1");
	}

	/**
	 * @return Return the SMARTS for atom at the end of the bond to be broken
	 */
	public String getSecondAtomTypeSMARTS() {
		if (m_SMARTS == null) {
			return null;
		}
		return m_SMARTS.split(">>")[0].replaceAll(".*?\\[.*?\\].*?(\\[.*?\\]).*", "$1");
	}
}
