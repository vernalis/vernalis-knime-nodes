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
package com.vernalis.knime.jfcplot.core.nodes;

import static com.vernalis.knime.nodes.NodeDescriptionUtils.addBundleInformation;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.createTab;

import java.math.BigInteger;
import java.util.Optional;

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
 * The base {@link NodeDescription} class for the Plot nodes
 * 
 * @author s.roughley
 *
 */
public abstract class AbstractDrawableSeriesPlotNodeDescription
		extends NodeDescription {

	protected final String[] columnNames;
	protected final String iconName, nodeName, tabName;
	protected final Class<? extends NodeFactory<?>> factoryClz;

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
	protected AbstractDrawableSeriesPlotNodeDescription(String iconName,
			String nodeName, String tabName, String[] columnNames,
			Class<? extends NodeFactory<?>> factoryClazz) {
		this.iconName = iconName;
		this.nodeName = nodeName;
		this.tabName = tabName;
		this.columnNames = columnNames;
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
		try {
			return factoryClz.getResource(iconName).getFile();
		} catch (NullPointerException e1) {
			try {
				return getClass().getResource(iconName).getFile();
			} catch (NullPointerException e) {
				return null;
			}
		}
	}

	@Override
	public String getInportName(int index) {
		if (index == 0) {
			return "Chart Data";
		}
		return null;
	}

	@Override
	public String getNodeName() {
		return nodeName;
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
		node.setShortDescription(getShortDescriptionImpl());
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();

		addNodeDescription(introCursor);

		addDevelopedByVernalis(introCursor);
		introCursor.dispose();

		prependAdditionalTabbedOptions(fullDesc);

		Tab tab = createTab(fullDesc, "General Plot Options");
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

		addBundleInformation(node, factoryClz);
		return (Element) node.getDomNode();
	}

	@Override
	public Optional<String> getShortDescription() {
		return Optional.ofNullable(getShortDescriptionImpl());
	}

	/**
	 * @return The short description text
	 */
	protected abstract String getShortDescriptionImpl();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeDescription#getInportDescription(int)
	 */
	@Override
	public String getInportDescription(int index) {
		if (index == 0) {
			return "The incoming data table for the plot to be generated from";
		}
		return null;
	}

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
	 * 
	 * @return The last tab added, so subclasses can add to the tab
	 */
	protected Tab prependAdditionalTabbedOptions(FullDescription fullDesc) {
		Tab tab = createTab(fullDesc, tabName);
		for (String columnName : columnNames) {
			addOptionToTab(tab, columnName, "Select the '" + columnName
					+ "' column from the input table");
		}
		return tab;
	}

	/**
	 * Method to add the node description. The abstract class takes care of the
	 * 'Made in Vernalis' and the options added by default to all JFreeChart
	 * plotting nodes
	 * 
	 * @param introCursor
	 *            The {@link XmlCursor} to add options to
	 */
	protected abstract void addNodeDescription(XmlCursor introCursor);

}
