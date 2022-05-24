/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.rdkit.nodes.abstrct;

import java.util.Map;

import org.RDKit.ROMol;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;

/**
 * The abstract node model class for internal webservices.
 * <p>
 * Subclasses need to provide the {@link NodeLogger} instance, and arrays
 * containing the new column name(s) and {@link DataType}s, along with
 * {@link #m_colFormats} - the acceptable input column formats.
 * </p>
 * <p>
 * The default {@link #configure(DataTableSpec[])} method checks the column type
 * is selected matching the allowed formats
 * </p>
 * <p>
 * The {@link #execute(BufferedDataTable[], ExecutionContext)} method generates
 * the output table using a column rearranger, which gets its results from the
 * {@link #getResultsFromRDKitObject(ROMol, long)} method, which needs to be
 * implemented.
 * </p>
 * <p>
 * During construction, the following must be initialised:
 * <ul>
 * <li>{@link #m_colFormats} - The acceptable column formats</li>
 * <li>{@link #m_newColNames} - The new column names</li>
 * <li>{@link #m_newColTypes} - The new column {@link DataType}s</li>
 * <li>{@link #m_logger} - The {@link NodeLogger} instance</li>
 * </ul>
 * 
 * @author Stephen Roughley knime@vernalis.com
 * 
 * @since v1.34.0
 * 
 */
public abstract class AbstractVerRDKitROMolColumnRearrangerNodeModel
		extends AbstractVerRDKitColumnRearrangerNodeModel<ROMol> {

	/**
	 * @param newColNames
	 * @param newColTypes
	 * @param colFormats
	 */
	protected AbstractVerRDKitROMolColumnRearrangerNodeModel(
			String[] newColNames, DataType[] newColTypes,
			ColumnFilter colFormats) {
		super(newColNames, newColTypes, colFormats);

	}

	/**
	 * @param newColNames
	 * @param newColTypes
	 * @param newColProps
	 * @param colFormats
	 * @param failOnError
	 */
	protected AbstractVerRDKitROMolColumnRearrangerNodeModel(
			String[] newColNames, DataType[] newColTypes,
			Map<String, Map<String, String>> newColProps,
			ColumnFilter colFormats, boolean failOnError) {
		super(newColNames, newColTypes, newColProps, colFormats, failOnError);

	}

	/**
	 * @param newColNames
	 * @param newColTypes
	 * @param newColProps
	 * @param colFormats
	 */
	protected AbstractVerRDKitROMolColumnRearrangerNodeModel(
			String[] newColNames, DataType[] newColTypes,
			Map<String, Map<String, String>> newColProps,
			ColumnFilter colFormats) {
		super(newColNames, newColTypes, newColProps, colFormats);

	}

	/**
	 * Returns an ROMol from a {@link DataCell} using the appropriate method for
	 * the cell type
	 * 
	 * @param cell
	 *            Input DataCell (should be RDKit, MOL, SDF or SMILES)
	 * @param wave
	 *            Wave index, should it be required for native object clean-up
	 *            (use {@link #getGC()} for garbage collector)
	 * 
	 * @return ROMol object containing the molecule
	 * 
	 * @throws RowExecutionException
	 *             Thrown if molecule parsing fails
	 * 
	 * @see #getChemicalReactionFromCell(DataCell)
	 */
	@Override
	protected ROMol getRDKitObjectFromCell(DataCell cell, long wave)
			throws RowExecutionException {

		return getGC().markForCleanup(
				RdkitCompatibleColumnFormats.getRDKitObjectFromCell(cell),
				wave);
	}

}
