/*******************************************************************************
 * Copyright (c) 2017,2019 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.abstrct;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.StreamableOperator;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.IncomingMoleculeException;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

/**
 * Abstract NodeModel Implementation for Nodes using a Fragmentation Factory and
 * {@link ColumnRearranger} to generate output. The node is streamable
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule object type
 * @param <U>
 *            The matcher object type
 */
public abstract class AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>
		extends AbstractMMPFragmentationFactoryNodeModel<T, U> {

	protected final String[] newColumnNames;
	protected final DataType[] newColumnTypes;
	protected final boolean applyAddHsAlways;
	protected U matcher;

	/**
	 * Convenience constructor for nodes which have the 'apply two cuts to
	 * single bond' option
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Whether the node is multicut
	 * @param hasRemoveHs
	 *            Does the node allow H-removal
	 * @param hasNumCuts
	 *            Does the node have the option to specify the number of cuts
	 * @param applyAddHsAlways
	 *            Should the 'AddHs' setting always be applied
	 * @param newColumnNames
	 *            The new column names
	 * @param newColumnTypes
	 *            The new column types
	 */
	public AbstractColumnRearrangerFragmentationFactoryNodeModel(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			boolean applyAddHsAlways, String[] newColumnNames,
			DataType[] newColumnTypes) {
		this(fragUtilityFactory, isMulticut, hasRemoveHs, hasNumCuts, true,
				applyAddHsAlways, newColumnNames, newColumnTypes);
	}

	/**
	 * Constructor
	 * 
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} instance
	 * @param isMulticut
	 *            Whether the node is multicut
	 * @param hasRemoveHs
	 *            Does the node allow H-removal
	 * @param hasNumCuts
	 *            Does the node have the option to specify the number of cuts
	 * @param hasTwoCutsToBond
	 *            Does the node have the 'two cuts to single bond' option
	 * @param applyAddHsAlways
	 *            Should the 'AddHs' setting always be applied
	 * @param newColumnNames
	 *            The new column names
	 * @param newColumnTypes
	 *            The new column types
	 */
	public AbstractColumnRearrangerFragmentationFactoryNodeModel(
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			boolean hasTwoCutsToBond, boolean applyAddHsAlways,
			String[] newColumnNames, DataType[] newColumnTypes) {
		super(1, 1, fragUtilityFactory, isMulticut, hasRemoveHs, hasNumCuts,
				hasTwoCutsToBond);
		if (newColumnNames == null || newColumnTypes == null
				|| newColumnNames.length < 1 || newColumnTypes.length < 1) {
			throw new IllegalArgumentException(
					"At least one new column must be specified!");
		}
		if (newColumnNames.length != newColumnTypes.length) {
			throw new IllegalArgumentException(
					"The number of new column names must match the number of new column types!");
		}
		this.newColumnNames =
				Arrays.copyOf(newColumnNames, newColumnNames.length);
		this.newColumnTypes =
				Arrays.copyOf(newColumnTypes, newColumnTypes.length);
		this.applyAddHsAlways = applyAddHsAlways;
	}

	@Override
	protected DataTableSpec[] doConfigure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		DataTableSpec in = inSpecs[0];
		ColumnRearranger r;
		try {
			r = createColumnRearranger(in);
		} catch (ToolkitException e) {
			throw new InvalidSettingsException(e);
		}
		DataTableSpec out = r.createSpec();
		return new DataTableSpec[] { out };
	}

	@Override
	protected BufferedDataTable[] doExecute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		BufferedDataTable in = inData[0];
		ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
		BufferedDataTable out = exec.createColumnRearrangeTable(in, r, exec);
		return new BufferedDataTable[] { out };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getInputPortRoles()
	 */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
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
		try {
			return createColumnRearranger((DataTableSpec) inSpecs[0])
					.createStreamableFunction();
		} catch (ToolkitException e) {
			throw new InvalidSettingsException(e);
		}
	}

	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec)
			throws ToolkitException, InvalidSettingsException {
		ColumnRearranger rearranger = new ColumnRearranger(inSpec);
		DataColumnSpec[] newColSpecs =
				new DataColumnSpec[newColumnNames.length];
		for (int i = 0; i < newColumnNames.length; i++) {
			newColSpecs[i] = new DataColumnSpecCreator(DataTableSpec
					.getUniqueColumnName(inSpec, newColumnNames[i]),
					newColumnTypes[i]).createSpec();
		}

		int molColIdx = inSpec.findColumnIndex(m_molColName.getStringValue());
		matcher = getMatcherFromSMARTS(getFragmentationSMARTSMatch());
		rearranger.append(new AbstractCellFactory(true, newColSpecs) {

			AtomicLong rowIdx = new AtomicLong(1);

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell molCell = row.getCell(molColIdx);
				DataCell[] retVal = new DataCell[newColSpecs.length];
				Arrays.fill(retVal, DataType.getMissingCell());
				if (molCell.isMissing()) {
					return retVal;
				}

				MoleculeFragmentationFactory2<T, U> fragFactory = null;
				final long rowIndex = rowIdx.getAndIncrement();
				try {
					T mol = getMoleculeFromRow(row, molColIdx, -1, rowIndex);
					if (fragUtilityFactory.moleculeIsEmpty(mol)
							|| fragUtilityFactory
									.moleculeIsMultiComponent(mol)) {
						fragUtilityFactory.rowCleanup(rowIndex);
						return retVal;
					}
					fragFactory = applyAddHsAlways && m_AddHs.getBooleanValue()
							? fragUtilityFactory
									.createHAddedFragmentationFactory(mol,
											matcher, false, verboseLogging,
											false, 0, 0.0, rowIndex)
							: fragUtilityFactory.createFragmentationFactory(mol,
									matcher, false, false, verboseLogging,
									false, 0, 0.0, cacheSize);
					retVal = getResultColumns(mol, fragFactory, rowIndex,
							newColSpecs.length);
				} catch (ToolkitException | RowExecutionException
						| IncomingMoleculeException e) {
					logger.info(e.getMessage() + " (" + row.getKey() + ")");
				} catch (Exception e) {
					if (e instanceof RuntimeException) {
						throw (RuntimeException) e;
					} else {
						throw new RuntimeException(e);
					}
				} finally {
					if (fragFactory != null) {
						fragFactory.close();
					}
					fragUtilityFactory.rowCleanup(rowIndex);
				}
				return retVal;
			}
		});
		return rearranger;
	}

	/**
	 * Abstract method to return the result columns
	 * 
	 * @param mol
	 *            The molecule object (guaranteed to be non-<code>null</code>
	 *            and not empty)
	 * @param fragFactory
	 *            the {@link MoleculeFragmentationFactory2} instance for the
	 *            molecule
	 * @param rowIndex
	 *            The row index
	 * @param newColCount
	 *            The number of new columns required
	 * @return The new Cells to be added
	 * @throws RowExecutionException
	 *             In the event of any errors processing the row
	 */
	protected abstract DataCell[] getResultColumns(T mol,
			MoleculeFragmentationFactory2<T, U> fragFactory, long rowIndex,
			int newColCount) throws RowExecutionException;

}
