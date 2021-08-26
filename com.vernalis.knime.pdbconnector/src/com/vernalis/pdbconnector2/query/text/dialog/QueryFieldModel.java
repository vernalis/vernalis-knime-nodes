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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.fasterxml.jackson.databind.JsonNode;
import com.vernalis.pdbconnector2.InvalidSettingsExceptionCombiner;
import com.vernalis.pdbconnector2.query.QueryModel;
import com.vernalis.pdbconnector2.query.text.fields.InvalidQueryField;
import com.vernalis.pdbconnector2.query.text.fields.QueryField;
import com.vernalis.pdbconnector2.query.text.fields.QueryFieldRegistry;

/**
 * An implementation of {@link QueryModel} representing a single text query
 * field.
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryFieldModel implements QueryModel {

	/**
	 * A {@link QueryFieldModel} with limited functionality to represent an
	 * invalid query - most likely one removed from the remote API
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.30.3
	 */
	public static class InvalidQueryFieldModel extends QueryFieldModel {

		private final NodeSettings brokenSettings;

		/**
		 * Constructor
		 * 
		 * @param brokenSettings
		 *            The settings object for the broken field
		 */
		public InvalidQueryFieldModel(NodeSettingsRO brokenSettings) {
			super((String) null);
			this.brokenSettings = (NodeSettings) brokenSettings;
		}

		@Override
		public QueryField getQueryField() {
			return new InvalidQueryField(brokenSettings);
		}

		/**
		 * @return The broken Node Settings object for the field
		 */
		public NodeSettingsRO getSettings() {
			return brokenSettings;
		}

		@Override
		public boolean hasInvalidQuery() {
			return true;
		}

		@Override
		public void setIsNot(boolean isNot) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getIsNot() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setOperator(QueryFieldOperator operator) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasOperator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public QueryFieldOperator getOperator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void validateSettings(NodeSettingsRO settings)
				throws InvalidSettingsException {
			// do nothing
		}

		@Override
		public void loadSettings(NodeSettingsRO settings)
				throws InvalidSettingsException {
			// do nothing
		}

		@Override
		public void saveSettingsTo(NodeSettingsWO settings) {
			settings.addNodeSettings(brokenSettings);
		}

		@Override
		public SettingsModelString getQueryTypeModel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public SettingsModelString getQueryOperatorModel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void clearQueryFieldValueModel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setQueryFieldValueModel(SettingsModel settingsModel) {
			throw new UnsupportedOperationException();
		}

		@Override
		public SettingsModel getQueryFieldValueModel() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasQuery() {
			return false;
		}

		@Override
		public void clearQuery() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ChangeListener> getChangeListeners() {
			return Collections.emptyList();
		}

		@Override
		public JsonNode getQueryNodes(AtomicInteger nodeId) {
			throw new UnsupportedOperationException();
		}

	}

	private static final String CFGKEY_ATTRIBUTE = "attribute";
	private static final String CFGKEY_OPERATOR = "operator";
	private static final String CFGKEY_IS_NOT = "isNot";

	/**
	 * An change even type for the query field
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.28.0
	 *
	 */
	public static enum QueryFieldEventType {
		/** The query type has changed */
		QueryTypeChange,
		/** The query operator has changed */
		OperatorChange,
		/** The query inversion ('NOT') value has changed */
		InversionChange,
		/** The query value has changed */
		FieldValueChange,
		/** The query value model has changed */
		FieldValueModelChange;
	}

	private final SettingsModelBoolean isNotMdl =
			new SettingsModelBoolean(CFGKEY_IS_NOT, false);
	private final SettingsModelString operatorMdl =
			new SettingsModelString(CFGKEY_OPERATOR, null);
	private final SettingsModelString attributeMdl;
	private SettingsModel queryFieldValueModel;
	private boolean isLocked = false;

	private final List<ChangeListener> changeListeners =
			new CopyOnWriteArrayList<>();

	/**
	 * Constructor for a specified query type ('attribute'). All other settings
	 * will be set to the default values for the given field
	 * 
	 * @param attribute
	 *            The attribute type
	 */
	public QueryFieldModel(String attribute) {

		this.attributeMdl =
				new SettingsModelString(CFGKEY_ATTRIBUTE, attribute);
		// null is passed when the query is invalid
		if (attribute != null) {
			setOperator(getQueryField().getDefaultOperator());
			queryFieldValueModel =
					getQueryField().getValueSettingsModel(getOperator());
			isNotMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					notifyChangeListeners(QueryFieldEventType.InversionChange);

				}
			});

			operatorMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					notifyChangeListeners(QueryFieldEventType.OperatorChange);

				}
			});
			attributeMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					notifyChangeListeners(QueryFieldEventType.QueryTypeChange);

				}
			});
		}
	}

	/**
	 * Constructor from a saved {@link NodeSettingsRO} object
	 * 
	 * @param nodeSettings
	 *            The settings object
	 * @throws InvalidSettingsException
	 *             If there was an error loading the settings during
	 *             construction
	 */
	public QueryFieldModel(NodeSettingsRO nodeSettings)
			throws InvalidSettingsException {
		attributeMdl = new SettingsModelString(CFGKEY_ATTRIBUTE, null);
		attributeMdl.loadSettingsFrom(nodeSettings);
		operatorMdl.loadSettingsFrom(nodeSettings);
		attributeMdl.loadSettingsFrom(nodeSettings);
		operatorMdl.loadSettingsFrom(nodeSettings);
		final QueryField queryField = getQueryField();
		if (queryField == null) {
			throw new KnowsROSettingsInvalidSettingsException("Query field '"
					+ attributeMdl.getStringValue() + "' not found!",
					nodeSettings);
		}
		queryFieldValueModel = queryField.getValueSettingsModel(getOperator());
		if (queryFieldValueModel != null) {
			queryFieldValueModel.loadSettingsFrom(nodeSettings);
		}
		isNotMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(QueryFieldEventType.InversionChange);

			}
		});

		operatorMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(QueryFieldEventType.OperatorChange);

			}
		});
		attributeMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(QueryFieldEventType.QueryTypeChange);

			}
		});
	}

	/**
	 * Overloaded constructor with the default query field
	 */
	public QueryFieldModel() {
		this(QueryFieldRegistry.getInstance().getDefault().getUniqueID());
	}

	/**
	 * @return The current {@link QueryField}
	 */
	public QueryField getQueryField() {
		return QueryFieldRegistry.getInstance()
				.getField(attributeMdl.getStringValue());
	}

	/**
	 * Set the inversion ('NOT') setting
	 * 
	 * @param isNot
	 *            the new value
	 */
	public void setIsNot(boolean isNot) {
		isNotMdl.setBooleanValue(isNot);
	}

	/**
	 * @return whether the query is inverted
	 */
	public boolean getIsNot() {
		return isNotMdl.getBooleanValue();
	}

	/**
	 * Set the query operator
	 * 
	 * @param operator
	 *            The new operator
	 */
	public void setOperator(QueryFieldOperator operator) {
		operatorMdl.setStringValue(
				operator == null ? null : operator.getDisplayName());
	}

	/**
	 * @return whether there is an operator stored
	 */
	public boolean hasOperator() {
		return operatorMdl.getStringValue() != null;
	}

	/**
	 * @return The stored operator, or {@code null} if no operator is stored
	 */
	public QueryFieldOperator getOperator() {
		return operatorMdl.getStringValue() == null ? null
				: QueryFieldOperator
						.fromDisplayName(operatorMdl.getStringValue());
	}

	/**
	 * @return the model type ID
	 */
	protected String getModelTypeID() {
		return "SMID_" + getClass().getName().replace("Model", "");
	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// We want to know everything that's wrong with the model so we collect
		// them all
		// here...
		final InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();

		try {
			isNotMdl.validateSettings(settings);
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			QueryFieldOperator
					.fromDisplayName(settings.getString(CFGKEY_OPERATOR));
		} catch (IllegalArgumentException | NullPointerException e) {
			isec.add(new InvalidSettingsException(e.getMessage(), e));
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			if (QueryFieldRegistry.getInstance()
					.getField(settings.getString(CFGKEY_ATTRIBUTE)) == null) {
				isec.add(new InvalidSettingsException(
						"Query field '" + settings.getString(CFGKEY_ATTRIBUTE)
								+ "' not found in registry"));
			}
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			if (queryFieldValueModel != null) {
				queryFieldValueModel.validateSettings(settings);
			}
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}

		isec.throwAll();

	}

	@Override
	public void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			isNotMdl.loadSettingsFrom(settings);
			operatorMdl.loadSettingsFrom(settings);
			attributeMdl.loadSettingsFrom(settings);
			if (queryFieldValueModel != null) {
				queryFieldValueModel.loadSettingsFrom(settings);
			}
		} catch (final InvalidSettingsException ise) {
			throw new InvalidSettingsException(
					getClass().getSimpleName() + ": " + ise.getMessage());
		}

	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		// The settings object has it's key created by QueryGroupModel, so here
		// we just
		// save direct to it
		isNotMdl.saveSettingsTo(settings);
		operatorMdl.saveSettingsTo(settings);
		attributeMdl.saveSettingsTo(settings);
		if (queryFieldValueModel != null) {
			queryFieldValueModel.saveSettingsTo(settings);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * @return the query type model
	 */
	public SettingsModelString getQueryTypeModel() {
		return attributeMdl;
	}

	/**
	 * @return the query operator model
	 */
	public SettingsModelString getQueryOperatorModel() {
		return operatorMdl;
	}

	/**
	 * Clear the field value
	 */
	public void clearQueryFieldValueModel() {
		queryFieldValueModel = null;
	}

	/**
	 * Set a new model as the field value model
	 * 
	 * @param settingsModel
	 *            The new model
	 */
	public void setQueryFieldValueModel(SettingsModel settingsModel) {
		queryFieldValueModel = settingsModel;
		queryFieldValueModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(QueryFieldEventType.FieldValueChange);
			}
		});
		notifyChangeListeners(QueryFieldEventType.FieldValueModelChange);

	}

	/**
	 * @return The field value model
	 */
	public SettingsModel getQueryFieldValueModel() {
		return queryFieldValueModel;
	}

	/**
	 * @return the inversion ('NOT') model
	 */
	public final SettingsModelBoolean getIsNotMdl() {
		return isNotMdl;
	}

	@Override
	public void addChangeListener(ChangeListener l) {
		if (!changeListeners.contains(l)) {
			changeListeners.add(l);
		}
	}

	/**
	 * Add a new {@link ChangeListener} to the start of the registered listeners
	 * 
	 * @param l
	 *            the new change listener
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
	 * Method to notify changed listeners that the model has changed. A call
	 * locks the model until all listeners have been notified to prevent
	 * multiple calls
	 * 
	 * @param type
	 *            The change type
	 */
	protected void notifyChangeListeners(QueryFieldEventType type) {
		// Dont fire the change listeners...
		if (isLocked) {
			return;
		}

		// Prevent any registered change listeners triggering a cascade of
		// further
		// change event firings
		isLocked = true;
		for (final ChangeListener l : changeListeners) {
			l.stateChanged(new ChangeEvent(type));
		}
		// And unlock
		isLocked = false;
	}

	@Override
	public boolean hasQuery() {

		if (queryFieldValueModel instanceof SettingsModelString) {
			if (getOperator() == QueryFieldOperator.exists) {
				return true;
			}
			final String val = ((SettingsModelString) queryFieldValueModel)
					.getStringValue();
			return val != null && !val.isEmpty();

		}
		// All other models always contain values, or there is a null model,
		// which means
		// the query has no user-supplied input
		return true;
	}

	@Override
	public void clearQuery() {
		attributeMdl.setStringValue(
				QueryFieldRegistry.getInstance().getDefault().getUniqueID());
		// Force it to redevelop UI even if we had a default query type...
		notifyChangeListeners(QueryFieldEventType.QueryTypeChange);
	}

	@Override
	public List<ChangeListener> getChangeListeners() {
		return Collections.unmodifiableList(changeListeners);
	}

	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeId) {
		return getQueryField().getQuery(nodeId, this);
	}
}
