/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ConvenientComboBoxRenderer;
import org.knime.core.node.util.StringHistory;
import org.knime.core.util.SimpleFileFilter;

/**
 * A dialog component providing a list box listing the files selected, a 'Add
 * file(s)' button to open a file chooser, and a 'Remove' button to remove the
 * selected files. A dropdown with files from history is also provided, and
 * optional flow variable model button and Title, border, and captions
 * 
 * @author s.roughley knime@vernalis.com
 * 
 */
public class DialogComponentListFilesSelector extends DialogComponent {

	private final JList<String> m_fileList;
	private final JComboBox<String> m_fileHistoryComboBox;
	private final DefaultListModel<String> m_fileListModel;
	private final JButton m_addButton, m_deleteButton, m_addFromHistoryButton, m_deleteAllButton;
	private final JLabel m_label;
	private final StringHistory m_fileHistory; // Not sure if we will need!
	private final Border m_border;
	private final List<SimpleFileFilter> m_fileFilter;
	private final FlowVariableModelButton m_fvmButton;

	// TODO: Add minimalist constructors

	/**
	 * Constructor with using default dimensions (350 x 12 rows) for the
	 * selected file list.
	 * 
	 * @param stringModel
	 *            The SettingsModel for the component - this must be a
	 *            {@link SettingsModelStringArrayFlowVarReplacable}
	 * @param title
	 *            The title, to be placed in the component border. Ignored if
	 *            hasBorder is {@code false}. If a border without title is
	 *            required, this should be set to {@code null}.
	 * @param hasBorder
	 *            {@code true} if the component is to be surrounded by a border.
	 *            See also {@code title} argument.
	 * @param label
	 *            This text is an optional descriptive label provided above the
	 *            file list
	 * @param fileHistoryID
	 *            The name of a file history to store previous files from the
	 *            node type.
	 * @param dialogType
	 *            The Dialog Type for the {@link JFileChooser} pop-up. This
	 *            should be one of {@link JFileChooser#DIRECTORIES_ONLY},
	 *            {@link JFileChooser#FILES_ONLY} or
	 *            {@link JFileChooser#FILES_AND_DIRECTORIES}.
	 * @param fvm
	 *            An optional {@link FlowVariableModel} for the SettingsModel.
	 *            If this is not {@code null}, then a button to add the files
	 *            from or two a flow variable is provided. The flow variable
	 *            needs to supply the full file paths as a string, with each
	 *            entry supplied by the deliminator used in the SettingsModel.
	 *            This is by default
	 *            {@link SettingsModelStringArrayFlowVarReplacable#DELIMINATOR}.
	 *            The value used can be obtained from
	 *            {@link SettingsModelStringArrayFlowVarReplacable#getDeliminator()}
	 * @param validExtensions
	 *            A list of valid file extensions. If {@code null}, then all
	 *            filetypes are accepted by the {@link JFileChooser} dialog.
	 *            Each separate array entry is added to a separate line in the
	 *            dropdown. Entries containing more than one extension separated
	 *            by '|' will appear as a single entry in the dropdown
	 */
	public DialogComponentListFilesSelector(SettingsModelStringArrayFlowVarReplacable stringModel,
			final String title, final boolean hasBorder, final String label,
			final String fileHistoryID, final int dialogType, final FlowVariableModel fvm,
			final String... validExtensions) {
		this(stringModel, title, hasBorder, label, fileHistoryID, 350, 12, dialogType, fvm,
				validExtensions);
	}

