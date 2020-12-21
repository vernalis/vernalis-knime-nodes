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
package com.vernalis.pdbconnector2.ports;

import java.io.IOException;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import com.vernalis.pdbconnector2.query.RCSBQueryModel;

/**
 * {@link AbstractPortObject} implementation wrapping an RCSB Advanced query
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class RCSBQueryPortObject extends AbstractPortObject {

	/**
	 * @noreference This class is not intended to be referenced by clients.
	 */
	public static final class Serializer
			extends AbstractPortObjectSerializer<RCSBQueryPortObject> {
	}

	/**
	 * Convenience object to access the {@link PortType}, equivalent to calling
	 * {@code PortTypeRegistry.getInstance().getPortType(RCSBQueryPortObject.class)}
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance()
			.getPortType(RCSBQueryPortObject.class);
	/**
	 * Convenience object to access the {@link PortType} for an optional port,
	 * equivalent to calling
	 * {@code PortTypeRegistry.getInstance().getPortType(RCSBQueryPortObject.class, true)}
	 */
	public static final PortType TYPE_OPTIONAL = PortTypeRegistry.getInstance()
			.getPortType(RCSBQueryPortObject.class, true);

	private MultiRCSBQueryModel model;

	/**
	 * Constructor creating a new port with no query defined
	 */
	public RCSBQueryPortObject() {
		this(new MultiRCSBQueryModel());
	}

	/**
	 * Constructor to define a new port with predefined spec
	 * 
	 * @param model
	 *            The model (= port spec)
	 */
	public RCSBQueryPortObject(MultiRCSBQueryModel model) {
		this.model = model;
	}

	/**
	 * Constructor to define a new port with a single {@link RCSBQueryModel}
	 * 
	 * @param model
	 *            The query model
	 */
	public RCSBQueryPortObject(RCSBQueryModel model) {
		this();
		getSpec().addModel(model);
	}

	@Override
	public String getSummary() {
		return "RCSB Advanced Query";
	}

	@Override
	public MultiRCSBQueryModel getSpec() {
		return model;
	}

	@Override
	public JComponent[] getViews() {
		return new JComponent[] {};
	}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

		// Nothing to do here
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec,
			ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		this.model = (MultiRCSBQueryModel) spec;
		// Nothing else to do here
	}

	/**
	 * @return The query model (= port spec)
	 */
	public MultiRCSBQueryModel getModel() {
		return model;
	}

}
