/*******************************************************************************
 * Copyright (c) 2020, 2021, Vernalis (R&D) Ltd
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

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentIntRangeBounded;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelIntegerRangeBounded;
import com.vernalis.pdbconnector2.query.RCSBQueryRunner;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.MAX;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.MIN;

/**
 * Query field for an {@link Integer} range
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class QueryFieldIntegerRange extends AbstractRangeQueryField<Integer> {

	/**
	 * Constructor
	 * 
	 * @param node
	 *            JSON definition
	 */
	public QueryFieldIntegerRange(JsonNode node) {
		super(node, node.get(MIN).asInt(), node.get(MAX).asInt());
	}

	private QueryFieldIntegerRange(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			String serviceName, Integer min, Integer max) {
		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator, serviceName, min, max);
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {
		switch (queryOperator) {

			case equals:
			case exact_match:
			case less:
			case less_or_equal:
			case greater:
			case greater_or_equal:
			case exists:
				return new DialogComponentNumber(
						(SettingsModelIntegerBounded) model, "", 1);

			case range:
			case range_closed:
				return new DialogComponentIntRangeBounded(
						(SettingsModelIntegerRangeBounded) model, 1, false);

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
			case less:
			case less_or_equal:
			case greater:
			case greater_or_equal:
			case exists:
				return new SettingsModelIntegerBounded(CFGKEY_QFIELD, getMin(),
						getMin(), getMax());

			case range:
			case range_closed:
				return new SettingsModelIntegerRangeBounded(CFGKEY_QFIELD,
						getMin(), getMax(), getMin(), getMax());

			default:
				throw new UnsupportedOperationException();

		}
	}

	@Override
	protected Object getFieldValue(QueryFieldModel queryFieldModel) {
		boolean includeUpper = true;
		switch (queryFieldModel.getOperator()) {

			case less:
			case less_or_equal:
			case greater_or_equal:
			case greater:
			case equals:
				return ((SettingsModelIntegerBounded) queryFieldModel
						.getQueryFieldValueModel()).getIntValue();

			case range:
				includeUpper = false;
			case range_closed:
				final SettingsModelIntegerRangeBounded model =
						(SettingsModelIntegerRangeBounded) queryFieldModel
								.getQueryFieldValueModel();
				// As of query API v2 we return a JSON object
				final Integer from = model.getLowerValue();
				final Integer to = model.getUpperValue();
				if (RCSBQueryRunner.getQueryAPIVersion() < 2) {
					return new int[] { from, to };
				}
				return createRangeJSONObject(includeUpper).put(FROM, from)
						.put(TO, to);

			case exists:
			default:
				return null;

		}
	}

	@Override
	protected QueryFieldIntegerRange createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return (QueryFieldIntegerRange) new QueryFieldIntegerRange(
				getAttribute(), getDisplayName(), getDescription(),
				getPlaceholder(), canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(),
				getOperators().stream().map(op -> op.name())
						.toArray(String[]::new),
				getDefaultOperator().name(), getServiceName(), getMin(),
				getMax()).setSubqueryNode(optionName, ddqf);
	}
}
