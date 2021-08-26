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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.ATTRIBUTE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NEGATION;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;

/**
 * A special {@link QueryField} for the full text field, which has no operator
 * choice, only a text input, and a much-simplified JSON query
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldFullText extends QueryField {

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON definition
	 */
	protected QueryFieldFullText(JsonNode node) {
		super(node, false);

	}

	private QueryFieldFullText(String attribute, String displayName,
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
		return new DialogComponentString((SettingsModelString) model,
				EMPTY_STRING);
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {
		return new SettingsModelString(CFGKEY_QFIELD, null);
	}

	@Override
	protected String getFieldValue(QueryFieldModel queryFieldModel) {
		return ((SettingsModelString) queryFieldModel.getQueryFieldValueModel())
				.getStringValue();
	}

	@Override
	public JsonNode getQuery(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel) {
		// The general query does not have attribute or operator parameters, and
		// negation is always false
		final ObjectNode retVal =
				(ObjectNode) super.getQuery(nodeIndex, queryFieldModel);
		final ObjectNode param = (ObjectNode) retVal.get(PARAMETERS);
		param.remove(ATTRIBUTE);
		param.remove(OPERATOR);
		param.remove(NEGATION);
		return retVal;
	}

	@Override
	protected QueryFieldFullText createCloneWithSubquery(String asText,
			QueryFieldDropdown queryFieldDropdown) {
		return (QueryFieldFullText) new QueryFieldFullText(getAttribute(),
				getDisplayName(), getDescription(), getPlaceholder(),
				canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(), null, null, getServiceName())
						.setSubqueryNode(asText, queryFieldDropdown);

	}

}
