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
package com.vernalis.pdbconnector2.nodes.loopstart;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
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
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.workflow.LoopStartNodeTerminator;
import org.knime.core.util.Pair;

import com.vernalis.knime.nodes.SettingsModelRegistry;
import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.ports.RCSBQueryPortObject;

import static com.vernalis.pdbconnector2.nodes.loopstart.QueryLoopStartNodeDialog.XMLVALUE_FILTER;
import static com.vernalis.pdbconnector2.nodes.loopstart.QueryLoopStartNodeDialog.createColumnNameModel;
import static com.vernalis.pdbconnector2.nodes.loopstart.QueryLoopStartNodeDialog.createIgnoreInvalidXMLModel;
import static com.vernalis.pdbconnector2.nodes.loopstart.QueryLoopStartNodeDialog.createNoQueryBehaviourModel;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_RCSB_PDB_ADVANCED_QUERY;
import static com.vernalis.pdbconnector2.nodes.save.SaveQueryNodeModel.CFGKEY_VERSION;

/**
 * The {@link NodeModel} implementation for the 'Query Loop Start' node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryLoopStartNodeModel extends NodeModel
		implements SettingsModelRegistry, LoopStartNodeTerminator {

	private final Set<SettingsModel> models = new LinkedHashSet<>();
	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColumnNameModel());
	private final SettingsModelString noQueryBehaviourMdl =
			registerSettingsModel(createNoQueryBehaviourModel());
	private final SettingsModelBoolean ignoreInvalidXMLMdl =
			registerSettingsModel(createIgnoreInvalidXMLModel());

	private Pair<DataRow, MultiRCSBQueryModel> nextQuery = null;
	// loop invariants
	/** The input table once execution has commenced. */
	private BufferedDataTable table;
	private CloseableRowIterator iter;

	// loop variants
	/** The interation counter. */
	private int iteration;
	private NoQueryBehaviour nqb;
	private int colIdx;
	private DataTableSpec outTableSpec;

	/**
	 * Constructor
	 */
	protected QueryLoopStartNodeModel() {
		super(new PortType[] { BufferedDataTable.TYPE }, new PortType[] {
				RCSBQueryPortObject.TYPE, BufferedDataTable.TYPE });
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

		outTableSpec = (DataTableSpec) inSpecs[0];
		colIdx = getValidatedColumnSelectionModelColumnIndex(colNameMdl,
				XMLVALUE_FILTER, outTableSpec, getLogger());

		iteration = 0;
		nqb = NoQueryBehaviour.valueOf(noQueryBehaviourMdl.getStringValue());

		pushFlowVariableInt("currentIteration", 0);

		// We dont know the query spec at this point for the output
		return new PortObjectSpec[] { null, outTableSpec };
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		BufferedDataTable inTable = (BufferedDataTable) inObjects[0];

		if (iteration == 0) {
			// First iteration - setup
			assert getLoopEndNode() == null : "1st iteration but end node set";
			table = inTable;
			iter = table.iterator();
			nextQuery = findNextValidQuery();
			if (nextQuery == null) {
				throw new RuntimeException(
						"No valid queries found in input table");
			}
		} else {
			// Just some assertions for second iteration and beyond
			assert getLoopEndNode() != null : "No end node set";
			assert inTable == table : "Input table changed between iterations";
		}

		// Now generate the output port
		RCSBQueryPortObject out = nextQuery == null ? null
				: new RCSBQueryPortObject(nextQuery.getSecond());
		BufferedDataContainer out1 = exec.createDataContainer(outTableSpec);
		out1.addRowToTable(nextQuery.getFirst());
		out1.close();
		nextQuery = findNextValidQuery();

		// Update the loop counters
		pushFlowVariableInt("currentIteration", iteration++);

		return new PortObject[] { out, out1.getTable() };
	}

	private Pair<DataRow, MultiRCSBQueryModel> findNextValidQuery() {
		while (iter.hasNext()) {
			DataRow row = iter.next();
			DataCell cell = row.getCell(colIdx);
			if (cell.isMissing()) {
				continue;
			}

			// Try to get the XML loaded into a NodeSettingsRO
			String xml = ((StringValue) cell).getStringValue();
			NodeSettingsRO settings;
			try {
				settings = NodeSettings
						.loadFromXML(new ByteArrayInputStream(xml.getBytes()));
			} catch (Exception e) {
				if (!ignoreInvalidXMLMdl.getBooleanValue()) {
					throw new RuntimeException(
							"Error Reading XML - " + e.getMessage(), e);
				} else {
					continue;
				}
			}

			// Check we have the right sort of XML
			if (!settings.getKey().equals(CFGKEY_RCSB_PDB_ADVANCED_QUERY)) {
				if (!ignoreInvalidXMLMdl.getBooleanValue()) {
					throw new RuntimeException("Invalid Query Definition XML");
				} else {
					continue;
				}
			}

			// Now try to load into a query model
			MultiRCSBQueryModel outputModel = new MultiRCSBQueryModel();
			try {
				switch (settings.getInt(CFGKEY_VERSION)) {
					case 1:
						outputModel.loadSettings(settings);
						break;

					default:
						if (!ignoreInvalidXMLMdl.getBooleanValue()) {
							throw new RuntimeException(
									"Unsupported Query Definition XML Version");
						} else {
							continue;
						}
				}
			} catch (InvalidSettingsException e) {
				if (!ignoreInvalidXMLMdl.getBooleanValue()) {
					throw new RuntimeException(
							"Missing keys in XML Definition");
				} else {
					continue;
				}
			}

			// Finally check the
			if (!outputModel.hasQuery()) {
				switch (nqb) {
					case Fail:
						throw new RuntimeException(
								"No Query was found in the loaded row");
					case Skip:
						continue;
				}
			}
			return new Pair<>(row, outputModel);
		}
		return null;
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
		table = null;
		if (iter != null) {
			iter.close();
		}
		iter = null;
		iteration = 0;
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
	}

	@Override
	public boolean terminateLoop() {
		boolean continueLoop = iter == null || nextQuery != null;
		return !continueLoop;
	}

}
