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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.knime.workbench.core.util.ImageRepository;
import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.actions.AbstractNodeAction;
import org.knime.workbench.editor2.editparts.NodeContainerEditPart;

import com.vernalis.knime.ui.VernalisUIPluginActivator;

import static com.vernalis.knime.ui.WorkflowHelpers.getUpstreamConnectedNodes;

/**
 * AbstractNodeAction implementation to move the selection to the directly
 * connected previous upstream node(s) from the current selection
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class MovePreviousNodeAction extends AbstractNodeAction {

	/**
	 * Constructor
	 * 
	 * @param editor
	 *            The current active {@link WorkflowEditor}
	 */
	public MovePreviousNodeAction(WorkflowEditor editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getText()
	 */
	@Override
	public String getText() {
		return "Move to Previous Node(s)";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return "Move the selection to all nodes directly preceding any selected nodes";
	}

	@Override
	public String getId() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void runOnNodes(NodeContainerEditPart[] nodeParts) {
		Set<NodeContainerEditPart> toSelect =
				getUpstreamConnectedNodes(nodeParts);
		if (toSelect != null) {
			for (NodeContainerEditPart tgt : toSelect) {
				tgt.getViewer().setSelection(
						new StructuredSelection(new ArrayList<>(toSelect)));
			}
			try {
				// Give focus to the editor again. Otherwise the actions
				// (selection)
				// is not updated correctly.
				getWorkbenchPart().getSite().getPage()
						.activate(getWorkbenchPart());
			} catch (Exception e) {
				// ignore
			}
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
				"icons/move_prev_disabled.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageRepository.getIconDescriptor(
				VernalisUIPluginActivator.PLUGIN_ID, "icons/move_prev.png");
	}

	@Override
	protected boolean internalCalculateEnabled() {
		NodeContainerEditPart[] selected =
				getSelectedParts(NodeContainerEditPart.class);
		if (selected.length < 1) {
			return false;
		}
		for (NodeContainerEditPart nodeEditPart : selected) {
			if (nodeEditPart.getIncomingConnections().length > 0) {
				List<?> sel = nodeEditPart.getViewer().getSelectedEditParts();
				Set<NodeContainerEditPart> upstreamConnectedNodes =
						getUpstreamConnectedNodes(nodeEditPart);
				upstreamConnectedNodes.removeAll(sel);
				if (!upstreamConnectedNodes.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

}
