/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 * 
 *  This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.pmi.nodes.rdkit.abstrct;

import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createMolColNameModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortType;
import org.rdkit.knime.types.RDKitMolValue;

import com.vernalis.knime.chem.pmi.util.misc.PmiUtils;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

public abstract class AbstractVerRDKitRearrangerNodeModel extends NodeModel {
	protected static final String COL_NAME = "colName";

	protected static final String REMOVE_INPUT_COL = "Remove Input Col";

	// the logger instance
	protected final NodeLogger logger = NodeLogger.getLogger(this.getClass());

	protected Map<String, SettingsModel> settingsModels = new HashMap<>();
	/**
	 * Garbage collector for RDKit SWIG Objects
	 */
	protected SWIGObjectGarbageCollector gc = new SWIGObjectGarbageCollector();

	/**
	 * The wave index for GC. As we plan to run in parallelisation, we need this
	 * to be atomic. It starts at 1 as wave 0 is reserved for non-assigned waves
	 */
	protected final AtomicInteger gcWave = new AtomicInteger(1);

	protected Map<String, DataType> resultColumns = new LinkedHashMap<>();

	/**
	 * @param nrInDataPorts
	 * @param nrOutDataPorts
	 */
	public AbstractVerRDKitRearrangerNodeModel(int nrInDataPorts, int nrOutDataPorts) {
		super(nrInDataPorts, nrOutDataPorts);
		registerSettingsModel(COL_NAME, createMolColNameModel());
	}

	/**
	 * @param inPortTypes
	 * @param outPortTypes
	 */
	public AbstractVerRDKitRearrangerNodeModel(PortType[] inPortTypes, PortType[] outPortTypes) {
		super(inPortTypes, outPortTypes);
		registerSettingsModel(COL_NAME, createMolColNameModel());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		if (resultColumns == null || resultColumns.size() == 0) {
			throw new InvalidSettingsException("No result columns defined.  "
					+ "Implementations should use registerResultColumn() "
					+ "to add output columns");
		}
		// Check the selection for the sdf or mol column
		((SettingsModelString) getSettingsModel(COL_NAME))
				.setStringValue(PmiUtils.checkColumnNameAndAutoPick(
						((SettingsModelString) getSettingsModel(COL_NAME)).getStringValue(),
						inSpecs[0], logger));

		// Use the rearranger to create the spec- also checks if any properties
		// are being calculated
		try {
			return new DataTableSpec[] { createColumnRearranger(inSpecs[0]).createSpec() };
		} catch (Exception e) {
			throw new InvalidSettingsException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable[], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
			throws Exception {
		gcWave.set(1);
		return new BufferedDataTable[] { exec.createColumnRearrangeTable(inData[0],
				createColumnRearranger(inData[0].getDataTableSpec()), exec) };
	}

	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec) throws Exception {
		ColumnRearranger rearranger = new ColumnRearranger(inSpec);

		// Now generate the new column specs
		DataColumnSpec[] newColSpec = createNewColumnSpecs(inSpec);

		final int molColIdx = inSpec.findColumnIndex(
				((SettingsModelString) getSettingsModel(COL_NAME)).getStringValue());

		// Create a CellFactory which calls #calculateResultCells() if the
		// molecule is non-null
		AbstractCellFactory cellFact = new AbstractCellFactory(true, newColSpec) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell[] retVal = new DataCell[resultColumns.size()];
				Arrays.fill(retVal, DataType.getMissingCell());

				DataCell molCell = row.getCell(molColIdx);
				if (molCell.isMissing()) {
					return retVal;
				}
				int currentWaveID = gcWave.getAndIncrement();

				ROMol mol = gc.markForCleanup(getMolFromCell(molCell), currentWaveID);
				if (mol != null) {
					calculateResultCells(retVal, mol);
					gc.cleanupMarkedObjects(currentWaveID);
				}
				return retVal;
			}
		};

