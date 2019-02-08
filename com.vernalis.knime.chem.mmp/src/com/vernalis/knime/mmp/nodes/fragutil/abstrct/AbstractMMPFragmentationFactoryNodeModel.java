/*******************************************************************************
 * Copyright (c) 2017, 2019 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.abstrct;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.mmp.FragmentationTypes;
import com.vernalis.knime.mmp.IncomingExplicitHsOption;
import com.vernalis.knime.mmp.IncomingMoleculeException;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

import static com.vernalis.knime.mmp.MolFormats.isColTypeRDKitCompatible;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createAddHModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createAllowTwoCutsToBondValueModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createCustomSMARTSModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createCutsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createIncomingExplicitHsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createMolColumnSettingsModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createSMIRKSModel;
import static com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog.createStripHModel;
import static com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage.MMP_PREF_FRAGMENT_CACHE;
import static com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage.MMP_PREF_VERBOSE_LOGGING;
import static com.vernalis.knime.mmp.prefs.MatchedPairPreferencePage.getThreadsCount;

/**
 * Simplest bare-bones {@link NodeModel} implementation for any node requiring a
 * fragmentation factory for execution
 * <p>
 * Added version argument to allow version 3 nodes to redirect here and
 * correctly convert settings to maintain behaviour (SDR, 11-May-2018)
 * </p>
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the substructure matching object
 */
