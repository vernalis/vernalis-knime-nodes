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
package com.vernalis.knime.ui.actions.objct;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.actions.AbstractNodeAction;

/**
 * An abstract {@link IObjectActionDelegate} implementation to add actions to
 * contextual windows in the {@link WorkflowEditor}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public abstract class AbstractNodeObjectAction
		implements IObjectActionDelegate {

	/** The current selection */
	protected ISelection selection;
	/** The workbench part, which should be a {@link WorkflowEditor} */
	protected IWorkbenchPart workbenchPart;

	/**
	 * Constructor
	 */
	protected AbstractNodeObjectAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		if (workbenchPart instanceof WorkflowEditor) {
			AbstractNodeAction cmdAction =
					getAction((WorkflowEditor) workbenchPart);
			cmdAction.run();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		if (workbenchPart instanceof WorkflowEditor) {
			AbstractNodeAction cmdAction =
					getAction((WorkflowEditor) workbenchPart);
			action.setEnabled(cmdAction.isEnabled());
		} else {
			// We are not in the right editor...
			action.setEnabled(false);
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbenchPart = targetPart;
	}

	/**
	 * Get the Node Action implementation for the command
	 * 
	 * @param editor
	 *            The current active {@link WorkflowEditor}
	 * 
	 * @return The {@link AbstractNodeAction} corresponding to the current
	 *         action implementation
	 */
	protected abstract AbstractNodeAction getAction(WorkflowEditor editor);

}
