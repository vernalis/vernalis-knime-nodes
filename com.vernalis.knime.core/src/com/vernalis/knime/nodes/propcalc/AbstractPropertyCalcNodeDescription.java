/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.nodes.propcalc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlCursor;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.core.util.Pair;
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

import static com.vernalis.knime.nodes.NodeDescriptionUtils.addBundleInformation;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.nodes.propcalc.AbstractPropertyCalcNodeDialog.MAX_VISIBLE_PROPERTIES;

/**
 * Node Description Implementation to automatically generate the node
 * description for the calculated properties nodes
 * 
 * @author s.roughley
 *
 */
public class AbstractPropertyCalcNodeDescription extends NodeDescription {

	private final String nodeName;
	private final String iconPath;
	private final String propName;
	private final CalculatedPropertyInterface<?>[] props;
	private final LinkedHashMap<String, List<Pair<String, String>>> additionalTabsAndOptions;
	private final Class<? extends NodeFactory<?>> nodeFactoryClz;
	private final String colName;

	/**
	 * Overloaded constructor when no options are supplied at construction
	 * 
	 * @param nodeName
	 *            The node name
	 * @param colName
	 *            The name of the column chooser
	 * @param propertyName
	 *            The name of the properties dialog
	 * @param iconPath
	 *            The icon path
	 * @param props
	 *            The properties available to the node
	 * @param nodeFactoryClass
	 *            The class of the {@link NodeFactory} implementation to add the
	 *            bundle information and attempt to resolve the icon path
	 */
	public AbstractPropertyCalcNodeDescription(String nodeName, String colName,
			String propertyName, String iconPath,
			CalculatedPropertyInterface<?>[] props,
			Class<? extends NodeFactory<?>> nodeFactoryClass) {
		this(nodeName, colName, propertyName, iconPath, props,
				new LinkedHashMap<>(), nodeFactoryClass);
	}

	/**
	 * Constructor
	 * 
	 * @param nodeName
	 *            The node name
	 * @param colName
	 *            The name of the column chooser
	 * @param propertyName
	 *            The name of the properties dialog
	 * @param iconPath
	 *            The icon path
	 * @param props
	 *            The properties available to the node
	 * @param additionalTabsAndOptions
	 *            Any additional tabs and options to add to the description,
	 * @param nodeFactoryClass
	 *            The class of the {@link NodeFactory} implementation to add the
	 *            bundle information and attempt to resolve the icon path
	 */
	public AbstractPropertyCalcNodeDescription(String nodeName, String colName,
			String propertyName, String iconPath,
			CalculatedPropertyInterface<?>[] props,
			LinkedHashMap<String, List<Pair<String, String>>> additionalTabsAndOptions,
			Class<? extends NodeFactory<?>> nodeFactoryClass) {
		super();
		this.nodeName = nodeName;
		this.iconPath = iconPath;
		this.propName = propertyName;
		this.props = props;
		this.additionalTabsAndOptions = additionalTabsAndOptions;
		this.nodeFactoryClz = nodeFactoryClass;
		this.colName = colName;
	}

	@Override
	public String getIconPath() {

		try {
			return nodeFactoryClz.getResource(iconPath).getFile();
		} catch (NullPointerException e1) {
			try {
				return getClass().getResource(iconPath).getFile();
			} catch (NullPointerException e) {
				return null;
			}
		}
	}

	@Override
	public String getInportDescription(int index) {
		if (index == 0) {
			return "Input table";
		}
		return null;
	}

	@Override
	public String getInportName(int index) {
		if (index == 0) {
			return "Input table";
		}
		return null;
	}

