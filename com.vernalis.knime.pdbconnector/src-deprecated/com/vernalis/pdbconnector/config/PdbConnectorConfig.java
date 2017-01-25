/*******************************************************************************
 * Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 ******************************************************************************/
package com.vernalis.pdbconnector.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.vernalis.pdbconnector.config.ConfigException;
import com.vernalis.pdbconnector.config.PdbConnectorConfig2;
import com.vernalis.pdbconnector.config.QueryCategory;
import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.ReportCategory2;
import com.vernalis.pdbconnector.config.ReportField2;
import com.vernalis.pdbconnector.config.StandardCategory;
import com.vernalis.pdbconnector.config.StandardReport;
import com.vernalis.pdbconnector.config.Values;

/**
 * Manages the dialog options for PDB Connector KNIME Node.
 *
 * <P>
 * Singleton class to define the query and report options presented in
 * {@link com.vernalis.internal.pdbconnector.PdbConnectorNodeDialog2} and used by
 * {@link com.vernalis.internal.pdbconnector.PdbConnectorNodeModel2}. The configuration
 * is loaded dynamically from an external
 * <code>xml/PdbConnectorConfig.xml/dtd</code> file at run time, to allow for
 * updates to the supported PDB query and report options without the need for
 * code modification.
 *
 * @author dmorley
 * @see com.vernalis.pdbconnector.PdbConnectorNodeDialog2
 * @see com.vernalis.pdbconnector.PdbConnectorNodeModel2
 * @deprecated 07-Jul-2016 SDR - Use {@link PdbConnectorConfig2} in place
 */
@Deprecated
public class PdbConnectorConfig {
	/** Singleton logger instance. */
	static final NodeLogger logger = NodeLogger.getLogger(PdbConnectorConfig.class);

	/**
	 * Path to the configuration xml file (relative to plugin bundle directory).
	 */
	public static final String XML_PATH = "xml/PdbConnectorConfig.xml";

	/**
	 * Path to the configuration dtd file (relative to plugin bundle directory).
	 */
	public static final String DTD_PATH = "xml/PdbConnectorConfig.dtd";

	/** Root XML element name in config file. */
	public static final String XML_ELEMENT = "pdbConnectorOptions";

	/** XML element name for ligand image options. */
	public static final String XML_LIGAND_IMG_ELEMENT = "ligandImage";

	/** XML element name for property collection. */
	public static final String XML_PROPERTIES_ELEMENT = "properties";

	/** XML element name for each property. */
	public static final String XML_PROPERTY_ELEMENT = "property";

	/** XML attribute name for property name (key). */
	public static final String XML_PROPERTY_KEY_ATTR = "key";

	/** XML element name for similarity options. */
	public static final String XML_SIM_ELEMENT = "similarity";

	private static PdbConnectorConfig theInstance = null;
	private List<QueryCategory> m_queryCategories = new ArrayList<QueryCategory>();
	private List<ReportCategory> m_reportCategories = new ArrayList<ReportCategory>();
	private List<StandardCategory> m_standardCategories = new ArrayList<StandardCategory>();
	private StandardReport m_defaultStandardReport = null;
	private StandardReport m_customStandardReport = null;
	private QueryOption m_similarity = null;
	private ConfigException m_lastError = null;
	private Values m_ligandImgOptions = null;
	private final Map<String, String> m_properties = new HashMap<String, String>();

	/**
	 * Gets the single instance of PdbConnectorConfig.
	 *
	 * @return single instance of PdbConnectorConfig
	 */
	public static PdbConnectorConfig getInstance() {
		if (theInstance == null) {
			theInstance = new PdbConnectorConfig();
		}
		return theInstance;
	}

	/**
	 * Default constructor, initialising configuration from XML file.
	 *
	 * <P>
	 * Configuration is loaded by {@link #initFromXML()}. In the event the XML
	 * or DTD files are not found (or are invalid), all configuration attributes
	 * are cleared and the last {@link ConfigException} error is stored. The
	 * error condition can be detected by {@link #isOK()} and the last error
	 * retrieved by {@link #getLastErrorMessage()}.
	 *
	 * @see #isOK()
	 * @see #getLastErrorMessage()
	 * @see ConfigException
	 */
	private PdbConnectorConfig() {
		try {
			m_lastError = null;
			initFromXML();
		} catch (ConfigException e) {
			m_lastError = e;
			m_queryCategories.clear();
			m_reportCategories.clear();
			m_standardCategories.clear();
			m_defaultStandardReport = null;
			m_customStandardReport = null;
			m_similarity = null;
			m_ligandImgOptions = null;
			m_properties.clear();
		}
	}

