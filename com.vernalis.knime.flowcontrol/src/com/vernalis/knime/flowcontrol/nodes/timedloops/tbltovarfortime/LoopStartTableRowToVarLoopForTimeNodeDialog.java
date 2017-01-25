/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.flowcontrol.nodes.timedloops.tbltovarfortime;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart.AbstractTimedLoopStartNodeDialog;

/**
 * <code>NodeDialog</code> for the "LoopStartTableRowToVarLoopForTime" Node.
 * 
 * 
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author "Stephen Roughley  knime@vernalis.com"
 */
public class LoopStartTableRowToVarLoopForTimeNodeDialog extends
		AbstractTimedLoopStartNodeDialog {

	/**
	 * New pane for configuring the LoopStartTableRowToVarLoopForTime node.
	 */
	protected LoopStartTableRowToVarLoopForTimeNodeDialog() {
		super();
		addMissingVariablePanel();
		addRunForTimePanel();
		addZerothIterationCountPanel();
	}
}
