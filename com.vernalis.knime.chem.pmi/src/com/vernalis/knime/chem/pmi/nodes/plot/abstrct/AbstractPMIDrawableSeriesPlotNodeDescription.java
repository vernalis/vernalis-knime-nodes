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
package com.vernalis.knime.chem.pmi.nodes.plot.abstrct;

import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.DEFAULT_SHAPE;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.ITEM_SIZE;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI1_I1_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI2_I2_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.PMI_OPTIONS;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SCATTER_OPTIONS;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SHOW_COLOUR_GRADIENT_LEGEND;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SHOW_FULL_TRIANGLE;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SHOW_SHAPE_NOMINAL_COLOURS_LEGEND;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SHOW_SIZE_LEGEND;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.TRIANGLE_BOUNDS_COLOUR;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.VERTEX_LABEL_COLOUR;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addBundleInformation;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.createTab;

import java.math.BigInteger;

import org.apache.xmlbeans.XmlCursor;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.node.v41.FullDescription;
import org.knime.node.v41.InPort;
import org.knime.node.v41.Intro;
import org.knime.node.v41.KnimeNode;
import org.knime.node.v41.KnimeNodeDocument;
import org.knime.node.v41.OutPort;
import org.knime.node.v41.Ports;
import org.knime.node.v41.Tab;
import org.knime.node.v41.View;
import org.knime.node.v41.Views;
import org.w3c.dom.Element;

import com.vernalis.knime.nodes.NodeDescriptionUtils;

/**
 * The base {@link NodeDescription} class for the PMI Plot nodes
 * 
 * @author s.roughley
 *
 */
