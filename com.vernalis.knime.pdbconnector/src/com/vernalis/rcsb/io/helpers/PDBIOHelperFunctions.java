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
package com.vernalis.rcsb.io.helpers;

/**
 * Utility class providing helper functions for downloading PDB files.
 * 
 * @author Stephen Roughley <knime@vernalis.com>
 * 
 */
public class PDBIOHelperFunctions {

	/**
	 * Helper function to create the correct download urls for the following
	 * file types FASTA Sequence, PDB, mmCIF, StructureFactor, PDBML/XML, with
	 * optional use of gzip archive if appropriate
	 * 
	 * @param pdbid
	 *            The 4-character PDB structure ID
	 * @param filetype
	 *            The required filetype Valid options are FASTA, PDB, MMCIF,
	 *            CIF, STRUCTUREFACTOR, SF, PDBML, PDBML/XML, XML (case
	 *            insensitive)
	 * @param gz
	 *            If true, then use the gzipped format for download
	 * @return A valid url to the required file in the correct format for
	 *         downloading from the RCSB PDB
	 */
	public static String createRCSBUrl(String pdbid, String filetype, Boolean gz) {

		if (pdbid == null) {
			return null;
		}
		String URL = "http://www.rcsb.org/pdb/files/";

		// Deal with FASTA first as the format is different
		if (filetype.toUpperCase().equals("FASTA")) {
			return URL + "fasta.txt?structureIdList=" + pdbid;
		}

		// Now deal with the others
		URL += pdbid;
		String temp = filetype.toLowerCase();

		if ("pdb".equals(temp)) {
			URL += ".pdb";
		} else if ("mmcif".equals(temp) || "cif".equals(temp)) {
			URL += ".cif";
		} else if ("structurefactor".equals(temp) || "sf".equals(temp)) {
			URL += "-sf.cif";
		} else if ("pdbml".equals(temp) || "pdbml/xml".equals(temp) || "xml".equals(temp)) {
			URL += ".xml";
		}
		return (gz) ? URL + ".gz" : URL;
	}

	/**
	 * Helper function to create the correct download urls for the following
	 * file types FASTA Sequence, PDB, mmCIF, StructureFactor, PDBML/XML.
	 * gzipped archives are assumed
	 * 
	 * @param pdbid
	 *            The 4-character PDB structure ID
	 * @param filetype
	 *            The required filetype Valid options are FASTA, PDB, MMCIF,
	 *            CIF, STRUCTUREFACTOR, SF, PDBML, PDBML/XML, XML (case
	 *            insensitive)
	 * @return A valid url to the required file in the correct format for
	 *         downloading from the RCSB PDB
	 */
	public static String createRCSBUrl(String pdbid, String filetype) {
		return createRCSBUrl(pdbid, filetype, true);
	}

	/**
	 * Helper function to create the correct download urls. PDB filetype and
	 * gzipped archives are assumed
	 * 
	 * @param pdbid
	 *            The 4-character PDB structure ID
	 * @return A valid url to the required file in the correct format for
	 *         downloading from the RCSB PDB
	 */
	public static String createRCSBUrl(String pdbid) {

		return createRCSBUrl(pdbid, "pdb", true);
	}
}
