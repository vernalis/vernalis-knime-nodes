package com.vernalis.knime.mmp.nodes.abstrct;

import org.apache.xmlbeans.XmlCursor;

import com.vernalis.knime.mmp.FragmentationTypes;

/**
 * Utility class with static methods specific to generation of node descriptions
 * for MMP nodes
 * 
 * @author s.roughley
 *
 */
public class MMPNodeDescriptionUtils {
	private MMPNodeDescriptionUtils() {
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
}
