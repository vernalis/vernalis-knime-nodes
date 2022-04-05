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
package com.vernalis.knime.ui.actions.delegates;

import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.actions.AbstractNodeAction;
import org.knime.workbench.editor2.actions.delegates.AbstractEditorAction;

import com.vernalis.knime.ui.actions.SelectConnectingNodesAction;

/**
 * AbstractEditorAction implementation to provide the Select All Connecting
 * Nodes action
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class SelectConnectingNodesEditorAction extends AbstractEditorAction {

	@Override
	protected AbstractNodeAction createAction(WorkflowEditor editor) {
		return new SelectConnectingNodesAction(editor);
	}

}
