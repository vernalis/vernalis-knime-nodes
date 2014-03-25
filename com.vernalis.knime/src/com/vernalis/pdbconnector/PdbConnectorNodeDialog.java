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
package com.vernalis.pdbconnector;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector.config.PdbConnectorConfig;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryCategory;
import com.vernalis.pdbconnector.config.QueryOption;

/**
 * PdbConnectorNode dialog class.
 */
public class PdbConnectorNodeDialog extends NodeDialogPane {
	private static final NodeLogger logger = NodeLogger
			.getLogger(PdbConnectorNodeDialog.class);
	private final List<QueryOptionDialog> m_queryDlgs = new ArrayList<QueryOptionDialog>();
	private QueryOptionDialog m_simDlg = null;// similarity dialog
	private JButton m_testButton = null;
	private JTextField m_resultCount = null;
	private JTextArea m_queryString = null;
	private JButton m_clearButton = null;
	private String m_lastError = "";
	private ReportOptionsDialog m_reportDlg = null;
	private DialogComponent m_ligandImgSizeDlg = null;
	private DialogComponent m_conjunctionDlg = null;

	private DialogComponent m_usePOST = null;
	private DialogComponent m_maxUrlLength = null;

	/**
	 * Instantiates a new pdb connector node dialog.
	 * 
	 * @param config
	 *            the configuration
	 */
	public PdbConnectorNodeDialog(final PdbConnectorConfig config) {
		super();
		if (!config.isOK()) {
			m_lastError = config.getLastErrorMessage();
			logger.fatal("Error loading query and report definitions from PdbConnectorConfig.xml/.dtd");
			logger.fatal("Last Error: " + m_lastError);
		} else {
			createMasterQueryPanel(config);
			createReportPanels(config);
			createQueryPanels(config);
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
			// Needs to be backwards compatible, will use post if setting not
			// available
			try {
				m_usePOST.loadSettingsFrom(settings, specs);
			} catch (Exception e) {
				((SettingsModelBoolean) m_usePOST.getModel())
						.setBooleanValue(true);
			}
		}
		if (m_maxUrlLength != null) {
			// Needs to be backwards compatible, will use 2000 from latest
			// release if setting not available.
			try {
				m_maxUrlLength.loadSettingsFrom(settings, specs);
			} catch (Exception e) {
				((SettingsModelIntegerBounded) m_maxUrlLength.getModel())
						.setIntValue(2000);
			}
		}
		setMaxUrlEnabled();

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
	}

