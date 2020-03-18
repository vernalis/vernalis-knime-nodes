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
package com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.Conformation;
import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

import static com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter.RdkitRMSDFilterNodeDialog.MOL_LIST_FILTER;
import static com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter.RdkitRMSDFilterNodeDialog.createConformerPropertiesModels;
import static com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter.RdkitRMSDFilterNodeDialog.createFilterModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter.RdkitRMSDFilterNodeDialog.createIgnoreHsModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter.RdkitRMSDFilterNodeDialog.createMakeUnfilterablesMissingModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rmsd.rdkitfilter.RdkitRMSDFilterNodeDialog.createMolColumnNameModel;

/**
 * Node Model implementation for the 'RMSD Conformer List Filter' node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class RdkitRMSDFilterNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private SWIGObjectGarbageCollector2WaveSupplier gc =
			new SWIGObjectGarbageCollector2WaveSupplier();

	private final SettingsModelString m_molColName =
			registerSettingsModel(createMolColumnNameModel());
	private final SettingsModelColumnFilter2 confPropColsMdl =
			registerSettingsModel(createConformerPropertiesModels());
	private final SettingsModelBoolean replaceUnfilterableWithMissingMdl =
			registerSettingsModel(createMakeUnfilterablesMissingModel());
	private final SettingsModelDoubleBounded m_threshold =
			registerSettingsModel(createFilterModel());
	private final SettingsModelBoolean m_ignoreHs =
			registerSettingsModel(createIgnoreHsModel());

	/**
	 * Constructor
	 */
	public RdkitRMSDFilterNodeModel() {
		super();
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		final int molColIdx = getValidatedColumnSelectionModelColumnIndex(
				m_molColName, MOL_LIST_FILTER, spec, getLogger());

		final String[] includeNames =
				confPropColsMdl.applyTo(spec).getIncludes();
		int[] propColIdx = new int[includeNames.length + 1];
		propColIdx[0] = molColIdx;
		System.arraycopy(spec.columnsToIndices(includeNames), 0, propColIdx, 1,
				includeNames.length);

		CellFactory cellFact = new AbstractCellFactory(
				Arrays.stream(propColIdx).mapToObj(i -> spec.getColumnSpec(i))
						.toArray(DataColumnSpec[]::new)) {

			List<RowKey> cantFilterRows = new ArrayList<>();

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.knime.core.data.container.AbstractCellFactory#afterProcessing
			 * ()
			 */
			@Override
			public void afterProcessing() {
				gc.cleanupMarkedObjects();
				if (!cantFilterRows.isEmpty()) {
					setWarningMessage(
							"Unable to filter rows " + cantFilterRows.stream()
									.limit(5).map(r -> r.getString())
									.collect(Collectors.joining(
											", "))
									+ (cantFilterRows.size() > 5 ? ("... and "
											+ (cantFilterRows.size() - 5)
											+ " others") : "")
									+ " - see log for details");
				}
				super.afterProcessing();
			}

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell molCell = row.getCell(molColIdx);
				DataCell[] propCells = Arrays.stream(propColIdx).skip(1)
						.mapToObj(i -> row.getCell(i)).toArray(DataCell[]::new);
				DataCell[] retVal = new DataCell[propColIdx.length];
				if (molCell.isMissing()) {
					retVal[0] = molCell;
					System.arraycopy(propCells, 0, retVal, 1, propCells.length);
					return retVal;
				}

				Arrays.fill(retVal, DataType.getMissingCell());

				long waveId = gc.getNextWaveIndex();
				final ListDataValue molListValue = (ListDataValue) molCell;
				Conformation[] mols = molListValue.stream().map(c -> {
					try {
						return c.isMissing() ? null
								: RdkitCompatibleColumnFormats
										.getRDKitObjectFromCell(c);
					} catch (RowExecutionException e) {
						throw new RuntimeException(e);
					}
				}).map(mol -> mol == null ? null : new Conformation(mol))
						.peek(x -> gc.markForCleanup(x, waveId))
						.toArray(Conformation[]::new);
				int size = mols.length;
				BitSet keeps = filterConformers(mols);
				retVal[0] = CollectionCellFactory.createListCell(
						keeps.stream().mapToObj(i -> molListValue.get(i))
								.collect(Collectors.toList()));
				for (int i = 1; i < propColIdx.length; i++) {
					if (propCells[i - 1].isMissing()) {
						continue;
					}
					ListDataValue propListValue =
							(ListDataValue) propCells[i - 1];
					if (propListValue.size() != size) {
						cantFilterRows.add(row.getKey());

						getLogger().info("Unable to filter column '"
								+ spec.getColumnSpec(propColIdx[i]).getName()
								+ "' in row '" + row.getKey().getString()
								+ "' - wrong number of cells in list");
						retVal[i] = replaceUnfilterableWithMissingMdl
								.getBooleanValue() ? DataType.getMissingCell()
										: propCells[i - 1];
						continue;
					}
					retVal[i] = CollectionCellFactory.createListCell(
							keeps.stream().mapToObj(j -> propListValue.get(j))
									.collect(Collectors.toList()));
				}
				gc.cleanupMarkedObjects(waveId);
				return retVal;

			}
		};

		rearranger.replace(cellFact, propColIdx);

		return rearranger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel#
	 * reset()
	 */
	@Override
	protected void reset() {
		gc.quarantineAndCleanupMarkedObjects(30000);
		super.reset();
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

	/**
	 * @param mols
	 *            The incoming conformers for the current row
	 * @return A bitset indicating which are to be kept
	 */
	protected BitSet filterConformers(Conformation[] mols) {
		BitSet retVal = new BitSet(mols.length);

		nextConf: for (int i = 0; i < mols.length; i++) {
			// Loop through the conformers - some may be null of there were
			// missing cells in the collection
			if (mols[i] == null) {
				continue;
			}

			// Work back through the kept conformers comparing RMSD
			for (int j =
					retVal.length(); (j = retVal.previousSetBit(j - 1)) >= 0;) {
				if (!mols[i].checkRMSDThreshold(mols[j],
						m_threshold.getDoubleValue(),
						m_ignoreHs.getBooleanValue())) {
					// RMSD threshold violated in current comparison for current
					// conformer - go to next conformer
					break nextConf;
				}
			}
			// If we are here the conformer passed
			retVal.set(i);
		}
		return retVal;
	}

}
