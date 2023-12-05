/*******************************************************************************
 * Copyright (c) 2018, 2023, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.abstrct.crossover;

import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionWithoutTab;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;
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
import org.xml.sax.SAXException;

import com.vernalis.knime.flowcontrol.nodes.abstrct.varvalifswitch.AbstractFvvalIfSwitchNodeDialog;
import com.vernalis.knime.nodes.NodeDescriptionUtils;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

public abstract class AbstractCrossoverNodeFactory
		extends NodeFactory<AbstractCrossoverNodeModel> {

	private final class CrossoverNodeDescription extends NodeDescription {

		CrossoverNodeDescription() {
			setIsDeprecated(true);
		}

		@Override
		public String getIconPath() {
			final String name = portType.getName();
			if (!"Data".equals(name)) {
				return name + "_crossover.png";
			}
			return "crossover.png";
		}

		@Override
		public String getInportDescription(int index) {
			switch (index) {
				case 0:
					return "First incoming table";
				case 1:
					return "Second incoming table";
				default:
					return null;
			}
		}

		@Override
		public String getInportName(int index) {
			switch (index) {
				case 0:
					return "Table 0";
				case 1:
					return "Table 1";
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
			final String name = portType.getName();
			String retVal;
			if (!"Data".equals(name)) {
				retVal = "Crossover " + portType.getName()
						+ " (Flow Variable Value)";
			} else {
				retVal = "Crossover (Flow Variable Value)";
			}
			return retVal + " (Deprecated)";
		}

		@Override
		public String getOutportDescription(int index) {
			switch (index) {
				case 0:
					return "First input table if comparison is true, otherwise second input table";
				case 1:
					return "Second input table if comparison is true, otherwise first input table";
				default:
					return null;
			}
		}

		@Override
		public String getOutportName(int index) {
			switch (index) {
				case 0:
					return "Output table 0";
				case 1:
					return "Output table 1";
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
		public Element getXMLDescription() {
			KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
			KnimeNode node = doc.addNewKnimeNode();
			node.setIcon(getIconPath());
			node.setName(getNodeName());
			node.setType(org.knime.node.v41.NodeType.MANIPULATOR);
			node.setShortDescription(
					"This node optionally swaps the two input tables, "
							+ "depending on the the result of a flow variable value comparison");
			FullDescription fullDesc = node.addNewFullDescription();
			Intro intro = fullDesc.addNewIntro();

			XmlCursor introCursor = intro.newCursor();
			introCursor.toFirstContentToken();
			introCursor.insertElementWithText("p",
					"This node either returns the input tables unchanged, "
							+ "if the result of the flow variable value comparison is 'true', "
							+ "or swaps them ('crosses over') if the comparison result is 'false'");
			introCursor.insertElementWithText("p",
					"If numeric variables (double or integer types) are selected, the node will "
							+ "attempt to process the user-entered value as a number. If this cannot be done, "
							+ "then the node will not be executable. An empty string will not be interpreted "
							+ "as a numeric value. Note that integers can not be compared with values where "
							+ "a '.' is present, so '1.0' cannot be interpreted, but '1' can.");
			introCursor.insertElementWithText("p",
					"For doubles, a threshold is specified in the dialogue. If the variable is within this "
							+ "threshold of the comparison value it will be considered to be equal to it (Applies "
							+ "to =, !=, >= and <= comparisons).");
			introCursor.insertElementWithText("p",
					"String comparisons are 'lexicographical' - i.e. the sorting order mirrors that "
							+ "in which the strings would be expected to appear in a dictionary. ");
			addDevelopedByVernalis(introCursor);

			introCursor.dispose();
			addOptionWithoutTab(fullDesc,
					"Flow variable selections (un-named in dialogue)",
					"Select from the available flow variables");
			addOptionWithoutTab(fullDesc,
					"Comparison operator selection (un-named in dialogue)",
					"Select the required comparison operation");
			addOptionWithoutTab(fullDesc,
					"Comparison value (un-named in dialogue)",
					"Enter a value to compare against");
			addOptionWithoutTab(fullDesc, "Ignore case",
					"Ignore the case (String comparisons only)");
			addOptionWithoutTab(fullDesc, "Ignore leading/trailing whitespace",
					"Ignore leading/trailing white space characters (String comparisons only)");
			addOptionWithoutTab(fullDesc, "Equality tolerance",
					"The maximum difference between double values within which they "
							+ "will still be considered to be equal (Double comparisons only)");

			Ports ports = node.addNewPorts();

			InPort inport = ports.addNewInPort();
			String portName;
			int portIdx = 0;
			while ((portName = getInportName(portIdx)) != null) {
				inport.setIndex(BigInteger.valueOf(portIdx));
				inport.setName(portName);
				inport.newCursor()
						.setTextValue(getInportDescription(portIdx++));
			}
			OutPort outport = ports.addNewOutPort();
			portIdx = 0;
			while ((portName = getOutportName(portIdx)) != null) {
				outport.setIndex(BigInteger.valueOf(portIdx));
				outport.setName(portName);
				outport.newCursor()
						.setTextValue(getOutportDescription(portIdx++));
			}

			if (getViewCount() > 0) {
				Views views = node.addNewViews();
				for (int i = 0; i < getViewCount(); i++) {
					View view = views.addNewView();
					view.setIndex(BigInteger.valueOf(i));
					view.setName(getViewName(i));
					view.newCursor().setTextValue(getViewDescription(i));
				}
			}
			NodeDescriptionUtils.addBundleInformation(node,
					AbstractCrossoverNodeFactory.class);
			return (Element) node.getDomNode();

		}

		@Override
		public String getViewName(int index) {
			return null;
		}

		@Override
		public String getViewDescription(int index) {
			return null;
		}
	}

	private final PortType portType;

	/**
	 * @param portType
	 */
	protected AbstractCrossoverNodeFactory(PortType portType) {
		super(true);
		this.portType = portType;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new VernalisDelegateNodeDescription(
				new CrossoverNodeDescription(), getClass());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public AbstractCrossoverNodeModel createNodeModel() {
		return new AbstractCrossoverNodeModel(portType);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AbstractCrossoverNodeModel> createNodeView(int viewIndex,
			AbstractCrossoverNodeModel nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractFvvalIfSwitchNodeDialog();
	}

}
