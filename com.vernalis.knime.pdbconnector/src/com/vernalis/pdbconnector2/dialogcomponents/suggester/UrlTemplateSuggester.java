/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.dialogcomponents.suggester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * A {@link Suggester} implementation which retrieves suggestions based on a URL
 * template. The URL template needs a single '%s' format code for the place in
 * the string to insert the query text. By default, the query is formatted via
 * {@link URLEncoder#encode(String, String)} with the supplied default query
 * encoding (UTF-8, or as specified in a constructor). This behaviour can be
 * modified by overriding {@link #preprocessQuery(String)}. This default
 * implementation assumes each line returned from the URL is a single
 * suggestion. To change this, override
 * {@link #convertLineToSuggestions(String)}
 *
 * Non-200 response codes will throw an IOException with the response code /
 * message, which will be handled in the {@link #handleException(Exception)}
 * method (the default is to ignore the exception completely) In the event of an
 * exception, an empty list is return as the {@link Suggester} must return to
 * allow dialogs to remain functioning
 *
 * Suggestions are stored in a cache to minimise repeated web calls
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class UrlTemplateSuggester implements Suggester {

	private static final String DEFAULT_ENCODING = "utf-8";
	/** The cache size */
	protected static final int CACHE_SIZE = 250;
	private final String urlFormatString, defaultResponseEncoding,
			defaultQueryEncoding;

	@SuppressWarnings("serial")
	private final LinkedHashMap<String, List<String>> cache =
			new LinkedHashMap<String, List<String>>(32, 0.75f, true) {

				@Override
				protected boolean
						removeEldestEntry(Entry<String, List<String>> eldest) {
					return size() > CACHE_SIZE;
				}

			};

	/**
	 * Constructor with default query and response encoding
	 *
	 * @param urlFormatString
	 *            The URL format string (see {@link UrlTemplateSuggester} for
	 *            details)
	 */
	public UrlTemplateSuggester(String urlFormatString) {
		this(urlFormatString, DEFAULT_ENCODING);
	}

	/**
	 * Constructor with default query encoding
	 *
	 * @param urlFormatString
	 *            The URL format string (see {@link UrlTemplateSuggester} for
	 *            details)
	 * @param defaultResponseEncoding
	 *            The default response encoding to use if the URL connection
	 *            does not specify anything else
	 */
	public UrlTemplateSuggester(String urlFormatString,
			String defaultResponseEncoding) {
		this(urlFormatString, defaultResponseEncoding, DEFAULT_ENCODING);
	}

	/**
	 * Constructor
	 *
	 * @param urlFormatString
	 *            The URL format string (see {@link UrlTemplateSuggester} for
	 *            details)
	 * @param defaultResponseEncoding
	 *            The default response encoding to use if the URL connection
	 *            does not specify anything else
	 * @param defaultQueryEncoding
	 *            The charset encoding for the String pre-processing
	 */
	public UrlTemplateSuggester(String urlFormatString,
			String defaultResponseEncoding, String defaultQueryEncoding) {
		this.urlFormatString = urlFormatString;
		this.defaultResponseEncoding = defaultResponseEncoding;
		this.defaultQueryEncoding = defaultQueryEncoding;
	}

	/**
	 * @return the format string for the URL to call for suggestions
	 */
	protected final String getUrlFormatString() {
		return urlFormatString;
	}

	/**
	 * @return The default response encoding
	 */
	protected final String getDefaultResponseEncoding() {
		return defaultResponseEncoding;
	}

	/**
	 * @return The default query encoding
	 */
	protected final String getDefaultQueryEncoding() {
		return defaultQueryEncoding;
	}

	@Override
	public List<String> suggest(String t) {

		try {
			final String t0 = preprocessQuery(t);
			if (!cache.containsKey(t0)) {
				final List<String> retVal = new ArrayList<>();
				final URL url =
						new URL(String.format(getUrlFormatString(), t0));
				final HttpURLConnection conn =
						(HttpURLConnection) url.openConnection();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					String enc = getDefaultResponseEncoding();
					if (conn.getContentEncoding() != null
							&& !conn.getContentEncoding().isEmpty()) {
						enc = conn.getContentEncoding();
					}
					try (BufferedReader br =
							new BufferedReader(new InputStreamReader(
									conn.getInputStream(), enc))) {
						String line;
						while ((line = br.readLine()) != null) {
							retVal.addAll(convertLineToSuggestions(line));
						}
					}
				} else {
					throw new IOException("Unable to open connection - "
							+ conn.getResponseCode() + ": "
							+ conn.getResponseMessage());
				}
				cache.put(t0, retVal);
			}
			return cache.get(t0);
		} catch (final Exception e) {
			handleException(e);
			return Collections.emptyList();
		}

	}

	/**
	 * This method should handle any exceptions resulting from the creation and
	 * calling of the URL, e.g logging or displaying a message somewhere in the
	 * dialog. The default implementation silently swallows the exception.
	 * Ultimately, the call to {@link #suggest(String)} will still return an
	 * empty List so that the UI continues
	 *
	 * @param e
	 *            The exception thrown
	 */
	protected void handleException(Exception e) {
		// Do nothing

	}

	/**
	 * Method to pre-process the query prior to inserting into the URL template
	 *
	 * @param t
	 *            The string to base suggestions on
	 * @return The pre-processed string. The default calls
	 *         {@link URLEncoder#encode(String,String)} with the encoding
	 *         specifed by {@link #getDefaultQueryEncoding()}
	 * @throws UnsupportedEncodingException
	 *             it the encoding was not recognised
	 */
	protected String preprocessQuery(String t)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(t, getDefaultQueryEncoding());
	}

	/**
	 * Method to convert a single line from the response to suggesion(s)
	 *
	 * @param line
	 *            The line from the URL response
	 * @return A list of suggestions which will be added to the overall
	 *         suggestion list. The default simply wraps the line in a singleton
	 *         list
	 */
	protected List<String> convertLineToSuggestions(String line) {
		return Collections.singletonList(line);
	}

}
