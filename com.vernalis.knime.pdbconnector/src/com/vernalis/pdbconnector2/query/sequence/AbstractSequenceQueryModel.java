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
package com.vernalis.pdbconnector2.query.sequence;

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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.InvalidSettingsExceptionCombiner;
import com.vernalis.pdbconnector2.query.QueryModel;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODE_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TARGET;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_TERMINAL;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.VALUE;

/**
 * An abstract base {@link QueryModel} for sequence queries
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public abstract class AbstractSequenceQueryModel
		implements ChangeListener, QueryModel {

	private static final String CFGKEY_TARGET = TARGET;

	private static final String CFGKEY_SEQUENCE = "sequence";

	/**
	 * The sequence model
	 */
	protected final SettingsModelString sequence =
			new SettingsModelString(CFGKEY_SEQUENCE, null);

	/**
	 * The Sequence target model
	 */
	protected final SettingsModelString target = new SettingsModelString(
			CFGKEY_TARGET, SequenceTarget.getDefault().name());

	/**
	 * The model config key for loading/saving
	 */
	protected final String configKey;
	private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            The node settings config key for loading and saving
	 */
	public AbstractSequenceQueryModel(String configKey) {
		this.configKey = configKey;
		sequence.addChangeListener(this);
		target.addChangeListener(this);
	}

	@Override
	public final void stateChanged(ChangeEvent e) {
		notifyChangeListeners(e);

	}

	/**
	 * @return The sequence
	 */
	public final String getSequence() {
		return sequence.getStringValue();
	}

	/**
	 * Set the sequence
	 * 
	 * @param sequence
	 *            the new sequence
	 */
	public final void setSequence(String sequence) {
		if (sequence == null) {
			setSequence("");
			return;
		}
		this.sequence.setStringValue(sequence);
	}

	/**
	 * @return The target
	 */
	public final SequenceTarget getTarget() {
		return SequenceTarget.valueOf(target.getStringValue());
	}

	/**
	 * Set the target
	 * 
	 * @param target
	 *            the new target value
	 */
	public final void setTarget(SequenceTarget target) {
		this.target.setStringValue(target.getText());
	}

	/**
	 * @return The config key
	 */
	public final String getConfigKey() {
		return configKey;
	}

	@Override
	public final void saveSettingsTo(NodeSettingsWO settings) {
		final NodeSettingsWO mySettings = settings.addNodeSettings(configKey);
		sequence.saveSettingsTo(mySettings);
		target.saveSettingsTo(mySettings);
		saveSubSettings(mySettings);
	}

	/**
	 * Abstract method for implementing classes to add their settings to the
	 * saved settings object. A new sub-object using the key is not required
	 * 
	 * @param mySettings
	 *            The sub-object to save into, created via
	 *            {@code settings.addNodeSettings(configKey)}
	 */
	protected abstract void saveSubSettings(final NodeSettingsWO mySettings);

	@Override
	public final void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();
		try {
			final NodeSettingsRO mySettings =
					settings.getNodeSettings(configKey);
			sequence.validateSettings(mySettings);
			try {
				SequenceTarget.valueOf(mySettings.getString(CFGKEY_TARGET));
			} catch (IllegalArgumentException | NullPointerException e) {
				isec.add(new InvalidSettingsException(e.getMessage(), e));
			} catch (final InvalidSettingsException e) {
				isec.add(e);
			}
			validateSubSettings(mySettings);
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		isec.throwAll();
	}

	/**
	 * Abstract method for implementing classes to validate their settings
	 * 
	 * @param mySettings
	 *            the sub-object to validate from (created via
	 *            {@code settings.getNodeSettings(configKey)}
	 * @throws InvalidSettingsException
	 *             if any settings validation issues are identified.
	 *             Implementations should validate all settings, catching all
	 *             thrown {@link InvalidSettingsException} into a
	 *             {@link InvalidSettingsExceptionCombiner} and calling
	 *             {@link InvalidSettingsExceptionCombiner#throwAll()} as the
	 *             final step
	 */
	protected abstract void validateSubSettings(final NodeSettingsRO mySettings)
			throws InvalidSettingsException;

	@Override
	public final void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final NodeSettingsRO mySettings = settings.getNodeSettings(configKey);
		setSequence(mySettings.getString(CFGKEY_SEQUENCE));

		final String seqTgtStr = mySettings.getString(CFGKEY_TARGET);
		try {
			setTarget(SequenceTarget.valueOf(seqTgtStr));
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"Sequence target '" + seqTgtStr + "' not valid");
		}
		loadSubSettings(mySettings);
	}

	/**
	 * Abstract method for implementing classes to load their settings
	 * 
	 * @param mySettings
	 *            the sub-object to load from (created via
	 *            {@code settings.getNodeSettings(configKey)}
	 * @throws InvalidSettingsException
	 *             if any settings validation issues are identified during load
	 */
	protected abstract void loadSubSettings(final NodeSettingsRO mySettings)
			throws InvalidSettingsException;

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

	private final void notifyChangeListeners(ChangeEvent source) {
		for (final ChangeListener l : listeners) {
			l.stateChanged(source);
		}
	}

	@Override
	public boolean hasQuery() {
		return getSequence() != null && !getSequence().isEmpty();
	}

	@Override
	public void clearQuery() {
		setTarget(SequenceTarget.getDefault());
		setSequence(EMPTY_STRING);

	}

	/**
	 * @return The model containing the sequence
	 */
	SettingsModelString getSequenceModel() {
		return sequence;
	}

	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeId) {
		if (!hasQuery()) {
			return null;
		}
		final ObjectNode retVal = new ObjectMapper().createObjectNode();
		retVal.put(TYPE_KEY, TYPE_TERMINAL);
		retVal.put(NODE_ID, nodeId.getAndIncrement());
		retVal.put(SERVICE_KEY, getService());
		final ObjectNode params = retVal.putObject(PARAMETERS);
		params.put(VALUE, getSequence());
		params.put(TARGET, getTarget().getActionCommand());
		addParams(params);
		return retVal;
	}

	/**
	 * @param params
	 *            The JSON 'parameters' object from the query node, to add
	 *            implementation-specific parameters to
	 */
	protected abstract void addParams(ObjectNode params);

	/**
	 * @return The query service name for the implementing class
	 */
	protected abstract String getService();

	/**
	 * @return The model for the target setting
	 */
	SettingsModelString getTargetModel() {
		return target;
	}

}
