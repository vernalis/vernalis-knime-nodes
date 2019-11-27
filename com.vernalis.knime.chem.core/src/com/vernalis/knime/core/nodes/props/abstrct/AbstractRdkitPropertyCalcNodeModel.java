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

import org.RDKit.Conformer;
import org.RDKit.ROMol;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;
import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

/**
 * An abstract NodeModel property calculation implementation when an RDKit
 * {@link ROMol} object is required to calculate the property. Handles garbage
 * collection of the native RDKit objects.
 * 
 * @author s.roughley
 * 
 */
public class AbstractRdkitPropertyCalcNodeModel
		extends AbstractMoleculePropertyCalcNodeModel<ROMol> {

	private static final String GC_WAVE = "GC_WAVE";
	protected final SWIGObjectGarbageCollector2WaveSupplier gc =
			new SWIGObjectGarbageCollector2WaveSupplier();

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
	protected AbstractRdkitPropertyCalcNodeModel(String propertyLabel,
			CalculatedPropertyInterface<ROMol>[] possibleProps,
			ColumnFilter acceptedColumnsFilter) {
		this(propertyLabel, possibleProps, acceptedColumnsFilter, false);

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
	protected AbstractRdkitPropertyCalcNodeModel(String propertyLabel,
			CalculatedPropertyInterface<ROMol>[] possibleProps,
			ColumnFilter acceptedColumnsFilter, boolean checkZCoordsfor3D) {
		super(propertyLabel, possibleProps, acceptedColumnsFilter,
				checkZCoordsfor3D);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.core.nodes.props.abstrct.
	 * AbstractMoleculePropertyCalcNodeModel#getPropertyCellFactory(java.util.
	 * List, org.knime.core.data.DataColumnSpec[])
	 */
	@Override
	protected PropertiesCellFactory getPropertyCellFactory(
			List<CalculatedPropertyInterface<ROMol>> propMembers,
			DataColumnSpec[] newColSpecs) {

		return new PropertiesCellFactory(newColSpecs, propMembers) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.core.nodes.props.abstrct.
			 * AbstractMoleculePropertyCalcNodeModel.PropertiesCellFactory#
			 * afterProcessing()
			 */
			@Override
			public void afterProcessing() {
				gc.quarantineAndCleanupMarkedObjects();
				super.afterProcessing();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.core.nodes.props.abstrct.
			 * AbstractMoleculePropertyCalcNodeModel.PropertiesCellFactory#
			 * doAfterPropertyCalc(java.lang.Object)
			 */
			@Override
			protected void doAfterPropertyCalc(ROMol mol) {
				gc.cleanupMarkedObjects(Long.parseLong(mol.getProp(GC_WAVE)));
				super.doAfterPropertyCalc(mol);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.core.nodes.props.abstrct.
	 * AbstractMoleculePropertyCalcNodeModel
	 * #getMolFromCell(org.knime.core.data.DataCell)
	 */
	@Override
	protected ROMol getMolContainerFromCell(DataCell molCell)
			throws RowExecutionException {
		final ROMol mol =
				RdkitCompatibleColumnFormats.getRDKitObjectFromCell(molCell);
		long waveId = gc.getNextWaveIndex();
		mol.setProp(GC_WAVE, "" + waveId);
		return gc.markForCleanup(mol, waveId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.core.nodes.props.abstrct.
	 * AbstractMoleculePropertyCalcNodeModel#reset()
	 */
	@Override
	protected void reset() {
		gc.quarantineAndCleanupMarkedObjects();
		super.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		gc.cleanupMarkedObjects();
		super.onDispose();
	}

	@Override
	protected boolean checkMolZCoord(ROMol mol) {
		long wave = Long.parseLong(mol.getProp(GC_WAVE));
		Conformer conf = gc.markForCleanup(mol.getConformer(), wave);
		if (conf == null) {
			return false;
		}
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			if (Math.abs(gc.markForCleanup(conf.getAtomPos(i), wave)
					.getZ()) > MAX_ZERO_Z) {
				return true;
			}
		}
		return false;
	}

}
