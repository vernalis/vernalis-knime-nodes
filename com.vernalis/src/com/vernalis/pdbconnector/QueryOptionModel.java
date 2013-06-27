/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2012, Vernalis (R&D) Ltd and Enspiral Discovery Limited
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.pdbconnector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.QueryParam;
import com.vernalis.pdbconnector.config.Values;

/**
 * QueryOptionModel class.
 * 
 * A QueryOptionModel defines the settings models for a single query option.
 * It contains:
 * <UL>
 * <LI>The query option (and the list of child query parameters) that are represented by the model</LI>
 * <LI>A Boolean settings model to control selection of the query option</LI>
 * <LI>A collection of settings models to represent the query parameters</LI>
 * </UL>
 */
public class QueryOptionModel {
	private final QueryOption m_queryOption;
	private final SettingsModelBoolean m_selected;
	private final List<QueryParam> m_params;
	/** The settings models.
	 * 
	 * Inner list contains the settings models for a single query param.
	 * Outer list is for each query param in m_params.
	 */
	private final List<List<SettingsModel>> m_models = new ArrayList<List<SettingsModel>>();

	/**
	 * Instantiates a new query option model for a given query option.
	 *
	 * @param queryOption the query option
	 */
	public QueryOptionModel(QueryOption queryOption) {
		m_queryOption = queryOption;
		m_params = m_queryOption.getParams();
		m_selected = ComponentFactory.createSelectionSettingsModel(m_queryOption);
		boolean isSelected = m_selected.getBooleanValue();
		for (final QueryParam param : m_params) {
			m_models.add(ComponentFactory.createSettingsModels(param, isSelected));
		}
	}

	/**
	 * Checks if query option is selected.
	 *
	 * @return true, if is selected
	 */
	public boolean isSelected() {
		return m_selected.getBooleanValue();
	}

