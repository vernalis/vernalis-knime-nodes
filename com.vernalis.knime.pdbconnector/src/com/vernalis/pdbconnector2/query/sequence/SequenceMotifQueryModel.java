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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.PATTERN_TYPE;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_SEQMOTIF;

/**
 * {@link AbstractSequenceQueryModel} implementation for Sequence Motif queries
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SequenceMotifQueryModel extends AbstractSequenceQueryModel {

	private static final String DEFAULT_KEY = "Sequence Motif";
	private static final String CFGKEY_MOTIF_TYPE = "motifType";
	private final SettingsModelString motifType = new SettingsModelString(
			CFGKEY_MOTIF_TYPE, SequenceMotifType.getDefault().name());

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            The model config key used during load/save
	 */
	public SequenceMotifQueryModel(String configKey) {
		super(configKey);
		motifType.addChangeListener(this);
	}

	/**
	 * Constructor using the default settings key
	 */
	public SequenceMotifQueryModel() {
		this(DEFAULT_KEY);
	}

	/**
	 * @return The {@link SequenceMotifType}
	 */
	public SequenceMotifType getMotifType() {
		return SequenceMotifType.valueOf(motifType.getStringValue());
	}

	/**
	 * Method to set the {@link SequenceMotifType}
	 * 
	 * @param motifType
	 *            The new type
	 */
	public void setMotifType(SequenceMotifType motifType) {
		this.motifType.setStringValue(motifType.name());
	}

	@Override
	protected void loadSubSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		motifType.loadSettingsFrom(settings);

	}

	@Override
	protected void validateSubSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			SequenceMotifType.valueOf(settings.getString(CFGKEY_MOTIF_TYPE));
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(e.getMessage(), e);
		}
	}

	@Override
	protected void saveSubSettings(NodeSettingsWO settings) {
		motifType.saveSettingsTo(settings);

	}

	@Override
	public void clearQuery() {
		setMotifType(SequenceMotifType.getDefault());
		super.clearQuery();

	}

	/**
	 * @return Get the motif type model
	 */
	SettingsModelString getModeModel() {
		return motifType;
	}

	@Override
	protected void addParams(ObjectNode params) {
		params.put(PATTERN_TYPE, getMotifType().getActionCommand());

	}

	@Override
	protected String getService() {
		return SERVICE_SEQMOTIF;
	}

}
