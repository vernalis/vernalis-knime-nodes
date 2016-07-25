/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.helpers;

/**
 * Utility class providing helper functions for processing PDB files.
 * 
 * @author Stephen Roughley <knime@vernalis.com>
 * 
 */
public class PDBHelperFunctions {

	/**
	 * Function to retrieve the Resolution from the REMARK 3 line of a PDB file
	 * supplied as a string
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The resolution of the structure
	 */
	public static Double getResolution(String pdbtext) {

		try {
			return new Double(
					pdbtext.split("REMARK   3   RESOLUTION RANGE HIGH \\(ANGSTROMS\\) :")[1]
							.split("\\n")[0].trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper function to return Number of models from NUMMDL. NB if this is not
	 * included we assume the answer is 1 if there are ATOM or HETATM records
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The number of models in the structure
	 */
	public static Integer getNumModels(String pdbtext) {

		try {
			return new Integer(pdbtext.split("NUMMDL ")[1].split("\\n")[0].trim());
		} catch (Exception e) {
			return (pdbtext.split("ATOM   |HETATM ").length > 1) ? 1 : null;
		}
	}

	/**
	 * Helper function to retrieve R from REMARK 3
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The R value
	 */
	public static Double getR(String pdbtext) {

		try {
			return new Double(
					pdbtext.split("REMARK   3   R VALUE            \\(WORKING SET\\) :")[1]
							.split("\\n")[0].trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper function to retrieve RFree from REMARK 3
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The RFree value
	 */
	public static Double getRFree(String pdbtext) {

		try {
			return new Double(pdbtext.split("REMARK   3   FREE R VALUE                     :")[1]
					.split("\\n")[0].trim());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper function to retrieve the crystallographic space group from the
	 * CRYST1 record
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The space group
	 */
	public static String getSpaceGroup(String pdbtext) {

		try {
			String r = pdbtext.split("CRYST1")[1].substring(49, 61).trim();
			return ("".equals(r)) ? null : r;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper function to retrieve the PDB ID from the HEADER of the PDB file
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The PDB ID
	 */
	public static String getPDBID(String pdbtext) {

		try {
			return pdbtext.split("HEADER ")[1].substring(55, 59).trim();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper function to return an array of the Experimental Methods found in
	 * the EXPDTA record
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @return The Experimental Method
	 */
	public static String getExpMethod(String pdbtext) {
		/*
		 * Helper function to return an array of the Experimental methods
		 */
		try {
			return getMultiLineText(pdbtext, "EXPDTA", false);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper function to retrieve pure text from a multi-line text record (e.g.
	 * TITLE) Used to retrieve REMARKs 1-3 and TITLE
	 * 
	 * @param pdbtext
	 *            The entire PDB file supplied as a string
	 * @param RecordName
	 *            The PDB record name e.g. TITLE
	 * @param keepLineBreaks
	 *            If true, linebreaks are kept, otherwise a space is inserted at
	 *            the end of the line before the next line is added
	 * @return A string containing the content of the multi-line text record
	 */
	public static String getMultiLineText(String pdbtext, String RecordName,
			Boolean keepLineBreaks) {
		/*
		
		 */
		String[] r = pdbtext.split("\\n");
		String s = "";
		String jc = (keepLineBreaks) ? "\n" : " ";
		for (int i = 0; i < r.length; i++) {
			s = (r[i].matches(RecordName + ".+"))
					? s + r[i].substring(10, r[i].length()).trim() + jc : s;
		}
		s = (keepLineBreaks) ? s : s.replace("  ", " ");
		s = s.trim();
		return ("".equals(s)) ? null : s;
	}
}