	/**
	 * Gets the query categories.
	 *
	 * Each QueryCategory contains a collection of related QueryOption objects,
	 * and is displayed on a single named tab of the node dialog.
	 *
	 * @return the query categories
	 * @see QueryOption
	 */
	public final List<QueryCategory> getQueryCategories() {
		return m_queryCategories;
	}

	/**
	 * Gets the report categories.
	 *
	 * Each ReportCategory contains a collection of related ReportField objects
	 * and is displayed on a single named subpanel of the node Report Options
	 * dialog tab.
	 *
	 * Report categories are useful in the Customizable Table report dialog to
	 * facilitate selection (or deselection) of all report fields in that
	 * category.
	 *
	 * @return the report categories
	 * @see ReportField2
	 * @see StandardReport
	 * @see StandardCategory
	 */
	public final List<ReportCategory> getReportCategories() {
		return m_reportCategories;
	}

	/**
	 * Gets the standard categories.
	 *
	 * Each StandardCategory contains a collection of related StandardReport
	 * objects and represents a heading in the Select Report dropdown of the
	 * Report Options dialog tab.
	 *
	 * Note that a StandardReport (for example Ligand) may not select all of the
	 * report fields in the ReportCategory of the same name, and may include
	 * fields from other report categories.
	 *
	 * @return the standard categories
	 */
	public final List<StandardCategory> getStandardCategories() {
		return m_standardCategories;
	}

	/**
	 * Gets the default standard report.
	 *
	 * @return the default standard report
	 */
	public final StandardReport getDefaultStandardReport() {
		return m_defaultStandardReport;
	}

	/**
	 * Gets the custom standard report.
	 *
	 * @return the custom standard report
	 */
	public final StandardReport getCustomStandardReport() {
		return m_customStandardReport;
	}

	/**
	 * Gets the similarity filter query option.
	 *
	 * The similarity filter query option is displayed on the main Query Options
	 * tab.
	 *
	 * @return the similarity filter query option.
	 */
	public final QueryOption getSimilarity() {
		return m_similarity;
	}

	/**
	 * Gets the ligand image size options.
	 *
	 * @return the ligand image size options
	 */
	public final Values getLigandImgOptions() {
		return m_ligandImgOptions;
	}

	/**
	 * Checks if a named property exists.
	 *
	 * @param key
	 *            the key
	 * @return true, if named property exists
	 */
	public final boolean isPropertyExists(final String key) {
		return (key != null) && (m_properties.containsKey(key));
	}

	/**
	 * Gets a named property value as a string.
	 *
	 * Returns the specified default value if the property key does not exist.
	 *
	 * @param key
	 *            the key
	 * @param defaultVal
	 *            the default value
	 * @return the property value
	 */
	public final String getProperty(final String key, final String defaultVal) {
		String retVal = defaultVal;
		if (!isPropertyExists(key)) {
			logger.warn("Property " + key + " not found - initializing from internal default");
		} else {
			retVal = m_properties.get(key);
		}
		logger.info("String property " + key + " = " + retVal);
		return retVal;
	}

	/**
	 * Gets the named property value as an integer.
	 *
	 * Returns the specified default value if the property key does not exist,
	 * or if the value is not a valid integer.
	 *
	 * @param key
	 *            the key
	 * @param defaultVal
	 *            the default value
	 * @return the property value as an integer
	 */
	public final int getPropertyAsInt(final String key, final int defaultVal) {
		int retVal = defaultVal;
		if (!isPropertyExists(key)) {
			logger.warn("Property " + key + " not found - initializing from internal default");
		} else {
			String strVal = m_properties.get(key);
			try {
				retVal = Integer.parseInt(strVal);
			} catch (NumberFormatException e) {
				logger.warn("Property " + key + " (" + strVal
						+ ") is not a valid integer - initializing from internal default");
				retVal = defaultVal;
			}
		}
		logger.info("Integer property " + key + " = " + retVal);
		return retVal;
	}

