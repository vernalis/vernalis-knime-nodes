/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector;

/**
 * A simple class providing a static method to clean-up XML into nicely a nicely
 * indented layout
 * 
 * @author S.Roughley
 *
 */
public abstract class XMLFormatter {

	/**
	 * A simple method to convert a single line XML string to a nicely formatted
	 * indented multi-line string
	 * 
	 * @param xml
	 *            The incoming single line string
	 * @return The formatted XML
	 */
	public static String indentXML(String xml) {
		String indent = "";
		String[] out = xml.replaceAll(">\\s*<", ">\n<").split("\n");
		StringBuilder retVal = new StringBuilder();
		for (int i = 0; i < out.length; i++) {
			if (i > 0 && out[i].startsWith("<") && !out[i].startsWith("</")) {
				indent += "\t";
			}
			out[i] = indent + out[i];
			retVal.append(out[i]).append("\n");
			if (out[i].endsWith("/>") || out[i].matches("[\\t]*<(.*?)>.*</\\1>")
					|| out[i].trim().startsWith("</")) {
				if (indent.length() > 1) {
					indent = indent.substring(1);
				} else {
					indent = "";
				}
			}
		}
		return retVal.toString().trim();
	}
}
