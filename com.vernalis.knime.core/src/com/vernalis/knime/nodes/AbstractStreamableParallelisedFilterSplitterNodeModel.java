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
package com.vernalis.knime.nodes;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
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
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.util.MultiThreadWorker;
import org.knime.core.util.MultiThreadWorker.ComputationTask;

/**
 * This is the base node model class for parallelised, streamable
 * filter/splitter nodes. It implements the KNIME Streaming API to allow
 * streaming execution where available.
 * <p>
 * The basic abstract implementation comes with settings models for whether
 * 'missing' value rows should be kept, and whether matching rows should be
 * kept. The implementation proceeds by extracting an intermediate object from
 * the incoming {@link DataRow} via the {@link #getObjectFromRow(DataRow)}
 * method. If this object is <code>null</code> then it is handled according to
 * the keep missing setting, otherwise it is tested in hte
 * {@link #rowMatches(Object)} method.
 * </p>
 * <p>
 * By default, the output table(s) have the same spec and row content as the
 * incoming rows. However, an implementing node may, calculate a property from
 * an incoming row on which to filter that row, and subsequently output that
 * value appended to the row. In this case, the implementation should override
 * the {@link #getOutputSpec(DataTableSpec, boolean)} and
 * {@link #getOutRow(DataRow, Object, boolean)} methods.
 * </p>
 * <p>
 * Implementations should also implement the following abstract methods:
 * <ul>
 * <li>{@link #doConfigure(DataTableSpec[])} - which checks or guesses any
 * settings and column selection(s) during the configuration phase</li>
 * <li>{@link #doPreExecutionSetup(DataTableSpec)} - which is called at the
 * start of node execution during either streaming or conventional parallelised
 * execution, e.g. to set an input column index</li></li>
 * </ul>
 * <p>
 * Implementations may manage the loading and saving of settings models by
 * registering them to this node using
 * {@link #registerSettingsModel(SettingsModel)}, removing the need to override
 * the standard {@link NodeModel} methods for loading / saving / validating
 * settings.
 * </p>
 * <p>
 * Static helper methods are provided to create the requisite
 * {@link SettingsModel}s ({@link #createKeepMissingModel()} and
 * {@link #createKeepMatchingModel()}), and add the corresponding dialog
 * components
 * ({@link #addFilterSplitterBehaviourDialogComponents(DefaultNodeSettingsPane)})
 * </p>
 * 
 * @author S Roughley
 * @param <T>
 *            The type parameter of the object retrieved from the incoming
 *            DataRow to perform the filter test on
 */
