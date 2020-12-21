/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentStringArrayInput;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.ENUMERATION;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.ENUM_LEN;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;

/**
 * A query field to allow choosing from a list of values , either in a single
 * {@link DialogComponentStringSelection} (via a JComboBox) or a
 * {@link DialogComponentStringArrayInput} if multiple values are allowed
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldDropdown extends QueryField {

	private final List<String> dropdownValues = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON definition
	 */
	protected QueryFieldDropdown(JsonNode node) {
		super(node, node.get(ENUM_LEN).asInt() > 2);
		node.get(ENUMERATION).elements()
				.forEachRemaining(elem -> dropdownValues.add(elem.asText()));
	}

	private QueryFieldDropdown(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			Collection<String> dropdownValues) {
		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator);
		this.dropdownValues.addAll(dropdownValues);
	}

	/**
	 * @return an unmodifiable view on the allowed values in the dropdown
	 */
	public List<String> getDropdownValues() {
		return Collections.unmodifiableList(dropdownValues);
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {
		switch (queryOperator) {

			case equals:
			case exact_match:
			case exists:
				return canHaveMultipleFields()
						? new DialogComponentStringArrayInput(
								(SettingsModelStringArray) model, EMPTY_STRING,
								false, getDropdownValues())
						: new DialogComponentStringSelection(
								(SettingsModelString) model, EMPTY_STRING,
								getDropdownValues());

			default:
				throw new UnsupportedOperationException();

		}
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {
		switch (queryOperator) {

			case equals:
			case exact_match:
			case exists:
				return canHaveMultipleFields()
						? new SettingsModelStringArray(CFGKEY_QFIELD,
								new String[] { getDropdownValues().get(0) })
						: new SettingsModelString(CFGKEY_QFIELD,
								getDropdownValues().get(0));

			default:
				throw new UnsupportedOperationException();

		}
	}

	@Override
	public JsonNode getQuery(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel) {
		// There is a strange and undocumented API feature here, in which
		// 'exact_match'
		// operator is used when multiple value are allowed, but the query
		// translates to
		// 'in'

		final ObjectNode retVal =
				(ObjectNode) super.getQuery(nodeIndex, queryFieldModel);
		if (canHaveMultipleFields() && queryFieldModel
				.getOperator() == QueryFieldOperator.exact_match) {
			((ObjectNode) retVal.get(PARAMETERS)).put(OPERATOR,
					QueryFieldOperator.in.getQueryString());
		}
		return retVal;
	}

	@Override
	protected Object getFieldValue(QueryFieldModel queryFieldModel) {
		return queryFieldModel
				.getQueryFieldValueModel() instanceof SettingsModelString
						? ((SettingsModelString) queryFieldModel
								.getQueryFieldValueModel()).getStringValue()
						: ((SettingsModelStringArray) queryFieldModel
								.getQueryFieldValueModel())
										.getStringArrayValue();
	}

	@Override
	protected QueryFieldDropdown createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return (QueryFieldDropdown) new QueryFieldDropdown(getAttribute(),
				getDisplayName(), getDescription(), getPlaceholder(),
				canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(),
				getOperators().stream().map(op -> op.name())
						.toArray(String[]::new),
				getDefaultOperator().name(), getDropdownValues())
						.setSubqueryNode(optionName, ddqf);

	}
}
