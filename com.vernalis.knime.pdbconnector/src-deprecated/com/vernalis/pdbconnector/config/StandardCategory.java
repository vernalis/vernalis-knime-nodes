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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * StandardCategory class.
 * 
 * A StandardCategory represents a sub-heading in the "Select Report" dropdown
 * of the Report Options dialog tab, and contains a collection of related
 * StandardReport objects.
 * 
 * @see StandardReport
 */
@Deprecated
public class StandardCategory {

	/** XML element name for StandardCategory definition. */
	static final String XML_ELEMENT = "standardCategory";

	/** XML attribute name for StandardCategory ID. */
	static final String XML_ATTR_ID = "id";

	/** XML attribute name for StandardCategory label. */
	static final String XML_ATTR_LABEL = "label";

	private String m_id;
	private String m_label;
	private final List<StandardReport> m_standardReports = new ArrayList<>();
	/** lookup table by key=ID. */
	private final Map<String, StandardReport> m_lookup = new HashMap<>();
	/**
	 * Default report. May be null if there is no default report in this
	 * category.
	 */
	private StandardReport m_defaultReport = null;
	/**
	 * Custom report. May be null if there is no custom report in this category.
	 */
	private StandardReport m_customReport = null;

	/**
	 * Instantiates a new standard category from an XML node.
	 *
	 * @param node
	 *            the XML node.
	 * @throws ConfigException
	 *             if any parse errors.
	 */
	public StandardCategory(Node node) throws ConfigException {
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
	 * Gets the list of standard reports.
	 *
	 * @return the standard reports
	 */
	public final List<StandardReport> getStandardReports() {
		return m_standardReports;
	}

	/**
	 * Gets the standard report for a given report id.
	 *
	 * @param reportId
	 *            the report id to lookup
	 * @return the standard report for this id (or null if id not found)
	 */
	public final StandardReport getStandardReport(String reportId) {
		return m_lookup.get(reportId);
	}

	/**
	 * Gets the default report.
	 *
	 * @return the default report (or null if there is no default report in this
	 *         category)
	 */
	public final StandardReport getDefaultReport() {
		return m_defaultReport;
	}

	/**
	 * Gets the custom report.
	 *
	 * @return the custom report (or null if there is no default report in this
	 *         category)
	 */
	public final StandardReport getCustomReport() {
		return m_customReport;
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
		m_standardReports.clear();
		m_lookup.clear();
		m_defaultReport = null;
		m_customReport = null;
		if (node == null) {
			throw new ConfigException("Null " + XML_ELEMENT + " node");
		} else if (XML_ELEMENT != node.getNodeName()) {
			throw new ConfigException("Invalid " + XML_ELEMENT + " node ("
					+ node.getNodeName() + ")");
		} else {
			NamedNodeMap attr = node.getAttributes();
			Node id = attr.getNamedItem(XML_ATTR_ID);
			Node label = attr.getNamedItem(XML_ATTR_LABEL);
			if (id == null) {
				throw new ConfigException("Missing " + XML_ATTR_ID
						+ " attribute in " + XML_ELEMENT);
			} else if (label == null) {
				throw new ConfigException("Missing " + XML_ATTR_LABEL
						+ " attribute in " + XML_ELEMENT);
			} else {
				m_id = id.getNodeValue();
				m_label = label.getNodeValue();
				NodeList children = node.getChildNodes();
				int numChildren = children.getLength();
				for (int i = 0; i < numChildren; ++i) {
					StandardReport report =
							new StandardReport(children.item(i));
					m_standardReports.add(report);
					m_lookup.put(report.getId(), report);// index by report ID
					// store first default report found (if any)
					if ((m_defaultReport == null) && report.isDefault()) {
						m_defaultReport = report;
					}
					// store first custom report found (if any)
					if ((m_customReport == null) && report.isCustom()) {
						m_customReport = report;
					}
				}
			}
		}
	}
}
