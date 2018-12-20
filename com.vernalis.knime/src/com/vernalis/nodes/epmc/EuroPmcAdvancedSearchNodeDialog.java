/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.epmc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButton;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "EuroPmcAdvancedSearch" Node. Node to run a
 * reference query on the European Pub Med Central webservice and return the
 * results as an XML table
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author SDR
 */
public class EuroPmcAdvancedSearchNodeDialog extends DefaultNodeSettingsPane {
	private static final String RETURN_ONLY_OPEN_ACCESS_ARTICLES =
			"Return only Open Access Articles";
	// The objects for the updateable text fields
	private DialogComponentLabel hitCount;
	private DialogComponentMultiLineString queryString;
	private SettingsModelString m_queryString;
	// Objects to which we need access to run test query
	private DialogComponentString Title;
	private DialogComponentString Author;
	private DialogComponentString Affiliation;
	private DialogComponentString From;
	private DialogComponentString To;
	private DialogComponentString Journal;
	private DialogComponentString MeSH;
	private DialogComponentString GenQuery;
	private DialogComponentStringSelection QType;
	private DialogComponentStringSelection Sort;
	private DialogComponentBoolean onlyOA;

	/**
	 * New pane for configuring the EuroPmcAdvancedSearch node.
	 */
	protected EuroPmcAdvancedSearchNodeDialog() {

		createNewGroup("ePub Med Central Advanced Query");
		Title = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_TITLE, null),
				"Title(s):");
		addDialogComponent(Title);

		Author = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_AUTHORS, null),
				"Author(s):");
		addDialogComponent(Author);

		Affiliation = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_AFFILIATION, null),
				"Affiliation(s):");
		addDialogComponent(Affiliation);

		addDialogComponent(new DialogComponentLabel("Publication Year"));

		setHorizontalPlacement(true);

		From = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_YEAR_FROM, null),
				"From");
		addDialogComponent(From);

		To = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_YEAR_TO, null), "To");
		addDialogComponent(To);

		setHorizontalPlacement(false);

		Journal = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_JOURNALS, null),
				"Journal(s):");
		addDialogComponent(Journal);

		MeSH = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_MESH, null),
				"MeSH Subject(s):");
		addDialogComponent(MeSH);

		GenQuery = new DialogComponentString(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_GNL_QUERY, null),
				"General Query:");
		addDialogComponent(GenQuery);

		onlyOA = new DialogComponentBoolean(createOAOnlyModel(), RETURN_ONLY_OPEN_ACCESS_ARTICLES);
		addDialogComponent(onlyOA);

		createNewGroup("Query Options");
		QType = new DialogComponentStringSelection(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_QUERY_TYPE, "Core"),
				"Select the query type:", Arrays.asList("Idlist", "Lite", "Core"));
		addDialogComponent(QType);

		Sort = new DialogComponentStringSelection(
				new SettingsModelString(EuroPmcAdvancedSearchNodeModel.CFG_SORT_ORDER, "Date"),
				"Select the sort order:", Arrays.asList("Date", "Relevance"));
		addDialogComponent(Sort);

		addDialogComponent(new DialogComponentNumber(createPageSizeModel(),
				"Page size (number of hits to retrieve per call)", 5));
		addDialogComponent(new DialogComponentString(createEmailModel(),
				"Optional email address for EBI Webservices mailing list"));

		// Now we add a test button, which runs the 'doTestQuery' on clicking
		setHorizontalPlacement(true);
		DialogComponentButton testButton = new DialogComponentButton("Test Query");
		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doTestQuery();
			}
		});
		addDialogComponent(testButton);

		// And a 'Clear Test' button

		DialogComponentButton clearTestButton = new DialogComponentButton("Clear");
		clearTestButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				doClearQuery();
			}
		});
		addDialogComponent(clearTestButton);

		// And a hit count text box
		hitCount = new DialogComponentLabel("Hit Count: Click 'Test Query'");
		addDialogComponent(hitCount);

		setHorizontalPlacement(false);
		// And a query text box - initially populated with some blank lines so
		// as not to make the
		// NB we have to use a disabled MultiLineSting as DialogComponentLabel
		// does not support
		// multi-line text
		m_queryString = new SettingsModelString("queryString", "\n\n\n");
		m_queryString.setEnabled(false);
		queryString =
				new DialogComponentMultiLineString(m_queryString, "Query (as parsed by ePubMed):");
		addDialogComponent(queryString);

	}

	static SettingsModelBoolean createOAOnlyModel() {
		return new SettingsModelBoolean(RETURN_ONLY_OPEN_ACCESS_ARTICLES, false);
	}

	protected void doTestQuery() {
		// Run a test query and show the number of hits
		hitCount.setText("Waiting...");
		m_queryString.setStringValue("Query sent to ePMC Webservices");

		// Build the query
		boolean sortDate = ((SettingsModelString) Sort.getModel()).getStringValue().equals("Date");
		String queryText = EpmcHelpers.buildQueryString(
				((SettingsModelString) Title.getModel()).getStringValue(),
				((SettingsModelString) Author.getModel()).getStringValue(),
				((SettingsModelString) Affiliation.getModel()).getStringValue(),
				((SettingsModelString) From.getModel()).getStringValue(),
				((SettingsModelString) To.getModel()).getStringValue(),
				((SettingsModelString) Journal.getModel()).getStringValue(),
				((SettingsModelString) MeSH.getModel()).getStringValue(),
				((SettingsModelString) GenQuery.getModel()).getStringValue(), sortDate,
				((SettingsModelBoolean) onlyOA.getModel()).getBooleanValue());

		// Now Run it
		String xmlResult;
		try {
			xmlResult = EpmcHelpers.askEpmc(EpmcHelpers.buildQueryURL(queryText,
					((SettingsModelString) QType.getModel()).getStringValue(), null), null);

			// update the hit counter
			hitCount.setText("Hit Count: "
					+ EpmcHelpers.getStringFromXmlField(xmlResult, "hitCount") + " hits");

			// And the query test
			int i = 1;
			int charsPerLine = 50;
			String query = EpmcHelpers.getStringFromXmlField(xmlResult, "query");
			// query = (query ==null) ? "An error occurred connecting to
			// ePMC\nCheck
			// query and try again!":EpmcHelpers.xmlCleanSpecialChars(query);
			// Insert some linebreaks
			while (query.length() > (charsPerLine * i)) {
				query = query.substring(0, (charsPerLine * i)) + "\n"
						+ query.substring(charsPerLine * i);
				i++;
			}
			m_queryString.setStringValue(query);
		} catch (CanceledExecutionException e) {
			// Do nothing!
		}

	}

	protected void doClearQuery() {
		// Reset the query test dialogs
		hitCount.setText("Hit Count: Click 'Test Query'");
		m_queryString.setStringValue("\n\n\n");
	}

	/**
	 * @return The settings model for the page size
	 */
	static SettingsModelIntegerBounded createPageSizeModel() {
		return new SettingsModelIntegerBounded("Page Size", 1000, 1, 1000);
	}

	/**
	 * @return The settings model for the optional EBI email address
	 */
	static SettingsModelString createEmailModel() {
		return new SettingsModelString("email", null);
	}

}