public abstract class AbstractMMPFragmentationFactoryNodeModel<T, U>
		extends NodeModel {

	protected final SettingsModelString m_molColName =
			createMolColumnSettingsModel();
	protected final SettingsModelString m_fragSMIRKS = createSMIRKSModel();
	protected final SettingsModelString m_customSmarts =
			createCustomSMARTSModel();
	protected final SettingsModelIntegerBounded m_numCuts;
	protected final SettingsModelBoolean m_allowTwoCutsToBondValue;
	protected final SettingsModelBoolean m_AddHs = createAddHModel();
	protected final SettingsModelBoolean m_stripHsAtEnd;
	protected final SettingsModelString m_incomingExplicitHsHandlingMdl;

	protected final FragmentationUtilsFactory<T, U> fragUtilityFactory;

	protected IPreferenceStore prefStore = null;
	protected boolean verboseLogging = false;
	protected Integer numThreads;
	protected Integer queueSize;
	protected int cacheSize;

	protected IncomingExplicitHsOption incomingExplicitHsOption = null;

	/** The node logger instance */
	protected NodeLogger logger = NodeLogger.getLogger(this.getClass());
	protected final boolean isMulticut;
	protected final int version;

	/**
	 * Overloaded constructor which creates a non-multicut node
	 * 
	 * @param inPorts
	 *            The number of incoming ports
	 * @param outPorts
	 *            The number of outgoing ports
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} to use for the node
	 */
	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory, int version) {
		this(inPorts, outPorts, fragUtilityFactory, false, version);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory) {
		this(inPorts, outPorts, fragUtilityFactory, false);
	}

	/**
	 * Constructor
	 * 
	 * @param inPorts
	 *            The number of incoming ports
	 * @param outPorts
	 *            The number of outgoing ports
	 * @param fragUtilityFactory
	 *            The {@link FragmentationUtilsFactory} to use for the node
	 */
	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, int version) {
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, true, version);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut) {
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, true);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, int version) {
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, hasRemoveHs,
				true, version);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs) {
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, hasRemoveHs,
				true);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			int version) {
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, hasRemoveHs,
				hasNumCuts, true, version);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts) {
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, hasRemoveHs,
				hasNumCuts, true);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			boolean hasTwoCutsToBond) {
		// USe default version 4 if none supplied
		this(inPorts, outPorts, fragUtilityFactory, isMulticut, hasRemoveHs,
				hasNumCuts, hasTwoCutsToBond, 4);
	}

	public AbstractMMPFragmentationFactoryNodeModel(int inPorts, int outPorts,
			FragmentationUtilsFactory<T, U> fragUtilityFactory,
			boolean isMulticut, boolean hasRemoveHs, boolean hasNumCuts,
			boolean hasTwoCutsToBond, int version) {
		super(inPorts, outPorts);
		this.version = version;
		this.fragUtilityFactory = fragUtilityFactory;
		this.isMulticut = isMulticut;

		if (hasNumCuts) {
			m_numCuts = createCutsModel();
			m_numCuts.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					m_AddHs.setEnabled(
							isMulticut || m_numCuts.getIntValue() == 1);
					if (hasRemoveHs) {
						m_stripHsAtEnd.setEnabled(m_AddHs.isEnabled()
								&& m_AddHs.getBooleanValue());
					}
					if (isMulticut && hasTwoCutsToBond) {
						m_allowTwoCutsToBondValue
								.setEnabled(m_numCuts.getIntValue() >= 2);
					} else {
						m_allowTwoCutsToBondValue
								.setEnabled(m_numCuts.getIntValue() == 2);
					}
				}
			});
			m_AddHs.setEnabled(isMulticut || m_numCuts.getIntValue() == 1);
		} else {
			m_numCuts = null;
		}

		if (hasTwoCutsToBond) {
			m_allowTwoCutsToBondValue = createAllowTwoCutsToBondValueModel();
		} else {
			m_allowTwoCutsToBondValue = null;
		}
		if (hasRemoveHs) {
			m_stripHsAtEnd = createStripHModel();
			m_stripHsAtEnd.setEnabled(
					m_AddHs.isEnabled() && m_AddHs.getBooleanValue());
			m_incomingExplicitHsHandlingMdl = createIncomingExplicitHsModel();
			m_incomingExplicitHsHandlingMdl
					.addChangeListener(new ChangeListener() {

						@Override
						public void stateChanged(ChangeEvent e) {
							try {
								incomingExplicitHsOption =
										IncomingExplicitHsOption.valueOf(
												m_incomingExplicitHsHandlingMdl
														.getStringValue());
							} catch (Exception e1) {
								logger.warn(
										"Unrecognised option for incoming explicit H treatment - using default setting");
								incomingExplicitHsOption =
										IncomingExplicitHsOption.getDefault();
							}
						}
					});
			try {
				incomingExplicitHsOption = IncomingExplicitHsOption.valueOf(
						m_incomingExplicitHsHandlingMdl.getStringValue());
			} catch (Exception e1) {
				logger.warn(
						"Unrecognised option for incoming explicit H treatment - using default setting");
				incomingExplicitHsOption =
						IncomingExplicitHsOption.getDefault();
			}
		} else {
			m_stripHsAtEnd = null;
			m_incomingExplicitHsHandlingMdl = null;
		}
		if (hasNumCuts && hasTwoCutsToBond) {
			if (isMulticut) {
				m_allowTwoCutsToBondValue
						.setEnabled(m_numCuts.getIntValue() >= 2);
			} else {
				m_allowTwoCutsToBondValue
						.setEnabled(m_numCuts.getIntValue() == 2);
			}
		}
		m_fragSMIRKS.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				m_customSmarts
						.setEnabled(FragmentationTypes.valueOf(m_fragSMIRKS
								.getStringValue()) == FragmentationTypes.USER_DEFINED);

			}
		});
		m_customSmarts.setEnabled(FragmentationTypes.valueOf(m_fragSMIRKS
				.getStringValue()) == FragmentationTypes.USER_DEFINED);

		// Preferences
		prefStore = MatchedPairsMultipleCutsNodePlugin.getDefault()
				.getPreferenceStore();
		prefStore.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {

				// Re-load the settings - onlve verbose logging - the queue and
				// threads are picked up at #execute
				verboseLogging = prefStore.getBoolean(MMP_PREF_VERBOSE_LOGGING);
				cacheSize = prefStore.getInt(MMP_PREF_FRAGMENT_CACHE);
			}
		});

		verboseLogging = prefStore.getBoolean(MMP_PREF_VERBOSE_LOGGING);
		cacheSize = prefStore.getInt(MMP_PREF_FRAGMENT_CACHE);
		this.setQueueSize(getQueueSize());
		this.setNumThreads(getThreadsCount());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir,
			final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		fragUtilityFactory.nodeReset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeModel#onDispose()
	 */
	@Override
	protected void onDispose() {
		fragUtilityFactory.nodeDispose();
		super.onDispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		fragUtilityFactory.nodeDispose();
		super.finalize();
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		m_molColName.saveSettingsTo(settings);
		m_fragSMIRKS.saveSettingsTo(settings);
		m_customSmarts.saveSettingsTo(settings);
		if (m_numCuts != null) {
			m_numCuts.saveSettingsTo(settings);
		}
		if (m_allowTwoCutsToBondValue != null) {
			m_allowTwoCutsToBondValue.saveSettingsTo(settings);
		}
		m_AddHs.saveSettingsTo(settings);
		if (m_stripHsAtEnd != null) {
			m_stripHsAtEnd.saveSettingsTo(settings);
		}
		if (m_incomingExplicitHsHandlingMdl != null) {
			m_incomingExplicitHsHandlingMdl.saveSettingsTo(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_molColName.loadSettingsFrom(settings);
		m_fragSMIRKS.loadSettingsFrom(settings);
		m_customSmarts.loadSettingsFrom(settings);
		if (m_numCuts != null) {
			m_numCuts.loadSettingsFrom(settings);
		}
		if (m_allowTwoCutsToBondValue != null) {
			m_allowTwoCutsToBondValue.loadSettingsFrom(settings);
		}
		m_AddHs.loadSettingsFrom(settings);
		if (m_stripHsAtEnd != null) {
			m_stripHsAtEnd.loadSettingsFrom(settings);
		}
		if (m_incomingExplicitHsHandlingMdl != null) {
			m_incomingExplicitHsHandlingMdl.loadSettingsFrom(settings);
		}
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {

		m_AddHs.validateSettings(settings);
		m_molColName.validateSettings(settings);
		m_fragSMIRKS.validateSettings(settings);
		m_customSmarts.validateSettings(settings);
		if (m_numCuts != null) {
			m_numCuts.validateSettings(settings);
		}
		if (m_allowTwoCutsToBondValue != null) {
			m_allowTwoCutsToBondValue.validateSettings(settings);
		}
		if (m_stripHsAtEnd != null) {
			m_stripHsAtEnd.validateSettings(settings);
		}
		if (m_incomingExplicitHsHandlingMdl != null) {
			m_incomingExplicitHsHandlingMdl.validateSettings(settings);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
			throws InvalidSettingsException {

		// Check the molCol is a molecule
		DataColumnSpec colSpec =
				inSpecs[0].getColumnSpec(m_molColName.getStringValue());

		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpecs[0].getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				if (isColTypeRDKitCompatible(
						inSpecs[0].getColumnSpec(i).getType())) {
					m_molColName.setStringValue(
							inSpecs[0].getColumnSpec(i).getName());
					logger.warn("No molecule column selected. "
							+ m_molColName.getStringValue()
							+ " auto-selected.");
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					logger.error("No molecule column of the accepted"
							+ " input formats (SDF, Mol, SMILES) was found.");
					throw new InvalidSettingsException(
							"No molecule column of the accepted"
									+ " input formats (SDF, Mol, SMILES) was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!isColTypeRDKitCompatible(colSpec.getType())) {
				// The column is not compatible with one of the accepted types
				logger.error("The column " + m_molColName.getStringValue()
						+ " is not one of the accepted"
						+ " input formats (SDF, Mol, SMILES)");
				throw new InvalidSettingsException(
						"The column " + m_molColName.getStringValue()
								+ " is not one of the accepted"
								+ " input formats (SDF, Mol, SMILES)");
			}
		}

		if (FragmentationTypes.valueOf(m_fragSMIRKS
				.getStringValue()) == FragmentationTypes.USER_DEFINED) {
			if (m_customSmarts.getStringValue() == null
					|| "".equals(m_customSmarts.getStringValue())) {
				logger.error("A reaction SMARTS string must be provided "
						+ "for user-defined fragmentation patterns");
				throw new InvalidSettingsException(
						"A reaction SMARTS string must be provided "
								+ "for user-defined fragmentation patterns");
			}
		}
		// Just check it here
		getFragmentationSMARTSMatch();

		return doConfigure(inSpecs);
	}

	abstract protected DataTableSpec[] doConfigure(DataTableSpec[] inSpecs)
			throws InvalidSettingsException;

	/**
	 * Method to check for unique column names from an array of
	 * {@link DataColumnSpec}. Based on
	 * {@link DataTableSpec#getUniqueColumnName(DataTableSpec, String)
	 * 
	 * @param colSpecs
	 *            The column specs - some of which may be <code>null</code>
	 *            (which will be ignored)
	 * @param columnName
	 *            The new column name
	 * @return A uniquified version of the column name if it duplicates any
	 *         previous
	 */
	protected String getUniqueColumnName(final DataColumnSpec[] colSpecs,
			final String columnName) {

		if (columnName == null) {
			throw new NullPointerException("Column name must not be null.");
		}

		List<String> names = Arrays.stream(colSpecs).filter(x -> x != null)
				.map(x -> x.getName()).collect(Collectors.toList());

		String trimedName = columnName.trim();
		int uniquifier = 1;
		String result = trimedName;
		while (names.contains(result)) {
			result = trimedName + " (#" + uniquifier + ")";
			uniquifier++;
		}
		return result;
	}

	/**
	 * Creates a column spec from the name and type
	 * 
	 * @param colName
	 *            The column name
	 * @param colType
	 *            The column {@link DataType}
	 * @return A {@link DataColumnSpec}
	 */
	protected final DataColumnSpec createColSpec(String colName,
			DataType colType) {
		return (new DataColumnSpecCreator(colName, colType)).createSpec();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception {

		try {
			return doExecute(inData, exec);
		} catch (Exception e) {
			throw e;
		} finally {
			fragUtilityFactory.postExecuteCleanup();
		}

	}

	abstract protected BufferedDataTable[] doExecute(BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception;

	/**
	 * Method to get the molecule from the row. The molecule is validated to
	 * ensure that it is not a missing cell, has a non-missing ID cell, and is a
	 * single component with at least 1 atom
	 * 
	 * @param row
	 *            The {@link DataRow} to extract the molecule from @param
	 *            molColIdx The column index of the molecule @param idColIdx The
	 *            column index of the row @return The validated molecule
	 *            object @throws IncomingMoleculeException If there is a problem
	 *            with the molecule
	 * @param molColIdx
	 *            The column index of the incoming molecules
	 * @param idColIdx
	 *            The column index of the IDs - or -1 if this is not to be
	 *            validated
	 * @param rowIndex
	 *            The index of the current row - may be needed for garbage
	 *            collection
	 */
	protected T getMoleculeFromRow(DataRow row, int molColIdx, Integer idColIdx,
			long rowIndex) throws IncomingMoleculeException {

		// We need both a molecule cell and an ID cell
		DataCell molCell = row.getCell(molColIdx);
		if (molCell.isMissing()) {
			// Deal with missing mols
			throw new IncomingMoleculeException(
					"Missing value in Molecule Column");
		}

		if (idColIdx >= 0 && row.getCell(idColIdx).isMissing()) {
			// Missing ID - causes problems later!
			throw new IncomingMoleculeException("Missing value in ID Column");
		}

		// Now try and get the molecule in the correct format:
		T mol;
		try {
			mol = fragUtilityFactory.getMolFromCell(row.getCell(molColIdx),
					rowIndex,
					incomingExplicitHsOption == null ? false
							: incomingExplicitHsOption
									.getRemoveHsBeforeFragmentation());
		} catch (ToolkitException e) {
			// Log the failed row
			throw new IncomingMoleculeException(e.getMessage(), e);
		}

		// Check we got a molecule containing atoms
		if (mol == null || fragUtilityFactory.moleculeIsEmpty(mol)) {
			// Deal with when we cannot get a mol object - e.g. for 'No
			// Structure' Mol files
			// And add it to the second output
			throw new IncomingMoleculeException(
					"'No Structure' input molecule");
		}

		// Multicomponent molecules make no sense... (and duplicate salts crash
		// the duplicate key resolver!)
		if (fragUtilityFactory.moleculeIsMultiComponent(mol)) {
			throw new IncomingMoleculeException(
					"Multi-component structures cannot be fragmented");
		}

		return mol;
	}

	/**
	 * @return The number of threads currently set to use
	 */
	public Integer getNumThreads() {
		return numThreads;
	}

	/**
	 * @return the queue size to use for completed fragmentations
	 */
	public Integer getQueueSize() {
		return queueSize;
	}

	/**
	 * @param numThreads
	 *            Set the number of threads being used
	 */
	protected void setNumThreads(Integer numThreads) {
		this.numThreads = numThreads;
	}

	/**
	 * @param queueSize
	 *            Set the queue size
	 */
	protected void setQueueSize(Integer queueSize) {
		this.queueSize = queueSize;
	}

	/**
	 * Method which uses the {@link FragmentationUtilsFactory} object to
	 * generate a Matcher Object from the SMARTS or reaction SMARTS string
	 * 
	 * @param fragSMIRKS
	 *            The SMARTS or Reaction SMARTS string
	 * @return The Matcher object
	 * @throws ToolkitException
	 */
	protected U getMatcherFromSMARTS(String fragSMIRKS)
			throws ToolkitException {
		return fragUtilityFactory.getMatcher(fragSMIRKS.contains(">>")
				? fragSMIRKS.split(">>")[0] : fragSMIRKS);
	}

	/**
	 * @return The fragmentation SMARTS Matcher string or Reaction SMARTS String
	 *         from based on the current settings models
	 * @throws InvalidSettingsException
	 */
	protected String getFragmentationSMARTSMatch()
			throws InvalidSettingsException {

		final FragmentationTypes fragType =
				FragmentationTypes.valueOf(m_fragSMIRKS.getStringValue());
		String fragSMIRKS = fragType.getSMARTS();
		if ((fragSMIRKS == null || "".equals(fragSMIRKS))
				&& fragType == FragmentationTypes.USER_DEFINED) {
			fragSMIRKS = m_customSmarts.getStringValue();
		}
		String validation =
				fragUtilityFactory.validateMatcherSmarts(fragSMIRKS);
		if (validation != null) {
			throw new InvalidSettingsException(
					"Invalid SMARTS Matcher - " + validation);
		}
		return fragSMIRKS;
	}

}
