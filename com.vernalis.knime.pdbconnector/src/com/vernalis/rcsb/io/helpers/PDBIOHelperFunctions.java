/*******************************************************************************
 * Copyright (c) 2013,2017, Vernalis (R&D) Ltd
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
		String URL = "http://www.rcsb.org/pdb/files/" + pdbid;

		switch (filetype.toLowerCase()) {

		case "fasta":
			return "http://www.rcsb.org/pdb/download/viewFastaFiles.do?structureIdList=" + pdbid
					+ (gz ? "&compressionType=gz" : "&compressionType=uncompressed");

		case "structurefactor":
		case "sf":
			URL = "https://files.rcsb.org/download/" + pdbid + "-sf.cif";
			break;
		case "pdb":
			URL += ".pdb";
			break;
		case "mmcif":
		case "cif":
			URL += ".cif";
			break;
		case "pdbml":
		case "pdbml/xml":
		case "xml":
			URL += ".xml";
			break;
		default:
			return null;
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
