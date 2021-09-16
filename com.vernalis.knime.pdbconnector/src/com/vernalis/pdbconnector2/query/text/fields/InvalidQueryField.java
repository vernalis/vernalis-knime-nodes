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
