/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.io.nodes.abstrct;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.io.FileEncodingWithGuess;
import com.vernalis.io.FileHelpers;
import com.vernalis.io.FileHelpers.LineBreak;
import com.vernalis.io.MultilineTextObject;
import com.vernalis.io.MultilineTextObjectReader;
import com.vernalis.knime.dialog.components.SettingsModelStringArrayFlowVarReplacable;

import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createFileEncodingModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createFilenamesModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createIncludeFilenameAsRowIDModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createIncludeFilenamesModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createIncludePathsModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractMultiLineObjectLoadFilesNodeDialog.createNewlineModel;

/**
 * This is the model implementation of the node for loading
 * {@link MultilineTextObject}s (one per table row). Subclasses need to
 * implement {@link #getObjectReader(BufferedReader)}
 * 
 * @author S. Roughley
 */
public abstract class AbstractMultiLineObjectLoadFilesNodeModel<T extends MultilineTextObject>
		extends NodeModel {

	private static final String NO_FILES_SELECTED = "No files selected";
	private static final String UNRECOGNISED_LINEBREAK_OPTION =
			"Unrecognised linebreak option";
	private static final String FILE_LOCATION_LOST_WARNING =
			"File location information will be lost in output table!";
	protected final SettingsModelStringArrayFlowVarReplacable m_files =
			createFilenamesModel();
	protected final SettingsModelBoolean m_rowIDs =
			createIncludeFilenameAsRowIDModel();
	protected final SettingsModelBoolean m_locCols = createIncludePathsModel();
	protected final SettingsModelString m_fileEncoding =
			createFileEncodingModel();
	protected final SettingsModelBoolean m_inclFilenames =
			createIncludeFilenamesModel();
	protected final SettingsModelString m_lineBreaksModel =
			createNewlineModel();

	protected final T nonReadableObject;
	protected DataTableSpec outSpec;
	protected FileEncodingWithGuess fileEnc;

	/**
	 * Constructor for the node model.
	 * 
	 * @param nonReadableObject
	 *            - An instance of the {@link MultilineTextObject} for
	 *            generating output spec during configure
	 * 
	 * 
	 */
	protected AbstractMultiLineObjectLoadFilesNodeModel(T nonReadableObject) {
		super(0, 1);
		this.nonReadableObject = nonReadableObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		BufferedDataTableRowOutput output = new BufferedDataTableRowOutput(
				exec.createDataContainer(outSpec));
		createStreamableOperator(null, null).runFinal(new PortInput[0],
				new PortOutput[] { output }, exec);
		return new BufferedDataTable[] { output.getDataTable() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(
			PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {
				RowOutput out = (RowOutput) outputs[0];
				Map<String, AtomicLong> rowSuffixes =
						Collections.synchronizedMap(new HashMap<>());
				String[] files = m_files.getStringArrayValue();
				LineBreak lineBreak =
						LineBreak.valueOf(m_lineBreaksModel.getStringValue());
				for (String file : files) {
					// Now, if it is a Location, convert to a URL
					file = FileHelpers.forceURL(file);
					File f = new File(file);
					try {
						BufferedReader br =
								FileHelpers.getReaderFromUrl(file, fileEnc);
						String newLineStr =
								lineBreak == LineBreak.PRESERVE_INCOMING
										? FileHelpers.getLineBreakFromReader(br)
												.getNewlineString()
										: lineBreak.getNewlineString();
						MultilineTextObjectReader<T> objReader =
								getObjectReader(br);
						T nextObj;
						while ((nextObj = objReader.readNext()) != null) {
							RowKey rK = generateRowKey(rowSuffixes, f);

							DataCell[] cells;
							int colidx = 0;
							if (m_locCols.getBooleanValue()
									|| m_inclFilenames.getBooleanValue()) {
								cells = new DataCell[outSpec.getNumColumns()];
								if (m_locCols.getBooleanValue()) {
									cells[colidx++] = new StringCell(file);
									cells[colidx++] = new StringCell(
											f.toURI().toURL().toString());
								}
								if (m_inclFilenames.getBooleanValue()) {
									cells[colidx++] =
											new StringCell(f.getName());
								}
								for (DataCell objCell : nextObj
										.getNewCells(newLineStr)) {
									cells[colidx++] = objCell;
								}
							} else {
								cells = nextObj.getNewCells(newLineStr);
							}
							out.push(new DefaultRow(rK, cells));
						}
					} catch (Exception e) {
						getLogger().warn("Problem reading file '" + file + "': "
								+ e.getMessage());
						DataCell[] cells =
								new DataCell[outSpec.getNumColumns()];
						Arrays.fill(cells, DataType.getMissingCell());
						int colidx = 0;
						if (m_locCols.getBooleanValue()) {
							cells[colidx++] = new StringCell(file);
							cells[colidx++] = new StringCell(
									new File(file).toURI().toURL().toString());
						}
						if (m_inclFilenames.getBooleanValue()) {
							cells[colidx++] = new StringCell(f.getName());
						}
						RowKey rK = generateRowKey(rowSuffixes, f);
						out.push(new DefaultRow(rK, cells));
					}
				}
				out.close();
			}
		};
	}

	protected abstract MultilineTextObjectReader<T> getObjectReader(
			BufferedReader br);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			LineBreak.valueOf(m_lineBreaksModel.getStringValue());
		} catch (IllegalArgumentException | NullPointerException e) {
			getLogger().error(UNRECOGNISED_LINEBREAK_OPTION);
			throw new InvalidSettingsException(UNRECOGNISED_LINEBREAK_OPTION,
					e);
		}
		if (m_files.getStringArrayValue() == null
				|| m_files.getStringArrayValue().length == 0) {
			getLogger().error(NO_FILES_SELECTED);
			throw new InvalidSettingsException(NO_FILES_SELECTED);
		}
		outSpec = createOutputSpec();
		fileEnc =
				FileEncodingWithGuess.valueOf(m_fileEncoding.getStringValue());
		if (!(m_inclFilenames.getBooleanValue() || m_rowIDs.getBooleanValue()
				|| m_locCols.getBooleanValue())) {
			getLogger().warn(FILE_LOCATION_LOST_WARNING);
			setWarningMessage(FILE_LOCATION_LOST_WARNING);
		}
		return new DataTableSpec[] { outSpec };
	}

	/**
	 * @return
	 */
	private DataTableSpec createOutputSpec() {
		DataTableSpecCreator specCreator = new DataTableSpecCreator();
		DataColumnSpecCreator colSpecFact;
		if (m_locCols.getBooleanValue()) {
			colSpecFact =
					new DataColumnSpecCreator("Location", StringCell.TYPE);
			specCreator.addColumns(colSpecFact.createSpec());
			colSpecFact = new DataColumnSpecCreator("URL", StringCell.TYPE);
			specCreator.addColumns(colSpecFact.createSpec());
		}
		if (m_inclFilenames.getBooleanValue()) {
			specCreator.addColumns(
					new DataColumnSpecCreator("Filename", StringCell.TYPE)
							.createSpec());
		}
		specCreator.addColumns(nonReadableObject.getNewColumnSpecs());
		return specCreator.createSpec();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_files.saveSettingsTo(settings);
		m_fileEncoding.saveSettingsTo(settings);
		m_rowIDs.saveSettingsTo(settings);
		m_locCols.saveSettingsTo(settings);
		m_inclFilenames.saveSettingsTo(settings);
		m_lineBreaksModel.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_files.loadSettingsFrom(settings);
		m_fileEncoding.loadSettingsFrom(settings);
		m_rowIDs.loadSettingsFrom(settings);
		m_locCols.loadSettingsFrom(settings);
		m_inclFilenames.loadSettingsFrom(settings);
		m_lineBreaksModel.loadSettingsFrom(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		m_files.validateSettings(settings);
		m_fileEncoding.validateSettings(settings);
		m_rowIDs.validateSettings(settings);
		m_locCols.validateSettings(settings);
		m_inclFilenames.validateSettings(settings);
		m_lineBreaksModel.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		//
	}

	/**
	 * @param rowSuffixes
	 * @param file
	 * @return
	 */
	private final synchronized RowKey generateRowKey(
			Map<String, AtomicLong> rowSuffixes, File f) {
		String rId = m_rowIDs.getBooleanValue() ? f.getName() : "Row";
		if (!rowSuffixes.containsKey(rId)) {
			rowSuffixes.put(rId, new AtomicLong());
		}
		rId += "_" + rowSuffixes.get(rId).getAndIncrement();
		RowKey rK = new RowKey(rId);
		return rK;
	}

}