public abstract class AbstractPMIDrawableSeriesPlotNodeDescription
		extends NodeDescription {

	public static final String SHOW_FULL_TRIANGLE_DESCRIPTION =
			"Should the full PMI triangle always be shown?";
	public static final String TRIANGLE_BOUND_COLOUR_DESCRIPTION =
			"The colour of the bounding triangle on the PMI Plot";
	public static final String VERTEX_LABEL_COLOUR_DESCRIPTION =
			"The colour of the vertex labels ('Rod', 'Disc', 'Sphere') on the PMI plot";
	private final String iconName, nodeName;
	private final int scatterPortId;
	private final String[] portNames;
	private final Class<? extends NodeFactory<?>> factoryClz;

	/**
	 * Constructor
	 * 
	 * @param iconName
	 *            The relative path to the icon file (relative either to the
	 *            implementing subclass or this base class)
	 * @param nodeName
	 *            The name of the node to be displayed
	 * @param factoryClazz
	 *            The {@link Class} of the {@link NodeFactory} creating the node
	 * @param portNames
	 *            The names of any ports in addition to the scatter port. This
	 *            class automatically handles the scatter port
	 */
	protected AbstractPMIDrawableSeriesPlotNodeDescription(String iconName,
			String nodeName, Class<? extends NodeFactory<?>> factoryClazz,
			String... portNames) {
		this.iconName = iconName;
		this.nodeName = nodeName;
		this.portNames = portNames;
		scatterPortId = portNames.length;
		this.factoryClz = factoryClazz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeDescription#getOutportDescription(int)
	 */
	@Override
	public String getOutportDescription(int index) {
		if (index == 0) {
			return "The image of the plot (SVG or PNG)";
		} else {
			return null;
		}
	}

	@Override
	public String getIconPath() {
		return getClass().getResource(iconName).getFile();
	}

	@Override
	public String getInportName(int index) {
		if (index == scatterPortId) {
			return scatterPortId > 0 ? "Optional Scatter overlay PMI data"
					: "Scatter plot PMI data";
		}
		if (index >= 0 && index < portNames.length) {
			return portNames[index];
		}
		return null;
	}

	@Override
	public String getInteractiveViewName() {
		return "PMI Plot";
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeDescription#getOutportName(int)
	 */
	@Override
	public String getOutportName(int index) {
		return "PMI Plot";
	}

	@Override
	public NodeType getType() {
		return NodeType.Visualizer;
	}

	@Override
	public int getViewCount() {
		return 1;
	}

	@Override
	public String getViewName(int index) {
		return index == 0 ? getInteractiveViewName() : null;
	}

	@Override
	public Element getXMLDescription() {
		KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
		KnimeNode node = doc.addNewKnimeNode();
		node.setIcon(getIconPath());
		node.setName(getNodeName());
		node.setType(org.knime.node.v41.NodeType.VISUALIZER);
		node.setShortDescription("This node produces a PMI plot");
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();

		addNodeDescription(introCursor);

		addDevelopedByVernalis(introCursor);
		introCursor.dispose();

		prependAdditionalTabbedOptions(fullDesc);
		Tab tab = portNames.length > 0 ? createTab(fullDesc, PMI_OPTIONS)
				: createTab(fullDesc, SCATTER_OPTIONS);
		addOptionToTab(tab, VERTEX_LABEL_COLOUR,
				VERTEX_LABEL_COLOUR_DESCRIPTION);
		addOptionToTab(tab, TRIANGLE_BOUNDS_COLOUR,
				TRIANGLE_BOUND_COLOUR_DESCRIPTION);
		addOptionToTab(tab, SHOW_FULL_TRIANGLE, SHOW_FULL_TRIANGLE_DESCRIPTION);
		if (portNames.length > 0) {
			tab = createTab(fullDesc, SCATTER_OPTIONS);
		}
		addOptionToTab(tab, N_PMI1_I1_I3_COLUMN,
				"The column containing the normalised-PMI1 (nPMI1, npr1) values");
		addOptionToTab(tab, N_PMI2_I2_I3_COLUMN,
				"The column containing the normalised-PMI2 (nPMI2, npr2) values");
		addOptionToTab(tab, ITEM_SIZE,
				"The base size for the scatter plot symbols");
		addOptionToTab(tab, DEFAULT_SHAPE,
				"The default scatter plot symbol shape");
		addOptionToTab(tab, SHOW_SIZE_LEGEND,
				"Show a legend for the item size (only available if the scatter "
						+ "table has a size manager associated with it");
		addOptionToTab(tab, SHOW_SHAPE_NOMINAL_COLOURS_LEGEND,
				"Show a legend for the scatter plot shapes and/or colours "
						+ "(Only available if the scatter table has either or both "
						+ "of a nominal colour manager or a shape manager associated "
						+ "with it)");
		addOptionToTab(tab, SHOW_COLOUR_GRADIENT_LEGEND,
				"Show a colour spectrum legend for the scatter plot colours "
						+ "(Only available if the scatter table has a continuous "
						+ "colour manager associated with it)");

		tab = createTab(fullDesc, "General Plot Options");
		addOptionToTab(tab, "Type of Image",
				"The type of the created image can either be png or svg. "
						+ "PNGs are mostly smaller, SVGs provide details about plot "
						+ "and the possibility to be changed individually");
		addOptionToTab(tab, "Title of Graph",
				"The title of the graph shown above the generated image. "
						+ "If the title is not activated, no title will be shown");
		addOptionToTab(tab, "Width of Image (in pixel)",
				"The width of the generated image, not the plot width");
		addOptionToTab(tab, "Height of Image (in pixel)",
				"The height of the generated image, not the plot height");
		addOptionToTab(tab, "Background Colour",
				"The color of the background of the plot. Hence this color is used for "
						+ "the empty space in a plot");
		addOptionToTab(tab, "Plot background Alpha",
				"The transparency of the plot background can be modified using an "
						+ "additional alpha value. An alpha value of 1 does not change "
						+ "the background transparency. Decreasing the alpha value will "
						+ "increase the plot background transparency");
		addOptionToTab(tab, "Scale Font Size",
				"Factor changes the font sizes within the JFreeChart view. A value "
						+ "greater the 1 increases all view fonts, a value between "
						+ "0 and 1 decrease them");

		Ports ports = node.addNewPorts();

		for (int i = 0; i < portNames.length; i++) {
			InPort inport = ports.addNewInPort();
			inport.setIndex(BigInteger.valueOf(i));
			inport.setName(getInportName(i));
			inport.newCursor().setTextValue(getInportDescription(i));
		}
		InPort inport = ports.addNewInPort();
		inport.setIndex(BigInteger.valueOf(scatterPortId));
		inport.setName(getInportName(scatterPortId));
		inport.newCursor().setTextValue(getInportDescription(scatterPortId));

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

		addBundleInformation(node, factoryClz);
		return (Element) node.getDomNode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeDescription#getInportDescription(int)
	 */
	@Override
	public String getInportDescription(int index) {
		if (index == scatterPortId) {
			return "A table of normalised PMI values. "
					+ "Each row is plotted as a single 'scatter' point on the PMI Plot";
		}
		return getInputPortDescription(index);
	}

	/**
	 * Method to get the description for any non-scatter ports. The scatter port
	 * is handled by the abstract base class
	 * 
	 * @param index
	 *            The input port index
	 * @return The description for the port
	 */
	protected abstract String getInputPortDescription(int index);

	/**
	 * This method should be used to add any additional options to the node
	 * dialog. The options must be included in {@link Tab}s. This can
	 * conveniently be achieved using the
	 * {@link NodeDescriptionUtils#createTab(FullDescription, String)} and
	 * {@link NodeDescriptionUtils#addOptionToTab(Tab, String, String)} utility
	 * methods
	 * 
	 * @param fullDesc
	 *            The node {@link FullDescription}
	 */
	protected abstract void prependAdditionalTabbedOptions(
			FullDescription fullDesc);

	/**
	 * Method to add the node description.
	 * 
	 * @param introCursor
	 */
	protected abstract void addNodeDescription(XmlCursor introCursor);

}
