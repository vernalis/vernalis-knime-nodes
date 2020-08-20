/*******************************************************************************
 * Copyright (c) 2015, 2018, Vernalis (R&D) Ltd
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.io.FileEncodingWithGuess;
import com.vernalis.io.FileHelpers;
import com.vernalis.knime.dialog.components.SettingsModelStringArrayFlowVarReplacable;

import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createFileEncodingModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createFilenamesModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createIncludeFilenameAsRowIDModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createIncludeFilenamesModel;
import static com.vernalis.knime.io.nodes.abstrct.AbstractLoadFilesNodeDialog.createIncludePathsModel;

/**
 * This is the model implementation of LoadTextFiles. Node to load one or more
 * text-based files into table cells (1 cell per file)
 * 
 * @author S. Roughley
 */
public abstract class AbstractLoadFilesNodeModel extends NodeModel {

	protected final SettingsModelStringArrayFlowVarReplacable m_files =
			createFilenamesModel();
	protected final SettingsModelBoolean m_rowIDs =
			createIncludeFilenameAsRowIDModel();
	protected final SettingsModelBoolean m_locCols = createIncludePathsModel();
	protected final SettingsModelString m_fileEncoding =
			createFileEncodingModel();
	protected final SettingsModelBoolean m_inclFilenames =
			createIncludeFilenamesModel();

	protected DataTableSpec outSpec;
	protected FileEncodingWithGuess fileEnc;
	protected final DataType m_DataType;
	protected String m_contentColumnName;

	/**
	 * Constructor for the node model.
	 */
	protected AbstractLoadFilesNodeModel(DataType dataType,
			String contentColumnName) {
		super(new PortType[] { FlowVariablePortObject.TYPE_OPTIONAL },
				new PortType[] { BufferedDataTable.TYPE });
		m_DataType = dataType;
		m_contentColumnName = contentColumnName;
		if (m_contentColumnName == null) {
			m_contentColumnName = "File contents";
		}
	}

	public AbstractLoadFilesNodeModel(DataType dataType) {
		this(dataType, "File contents");
	}

	public AbstractLoadFilesNodeModel() {
		this(StringCell.TYPE, "File contents");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final PortObject[] inData,
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
				HashMap<String, Integer> rowSuffixes = new HashMap<>();
				Integer rowSuffix = 0;
				String[] files = m_files.getStringArrayValue();
				for (String file : files) {
					// Now, if it is a Location, convert to a URL
					file = FileHelpers.forceURL(file);
					final File f = new File(file);
					String fName = f.getName();
					// Only try to load the files - do not check type! Encoding
					// and
					// un-zipping will be handled
					String r = FileHelpers.readURLToString(file, fileEnc);

					// Now the RowID
					String rId;
					if (m_rowIDs.getBooleanValue()) {
						rId = fName;
						rowSuffix = rowSuffixes.get(rId);
						if (rowSuffix == null) {
							rowSuffix = 0;
						} else {
							// Only add a suffix if duplication
							rId += "_" + rowSuffix;
						}
						rowSuffixes.put(rId, rowSuffix);
					} else {
						rId = "Row_" + (rowSuffix++);
					}

					// Now the data cells
					DataCell[] cells = new DataCell[outSpec.getNumColumns()];
					int colidx = 0;
					if (m_locCols.getBooleanValue()) {
						cells[colidx++] = new StringCell(file);
						cells[colidx++] =
								new StringCell(f.toURI().toURL().toString());
					}
					if (m_inclFilenames.getBooleanValue()) {
						cells[colidx++] = new StringCell(fName);
					}
					if (r != null) {
						cells[colidx++] = getDataCellFromString(r);
					} else {
						cells[colidx++] = DataType.getMissingCell();
					}
					out.push(new DefaultRow(new RowKey(rId), cells));
				}

				out.close();

			}
		};
	}

	/**
	 * This method needs to return a DataCell of the same type as
	 * {@link #m_DataType}. The superclass handles {@code null} values.
	 * 
	 * @param fileContentString
	 *            The contents of the file as a String
	 * @return A DataCell of the appropriate type
	 */
	protected abstract DataCell getDataCellFromString(String fileContentString);

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
	protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (m_files.getStringArrayValue() == null
				|| m_files.getStringArrayValue().length == 0) {
			getLogger().error("No files selected");
			throw new InvalidSettingsException("No files selected");
		}
		outSpec = createOutputSpec();
		fileEnc =
				FileEncodingWithGuess.valueOf(m_fileEncoding.getStringValue());
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
		colSpecFact =
				new DataColumnSpecCreator(m_contentColumnName, m_DataType);
		specCreator.addColumns(colSpecFact.createSpec());
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
		try {
			m_inclFilenames.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// Set false for back compatibility
			m_inclFilenames.setBooleanValue(false);
		}
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
		// Dont validate include filenames settings for back compatibility
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

}
