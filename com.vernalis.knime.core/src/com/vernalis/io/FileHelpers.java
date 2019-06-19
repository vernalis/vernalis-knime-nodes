/*******************************************************************************
 * Copyright (c) 2013, 2019, Vernalis (R&D) Ltd
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

package com.vernalis.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.core.util.Pair;
import org.knime.core.util.pathresolve.ResolverUtil;

/**
 * Utility class to provide basic file operations for a number of the Vernalis
 * KNIME nodes.
 * 
 * @author Stephen Roughley <knime@vernalis.com>
 * 
 */
public class FileHelpers {

	public static final int MAX_DOWNLOAD_ATTEMPTS = 5;

	/**
	 * The default encoding, assumed if no other encoding is able to be found
	 */
	public static final String DEFAULT_ENCODING = "UTF-8";

	private static final NodeLogger logger =
			NodeLogger.getLogger(FileHelpers.class);

	/**
	 * Utility function to attempt to co-erce a filepath to a valid URL
	 * 
	 * @param pathToFile
	 *            filepath or URL
	 * @return URL as a String
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static String forceURL(String pathToFile)
			throws MalformedURLException, IOException, URISyntaxException {
		/*
		 * Helper function to coerce filepaths to URLs NB There is no attempt to
		 * validate in anyway!
		 */
		String r = pathToFile;
		if (r.toLowerCase().startsWith("knime:")) {
			r = ResolverUtil.resolveURItoLocalFile(new URI(r)).toURI().toURL()
					.toString();
		} else if (!(r.toLowerCase().startsWith("http:")
				|| r.toLowerCase().startsWith("file:")
				|| r.toLowerCase().startsWith("ftp:")
				|| r.toLowerCase().startsWith("https:"))) {
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
	public static boolean checkContainerFolderExists(String PathToFile) {
		File f = new File(PathToFile);
		f = f.getParentFile();
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
	public static synchronized boolean createContainerFolder(
			String PathToFile) {
		File f = new File(PathToFile);
		f = f.getParentFile();
		if (f.exists()) {
			// It now exists, even if it didnt previously!
			return true;
		}
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
				BufferedWriter output =
						new BufferedWriter(new FileWriter(PathToFile));
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

		return (PathToFile.matches("\\\\\\\\[A-Za-z0-9]*.*\\\\.+[^\\\\]")
				|| PathToFile.matches("[A-Za-z]:\\\\.+[^\\\\]"))
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
	 * @throws FileDownloadException
	 *             If an exception was encountered downloading file
	 * @see #readURLToString(String, FileEncodingWithGuess)
	 */
	public static String readURLToString(String urlToRetrieve)
			throws FileDownloadException {
		return readURLToString(urlToRetrieve, FileEncodingWithGuess.GUESS);

	}

	/**
	 * Helper function to retrieve a file from a local or remote URL and return
	 * entire contents as a string. UTF-8 encoding is assumed by default. URLs
	 * ending '.gz' will be treated as gzipped and unzipped. In the event of an
	 * error, retries will be made until {@link #MAX_DOWNLOAD_ATTEMPTS} tries
	 * have failed
	 * 
	 * @param urlToRetrieve
	 *            A String containing the URL of the file of interest
	 * @param fileEncoding
	 *            The file encoding option
	 * @return A String containing the (unzipped if appropriate) content of the
	 *         file.
	 * @throws FileDownloadException
	 * @see #readURLToString(String)
	 */
	public static String readURLToString(String urlToRetrieve,
			FileEncodingWithGuess fileEncoding) throws FileDownloadException {
		Exception lastException = null;
		for (int i = 0; i < MAX_DOWNLOAD_ATTEMPTS; i++) {
			try {
				BufferedReader in =
						getReaderFromUrl(urlToRetrieve, fileEncoding);

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
				// Just try again
				lastException = e;
			}
		}
		if (lastException != null) {
			throw new FileDownloadException("Unable to download file after "
					+ MAX_DOWNLOAD_ATTEMPTS + " tries", lastException);
		}
		return null;
	}

	/**
	 * @param urlStr
	 *            The URL to open a {@link BufferedReader} on
	 * @param fileEncoding
	 *            The encoding policy - {@link FileEncodingWithGuess#GUESS} will
	 *            result in the URL header or content Byte Order Mark (BOM)
	 *            being used to guess, otherwise the encoding will be set to the
	 *            value specified here
	 * @return A {@link BufferedReader} for the supplied URL
	 * @throws MalformedURLException
	 *             If the URL was incorrectly formatted
	 * @throws IOException
	 *             If there was a problem reading the BOM from the
	 *             {@link InputStream}
	 * @throws UnsupportedEncodingException
	 *             If the guessed charset encoding is not supported within the
	 *             current JVM
	 */
	public static BufferedReader getReaderFromUrl(String urlStr,
			FileEncodingWithGuess fileEncoding) throws MalformedURLException,
			IOException, UnsupportedEncodingException {
		return getLengthedReaderFromUrl(urlStr, fileEncoding).getFirst();
	}

	/**
	 * Method to return the reader and the length in bytes of the content
	 * 
	 * @param urlStr
	 *            The URL to open a {@link BufferedReader} on
	 * @param fileEncoding
	 *            The encoding policy - {@link FileEncodingWithGuess#GUESS} will
	 *            result in the URL header or content Byte Order Mark (BOM)
	 *            being used to guess, otherwise the encoding will be set to the
	 *            value specified here
	 * @return A {@link BufferedReader} for the supplied URL
	 * @throws MalformedURLException
	 *             If the URL was incorrectly formatted
	 * @throws IOException
	 *             If there was a problem reading the BOM from the
	 *             {@link InputStream}
	 * @throws UnsupportedEncodingException
	 *             If the guessed charset encoding is not supported within the
	 *             current JVM
	 */
	public static Pair<BufferedReader, Long> getLengthedReaderFromUrl(
			String urlStr, FileEncodingWithGuess fileEncoding)
			throws MalformedURLException, IOException,
			UnsupportedEncodingException {
		// Form a URL connection
		URL url = new URL(urlStr);
		URLConnection uc = url.openConnection();
		InputStream is = uc.getInputStream();
		long length = uc.getContentLengthLong();

		// decompress, if necessary
		if (urlStr.endsWith(".gz") || urlStr.contains("&compressionType=gz")
				|| (uc.getHeaderField("Content-Disposition") != null
						&& uc.getHeaderField("Content-Disposition")
								.matches(".*[Ff]ilename=\\\".*?\\.gz\\\".*"))) {
			is = new GZIPInputStream(is);
		}

		String encoding = guessEncoding(fileEncoding, uc, is);

		// Now set up a buffered reader to read it
		BufferedReader in =
				new BufferedReader(new InputStreamReader(is, encoding));
		return new Pair<>(in, length);
	}

	/**
	 * Method to guess the encoding scheme according to the specified policy of
	 * a URL Connection
	 * 
	 * @param fileEncoding
	 *            The encoding policy - {@link FileEncodingWithGuess#GUESS} will
	 *            result in the URL header or content Byte Order Mark (BOM)
	 *            being used to guess, otherwise the encoding will be set to the
	 *            value specified here
	 * @param uc
	 *            The URL Connection
	 * @param is
	 *            The {@link InputStream} associated with the URL connection, in
	 *            case this is needed for the BOM
	 * @return The charset name for the encoding
	 * @throws IOException
	 *             In the event of an error reading the BOM
	 */
	public static String guessEncoding(FileEncodingWithGuess fileEncoding,
			URLConnection uc, InputStream is) throws IOException {
		// Now detect encoding associated with the URL
		String contentType = uc.getContentType();

		String encoding;
		if (fileEncoding == FileEncodingWithGuess.GUESS) {
			encoding = DEFAULT_ENCODING;

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
						+ " to file " + uc.getURL()
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
					// http://en.wikipedia.org/wiki/Byte_order_mark use >>>
					// for
					// last byte of UTF7
					if (foundEnc) {
						logger.info("Assigned charset encoding " + encoding
								+ " to file " + uc.getURL() + " based on BOM");
					} else {
						logger.warn("Unable to assign charset encoding to file "
								+ uc.getURL() + "; Using default (" + encoding
								+ ")");
					}
				} finally {
					is.reset();
				}
			}

		} else {
			// Use the user-specified value
			encoding = fileEncoding.getText();
		}
		return encoding;
	}

	public static final int MAX_READAHEAD_FOR_LINEBREAK = 65535;

	/**
	 * Method to determine the linebreak from a Reader. The method leaves the
	 * reader in the state it found it. If no linebreak is found in the first
	 * {@link #MAX_READAHEAD_FOR_LINEBREAK} characters, then the system default
	 * is used
	 * 
	 * @param rd
	 *            The reader
	 * @return The {@link LineBreak} system
	 * @throws IOException
	 */
	public static LineBreak getLineBreakFromReader(Reader rd)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		try {
			rd.mark(MAX_READAHEAD_FOR_LINEBREAK);
			boolean foundStart = false;
			for (int i = 0; i < MAX_READAHEAD_FOR_LINEBREAK; i++) {
				int next = rd.read();
				if (next < 0) {
					// EOF
					break;
				}
				char c = (char) next;
				if (c == '\n') {
					// '\n' ends the normal newline options ('\n' or '\r\n'), so
					// our job is done
					sb.append(c);
					break;
				} else if (foundStart) {
					// We had a stray '\r' - ditch it
					sb.deleteCharAt(0);
					foundStart = false;
				} else if (c == '\r') {
					foundStart = true;
					sb.append(c);
				}
			}
		} finally {
			rd.reset();
		}
		switch (sb.length()) {
		case 0:
			// Nothing found
			logger.info(
					"No linebreak found, using System default ("
							+ LineBreak.SYSTEM.getNewlineString()
									.replace("\n", "\\n").replace("\r", "\\r")
							+ ")");
			return LineBreak.SYSTEM;
		case 1:
			logger.debug("Using UNIX-Style linebreaks");
			return LineBreak.UNIX;
		case 2:
			logger.debug("Using Windows-style linebreaks");
			return LineBreak.WINDOWS;
		default:
			// Shouldnt ever be here!
			logger.info("Linebreak found has " + sb.length() + " characters ("
					+ sb.toString() + "), using System default");
			return LineBreak.getDefault();
		}
	}

