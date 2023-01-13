/*******************************************************************************
 * Copyright (c) 2022,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.mask;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.BooleanValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.collection.ListDataValue;
import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeDialog;

/**
 * Node Dialog for the mask list node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ListMaskNodeDialog extends AbstractMultiCollectionNodeDialog {

	private static final String USE_VALUES_AS_INDICES = "Use values as indices";
	private static final String TREAT_MISSING_MASKS_AS_EMPTY_COLLECTIONS =
			"Treat missing masks as empty collections";
	/** The column filter name */
	static final String LIST_COLUMNS_TO_MASK = "List Columns to Mask";
	private static final String MASK_COLUMN = "Mask Column";
	/**
	 * {@link ColumnFilter} for a column which is a list of integers or booleans
	 */
	static final ColumnFilter MASK_COLUMN_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			DataType type = colSpec.getType();
			if (type.isCompatible(ListDataValue.class)) {
				type = type.getCollectionElementType();
				return type.isCompatible(IntValue.class)
						|| type.isCompatible(BooleanValue.class);
			}
			return type.isCompatible(BitVectorValue.class);
		}

		@Override
		public String allFilteredMsg() {
			return "No Integer or Boolean List columns found";
		}
	};

	private DataTableSpec lastSpec = null;
	private SettingsModelString maskColNameMdl;
	private SettingsModelBoolean intsAsIndicesMdl;

	/**
	 * Constructor
	 */
	ListMaskNodeDialog() {
		super(LIST_COLUMNS_TO_MASK, false, true, false);

	}

	@Override
	protected void prependDialogComponents() {
		maskColNameMdl = createMaskColumnNameModel();
		intsAsIndicesMdl = createUseIntMaskAsIndicesModel();
		maskColNameMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				setState(maskColNameMdl, intsAsIndicesMdl);

			}
		});
		setState(maskColNameMdl, intsAsIndicesMdl);

		createNewGroup("Mask Settings");
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColumnNameSelection(
				maskColNameMdl, MASK_COLUMN, 0, MASK_COLUMN_FILTER));
		addDialogComponent(new DialogComponentBoolean(intsAsIndicesMdl,
				USE_VALUES_AS_INDICES));

		addDialogComponent(
				new DialogComponentBoolean(createMissingMasksEmptyModel(),
						TREAT_MISSING_MASKS_AS_EMPTY_COLLECTIONS));
		setHorizontalPlacement(false);
		closeCurrentGroup();
	}

	/**
	 * Method to update the state of the 'ints-as-indices' setting based on the
	 * type of list members of the selected mask column
	 * 
	 * @param maskColNameMdl
	 *            The settings model containing the mask column name
	 * @param intsAsIndicesMdl
	 *            The ints-as-indices model
	 */
	protected void setState(SettingsModelString maskColNameMdl,
			SettingsModelBoolean intsAsIndicesMdl) {
		if (lastSpec != null) {
			DataType maskColType = lastSpec
					.getColumnSpec(maskColNameMdl.getStringValue()).getType();
			if (maskColType.isCollectionType()) {
				maskColType = maskColType.getCollectionElementType();
			}
			final Class<? extends DataValue> prefValClz =
					maskColType.getPreferredValueClass();
			intsAsIndicesMdl.setEnabled(!prefValClz.equals(BooleanValue.class)
					&& !prefValClz.equals(BitVectorValue.class));
		}

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {
		this.lastSpec = specs[0];
		super.loadAdditionalSettingsFrom(settings, specs);
		setState(maskColNameMdl, intsAsIndicesMdl);

	}

	/**
	 * @return the settings model for the use ints as indices option
	 */
	static SettingsModelBoolean createUseIntMaskAsIndicesModel() {
		return new SettingsModelBoolean(USE_VALUES_AS_INDICES, false);
	}

	/**
	 * @return the settings model for the treat missing cells as empty masks
	 *         option
	 */
	static SettingsModelBoolean createMissingMasksEmptyModel() {
		return new SettingsModelBoolean(
				TREAT_MISSING_MASKS_AS_EMPTY_COLLECTIONS, false);
	}

	/**
	 * @return the settings model for the mask column name
	 */
	static SettingsModelString createMaskColumnNameModel() {
		return new SettingsModelString(MASK_COLUMN, null);
	}

}
