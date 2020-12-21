/*******************************************************************************
 * Copyright (c) 2016, 2020 Vernalis (R&D) Ltd, based on earlier PDB Connector work.
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
package com.vernalis.pdbconnector.nodes.pdbconnector;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

import org.knime.core.data.StringValue;
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector.ModelHelperFunctions2;
import com.vernalis.pdbconnector.QueryOptionDialog;
import com.vernalis.pdbconnector.QueryOptionModel;
import com.vernalis.pdbconnector.ReportOptionsDialog2;
import com.vernalis.pdbconnector.XMLFormatter;
import com.vernalis.pdbconnector.config.PdbConnectorConfig2;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryCategory;
import com.vernalis.pdbconnector.config.QueryOption;

/**
 * The node dialog pane for the PDB Connector node family
 * 
 */
@Deprecated
public class AbstractPdbConnectorNodeDialog extends NodeDialogPane {

	private static final int SCROLL_SPEED = 16;
	private static final NodeLogger logger =
			NodeLogger.getLogger(AbstractPdbConnectorNodeDialog.class);
	private final List<QueryOptionDialog> m_queryDlgs = new ArrayList<>();
	private QueryOptionDialog m_simDlg = null;// similarity dialog
	private JButton m_testButton = null;
	private JTextArea m_feedbackString = null;
	private JTextArea m_queryString = null;
	private DialogComponentMultiLineString m_xmlQuery;
	private JButton m_clearButton = null;
	private JButton m_copyButton = null;
	private String m_lastError = "";
	private ReportOptionsDialog2 m_reportDlg = null;
	private DialogComponent m_ligandImgSizeDlg = null;
	private DialogComponent m_conjunctionDlg = null;

	private FlowVariableModel fvm = null;

	private DialogComponent m_usePOST = null;
	private DialogComponent m_maxUrlLength = null;

	protected boolean m_hasQueryBuilder, m_runQuery, m_runReport;
	private DialogComponentColumnNameSelection m_idCol = null;
	private DialogComponentString m_xmlVarName = null;

