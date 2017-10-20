/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.pairgen.abstrct;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.chem.types.SmilesCellFactory;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.ComplexNumberCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

import com.vernalis.knime.dialog.components.DialogComponentIntRange;
import com.vernalis.knime.dialog.components.SettingsModelDoubleBoundedRerangable;
import com.vernalis.knime.dialog.components.SettingsModelIntegerBoundedRerangable;
import com.vernalis.knime.dialog.components.SettingsModelIntegerRange;
import com.vernalis.knime.mmp.MMPConstants;

/**
 * Abstract node dialog for the matched pairs from fragments nodes
 * 
 * @author s.roughley
 * 
 */
public class AbstractMatchedPairsFromFragmentsNodeDialog extends DefaultNodeSettingsPane {

	protected final SettingsModelBoolean m_showTransformsMdl;
	protected final SettingsModelBoolean m_requireAcyclicMdl;

	/**
	 * Filter for numeric columns
	 */
	private static final InputFilter<DataColumnSpec> NUMBER_FILTER =
			new InputFilter<DataColumnSpec>() {

				@Override
				public boolean include(DataColumnSpec name) {
					DataType type = name.getType();
					return type == IntCell.TYPE || type == LongCell.TYPE || type == DoubleCell.TYPE
							|| type == ComplexNumberCell.TYPE;
				}
			};
	private final SettingsModelString valueGraphDistFilterMdl;
	private final SettingsModelDoubleBoundedRerangable graphDistDblFilterCutoffMdl;
	private final SettingsModelIntegerBoundedRerangable graphDistIntFilterCutoffMdl;
	private final SettingsModelString graphDistFpColnameMdl;
	private final SettingsModelBoolean includeGraphSimilarityMdl;
	protected static final ColumnFilter GRAPH_DISTANCE_COLUMN_FILTER = new ColumnFilter() {

		@Override
		public boolean includeColumn(DataColumnSpec colSpec) {
			return colSpec.getType() == DenseByteVectorCell.TYPE;
		}

		@Override
		public String allFilteredMsg() {
			return "No Dense Byte Vector columns available";
		}
	};

