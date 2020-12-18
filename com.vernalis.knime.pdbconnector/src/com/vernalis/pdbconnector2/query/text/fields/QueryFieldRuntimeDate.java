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

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.ATTRIBUTE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NEGATION;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODE_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_TEXT;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_TERMINAL;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.VALUE;

/**
 * This is a special {@link QueryField} implementation for Date query fields
 * which are execution-time dependent. These don't exist in the new web UI, but
 * are provided for simplicity and backwards compatibility. They have no
 * operators and no user-defined field value. Instead the field value is
 * predefined to be the runtime - 7 days, and the operator greater_or_equal
 *
 * @author Steve
 *
 */
public class QueryFieldRuntimeDate extends QueryField {

	private final String uniqueID;

	/**
	 * Constructor
	 * 
	 * @param attribute
	 *            The query attribute
	 * @param displayName
	 *            The display name
	 * @param searchGroupName
	 *            The menu category
	 * @param description
	 *            The optional description
	 * @param uniqueID
	 *            The unique field ID
	 */
	protected QueryFieldRuntimeDate(String attribute, String displayName,
			String searchGroupName, String description, String uniqueID) {
		super(attribute, displayName, description, null, false, searchGroupName,
				priority);
		this.uniqueID = uniqueID;
		priority += 5;
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {
		// This one has no component
		return null;
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {
		// Has no model
		return null;
	}

	@Override
	protected String getFieldValue(QueryFieldModel queryFieldModel) {
		// The value is 7 days prior to query execution time...
		final Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -7);
		return String.format("%1$tFT00:00:00Z", c);
	}

	@Override
	public JsonNode getQuery(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel) {
		final ObjectNode retVal = new ObjectMapper().createObjectNode()
				.put(TYPE_KEY, TYPE_TERMINAL)
				.put(NODE_ID, nodeIndex.getAndIncrement())
				.put(SERVICE_KEY, SERVICE_TEXT);
		retVal.putObject(PARAMETERS).put(ATTRIBUTE, getAttribute())
				.put(NEGATION, false)
				.put(OPERATOR, QueryFieldOperator.greater.getQueryString())
				.put(VALUE, getFieldValue(queryFieldModel));
		return retVal;
	}

	@Override
	public String getUniqueID() {
		return uniqueID;
	}

	@Override
	protected QueryField createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return new QueryFieldRuntimeDate(getAttribute(), getDisplayName(),
				getSearchGroupName(), getDescription(), getUniqueID())
						.setSubqueryNode(optionName, ddqf);
	}

}
