/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *  
 *******************************************************************************/
package com.vernalis.nodes.fingerprint.properties;

import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "SparseToDense" Node. Node to convert a
 * SparseBitVector Fingerprint to a DenseBitVector fingerprint
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author S. Roughley
 */
public class FpPropsNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * New pane for configuring the SparseToDense node.
	 */
	protected FpPropsNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				new SettingsModelString(FpPropsNodeModel.CFG_FPCOL, null),
				"Select the fingerprint column", 0, true, BitVectorValue.class));

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				FpPropsNodeModel.CFG_FPTYPE, true), "Fingerprint Type"));

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				FpPropsNodeModel.CFG_FPLENGTH, true), "Length (No. of Bits)"));

		addDialogComponent(new DialogComponentBoolean(new SettingsModelBoolean(
				FpPropsNodeModel.CFG_FPCARDINALITY, true),
				"Cardinality (No. of Set Bits)"));

	}
}
