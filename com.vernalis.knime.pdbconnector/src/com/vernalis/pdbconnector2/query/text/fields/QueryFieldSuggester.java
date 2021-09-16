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
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentStringArrayInput;
import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentStringSuggester;
import com.vernalis.pdbconnector2.dialogcomponents.suggester.RcsbUrlSuggester;
import com.vernalis.pdbconnector2.dialogcomponents.suggester.Suggester;
import com.vernalis.pdbconnector2.dialogcomponents.swing.JSuggestingTextField;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;

/**
 * A {@link QueryFieldString} with dynamic text suggestion via a
 * {@link Suggester}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldSuggester extends QueryFieldString {

	private static final String SUGGESTER_SERVICE_URL_FORMAT =
			"https://www.rcsb.org/search/suggester/%s/%%s";
	private String suggesterUrlFmt;
	private Suggester suggester;

	/**
	 * Constructor
	 * 
	 * @param node
	 *            The JSON Definition
	 */
	protected QueryFieldSuggester(JsonNode node) {
		super(node);
		suggesterUrlFmt =
				String.format(SUGGESTER_SERVICE_URL_FORMAT, getAttribute());
		suggester = RcsbUrlSuggester.get(suggesterUrlFmt);
	}

	private QueryFieldSuggester(String attribute, String displayName,
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
						false, w, false, suggester,
						JSuggestingTextField.DEFAULT_MIN_SUGGEST_SIZE, 8);

			case contains_phrase:
			case equals:
			case exact_match:
			case exists:
				final SettingsModelString stringModel =
						(SettingsModelString) model;
				final int fieldWidth = calculateComponentSize(stringModel);
				return new DialogComponentStringSuggester(stringModel,
						EMPTY_STRING, suggester,
						JSuggestingTextField.DEFAULT_MIN_SUGGEST_SIZE,
						fieldWidth, false);
			default:
				throw new IllegalArgumentException();
		}

	}

	@Override
	public QueryField setSubqueryNode(String optionName,
			QueryFieldDropdown ddqf) {
		super.setSubqueryNode(optionName, ddqf);
		this.suggesterUrlFmt = suggesterUrlFmt + "/" + optionName;
		suggester = RcsbUrlSuggester.get(suggesterUrlFmt);
		return this;
	}

	@Override
	protected QueryFieldSuggester createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		return (QueryFieldSuggester) new QueryFieldSuggester(getAttribute(),
				getDisplayName(), getDescription(), getPlaceholder(),
				canHaveMultipleFields(), getSearchGroupName(),
				getSearchGroupPriority(),
				getOperators().stream().map(op -> op.name())
						.toArray(String[]::new),
				getDefaultOperator().name(), getServiceName())
						.setSubqueryNode(optionName, ddqf);
	}

}