	/**
	 * Checks if configuration initialized OK from XML file.
	 *
	 * @return true, if configuration OK.
	 */
	public final boolean isOK() {
		return m_lastError == null;
	}

	/**
	 * Gets the last error message.
	 *
	 * @return the last error message
	 */
	public final String getLastErrorMessage() {
		return isOK() ? "" : m_lastError.getLocalizedMessage();
	}

	/**
	 * Initializes the configuration from XML file.
	 *
	 * @throws ConfigException
	 *             if any load or parse errors.
	 */
	private void initFromXML() throws ConfigException {
		Document doc = loadDocument(XML_PATH, DTD_PATH);
		Element root = doc.getDocumentElement();
		if (root == null) {
			throw new ConfigException("Null " + XML_ELEMENT + " node");
		} else if (XML_ELEMENT != root.getNodeName()) {
			throw new ConfigException(
					"Invalid " + XML_ELEMENT + " root (" + root.getNodeName() + ")");
		} else {
			NodeList queryCategories = root.getElementsByTagName(QueryCategory.XML_ELEMENT);
			for (int i = 0, length = queryCategories.getLength(); i < length; ++i) {
				m_queryCategories.add(new QueryCategory(queryCategories.item(i)));
			}
			NodeList reportCategories = root.getElementsByTagName(ReportCategory2.XML_ELEMENT);
			for (int i = 0, length = reportCategories.getLength(); i < length; ++i) {
				m_reportCategories.add(new ReportCategory(reportCategories.item(i)));
			}
			NodeList standardCategories = root.getElementsByTagName(StandardCategory.XML_ELEMENT);
			for (int i = 0, length = standardCategories.getLength(); i < length; ++i) {
				m_standardCategories.add(new StandardCategory(standardCategories.item(i)));
			}
			// Find and store the first default standard report
			Iterator<StandardCategory> iter = m_standardCategories.iterator();
			while (iter.hasNext() && (m_defaultStandardReport == null)) {
				m_defaultStandardReport = iter.next().getDefaultReport();
			}
			if (m_defaultStandardReport == null) {
				throw new ConfigException(
						"Default " + StandardReport.XML_ELEMENT + " node not found");
			}
			// Find and store the custom standard report
			iter = m_standardCategories.iterator();
			while (iter.hasNext() && (m_customStandardReport == null)) {
				m_customStandardReport = iter.next().getCustomReport();
			}
			if (m_customStandardReport == null) {
				throw new ConfigException(
						"Custom " + StandardReport.XML_ELEMENT + " node not found");
			}
			m_similarity = createSingletonQuery(root, XML_SIM_ELEMENT);
			loadLigandImageOptions(root);
			loadProperties(root);
		}
	}

	/**
	 * Creates a singleton QueryOption child of a singleton elementName element.
	 *
	 * Singleton expectations:
	 * <OL>
	 * <LI>XML root element contains one and only one child element called
	 * elementName</LI>
	 * <LI>XML elementName element contains one and only one child element,
	 * which is a valid QueryOption</LI>
	 * </OL>
	 *
	 * @param root
	 *            the root XML element
	 * @param elementName
	 *            the child element name
	 * @return the singleton QueryOption
	 * @throws ConfigException
	 *             if singleton expectations are violated.
	 */
	private QueryOption createSingletonQuery(final Element root, final String elementName)
			throws ConfigException {
		QueryOption retVal = null;
		NodeList elements = root.getElementsByTagName(elementName);
		switch (elements.getLength()) {
		case 0:
			throw new ConfigException("Missing " + elementName + " element");
		case 1:
			NodeList children = elements.item(0).getChildNodes();
			switch (children.getLength()) {
			case 0:
				throw new ConfigException("Missing " + QueryOption.XML_ELEMENT + " child of "
						+ elementName + " element");
			case 1:
				retVal = new QueryOption(children.item(0));
				break;
			default:
				throw new ConfigException("Multiple " + QueryOption.XML_ELEMENT + " children of "
						+ elementName + " element");
			}
			break;
		default:
			throw new ConfigException("Multiple " + elementName + " elements");
		}
		return retVal;
	}

