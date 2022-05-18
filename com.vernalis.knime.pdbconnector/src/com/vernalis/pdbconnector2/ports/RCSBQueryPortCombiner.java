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
package com.vernalis.pdbconnector2.ports;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.flowcontrol.portcombiner.api.PortTypeCombiner;
import com.vernalis.knime.flowcontrol.portcombiner.api.Warnable;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupConjunction;

/**
 * {@link PortTypeCombiner} for {@link RCSBQueryPortObject}s
 * 
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.32.0
 */
public class RCSBQueryPortCombiner implements PortTypeCombiner {

	private static final String CONJUNCTION = "Conjunction";

	/**
	 * Constructor
	 *
	 * @since v1.32.0
	 */
	public RCSBQueryPortCombiner() {
		//
	}

	@Override
	public Map<String, SettingsModel> getCombinerModels() {
		return Collections.singletonMap(CONJUNCTION,
				new SettingsModelString(CONJUNCTION,
						QueryGroupConjunction.getDefault().getActionCommand()));
	}

	@Override
	public void createDialog(DefaultNodeSettingsPane dialog,
			Map<String, SettingsModel> models) {
		dialog.addDialogComponent(new DialogComponentButtonGroup(
				(SettingsModelString) models.get(CONJUNCTION), CONJUNCTION,
				false, QueryGroupConjunction.values()));

	}

	@Override
	public boolean hasDialogOptions() {
		return true;
	}

	@Override
	public PortObjectSpec createOutputPortObjectSpec(
			List<? extends PortObjectSpec> activePorts,
			Map<String, SettingsModel> models, Warnable warnable)
			throws InvalidSettingsException {
		if (activePorts.size() == 1) {
			return activePorts.get(0);
		}
		final MultiRCSBQueryModel model = new MultiRCSBQueryModel();
		final String conjStr = ((SettingsModelString) models.get(CONJUNCTION))
				.getStringValue();
		try {

			model.setConjunction(QueryGroupConjunction.fromText(conjStr));
		} catch (NullPointerException | IllegalArgumentException e) {
			throw new InvalidSettingsException(
					"'" + conjStr + "' is not a valid conjunction", e);
		}
		activePorts.stream().filter(x -> x instanceof MultiRCSBQueryModel)
				.map(x -> MultiRCSBQueryModel.class.cast(x))
				.filter(x -> x.hasQuery()).forEach(x -> model.addModel(x));
		if (!model.hasQuery()) {
			throw new InvalidSettingsException(
					"The incoming ports do not contain a query!");
		}
		return model;
	}

	@Override
	public PortObject createOutputPortObject(int[] activePortIndices,
			ExecutionContext exec, Map<String, SettingsModel> models,
			PortObject[] inPorts) throws InvalidSettingsException,
			CanceledExecutionException, Exception {
		if (activePortIndices.length == 1) {
			return inPorts[activePortIndices[0]];
		}
		final MultiRCSBQueryModel model = new MultiRCSBQueryModel();
		final String conjStr = ((SettingsModelString) models.get(CONJUNCTION))
				.getStringValue();
		try {
			model.setConjunction(QueryGroupConjunction.fromText(conjStr));
		} catch (NullPointerException | IllegalArgumentException e) {
			throw new InvalidSettingsException(
					"'" + conjStr + "' is not a valid conjunction", e);
		}
		Arrays.stream(activePortIndices).mapToObj(i -> inPorts[i])
				.filter(x -> x instanceof RCSBQueryPortObject)
				.map(x -> RCSBQueryPortObject.class.cast(x))
				.map(x -> x.getModel()).filter(x -> x.hasQuery())
				.forEach(x -> model.addModel(x));
		return new RCSBQueryPortObject(model);
	}

}
