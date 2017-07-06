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
package com.vernalis.knime.mmp.nodes.transform.abstrct;

import static com.vernalis.knime.mmp.nodes.abstrct.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.mmp.nodes.abstrct.NodeDescriptionUtils.addOptionToTab;

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

import com.vernalis.knime.mmp.transform.TransformUtilityFactory;

/**
 * Node description class for the apply transform nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the query molecule object
 * @param <V>
 *            The type of the reaction/transform object
 */
public class AbstractApplyTransformNodeDescription<T, U, V> extends NodeDescription {
	private final TransformUtilityFactory<T, U, V> transformUtilityFactory;

	/**
	 * Constructor
	 * 
	 * @param transformUtilityFactory
	 *            The {@link TransformUtilityFactory} instance for the node
	 */
	public AbstractApplyTransformNodeDescription(
			TransformUtilityFactory<T, U, V> transformUtilityFactory) {
		super();
		this.transformUtilityFactory = transformUtilityFactory;
	}

	@Override
	public String getIconPath() {
		return getClass().getResource("MMPApplyTransforms.png").getFile();
	}

	@Override
	public String getInportDescription(int index) {
		switch (index) {
		case 0:
			return "The table containing molecules to transform";
		case 1:
			return "The table containing transforms";
		default:
			return null;
		}
	}

