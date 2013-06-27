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

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * ReportField class.
 *
 * A ReportField represents a single report column, and defines:
 * <UL>
 * <LI>UI label</LI>
 * <LI>String to use in report request</LI>
 * <LI>Report table column type</LI>
 * <LI>Report table column name (default is same as UI label)</LI>
 * <LI>Default status (selected or unselected)</LI>
 * <LI>Triggers (other report fields, report categories, or standard reports that will trigger the selection of this field)</LI>
 * </UL>
 *
 * @see ReportCategory
 * @see StandardReport
 */
public class ReportField {

	/** XML element name for ReportField definition. */
	static final String XML_ELEMENT="reportField";

	/** XML attribute name for ReportField ID. */
	static final String XML_ATTR_ID = "id";

	/** XML attribute name for ReportField column type. */
	static final String XML_ATTR_TYPE = "type";

	/** XML attribute name for ReportField UI label. */
	static final String XML_ATTR_LABEL = "label";

	/** XML attribute name for ReportField column name. */
	static final String XML_ATTR_COLNAME = "colName";

	/** XML attribute name for ReportField default status. */
	static final String XML_ATTR_DEFAULT = "default";

	/** XML attribute name for ReportField triggers. */
	static final String XML_ATTR_TRIGGER = "trigger";

	/**
	 * Enumerated ReportField column types.
	 */
	public enum eType {

		/** STRING values. */
		STRING,

		/** INTEGER values. */
		INTEGER,

		/** DOUBLE values. */
		DOUBLE,

		/** DATE values. */
		DATE,

		/** SMILES values. */
		SMILES,

		/** Ligand image PNG URL. */
		PNG_URL
	}

	private ReportCategory m_parent;
	private String m_id;
	private eType m_type;
	private String m_label;
	private String m_colName;
	private String m_value;
	private boolean m_default = false;
	private Set<String> m_trigger = new HashSet<String>();

	/**
	 * Converts from string to eType enum.
	 *
	 * @param strType the string to convert.
	 * @return the eType value.
	 * @throws ConfigException if strType is invalid.
	 */
	public static eType string2Type(final String strType) throws ConfigException {
		if (strType.equalsIgnoreCase("string")) {
            return eType.STRING;
        } else if (strType.equalsIgnoreCase("integer")) {
            return eType.INTEGER;
        } else if (strType.equalsIgnoreCase("double")) {
            return eType.DOUBLE;
        } else if (strType.equalsIgnoreCase("date")) {
            return eType.DATE;
        } else if (strType.equalsIgnoreCase("smiles")) {
            return eType.SMILES;
        } else if (strType.equalsIgnoreCase("png_url")) {
            return eType.PNG_URL;
        } else {
            throw new ConfigException("Invalid " + XML_ATTR_TYPE + " attribute (" + strType + ")");
        }
	}

	/**
	 * Converts from eType enum to string.
	 *
	 * @param type the eType enum to convert.
	 * @return the string value.
	 */
	public static String type2String(final eType type) {
		switch (type) {
		case STRING:
			return "String";
		case INTEGER:
			return "Integer";
		case DOUBLE:
			return "Double";
		case DATE:
			return "Date";
		case SMILES:
			return "Smiles";
		case PNG_URL:
			return "PNG_URL";
		default:
			return "Unknown";
		}
	}

	/**
	 * Instantiates a new report field from an XML node.
	 *
	 * @param parent the parent ReportCategory
	 * @param node the XML node
	 * @throws ConfigException if any parse errors.
	 */
	public ReportField(final ReportCategory parent, final Node node) throws ConfigException {
		m_parent = parent;
		initFromXML(node);
	}

