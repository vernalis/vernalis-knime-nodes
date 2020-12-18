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

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentDoubleRangeBounded;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelDoubleRangeBounded;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.MAX;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.MIN;

/**
 * Query field for a {@link Double} range
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldDoubleRange extends AbstractRangeQueryField<Double> {

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON definition
	 */
	public QueryFieldDoubleRange(JsonNode node) {
		super(node, node.get(MIN).asDouble(), node.get(MAX).asDouble());
	}

	private QueryFieldDoubleRange(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			Double min, Double max) {
		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator, min, max);
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
						(SettingsModelDoubleBounded) model, "", 1.0);

			case range:
			case range_closed:
				return new DialogComponentDoubleRangeBounded(
						(SettingsModelDoubleRangeBounded) model, 1.0, false);

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
				return new SettingsModelDoubleBounded(CFGKEY_QFIELD, getMin(),
						getMin(), getMax());

			case range:
			case range_closed:
				return new SettingsModelDoubleRangeBounded(CFGKEY_QFIELD,
						getMin(), getMax(), getMin(), getMax());

			default:
				throw new UnsupportedOperationException();

		}
	}

	@Override
	protected Object getFieldValue(QueryFieldModel queryFieldModel) {
		switch (queryFieldModel.getOperator()) {

			case less:
			case less_or_equal:
			case greater_or_equal:
			case greater:
			case equals:
				return ((SettingsModelDoubleBounded) queryFieldModel
						.getQueryFieldValueModel()).getDoubleValue();

			case range:
			case range_closed:
				final SettingsModelDoubleRangeBounded model =
						(SettingsModelDoubleRangeBounded) queryFieldModel
								.getQueryFieldValueModel();
				return new double[] { model.getLowerValue(),
						model.getUpperValue() };

			case exists:
			default:
				return null;

		}
	}

	@Override
	protected QueryFieldDoubleRange createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return (QueryFieldDoubleRange) new QueryFieldDoubleRange(getAttribute(),
				getDisplayName(), getDescription(), getPlaceholder(),
				canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(),
				getOperators().stream().map(op -> op.name())
						.toArray(String[]::new),
				getDefaultOperator().name(), getMin(), getMax())
						.setSubqueryNode(optionName, ddqf);

	}

}
