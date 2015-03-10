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

import org.knime.core.node.NodeLogger;

import com.vernalis.nodes.io.txt.FileEncodingWithGuess;

/**
 * Utility class to provide basic file operations for a number of the Vernalis
 * KNIME nodes. N.B. Some of these duplicate PDBHelperFunctions which are
 * deprecated
 * 
 * @author Stephen Roughley <knime@vernalis.com>
 * 
 */
public class FileHelpers {

	/**
	 * The default encoding, assumed if no other encoding is able to be found
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Utility function to attempt to co-erce a filepath to a valid URL
	 * 
	 * @param pathToFile
	 *            filepath or URL
	 * @return URL as a String
	 */
	public static String forceURL(String pathToFile) {
		/*
		 * Helper function to coerce filepaths to URLs NB There is no attempt to
		 * validate in anyway!
		 */
		String r = pathToFile;
		if (!(r.toLowerCase().startsWith("http:")
				|| r.toLowerCase().startsWith("file:")
				|| r.toLowerCase().startsWith("ftp:") || r.toLowerCase()
				.startsWith("knime:"))) {
			try {
				r = new File(r).toURI().toURL().toString();
			} catch (Exception e) {
				// do nothing
			}
		}
		return r;
	}

	/**
	 * Utility function to check whether the container folder exists for a given
	 * filepath
	 * 
	 * @param PathToFile
	 *            Filepath of the file to test
	 * @return True if the container folder exists
	 */
	public static Boolean checkContainerFolderExists(String PathToFile) {
		File f = new File(PathToFile);
		f = new File(f.getParent());
		return f.exists();
	}

