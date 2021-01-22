/*******************************************************************************
 * Copyright (c) 2017, 2021 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.container.DataContainerException;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.property.hilite.DefaultHiLiteMapper;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteTranslator;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.util.MultiThreadWorker;

import com.vernalis.knime.mmp.IncomingMoleculeException;
import com.vernalis.knime.mmp.MMPConstants;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.AbstractFragmentationFactory;
import com.vernalis.knime.mmp.fragmentors.ClosedFactoryException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;
import com.vernalis.knime.mmp.frags.abstrct.AbstractFragmentKey;
import com.vernalis.knime.mmp.frags.abstrct.AbstractLeaf;
import com.vernalis.knime.mmp.frags.abstrct.AbstractMulticomponentFragmentationParser;
import com.vernalis.knime.mmp.frags.abstrct.BondIdentifier;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeModel;
import com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage;

import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createAddFailReasonModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createAddValueGraphDistanceFPModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createAllowHiliteModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createApFingerprintsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createBreakingBondColourModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createFpLengthModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createFpUseBondTypesModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createFpUseChiralityModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createHARatioModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createHasFixedAtomsFilterModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createHasHARatioFilterModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createHasMaxChangingAtomsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createIDColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createKeptColumnsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createKeyColourModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createLimitByComplexityModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createMaxChangingAtomsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createMaxFragmentationsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createMinFixedAtomsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createMorganRadiusModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createOutputChangingHACountsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createOutputHARatiosModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createProchiralModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createRenderBreakingBondModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createRenderFragmentationModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createRenderKeyBondModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createRenderValueBondModel;
import static com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct.AbstractMMPFragmentNodeDialog.createValueColourModel;

/**
 * The abstract {@link NodeModel} implementation for the MMP Molecule Fragment
 * nodes
 * <p>
 * Added version argument handling to allow version 3 nodes to redirect here and
 * correctly convert settings to maintain behaviour (SDR, 11-May-2018)
 * </p>
 * 
 * @author s.roughley
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the substructure matching object
 */
