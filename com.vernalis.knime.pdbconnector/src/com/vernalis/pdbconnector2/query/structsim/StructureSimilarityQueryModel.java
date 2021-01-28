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
package com.vernalis.pdbconnector2.query.structsim;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

import static com.vernalis.pdbconnector2.RcsbJSONConstants.ASSEMBLY_IDS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.ASYM_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.CHAINS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.EMPTY_STRING;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.ENTRY_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.MSG;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.NODE_ID;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.OPERATOR;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.PARAMETERS;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_STRUCTURE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_KEY;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.TYPE_TERMINAL;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.VALUE;

/**
 * {@link QueryModel} for a Structure similarity query
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class StructureSimilarityQueryModel implements QueryModel {

	private static final String RCSB_STRUCTURE_QUERY_URL_MASK =
			"http://www.rcsb.org/search/structure/%s";

	private static final String CFGKEY_MATCH_UNIT_ID = "Match Unit ID";
	private static final String CFGKEY_MATCH_UNIT = "Match Unit";
	private static final String DEFAULT_KEY = "Structure Similarity";
	private static final String CFGKEY_SIM_TYPE = "simType";
	private static final String CFGKEY_PDB_ID = "PDB ID";

	private final SettingsModelString pdbId =
			new SettingsModelString(CFGKEY_PDB_ID, null);
	private final SettingsModelString matchUnitType = new SettingsModelString(
			CFGKEY_MATCH_UNIT, MatchUnitType.getDefault().getText());
	private final SettingsModelString matchUnitId =
			new SettingsModelString(CFGKEY_MATCH_UNIT_ID,
					MatchUnitType.getDefault().getDefaultIDs().get(0));
	private final SettingsModelString simType = new SettingsModelString(
			CFGKEY_SIM_TYPE, StructureSimilarityType.getDefault().name());
	private final String configKey;
	private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();
	private Map<MatchUnitType, List<String>> possibleIDs = new HashMap<>();

	/**
	 * The type of change to the model
	 * 
	 * @author S.Roughley knime@vernalis.com
	 * @since 1.28.0
	 *
	 */
	static enum EventType {
		/**
		 * The PDB ID changed
		 */
		PDBID_CHANGE,
		/**
		 * The match unit type changed
		 */
		MATCH_UNIT_TYPE_CHANGE,
		/**
		 * The id of the match unit changed
		 */
		MATCH_UNIT_ID_CHANGE,
		/**
		 * The similarity type changed
		 */
		SIMILARITY_TYPE_CHANGE;
	}

	private boolean isLocked = false;

	/**
	 * Overloaded constructur using the default settings key
	 */
	public StructureSimilarityQueryModel() {
		this(DEFAULT_KEY);
	}

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            the model config key for load/save
	 */
	public StructureSimilarityQueryModel(String configKey) {
		this.configKey = configKey;

		for (final MatchUnitType t : MatchUnitType.values()) {
			possibleIDs.put(t, t.getDefaultIDs());
		}

		pdbId.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {

				updateMatchUnitParams();

				notifyChangeListeners(EventType.PDBID_CHANGE);
			}
		});
		updateMatchUnitParams();

		simType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.SIMILARITY_TYPE_CHANGE);

			}
		});
		matchUnitType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.MATCH_UNIT_TYPE_CHANGE);

			}
		});
		setMatchUnitID(getPossibleIDs().get(0));
		matchUnitId.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(EventType.MATCH_UNIT_ID_CHANGE);

			}
		});
	}

	/**
	 * @return An unmodifiable view of the possible IDs for the current match
	 *         unit type
	 */
	public List<String> getPossibleIDs() {
		return Collections
				.unmodifiableList(possibleIDs.get(getMatchUnitType()));
	}

	/**
	 * Method to rebuild the possible IDs list for given PDB ID
	 * 
	 * @param pdbID
	 *            The PDB ID
	 * @return {@code true} if the list was updated
	 */
	protected boolean rebuildPossibleIDsList(String pdbID) {
		try {
			final URL url = new URL(
					String.format(RCSB_STRUCTURE_QUERY_URL_MASK, pdbID));
			final HttpURLConnection conn =
					(HttpURLConnection) url.openConnection();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				final JsonNode result =
						new ObjectMapper().readTree(conn.getInputStream());
				if (result.has(MSG)) {
					return false;
				}
				final JsonNode assemblyIDsNode = result.get(ASSEMBLY_IDS);
				final JsonNode chainIDsNode = result.get(CHAINS);
				if (!assemblyIDsNode.isArray() || !chainIDsNode.isArray()) {
					return false;
				}
				final List<String> newAssIDs = new ArrayList<>();
				assemblyIDsNode.elements()
						.forEachRemaining(id -> newAssIDs.add(id.asText()));
				if (newAssIDs.isEmpty()) {
					return false;
				}
				final List<String> newChIDs = new ArrayList<>();
				chainIDsNode.elements().forEachRemaining(
						chain -> newChIDs.add(chain.get(ASYM_ID).asText()));
				if (newChIDs.isEmpty()) {
					return false;
				}
				Collections.sort(newAssIDs);
				Collections.sort(newChIDs);
				possibleIDs.put(MatchUnitType.assembly_id, newAssIDs);
				possibleIDs.put(MatchUnitType.asym_id, newChIDs);
				return true;

			} else {
				return false;
			}
		} catch (final Exception e) {
			return false;
		}

	}

	/**
	 * @return The possible match unit type text values
	 */
	public List<String> getPossibleMatchUnits() {
		return Arrays.stream(MatchUnitType.values()).map(x -> x.getText())
				.collect(Collectors.toList());
	}

	/**
	 * @return the stored PDB ID
	 */
	public final String getPdbID() {
		return pdbId.getStringValue();
	}

	/**
	 * Set the stored PDB ID
	 * 
	 * @param pdbID
	 *            The new ID
	 */
	public final void setPdbID(String pdbID) {
		if (pdbID == null) {
			setPdbID(EMPTY_STRING);
			return;
		}
		this.pdbId.setStringValue(pdbID.toUpperCase());
	}

	/**
	 * @return The stored similarity type
	 */
	public final StructureSimilarityType getType() {
		return StructureSimilarityType.fromString(simType.getStringValue());
	}

	/**
	 * Set the stored similarity type
	 * 
	 * @param type
	 *            the new type
	 */
	public final void setType(StructureSimilarityType type) {
		this.simType.setStringValue(type.getText());
	}

	/**
	 * @return The stored match unit type
	 */
	public final MatchUnitType getMatchUnitType() {
		return validateMatchUnitType(matchUnitType.getStringValue());
	}

	/**
	 * Set the stored match unit type
	 * 
	 * @param matchUnitType
	 *            The new match unit type
	 */
	public final void setMatchUnitType(MatchUnitType matchUnitType) {
		this.matchUnitType.setStringValue(matchUnitType.getText());
	}

	/**
	 * @return the stored match unit ID
	 */
	public final String getMatchUnitID() {
		return matchUnitId.getStringValue();
	}

	/**
	 * Set the stored match unit ID
	 * 
	 * @param unitId
	 *            The new match unit ID
	 */
	public void setMatchUnitID(String unitId) {
		validateMatchUnitID(unitId);
		matchUnitId.setStringValue(unitId);
	}

	private void validateMatchUnitID(String unitId) {
		// Only validate the unit id if we have a query
		if (hasQuery()) {
			if (!getPossibleIDs().contains(unitId)) {
				throw new IllegalArgumentException(
						"'" + unitId + "' is not a valid unit id");
			}
		}
	}

	private MatchUnitType validateMatchUnitType(String matchUnitType) {
		return MatchUnitType.fromString(matchUnitType);
	}

	@Override
	public boolean hasQuery() {
		return getPdbID() != null && getPdbID().length() == 4;
	}

	/**
	 * @return the model key
	 */
	public final String getConfigKey() {
		return configKey;
	}

	@Override
	public final void saveSettingsTo(NodeSettingsWO settings) {
		final NodeSettingsWO mySettings = settings.addNodeSettings(configKey);
		pdbId.saveSettingsTo(mySettings);
		simType.saveSettingsTo(mySettings);
		matchUnitType.saveSettingsTo(mySettings);
		matchUnitId.saveSettingsTo(mySettings);
	}

	@Override
	public final void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		new StructureSimilarityQueryModel(getConfigKey())
				.loadSettings(settings);
	}

	@Override
	public final void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final NodeSettingsRO mySettings = settings.getNodeSettings(configKey);
		InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();
		try {
			setPdbID(mySettings.getString(CFGKEY_PDB_ID));
		} catch (InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			setType(StructureSimilarityType
					.fromString(mySettings.getString(CFGKEY_SIM_TYPE)));
		} catch (InvalidSettingsException e) {
			isec.add(e);
		} catch (NullPointerException | IllegalArgumentException e) {
			isec.add(new InvalidSettingsException(
					"Error parsing Similarity Type", e));
		}
		try {
			setMatchUnitType(validateMatchUnitType(
					mySettings.getString(CFGKEY_MATCH_UNIT)));
		} catch (InvalidSettingsException e) {
			isec.add(e);
		} catch (NullPointerException | IllegalArgumentException e) {
			isec.add(new InvalidSettingsException(
					"Error parsing Similarity Type", e));
		}
		try {
			setMatchUnitID(mySettings.getString(CFGKEY_MATCH_UNIT_ID));
		} catch (InvalidSettingsException e) {
			isec.add(e);
		}
		isec.throwAll();
	}

	@Override
	public final void addChangeListener(ChangeListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
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

	/**
	 * @return The model for the PDB ID
	 */
	public SettingsModelString getPdbIdModel() {
		return pdbId;
	}

	/**
	 * @return The model for the similarity type
	 */
	public SettingsModelString getSimilarityTypeModel() {
		return simType;
	}

	/**
	 * @return The model for the match unit type
	 */
	public SettingsModelString getMatchUnitTypeModel() {
		return matchUnitType;
	}

	/**
	 * @return The model for the match unit ID
	 */
	public SettingsModelString getMatchUnitIdModel() {
		return matchUnitId;
	}

	@Override
	public void clearQuery() {
		setPdbID(EMPTY_STRING);
		setType(StructureSimilarityType.getDefault());
		setMatchUnitType(MatchUnitType.getDefault());
		for (final MatchUnitType t : MatchUnitType.values()) {
			possibleIDs.put(t, t.getDefaultIDs());
		}
		setMatchUnitID(MatchUnitType.getDefault().getDefaultIDs().get(0));
	}

	private void updateMatchUnitParams() {
		final boolean enabled =
				hasQuery() && rebuildPossibleIDsList(getPdbID());
		matchUnitType.setEnabled(enabled);
		matchUnitId.setEnabled(enabled);

	}

	@Override
	public List<ChangeListener> getChangeListeners() {
		return Collections.unmodifiableList(listeners);
	}

	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeId) {
		if (!hasQuery()) {
			return null;
		}
		final ObjectNode retVal = new ObjectMapper().createObjectNode();
		retVal.put(TYPE_KEY, TYPE_TERMINAL);
		retVal.put(NODE_ID, nodeId.getAndIncrement());
		retVal.put(SERVICE_KEY, SERVICE_STRUCTURE);
		final ObjectNode params = retVal.putObject(PARAMETERS);
		params.put(OPERATOR, getType().getActionCommand());
		final ObjectNode val = params.putObject(VALUE);
		val.put(ENTRY_ID, getPdbID());
		val.put(getMatchUnitType().getActionCommand(), getMatchUnitID());
		return retVal;
	}

}
