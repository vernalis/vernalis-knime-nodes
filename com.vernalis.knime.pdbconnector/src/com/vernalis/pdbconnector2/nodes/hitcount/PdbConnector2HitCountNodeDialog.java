/*******************************************************************************
 * Copyright (c) 2024 Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector2.nodes.hitcount;

import java.util.Arrays;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.pdbconnector2.ports.MultiRCSBQueryModel;
import com.vernalis.pdbconnector2.query.QueryResultType;
import com.vernalis.pdbconnector2.query.ResultContentType;
import com.vernalis.pdbconnector2.query.ScoringType;

/**
 * {@link DefaultNodeSettingsPane} implementation for the PDB Connector Query
 * Hit Count node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 * @since 24-Jan-2023
 */
public class PdbConnector2HitCountNodeDialog extends DefaultNodeSettingsPane {
	/**
	 * Key for the results content type model
	 * 
	 * @since 23-Jan-2024
	 */
	private static final String RESULTS_CONTENT_TYPE = "Results content type";
	private static final String ADD_QUERY_TO_OUTPUT_TABLE =
			"Add query to output table";
	private static final String SCORING_STRATEGY = "Scoring Strategy";
	private static final String RETURN_TYPE = "Return Type";
	private static final String RCSB_PDB_ADVANCED_QUERY_DEFINITION =
			"RCSB PDB Advanced Query Definition";
	private static final String COLUMN_NAME = "Column Name";

	private MultiRCSBQueryModel query;
	private DialogComponentStringSelection scoringChooser =
			new DialogComponentStringSelection(createScoringTypeModel(),
					SCORING_STRATEGY, Arrays
							.stream(query == null ? ScoringType.values()
									: ScoringType
											.getAvailableScoringTypes(query))
							.map(x -> x.getText()).toList());
	private final SettingsModelBoolean addQueryToOutputTableMdl;
	private SettingsModelString colNameMdl;

	/**
	 * Constructor
	 */
	public PdbConnector2HitCountNodeDialog() {
		addDialogComponent(
				new DialogComponentStringSelection(createReturnTypeModel(),
						RETURN_TYPE, Arrays.stream(QueryResultType.values())
								.map(x -> x.getText()).toList()));

		addDialogComponent(new DialogComponentStringListSelection(
				createResultContentTypeModel(), RESULTS_CONTENT_TYPE,
				Arrays.stream(ResultContentType.values())
						.map(rct -> rct.getText()).toList(),
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true,
				ResultContentType.values().length));

		addDialogComponent(scoringChooser);

		addQueryToOutputTableMdl = createAddQueryToOutputModel();
		colNameMdl = createColumnNameModel();
		addQueryToOutputTableMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				colNameMdl
						.setEnabled(addQueryToOutputTableMdl.getBooleanValue());

			}
		});
		colNameMdl.setEnabled(addQueryToOutputTableMdl.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(addQueryToOutputTableMdl,
				ADD_QUERY_TO_OUTPUT_TABLE));
		addDialogComponent(
				new DialogComponentString(colNameMdl, COLUMN_NAME, true, 30));

	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings,
			PortObjectSpec[] specs) throws NotConfigurableException {
		List<String> scoringOptions;
		if (specs[0] instanceof MultiRCSBQueryModel rcsbModel) {
			query = rcsbModel;
			scoringOptions =
					Arrays.stream(ScoringType.getAvailableScoringTypes(query))
							.map(ScoringType::getText).toList();
		} else {
			query = null;
			scoringOptions = Arrays.stream(ScoringType.values())
					.map(ScoringType::getText).toList();
		}
		scoringChooser.replaceListItems(scoringOptions, null);
		colNameMdl.setEnabled(addQueryToOutputTableMdl.getBooleanValue());
		super.loadAdditionalSettingsFrom(settings, specs);
	}

	/**
	 * @return the model for the results content type setting
	 * @since 23-Jan-2024
	 */
	static final SettingsModelStringArray createResultContentTypeModel() {
		return new SettingsModelStringArray(RESULTS_CONTENT_TYPE,
				Arrays.stream(ResultContentType.getDefaults())
						.map(rct -> rct.getText()).toArray(String[]::new));
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

	/**
	 * @return model for the Add Query to output table setting
	 *
	 * @since 24-Jan-2023
	 */
	static SettingsModelBoolean createAddQueryToOutputModel() {
		return new SettingsModelBoolean(ADD_QUERY_TO_OUTPUT_TABLE, true);
	}

	/**
	 * @return The model for the 'Column Name' setting
	 * 
	 * @since 24-Jan-2023
	 */
	static SettingsModelString createColumnNameModel() {
		return new SettingsModelString(COLUMN_NAME,
				RCSB_PDB_ADVANCED_QUERY_DEFINITION);
	}

}
