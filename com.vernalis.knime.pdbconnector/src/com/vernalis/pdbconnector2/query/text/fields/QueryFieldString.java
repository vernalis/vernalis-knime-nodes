/*******************************************************************************
 * Copyright (c) 2020,2021 Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query.text.fields;

import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentStringArrayInput;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.IS_ITERABLE;

/**
 * A query field for a free-text input. This may be a single value, or multiple
 * values in an editable table
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 * @see QueryFieldSuggester
 */
public class QueryFieldString extends QueryField {

	/** The minimum allowed field width */
	protected static final int MIN_FIELD_WIDTH = 5;
	/** The default field width */
	protected static final int DEFAULT_FIELD_WIDTH = 15;
	/** The maximum allowed field width */
	protected static final int MAX_FIELD_WIDTH = 30;

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON definition
	 */
	protected QueryFieldString(JsonNode node) {
		super(node, !node.get(IS_ITERABLE).asBoolean());

	}

	/**
	 * Constructor
	 * 
	 * @param attribute
	 *            The query attribute
	 * @param displayName
	 *            The display name
	 * @param description
	 *            The optional description
	 * @param placeholder
	 *            The optiona placeholder
	 * @param canHaveMultipleFields
	 *            Whether it is possible to input multiple values (in a table)
	 * @param searchGroupName
	 *            The parent menu name
	 * @param searchGroupPriority
	 *            The priority determining place in the menu
	 * @param operators
	 *            The allowed operators
	 * @param defaultOperator
	 *            The default operator
	 * @param serviceName
	 *            The search service name
	 */
	protected QueryFieldString(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			String serviceName) {
		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator, serviceName);
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {

		switch (queryOperator) {

			case in:
			case contains_words:
				final SettingsModelStringArray arrMdl =
						(SettingsModelStringArray) model;
				int w = DEFAULT_FIELD_WIDTH;
				final String[] strArr = arrMdl.getStringArrayValue();
				if (strArr != null) {
					for (final String s : strArr) {
						if (s == null || s.isEmpty()) {
							continue;
						}
						w = Math.max(w, Math.max(MIN_FIELD_WIDTH,
								Math.min(s.length(), MAX_FIELD_WIDTH)));
					}
				}
				return new DialogComponentStringArrayInput(arrMdl, EMPTY_STRING,
						false, w, false);

			case contains_phrase:
			case equals:
			case exact_match:
			case exists:
				final SettingsModelString stringModel =
						(SettingsModelString) model;
				final int fieldWidth = calculateComponentSize(stringModel);
				return new DialogComponentString(stringModel, EMPTY_STRING,
						false, fieldWidth);

			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * @param stringModel
	 *            The Settings Model containing the field value
	 * @return An appropriate width for the component, which will be the default
	 *         if the model contains {@code null} or an empty string, the length
	 *         of the string if it between {@link #MIN_FIELD_WIDTH} and
	 *         {@link #MAX_FIELD_WIDTH}, and the appropriate one of those bounds
	 *         if it is beyond them
	 */
	protected int
			calculateComponentSize(final SettingsModelString stringModel) {
		int fieldWidth = DEFAULT_FIELD_WIDTH;
		if (stringModel.getStringValue() != null
				&& !stringModel.getStringValue().isEmpty()) {
			fieldWidth = Math.max(MIN_FIELD_WIDTH, Math.min(
					stringModel.getStringValue().length(), MAX_FIELD_WIDTH));
		}
		return fieldWidth;
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {

		switch (queryOperator) {

			case in:
			case contains_words:
				return new SettingsModelStringArray(CFGKEY_QFIELD, null);
			case contains_phrase:
			case equals:
			case exact_match:
			case exists:
				return new SettingsModelString(CFGKEY_QFIELD, null);

			default:
				throw new IllegalArgumentException();

		}

	}

	@Override
	protected Object getFieldValue(QueryFieldModel queryFieldModel) {

		final SettingsModel mdl = queryFieldModel.getQueryFieldValueModel();
		if (mdl instanceof SettingsModelString) {
			return ((SettingsModelString) mdl).getStringValue();
		} else {
			return ((SettingsModelStringArray) mdl).getStringArrayValue();
		}

	}

	@Override
	public JsonNode getQuery(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel) {
		return wrapQuery(nodeIndex, super.getQuery(nodeIndex, queryFieldModel));
	}

	@Override
	protected QueryFieldString createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return (QueryFieldString) new QueryFieldString(getAttribute(),
				getDisplayName(), getDescription(), getPlaceholder(),
				canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(),
				getOperators().stream().map(op -> op.name())
						.toArray(String[]::new),
				getDefaultOperator().name(), getServiceName())
						.setSubqueryNode(optionName, ddqf);

	}

}
