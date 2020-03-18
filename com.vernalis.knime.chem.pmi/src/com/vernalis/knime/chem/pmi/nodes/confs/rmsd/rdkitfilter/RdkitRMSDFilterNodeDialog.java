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

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;
import com.vernalis.knime.dialog.components.DialogComponentGroup;

/**
 * Node dialog for the 'RMSD Conformer List Filter' node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class RdkitRMSDFilterNodeDialog extends DefaultNodeSettingsPane {

	private static final String REPLACE_UNFILTERABLE_LIST_CELLS_WITH_MISSING_VALUES =
			"Replace unfilterable list cells with missing values";
	private static final String RMSD_FILTER_THRESHOLD_A =
			"RMSD Filter Threshold (\u212B)";
	private static final String IGNORE_H_S = "Ignore H's";
	private static final String CONFORMERS = "Conformers";
	private static final String CONFORMER_PROPERTIES_COLUMNS =
			"Conformer Properties Columns";

	/**
	 * {@link ColumnFilter} for a list cell containing Mol, Sdf or RDKit
	 * molecules
	 */
	static final ColumnFilter MOL_LIST_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			DataType type = colSpec.getType();
			return type.isCompatible(ListDataValue.class)
					&& RdkitCompatibleColumnFormats.MOL_WITH_COORDS
							.includeColumn(type.getCollectionElementType());
		}

		@Override
		public String allFilteredMsg() {
			return "No Lists of  RDKit, SDF, Molecule columns are available";
		}
	};

	/**
	 * An {@link InputFilter} for a list cell containing anything other than
	 * molecule formats
	 */
	static final InputFilter<DataColumnSpec> LIST_FILTER =
			new InputFilter<DataColumnSpec>() {

				@Override
				public boolean include(DataColumnSpec name) {
					// Unfortunately, all the small molecule types are adaptable
					// to each other so we cant include e.g. SMILES as that
					// put's the conformers straight back in there
					return name.getType().isCompatible(ListDataValue.class)
							&& !RdkitCompatibleColumnFormats.MOL_ANY
									.includeColumn(name.getType()
											.getCollectionElementType());
				}

			};

	/**
	 * Constructor
	 */
	public RdkitRMSDFilterNodeDialog() {
		super();
		addDialogComponent(new DialogComponentColumnNameSelection(
				createMolColumnNameModel(), CONFORMERS, 0, MOL_LIST_FILTER));

		DialogComponentGroup dcg = new DialogComponentGroup(this,
				CONFORMER_PROPERTIES_COLUMNS, new DialogComponentColumnFilter2(
						createConformerPropertiesModels(), 0),
				false);
		dcg.addComponent(new DialogComponentBoolean(
				createMakeUnfilterablesMissingModel(),
				REPLACE_UNFILTERABLE_LIST_CELLS_WITH_MISSING_VALUES));

		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentNumber(createFilterModel(),
				RMSD_FILTER_THRESHOLD_A, 0.05, 5));

		addDialogComponent(
				new DialogComponentBoolean(createIgnoreHsModel(), IGNORE_H_S));
	}

	/**
	 * @return Settings model for the
	 *         {@value #REPLACE_UNFILTERABLE_LIST_CELLS_WITH_MISSING_VALUES}
	 *         option
	 */
	static SettingsModelBoolean createMakeUnfilterablesMissingModel() {
		return new SettingsModelBoolean(
				REPLACE_UNFILTERABLE_LIST_CELLS_WITH_MISSING_VALUES, true);
	}

	/**
	 * @return Settings model for the {@value #CONFORMER_PROPERTIES_COLUMNS}
	 *         option
	 */
	static SettingsModelColumnFilter2 createConformerPropertiesModels() {
		return new SettingsModelColumnFilter2(CONFORMER_PROPERTIES_COLUMNS,
				LIST_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

	/**
	 * @return Settings model for the {@value #IGNORE_H_S} option
	 */
	static SettingsModelBoolean createIgnoreHsModel() {
		return new SettingsModelBoolean(IGNORE_H_S, true);
	}

	/**
	 * @return Settings model for the {@value #RMSD_FILTER_THRESHOLD_A} option
	 */
	static SettingsModelDoubleBounded createFilterModel() {
		return new SettingsModelDoubleBounded(RMSD_FILTER_THRESHOLD_A, 0.35,
				0.05, 100.0);
	}

	/**
	 * @return Settings model for the {@value #CONFORMERS} option
	 */
	static SettingsModelString createMolColumnNameModel() {
		return new SettingsModelString(CONFORMERS, "");
	}
}
