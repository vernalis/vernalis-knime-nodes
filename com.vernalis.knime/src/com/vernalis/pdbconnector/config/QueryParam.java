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

import com.vernalis.pdbconnector.ComponentFactory;

/**
 * QueryParam class.
 * 
 * A QueryParam represents a user input parameter (or set of related parameters) of a specified type.
 * Valid parameter types are defined by the <code>eType</code> enum.
 * 
 * QueryParam definitions are used by ComponentFactory to construct KNIME dialog components and
 * settings models dynamically.
 * 
 * @see QueryOption
 * @see ComponentFactory
 */
public class QueryParam {

	/** XML element name for QueryParam definition. */
	static final String XML_ELEMENT="queryParam";

	/** XML attribute name for QueryParam ID. */
	static final String XML_ATTR_ID = "id";

	/** XML attribute name for QueryParam type. */
	static final String XML_ATTR_TYPE = "type";

	/** XML attribute name for QueryParam label. */
	static final String XML_ATTR_LABEL = "label";

	/** XML attribute name for QueryParam preferred dialog width.
	 * 
	 * UI hint. If non-zero, overrides global property for dialog width.
	 * 
	 */
	static final String XML_ATTR_WIDTH = "width";

	/** XML attribute name for QueryParam min value (numeric types only). */
	static final String XML_ATTR_MIN = "min";

	/** XML attribute name for QueryParam max value (numeric types only). */
	static final String XML_ATTR_MAX = "max";

	/** XML attribute name for QueryParam default value (numeric types only). */
	static final String XML_ATTR_DEFAULT = "default";

	/** XML element name for conditional subquery strings (_COND types only). */
	static final String XML_ELEMENT_QUERY_STRING="queryString";

	/**
	 * Enumerated QueryParam types.
	 */
	public enum eType {

		/** STRING parameter (free text). */
		STRING,

		/** INTEGER parameter (unbounded). */
		INTEGER,

		/** DOUBLE parameter (unbounded). */
		DOUBLE,

		/** INTEGER range (two values: min and max). */
		INTEGER_RANGE,

		/** DOUBLE range (two values: min and max). */
		DOUBLE_RANGE,

		/** STRING list (restricted to list of valid values). */
		STRING_LIST,

		/** DATE parameter. */
		DATE,

		/** BIG STRING (multi-line, free text). */
		BIG_STRING,

		/** Conditional STRING parameter.
		 * 
		 * Requires one XML subquery string.
		 * 
		 * The XML subquery string is included in the overall query string
		 * if and only if the user inputs a non-empty value for this parameter.
		 */
		STRING_COND,
		/** Conditional INTEGER_RANGE parameter.
		 * 
		 * Purpose: to replicate RCSB usage as far as possible, there are some query options where it
		 * is required to include only those subquery strings where the user has actively entered a
		 * non-default value for a query parameter.

		 * Requires two XML subquery strings (for min and max respectively).
		 * 
		 * The first XML subquery string is included in the overall query string
		 * if and only if the user inputs a value for the min parameter that is greater than the
		 * overall minimum value allowed.
		 * 
		 * The second XML subquery string is included in the overall query string
		 * if and only if the user inputs a value for the max parameter that is less than the
		 * overall maximum value allowed.
		 */
		INTEGER_RANGE_COND,
		/** Conditional DOUBLE_RANGE parameter.
		 * 
		 * Purpose: to replicate RCSB usage as far as possible, there are some query options where it
		 * is required to include only those subquery strings where the user has actively entered a
		 * non-default value for a query parameter.
		 * 
		 * Example: see the "Secondary Structure Content" query definition.
		 *
		 * Requires two XML subquery strings (for min and max respectively).
		 * 
		 * The first XML subquery string is included in the overall query string
		 * if and only if the user inputs a value for the min parameter that is greater than the
		 * overall minimum value allowed.
		 * 
		 * The second XML subquery string is included in the overall query string
		 * if and only if the user inputs a value for the max parameter that is less than the
		 * overall maximum value allowed.
		 */
		DOUBLE_RANGE_COND
	};

	private String m_id;
	private eType m_type;
	private String m_label;
	private int m_width=0;
	private double m_min=0;
	private double m_max=99999;
	private double m_default=0;
	/** List of subquery strings for use with _COND params. */
	private final List<String> m_queryStrings = new ArrayList<String>();
	/** Allowed string values (for STRING_LIST type only. */
	private Values m_values = null;

