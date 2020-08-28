/*******************************************************************************
 * Copyright (c) 2020 Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

public class AbstractPerfMonTimingEndNodeDialog extends DefaultNodeSettingsPane {

	static final String USE_LEGACY_DATE_TIME_FIELDS = "Use legacy Date/Time Fields";

	AbstractPerfMonTimingEndNodeDialog() {
		super();
		addDialogComponent(
				new DialogComponentBoolean(createUseLegacyDateTimeFieldsModel(), USE_LEGACY_DATE_TIME_FIELDS));
	}

	static SettingsModelBoolean createUseLegacyDateTimeFieldsModel() {
		return new SettingsModelBoolean(USE_LEGACY_DATE_TIME_FIELDS, false);
	}

}
