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

import java.util.NoSuchElementException;

import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.iterators.LinewiseStringIterator;

public class SettingsModelMultilineString extends SettingsModelString
		implements Iterable<String> {

	public SettingsModelMultilineString(String configName,
			String defaultValue) {
		super(configName, defaultValue);
	}

	public String getDeliminator() throws NoSuchElementException {
		final LinewiseStringIterator iter = iterator();
		iter.next();
		return iter.getLineBreak();
	}

	@Override
	public LinewiseStringIterator iterator() {
		return new LinewiseStringIterator(getStringValue());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.defaultnodesettings.SettingsModelString#
	 * getModelTypeID()
	 */
	@Override
	protected String getModelTypeID() {
		return super.getModelTypeID();
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

}