	/**
	 * Converts from string to eType enum.
	 *
	 * @param strType the string to convert.
	 * @return the eType value.
	 * @throws ConfigException if strType is invalid.
	 */
	public static eType string2Type(String strType) throws ConfigException {
		if (strType.equalsIgnoreCase("string")) return eType.STRING;
		else if (strType.equalsIgnoreCase("integer")) return eType.INTEGER;
		else if (strType.equalsIgnoreCase("double")) return eType.DOUBLE;
		else if (strType.equalsIgnoreCase("integer_range")) return eType.INTEGER_RANGE;
		else if (strType.equalsIgnoreCase("double_range")) return eType.DOUBLE_RANGE;
		else if (strType.equalsIgnoreCase("string_list")) return eType.STRING_LIST;
		else if (strType.equalsIgnoreCase("date")) return eType.DATE;
		else if (strType.equalsIgnoreCase("big_string")) return eType.BIG_STRING;
		else if (strType.equalsIgnoreCase("string_cond")) return eType.STRING_COND;
		else if (strType.equalsIgnoreCase("integer_range_cond")) return eType.INTEGER_RANGE_COND;
		else if (strType.equalsIgnoreCase("double_range_cond")) return eType.DOUBLE_RANGE_COND;
		else throw new ConfigException("Invalid " + XML_ATTR_TYPE + " attribute (" + strType + ")");
	}

	/**
	 * Converts from eType enum to string.
	 *
	 * @param type the eType enum to convert.
	 * @return the string value.
	 */
	public static String type2String(eType type) {
		switch (type) {
		case STRING:
			return "String";
		case INTEGER:
			return "Integer";
		case DOUBLE:
			return "Double";
		case INTEGER_RANGE:
			return "Integer_Range";
		case DOUBLE_RANGE:
			return "Double_Range";
		case STRING_LIST:
			return "String_List";
		case DATE:
			return "Date";
		case BIG_STRING:
			return "Big_String";
		case STRING_COND:
			return "String_Cond";
		case INTEGER_RANGE_COND:
			return "Integer_Range_Cond";
		case DOUBLE_RANGE_COND:
			return "Double_Range_Cond";
		default:
			return "Unknown";
		}
	}

