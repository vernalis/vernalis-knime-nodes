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
package com.vernalis.knime.db.nodes.removesql;

import org.knime.base.node.io.database.DBNodeModel;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.database.node.util.PortObjectSpecHelper;
import org.knime.database.port.DBDataPortObject;
import org.knime.database.port.DBDataPortObjectSpec;
import org.knime.database.port.DBSessionPortObject;
import org.knime.database.port.DBSessionPortObjectSpec;

/**
 * {@link DBNodeModel} implementation for the DB Remove SQL node
 * 
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBRemoveSQLNodeModel extends DBNodeModel {

	/**
	 * Constructor
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	DBRemoveSQLNodeModel() {
		super(new PortType[] { DBDataPortObject.TYPE },
				new PortType[] { DBSessionPortObject.TYPE });
	}

	@Override
	protected PortObject[] execute(PortObject[] inObjects,
			ExecutionContext exec) throws Exception {

		final DBDataPortObject inDbDataPort = (DBDataPortObject) inObjects[0];

		return new PortObject[] { new DBSessionPortObject(
				inDbDataPort.getSessionSummary().orElseThrow(),
				inDbDataPort.getKnimeToExternalTypeMapping(),
				inDbDataPort.getExternalToKnimeTypeMapping()) };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {

		DBDataPortObjectSpec inSpec =
				PortObjectSpecHelper.asDBDataPortObjectSpec(inSpecs[0], true);

		return new PortObjectSpec[] { new DBSessionPortObjectSpec(
				inSpec.getSessionSummary()
						.orElseThrow(() -> new InvalidSettingsException(
								"No DB Session Summary available!")),
				inSpec.getKnimeToExternalTypeMapping(),
				inSpec.getExternalToKnimeTypeMapping()) };
	}

}
