/*******************************************************************************
 * Copyright (c) 2014, 2015, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.chem.pmi.nodes.transform.toprincaxes;

import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createRemoveInputColModel;

import org.RDKit.ROMol;
import org.knime.chem.types.MolCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.vernalis.knime.chem.pmi.nodes.rdkit.abstrct.AbstractVerRDKitRearrangerNodeModel;
import com.vernalis.knime.chem.pmi.util.misc.PmiUtils;

/**
 * This is the model implementation of MolPreProc. Node to convert an sd-file
 * column to a scaffold according to the PMI plots options
 * 
 * @author S.Roughley
 */
public class AlignToPrincipalAxesNodeModel extends AbstractVerRDKitRearrangerNodeModel {

	/**
	 * Constructor for the node model.
	 */
	protected AlignToPrincipalAxesNodeModel() {
		super(1, 1);
		registerSettingsModel(REMOVE_INPUT_COL, createRemoveInputColModel());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		DataTableSpec inSpec = inData[0].getDataTableSpec();
		ColumnRearranger rearranger = createColumnRearranger(inSpec);
		BufferedDataTable outTable = exec.createColumnRearrangeTable(inData[0], rearranger, exec);

		return new BufferedDataTable[] { outTable };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		resetResultColumns();
		registerResultColumn("Aligned molecule", MolCellFactory.TYPE);

		return super.configure(inSpecs);
	}

	@Override
	protected void calculateResultCells(DataCell[] resultCells, ROMol mol) {
		PmiUtils.alignToPrincipalAxes(mol);
		resultCells[0] = MolCellFactory.createAdapterCell(mol.MolToMolBlock());
	}

}
