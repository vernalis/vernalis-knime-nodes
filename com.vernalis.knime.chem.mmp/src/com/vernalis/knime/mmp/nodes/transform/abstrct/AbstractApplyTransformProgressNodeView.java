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
package com.vernalis.knime.mmp.nodes.transform.abstrct;

import java.awt.GridLayout;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.knime.core.node.NodeView;

/**
 * Simple node view panel with a progress bar and label for each thread
 * (transform)
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the query molecule object
 * @param <V>
 *            The type of the reaction/transform object
 */
public class AbstractApplyTransformProgressNodeView<T, U, V>
		extends NodeView<AbstractApplyTransformNodeModel<T, U, V>> {
	JProgressBar overallProgress, completedQueue, activeThreads;
	JPanel masterPanel;
	private JPanel overallProgressPane;
	private JPanel transformProgressPane;
	private JLabel[] transformProgressLabels;
	private JProgressBar[] transformProgressBars;

	/**
	 * Constructor
	 * 
	 * @param nodeModel
	 *            The node model instance for the view
	 */
	public AbstractApplyTransformProgressNodeView(
			AbstractApplyTransformNodeModel<T, U, V> nodeModel) {
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
		if (getNodeModel().totalProgress >= 0.0) {
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
		if (getNodeModel().totalProgress < 0.0) {
			overallProgress.setIndeterminate(true);
			for (JProgressBar pb : transformProgressBars) {
				pb.setIndeterminate(true);
			}
		} else {
			overallProgress.setIndeterminate(false);
			for (JProgressBar pb : transformProgressBars) {
				pb.setIndeterminate(false);
			}
			overallProgress.setValue((int) (100.0 * getNodeModel().totalProgress));
			int i = 0;
			try {
				Iterator<Entry<Long, Double>> iter =
						getNodeModel().transformProgress.entrySet().iterator();
				while (iter.hasNext() && i < transformProgressBars.length) {
					Entry<Long, Double> prog = iter.next();
					transformProgressLabels[i].setText("Transform " + prog.getKey());
					transformProgressLabels[i]
							.setToolTipText(getNodeModel().transformSMARTSMap.get(prog.getKey()));
					transformProgressBars[i].setValue((int) (prog.getValue() * 100.0));
					if (!transformProgressBars[i].isVisible()) {
						transformProgressBars[i].setVisible(true);
					}
					i++;
				}
				while (i < transformProgressBars.length) {
					transformProgressLabels[i].setText("");
					transformProgressLabels[i].setToolTipText("");
					transformProgressBars[i].setValue(0);
					transformProgressBars[i].setVisible(false);
					i++;
				}
			} catch (ConcurrentModificationException e) {
				// just skip and keep going - another thread finished a task
				// while this was redrawing
			}
		}

	}

	private void initialiseComponents() {
		masterPanel = new JPanel();
		// 2 rows, 1 col
		masterPanel.setLayout(new GridLayout(2, 1));

		overallProgressPane = new JPanel();
		overallProgressPane.setLayout(new GridLayout(1, 2));
		overallProgressPane.setBorder(new EtchedBorder());

		masterPanel.add(overallProgressPane);
		overallProgressPane.add(new JLabel("Overall Progress"));
		overallProgress = new JProgressBar(0, 100);
		if (getNodeModel().totalProgress < 0.0) {
			overallProgress.setIndeterminate(true);
		} else {
			overallProgress.setIndeterminate(false);
		}
		overallProgress.setToolTipText("The proportion of the input table fully processed");
		overallProgressPane.add(overallProgress);

		transformProgressPane = new JPanel();
		transformProgressPane.setLayout(new GridLayout(0, 2));
		transformProgressPane.setBorder(new TitledBorder("Individual threads progress"));
		masterPanel.add(transformProgressPane);

		transformProgressLabels = new JLabel[getNodeModel().numThreads];
		transformProgressBars = new JProgressBar[getNodeModel().numThreads];
		for (int i = 0; i < transformProgressLabels.length; i++) {
			transformProgressLabels[i] = new JLabel();
			transformProgressBars[i] = new JProgressBar(0, 100);
			transformProgressPane.add(transformProgressLabels[i]);
			transformProgressPane.add(transformProgressBars[i]);
			transformProgressBars[i].setIndeterminate(getNodeModel().totalProgress < 0.0);
			transformProgressBars[i].setVisible(false);
		}

		setComponent(masterPanel);
	}

}
