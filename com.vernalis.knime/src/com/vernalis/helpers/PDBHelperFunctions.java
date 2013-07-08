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
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

public class PDBHelperFunctions {
	/*
	 * Some helper functions for the pdb downloading and saving nodes
	 * 
	 * S.Roughley@vernalis.com
	 */
	public static Boolean checkContainerFolderExists (String PathToFile){
		/* 
		 * Checks whether the container folder for a full path to a file exists
		 */
		File f = new File (PathToFile);
		f = new File(f.getParent());
		return f.exists();
	}


	public static Boolean createContainerFolder (String PathToFile){
		/*
		 * Helper function to create the containing folder of a file
		 * in order to allow that file then to be written
		 */
		File f = new File (PathToFile);
		f = new File(f.getParent());
		return f.mkdirs();
	}

	public static boolean saveStringToPath (String PDBString, String PathToFile, Boolean Overwrite){
		/*
		 * Helper function to attempt to save out a string to a file
		 * identified by a second string.  Returns true or false depending
		 * on whether the file was successfully written.
		 * Assumes that the container folder exists.
		 * If overwrite is false the file will not be overwritten
		 * 
		 */
		File f = new File (PathToFile);
		Boolean r = false;
		if (Overwrite || !f.exists()){
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(PathToFile));
				//FileWriter always assumes default encoding is OK!
				output.write(PDBString);
				output.close();
				r=true;
			} catch (Exception e){
				e.printStackTrace();
				r=false;
			}
		}
		return r;
	}

	public static boolean isPath (String PathToFile){
		/*
		 * Helper function to check whether a string might reasonably be considered to be a path to a file
		 * musnt contain 'null' as this suggests comes from a missing value somewhere along the way
		 * musnt end with a \
		 * must start with either a driveletter:\ or \\servername\
		 */
		return (PathToFile.matches("\\\\\\\\[A-Za-z0-9]*.*\\\\.+[^\\\\]") || 
				PathToFile.matches("[A-Za-z]:\\\\.+[^\\\\]")) && !PathToFile.matches(".*null.*");
	}

	public static Double getResolution (String pdbtext){
		/*
		 * Helper function to retrieve resolution from REMARK 3
		 */
		try {
			return new Double(pdbtext.split("REMARK   3   RESOLUTION RANGE HIGH \\(ANGSTROMS\\) :")[1].split("\\n")[0].trim());
		} catch (Exception e){
			return null;
		}
	}

	public static Integer getNumModels (String pdbtext){
		/*
		 * Helper function to return Number of models from NUMMDL.  NB if this is not included we assume the answer is 1
		 * if there are ATOM or HETATM records
		 */
		try {
			return new Integer(pdbtext.split("NUMMDL ")[1].split("\\n")[0].trim());
		} catch (Exception e){
			return (pdbtext.split("ATOM   |HETATM ").length > 1) ? 1 : null;
		}
	}

	public static Double getR (String pdbtext){
		/*
		 * Helper function to retrieve R from REMARK 3
		 */
		try {
			return new Double(pdbtext.split("REMARK   3   R VALUE            \\(WORKING SET\\) :")[1].split("\\n")[0].trim());
		} catch (Exception e){
			return null;
		}
	}

	public static Double getRFree (String pdbtext){
		/*
		 * Helper function to retrieve RFree from REMARK 3
		 */
		try {
			return new Double(pdbtext.split("REMARK   3   FREE R VALUE                     :")[1].split("\\n")[0].trim());
		} catch (Exception e){
			return null;
		}
	}

	public static String getSpaceGroup (String pdbtext){
		/*
		 * Helper function to retrieve space group from CRYST1
		 */
		try{
			String r = pdbtext.split("CRYST1")[1].substring(49, 61).trim();
			return ("".equals(r))? null:r;
		}catch (Exception e){
			return null;
		}
	}

	public static String getPDBID (String pdbtext){
		/*
		 * Helper function to retrieve space group from HEADER
		 */
		try{
			return pdbtext.split("HEADER ")[1].substring(55, 59).trim();
		}catch (Exception e){
			return null;
		}
	}

	public static String getExpMethod (String pdbtext){
		/*
		 * Helper function to return an array of the Experimental methods
		 */
		try{
			return getMultiLineText(pdbtext, "EXPDTA", false);
		}catch (Exception e){
			return null;
		}
	}


	public static String getMultiLineText (String pdbtext, String RecordName, Boolean keepLineBreaks){
		/*
		 * Helper function to retrieve pure text from a multiline text field (e.g. TITLE)
		 * User to retrieve REMARKs 1-3 and TITLE
		 */
		String[] r = pdbtext.split("\\n");
		String s="";
		String jc = (keepLineBreaks) ? "\n" : " ";
		for (int i=0; i< r.length; i++){
			s = (r[i].matches(RecordName+".+")) ? s+r[i].substring(10, r[i].length()).trim()+ jc : s;
		}
		s = (keepLineBreaks) ? s : s.replace("  "," ");
		s = s.trim();
		return ("".equals(s))?null:s;
	}


	public static String createRCSBUrl(String pdbid, String filetype, Boolean gz){
		/*
		 * Helper function to create the correct download urls for the following file types
		 * FASTA Sequence
		 * PDB
		 * mmCIF
		 * StructureFactor
		 * PDBML/XML
		 * With optional use of gzip archive if appropriate
		 */
		if (pdbid==null){
			return null;
		}
		String URL = "http://www.rcsb.org/pdb/files/";

		//Deal with FASTA first as the format is different
		if (filetype.toUpperCase().equals("FASTA")){
			return URL + "fasta.txt?structureIdList=" + pdbid;
		}

		//Now deal with the others
		URL += pdbid;
		String temp = filetype.toLowerCase();
		
		if ("pdb".equals(temp)){
			URL += ".pdb";
		} else if ("mmcif".equals(temp) ||
				"cif".equals(temp)){
			URL += ".cif";
		} else if ("structurefactor".equals(temp) ||
				"sf".equals(temp)){
			URL += "-sf.cif";
		} else if ("pdbml".equals(temp) || 
				"pdbml/xml".equals(temp) ||
				"xml".equals(temp)){
			URL += ".xml";
		}
		return (gz) ? URL+".gz":URL;
	}

	public static String createRCSBUrl(String pdbid, String filetype){
		/* 
		 * if gz omitted default to 'true'
		 */
		return createRCSBUrl (pdbid, filetype,true);
	}

	public static String createRCSBUrl(String pdbid){
		/*
		 * if only pdb id supplier assume pdb filetype and gz
		 */
		return createRCSBUrl(pdbid, "pdb", true);
	}

	public static String readUrltoString (String urlToRetrieve){
		/*
		 * Return a string containing the contents a remote url.
		 * If the url ends '.gz' it is un-gzipped.
		 * Encoding is assumed to be UTF-8 unless shown otherwise
		 */
		if (urlToRetrieve==null){
			return null;
		}

		Boolean gz = urlToRetrieve.toLowerCase().endsWith(".gz");

		try {
			//Form a URL connection
			URL url = new URL(urlToRetrieve);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();

			// deflate, if necesarily
			if (gz) {
				is = new GZIPInputStream(is);
			}

			//Now detect encoding associated with the URL
			String contentType = uc.getContentType();

			//Default type is UTF-8
			String encoding = "UTF-8";
			if (contentType != null){
				int chsIndex = contentType.indexOf("charset=");
				if (chsIndex != -1){
					encoding = contentType.split("charset=")[1];
					if (encoding.indexOf(';')!= -1){
						encoding = encoding.split(";")[0];
					}
					encoding = encoding.trim();
				}
			}


			//Now set up a buffered reader to 
			BufferedReader in = new BufferedReader(new InputStreamReader(is , encoding));
			StringBuilder output = new StringBuilder();
			String str;
			boolean first = true;
			while ((str = in.readLine()) != null)
			{
				if (!first)
					output.append("\n");
				first = false;
				output.append(str);
			}
			in.close();
			str = output.toString();
			return ("".equals(str)) ? null:str;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

}