public abstract class AbstractStreamableParallelisedFilterSplitterNodeModel<T>
		extends NodeModel implements SettingsModelRegistry {

	private static final String KEEP_MISSING_CELLS = "Keep missing cells";
	private static final String KEEP_MATCHES = "Keep matches";

	/**
	 * A simple immutable container class when running in normal, multithreaded
	 * mode rather than streaming mode. The object stores a boolean indicating
	 * whether the row should be kept or not, along with the object for
	 * filtering derived from the row, in case it is required to append
	 * additional columns. The incoming row is not stored, as that can be
	 * obtained from the {@link ComputationTask#getInput()} method
	 * 
	 * @author s.roughley
	 *
	 * @param <U>
	 *            The type parameter of the object retrieved from the incoming
	 *            DataRow to perform the filter test on
	 */
	private final class FilterResult<U> {

		private final boolean keep;
		private final U obj;

		/**
		 * @param keep
		 *            Is the result a 'keep' (ie.e. matches filter
		 * @param obj
		 *            The object which forms the basis for filtering
		 */
		private FilterResult(boolean keep, U obj) {
			this.keep = keep;
			this.obj = obj;
		}

		/**
		 * @return the keep
		 */
		private boolean isKeep() {
			return keep;
		}

		/**
		 * @return the obj
		 */
		private U getObj() {
			return obj;
		}

	}

	private final Set<SettingsModel> models = new HashSet<>();
	protected final SettingsModelBoolean m_keepMissing =
			registerSettingsModel(createKeepMissingModel());
	protected final SettingsModelBoolean m_keepMatches =
			registerSettingsModel(createKeepMatchingModel());

	protected final boolean isSplitter;

	/**
	 * Constructor for the node model.
	 * 
	 * @param isSplitter
	 *            if <code>true</code> then 2 outputs, otherwise 1
	 */
	protected AbstractStreamableParallelisedFilterSplitterNodeModel(
			boolean isSplitter) {
		super(1, isSplitter ? 2 : 1);
		this.isSplitter = isSplitter;
	}

	/**
	 * This method is called during the configure method. It should guess /
	 * check any column settings and any other settings. Invalid settings which
	 * cannot be recovered should throw an {@link InvalidSettingsException}. If
	 * all is well, then return <code>null</code>, otherwise, a message which
	 * will be logged as a WARNing. If other messages are required to be logged
	 * then access the {@link NodeLogger} instance associated with the node via
	 * a call to {@link #getLogger()}
	 * 
	 * @param inSpecs
	 *            The incoming data table specs
	 * @return A warning message to be sent to the logger, or <code>null</code>
	 *         if nothing is wrong
	 * @throws InvalidSettingsException
	 *             If the settings are invalid and cannot be guessed
	 */
	protected abstract String doConfigure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException;

	/**
	 * Method to allow the output port spec to be varied in subclasses, e.g. if
	 * a filter calculates and appends a property. By default, the method
	 * returns the input spec, as would be expected for a simple row filter or
	 * splitter, regardless of port index
	 * 
	 * @param inSpec
	 *            The incoming table spec
	 * @param keep
	 *            Is the spec for the 'keep' table or the 'drop' table (which
	 *            will not be present if the node is not a splitter)
	 * 
	 * @return The output spec for the output port with the specified index
	 */
	protected DataTableSpec getOutputSpec(DataTableSpec inSpec, boolean keep) {
		return inSpec;
	}

	/**
	 * This method is called immediately prior to node execution in either
	 * streaming or normal execution modes. It should be used to e.g. set column
	 * indices required for node calculation
	 * 
	 * @param inSpec
	 *            The incoming table spec
	 * @throws InvalidSettingsException
	 *             if there is a problem with the settings meaning that
	 *             execution is not possible, e.g. the selected column has not
	 *             been found
	 */
	protected abstract void doPreExecutionSetup(DataTableSpec inSpec)
			throws InvalidSettingsException;

	/**
	 * This method should be used to retrieve an object from the datarow upon
	 * which the filter criteria can be tested. In many cases, this will simply
	 * be the value contained in a cell of a given column of the input table
	 * 
	 * @param row
	 *            The incoming datarow to filter
	 * @return The object to test on for row inclusion (may be
	 *         <code>null</code>, e.g. for a missing input cell
	 * @throws Exception
	 *             Any exceptions thrown during the retrieval of the object. The
	 *             method should handle missing cells in the input table,
	 *             returning <code>null</code> if it is not possible to create
	 *             the object but execution should continue
	 */
	protected abstract T getObjectFromRow(DataRow row) throws Exception;

	/**
	 * This method returns the output row for the node, based on the contents of
	 * the incoming row, the filter contaniner object and whether the row is a
	 * keep or drop row.
	 * <p>
	 * The method will only be called should the row be required - i.e. a filter
	 * node will not call this method for rows not kept in the output table
	 * </p>
	 * <p>
	 * The default implementation returns the incoming row unchanged
	 * </p>
	 * 
	 * @param row
	 *            The incoming table row
	 * @param obj
	 *            The object for filtering, returned by
	 *            {@link #getObjectFromRow(DataRow)}
	 * @param isKeepRow
	 *            Whether the row is a 'keep' or 'drop' row.
	 * @return The output data row
	 */
	protected DataRow getOutRow(DataRow row, T obj, boolean isKeepRow) {
		return row;
	}

	/**
	 * Method to check if row is to be kept. The result of the method will be
	 * combined with the keepMatches setting from the node dialog using !(a^b)
	 * to determine whether row is kept or discarded. NB if
	 * {@link #getObjectFromRow(DataRow)} returns <code>null</code>, then this
	 * method will not be called, and the fate of the row will instead be
	 * determined by the 'keep missing' setting from the node dialog
	 * 
	 * @param obj
	 *            The object from the incoming DataRow
	 * @return <code>true</code> if the row matches the filter
	 * @see #getObjectFromRow(DataRow)
	 */
	protected abstract boolean rowMatches(T obj);

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			String msg = doConfigure(inSpecs);
			if (msg != null) {
				getLogger().warn(msg);
				setWarningMessage(msg);
			}
		} catch (InvalidSettingsException e) {
			getLogger().error(e.getMessage());
			throw e;
		}
		DataTableSpec[] retVal = new DataTableSpec[isSplitter ? 2 : 1];
		retVal[0] = getOutputSpec(inSpecs[0], true);
		if (isSplitter) {
			retVal[1] = getOutputSpec(inSpecs[0], false);
		}
		return retVal;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		BufferedDataTable inTable = inData[0];
		doPreExecutionSetup(inData[0].getDataTableSpec());
		// Create the output containers
		BufferedDataContainer[] bdc =
				new BufferedDataContainer[isSplitter ? 2 : 1];
		bdc[0] = exec
				.createDataContainer(getOutputSpec(inTable.getSpec(), true));
		if (isSplitter) {
			bdc[1] = exec.createDataContainer(
					getOutputSpec(inTable.getSpec(), true));
		}

		int maxParallelWorkers = (int) Math
				.ceil(1.5 * Runtime.getRuntime().availableProcessors());
		MultiThreadWorker<DataRow, FilterResult<T>> worker =
				new MultiThreadWorker<DataRow, FilterResult<T>>(
						10 * maxParallelWorkers, maxParallelWorkers) {

					@Override
					protected void processFinished(ComputationTask task)
							throws ExecutionException, CancellationException,
							InterruptedException {

						final FilterResult<T> filterResult = task.get();
						if (filterResult.isKeep()) {
							bdc[0].addRowToTable(getOutRow(task.getInput(),
									filterResult.getObj(),
									filterResult.isKeep()));
						} else if (isSplitter) {
							bdc[1].addRowToTable(getOutRow(task.getInput(),
									filterResult.getObj(),
									filterResult.isKeep()));
						}
						exec.setProgress(
								(double) task.getIndex() / inTable.size());
					}

					@Override
					protected FilterResult<T> compute(DataRow in, long index)
							throws Exception {
						T obj = getObjectFromRow(in);
						return new FilterResult<>(
								obj == null ? m_keepMissing.getBooleanValue()
										: rowMatches(obj) == m_keepMatches
												.getBooleanValue(),
								obj);
					}
				};

		try {
			worker.run(inTable);
		} catch (InterruptedException e) {
			CanceledExecutionException cee =
					new CanceledExecutionException(e.getMessage());
			cee.initCause(e);
			throw cee;
		}
		bdc[0].close();
		if (isSplitter) {
			bdc[1].close();
		}
		return (isSplitter)
				? new BufferedDataTable[] { bdc[0].getTable(),
						bdc[1].getTable() }
				: new BufferedDataTable[] { bdc[0].getTable() };
	}

	@Override
	public StreamableOperator createStreamableOperator(
			final PartitionInfo partitionInfo, final PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		doPreExecutionSetup((DataTableSpec) inSpecs[0]);
		return new StreamableOperator() {

			@Override
			public void runFinal(PortInput[] inputs, PortOutput[] outputs,
					ExecutionContext exec) throws Exception {

				// Run it - dont know row count!
				AbstractStreamableParallelisedFilterSplitterNodeModel.this
						.doExecute((RowInput) inputs[0], (RowOutput) outputs[0],
								isSplitter ? (RowOutput) outputs[1] : null, -1,
								exec);

			}

		};
	}

	protected void doExecute(final RowInput inRow, RowOutput keeps,
			RowOutput drop, final long numRows, final ExecutionContext exec)
			throws InterruptedException, CanceledExecutionException,
			ExecutionException {

		long rowIdx = 0;
		DataRow row;
		while ((row = inRow.poll()) != null) {
			if (numRows > 0) {
				exec.setProgress((++rowIdx) / (double) numRows,
						"Filtering row " + rowIdx + " of " + numRows);
			} else {
				exec.setProgress("Filtering row " + rowIdx);
			}
			exec.checkCanceled();

			T obj;
			try {
				obj = getObjectFromRow(row);
			} catch (InterruptedException | CanceledExecutionException e) {
				throw e;
			} catch (Exception e) {
				throw new ExecutionException(e);
			}

			if (obj == null) {
				if (m_keepMissing.getBooleanValue()) {
					keeps.push(getOutRow(row, obj, true));
				} else if (drop != null) {
					drop.push(getOutRow(row, obj, false));
				}
				continue;
			}

			if (rowMatches(obj) == m_keepMatches.getBooleanValue()) {
				keeps.push(getOutRow(row, obj, true));
			} else if (drop != null) {
				drop.push(getOutRow(row, obj, false));
			}
		}
		keeps.close();
		if (drop != null) {
			drop.close();
		}
	}

	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	@Override
	public OutputPortRole[] getOutputPortRoles() {
		OutputPortRole[] retVal = new OutputPortRole[isSplitter ? 2 : 1];
		Arrays.fill(retVal, OutputPortRole.DISTRIBUTED);
		return retVal;
	}

	@Override
	public Set<SettingsModel> getModels() {
		return models;
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
	public void saveSettingsTo(final NodeSettingsWO settings) {
		SettingsModelRegistry.super.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.loadValidatedSettingsFrom(settings);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateSettings(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		SettingsModelRegistry.super.validateSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
	}

	/**
	 * Static helper method to add the required dialog components for Filter /
	 * Splitter node behaviour to the node dialog
	 * 
	 * @param defaultNodeSettingsPane
	 *            The node settings pane instance
	 * 
	 */
	public static void addFilterSplitterBehaviourDialogComponents(
			DefaultNodeSettingsPane defaultNodeSettingsPane) {
		defaultNodeSettingsPane.addDialogComponent(new DialogComponentBoolean(
				createKeepMissingModel(), KEEP_MISSING_CELLS));
		defaultNodeSettingsPane.addDialogComponent(new DialogComponentBoolean(
				createKeepMatchingModel(), KEEP_MATCHES));
	}

	/**
	 * Method to create the settings model for the 'Keep Missing Cells'
	 * behaviour
	 * 
	 * @return Keep Missing Cells Settings Model
	 */
	public static SettingsModelBoolean createKeepMissingModel() {
		return new SettingsModelBoolean(KEEP_MISSING_CELLS, true);
	}

	/**
	 * Method to create the settings model for the 'Keep Matches' model
	 * 
	 * @return Keep Matches Settings Model
	 */
	public static SettingsModelBoolean createKeepMatchingModel() {
		return new SettingsModelBoolean(KEEP_MATCHES, true);
	}

}