		// Append or replace the new column(s)
		if (settingsModels.containsKey(REMOVE_INPUT_COL)
				&& ((SettingsModelBoolean) getSettingsModel(REMOVE_INPUT_COL)).getBooleanValue()) {
			// #replace() only works for single column, and doesnt look right to
			// insert all at the replace column using #remove() then #insertAt()
			if (cellFact.getColumnSpecs().length > 1) {
				SingleCellFactory scf = new SingleCellFactory(cellFact.isParallelProcessing(),
						cellFact.getColumnSpecs()[0]) {

					@Override
					public DataCell getCell(DataRow row) {
						return cellFact.getCells(row)[0];
					}
				};
				rearranger.replace(scf, molColIdx);

				AbstractCellFactory newCellFact = new AbstractCellFactory(
						cellFact.isParallelProcessing(), Arrays.copyOfRange(
								cellFact.getColumnSpecs(), 1, cellFact.getColumnSpecs().length)) {

					@Override
					public DataCell[] getCells(DataRow row) {
						return Arrays.copyOfRange(cellFact.getCells(row), 1,
								cellFact.getColumnSpecs().length);
					}
				};
				rearranger.append(newCellFact);
			} else {
				rearranger.replace(cellFact, molColIdx);
			}
		} else {
			rearranger.append(cellFact);
		}
		return rearranger;
	}

	/**
	 * This method needs to be implemented to calculate the required results and
	 * place them in the required positions in retVal. It will not be called if
	 * the ROMol is {@code null}. The result cells array is preloaded with
	 * missing cells.
	 * 
	 * @param resultCells
	 *            The result cells
	 * @param mol
	 *            The ROMol object
	 */
	protected abstract void calculateResultCells(DataCell[] resultCells, ROMol mol);

	protected ROMol getMolFromCell(DataCell molCell) {
		ROMol mol = null;
		DataType type = molCell.getType();
		try {
			if (type.isCompatible(RDKitMolValue.class)) {
				mol = ((RDKitMolValue) molCell).readMoleculeValue();
			} else if (type.isCompatible(SmilesValue.class)) {
				RWMol rwMol = RWMol.MolFromSmiles(((SmilesValue) molCell).getSmilesValue(), 0,
						false);
				RDKFuncs.sanitizeMol(rwMol);
				mol = rwMol;
			} else if (type.isCompatible(MolValue.class)) {
				mol = RWMol.MolFromMolBlock(((MolValue) molCell).getMolValue(), true, false);
			} else if (type.isCompatible(SdfValue.class)) {
				mol = RWMol.MolFromMolBlock(((SdfValue) molCell).getSdfValue(), true, false);
			}
		} catch (Exception e) {
			// Nothing - we return a null
		}
		return mol;
	}

	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec inSpec) {
		DataColumnSpec[] retVal = new DataColumnSpec[resultColumns.size()];
		int colIdx = 0;
		for (Entry<String, DataType> ent : resultColumns.entrySet()) {
			retVal[colIdx++] = new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(inSpec, ent.getKey().replace("_", " ")),
					ent.getValue()).createSpec();
		}
		return retVal;
	}

	protected void registerSettingsModel(String name, SettingsModel model) {
		settingsModels.put(name, model);
	}

	protected SettingsModel getSettingsModel(String name) {
		return settingsModels.get(name);
	}

	protected void registerResultColumn(String name, DataType dataType) {
		resultColumns.put(name, dataType);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		for (SettingsModel model : settingsModels.values()) {
			model.saveSettingsTo(settings);
		}

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		for (SettingsModel model : settingsModels.values()) {
			model.validateSettings(settings);
		}

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		for (SettingsModel model : settingsModels.values()) {
			model.loadSettingsFrom(settings);
		}
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing

	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.props.abstrct.
	 * AbstractPropertyCalcNodeModel#reset()
	 */
	@Override
	protected void reset() {
		gc.quarantineAndCleanupMarkedObjects();
		gcWave.set(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		gc.quarantineAndCleanupMarkedObjects();
		super.onDispose();
	}

	protected void resetResultColumns() {
		resultColumns = new LinkedHashMap<>();

	}

}
