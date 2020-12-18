/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.nodes.totable;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.ports.PortsConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;

import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_RCSB_PDB_ADVANCED_QUERY;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_VERSION;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.VERSION;
import static com.vernalis.pdbconnector2.nodes.totable.QueriesToTableNodeDialog.createColumnNameModel;
import static com.vernalis.pdbconnector2.nodes.totable.QueriesToTableNodeDialog.createNoQueryBehaviourModel;
import static com.vernalis.pdbconnector2.nodes.totable.QueriesToTableNodeDialog.createTreatMissingPortAsEmptyQueryModel;

/**
 * The {@link NodeModel} implementation for the 'Queries to Table' node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueriesToTableNodeModel extends NodeModel
		implements SettingsModelRegistry {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColumnNameModel());
	private final SettingsModelString noQueryBehaviourMdl =
			registerSettingsModel(createNoQueryBehaviourModel());
	private final SettingsModelBoolean treatMissingAsEmptyMdl =
			registerSettingsModel(createTreatMissingPortAsEmptyQueryModel());

	/**
	 * Default constructor, with 1 incoming port
	 */
	protected QueriesToTableNodeModel() {
		this(1);
	}

	/**
	 * Constructor specifying the number of incoming ports
	 * 
	 * @param numInports
	 *            The number incoming query ports
	 */
	public QueriesToTableNodeModel(int numInports) {
		super(createInports(numInports),
				new PortType[] { BufferedDataTable.TYPE });
	}

	/**
	 * Constructor from a {@link PortsConfiguration} object
	 * 
	 * @param portConfig
	 *            The {@link PortsConfiguration}
	 */
	public QueriesToTableNodeModel(PortsConfiguration portConfig) {
		super(portConfig.getInputPorts(), portConfig.getOutputPorts());
	}

	private static PortType[] createInports(int numInports) {
		if (numInports < 1) {
			throw new IllegalArgumentException(
					"Node must have 1 or more ports!");
		}
		final PortType[] retVal =
				ArrayUtils.of(RCSBQueryPortObject.TYPE_OPTIONAL, numInports);
		retVal[0] = RCSBQueryPortObject.TYPE;
		return retVal;
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		try {
			NoQueryBehaviour.valueOf(noQueryBehaviourMdl.getStringValue());
		} catch (IllegalArgumentException e) {
			throw new InvalidSettingsException(
					"No query behaviour type not recognised", e);
		}
		return new PortObjectSpec[] { createOutputSpec() };
	}

	private DataTableSpec createOutputSpec() throws InvalidSettingsException {
		if (colNameMdl.getStringValue() == null
				|| colNameMdl.getStringValue().isEmpty()) {
			throw new InvalidSettingsException("No column name provided");
		}
		return new DataTableSpecCreator().addColumns(
				new DataColumnSpecCreator(colNameMdl.getStringValue(),
						XMLCellFactory.TYPE).createSpec())
				.createSpec();
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		BufferedDataContainer bdc =
				exec.createDataContainer(createOutputSpec());
		NoQueryBehaviour nqb =
				NoQueryBehaviour.valueOf(noQueryBehaviourMdl.getStringValue());
		long rowIdx = 0L;

		for (PortObject inport : inObjects) {
			MultiRCSBQueryModel model;
			if (inport == null) {
				// No connection
				if (treatMissingAsEmptyMdl.getBooleanValue()) {
					model = new MultiRCSBQueryModel();
				} else {
					continue;
				}
			} else {
				model = ((RCSBQueryPortObject) inport).getModel();
			}

			// Check the model
			if (!model.hasQuery())
				switch (nqb) {
					case Fail:
						throw new RuntimeException("Empty model at inport "
								+ Arrays.asList(inObjects).indexOf(inport));

					case Skip:
						continue;

					case Write_Empty_Query:
						break;

					case Write_Missing_Value:
						bdc.addRowToTable(new DefaultRow(
								RowKey.createRowKey(rowIdx++),
								new DataCell[] { DataType.getMissingCell() }));
						continue;
					default:
						assert false : "Unknown missing query behaviour";
				}

			// Store the query in a NodeSettings object
			NodeSettings settings =
					new NodeSettings(CFGKEY_RCSB_PDB_ADVANCED_QUERY);
			settings.addInt(CFGKEY_VERSION, VERSION);
			model.saveSettingsTo(settings);

			try (PipedInputStream pis = new PipedInputStream();
					PipedOutputStream pos = new PipedOutputStream(pis)) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							settings.saveToXML(pos);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}

					}
				}).start();

				bdc.addRowToTable(new DefaultRow(RowKey.createRowKey(rowIdx++),
						new DataCell[] { XMLCellFactory.create(pis) }));
			}

			exec.checkCanceled();
			exec.setProgress(1.0 * rowIdx / inObjects.length,
					"Converted " + rowIdx + " of " + inObjects.length);
		}
		bdc.close();
		return new PortObject[] { bdc.getTable() };
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