	@Override
	public String getInportName(int index) {
		switch (index) {
		case 0:
			return "Molecules";
		case 1:
			return "Transforms";
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
		return "Apply Transforms (" + transformUtilityFactory.getToolkitName() + ") (Experimental)";
	}

	@Override
	public String getOutportDescription(int index) {
		switch (index) {
		case 0:
			return "Table containing transformed molecules. The table is ordered by "
					+ "transform then by input molecule";
		default:
			return null;
		}
	}

	@Override
	public String getOutportName(int index) {
		switch (index) {
		case 0:
			return "Transformed molecules";
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
		return 2;
	}

	@Override
	public String getViewDescription(int index) {
		switch (index) {
		case 0:
			return "A view showing the overall node execution progress, along with "
					+ "the progress of individual current transforms. The tool tip for the "
					+ "transform ID shows the transform SMARTS";
		case 1:
			return "A view showing the overall node execution progress, along with "
					+ "the progress of individual current transforms. The transforms are "
					+ "rendered using the default SMARTS cell renderer. Holding down the "
					+ "'Shift' key whilst resizing the column preserves/restores the 2:1 "
					+ "aspect ratio";
		default:
			return null;
		}
	}

	@Override
	public String getViewName(int index) {
		switch (index) {
		case 0:
			return "Progress view";
		case 1:
			return "Enhanced progress view";
		default:
			return null;
		}
	}

	@Override
	public Element getXMLDescription() {
		KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
		KnimeNode node = doc.addNewKnimeNode();
		node.setIcon(getIconPath());
		node.setName(getNodeName());
		node.setType(KnimeNode.Type.MANIPULATOR);
		node.setShortDescription(
				"This node applies Matched Pair transforms to an input table of molecules");
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		introCursor.insertElementWithText("p",
				"This node transforms input structures according to the Matched "
						+ "Molecular Pair transforms generated in the incoming table. If a "
						+ "transform matches more than one position in the molecule, then the "
						+ "transform is applied to each position singly - multiple "
						+ "transformation combinations are not applied.  The implementation is "
						+ "still experimental at present and new features may be added in future "
						+ "versions");
		introCursor.insertElementWithText("p",
				"The transforms can be filtered according to environment "
						+ "similarity, in which case a transform is applied whenever any set of "
						+ "fingerprints (if the transform occurs more than once) match the "
						+ "criteria. Where the transform matches multiple positions in the "
						+ "molecule, only those positions which match the criteria are "
						+ "transformed");
		introCursor.insertElementWithText("p",
				"The output table will always contain at least 3 columns - "
						+ "the transformed molecule, the incoming molecule and the rSMARTS transform applied to "
						+ "effect the transformation. Additionally, columns from the transform table and molecules "
						+ "table can also be passed through. In the case of transform table columns, these will be "
						+ "grouped on the transform into collection cells.");
		introCursor.beginElement("p");
		introCursor
				.insertChars("NB - Multiple different but overlapping transforms may transform a "
						+ "molecule into the same structure - the node does not check for this "
						+ "scenario (e.g. [*:1]-!@OC>>[*:1]OC(F)(F)F and "
						+ "[*:1]-!@C>>[*:1]C(F)(F)F) will both transform PhOMe to PhOCF");
		introCursor.insertElementWithText("sub", "3");
		introCursor.toEndToken();
		introCursor.toNextToken();

		addDevelopedByVernalis(introCursor);

		introCursor.dispose();

		Tab tab = fullDesc.addNewTab();
		tab.setName("Molecule Options");
		addOptionToTab(tab, "Select Molecule column",
				"Select the column containing the incoming molecules to transform");

		addOptionToTab(tab, "Molecule pass-through columns",
				"Any columns associated with the molecule to be "
						+ "kept in the output table. The Molecule column is always kept, "
						+ "regardless of it's position here");

		tab = fullDesc.addNewTab();
		tab.setName("Transform Options");

		addOptionToTab(tab, "Select transform column",
				"The column containing the rSMARTS Transforms for " + "each Matched Pair");
		addOptionToTab(tab, "Transforms are sorted",
				"Checking this indicates that the transform table "
						+ "is pre-sorted by the rSMARTS column. Checking this option when it is "
						+ "not may result in a transform being applied multiple times.");
		addOptionToTab(tab, "Attempt to create enantiomeric products",
				"Where a transform could generate a pair of "
						+ "enantiomeric products, should this be attempted? There is a time "
						+ "penalty as each transform has to be applied 2^n times (where n is the number "
						+ "of attachment points in the transform), and there is no "
						+ "way of knowing in advance which positions need to be duplicated. For "
						+ "example, the transform '[*:1]-!@[H]>>[*:1]-[Cl]' when applied to the "
						+ "SMILES string 'N1CCCC1' can generate, amongst others, the "
						+ "enantiomeric pair 'N1[C@H]([Cl])CCC1' and 'N1[C@@H]([Cl])CCC1' - "
						+ "without selecting this option, only 'N1C(Cl)CCC1' will be generated.  "
						+ "NB At present, double bond geometry is not created, and only the atom which "
						+ "replaces the attachment point is enantomerically enumerated due "
						+ "to toolkit limitations");
		addOptionToTab(tab, "Transform pass-through columns",
				"Any columns associated with the transform to be "
						+ "kept in the output table. The transform column is always kept, "
						+ "regardless of the setting here");
		addOptionToTab(tab, "Filter by transform environment",
				"Should the transform only be applied to "
						+ "environments matching the attachment point key fingerprints "
						+ "according to the criteria specified?");
		addOptionToTab(tab, "First Key Attachment point Fingerprint Column",
				"The first attachment point column index for the " + "transform reactant ('(L)')");
		addOptionToTab(tab, "Similarity metric settings",
				"The similarity comparison type. For the "
						+ "asymmetric Tversky similarity, the similarity comparison is from the "
						+ "transform fingerprint to the molecule fingerprint");
		addOptionToTab(tab, "Threshold", "The minimum similarity to allow");
		addOptionToTab(tab, "Alpha", "Tversky similarity \u03B1 coefficient");
		addOptionToTab(tab, "Beta", "The Tversky similarity \u03B2 coefficient");
		addOptionToTab(tab, "AP Fingerprint Comparison Type",
				"When there is more than 1 cut, this setting "
						+ "determines how the comparison will be performed - requiring all "
						+ "attachment point or any attachment point environment to match, or "
						+ "the overall concatenated environment to match");

		Ports ports = node.addNewPorts();

		InPort inport = ports.addNewInPort();
		inport.setIndex(0);
		inport.setName(getInportName(0));
		inport.newCursor().setTextValue(getInportDescription(0));

		inport = ports.addNewInPort();
		inport.setIndex(1);
		inport.setName(getInportName(1));
		inport.newCursor().setTextValue(getInportDescription(1));

		OutPort outport = ports.addNewOutPort();
		outport.setIndex(0);
		outport.setName(getOutportName(0));
		outport.newCursor().setTextValue(getOutportDescription(0));

		Views views = node.addNewViews();
		View view = views.addNewView();
		view.setIndex(0);
		view.setName(getViewName(0));
		view.newCursor().setTextValue(getViewDescription(0));

		view = views.addNewView();
		view.setIndex(1);
		view.setName(getViewName(1));
		view.newCursor().setTextValue(getViewDescription(1));

		return (Element) node.getDomNode();

	}
}