	/**
	 * Utility function to create the container folder for a given filepath
	 * 
	 * @param PathToFile
	 *            Filepath of the file (NB This is the path to the file, not the
	 *            folder being created)
	 * @return True if the folder was successfully created (NB will return False
	 *         if already exists)
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
	 *            The String to write to the file
	 * @param PathToFile
	 *            The full path to the file
	 * @param Overwrite
	 *            If True, existing files will be overwritten
	 * @return True if the file was successfully written
	 */
	public static boolean saveStringToPath(String PDBString, String PathToFile,
			Boolean Overwrite) {

		File f = new File(PathToFile);
		Boolean r = false;
		if (Overwrite || !f.exists()) {
			try {
				BufferedWriter output = new BufferedWriter(new FileWriter(
						PathToFile));
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
	 * to be a path to a file musn't contain 'null' as this suggests the path
	 * comes from a missing value somewhere along the way, musn't end with a \,
	 * and must start with either a driveletter:\ or \\servername\.
	 * 
	 * The path is checked against the regular expressions
	 * "\\\\\\\\[A-Za-z0-9]*.*\\\\.+[^\\\\]" and "[A-Za-z]:\\\\.+[^\\\\]"
	 * 
	 * NB This is for pathnames, not URLs
	 * 
	 * @param PathToFile
	 *            The filepath to test (NB Not a URL)
	 * @return True If the path matches either of the given regular expressions
	 *         and does not contain 'null'
	 */
	public static boolean isPath(String PathToFile) {

		return (PathToFile.matches("\\\\\\\\[A-Za-z0-9]*.*\\\\.+[^\\\\]") || PathToFile
				.matches("[A-Za-z]:\\\\.+[^\\\\]"))
				&& !PathToFile.matches(".*null.*");
	}

	/**
	 * Helper function to retrieve a file from a local or remote URL and return
	 * entire contents as a string. UTF-8 encoding is assumed by default. URLs
	 * ending '.gz' will be treated as gzipped and unzipped.
	 * 
	 * @param urlToRetrieve
	 *            A String containing the URL of the file of interest
	 * @return A String containing the (unzipped if appropriate) content of the
	 *         file.
	 * @see #readURLToString(String, FileEncodingWithGuess)
	 */
	public static String readURLToString(String urlToRetrieve) {
		NodeLogger logger = NodeLogger.getLogger(FileHelpers.class);
		try {
			// Form a URL connection
			URL url = new URL(urlToRetrieve);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();

			// decompress, if necessary
			if (urlToRetrieve.endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}

			// Now detect encoding associated with the URL
			String contentType = uc.getContentType();

			// Default type is UTF-8
			String encoding = DEFAULT_ENCODING;
			if (contentType != null && !contentType.equals("content/unknown")) {
				int chsIndex = contentType.indexOf("charset=");
				if (chsIndex != -1) {
					encoding = contentType.split("charset=")[1];
					if (encoding.indexOf(';') != -1) {
						encoding = encoding.split(";")[0];
					}
					encoding = encoding.trim();
				}
				logger.info("Assigned charset encoding " + encoding
						+ " to file " + urlToRetrieve
						+ " based on URL Connection meta-info");
			} else {
				// The URL connection didnt provide an encoding, so let's try
				// the first 4 (BOM) chars
				is.mark(4);
				try {

					byte[] buffer = new byte[4];
					is.read(buffer);
					boolean foundEnc = false;
					if (buffer[0] == (byte) 0xEF && buffer[1] == (byte) 0xBB
							&& buffer[2] == (byte) 0xBF) {
						encoding = "UTF-8";
						foundEnc = true;
					} else if (buffer[0] == (byte) 0xFE
							&& buffer[1] == (byte) 0xFF) {
						encoding = "UTF-16BE";
						foundEnc = true;
					} else if (buffer[0] == (byte) 0xFF
							&& buffer[1] == (byte) 0xFE) {
						encoding = "UTF-16LE";
						foundEnc = true;
					} else if (buffer[0] == (byte) 0x00
							&& buffer[1] == (byte) 0x00
							&& buffer[2] == (byte) 0xFE
							&& buffer[3] == (byte) 0xFF) {
						encoding = "UTF-32BE";
						foundEnc = true;
					} else if (buffer[0] == (byte) 0xFF
							&& buffer[1] == (byte) 0xFE
							&& buffer[2] == (byte) 0x00
							&& buffer[3] == (byte) 0x00) {
						encoding = "UTF-32LE";
						foundEnc = true;
					}
					// TODO:Add others here from e.g.
					// http://en.wikipedia.org/wiki/Byte_order_mark use >>> for
					// last byte of UTF7
					if (foundEnc) {
						logger.info("Assigned charset encoding " + encoding
								+ " to file " + urlToRetrieve + " based on BOM");
					} else {
						logger.warn("Unable to assign charset encoding to file "
								+ urlToRetrieve
								+ "; Using default ("
								+ encoding + ")");
					}
				} finally {
					is.reset();
				}

			}

			// Now set up a buffered reader to read it
			BufferedReader in = new BufferedReader(new InputStreamReader(is,
					encoding));

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

			// Return the result as a string
			return output.toString();
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * Helper function to retrieve a file from a local or remote URL and return
	 * entire contents as a string. UTF-8 encoding is assumed by default. URLs
	 * ending '.gz' will be treated as gzipped and unzipped.
	 * 
	 * @param urlToRetrieve
	 *            A String containing the URL of the file of interest
	 * @param fileEncoding
	 *            The file encoding option
	 * @return A String containing the (unzipped if appropriate) content of the
	 *         file.
	 * @see #readURLToString(String)
	 */
	public static String readURLToString(String urlToRetrieve,
			FileEncodingWithGuess fileEncoding) {
		NodeLogger logger = NodeLogger.getLogger(FileHelpers.class);
		try {
			// Form a URL connection
			URL url = new URL(urlToRetrieve);
			URLConnection uc = url.openConnection();
			InputStream is = uc.getInputStream();

			// decompress, if necessary
			if (urlToRetrieve.endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}

			// Now detect encoding associated with the URL
			String contentType = uc.getContentType();

			String encoding;
			if (fileEncoding == FileEncodingWithGuess.GUESS) {
				encoding = DEFAULT_ENCODING;

				if (contentType != null
						&& !contentType.equals("content/unknown")) {
					int chsIndex = contentType.indexOf("charset=");
					if (chsIndex != -1) {
						encoding = contentType.split("charset=")[1];
						if (encoding.indexOf(';') != -1) {
							encoding = encoding.split(";")[0];
						}
						encoding = encoding.trim();
					}
					logger.info("Assigned charset encoding " + encoding
							+ " to file " + urlToRetrieve
							+ " based on URL Connection meta-info");
				} else {
					// The URL connection didnt provide an encoding, so let's
					// try
					// the first 4 (BOM) chars
					is.mark(4);
					try {

						byte[] buffer = new byte[4];
						is.read(buffer);
						boolean foundEnc = false;
						if (buffer[0] == (byte) 0xEF
								&& buffer[1] == (byte) 0xBB
								&& buffer[2] == (byte) 0xBF) {
							encoding = "UTF-8";
							foundEnc = true;
						} else if (buffer[0] == (byte) 0xFE
								&& buffer[1] == (byte) 0xFF) {
							encoding = "UTF-16BE";
							foundEnc = true;
						} else if (buffer[0] == (byte) 0xFF
								&& buffer[1] == (byte) 0xFE) {
							encoding = "UTF-16LE";
							foundEnc = true;
						} else if (buffer[0] == (byte) 0x00
								&& buffer[1] == (byte) 0x00
								&& buffer[2] == (byte) 0xFE
								&& buffer[3] == (byte) 0xFF) {
							encoding = "UTF-32BE";
							foundEnc = true;
						} else if (buffer[0] == (byte) 0xFF
								&& buffer[1] == (byte) 0xFE
								&& buffer[2] == (byte) 0x00
								&& buffer[3] == (byte) 0x00) {
							encoding = "UTF-32LE";
							foundEnc = true;
						}
						// TODO:Add others here from e.g.
						// http://en.wikipedia.org/wiki/Byte_order_mark use >>>
						// for
						// last byte of UTF7
						if (foundEnc) {
							logger.info("Assigned charset encoding " + encoding
									+ " to file " + urlToRetrieve
									+ " based on BOM");
						} else {
							logger.warn("Unable to assign charset encoding to file "
									+ urlToRetrieve
									+ "; Using default ("
									+ encoding + ")");
						}
					} finally {
						is.reset();
					}
				}

			} else {
				// Use the user-specified value
				encoding = fileEncoding.getText();
			}

			// Now set up a buffered reader to read it
			BufferedReader in = new BufferedReader(new InputStreamReader(is,
					encoding));

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

			// Return the result as a string
			return output.toString();
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}
}
