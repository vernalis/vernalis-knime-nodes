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
package com.vernalis.knime.mmp.nodes.abstrct;

import org.apache.xmlbeans.XmlCursor;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.OptionDocument.Option;
import org.knime.node2012.TabDocument.Tab;

import com.vernalis.knime.mmp.FragmentationTypes;

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
	 * Method to add the {@link FragmentationTypes} descriptions
	 * 
	 * @param introCursor
	 *            The cursor
	 */
	public static void addFragmentationOptionsDiscription(XmlCursor introCursor) {
		introCursor.beginElement("p");
		introCursor.insertChars("A variety of fragmentation options are included:");
		introCursor.beginElement("ol");
		for (FragmentationTypes type : FragmentationTypes.values()) {
			introCursor.insertElementWithText("li", "\"" + type.getText() + "\" - "
					+ type.getToolTip()
					+ (type.getSMARTS() != null ? " (rSMARTS: " + type.getSMARTS() + ")" : ""));
		}
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/**
	 * Method to add the guidelines about the rSMARTS specification
	 * 
	 * @param introCursor
	 *            The cursor
	 */
	public static void addRSmartsGuidelines(XmlCursor introCursor) {
		introCursor.beginElement("p");
		introCursor.insertElementWithText("i", "Guidelines for Custom (r)SMARTS Definition");
		introCursor.insertElement("br");
		introCursor.insertChars(
				"An rSMARTS is no longer required, but may be specified if preferred for backwards compatibility. "
						+ "If specified must comply with the following rules. "
						+ "Otherwise, simply a match for two atoms separated by a single, acyclic bond must be provided");
		introCursor.beginElement("ul");
		introCursor.insertElementWithText("li",
				"'>>' is required to separate reactants and products");
		introCursor.insertElementWithText("li",
				"Products require '[*]' to occur twice, for the attachment "
						+ "points (the node will handle the tagging of these)");
		introCursor.insertElementWithText("li",
				"Reactants and products require exactly two atom mappings, e.g. "
						+ ":1] and :2] (other values could be used).");
		introCursor.insertElementWithText("li", "The atom mappings must be two different values");
		introCursor.insertElementWithText("li",
				"The same atom mappings must be used for reactants and products");
		introCursor.toEndToken();
		introCursor.insertChars("rSMARTS not conforming to these guidelines will be "
				+ "rejected during node configuration.");
		introCursor.toEndToken();
		introCursor.toNextToken();
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

}
