/*******************************************************************************
 * Copyright (c) 2019,2020 Vernalis (R&D) Ltd
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

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.port.PortType;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.InPortDocument.InPort;
import org.knime.node2012.IntroDocument.Intro;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.OutPortDocument.OutPort;
import org.knime.node2012.PortsDocument.Ports;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vernalis.knime.nodes.NodeDescriptionUtils;

/**
 * Abstract base NodeFactory class for all Performance Monitoring Loop ends
 * v1.27.0 - Added node settings with option to use new date-time classes
 *
 * @author S.Roughley
 *
 * @param <T> The Type of the NodeModel
 */
public abstract class AbstractPerfMonTimingEndNodeFactory extends NodeFactory<AbstractPerfMonTimingEndNodeModel> {

	private final int numPorts;
	private final String portTypeDescription;
	private final PortType portType;
	private final boolean isMemMon;
	private static final String[] names = { "First", "Second", "Third", "Fourth", "Fifth", "Sixth" };

	/**
	 * Simple constructor, for default port type ({@link BufferedDataTable#TYPE}),
	 * non-memory monitoring nodes
	 *
	 * @param numPorts The number of ports
	 */
	public AbstractPerfMonTimingEndNodeFactory(int numPorts) {
		this(numPorts, false);
	}

	/**
	 * Constructor for default port type ({@link BufferedDataTable#TYPE}), with the
	 * option to have memory monitoring
	 *
	 * @param numPorts The number of ports
	 * @param isMemMon Is the loop a memory-monitoring loop?
	 */
	public AbstractPerfMonTimingEndNodeFactory(int numPorts, boolean isMemMon) {
		this(numPorts, isMemMon, BufferedDataTable.TYPE);
	}

	/**
	 * Constructor allowing specification of port type in addition to number of
	 * ports and memory monitoring option. The port description in the node
	 * description will use the result of {@link PortType#getName()}
	 *
	 * @param numPorts The number of ports
	 * @param isMemMon Is the loop a memory-monitoring loop?
	 * @param portType The port type
	 */
	public AbstractPerfMonTimingEndNodeFactory(int numPorts, boolean isMemMon, PortType portType) {
		this(numPorts, isMemMon, portType, portType.getName());
	}