	/**
	 * Constructor
	 */
	@SuppressWarnings("unchecked")
	public AbstractMatchedPairsFromFragmentsNodeDialog(boolean presortTableByKey) {
		// NB Although we might have 2 tables we ignore this as the
		// nodemodel#configure enforces both tables to have the same structure
		super();

		createNewGroup("'Key' Options");
		addDialogComponent(new DialogComponentColumnNameSelection(createFragKeyModel(),
				"Select the Fragment Key column", 0, new ColumnFilter() {

					@Override
					public boolean includeColumn(DataColumnSpec colSpec) {
						return (colSpec.getType() == SmilesCellFactory.TYPE
								|| colSpec.getType().isAdaptable(SmilesValue.class))
								&& colSpec.getName().startsWith("Fragmentation 'Key'");
					}

					@Override
					public String allFilteredMsg() {
						return "No SMILES columns with the name starting\"Fragmentation 'Key'\" are present";
					}
				}));
		if (presortTableByKey) {
			addDialogComponent(
					new DialogComponentBoolean(createSortedKeysModel(), "Keys are sorted"));
		}
		addKeyColOptions();
		closeCurrentGroup();

		createNewGroup("'Value' Options");
		addDialogComponent(new DialogComponentColumnNameSelection(createFragValueModel(),
				"Select the Fragment Value column", 0, new ColumnFilter() {

					@Override
					public boolean includeColumn(DataColumnSpec colSpec) {
						return (colSpec.getType() == SmilesCellFactory.TYPE
								|| colSpec.getType().isAdaptable(SmilesValue.class))
								&& colSpec.getName().startsWith("Fragmentation 'Value'");
					}

					@Override
					public String allFilteredMsg() {
						return "No SMILES columns with the name starting\"Fragmentation 'Value'\" are present";
					}
				}));
		addValueColOptions();
		closeCurrentGroup();

		createNewGroup("'ID' and uniqueness Options");
		addDialogComponent(new DialogComponentColumnNameSelection(createIDModel(),
				"Select the ID column", 0, StringValue.class));

		addDialogComponent(new DialogComponentBoolean(createAllowSelfTransformsModel(),
				"Allow self-transforms"));
		addIDColOptions();
		closeCurrentGroup();
		addDialogComponent(new DialogComponentBoolean(createAllowHiliteModel(), "Allow HiLiting"));

		createNewGroup("Transform HAC filters");
		SettingsModelBoolean filterByDeltaHACMdl = createFilterByDeltaHACModel();
		SettingsModelIntegerRange hacDeltaRangeMdl = createHACDeltaRangeModel();
		SettingsModelBoolean showDeltaHACMdl = createShowDeltaHACModel();
		filterByDeltaHACMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				hacDeltaRangeMdl.setEnabled(filterByDeltaHACMdl.getBooleanValue());
				showDeltaHACMdl.setEnabled(filterByDeltaHACMdl.getBooleanValue());
			}
		});
		hacDeltaRangeMdl.setEnabled(filterByDeltaHACMdl.getBooleanValue());
		showDeltaHACMdl.setEnabled(filterByDeltaHACMdl.getBooleanValue());

		addDialogComponent(new DialogComponentBoolean(filterByDeltaHACMdl, "Filter by HAC change"));
		addDialogComponent(
				new DialogComponentIntRange(hacDeltaRangeMdl, -1000, 1000, 1, "HAC Change Range"));
		addDialogComponent(
				new DialogComponentBoolean(showDeltaHACMdl, "Show HAC change in output table"));

		createNewGroup("Transform Similarity Filters");
		valueGraphDistFilterMdl = createValueGraphDistFilterModel();
		graphDistDblFilterCutoffMdl = createGraphDistDblFilterCutoffModel();
		graphDistIntFilterCutoffMdl = createGraphDistIntFilterCutoffModel();
		graphDistFpColnameMdl = createGraphDistFpColnameModel();
		includeGraphSimilarityMdl = createIncludeGraphSimilarityModel();

		valueGraphDistFilterMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateGraphDistSettings();

			}
		});
		updateGraphDistSettings();

		addDialogComponent(
				new DialogComponentButtonGroup(valueGraphDistFilterMdl, "Graph Distance Similarity",
						true, ValueGraphDistanceFingerprintComparisonType.values()));
		addDialogComponent(
				new DialogComponentNumber(graphDistDblFilterCutoffMdl, "Cutoff (Double)", 0.1));
		addDialogComponent(
				new DialogComponentNumber(graphDistIntFilterCutoffMdl, "Cutoff (Integer)", 1));

		addDialogComponent(new DialogComponentColumnNameSelection(graphDistFpColnameMdl,
				"Graph Distance fingerprint Column", 0, false, GRAPH_DISTANCE_COLUMN_FILTER));

		addDialogComponent(new DialogComponentBoolean(includeGraphSimilarityMdl,
				"Include distance/similarity in output"));
		closeCurrentGroup();

		createNewTab("Pass-through columns");
		createNewGroup("Left Columns to pass through unchanged");
		addDialogComponent(new DialogComponentColumnFilter2(createLeftPassThroughColsModel(), 0));
		createNewGroup("Right Columns to pass through unchanged");
		addDialogComponent(new DialogComponentColumnFilter2(createRightPassThroughColsModel(), 0));

		createNewTab("Difference columns");
		createNewGroup("Left - Right");
		addDialogComponent(new DialogComponentColumnFilter2(createLMinusRModel(), 0));
		createNewGroup("Right - Left");
		addDialogComponent(new DialogComponentColumnFilter2(createRMinusLModel(), 0));

		createNewTab("Ratio columns");
		createNewGroup("Left / Right");
		addDialogComponent(new DialogComponentColumnFilter2(createLdbRModel(), 0));
		createNewGroup("Right / Left");
		addDialogComponent(new DialogComponentColumnFilter2(createRdbLModel(), 0));

		createNewTab("Output Settings");

		addDialogComponent(
				new DialogComponentBoolean(createOutputKeyModel(), "Show unchanging portion"));

		addDialogComponent(new DialogComponentBoolean(createOutputChangingHACountsModel(),
				"Show number of changing atoms"));
		addDialogComponent(new DialogComponentBoolean(createOutputHARatiosModel(),
				"Show ratio of constant / changing heavy atoms"));

		addDialogComponent(new DialogComponentBoolean(createShowReverseTransformsModel(),
				"Show reverse-direction transforms"));

		m_showTransformsMdl = createShowSmartsTransformsModel();
		m_requireAcyclicMdl = createForceSingleAcyclicsModel();
		m_showTransformsMdl.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_requireAcyclicMdl.setEnabled(m_showTransformsMdl.getBooleanValue());

			}
		});
		m_requireAcyclicMdl.setEnabled(m_showTransformsMdl.getBooleanValue());

		addDialogComponent(
				new DialogComponentBoolean(m_showTransformsMdl, "Include Reaction SMARTS"));

		addDialogComponent(new DialogComponentBoolean(m_requireAcyclicMdl,
				"Require single acyclicly bonded attachment points"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.data.DataTableSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		super.loadAdditionalSettingsFrom(settings, specs);
		// Check if there is an GraphDistance FP column...
		if (specs[0] != null) {

			valueGraphDistFilterMdl.setEnabled(specs[0].stream()
					.anyMatch(col -> GRAPH_DISTANCE_COLUMN_FILTER.includeColumn(col)));
			if (valueGraphDistFilterMdl.isEnabled()) {
				updateGraphDistSettings();
			} else {
				graphDistDblFilterCutoffMdl.setEnabled(false);
				graphDistIntFilterCutoffMdl.setEnabled(false);
				graphDistFpColnameMdl.setEnabled(false);
				includeGraphSimilarityMdl.setEnabled(false);
			}
		}
	}

	/**
	 * @return The settings model for the allow hiliting setting
	 */
	static SettingsModelBoolean createAllowHiliteModel() {
		return new SettingsModelBoolean("Allow HiLiting", false);
	}

	/**
	 * @return The settings model for the show HAC change setting
	 */
	static SettingsModelBoolean createShowDeltaHACModel() {
		return new SettingsModelBoolean("Show delta HAC", true);
	}

	/**
	 * @return The settings model for the HAC change range
	 */
	static SettingsModelIntegerRange createHACDeltaRangeModel() {
		return new SettingsModelIntegerRange("HAC Delta Range", -5, +5);
	}

	/**
	 * @return The settings model for the filter by HAC change setting
	 */
	static SettingsModelBoolean createFilterByDeltaHACModel() {
		return new SettingsModelBoolean("Filter by delta HAC", false);
	}

	/**
	 * Handle to allow adding additional option settings to the ID column group
	 */
	protected void addIDColOptions() {

	}

	/**
	 * Handle to allow adding additional option settings to the Value column
	 * group
	 */
	protected void addValueColOptions() {

	}

	/**
	 * Handle to allow adding additional option settings to the Key column group
	 */
	protected void addKeyColOptions() {

	}

	/**
	 * 
	 */
	private void updateGraphDistSettings() {
		ValueGraphDistanceFingerprintComparisonType filtType =
				ValueGraphDistanceFingerprintComparisonType
						.valueOf(valueGraphDistFilterMdl.getStringValue());
		if (filtType == ValueGraphDistanceFingerprintComparisonType.NONE) {
			graphDistDblFilterCutoffMdl.setEnabled(false);
			graphDistIntFilterCutoffMdl.setEnabled(false);
			graphDistFpColnameMdl.setEnabled(false);
			includeGraphSimilarityMdl.setEnabled(false);
		} else {

			graphDistFpColnameMdl.setEnabled(true);
			includeGraphSimilarityMdl.setEnabled(true);
			if (filtType.getDataType() == IntCell.TYPE) {
				graphDistDblFilterCutoffMdl.setEnabled(false);
				graphDistIntFilterCutoffMdl.setEnabled(true);
				graphDistIntFilterCutoffMdl.setBounds(filtType.getMinimum().intValue(),
						filtType.getMaximum().intValue());
			} else {
				graphDistIntFilterCutoffMdl.setEnabled(false);
				graphDistDblFilterCutoffMdl.setEnabled(true);
				graphDistDblFilterCutoffMdl.setBounds(filtType.getMinimum().doubleValue(),
						filtType.getMaximum().doubleValue());
			}
		}
	}

	/** Create model for sorted keys */
	static SettingsModelBoolean createSortedKeysModel() {
		return new SettingsModelBoolean("Sorted keys", MMPConstants.DEFAULT_HAS_SORTED_KEYS);
	}

	/**
	 * @return The settings model for the graph distance integer filter cutoff
	 */
	static SettingsModelIntegerBoundedRerangable createGraphDistIntFilterCutoffModel() {
		return new SettingsModelIntegerBoundedRerangable("Graph Distance Integer filter cutoff", 0,
				0, Integer.MAX_VALUE);
	}

	/**
	 * @return the settings model for the include graph fp similarity or
	 *         distance
	 */
	static SettingsModelBoolean createIncludeGraphSimilarityModel() {
		return new SettingsModelBoolean("Include graph fp sim or dist", true);
	}

	/**
	 * @return The settings model for the graph distance fp model
	 */
	static SettingsModelString createGraphDistFpColnameModel() {
		return new SettingsModelString("Graph Dist fp column", null);
	}

	/**
	 * @return The settings model for the graph distance double filter cutoff
	 */
	static SettingsModelDoubleBoundedRerangable createGraphDistDblFilterCutoffModel() {
		return new SettingsModelDoubleBoundedRerangable("Graph Distance Double Filter Cutoff", 0.0,
				0.0, Double.MAX_VALUE);
	}

	/**
	 * @return The settings model for the graph distance filter type
	 */
	static SettingsModelString createValueGraphDistFilterModel() {
		return new SettingsModelString("Graph Distance Filter Type",
				ValueGraphDistanceFingerprintComparisonType.getDefault().getActionCommand());
	}

	/**
	 * @return The settings model for the 'require acyclic' setting
	 */
	protected static SettingsModelBoolean createForceSingleAcyclicsModel() {
		return new SettingsModelBoolean("Required acyclic", true);
	}

	/** Create model to allow self-transforms */
	protected static SettingsModelBoolean createAllowSelfTransformsModel() {
		return new SettingsModelBoolean("Allow self-transforms",
				MMPConstants.DEFAULT_ALLOW_SELF_TRANSFORMS);
	}

	/** Create model to include Reaction SMARTS */
	protected static SettingsModelBoolean createShowSmartsTransformsModel() {
		return new SettingsModelBoolean("Include Reaction SMARTS",
				MMPConstants.DEFAULT_OUTPUT_REACTION_SMARTS);
	}

	/** Create model to include reverse transforms in outpu */
	protected static SettingsModelBoolean createShowReverseTransformsModel() {
		return new SettingsModelBoolean("Show Reverse Transforms",
				MMPConstants.DEFAULT_OUTPUT_REVERSE_TRANSFORMS);
	}

	/** Create model for fragment value column */
	protected static SettingsModelString createFragValueModel() {
		return new SettingsModelString("Fragment Value Column", null);
	}

	/** Create model for ID column */
	protected static SettingsModelString createIDModel() {
		return new SettingsModelString("ID Column", null);
	}

	/** Create model for Fragment Key */
	protected static SettingsModelString createFragKeyModel() {
		return new SettingsModelString("Fragment Key Column", null);
	}

	/** Create the settings model for the output fragment key model */
	protected static SettingsModelBoolean createOutputKeyModel() {
		return new SettingsModelBoolean("Output fragment key",
				MMPConstants.DEFAULT_OUTPUT_FRAGMENT_KEY);
	}

	/** Create the settings model for the output changing HAC model */
	protected static SettingsModelBoolean createOutputChangingHACountsModel() {
		return new SettingsModelBoolean("Output changing HA counts",
				MMPConstants.DEFAULT_OUTPUT_DELTA_HAC);
	}

	/** Create the settings model for the output HAC ratio key model */
	protected static SettingsModelBoolean createOutputHARatiosModel() {
		return new SettingsModelBoolean("Output changing / unchanging HA ratios",
				MMPConstants.DEFAULT_OUTPUT_CHANGING_UNCHANGING_HAC_RATIOS);
	}

	/**
	 * @return The settings model for the 'right' pass through columns
	 */
	static SettingsModelColumnFilter2 createRightPassThroughColsModel() {
		return new SettingsModelColumnFilter2("Right pass through");
	}

	/**
	 * @return The settings model for the 'left' pass through columns
	 */
	static SettingsModelColumnFilter2 createLeftPassThroughColsModel() {
		return new SettingsModelColumnFilter2("Left pass through");
	}

	/**
	 * Double column filter
	 */
	private static final InputFilter<DataColumnSpec> DOUBLE_FILTER =
			new InputFilter<DataColumnSpec>() {

				@Override
				public boolean include(DataColumnSpec name) {
					return name.getType() == DoubleCell.TYPE;
				}
			};

	/**
	 * @return The settings model for the Right / Left columns
	 */
	static SettingsModelColumnFilter2 createRdbLModel() {
		return new SettingsModelColumnFilter2("R / L", DOUBLE_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

	/**
	 * @return The settings model for the Left / Right columns
	 */
	static SettingsModelColumnFilter2 createLdbRModel() {
		return new SettingsModelColumnFilter2("L / R", DOUBLE_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

	/**
	 * @return The settings model for the Right - Left columns
	 */
	static SettingsModelColumnFilter2 createRMinusLModel() {
		return new SettingsModelColumnFilter2("R - L", NUMBER_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_DATATYPE
						| DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}

	/**
	 * @return The settings model for the Left - Right columns
	 */
	static SettingsModelColumnFilter2 createLMinusRModel() {
		return new SettingsModelColumnFilter2("L - R", NUMBER_FILTER,
				DataColumnSpecFilterConfiguration.FILTER_BY_DATATYPE
						| DataColumnSpecFilterConfiguration.FILTER_BY_NAMEPATTERN);
	}
}
