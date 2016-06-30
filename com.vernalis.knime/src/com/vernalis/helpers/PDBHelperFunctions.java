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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.knime.core.node.NodeLogger;

/**
 * Utility class providing helper functions for downloading and processing PDB
 * files. NB Some methods are duplicated in the newer
 * {@link com.vernalis.helpers.FileHelpers} class, which should be used in
 * future implementations
 * 
 * @author Stephen Roughley <knime@vernalis.com>
 * 
 */
public class PDBHelperFunctions {

	public static final int MAX_DOWNLOAD_ATTEMPTS = 5;

	/**
	 * Checks whether the container folder for a full path to a file exists
	 * 
	 * @param PathToFile
	 *            The path to the file to check
	 * @return True if the folder exists
	 */
	public static Boolean checkContainerFolderExists(String PathToFile) {

		File f = new File(PathToFile);
		f = new File(f.getParent());
		return f.exists();
	}

	/**
	 * Helper function to create the containing folder of a file in order to
	 * allow that file then to be written. NB contains the path to the file, not
	 * to the created folder
	 * 
	 * @param PathToFile
	 *            The path to the file
	 * @return True if folder created (NB will be False if the folder already
	 *         exists)
	 */
	public static Boolean createContainerFolder(String PathToFile) {

		File f = new File(PathToFile);
		f = new File(f.getParent());
		return f.mkdirs();
	}

	/**
	 * Helper function to attempt to save out a string to a file identified by a
	 * second string. Returns true or false depending on whether the file was
	 * successfully written. Assumes that the container folder exists. If
	 * overwrite is false the file will not be overwritten
	 * 
	 * @param PDBString
	 *            The string to write to the new file
	 * @param PathToFile
	 *            The path of the file to write the string to
	 * @param Overwrite
	 *            If True, an existing file will be overwritten
	 * @return True if the file was successfully written
	 */
	public static boolean saveStringToPath(String PDBString, String PathToFile, Boolean Overwrite) {

		File f = new File(PathToFile);
		Boolean r = false;
		if (Overwrite || !f.exists()) {
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(PathToFile));
				// FileWriter always assumes default encoding is OK!
				output.write(PDBString);
				output.close();
				r = true;
			} catch (Exception e) {
				e.printStackTrace();
				r = false;
			}
		}
		return r;
	}

	/**
	 * Helper function to check whether a string might reasonably be considered
	 * to be a path to a file musnt contain 'null' as this suggests comes from a
	 * missing value somewhere along the way musnt end with a \ must start with
	 * either a driveletter:\ or \\servername\
	 * 
	 * @param PathToFile
	 *            The path to check
	 * @return True if the path looks to be a valid path
	 */
	public static boolean isPath(String PathToFile) {

		return (PathToFile.matches("\\\\\\\\[A-Za-z0-9]*.*\\\\.+[^\\\\]")
				|| PathToFile.matches("[A-Za-z]:\\\\.+[^\\\\]")) && !PathToFile.matches(".*null.*");
	}

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

	/**
	 * Return a string containing the contents a remote url. If the url ends
	 * '.gz' it is un-gzipped. Encoding is assumed to be UTF-8 unless shown
	 * otherwise
	 * 
	 * @param urlToRetrieve
	 *            The url of the file to fetch
	 * @return A String containing the (decompressed if appropriate) file
	 *         contents
	 * @throws PdbDownloadException
	 *             If unable to download
	 */
	public static String readUrltoString(String urlToRetrieve) throws PdbDownloadException {

		if (urlToRetrieve == null) {
			return null;
		}

		Boolean gz = urlToRetrieve.toLowerCase().endsWith(".gz");

		for (int i = 0; i < MAX_DOWNLOAD_ATTEMPTS; i++) {
			try {
				// Form a URL connection
				URL url = new URL(urlToRetrieve);
				URLConnection uc = url.openConnection();
				InputStream is = uc.getInputStream();

				// deflate, if necesarily
				if (gz) {
					is = new GZIPInputStream(is);
				}

				// Now detect encoding associated with the URL
				String contentType = uc.getContentType();

				// Default type is UTF-8
				String encoding = "UTF-8";
				if (contentType != null) {
					int chsIndex = contentType.indexOf("charset=");
					if (chsIndex != -1) {
						encoding = contentType.split("charset=")[1];
						if (encoding.indexOf(';') != -1) {
							encoding = encoding.split(";")[0];
						}
						encoding = encoding.trim();
					}
				}

				// Now set up a buffered reader to
				BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));
				StringBuilder output = new StringBuilder();
				String str;
				boolean first = true;
				while ((str = in.readLine()) != null) {
					if (!first)
						output.append("\n");
					first = false;
					output.append(str);
				}
				in.close();
				str = output.toString();
				return ("".equals(str)) ? null : str;
			} catch (ConnectException e) {
				NodeLogger.getLogger("PDB Downloader").info("Connection failed; "
						+ (MAX_DOWNLOAD_ATTEMPTS - i - 1) + " attempts remaining...");
			} catch (Exception e) {
				throw new PdbDownloadException("Problem downloading pdb file: " + e.getMessage(),
						e);
			}
		}

		throw new PdbDownloadException(
				"Unable to download file after " + MAX_DOWNLOAD_ATTEMPTS + " tries");
	}
}
