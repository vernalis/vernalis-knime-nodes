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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * StandardReport class.
 * 
 * A StandardReport represents a pre-defined set of report fields that constitute a standard report.
 * This is in contrast to a custom report where the user has full control over the field selection.
 * 
 * Notes:
 * <OL>
 * <LI>The constituent report fields are not stored as part of the StandardReport object. Rather, the
 * StandardReport acts as a trigger to select or deselect the individual ReportField objects stored in each ReportCategory.</LI>
 * <LI>A StandardReport (for example Ligand) may not select all of the report fields in the ReportCategory of the same name,
 * and may include fields from other report categories.</LI>
 * </OL>
 *
 * @see ReportField
 * @see ReportField#isTriggered(StandardReport)
 * @see ReportCategory
 * @see StandardCategory
 */
public class StandardReport {
	
	/** XML element name for StandardReport definition. */
	static final String XML_ELEMENT="standardReport";
	
	/** XML attribute name for StandardReport ID. */
	static final String XML_ATTR_ID = "id";
	
	/** XML attribute name for StandardReport label. */
	static final String XML_ATTR_LABEL = "label";
	
	/** XML attribute name for StandardReport default status. */
	static final String XML_ATTR_DEFAULT = "default";

	private String m_id;
	private String m_label;
	private boolean m_isDefault = false;

	/**
	 * Instantiates a new standard report from an XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	public StandardReport(Node node) throws ConfigException {
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
	 * Gets the label.
	 *
	 * @return the label
	 */
	public final String getLabel() {
		return m_label;
	}
	
	/**
	 * Checks if this is the customizable report.
	 * 
	 * The customizable report is defined by the special name "CUSTOM".
	 *
	 * @return true, if is customizable.
	 */
	public final boolean isCustom() {
		return m_id.equals("CUSTOM");
	}
	
	/**
	 * Checks if this is the default standard report.
	 *
	 * @return true, if is default
	 */
	public final boolean isDefault() {
		return m_isDefault;
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
				m_isDefault = defaultAttr.getNodeValue().equalsIgnoreCase(Boolean.toString(true));
			}
		}
	}
}
