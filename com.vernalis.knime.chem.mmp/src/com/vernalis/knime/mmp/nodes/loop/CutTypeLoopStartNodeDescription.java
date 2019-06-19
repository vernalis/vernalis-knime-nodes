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
package com.vernalis.knime.mmp.nodes.loop;

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

import com.vernalis.knime.mmp.FragmentationTypes;

import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addFragmentationOptionsDiscription;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionWithoutTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.insertReference;

/**
 * Node description which builds the node description for the 'Cut Type Loop
 * Start' node dynamically from the {@link FragmentationTypes} enum
 * 
 * @author s.roughley
 *
 */
public class CutTypeLoopStartNodeDescription extends NodeDescription {

	/**
	 * Constructor
	 */
	public CutTypeLoopStartNodeDescription() {
		super();

	}

	@Override
	public String getIconPath() {
		return getClass().getResource("MMPLoopStart.png").getFile();
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
		return "MMP Fragmentation Type Loop Start";
	}

	@Override
	public String getOutportDescription(int index) {
		switch (index) {
		case 0:
			return "The unchanged input table, with a fragmentation type "
					+ "exposed in the flow variables as 'Fragmentation Type'";
		default:
			return null;
		}
	}

	@Override
	public String getOutportName(int index) {
		switch (index) {
		case 0:
			return "Molecules-with-types";
		default:
			return null;
		}
	}

	@Override
	public NodeType getType() {
		return NodeType.LoopStart;
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
		node.setType(KnimeNode.Type.LOOP_START);
		node.setShortDescription(
				"This node allows simple looping through pre-defined fragmentation patterns");
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		introCursor.insertElementWithText("p",
				"This node allows the user to select one or more of the pre-defined "
						+ "fragmentation types and loops through those selected. The "
						+ "current fragmentation type is exposed as a flow variable.");

		addFragmentationOptionsDiscription(introCursor);

		addDevelopedByVernalis(introCursor);

		insertReference(introCursor, 1, "J. Hussain and C Rea",
				"Computationally efficient algorithm to identify matched molecular pairs"
						+ " (MMPs) in large datasets",
				"J. Chem. Inf. Model.", 2010, 50, "339-348", "10.1021/ci900450m");

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
		introCursor.insertChars(" (http://www.rdkit.org/docs/Overview.html#the-contrib-directory) "
				+ "(section entitled 'mmpa')");
		introCursor.toEndToken();
		introCursor.toNextToken();

		insertReference(introCursor, 4, "N. M. O'Boyle, J. Bostrom, R. A. Sayle and A. Gill",
				"Using Matched Molecular Series as a Predictive Tool To Optimize Biological Activity",
				"J. Med. Chem.", 2014, 57, "2704-2713", "10.1021/jm500022q");

		introCursor.dispose();

		addOptionWithoutTab(fullDesc, "Bond match types", "Select the desired fragmentation types");

		Ports ports = node.addNewPorts();

		InPort inport = ports.addNewInPort();
		inport.setIndex(0);
		inport.setName(getInportName(0));
		inport.newCursor().setTextValue(getInportDescription(0));

		OutPort outport = ports.addNewOutPort();
		outport.setIndex(0);
		outport.setName(getOutportName(0));
		outport.newCursor().setTextValue(getOutportDescription(0));

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
