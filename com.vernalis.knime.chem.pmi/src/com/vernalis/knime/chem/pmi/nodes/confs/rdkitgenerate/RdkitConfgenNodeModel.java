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
package com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.RDKit.AtomComparator;
import org.RDKit.BondComparator;
import org.RDKit.Conformer;
import org.RDKit.DistanceConstraintContrib;
import org.RDKit.DistanceGeom;
import org.RDKit.ForceField;
import org.RDKit.GenericRDKitException;
import org.RDKit.Int_Pair;
import org.RDKit.Int_Point3D_Map;
import org.RDKit.Int_Vect;
import org.RDKit.MCSResult;
import org.RDKit.Match_Vect;
import org.RDKit.MolSanitizeException;
import org.RDKit.Point3D;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.RWMol;
import org.RDKit.Transform3D;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;
import com.vernalis.knime.dialog.components.SettingsModelMultilineString;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createAddHsModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createAllowBondOrderMismatchesModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createAllowHeavyAtomMismatchesModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createBasicKnowledgeModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createCleanUpModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createColumnNameModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createCompleteRingsOnlyModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createEnergyFilterModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createExpTorsionModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createFilterByRMSDModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createFilterByTemplateRMSDModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createForceFieldModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createIgnoreHsRMSDModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createIterationsModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createMatchChiralTagsModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createMatchValencesModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createMaxEnergyModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createMaxTemplateRMSDModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createMinRMSDModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createNumConfsModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createNumTriesModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createOutputActualTemplateModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createOutputFormatModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createRandomSeedMdl;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createRemoveHsModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createRingMatchesRingOnlyModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createTemplateColNameModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createTemplateMolBlockModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createUseNRotModel;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.createUseTethersModel;

