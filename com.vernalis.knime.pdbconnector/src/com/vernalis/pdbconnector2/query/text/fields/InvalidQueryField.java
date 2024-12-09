/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query.text.fields;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModel;

import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldOperator;

/**
 * A {@link QueryField} representing an invalid query - most likely one removed
 * from the remote API
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.30.3
 */
public class InvalidQueryField extends QueryField {

	private final NodeSettingsRO brokenSettings;

	/**
	 * Constructor
	 * 
	 * @param brokenSettings
	 *            The settings model with the saved state
	 */
	public InvalidQueryField(NodeSettingsRO brokenSettings) {
		super(null, null, null, null, false, null, -1, null, null, null);
		this.brokenSettings = brokenSettings;
	}

	@Override
	public DialogComponent getDialogComponent(QueryFieldOperator queryOperator,
			SettingsModel model) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SettingsModel
			getValueSettingsModel(QueryFieldOperator queryOperator) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object getFieldValue(QueryFieldModel queryFieldModel) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected QueryField createCloneWithSubquery(String optionName,
			QueryFieldDropdown ddqf) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return The settings object containing the saved state
	 */
	public NodeSettingsRO getBrokenSettings() {
		return brokenSettings;
	}

}