	/**
	 * Instantiates a new query param from an XML node.
	 *
	 * @param node the XML node.
	 * @throws ConfigException if any parse errors.
	 */
	public QueryParam(Node node) throws ConfigException {
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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public eType getType() {
		return m_type;
	}

	/**
	 * Gets the type as a string.
	 *
	 * @return the type as string
	 */
	public String getTypeAsString() {
		return type2String(m_type);
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
	 * Gets the UI width in characters.
	 *
	 * If XML width attribute is greater than zero, then
	 * returns the custom width for this parameter.
	 * 
	 * Else returns the appropriate global width for this
	 * parameter type.
	 * 
	 * @return the UI width in characters.
	 */
	public int getWidth() {
		if (m_width > 0) {
			return m_width;
		}
		else {
			switch (m_type) {
			case STRING:
			case STRING_COND:
				return Properties.QUERY_PRM_STRING_WIDTH;
			case INTEGER:
			case INTEGER_RANGE:
			case INTEGER_RANGE_COND:
				return Properties.QUERY_PRM_INTEGER_WIDTH;
			case DOUBLE:
			case DOUBLE_RANGE:
			case DOUBLE_RANGE_COND:
				return Properties.QUERY_PRM_DOUBLE_WIDTH;
			case STRING_LIST:
				return 0;//custom width not used for String Lists
			case DATE:
				return 0;//custom width not used for Date dialogs
			case BIG_STRING:
				return Properties.QUERY_PRM_BIGSTRING_WIDTH;
			default:
				return 0;
			}
		}
	}

	/**
	 * Gets the min allowed value (numeric types only).
	 *
	 * @return the min allowed value
	 */
	public final double getMin() {
		return m_min;
	}

	/**
	 * Gets the max allowed value (numeric types only).
	 *
	 * @return the max allowed value
	 */
	public final double getMax() {
		return m_max;
	}

	/**
	 * Gets the default value (numeric types only).
	 *
	 * @return the default value
	 */
	public final double getDefault() {
		return m_default;
	}

	/**
	 * Gets the list of subquery strings (_COND types only).
	 *
	 * @return the subquery strings
	 */
	public final List<String> getQueryStrings() {
		return m_queryStrings;
	}

	/**
	 * Gets the allowed values (STRING_LIST type only).
	 *
	 * @return the allowed values
	 */
	public final Values getValues() {
		return m_values;
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
			Node type = attr.getNamedItem(XML_ATTR_TYPE);
			Node label = attr.getNamedItem(XML_ATTR_LABEL);
			Node width = attr.getNamedItem(XML_ATTR_WIDTH);
			Node min = attr.getNamedItem(XML_ATTR_MIN);
			Node max = attr.getNamedItem(XML_ATTR_MAX);
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
			else if (width == null) {
				throw new ConfigException("Missing " + XML_ATTR_WIDTH + " attribute in " + XML_ELEMENT);
			}
			else if (min == null) {
				throw new ConfigException("Missing " + XML_ATTR_MIN + " attribute in " + XML_ELEMENT);
			}
			else if (max == null) {
				throw new ConfigException("Missing " + XML_ATTR_MAX + " attribute in " + XML_ELEMENT);
			}
			else if (defaultAttr == null) {
				throw new ConfigException("Missing " + XML_ATTR_DEFAULT + " attribute in " + XML_ELEMENT);
			}
			else {
				m_id = id.getNodeValue();
				m_type = string2Type(type.getNodeValue());
				m_label = label.getNodeValue();
				//UI component width hint should be an integer
				try {
					m_width = Integer.parseInt(width.getNodeValue());
				} catch (NumberFormatException e) {
					throw new ConfigException("Invalid " + XML_ATTR_WIDTH + " attribute (" + width.getNodeValue()
							+ ") in " + XML_ELEMENT);
				}
				//Min,max values should be a double
				try {
					m_min = Double.parseDouble(min.getNodeValue());
				} catch (NumberFormatException e) {
					throw new ConfigException("Invalid " + XML_ATTR_MIN + " attribute (" + min.getNodeValue()
							+ ") in " + XML_ELEMENT);
				}
				try {
					m_max = Double.parseDouble(max.getNodeValue());
					if (m_max < m_min) {
						throw new ConfigException("Out of range " + XML_ATTR_MAX + " attribute (" + m_max
								+ ") in " + XML_ELEMENT + " (" + XML_ATTR_MIN + "=" + m_min + ")");
					}
				} catch (NumberFormatException e) {
					throw new ConfigException("Invalid " + XML_ATTR_MAX + " attribute (" + max.getNodeValue()
							+ ") in " + XML_ELEMENT);
				}
				try {
					m_default = Double.parseDouble(defaultAttr.getNodeValue());
					if (m_default < m_min) {
						throw new ConfigException("Out of range " + XML_ATTR_DEFAULT + " attribute (" + m_default
								+ ") in " + XML_ELEMENT + " (" + XML_ATTR_MIN + "=" + m_min + ")");
					}
					else if (m_default > m_max) {
						throw new ConfigException("Out of range " + XML_ATTR_DEFAULT + " attribute (" + m_default
								+ ") in " + XML_ELEMENT + " (" + XML_ATTR_MAX + "=" + m_max + ")");
					}
				} catch (NumberFormatException e) {
					throw new ConfigException("Invalid " + XML_ATTR_DEFAULT + " attribute (" + defaultAttr.getNodeValue()
							+ ") in " + XML_ELEMENT);
				}
				//Type-specific parsing
				switch (m_type) {
				case STRING_LIST:
					m_values = new Values(node.getChildNodes());
					if (m_values.getLabels().isEmpty()) {
						throw new ConfigException("Missing " + Values.XML_ELEMENT + " elements in " + XML_ELEMENT);						
					}
					break;
				case STRING_COND://load conditional subquery strings (should be one or two, depending on type)
				case INTEGER_RANGE_COND:
				case DOUBLE_RANGE_COND:
					int numExpected = (m_type == QueryParam.eType.STRING_COND) ? 1 : 2;
					NodeList children = node.getChildNodes();
					for (int i = 0, length = children.getLength(); i < length; ++i) {
						Node child = children.item(i);
						if (XML_ELEMENT_QUERY_STRING == child.getNodeName()) {
							m_queryStrings.add(child.getTextContent());
						}
					}
					if (m_queryStrings.size() != numExpected) {
						throw new ConfigException("Incorrect number of " + XML_ELEMENT_QUERY_STRING + " child elements in " + XML_ELEMENT
								+ " (expected=" + numExpected + ", actual=" + m_queryStrings.size() + ")");						
					}
					break;
				default:
					break;
				}
			}
		}
	}
}
