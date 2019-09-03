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
package com.vernalis.knime.mmp.nodes.fragutil.filter.abstrct;

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
import org.knime.node2012.ViewDocument.View;
import org.knime.node2012.ViewsDocument.Views;
import org.w3c.dom.Element;

import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addFragmentationOptionsDiscription;
import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addRSmartsGuidelines;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionWithoutTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.insertReference;

/**
 * Node Description for the Filter / Splitter nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule object type
 * @param <U>
 *            The matcher object type
 */
public class AbstractMMPFragmentationFilterSplitterNodeDescription<T, U>
		extends NodeDescription {

	private final boolean isSplitter;
	private final FragmentationUtilsFactory<T, U> fragUtilityFactory;

	/**
	 * Constructor
	 * 
	 * @param isSplitter
	 *            Is the node a splitter (i.e. two output ports) or no (i.e.
	 *            filter, 1 output port)
	 */
	public AbstractMMPFragmentationFilterSplitterNodeDescription(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isSplitter) {
		super();
		this.isSplitter = isSplitter;
		this.fragUtilityFactory = fragUtilityFactory;
	}

	@Override
	public String getIconPath() {
		return getClass()
				.getResource(isSplitter ? "MMP_Prefilter_Splitter.png" : "MMP_Prefilter_Filter.png")
				.getFile();
	}

	@Override
	public String getInportDescription(int index) {
		switch (index) {
		case 0:
			return "Molecules for filtering";
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
		return "MMP Molecule " + (isSplitter ? "Splitter" : "Filter") + " ("
				+ fragUtilityFactory.getToolkitName() + ")";
	}

	@Override
	public String getOutportDescription(int index) {
		switch (index) {
		case 0:
			return "Molecules which can be fragmented according to the Schema. Molecules not readable by "
					+ fragUtilityFactory.getToolkitName() + " will also fail";
		case 1:
			if (isSplitter) {
				return "Molecules which cannot be fragmented according to the Schema. Molecules not readable by "
						+ fragUtilityFactory.getToolkitName()
						+ " will also fail";
			}
		default:
			return null;
		}
	}

	@Override
	public String getOutportName(int index) {
		switch (index) {
		case 0:
			return "Passed Molecules";
		case 1:
			if (isSplitter) {
				return "Failed Molecules";
			}
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
		return 0;
	}

	@Override
	public String getViewDescription(int index) {
		return null;
	}

	@Override
	public String getViewName(int index) {
		return null;
	}

	@Override
	public Element getXMLDescription() {
		KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
		KnimeNode node = doc.addNewKnimeNode();
		node.setIcon(getIconPath());
		node.setName(getNodeName());
		node.setType(KnimeNode.Type.MANIPULATOR);
		node.setShortDescription(
				"This node prefilters molecules prior to Matched Pair Finding");
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		introCursor.insertElementWithText("p",
				"This node pre-filters molecules by viabilty in the specified MMP "
						+ "schema. The user can specify the number of cuts to be made (1 - 10), "
						+ "and whether Hydrogens should be added (for 1 cut only).");

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

		addOptionWithoutTab(fullDesc, "Select Molecule column",
				"Select the column containing the molecules");
		addOptionWithoutTab(fullDesc, "Select the Fragmentation Type",
				"Select the required fragmentation option");
		addOptionWithoutTab(fullDesc, "User SMARTS",
				"The optional user-defined (r)SMARTS (see above for details)");
		addOptionWithoutTab(fullDesc, "Number of cuts",
				"Select the number of cuts (1-10)");
		addOptionWithoutTab(fullDesc, "Add H's prior to fragmentation",
				"If checked, pairs with -H as a substituent will be included. "
						+ "This is recommended for when the number of cuts is 1, "
						+ "and is unavailable for other values");
		addOptionWithoutTab(fullDesc,
				"Allow 2 cuts along single bond giving a single bond as 'value'?",
				"If selected, for the 2 cuts case, 1 bond can be cut twice, "
						+ "allowing a 'value' of [*:1]-[*:2] (i.e. a 'bond') to be formed");

		Ports ports = node.addNewPorts();

		InPort inport = ports.addNewInPort();
		inport.setIndex(0);
		inport.setName(getInportName(0));
		inport.newCursor().setTextValue(getInportDescription(0));

		OutPort outport = ports.addNewOutPort();
		outport.setIndex(0);
		outport.setName(getOutportName(0));
		outport.newCursor().setTextValue(getOutportDescription(0));
		if (isSplitter) {
			outport = ports.addNewOutPort();
			outport.setIndex(1);
			outport.setName(getOutportName(1));
			outport.newCursor().setTextValue(getOutportDescription(1));
		}

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
