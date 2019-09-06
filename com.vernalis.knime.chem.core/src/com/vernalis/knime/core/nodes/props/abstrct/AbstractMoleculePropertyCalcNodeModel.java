/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.core.nodes.props.abstrct;

import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.propcalc.AbstractPropertyCalcNodeModel;
import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;

/**
 * Abstract NodeModel to calculate properties based on a
 * {@link CalculatedPropertyInterface} implementation. The node will calculated
 * the properties via a {@link ColumnRearranger} in parallel. The user needs to
 * supply an array of the possible property values at construction, and to
 * implement the abstract {@link #getMolContainerFromCell(DataCell)} and
 * {@link #checkMolZCoord(Object)} methods, returning the molecule in the
 * appropriate format. NB The linked abstract subclasses handle some or all of
 * these operations. Properties calculated via an intermediate list of Points
 * should use {@link AbstractPointListMoleculePropertyCalcNodeModel}
 * 
 * @author s.roughley knime@vernalis.com
 * @param <T>
 *            The type of the object required by the
 *            {@link CalculatedPropertyInterface} implementation's
 *            {@code #calculate(T input)} method. Nodes requiring T=ROMol should
 *            use the {@link AbstractRdkitPropertyCalcNodeModel}
 * @see AbstractRdkitPropertyCalcNodeModel
 * @see AbstractPointListMoleculePropertyCalcNodeModel
 */