	/**
	 * Creates the master query panel.
	 * 
	 * @param config
	 *            the configuration
	 */
	private void createMasterQueryPanel(final PdbConnectorConfig config) {
		JPanel tab = new JPanel(new GridBagLayout());
		super.addTab("Query Options", tab);
		GridBagConstraints cons = new GridBagConstraints();
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.weightx = 0.0;
		cons.weighty = 0.0;
		cons.gridx = 0;// x cell coord
		cons.gridy = 0;// y cell coord
		cons.gridwidth = GridBagConstraints.REMAINDER;// occupy all columns
		cons.gridheight = 1;
		cons.anchor = GridBagConstraints.WEST;// anchor to centre left

		m_simDlg = new QueryOptionDialog(config.getSimilarity());
		tab.add(m_simDlg, cons);

		// new line
		++(cons.gridy);
		cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;
		m_conjunctionDlg = new DialogComponentStringSelection(
				new SettingsModelString(PdbConnectorNodeModel.CONJUNCTION_KEY,
						Properties.CONJUNCTION_AND_LABEL),
				"Match multiple query terms using",
				Properties.CONJUNCTION_AND_LABEL,
				Properties.CONJUNCTION_OR_LABEL);
		m_conjunctionDlg.getComponentPanel().setLayout(
				new FlowLayout(FlowLayout.LEFT));
		tab.add(m_conjunctionDlg.getComponentPanel(), cons);

		++(cons.gridx);
		m_ligandImgSizeDlg = new DialogComponentStringSelection(
				new SettingsModelString(
						PdbConnectorNodeModel.LIGAND_IMG_SIZE_KEY, config
								.getLigandImgOptions().getDefaultLabel()),
				"Ligand Image Size", config.getLigandImgOptions().getLabels());
		m_ligandImgSizeDlg.getComponentPanel().setLayout(
				new FlowLayout(FlowLayout.LEFT));
		tab.add(m_ligandImgSizeDlg.getComponentPanel(), cons);

		// new line
		cons.gridx = 0;
		++(cons.gridy);

		// Add the use POST service checkbox
		m_usePOST = new DialogComponentBoolean(new SettingsModelBoolean(
				PdbConnectorNodeModel.USE_POST_KEY, true),
				"Use POST Query method (Faster)");
		m_usePOST.getModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				// Set or unset the enabled status of the max url length
				setMaxUrlEnabled();
			}
		});
		tab.add(m_usePOST.getComponentPanel(), cons);

		// Add the MAX URL Length setting, making sure enabled status matches
		// the POST selection
		++(cons.gridx);
		m_maxUrlLength = new DialogComponentNumber(
				new SettingsModelIntegerBounded(
						PdbConnectorNodeModel.MAX_URL_LENGTH_KEY, 2000,
						1000, 8000), "Max. Report GET URL Length", 100, 5);
		m_maxUrlLength.getModel().setEnabled(
				!((SettingsModelBoolean) m_usePOST.getModel())
						.getBooleanValue());
		tab.add(m_maxUrlLength.getComponentPanel(), cons);

		// new line
		cons.gridx = 0;
		++(cons.gridy);
		m_clearButton = new JButton("Clear All Queries");
		m_clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				doClearQueries();
			}
		});
		tab.add(m_clearButton, cons);

		++(cons.gridx);
		m_testButton = new JButton("Test Query");
		m_testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				doTestQuery();
			}
		});
		tab.add(m_testButton, cons);

		++(cons.gridx);
		tab.add(new JLabel("Result Count"), cons);

		++(cons.gridx);
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.weightx = 1.0;
		m_resultCount = new JTextField();
		m_resultCount.setColumns(12);
		m_resultCount.setEditable(false);
		m_resultCount.setHorizontalAlignment(SwingConstants.RIGHT);
		tab.add(m_resultCount, cons);

		cons.gridx = 0;
		++(cons.gridy);
		cons.fill = GridBagConstraints.BOTH;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.weightx = 1.0;
		cons.weighty = 1.0;
		cons.anchor = GridBagConstraints.CENTER;
		m_queryString = new JTextArea();
		m_queryString.setEditable(false);
		m_queryString.setLineWrap(true);
		m_queryString.setWrapStyleWord(true);
		m_queryString.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "xmlQuery string"));
		tab.add(m_queryString, cons);
	}

	/**
	 * Creates the tabs for each query category.
	 * 
	 * @param config
	 *            the configuration
	 */
	private void createQueryPanels(final PdbConnectorConfig config) {
		m_queryDlgs.clear();
		List<QueryCategory> categories = config.getQueryCategories();
		for (QueryCategory category : categories) {
			List<QueryOption> queries = category.getQueryOptions();
			if (!queries.isEmpty()) {
				JPanel tab = new JPanel();
				tab.setLayout(new BoxLayout(tab, BoxLayout.Y_AXIS));
				super.addTab(category.getLabel(), new JScrollPane(tab));
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
	private void createReportPanels(final PdbConnectorConfig config) {
		m_reportDlg = new ReportOptionsDialog(config);
		super.addTab("Report Options", new JScrollPane(m_reportDlg));
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
			m_resultCount.setText("Error");
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
		// Clear all query options
		for (QueryOptionDialog dlg : m_queryDlgs) {
			dlg.setSelected(false);
			dlg.resetParams();
			m_resultCount.setText("");
			m_queryString.setText("");
		}
		// Clear the sequence similarity filter
		m_simDlg.setSelected(false);
		m_simDlg.resetParams();
		// Reset the conjunction dropdown
		((SettingsModelString) m_conjunctionDlg.getModel())
				.setStringValue(Properties.CONJUNCTION_AND_LABEL);
	}
	

	/**
	 * Sets the enabled status of the Max Url Length. Called on load settings
	 * and when use POST change listener is triggered
	 */
	private void setMaxUrlEnabled() {
		if (m_maxUrlLength != null) {
			m_maxUrlLength.getModel().setEnabled(
					!((SettingsModelBoolean) m_usePOST.getModel())
							.getBooleanValue());
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
		@Override
		public void run() {
			try {
				// Disable button to prevent further tests being launched.
				EventQueue
						.invokeLater(new EnableComponent(m_testButton, false));
				EventQueue.invokeLater(new UpdateTextField(m_resultCount,
						"Processing..."));
				EventQueue.invokeLater(new UpdateTextField(m_queryString, ""));
				// Copy current dialog settings to temporary QueryOptionModels
				NodeSettings tmpSettings = new NodeSettings("DO_TEST_QUERY");
				List<QueryOptionModel> queryModels = new ArrayList<QueryOptionModel>();
				QueryOptionModel simModel = null;
				for (QueryOptionDialog dlg : m_queryDlgs) {
					dlg.saveSettingsTo(tmpSettings);
					QueryOptionModel model = new QueryOptionModel(
							dlg.getQueryOption());
					model.loadValidatedSettingsFrom(tmpSettings);
					queryModels.add(model);
				}
				m_simDlg.saveSettingsTo(tmpSettings);
				simModel = new QueryOptionModel(m_simDlg.getQueryOption());
				simModel.loadValidatedSettingsFrom(tmpSettings);
				// select the appropriate conjunction string (either AND or OR)
				String conjunction = ((SettingsModelString) m_conjunctionDlg
						.getModel()).getStringValue().equals(
						Properties.CONJUNCTION_AND_LABEL) ? Properties.CONJUNCTION_AND
						: Properties.CONJUNCTION_OR;
				// Get the xml query string and display it.
				String xmlQuery = ModelHelperFunctions.getXmlQuery(queryModels,
						simModel, conjunction);
				EventQueue.invokeLater(new UpdateTextField(m_queryString,
						xmlQuery));
				// Execute the query!
				List<String> pdbIds = ModelHelperFunctions.postQuery(xmlQuery);
				// Display the result count
				EventQueue.invokeLater(new UpdateTextField(m_resultCount,
						Integer.toString(pdbIds.size())));
			} catch (InvalidSettingsException e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(new UpdateTextField(m_resultCount,
						"Error"));
				EventQueue.invokeLater(new UpdateTextField(m_queryString, e
						.getLocalizedMessage()));
			} catch (IOException e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(new UpdateTextField(m_resultCount,
						"Error"));
				EventQueue.invokeLater(new UpdateTextField(m_queryString, e
						.getLocalizedMessage()));
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