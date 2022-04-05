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
package com.vernalis.knime.ui.actions;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.knime.workbench.core.util.ImageRepository;
import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.actions.AbstractNodeAction;
import org.knime.workbench.editor2.editparts.NodeContainerEditPart;

import com.vernalis.knime.ui.VernalisUIPluginActivator;

/**
 * AbstractNodeAction implementation to deselect all the nodes in a workflow
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class DeselectAllNodesAction extends AbstractNodeAction {

	/**
	 * Constructor
	 * 
	 * @param editor
	 *            The current active {@link WorkflowEditor}
	 */
	public DeselectAllNodesAction(WorkflowEditor editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getText()
	 */
	@Override
	public String getText() {
		return "Deselect all Nodes";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return "Deselect all Nodes";
	}

	@Override
	public String getId() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void runOnNodes(NodeContainerEditPart[] nodeParts) {
		for (NodeContainerEditPart tgt : nodeParts) {
			final EditPartViewer viewer = tgt.getViewer();
			StructuredSelection sel =
					(StructuredSelection) viewer.getSelection();
			if (sel.isEmpty()) {
				// We are done
				break;
			}
			sel = StructuredSelection.EMPTY;
			viewer.setSelection(sel);
		}
		try {
			// Give focus to the editor again. Otherwise the actions
			// (selection)
			// is not updated correctly.
			getWorkbenchPart().getSite().getPage().activate(getWorkbenchPart());
		} catch (Exception e) {
			// ignore
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getDisabledImageDescriptor()
	 */
	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return ImageRepository.getIconDescriptor(
				VernalisUIPluginActivator.PLUGIN_ID,
				"icons/clear_selection_disabled.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageRepository.getIconDescriptor(
				VernalisUIPluginActivator.PLUGIN_ID,
				"icons/clear_selection.png");
	}

	@Override
	protected boolean internalCalculateEnabled() {
		// As long as the workflow contains 1 or more selected nodes...
		return getSelectedParts(NodeContainerEditPart.class).length > 0;
	}

}
