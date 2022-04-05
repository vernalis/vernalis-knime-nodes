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
package com.vernalis.knime.ui.actions.window;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.actions.AbstractNodeAction;

/**
 * Abstract {@link IWorkbenchWindowActionDelegate} implementation to supply
 * {@link AbstractNodeAction}s for a {@link WorkflowEditor}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public abstract class AbstractNodeWindowAction
		implements IWorkbenchWindowActionDelegate {

	/** The current selection */
	protected ISelection selection;

	/** The window - will be a WorkflowEditor if the action is enabled */
	protected IWorkbenchWindow window;

	/**
	 * Constructor
	 */
	protected AbstractNodeWindowAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		if (window.getActivePage().getActivePart() instanceof WorkflowEditor) {
			AbstractNodeAction cmdAction = getAction(
					(WorkflowEditor) window.getActivePage().getActivePart());
			cmdAction.run();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		if (window.getActivePage().getActivePart() instanceof WorkflowEditor) {
			AbstractNodeAction cmdAction = getAction(
					(WorkflowEditor) window.getActivePage().getActivePart());
			action.setEnabled(cmdAction.isEnabled());
		} else {
			// We are not in the right editor...
			action.setEnabled(false);
		}
	}

	@Override
	public void dispose() {

	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	/**
	 * Get the Node Action implementation for the command
	 * 
	 * @param editor
	 *            The current active workflow editor
	 * 
	 * @return the {@link AbstractNodeAction} implementation for the command
	 */
	protected abstract AbstractNodeAction getAction(WorkflowEditor editor);

}
