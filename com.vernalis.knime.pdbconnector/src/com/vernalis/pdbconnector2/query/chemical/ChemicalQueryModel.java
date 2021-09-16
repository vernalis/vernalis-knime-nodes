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
package com.vernalis.pdbconnector2.query.chemical;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.InvalidSettingsExceptionCombiner;
import com.vernalis.pdbconnector2.RcsbJSONConstants;
import com.vernalis.pdbconnector2.query.QueryModel;
import com.vernalis.pdbconnector2.query.ScoringType;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODE_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_TERMINAL;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.VALUE;

/**
 * A {@link QueryModel} implementation representing a chemical query
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class ChemicalQueryModel implements QueryModel {

	private static final boolean DEFAULT_MATCH_SUBSET = false;
	private static final String CFGKEY_MATCH_SUBSET = "matchSubset";
	private static final String CFGKEY_QUERY = "query";
	private static final String CFGKEY_MATCH_TYPE = "matchType";
	private static final String CFGKEY_DESCRIPTOR_TYPE = "descriptorType";
	private static final String CFGKEY_QUERY_TYPE = "queryType";
	private static final String DEFAULT_KEY = "Chemical Query";
	private final SettingsModelString queryType =
			new SettingsModelString(CFGKEY_QUERY_TYPE,
					ChemicalQueryType.getDefault().getActionCommand());
	private final SettingsModelString descriptorType =
			new SettingsModelString(CFGKEY_DESCRIPTOR_TYPE,
					ChemicalDescriptorType.getDefault().getActionCommand());
	private final SettingsModelString matchType =
			new SettingsModelString(CFGKEY_MATCH_TYPE,
					ChemicalMatchType.getDefault().getActionCommand());
	private final SettingsModelString queryText =
			new SettingsModelString(CFGKEY_QUERY, EMPTY_STRING);
	private final SettingsModelBoolean matchSubset =
			new SettingsModelBoolean(CFGKEY_MATCH_SUBSET, DEFAULT_MATCH_SUBSET);

	private final String configKey;
	private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * An enum representing the possible model changes
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.28.0
	 *
	 */
	enum EventType {
		/** The query type has changed */
		QUERY_TYPE_CHANGE,
		/** The query input string has changed */
		STRING_CHANGE,
		/** The match subset option has changed */
		MATCH_SUBSET_CHANGE,
		/** The descriptor type has changed */
		DESCRIPTOR_TYPE_CHANGE,
		/** The match type has changed */
		MATCH_TYPE_CHANGE
	};

	private boolean isLocked = false;

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            The settings config key for loading and saving the model
	 */
	public ChemicalQueryModel(String configKey) {
		this.configKey = configKey;

		// Register change listeners to all the individual models
		queryType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateEnabledStatuses();
				notifyChangeListeners(EventType.QUERY_TYPE_CHANGE);

			}
		});
		descriptorType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.DESCRIPTOR_TYPE_CHANGE);

			}
		});
		matchType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.MATCH_TYPE_CHANGE);

			}
		});
		queryText.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.STRING_CHANGE);

			}
		});
		matchSubset.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.MATCH_SUBSET_CHANGE);

			}
		});
		updateEnabledStatuses();
	}

	/**
	 * Keyless constructor, using the default settings key
	 */
	public ChemicalQueryModel() {
		this(DEFAULT_KEY);
	}

	/**
	 * Method to update the enabled status of the various settings models based
	 * on the current query type
	 */
	protected void updateEnabledStatuses() {
		boolean hasDescriptorType = getQueryType().hasDescriptorType();
		matchSubset.setEnabled(!hasDescriptorType);
		matchType.setEnabled(hasDescriptorType);
		descriptorType.setEnabled(hasDescriptorType);
	}

	/**
	 * @return The settings config key for loading and saving
	 */
	public final String getConfigKey() {
		return configKey;
	}

	/**
	 * @return The settings model containing the input text
	 */
	SettingsModelString getTextInputModel() {
		return queryText;
	}

	/**
	 * @return The input text
	 */
	public String getQueryText() {
		return getTextInputModel().getStringValue();
	}

	/**
	 * Method to set the input text
	 * 
	 * @param newValue
	 *            The new input value to set
	 */
	public void setQueryText(String newValue) {
		getTextInputModel()
				.setStringValue(newValue == null ? EMPTY_STRING : newValue);
	}

	/**
	 * @return The query type model
	 */
	SettingsModelString getQueryTypeModel() {
		return queryType;
	}

	/**
	 * @return the query type
	 */
	public ChemicalQueryType getQueryType() {
		return ChemicalQueryType.fromActionCommand(queryType.getStringValue());
	}

	/**
	 * Method to set a new query type
	 * 
	 * @param newValue
	 *            the new query type to set
	 */
	public void setQueryType(ChemicalQueryType newValue) {
		queryType.setStringValue(newValue.getActionCommand());
	}

	/**
	 * @return the descriptor type model
	 */
	SettingsModelString getDescriptorTypeModel() {
		return descriptorType;
	}

	/**
	 * @return The descriptor type, or {@code null} if the current query type
	 *         does not have a descriptor type
	 */
	public ChemicalDescriptorType getDescriptorType() {
		return getQueryType().hasDescriptorType()
				? ChemicalDescriptorType
						.valueOf(descriptorType.getStringValue())
				: null;
	}

	/**
	 * Method to set a new descriptor type. Does nothing if the current query
	 * type does not have a descriptor type
	 * 
	 * @param newValue
	 *            The new descriptor type to set
	 */
	public void setDescriptorType(ChemicalDescriptorType newValue) {
		if (getQueryType().hasDescriptorType()) {
			descriptorType.setStringValue(newValue.getActionCommand());
		}
	}

	/**
	 * @return The match type
	 */
	SettingsModelString getMatchTypeModel() {
		return matchType;
	}

	/**
	 * @return The match type, or {@code null} if the current query type does
	 *         not have a descriptor type
	 */
	public ChemicalMatchType getMatchType() {
		return getQueryType().hasDescriptorType()
				? ChemicalMatchType.getFromText(matchType.getStringValue())
				: null;
	}

	/**
	 * Method to set a new match type. Does nothing if the current query type
	 * does not have a descriptor type
	 * 
	 * @param newValue
	 *            The new descriptor type to set
	 */
	public void setMatchType(ChemicalMatchType newValue) {
		if (getQueryType().hasDescriptorType()) {
			matchType.setStringValue(newValue.getActionCommand());
		}
	}

	/**
	 * @return the match subset model
	 */
	SettingsModelBoolean getMatchSubsetModel() {
		return matchSubset;
	}

	/**
	 * @return the value of the match subset model
	 */
	public Boolean getMatchSubset() {
		return !getQueryType().hasDescriptorType()
				? matchSubset.getBooleanValue()
				: null;
	}

	/**
	 * Method to set a new value for the match subset setting
	 * 
	 * @param newValue
	 *            The new value to set
	 */
	public void setMatchSubset(boolean newValue) {
		if (!getQueryType().hasDescriptorType()) {
			matchSubset.setBooleanValue(newValue);
		}
	}

	@Override
	public final void saveSettingsTo(NodeSettingsWO settings) {
		final NodeSettingsWO mySettings = settings.addNodeSettings(configKey);
		queryText.saveSettingsTo(mySettings);
		queryType.saveSettingsTo(mySettings);
		descriptorType.saveSettingsTo(mySettings);
		matchType.saveSettingsTo(mySettings);
		matchSubset.saveSettingsTo(mySettings);
	}

	@Override
	public final void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();
		try {
			final NodeSettingsRO mySettings =
					settings.getNodeSettings(configKey);
			queryText.validateSettings(mySettings);
			matchSubset.validateSettings(mySettings);
			// As we save/load all options in whatever state they are in we need
			// to validate them all regardless of utility in current query
			try {
				ChemicalQueryType
						.valueOf(mySettings.getString(CFGKEY_QUERY_TYPE));
			} catch (IllegalArgumentException | NullPointerException e) {
				isec.add(new InvalidSettingsException(e.getMessage(), e));
			} catch (final InvalidSettingsException e) {
				isec.add(e);
			}
			try {
				ChemicalDescriptorType
						.valueOf(mySettings.getString(CFGKEY_DESCRIPTOR_TYPE));
			} catch (IllegalArgumentException | NullPointerException e) {
				isec.add(new InvalidSettingsException(e.getMessage(), e));
			} catch (final InvalidSettingsException e) {
				isec.add(e);
			}
			try {
				ChemicalMatchType
						.getFromText(mySettings.getString(CFGKEY_MATCH_TYPE));
			} catch (IllegalArgumentException | NullPointerException e) {
				isec.add(new InvalidSettingsException(e.getMessage(), e));
			} catch (final InvalidSettingsException e) {
				isec.add(e);
			}
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		isec.throwAll();
	}

	@Override
	public final void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final NodeSettingsRO mySettings = settings.getNodeSettings(configKey);
		// Use the setter to prevent null being applied
		setQueryText(mySettings.getString(CFGKEY_QUERY));

		// Load everything else directly as we will potentially need later of
		// type changes
		// should already be validated so no need to recheck here
		queryType.loadSettingsFrom(mySettings);
		descriptorType.loadSettingsFrom(mySettings);
		matchType.loadSettingsFrom(mySettings);
		matchSubset.loadSettingsFrom(mySettings);
	}

	@Override
	public final void addChangeListener(ChangeListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	@Override
	public final List<ChangeListener> getChangeListeners() {
		return Collections.unmodifiableList(listeners);
	}

	@Override
	public final void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	private final void notifyChangeListeners(EventType type) {
		// Dont fire the change listeners...
		if (isLocked) {
			return;
		}

		// Prevent any registered change listeners triggering a cascade of
		// further
		// change event firings
		isLocked = true;
		for (final ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(type));
		}
		// And unlock
		isLocked = false;
	}

	@Override
	public boolean hasQuery() {
		return getQueryText() != null && !getQueryText().isEmpty();
	}

	@Override
	public void clearQuery() {
		setQueryText(EMPTY_STRING);
		setQueryType(ChemicalQueryType.getDefault());
		// Need to set these directly as the setters will ignore if not
		// applicable
		matchSubset.setBooleanValue(DEFAULT_MATCH_SUBSET);
		descriptorType.setStringValue(
				ChemicalDescriptorType.getDefault().getActionCommand());
		matchType.setStringValue(
				ChemicalMatchType.getDefault().getActionCommand());
	}

	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeId) {
		if (!hasQuery()) {
			return null;
		}
		final ObjectNode retVal = new ObjectMapper().createObjectNode();
		retVal.put(TYPE_KEY, TYPE_TERMINAL);
		retVal.put(NODE_ID, nodeId.getAndIncrement());
		retVal.put(SERVICE_KEY, RcsbJSONConstants.SERVICE_CHEMICAL);
		final ObjectNode params = retVal.putObject(PARAMETERS);
		params.put(RcsbJSONConstants.TYPE, getQueryType().getActionCommand());
		params.put(VALUE, getQueryText());
		addParams(params);
		return retVal;
	}

	/**
	 * Method to add the appropriate additional query parameters to the query
	 * node - called from {@link #getQueryNodes(AtomicInteger)}
	 * 
	 * @param params
	 *            The 'parameters' JSON object to add any additional parameters
	 *            to
	 * 
	 */
	protected void addParams(ObjectNode params) {
		switch (getQueryType()) {
			case Descriptor:
				params.put(RcsbJSONConstants.DESCRIPTOR_TYPE,
						getDescriptorType().getActionCommand());
				params.put(RcsbJSONConstants.MATCH_TYPE,
						getMatchType().getActionCommand());
				break;
			case Formula:
				params.put(RcsbJSONConstants.MATCH_SUBSET, getMatchSubset());
				break;
			default:
				throw new IllegalArgumentException(
						"Unknown chemical query type");
		}
	}

	@Override
	public boolean isScoringTypeValid(ScoringType scoringType) {
		return scoringType == ScoringType.Chemical
				|| scoringType == ScoringType.Combined;
	};

}
