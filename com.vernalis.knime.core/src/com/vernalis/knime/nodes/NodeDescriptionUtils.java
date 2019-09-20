/*******************************************************************************
 * Copyright (c) 2017,2019 Vernalis (R&D) Ltd
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
package com.vernalis.knime.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.xmlbeans.XmlCursor;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.knime.core.eclipseUtil.OSGIHelper;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.OptionDocument.Option;
import org.knime.node2012.TabDocument.Tab;
import org.osgi.framework.Bundle;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.EitherOr;

/**
 * Utility class to proved tools to build Node Descriptions
 * 
 * @author s.roughley
 *
 */
public class NodeDescriptionUtils {

	private NodeDescriptionUtils() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * Method to add the guidelines about the rSMARTS specification
	 * 
	 * @param introCursor
	 *            The cursor
	 */
	public static void addRSmartsGuidelines(XmlCursor introCursor) {
		introCursor.beginElement("p");
		introCursor.insertElementWithText("i",
				"Guidelines for Custom (r)SMARTS Definition");
		introCursor.insertElement("br");
		introCursor.insertChars(
				"An rSMARTS is no longer required, but may be specified if preferred for backwards compatibility. "
						+ "If specified must comply with the following rules. "
						+ "Otherwise, simply a match for two atoms separated by a single, acyclic bond must be provided");
		introCursor.beginElement("ul");
		introCursor.insertElementWithText("li",
				"'>>' is required to separate reactants and products");
		introCursor.insertElementWithText("li",
				"Products require '[*]' to occur twice, for the attachment "
						+ "points (the node will handle the tagging of these)");
		introCursor.insertElementWithText("li",
				"Reactants and products require exactly two atom mappings, e.g. "
						+ ":1] and :2] (other values could be used).");
		introCursor.insertElementWithText("li",
				"The atom mappings must be two different values");
		introCursor.insertElementWithText("li",
				"The same atom mappings must be used for reactants and products");
		introCursor.toEndToken();
		introCursor.insertChars(
				"rSMARTS not conforming to these guidelines will be "
						+ "rejected during node configuration.");
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/**
	 * @param fullDesc
	 *            The {@link FullDescription} object to add the option too,
	 *            without adding a tab
	 * @param optionName
	 *            The name of the option
	 * @param optionDescription
	 *            The description of the option
	 */
	public static void addOptionWithoutTab(FullDescription fullDesc,
			String optionName, String optionDescription) {
		Option opt = fullDesc.addNewOption();
		configureOption(opt, optionName, optionDescription);
	}

	/**
	 * @param tab
	 *            The tab to add the option to
	 * @param optionName
	 *            The name of the option
	 * @param optionDescription
	 *            The description of the option
	 */
	public static void addOptionToTab(Tab tab, String optionName,
			String optionDescription) {
		Option opt = tab.addNewOption();
		configureOption(opt, optionName, optionDescription);
	}

	/**
	 * Convenience method to actually build the option from the parameters
	 * 
	 * @param opt
	 * @param optionName
	 * @param optionDescription
	 */
	private static void configureOption(Option opt, String optionName,
			String optionDescription) {
		opt.setName(optionName);
		XmlCursor optCursor = opt.newCursor();
		optCursor.toFirstContentToken();
		optCursor.insertChars(optionDescription);
		optCursor.dispose();
	}

	/**
	 * Method to insert a numbered reference
	 * 
	 * @param introCursor
	 *            The cursor to add to
	 * @param refNumber
	 *            The reference number
	 * @param authors
	 *            The authors
	 * @param title
	 *            The title (quoted italics)
	 * @param journalAbbrev
	 *            The journal name abbreviation (Italics)
	 * @param year
	 *            The year
	 * @param volume
	 *            The volume (bold)
	 * @param pageRange
	 *            The page range
	 * @param doi
	 *            The DOI (will be linked too)
	 */
	public static void insertReference(XmlCursor introCursor, int refNumber,
			String authors, String title, String journalAbbrev, int year,
			int volume, String pageRange, String doi) {
		introCursor.beginElement("p");
		introCursor.insertChars(refNumber + ". " + authors + ", \"");
		introCursor.insertElementWithText("i", title);
		introCursor.insertChars("\", ");
		introCursor.insertElementWithText("i", journalAbbrev);
		introCursor.insertChars(", " + year + ", ");
		introCursor.insertElementWithText("b", volume + ", ");
		introCursor.insertChars(pageRange + " (DOI:");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href",
				"http://dx.doi.org/" + doi);
		introCursor.insertChars(doi);
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(")");
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/**
	 * Add the 'Developed by Vernalis' monicker to the end of the node
	 * description
	 * 
	 * @param introCursor
	 *            The cursor to add to
	 */
	public static void addDevelopedByVernalis(XmlCursor introCursor) {
		introCursor.beginElement("p");
		introCursor.insertChars("This node was developed by ");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href",
				"http://www.vernalis-research.com");
		introCursor.insertChars("Vernalis Research");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.insertChars(
				". For feedback and more information, please contact ");
		introCursor.beginElement("a");
		introCursor.insertAttributeWithValue("href",
				"mailto:knime@vernalis.com");
		introCursor.insertChars("knime@vernalis.com");
		introCursor.toEndToken();
		introCursor.toNextToken();
		introCursor.toEndToken();
		introCursor.toNextToken();
	}

