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
package com.vernalis.knime.db;

import org.knime.core.node.NodeFactory;
import org.knime.workflow.migration.MigrationException;
import org.knime.workflow.migration.MigrationNodeMatchResult;
import org.knime.workflow.migration.NodeMigrationAction;
import org.knime.workflow.migration.NodeMigrationRule;
import org.knime.workflow.migration.NodeSettingsMigrationManager;
import org.knime.workflow.migration.model.MigrationNode;

import static java.util.Objects.requireNonNull;

/**
 * An abstract {@link NodeMigrationRule} to map all ports and settings from a
 * legacy node to a new node
 * 
 * @author S Roughley
 *
 */
public abstract class SimpleNodeMigrationRule extends NodeMigrationRule {

	private final Class<? extends NodeFactory<?>> replacementClass;
	private final String legacyClassName;

	/**
	 * Constructor
	 * 
	 * @param replacementClass
	 *            the new class to replace a matching node with
	 * @param legacyClassName
	 *            the full legacy classname
	 * 
	 * @throws NullPointerException
	 *             if either of the arguments are {@code null}
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	protected SimpleNodeMigrationRule(
			Class<? extends NodeFactory<?>> replacementClass,
			String legacyClassName) throws NullPointerException {
		this.replacementClass =
				requireNonNull(replacementClass, "replacementClass");
		this.legacyClassName =
				requireNonNull(legacyClassName, "legacyClassName");
	}

	@Override
	protected Class<? extends NodeFactory<?>> getReplacementNodeFactoryClass(
			MigrationNode arg0, MigrationNodeMatchResult arg1) {
		return replacementClass;
	}

	@Override
	protected MigrationNodeMatchResult match(MigrationNode migrationNode) {
		return MigrationNodeMatchResult.of(migrationNode,
				legacyClassName
						.equals(migrationNode.getOriginalNodeFactoryClassName())
								? NodeMigrationAction.REPLACE
								: null);
	}

	@Override
	protected void migrate(MigrationNode migrationNode,
			MigrationNodeMatchResult arg1) throws MigrationException {
		// Ports
		associateEveryOriginalPortWithNew(migrationNode);

		// Settings
		final NodeSettingsMigrationManager settingsManager =
				createSettingsManager(migrationNode);

		// Model and variable settings
		settingsManager.copyAllModelAndOptionalVariableSettings().toIdentical();

		// Miscellaneous settings
		settingsManager.copyAllMiscellaneousSettings().toIdentical();
	}

}
