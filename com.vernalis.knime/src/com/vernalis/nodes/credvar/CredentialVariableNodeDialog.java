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
package com.vernalis.nodes.credvar;

import java.util.Collection;
import java.util.Collections;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

/**
 * <code>NodeDialog</code> for the "CredentialVariable" Node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Steve <knime@vernalis.com>
 * @since 1.27.0
 */
public class CredentialVariableNodeDialog extends DefaultNodeSettingsPane {
	private final DialogComponentStringSelection credNameDiaC;
	private final SettingsModelString credNameMdl = createCredNameModel();

	/**
	 * New pane for configuring the CredentialVariable node.
	 */
	protected CredentialVariableNodeDialog() {

		credNameDiaC = new DialogComponentStringSelection(credNameMdl, "Credentials set name",
				getCredentialsNameNeverEmpty());
		addDialogComponent(credNameDiaC);

	}

	static SettingsModelString createCredNameModel() {
		return new SettingsModelString("CP Name", "");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane#
	 * loadAdditionalSettingsFrom(org.knime.core.node.NodeSettingsRO,
	 * org.knime.core.node.port.PortObjectSpec[])
	 */
	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		// super.loadAdditionalSettingsFrom(settings, specs);
		if (credNameDiaC != null) {
			credNameDiaC.replaceListItems(getCredentialsNameNeverEmpty(), null);
		}
	}

	/**
	 * @return
	 */
	protected Collection<String> getCredentialsNameNeverEmpty() {
		Collection<String> credentialsNames = getCredentialsNames();
		credNameMdl.setEnabled(true);
		if (credentialsNames == null || credentialsNames.isEmpty()) {
			credentialsNames = Collections.singleton("<-- No Credentials found -->");
			credNameMdl.setEnabled(false);
		}
		return credentialsNames;
	}

}
