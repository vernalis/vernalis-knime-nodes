/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
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
package com.vernalis.pdbconnector.config;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * QueryCategory class.
 * 
 * A QueryCategory represents a single named query tab of the node dialog, and
 * contains a collection of related QueryOption objects.
 * 
 * @see QueryOption
 */
public class QueryCategory {

	/** XML element name for QueryCategory definition. */
	static final String XML_ELEMENT = "queryCategory";

	/** XML attribute name for QueryCategory ID. */
	static final String XML_ATTR_ID = "id";

	/** XML attribute name for QueryCategory label. */
	static final String XML_ATTR_LABEL = "label";

	private String m_id;
	private String m_label;
	private List<QueryOption> m_queryOptions = new ArrayList<>();

	/**
	 * Instantiates a new query category from an XML node.
	 *
	 * @param node
	 *            the XML node.
	 * @throws ConfigException
	 *             if any parse errors.
	 */
	public QueryCategory(Node node) throws ConfigException {
		initFromXML(node);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public final String getId() {
		return m_id;
	}

	/**
	 * Checks if this is the master query category.
	 * 
	 * The master query category is defined by the special name "MASTER", and is
	 * displayed on the main Query Options tab along with other dialog options.
	 *
	 * @return true, if is master
	 */
	public final boolean isMaster() {
		return m_id.equals("MASTER");
	}

	/**
	 * Gets the label.
	 * 
	 * The label is used as the query tab name in the dialog.
	 *
	 * @return the label
	 */
	public final String getLabel() {
		return m_label;
	}

	/**
	 * Gets the query options.
	 *
	 * Each query option is displayed in a separate, bordered, subpanel of the
	 * category query tab.
	 * 
	 * @return the query options
	 */
	public final List<QueryOption> getQueryOptions() {
		return m_queryOptions;
	}

	/**
	 * Initializes from XML node.
	 *
	 * @param node
	 *            the XML node.
	 * @throws ConfigException
	 *             if any parse errors.
	 */
	private void initFromXML(Node node) throws ConfigException {
		m_queryOptions.clear();
		if (node == null) {
			throw new ConfigException("Null " + XML_ELEMENT + " node");
		} else if (XML_ELEMENT != node.getNodeName()) {
			throw new ConfigException(
					"Invalid " + XML_ELEMENT + " node (" + node.getNodeName() + ")");
		} else {
			NamedNodeMap attr = node.getAttributes();
			Node id = attr.getNamedItem(XML_ATTR_ID);
			Node label = attr.getNamedItem(XML_ATTR_LABEL);
			if (id == null) {
				throw new ConfigException(
						"Missing " + XML_ATTR_ID + " attribute in " + XML_ELEMENT);
			} else if (label == null) {
				throw new ConfigException(
						"Missing " + XML_ATTR_LABEL + " attribute in " + XML_ELEMENT);
			} else {
				m_id = id.getNodeValue();
				m_label = label.getNodeValue();
				NodeList children = node.getChildNodes();
				int numChildren = children.getLength();
				for (int i = 0; i < numChildren; ++i) {
					m_queryOptions.add(new QueryOption(children.item(i)));
				}
			}
		}
	}
}
