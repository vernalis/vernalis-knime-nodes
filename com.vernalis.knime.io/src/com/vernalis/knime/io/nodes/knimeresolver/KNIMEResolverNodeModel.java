/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.knime.io.nodes.knimeresolver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.uri.URIDataValue;
import org.knime.core.data.uri.UriCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.VariableType.StringType;

import com.vernalis.io.FileHelpers;
import com.vernalis.knime.nodes.SettingsModelRegistry;

import static com.vernalis.knime.io.nodes.knimeresolver.KNIMEResolverNodeDialog.URI_COLUMN_FILTER;
import static com.vernalis.knime.io.nodes.knimeresolver.KNIMEResolverNodeDialog.createCanonicalizeOutputModel;
import static com.vernalis.knime.io.nodes.knimeresolver.KNIMEResolverNodeDialog.createColNameModel;
import static com.vernalis.knime.io.nodes.knimeresolver.KNIMEResolverNodeDialog.createReplaceInputModel;
import static com.vernalis.knime.io.nodes.knimeresolver.KNIMEResolverNodeDialog.createReturnURIModel;
import static com.vernalis.knime.io.nodes.knimeresolver.KNIMEResolverNodeDialog.createVarNameModel;

/**
 * Node Model implementation for the KNIME URI resolver nodes
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.0
 *
 */
public class KNIMEResolverNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelBoolean replaceInputMdl =
			registerSettingsModel(createReplaceInputModel());
	private final SettingsModelBoolean canonicalizeOutputMdl =
			registerSettingsModel(createCanonicalizeOutputModel());
	private final SettingsModelString colNameMdl;
	private final SettingsModelString flowVarNameMdl;
	private final SettingsModelBoolean returnURIMdl;

	private final boolean flowVar;

	/**
	 * NodeModel Constructor
	 * 
	 * @param flowVar
	 *            If {@code true} the node is a flow variable node rather than a
	 *            Table manipulator
	 */
	protected KNIMEResolverNodeModel(boolean flowVar) {
		super(createPortTypes(flowVar, true), createPortTypes(flowVar, false));
		this.flowVar = flowVar;
		if (flowVar) {
			flowVarNameMdl = registerSettingsModel(createVarNameModel());
			returnURIMdl = registerSettingsModel(createReturnURIModel());
			colNameMdl = null;
		} else {
			colNameMdl = registerSettingsModel(createColNameModel());
			returnURIMdl = null;
			flowVarNameMdl = null;
		}
	}

	private static PortType[] createPortTypes(boolean flowVar,
			boolean isInput) {
		return new PortType[] { flowVar
				? isInput ? FlowVariablePortObject.TYPE_OPTIONAL
						: FlowVariablePortObject.TYPE
				: BufferedDataTable.TYPE };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		if (flowVar) {
			final Map<String, FlowVariable> availableFlowVariables =
					getAvailableFlowVariables(StringType.INSTANCE);
			if (!availableFlowVariables
					.containsKey(flowVarNameMdl.getStringValue())) {
				throw new InvalidSettingsException("Selected flow variable '"
						+ flowVarNameMdl.getStringValue() + "' not available");
			}
			if (!replaceInputMdl.getBooleanValue()) {
				pushFlowVariable(flowVarNameMdl.getStringValue() + "_resolved",
						StringType.INSTANCE, "");
			}
			return new PortObjectSpec[] { FlowVariablePortObjectSpec.INSTANCE };

		} else {
			return new PortObjectSpec[] {
					createColumnRearranger((DataTableSpec) inSpecs[0])
							.createSpec() };
		}
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		if (flowVar) {
			String outName = replaceInputMdl.getBooleanValue()
					? flowVarNameMdl.getStringValue()
					: (flowVarNameMdl.getStringValue() + "_resolved");
			try {
				pushFlowVariable(outName, StringType.INSTANCE, resolveURI(
						peekFlowVariable(flowVarNameMdl.getStringValue(),
								StringType.INSTANCE),
						returnURIMdl.getBooleanValue(),
						canonicalizeOutputMdl.getBooleanValue()));
			} catch (Exception e) {
				throw new RuntimeException("Unable to resolve mountpoint '"
						+ peekFlowVariable(flowVarNameMdl.getStringValue(),
								StringType.INSTANCE)
						+ "'" + (e.getMessage() == null ? ""
								: (" - " + e.getMessage())),
						e);
			}
			return new PortObject[] { FlowVariablePortObject.INSTANCE };
		} else {
			BufferedDataTable inTable = (BufferedDataTable) inObjects[0];
			return new PortObject[] { exec.createColumnRearrangeTable(inTable,
					createColumnRearranger(inTable.getDataTableSpec()), exec) };
		}
	}

	private ColumnRearranger createColumnRearranger(DataTableSpec dataTableSpec)
			throws InvalidSettingsException {
		int colIdx = getValidatedColumnSelectionModelColumnIndex(colNameMdl,
				URI_COLUMN_FILTER, dataTableSpec, getLogger());
		ColumnRearranger rearranger = new ColumnRearranger(dataTableSpec);
		final DataColumnSpec inColSpec = dataTableSpec.getColumnSpec(colIdx);
		if (replaceInputMdl.getBooleanValue()) {
			rearranger.replace(getCellFactory(inColSpec, colIdx), colIdx);
		} else {
			rearranger
					.append(getCellFactory(
							new DataColumnSpecCreator(
									DataTableSpec.getUniqueColumnName(
											dataTableSpec,
											inColSpec.getName()
													+ " (Resolved)"),
									inColSpec.getType()).createSpec(),
							colIdx));
		}
		return rearranger;
	}

	private SingleCellFactory getCellFactory(DataColumnSpec colSpec,
			int colIdx) {
		return new SingleCellFactory(colSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell inCell = row.getCell(colIdx);
				if (inCell.isMissing()) {
					return DataType.getMissingCell();
				}
				String uri = ((StringValue) inCell).getStringValue();
				try {
					boolean isURI = inCell instanceof URIDataValue;
					String resolved = resolveURI(uri, isURI,
							canonicalizeOutputMdl.getBooleanValue());
					return isURI ? UriCellFactory.create(resolved)
							: new StringCell(resolved);
				} catch (Exception e) {
					setWarningMessage("Unable to resolve all URIs");
					return new MissingCell("Unable to resolve URI '" + uri + "'"
							+ (e.getMessage() == null ? ""
									: (" - " + e.getMessage())));
				}

			}
		};
	}

	/**
	 * @param uri
	 *            The URI to resolve
	 * @param isURI
	 *            Should the uri be resolved to a URI ({@code true}) or a local
	 *            path?
	 * @param canonicalize
	 *            Should the path be canonicalised (e.g. redundantant '.' and
	 *            '..' stripped)
	 * @return The resolved path
	 * @throws IOException
	 *             If the URI cannot be resolved or canincalised
	 * @throws URISyntaxException
	 *             If a file:// URI is supplied and is malformed
	 * @throws IllegalArgumentException
	 *             If an ftp:, http: or https: protocol URI is supplied
	 */
	protected String resolveURI(String uri, boolean isURI, boolean canonicalize)
			throws IOException, URISyntaxException, IllegalArgumentException {
		File f = FileHelpers.resolveKnimeProtocol(uri);
		if (canonicalize) {
			f = f.getCanonicalFile();
		}
		String resolved = isURI ? f.toURI().toString() : f.getAbsolutePath();
		return resolved;
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		SettingsModelRegistry.super.saveSettingsTo(settings);

	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);
	}

	@Override
	public void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);

	}

	@Override
	protected void reset() {

	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

}
