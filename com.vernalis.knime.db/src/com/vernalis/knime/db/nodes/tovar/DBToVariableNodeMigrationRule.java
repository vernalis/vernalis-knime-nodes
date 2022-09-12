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
package com.vernalis.knime.db.nodes.tovar;

import java.util.Collections;
import java.util.Map;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.port.database.DatabasePortObject;
import org.knime.workflow.migration.MigrationException;
import org.knime.workflow.migration.MigrationNodeMatchResult;
import org.knime.workflow.migration.NodeMigrationAction;
import org.knime.workflow.migration.NodeMigrationRule;
import org.knime.workflow.migration.NodeSettingsMigrationManager;
import org.knime.workflow.migration.model.MigrationNode;
import org.knime.workflow.migration.model.MigrationNodePort;

import static java.util.Objects.requireNonNull;

/**
 * {@link NodeMigrationRule} to map the Database to Variable node to either the
 * new DB to Variable node (which is configurable), or, if the node has an
 * incoming connection which contains data to a new (but deprecated) DB Data to
 * Variable node
 * 
 * @author S Roughley
 *
 */
public class DBToVariableNodeMigrationRule extends NodeMigrationRule {

	/**
	 * A {@link MigrationNodeMatchResult} implementation which tracks the
	 * presence of an incoming data-containing connection
	 * 
	 * @author S Roughley
	 *
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	private static class KnowsDataStateMigrationNodeMatchResult
			implements MigrationNodeMatchResult {

		private final NodeMigrationAction nma;
		private final MigrationNode mn;
		private final boolean hasData;

		/**
		 * @param mn
		 *            the matched node (non-null)
		 * @param nma
		 *            the node migration action (can be null, which would
		 *            indicate that the node did not match and so is not
		 *            replaced by this rule
		 * @param hasData
		 *            whether there was an incoming connection which contained
		 *            'data' (i.e. an SQL query)
		 * 
		 * @since 07-Sep-2022
		 * @since v1.36.0
		 */
		KnowsDataStateMigrationNodeMatchResult(MigrationNode mn,
				NodeMigrationAction nma, boolean hasData) {
			this.nma = nma;
			this.mn = requireNonNull(mn, "MigrationNode");
			this.hasData = hasData;
		}

		@Override
		public Map<MigrationNode, NodeMigrationAction> getNodeActions() {
			return nma == null ? Collections.emptyMap()
					: Collections.singletonMap(mn, nma);
		}

		/**
		 * @return whether the matched node has an incoming data-containing
		 *         connection
		 *
		 * @since 07-Sep-2022
		 * @since v1.36.0
		 */
		public final boolean hasData() {
			return hasData;
		}

	}

	@Override
	protected Class<? extends NodeFactory<?>> getReplacementNodeFactoryClass(
			MigrationNode arg0, MigrationNodeMatchResult arg1) {
		Class<? extends NodeFactory<?>> retVal;
		if (arg1 instanceof KnowsDataStateMigrationNodeMatchResult) {
			// We have an incoming port, so if we have data in the port then we
			// need to replace with the deprecated-from-new version
			KnowsDataStateMigrationNodeMatchResult mnmr =
					(KnowsDataStateMigrationNodeMatchResult) arg1;
			retVal = mnmr.hasData() ? DBDataToVariableNodeFactory.class
					: DBToVariableNodeFactory.class;
		} else {
			// We should be here, but if we are, we use the configurable version
			retVal = DBToVariableNodeFactory.class;
		}
		return retVal;
	}

	@Override
	protected MigrationNodeMatchResult match(MigrationNode migrationNode) {
		if ("com.vernalis.knime.database.nodes.tovar.DatabaseToVariableNodeFactory"
				.equals(migrationNode.getOriginalNodeFactoryClassName())) {
			final MigrationNodePort dbInputPort =
					migrationNode.getOriginalInputPorts().get(1);
			// We need to check whether other end of an incoming
			// connection has a query ("data")
			boolean hasIncomingData = dbInputPort.getConnections().stream()
					.anyMatch(c -> c.getSourcePort()
							.getType() == DatabasePortObject.TYPE);
			return new KnowsDataStateMigrationNodeMatchResult(migrationNode,
					NodeMigrationAction.REPLACE, hasIncomingData);
		} else {
			return MigrationNodeMatchResult.of(migrationNode, null);
		}

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