	@Override
	public String getInteractiveViewName() {
		return null;
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public String getOutportDescription(int index) {
		if (index == 0) {
			return "Input table with calculated properties appended";
		}
		return null;
	}

	@Override
	public String getOutportName(int index) {
		if (index == 0) {
			return propName;
		}
		return null;
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
		node.setShortDescription("This node calculates " + propName);
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		introCursor.insertElementWithText("p",
				"This node calculates " + propName);

		addAdditionalIntroductoryParagraphs(introCursor);

		introCursor.insertElementWithText("p", "The properties are:");
		introCursor.beginElement("ul");
		for (CalculatedPropertyInterface<?> prop : props) {
			introCursor.beginElement("li");
			introCursor.insertElementWithText("i", prop.getName());
			if (prop.getDescription() != null
					&& !prop.getDescription().isEmpty()) {
				introCursor.insertChars(" - " + prop.getDescription());
			}
			if (prop.getAliases() != null && prop.getAliases().length > 0) {
				introCursor.insertChars(" (also referred to as: ");
				StringBuilder sb = new StringBuilder();
				for (String alias : prop.getAliases()) {
					sb.append(", ").append(alias);
				}
				introCursor.insertChars(sb.substring(2));
				introCursor.insertChars(")");
			}
			if (prop.getReferences() != null
					&& prop.getReferences().length > 0) {
				introCursor.insertChars(" (See: ");
				StringBuilder sb = new StringBuilder();
				for (String ref : prop.getReferences()) {
					sb.append(", ").append(ref);
				}
				introCursor.insertChars(sb.substring(2));
				introCursor.insertChars(")");
			}
			introCursor.toEndToken();
			introCursor.toNextToken();
		}
		introCursor.toEndToken();
		introCursor.toNextToken();

		addDevelopedByVernalis(introCursor);

		introCursor.dispose();

		Tab tab = fullDesc.addNewTab();
		tab.setName("Options");
		addOptionToTab(tab, colName, "Select the input column");
		addAdditionalOptionsTabOptions(tab);

		tab = fullDesc.addNewTab();
		tab.setName("Calculated Properties");

		addOptionToTab(tab, propName,
				"Select at least 1 property to calculate (see above for details)");
		if (props.length > 1) {
			addOptionToTab(tab, "Select All", "Selects all properties");
			addOptionToTab(tab, "Clear",
					"Deselects all properties, and selects only the 1st property in the list");
			if (props.length > MAX_VISIBLE_PROPERTIES) {
				addOptionToTab(tab,
						"Show All/Show 1st " + MAX_VISIBLE_PROPERTIES,
						"Toggles the display of the options list between the first "
								+ MAX_VISIBLE_PROPERTIES
								+ " options and all options");
			}
		}
		addAdditionalCalcPropsTabOptions(tab);

		for (Entry<String, List<Pair<String, String>>> extraTab : additionalTabsAndOptions
				.entrySet()) {
			tab = fullDesc.addNewTab();
			tab.setName(extraTab.getKey());
			for (Pair<String, String> extraOption : extraTab.getValue()) {
				addOptionToTab(tab, extraOption.getFirst(),
						extraOption.getSecond());
			}
		}

		Ports ports = node.addNewPorts();

		InPort inport = ports.addNewInPort();
		int portIdx = 0;
		while (getInportName(portIdx) != null) {
			inport.setIndex(portIdx);
			inport.setName(getInportName(portIdx));
			inport.newCursor().setTextValue(getInportDescription(portIdx));
			portIdx++;
		}

		OutPort outport = ports.addNewOutPort();
		portIdx = 0;
		while (getOutportName(portIdx) != null) {
			outport.setIndex(portIdx);
			outport.setName(getOutportName(portIdx));
			outport.newCursor().setTextValue(getOutportDescription(portIdx));
			portIdx++;
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

		addBundleInformation(node, nodeFactoryClz);
		return (Element) node.getDomNode();
	}

	/**
	 * Hook to add additional text to the introductory paragraph
	 * 
	 * @param introCursor
	 *            The {@link XmlCursor}
	 */
	protected void addAdditionalIntroductoryParagraphs(XmlCursor introCursor) {

	}

	/**
	 * Hook to add additional options to the 'calculated properties' tab
	 * description
	 * 
	 * @param tab
	 *            The calculated properties tab
	 */
	protected void addAdditionalCalcPropsTabOptions(Tab tab) {

	}

	/**
	 * Hook to add additional options to the 'options' tab
	 * 
	 * @param tab
	 *            The options tab
	 */
	protected void addAdditionalOptionsTabOptions(Tab tab) {

	}

}
