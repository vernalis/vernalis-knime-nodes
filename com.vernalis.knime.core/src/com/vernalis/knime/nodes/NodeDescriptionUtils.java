/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.nodes;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.apache.xmlbeans.XmlCursor;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.OptionDocument.Option;
import org.knime.node2012.TabDocument.Tab;

import com.vernalis.knime.misc.EitherOr;

/**
 * Utility class to proved tools to build Node Descriptions
 * 
 * @author s.roughley
 *
 */
public class NodeDescriptionUtils {
	private NodeDescriptionUtils() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * @param fullDesc
	 *            The {@link FullDescription} object to add the option too,
	 *            without adding a tab
	 * @param optionName
	 *            The name of the option
	 * @param optionDescription
	 *            The description of the option
	 */
	public static void addOptionWithoutTab(FullDescription fullDesc, String optionName,
			String optionDescription) {
		Option opt = fullDesc.addNewOption();
		configureOption(opt, optionName, optionDescription);
	}

	/**
	 * @param tab
	 *            The tab to add the option to
	 * @param optionName
	 *            The name of the option
	 * @param optionDescription
	 *            The description of the option
	 */
	public static void addOptionToTab(Tab tab, String optionName, String optionDescription) {
		Option opt = tab.addNewOption();
		configureOption(opt, optionName, optionDescription);
	}

	/**
	 * Convenience method to actually build the option from the parameters
	 * 
	 * @param opt
	 * @param optionName
	 * @param optionDescription
	 */
	private static void configureOption(Option opt, String optionName, String optionDescription) {
		opt.setName(optionName);
		XmlCursor optCursor = opt.newCursor();
		optCursor.toFirstContentToken();
		optCursor.insertChars(optionDescription);
		optCursor.dispose();
	}

	/**
	 * Method to insert a numbered reference
	 * 
	 * @param introCursor
	 *            The cursor to add to
	 * @param refNumber
	 *            The reference number
	 * @param authors
	 *            The authors
	 * @param title
	 *            The title (quoted italics)
	 * @param journalAbbrev
	 *            The journal name abbreviation (Italics)
	 * @param year
	 *            The year
	 * @param volume
	 *            The volume (bold)
	 * @param pageRange
	 *            The page range
	 * @param doi
	 *            The DOI (will be linked too)
	 */
	public static void insertReference(XmlCursor introCursor, int refNumber, String authors,
			String title, String journalAbbrev, int year, int volume, String pageRange,
			String doi) {
		introCursor.beginElement("p");
		introCursor.insertChars(refNumber + ". " + authors + ", \"");
		introCursor.insertElementWithText("i", title);
		introCursor.insertChars("\", ");
		introCursor.insertElementWithText("i", journalAbbrev);
		introCursor.insertChars(", " + year + ", ");
		introCursor.insertElementWithText("b", volume + ", ");
		introCursor.insertChars(pageRange + " (DOI:");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href", "http://dx.doi.org/" + doi);
		introCursor.insertChars(doi);
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(")");
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/**
	 * Add the 'Developed by Vernalis' monicker to the end of the node
	 * description
	 * 
	 * @param introCursor
	 *            The cursor to add to
	 */
	public static void addDevelopedByVernalis(XmlCursor introCursor) {
		introCursor.beginElement("p");
		introCursor.insertChars("This node was developed by ");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href", "http://www.vernalis-research.com");
		introCursor.insertChars("Vernalis Research");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(". For feedback and more information, please contact ");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href", "mailto:knime@vernalis.com");
		introCursor.insertChars("knime@vernalis.com");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/**
	 * Method to add all options to the node description. Options are either
	 * added to the end of the description or in Tabs
	 * 
	 * @param fullDesc
	 *            The full description object of the node description
	 * @param options
	 *            The options - either a {@code Map<String, String>} for all
	 *            options to be added (K=option name, V= description), or a
	 *            {@code Map<String, Map<String, String>>} keyed on tab names
	 *            for options to be added to tabs
	 * @throws NoSuchElementException
	 */
	public static void addOptionsToDescription(FullDescription fullDesc,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options)
			throws NoSuchElementException {
		if (options != null && options.isPresent()) {
			if (options.isLeft()) {
				for (Entry<String, String> option : options.getLeft().entrySet()) {
					addOptionWithoutTab(fullDesc, option.getKey(), option.getValue());
				}
			} else {
				for (Entry<String, Map<String, String>> tabEntry : options.getRight().entrySet()) {
					Tab tab = fullDesc.addNewTab();
					tab.setName(tabEntry.getKey());
					for (Entry<String, String> option : tabEntry.getValue().entrySet()) {
						addOptionToTab(tab, option.getKey(), option.getValue());
					}
				}
			}
		}
	}

}
