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
package com.vernalis.knime.db.nodes.switches;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import com.vernalis.knime.misc.ArrayUtils;

/**
 * {@link NodeModel} implementation for the Reference Database Table Exists node
 * 
 * @author S Roughley
 *
 */
public final class ReferenceDBTableExistsNodeModel
		extends DBTableExistsNodeModel {

	/**
	 * @param creationConfig
	 *            the {@link NodeCreationConfiguration}
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	ReferenceDBTableExistsNodeModel(NodeCreationConfiguration creationConfig) {
		// 2 inputs - first is changeable
		// 2 outputs - both match port type of first input
		super(creationConfig.getPortConfig().orElseThrow().getInputPorts(),
				ArrayUtils.fill(new PortType[2], creationConfig.getPortConfig()
						.orElseThrow().getInputPorts()[0]));
	}

	/**
	 * Method to create the base output port specs called by the
	 * {@link #configure(PortObjectSpec[])} method. The calling method will
	 * check whether the active branch should be set during configure, and if
	 * required replace the inactive branch. The calling method will also check
	 * for the name of the table being present
	 * 
	 * @param inSpecs
	 *            the incoming port spec(s)
	 * 
	 * @return the outgoing port specs
	 * 
	 * @throws InvalidSettingsException
	 *             if there was a problem creating the output
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Override
	protected PortObjectSpec[] getOutputSpecs(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		return ArrayUtils.fill(new PortObjectSpec[2], inSpecs[0]);
	}

	/**
	 * Method to create the base output ports. This is called from the
	 * {@link #execute(PortObject[], ExecutionContext)} method, and should
	 * create the base port objects. The calling method will then replace the
	 * appropriate value with an inactive branch
	 * 
	 * @param inObjects
	 *            in the incoming port object(s)
	 * @param exec
	 *            execution context in case it is needed during output creation
	 * 
	 * @return the outgoing port objects
	 * 
	 * @throws Exception
	 *             if there was an error during output creation
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Override
	protected PortObject[] getOutputPortObjects(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {
		return ArrayUtils.fill(new PortObject[2], inObjects[0]);
	}

}
