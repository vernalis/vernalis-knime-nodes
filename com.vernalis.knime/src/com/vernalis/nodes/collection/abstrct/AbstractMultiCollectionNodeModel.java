/*******************************************************************************
 * Copyright (c) 2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.abstrct;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog.COLLECTION_COLUMNS;
import static com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog.DEFAULT_ACCEPT_LISTS;
import static com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog.DEFAULT_ACCEPT_SETS;
import static com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog.createColumnFilterModel;
import static com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog.createFailIfNoMatchingColumnsModel;
import static com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog.createReplaceInputColumnsModel;

/**
 * Abstract base Node Model class for collection handling nodes taking multiple
 * collection columns as inputs
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.36.2
 *
 */
public abstract class AbstractMultiCollectionNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private static final int DEFAULT_CURRENT_IMPLEMENTATION_VERSION = 2;
	private final SettingsModelColumnFilter2 colsMdl;

	private final SettingsModelBoolean replaceInputColsMdl;
	// New setting introduced in v2 of the nodes to fix public bug #18
	private final SettingsModelBoolean failIfNoSelectedColsMdl =
			registerSettingsModel(createFailIfNoMatchingColumnsModel(), 2,
					mdl -> mdl.setBooleanValue(true),
					"Applied legacy compatible value to 'Fail if no matching columns' option");

	/**
	 * Overloaded constructor with defaults for all parameters the
	 * includeReplaceInputsOption and allowing the user to specify a model
	 * version
	 * 
	 * @param includeReplaceInputsOption
	 *            Whether the node includes an option to replace inputs
	 * @param nodeSettingsVersion
	 *            The current node settings version of the node
	 * 
	 * @since 1.36.2
	 */
	protected AbstractMultiCollectionNodeModel(
			boolean includeReplaceInputsOption, int nodeSettingsVersion) {
		this(COLLECTION_COLUMNS, includeReplaceInputsOption,
				DEFAULT_ACCEPT_LISTS, DEFAULT_ACCEPT_SETS,
				DEFAULT_CURRENT_IMPLEMENTATION_VERSION);
	}

	/**
	 * Overloaded constructor with defaults for all parameters the
	 * includeReplaceInputsOption
	 * 
	 * @param includeReplaceInputsOption
	 *            Whether the node includes an option to replace inputs
	 *
	 * @since 1.36.2
	 */
	protected AbstractMultiCollectionNodeModel(
			boolean includeReplaceInputsOption) {
		this(COLLECTION_COLUMNS, includeReplaceInputsOption,
				DEFAULT_ACCEPT_LISTS, DEFAULT_ACCEPT_SETS);
	}

	/**
	 * Overloaded constructor using the base current node settings version
	 *
	 * @param collectionSelectorName
	 *            The name used in the dialog for the collection column selector
	 *            panel (also used as the settings key)
	 * @param includeReplaceInputsOption
	 *            Whether the node includes an option to replace inputs
	 * @param acceptLists
	 *            Whether the node accepts Lists
	 * @param acceptSets
	 *            Whether the node accepts Sets
	 * 
	 * @since 1.36.2
	 */
	protected AbstractMultiCollectionNodeModel(String collectionSelectorName,
			boolean includeReplaceInputsOption, boolean acceptLists,
			boolean acceptSets) {
		this(collectionSelectorName, includeReplaceInputsOption, acceptLists,
				acceptSets, DEFAULT_CURRENT_IMPLEMENTATION_VERSION);
	}

	/**
	 * Full constructor
	 * 
	 * @param collectionSelectorName
	 *            The name used in the dialog for the collection column selector
	 *            panel (also used as the settings key)
	 * @param includeReplaceInputsOption
	 *            Whether the node includes an option to replace inputs
	 * @param acceptLists
	 *            Whether the node accepts Lists
	 * @param acceptSets
	 *            Whether the node accepts Sets
	 * @param nodeSettingsVersion
	 *            The current node settings version of the node
	 *
	 * @since 1.36.2
	 */
	protected AbstractMultiCollectionNodeModel(String collectionSelectorName,
			boolean includeReplaceInputsOption, boolean acceptLists,
			boolean acceptSets, int nodeSettingsVersion) {
		super(nodeSettingsVersion);
		colsMdl = registerSettingsModel(createColumnFilterModel(
				collectionSelectorName, acceptLists, acceptSets));
		if (includeReplaceInputsOption) {
			replaceInputColsMdl =
					registerSettingsModel(createReplaceInputColumnsModel());
		} else {
			replaceInputColsMdl = null;
		}
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		int[] idx = getSelectedColumnIndices(spec);
		DataColumnSpec[] newColSpecs = createNewColumnSpecs(spec, idx);

		doConfigure(spec);

		AbstractCellFactory cellFact = new AbstractCellFactory(newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				if (idx.length == 0) {
					return ArrayUtils.of(DataType.getMissingCell(),
							newColSpecs.length);
				}
				return AbstractMultiCollectionNodeModel.this.getCells(idx, row,
						newColSpecs);
			}

		};
		// Create the rearranger, and apply the cell factory appropriately
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		if (isReplaceInputCols()) {
			rearranger.replace(cellFact, idx);
		} else {
			rearranger.append(cellFact);
		}
		return rearranger;
	}

	/**
	 * Method to get the incoming collection column indices selected by the
	 * filter panel. Implementing classes may wish to override this method if
	 * further manipulation of the selected column indices are required
	 * 
	 * @param spec
	 *            the incoming table spec
	 * 
	 * @return the selected column indices
	 * 
	 * @throws InvalidSettingsException
	 *             if not columns are selected and the node setting stipulate
	 *             the node should thus fail
	 *
	 * @since 1.36.2
	 */
	protected int[] getSelectedColumnIndices(DataTableSpec spec)
			throws InvalidSettingsException {
		int[] idx = spec.columnsToIndices(colsMdl.applyTo(spec).getIncludes());
		if (idx.length == 0 && failIfNoSelectedColsMdl.getBooleanValue()) {
			throw new InvalidSettingsException("No columns selected");
		}
		return idx;
	}

	/**
	 * Method to allow additional steps to be performed during the configure
	 * step. Called after the selected column indices have been determined and
	 * new output column specs created. The default implementation does nothing
	 * 
	 * @param spec
	 *            the incoming table sepc
	 * 
	 * @throws InvalidSettingsException
	 *             if there was an error during the method
	 *
	 * @since 1.36.2
	 */
	protected void doConfigure(DataTableSpec spec)
			throws InvalidSettingsException {
		// Do nothing
	}

	/**
	 * Method to column specs for the new output table
	 * 
	 * @param spec
	 *            the incoming table spec. Maybe empty, but never {@code null}
	 * @param idx
	 *            the selected incoming column indices
	 * 
	 * @return the new column specs to be appended to the input table
	 *
	 * @since 1.36.2
	 */
	protected abstract DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx);

	/**
	 * Method to calculate the result cells for an incoming row. Implementations
	 * need to handle missing cells
	 * 
	 * @param idx
	 *            the selected incoming column IDs. Never {@code null} or empty
	 * @param row
	 *            the incoming data row
	 * @param newColSpecs
	 *            the new out column specs
	 * 
	 * @return the new data cells to be added to the output table
	 * 
	 * @throws RuntimeException
	 *             if there was an error during the computation
	 *
	 * @since 1.36.2
	 */
	protected abstract DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException;

	/**
	 * @return the colsMdl
	 *
	 * @since 1.36.2
	 */
	protected final SettingsModelColumnFilter2 getColsMdl() {
		return colsMdl;
	}

	/**
	 * @return the replaceInputColsMdl
	 *
	 * @since 1.36.2
	 */
	protected final SettingsModelBoolean getReplaceInputColsMdl() {
		return replaceInputColsMdl;
	}

	/**
	 * @return whether the input columns should be replaced. The default
	 *         implementation returns {@code true} if there is no dialog option
	 *         to replace the input or the dialog component exists and it's
	 *         value is true. Implementing classes may override this method to
	 *         change this behaviour
	 *
	 * @since 1.36.2
	 */
	protected boolean isReplaceInputCols() {
		return replaceInputColsMdl == null
				|| replaceInputColsMdl.getBooleanValue();
	}
}
