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

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.knime.core.node.NodeLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.dialogcomponents.swing.NestedDropdownTextField.MenuAction;
import com.vernalis.pdbconnector2.query.PdbStaticSchemaLoader;
import com.vernalis.pdbconnector2.query.text.fields.QueryField.QueryFieldFactory;

/**
 * Registry of query fields, loaded from a static JSON definition file via
 * {@link PdbStaticSchemaLoader} This class is a singleton which should be
 * accessed via the static accessor method {@link #getInstance()}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public final class QueryFieldRegistry {

	private final class ConfigException extends Exception {

		private static final long serialVersionUID = 1L;

		public ConfigException(Throwable cause) {
			super(cause);

		}

	}

	private static final NodeLogger logger =
			NodeLogger.getLogger(QueryFieldRegistry.class);
	private final LinkedHashMap<String, SortedSet<QueryField>> catFieldMap =
			new LinkedHashMap<>();
	private final Map<String, String> idCatMap = new HashMap<>();
	private final Map<String, QueryField> idFieldMap = new HashMap<>();

	private ConfigException lastError = null;

	private static class Helper {

		private static final QueryFieldRegistry INSTANCE =
				new QueryFieldRegistry();
	}

	/**
	 * @return the singleton instance, which will be instantiated on first
	 *         access. Error-free loading should be checked by calling
	 *         {@link #isOK()} before using
	 */
	public static QueryFieldRegistry getInstance() {
		return Helper.INSTANCE;
	}

	private QueryFieldRegistry() {
		super();
		reload();
	}

	/**
	 * Method to reload the definitions. If an error occurs, this is caught and
	 * should be checked via a call to {@link #isOK()}
	 */
	public synchronized void reload() {
		try {
			lastError = null;
			initFromWeb();
		} catch (final ConfigException e) {
			lastError = e;
			catFieldMap.clear();
			idCatMap.clear();
		}
	}

	private void initFromWeb() throws ConfigException {

		try {
			final Map<String, QueryField> fields = new HashMap<>();

			// Firstly generate all the possible QueryFields from the attribute
			// metadata
			for (final JsonNode node : PdbStaticSchemaLoader.getInstance()
					.getQueryMetadata()) {
				try {
					final QueryField qField = QueryFieldFactory.build(node);
					fields.put(qField.getAttribute(), qField);
				} catch (final ParseException e) {
					throw new ConfigException(e);
				}
			}

			// Now work through the section data
			final Map<Integer, String> categories = new LinkedHashMap<>();
			int catIndex = 0;
			String catName = null;
			QueryField qField = null;
			for (final JsonNode node : PdbStaticSchemaLoader.getInstance()
					.getSelectorItems()) {
				try {
					boolean deprecated =
							node.path("deprecated").asBoolean(false);
					if (deprecated) {
						// We just ignore it!
						continue;
					}
					switch (node.get("type").asText()) {
						case "header":
							int headerIndex = node.path("index").asInt(-1);
							if (!deprecated && headerIndex > -1) {
								categories.put(headerIndex,
										node.get("name").asText());
							}
							break;

						case "item":
							catIndex = node.get("header_index").asInt();
							catName = categories.get(catIndex); // null for a
																// deprecated,
																// negative-indexed
																// or invalid
																// index
							qField = fields.get(node.get("attribute").asText());

							addField(catName, qField);
							if (qField.getDisplayName()
									.equalsIgnoreCase("Release Date")) {
								// Add the artificial 'Latest Released
								// Structures' field
								addField(qField.getSearchGroupName(),
										new QueryFieldRuntimeDate(
												qField.getAttribute(),
												"Latest Released Structures",
												qField.getSearchGroupName(),
												"Structures released in the last 7 days",
												"LatestReleased",
												qField.getServiceName()));
							} else if (qField.getDisplayName()
									.equalsIgnoreCase("Revision Date")) {
								// Add the artificial 'Latest Revised
								// Structures' field
								addField(qField.getSearchGroupName(),
										new QueryFieldRuntimeDate(
												qField.getAttribute(),
												"Latest Revised Structures",
												qField.getSearchGroupName(),
												"Structures revised in the last 7 days",
												"LatestRevised",
												qField.getServiceName()));
							} else if (qField.getDisplayName().equalsIgnoreCase(
									"Chemical Component Type")) {
								// Add the artificial 'Has Ligand' field
								addField(qField.getSearchGroupName(),
										new QueryFieldHasLigand(
												qField.getAttribute(),
												qField.getSearchGroupName(),
												qField.getServiceName()));
							}
							break;

						case "item-nested":
							catIndex = node.get("header_index").asInt();
							catName = categories.get(catIndex); // null for a
							// deprecated,
							// negative-indexed
							// or invalid
							// index
							qField = fields.get(node.get("attribute").asText());
							addField(catName, qField.createCloneWithSubquery(
									node.get("attribute_nested_value").asText(),
									(QueryFieldDropdown) fields
											.get(node.get("attribute_nested")
													.asText())));
							if (qField.getDisplayName()
									.equalsIgnoreCase("Release Date")) {
								// Add the artificial 'Latest Released
								// Structures' field
								addField(qField.getSearchGroupName(),
										new QueryFieldRuntimeDate(
												qField.getAttribute(),
												"Latest Released Structures",
												qField.getSearchGroupName(),
												"Structures released in the last 7 days",
												"LatestReleased",
												qField.getServiceName())
														.createCloneWithSubquery(
																node.get(
																		"attribute_nested_value")
																		.asText(),
																(QueryFieldDropdown) fields
																		.get(node
																				.get("attribute_nested")
																				.asText())));
							} else if (qField.getDisplayName()
									.equalsIgnoreCase("Revision Date")) {
								// Add the artificial 'Latest Revised
								// Structures' field
								addField(qField.getSearchGroupName(),
										new QueryFieldRuntimeDate(
												qField.getAttribute(),
												"Latest Revised Structures",
												qField.getSearchGroupName(),
												"Structures revised in the last 7 days",
												"LatestRevised",
												qField.getServiceName())
														.createCloneWithSubquery(
																node.get(
																		"attribute_nested_value")
																		.asText(),
																(QueryFieldDropdown) fields
																		.get(node
																				.get("attribute_nested")
																				.asText())));
							}
							break;

						default:
							throw new ConfigException(
									new UnsupportedOperationException(
											"Menu item type '"
													+ node.get("type").asText()
													+ "' not supported"));
					}
				} catch (final Exception e) {
					if (e instanceof ConfigException) {
						throw e;
					}
					throw new ConfigException(e);
				}
			}

			logger.info("Loaded " + idCatMap.size() + " query fields into "
					+ catFieldMap.size() + " categories");

		} catch (final IOException e) {
			throw new ConfigException(e);
		}

	}

	/**
	 * @return {@code true} if no exceptions where thrown during loading. If an
	 *         exception was thrown, its message can be accessed via
	 *         {@link #errorMessage()}
	 */
	public boolean isOK() {
		return lastError == null;
	}

	/**
	 * @return The message of the exception thrown during loading, or
	 *         {@code null} if loading was successful.
	 * @see #isOK()
	 */
	public String errorMessage() {
		return isOK() ? null : lastError.getMessage();
	}

	/**
	 * @return a {@link SortedSet} of the categories
	 */
	public SortedSet<String> getCategories() {
		return (SortedSet<String>) catFieldMap.keySet();
	}

	/**
	 * @param category
	 *            The category to return the fields for
	 * @return A {@link SortedSet} of the fields for the categories
	 * @throws NoSuchElementException
	 *             if the category does not exist
	 */
	public SortedSet<QueryField> getFields(String category)
			throws NoSuchElementException {
		if (!catFieldMap.containsKey(category)) {
			throw new NoSuchElementException();
		}
		return catFieldMap.get(category);
	}

	/**
	 * @param uniqueID
	 *            The field unique ID
	 * @return The field (or {@code null} if no field is found for the ID)
	 */
	public QueryField getField(String uniqueID) {
		return idFieldMap.get(uniqueID);
	}

	/**
	 * @param id
	 *            The field ID
	 * @return The category ID which contains the field
	 * @throws NoSuchElementException
	 *             if no field with the ID is registered
	 */
	public String getCategoryForId(String id) throws NoSuchElementException {
		if (!idCatMap.containsKey(id)) {
			throw new NoSuchElementException();
		}
		return idCatMap.get(id);
	}

	private void addCategory(String category) {
		catFieldMap.computeIfAbsent(category, k -> new TreeSet<>());
	}

	private void addField(String category, QueryField field) {
		if (category == null) {
			addField("", field);
			return;
		}
		addCategory(category);
		catFieldMap.get(category).add(field);
		idCatMap.put(field.getUniqueID(), category);
		idFieldMap.put(field.getUniqueID(), field);
	}

	/**
	 * @return A Map of {@link MenuAction}s for the categories and fields. The
	 *         map keys are the Category names, the values are a collection of
	 *         MenuActions for each field
	 */
	public Map<String, Collection<MenuAction>> getActionMap() {
		final Map<String, Collection<MenuAction>> retVal =
				new LinkedHashMap<>();
		for (final Entry<String, SortedSet<QueryField>> cat : catFieldMap
				.entrySet()) {
			retVal.put(cat.getKey(),
					cat.getValue().stream().map(qf -> qf.getMenuAction())
							.collect(Collectors.toList()));
		}
		return retVal;
	}

	/**
	 * @return the default {@link QueryField}
	 */
	public QueryField getDefault() {
		// The default is the first member of the first non-empty category, or
		// null if there is no such field
		return catFieldMap.values().stream().filter(e -> !e.isEmpty())
				.findFirst().orElse(Collections.emptySortedSet()).stream()
				.findFirst().orElse(null);

	}
}
