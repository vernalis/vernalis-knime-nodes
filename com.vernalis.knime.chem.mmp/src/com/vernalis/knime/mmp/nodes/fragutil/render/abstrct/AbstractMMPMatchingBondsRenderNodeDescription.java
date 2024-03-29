/*******************************************************************************
 * Copyright (c) 2017, 2023, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.render.abstrct;

import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addFragmentationOptionsDiscription;
import static com.vernalis.knime.mmp.nodes.abstrct.MMPNodeDescriptionUtils.addRSmartsGuidelines;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionWithoutTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.insertReference;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.node.v41.FullDescription;
import org.knime.node.v41.InPort;
import org.knime.node.v41.Intro;
import org.knime.node.v41.KnimeNode;
import org.knime.node.v41.KnimeNodeDocument;
import org.knime.node.v41.OutPort;
import org.knime.node.v41.Ports;
import org.knime.node.v41.View;
import org.knime.node.v41.Views;
import org.w3c.dom.Element;

import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

/**
 * The node description for the render matching bonds nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPMatchingBondsRenderNodeDescription<T, U> extends NodeDescription {
	private final FragmentationUtilsFactory<T, U> fragUtilityFactory;

	/**
	 * Constructor
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 */
	public AbstractMMPMatchingBondsRenderNodeDescription(
			FragmentationUtilsFactory<T, U> fragUtilityFactory) {
		super();
		this.fragUtilityFactory = fragUtilityFactory;
	}

	@Override
	public String getIconPath() {
		// Otherwise it is relative to the path of the node factory class
		return getClass().getResource("MMPMarkBonds.png").getFile();
	}

	@Override
	public String getInportDescription(int index) {
		switch (index) {
		case 0:
			return "Molecules to render cuttable bonds";
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
		return "MMP Show Matching Bonds (" + fragUtilityFactory.getToolkitName() + ")";
	}

	@Override
	public String getOutportDescription(int index) {
		switch (index) {
		case 0:
			return "Molecules with matching bonds for the cut schema highlighted";
		default:
			return null;
		}
	}

	@Override
	public String getOutportName(int index) {
		switch (index) {
		case 0:
			return "Matching Bonds";
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
		node.setType(org.knime.node.v41.NodeType.MANIPULATOR);
		node.setShortDescription("This node renders all bonds matching the indicated bond type");
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		introCursor.insertElementWithText("p",
				"This node renders all bonds matching the indicated bond type. The user "
						+ "can specify the number of cuts to be made (1 - 10), and whether "
						+ "Hydrogens should be added (1 cut only)");
		addFragmentationOptionsDiscription(introCursor);

		addRSmartsGuidelines(introCursor);

		introCursor.insertElementWithText("p", "The algorithm is implemented using the "
				+ fragUtilityFactory.getToolkitName() + " toolkit");

		addDevelopedByVernalis(introCursor);

		insertReference(introCursor, 1, "S. D. Roughley and A. M. Jordan",
				"The Medicinal Chemist�s Toolbox: An Analysis of Reactions Used in the Pursuit of Drug Candidates",
				"J. Med. Chem.", 2011, 54, "3451-3479", "10.1021/jm200187y");
		insertReference(introCursor, 2, "J. Hussain and C Rea",
				"Computationally efficient algorithm to identify matched molecular pairs"
						+ " (MMPs) in large datasets",
				"J. Chem. Inf. Model.", 2010, 50, "339-348", "10.1021/ci900450m");

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

		addOptionWithoutTab(fullDesc, "Select Molecule column",
				"Select the column containing the molecules");
		addOptionWithoutTab(fullDesc, "Select the Fragmentation Type",
				"Select the required fragmentation option");
		addOptionWithoutTab(fullDesc, "User SMARTS",
				"The optional user-defined (r)SMARTS (see above for details)");
		addOptionWithoutTab(fullDesc, "Add H's prior to fragmentation",
				"If checked, pairs with -H as a substituent will be included. "
						+ "This is recommended for when the number of cuts is 1, "
						+ "and is unavailable for other values");
		addOptionWithoutTab(fullDesc, "Matching bond highlight colour",
				"The colour to highlight matching bonds");

		Ports ports = node.addNewPorts();

		InPort inport = ports.addNewInPort();
		inport.setIndex(BigInteger.ZERO);
		inport.setName(getInportName(0));
		inport.newCursor().setTextValue(getInportDescription(0));

		OutPort outport = ports.addNewOutPort();
		outport.setIndex(BigInteger.ZERO);
		outport.setName(getOutportName(0));
		outport.newCursor().setTextValue(getOutportDescription(0));

		if (getViewCount() > 0) {
			Views views = node.addNewViews();
			for (int i = 0; i < getViewCount(); i++) {
				View view = views.addNewView();
				view.setIndex(BigInteger.valueOf(i));
				view.setName(getViewName(i));
				view.newCursor().setTextValue(getViewDescription(i));
			}
		}

		return (Element) node.getDomNode();

	}
}
