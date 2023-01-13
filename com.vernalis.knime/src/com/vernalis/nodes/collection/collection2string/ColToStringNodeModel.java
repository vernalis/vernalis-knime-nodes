/*******************************************************************************
 * Copyright (c) 2019,2023, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.collection.collection2string;

import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionDataValue;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.nodes.collection.abstrct.AbstractMultiCollectionNodeModel;

import static com.vernalis.nodes.collection.collection2string.ColToStringNodeDialog.createCellPrefixModel;
import static com.vernalis.nodes.collection.collection2string.ColToStringNodeDialog.createCellSuffixModel;
import static com.vernalis.nodes.collection.collection2string.ColToStringNodeDialog.createJoinerModel;
import static com.vernalis.nodes.collection.collection2string.ColToStringNodeDialog.createPrefixModel;
import static com.vernalis.nodes.collection.collection2string.ColToStringNodeDialog.createSkipMissingValuesModel;
import static com.vernalis.nodes.collection.collection2string.ColToStringNodeDialog.createSuffixModel;

/**
 * Node model for the Collection to String node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class ColToStringNodeModel extends AbstractMultiCollectionNodeModel {

	private final SettingsModelBoolean skipMissingsMdl =
			registerSettingsModel(createSkipMissingValuesModel());
	private final SettingsModelString prefixMdl =
			registerSettingsModel(createPrefixModel());
	private final SettingsModelString cellPrefixMdl =
			registerSettingsModel(createCellPrefixModel());
	private final SettingsModelString joinerMdl =
			registerSettingsModel(createJoinerModel());
	private final SettingsModelString cellSuffixMdl =
			registerSettingsModel(createCellSuffixModel());
	private final SettingsModelString suffixMdl =
			registerSettingsModel(createSuffixModel());
	private String cellPrefix;
	private String cellSuffix;
	private String joiner;
	private String prefix;
	private String suffix;

	/**
	 * Constructor
	 *
	 * @since 1.36.2
	 */
	protected ColToStringNodeModel() {
		super(true);
	}

	@Override
	protected void doConfigure(DataTableSpec spec)
			throws InvalidSettingsException {
		cellPrefix = cellPrefixMdl.getJavaUnescapedStringValue() == null ? ""
				: cellPrefixMdl.getJavaUnescapedStringValue();
		cellSuffix = cellSuffixMdl.getJavaUnescapedStringValue() == null ? ""
				: cellSuffixMdl.getJavaUnescapedStringValue();
		joiner = joinerMdl.getJavaUnescapedStringValue() == null ? ""
				: joinerMdl.getJavaUnescapedStringValue();
		prefix = prefixMdl.getJavaUnescapedStringValue() == null ? ""
				: prefixMdl.getJavaUnescapedStringValue();
		suffix = suffixMdl.getJavaUnescapedStringValue() == null ? ""
				: suffixMdl.getJavaUnescapedStringValue();
	}

	@Override
	protected void reset() {
		super.reset();
		// Clean up
		cellPrefix = null;
		cellSuffix = null;
		joiner = null;
		prefix = null;
		suffix = null;
	}

	@Override
	protected DataColumnSpec[] createNewColumnSpecs(DataTableSpec spec,
			int[] idx) {
		// Now generate the output spec
		String colNameSuffix = isReplaceInputCols() ? "" : " (String)";
		final DataColumnSpec[] newColSpecs = new DataColumnSpec[idx.length];
		for (int i = 0; i < newColSpecs.length; i++) {
			String newColName =
					spec.getColumnSpec(idx[i]).getName() + colNameSuffix;
			if (!isReplaceInputCols()) {
				newColName =
						DataTableSpec.getUniqueColumnName(spec, newColName);
			}
			newColSpecs[i] =
					new DataColumnSpecCreator(newColName, StringCell.TYPE)
							.createSpec();
		}
		return newColSpecs;
	}

	@Override
	protected DataCell[] getCells(int[] idx, DataRow row,
			DataColumnSpec[] newColSpecs) throws RuntimeException {

		DataCell[] retVal = ArrayUtils.fill(new DataCell[newColSpecs.length],
				DataType.getMissingCell());
		int cellIndex = 0;
		for (int col : idx) {
			DataCell colCell = row.getCell(col);
			if (!colCell.isMissing()) {
				retVal[cellIndex] =
						new StringCell(((CollectionDataValue) colCell).stream()
								.filter(x -> !(skipMissingsMdl.getBooleanValue()
										&& x.isMissing()))
								.map(cell -> String.format("%s%s%s", cellPrefix,
										cell.toString(), cellSuffix))
								.collect(Collectors.joining(joiner, prefix,
										suffix)));
			}
			cellIndex++;
		}
		return retVal;
	}

}
