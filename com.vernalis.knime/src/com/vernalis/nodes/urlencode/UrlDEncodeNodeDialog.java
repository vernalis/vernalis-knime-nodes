/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.urlencode;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.knime.chem.types.InchiAdapterCell;
import org.knime.chem.types.InchiCellFactory;
import org.knime.chem.types.InchiValue;
import org.knime.chem.types.SmartsAdapterCell;
import org.knime.chem.types.SmartsCellFactory;
import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.uri.URIDataValue;
import org.knime.core.data.uri.UriCellFactory;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;

public class UrlDEncodeNodeDialog extends DefaultNodeSettingsPane {

	private static final String CHARSET_ENCODING = "Charset encoding";
	private static final String OUTPUT_TYPE = "Output Type";
	private static final String REMOVE_INPUT_COLUMN = "Remove Input Column";
	private static final String INPUT_COLUMN = "Input Column";
	static final Map<DataType, Function<String, DataCell>> RETURN_FUNCTIONS =
			new LinkedHashMap<>();
	static final Map<Class<? extends DataValue>, DataType> OUTPUT_TYPES =
			new LinkedHashMap<>();

	static {

		RETURN_FUNCTIONS.put(SmartsAdapterCell.RAW_TYPE,
				x -> SmartsCellFactory.createAdapterCell(x));
		OUTPUT_TYPES.put(SmartsValue.class, SmartsAdapterCell.RAW_TYPE);
		RETURN_FUNCTIONS.put(SmilesAdapterCell.RAW_TYPE,
				x -> SmilesCellFactory.createAdapterCell(x));
		OUTPUT_TYPES.put(SmilesValue.class, SmilesAdapterCell.RAW_TYPE);
		RETURN_FUNCTIONS.put(InchiAdapterCell.RAW_TYPE,
				x -> InchiCellFactory.createAdapterCell(x));
		OUTPUT_TYPES.put(InchiValue.class, InchiAdapterCell.RAW_TYPE);
		RETURN_FUNCTIONS.put(UriCellFactory.TYPE,
				x -> UriCellFactory.create(x));
		OUTPUT_TYPES.put(URIDataValue.class, UriCellFactory.TYPE);
		// We put StringValue last in Return types to return the more specific
		// type above first if available
		RETURN_FUNCTIONS.put(StringCell.TYPE, x -> new StringCell(x));
		OUTPUT_TYPES.put(StringValue.class, StringCell.TYPE);

	}
	static final ColumnFilter TYPE_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			final DataType colType = colSpec.getType();
			if (colType.isCollectionType()) {
				return false;
			}
			if (colSpec.getType() == StringCell.TYPE) {
				// Shortcut!
				return true;
			}
			for (Class<? extends DataValue> clazz : OUTPUT_TYPES.keySet()) {
				if (colType.isCompatible(clazz) || colType.isAdaptable(clazz)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String allFilteredMsg() {
			return "No suitable columns in input table";
		}
	};

	/**
	 * 
	 */
	protected UrlDEncodeNodeDialog() {
		this(false);
	}

	protected UrlDEncodeNodeDialog(boolean showOutputTypesOption) {
		addDialogComponent(new DialogComponentColumnNameSelection(
				createColNameModel(), INPUT_COLUMN, 0, TYPE_FILTER));
		addDialogComponent(new DialogComponentBoolean(
				createRemoveInputColModel(), REMOVE_INPUT_COLUMN));
		if (showOutputTypesOption) {
			addDialogComponent(
					new DialogComponentButtonGroup(createOutputTypeModel(),
							OUTPUT_TYPE, false, OutputType.values()));
		}
		addDialogComponent(new DialogComponentStringSelection(
				createCharsetNameModel(), CHARSET_ENCODING,
				Charset.availableCharsets().keySet()));
	}

	static SettingsModelString createCharsetNameModel() {
		return new SettingsModelString(CHARSET_ENCODING, "UTF-8");
	}

	static SettingsModelString createOutputTypeModel() {
		return new SettingsModelString(OUTPUT_TYPE,
				OutputType.getDefault().getActionCommand());
	}

	static SettingsModelBoolean createRemoveInputColModel() {
		return new SettingsModelBoolean(REMOVE_INPUT_COLUMN, true);
	}

	static SettingsModelString createColNameModel() {
		return new SettingsModelString(INPUT_COLUMN, null);
	}

}
