/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.io.listdirs2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.pathresolve.ResolverUtil;

/**
 * This is the model implementation of ListDirs.
 */
public class ListDirs2NodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(ListDirs2NodeModel.class);

	/**
	 * the settings key which is used to retrieve and store the settings (from the
	 * dialog or from a settings file) (package visibility to be usable from the
	 * dialog).
	 */

	static final String CFG_PATH = "Path_name";
	static final String CFG_SUB_DIRS = "Include_Subfolders";
	static final String CFG_INCL_CTG_PATH = "Include_Containing_Path";
	static final String CFG_FOLDER_NAME = "Include_Folder_Name";
	static final String CFG_IS_VIS = "Include_Visibility";
	static final String CFG_LAST_MOD = "Include_LastModified";

	private final SettingsModelString m_Path = new SettingsModelString(CFG_PATH, null);

	private final SettingsModelBoolean m_subDirs = new SettingsModelBoolean(CFG_SUB_DIRS, false);

	private final SettingsModelBoolean m_ctgDirPath = new SettingsModelBoolean(CFG_INCL_CTG_PATH, true);

	private final SettingsModelBoolean m_folderName = new SettingsModelBoolean(CFG_FOLDER_NAME, true);

	private final SettingsModelBoolean m_isVisible = new SettingsModelBoolean(CFG_IS_VIS, true);

	private final SettingsModelBoolean m_lastModified = new SettingsModelBoolean(CFG_LAST_MOD, true);

	private BufferedDataContainer m_dc;

	private DataTableSpec spec;

	private long m_analysed_files;

	private long m_currentRowID;

	/**
	 * Constructor for the node model.
	 */
	protected ListDirs2NodeModel() {

		super(0, 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		// List the folders from the dialogue
		final String[] folders = m_Path.getStringValue().split(";");

		// Now create a data container for the new output table

		m_dc = exec.createDataContainer(spec);

		m_currentRowID = 0;
		m_analysed_files = 0;

		for (String folder : folders) {
			folder = folder.trim();
			File location = new File(folder);
			if (!location.isDirectory()) {
				// Handle URLs instead of paths
				try {
					if (folder.startsWith("file:")) {
						folder = folder.substring(5);
						location = new File(new URI(folder).getPath());
					} else if (folder.startsWith("knime:")) {
						final URI uri = new URI(folder);
						location = ResolverUtil.resolveURItoLocalFile(uri);
					}
				} catch (final Exception e) {
					throw new InvalidSettingsException("\"" + folder + "\" does not exist or is not a directory");
				}
				// Fixes possible NPE - see
				// https://forum.knime.com/t/id-like-to-download-files-from-s3-to-knime-server/23289/5?u=s.roughley
				if (location == null) {
					throw new InvalidSettingsException("Unable to resolve path \"" + folder + "\" to a directory");
				}
				if (!location.isDirectory()) {
					throw new InvalidSettingsException("\"" + folder + "\" does not exist or is not a directory");
				}
			}
			addLocation(location, exec);

		}
		m_dc.close();

		return new BufferedDataTable[] { m_dc.getTable() };

	}

	private void addLocation(final File location, final ExecutionContext exec) throws CanceledExecutionException {

		// List the folders - recursively if we are doing subfolders too
		m_analysed_files++;
		exec.setProgress(m_analysed_files + " file(s) and folder(s) analysed..." + m_currentRowID + " added to output");
		exec.checkCanceled();

		if (location.isDirectory()) {
			final File[] listFiles = location.listFiles();
			if (listFiles != null) {
				for (final File loc : listFiles) {
					if (loc.isDirectory()) {
						// We need to add a directory whenever it is found
						try {
							final DataCell[] row = new DataCell[spec.getNumColumns()];
							Arrays.fill(row, DataType.getMissingCell());
							int colIndex = 0;
							row[colIndex++] = new StringCell(loc.getAbsolutePath());
							row[colIndex++] = new StringCell(loc.getAbsoluteFile().toURI().toURL().toString());

							if (m_folderName.getBooleanValue()) {
								row[colIndex++] = new StringCell(loc.getName());
							}

							if (m_ctgDirPath.getBooleanValue()) {
								final File parent = loc.getParentFile();
								if (parent != null) {
									row[colIndex++] = new StringCell(parent.getAbsolutePath());
									row[colIndex++] = new StringCell(
											parent.getAbsoluteFile().toURI().toURL().toString());
								}
							}

							if (m_isVisible.getBooleanValue()) {
								// NB we return opposite result of "isHidden" as
								// we are asking "isVisible"
								row[colIndex++] = loc.isHidden() ? BooleanCell.FALSE : BooleanCell.TRUE;
							}

							if (m_lastModified.getBooleanValue()) {
								row[colIndex++] = longTimeToDateAndTimeCell(loc.lastModified());
							}

							m_dc.addRowToTable(new DefaultRow("Row " + m_currentRowID, row));
							m_currentRowID++;

						} catch (final MalformedURLException e) {
							logger.error("Unable to create URL to folder", e);
						}

						// Deal with last-modified and isVisible

						// Now deal with subfolders
						if (m_subDirs.getBooleanValue()) {
							// Recursively call to add them
							addLocation(loc, exec);

						}
					}
				}
			}
		}
	}

	private DataCell longTimeToDateAndTimeCell(long lastModified) {

		return new DateAndTimeCell(lastModified, true, true, true);
	}

	/**
	 * Return the DataColumnSpec for the output table, taking into account the user
	 * settings
	 *
	 * @return {@link DataColumnSpec}[] of the output columns
	 */
	private DataColumnSpec[] createDataColumnSpec() {
		// Create the output table spec
		// Use an array list as saves counting columns via if statements and
		// then
		// Counting through them all again
		final ArrayList<DataColumnSpec> dcs = new ArrayList<>();

		dcs.add(new DataColumnSpecCreator("Location", StringCell.TYPE).createSpec());
		dcs.add(new DataColumnSpecCreator("URL", StringCell.TYPE).createSpec());

		if (m_folderName.getBooleanValue()) {
			dcs.add(new DataColumnSpecCreator("Folder Name", StringCell.TYPE).createSpec());
		}

		if (m_ctgDirPath.getBooleanValue()) {
			dcs.add(new DataColumnSpecCreator("Parent Location", StringCell.TYPE).createSpec());
			dcs.add(new DataColumnSpecCreator("Parent URL", StringCell.TYPE).createSpec());
		}

		if (m_isVisible.getBooleanValue()) {
			dcs.add(new DataColumnSpecCreator("Is Visible?", BooleanCell.TYPE).createSpec());
		}

		if (m_lastModified.getBooleanValue()) {
			dcs.add(new DataColumnSpecCreator("Last Modified Date", DateAndTimeCell.TYPE).createSpec());
		}

		return dcs.toArray(new DataColumnSpec[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

		// Check there is something in the path box
		if (m_Path == null) {
			throw new InvalidSettingsException("No folder selected");
		}
		spec = new DataTableSpec(createDataColumnSpec());
		return new DataTableSpec[] { spec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {

		m_Path.saveSettingsTo(settings);
		m_subDirs.saveSettingsTo(settings);
		m_folderName.saveSettingsTo(settings);
		m_ctgDirPath.saveSettingsTo(settings);
		m_isVisible.saveSettingsTo(settings);
		m_lastModified.saveSettingsTo(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_Path.loadSettingsFrom(settings);
		m_subDirs.loadSettingsFrom(settings);
		// Emulate old behaviour with new settings
		try {
			m_ctgDirPath.loadSettingsFrom(settings);
		} catch (final Exception e) {
			m_ctgDirPath.setBooleanValue(false);
		}
		try {
			m_folderName.loadSettingsFrom(settings);
		} catch (final Exception e) {
			m_folderName.setBooleanValue(false);
		}
		try {
			m_isVisible.loadSettingsFrom(settings);
		} catch (final Exception e) {
			m_isVisible.setBooleanValue(false);
		}
		try {
			m_lastModified.loadSettingsFrom(settings);
		} catch (final Exception e) {
			m_lastModified.setBooleanValue(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {

		m_Path.validateSettings(settings);
		m_subDirs.validateSettings(settings);
		// Newer settings are not validated to allow backwards compatibility
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

}
