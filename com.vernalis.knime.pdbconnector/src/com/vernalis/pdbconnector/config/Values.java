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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Values class.
 * 
 * A Values object stores allowed values for a QueryParam of type STRING_LIST.
 * Each allowed value is represented as a {label,value} pair, where the label is
 * used in the UI dropdown, and the value is used in the xml query itself.
 * 
 * @see QueryParam
 */
public class Values {

	/** XML element name for Value definition. */
	static final String XML_ELEMENT = "value";

	/** XML attribute name for Value label. */
	static final String XML_ATTR_LABEL = "label";

	/** XML attribute name for Value default status. */
	static final String XML_ATTR_DEFAULT = "default";

	/** The list of labels in the order they are added. */
	private final List<String> m_labels = new ArrayList<>();
	/** Lookup map of {key=label, value=value}. */
	private final Map<String, String> m_values = new HashMap<>();

	/** The label of the default value. */
	private String m_defaultLabel = "";

	/**
	 * Instantiates a new Values collection from an XML NodeList.
	 *
	 * @param nodes
	 *            the XML nodes
	 * @throws ConfigException
	 *             if any parse errors
	 */
	public Values(NodeList nodes) throws ConfigException {
		initFromXML(nodes);
	}

	/**
	 * Checks if UI label exists in this collection.
	 *
	 * @param label
	 *            the UI label to check
	 * @return true, if label exists
	 */
	public boolean isExists(String label) {
		return (label != null) && m_values.containsKey(label);
	}

	/**
	 * Gets the value for a given UI label.
	 *
	 * @param label
	 *            the UI label to check
	 * @return the value
	 */
	public final String getValue(String label) {
		return m_values.get(label);
	}

	/**
	 * Gets the list of all UI labels in this collection.
	 *
	 * @return the labels
	 */
	public final List<String> getLabels() {
		return m_labels;
	}

	/**
	 * Gets the default UI label.
	 *
	 * @return the default label
	 */
	public final String getDefaultLabel() {
		return m_defaultLabel;
	}

	/**
	 * Method to lookup the key from a given value
	 * 
	 * @param value
	 *            The value to lookup
	 * @return The key associated with the value, of {@code null} if the value
	 *         is not present
	 */
	public final String lookupKeyForValue(String value) {
		for (Entry<String, String> ent : m_values.entrySet()) {
			if (ent.getValue().equals(value)) {
				return ent.getKey();
			}
		}
		return null;
	}

	/**
	 * Initializes from XML nodes.
	 *
	 * @param nodes
	 *            the nodes
	 * @throws ConfigException
	 *             if any parse errors.
	 */
	private void initFromXML(NodeList nodes) throws ConfigException {
		for (int i = 0, length = nodes.getLength(); i < length; ++i) {
			Node node = nodes.item(i);
			if (XML_ELEMENT == node.getNodeName()) {
				NamedNodeMap attr = node.getAttributes();
				Node labelAttr = attr.getNamedItem(XML_ATTR_LABEL);
				Node defaultAttr = attr.getNamedItem(XML_ATTR_DEFAULT);
				if (labelAttr == null) {
					throw new ConfigException(
							"Missing " + XML_ATTR_LABEL + " attribute in " + XML_ELEMENT);
				} else {
					String value = node.getTextContent();
					String label = labelAttr.getNodeValue();
					if (label.isEmpty()) {// check for empty label (use value as
											// label)
						label = value;
					}
					if (isExists(label)) {// check for duplicates
						throw new ConfigException("Duplicate " + XML_ATTR_LABEL + " attribute ("
								+ label + ") in " + XML_ELEMENT);
					} else {
						m_labels.add(label);
						m_values.put(label, value);
						// Check if this is the default label
						// (silently override previous default labels)
						if ((defaultAttr != null) && defaultAttr.getNodeValue()
								.equalsIgnoreCase(Boolean.toString(true))) {
							m_defaultLabel = label;
						}
					}
				}
			}
		}
		// If default label is not specified explicitly, then use first in list.
		if (m_defaultLabel.isEmpty() && !m_labels.isEmpty()) {
			m_defaultLabel = m_labels.get(0);
		}
	}
}