	/**
	 * Loads ligand image size options.
	 *
	 * @param root
	 *            the root XML element
	 * @throws ConfigException
	 *             if ligand image size XML element is missing or invalid.
	 */
	private void loadLigandImageOptions(final Element root) throws ConfigException {
		m_ligandImgOptions = null;
		NodeList elements = root.getElementsByTagName(XML_LIGAND_IMG_ELEMENT);
		switch (elements.getLength()) {
		case 0:
			throw new ConfigException("Missing " + XML_LIGAND_IMG_ELEMENT + " element");
		case 1:
			Node node = elements.item(0);
			m_ligandImgOptions = new Values(node.getChildNodes());
			if (m_ligandImgOptions.getLabels().isEmpty()) {
				throw new ConfigException(
						"Missing " + Values.XML_ELEMENT + " elements in " + XML_LIGAND_IMG_ELEMENT);
			}
			break;
		default:
			throw new ConfigException("Multiple " + XML_LIGAND_IMG_ELEMENT + " elements");
		}
	}

	/**
	 * Loads named properties.
	 *
	 * @param root
	 *            the root XML element
	 * @throws ConfigException
	 *             if properties XML element is missing or invalid.
	 */
	private void loadProperties(final Element root) throws ConfigException {
		m_properties.clear();
		NodeList elements = root.getElementsByTagName(XML_PROPERTIES_ELEMENT);
		switch (elements.getLength()) {
		case 0:
			throw new ConfigException("Missing " + XML_PROPERTIES_ELEMENT + " element");
		case 1:
			Node element = elements.item(0);
			NodeList children = element.getChildNodes();
			for (int i = 0, length = children.getLength(); i < length; ++i) {
				Node child = children.item(i);
				if (XML_PROPERTY_ELEMENT == child.getNodeName()) {
					NamedNodeMap childAttr = child.getAttributes();
					Node keyAttr = childAttr.getNamedItem(XML_PROPERTY_KEY_ATTR);
					if (keyAttr == null) {
						throw new ConfigException("Missing " + XML_PROPERTY_KEY_ATTR
								+ " attribute in " + XML_PROPERTY_ELEMENT);
					} else {
						String key = keyAttr.getNodeValue();
						String value = child.getTextContent();
						m_properties.put(key, value);
					}
				}
			}
			break;
		default:
			throw new ConfigException("Multiple " + XML_PROPERTIES_ELEMENT + " elements");
		}
	}

	/**
	 * Loads configuration XML document.
	 *
	 * @param xml
	 *            the XML file name to load (relative to plugin bundle
	 *            directory)
	 * @param dtd
	 *            the DTD file name to load (relative to plugin bundle
	 *            directory)
	 * @return the XML document
	 * @throws ConfigException
	 *             if any load or parse errors.
	 */
	private Document loadDocument(final String xml, final String dtd) throws ConfigException {
		Document retVal = null;
		try {
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			IPath xmlPath = new Path(xml);
			IPath dtdPath = new Path(dtd);
			final URL XML = FileLocator.find(bundle, xmlPath, null);
			final URL DTD = FileLocator.find(bundle, dtdPath, null);
			if (XML == null) {
				throw new ConfigException("Error finding path to " + xml);
			} else if (DTD == null) {
				throw new ConfigException("Error finding path to " + dtd);
			}
			logger.debug("URL for " + xml + ": " + XML.toExternalForm());
			logger.debug("URL for " + dtd + ": " + DTD.toExternalForm());

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			dbf.setValidating(true);
			dbf.setIgnoringComments(true);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setCoalescing(true);
			dbf.setExpandEntityReferences(true);

			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(final String publicId, final String systemId)
						throws SAXException, IOException {
					InputStream is = DTD.openStream();
					return new InputSource(is);
				}
			});

			final InputSource source = new InputSource(XML.openStream());
			source.setSystemId("");
			retVal = db.parse(source);
		} catch (ParserConfigurationException e) {
			throw new ConfigException(e);
		} catch (IOException e) {
			throw new ConfigException(e);
		} catch (SAXException e) {
			throw new ConfigException(e);
		}
		return retVal;
	}
}
