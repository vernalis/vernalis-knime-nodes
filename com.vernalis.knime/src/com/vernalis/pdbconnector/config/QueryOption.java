/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.pdbconnector.config;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * QueryOption class.
 * 
 * QueryOption represents a single query option in the dialog. Each query option is displayed
 * in a separate, bordered, subpanel of the parent query category tab. Example query options include:
 * <UL>
 * <LI>Text search</LI>
 * <LI>SMILES/SMARTS search</LI>
 * <LI>Molecular weight search</LI>
 * </UL>
 * A QueryOption contains the list of (user input) query parameters that are required to
 * define the query, as well as the XML query string that should be passed to
 * the PDB web service. The XML query string is stored with placeholders for each query
 * parameter argument. The placeholders are substituted with the actual argument values at runtime.
 * 
 * @see QueryCategory
 * @see QueryParam
 */
public class QueryOption {
	
	/** XML element name for QueryOption definition. */
	static final String XML_ELEMENT="queryOption";
	
	/** XML element name for query string definition. */
	static final String XML_ELEMENT_QUERY_STRING="queryString";
	
	/** XML attribute name for QueryOption ID. */
	static final String XML_ATTR_ID = "id";
	
	/** XML attribute name for QueryOption label. */
	static final String XML_ATTR_LABEL = "label";
	
	/** XML attribute name for QueryOption default selection. */
	static final String XML_ATTR_DEFAULT = "default";
	
	private String m_id;
	private String m_label;
	private String m_queryString;
	private boolean m_default = false;
	private List<QueryParam> m_params = new ArrayList<QueryParam>();

	/**
	 * Instantiates a new query option from an XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	public QueryOption(Node node) throws ConfigException {
		initFromXML(node);
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return m_id;
	}
	
	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return m_label;
	}
	
	/**
	 * Gets the list of query parameters.
	 *
	 * @return the list of query parameters
	 */
	public final List<QueryParam> getParams() {
		return m_params;
	}
	
	/**
	 * Gets the XML query string.
	 *
	 * @return the XML query string
	 */
	public String getQueryString() {
		return m_queryString;
	}
	
	/**
	 * Gets the default selection (true/false).
	 *
	 * @return the default selection
	 */
	public final boolean getDefault() {
		return m_default;
	}
	
	/**
	 * Initializes from XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	private void initFromXML(Node node) throws ConfigException {
		if (node == null) {
			throw new ConfigException("Null " + XML_ELEMENT + " node");
		}
		else if (XML_ELEMENT != node.getNodeName()) {
			throw new ConfigException("Invalid " + XML_ELEMENT + " node (" + node.getNodeName() + ")");
		}
		else {
			NamedNodeMap attr = node.getAttributes();
			Node id = attr.getNamedItem(XML_ATTR_ID);
			Node label = attr.getNamedItem(XML_ATTR_LABEL);
			Node defaultAttr = attr.getNamedItem(XML_ATTR_DEFAULT);
			if (id == null) {
				throw new ConfigException("Missing " + XML_ATTR_ID + " attribute in " + XML_ELEMENT);
			}
			else if (label == null) {
				throw new ConfigException("Missing " + XML_ATTR_LABEL + " attribute in " + XML_ELEMENT);
			}
			else if (defaultAttr == null) {
				throw new ConfigException("Missing " + XML_ATTR_DEFAULT + " attribute in " + XML_ELEMENT);
			}
			else {
				m_id = id.getNodeValue();
				m_label = label.getNodeValue();
				m_default = defaultAttr.getNodeValue().equalsIgnoreCase(Boolean.toString(true));
				NodeList children = node.getChildNodes();
				int numChildren = children.getLength();
				for (int i = 0; i < numChildren; ++i) {
					Node child = children.item(i);
					if (XML_ELEMENT_QUERY_STRING == child.getNodeName()) {
						m_queryString = child.getTextContent();
					}
					else {
						QueryParam param = new QueryParam(child);
						m_params.add(param);
					}
				}
			}
			//Check we loaded query string ok
			if ( (m_queryString == null) || m_queryString.isEmpty()) {
				throw new ConfigException("Missing " + XML_ELEMENT_QUERY_STRING + " element in " + XML_ELEMENT);				
			}
		}
	}
}