public class AbstractMMPFragmentNodeModel<T, U>
		extends AbstractMMPFragmentationFactoryNodeModel<T, U> {

	protected final SettingsModelBoolean m_prochiralAsChiral =
			createProchiralModel();
	protected final SettingsModelColumnName m_idColName =
			createIDColumnSettingsModel();
	protected final SettingsModelBoolean m_hiliteMdl = createAllowHiliteModel();
	protected final SettingsModelBoolean m_hasChangingAtoms =
			createHasMaxChangingAtomsModel();
	protected final SettingsModelBoolean m_hasHARatioFilter =
			createHasHARatioFilterModel();
	protected final SettingsModelIntegerBounded m_maxChangingAtoms =
			createMaxChangingAtomsModel();
	protected final SettingsModelBoolean hasMinFixedHAFilter =
			createHasFixedAtomsFilterModel();
	protected final SettingsModelIntegerBounded minFixedHAMdl =
			createMinFixedAtomsModel();
	protected final SettingsModelDoubleBounded m_minHARatioFilter =
			createHARatioModel();
	protected final SettingsModelBoolean renderFragmentationMdl;
	protected final SettingsModelBoolean renderBreakingBondMdl;
	protected final SettingsModelColor breakingBondColourMdl;
	protected final SettingsModelBoolean renderKeyBondMdl;
	protected final SettingsModelColor keyColourMdl;
	protected final SettingsModelBoolean renderValueBondMdl;
	protected final SettingsModelColor valueColourMdl;
	protected final SettingsModelBoolean m_outputNumChgHAs =
			createOutputChangingHACountsModel();
	protected final SettingsModelBoolean m_outputHARatio =
			createOutputHARatiosModel();
	protected final SettingsModelColumnFilter2 m_keptColumns =
			createKeptColumnsModel();
	protected final SettingsModelBoolean m_addFailReasons =
			createAddFailReasonModel();
	protected final SettingsModelBoolean m_apFingerprints =
			createApFingerprintsModel();
	protected final SettingsModelIntegerBounded m_fpLength =
			createFpLengthModel();
	protected final SettingsModelIntegerBounded m_morganRadius =
			createMorganRadiusModel();
	protected final SettingsModelBoolean m_fpUseChirality, m_fpUseBondTypes;
	protected final SettingsModelBoolean m_limitByComplexity =
			createLimitByComplexityModel();
	protected final SettingsModelIntegerBounded m_maxFragmentations =
			createMaxFragmentationsModel();
	protected SettingsModelBoolean m_addValueGraphDistanceFingerprint =
			createAddValueGraphDistanceFPModel();

	/** Output table specs */
	protected DataTableSpec m_spec_0;
	/** Output table specs */
	protected DataTableSpec m_spec_1;

	// Some fields to be accesses by the progress view
	protected Long numRows = null;
	protected long rowsRun = 0;
	protected int completedQueue = 0;
	protected int activeThreads = 0;
	protected boolean[] keptColMask;
	protected Color bondColour;
	protected Color keyColour;
	protected Color valueColour;

	// Highlighting fields
	/** Mapping from the input row to fragments */
	protected final HiLiteTranslator hiLiteTranslator = new HiLiteTranslator();
	protected static final String HILITE_KEY = "HiLiteMapping";
	protected static final String SETTINGS_FILE_NAME = "internals";

	/**
	 * Constructor
	 * 
	 * @param isMulticut
	 *            Should the node perform 1-n cuts in place of n cuts
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} for the node
	 * @param version
	 *            A version index - to differentiated between redirected v3 node
	 *            and v4 nodes
	 */
	public AbstractMMPFragmentNodeModel(boolean isMulticut,
			FragmentationUtilsFactory<T, U> fragUtilityFactory, int version) {
		super(1, 2, fragUtilityFactory, isMulticut, version);

		// SettingsModels
		m_hasChangingAtoms.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_maxChangingAtoms
						.setEnabled(m_hasChangingAtoms.getBooleanValue());

			}
		});
		m_maxChangingAtoms.setEnabled(m_hasChangingAtoms.getBooleanValue());

		m_hasHARatioFilter.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_minHARatioFilter
						.setEnabled(m_hasHARatioFilter.getBooleanValue());

			}
		});
		m_minHARatioFilter.setEnabled(m_hasHARatioFilter.getBooleanValue());

		m_limitByComplexity.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_maxFragmentations
						.setEnabled(m_limitByComplexity.getBooleanValue());

			}
		});
		m_maxFragmentations.setEnabled(m_limitByComplexity.getBooleanValue());

		hasMinFixedHAFilter.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				minFixedHAMdl.setEnabled(hasMinFixedHAFilter.getBooleanValue());

			}
		});
		minFixedHAMdl.setEnabled(hasMinFixedHAFilter.getBooleanValue());

		m_apFingerprints.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_fpLength.setEnabled(m_apFingerprints.isEnabled()
						&& m_apFingerprints.getBooleanValue());
				m_morganRadius.setEnabled(m_apFingerprints.isEnabled()
						&& m_apFingerprints.getBooleanValue());
			}
		});
		m_fpLength.setEnabled(m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue());
		m_morganRadius.setEnabled(m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue());

		if (this.fragUtilityFactory.hasExtendedFingerprintOptions()) {
			m_fpUseChirality = createFpUseChiralityModel();
			m_fpUseBondTypes = createFpUseBondTypesModel();
			m_apFingerprints.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					m_fpUseChirality.setEnabled(m_apFingerprints.isEnabled()
							&& m_apFingerprints.getBooleanValue());
					m_fpUseBondTypes.setEnabled(m_apFingerprints.isEnabled()
							&& m_apFingerprints.getBooleanValue());
				}
			});
			m_fpUseChirality.setEnabled(m_apFingerprints.isEnabled()
					&& m_apFingerprints.getBooleanValue());
			m_fpUseBondTypes.setEnabled(m_apFingerprints.isEnabled()
					&& m_apFingerprints.getBooleanValue());
		} else {
			m_fpUseChirality = null;
			m_fpUseBondTypes = null;
		}

		if (isMulticut) {
			m_numCuts.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					m_allowTwoCutsToBondValue
							.setEnabled(m_numCuts.getIntValue() >= 2);
				}
			});
			m_allowTwoCutsToBondValue.setEnabled(m_numCuts.getIntValue() >= 2);
		} else {
			m_numCuts.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					m_allowTwoCutsToBondValue
							.setEnabled(m_numCuts.getIntValue() == 2);
				}
			});
			m_allowTwoCutsToBondValue.setEnabled(m_numCuts.getIntValue() == 2);
		}

		if (fragUtilityFactory.getRendererType() != null) {
			renderFragmentationMdl = createRenderFragmentationModel();
			renderBreakingBondMdl = createRenderBreakingBondModel();
			breakingBondColourMdl = createBreakingBondColourModel();
			renderKeyBondMdl = createRenderKeyBondModel();
			keyColourMdl = createKeyColourModel();
			renderValueBondMdl = createRenderValueBondModel();
			valueColourMdl = createValueColourModel();

			renderFragmentationMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (renderFragmentationMdl.getBooleanValue()) {
						renderBreakingBondMdl.setEnabled(true);
						renderKeyBondMdl.setEnabled(true);
						renderValueBondMdl.setEnabled(true);
						breakingBondColourMdl.setEnabled(
								renderBreakingBondMdl.getBooleanValue());
						keyColourMdl
								.setEnabled(renderKeyBondMdl.getBooleanValue());
						valueColourMdl.setEnabled(
								renderValueBondMdl.getBooleanValue());
					} else {
						renderBreakingBondMdl.setEnabled(false);
						renderKeyBondMdl.setEnabled(false);
						renderValueBondMdl.setEnabled(false);
						breakingBondColourMdl.setEnabled(false);
						keyColourMdl.setEnabled(false);
						valueColourMdl.setEnabled(false);
					}

				}
			});

			renderBreakingBondMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					breakingBondColourMdl.setEnabled(
							renderBreakingBondMdl.getBooleanValue());
				}
			});
			renderKeyBondMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					keyColourMdl.setEnabled(renderKeyBondMdl.getBooleanValue());

				}
			});
			renderValueBondMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					valueColourMdl
							.setEnabled(renderValueBondMdl.getBooleanValue());
				}
			});

			if (renderFragmentationMdl.getBooleanValue()) {
				renderBreakingBondMdl.setEnabled(true);
				renderKeyBondMdl.setEnabled(true);
				renderValueBondMdl.setEnabled(true);
				breakingBondColourMdl
						.setEnabled(renderBreakingBondMdl.getBooleanValue());
				keyColourMdl.setEnabled(renderKeyBondMdl.getBooleanValue());
				valueColourMdl.setEnabled(renderValueBondMdl.getBooleanValue());
			} else {
				renderBreakingBondMdl.setEnabled(false);
				renderKeyBondMdl.setEnabled(false);
				renderValueBondMdl.setEnabled(false);
				breakingBondColourMdl.setEnabled(false);
				keyColourMdl.setEnabled(false);
				valueColourMdl.setEnabled(false);
			}
		} else {
			renderFragmentationMdl = null;
			renderBreakingBondMdl = null;
			breakingBondColourMdl = null;
			renderKeyBondMdl = null;
			keyColourMdl = null;
			renderValueBondMdl = null;
			valueColourMdl = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragment.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#doConfigure(org.knime.core.data.
	 * DataTableSpec[])
	 */
	@Override
	protected DataTableSpec[] doConfigure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException {
		// Now check the ID column
		DataColumnSpec colSpec =
				inSpecs[0].getColumnSpec(m_idColName.getStringValue());

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added, trying to avoid
				// the molecule column
				DataType colType = inSpecs[0].getColumnSpec(i).getType();
				if (colType.isCompatible(StringValue.class)
						&& !inSpecs[0].getColumnSpec(i).getName()
								.equals(m_molColName.getStringValue())) {
					m_idColName.setStringValue(
							inSpecs[0].getColumnSpec(i).getName());
					logger.warn("No ID column selected. "
							+ m_idColName.getStringValue() + " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found so
				// we use the molecule column
				if (i == 0) {
					m_idColName.setStringValue(m_molColName.getStringValue());
					logger.warn(
							"No ID column selected, and none available - using the molecule column");
				}
			}
		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				// The column is not compatible with one of the accepted types
				logger.error("The column " + m_idColName.getStringValue()
						+ " is not String-compatible");
				throw new InvalidSettingsException(
						"The column " + m_idColName.getStringValue()
								+ " is not String-compatible");
			}
		}

		if (renderFragmentationMdl != null
				&& renderFragmentationMdl.getBooleanValue()) {
			if (!(renderBreakingBondMdl.getBooleanValue()
					|| renderKeyBondMdl.getBooleanValue()
					|| renderValueBondMdl.getBooleanValue())) {
				throw new InvalidSettingsException(
						"No options to render selected");
			}
		}

		m_spec_0 = createSpec_0(inSpecs[0]);
		m_spec_1 = createSpec_1(inSpecs[0]);
		keptColMask = createKeptColMask(inSpecs[0]);

		// Update any views parameters
		numRows = null;
		rowsRun = 0;
		completedQueue = 0;
		activeThreads = 0;
		notifyViews(true);
		return new DataTableSpec[] { m_spec_0, m_spec_1 };
	}

	protected int countFragmentationColumns() {

		int colCnt = 3; // ID, key, value

		if (m_outputNumChgHAs.getBooleanValue()) {
			colCnt++;
		}
		if (m_outputHARatio.getBooleanValue()) {
			colCnt++;
		}
		if (m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue()) {
			colCnt += m_numCuts.getIntValue();
		}
		if (m_addValueGraphDistanceFingerprint.getBooleanValue()) {
			colCnt++;
		}
		if (isMulticut) {
			colCnt++;
		}
		if (renderFragmentationMdl != null
				&& renderFragmentationMdl.getBooleanValue()) {
			colCnt++;
		}
		return colCnt;
	}

	/**
	 * Method to create the 1st output table spec based on the node settings
	 * 
	 * @param spec
	 *            The incoming data table spec
	 * @return The 1st output table spec
	 */
	protected DataTableSpec createSpec_0(DataTableSpec spec) {
		int numCols = countFragmentationColumns();

		List<String> keptColNames = new ArrayList<>(
				Arrays.asList(m_keptColumns.applyTo(spec).getIncludes()));
		if (!m_idColName.useRowID()) {
			keptColNames.remove(m_idColName.getStringValue());
		}
		numCols += keptColNames.size();

		DataColumnSpec[] specs = new DataColumnSpec[numCols];
		int i = 0;
		specs[i++] = createColSpec("ID", StringCell.TYPE);
		specs[i++] = createColSpec(
				"Fragmentation 'Key' (" + (isMulticut ? "Upto " : "")
						+ m_numCuts.getIntValue() + " bond cuts)",
				MMPConstants.DEFAULT_OUTPUT_MOLECULE_COMPONENT_TYPE);
		specs[i++] = createColSpec("Fragmentation 'Value'",
				MMPConstants.DEFAULT_OUTPUT_MOLECULE_COMPONENT_TYPE);

		if (renderFragmentationMdl != null
				&& renderFragmentationMdl.getBooleanValue()) {
			specs[i++] = createColSpec("Fragmentation Depiction",
					fragUtilityFactory.getRendererType());
		}

		if (m_outputNumChgHAs.getBooleanValue()) {
			specs[i++] = createColSpec("Changing Heavy Atoms", IntCell.TYPE);
		}
		if (m_outputHARatio.getBooleanValue()) {
			specs[i++] =
					createColSpec("Ratio of Changing / Unchanging Heavy Atoms",
							DoubleCell.TYPE);
		}
		if (m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue()) {
			for (int fpIdx = 1; fpIdx <= m_numCuts.getIntValue(); fpIdx++) {
				final DataColumnSpecCreator colSpec = new DataColumnSpecCreator(
						"Attachment point " + fpIdx + " fingerprint",
						DenseBitVectorCell.TYPE);
				Map<String, String> fpColProps = new LinkedHashMap<>();
				fpColProps.put("Length", "" + m_fpLength.getIntValue());
				fpColProps.put("Radius", "" + m_morganRadius.getIntValue());
				if (m_fpUseBondTypes != null) {
					fpColProps.put("Use bond types",
							"" + m_fpUseBondTypes.getBooleanValue());
				}
				if (m_fpUseChirality != null) {
					fpColProps.put("Use chirality",
							"" + m_fpUseChirality.getBooleanValue());
				}
				fpColProps.put("Toolkit", fragUtilityFactory.getToolkitName());
				colSpec.setProperties(new DataColumnProperties(fpColProps));
				specs[i++] = colSpec.createSpec();
			}
		}
		if (m_addValueGraphDistanceFingerprint.getBooleanValue()) {
			specs[i++] = new DataColumnSpecCreator(
					"Value Attachment Point Graph Distance fingerprint",
					DenseByteVectorCell.TYPE).createSpec();
		}

		if (isMulticut) {
			specs[i++] = createColSpec("Number of Cuts", IntCell.TYPE);
		}

		for (String keptCol : keptColNames) {
			DataColumnSpec inColSpec = spec.getColumnSpec(keptCol);
			String newName = getUniqueColumnName(specs, inColSpec.getName());
			if (!newName.equals(inColSpec.getName())) {
				DataColumnSpecCreator specFact =
						new DataColumnSpecCreator(inColSpec);
				specFact.setName(newName);
				specs[i++] = specFact.createSpec();
			} else {
				specs[i++] = spec.getColumnSpec(keptCol);
			}
		}

		return new DataTableSpec(specs);
	}

	/**
	 * Method to create the 2nd output table spec (failed rows) based on the
	 * node settings
	 * 
	 * @param dataTableSpec
	 *            Incoming DTS
	 * @return DTS with optional fail reason column
	 */
	protected DataTableSpec createSpec_1(DataTableSpec dataTableSpec) {
		if (m_addFailReasons.getBooleanValue()) {
			DataTableSpecCreator retVal =
					new DataTableSpecCreator(dataTableSpec);
			retVal.addColumns(new DataColumnSpecCreator(DataTableSpec
					.getUniqueColumnName(dataTableSpec, "Failure Reason"),
					StringCell.TYPE).createSpec());
			return retVal.createSpec();
		} else {
			return dataTableSpec;
		}
	}

	protected int[] createKeptColIds(DataTableSpec spec) {
		return Arrays.stream(m_keptColumns.applyTo(spec).getIncludes())
				.filter(x -> !x.equals(m_idColName.getStringValue()))
				.mapToInt(x -> spec.findColumnIndex(x)).toArray();
	}

	protected boolean[] createKeptColMask(DataTableSpec spec) {
		final String[] includes = m_keptColumns.applyTo(spec).getIncludes();
		boolean[] retVal = new boolean[spec.getNumColumns()];
		for (String colName : includes) {
			if (!m_idColName.useRowID()
					&& colName.equals(m_idColName.getStringValue())) {
				continue;
			}
			retVal[spec.findColumnIndex(colName)] = true;
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragment.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#doExecute(org.knime.core.node.
	 * BufferedDataTable[])
	 */
	@Override
	protected BufferedDataTable[] doExecute(BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {
		// Do some setting up
		BufferedDataTable table = inData[0];
		DataTableSpec spec = table.getSpec();
		final int molColIdx =
				spec.findColumnIndex(m_molColName.getStringValue());
		final int idColIdx = m_idColName.useRowID() ? -1
				: spec.findColumnIndex(m_idColName.getStringValue());
		numRows = table.size();

		// set any colours for optional rendering
		bondColour = renderFragmentationMdl.getBooleanValue()
				&& renderBreakingBondMdl.getBooleanValue()
						? breakingBondColourMdl.getColorValue()
						: null;
		keyColour = renderFragmentationMdl.getBooleanValue()
				&& renderKeyBondMdl.getBooleanValue()
						? keyColourMdl.getColorValue()
						: null;
		valueColour = renderFragmentationMdl.getBooleanValue()
				&& renderValueBondMdl.getBooleanValue()
						? valueColourMdl.getColorValue()
						: null;

		final BufferedDataContainer[] dc = new BufferedDataContainer[] {
				exec.createDataContainer(
						m_spec_0)/* A table for the processed output */,
				exec.createDataContainer(
						m_spec_1)/* And a table for unprocessed rows */ };

		// Deal with the empty table situation
		if (numRows == 0) {
			Arrays.stream(dc).forEach(x -> x.close());
			return Arrays.stream(dc).map(x -> x.getTable())
					.toArray(x -> new BufferedDataTable[x]);
		}
		try {
			this.setQueueSize(MatchedPairPreferencePage.getQueueSize());
			this.setNumThreads(MatchedPairPreferencePage.getThreadsCount());
			// update open views
			notifyViews(true);

			// Sort out the reaction
			String fragSMIRKS = getFragmentationSMARTSMatch();
			final U bondMatch = getMatcherFromSMARTS(fragSMIRKS);

			// Now do some final setting up
			final int maxNumCuts = m_numCuts.getIntValue();
			final int minNumCuts = isMulticut ? 1 : maxNumCuts;

			final boolean addHs = isMulticut ? m_AddHs.getBooleanValue()
					: (maxNumCuts == 1) ? m_AddHs.getBooleanValue() : false;

			// These three can all be null
			final Integer maxNumVarAtm = (m_hasChangingAtoms.getBooleanValue())
					? m_maxChangingAtoms.getIntValue()
					: null;
			final Integer minNumFixedAtm =
					(hasMinFixedHAFilter.getBooleanValue())
							? minFixedHAMdl.getIntValue()
							: null;
			final Double minCnstToVarAtmRatio =
					(m_hasHARatioFilter.getBooleanValue())
							? m_minHARatioFilter.getDoubleValue()
							: null;

			final boolean stripHsAtEnd = m_stripHsAtEnd.isEnabled()
					&& m_stripHsAtEnd.getBooleanValue();

			logger.info("Fragmentation SMIRKS: " + fragSMIRKS + " (Upto "
					+ maxNumCuts + " cuts)");
			logger.info("Using " + getNumThreads() + " threads and "
					+ getQueueSize() + " queue items to parallel process...");
			logger.info("Starting fragmentation at " + new Date().toString());
			long systemStartTime = System.nanoTime();

			Map<RowKey, Set<RowKey>> mapping = new HashMap<>();

			MultiThreadWorker<DataRow, Set<AbstractMulticomponentFragmentationParser<T>>> processor =
					new MultiThreadWorker<DataRow, Set<AbstractMulticomponentFragmentationParser<T>>>(
							getQueueSize(), getNumThreads()) {

						@Override
						protected
								Set<AbstractMulticomponentFragmentationParser<T>>
								compute(DataRow in, long index)
										throws IncomingMoleculeException,
										ClosedFactoryException {

							try {
								long index1 = index + 1L;
								Set<AbstractMulticomponentFragmentationParser<T>> retVal =
										runFragmentationsOnRow(
												getMoleculeFromRow(in,
														molColIdx, idColIdx,
														index1),
												bondMatch, index1, minNumCuts,
												maxNumCuts,
												m_prochiralAsChiral
														.getBooleanValue(),
												addHs, stripHsAtEnd,
												m_allowTwoCutsToBondValue
														.getBooleanValue(),
												maxNumVarAtm, minNumFixedAtm,
												minCnstToVarAtmRatio, exec);

								updateProgress(exec);
								return retVal;
							} catch (CanceledExecutionException e) {
								throw new CancellationException();
							} finally {
								fragUtilityFactory.rowCleanup(index + 1L);
							}

						}

						@Override
						protected void processFinished(ComputationTask task)
								throws ExecutionException,
								CancellationException, InterruptedException {
							DataRow inRow = task.getInput();
							Set<RowKey> inKeySet =
									Collections.singleton(inRow.getKey());
							Set<AbstractMulticomponentFragmentationParser<T>> result;
							long subRowIdx = 0;
							try {
								result = task.get();
								DataCell idCell = idColIdx < 0
										? new StringCell(
												inRow.getKey().getString())
										: new StringCell(((StringValue) inRow
												.getCell(idColIdx))
														.getStringValue());
								for (AbstractMulticomponentFragmentationParser<T> fragmentation : result) {
									DataRow baseRow = new DefaultRow(
											inRow.getKey().getString() + "_"
													+ (subRowIdx++),
											getCellsForFragmentation(idCell,
													fragmentation));
									if (m_hiliteMdl.getBooleanValue()) {
										mapping.put(baseRow.getKey(), inKeySet);
									}
									dc[0].addRowToTable(new AppendedColumnRow(
											baseRow, inRow, keptColMask));
								}

							} catch (ExecutionException ee) {
								Throwable e = ee.getCause();
								if (e instanceof IncomingMoleculeException) {
									if (verboseLogging) {
										logger.info("Rejecting molecule: ("
												+ inRow.getKey().getString()
												+ "): " + e.getMessage());
									}
									dc[1].addRowToTable(
											(m_addFailReasons.getBooleanValue())
													? new AppendedColumnRow(
															inRow,
															new StringCell(e
																	.getMessage()))
													: inRow);
								} else {
									throw ee;
								}
							} catch (DataContainerException e) {
								// Cancellation can cause table writing blow-up!
								if (!task.isCancelled()) {
									throw new InterruptedException(
											"Exception encountered during execution: "
													+ e.getClass()
															.getSimpleName()
													+ " '" + e.getMessage()
													+ "'");
								}
							} finally {
								updateProgress(exec);
							}

							try {
								exec.checkCanceled();
							} catch (CanceledExecutionException e) {
								throw new CancellationException();
							}

						}

						/**
						 * @param exec
						 */
						private void
								updateProgress(final ExecutionContext exec) {
							rowsRun = getFinishedCount();
							completedQueue = getFinishedTaskCount();
							activeThreads = getActiveCount();
							exec.setProgress(1.0 * rowsRun / numRows,
									"Completed row " + rowsRun + " of "
											+ numRows + "; " + completedQueue
											+ " further rows finished and waiting release; "
											+ activeThreads
											+ " active threads");
							notifyViews(false);
						}
					};

			try {
				processor.run(table);
				rowsRun = numRows;
			} catch (InterruptedException e) {
				CanceledExecutionException cee =
						new CanceledExecutionException(e.getMessage());
				cee.initCause(e);
				rowsRun = 0;
				throw cee;
			} finally {
				// Tidy up the views
				completedQueue = 0;
				activeThreads = 0;
				// update open views
				notifyViews(true);
			}
			Arrays.stream(dc).forEach(x -> x.close());
			if (m_hiliteMdl.getBooleanValue()) {
				hiLiteTranslator.setMapper(new DefaultHiLiteMapper(mapping));
			}
			logger.info("Fragmentation completed at " + new Date().toString());
			Duration duration =
					Duration.ofNanos(System.nanoTime() - systemStartTime);
			StringBuilder sb = new StringBuilder().append(inData[0].size())
					.append(" rows fragmented in ");
			boolean keepanyway = false;
			if (duration.toDays() > 0) {
				sb.append(duration.toDays()).append("days ");
				duration = duration.minusDays(duration.toDays());
				keepanyway = true;
			}
			if (keepanyway || duration.toHours() > 0) {
				sb.append(duration.toHours()).append("h ");
				duration = duration.minusHours(duration.toHours());
				keepanyway = true;
			}
			if (keepanyway || duration.toMinutes() > 0) {
				sb.append(duration.toMinutes()).append("mins ");
				duration = duration.minusMinutes(duration.toMinutes());
				keepanyway = true;
			}
			if (duration.toMillis() > 1000) {
				sb.append(String.format("%.2f", duration.toMillis() / 1000.0))
						.append("s");
			} else {
				sb.append(duration.toMillis()).append("ms");
			}
			logger.info(sb.toString());
			logger.info(dc[0].size() + " fragments produced");
			logger.info(dc[1].size() + " rows rejected");
		} catch (Exception e) {
			throw e;
		}
		return Arrays.stream(dc).map(x -> x.getTable())
				.toArray(x -> new BufferedDataTable[x]);
	}

	protected DataCell[] getCellsForFragmentation(DataCell idCell,
			AbstractMulticomponentFragmentationParser<T> fragmentation) {
		DataCell[] cells = new DataCell[countFragmentationColumns()];
		Arrays.fill(cells, DataType.getMissingCell());
		int colIdx = 0;
		cells[colIdx++] = idCell;
		AbstractFragmentKey<T> key = fragmentation.getKey();
		// The fragmentation factory should already have
		// removed H's so we dont
		// repeat the effort here!
		cells[colIdx++] = key.getKeyAsDataCell();
		cells[colIdx++] = fragmentation.getValue().getSMILESCell();

		if (renderFragmentationMdl != null
				&& renderFragmentationMdl.getBooleanValue()) {
			cells[colIdx++] = fragmentation.getRenderingCell();
		}

		if (m_outputNumChgHAs.getBooleanValue()) {
			cells[colIdx++] =
					fragmentation.getValue().getNumberChangingAtomsCell();
		}
		if (m_outputHARatio.getBooleanValue()) {
			cells[colIdx++] =
					fragmentation.getKey().getConstantToVaryingAtomRatioCell(
							fragmentation.getValue());
		}
		if (m_apFingerprints.isEnabled()
				&& m_apFingerprints.getBooleanValue()) {
			for (int i = 0; i < fragmentation.getNumCuts(); i++) {
				AbstractLeaf<T> leaf = null;
				try {
					leaf = key.getLeafWithIdx(i + 1);
				} catch (IndexOutOfBoundsException e) {
					// We have got no more leafs to find
					break;
				}
				cells[colIdx++] = leaf.getMorganFingerprintCell(
						m_morganRadius.getIntValue(), m_fpLength.getIntValue(),
						m_fpUseChirality.getBooleanValue(),
						m_fpUseBondTypes.getBooleanValue());
			}
			colIdx += m_numCuts.getIntValue() - fragmentation.getNumCuts();
		}
		if (m_addValueGraphDistanceFingerprint.getBooleanValue()) {
			cells[colIdx++] = fragmentation.getValue()
					.getAttachmentPointGraphDistanceFingerprint();
		}
		if (isMulticut) {
			cells[colIdx++] = new IntCell(fragmentation.getNumCuts());
		}

		return cells;
	}

	/**
	 * @param mol
	 * @param bondMatch
	 * @param rowIndexForGc
	 *            The index of the row, for possible GC
	 * @param minNumCuts
	 * @param maxNumCuts
	 * @param prochiralAsChiral
	 * @param addHs
	 * @param stripHsAtEnd
	 * @param allowTwoCutsToBondValue
	 * @param maxNumVarAtm
	 * @param minNumFixedAtm
	 *            TODO
	 * @param minCnstToVarAtmRatio
	 * @param exec
	 * @param addFailReasons
	 * @param logger
	 * @param verboseLogging
	 * @param retVal
	 * @return
	 * @throws IllegalArgumentException
	 * @throws CanceledExecutionException
	 * @throws ClosedFactoryException
	 */
	protected Set<AbstractMulticomponentFragmentationParser<T>>
			runFragmentationsOnRow(T mol, U bondMatch, long rowIndexForGc,
					int minNumCuts, int maxNumCuts, boolean prochiralAsChiral,
					boolean addHs, boolean stripHsAtEnd,
					boolean allowTwoCutsToBondValue, Integer maxNumVarAtm,
					Integer minNumFixedAtm, Double minCnstToVarAtmRatio,
					ExecutionContext exec) throws CanceledExecutionException,
					IncomingMoleculeException, ClosedFactoryException {

		Set<AbstractMulticomponentFragmentationParser<T>> fragmentations =
				new TreeSet<>();

		// Deal with 1 cut
		MoleculeFragmentationFactory2<T, U> fragFactory = null;
		MoleculeFragmentationFactory2<T, U> fragFactoryHAdded = null;
		long numberOfFragmentations = 0;

		// Initialise the factories
		if (addHs) {
			// Treat separately, using an H-added factory to break along all the
			// bonds-to-hydrogen
			try {
				fragFactoryHAdded =
						fragUtilityFactory.createHAddedFragmentationFactory(mol,
								bondMatch, stripHsAtEnd, verboseLogging,
								prochiralAsChiral, maxNumVarAtm, minNumFixedAtm,
								minCnstToVarAtmRatio, rowIndexForGc);
			} catch (ToolkitException e) {
				throw new IncomingMoleculeException(
						"Unable to add Hydrogens - " + e.getMessage(), e);
			}
		}
		try {
			fragFactory = fragUtilityFactory.createFragmentationFactory(mol,
					bondMatch,
					incomingExplicitHsOption.getRemoveHsAfterFragmentation(),
					false, verboseLogging, prochiralAsChiral, maxNumVarAtm,
					minNumFixedAtm, minCnstToVarAtmRatio, cacheSize);
		} catch (ToolkitException e) {
			throw new IncomingMoleculeException(
					"Unable to create fragmentation factory - "
							+ e.getMessage(),
					e);
		}

		// Now handle possible complexity limit filter
		if (m_limitByComplexity.getBooleanValue()) {
			if (addHs) {
				numberOfFragmentations +=
						fragFactoryHAdded.getMatchingBonds().size();
			}
			final int matchingBondCount = fragFactory.getMatchingBonds().size();
			numberOfFragmentations += matchingBondCount;
			// Each 1-bond cut can be either way around...
			numberOfFragmentations *= 2;
			if (minNumCuts <= 2 && maxNumCuts >= 2 && allowTwoCutsToBondValue) {
				if (m_limitByComplexity.getBooleanValue()) {
					numberOfFragmentations += matchingBondCount;
				}
			}

			int i = Math.max(2, minNumCuts);
			while (i <= maxNumCuts && i < matchingBondCount
					&& numberOfFragmentations >= 0
					&& numberOfFragmentations < m_maxFragmentations
							.getIntValue()) {
				// -ve numberOfFragmentations indicates numerical overflow
				numberOfFragmentations += AbstractFragmentationFactory
						.numCombinations(matchingBondCount, i++);
			}

			if (numberOfFragmentations > m_maxFragmentations.getIntValue()) {
				if (addHs) {
					fragFactoryHAdded.close();
				}
				fragFactory.close();
				throw new IncomingMoleculeException(
						"Molecule failed complexity limit");
			}
		}

		// Now do the fragmentations
		if (addHs) {
			try {
				fragmentations.addAll(
						fragFactoryHAdded.breakMoleculeAlongMatchingBonds(exec,
								bondColour, keyColour, valueColour));
			} catch (IllegalArgumentException | ToolkitException e) {
				fragFactory.close();
				throw new IncomingMoleculeException(
						"Unable to fragment molecule - " + e.getMessage(), e);
			} finally {
				fragFactoryHAdded.close();

			}
		}
		if (minNumCuts == 1) {
			// Do 1 cut along all matching bonds
			try {
				fragmentations.addAll(
						fragFactory.breakMoleculeAlongMatchingBonds(exec,
								bondColour, keyColour, valueColour));
			} catch (ToolkitException e) {
				fragFactory.close();
				throw new IncomingMoleculeException(
						"Unable to fragment molecule - " + e.getMessage(), e);
			}
		}

		// Deal with the special case of 2 cuts, and allowing *-* as a value
		if (minNumCuts <= 2 && maxNumCuts >= 2 && allowTwoCutsToBondValue) {
			try {
				fragmentations.addAll(fragFactory
						.breakMoleculeAlongMatchingBondsWithBondInsertion(exec,
								bondColour, keyColour, valueColour));
			} catch (ToolkitException e) {
				fragFactory.close();
				throw new IncomingMoleculeException(
						"Unable to fragment molecule - " + e.getMessage(), e);
			}
		}

		// Now do 2 or more cuts
		if (maxNumCuts >= 2) {

			// Now generate the combinations of bonds to cut for 2 or more cuts,
			// removing higher
			// graphs of invalid triplets where appropriate
			// Why doesnt this take cuttableBonds as an argument? - Because
			// cuttable
			// bonds change with number of cuts!
			Set<Set<BondIdentifier>> bondCombos;
			try {
				bondCombos = fragFactory.generateCuttableBondCombos(
						Math.max(minNumCuts, 2), maxNumCuts);
			} catch (IllegalArgumentException | ToolkitException e) {
				fragFactory.close();
				throw new IncomingMoleculeException(
						"Unable to fragment molecule - " + e.getMessage(), e);
			}
			fragmentations.addAll(fragFactory.breakMoleculeAlongBondCombos(
					bondCombos, prochiralAsChiral, exec, bondColour, keyColour,
					valueColour, logger, verboseLogging));
		}
		fragFactory.close();

		// If we are here and no fragmentations, then there will not be any...
		if (fragmentations.size() == 0) {
			// No frags generated
			throw new IncomingMoleculeException("No fragmentations generated");
		}
		return fragmentations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getInputPortRoles()
	 */
	@Override
	public InputPortRole[] getInputPortRoles() {
		return new InputPortRole[] { InputPortRole.DISTRIBUTED_STREAMABLE };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutputPortRoles()
	 */
	@Override
	public OutputPortRole[] getOutputPortRoles() {
		return new OutputPortRole[] { OutputPortRole.DISTRIBUTED,
				OutputPortRole.DISTRIBUTED };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.NodeModel#createStreamableOperator(org.knime.core.
	 * node.streamable.PartitionInfo, org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public StreamableOperator createStreamableOperator(
			PartitionInfo partitionInfo, PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		try {
			return new StreamableOperator() {
				// Do some setting up

				DataTableSpec spec = (DataTableSpec) inSpecs[0];
				final int molColIdx =
						spec.findColumnIndex(m_molColName.getStringValue());
				final int idColIdx = m_idColName.useRowID() ? -1
						: spec.findColumnIndex(m_idColName.getStringValue());
				// Sort out the reaction
				String fragSMIRKS = getFragmentationSMARTSMatch();
				final U bondMatch = getMatcherFromSMARTS(fragSMIRKS);

				// Now do some final setting up
				final int maxNumCuts = m_numCuts.getIntValue();
				final int minNumCuts = isMulticut ? 1 : maxNumCuts;

				final boolean addHs = isMulticut ? m_AddHs.getBooleanValue()
						: (maxNumCuts == 1) ? m_AddHs.getBooleanValue() : false;

				// These three can all be null
				final Integer maxNumVarAtm =
						(m_hasChangingAtoms.getBooleanValue())
								? m_maxChangingAtoms.getIntValue()
								: null;
				final Integer minNumFixedAtm =
						(hasMinFixedHAFilter.getBooleanValue())
								? minFixedHAMdl.getIntValue()
								: null;
				final Double minCnstToVarAtmRatio =
						(m_hasHARatioFilter.getBooleanValue())
								? m_minHARatioFilter.getDoubleValue()
								: null;

				final boolean stripHsAtEnd = m_stripHsAtEnd.isEnabled()
						&& m_stripHsAtEnd.getBooleanValue();

				final AtomicLong rowIdx = new AtomicLong(1);

				@Override
				public void runFinal(PortInput[] inputs, PortOutput[] outputs,
						ExecutionContext exec) throws Exception {

					RowInput input = (RowInput) inputs[0];
					RowOutput fragmentTable = (RowOutput) outputs[0];
					RowOutput rejectTable = (RowOutput) outputs[1];
					DataRow row;
					while ((row = input.poll()) != null) {
						try {
							long idx = rowIdx.incrementAndGet();
							Set<AbstractMulticomponentFragmentationParser<T>> frags =
									runFragmentationsOnRow(
											getMoleculeFromRow(row, molColIdx,
													idColIdx, idx),
											bondMatch, idx, minNumCuts,
											maxNumCuts,
											m_prochiralAsChiral
													.getBooleanValue(),
											addHs, stripHsAtEnd,
											m_allowTwoCutsToBondValue
													.getBooleanValue(),
											maxNumVarAtm, minNumFixedAtm,
											minCnstToVarAtmRatio, exec);
							long subRowIdx = 0;
							Set<RowKey> mappedKeys = new HashSet<>();

							DataCell idCell = idColIdx < 0
									? new StringCell(row.getKey().getString())
									: new StringCell(((StringValue) row
											.getCell(idColIdx))
													.getStringValue());
							for (AbstractMulticomponentFragmentationParser<T> fragmentation : frags) {
								DataRow baseRow = new DefaultRow(
										row.getKey().getString() + "_"
												+ (subRowIdx++),
										getCellsForFragmentation(idCell,
												fragmentation));
								if (m_hiliteMdl.getBooleanValue()) {
									mappedKeys.add(baseRow.getKey());
								}
								fragmentTable.push(new AppendedColumnRow(
										baseRow, row, keptColMask));
							}
							// if (m_hiliteMdl.getBooleanValue()) {
							// mapping.put(row.getKey(), mappedKeys);
							// }
						} catch (IncomingMoleculeException e) {
							if (verboseLogging) {
								logger.info("Rejecting molecule: ("
										+ row.getKey().getString() + "): "
										+ e.getMessage());
							}
							rejectTable
									.push((m_addFailReasons.getBooleanValue())
											? new AppendedColumnRow(row,
													new StringCell(
															e.getMessage()))
											: row);
						}
						exec.checkCanceled();
					}
					fragmentTable.close();
					rejectTable.close();
				}
			};
		} catch (Exception e) {
			throw new InvalidSettingsException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#setInHiLiteHandler(int,
	 * org.knime.core.node.property.hilite.HiLiteHandler)
	 */
	@Override
	protected void setInHiLiteHandler(int inIndex, HiLiteHandler hiLiteHdl) {
		hiLiteTranslator.removeAllToHiliteHandlers();
		hiLiteTranslator.addToHiLiteHandler(hiLiteHdl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#getOutHiLiteHandler(int)
	 */
	@Override
	protected HiLiteHandler getOutHiLiteHandler(int outIndex) {
		if (outIndex == 0) {
			return hiLiteTranslator.getFromHiLiteHandler();
		}
		return super.getOutHiLiteHandler(outIndex);
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		super.saveSettingsTo(settings);
		m_idColName.saveSettingsTo(settings);
		m_hiliteMdl.saveSettingsTo(settings);
		m_limitByComplexity.saveSettingsTo(settings);
		m_maxFragmentations.saveSettingsTo(settings);

		m_prochiralAsChiral.saveSettingsTo(settings);
		m_hasChangingAtoms.saveSettingsTo(settings);
		m_hasHARatioFilter.saveSettingsTo(settings);
		hasMinFixedHAFilter.saveSettingsTo(settings);
		m_maxChangingAtoms.saveSettingsTo(settings);
		m_minHARatioFilter.saveSettingsTo(settings);
		minFixedHAMdl.saveSettingsTo(settings);

		m_outputNumChgHAs.saveSettingsTo(settings);
		m_outputHARatio.saveSettingsTo(settings);
		m_addFailReasons.saveSettingsTo(settings);
		m_keptColumns.saveSettingsTo(settings);

		m_apFingerprints.saveSettingsTo(settings);
		m_fpLength.saveSettingsTo(settings);
		m_morganRadius.saveSettingsTo(settings);
		if (m_fpUseChirality != null) {
			m_fpUseChirality.saveSettingsTo(settings);
		}
		if (m_fpUseBondTypes != null) {
			m_fpUseBondTypes.saveSettingsTo(settings);
		}
		m_addValueGraphDistanceFingerprint.saveSettingsTo(settings);
		if (fragUtilityFactory.getRendererType() != null) {
			renderFragmentationMdl.saveSettingsTo(settings);
			renderBreakingBondMdl.saveSettingsTo(settings);
			breakingBondColourMdl.saveSettingsTo(settings);
			renderKeyBondMdl.saveSettingsTo(settings);
			keyColourMdl.saveSettingsTo(settings);
			renderValueBondMdl.saveSettingsTo(settings);
			valueColourMdl.saveSettingsTo(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.loadValidatedSettingsFrom(settings);
		m_idColName.loadSettingsFrom(settings);
		m_hiliteMdl.loadSettingsFrom(settings);
		m_limitByComplexity.loadSettingsFrom(settings);
		m_maxFragmentations.loadSettingsFrom(settings);

		m_prochiralAsChiral.loadSettingsFrom(settings);
		m_hasChangingAtoms.loadSettingsFrom(settings);
		m_hasHARatioFilter.loadSettingsFrom(settings);
		m_maxChangingAtoms.loadSettingsFrom(settings);
		m_minHARatioFilter.loadSettingsFrom(settings);
		try {
			hasMinFixedHAFilter.loadSettingsFrom(settings);
			minFixedHAMdl.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			getLogger().info(
					"No settings found for Min Fixed HAC filter - using legacy defaults");
		}

		m_outputNumChgHAs.loadSettingsFrom(settings);
		m_outputHARatio.loadSettingsFrom(settings);
		m_addFailReasons.loadSettingsFrom(settings);
		m_keptColumns.loadSettingsFrom(settings);

		m_apFingerprints.loadSettingsFrom(settings);

		m_fpLength.loadSettingsFrom(settings);
		m_morganRadius.loadSettingsFrom(settings);
		if (m_fpUseChirality != null) {
			m_fpUseChirality.loadSettingsFrom(settings);
		}
		if (m_fpUseBondTypes != null) {
			m_fpUseBondTypes.loadSettingsFrom(settings);
		}
		m_addValueGraphDistanceFingerprint.loadSettingsFrom(settings);
		if (fragUtilityFactory.getRendererType() != null) {
			renderFragmentationMdl.loadSettingsFrom(settings);
			renderBreakingBondMdl.loadSettingsFrom(settings);
			breakingBondColourMdl.loadSettingsFrom(settings);
			renderKeyBondMdl.loadSettingsFrom(settings);
			keyColourMdl.loadSettingsFrom(settings);
			renderValueBondMdl.loadSettingsFrom(settings);
			valueColourMdl.loadSettingsFrom(settings);
		}
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.validateSettings(settings);
		m_prochiralAsChiral.validateSettings(settings);
		m_hiliteMdl.validateSettings(settings);
		m_idColName.validateSettings(settings);
		m_hasChangingAtoms.validateSettings(settings);
		m_hasHARatioFilter.validateSettings(settings);
		m_maxChangingAtoms.validateSettings(settings);
		m_minHARatioFilter.validateSettings(settings);
		m_outputNumChgHAs.validateSettings(settings);
		m_outputHARatio.validateSettings(settings);
		m_keptColumns.validateSettings(settings);
		m_addFailReasons.validateSettings(settings);
		m_apFingerprints.validateSettings(settings);
		m_limitByComplexity.validateSettings(settings);
		m_maxFragmentations.validateSettings(settings);

		// Dont validate min fixed HAC filter settings

		m_fpLength.validateSettings(settings);
		m_morganRadius.validateSettings(settings);
		if (m_fpUseChirality != null) {
			m_fpUseChirality.validateSettings(settings);
		}
		if (m_fpUseBondTypes != null) {
			m_fpUseBondTypes.validateSettings(settings);
		}
		m_addValueGraphDistanceFingerprint.validateSettings(settings);
		if (fragUtilityFactory.getRendererType() != null) {
			renderFragmentationMdl.validateSettings(settings);
			renderBreakingBondMdl.validateSettings(settings);
			breakingBondColourMdl.validateSettings(settings);
			renderKeyBondMdl.validateSettings(settings);
			keyColourMdl.validateSettings(settings);
			renderValueBondMdl.validateSettings(settings);
			valueColourMdl.validateSettings(settings);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragutil.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#loadInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void loadInternals(File internDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		File settingsFile = new File(internDir, SETTINGS_FILE_NAME);
		FileInputStream in = new FileInputStream(settingsFile);
		NodeSettingsRO settings = NodeSettings.loadFromXML(in);
		try {
			if (m_hiliteMdl.getBooleanValue()) {
				NodeSettingsRO mapSet = settings.getNodeSettings(HILITE_KEY);
				hiLiteTranslator.setMapper(DefaultHiLiteMapper.load(mapSet));
			}
		} catch (InvalidSettingsException e) {
			throw new IOException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragutil.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#saveInternals(java.io.File,
	 * org.knime.core.node.ExecutionMonitor)
	 */
	@Override
	protected void saveInternals(File internDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		NodeSettings settings = new NodeSettings("MMPFragment");
		if (m_hiliteMdl.getBooleanValue()) {
			NodeSettingsWO mapSet = settings.addNodeSettings(HILITE_KEY);
			((DefaultHiLiteMapper) hiLiteTranslator.getMapper()).save(mapSet);
		}
		File f = new File(internDir, SETTINGS_FILE_NAME);
		FileOutputStream fos = new FileOutputStream(f);
		settings.saveToXML(fos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.mmp.nodes.fragutil.abstrct.
	 * AbstractMMPFragmentationFactoryNodeModel#reset()
	 */
	@Override
	protected void reset() {
		hiLiteTranslator.setMapper(null);
		super.reset();
	}

}
