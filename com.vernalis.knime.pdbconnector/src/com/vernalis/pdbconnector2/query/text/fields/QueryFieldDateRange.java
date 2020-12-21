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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentDateInput;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentDateRangeInput;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelDateBounded;
import com.vernalis.pdbconnector2.dialogcomponents.SettingsModelDateRangeBounded;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.MAX;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.MIN;

/**
 * A query field for a date range
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldDateRange extends AbstractRangeQueryField<Date> {

	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final String MIDNIGHT_FORMAT = "%1$tFT00:00:00Z";
	private static final String LAST_SECOND_FORMAT = "%1$tFT23:59:59Z";

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON definition
	 * @throws ParseException
	 *             If there is an error parsing the JSON
	 */
	protected QueryFieldDateRange(JsonNode node) throws ParseException {
		super(node, getDateFromNode(node.get(MIN)),
				getDateFromNode(node.get(MAX)));
	}

	private QueryFieldDateRange(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			Date min, Date max) {
		super(attribute, displayName, description, placeholder,
				canHaveMultipleFields, searchGroupName, searchGroupPriority,
				operators, defaultOperator, min, max);
	}

	/**
	 * Static method to convert a String date in MM/dd/yyyy text format to a
	 * Java {@link Date}
	 * 
	 * @param node
	 *            The JSON
	 * @return the Date
	 * @throws ParseException
	 *             if the date could not be parsed
	 */
	public static Date getDateFromNode(JsonNode node) throws ParseException {
		if (node.isLong()) {
			return new Date(node.asLong());
		}

		return new SimpleDateFormat(DATE_FORMAT).parse(node.asText());
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {
		switch (queryOperator) {

			case less:
			case less_or_equal:
			case greater_or_equal:
			case greater:
			case equals:
			case exists:
				return new DialogComponentDateInput(
						(SettingsModelDateBounded) model, EMPTY_STRING);

			case range:
			case range_closed:
				return new DialogComponentDateRangeInput(
						(SettingsModelDateRangeBounded) model, EMPTY_STRING,
						true);

			default:
				throw new UnsupportedOperationException();

		}
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {
		switch (queryOperator) {

			case less:
			case less_or_equal:
			case greater_or_equal:
			case greater:
			case equals:
				return new SettingsModelDateBounded(CFGKEY_QFIELD, getMax(),
						getMin(), getMax());

			case exists:
				return null;
			case range:
			case range_closed:
				return new SettingsModelDateRangeBounded(CFGKEY_QFIELD,
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
				return String.format(MIDNIGHT_FORMAT,
						((SettingsModelDateBounded) queryFieldModel
								.getQueryFieldValueModel()).getDate());

			case range:
			case range_closed:
				final SettingsModelDateRangeBounded model =
						(SettingsModelDateRangeBounded) queryFieldModel
								.getQueryFieldValueModel();
				return new String[] {
						String.format(MIDNIGHT_FORMAT, model.getLowerValue()),
						String.format(LAST_SECOND_FORMAT,
								model.getUpperValue()) };

			case exists:
			default:
				return null;

		}
	}

	@Override
	protected QueryFieldDateRange createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return (QueryFieldDateRange) new QueryFieldDateRange(getAttribute(),
				getDisplayName(), getDescription(), getPlaceholder(),
				canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(),
				getOperators().stream().map(op -> op.name())
						.toArray(String[]::new),
				getDefaultOperator().name(), getMin(), getMax())
						.setSubqueryNode(optionName, ddqf);

	}

}
