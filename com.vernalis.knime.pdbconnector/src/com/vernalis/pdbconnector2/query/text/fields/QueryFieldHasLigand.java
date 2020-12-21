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

import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.ATTRIBUTE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
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
 * An added field to create a 'has ligand' query, which was removed from the new
 * interface
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldHasLigand extends QueryField {

	private final String uniqueID = "HasLigand";

	/**
	 * Constructor
	 * 
	 * @param attribute
	 *            The id
	 * @param searchGroupName
	 *            The search group name
	 */
	protected QueryFieldHasLigand(String attribute, String searchGroupName) {
		super(attribute, "Has Ligand",
				"Structure contains a non-polymer component", null, false,
				searchGroupName, priority);
		priority += 5;
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {
		return new DialogComponentBoolean((SettingsModelBoolean) model,
				EMPTY_STRING);
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {

		return new SettingsModelBoolean(CFGKEY_QFIELD, true);
	}

	@Override
	protected String[] getFieldValue(QueryFieldModel queryFieldModel) {
		return new String[] { "non-polymer" };
	}

	@Override
	public JsonNode getQuery(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel) {
		final ObjectNode retVal = new ObjectMapper().createObjectNode()
				.put(TYPE_KEY, TYPE_TERMINAL)
				.put(NODE_ID, nodeIndex.getAndIncrement())
				.put(SERVICE_KEY, SERVICE_TEXT);
		ObjectNode paramObj = retVal.putObject(PARAMETERS)
				.put(ATTRIBUTE, getAttribute())
				.put(NEGATION,
						!((SettingsModelBoolean) queryFieldModel
								.getQueryFieldValueModel()).getBooleanValue())
				.put(OPERATOR, QueryFieldOperator.in.getQueryString());
		final ArrayNode valArr = paramObj.putArray(VALUE);
		for (final String str : getFieldValue(queryFieldModel)) {
			valArr.add(str);
		}
		return retVal;
	}

	@Override
	public String getUniqueID() {
		return uniqueID;
	}

	@Override
	protected QueryField createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return new QueryFieldHasLigand(getAttribute(), getSearchGroupName())
				.setSubqueryNode(optionName, ddqf);
	}

}