/**
 * NodeModel implementation for the 'Conformer Generation' node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class RdkitConfgenNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private static final String ACYCLIC_SINGLE_BOND_SMARTS = "-&!@";
	private static final String NROT = "NRot";
	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColumnNameModel());
	private final SettingsModelBoolean addHsMdl =
			registerSettingsModel(createAddHsModel());
	private final SettingsModelBoolean useNRotForNConfMdl =
			registerSettingsModel(createUseNRotModel());
	private final SettingsModelIntegerBounded numConfsMdl =
			registerSettingsModel(createNumConfsModel());
	private final SettingsModelIntegerBounded maxTriesMdl =
			registerSettingsModel(createNumTriesModel());
	private final SettingsModelBoolean uffCleanupMdl =
			registerSettingsModel(createCleanUpModel());
	private final SettingsModelString forceFieldMdl =
			registerSettingsModel(createForceFieldModel());
	private final SettingsModelIntegerBounded iterationsMdl =
			registerSettingsModel(createIterationsModel());
	private final SettingsModelBoolean filterByEnergyMdl =
			registerSettingsModel(createEnergyFilterModel());
	private final SettingsModelDouble energyMaxMdl =
			registerSettingsModel(createMaxEnergyModel());
	private final SettingsModelString outputFormatMdl =
			registerSettingsModel(createOutputFormatModel());
	private final SettingsModelBoolean removeHsMdl =
			registerSettingsModel(createRemoveHsModel());

	private final SettingsModelBoolean filterByRMSDMdl =
			registerSettingsModel(createFilterByRMSDModel());
	private final SettingsModelDoubleBounded minRMSDMdl =
			registerSettingsModel(createMinRMSDModel());
	private final SettingsModelBoolean ignoreHMdl =
			registerSettingsModel(createIgnoreHsRMSDModel());

	private final SettingsModelString templateColNameMdl =
			registerSettingsModel(createTemplateColNameModel());
	private final SettingsModelMultilineString templateMolBlockMdl =
			registerSettingsModel(createTemplateMolBlockModel());
	private final SettingsModelBoolean filterByTemplateRMDSMdl =
			registerSettingsModel(createFilterByTemplateRMSDModel());
	private final SettingsModelDoubleBounded maxTemplateRMSDMdl =
			registerSettingsModel(createMaxTemplateRMSDModel());

	private final SettingsModelBoolean outputActualTemplateMdl =
			registerSettingsModel(createOutputActualTemplateModel());
	private final SettingsModelBoolean useTethersMdl =
			registerSettingsModel(createUseTethersModel());

	private final SettingsModelBoolean useExpTorsionsMdl =
			registerSettingsModel(createExpTorsionModel());
	private final SettingsModelBoolean useBasicKnowledgeMdl =
			registerSettingsModel(createBasicKnowledgeModel());

	private final SettingsModelInteger randomSeedMdl =
			registerSettingsModel(createRandomSeedMdl());

	private final SettingsModelBoolean allowHeavyAtomMismatchesMdl =
			registerSettingsModel(createAllowHeavyAtomMismatchesModel());
	private final SettingsModelBoolean allowBondOrderMismatchesMdl =
			registerSettingsModel(createAllowBondOrderMismatchesModel());
	private final SettingsModelBoolean matchValencesMdl =
			registerSettingsModel(createMatchValencesModel());
	private final SettingsModelBoolean matchChiralTagsMdl =
			registerSettingsModel(createMatchChiralTagsModel());
	private final SettingsModelBoolean ringMatchesRingOnlyMdl =
			registerSettingsModel(createRingMatchesRingOnlyModel());
	private final SettingsModelBoolean completeRingsOnlyMdl =
			registerSettingsModel(createCompleteRingsOnlyModel());

	private final SWIGObjectGarbageCollector2WaveSupplier gc =
			new SWIGObjectGarbageCollector2WaveSupplier();

	private AtomComparator templateAtomComparator = null;
	private BondComparator templateBondComparator = null;

	/**
	 * Node Model constructor
	 */
	public RdkitConfgenNodeModel() {
		super();
		numConfsMdl.setEnabled(!useNRotForNConfMdl.getBooleanValue());
		useNRotForNConfMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				numConfsMdl.setEnabled(!useNRotForNConfMdl.getBooleanValue());
			}
		});

		energyMaxMdl.setEnabled(filterByEnergyMdl.getBooleanValue());
		filterByEnergyMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				energyMaxMdl.setEnabled(filterByEnergyMdl.getBooleanValue());

			}
		});

		templateColNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});
		updateTemplateModels();
		templateMolBlockMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});
		filterByTemplateRMDSMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTemplateModels();

			}
		});

		filterByRMSDMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateRMSDModels();
			}
		});
		updateRMSDModels();
	}

	private final void updateTemplateModels() {
		templateMolBlockMdl
				.setEnabled(templateColNameMdl.getStringValue() == null
						|| templateColNameMdl.getStringValue().isEmpty());
		boolean hasTemplate = (templateColNameMdl.getStringValue() != null
				&& !templateColNameMdl.getStringValue().isEmpty())
				|| (templateMolBlockMdl.getStringValue() != null
						&& !templateMolBlockMdl.getStringValue().isEmpty());
		outputActualTemplateMdl.setEnabled(hasTemplate);
		filterByTemplateRMDSMdl.setEnabled(hasTemplate);
		maxTemplateRMSDMdl.setEnabled(
				hasTemplate && filterByTemplateRMDSMdl.getBooleanValue());
		useTethersMdl.setEnabled(hasTemplate);
		allowHeavyAtomMismatchesMdl.setEnabled(hasTemplate);
		allowBondOrderMismatchesMdl.setEnabled(hasTemplate);
		completeRingsOnlyMdl.setEnabled(hasTemplate);
		ringMatchesRingOnlyMdl.setEnabled(hasTemplate);
		matchChiralTagsMdl.setEnabled(hasTemplate);
		matchValencesMdl.setEnabled(hasTemplate);
	}

	/**
	 * 
	 */
	private final void updateRMSDModels() {
		minRMSDMdl.setEnabled(filterByRMSDMdl.getBooleanValue());
		ignoreHMdl.setEnabled(filterByRMSDMdl.getBooleanValue());
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		final int molColIdx = getValidatedColumnSelectionModelColumnIndex(
				colNameMdl, RdkitCompatibleColumnFormats.MOL_ANY, spec,
				getLogger());
		ConformerOutputFormats outputFormat;
		try {
			outputFormat = ConformerOutputFormats
					.valueOf(outputFormatMdl.getStringValue());
		} catch (Exception e) {
			throw new InvalidSettingsException("Invalid output format - '"
					+ outputFormatMdl.getStringValue() + "'", e);
		}

		ForceFieldFactory forceField;
		try {
			forceField =
					ForceFieldFactory.valueOf(forceFieldMdl.getStringValue());
		} catch (Exception e) {
			throw new InvalidSettingsException("Invalid forcefield - '"
					+ forceFieldMdl.getStringValue() + "'", e);
		}

		if (templateColNameMdl.getStringValue() != null
				&& !templateColNameMdl.getStringValue().isEmpty()) {
			// We have a template column, check it exists, is of the correct
			// type, and is not the same column as the molecules to generate
			// conformers of
			DataColumnSpec templateColSpec =
					spec.getColumnSpec(templateColNameMdl.getStringValue());
			if (templateColSpec == null) {
				// Not in the table
				throw new InvalidSettingsException(
						"The selected template column '"
								+ templateColNameMdl.getStringValue()
								+ "' is no longer present in the incoming table");
			}
			if (!RdkitCompatibleColumnFormats.MOL_FROM_MOLBLOCK
					.includeColumn(templateColSpec)) {
				// Not the correct type
				throw new InvalidSettingsException(
						"The selected template column '"
								+ templateColNameMdl.getStringValue()
								+ "' is not of the correct type (Mol or Sdf)");
			}
			if (templateColNameMdl.getStringValue()
					.equals(colNameMdl.getStringValue())) {
				throw new InvalidSettingsException(
						"The same column cannot be selected for input molecules and templates ("
								+ templateColNameMdl.getStringValue() + ")");
			}
		}
		int templateColIdx =
				spec.findColumnIndex(templateColNameMdl.getStringValue());

		final ROMol globalTemplateMol;
		final boolean hasTemplate = hasTemplate();
		if (hasTemplate && templateColIdx < 0) {
			try {
				globalTemplateMol = gc.markForCleanup(RWMol.MolFromMolBlock(
						templateMolBlockMdl.getStringValue(), true, false));
			} catch (Exception e) {
				gc.cleanupMarkedObjects();
				throw new InvalidSettingsException(
						"Problem getting global template", e);
			}
			if (globalTemplateMol == null) {
				throw new InvalidSettingsException(
						"Problem getting global template");
			}
			if (globalTemplateMol.getNumConformers() == 0) {
				gc.cleanupMarkedObjects();
				throw new InvalidSettingsException(
						"The template structure has no coordinates");
			}
		} else {
			// No global template
			globalTemplateMol = null;
		}

		if (hasTemplate) {
			templateAtomComparator = allowHeavyAtomMismatchesMdl.isEnabled()
					&& allowHeavyAtomMismatchesMdl.getBooleanValue()
							? AtomComparator.AtomCompareAnyHeavyAtom
							: AtomComparator.AtomCompareElements;
			templateBondComparator = allowBondOrderMismatchesMdl.isEnabled()
					&& allowBondOrderMismatchesMdl.getBooleanValue()
							? BondComparator.BondCompareAny
							: BondComparator.BondCompareOrder;
		} else {
			templateAtomComparator = null;
			templateBondComparator = null;
		}
		final int nConfs = useNRotForNConfMdl.getBooleanValue() ? -1
				: numConfsMdl.getIntValue();

		ColumnRearranger rearranger = new ColumnRearranger(spec);

		// New columns
		List<DataColumnSpec> newColSpecs = new ArrayList<>();
		if (useNRotForNConfMdl.getBooleanValue()) {
			newColSpecs.add(new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, NROT), IntCell.TYPE)
							.createSpec());
		}
		newColSpecs.add(new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec, "Conformers"),
				ListCell.getCollectionType(outputFormat.getCellType()))
						.createSpec());
		newColSpecs.add(new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						forceFieldMdl.getStringValue()
								+ " Absolute Conformer Energies"),
				ListCell.getCollectionType(DoubleCell.TYPE)).createSpec());
		newColSpecs.add(new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec,
						forceFieldMdl.getStringValue()
								+ " Relative Conformer Energies"),
				ListCell.getCollectionType(DoubleCell.TYPE)).createSpec());
		if (hasTemplate) {
			if (outputActualTemplateMdl.getBooleanValue()) {
				newColSpecs.add(new DataColumnSpecCreator(
						DataTableSpec.getUniqueColumnName(spec,
								"Actual Row Template"),
						outputFormat.getCellType()).createSpec());
			}
			newColSpecs.add(new DataColumnSpecCreator(
					DataTableSpec.getUniqueColumnName(spec, "Template RMSDs"),
					ListCell.getCollectionType(DoubleCell.TYPE)).createSpec());
		}

		rearranger.append(new AbstractCellFactory(true,
				newColSpecs.toArray(new DataColumnSpec[newColSpecs.size()])) {

			@Override
			public DataCell[] getCells(DataRow row) {

				// Firstly, we handle the input molecule for the row
				DataCell molCell = row.getCell(molColIdx);
				if (molCell.isMissing()) {
					return ArrayUtils.fill(new DataCell[newColSpecs.size()],
							DataType.getMissingCell());
				}
				long waveID = gc.getNextWaveIndex();
				ROMol mol;
				try {
					mol = gc.markForCleanup(RdkitCompatibleColumnFormats
							.getRDKitObjectFromCell(molCell), waveID);
				} catch (RowExecutionException e) {
					gc.cleanupMarkedObjects(waveID);
					return ArrayUtils.fill(new DataCell[newColSpecs.size()],
							DataType.getMissingCell());
				}

				assert mol != null;
				if (addHsMdl.getBooleanValue()) {
					mol = gc.markForCleanup(mol.addHs(false, false), waveID);
				}

				// Now we generate a template molecule based on the settings
				// If there is a template, then
				ROMol rowBaseTemplateMol = getRowBaseTemplate(templateColIdx,
						globalTemplateMol, row, waveID);
				ROMol rowTemplateMol =
						getRowTemplate(row, waveID, mol, rowBaseTemplateMol);

				int numRotBond = (int) (useNRotForNConfMdl.getBooleanValue()
						? RDKFuncs.calcNumRotatableBonds(mol)
						: -1);
				double maxTemplateRMSD = rowTemplateMol == null
						|| !filterByTemplateRMDSMdl.getBooleanValue()
								? Double.NaN
								: maxTemplateRMSDMdl.getDoubleValue();

				try {
					SortedSet<Conformation> confs = generateConformers(mol,
							rowTemplateMol/* Template */, numRotBond, nConfs,
							maxTriesMdl.getIntValue(),
							useTethersMdl.isEnabled()
									&& useTethersMdl.getBooleanValue(),
							useExpTorsionsMdl.getBooleanValue(),
							useBasicKnowledgeMdl.getBooleanValue(), forceField,
							uffCleanupMdl.getBooleanValue(),
							iterationsMdl.getIntValue(), waveID,
							maxTemplateRMSD);
					if (confs.isEmpty()) {
						return ArrayUtils.fill(new DataCell[newColSpecs.size()],
								DataType.getMissingCell());
					}
					List<DataCell> newCells = new ArrayList<>();
					if (useNRotForNConfMdl.getBooleanValue()) {
						newCells.add(new IntCell(numRotBond));
					}
					double eMin = confs.stream().findFirst().get()
							.getConformerEnergy();
					List<DataCell> confCells = new ArrayList<>();
					List<DataCell> absECells = new ArrayList<>();
					List<DataCell> relECells = new ArrayList<>();
					List<DataCell> rmsdCells =
							hasTemplate ? new ArrayList<>() : null;

					for (Conformation conf : confs) {
						double dE = conf.getConformerEnergy() - eMin;
						if (filterByEnergyMdl.getBooleanValue()
								&& Double.compare(dE,
										energyMaxMdl.getDoubleValue()) > 0) {
							// Ignore remaining conformers
							break;
						}

						confCells.add(outputFormat.getConformerCell(conf,
								removeHsMdl.getBooleanValue()));
						absECells
								.add(new DoubleCell(conf.getConformerEnergy()));
						relECells.add(new DoubleCell(dE));
						if (rmsdCells != null) {
							rmsdCells.add(
									new DoubleCell(conf.getTemplateRMSD()));
						}
					}

					newCells.add(
							CollectionCellFactory.createListCell(confCells));
					newCells.add(
							CollectionCellFactory.createListCell(absECells));
					newCells.add(
							CollectionCellFactory.createListCell(relECells));

					if (hasTemplate) {
						if (outputActualTemplateMdl.getBooleanValue()) {
							newCells.add(outputFormat.getTemplateCell(
									confs.stream().findFirst().get()));
						}
						newCells.add(CollectionCellFactory
								.createListCell(rmsdCells));
					}
					return newCells.toArray(new DataCell[newCells.size()]);
				} catch (MolSanitizeException e) {
					throw new RuntimeException(e.what(), e);
				} catch (GenericRDKitException e) {
					throw new RuntimeException(e.what(), e);
				} finally {
					gc.cleanupMarkedObjects(waveID);
				}

			}

			/*
			 * (non-Javadoc)
			 *
			 * @see
			 *
			 * org.knime.core.data.container.AbstractCellFactory#afterProcessing
			 * ()
			 */
			@Override
			public void afterProcessing() {
				// quarantine in case user cancels which bombs JVM
				gc.quarantineAndCleanupMarkedObjects();
				super.afterProcessing();
			}
		});
		return rearranger;
	}

	/**
	 * @return The actual row template, which may be the base row template or an
	 *         MCS of the base row template and the incoming molecule
	 */
	private ROMol getRowTemplate(DataRow row, long waveID, ROMol mol,
			ROMol rowBaseTemplateMol) {
		ROMol rowTemplateMol;
		if (rowBaseTemplateMol == null) {
			rowTemplateMol = null;
		} else {
			if (mol.hasSubstructMatch(rowBaseTemplateMol)) {
				rowTemplateMol = rowBaseTemplateMol;
			} else {
				ROMol_Vect molVect =
						gc.markForCleanup(new ROMol_Vect(), waveID);
				molVect.add(mol);
				molVect.add(rowBaseTemplateMol);

				MCSResult mcsResult = gc.markForCleanup(RDKFuncs.findMCS(
						molVect, true /* maximise bonds */,
						1.0/*
							 * threshold - we want it to cover both template and
							 * molecule
							 */, 3600/* Timeout */, false/* verbose */,
						matchValencesMdl.getBooleanValue() /* match valences */,
						ringMatchesRingOnlyMdl
								.getBooleanValue()/* Ring matches ring only */,
						completeRingsOnlyMdl
								.getBooleanValue()/* complete rings only */,
						matchChiralTagsMdl
								.getBooleanValue()/* match chiral tag */,
						templateAtomComparator, templateBondComparator),
						waveID);
				RWMol mcsResultMatch = gc.markForCleanup(
						RWMol.MolFromSmarts(mcsResult.getSmartsString()),
						waveID);

				getLogger().debugWithFormat("Template MCS for row '%s':\t%s",
						row.getKey(), mcsResult.getSmartsString());
				// This is crude but best we can do for NRot on a query
				// molecule...
				mcsResultMatch.setProp(NROT, String.format("%d",
						(mcsResult.getSmartsString().length()
								- mcsResult.getSmartsString()
										.replace(ACYCLIC_SINGLE_BOND_SMARTS, "")
										.length())
								/ ACYCLIC_SINGLE_BOND_SMARTS.length()));

				// A new conformer to paint the template coordinates
				// onto the MCS Match, as this may have query atoms
				Conformer mcsConf = gc.markForCleanup(new Conformer(), waveID);
				Conformer rowTemplateConf = gc.markForCleanup(
						rowBaseTemplateMol.getConformer(), waveID);

				Match_Vect match = gc.markForCleanup(
						rowBaseTemplateMol.getSubstructMatch(mcsResultMatch),
						waveID);
				for (int i = 0; i < match.size(); i++) {
					mcsConf.setAtomPos(match.get(i).getFirst(), rowTemplateConf
							.getAtomPos(match.get(i).getSecond()));
				}

				mcsResultMatch.addConformer(mcsConf);
				rowTemplateMol =
						gc.markForCleanup(new ROMol(mcsResultMatch), waveID);
			}
			if (rowTemplateMol.getNumConformers() == 0) {
				getLogger().warn("No coordinates found for template - "
						+ row.getKey().getString());
			}
		}
		return rowTemplateMol;
	}

	/**
	 * Method to generate the base template for the row, before any SS-matching
	 * is tried
	 */
	private ROMol getRowBaseTemplate(int templateColIdx,
			final ROMol globalTemplateMol, DataRow row, long waveID) {
		ROMol rowBaseTemplateMol;
		if (templateColIdx > 0) {
			DataCell templateCell = row.getCell(templateColIdx);
			if (templateCell.isMissing()) {
				rowBaseTemplateMol = null;
			} else {
				try {
					rowBaseTemplateMol =
							gc.markForCleanup(RdkitCompatibleColumnFormats
									.getRDKitObjectFromCell(templateCell),
									waveID);
				} catch (RowExecutionException e) {
					rowBaseTemplateMol = null;
					setWarningMessage(
							"Unable to create template molecule for row: "
									+ row.getKey().getString());
				}
			}
		} else {
			rowBaseTemplateMol = globalTemplateMol;
		}
		return rowBaseTemplateMol;
	}

	/**
	 * Generates the required number of conformers applying the requisite
	 * options
	 * 
	 * @param mol
	 *            The input molecule
	 * @param nRotForNConf
	 *            The number of rotatable bonds in order to determine the number
	 *            of conformers to generate. A negative value or
	 *            {@code Double.NaN} indicates that {@code nConfs} should be
	 *            used
	 * @param nConfs
	 *            The number of conformers to generate if nRotForNConf is
	 *            negative
	 * @param nTries
	 *            The number of attempts to generate a conformer
	 * @param useExperimentalTorsions
	 *            Should experimental torsions be used
	 * @param useBasicKnowledge
	 *            Should basic knowledge (e.g. planar rings) be used
	 * @param forceField
	 *            The name of the forcefield for geometry optimisation
	 * @param uffCleanup
	 *            Should UFF be used as a fall back option for non-parameterised
	 *            molecules?
	 * @param iterations
	 *            The number of iterations to attempt for convergence of
	 *            geometry optimisation
	 * @param waveId
	 *            The wave index for garbage collection
	 * @param maxTemplateRMSD
	 *            TODO
	 * @return A set of Conformations, sorted in increasing order of energy
	 */
	private SortedSet<Conformation> generateConformers(ROMol mol,
			ROMol template, int nRotForNConf, int nConfs, int nTries,
			boolean useTethers, boolean useExperimentalTorsions,
			boolean useBasicKnowledge, ForceFieldFactory forceField,
			boolean uffCleanup, int iterations, long waveId,
			double maxTemplateRMSD) {

		NavigableSet<Conformation> retVal = new TreeSet<>();

		if (mol == null || mol.getNumAtoms() == 0) {
			return retVal;
		}

		if (mol.getNumConformers() > 0) {
			// Handle incoming molecules which have conformers already, which
			// causes issues with adding a conformer later
			mol.clearConformers();
		}

		ROMol molConfs = gc.markForCleanup(new ROMol(mol), waveId);
		Match_Vect match, algMap;
		Conformer templateConf;
		Int_Point3D_Map atomConstraints;
		if (template != null) {
			match = gc.markForCleanup(mol.getSubstructMatch(template), waveId);
			templateConf = gc.markForCleanup(template.getConformer(), waveId);

			atomConstraints = gc.markForCleanup(new Int_Point3D_Map(), waveId);
			algMap = gc.markForCleanup(new Match_Vect(), waveId);
			for (int i = 0; i < match.size(); i++) {
				Int_Pair m = match.get(i);
				atomConstraints.set(m.getSecond(),
						templateConf.getAtomPos(m.getFirst()));
				algMap.add(gc.markForCleanup(
						new Int_Pair(m.getSecond(), m.getFirst()), waveId));
			}
		} else {
			match = null;
			templateConf = null;
			atomConstraints = null;
			algMap = null;
		}

		long templateNrot =
				template == null ? 0 : Long.parseLong(template.getProp(NROT));
		int numConfs =
				calculateNumConfs(nRotForNConf, nConfs, (int) templateNrot);

		// TODO: Simple quick-mapping here when template atom count == mol atom
		// count

		// See
		// http://www.rdkit.org/Python_Docs/rdkit.Chem.rdDistGeom-module.html
		// for arguments descriptions
		Int_Vect confIds = gc.markForCleanup(DistanceGeom.EmbedMultipleConfs(
				molConfs, numConfs, nTries,
				randomSeedMdl.getIntValue() /* Random seed - for testing! */,
				true /* clear conformers */, false /* Use random coords */,
				2.0/* box size multiplier */, true /* rand neg eigen */,
				1 /* num zero fail */, -1.0 /* Prune RMS Threshold */,
				atomConstraints /* Fixed coords map */,
				0.001 /* Dist geom forcefield tolerance */ ,
				false /* Ignore smoothing failures */ ,
				true/* Preserve chirality */, useExperimentalTorsions,
				useBasicKnowledge), waveId);

		// Now we have to create multiple copies, each with only one conformer,
		// and optimize the conformer geometry
		for (int i = 0; i < confIds.size(); i++) {
			ROMol tmp = gc.markForCleanup(new ROMol(mol), waveId);
			Conformer conf = gc.markForCleanup(
					new Conformer(molConfs.getConformer(confIds.get(i))),
					waveId);
			tmp.addConformer(conf,
					true/* Assign ID - so always becomes confID 0 */);
			ForceField ff = gc.markForCleanup(getForceField(useTethers,
					forceField, uffCleanup, waveId, match, templateConf, tmp),
					waveId);
			ff.initialize();
			if (iterations > 0 && ff.minimize(iterations) != 0) {
				// didnt minimise - skip
				continue;
			}

			final Conformation conformation;
			if (template == null) {
				conformation = new Conformation(tmp, ff.calcEnergy());
			} else {
				final Transform3D transform3d = new Transform3D();
				final double templateRMSD = tmp.getAlignmentTransform(template,
						transform3d, -1, -1, algMap);
				transform3d.delete();

				if (Double.isNaN(maxTemplateRMSD) || maxTemplateRMSD < 0.0
						|| templateRMSD <= maxTemplateRMSD) {
					conformation = new Conformation(tmp, template, templateRMSD,
							ff.calcEnergy());
				} else {
					// Dont process this conformation - it fails the
					// template RMSD match filter
					continue;
				}
			}
			gc.markForCleanup(conformation, waveId);

			if (!filterByRMSDMdl.getBooleanValue()) {
				// No RMSD Filter, so just add the conformer and continue to
				// the next one..
				retVal.add(conformation);
				continue;
			}

			// Check the inter-conformer RMSD filter
			if (checkRMSD(conformation, retVal, minRMSDMdl.getDoubleValue(),
					ignoreHMdl.getBooleanValue())) {
				retVal.add(conformation);
			}

		}

		return retVal;
	}

	/**
	 * @return {@code true} if the new conformation RMSD to all existing
	 *         retained conformers is > the RMSD threshold
	 */
	private boolean checkRMSD(Conformation conformation,
			NavigableSet<Conformation> retVal, double rmsdThreshold,
			boolean ignoreHs) {
		Iterator<Conformation> iter = retVal.descendingIterator();
		while (iter.hasNext()) {
			if (!conformation.checkRMSDThreshold(iter.next(), rmsdThreshold,
					ignoreHs)) {
				return false;
			}
		}
		// Passed - keep the molecule
		return true;
	}

	/**
	 * @return The forcefield for the molecule
	 */
	private ForceField getForceField(boolean useTethers,
			ForceFieldFactory forceField, boolean uffCleanup, long waveId,
			Match_Vect match, Conformer templateConf, ROMol tmp) {
		ForceField ff = forceField.getForceField(tmp, uffCleanup);
		if (templateConf != null) {
			// We need to align the conformer to the template
			if (useTethers) {
				// Rotate onto the core
				for (int j = 0; j < templateConf.getNumAtoms(); j++) {
					Point3D p = templateConf.getAtomPos(j);
					Point3D p1 = gc.markForCleanup(
							new Point3D(p.getX(), p.getY(), p.getZ()), waveId);
					ff.positions3D().add(p1);
					long pIdx = ff.positions3D().size() - 1;
					ff.fixedPoints().add((int) pIdx);
					ff.contribs().add(new DistanceConstraintContrib(ff, pIdx,
							match.get(j).getSecond(), 0, 0, 100.0));
				}
			} else {
				// Add distance constraints between each pair of atoms matching
				// those in the template
				for (int j = 0; j < match.size(); j++) {
					Int_Pair m = match.get(j);
					for (int k = m.getFirst() + 1; k < match.size(); k++) {
						int idxK = match.get(k).getSecond();
						double d =
								gc.markForCleanup(
										templateConf.getAtomPos(m.getFirst())
												.minus(templateConf
														.getAtomPos(idxK)),
										waveId).length();
						ff.contribs().add(new DistanceConstraintContrib(ff,
								m.getFirst(), idxK, d, d, 100.0));
					}
				}
			}
		}
		return ff;
	}

	/**
	 * @return The number of conformers based on the user settings, and if using
	 *         the auto-selection based on NRot, the apparent rotatable bonds
	 *         count (after accounting for any template)
	 */
	private int calculateNumConfs(int nRotForNConf, int nConfs,
			int templateNRot) {
		int numConfs;
		if (nRotForNConf >= 0) {
			int effectiveNRot =
					nRotForNConf - (templateNRot > 0 ? templateNRot : 0);
			if (effectiveNRot < 8) {
				numConfs = 50;
			} else if (effectiveNRot < 13) {
				numConfs = 200;
			} else {
				numConfs = 300;
			}
		} else {
			numConfs = nConfs;
		}
		return numConfs;
	}

	/**
	 * @return Whether the node settings provide a template (NB does not
	 *         indicate whether a given row will actually have a template)
	 */
	private final boolean hasTemplate() {
		return (templateColNameMdl.getStringValue() != null
				&& !templateColNameMdl.getStringValue().isEmpty())
				|| (templateMolBlockMdl.getStringValue() != null
						&& !templateMolBlockMdl.getStringValue().trim()
								.isEmpty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#reset()
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
		gc.quarantineAndCleanupMarkedObjects();
		super.onDispose();
	}

}
