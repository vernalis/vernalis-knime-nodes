/*******************************************************************************
 * Copyright (c) 2020, 2021 Vernalis (R&D) Ltd
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

import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.HtmlEncoder;
import com.vernalis.pdbconnector2.RcsbJSONConstants;
import com.vernalis.pdbconnector2.dialogcomponents.swing.NestedDropdownTextField.MenuAction;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupConjunction;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.ATTRIBUTE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.DATE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.DEFAULT_OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.DESCRIPTION;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.DISPLAY_NAME;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.INPUT_TYPE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.INTEGER;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.LOGICAL_OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NEGATION;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODES;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODE_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NUMBER;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.OPERATORS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PLACEHOLDER;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SEARCH_GROUP_NAME;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SEARCH_GROUP_PRIORITY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SELECT;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_TEXT;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SUGGESTER;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_GROUP;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_TERMINAL;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.VALUE;

/**
 * This class represents a single query field. A query comprises one or more
 * groups, each comprising one or more fields and subgroups. All have an
 * attribute (or id), a display name, a priority and a search group name (their
 * parent menu category). Optionally they have a description and placeholder
 * text. The latter is not used. Eventually, the plan is for the Node
 * Description to be build dynamically and list the fields and their
 * descriptions where available
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public abstract class QueryField implements Comparable<QueryField> {

	/**
	 * A factory class to return the correct implementation from the JSON
	 * definition
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.28.0
	 *
	 */
	public static class QueryFieldFactory {

		/**
		 * static factory method to return the required {@link QueryField}
		 * implementation
		 * 
		 * @param node
		 *            the JSON definition
		 * @return the field
		 * @throws ParseException
		 *             is there is an error parsing the JSON to a field
		 */
		public static QueryField build(JsonNode node) throws ParseException {
			final String inputType =
					node.get(INPUT_TYPE).asText().toLowerCase();
			switch (inputType) {
				case DATE:
					return new QueryFieldDateRange(node);
				case SELECT:
					return new QueryFieldDropdown(node);
				case SUGGESTER:
					return new QueryFieldSuggester(node);
				default:
					break;
			}

			if (node.get(DISPLAY_NAME).asText().equalsIgnoreCase("Full Text")) {
				return new QueryFieldFullText(node);
			}
			final String type = node.get(TYPE_KEY).asText().toLowerCase();
			switch (type) {
				case NUMBER:
					return new QueryFieldDoubleRange(node);
				case INTEGER:
					return new QueryFieldIntegerRange(node);
				default:
					return new QueryFieldString(node);
			}
		}
	}

	/** The key for fields */
	protected static final String CFGKEY_QFIELD = "qField";
	/** Priority counter for artificially injected fields */
	protected static int priority = 10000;
	private final String attribute, description, placeholder, searchGroupName,
			serviceName;
	private String displayName;// Not final so we can modify it later for nested
								// contexts

	private final boolean canHaveMultipleFields;
	private final int searchGroupPriority;
	private final QueryFieldOperator defaultOperator;
	private final Set<QueryFieldOperator> operators;
	/** The option name if the field definition relies on second subfield */
	protected String optionName = null;
	/** The optional second subfield */
	protected QueryFieldDropdown ddqf = null;

	/**
	 * Constructor from JSON
	 * 
	 * @param node
	 *            The JSON object
	 * @param canHaveMultipleFields
	 *            Whether it is possible to have multiple input values
	 */
	protected QueryField(JsonNode node, boolean canHaveMultipleFields) {
		this(node.get(ATTRIBUTE).asText(), node.get(DISPLAY_NAME).asText(),
				node.path(DESCRIPTION).asText(null),
				node.path(PLACEHOLDER).asText(null), canHaveMultipleFields,
				node.path(SEARCH_GROUP_NAME).asText(EMPTY_STRING),
				node.path(SEARCH_GROUP_PRIORITY).asInt(0),
				nodeToStringArray(node),
				node.path(DEFAULT_OPERATOR).asText(
						QueryFieldOperator.getDefault().getDisplayName()),
				node.path("service_name").asText(SERVICE_TEXT));
	}

	private static String[] nodeToStringArray(JsonNode node) {

		final JsonNode opNode = node.path(OPERATORS);
		if (!opNode.isArray()) {
			return null;
		}
		final String[] retVal = new String[opNode.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = opNode.get(i).asText();
		}
		return retVal;

	}

	/**
	 * Overloaded constructor in top level of menu with priority 0
	 * 
	 * @param attribute
	 *            The field id
	 * @param displayName
	 *            The display name
	 * @param description
	 *            The optional description
	 * @param placeholder
	 *            The optional placeholder text
	 * @param canHaveMultipleFields
	 *            Whether it is possible to have multiple input values
	 * @param searchGroupName
	 *            The name of the group menu the field belongs to
	 * @param searchGroupPriority
	 *            the priority governing the position in the menu
	 * @param operators
	 *            The possible operators
	 * @param defaultOperator
	 *            The default operator in a new query
	 * @param serviceName
	 *            The name of the search service for the request. If
	 *            {@code null} the default
	 *            {@link RcsbJSONConstants#SERVICE_TEXT} will be used
	 */
	protected QueryField(String attribute, String displayName,
			String description, String placeholder,
			boolean canHaveMultipleFields, String searchGroupName,
			int searchGroupPriority, String[] operators, String defaultOperator,
			String serviceName) {
		this.attribute = attribute;
		this.displayName =
				displayName == null ? null : HtmlEncoder.decode(displayName);
		this.description = description;
		this.placeholder = placeholder;
		this.canHaveMultipleFields = canHaveMultipleFields;
		this.searchGroupName = searchGroupName;
		this.searchGroupPriority = searchGroupPriority;
		this.serviceName = serviceName == null ? SERVICE_TEXT : serviceName;
		this.defaultOperator = defaultOperator == null ? null
				: QueryFieldOperator.valueOf(defaultOperator);
		this.operators =
				operators == null ? null : EnumSet.of(this.defaultOperator);
		if (operators != null) {
			for (final String op : operators) {
				this.operators.add(QueryFieldOperator.valueOf(op));
			}
		}
	}

	/**
	 * @return The attribute or 'id'
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @return The priority, governing the position in the group
	 */
	public int getSearchGroupPriority() {
		return searchGroupPriority;
	}

	/**
	 * @return The optional description (may be {@code null})
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return The display name
	 */
	public String getDisplayName() {
		return optionName == null ? displayName
				: String.format("%s - %s", displayName, optionName);
	}

	/**
	 * Method to set the field display name
	 * 
	 * @param displayName
	 *            The new display name
	 */
	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return The attribute or 'id'
	 */
	public String getId() {
		return attribute;
	}

	/**
	 * @return a unique id, which depends on both the value of {@link #getId()}
	 *         and any optional sub-field
	 */
	public String getUniqueID() {
		return optionName == null ? getId()
				: String.format("%s:%s", getId(), optionName);
	}

	/**
	 * @return the optional placeholder text
	 */
	public String getPlaceholder() {
		return placeholder;
	}

	/**
	 * @return whether the field can take multiple input values
	 */
	public boolean canHaveMultipleFields() {
		return canHaveMultipleFields;
	}

	/**
	 * Method to set the optional subquery (which always represents an enum)
	 * 
	 * @param optionName
	 *            The option name for the enum value of the subquery
	 * @param ddqf
	 *            The actual subquery - this is not displayed, but required for
	 *            building the query, and provides the complete name of this
	 *            parent field from one of it's values
	 * @return this field, for method call daisy-chaining
	 */
	protected QueryField setSubqueryNode(String optionName,
			QueryFieldDropdown ddqf) {
		this.optionName = optionName;
		this.ddqf = ddqf;
		return this;
	}

	/**
	 * Get the dialog component for the field, based on the selected operator.
	 * the model is supplied by
	 * {@link #getValueSettingsModel(QueryFieldOperator)}
	 * 
	 * @param queryOperator
	 *            The current operator
	 * @param model
	 *            The model from
	 *            {@link #getValueSettingsModel(QueryFieldOperator)}
	 * @return The {@link DialogComponent} for field value(s) entry
	 */
	public abstract DialogComponent getDialogComponent(
			QueryFieldOperator queryOperator, SettingsModel model);

	/**
	 * Get the settings model for the value, based on the supplied operator
	 * 
	 * @param queryOperator
	 *            The operator
	 * @return the appropriate SettingsModel
	 */
	public abstract SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator);

	/**
	 * @return the default operator
	 */
	public final QueryFieldOperator getDefaultOperator() {
		return defaultOperator;
	}

	/**
	 * @return the available operators
	 */
	public final Set<QueryFieldOperator> getOperators() {
		return operators;
	}

	@Override
	public int compareTo(QueryField o) {
		// We only do comparisons within a query group, so we order by search
		// group
		// priority
		final int retVal = Integer.compare(this.searchGroupPriority,
				o.searchGroupPriority);
		if (retVal != 0) {
			return retVal;
		}
		return getDisplayName().compareTo(o.getDisplayName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (getDisplayName() == null ? 0 : getDisplayName().hashCode());
		result = prime * result
				+ (getAttribute() == null ? 0 : getAttribute().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final QueryField other = (QueryField) obj;
		if (getDisplayName() == null) {
			if (other.getDisplayName() != null) {
				return false;
			}
		} else if (!getDisplayName().equals(other.getDisplayName())) {
			return false;
		}
		if (getAttribute() == null) {
			if (other.getAttribute() != null) {
				return false;
			}
		} else if (!getAttribute().equals(other.getAttribute())) {
			return false;
		}
		return true;
	}

	/**
	 * @return the parent menu name
	 */
	public String getSearchGroupName() {
		return searchGroupName;
	}

	/**
	 * Method to get the RCSB guery JSON for the field and its
	 * {@link QueryFieldModel} representing an actual query using the field
	 * 
	 * @param nodeIndex
	 *            The incoming node index, as each node in the query has to be
	 *            uniquely sequentially numbered
	 * @param queryFieldModel
	 *            The field model representing a query state for this field
	 * @return the JSON representation of the query field
	 */
	public JsonNode getQuery(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel) {
		final ObjectNode retVal = new ObjectMapper().createObjectNode();
		if (queryFieldModel.hasOperator()
				&& queryFieldModel.getOperator().needsGroupQuery()) {
			// We need a group node enclosing each array member, with 'OR' logic
			final ArrayNode nodes = retVal.put(TYPE_KEY, TYPE_GROUP)
					.put(LOGICAL_OPERATOR,
							QueryGroupConjunction.OR.getActionCommand())
					.putArray(NODES);
			final Object val = getFieldValue(queryFieldModel);
			if (val == null) {
				throw new NullPointerException(
						"'null' is not a valid query field parameter ("
								+ queryFieldModel.getQueryField()
										.getDisplayName()
								+ ")");
			}
			if (val.getClass().isArray()) {
				for (final Object valObj : (Object[]) val) {
					final ObjectNode node = nodes.addObject();
					final ObjectNode parameters = createTerminalNode(nodeIndex,
							queryFieldModel, node);
					if (valObj instanceof String) {
						parameters.put(VALUE, (String) valObj);
					} else {
						parameters.putPOJO(VALUE, valObj);
					}
				}
			} else {
				throw new UnsupportedOperationException(
						"Only array objects are supported here");
			}
		} else {
			// We just need a simple terminal node
			final ObjectNode parameters =
					createTerminalNode(nodeIndex, queryFieldModel, retVal);
			if (queryFieldModel.getOperator() != QueryFieldOperator.exists) {
				final Object val = getFieldValue(queryFieldModel);
				if (val == null) {
					throw new NullPointerException(
							"'null' is not a valid query field parameter ("
									+ queryFieldModel.getQueryField()
											.getDisplayName()
									+ ")");
				}
				if (val.getClass().isArray()) {
					final ArrayNode valArr = parameters.putArray(VALUE);
					if (val instanceof String[]) {
						for (final String str : (String[]) val) {
							valArr.add(str);
						}
					} else {
						for (int i = 0; i < Array.getLength(val); i++) {
							valArr.addPOJO(Array.get(val, i));
						}
					}
				} else {
					if (val instanceof String) {
						parameters.put(VALUE, (String) val);
					} else {
						parameters.putPOJO(VALUE, val);
					}
				}
			}
		}
		return wrapQuery(nodeIndex, retVal);

	}

	/**
	 * This method builds a correctly formatted terminal node. The supplied
	 * {@link ObjectNode} becomes the terminal node
	 *
	 * @param nodeIndex
	 *            The node index counter
	 * @param queryFieldModel
	 *            The {@link QueryFieldModel} to populate the correct parameters
	 * @param node
	 *            The JSON node which will become the required query terminal
	 *            node
	 * @return The {@code 'parameters'} JSON node which will need the
	 *         {@code 'value'} field added
	 */
	protected ObjectNode createTerminalNode(AtomicInteger nodeIndex,
			QueryFieldModel queryFieldModel, final ObjectNode node) {
		node.put(TYPE_KEY, TYPE_TERMINAL);
		node.put(NODE_ID, nodeIndex.getAndIncrement());
		node.put(SERVICE_KEY, getServiceName());
		final ObjectNode parameters =
				node.putObject(PARAMETERS).put(ATTRIBUTE, getAttribute());
		if (queryFieldModel.hasOperator()) {
			parameters
					.put(NEGATION,
							queryFieldModel.getIsNotMdl().getBooleanValue())
					.put(OPERATOR,
							queryFieldModel.getOperator().getQueryString());
		}
		return parameters;
	}

	/**
	 * @return the service name for the field
	 * @since 1.30.3
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * @param queryFieldModel
	 *            the field model for a query
	 * @return the value stored in the model for the field
	 */
	protected abstract Object getFieldValue(QueryFieldModel queryFieldModel);

	/**
	 * Method to handle the query if we have a subquery associated, which must
	 * be placed in a query group node alongside the actual query
	 * 
	 * @param nodeIndex
	 *            The node index of counter
	 * @param superQuery
	 *            The query for the field value
	 * @return the nested query if required, otherwise the unchanged
	 *         {@code superQuery}
	 */
	protected JsonNode wrapQuery(AtomicInteger nodeIndex,
			final JsonNode superQuery) {
		if (ddqf == null) {
			// No subquery, so we just use the basic string value
			return superQuery;
		} else {
			// Needs to go in a group with the value and the ddqf
			final ObjectNode retVal = new ObjectMapper().createObjectNode();
			retVal.put(TYPE_KEY, TYPE_GROUP).put(LOGICAL_OPERATOR,
					QueryGroupConjunction.AND.getActionCommand());
			final ArrayNode nodes = retVal.putArray(NODES);
			// Add the value to the nodes array
			nodes.add(superQuery);
			// And the subquery type
			final ObjectNode ddqfNode = nodes.addObject();
			ddqfNode.put(TYPE_KEY, TYPE_TERMINAL)
					.put(SERVICE_KEY, getServiceName())
					.put(NODE_ID, nodeIndex.getAndIncrement())
					.putObject(PARAMETERS).put(ATTRIBUTE, ddqf.getAttribute())
					.put(OPERATOR,
							QueryFieldOperator.exact_match.getQueryString())
					.put(NEGATION, false).put(VALUE, optionName);
			return retVal;
		}
	}

	/**
	 * Method to create a clone of this field with a new subquery type/value
	 * 
	 * @param optionName
	 *            the option name for the subquery in the new clone
	 * @param ddqf
	 *            The subquery field
	 * @return a clone with subquery set
	 */
	protected abstract QueryField createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf);

	/**
	 * @return A {@link MenuAction} with the display name and the unique ID as
	 *         the action
	 */
	public MenuAction getMenuAction() {
		return new MenuAction(getDisplayName(), getUniqueID());
	}

}
