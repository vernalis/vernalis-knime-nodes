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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelNumber;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.InvalidSettingsExceptionCombiner;
import com.vernalis.pdbconnector2.query.ScoringType;

import static com.vernalis.pdbconnector2.RcsbJSONConstants.EVALUE_CUTOFF;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.IDENTITY_CUTOFF;
import static com.vernalis.pdbconnector2.RcsbJSONConstants.SERVICE_SEQUENCE;

/**
 * {@link AbstractSequenceQueryModel} implementation for sequence queries
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SequenceQueryModel extends AbstractSequenceQueryModel {

	private static final String CFGKEY_IDENTITY = "identity";
	private static final String CFGKEY_EVALUE = "evalue";
	private static final int DEFAULT_IDENTITY = 25;
	private static final int DEFAULT_EVALUE = 1000000;

	private static final String DEFAULT_KEY = SERVICE_SEQUENCE;
	private final SettingsModelDoubleBounded eValue =
			new SettingsModelDoubleBounded(CFGKEY_EVALUE, DEFAULT_EVALUE, 0,
					Double.MAX_VALUE);
	private final SettingsModelIntegerBounded identity =
			new SettingsModelIntegerBounded(CFGKEY_IDENTITY, DEFAULT_IDENTITY,
					0, 100);

	/**
	 * Constructor
	 * 
	 * @param configKey
	 *            The settings key used during model loading / saving
	 */
	public SequenceQueryModel(String configKey) {
		super(configKey);
		eValue.addChangeListener(this);
		identity.addChangeListener(this);
	}

	/**
	 * Overloaded constructor using the default settings key
	 */
	public SequenceQueryModel() {
		this(DEFAULT_KEY);
	}

	/**
	 * @return The query's e-Value
	 */
	public double getEValue() {
		return eValue.getDoubleValue();
	}

	/**
	 * Set the e-Value
	 * 
	 * @param eValue
	 *            The new e-Value
	 */
	public void setEValue(double eValue) {
		this.eValue.setDoubleValue(eValue);
	}

	/**
	 * @return the fractional identity
	 */
	public double getFractionalIdentity() {
		return getIdentity() / 100.0;
	}

	/**
	 * @return The identity (as a % ranging from 0 to 100)
	 */
	public int getIdentity() {
		return identity.getIntValue();
	}

	/**
	 * Set the fractional identity
	 * 
	 * @param identity
	 *            The new fractional identity
	 */
	public void setIdentity(double identity) {
		setIdentity((int) (identity * 100));
	}

	/**
	 * Set the identity
	 * 
	 * @param identity
	 *            The new identity
	 */
	public void setIdentity(int identity) {
		this.identity.setIntValue(identity);
	}

	@Override
	protected void saveSubSettings(final NodeSettingsWO subSettings) {
		eValue.saveSettingsTo(subSettings);
		identity.saveSettingsTo(subSettings);
	}

	@Override
	protected final void validateSubSettings(final NodeSettingsRO subSettings)
			throws InvalidSettingsException {
		InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();
		try {
			subSettings.getDouble(CFGKEY_EVALUE);
		} catch (InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			subSettings.getInt(CFGKEY_IDENTITY);
		} catch (InvalidSettingsException e) {
			isec.add(e);
		}
		isec.throwAll();
	}

	@Override
	protected final void loadSubSettings(final NodeSettingsRO subSettings)
			throws InvalidSettingsException {
		eValue.loadSettingsFrom(subSettings);
		identity.loadSettingsFrom(subSettings);
	}

	@Override
	public void clearQuery() {
		setEValue(DEFAULT_EVALUE);
		setIdentity(DEFAULT_IDENTITY);
		super.clearQuery();
	}

	/**
	 * @return The model for the e-Value
	 */
	SettingsModelNumber getEValueModel() {
		return eValue;
	}

	/**
	 * @return the model for the identity
	 */
	SettingsModelNumber getIdentityModel() {
		return identity;
	}

	@Override
	protected void addParams(ObjectNode params) {
		params.put(IDENTITY_CUTOFF, getFractionalIdentity());
		params.put(EVALUE_CUTOFF, getEValue());

	}

	@Override
	protected String getService() {
		return SERVICE_SEQUENCE;
	}

	@Override
	public boolean isScoringTypeValid(ScoringType scoringType) {
		return scoringType == ScoringType.Sequence
				|| scoringType == ScoringType.Combined;
	}
}