	public enum LineBreak implements ButtonGroupEnumInterface {
		SYSTEM {

			@Override
			public String getNewlineString() {
				return System.lineSeparator();
			}
		},
		WINDOWS {

			@Override
			public String getNewlineString() {
				return "\r\n";
			}
		},
		UNIX {

			@Override
			public String getNewlineString() {
				return "\n";
			}
		},

		PRESERVE_INCOMING {

			@Override
			public String getNewlineString() {
				return null;
			}

		};

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getText()
		 */
		@Override
		public String getText() {
			return name().substring(0, 1)
					+ name().substring(1).toLowerCase().replace("_", " ")
					+ (getNewlineString() == null ? ""
							: " (" + getNewlineString().replace("\n", "\\n")
									.replace("\r", "\\r") + ")");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.knime.core.node.util.ButtonGroupEnumInterface#getActionCommand()
		 */
		@Override
		public String getActionCommand() {
			return name();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getToolTip()
		 */
		@Override
		public String getToolTip() {
			return getText();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.knime.core.node.util.ButtonGroupEnumInterface#isDefault()
		 */
		@Override
		public boolean isDefault() {
			return this == getDefault();
		}

		public abstract String getNewlineString();

		public static LineBreak getDefault() {
			return SYSTEM;
		}
	}
}
