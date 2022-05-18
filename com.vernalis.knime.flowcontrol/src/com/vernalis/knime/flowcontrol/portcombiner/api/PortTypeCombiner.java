/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.portcombiner.api;

import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

/**
 * An interface defining a Port Type combiner which provides methods to merge
 * one or more instances of a port and its corresponding port spec.
 * Implementations should not store state as they will be used as singleton
 * instances
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public interface PortTypeCombiner {

	/**
	 * @return a map of the settings models required for the combiner. This
	 *         should return a new map of new instances each time it is called.
	 *         The framework will call this once during node dialog construction
	 *         and once during node model construction
	 */
	public Map<String, SettingsModel> getCombinerModels();

	/**
	 * Method to add the dialog components for the combiner to the node settings
	 * pane. May do nothing if {@link #hasDialogOptions()} returns false
	 * 
	 * @param dialog
	 *            the node dialog
	 * @param models
	 *            the settings models from a call to
	 *            {@link #getCombinerModels()}
	 */
	public void createDialog(DefaultNodeSettingsPane dialog,
			Map<String, SettingsModel> models);

	/**
	 * @return whether the combiner has any user-defined options to display in
	 *         the node dialog
	 */
	public boolean hasDialogOptions();

	/**
	 * Method to create a combined output port spec based on any settings and
	 * the active inputs
	 * 
	 * @param activePorts
	 *            The active, non-null incoming ports. Will always contain at
	 *            least one port
	 * @param models
	 *            the models for the combiner with the stored values for the
	 *            node
	 * @param warnable
	 *            a {@link Warnable} implementation to send any non-fatal but
	 *            informative warning message during node configuration. Most
	 *            likely this is a NodeModel with the
	 *            {@link Warnable#setWarning(String)} relaying the message to
	 *            the setWarningMessage(String) method of the node
	 * 
	 * @return the correct output spec for the node based on the incoming specs
	 *         and the combiner settings. Maybe {@code null} if the output spec
	 *         cannot be determined at node configure time
	 * 
	 * @throws InvalidSettingsException
	 *             if there is a fatal error with the configuration - either in
	 *             reading or applying the settings, or that the current
	 *             settings guarantee that node execution will fail
	 */
	public PortObjectSpec createOutputPortObjectSpec(
			List<? extends PortObjectSpec> activePorts,
			Map<String, SettingsModel> models, Warnable warnable)
			throws InvalidSettingsException;

	/**
	 * Method to create a combined output port object at node execution based on
	 * any settings and the active ports
	 * 
	 * @param activePortIndices
	 *            an array of the indices of the active, non-null incoming
	 *            ports. Will always contain at least one value
	 * @param exec
	 *            the node execution context
	 * @param models
	 *            the models for the combiner with the stored values for the
	 *            node
	 * @param inPorts
	 *            the incoming {@link PortObject}s for the node, some of which
	 *            may be {@code null} or inactive.
	 * 
	 * @return A {@link PortObject} representing the combined incoming active
	 *         ports according to the settings
	 * 
	 * @throws InvalidSettingsException
	 *             if there is a fatal error with the configuration - either in
	 *             reading or applying the settings
	 * @throws CanceledExecutionException
	 *             if the user cancelled during execution (requires calls to
	 *             exec#checkCancelled())
	 * @throws Exception
	 *             if there are any other other exceptions thrown, e.g. because
	 *             the ports could not be combined according to the current
	 *             settings
	 */
	public PortObject createOutputPortObject(int[] activePortIndices,
			ExecutionContext exec, Map<String, SettingsModel> models,
			PortObject[] inPorts) throws InvalidSettingsException,
			CanceledExecutionException, Exception;

}
