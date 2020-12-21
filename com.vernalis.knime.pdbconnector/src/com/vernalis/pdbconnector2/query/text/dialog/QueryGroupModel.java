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
package com.vernalis.pdbconnector2.query.text.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.InvalidSettingsExceptionCombiner;
import com.vernalis.pdbconnector2.query.QueryModel;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.LOGICAL_OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODES;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_GROUP;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_KEY;

/**
 * The {@link QueryModel} implementation for a Query Group. A query group
 * comprises a logic operator, and a mixture of Query Fields (represented by
 * {@link QueryFieldModel}s) and sub-groups (represented by
 * {@link QueryGroupModel}s)
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryGroupModel implements QueryModel {

	/**
	 * The settings key for the logic operator
	 */
	public static final String CFGKEY_OPERATOR = "operator";
	private static final String CFG_KEY_SUBGROUPS = "subGroups";
	private static final String CFG_KEY_FIELDS = "fields";
	private final SettingsModelString logicOperatorModel =
			new SettingsModelString(CFGKEY_OPERATOR,
					QueryGroupConjunction.getDefault().name());
	private final List<QueryFieldModel> fields = new ArrayList<>();
	private final List<QueryGroupModel> subModels = new ArrayList<>();

	private final List<ChangeListener> changeListeners =
			new CopyOnWriteArrayList<>();

	/**
	 * Enum representing the possible change types
	 * 
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	public enum QueryGroupEventType {
		/**
		 * The logic operator has changed
		 */
		LogicOperatorChange,
		/**
		 * A field has been added
		 */
		FieldAdded,
		/**
		 * A field has been removed
		 */
		FieldRemoved,
		/**
		 * A group has been added
		 */
		GroupAdded,
		/**
		 * A group has been removed
		 */
		GroupRemoved,
		/**
		 * A field has been changed
		 */
		FieldChanged,
		/**
		 * A group has been changed
		 */
		SubgroupChanged;
	}

	/**
	 * Constructor. A group is created with a single default
	 * {@link QueryFieldModel}
	 */
	public QueryGroupModel() {
		addField(new QueryFieldModel());
		logicOperatorModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(logicOperatorModel,
						QueryGroupEventType.LogicOperatorChange);

			}
		});
	}

	/**
	 * Method to set the query conjunction operator
	 * 
	 * @param conjunction
	 *            the new value
	 */
	public void setConjunction(QueryGroupConjunction conjunction) {
		logicOperatorModel.setStringValue(conjunction.name());
	}

	/**
	 * @return The stored query conjunction
	 */
	public QueryGroupConjunction getConjunction() {
		return QueryGroupConjunction
				.fromText(logicOperatorModel.getStringValue());
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		logicOperatorModel.saveSettingsTo(settings);
		final NodeSettingsWO fieldsSettings =
				settings.addNodeSettings(CFG_KEY_FIELDS);
		int i = 0;
		for (final QueryFieldModel field : fields) {
			final NodeSettingsWO fieldSetting =
					fieldsSettings.addNodeSettings(String.format("%d", i++));
			field.saveSettingsTo(fieldSetting);
		}

		final NodeSettingsWO subGroupSettings =
				settings.addNodeSettings(CFG_KEY_SUBGROUPS);
		i = 0;
		for (final QueryGroupModel group : subModels) {
			final NodeSettingsWO groupSetting =
					subGroupSettings.addNodeSettings(String.format("%d", i++));
			group.saveSettingsTo(groupSetting);
		}
	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();
		try {
			QueryGroupConjunction.fromText(settings.getString(CFGKEY_OPERATOR));
		} catch (final IllegalArgumentException | NullPointerException e) {
			isec.add(new InvalidSettingsException(e.getMessage(), e));
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		NodeSettingsRO fieldsSettings;
		try {
			fieldsSettings = settings.getNodeSettings(CFG_KEY_FIELDS);
			for (final String key : fieldsSettings) {
				try {
					new QueryFieldModel(fieldsSettings.getNodeSettings(key));
				} catch (final InvalidSettingsException e) {
					isec.add(e);
				}
			}
		} catch (final InvalidSettingsException e1) {
			isec.add(e1);
		}
		try {
			final NodeSettingsRO subGroupSettings =
					settings.getNodeSettings(CFG_KEY_SUBGROUPS);
			for (final String key : subGroupSettings) {
				try {
					new QueryGroupModel().validateSettings(
							subGroupSettings.getNodeSettings(key));
				} catch (final InvalidSettingsException e) {
					isec.add(e);
				}
			}
		} catch (final InvalidSettingsException e1) {
			isec.add(e1);
		}
		isec.throwAll();
	}

	@Override
	public void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		logicOperatorModel.loadSettingsFrom(settings);

		clearQuery();
		final NodeSettingsRO fieldsSettings =
				settings.getNodeSettings(CFG_KEY_FIELDS);
		for (final String key : fieldsSettings) {
			addField(new QueryFieldModel(fieldsSettings.getNodeSettings(key)));
		}

		final NodeSettingsRO subGroupSettings =
				settings.getNodeSettings(CFG_KEY_SUBGROUPS);
		for (final String key : subGroupSettings) {
			final QueryGroupModel subGroup = new QueryGroupModel();
			subGroup.loadSettings(subGroupSettings.getNodeSettings(key));
			addGroup(subGroup);
		}
	}

	@Override
	public void clearQuery() {
		clearFields();
		clearSubGroups();
	}

	@Override
	public boolean hasQuery() {
		for (final QueryFieldModel f : getFields()) {
			if (f.hasQuery()) {
				return true;
			}
		}
		for (final QueryGroupModel g : getSubgroups()) {
			if (g.hasQuery()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return {@code true} is the model contains no fields and no subgruops
	 */
	public boolean isEmpty() {
		return fields.isEmpty() && subModels.isEmpty();
	}

	/**
	 * @return The logical conjunction operator model
	 */
	public SettingsModelString getConjunctionModel() {
		return logicOperatorModel;
	}

	/**
	 * Method to add a field to the query
	 * 
	 * @param subModel
	 *            the model for the field to add
	 */
	public void addField(QueryFieldModel subModel) {
		if (fields.add(subModel)) {
			subModel.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					notifyChangeListeners(subModel,
							QueryGroupEventType.FieldChanged);

				}
			});
			notifyChangeListeners(subModel, QueryGroupEventType.FieldAdded);
		}
	}

	/**
	 * Method to remove a field from the query
	 * 
	 * @param fieldModel
	 *            The model for the field to remove
	 */
	public void removeField(QueryFieldModel fieldModel) {
		if (fields.remove(fieldModel)) {
			notifyChangeListeners(fieldModel, QueryGroupEventType.FieldRemoved);
		}
	}

	/**
	 * Method to remove all query fields
	 */
	public void clearFields() {
		// If we use a standard for model:submodels we encounter
		// ConcurrentModificationException
		while (!fields.isEmpty()) {
			removeField(fields.get(0));
		}
	}

	/**
	 * @return the number of fields in the query group
	 */
	public int getFieldCount() {
		return fields.size();
	}

	/**
	 * @return {@code true} if the group has at least 1 field
	 */
	public boolean hasFields() {
		return !fields.isEmpty();
	}

	/**
	 * @return The an unmodifiable view of the models for the fields in this
	 *         group
	 */
	public List<QueryFieldModel> getFields() {
		return Collections.unmodifiableList(fields);
	}

	/**
	 * Method to add a subgroup to the query
	 * 
	 * @param groupModel
	 *            The model of the new subgroup
	 */
	public void addGroup(QueryGroupModel groupModel) {

		if (subModels.add(groupModel)) {
			groupModel.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					notifyChangeListeners(groupModel,
							QueryGroupEventType.SubgroupChanged);

				}
			});
			notifyChangeListeners(groupModel, QueryGroupEventType.GroupAdded);
		}
	}

	/**
	 * Method to remove a subgroup from the query
	 * 
	 * @param groupModel
	 *            The model of the subgroup to remove
	 */
	public void removeGroup(QueryGroupModel groupModel) {
		if (subModels.remove(groupModel)) {
			notifyChangeListeners(groupModel, QueryGroupEventType.GroupRemoved);
		}
	}

	/**
	 * Method to remove all subgroups
	 */
	public void clearSubGroups() {
		// If we use a standard for model:submodels we encounter
		// ConcurrentModificationException
		while (!subModels.isEmpty()) {
			removeGroup(subModels.get(0));
		}
	}

	/**
	 * @return the number of sub-groups
	 */
	public int getSubGroupCount() {
		return subModels.size();
	}

	/**
	 * @return {@code true} if the model has at least 1 subgroup
	 */
	public boolean hasSubGroups() {
		return !subModels.isEmpty();
	}

	/**
	 * @return An unmodifiable view of the models for the subgroups in this
	 *         group
	 */
	public List<QueryGroupModel> getSubgroups() {
		return Collections.unmodifiableList(subModels);
	}

	@Override
	public void addChangeListener(ChangeListener l) {
		if (!changeListeners.contains(l)) {
			changeListeners.add(l);
		}
	}

	/**
	 * Method to add a {@link ChangeListener} to the start of the registered
	 * listeners
	 * 
	 * @param l
	 *            the listener to add
	 */
	public void prependChangeListener(ChangeListener l) {
		if (!changeListeners.contains(l)) {
			changeListeners.add(0, l);
		}
	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}

	/**
	 * Method to notify any registered change listeners of a change
	 * 
	 * @param source
	 *            The object triggering the change
	 * @param type
	 *            The type of change
	 */
	protected void notifyChangeListeners(Object source,
			QueryGroupEventType type) {
		// Prevent any registered change listeners triggering a cascade of
		// further
		// change event firings
		for (final ChangeListener l : changeListeners) {
			l.stateChanged(new QueryGroupChangeEvent(source, type));
		}

	}

	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeIndex) {
		if (!hasQuery()) {
			return null;
		}

		final ObjectNode retVal = new ObjectMapper().createObjectNode()
				.put(TYPE_KEY, TYPE_GROUP)
				.put(LOGICAL_OPERATOR, getConjunction().getActionCommand());
		final ArrayNode nodes = retVal.putArray(NODES);
		for (final QueryFieldModel fModel : getFields()) {
			nodes.add(fModel.getQueryNodes(nodeIndex));
		}
		for (final QueryGroupModel gModel : getSubgroups()) {
			nodes.add(gModel.getQueryNodes(nodeIndex));
		}
		return retVal;
	}

	/**
	 * @return {@code true} if the query contains exactly 1 field or subgroup
	 */
	public boolean isSingleton() {
		return getFieldCount() + getSubGroupCount() == 1;
	}

	@Override
	public List<ChangeListener> getChangeListeners() {
		return Collections.unmodifiableList(changeListeners);
	}

}
