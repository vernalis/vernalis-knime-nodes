/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.knime.chem.pmi.nodes.pmi2;

import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createNormalisedPmisModel;
import static com.vernalis.knime.chem.pmi.util.misc.PmiUtils.createPmisModel;

import java.util.List;

import org.RDKit.ROMol;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.vernalis.knime.chem.pmi.nodes.rdkit.abstrct.AbstractVerRDKitRearrangerNodeModel;
import com.vernalis.knime.chem.pmi.util.misc.PmiUtils;

/**
 * NodeModel implementation to calculate Principle Moments of Inertia (PMI) and
 * derived properties
 * 
 * @author Vernalis
 */
public class Pmi2NodeModel extends AbstractVerRDKitRearrangerNodeModel {

	protected static final String NPMIS = "nPMIs";
	protected static final String PMIS = "PMIs";

	/**
	 * Constructor for the node model.
	 */
	protected Pmi2NodeModel() {
		super(1, 1);
		registerSettingsModel(PMIS, createPmisModel());
		registerSettingsModel(NPMIS, createNormalisedPmisModel());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		resetResultColumns();
		if (((SettingsModelBoolean) getSettingsModel(PMIS)).getBooleanValue()) {
			registerResultColumn("I1", DoubleCell.TYPE);
			registerResultColumn("I2", DoubleCell.TYPE);
			registerResultColumn("I3", DoubleCell.TYPE);
		}
		if (((SettingsModelBoolean) getSettingsModel(NPMIS)).getBooleanValue()) {
			registerResultColumn("I1 / I3", DoubleCell.TYPE);
			registerResultColumn("I2 / I3", DoubleCell.TYPE);
		}
		return super.configure(inSpecs);
	}

	@Override
	protected void calculateResultCells(DataCell[] resultCells, ROMol mol) {
		List<Double> PMIs = PmiUtils.getPMIs(mol);
		int colIdx = 0;
		if (((SettingsModelBoolean) getSettingsModel(PMIS)).getBooleanValue()) {
			resultCells[colIdx++] = new DoubleCell(PMIs.get(0));
			resultCells[colIdx++] = new DoubleCell(PMIs.get(1));
			resultCells[colIdx++] = new DoubleCell(PMIs.get(2));
		}
		if (((SettingsModelBoolean) getSettingsModel(NPMIS)).getBooleanValue()) {
			resultCells[colIdx++] = new DoubleCell(PMIs.get(0) / PMIs.get(2));
			resultCells[colIdx++] = new DoubleCell(PMIs.get(1) / PMIs.get(2));
		}
	}

}
