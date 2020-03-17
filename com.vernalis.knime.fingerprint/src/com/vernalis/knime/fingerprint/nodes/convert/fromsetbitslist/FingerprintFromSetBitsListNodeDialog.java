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
package com.vernalis.knime.fingerprint.nodes.convert.fromsetbitslist;

import java.util.List;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.IntValue;
import org.knime.core.data.LongValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

import com.vernalis.knime.fingerprint.abstrct.FingerPrintTypes;

/**
 * Abstract <code>NodeDialog</code> for single fingerprint column nodes
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S. Roughley
 */
public class FingerprintFromSetBitsListNodeDialog extends DefaultNodeSettingsPane {

	ColumnFilter cf = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			final DataType colType = colSpec.getType();
			if (colType.isCollectionType()) {
				final List<Class<? extends DataValue>> elementValueClasses =
						colType.getCollectionElementType().getValueClasses();
				return elementValueClasses.contains(IntValue.class)
						|| elementValueClasses.contains(LongValue.class);
			}
			return false;
		}

		@Override
		public String allFilteredMsg() {
			return "No collections of integers or longs found";
		}
	};

	/**
	 * abstract node dialog providing a single fingerprint column
	 */
	@SuppressWarnings("unchecked")
	public FingerprintFromSetBitsListNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(createLengthColNameModel(),
				"Select the length column", 0, true, LongValue.class, IntValue.class));

		addDialogComponent(new DialogComponentColumnNameSelection(createBitsColNameModel(),
				"Select the list of set bits column", 0, true, cf));

		addDialogComponent(
				new DialogComponentBoolean(createKeepInputColumnsModel(), "Keep input columns"));

		addDialogComponent(new DialogComponentButtonGroup(createFPTypeModel(), "Fingerprint Type",
				false, FingerPrintTypes.values()));

	}

	/** The settings model for the set bits list column */
	public static SettingsModelString createBitsColNameModel() {
		return new SettingsModelString("Set bits list Column", null);
	}

	/** Static method to create the FP type settings model */
	public static SettingsModelString createFPTypeModel() {
		return new SettingsModelString("Fingerprint type",
				FingerPrintTypes.getDefaultMethod().getActionCommand());
	}

	/** @return The settings model for the length column */
	public static SettingsModelString createLengthColNameModel() {
		return new SettingsModelString("Length Column", null);
	}

	/** Static method to create keep input columns settings model */
	public static SettingsModelBoolean createKeepInputColumnsModel() {
		return new SettingsModelBoolean("Keep input cols", true);
	}
}
