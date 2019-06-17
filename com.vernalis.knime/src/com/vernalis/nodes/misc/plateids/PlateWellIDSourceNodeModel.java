/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.misc.plateids;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.concurrent.CancellationException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.knime.data.datacolumn.EnhancedDataColumnSpecCreator;
import com.vernalis.knime.dialog.components.SettingsModelIntegerBoundedRerangable;

import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createColPadModel;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createDirectionModel;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createFirstPlateIndexModel;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createPlatePrefixMdl;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createPlateRowDelimMdl;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createRowColumnSepMdl;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createRowPadModel;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createSizeModel;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createSkipEndMdl;
import static com.vernalis.nodes.misc.plateids.PlateWellIDNodeDialog.createSkipStartMdl;

/**
 * {@link NodeModel} implementation for the Plate Well IDs source node
 * 
 * @author s.roughley
 *
 */
public class PlateWellIDSourceNodeModel extends NodeModel {

	private final SettingsModelString sizeMdl = createSizeModel();
	private final SettingsModelString dirMdl = createDirectionModel();
	private final SettingsModelBoolean padRowMdl = createRowPadModel();
	private final SettingsModelBoolean padColMdl = createColPadModel();
	private final SettingsModelString platePrefixMdl = createPlatePrefixMdl();
	private final SettingsModelString plateRowDelimMdl =
			createPlateRowDelimMdl();
	private final SettingsModelString rowColDelimMdl = createRowColumnSepMdl();
	private final SettingsModelIntegerBounded firstPlateMdl =
			createFirstPlateIndexModel();
	private final SettingsModelIntegerBoundedRerangable skipStartMdl =
			createSkipStartMdl();
	private final SettingsModelIntegerBoundedRerangable skipEndMdl =
			createSkipEndMdl();

	private static final String EMPTY_STRING = "";
	private PlateSize size = PlateSize.getDefault();
	private PlateDirection direction = PlateDirection.getDefault();