	/**
	 * Method to add all options to the node description. Options are either
	 * added to the end of the description or in Tabs
	 * 
	 * @param fullDesc
	 *            The full description object of the node description
	 * @param options
	 *            The options - either a {@code Map<String, String>} for all
	 *            options to be added (K=option name, V= description), or a
	 *            {@code Map<String, Map<String, String>>} keyed on tab names
	 *            for options to be added to tabs
	 * @throws NoSuchElementException
	 */
	public static void addOptionsToDescription(FullDescription fullDesc,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options)
			throws NoSuchElementException {
		if (options != null && options.isPresent()) {
			if (options.isLeft()) {
				for (Entry<String, String> option : options.getLeft()
						.entrySet()) {
					addOptionWithoutTab(fullDesc, option.getKey(),
							option.getValue());
				}
			} else {
				for (Entry<String, Map<String, String>> tabEntry : options
						.getRight().entrySet()) {
					Tab tab = fullDesc.addNewTab();
					tab.setName(tabEntry.getKey());
					for (Entry<String, String> option : tabEntry.getValue()
							.entrySet()) {
						addOptionToTab(tab, option.getKey(), option.getValue());
					}
				}
			}
		}
	}

	/**
	 * Method to add the vendor bundle information to the node description. For
	 * nodes with an XML Node description, this is performed dynamically,
	 * however when the node description is supplied dynamically this needs to
	 * be added manually. Redirects to
	 * {@link #addBundleInformation(KnimeNode, Class)}
	 * 
	 * @param node
	 *            The KnimeNode element
	 * @param nodeFactory
	 *            The {@link NodeFactory} instance
	 * @throws DOMException
	 * @Deprecated Do not use - broken in v3.7 upwards
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static void addBundleInformation(KnimeNode node,
			NodeFactory<? extends NodeModel> nodeFactory) throws DOMException {
		addBundleInformation(node,
				(Class<? extends NodeFactory<?>>) nodeFactory.getClass());
	}

	/**
	 * Method to add the vendor bundle information to the node description. For
	 * nodes with an XML Node description, this is performed dynamically,
	 * however when the node description is supplied dynamically this needs to
	 * be added manually. This version does not require an instance of the
	 * {@link NodeFactory} class
	 * 
	 * @param node
	 *            The KnimeNode element
	 * @param nodeFactoryClazz
	 *            The {@link NodeFactory} instance
	 * @throws DOMException
	 */
	public static void addBundleInformation(KnimeNode node,
			Class<? extends NodeFactory<?>> nodeFactoryClazz)
			throws DOMException {
		Element bundleElement = ((Element) node.getDomNode()).getOwnerDocument()
				.createElement("osgi-info");
		Bundle bundle = OSGIHelper.getBundle(nodeFactoryClazz);
		if (bundle != null) {
			Optional<IInstallableUnit> feature = OSGIHelper.getFeature(bundle);
			bundleElement.setAttribute("bundle-symbolic-name", feature
					.map(f -> f.getId()).orElse(bundle.getSymbolicName()));
			bundleElement.setAttribute("bundle-name", feature
					.map(f -> f.getProperty(IInstallableUnit.PROP_NAME, null))
					.orElse(bundle.getHeaders().get("Bundle-Name")));
			bundleElement.setAttribute("bundle-vendor", feature.map(
					f -> f.getProperty(IInstallableUnit.PROP_PROVIDER, null))
					.orElse(bundle.getHeaders().get("Bundle-Vendor")));
		} else {
			bundleElement.setAttribute("bundle-symbolic-name", "<Unknown>");
			bundleElement.setAttribute("bundle-name", "<Unknown>");
			bundleElement.setAttribute("bundle-vendor", "<Unknown>");
		}
		bundleElement.setAttribute("factory-package",
				nodeFactoryClazz.getPackage().getName());
		((Element) node.getDomNode()).appendChild(bundleElement);
	}

	/**
	 * Method to add a new tab to a node description
	 * 
	 * @param fullDesc
	 *            The full description object of the node description
	 * @param tabName
	 *            The name of the new tab
	 * @return The new tab
	 */
	public static Tab createTab(FullDescription fullDesc, String tabName) {
		Tab retVal = fullDesc.addNewTab();
		retVal.setName(tabName);
		return retVal;
	}

	/**
	 * A helper class to build a paragraph with a table inserted
	 * 
	 * @author S.Roughley knime@vernalis.com
	 *
	 */
	public static class TableFactory {

		private String[] columnTitles;
		private String preTableText, postTableText;
		private List<String[]> tableRows = new ArrayList<>();

		public TableFactory(String... columnTitles) {
			this.columnTitles = ArrayUtils.copy(columnTitles);
		}

		public TableFactory setPreTableText(String str) {
			this.preTableText = str.isEmpty() ? null : str;
			return this;
		}

		public TableFactory setPostTableText(String str) {
			this.postTableText = str.isEmpty() ? null : str;
			return this;
		}

		public TableFactory addRowToTable(String... str) {
			this.tableRows.add(ArrayUtils.copy(str));
			return this;
		}

		public void buildTable(XmlCursor introCursor) {
			introCursor.beginElement("p");
			if (preTableText != null && !preTableText.isEmpty()) {
				introCursor.insertChars(preTableText);
			}
			introCursor.beginElement("table");
			introCursor.insertAttributeWithValue("class", "introtable");
			introCursor.insertAttributeWithValue("style", "width:100%");
			introCursor.beginElement("tr");
			for (String columnTitle : columnTitles) {
				introCursor.insertElementWithText("th", columnTitle);
			}
			introCursor.toEndToken();
			introCursor.toNextToken();// tr

			for (String[] row : tableRows) {
				introCursor.beginElement("tr");
				for (String cell : row) {
					introCursor.insertElementWithText("td", cell);
				}

				introCursor.toEndToken();
				introCursor.toNextToken();
			}

			introCursor.toEndToken();
			introCursor.toNextToken();// End of table
			if (postTableText != null && !postTableText.isEmpty()) {
				introCursor.insertChars(postTableText);
			}
			introCursor.toEndToken();
			introCursor.toNextToken();// End of para
		}
	}
}