	/**
	 * Full constructor allowing control of all options
	 *
	 * @param numPorts            The number of ports
	 * @param isMemMon            Is the loop a memory-monitoring loop?
	 * @param portType            The port type
	 * @param portTypeDescription The port type as shown in the node description
	 */
	public AbstractPerfMonTimingEndNodeFactory(int numPorts, boolean isMemMon, PortType portType,
			String portTypeDescription) {
		super(true);
		this.numPorts = numPorts;
		this.isMemMon = isMemMon;
		this.portType = portType;
		this.portTypeDescription = portTypeDescription;
		init();
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<AbstractPerfMonTimingEndNodeModel> createNodeView(int viewIndex,
			AbstractPerfMonTimingEndNodeModel nodeModel) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractPerfMonTimingEndNodeModel createNodeModel() {
		return isMemMon() ? new AbstractMemMonPerfLoopEndNodeModel(getPortType(), getNumPorts())
				: new AbstractPerfMonTimingEndNodeModel(getPortType(), getNumPorts());
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractPerfMonTimingEndNodeDialog();
	}

	@Override
	protected NodeDescription createNodeDescription() throws SAXException, IOException, XmlException {
		final NodeDescription retVal = new NodeDescription() {

			@Override
			public String getIconPath() {
				return isMemMon() ? "Benchmarking_End_Mon.png" : "Benchmarking_End.png";
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
				final StringBuilder sb = new StringBuilder("Benchmark End");
				if (getPortType() != BufferedDataTable.TYPE) {
					sb.append(" (").append(getPortTypeDescription()).append(')');
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
				switch (index) {
				case 0:
					return "Flow variable port with summary statistics added";
				case 1:
					return "Data table listing timings for each iteration";
				case 2:
					if (isMemMon()) {
						return "Data table listing the memory usage during loop execution";
					}
				default:
					final int modifiedIndex = index - (isMemMon() ? 3 : 2);
					if (getNumPorts() == 1 && modifiedIndex == 0) {
						return "Unmodified " + getPortTypeDescription() + " from final iteration";
					}
					if (modifiedIndex < names.length) {
						return "Unmodified " + names[modifiedIndex] + " " + getPortTypeDescription()
						+ " from final iteration";
					}
					return "Unmodified " + modifiedIndex + "th " + getPortTypeDescription() + " from final iteration";
				}

			}

			@Override
			public String getOutportName(int index) {
				switch (index) {
				case 0:
					return "Flow variable port with summary statistics added";
				case 1:
					return "Data table listing timings for each iteration";
				case 2:
					if (isMemMon()) {
						return "Data table listing the memory usage during loop execution";
					}
				default:
					final int modifiedIndex = index - (isMemMon() ? 3 : 2);
					if (getNumPorts() == 1 && modifiedIndex == 0) {
						return "Unmodified " + getPortTypeDescription();
					}
					if (index < names.length) {
						return "Unmodified " + getPortTypeDescription() + " " + modifiedIndex;
					}
					return "Unmodified " + modifiedIndex + "th " + getPortTypeDescription() + " from final iteration";
				}
			}

			@Override
			public NodeType getType() {
				return NodeType.LoopEnd;
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
				final KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
				final KnimeNode node = doc.addNewKnimeNode();
				node.setDeprecated(KnimeNode.Deprecated.FALSE);
				node.setIcon(getIconPath());
				node.setName(getNodeName());
				node.setType(KnimeNode.Type.LOOP_END);
				node.setShortDescription(
						"Loop end for execution timing" + (isMemMon() ? "with memory monitoring" : ""));
				final FullDescription fullDesc = node.addNewFullDescription();
				final Intro intro = fullDesc.addNewIntro();

				final XmlCursor introCursor = intro.newCursor();
				introCursor.toFirstContentToken();

				introCursor.beginElement("p");
				introCursor.insertChars("This node provides the end of a timing block for monitoring "
						+ "performance of a node or nodes. It needs to be paired with a "
						+ "corresponding Performance Monitoring Timing Start "
						+ (isMemMon() ? "(Memory monitoring) " : "")
						+ "node. The node passes through the last iteration's "
						+ "occurrence of the input tables. An additional flow variable port with "
						+ "the following variable is added: ");
				introCursor.beginElement("ul");
				introCursor.insertElementWithText("li", "Overall Start Time");
				introCursor.insertElementWithText("li", "Overall End Time");
				introCursor.insertElementWithText("li", "Number of executions");
				introCursor.insertElementWithText("li", "Best execution time");
				introCursor.insertElementWithText("li", "Worst execution time");
				introCursor.insertElementWithText("li", "Total execution time");
				introCursor.insertElementWithText("li", "Mean execution time");
				introCursor.toEndToken(); // </ul>
				introCursor.toNextToken();
				introCursor.toEndToken();
				introCursor.toNextToken();
				introCursor.insertElementWithText("p",
						"A second table is added with the details of individual timings");
				if (isMemMon()) {
					introCursor.insertElementWithText("p",
							"A third table is added with the details details of memory usage");
				}
				introCursor.insertElementWithText("p", "The incoming " + getPortTypeDescription()
				+ "s are passed through unchanged from the final loop iteration");
				introCursor.insertElementWithText("p",
						"Execution can be halted either by the number of executions in the "
								+ "loop start node being passed, or the optional timeout time (again "
								+ "from the loop start node) being passed.");
				introCursor.insertElementWithText("p", "NB The timeout will not halt intermediate node execution, only "
						+ "prevent a new iteration starting.");

				NodeDescriptionUtils.addDevelopedByVernalis(introCursor);

				introCursor.dispose();
				NodeDescriptionUtils.addOptionWithoutTab(fullDesc,
						AbstractPerfMonTimingEndNodeDialog.USE_LEGACY_DATE_TIME_FIELDS,
						"The iteration timings table " + (isMemMon() ? "and memory usage table " : "")
						+ "will use the new date and time column types unless this option is selected.  "
						+ "Selecting the option matches the historic behaviour of the node");
				final Ports ports = node.addNewPorts();
				for (int i = 0; i < getNumPorts(); i++) {
					final InPort inPort = ports.addNewInPort();
					inPort.setIndex(i);
					inPort.setName(getInportName(i));
					inPort.newCursor().setTextValue(getInportDescription(i));
				}
				for (int i = 0; i < getNumPorts() + (isMemMon() ? 3 : 2); i++) {
					final OutPort outport = ports.addNewOutPort();
					outport.setIndex(i);
					outport.setName(getOutportName(i));
					outport.newCursor().setTextValue(getOutportDescription(i));
				}
				NodeDescriptionUtils.addBundleInformation(node, AbstractPerfMonTimingEndNodeFactory.class);
				return (Element) node.getDomNode();
			}
		};
		return retVal;
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
