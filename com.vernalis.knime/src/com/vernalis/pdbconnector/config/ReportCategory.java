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
 * ReportCategory class.
 * 
 * A ReportCategory represents a single named subpanel of the Report Options dialog tab,
 * and contains a collection of related ReportField objects.
 * 
 * @see ReportField
 */
public class ReportCategory {
	
	/** XML element name for ReportCategory definition. */
	static final String XML_ELEMENT="reportCategory";
	
	/** XML attribute name for ReportCategory ID. */
	static final String XML_ATTR_ID = "id";
	
	/** XML attribute name for ReportCategory label. */
	static final String XML_ATTR_LABEL = "label";

	private String m_id;
	private String m_label;
	private List<ReportField> m_reportFields = new ArrayList<ReportField>();
	
	/**
	 * Instantiates a new report category from an XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	public ReportCategory(Node node) throws ConfigException {
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
	 * Checks if this is the hidden report category.
	 * 
	 * The hidden report category is defined by the special name "HIDDEN".
	 * This category contains those fields that are added to the report
	 * automatically depending on which other fields are selected, and
	 * should not be user-selectable in the UI.
	 * 
	 * For example:
	 * <UL>
	 * <LI>Structure ID</LI>
	 * <LI>Chain ID</LI>
	 * </UL>
	 * 
	 * @return true, if is hidden
	 */
	public final boolean isHidden() {
		return m_id.equals("HIDDEN");
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
	 * Gets the report fields.
	 *
	 * @return the report fields
	 */
	public final List<ReportField> getReportFields() {
		return m_reportFields;
	}

	/**
	 * Initializes from XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	private void initFromXML(Node node) throws ConfigException {
		m_reportFields.clear();
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
			if (id == null) {
				throw new ConfigException("Missing " + XML_ATTR_ID + " attribute in " + XML_ELEMENT);
			}
			else if (label == null) {
				throw new ConfigException("Missing " + XML_ATTR_LABEL + " attribute in " + XML_ELEMENT);
			}
			else {
				m_id = id.getNodeValue();
				m_label = label.getNodeValue();
				NodeList children = node.getChildNodes();
				int numChildren = children.getLength();
				for (int i = 0; i < numChildren; ++i) {
					m_reportFields.add(new ReportField(this,children.item(i)));
				}
			}
		}
	}
}