	/**
	 * Instantiates a new pdb connector node dialog.
	 * 
	 * @param config
	 *            the configuration
	 * @param hasQueryBuilder
	 *            Does the node dialog include a query builder?
	 * @param runQuery
	 *            Does the node execute a query? (If it does, and the node does
	 *            not have a query builder, then a text box is included for the
	 *            XML Query to be entered)
	 * @param runReport
	 *            Does the node run a report?
	 * @throws IllegalArgumentException
	 *             if an invalid combination of boolean parameters is supplied
	 */
	public AbstractPdbConnectorNodeDialog(final PdbConnectorConfig2 config,
			boolean hasQueryBuilder, boolean runQuery, boolean runReport)
			throws IllegalArgumentException {
		super();
		m_hasQueryBuilder = hasQueryBuilder;
		m_runQuery = runQuery;
		m_runReport = runReport;

		if (!config.isOK()) {
			m_lastError = config.getLastErrorMessage();
			logger.fatal(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd");
			logger.fatal("Last Error: " + m_lastError);
		} else {
			createMasterQueryPanel(config);
			if (m_runQuery && !m_hasQueryBuilder) {
				// create the flow variable model associated with the query
				// string box
				fvm = createFlowVariableModel(
						(SettingsModelFlowVariableCompatible) m_xmlQuery
								.getModel());
			}
			if (m_runReport) {
				createReportPanels(config);
			}
			if (m_hasQueryBuilder) {
				createQueryPanels(config);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeDialogPane#loadSettingsFrom(org.knime.core.node
	 * .NodeSettingsRO, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public final void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		assert settings != null;
		assert specs != null;
		if (!m_lastError.isEmpty()) {
			throw new NotConfigurableException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}
		for (QueryOptionDialog queryDialog : m_queryDlgs) {
			queryDialog.loadSettingsFrom(settings, specs);
		}
		if (m_simDlg != null) {
			m_simDlg.loadSettingsFrom(settings, specs);
		}
		if (m_reportDlg != null) {
			m_reportDlg.loadSettingsFrom(settings, specs);
		}
		if (m_ligandImgSizeDlg != null) {
			m_ligandImgSizeDlg.loadSettingsFrom(settings, specs);
		}
		if (m_conjunctionDlg != null) {
			m_conjunctionDlg.loadSettingsFrom(settings, specs);
		}
		if (m_usePOST != null) {
			m_usePOST.loadSettingsFrom(settings, specs);
		}
		if (m_maxUrlLength != null) {
			m_maxUrlLength.loadSettingsFrom(settings, specs);
		}
		if (m_xmlQuery != null) {
			m_xmlQuery.loadSettingsFrom(settings, specs);
		}
		if (m_xmlVarName != null) {
			m_xmlVarName.loadSettingsFrom(settings, specs);
		}
		if (m_idCol != null) {
			m_idCol.loadSettingsFrom(settings, specs);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeDialogPane#saveSettingsTo(org.knime.core.node
	 * .NodeSettingsWO)
	 */
	@Override
	public final void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		if (!m_lastError.isEmpty()) {
			throw new InvalidSettingsException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}
		for (QueryOptionDialog queryDialog : m_queryDlgs) {
			queryDialog.saveSettingsTo(settings);
		}
		if (m_simDlg != null) {
			m_simDlg.saveSettingsTo(settings);
		}
		if (m_reportDlg != null) {
			m_reportDlg.saveSettingsTo(settings);
		}
		if (m_ligandImgSizeDlg != null) {
			m_ligandImgSizeDlg.saveSettingsTo(settings);
		}
		if (m_conjunctionDlg != null) {
			m_conjunctionDlg.saveSettingsTo(settings);
		}
		if (m_usePOST != null) {
			m_usePOST.saveSettingsTo(settings);
		}
		if (m_maxUrlLength != null) {
			m_maxUrlLength.saveSettingsTo(settings);
		}
		if (m_xmlQuery != null) {
			m_xmlQuery.saveSettingsTo(settings);
		}
		if (m_xmlVarName != null) {
			m_xmlVarName.saveSettingsTo(settings);
		}
		if (m_idCol != null) {
			m_idCol.saveSettingsTo(settings);
		}
	}

	/**
	 * Creates the master query panel.
	 * 
	 * @param config
	 *            the configuration
	 */
	@SuppressWarnings("unchecked")
	private void createMasterQueryPanel(final PdbConnectorConfig2 config) {
		JPanel tab = new JPanel(new GridBagLayout());
		super.addTab("Query Options", tab);
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0.0;
		cons.weighty = 0.0;
		cons.gridx = 0;// x cell coord
		cons.gridy = 0;// y cell coord
		cons.gridwidth = GridBagConstraints.REMAINDER;// occupy all columns
		cons.gridheight = 1;
		cons.anchor = GridBagConstraints.WEST;// anchor to centre left

		if (m_hasQueryBuilder) {
			// The simlarity filter - part of the query builder!
			m_simDlg = new QueryOptionDialog(config.getSimilarity());
			tab.add(m_simDlg, cons);

			// new line
			++(cons.gridy);
			int oldGridWidth = cons.gridwidth;
			cons.gridwidth = 2;
			m_conjunctionDlg = new DialogComponentStringSelection(
					new SettingsModelString(
							AbstractPdbConnectorNodeModel.CONJUNCTION_KEY,
							Properties.CONJUNCTION_AND_LABEL),
					"Match multiple query terms using",
					Properties.CONJUNCTION_AND_LABEL,
					Properties.CONJUNCTION_OR_LABEL);
			m_conjunctionDlg.getComponentPanel()
					.setLayout(new FlowLayout(FlowLayout.LEFT));
			tab.add(m_conjunctionDlg.getComponentPanel(), cons);
			cons.gridwidth = oldGridWidth;
			++(cons.gridx);
		}

		if (m_runReport) {
			// Some report options - ligand image size, and post/get and query
			// size options
			// new line
			cons.gridx = 0;
			++(cons.gridy);
			m_ligandImgSizeDlg = new DialogComponentStringSelection(
					new SettingsModelString(
							AbstractPdbConnectorNodeModel.LIGAND_IMG_SIZE_KEY,
							config.getLigandImgOptions().getDefaultLabel()),
					"Ligand Image Size",
					config.getLigandImgOptions().getLabels());
			m_ligandImgSizeDlg.getComponentPanel()
					.setLayout(new FlowLayout(FlowLayout.LEFT));
			tab.add(m_ligandImgSizeDlg.getComponentPanel(), cons);

			// new line
			cons.gridx = 0;
			++(cons.gridy);
			cons.gridwidth = 2;

			// Add the use POST service checkbox
			m_usePOST = new DialogComponentBoolean(
					new SettingsModelBoolean(
							AbstractPdbConnectorNodeModel.USE_POST_KEY, true),
					"Use POST method (Faster)");
			tab.add(m_usePOST.getComponentPanel(), cons);

			// Add the MAX URL Length setting
			++(cons.gridx);
			++(cons.gridx);
			// cons.gridx = 0;
			// ++(cons.gridy);
			m_maxUrlLength = new DialogComponentNumber(
					new SettingsModelIntegerBounded(
							AbstractPdbConnectorNodeModel.MAX_QUERY_LENGTH_KEY,
							2000, 1000, 100000),
					"Max. Report request size", 100, 5);
			m_maxUrlLength.getComponentPanel()
					.setLayout(new FlowLayout(FlowLayout.LEFT));
			tab.add(m_maxUrlLength.getComponentPanel(), cons);
		}

		if (m_hasQueryBuilder || m_runQuery) {
			// Add the Clear/Test/Copy buttons
			// new line
			cons.anchor = GridBagConstraints.CENTER;
			cons.gridx = 0;
			++(cons.gridy);
			cons.gridwidth = 1;
			m_clearButton = new JButton("Clear Query");
			m_clearButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					doClearQueries();
				}
			});
			tab.add(m_clearButton, cons);

			++(cons.gridx);
			m_copyButton = new JButton("Copy Query");
			m_copyButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					StringSelection ss = null;
					if (m_queryString != null) {
						// We populate the dialog with the XML from the query
						// builder first
						try {
							NodeSettings tmpSettings =
									new NodeSettings("DO_TEST_QUERY");
							List<QueryOptionModel> queryModels =
									new ArrayList<>();
							QueryOptionModel simModel = null;
							for (QueryOptionDialog dlg : m_queryDlgs) {
								dlg.saveSettingsTo(tmpSettings);
								QueryOptionModel model = new QueryOptionModel(
										dlg.getQueryOption());
								model.loadValidatedSettingsFrom(tmpSettings);
								queryModels.add(model);
							}
							m_simDlg.saveSettingsTo(tmpSettings);
							simModel = new QueryOptionModel(
									m_simDlg.getQueryOption());
							simModel.loadValidatedSettingsFrom(tmpSettings);
							// select the appropriate conjunction string (either
							// AND or
							// OR)
							String conjunction =
									((SettingsModelString) m_conjunctionDlg
											.getModel()).getStringValue()
													.equals(Properties.CONJUNCTION_AND_LABEL)
															? Properties.CONJUNCTION_AND
															: Properties.CONJUNCTION_OR;
							String xmlQuery = ModelHelperFunctions2.getXmlQuery(
									queryModels, simModel, conjunction);
							m_queryString
									.setText(XMLFormatter.indentXML(xmlQuery));
						} catch (Exception e1) {
							// do nothing - the dialog will simply not be
							// populated
						}

						// Get the xml query string and display it.
						ss = new StringSelection(m_queryString.getText());
					} else if (m_xmlQuery != null) {
						for (Component comp : m_xmlQuery.getComponentPanel()
								.getComponents()) {
							if (comp instanceof JTextArea) {
								ss = new StringSelection(XMLFormatter.indentXML(
										((JTextArea) comp).getText()));
								break;
							}
						}
					}
					if (ss != null) {
						Toolkit.getDefaultToolkit().getSystemClipboard()
								.setContents(ss, null);
					}

				}
			});
			tab.add(m_copyButton, cons);

			++(cons.gridx);
			m_testButton = new JButton("Test Query");
			m_testButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent arg0) {
					doTestQuery();
				}
			});
			tab.add(m_testButton, cons);

			// Add the 'Messages' area
			++(cons.gridx);
			cons.anchor = GridBagConstraints.WEST;
			m_feedbackString = new JTextArea();
			m_feedbackString.setEditable(false);
			m_feedbackString.setLineWrap(true);
			m_feedbackString.setWrapStyleWord(true);
			m_feedbackString.setRows(2);
			m_feedbackString.setColumns(30);
			m_feedbackString.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Messages"));
			tab.add(m_feedbackString, cons);

			// New line
			cons.gridx = 0;
			++(cons.gridy);
			cons.anchor = GridBagConstraints.CENTER;
			cons.gridwidth = 3;
			m_xmlVarName = new DialogComponentString(new SettingsModelString(
					AbstractPdbConnectorNodeModel.XML_VARNAME_KEY, "xmlQuery"),
					"XML Query Variable Name", true, 20);
			tab.add(m_xmlVarName.getComponentPanel(), cons);

			cons.anchor = GridBagConstraints.WEST;
			cons.gridwidth = 1;
		}

		if (m_hasQueryBuilder) {
			// Add the Non-editable XML Query string box
			cons.gridx = 0;
			++(cons.gridy);
			cons.fill = GridBagConstraints.VERTICAL;
			cons.gridwidth = GridBagConstraints.REMAINDER;
			cons.weightx = 1.0;
			cons.weighty = 1.0;
			cons.anchor = GridBagConstraints.WEST;
			m_queryString = new JTextArea();
			m_queryString.setColumns(80);
			m_queryString.setEditable(false);
			// m_queryString.setLineWrap(true);
			// m_queryString.setWrapStyleWord(true);
			m_queryString.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "XML Query string"));
			tab.add(m_queryString, cons);
		} else if (m_runQuery) {
			// Add an editable XML Query Sting dialog
			m_xmlQuery = new DialogComponentMultiLineString(
					new SettingsModelString(
							AbstractPdbConnectorNodeModel.XML_QUERY_KEY, ""),
					"", false, 80, 35);
			m_xmlQuery.getComponentPanel()
					.setLayout(new FlowLayout(FlowLayout.LEFT));
			m_xmlQuery.getComponentPanel()
					.setBorder(BorderFactory.createTitledBorder(
							BorderFactory.createEtchedBorder(),
							"XML Query string"));
			cons.gridx = 0;
			++(cons.gridy);
			cons.fill = GridBagConstraints.VERTICAL;
			cons.gridwidth = GridBagConstraints.REMAINDER;
			cons.weightx = 1.0;
			cons.weighty = 0.8;
			cons.anchor = GridBagConstraints.CENTER;
			tab.add(m_xmlQuery.getComponentPanel(), cons);
		}
		if (!m_hasQueryBuilder && !m_runQuery && m_runReport) {
			// Need a column selector
			m_idCol = new DialogComponentColumnNameSelection(
					new SettingsModelString(
							AbstractPdbConnectorNodeModel.ID_COL_NAME_KEY,
							null),
					"PDB ID Columns", 0, StringValue.class);
			m_idCol.getComponentPanel()
					.setLayout(new FlowLayout(FlowLayout.LEFT));
			cons.gridx = 0;
			++(cons.gridy);
			cons.fill = GridBagConstraints.BOTH;
			cons.gridwidth = GridBagConstraints.REMAINDER;
			cons.weightx = 1.0;
			cons.weighty = 0.8;
			cons.anchor = GridBagConstraints.WEST;
			tab.add(m_idCol.getComponentPanel(), cons);
		}
	}

	/**
	 * Creates the tabs for each query category.
	 * 
	 * @param config
	 *            the configuration
	 */
	private void createQueryPanels(final PdbConnectorConfig2 config) {
		m_queryDlgs.clear();
		List<QueryCategory> categories = config.getQueryCategories();
		for (QueryCategory category : categories) {
			List<QueryOption> queries = category.getQueryOptions();
			if (!queries.isEmpty()) {
				JPanel tab = new JPanel();
				tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
				final JScrollPane scrollPane = new JScrollPane(tab);
				// Make mousewheel scrolling a bit faster...
				System.out.println(
						scrollPane.getVerticalScrollBar().getUnitIncrement());
				scrollPane.getVerticalScrollBar()
						.setUnitIncrement(SCROLL_SPEED);
				super.addTab(category.getLabel(), scrollPane);
				for (QueryOption query : queries) {
					QueryOptionDialog queryDlg = new QueryOptionDialog(query);
					tab.add(queryDlg);
					m_queryDlgs.add(queryDlg);
				}
			}
		}
	}

	/**
	 * Creates the report options panel.
	 * 
	 * @param config
	 *            the configuration
	 */
	private void createReportPanels(final PdbConnectorConfig2 config) {
		m_reportDlg = new ReportOptionsDialog2(config);
		final JScrollPane scrollPane = new JScrollPane(m_reportDlg);
		scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		super.addTab("Report Options", scrollPane);
	}

	/**
	 * Tests the current query.
	 * 
	 * Query is executed on a separate daemon worker thread. On completion, the
	 * result count is displayed.
	 */
	private void doTestQuery() {
		// start query test on a separate daemon worker thread
		try {
			Thread thread = new Thread(new TestQuery());
			thread.setDaemon(true);
			thread.start();
		} catch (SecurityException e) {
			m_feedbackString.setText("Error");
			m_queryString.setText(e.getLocalizedMessage());
		}
	}

	/**
	 * Clears all queries.
	 * 
	 * All query options are unselected, and query parameters are reset to
	 * default values.
	 */
	private void doClearQueries() {
		if (m_hasQueryBuilder) {
			// Clear all query options
			for (QueryOptionDialog dlg : m_queryDlgs) {
				dlg.setSelected(false);
				dlg.resetParams();
				m_feedbackString.setText("");
				m_queryString.setText("");
			}
			// Clear the sequence similarity filter
			m_simDlg.setSelected(false);
			m_simDlg.resetParams();
			// Reset the conjunction dropdown
			((SettingsModelString) m_conjunctionDlg.getModel())
					.setStringValue(Properties.CONJUNCTION_AND_LABEL);
		} else {
			((SettingsModelString) m_xmlQuery.getModel()).setStringValue("");
		}
	}

	/**
	 * Worker class to execute the "Test Query" function on a separate thread.
	 */
	private final class TestQuery implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			try {
				// Disable button to prevent further tests being launched.
				EventQueue
						.invokeLater(new EnableComponent(m_testButton, false));
				EventQueue.invokeLater(
						new UpdateTextField(m_feedbackString, "Processing..."));

				if (m_hasQueryBuilder) {
					EventQueue.invokeLater(
							new UpdateTextField(m_queryString, ""));
					// Copy current dialog settings to temporary
					// QueryOptionModels

					NodeSettings tmpSettings =
							new NodeSettings("DO_TEST_QUERY");
					List<QueryOptionModel> queryModels = new ArrayList<>();
					QueryOptionModel simModel = null;
					for (QueryOptionDialog dlg : m_queryDlgs) {
						dlg.saveSettingsTo(tmpSettings);
						QueryOptionModel model =
								new QueryOptionModel(dlg.getQueryOption());
						model.loadValidatedSettingsFrom(tmpSettings);
						queryModels.add(model);
					}
					m_simDlg.saveSettingsTo(tmpSettings);
					simModel = new QueryOptionModel(m_simDlg.getQueryOption());
					simModel.loadValidatedSettingsFrom(tmpSettings);
					// select the appropriate conjunction string (either AND or
					// OR)
					String conjunction =
							((SettingsModelString) m_conjunctionDlg.getModel())
									.getStringValue()
									.equals(Properties.CONJUNCTION_AND_LABEL)
											? Properties.CONJUNCTION_AND
											: Properties.CONJUNCTION_OR;
					// Get the xml query string and display it.
					String xmlQuery = ModelHelperFunctions2
							.getXmlQuery(queryModels, simModel, conjunction);
					EventQueue.invokeLater(new UpdateTextField(m_queryString,
							XMLFormatter.indentXML(xmlQuery)));
					// Execute the query!
					List<String> pdbIds =
							ModelHelperFunctions2.postQuery(xmlQuery);
					// Display the result count
					EventQueue.invokeLater(new UpdateTextField(m_feedbackString,
							"Query retrieved " + Integer.toString(pdbIds.size())
									+ " hits."));
				} else {
					// Get the xml query string
					String xmlQuery;
					if (fvm.isVariableReplacementEnabled()) {
						xmlQuery = getAvailableFlowVariables()
								.get(fvm.getInputVariableName())
								.getStringValue();
						EventQueue.invokeLater(
								new UpdateTextField(m_feedbackString,
										"Trying flow var value from"
												+ fvm.getInputVariableName()
												+ "for query...\n" + xmlQuery));
					} else {
						xmlQuery = ((SettingsModelString) m_xmlQuery.getModel())
								.getStringValue();
						// Tidy up the xml!
						xmlQuery = XMLFormatter.indentXML(xmlQuery);
						((SettingsModelString) m_xmlQuery.getModel())
								.setStringValue(xmlQuery);
					}
					if (xmlQuery == null) {
						// NB WE dont worry about an empty string here in case
						// the
						// user is using a flow variable - the NodeModel will
						// take
						// care of that.
						throw new InvalidSettingsException(
								"No Query string entered");
					}
					if ("".equals(xmlQuery)) {
						// But we will warn the user!
						String warning = "Warning - Empty query string!";
						if (fvm.isVariableReplacementEnabled()) {
							warning += "\nFlow variable currently empty";
						}
						EventQueue.invokeLater(
								new UpdateTextField(m_feedbackString, warning));
					}

					// Execute the query!
					List<String> pdbIds =
							ModelHelperFunctions2.postQuery(xmlQuery);

					// Display the result count
					EventQueue.invokeLater(new UpdateTextField(m_feedbackString,
							"Query retrieved " + Integer.toString(pdbIds.size())
									+ " hits."));
				}
			} catch (InvalidSettingsException e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(
						new UpdateTextField(m_feedbackString, "Error"));
				EventQueue.invokeLater(new UpdateTextField(m_queryString,
						e.getLocalizedMessage()));
			} catch (IOException e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(
						new UpdateTextField(m_feedbackString, "Error"));
				EventQueue.invokeLater(new UpdateTextField(m_queryString,
						e.getLocalizedMessage()));
			} finally {
				EventQueue.invokeLater(new EnableComponent(m_testButton, true));
			}
		}
	}

	/**
	 * Worker class to update the text value of a Swing text component.
	 */
	private final class UpdateTextField implements Runnable {

		private final JTextComponent m_comp;
		private final String m_val;

		public UpdateTextField(final JTextComponent comp, final String val) {
			m_comp = comp;
			m_val = val;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (m_comp != null) {
				m_comp.setText(m_val);
			}
		}
	}

	/**
	 * Worker class to update the enabled state of a Swing component.
	 */
	private final class EnableComponent implements Runnable {

		private final JComponent m_comp;
		private final boolean m_isEnabled;

		public EnableComponent(final JComponent comp, final boolean isEnabled) {
			m_comp = comp;
			m_isEnabled = isEnabled;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if (m_comp != null) {
				m_comp.setEnabled(m_isEnabled);
			}
		}
	}
}
