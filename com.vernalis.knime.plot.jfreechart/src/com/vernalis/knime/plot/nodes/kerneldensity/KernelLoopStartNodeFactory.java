/*******************************************************************************
 * Copyright (c) 2019, 2023, Vernalis (R&D) Ltd
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
package com.vernalis.knime.plot.nodes.kerneldensity;

import static com.vernalis.knime.nodes.NodeDescriptionUtils.addBundleInformation;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionWithoutTab;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_ESTIMATOR;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_SYMMETRY;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelLoopStartNodeDialog.IS_MULTI_DIMENSIONAL;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelLoopStartNodeDialog.KERNEL_ESTIMATORS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelLoopStartNodeDialog.KERNEL_SYMMETRIES;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.node.v41.FullDescription;
import org.knime.node.v41.InPort;
import org.knime.node.v41.Intro;
import org.knime.node.v41.KnimeNode;
import org.knime.node.v41.KnimeNodeDocument;
import org.knime.node.v41.OutPort;
import org.knime.node.v41.Ports;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vernalis.knime.nodes.NodeDescriptionUtils.TableFactory;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

/**
 * Node Factory for the Kernel Loop Start Node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelLoopStartNodeFactory
		extends NodeFactory<KernelLoopStartNodeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public KernelLoopStartNodeModel createNodeModel() {
		return new KernelLoopStartNodeModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<KernelLoopStartNodeModel> createNodeView(int viewIndex,
			KernelLoopStartNodeModel nodeModel) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new KernelLoopStartNodeDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new VernalisDelegateNodeDescription(new NodeDescription() {

			@Override
			public Element getXMLDescription() {
				KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
				KnimeNode node = doc.addNewKnimeNode();
				node.setIcon(getIconPath());
				node.setName(getNodeName());
				node.setType(org.knime.node.v41.NodeType.VISUALIZER);
				node.setShortDescription(
						"This node allows simple looping through various Kernel Estimators and symmetries");
				FullDescription fullDesc = node.addNewFullDescription();
				Intro intro = fullDesc.addNewIntro();

				XmlCursor introCursor = intro.newCursor();
				introCursor.toFirstContentToken();

				introCursor.insertElementWithText("p",
						"This node allows the user to select one or more Kernel Estimators "
								+ "and one or more Kernel Symmetries (if the '"
								+ IS_MULTI_DIMENSIONAL
								+ "' option is selected). "
								+ "All combinations are looped through, with the values in each in "
								+ "the 'Kernel Estimator' and 'Kernel Symmetry' flow variables.");

				introCursor.insertElementWithText("h2", "Kernel Estimators");
				TableFactory tf =
						new TableFactory("Name", "Function").setPreTableText(
								"A variety of kernel estimators are available, as shown in the table:");
				for (KernelEstimator kEst : KernelEstimator.values()) {
					tf.addRowToTable(kEst.name(), kEst.getDescription());
				}
				tf.buildTable(introCursor);

				tf = new TableFactory("Name", "Function")
						.setPreTableText("In the 2D case, u is a vector.  The '"
								+ KERNEL_SYMMETRY
								+ "' option controls how the 1-dimensional '"
								+ KERNEL_ESTIMATOR
								+ "' is applied, as shown in the table");
				for (KernelSymmetry kSymm : KernelSymmetry.values()) {
					tf.addRowToTable(kSymm.name(), kSymm.getDescription());
				}
				tf.buildTable(introCursor);

				addDevelopedByVernalis(introCursor);
				introCursor.dispose();

				addOptionWithoutTab(fullDesc, KERNEL_ESTIMATORS,
						"The Kernel Estimator(s) to loop through");
				addOptionWithoutTab(fullDesc, KERNEL_SYMMETRIES,
						"The kernel Symmetries looped through if '"
								+ IS_MULTI_DIMENSIONAL + "' is selected");

				Ports ports = node.addNewPorts();

				InPort inport = ports.addNewInPort();
				inport.setIndex(BigInteger.ZERO);
				inport.setName(getInportName(0));
				inport.newCursor().setTextValue(getInportDescription(0));

				OutPort outport = ports.addNewOutPort();
				outport.setIndex(BigInteger.ZERO);
				outport.setName(getOutportName(0));
				outport.newCursor().setTextValue(getOutportDescription(0));
				addBundleInformation(node,
						KernelLoopStartNodeFactory.this.getClass());
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

			@Override
			public int getViewCount() {
				return 0;
			}

			@Override
			public NodeType getType() {
				return NodeType.LoopStart;
			}

			@Override
			public String getOutportName(int index) {
				return "Unchanged input table";
			}

			@Override
			public String getOutportDescription(int index) {
				return "The input unchanged input table, with flow variables "
						+ "for Kernel Estimator and optionally Kernel Symmetry";
			}

			@Override
			public String getNodeName() {
				return "Kernel Loop Start";
			}

			@Override
			public String getInteractiveViewName() {
				return null;
			}

			@Override
			public String getInportName(int index) {
				return "Input table";
			}

			@Override
			public String getInportDescription(int index) {
				return "Input table for Kernel Density analysis";
			}

			@Override
			public String getIconPath() {
				return "KernelLoopStart.png";
			}
		}, getClass());
	}

}
