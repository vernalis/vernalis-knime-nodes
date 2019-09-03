/*******************************************************************************
 * Copyright (c) 2017,2018 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct;

import org.apache.xmlbeans.XmlCursor;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.InPortDocument.InPort;
import org.knime.node2012.IntroDocument.Intro;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.OutPortDocument.OutPort;
import org.knime.node2012.PortsDocument.Ports;
import org.knime.node2012.TabDocument.Tab;
import org.knime.node2012.ViewDocument.View;
import org.knime.node2012.ViewsDocument.Views;
import org.w3c.dom.Element;

import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addFragmentationOptionsDiscription;
import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addRSmartsGuidelines;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.insertReference;

/**
 * Node Description class for the MMP Fragment nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPFragmentNodeDescription<T, U> extends NodeDescription {

	private final boolean isMulticut;
	private final FragmentationUtilsFactory<T, U> fragUtilityFactory;
	private static final int CURRENT_VERSION = 4;

	/**
	 * Constructor
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 * @param isMulticut
	 *            Is the node multicut or only performs n cuts?
	 */
	public AbstractMMPFragmentNodeDescription(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, int version) {
		super();
		this.isMulticut = isMulticut;
		this.fragUtilityFactory = fragUtilityFactory;
		setIsDeprecated(version < CURRENT_VERSION);
	}

	@Override
	public String getIconPath() {
		// Otherwise it is relative to the path of the node factory class
		return getClass()
				.getResource(isMulticut ? "MMPMultiFrag.png" : "MMPFrag.png")
				.getFile();
	}

	@Override
	public String getInportDescription(int index) {
		switch (index) {
		case 0:
			return "Molecules for fragmenting";
		default:
			return null;
		}
	}

	@Override
	public String getInportName(int index) {
		switch (index) {
		case 0:
			return "Molecules";
		default:
			return null;
		}
	}

	@Override
	public String getInteractiveViewName() {
		return null;
	}

	@Override
	public String getNodeName() {
		StringBuilder sb = new StringBuilder("MMP Molecule ");
		if (isMulticut) {
			sb.append("Multi-cut ");
		}
		sb.append("Fragment (").append(fragUtilityFactory.getToolkitName())
				.append(")");
		if (isDeprecated()) {
			sb.append(" (deprecated)");
		}
		return sb.toString();
	}

	@Override
	public String getOutportDescription(int index) {
		switch (index) {
		case 0:
			return "Key-value fragmentation pairs";
		case 1:
			return "Input rows for which the molecule could not be parsed in "
					+ fragUtilityFactory.getToolkitName()
					+ ", or which could not be fragmented according to the options specified";
		default:
			return null;
		}
	}

	@Override
	public String getOutportName(int index) {
		switch (index) {
		case 0:
			return "Fragments";
		case 1:
			return "Failed Input Rows";
		default:
			return null;
		}
	}

	@Override
	public NodeType getType() {
		return NodeType.Manipulator;
	}

	@Override
	public int getViewCount() {
		return 1;
	}

	@Override
	public String getViewDescription(int index) {
		switch (index) {
		case 0:
			return "The view shows the proportion of the table completely "
					+ "processed, the proportion of the queue currently filled, and the "
					+ "proportion of the allocated threads currently active. The size of the "
					+ "queue and the number of threads can be controlled in the preferences "
					+ "- a bigger queue may use more memory, but is more likely to keep all "
					+ "parallel threads active, resulting in shorter processing times";
		default:
			return null;
		}
	}

	@Override
	public String getViewName(int index) {
		switch (index) {
		case 0:
			return "Fragmentation Progress";
		default:
			return null;
		}
	}

	@Override
	public Element getXMLDescription() {
		KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
		KnimeNode node = doc.addNewKnimeNode();
		node.setDeprecated(KnimeNodeDocument.KnimeNode.Deprecated.Enum.forInt(
				isDeprecated() ? KnimeNodeDocument.KnimeNode.Deprecated.INT_TRUE
						: KnimeNodeDocument.KnimeNode.Deprecated.INT_FALSE));
		node.setIcon(getIconPath());
		node.setName(getNodeName());
		node.setType(KnimeNode.Type.MANIPULATOR);
		node.setShortDescription(
				"This node implements the molecule fragmentation stage of the Hussain and Rea "
						+ "algorithm for Matched Molecular Pair finding");
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		introCursor
				.insertElementWithText("p",
						"This node implements the molecule fragmentation part of the Hussain and Rea "
								+ "algorithm (Ref 1) for finding Matched Molecular Pairs in a dataset, enabling "
								+ "the fragmented molecule key-value pairs to be stored in a database for later "
								+ "recall or used directly in a subsequent pair-finding node. The user "
								+ "can specify the "
								+ (isMulticut ? "maximum " : "")
								+ "number of cuts to be made (1 - 10), and whether "
								+ "Hydrogens should be added (1 cut only)"
								+ (isMulticut
										? ". All cuts from 1 to the"
												+ "specified number are made"
										: ""));
		addFragmentationOptionsDiscription(introCursor);
		addRSmartsGuidelines(introCursor);

		introCursor.insertElementWithText("p",
				"The algorithm is implemented using the "
						+ fragUtilityFactory.getToolkitName() + " toolkit");

		addDevelopedByVernalis(introCursor);

		insertReference(introCursor, 1, "J. Hussain and C Rea",
				"Computationally efficient algorithm to identify matched molecular pairs"
						+ " (MMPs) in large datasets",
				"J. Chem. Inf. Model.", 2010, 50, "339-348",
				"10.1021/ci900450m");

		insertReference(introCursor, 2, "S. D. Roughley and A. M. Jordan",
				"The Medicinal Chemist’s Toolbox: An Analysis of Reactions Used in the Pursuit of Drug Candidates",
				"J. Med. Chem.", 2011, 54, "3451-3479", "10.1021/jm200187y");

		introCursor.beginElement("p");
		introCursor.insertChars("3. G. Landrum, \"");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href",
				"http://www.rdkit.org/docs/Overview.html#the-contrib-directory");
		introCursor.insertChars("An Overview of RDKit");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(
				" (http://www.rdkit.org/docs/Overview.html#the-contrib-directory) "
						+ "(section entitled 'mmpa')");
		introCursor.toEndToken();
		introCursor.toNextToken();

		insertReference(introCursor, 4,
				"N. M. O'Boyle, J. Bostrom, R. A. Sayle and A. Gill",
				"Using Matched Molecular Series as a Predictive Tool To Optimize Biological Activity",
				"J. Med. Chem.", 2014, 57, "2704-2713", "10.1021/jm500022q");

		introCursor.dispose();

		Tab tab = fullDesc.addNewTab();
		tab.setName("Molecule & Fragmentation Options");
		addOptionToTab(tab, "Select Molecule column",
				"Select the column containing the molecules");
		addOptionToTab(tab, "Select Molecule IDs column",
				"Select the column containing the molecule IDs");
		addOptionToTab(tab, "Allow HiLiting",
				"If selected, then hiliting between the incoming molecules "
						+ "and fragments is preserved. WARNING - this can result in significant memory "
						+ "use for big tables or large numbers of fragmentations!");
		addOptionToTab(tab, "Select the Fragmentation Type",
				"Select the required fragmentation option");
		addOptionToTab(tab, "User SMARTS",
				"The optional user-defined (r)SMARTS (see above for details)");
		addOptionToTab(tab, (isMulticut ? "Maximum n" : "N") + "umber of cuts",
				"Select the " + (isMulticut ? "maximum " : "")
						+ "number of cuts (1-10)");
		addOptionToTab(tab,
				"Allow 2 cuts along single bond giving a single bond as 'value'?",
				"If selected, for the 2 cuts case, 1 bond can be cut twice, "
						+ "allowing a 'value' of [*:1]-[*:2] (i.e. a 'bond') to be formed");
		addOptionToTab(tab, "Explicit Hydrogens",
				"Options for handling explicit hydrogens "
						+ "during fragmentation. Users are strongly advised to retain the recommended default settings"
						+ "To understand these options, fragmentation is performed using either a single or "
						+ "two (if explicit H's are added for 1 cut) 'fragmentation factories' for each "
						+ "incoming molecule. The factory with explicit H's added is only used for 1 cut, "
						+ "and only for those cuts of bonds to 'H'.  All other bond breaks are performed "
						+ "using the 2nd factory.  If you wish to achieve specific effects, using these settings "
						+ "beyond the default then we suggest trialing with with a simple example molecule containing "
						+ "an explicit 'H', e.g. '[H]c1c([Cl])cccc1' or 'C/C([H])=C/C'");
		addOptionToTab(tab, "Add H's prior to fragmentation",
				"If checked, pairs with -H as a substituent will be included. "
						+ "This is recommended for when the number of cuts is 1, "
						+ "and is unavailable for other values");
		addOptionToTab(tab, "Remove Added Explicit H's from output",
				"Explicit hydrogens added for a single cut will be removed from the output if selected "
						+ "(Only available when 'Add H's prior to fragmentation' is selected and enabled)");
		addOptionToTab(tab, "Incoming explicit H's treatment",
				"If incoming molecules contain any explicit H's, how should they be treated? "
						+ "The preferred option is to remove them prior to fragmentation, "
						+ "otherwise seemingly spurious results may follow. ");
		tab = fullDesc.addNewTab();
		tab.setName("Fragmentation Filtering Settings");

		addOptionToTab(tab, "Limit by Complexity",
				"If checked, this option will skip molecules likely to have "
						+ "a very large number of fragmentations, based on the number of possible "
						+ "fragmentable bond combinations (different bond combinations leading to "
						+ "identical fragmentations are not discounted)");
		addOptionToTab(tab, "Maximum Fragmentations",
				"The limit of predicted fragmentations");
		addOptionToTab(tab, "Treat no undefined chiral centres as chiral",
				"In molecules with explicit chiral centres, newly created "
						+ "stereocentres are given defined chirality. Molecules with only undefined "
						+ "possible stereocentres (e.g. CC(F)Br) will not have explicit stereochemistry "
						+ "assigned to newly created centres. Molecules with neither explicit or "
						+ "undefined stereocentres will have explicit chirality set at newly created "
						+ "centres if this option is selected, otherwise they will not be set.");
		addOptionToTab(tab, "Filter by maximum number of changing heavy atoms?",
				"If checked, the user can specify a maximum number of heavy "
						+ "atoms which are allowed to change between Matched Pairs");
		addOptionToTab(tab, "Maximum Number of variable heavy atoms",
				"The maximum number of heavy atoms which are allowed to change between pairs");
		addOptionToTab(tab, "Filter by ratio of changing / unchanging atoms?",
				"If checked, the user can specify a maximum ratio of changing "
						+ "to unchanging heavy atoms during fragmentation");
		addOptionToTab(tab,
				"Minimum ratio of changing to unchanging heavy atoms",
				"The minimum ratio of changing to unchanging heavy atoms");

		tab = fullDesc.addNewTab();
		tab.setName("Output Settings");

		addOptionToTab(tab, "Show number of changing atoms",
				"The number of heavy atoms (not including 'A', the attachment "
						+ "point) will be included for Left and Right fragments");
		addOptionToTab(tab, "Show ratio of constant / changing heavy atoms",
				"The ratio of constant / changing heavy atoms (not including 'A', "
						+ "the attachment point) will be included for Left and Right fragments");
		addOptionToTab(tab, "Add failure reasons to 2nd output table",
				"If checked, the reason the molecule could not be fragmented "
						+ "is added to the second output table");

		if (fragUtilityFactory.getRendererType() != null) {
			addOptionToTab(tab, "Render Fragmentation",
					"Should the fragmentation be rendered");
			addOptionToTab(tab, "Show breaking bonds",
					"Should breaking bonds be highlighted in the rendering?");
			addOptionToTab(tab, "Breaking bond colour",
					"The colour to highlight the breaking bond(s)");
			addOptionToTab(tab, "Show key",
					"Should the atoms/bonds forming the 'key' be "
							+ "highlighted in the rendering?");
			addOptionToTab(tab, "Key Colour",
					"The colour to highlight the 'key'");
			addOptionToTab(tab, "Show value",
					"Should the atoms/bonds forming the 'value' be "
							+ "highlighted in the rendering? If the fragmentation is a double cut to 1 bond, "
							+ "then the breaking bond is also the value, and will be shown as such");
			addOptionToTab(tab, "Value Colour",
					"The colour to highlight the 'value'");
		}
		addOptionToTab(tab, "Incoming columns to keep",
				"Select incoming data columns to keep. The ID column will always "
						+ "be present in the output, regardless of the setting here, with the name "
						+ "'ID'. Fragmentation columns will be left-most in the table, and incoming "
						+ "columns may be renamed by the addition of a suffix, e.g. '(#1)', "
						+ "to avoid duplicate names)");

		tab = fullDesc.addNewTab();
		tab.setName("Fingerprints");

		addOptionToTab(tab,
				"Show 'Value' attachment point graph distances fingerprint",
				"Include a graph distance fingerprint showing the graph distance between each "
						+ "attachment point in the fragmentation 'value'. The fingeprint is the "
						+ "number of bonds between each pair of attachment points, so "
						+ "'[*:1]-[*:2]' is {1}, '[*:1]c1c([*:2])cc([*:3])cc1' is {3,5,4} etc");
		addOptionToTab(tab, "Add Attachment Point Fingerprints",
				"If checked, then attachment point fingerprints are added. "
						+ "See above for further details. One column is added for each attachment point");
		addOptionToTab(tab, "Fingerprint Length",
				"The number of bits in the fingerprints");
		addOptionToTab(tab, "Morgan Radius",
				"The radius of the Morgan fingerprint");

		if (fragUtilityFactory.hasExtendedFingerprintOptions()) {
			addOptionToTab(tab, "Use Bond Types",
					"Should the bond types be included in the fingerprint generation");
			addOptionToTab(tab, "Use chirality",
					"Should chirality be included in the fingerprint generation");
		}

		Ports ports = node.addNewPorts();

		InPort inport = ports.addNewInPort();
		inport.setIndex(0);
		inport.setName(getInportName(0));
		inport.newCursor().setTextValue(getInportDescription(0));

		OutPort outport = ports.addNewOutPort();
		outport.setIndex(0);
		outport.setName(getOutportName(0));
		outport.newCursor().setTextValue(getOutportDescription(0));

		outport = ports.addNewOutPort();
		outport.setIndex(1);
		outport.setName(getOutportName(1));
		outport.newCursor().setTextValue(getOutportDescription(1));

		if (getViewCount() > 0) {
			Views views = node.addNewViews();
			for (int i = 0; i < getViewCount(); i++) {
				View view = views.addNewView();
				view.setIndex(i);
				view.setName(getViewName(i));
				view.newCursor().setTextValue(getViewDescription(i));
			}
		}

		return (Element) node.getDomNode();

	}
}