	/**
	 * Gets the xml query string for this query option.
	 *
	 * The placeholders (%ARG%) in the query string are replaced with the current values of the
	 * query parameters, as defined by the relevant settings models.
	 * 
	 * @return the xml query string
	 */
	public String getXmlQuery() {
		String retVal = "";
		if (isSelected()) {
			retVal = m_queryOption.getQueryString();//master query string
			int argNum = 1;
			Iterator<QueryParam> paramIter = m_params.iterator();
			Iterator<List<SettingsModel>> modelsIter = m_models.iterator();
			while (paramIter.hasNext() && modelsIter.hasNext()) {
				final QueryParam param = paramIter.next();
				final List<SettingsModel> models = modelsIter.next();
				final List<String> subQueries = param.getQueryStrings();
				String arg;
				switch (param.getType()) {
				case STRING:
				case BIG_STRING:
					assert models.size() == 1;
					arg = "%ARG" + (argNum++) + "%";
					retVal = retVal.replaceAll(arg,((SettingsModelString)models.get(0)).getStringValue());
					break;
				case STRING_LIST:
					assert models.size() == 1;
					arg = "%ARG" + (argNum++) + "%";
					//For String_Lists we need to retrieve the underlying value
					//for this label from the QueryParam
					String label = ((SettingsModelString)models.get(0)).getStringValue();
					Values values = param.getValues();
					if (values.isExists(label)) {
						retVal = retVal.replaceAll(arg,values.getValue(label));
					}
					else {
						PdbConnectorNodeModel.logger.warn("Value not found for label " + label
								+ " for " + param.getLabel());
					}
					break;
				case INTEGER:
					assert models.size() == 1;
					arg = "%ARG" + (argNum++) + "%";
					retVal = retVal.replaceAll(arg,Integer.toString(((SettingsModelInteger)models.get(0)).getIntValue()));
					break;
				case DOUBLE:
					assert models.size() == 1;
					arg = "%ARG" + (argNum++) + "%";
					retVal = retVal.replaceAll(arg,Double.toString(((SettingsModelDouble)models.get(0)).getDoubleValue()));
					break;
				case INTEGER_RANGE:
					assert models.size() == 2;
					for (int i = 0, numModels = models.size(); i < numModels; ++i) {
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,Integer.toString(((SettingsModelInteger)models.get(i)).getIntValue()));
					}
					break;
				case DOUBLE_RANGE:
					assert models.size() == 2;
					for (int i = 0, numModels = models.size(); i < numModels; ++i) {
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,Double.toString(((SettingsModelDouble)models.get(i)).getDoubleValue()));
					}
					break;
				case DATE:
					assert models.size() == 3;
					for (int i = 0, numModels = models.size(); i < numModels; ++i) {
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,Integer.toString(((SettingsModelInteger)models.get(i)).getIntValue()));
					}
					break;
				case STRING_COND:
					assert (models.size() == 1) && (subQueries.size() == 1);
					{
						//Perform subQuery replacement on the string value entered
						//Subquery is only active if a non-empty string is entered
						String val = ((SettingsModelString)models.get(0)).getStringValue();
						String query = !val.isEmpty() ? subQueries.get(0).replaceAll("%ARG%", val) : "";
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,query);
					}
					break;
				case INTEGER_RANGE_COND:
					assert (models.size() == 2) && (subQueries.size() == 2);
					{
						//Perform subQuery replacements on the min and max values entered.
						//Subqueries are only active if the value entered is gt min or lt max respectively.
						int valMin = ((SettingsModelInteger)models.get(0)).getIntValue();
						String queryMin = (valMin > param.getMin()) ? subQueries.get(0).replaceAll("%ARG%", Integer.toString(valMin)) : "";
						int valMax = ((SettingsModelInteger)models.get(1)).getIntValue();
						String queryMax = (valMax < param.getMax()) ? subQueries.get(1).replaceAll("%ARG%", Integer.toString(valMax)) : "";
						//Now substitute the subqueries into the master query string
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,queryMin);
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,queryMax);
					}
					break;
				case DOUBLE_RANGE_COND:
					assert (models.size() == 2) && (subQueries.size() == 2);
					{
						//Perform subQuery replacements on the min and max values entered.
						//Subqueries are only active if the value entered is gt min or lt max respectively.
						double valMin = ((SettingsModelDouble)models.get(0)).getDoubleValue();
						String queryMin = (valMin > param.getMin()) ? subQueries.get(0).replaceAll("%ARG%", Double.toString(valMin)) : "";
						double valMax = ((SettingsModelDouble)models.get(1)).getDoubleValue();
						String queryMax = (valMax < param.getMax()) ? subQueries.get(1).replaceAll("%ARG%", Double.toString(valMax)) : "";
						//Now substitute the subqueries into the master query string
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,queryMin);
						arg = "%ARG" + (argNum++) + "%";
						retVal = retVal.replaceAll(arg,queryMax);
						break;
					}
				default:
					break;
				}
			}
		}
		return retVal;
	}

	/**
	 * Saves model settings.
	 *
	 * Calls {@link SettingsModel#saveSettingsTo(NodeSettingsWO)} on each underlying settings model.
	 * 
	 * @param settings the settings
	 */
	public void saveSettingsTo(final NodeSettingsWO settings) {
		m_selected.saveSettingsTo(settings);
		for (List<SettingsModel> models : m_models) {
			for (SettingsModel model : models) {
				model.saveSettingsTo(settings);
			}
		}
	}

	/**
	 * Loads validated model settings.
	 * 
	 * Calls {@link SettingsModel#loadSettingsFrom(NodeSettingsRO)} on each underlying settings model.
	 *
	 * @param settings the settings
	 * @throws InvalidSettingsException
	 */
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_selected.loadSettingsFrom(settings);
		for (List<SettingsModel> models : m_models) {
			for (SettingsModel model : models) {
				model.loadSettingsFrom(settings);
			}
		}
	}

	/**
	 * Validates model settings.
	 * 
	 * Calls {@link SettingsModel#validateSettings(NodeSettingsRO)} on each underlying settings model.
	 * 
	 * @param settings the settings
	 * @throws InvalidSettingsException
	 */
	public void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_selected.validateSettings(settings);
		for (List<SettingsModel> models : m_models) {
			for (SettingsModel model : models) {
				model.validateSettings(settings);
			}
		}
	}

}