	/**
	 * Gets the parent ReportCategory.
	 *
	 * @return the parent ReportCategory
	 */
	public final ReportCategory getParent() {
		return m_parent;
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
	 * Checks if this is the hidden report field controlling the primary citation suffix.
	 *
	 * The primary citation suffix field is defined by the special name "PRIMARY_CITATION_SUFFIX".
	 * This hidden pseudo field is triggered automatically by selection of any field in the
	 * primary citation category, and should not be user-selectable in the UI.
	 *
	 * Unlike all other report fields, the primary citation suffix does not represent a column in
	 * the KNIME output table. Rather, it is a modifier of the behaviour of the citation report fields.
	 * Hence, the string value represented by this field should be appended to the custom report URL,
	 * and should not be concatenated with the regular report fields.
	 *
	 * @return true, if is primary citation suffix.
	 */
	public final boolean isPrimaryCitationSuffix() {
		return m_id.equals("PRIMARY_CITATION_SUFFIX");
	}

	/**
	 * Gets the report table column type.
	 *
	 * @return the type
	 */
	public final eType getType() {
		return m_type;
	}

	/**
	 * Gets the report table column type as string.
	 *
	 * @return the type as string
	 */
	public final String getTypeAsString() {
		return type2String(m_type);
	}

	/**
	 * Gets the UI label.
	 *
	 * @return the UI label
	 */
	public final String getLabel() {
		return m_label;
	}

	/**
	 * Gets the report table column name.
	 *
	 * @return the col name
	 */
	public final String getColName() {
		return m_colName;
	}

	/**
	 * Gets the value to use in report request string.
	 *
	 * @return the value
	 */
	public final String getValue() {
		return m_value;
	}

	/**
	 * Gets the default status.
	 *
	 * @return the default status (true=selected)
	 */
	public final boolean getDefault() {
		return m_default;
	}

	/**
	 * Checks if is triggered by another ReportField.
	 *
	 * Returns true if the other ReportField (or the parent category of the other ReportField)
	 * is in the trigger list for this field.
	 *
	 * @param field the field
	 * @return true, if is triggered
	 */
	public boolean isTriggered(final ReportField field) {
		return m_trigger.contains(field.getParent().getId()) || m_trigger.contains(field.getId());
	}

	/**
	 * Checks if is triggered by a StandardReport.
	 *
	 * Returns true if this field should be selected as part of a StandardReport.
	 *
	 * @param report the report
	 * @return true, if is triggered
	 */
	public boolean isTriggered(final StandardReport report) {
		return m_trigger.contains(report.getId());
	}

	/**
	 * Initializes from XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	private void initFromXML(final Node node) throws ConfigException {
		m_trigger.clear();
		if (node == null) {
			throw new ConfigException("Null " + XML_ELEMENT + " node");
		}
		else if (XML_ELEMENT != node.getNodeName()) {
			throw new ConfigException("Invalid " + XML_ELEMENT + " node (" + node.getNodeName() + ")");
		}
		else {
			NamedNodeMap attr = node.getAttributes();
			Node id = attr.getNamedItem(XML_ATTR_ID);
			Node type = attr.getNamedItem(XML_ATTR_TYPE);
			Node label = attr.getNamedItem(XML_ATTR_LABEL);
			Node colName = attr.getNamedItem(XML_ATTR_COLNAME);
			Node defaultAttr = attr.getNamedItem(XML_ATTR_DEFAULT);
			if (id == null) {
				throw new ConfigException("Missing " + XML_ATTR_ID + " attribute in " + XML_ELEMENT);
			}
			else if (type == null) {
				throw new ConfigException("Missing " + XML_ATTR_TYPE + " attribute in " + XML_ELEMENT);
			}
			else if (label == null) {
				throw new ConfigException("Missing " + XML_ATTR_LABEL + " attribute in " + XML_ELEMENT);
			}
			else if (colName == null) {
				throw new ConfigException("Missing " + XML_ATTR_COLNAME + " attribute in " + XML_ELEMENT);
			}
			else if (defaultAttr == null) {
				throw new ConfigException("Missing " + XML_ATTR_DEFAULT + " attribute in " + XML_ELEMENT);
			}
			else {
				m_id = id.getNodeValue();
				m_type = string2Type(type.getNodeValue());
				m_label = label.getNodeValue();
				m_colName = colName.getNodeValue();
				if (m_colName.isEmpty()) {
					m_colName = m_label;
				}
				m_default = defaultAttr.getNodeValue().equalsIgnoreCase(Boolean.toString(true));
				m_value = node.getTextContent();
				//optional attribute
				Node triggerAttr = attr.getNamedItem(XML_ATTR_TRIGGER);
				if (triggerAttr != null) {
					String[] triggers = triggerAttr.getNodeValue().split(" ");
					for (String trigger : triggers) {
						m_trigger.add(trigger);
					}
				}
			}
		}
	}
}
