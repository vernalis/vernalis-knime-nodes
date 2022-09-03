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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link UrlTemplateSuggester} which handled the RCSB suggestion encoding
 * ('+' encoding of spaces fails), and parses the JSON returned into individual
 * responses, removing the {@code <em>} and {@code </em>} tags
 *
 * Access is via the static {@link #get(String)} method which returns a
 * singleton instance for the URL
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class RcsbUrlSuggester extends UrlTemplateSuggester {

	private static class RcsbSuggesterSupplier {

		private static final RcsbSuggesterSupplier INSTANCE =
				new RcsbSuggesterSupplier();

		private final Map<String, RcsbUrlSuggester> suggestors =
				new HashMap<>();

		private RcsbSuggesterSupplier() {
		}

		public static RcsbSuggesterSupplier getInstance() {
			return INSTANCE;
		}

		public synchronized RcsbUrlSuggester get(String urlTemplate) {

			return suggestors.computeIfAbsent(urlTemplate,
					k -> new RcsbUrlSuggester(k));

		}

	}

	private static final Pattern patt = Pattern.compile("\\\"(.*?)\\\"");

	private RcsbUrlSuggester(String urlFormatString) {
		super(urlFormatString);
	}

	/**
	 * Method to get an effectively singleton instance of the suggester for the
	 * given urlformat
	 *
	 * @param urlFormatString
	 *            The format string for the URL - '%s' will be replaced with the
	 *            text value from the dialog
	 * 
	 * @return The {@link Suggester} for the URL. Instances are effective
	 *         singletons
	 */
	public static RcsbUrlSuggester get(String urlFormatString) {
		return RcsbSuggesterSupplier.getInstance().get(urlFormatString);
	}

	@Override
	public List<String> convertLineToSuggestions(String line) {
		final List<String> retVal = new ArrayList<>();
		final Matcher m = patt.matcher(line);
		while (m.find()) {
			retVal.add(m.group(1).replace("<em>", "").replace("</em>", ""));
		}
		return retVal;
	}

	@Override
	protected String preprocessQuery(String t)
			throws UnsupportedEncodingException {
		return super.preprocessQuery(t).replace("+", "%20");
	}

}
