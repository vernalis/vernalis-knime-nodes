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

import java.awt.Component;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.Consumer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * A simple wrapper for a {@link Component} which allows getting and setting of
 * the value via a common method call, along with load/save operations to / from
 * NodeSettings objects.
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 * @param <T>
 *            The type of the Component
 * @param <U>
 *            The type of value
 * @param <E>
 *            The type of EventObject which is triggered by a change to the
 *            wrapped component
 */
public abstract class ComponentWrapper<T extends Component, U, E extends EventObject> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getSimpleName());
		builder.append(" [ID=");
		builder.append(getID());
		builder.append(", Value=");
		builder.append(getValue());
		builder.append("]");
		return builder.toString();
	}

	private final T c;
	private final String id;
	private final boolean isOption;

	/**
	 * Overloaded constructor for a non-option component
	 * 
	 * @param c
	 *            the component
	 * @param id
	 *            the ID
	 */
	protected ComponentWrapper(T c, String id) {
		this(c, id, false);
	}

	/**
	 * Full constructor
	 * 
	 * @param c
	 *            the component
	 * @param id
	 *            the ID
	 * @param isOption
	 *            whether the component is an option ({@code true}) or a
	 *            reference value
	 */
	protected ComponentWrapper(T c, String id, boolean isOption) {
		this.c = Objects.requireNonNull(c);
		this.id = Objects.requireNonNull(id);
		this.isOption = isOption;
	}

	/**
	 * @return the wrapped component
	 */
	public T getComponent() {
		return c;
	}

	/**
	 * @return the ID
	 */
	public String getID() {
		return id;
	}

	/**
	 * Method to set the component value
	 * 
	 * @param value
	 *            the new value
	 */
	public abstract void setValue(U value);

	/**
	 * Method to set the enabled status of the component
	 * 
	 * @param enabled
	 *            the new status
	 */
	public void setEnabled(boolean enabled) {
		getComponent().setEnabled(enabled);
	}

	/**
	 * Method to register an event listener to the component based on a Consumer
	 * 
	 * @param action
	 *            a consumer function which consumes the event trigger
	 * 
	 * @return the {@link EventListener} which was registered
	 */
	public abstract EventListener
			registerValueChangeComponentListener(Consumer<? super E> action);

	/**
	 * Method to register an event listener to the component based on a Runnable
	 * 
	 * @param action
	 *            a runnable which acts on the event trigger
	 * 
	 * @return the {@link EventListener} which was registered
	 */
	public abstract EventListener
			registerValueChangeComponentListener(Runnable action);

	/**
	 * @return the value displayed by the component
	 */
	public abstract U getValue();

	/**
	 * @return whether the component is an option or a reference value
	 */
	public boolean isOption() {
		return isOption;
	}

	/**
	 * Method to save the value to a settings object
	 * 
	 * @param settings
	 *            the settings object
	 * 
	 * @throws InvalidSettingsException
	 *             if there is a problem storing the value
	 */
	public abstract void saveToSettings(NodeSettingsWO settings)
			throws InvalidSettingsException;

	/**
	 * Method to load the value from a settings object
	 * 
	 * @param settings
	 *            the settings object
	 * 
	 * @throws InvalidSettingsException
	 *             if there was an error loading the value
	 */
	public abstract void loadFromSettings(NodeSettingsRO settings)
			throws InvalidSettingsException;

	/**
	 * @return an exact copy of the current component including its state,
	 *         possible values etc
	 */
	public abstract ComponentWrapper<T, U, E> createClone();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c == null) ? 0 : c.getClass().hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ (getValue() == null ? 0 : getValue().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ComponentWrapper)) {
			return false;
		}

		ComponentWrapper<?, ?, ?> other = (ComponentWrapper<?, ?, ?>) obj;
		// Components have to be the same
		if (!isSameComponent(other)) {
			return false;
		}

		// Values have to be the same
		if (getValue() == null) {
			if (other.getValue() != null) {
				return false;
			}
		} else if (!getValue().equals(other.getValue())) {
			return false;
		}
		return equalsImpl(other, false);
	}

	/**
	 * Once the initial equals check has been done (the other object is also a
	 * {@link ComponentWrapper}, and it has the same components and the values
	 * are equal) this method allows any further equality checks. Called from
	 * {@link #equals(Object)} (with value {@code false} for {@code ignoreValue}
	 * parameter) and from {@link #isSameComponent(ComponentWrapper)} (with
	 * value {@code true} for {@code ignoreValue})
	 * 
	 * @param other
	 *            the other component to compare with
	 * @param ignoreValue
	 *            whether the value should be ignored
	 * 
	 * @return the result of the equality check
	 */
	protected boolean equalsImpl(ComponentWrapper<?, ?, ?> other,
			boolean ignoreValue) {
		return true;
	}

	/**
	 * A method to check whether the other component is the same - the value is
	 * ignored. The IDs and isOption fields must be the same, and the wrapped
	 * component classes must also be the same. If all those checks are passed,
	 * then the result of {@link #equalsImpl(ComponentWrapper, boolean)} (with
	 * value {@code false} for the boolean parameter) is returned
	 * 
	 * @param other
	 *            the other component
	 * 
	 * @return whether the two components are the same, ignoring the actual
	 *         values
	 * 
	 * @see ComponentWrapper#equalsImpl(ComponentWrapper, boolean)
	 */
	public final boolean isSameComponent(ComponentWrapper<?, ?, ?> other) {

		// IDs have to be the same
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}

		// They have to have the same isOption value
		if (isOption != other.isOption) {
			return false;
		}

		// Component has to be the same class
		if (c == null) {
			if (other.c != null) {
				return false;
			}
		} else if (!c.getClass().equals(other.c.getClass())) {
			return false;
		}
		return equalsImpl(other, true);
	}
}