	public PlateWellIDSourceNodeModel() {
		super(0, 1);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec
	 * [])
	 */
	@Override
	protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		if (platePrefixMdl.getStringValue() == null) {
			platePrefixMdl.setStringValue(EMPTY_STRING);
		}
		if (plateRowDelimMdl.getStringValue() == null) {
			plateRowDelimMdl.setStringValue(EMPTY_STRING);
		}
		if (rowColDelimMdl.getStringValue() == null) {
			rowColDelimMdl.setStringValue(EMPTY_STRING);
		}
		size = PlateSize.valueOf(sizeMdl.getStringValue());
		direction = PlateDirection.valueOf(dirMdl.getStringValue());
		if (size.getWells() - skipStartMdl.getIntValue() < 1) {
			throw new InvalidSettingsException(
					"Skipped wells at start means no wells will be used");
		}
		if (size.getWells() - skipEndMdl.getIntValue() < 1) {
			throw new InvalidSettingsException(
					"Skipped wells at end means no wells will be used");
		}
		if (size.getWells() - skipStartMdl.getIntValue()
				- skipEndMdl.getIntValue() < 1) {
			throw new InvalidSettingsException(
					"Skipped wells settings mean no wells will be used");
		}
		return new DataTableSpec[] { createOutputSpec() };
	}

	private DataTableSpec createOutputSpec() {
		DataColumnProperties colProps = new PlatePropertyBuilder()
				.setPlateSize(size).setPlateDirection(direction)
				.setWellsSkippedAtStart(skipStartMdl.getIntValue())
				.setWellsSkippedAtEnd(skipEndMdl.getIntValue())
				.setSpacePadRowIDs(padRowMdl.getBooleanValue())
				.setZeroPadColumnIDs(padColMdl.getBooleanValue())
				.setPlatePrefix(platePrefixMdl.getStringValue())
				.setPlateRowDeliminator(plateRowDelimMdl.getStringValue())
				.setRowColDeliminator(rowColDelimMdl.getStringValue())
				.getProperties();

		DataColumnSpec[] newColSpecs = new DataColumnSpec[7];
		newColSpecs[0] =
				new EnhancedDataColumnSpecCreator("Plate Index", IntCell.TYPE)
						.setProperties(colProps).createSpec();
		newColSpecs[1] =
				new EnhancedDataColumnSpecCreator("Well Index", IntCell.TYPE)
						.setProperties(colProps).createSpec();
		newColSpecs[2] =
				new EnhancedDataColumnSpecCreator("Row Index", IntCell.TYPE)
						.setProperties(colProps).createSpec();
		newColSpecs[3] =
				new EnhancedDataColumnSpecCreator("Row ID", StringCell.TYPE)
						.setProperties(colProps).createSpec();
		newColSpecs[4] =
				new EnhancedDataColumnSpecCreator("Column Index", IntCell.TYPE)
						.setProperties(colProps).createSpec();
		newColSpecs[5] =
				new EnhancedDataColumnSpecCreator("Well ID", StringCell.TYPE)
						.setProperties(colProps).createSpec();
		newColSpecs[6] = new EnhancedDataColumnSpecCreator("Plate/Well ID",
				StringCell.TYPE).setProperties(colProps).createSpec();
		return new DataTableSpecCreator().addColumns(newColSpecs).createSpec();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.
	 * BufferedDataTable[], org.knime.core.node.ExecutionContext)
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {

		BufferedDataContainer bdc =
				exec.createDataContainer(createOutputSpec());
		BufferedDataTableRowOutput bdtro = new BufferedDataTableRowOutput(bdc);
		execute(bdtro, exec);
		bdc.close();
		return new BufferedDataTable[] { bdc.getTable() };
	}

	protected void execute(RowOutput outPort, ExecutionContext exec)
			throws InterruptedException {
		// Some execution time constants from settings
		size = PlateSize.valueOf(sizeMdl.getStringValue());
		direction = PlateDirection.valueOf(dirMdl.getStringValue());
		// %[flags][width]conversion
		final String rowIdFmt = padRowMdl.getBooleanValue()
				? "%" + size.getMaxRowStringWidth() + "s" : "%s";
		final String colIdFmt = padColMdl.getBooleanValue()
				? "%0" + size.getMaxColStringWidth() + "d" : "%d";
		int plateIdx = firstPlateMdl.getIntValue();
		long rowIdx = 0;
		double progressPerRow = 1.0 / (1 + skipStartMdl.getIntValue()
				- (size.getWells() - skipEndMdl.getIntValue()));
		for (int wellIdx =
				1 + skipStartMdl.getIntValue(); wellIdx <= size.getWells()
						- skipEndMdl.getIntValue(); wellIdx++) {
			try {
				exec.checkCanceled();
			} catch (CanceledExecutionException e) {
				throw new CancellationException(e.getMessage());
			}
			exec.setProgress(rowIdx * progressPerRow,
					"Added " + rowIdx + " wells...");
			DataCell[] newCells = new DataCell[7];
			newCells[0] = new IntCell(plateIdx);
			newCells[1] = new IntCell(wellIdx);
			int r = size.getRowFromWellIndex(wellIdx, direction);
			int c = size.getColFromWellIndex(wellIdx, direction);
			StringBuilder wellName = new StringBuilder();
			Formatter fmt = new Formatter(wellName);
			fmt.format(rowIdFmt, PlateSize.rowIDFromRowIndex(r));
			newCells[2] = new IntCell(r);
			newCells[3] = new StringCell(wellName.toString().trim());
			newCells[4] = new IntCell(c);
			wellName.append(rowColDelimMdl.getStringValue());
			fmt.format(colIdFmt, c);
			newCells[5] = new StringCell(wellName.toString());
			wellName.insert(0, plateRowDelimMdl.getStringValue());
			wellName.insert(0, plateIdx);
			wellName.insert(0, platePrefixMdl.getStringValue());
			fmt.close();
			newCells[6] = new StringCell(wellName.toString());
			outPort.push(
					new DefaultRow(RowKey.createRowKey(rowIdx++), newCells));
		}
		outPort.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getInputPortRoles()
	 */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED };
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
				execute((RowOutput) outputs[0], exec);

			}
		};
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		sizeMdl.saveSettingsTo(settings);
		dirMdl.saveSettingsTo(settings);
		padRowMdl.saveSettingsTo(settings);
		padColMdl.saveSettingsTo(settings);
		platePrefixMdl.saveSettingsTo(settings);
		plateRowDelimMdl.saveSettingsTo(settings);
		rowColDelimMdl.saveSettingsTo(settings);
		firstPlateMdl.saveSettingsTo(settings);
		skipStartMdl.saveSettingsTo(settings);
		skipEndMdl.saveSettingsTo(settings);
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		sizeMdl.validateSettings(settings);
		dirMdl.validateSettings(settings);
		padRowMdl.validateSettings(settings);
		padColMdl.validateSettings(settings);
		platePrefixMdl.validateSettings(settings);
		plateRowDelimMdl.validateSettings(settings);
		rowColDelimMdl.validateSettings(settings);
		firstPlateMdl.validateSettings(settings);
		skipStartMdl.validateSettings(settings);
		skipEndMdl.validateSettings(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		sizeMdl.loadSettingsFrom(settings);
		dirMdl.loadSettingsFrom(settings);
		padRowMdl.loadSettingsFrom(settings);
		padColMdl.loadSettingsFrom(settings);
		platePrefixMdl.loadSettingsFrom(settings);
		plateRowDelimMdl.loadSettingsFrom(settings);
		rowColDelimMdl.loadSettingsFrom(settings);
		firstPlateMdl.loadSettingsFrom(settings);
		skipStartMdl.loadSettingsFrom(settings);
		skipEndMdl.loadSettingsFrom(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
	 */
	@Override
	protected void reset() {
	}

}
