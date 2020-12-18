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
package com.vernalis.pdbconnector2.nodes.execute;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.pdbconnector2.query.QueryResultType;
import com.vernalis.pdbconnector2.query.ScoringType;

/**
 * {@link DefaultNodeSettingsPane} implementation for the PDB Connector Query
 * Executer node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbConnector2QueryExecutionNodeDialog
		extends DefaultNodeSettingsPane {

	private static final String INCLUDE_JSON_IN_OUTPUT =
			"Include JSON in output";
	private static final String PAGE_SIZE = "Page Size";
	private static final String SCORING_STRATEGY = "Scoring Strategy";
	private static final String RETURN_TYPE = "Return Type";

	/**
	 * Constructor
	 */
	public PdbConnector2QueryExecutionNodeDialog() {
		addDialogComponent(new DialogComponentStringSelection(
				createReturnTypeModel(), RETURN_TYPE,
				Arrays.stream(QueryResultType.values()).map(x -> x.getText())
						.collect(Collectors.toList())));

		// TODO: Only show scoring types allowed by incoming query
		addDialogComponent(new DialogComponentStringSelection(
				createScoringTypeModel(), SCORING_STRATEGY,
				Arrays.stream(ScoringType.values()).map(x -> x.getText())
						.collect(Collectors.toList())));

		addDialogComponent(new DialogComponentNumber(createPageSizeModel(),
				PAGE_SIZE, 10, 5));

		addDialogComponent(new DialogComponentBoolean(createIncludeJsonModel(),
				INCLUDE_JSON_IN_OUTPUT));

	}

	/**
	 * @return The model for the 'Include JSON in output' setting
	 */
	static SettingsModelBoolean createIncludeJsonModel() {
		return new SettingsModelBoolean(INCLUDE_JSON_IN_OUTPUT, false);
	}

	/**
	 * @return The model for the 'Page Size' setting
	 */
	static SettingsModelIntegerBounded createPageSizeModel() {
		return new SettingsModelIntegerBounded(PAGE_SIZE, 1000, 10, 10000);
	}

	/**
	 * @return The model for the 'Scoring Strategy' setting
	 */
	static SettingsModelString createScoringTypeModel() {
		return new SettingsModelString(SCORING_STRATEGY,
				ScoringType.getDefault().getText());
	}

	/**
	 * @return The model for the 'Return Type' setting
	 */
	static SettingsModelString createReturnTypeModel() {
		return new SettingsModelString(RETURN_TYPE,
				QueryResultType.getDefault().getText());
	}

}
