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
package com.vernalis.knime.flowcontrol.flowvarcond.compwrapper;

import java.text.ParseException;
import java.util.EventListener;
import java.util.function.Consumer;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * Component to wrap a Number Spinner
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class ComponentWrapperNumberSpinner
		extends ComponentWrapper<JSpinner, Number, ChangeEvent> {

	/**
	 * Overloaded constructor. The component is not an option
	 * 
	 * @param c
	 *            a {@link JSpinner} to wrap
	 * @param id
	 *            the component ID
	 */
	public ComponentWrapperNumberSpinner(JSpinner c, String id) {
		this(c, id, false);
	}

	/**
	 * Constructor
	 * 
	 * @param c
	 *            a {@link JSpinner} to wrap
	 * @param id
	 *            the component ID
	 * @param isOption
	 *            whether the component is an option
	 */
	public ComponentWrapperNumberSpinner(JSpinner c, String id,
			boolean isOption) {
		super(c, id, isOption);
	}

	@Override
	public void setValue(Number value) {
		Number oldValue = (Number) getComponent().getValue();
		try {
			getComponent().commitEdit();
			getComponent().setValue(value);
		} catch (final ParseException e) {
			// spinner contains invalid value - update component!
			getComponent().setValue(oldValue);
		}
	}

	@Override
	public Number getValue() {
		return (Number) getComponent().getValue();
	}

	@Override
	public void saveToSettings(NodeSettingsWO settings)
			throws InvalidSettingsException {
		Number n = getValue();
		if (n instanceof Double) {
			settings.addDouble(getID(), n.doubleValue());
		} else if (n instanceof Integer) {
			settings.addInt(getID(), n.intValue());
		} else if (n instanceof Long) {
			settings.addLong(getID(), n.longValue());
		} else {
			throw new InvalidSettingsException(
					"Unsupported number type - " + n.getClass().getName());
		}

	}

	@Override
	public void loadFromSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {

		try {
			setValue(settings.getDouble(getID()));
		} catch (InvalidSettingsException e) {
			try {
				setValue(settings.getInt(getID()));
			} catch (InvalidSettingsException e0) {
				// If this fails, we throw
				setValue(settings.getLong(getID()));
			}
		}

	}

	@Override
	public ComponentWrapperNumberSpinner createClone() {
		SpinnerNumberModel mdl = (SpinnerNumberModel) getComponent().getModel();
		SpinnerNumberModel newMdl = new SpinnerNumberModel(mdl.getNumber(),
				mdl.getMinimum(), mdl.getMaximum(), mdl.getStepSize());
		return new ComponentWrapperNumberSpinner(new JSpinner(newMdl), getID());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.flowcontrol.nodes.abstrct.varvalifswitch.
	 * ComponentWrapper#equalsImpl(com.vernalis.knime.internal.flowcontrol.nodes
	 * .abstrct.varvalifswitch.ComponentWrapper)
	 */
	@Override
	protected boolean equalsImpl(ComponentWrapper<?, ?, ?> other,
			boolean ignoreValue) {

		SpinnerNumberModel mdl = (SpinnerNumberModel) getComponent().getModel();
		SpinnerNumberModel otherMdl =
				(SpinnerNumberModel) ((ComponentWrapperNumberSpinner) other)
						.getComponent().getModel();

		if (mdl.getMinimum() == null) {
			if (otherMdl.getMinimum() != null) {
				return false;
			}
		} else if (!mdl.getMinimum().equals(otherMdl.getMinimum())) {
			return false;
		}

		if (mdl.getMaximum() == null) {
			if (otherMdl.getMaximum() != null) {
				return false;
			}
		} else if (!mdl.getMaximum().equals(otherMdl.getMaximum())) {
			return false;
		}

		if (mdl.getStepSize() == null) {
			if (otherMdl.getStepSize() != null) {
				return false;
			}
		} else if (!mdl.getStepSize().equals(otherMdl.getStepSize())) {
			return false;
		}
		return true;
	}

	@Override
	public EventListener registerValueChangeComponentListener(
			Consumer<? super ChangeEvent> action) {
		ChangeListener l = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				action.accept(e);
			}
		};
		getComponent().addChangeListener(l);
		return l;
	}

	@Override
	public EventListener registerValueChangeComponentListener(Runnable action) {
		ChangeListener l = new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				action.run();
			}
		};
		getComponent().addChangeListener(l);
		return l;
	}

}
