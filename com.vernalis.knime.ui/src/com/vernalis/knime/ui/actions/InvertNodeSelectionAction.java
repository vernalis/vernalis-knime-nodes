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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.knime.workbench.core.util.ImageRepository;
import org.knime.workbench.editor2.WorkflowEditor;
import org.knime.workbench.editor2.actions.AbstractNodeAction;
import org.knime.workbench.editor2.editparts.NodeContainerEditPart;

import com.vernalis.knime.ui.VernalisUIPluginActivator;

import static com.vernalis.knime.ui.actions.NodeActionHelpers.castList;

/**
 * AbstractNodeAction implementation to Invert the current node selection
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class InvertNodeSelectionAction extends AbstractNodeAction {

	/**
	 * Constructor
	 * 
	 * @param editor
	 *            The current active {@link WorkflowEditor}
	 */
	public InvertNodeSelectionAction(WorkflowEditor editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getText()
	 */
	@Override
	public String getText() {
		return "Invert Node Selection";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return "Invert the node selection";
	}

	@Override
	public String getId() {
		return this.getClass().getCanonicalName();
	}

	@Override
	public void runOnNodes(NodeContainerEditPart[] nodeParts) {
		List<NodeContainerEditPart> nodes = new ArrayList<>();
		Collections.addAll(nodes, getAllParts(NodeContainerEditPart.class));
		List<NodeContainerEditPart> selectedNodes = Arrays.asList(nodeParts);
		nodes.removeAll(selectedNodes);
		if (selectedNodes != null) {
			if (nodes.isEmpty()) {
				// All nodes must be selected, so we need to try using one of
				// them to get the viewer..
				for (NodeContainerEditPart tgt : nodeParts) {
					EditPartViewer viewer = tgt.getViewer();
					StructuredSelection sel =
							(StructuredSelection) viewer.getSelection();
					if (sel.isEmpty()) {
						// we are done...
						break;
					}
					sel = StructuredSelection.EMPTY;
					viewer.setSelection(sel);
				}
			} else {
				for (NodeContainerEditPart tgt : nodes) {
					final EditPartViewer viewer = tgt.getViewer();
					StructuredSelection sel =
							(StructuredSelection) viewer.getSelection();
					List<EditPart> selected =
							castList(EditPart.class, sel.toList());
					if (selected.containsAll(nodes)
							&& Collections.disjoint(selectedNodes, nodes)) {
						// We are done
						break;
					}
					sel = new StructuredSelection(nodes);
					viewer.setSelection(sel);
				}
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
				"icons/invert_selection_disabled.png");
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
				"icons/invert_selection.png");
	}

	@Override
	protected boolean internalCalculateEnabled() {
		// As long as the workflow contains 1 or more nodes, we are selected...
		return getAllParts(NodeContainerEditPart.class).length > 0;
	}

}
