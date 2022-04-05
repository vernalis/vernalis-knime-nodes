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
package com.vernalis.knime.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.knime.workbench.editor2.editparts.ConnectionContainerEditPart;
import org.knime.workbench.editor2.editparts.NodeContainerEditPart;
import org.knime.workbench.editor2.editparts.NodeInPortEditPart;

/**
 * Utility class providing methods to walk node graph in a KNIME workflow
 * 
 * @author S.Roughley
 *
 */
public class WorkflowHelpers {

	private WorkflowHelpers() {
		// Utility Class - Do not Instantiate
		throw new UnsupportedOperationException();
	}

	/**
	 * Method to get all directly downstream nodes from a selected node
	 * 
	 * @param selectedNode
	 *            The selected node
	 * 
	 * @return A set of the directly downstream nodes
	 */
	public static Set<NodeContainerEditPart>
			getDownstreamConnectedNodes(NodeContainerEditPart selectedNode) {
		HashSet<NodeContainerEditPart> retVal = new HashSet<>();

		ConnectionContainerEditPart[] outConnections =
				selectedNode.getOutgoingConnections();

		for (ConnectionContainerEditPart outConnection : outConnections) {
			final EditPart target = outConnection.getTarget();
			if (!(target instanceof NodeInPortEditPart)) {
				continue;
			}

			final EditPart parent = target.getParent();
			if (!(parent instanceof NodeContainerEditPart)) {
				continue;
			}
			retVal.add((NodeContainerEditPart) parent);

		}
		return retVal;
	}

	/**
	 * Method to get the directly downstream nodes for a set of selected nodes.
	 * This is a wrapper for {@link #getDownstreamConnectedNodes(Iterable)}
	 * 
	 * @param selectedNodes
	 *            The current selected nodes
	 * 
	 * @return A set of the directly downstream nodes, not including the
	 *         incoming selection
	 */
	public static Set<NodeContainerEditPart>
			getDownstreamConnectedNodes(NodeContainerEditPart[] selectedNodes) {
		return getDownstreamConnectedNodes(Arrays.asList(selectedNodes));

	}

	/**
	 * Method to get the directly downstream nodes for a set of selected nodes.
	 * 
	 * @param selectedNodes
	 *            The current selected nodes
	 * 
	 * @return A set of the directly downstream nodes, not including the
	 *         incoming selection
	 */
	public static Set<NodeContainerEditPart> getDownstreamConnectedNodes(
			Iterable<NodeContainerEditPart> selectedNodes) {
		Set<NodeContainerEditPart> toSelect = new HashSet<>();
		for (NodeContainerEditPart selNode : selectedNodes) {
			toSelect.addAll(getDownstreamConnectedNodes(selNode));
		}
		// Now remove any nodes already selected
		for (NodeContainerEditPart selNode : selectedNodes) {
			toSelect.remove(selNode);
		}
		return toSelect;
	}

	/**
	 * Method to get all downstream nodes for a set of selected nodes.
	 * 
	 * @param selectedNodes
	 *            The current selected nodes
	 * 
	 * @return A set of all the downstream nodes, not including the incoming
	 *         selection
	 */
	public static Set<NodeContainerEditPart> getAllDownstreamConnectedNodes(
			NodeContainerEditPart[] selectedNodes) {
		Set<NodeContainerEditPart> retVal = new HashSet<>();
		Collection<NodeContainerEditPart> nextLayer =
				getDownstreamConnectedNodes(selectedNodes);
		while (retVal.addAll(nextLayer)) {
			nextLayer = getDownstreamConnectedNodes(nextLayer);
		}
		return retVal;
	}

	/**
	 * Method to get all directly upstream nodes from a selected node
	 * 
	 * @param selectedNode
	 *            The selected node
	 * 
	 * @return A set of the directly upstream nodes
	 */
	public static Set<NodeContainerEditPart>
			getUpstreamConnectedNodes(NodeContainerEditPart selectedNode) {
		HashSet<NodeContainerEditPart> retVal = new HashSet<>();
		ConnectionContainerEditPart[] inConnections =
				selectedNode.getIncomingConnections();
		for (ConnectionContainerEditPart inConnection : inConnections) {
			EditPart connSource = inConnection.getSource();
			if (connSource == null) {
				continue;
			}
			final EditPart parent = connSource.getParent();
			if (!(parent instanceof NodeContainerEditPart)) {
				continue;
			}
			retVal.add((NodeContainerEditPart) parent);
		}
		return retVal;
	}

	/**
	 * Method to get the directly upstream nodes for a set of selected nodes.
	 * This is a wrapper for {@link #getUpstreamConnectedNodes(Iterable)}
	 * 
	 * @param selectedNodes
	 *            The current selected nodes
	 * 
	 * @return A set of the directly upstream nodes, not including the incoming
	 *         selection
	 */
	public static Set<NodeContainerEditPart>
			getUpstreamConnectedNodes(NodeContainerEditPart[] selectedNodes) {
		return getUpstreamConnectedNodes(Arrays.asList(selectedNodes));
	}

