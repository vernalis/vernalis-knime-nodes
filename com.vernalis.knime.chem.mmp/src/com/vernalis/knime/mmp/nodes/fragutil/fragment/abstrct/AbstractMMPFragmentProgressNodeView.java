/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.knime.core.node.NodeView;

/**
 * Progress Node view to show progress of fragmentation, active threads and
 * completed queue size
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPFragmentProgressNodeView<T, U>
		extends NodeView<AbstractMMPFragmentNodeModel<T, U>> {
	JProgressBar inputTableProgress, completedQueue, activeThreads;
	JPanel masterPanel;

	/**
	 * Constructor
	 * 
	 * @param nodeModel
	 *            The node model instance for the view
	 */
	public AbstractMMPFragmentProgressNodeView(AbstractMMPFragmentNodeModel<T, U> nodeModel) {
		super(nodeModel);
		initialiseComponents();
		setShowNODATALabel(false);
	}

	@Override
	protected void onClose() {
		// nothing

	}

	@Override
	protected void onOpen() {
		initialiseComponents();
		if (getNodeModel().numRows != null) {
			setProgress();
		}
	}

	@Override
	protected void modelChanged() {
		// Prefer to call updateModel
		initialiseComponents();
		setProgress();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.AbstractNodeView#updateModel(java.lang.Object)
	 */
	@Override
	protected void updateModel(Object arg) {
		// if arg==true then we want to change bounds, otherwise we update
		// progress
		if ((boolean) arg) {
			initialiseComponents();
		} else {
			setProgress();
		}
	}

	private void setProgress() {
		// Progress on scale of 0 - 100
		if (getNodeModel().numRows != null) {
			inputTableProgress.setIndeterminate(false);
			double progressPercent = 100.0 * getNodeModel().rowsRun / getNodeModel().numRows;
			inputTableProgress.setValue((int) progressPercent);
			inputTableProgress.setStringPainted(true);
		} else {
			inputTableProgress.setIndeterminate(true);
			inputTableProgress.setStringPainted(false);
		}
		if (getNodeModel().getQueueSize() != null) {
			completedQueue.setValue(getNodeModel().completedQueue);
			completedQueue.setString(completedQueue.getValue() + "/" + completedQueue.getMaximum());
			completedQueue.setIndeterminate(false);
		} else {
			completedQueue.setIndeterminate(true);
		}

		activeThreads.setValue(getNodeModel().activeThreads);
		activeThreads.setString(activeThreads.getValue() + "/" + activeThreads.getMaximum());

	}

	private void initialiseComponents() {
		masterPanel = new JPanel();
		// 2 columns and as many rows as needed
		masterPanel.setLayout(new GridLayout(0, 2));

		masterPanel.add(new JLabel("Table Processed"));
		inputTableProgress = new JProgressBar(0, 100);
		if (getNodeModel().numRows != null) {
			inputTableProgress.setIndeterminate(false);
		} else {
			inputTableProgress.setIndeterminate(true);
		}
		inputTableProgress.setToolTipText("The proportion of the input table fully processed");
		masterPanel.add(inputTableProgress);

		masterPanel.add(new JLabel("Pending Queue Size"));
		if (getNodeModel().getQueueSize() != null) {
			completedQueue = new JProgressBar(0, getNodeModel().getQueueSize());
			completedQueue.setIndeterminate(false);
		} else {
			completedQueue = new JProgressBar();
			completedQueue.setIndeterminate(true);
		}
		completedQueue.setToolTipText(
				"Proportion of total queue space occupied by processed rows awaiting adding to outputs");
		completedQueue.setStringPainted(true);
		completedQueue.setString("0/" + completedQueue.getMaximum());
		masterPanel.add(completedQueue);

		masterPanel.add(new JLabel("Active threads"));
		activeThreads = new JProgressBar(0, getNodeModel().getNumThreads());
		activeThreads.setToolTipText("Proportion of available threads active");
		activeThreads.setStringPainted(true);
		activeThreads.setString("0/" + activeThreads.getMaximum());
		masterPanel.add(activeThreads);

		setComponent(masterPanel);
	}

}
