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
package com.vernalis.pdbconnector2;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides a simple mechanism to HTML-escape and unescape named
 * entity characters upto HTML 4
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 */
public final class HtmlEncoder {

	private static final Map<String, Character> namedEntityLookup =
			new HashMap<>();
	private static final Map<Character, String> charToEntityLookupName =
			new HashMap<>();
	static {

		// HTML1.1
		namedEntityLookup.put("amp", '&');
		namedEntityLookup.put("lt", '<');
		namedEntityLookup.put("gt", '>');
		// namedEntityLookup.put("apos", '>'); //XML only

		// HTML2.0
		namedEntityLookup.put("quot", '"');
		namedEntityLookup.put("Agrave", '\u00c0');
		namedEntityLookup.put("Aacute", '\u00c1');
		namedEntityLookup.put("Acirc", '\u00c2');
		namedEntityLookup.put("Atilde", '\u00c3');
		namedEntityLookup.put("Auml", '\u00c4');
		namedEntityLookup.put("Aring", '\u00c5');
		namedEntityLookup.put("AElig", '\u00c6');
		namedEntityLookup.put("Ccedil", '\u00c7');
		namedEntityLookup.put("Egrave", '\u00c8');
		namedEntityLookup.put("Eacute", '\u00c9');
		namedEntityLookup.put("Ecirc", '\u00ca');
		namedEntityLookup.put("Euml", '\u00cb');
		namedEntityLookup.put("Igrave", '\u00cc');
		namedEntityLookup.put("Iacute", '\u00cd');
		namedEntityLookup.put("Icirc", '\u00ce');
		namedEntityLookup.put("Iuml", '\u00cf');
		namedEntityLookup.put("ETH", '\u00d0');
		namedEntityLookup.put("Ntilde", '\u00d1');
		namedEntityLookup.put("Ograve", '\u00d2');
		namedEntityLookup.put("Oacute", '\u00d3');
		namedEntityLookup.put("Ocirc", '\u00d4');
		namedEntityLookup.put("Otilde", '\u00d5');
		namedEntityLookup.put("Ouml", '\u00d6');
		namedEntityLookup.put("Oslash", '\u00d8');
		namedEntityLookup.put("Ugrave", '\u00d9');
		namedEntityLookup.put("Uacute", '\u00da');
		namedEntityLookup.put("Ucirc", '\u00db');
		namedEntityLookup.put("Uuml", '\u00dc');
		namedEntityLookup.put("Yacute", '\u00dd');
		namedEntityLookup.put("THORN", '\u00de');
		namedEntityLookup.put("szlig", '\u00df');
		namedEntityLookup.put("agrave", '\u00e0');
		namedEntityLookup.put("aacute", '\u00e1');
		namedEntityLookup.put("acirc", '\u00e2');
		namedEntityLookup.put("atilde", '\u00e3');
		namedEntityLookup.put("auml", '\u00e4');
		namedEntityLookup.put("aring", '\u00e5');
		namedEntityLookup.put("aelig", '\u00e6');
		namedEntityLookup.put("ccedil", '\u00e7');
		namedEntityLookup.put("egrave", '\u00e8');
		namedEntityLookup.put("eacute", '\u00e9');
		namedEntityLookup.put("ecirc", '\u00ea');
		namedEntityLookup.put("euml", '\u00eb');
		namedEntityLookup.put("igrave", '\u00ec');
		namedEntityLookup.put("iacute", '\u00ed');
		namedEntityLookup.put("icirc", '\u00ee');
		namedEntityLookup.put("iuml", '\u00ef');
		namedEntityLookup.put("eth", '\u00f0');
		namedEntityLookup.put("ntilde", '\u00f1');
		namedEntityLookup.put("ograve", '\u00f2');
		namedEntityLookup.put("oacute", '\u00f3');
		namedEntityLookup.put("ocirc", '\u00f4');
		namedEntityLookup.put("otilde", '\u00f5');
		namedEntityLookup.put("ouml", '\u00f6');
		namedEntityLookup.put("oslash", '\u00f8');
		namedEntityLookup.put("ugrave", '\u00f9');
		namedEntityLookup.put("uacute", '\u00fa');
		namedEntityLookup.put("ucirc", '\u00fb');
		namedEntityLookup.put("uuml", '\u00fc');
		namedEntityLookup.put("yacute", '\u00fd');
		namedEntityLookup.put("thorn", '\u00fe');
		namedEntityLookup.put("yuml", '\u00ff');

		// HTML3.2
		namedEntityLookup.put("nbsp", '\u00a0');
		namedEntityLookup.put("iexcl", '\u00a1');
		namedEntityLookup.put("cent", '\u00a2');
		namedEntityLookup.put("pound", '\u00a3');
		namedEntityLookup.put("curren", '\u00a4');
		namedEntityLookup.put("yen", '\u00a5');
		namedEntityLookup.put("brvbar", '\u00a6');
		namedEntityLookup.put("sect", '\u00a7');
		namedEntityLookup.put("uml", '\u00a8');
		namedEntityLookup.put("copy", '\u00a9');
		namedEntityLookup.put("ordf", '\u00aa');
		namedEntityLookup.put("laquo", '\u00ab');
		namedEntityLookup.put("not", '\u00ac');
		namedEntityLookup.put("shy", '\u00ad');
		namedEntityLookup.put("reg", '\u00ae');
		namedEntityLookup.put("macr", '\u00af');
		namedEntityLookup.put("deg", '\u00b0');
		namedEntityLookup.put("plusmn", '\u00b1');
		namedEntityLookup.put("sup2", '\u00b2');
		namedEntityLookup.put("sup3", '\u00b3');
		namedEntityLookup.put("acute", '\u00b4');
		namedEntityLookup.put("micro", '\u00b5');
		namedEntityLookup.put("para", '\u00b6');
		namedEntityLookup.put("middot", '\u00b7');
		namedEntityLookup.put("cedil", '\u00b8');
		namedEntityLookup.put("sup1", '\u00b9');
		namedEntityLookup.put("ordm", '\u00ba');
		namedEntityLookup.put("raquo", '\u00bb');
		namedEntityLookup.put("frac14", '\u00bc');
		namedEntityLookup.put("frac12", '\u00bd');
		namedEntityLookup.put("frac34", '\u00be');
		namedEntityLookup.put("iquest", '\u00bf');
		namedEntityLookup.put("times", '\u00d7');
		namedEntityLookup.put("divide", '\u00f7');

		// HTML4.0
		namedEntityLookup.put("OElig", '\u0152');
		namedEntityLookup.put("oelig", '\u0153');
		namedEntityLookup.put("Scaron", '\u0160');
		namedEntityLookup.put("scaron", '\u0161');
		namedEntityLookup.put("Yuml", '\u0178');
		namedEntityLookup.put("fnof", '\u0192');
		namedEntityLookup.put("circ", '\u02c6');
		namedEntityLookup.put("tilde", '\u02dc');

		namedEntityLookup.put("Alpha", '\u0391');
		namedEntityLookup.put("Beta", '\u0392');
		namedEntityLookup.put("Gamma", '\u0393');
		namedEntityLookup.put("Delta", '\u0394');
		namedEntityLookup.put("Epsilon", '\u0395');
		namedEntityLookup.put("Zeta", '\u0396');
		namedEntityLookup.put("Eta", '\u0397');
		namedEntityLookup.put("Theta", '\u0398');
		namedEntityLookup.put("Iota", '\u0399');
		namedEntityLookup.put("Kappa", '\u039a');
		namedEntityLookup.put("Lambda", '\u039b');
		namedEntityLookup.put("Mu", '\u039c');
		namedEntityLookup.put("Nu", '\u039d');
		namedEntityLookup.put("Xi", '\u039e');
		namedEntityLookup.put("Omicron", '\u039f');
		namedEntityLookup.put("Pi", '\u03a0');
		namedEntityLookup.put("Rho", '\u03a1');
		namedEntityLookup.put("Sigma", '\u03a3');
		namedEntityLookup.put("Tau", '\u03a4');
		namedEntityLookup.put("Upsilon", '\u03a5');
		namedEntityLookup.put("Phi", '\u03a6');
		namedEntityLookup.put("Chi", '\u03a7');
		namedEntityLookup.put("Psi", '\u03a8');
		namedEntityLookup.put("Omega", '\u03a9');
		namedEntityLookup.put("alpha", '\u03b1');
		namedEntityLookup.put("beta", '\u03b2');
		namedEntityLookup.put("gamma", '\u03b3');
		namedEntityLookup.put("delta", '\u03b4');
		namedEntityLookup.put("epsilon", '\u03b5');
		namedEntityLookup.put("zeta", '\u03b6');
		namedEntityLookup.put("eta", '\u03b7');
		namedEntityLookup.put("theta", '\u03b8');
		namedEntityLookup.put("iota", '\u03b9');
		namedEntityLookup.put("kappa", '\u03ba');
		namedEntityLookup.put("lambda", '\u03bb');
		namedEntityLookup.put("mu", '\u03bc');
		namedEntityLookup.put("nu", '\u03bd');
		namedEntityLookup.put("xi", '\u03be');
		namedEntityLookup.put("omicron", '\u03bf');
		namedEntityLookup.put("pi", '\u03c0');
		namedEntityLookup.put("rho", '\u03c1');
		namedEntityLookup.put("sigmaf", '\u03c2');
		namedEntityLookup.put("sigma", '\u03c3');
		namedEntityLookup.put("tau", '\u03c4');
		namedEntityLookup.put("upsilon", '\u03c5');
		namedEntityLookup.put("phi", '\u03c6');
		namedEntityLookup.put("chi", '\u03c7');
		namedEntityLookup.put("psi", '\u03c8');
		namedEntityLookup.put("omega", '\u03c9');
		namedEntityLookup.put("thetasym", '\u03d1');
		namedEntityLookup.put("upsih", '\u03d2');
		namedEntityLookup.put("piv", '\u03d6');

		namedEntityLookup.put("forall", '\u2200');
		namedEntityLookup.put("part", '\u2202');
		namedEntityLookup.put("exist", '\u2203');
		namedEntityLookup.put("empty", '\u2205');
		namedEntityLookup.put("nabla", '\u2207');
		namedEntityLookup.put("isin", '\u2208');
		namedEntityLookup.put("notin", '\u2209');
		namedEntityLookup.put("ni", '\u220b');
		namedEntityLookup.put("prod", '\u220f');
		namedEntityLookup.put("sum", '\u2211');
		namedEntityLookup.put("minus", '\u2212');
		namedEntityLookup.put("lowast", '\u2217');
		namedEntityLookup.put("radic", '\u221a');
		namedEntityLookup.put("prop", '\u221d');
		namedEntityLookup.put("infin", '\u221e');
		namedEntityLookup.put("ang", '\u2220');
		namedEntityLookup.put("and", '\u2227');
		namedEntityLookup.put("or", '\u2228');
		namedEntityLookup.put("cap", '\u2229');
		namedEntityLookup.put("cup", '\u222a');
		namedEntityLookup.put("int", '\u222b');
		namedEntityLookup.put("there4", '\u2234');
		namedEntityLookup.put("sim", '\u223c');
		namedEntityLookup.put("cong", '\u2245');
		namedEntityLookup.put("asymp", '\u2248');
		namedEntityLookup.put("ne", '\u2260');
		namedEntityLookup.put("equiv", '\u2261');
		namedEntityLookup.put("le", '\u8804');
		namedEntityLookup.put("ge", '\u2265');
		namedEntityLookup.put("sub", '\u2282');
		namedEntityLookup.put("sup", '\u2283');
		namedEntityLookup.put("nsub", '\u2284');
		namedEntityLookup.put("nsup", '\u2285');
		namedEntityLookup.put("sube", '\u2286');
		namedEntityLookup.put("supe", '\u2287');
		namedEntityLookup.put("oplus", '\u2295');
		namedEntityLookup.put("otimes", '\u2297');
		namedEntityLookup.put("perp", '\u22a5');
		namedEntityLookup.put("sdot", '\u22c5');

		namedEntityLookup.put("ensp", '\u2002');
		namedEntityLookup.put("emsp", '\u2003');
		namedEntityLookup.put("thinsp", '\u2009');
		namedEntityLookup.put("zwnj", '\u200c');
		namedEntityLookup.put("zwj", '\u200d');
		namedEntityLookup.put("lrm", '\u200e');
		namedEntityLookup.put("rlm", '\u200f');
		namedEntityLookup.put("ndash", '\u2013');
		namedEntityLookup.put("mdash", '\u2014');
		namedEntityLookup.put("lsquo", '\u2018');
		namedEntityLookup.put("rsquo", '\u2019');
		namedEntityLookup.put("sbquo", '\u201a');
		namedEntityLookup.put("ldquo", '\u201c');
		namedEntityLookup.put("rdquo", '\u201d');
		namedEntityLookup.put("bdquo", '\u201e');
		namedEntityLookup.put("dagger", '\u2020');
		namedEntityLookup.put("Dagger", '\u2021');
		namedEntityLookup.put("bull", '\u2022');
		namedEntityLookup.put("hellip", '\u2026');
		namedEntityLookup.put("permil", '\u2030');
		namedEntityLookup.put("prime", '\u2032');
		namedEntityLookup.put("Prime", '\u2033');
		namedEntityLookup.put("lsaquo", '\u2039');
		namedEntityLookup.put("rsaquo", '\u203a');
		namedEntityLookup.put("oline", '\u203e');
		namedEntityLookup.put("frasl", '\u2044');
		namedEntityLookup.put("euro", '\u20ac');

		namedEntityLookup.put("image", '\u2111');
		namedEntityLookup.put("wieerp", '\u2118');
		namedEntityLookup.put("real", '\u211c');
		namedEntityLookup.put("trade", '\u2122');
		namedEntityLookup.put("alefsym", '\u2135');
		namedEntityLookup.put("larr", '\u2190');
		namedEntityLookup.put("uarr", '\u2191');
		namedEntityLookup.put("rarr", '\u2192');
		namedEntityLookup.put("darr", '\u2193');
		namedEntityLookup.put("harr", '\u2194');
		namedEntityLookup.put("crarr", '\u21b5');
		namedEntityLookup.put("lArr", '\u21d0');
		namedEntityLookup.put("uArr", '\u21d1');
		namedEntityLookup.put("rArr", '\u21d2');
		namedEntityLookup.put("dArr", '\u21d3');
		namedEntityLookup.put("hArr", '\u21d4');
		namedEntityLookup.put("lceil", '\u2308');
		namedEntityLookup.put("rceil", '\u2309');
		namedEntityLookup.put("lfloor", '\u230a');
		namedEntityLookup.put("rfloor", '\u230b');
		namedEntityLookup.put("loz", '\u25ca');
		namedEntityLookup.put("spades", '\u2660');
		namedEntityLookup.put("clubs", '\u2663');
		namedEntityLookup.put("hearts", '\u2665');
		namedEntityLookup.put("diams", '\u2666');

		// HTML5.0
		namedEntityLookup.put("angst", '\u00c5');
		// TODO: As many other HTML5 as we can be bothered with...

		// NB Some characters have multiple mappings so ensure we always use the
		// first
		namedEntityLookup.entrySet().forEach(e -> charToEntityLookupName
				.putIfAbsent(e.getValue(), e.getKey()));
	}