	/**
	 * Method to get the directly upstream nodes for a set of selected nodes.
	 * 
	 * @param selectedNodes
	 *            The current selected nodes
	 * 
	 * @return A set of the directly upstream nodes, not including the incoming
	 *         selection
	 */
	public static Set<NodeContainerEditPart> getUpstreamConnectedNodes(
			Iterable<NodeContainerEditPart> selectedNodes) {
		Set<NodeContainerEditPart> toSelect = new HashSet<>();
		for (NodeContainerEditPart selNode : selectedNodes) {
			toSelect.addAll(getUpstreamConnectedNodes(selNode));
		}
		// Now remove any nodes already selected
		for (NodeContainerEditPart selNode : selectedNodes) {
			toSelect.remove(selNode);
		}
		return toSelect;
	}

	/**
	 * Method to get all upstream nodes for a set of selected nodes.
	 * 
	 * @param selectedNodes
	 *            The current selected nodes
	 * 
	 * @return A set of all the upstream nodes, not including the incoming
	 *         selection
	 */
	public static Set<NodeContainerEditPart> getAllUpstreamConnectedNodes(
			NodeContainerEditPart[] selectedNodes) {
		Set<NodeContainerEditPart> retVal = new HashSet<>();
		Collection<NodeContainerEditPart> nextLayer =
				getUpstreamConnectedNodes(selectedNodes);
		while (retVal.addAll(nextLayer)) {
			nextLayer = getUpstreamConnectedNodes(nextLayer);
		}
		return retVal;
	}

	/**
	 * Method to test whether two nodes are connected to each other (directly or
	 * indirectly) within a workflow
	 * 
	 * @param node0
	 *            The first node
	 * @param node1
	 *            The second node
	 * 
	 * @return {@code true} if the nodes are connected (i.e. it is possible to
	 *         walk along the workflow node graph from one to the other)
	 */
	public static boolean areNodesConnected(NodeContainerEditPart node0,
			NodeContainerEditPart node1) {
		// First See if node1 comes before node0
		Collection<NodeContainerEditPart> nextLayer =
				getUpstreamConnectedNodes(node0);
		while (!nextLayer.isEmpty()) {
			if (nextLayer.contains(node1)) {
				return true;
			}
			nextLayer = getUpstreamConnectedNodes(nextLayer);
		}

		// Now look in the opposite direction
		nextLayer = getDownstreamConnectedNodes(node0);
		while (!nextLayer.isEmpty()) {
			if (nextLayer.contains(node1)) {
				return true;
			}
			nextLayer = getDownstreamConnectedNodes(nextLayer);
		}

		// Didnt find... therefore not connected..
		return false;
	}

	/**
	 * Method to test if all nodes in a set are connected to each other
	 * (directly or indirectly)
	 * 
	 * @param nodes
	 *            the nodes
	 * 
	 * @return {@code true} if all the nodes are interconnected
	 */
	public static boolean areAllNodesConnected(NodeContainerEditPart[] nodes) {
		boolean areAllConnected = true;
		for (int i = 0; areAllConnected && i < nodes.length - 1; i++) {
			for (int j = i + 1; areAllConnected && j < nodes.length; j++) {
				areAllConnected &= areNodesConnected(nodes[i], nodes[j]);
			}
		}
		return areAllConnected;
	}

	/**
	 * Method to get all nodes connecting any pair of nodes in a set
	 * 
	 * @param nodes
	 *            the selected nodes
	 * 
	 * @return A set of all nodes connecting but not including the set of
	 *         supplied nodes
	 */
	public static Set<NodeContainerEditPart>
			getAllConnectingNodes(NodeContainerEditPart[] nodes) {

		// We dont require every node to be connected to every other... we dont
		// bother checking as graph-walking is then duplicated
		Set<NodeContainerEditPart> retVal = new HashSet<>();
		for (int i = 0; i < nodes.length - 1; i++) {
			for (int j = i + 1; j < nodes.length; j++) {
				// Seems that the collection can contain the same node twice..
				if (!isAconnectedToB(nodes[i], nodes[j], retVal)) {
					// Check the opposite way round (cannot be both ways!)
					isAconnectedToB(nodes[j], nodes[i], retVal);
				}
			}
		}
		retVal.removeAll(Arrays.asList(nodes));
		return retVal;
	}

	/**
	 * Method to check if node A is connected to a downstream node, B. It is if
	 * any immediate successor is (i.e. recursive), or an immediate successor is
	 * B. All successors are always checked to catch all routes from A->B. If
	 * {@code true}, then a is added to the path set argument
	 * 
	 * @param a
	 *            the first node
	 * @param b
	 *            the second, downstream node
	 * @param path
	 *            A set to which all nodes intermediate between A and B will be
	 *            added. Will include A but not B.
	 * 
	 * @return true if A is somewhere upstream of B
	 */
	static boolean isAconnectedToB(NodeContainerEditPart a,
			NodeContainerEditPart b, Set<NodeContainerEditPart> path) {
		assert a != b;
		boolean retVal = false;
		Set<NodeContainerEditPart> downStream = getDownstreamConnectedNodes(a);
		for (NodeContainerEditPart ds : downStream) {
			if (ds == b) {
				retVal = true;
			} else {
				retVal |= isAconnectedToB(ds, b, path);
			}
		}
		if (retVal) {
			path.add(a);
		}
		return retVal;
	}

}