public abstract class AbstractMoleculePropertyCalcNodeModel<T>
		extends AbstractPropertyCalcNodeModel<T> {

	// Minimum z coordinate for non-2D coordinates
	protected static final double MAX_ZERO_Z = 0.0001;

	protected boolean hasNonZeroZ = false;
	protected final boolean checkZCoordsfor3D;

	/**
	 * Overloaded constructor, specifying no checking of z-coordinates
	 * 
	 * @param propertyLabel
	 *            The name properties label to be used as the settings model key
	 * @param possibleProps
	 *            The properties to be calculated
	 * @param acceptedColumns
	 *            A {@link ColumnFilter} for the acceptable input types
	 */
	protected AbstractMoleculePropertyCalcNodeModel(String propertyLabel,
			CalculatedPropertyInterface<T>[] possibleProps,
			ColumnFilter acceptedColumns) {
		this(propertyLabel, possibleProps, acceptedColumns, false);
	}

	/**
	 * Main constructor. Provides a 1-in 1-out node with column rearranger. The
	 * user must supply the full list of possible values. Assuming that
	 * {@link CalculatedPropertyInterface} is implemented as an {@link Enum},
	 * this can be obtained by calling the #values() method.
	 * 
	 * @param propertyLabel
	 *            The name properties label to be used as the settings model key
	 * @param possibleProps
	 *            The properties to be calculated
	 * @param acceptedColumns
	 *            A {@link ColumnFilter} for the acceptable input types
	 * @param checkZCoordsfor3D
	 *            Should the node check that there are non-zero z-coordinates
	 *            present in the table? If {@code true} and there are none found
	 *            then a warning message will be shown after node execution
	 */
	protected AbstractMoleculePropertyCalcNodeModel(String propertyLabel,
			CalculatedPropertyInterface<T>[] possibleProps,
			ColumnFilter acceptedColumns, boolean checkZCoordsfor3D) {
		super(AbstractMoleculePropertyCalcNodeFactory.MOLECULE_COLUMN,
				propertyLabel, possibleProps, acceptedColumns);

		this.checkZCoordsfor3D = checkZCoordsfor3D;
	}

	/**
	 * Column Rearranger method to generate the output tables
	 * 
	 * @param inSpec
	 *            The incoming table spec
	 * @return A {@link ColumnRearranger} to generate the output table
	 * @throws InvalidSettingsException
	 *             if there is a problem with the configuration
	 */
	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec inSpec)
			throws InvalidSettingsException {

		hasNonZeroZ = false;
		return super.createColumnRearranger(inSpec);
	}

	/**
	 * Method to actually get the property cell factory. Subclasses may override
	 * if for example a post-calculation clean-up is required after each row
	 * 
	 * @param propMembers
	 *            The properties to calculate
	 * @param newColSpecs
	 *            The output column specs
	 * @return A PropertiesCellFactory
	 */
	protected PropertiesCellFactory getPropertyCellFactory(
			final List<CalculatedPropertyInterface<T>> propMembers,
			DataColumnSpec[] newColSpecs) {
		return new PropertiesCellFactory(newColSpecs, propMembers);
	}

	/**
	 * Method to check that a molecules maximum absolute Z coordinate is >
	 * {@link #MAX_ZERO_Z} ({@value #MAX_ZERO_Z}). Only called is
	 * {@link #checkZCoordsfor3D} and no molecule has yet been found to pass the
	 * test
	 * 
	 * @param mol
	 *            The molecule
	 * @return {@code true} if the molecule has any atom with an absolute value
	 *         of a z-coordinate > {@value #MAX_ZERO_Z}
	 */
	protected abstract boolean checkMolZCoord(T mol);

	/**
	 * Method to return the correct molecule object given the supplied
	 * {@link DataCell}. The node implementation guarantees that the molCell
	 * will not be a missing cell. Implementations should return {@code null} if
	 * the molecule could not be parsed into the correct format.
	 * 
	 * @param molCell
	 * @return
	 * @throws RowExecutionException
	 */
	protected abstract T getMolContainerFromCell(DataCell molCell)
			throws RowExecutionException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.nodes.propcalc.AbstractPropertyCalcNodeModel#
	 * getContainerObjFromCell(org.knime.core.data.DataCell)
	 */
	@Override
	protected final T getContainerObjFromCell(DataCell objCell)
			throws RowExecutionException {
		// We just delegate to the differently-named method to reflect the
		// specific purpose of the implementation
		return getMolContainerFromCell(objCell);
	}

	/**
	 * A {@link AbstractCellFactory} implementation which handles setting a
	 * warning message about z-coordinates if required after execution, and
	 * provides a handle for cleanup after row calculation if needed
	 * 
	 * @author s.roughley
	 *
	 */
	protected class PropertiesCellFactory extends AbstractCellFactory {

		private final List<CalculatedPropertyInterface<T>> propMembers;

		/**
		 * Constructor
		 * 
		 * @param colSpecs
		 *            the new column specs
		 * @param propMembers
		 *            Properties to calculate
		 */
		protected PropertiesCellFactory(DataColumnSpec[] colSpecs,
				List<CalculatedPropertyInterface<T>> propMembers) {
			super(colSpecs);
			this.propMembers = propMembers;
		}

		@Override
		public DataCell[] getCells(DataRow row) {
			DataCell[] retVal =
					ArrayUtils.fill(new DataCell[propMembers.size()],
							DataType.getMissingCell());

			DataCell molCell = row.getCell(colIdx);
			if (molCell.isMissing()) {
				return retVal;
			}
			T mol;
			try {
				mol = getContainerObjFromCell(molCell);
			} catch (RowExecutionException e1) {
				mol = null;
				getLogger().info("Unable to convert object in row '"
						+ row.getKey().getString() + "' - " + e1.getMessage());
			}

			if (mol != null) {
				if (checkZCoordsfor3D && !hasNonZeroZ) {
					hasNonZeroZ |= checkMolZCoord(mol);
				}
				for (int i = 0; i < propMembers.size(); i++) {
					try {
						retVal[i] = propMembers.get(i).calculate(mol);
					} catch (Exception e) {
						// We do nothing - the property was undefined for
						// the molecule
					}
				}
				doAfterPropertyCalc(mol);
			}
			return retVal;
		}

		/**
		 * This method is a handle to perform post-property calculation
		 * operations on non-null molecules
		 * 
		 * @param mol
		 *            The molecule - guarranteed non-null
		 */
		protected void doAfterPropertyCalc(T mol) {
			// Empty handle

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.knime.core.data.container.AbstractCellFactory#afterProcessing ()
		 */
		@Override
		public void afterProcessing() {
			if (checkZCoordsfor3D && !hasNonZeroZ) {
				setWarningMessage(
						"Node requires 3D structures, but no non-zero "
								+ "z-coordinates were found in any input molecules");
			}
			super.afterProcessing();
		}
	}

}
