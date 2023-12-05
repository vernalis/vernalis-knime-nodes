/*******************************************************************************
 * Copyright (c) 2016, 2023, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.core.node.BufferedDataTable;
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
import org.knime.node.v41.Tab;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vernalis.knime.nodes.NodeDescriptionUtils;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

/**
 * <code>NodeFactory</code> for the "TimingStart" Node. Loop start for execution
 * timing
 *
 * @author S. Roughley
 */
/**
 * @author S.Roughley
 *
 */
public class AbstractPerfMonTimingStartNodeFactory
		extends NodeFactory<AbstractPerfMonTimingStartNodeModel> {

	private final int numPorts;
	private final String portTypeDescription;
	private final PortType portType;
	private final boolean isMemMon;

	/**
	 * Simple constructor, for default port type
	 * ({@link BufferedDataTable#TYPE}), non-memory monitoring nodes
	 * 
	 * @param numPorts
	 *            The number of ports
	 */
	public AbstractPerfMonTimingStartNodeFactory(int numPorts) {
		this(numPorts, false);
	}

	/**
	 * Constructor for default port type ({@link BufferedDataTable#TYPE}), with
	 * the option to have memory monitoring
	 * 
	 * @param numPorts
	 *            The number of ports
	 * @param isMemMon
	 *            Is the loop a memory-monitoring loop?
	 */
	public AbstractPerfMonTimingStartNodeFactory(int numPorts,
			boolean isMemMon) {
		this(numPorts, isMemMon, BufferedDataTable.TYPE, "Data table");
	}

	/**
	 * Constructor allowing specification of port type in addition to number of
	 * ports and memory monitoring option. The port description in the node
	 * description will use the result of {@link PortType#getName()}
	 * 
	 * @param numPorts
	 *            The number of ports
	 * @param isMemMon
	 *            Is the loop a memory-monitoring loop?
	 * @param portType
	 *            The port type
	 */
	public AbstractPerfMonTimingStartNodeFactory(int numPorts, boolean isMemMon,
			PortType portType) {
		this(numPorts, isMemMon, portType, portType.getName());
	}

	/**
	 * Full constructor allowing control of all options
	 * 
	 * @param numPorts
	 *            The number of ports
	 * @param isMemMon
	 *            Is the loop a memory-monitoring loop?
	 * @param portType
	 *            The port type
	 * @param portTypeDescription
	 *            The port type as shown in the node description
	 */
	public AbstractPerfMonTimingStartNodeFactory(int numPorts, boolean isMemMon,
			PortType portType, String portTypeDescription) {
		super(true);
		this.numPorts = numPorts;
		this.isMemMon = isMemMon;
		this.portType = portType;
		this.portTypeDescription = portTypeDescription;
		init();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractPerfMonTimingStartNodeModel createNodeModel() {
		return isMemMon()
				? new AbstractMemMonPerfLoopStartNodeModel(getPortType(),
						getNumPorts())
				: new AbstractPerfMonTimingStartNodeModel(getPortType(),
						getNumPorts());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNrNodeViews() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeView<AbstractPerfMonTimingStartNodeModel> createNodeView(
			final int viewIndex,
			final AbstractPerfMonTimingStartNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasDialog() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NodeDialogPane createNodeDialogPane() {
		return isMemMon() ? new AbstractMemMonPerfLoopStartNodeDialog()
				: new AbstractPerfMonTimingStartNodeDialog();
	}

	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		NodeDescription retVal = new NodeDescription() {

			@Override
			public String getIconPath() {
				return isMemMon() ? "Benchmarking_Start_Mon.png"
						: "Benchmarking_Start.png";
			}

			@Override
			public String getInportDescription(int index) {
				if (getNumPorts() == 1 && index == 0) {
					return "Input " + getPortTypeDescription();
				}
				return "Input " + getPortTypeDescription() + " " + index;

			}

			@Override
			public String getInportName(int index) {
				return getInportDescription(index);
			}

			@Override
			public String getInteractiveViewName() {
				return null;
			}

			@Override
			public String getNodeName() {
				StringBuilder sb = new StringBuilder("Benchmark Start");
				if (getPortType() != BufferedDataTable.TYPE) {
					sb.append(" (").append(getPortTypeDescription())
							.append(')');
				}
				if (isMemMon()) {
					sb.append(" (Memory Monitoring)");
				}
				if (getNumPorts() > 1) {
					sb.append(" (").append(getNumPorts()).append(" ports)");
				}
				if (isDeprecated()) {
					sb.append(" (Deprecated)");
				}
				return sb.toString();
			}

			@Override
			public String getOutportDescription(int index) {
				if (getNumPorts() == 1 && index == 0) {
					return "Unchanged Input " + getPortTypeDescription();
				}
				return "Unchanged Input " + getPortTypeDescription() + " "
						+ index;

			}

			@Override
			public String getOutportName(int index) {
				return getOutportDescription(index);
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
				node.setDeprecated(false);
				node.setIcon(getIconPath());
				node.setName(getNodeName());
				node.setType(org.knime.node.v41.NodeType.LOOP_START);
				node.setShortDescription("Loop start for execution timing"
						+ (isMemMon() ? "with memory monitoring" : ""));
				FullDescription fullDesc = node.addNewFullDescription();
				Intro intro = fullDesc.addNewIntro();

				XmlCursor introCursor = intro.newCursor();
				introCursor.toFirstContentToken();

				introCursor.insertElementWithText("p",
						"This node provides the start of a timing block for monitoring "
								+ "performance of a node or nodes. It needs to be paired with a "
								+ "corresponding Performance Monitoring Timing End "
								+ (isMemMon() ? "(Memory monitoring) " : "")
								+ "node.");

				introCursor.insertElementWithText("p",
						"The incoming " + getPortTypeDescription()
								+ "s are passed through unchanged");
				introCursor.insertElementWithText("p",
						"Execution can be halted either by the number of executions in the "
								+ "loop start node being passed, or the optional timeout time (again "
								+ "from the loop start node) being passed.");

				introCursor.insertElementWithText("p",
						"NB The timeout will not halt intermediate node execution, only "
								+ "prevent a new iteration starting.");

				NodeDescriptionUtils.addDevelopedByVernalis(introCursor);

				introCursor.dispose();

				Tab optTab =
						NodeDescriptionUtils.createTab(fullDesc, "Options");
				NodeDescriptionUtils.addOptionToTab(optTab,
						"Number of iterations",
						"The number of loop iterations");

				Tab timeoutTab =
						NodeDescriptionUtils.createTab(fullDesc, "Timeout");
				NodeDescriptionUtils.addOptionToTab(timeoutTab,
						"Stop tests after timeout period?",
						"Should the loops stop if the timeout period expires before the indicated number of loops "
								+ "has executed?  NB Does not stop loop execution mid-loop");
				NodeDescriptionUtils.addOptionToTab(timeoutTab,
						"Maximum total time (s)",
						"The maximum loop iteration time if the above option is selected");

				Tab loopBodyTab = NodeDescriptionUtils.createTab(fullDesc,
						"Loop-body nodes");
				NodeDescriptionUtils.addOptionToTab(loopBodyTab,
						"Report node times",
						"Should the times of the individual loop body nodes be reported?  "
								+ "This uses the timings shown in the 'Timer info...' node");
				NodeDescriptionUtils.addOptionToTab(loopBodyTab,
						"Probe wrapped metanodes timings",
						"If the above option is selected, this option allows the timings of individual "
								+ "nodes within wrapped metanodes (and other subnodes) to be individually probed.  "
								+ "The total time for wrapped metanodes and subnodes are always reported. "
								+ "Metanodes are always probed, as they provide no useful top level NodeTimer "
								+ "information.  In the timings output table of the loop end, nested nodes have their paths separated by ' --> '");
				if (isMemMon()) {
					Tab memMonTab = NodeDescriptionUtils.createTab(fullDesc,
							"Memory monitoring");
					NodeDescriptionUtils.addOptionToTab(memMonTab,
							"Interval between memory monitoring checks (ms)",
							"The interval to use between memory use checks during loop execution");
				}

				Ports ports = node.addNewPorts();
				for (int i = 0; i < getNumPorts(); i++) {
					InPort inPort = ports.addNewInPort();
					inPort.setIndex(BigInteger.valueOf(i));
					inPort.setName(getInportName(i));
					inPort.newCursor().setTextValue(getInportDescription(i));
				}
				for (int i = 0; i < getNumPorts() + (isMemMon() ? 3 : 2); i++) {
					OutPort outport = ports.addNewOutPort();
					outport.setIndex(BigInteger.valueOf(i));
					outport.setName(getOutportName(i));
					outport.newCursor().setTextValue(getOutportDescription(i));
				}
				NodeDescriptionUtils.addBundleInformation(node,
						AbstractPerfMonTimingEndNodeFactory.class);
				return (Element) node.getDomNode();
			}
		};
		return new VernalisDelegateNodeDescription(retVal, getClass());
	}

	/**
	 * @return the numPorts
	 */
	protected int getNumPorts() {
		return numPorts;
	}

	/**
	 * @return the portTypeDescription
	 */
	protected String getPortTypeDescription() {
		return portTypeDescription;
	}

	/**
	 * @return the portType
	 */
	protected PortType getPortType() {
		return portType;
	}

	/**
	 * @return the isMemMon
	 */
	protected boolean isMemMon() {
		return isMemMon;
	}

}
