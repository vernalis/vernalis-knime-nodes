/*******************************************************************************
 *  Copyright (C) 2014 Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 *  
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
package com.vernalis.pdbconnector;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector.config.PdbConnectorConfig;

/**
 * PdbConnectorNode dialog class.
 * 
 * @deprecated Use {@link PdbConnectorXmlQueryNodeDialog2}
 */
@Deprecated
public class PdbConnectorXmlQueryNodeDialog extends NodeDialogPane {
	private static final NodeLogger logger = NodeLogger
			.getLogger(PdbConnectorXmlQueryNodeDialog.class);

	private JButton m_testButton = null;
	private JTextArea m_feedbackString = null;
	private JButton m_clearButton = null;
	private String m_lastError = "";
	private ReportOptionsDialog m_reportDlg = null;
	private DialogComponent m_ligandImgSizeDlg = null;
	private FlowVariableModel fvm;
	private DialogComponent m_xmlQuery = null;

	private DialogComponent m_usePOST = null;
	private DialogComponent m_maxUrlLength = null;

	/**
	 * Instantiates a new pdb connector node dialog.
	 * 
	 * @param config
	 *            the configuration
	 */
	public PdbConnectorXmlQueryNodeDialog(final PdbConnectorConfig config) {
		super();

		if (!config.isOK()) {
			m_lastError = config.getLastErrorMessage();
			logger.fatal(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd");
			logger.fatal("Last Error: " + m_lastError);
		} else {
			createMasterQueryPanel(config);
			createReportPanels(config);
			// And create the flow variable model associated with the query
			// string box
			fvm = createFlowVariableModel(
					(SettingsModelFlowVariableCompatible) m_xmlQuery.getModel());
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
	public final void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		assert settings != null;
		assert specs != null;
		if (!m_lastError.isEmpty()) {
			throw new NotConfigurableException(
					"Error loading query and report definitions from PdbConnectorConfig.xml/.dtd"
							+ " (" + m_lastError + ")");
		}

		if (m_xmlQuery != null) {
			m_xmlQuery.loadSettingsFrom(settings, specs);
		}
		if (m_usePOST != null) {
			m_usePOST.loadSettingsFrom(settings, specs);
		}
		if (m_maxUrlLength != null) {
			m_maxUrlLength.loadSettingsFrom(settings, specs);
		}
		if (m_reportDlg != null) {
			m_reportDlg.loadSettingsFrom(settings, specs);
		}
		if (m_ligandImgSizeDlg != null) {
			m_ligandImgSizeDlg.loadSettingsFrom(settings, specs);
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
		if (m_xmlQuery != null) {
			m_xmlQuery.saveSettingsTo(settings);
		}
		if (m_usePOST != null) {
			m_usePOST.saveSettingsTo(settings);
		}
		if (m_maxUrlLength != null) {
			m_maxUrlLength.saveSettingsTo(settings);
		}
		if (m_reportDlg != null) {
			m_reportDlg.saveSettingsTo(settings);
		}
		if (m_ligandImgSizeDlg != null) {
			m_ligandImgSizeDlg.saveSettingsTo(settings);
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

		// Add the Tab to the dialog
		super.addTab("Query Options", tab);

		// Start a GBC layout
		GridBagConstraints cons = new GridBagConstraints();
		cons.weightx = 0.0;
		cons.weighty = 0.0;
		cons.gridx = 0;// x cell coord
		cons.gridy = 0;// y cell coord
		// cons.gridwidth = GridBagConstraints.REMAINDER;// occupy all columns
		cons.gridheight = 1;
		cons.anchor = GridBagConstraints.WEST;// anchor to centre left

		// new line
		// cons.gridwidth = 1;
		cons.fill = GridBagConstraints.NONE;

		m_ligandImgSizeDlg = new DialogComponentStringSelection(
				new SettingsModelString(PdbConnectorXmlQueryNodeModel.LIGAND_IMG_SIZE_KEY,
						config.getLigandImgOptions().getDefaultLabel()),
				"Ligand Image Size", config.getLigandImgOptions().getLabels());
		m_ligandImgSizeDlg.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		tab.add(m_ligandImgSizeDlg.getComponentPanel(), cons);

		// Add the Use POST query method checkbox
		cons.gridx = 0;
		++(cons.gridy);
		m_usePOST = new DialogComponentBoolean(
				new SettingsModelBoolean(PdbConnectorXmlQueryNodeModel.USE_POST_KEY, true),
				"Use POST Query method (Faster)");
		m_usePOST.getModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				// Set or unset the enabled status of the max url length
				setMaxUrlEnabled();
			}
		});
		tab.add(m_usePOST.getComponentPanel(), cons);

		// Add the MAX URL Length setting, makin sure enabled matched the POST
		// selection
		++(cons.gridx);
		m_maxUrlLength = new DialogComponentNumber(
				new SettingsModelIntegerBounded(PdbConnectorXmlQueryNodeModel.MAX_URL_LENGTH_KEY,
						2000, 1000, 8000),
				"Max. Report GET URL Length", 100, 5);
		m_maxUrlLength.getModel()
				.setEnabled(!((SettingsModelBoolean) m_usePOST.getModel()).getBooleanValue());
		tab.add(m_maxUrlLength.getComponentPanel(), cons);

		// new line
		++(cons.gridx);
		// cons.gridx = 0;
		// ++(cons.gridy);

		// Add CLEAR QUERIES Button
		m_clearButton = new JButton("Clear Query");
		m_clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				doClearQueries();
			}
		});
		tab.add(m_clearButton, cons);

		// Add the TEST BUTTON
		++(cons.gridx);

		m_testButton = new JButton("Test Query");
		m_testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				doTestQuery();
			}
		});
		tab.add(m_testButton, cons);

		// Add the MESSAGES BOX
		++(cons.gridy);
		cons.fill = GridBagConstraints.HORIZONTAL;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.gridx = 0;

		m_feedbackString = new JTextArea();
		m_feedbackString.setEditable(false);
		m_feedbackString.setLineWrap(true);
		m_feedbackString.setWrapStyleWord(true);
		m_feedbackString.setRows(2);
		m_feedbackString.setBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Messages"));
		tab.add(m_feedbackString, cons);

		// Add the XML QUERY BOX
		m_xmlQuery = new DialogComponentMultiLineString(
				new SettingsModelString(PdbConnectorXmlQueryNodeModel.XML_QUERY_KEY, ""),
				"XML Query");

		cons.gridx = 0;
		++(cons.gridy);
		cons.fill = GridBagConstraints.BOTH;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.weightx = 1.0;
		cons.weighty = 0.8;
		cons.anchor = GridBagConstraints.CENTER;
		tab.add(m_xmlQuery.getComponentPanel(), cons);

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

		System.out.println(fvm.isVariableReplacementEnabled());
		System.out.println(fvm.getInputVariableName());
		// start query test on a separate daemon worker thread
		try {
			Thread thread = new Thread(new TestQuery());
			thread.setDaemon(true);
			thread.start();
		} catch (SecurityException e) {
			m_feedbackString.setText(e.getLocalizedMessage());
		}
	}

	/**
	 * Clears all queries.
	 * 
	 * All query options are unselected, and query parameters are reset to
	 * default values.
	 */
	private void doClearQueries() {
		m_feedbackString.setText("");
		((SettingsModelString) m_xmlQuery.getModel()).setStringValue("");
		// We also clear the flow variable selection
		fvm.setInputVariableName(null);

	}

	/**
	 * Sets the enabled status of the Max Url Length. Called on load settings
	 * and when use POST change listener is triggered
	 */
	private void setMaxUrlEnabled() {
		if (m_maxUrlLength != null) {
			m_maxUrlLength.getModel()
					.setEnabled(!((SettingsModelBoolean) m_usePOST.getModel()).getBooleanValue());
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
				EventQueue.invokeLater(new EnableComponent(m_testButton, false));
				EventQueue.invokeLater(new UpdateTextField(m_feedbackString, "Processing..."));

				// Get the xml query string
				String xmlQuery;
				if (fvm.isVariableReplacementEnabled()) {
					xmlQuery = getAvailableFlowVariables().get(fvm.getInputVariableName())
							.getStringValue();
					EventQueue.invokeLater(
							new UpdateTextField(m_feedbackString, "Trying flow var value from"
									+ fvm.getInputVariableName() + "for query...\n" + xmlQuery));
				} else {
					xmlQuery = ((SettingsModelString) m_xmlQuery.getModel()).getStringValue();
				}
				if (xmlQuery == null) {
					// NB WE dont worry about an empty string here in case the
					// user is using a flow variable - the NodeModel will take
					// care of that.
					throw new InvalidSettingsException("No Query string entered");
				}
				if ("".equals(xmlQuery)) {
					// But we will warn the user!
					String warning = "Warning - Empty query string!";
					if (fvm.isVariableReplacementEnabled()) {
						warning += "\nFlow variable currently empty";
					}
					EventQueue.invokeLater(new UpdateTextField(m_feedbackString, warning));
				}

				// Execute the query!
				List<String> pdbIds = ModelHelperFunctions.postQuery(xmlQuery);

				// Display the result count
				EventQueue.invokeLater(new UpdateTextField(m_feedbackString,
						"Query retrieved " + Integer.toString(pdbIds.size()) + " hits."));
			} catch (InvalidSettingsException e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(
						new UpdateTextField(m_feedbackString, e.getLocalizedMessage()));

			} catch (IOException e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(
						new UpdateTextField(m_feedbackString, e.getLocalizedMessage()));
			} catch (Exception e) {
				logger.warn(e.getLocalizedMessage());
				EventQueue.invokeLater(
						new UpdateTextField(m_feedbackString, e.getLocalizedMessage()));
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
