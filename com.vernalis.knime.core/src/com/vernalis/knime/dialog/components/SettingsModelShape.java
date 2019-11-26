/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import javax.swing.event.ChangeListener;

import org.knime.core.data.property.ShapeFactory;
import org.knime.core.data.property.ShapeFactory.Shape;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * A settings model to select a shape
 * 
 * @author S.Roughley
 *
 */
public class SettingsModelShape extends SettingsModelString {
	Shape defaultShape;

	/**
	 * Constructor
	 * 
	 * @param configName
	 *            The config name
	 * @param defaultShape
	 *            The default shape. The default shape is used if
	 *            setShapeValue(String) is called with an incorrect name
	 */
	public SettingsModelShape(String configName, Shape defaultShape) {
		super(configName, defaultShape.toString());
		this.defaultShape = defaultShape;
	}

	/**
	 * @return the shape stored by the model
	 */
	public Shape getShapeValue() {
		return ShapeFactory.getShape(super.getStringValue());
	}

	/**
	 * @param newValue
	 *            set the new shape value
	 */
	public void setShapeValue(Shape newValue) {
		super.setStringValue(newValue.toString());
	}

	/**
	 * @return the default shape value for the model.
	 */
	public Shape getDefaultShape() {
		return defaultShape;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * setStringValue(java.lang.String)
	 */
	@Override
	public void setStringValue(String newValue) {
		Shape shape = ShapeFactory.getShape(newValue);
		if (!shape.toString().equalsIgnoreCase(newValue)) {
			// Use the default
			shape = defaultShape;
		}
		super.setStringValue(shape.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * getModelTypeID()
	 */
	@Override
	protected String getModelTypeID() {
		return "SMID_Shape";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModel#
	 * prependChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	protected void prependChangeListener(ChangeListener l) {
		super.prependChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModel#
	 * notifyChangeListeners()
	 */
	@Override
	protected void notifyChangeListeners() {
		super.notifyChangeListeners();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.SettingsModelString#createClone()
	 */
	@Override
	protected SettingsModelString createClone() {
		return super.createClone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.knime.core.node.defaultnodesettings.SettingsModelString#getConfigName
	 * ()
	 */
	@Override
	protected String getConfigName() {
		return super.getConfigName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * loadSettingsForDialog(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		super.loadSettingsForDialog(settings, specs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * saveSettingsForDialog(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		super.saveSettingsForDialog(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * validateSettingsForModel(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings)
			throws InvalidSettingsException {
		super.validateSettingsForModel(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * loadSettingsForModel(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		super.loadSettingsForModel(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * saveSettingsForModel(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		super.saveSettingsForModel(settings);
	}

}
