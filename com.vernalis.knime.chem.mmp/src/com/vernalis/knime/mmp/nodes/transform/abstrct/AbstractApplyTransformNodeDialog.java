/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.transform.abstrct;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.nodes.pairgen.abstrct.AttachmentPointFingerprintComparisonType2;
import com.vernalis.knime.mmp.nodes.pairgen.abstrct.AttachmentPointFingerprintSimilarityType;
import com.vernalis.knime.mmp.transform.TransformUtilityFactory;

/**
 * The node dialog for the apply transforms nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the query molecule object
 * @param <V>
 *            The type of the reaction/transform object
 */
public class AbstractApplyTransformNodeDialog<T, U, V> extends DefaultNodeSettingsPane {

	TransformUtilityFactory<T, U, V> utilFactory;
	private final SettingsModelDoubleBounded alphaMdl, betaMdl;
	private final SettingsModelString similarityType;
	private final SettingsModelString firstFPColNameMdl;
	private final SettingsModelBoolean filterByKeySimilarityMdl;
	private final SettingsModelDoubleBounded similarityThresholdMdl;
	private final SettingsModelString comparisonTypeMdl;
	/**
	 * Column filter for 'Attachment point 1 fingerprint (L)' columns
	 */
	static final ColumnFilter AP1_COLUMN_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return colSpec.getType() == DenseBitVectorCell.TYPE
					&& colSpec.getName().matches("Attachment point 1 fingerprint.*(L).*");
		}

		@Override
		public String allFilteredMsg() {
			return "No Key Attachment Point fingerprints found";
		}
	};

	/**
	 * Constructor
	 * 
	 * @param utilFactory
	 *            The {@link TransformUtilityFactory} instance for the node
	 */
	public AbstractApplyTransformNodeDialog(TransformUtilityFactory<T, U, V> utilFactory) {
		this.utilFactory = utilFactory;

		/*
		 * Molecule options tab
		 */
		renameTab("Options", "Molecule Options");
		createNewGroup("Incoming molecules options");
		addDialogComponent(new DialogComponentColumnNameSelection(createMolColumnSettingsModel(),
				"Select Molecule column", 0,
				utilFactory.getInputColumnTypes() /* SmilesValue.class */));

		createNewGroup("Molecule pass-through columns");
		addDialogComponent(
				new DialogComponentColumnFilter2(createMoleculePassThroughColsModel(), 0));
		closeCurrentGroup();

		/*
		 * Transform Options TAB
		 */
		createNewTab("Transform options");
		addDialogComponent(new DialogComponentColumnNameSelection(createRxnColumnSettingsModel(),
				"Select transform column", 1,
				utilFactory.getReactionCellTypes()/* SmartsValue.class */));
		addDialogComponent(
				new DialogComponentBoolean(createTransformsSortedModel(), "Transforms are sorted"));

		addDialogComponent(new DialogComponentBoolean(createGenerateChiralProductsModel(),
				"Attempt to create enantiomeric products"));

		createNewGroup("Transform pass-through columns");
		addDialogComponent(
				new DialogComponentColumnFilter2(createTransformPassThroughColsModel(), 1));
		closeCurrentGroup();

		filterByKeySimilarityMdl = createFilterByKeySimilarityModel();
		alphaMdl = createTverskyAlphaModel();
		betaMdl = createTverskyBetaModel();
		firstFPColNameMdl = createFirstFPColNameModel();
		similarityType = createSimilarityTypeModel();
		similarityThresholdMdl = createSimilarityThresholdModel();
		comparisonTypeMdl = createComparisonTypeModel();

		filterByKeySimilarityMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				similarityType.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				firstFPColNameMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				similarityThresholdMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				comparisonTypeMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				updateTverskyParametersEnabledStatus();

			}
		});

		similarityType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateTverskyParametersEnabledStatus();
			}
		});

		similarityType.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
		firstFPColNameMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
		similarityThresholdMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
		comparisonTypeMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
		updateTverskyParametersEnabledStatus();

		addDialogComponent(new DialogComponentBoolean(filterByKeySimilarityMdl,
				"Filter by Transform Environment"));

		addDialogComponent(new DialogComponentColumnNameSelection(firstFPColNameMdl,
				"First Key Attachment point Fingerprint Column", 1, false, AP1_COLUMN_FILTER));

		createNewGroup("Similarity Metric settings");
		addDialogComponent(new DialogComponentButtonGroup(similarityType, null, false,
				AttachmentPointFingerprintSimilarityType.values()));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentNumber(similarityThresholdMdl, "Threshold", 0.05, 5));
		addDialogComponent(new DialogComponentNumber(alphaMdl, MMPConstants.ALPHA, 0.1, 5));
		addDialogComponent(new DialogComponentNumber(betaMdl, MMPConstants.BETA, 0.1, 5));

		setHorizontalPlacement(false);

		closeCurrentGroup();

		addDialogComponent(
				new DialogComponentButtonGroup(comparisonTypeMdl, "AP Fingerprint Comparison Type",
						false, AttachmentPointFingerprintComparisonType2.values()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.data.DataTableSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
		// Check if there is an AP FP column...
		if (specs[1] != null) {

			filterByKeySimilarityMdl.setEnabled(
					specs[1].stream().anyMatch(col -> AP1_COLUMN_FILTER.includeColumn(col)));
			if (filterByKeySimilarityMdl.isEnabled()) {
				similarityType.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				firstFPColNameMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				similarityThresholdMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				comparisonTypeMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue());
				updateTverskyParametersEnabledStatus();
			} else {
				similarityType.setEnabled(false);
				firstFPColNameMdl.setEnabled(false);
				similarityThresholdMdl.setEnabled(false);
				comparisonTypeMdl.setEnabled(false);
				alphaMdl.setEnabled(false);
				betaMdl.setEnabled(false);
			}
		}
	}

	/**
	 * @return The settings model for the create chiral products setting
	 */
	static SettingsModelBoolean createGenerateChiralProductsModel() {
		return new SettingsModelBoolean(MMPConstants.CREATE_CHIRAL_PRODUCTS,
				MMPConstants.DEFAULT_CREATE_CHIRAL_PRODUCTS);
	}

	/**
	 * @return The settings model for the 'transforms are sorted' setting
	 */
	static SettingsModelBoolean createTransformsSortedModel() {
		return new SettingsModelBoolean(MMPConstants.TRANSFORMS_SORTED,
				MMPConstants.DEFAULT_TRANSFORMS_SORTED);
	}

	/**
	 * @return The settings model for pass-through columns from the transform
	 *         table
	 */
	static SettingsModelColumnFilter2 createTransformPassThroughColsModel() {
		return new SettingsModelColumnFilter2(MMPConstants.TRANSFORM_TABLE_PASS_THROUGH_COLUMNS);
	}

	/**
	 * @return The settings model for pass-through columns from the molecule
	 *         table
	 */
	static SettingsModelColumnFilter2 createMoleculePassThroughColsModel() {
		return new SettingsModelColumnFilter2(MMPConstants.MOLECULE_TABLE_PASS_THROUGH_COLUMNS);
	}

	/**
	 * @return The settings model for the filter by transform environment
	 *         setting
	 */
	static SettingsModelBoolean createFilterByKeySimilarityModel() {
		return new SettingsModelBoolean(MMPConstants.FILTER_BY_TRANSFROM_ENVIRONMENT,
				MMPConstants.DEFAULT_FILTER_BY_ENVIRONMENT_SETTING);
	}

	/**
	 * @return The settings model for the reaction column name
	 */
	static SettingsModelString createRxnColumnSettingsModel() {
		return new SettingsModelString(MMPConstants.R_SMARTS_COLUMN, null);
	}

	/**
	 * @return The settings model for the Molecule column name
	 */
	static SettingsModelString createMolColumnSettingsModel() {
		return new SettingsModelString(MMPConstants.MOLECULE_COLUMN, null);
	}

	/**
	 * @return The settings model for the first AP fingerprint column name
	 */
	static SettingsModelString createFirstFPColNameModel() {
		return new SettingsModelString(MMPConstants.FIRST_AP_FINGERPRINT_COLUMN, null);
	}

	/**
	 * Method to update the enabled status of the Tversky parameters
	 */
	protected void updateTverskyParametersEnabledStatus() {
		alphaMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue()
				&& AttachmentPointFingerprintSimilarityType.valueOf(similarityType
						.getStringValue()) == AttachmentPointFingerprintSimilarityType.TVERSKY);
		betaMdl.setEnabled(filterByKeySimilarityMdl.getBooleanValue()
				&& AttachmentPointFingerprintSimilarityType.valueOf(similarityType
						.getStringValue()) == AttachmentPointFingerprintSimilarityType.TVERSKY);
	}

	/**
	 * @return The settings model for the similarity threshold
	 */
	static SettingsModelDoubleBounded createSimilarityThresholdModel() {
		return new SettingsModelDoubleBounded(MMPConstants.SIMILARITY_THRESHOLD,
				MMPConstants.DEFAULT_DOUBLE_SIMILARITY_THRESHOLD,
				MMPConstants.MINIMUM_DOUBLE_SIMILARITY_THRESHOLD,
				MMPConstants.MAXIMUM_DOUBLE_SIMILARITY_THRESHOLD);
	}

	/**
	 * @return The settings model for the comparison type
	 */
	static SettingsModelString createComparisonTypeModel() {
		return new SettingsModelString(MMPConstants.COMPARISON_TYPE,
				AttachmentPointFingerprintComparisonType2.getDefault().getActionCommand());
	}

	/**
	 * @return The settings model for the similarity type
	 */
	static SettingsModelString createSimilarityTypeModel() {
		return new SettingsModelString(MMPConstants.SIMILARITY_TYPE,
				AttachmentPointFingerprintSimilarityType.getDefault().getActionCommand());
	}

	/**
	 * @return The settings model for the Tversky beta parameter
	 */
	static SettingsModelDoubleBounded createTverskyBetaModel() {
		return new SettingsModelDoubleBounded(MMPConstants.BETA, MMPConstants.DEFAULT_TVERSKY_BETA,
				MMPConstants.MIN_TVERSKY_PARAM, MMPConstants.MAX_TVERSKY_PARAM);
	}

	/**
	 * @return The settings model for the Tversky alpha parameter
	 */
	static SettingsModelDoubleBounded createTverskyAlphaModel() {
		return new SettingsModelDoubleBounded(MMPConstants.ALPHA,
				MMPConstants.DEFAULT_TVERSKY_ALPHA, MMPConstants.MIN_TVERSKY_PARAM,
				MMPConstants.MAX_TVERSKY_PARAM);
	}

}