	/**
	 * The main constructor with all the options available
	 * 
	 * @param stringModel
	 *            The SettingsModel for the component - this must be a
	 *            {@link SettingsModelStringArrayFlowVarReplacable}
	 * @param title
	 *            The title, to be placed in the component border. Ignored if
	 *            hasBorder is {@code false}. If a border without title is
	 *            required, this should be set to {@code null}.
	 * @param hasBorder
	 *            {@code true} if the component is to be surrounded by a border.
	 *            See also {@code title} argument.
	 * @param label
	 *            This text is an optional descriptive label provided above the
	 *            file list
	 * @param fileHistoryID
	 *            The name of a file history to store previous files from the
	 *            node type.
	 * @param selectedFileListWidth
	 *            The width of the selected file width box
	 * @param selectedFileListVisibleRows
	 *            The number of visisble rows in the selected file width box
	 * @param dialogType
	 *            The Dialog Type for the {@link JFileChooser} pop-up. This
	 *            should be one of {@link JFileChooser#DIRECTORIES_ONLY},
	 *            {@link JFileChooser#FILES_ONLY} or
	 *            {@link JFileChooser#FILES_AND_DIRECTORIES}.
	 * @param fvm
	 *            An optional {@link FlowVariableModel} for the SettingsModel.
	 *            If this is not {@code null}, then a button to add the files
	 *            from or two a flow variable is provided. The flow variable
	 *            needs to supply the full file paths as a string, with each
	 *            entry supplied by the deliminator used in the SettingsModel.
	 *            This is by default
	 *            {@link SettingsModelStringArrayFlowVarReplacable#DELIMINATOR}.
	 *            The value used can be obtained from
	 *            {@link SettingsModelStringArrayFlowVarReplacable#getDeliminator()}
	 * @param validExtensions
	 *            A list of valid file extensions. If {@code null}, then all
	 *            filetypes are accepted by the {@link JFileChooser} dialog.
	 *            Each separate array entry is added to a separate line in the
	 *            dropdown. Entries containing more than one extension separated
	 *            by '|' will appear as a single entry in the dropdown
	 */
	@SuppressWarnings("unchecked")
	public DialogComponentListFilesSelector(SettingsModelStringArrayFlowVarReplacable stringModel,
			final String title, final boolean hasBorder, final String label,
			final String fileHistoryID, final int selectedFileListWidth,
			final int selectedFileListVisibleRows, final int dialogType,
			final FlowVariableModel fvm, final String... validExtensions) {

		super(stringModel);
		getComponentPanel().setLayout(new FlowLayout());

		// The panel which contains this component
		final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		/*
		 * Sort the border
		 */
		if (hasBorder) {
			if (title != null) {
				m_border = BorderFactory.createTitledBorder(title);
			} else {
				m_border = BorderFactory.createEtchedBorder();
			}
		} else {
			m_border = BorderFactory.createEmptyBorder();
		}
		panel.setBorder(m_border);

		GridBagConstraints gbc = new GridBagConstraints();
		int col = 0;
		int row = 0;
		// Add a small amount of space around each
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = row++;
		gbc.gridx = col;

		// First few components occupy all 7 columns - we use remainder in case
		// we add more columns later
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		/*
		 * And the label text
		 */
		if (label == null) {
			m_label = null;
		} else {
			m_label = new JLabel(label);
			panel.add(m_label, gbc);
		}

		// Add a label for the selected files list
		gbc.gridy = row++;
		JLabel selectedFilesLabel = new JLabel("Selected files:");
		panel.add(selectedFilesLabel, gbc);

		/*
		 * Deal with the list box to contain selected files
		 */

		m_fileListModel = new DefaultListModel<>();
		m_fileList = new JList<>(m_fileListModel);
		m_fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		m_fileList.setLayoutOrientation(JList.VERTICAL);
		m_fileList.setVisibleRowCount(selectedFileListVisibleRows);
		m_fileList.setToolTipText("Selected files");

		// The list goes in a scrollable panel
		JScrollPane fileListScroller = new JScrollPane(m_fileList);
		fileListScroller.setPreferredSize(new Dimension(selectedFileListWidth,
				m_fileList.getPreferredScrollableViewportSize().height));

		m_fileList.addListSelectionListener(new ListSelectionListener() {
			// Add a listener to disable the 'Remove' buttons if nothing is
			// selected
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					m_deleteButton.setEnabled(
							getModel().isEnabled() && m_fileList.getSelectedIndices().length != 0);
					m_deleteAllButton
							.setEnabled(getModel().isEnabled() && m_fileListModel.size() != 0);
				}
			}
		});

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = row++;
		panel.add(fileListScroller, gbc);

		// A row of buttons
		m_addButton = new JButton("Browse...");
		m_addButton.setToolTipText("Browse file system to select files. \n"
				+ "Browser opens in location of selected file");
		gbc.gridy = row++;
		col = 0;
		gbc.gridx = col;
		gbc.gridwidth = 2;
		col += gbc.gridwidth;
		// gbc.weightx = fvm == null ? 0.33 : 0.3;
		gbc.weightx = 1.0;
		panel.add(m_addButton, gbc);

		// Add the Remove button
		m_deleteButton = new JButton("Remove");
		m_deleteButton.setToolTipText("Remove selected file(s) from list");
		gbc.gridx = col;
		col += gbc.gridwidth;
		panel.add(m_deleteButton, gbc);

		// And The Remove All button
		m_deleteAllButton = new JButton("Remove All");
		m_deleteAllButton.setToolTipText("Remove all files from list");
		gbc.gridx = col;
		col += gbc.gridwidth;
		panel.add(m_deleteAllButton, gbc);

		/*
		 * Deal with FlowVariableButton
		 */
		if (fvm != null) {
			fvm.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent arg0) {
					getModel().setEnabled(!fvm.isVariableReplacementEnabled());
				}
			});
			m_fvmButton = new FlowVariableModelButton(fvm);
			gbc.gridx = col++;
			gbc.weightx = 0.0;
			gbc.gridwidth = 1;
			m_fvmButton.setToolTipText("Use flow variable for file list");
			panel.add(m_fvmButton, gbc);
		} else {
			m_fvmButton = null;
		}

		// Initialise the history dropdown
		JPanel historyPanel = new JPanel();
		historyPanel.setLayout(new FlowLayout());
		historyPanel.setBorder(BorderFactory.createTitledBorder("File History"));
		historyPanel.add(new JLabel("Select file:"));

		m_fileHistory = StringHistory.getInstance(fileHistoryID);
		m_fileHistoryComboBox = new JComboBox<>();
		m_fileHistoryComboBox.setPreferredSize(
				new Dimension(300, m_fileHistoryComboBox.getPreferredSize().height));
		m_fileHistoryComboBox.setRenderer(new ConvenientComboBoxRenderer());
		m_fileHistoryComboBox.setEditable(false);
		for (final String fName : m_fileHistory.getHistory()) {
			m_fileHistoryComboBox.addItem(fName);
		}
		m_fileHistoryComboBox.setToolTipText("Select previously used file from dropdown");

		historyPanel.add(m_fileHistoryComboBox);

		// The add from History button
		m_addFromHistoryButton = new JButton("Add from history");
		m_addButton.setToolTipText("Add selected file from history to selected files panel");
		historyPanel.add(m_addFromHistoryButton);

		// Now add the history panel on a new row, taking full width
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridy = row++;
		panel.add(historyPanel, gbc);

		// Add it all to the dialog
		getComponentPanel().add(panel);

		// Deal with the valid extensions if they exist
		if (validExtensions != null) {
			m_fileFilter = new ArrayList<>(validExtensions.length);
			for (final String ext : validExtensions) {
				if (ext.indexOf("|") > 0) {
					m_fileFilter.add(new SimpleFileFilter(ext.split("\\|")));
				} else {
					m_fileFilter.add(new SimpleFileFilter(ext));
				}
			}
		} else {
			m_fileFilter = new ArrayList<>(0);
		}

		// Add the Button push events
		m_addFromHistoryButton.addActionListener(new ActionListener() {
			// Just add the file selected in the dropdown to the list
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (m_addFromHistoryButton.isEnabled()) {
					addFileToList((String) m_fileHistoryComboBox.getSelectedItem());
				}
			}
		});

		m_addButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// If there is a file selected in the list, then we start there
				String selectedFile = m_fileList.getSelectedValue();
				if (selectedFile == null && m_fileHistoryComboBox.getSelectedIndex() > -1) {
					// If not, and there is something in the dropdown, then we
					// start there instead
					selectedFile = (String) m_fileHistoryComboBox.getSelectedItem();
				}
				final JFileChooser fChooser = new JFileChooser(selectedFile);
				fChooser.setDialogType(dialogType);
				if (m_fileFilter != null && m_fileFilter.size() > 0) {
					fChooser.setAcceptAllFileFilterUsed(false);
					for (final FileFilter filter : m_fileFilter) {
						fChooser.setFileFilter(filter);
					}
					// Set the first filter as the default
					fChooser.setFileFilter(m_fileFilter.get(0));
				}
				fChooser.setMultiSelectionEnabled(true);

				final int returnVal = fChooser.showDialog(getComponentPanel().getParent(), null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// Need to add all selected files
					ArrayList<File> filesToAdd = new ArrayList<>();
					for (File f : fChooser.getSelectedFiles()) {
						String pathToF = f.getAbsoluteFile().toString();
						if (m_fileFilter != null) {
							// Check that the user added the extension
							boolean hasExtension = false;
							for (SimpleFileFilter filter : m_fileFilter) {
								String[] extensions = filter.getValidExtensions();
								for (String ext : extensions) {
									if (pathToF.endsWith(ext)) {
										hasExtension = true;
										break;
									}
								}
								if (hasExtension) {
									break;
								}
							}
							if (!hasExtension) {
								FileFilter fFilter = fChooser.getFileFilter();
								if (fFilter != null && fFilter instanceof SimpleFileFilter) {
									pathToF += ((SimpleFileFilter) fFilter).getValidExtensions()[0];
								}
							}
						}
						filesToAdd.add(new File(pathToF));
					}
					addFilesToList(filesToAdd);
				}
			}
		});

		m_deleteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteSelectedFiles();

			}
		});

		m_deleteAllButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteAllFiles();

			}
		});

		// Add Listener to ensure that Add from history is only enabled when
		// something is selected
		m_fileHistoryComboBox.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				updateAddFromHistoryButtonStatus();
			}

			@Override
			public void focusGained(FocusEvent e) {
				updateAddFromHistoryButtonStatus();
			}
		});
		m_fileHistoryComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateAddFromHistoryButtonStatus();
			}
		});
		m_fileHistoryComboBox.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				updateAddFromHistoryButtonStatus();
			}
		});

		getModel().addChangeListener(new ChangeListener() {
			// TODO: This should use the #prependChangeListener() method, but
			// that is protected not public in SettingsModel
			@Override
			public void stateChanged(ChangeEvent e) {
				updateComponent();

			}
		});
	}

	protected void deleteAllFiles() {
		m_fileListModel.removeAllElements();
		try {
			updateModel(true);
		} catch (InvalidSettingsException e) {
			// Ignore it
		}
		getComponentPanel().revalidate();
	}

	protected void deleteSelectedFiles() {
		for (String fName : m_fileList.getSelectedValuesList()) {
			while (m_fileListModel.removeElement(fName)) {
				// Make sure every copy is gone - just in case we managed to add
				// duplicates
				;
			}
		}
		try {
			updateModel(true);
		} catch (InvalidSettingsException e) {
			// Ignore it
		}
		getComponentPanel().revalidate();
	}

	/**
	 * @param filePath
	 */
	protected void addFileToList(String filePath) {
		// Needs to add to the file history as well as the selection
		m_fileHistoryComboBox.removeItem(filePath);
		m_fileHistoryComboBox.addItem(filePath);
		m_fileHistoryComboBox.setSelectedItem(filePath);
		m_fileListModel.removeElement(filePath);
		m_fileListModel.addElement(filePath);
		// Select the new entry
		m_fileList.setSelectedIndex(m_fileListModel.getSize() - 1);
		try {
			updateModel(true);
		} catch (InvalidSettingsException e) {
			// ignore it
		}
		getComponentPanel().revalidate();
	}

	protected void addFilesToList(List<File> files) {
		int[] newSel = new int[files.size()];
		int i = 0;
		for (File f : files) {
			// Needs to add to the file history as well as the selection
			m_fileHistoryComboBox.removeItem(f.getAbsolutePath());
			m_fileHistoryComboBox.addItem(f.getAbsolutePath());
			m_fileListModel.addElement(f.getAbsolutePath());
			newSel[i++] = m_fileListModel.getSize() - 1;
		}
		m_fileList.setSelectedIndices(newSel);
		m_fileHistoryComboBox.setSelectedIndex(m_fileHistoryComboBox.getItemCount() - 1);
		try {
			updateModel(true);
		} catch (InvalidSettingsException e) {
			// Ignore it
		}
		getComponentPanel().revalidate();
	}

	protected void addFilesToList(String[] files) {
		if (files != null) {
			int[] newSel = new int[files.length];
			int i = 0;
			for (String f : files) {
				// Needs to add to the file history as well as the selection
				m_fileHistoryComboBox.removeItem(f);
				m_fileHistoryComboBox.addItem(f);
				m_fileListModel.addElement(f);
				newSel[i++] = m_fileListModel.getSize() - 1;
			}
			m_fileList.setSelectedIndices(newSel);
			m_fileHistoryComboBox.setSelectedIndex(m_fileHistoryComboBox.getItemCount() - 1);
			try {
				updateModel(true);
			} catch (InvalidSettingsException e) {
				// Ignore it
			}
			getComponentPanel().revalidate();
		}
	}

	/**
	 * @throws InvalidSettingsException
	 * 
	 */
	protected void updateModel(boolean noColouring) throws InvalidSettingsException {
		String[] files = new String[m_fileListModel.toArray().length];
		for (int i = 0; i < files.length; i++) {
			files[i] = (String) m_fileListModel.toArray()[i];
		}
		if (files != null && files.length > 0) {
			try {
				((SettingsModelStringArrayFlowVarReplacable) getModel()).setStringArrayValue(files);
			} catch (RuntimeException e) {
				if (!noColouring) {
					showError(m_fileList);
				}
				throw new InvalidSettingsException(e);
			}
		} else {
			if (!noColouring) {
				showError(m_fileList);
			}
			throw new InvalidSettingsException("At least one file needs to be specified");
		}
	}

	// /**
	// * @param files
	// * @return
	// */
	// private String arrayToString(String[] files) {
	// if (files == null) {
	// return null;
	// }
	// if (files.length == 0) {
	// return "";
	// }
	// // We have at least one value...
	// StringBuilder retVal = new StringBuilder(files[0]);
	// for (int i = 1; i < files.length; i++) {
	// retVal.append(DELIMINATOR).append(files[i]);
	// }
	// return retVal.toString();
	// }

	/**
	 * @param jList
	 */
	private void showError(final JList<String> jList) {
		if (!getModel().isEnabled()) {
			// Disabled - no error
			return;
		}
		if (jList.getModel().getSize() == 0) {
			jList.setBackground(Color.RED);
		} else {
			jList.setForeground(Color.RED);
		}

		// Clear the color as soon as anything happens to the list
		jList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				jList.setForeground(DEFAULT_FG);
				jList.setBackground(DEFAULT_BG);
				jList.removeListSelectionListener(this);
			}
		});
	}

	protected void clearError(final JList<String> jList) {
		jList.setForeground(DEFAULT_FG);
		jList.setBackground(DEFAULT_BG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#updateComponent()
	 */
	@Override
	protected void updateComponent() {
		clearError(m_fileList);

		// update only if model and component are out of sync
		final SettingsModelStringArrayFlowVarReplacable model =
				(SettingsModelStringArrayFlowVarReplacable) getModel();
		String[] modelValue = model.getStringArrayValue();
		Object[] componentValue = m_fileListModel.toArray();
		boolean update;
		if (modelValue == null) {
			update = componentValue != null;
		} else if (modelValue.length != componentValue.length) {
			update = true;
		} else {
			// Same length - only update if all the items in the component are
			// not in the model
			for (Object fName : componentValue) {
				if (!m_fileListModel.contains(fName)) {
					update = true;
					break;
				}
			}
			update = false;
		}
		if (update) {
			// Remove everything first
			m_fileListModel.removeAllElements();
			if (modelValue != null) {
				addFilesToList(modelValue);
			}
			m_fileList.clearSelection();
		}
		setEnabledComponents(model.isEnabled());
	}

	// /**
	// * @param stringValue
	// * @return
	// */
	// private String[] stringToArray(String stringValue) {
	// if (stringValue == null) {
	// return null;
	// }
	// if ("".equals(stringValue)) {
	// return new String[0];
	// }
	// String[] retVal;
	// if (stringValue.indexOf(DELIMINATOR) >= 0) {
	// retVal = stringValue.split(DELIMINATOR);
	// for (int i = 0; i < retVal.length; i++) {
	// retVal[i] = retVal[i].trim();
	// }
	// } else {
	// // Only a single value
	// retVal = new String[1];
	// retVal[0] = stringValue.trim();
	// }
	// return retVal;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * validateSettingsBeforeSave()
	 */
	@Override
	protected void validateSettingsBeforeSave() throws InvalidSettingsException {
		updateModel(false); // Now we mark erroneous components
		// And add the saved filenames to the history
		for (String fname : ((SettingsModelStringArrayFlowVarReplacable) getModel())
				.getStringArrayValue()) {
			m_fileHistory.add(fname);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * checkConfigurabilityBeforeLoad(org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Incoming spec doesnt affect this

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DialogComponent#
	 * setEnabledComponents (boolean)
	 */
	@Override
	protected void setEnabledComponents(boolean enabled) {
		m_fileHistoryComboBox.setEnabled(enabled);
		m_addButton.setEnabled(enabled);
		m_deleteAllButton.setEnabled(enabled);
		m_deleteButton.setEnabled(enabled);
		m_addFromHistoryButton.setEnabled(enabled);
		m_fileList.setEnabled(enabled);
		updateAddFromHistoryButtonStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.DialogComponent#setToolTipText
	 * (java.lang.String)
	 */
	@Override
	public void setToolTipText(String text) {
		m_label.setToolTipText(text);
		m_fileList.setToolTipText(text);

	}

	private void updateAddFromHistoryButtonStatus() {
		m_addFromHistoryButton.setEnabled(
				getModel().isEnabled() && m_fileHistoryComboBox.getSelectedIndex() != -1);
	}

}
