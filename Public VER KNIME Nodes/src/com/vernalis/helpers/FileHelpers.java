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

public class FileHelpers {
	/*
	 * Some helper functions for file-related operations.
	 * N.B. Some of these duplicate PDBHelperFunctions which
	 * are deprecated but which are maintained for backwards
	 * compatibility
	 */
	public static String forceURL (String pathToFile){
		/*
		 * Helper function to coerce filepaths to URLs
		 * NB There is no attempt to validate in anyway!
		 */
		String r = pathToFile;
		if (!(r.toLowerCase().startsWith("http:") || r.toLowerCase().startsWith("file:")
				|| r.toLowerCase().startsWith("ftp:"))){
			try {
				r= new File(r).toURI().toURL().toString();
			}catch (Exception e){
				//do nothing
			}
		}
		return r;
	}
	
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
		 * musn't contain 'null' as this suggests comes from a missing value somewhere along the way
		 * musn't end with a \
		 * must start with either a driveletter:\ or \\servername\
		 * NB This is for pathnames, not URLs
		 */
		return (PathToFile.matches("\\\\\\\\[A-Za-z0-9]*.*\\\\.+[^\\\\]") || 
				PathToFile.matches("[A-Za-z]:\\\\.+[^\\\\]")) && !PathToFile.matches(".*null.*");
	}
	
	public static String readURLToString (String urlToRetrieve){
		/*
		 * Helper function to retrieve a file from a local or remote url and return entire
		 * contents as a string
		 */
		try {
			//Form a URL connection
			URL url = new URL(urlToRetrieve);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();
			
    		// decompress, if necessary
    		if (urlToRetrieve.endsWith(".gz")) {
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

			//Now set up a buffered reader to read it
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
			
			//Return the result as a string
			return output.toString();
		} catch (Exception e){
			//e.printStackTrace();
			return null;
		} 
	}
}