	private HtmlEncoder() {
		// Do not instantiate
	}

	/**
	 * @param str
	 *            The String to encode with HTML entities
	 * @return The HTML-named-entity encoded string. Calls
	 *         {@link #encode(String, boolean, boolean, boolean)} with the
	 *         boolean parameter {@code true}, {@code false}, {@code false}
	 */
	public static String encode(String str) {
		return encode(str, true, false, false);
	}

	/**
	 * @param str
	 *            The string to encode with HTML named entities
	 * @param useNamedEntities
	 *            Should a named entity be used in place of the numeric entity
	 *            code where this exists (upto HTML 4.0)
	 * @param useEntitiesForAllNonalphanumeric
	 *            Should all non-alphanumeric characters be encoded?
	 * @param useHexEntityNumbers
	 *            Should hex encoding be used in place of decimal? e.g.
	 * @return The HTML named entity-encoded string
	 */
	public static String encode(String str, boolean useNamedEntities,
			boolean useEntitiesForAllNonalphanumeric,
			boolean useHexEntityNumbers) {
		int i = 0;
		final StringBuilder sb = new StringBuilder();
		while (i < str.length()) {
			final char c = str.charAt(i++);
			final boolean useEntity = charToEntityLookupName.containsKey(c)
					|| useEntitiesForAllNonalphanumeric
							&& !Character.isLetterOrDigit(c);
			if (useEntity) {
				// Start then entity
				sb.append('&');
				if (useNamedEntities && charToEntityLookupName.containsKey(c)) {
					// Named entity
					sb.append(charToEntityLookupName.get(c));
				} else {
					// Numeric entity
					sb.append('#');
					if (useHexEntityNumbers) {
						sb.append('x');
						sb.append(Integer.toHexString(c));
					} else {
						sb.append(Integer.toString(c));
					}
				}
				// Close the entity
				sb.append(';');
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * Method to remove entity-encoding from an HTML string
	 * 
	 * @param str
	 *            The string to decode
	 * @return The decoded String. NB named entities in HTML1.0 not requiring
	 *         the final ';' will node be decoded
	 */
	public static String decode(String str) {
		int i = 0;
		final StringBuilder sb = new StringBuilder();
		while (i < str.length()) {
			char c = str.charAt(i++);
			if (c == '&') {
				// We have hit an entity
				c = str.charAt(i++);
				if (c == '#') {
					// Numeric entity
					c = str.charAt(i++);
					if (c == 'x') {
						// Hex
						final StringBuilder entBuilder = new StringBuilder();
						while ((c = str.charAt(i++)) != ';') {
							entBuilder.append(c);
						}
						sb.append((char) Integer.parseInt(entBuilder.toString(),
								16));
					} else {
						// Numeric
						final StringBuilder entBuilder = new StringBuilder();
						entBuilder.append(c);
						while ((c = str.charAt(i++)) != ';') {
							entBuilder.append(c);
						}
						sb.append(
								(char) Integer.parseInt(entBuilder.toString()));
					}
				} else {
					// Named entity
					final StringBuilder entBuilder = new StringBuilder();
					entBuilder.append(c);
					while ((c = str.charAt(i++)) != ';') {
						entBuilder.append(c);
					}
					sb.append(namedEntityLookup.get(entBuilder.toString()));
				}
			} else {
				// Normal character
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
