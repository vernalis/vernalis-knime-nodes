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
/**
 * 
 */
package com.vernalis.knime.testing.nodes.db;

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

import static java.util.Objects.requireNonNull;

/**
 * @author S Roughley
 *
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBPortComparatorNodeMigrationRule extends NodeMigrationRule {

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
		private final boolean hasGoldenIncomingData;
		private final boolean hasTestIncomingData;

		/**
		 * @param mn
		 *            the matched node (non-null)
		 * @param nma
		 *            the node migration action (can be null, which would
		 *            indicate that the node did not match and so is not
		 *            replaced by this rule
		 * @param hasGoldenIncomingData
		 *            whether there was an incoming connection which contained
		 *            'data' (i.e. an SQL query) at the 'golden' port
		 * @param hasTestIncomingData
		 *            whether there was an incoming connection which contained
		 *            'data' (i.e. an SQL query) at the 'test' port
		 * 
		 * @since 07-Sep-2022
		 * @since v1.36.0
		 */
		KnowsDataStateMigrationNodeMatchResult(MigrationNode mn,
				NodeMigrationAction nma, boolean hasGoldenIncomingData,
				boolean hasTestIncomingData) {
			this.nma = nma;
			this.mn = requireNonNull(mn, "MigrationNode");
			this.hasGoldenIncomingData = hasGoldenIncomingData;
			this.hasTestIncomingData = hasTestIncomingData;
		}

		@Override
		public Map<MigrationNode, NodeMigrationAction> getNodeActions() {
			return nma == null ? Collections.emptyMap()
					: Collections.singletonMap(mn, nma);
		}

		/**
		 * @return whether the matched node has an incoming data-containing
		 *         connection at the 'golden' port
		 *
		 * @since 07-Sep-2022
		 * @since v1.36.0
		 */
		public final boolean hasGoldenIncomingData() {
			return hasGoldenIncomingData;
		}

		/**
		 * @return whether the matched node has an incoming data-containing
		 *         connection at the 'test' port
		 *
		 * @since 07-Sep-2022
		 * @since v1.36.0
		 */
		public final boolean hasTestIncomingData() {
			return hasTestIncomingData;
		}

	}

	@Override
	protected Class<? extends NodeFactory<?>> getReplacementNodeFactoryClass(
			MigrationNode migrationNode, MigrationNodeMatchResult matchResult) {
		Class<? extends NodeFactory<?>> retVal;
		if (matchResult instanceof KnowsDataStateMigrationNodeMatchResult) {
			// We have an incoming port, so if we have data in the port then we
			// need to replace with the deprecated-from-new version
			KnowsDataStateMigrationNodeMatchResult mnmr =
					(KnowsDataStateMigrationNodeMatchResult) matchResult;
			retVal = mnmr.hasGoldenIncomingData() && mnmr.hasTestIncomingData
					? DBDataPortComparatorNodeFactory.class
					: DBPortComparatorNodeFactory.class;
		} else {
			// We should be here, but if we are, we use the configurable version
			retVal = DBPortComparatorNodeFactory.class;
		}
		return retVal;
	}

	@Override
	protected MigrationNodeMatchResult match(MigrationNode migrationNode) {
		if ("com.vernalis.knime.testing.nodes.database.DatabasePortComparatorNodeFactory"
				.equals(migrationNode.getOriginalNodeFactoryClassName())) {
			// We need to check whether other end of any incoming
			// connection has a query ("data")
			boolean hasTestIncomingData = migrationNode.getOriginalInputPorts()
					.get(1).getConnections().stream()
					.anyMatch(c -> c.getSourcePort()
							.getType() == DatabasePortObject.TYPE);
			boolean hasGoldenIncomingData = migrationNode
					.getOriginalInputPorts().get(2).getConnections().stream()
					.anyMatch(c -> c.getSourcePort()
							.getType() == DatabasePortObject.TYPE);

			return new KnowsDataStateMigrationNodeMatchResult(migrationNode,
					NodeMigrationAction.REPLACE, hasGoldenIncomingData,
					hasTestIncomingData);
		} else {
			return MigrationNodeMatchResult.of(migrationNode, null);
		}
	}

	@Override
	protected void migrate(MigrationNode migrationNode,
			MigrationNodeMatchResult matchResult) throws MigrationException {
		if (matchResult instanceof KnowsDataStateMigrationNodeMatchResult) {
			KnowsDataStateMigrationNodeMatchResult mnmr =
					(KnowsDataStateMigrationNodeMatchResult) matchResult;
			if (mnmr.hasGoldenIncomingData() != mnmr.hasTestIncomingData()) {
				throw new MigrationException(
						"Unable to migrate nodes with mixed input port types");
			}
		}

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
