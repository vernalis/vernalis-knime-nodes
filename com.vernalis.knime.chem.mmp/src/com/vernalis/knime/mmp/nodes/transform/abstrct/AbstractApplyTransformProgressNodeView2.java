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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.NodeView;

/**
 * More advanced node progress view, showing a table of current transforms and
 * their progress
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
public class AbstractApplyTransformProgressNodeView2<T, U, V>
		extends NodeView<AbstractApplyTransformNodeModel<T, U, V>> {
	JProgressBar overallProgress;
	JPanel masterPanel;
	private JPanel overallProgressPane;
	private JPanel transformProgressPane;
	private ProgressTable transformProgressTable;

	/**
	 * Constructor
	 * 
	 * @param nodeModel
	 *            The node model instance for the view
	 */
	public AbstractApplyTransformProgressNodeView2(
			AbstractApplyTransformNodeModel<T, U, V> nodeModel) {
		super(nodeModel);

		masterPanel = new JPanel();

		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));

		overallProgressPane = new JPanel();
		overallProgressPane.setLayout(new GridLayout(1, 2));
		overallProgressPane.setBorder(new EtchedBorder());

		masterPanel.add(overallProgressPane);
		overallProgressPane.add(new JLabel("Overall Progress"));
		overallProgress = new JProgressBar(0, 100);

		overallProgress.setToolTipText("The proportion of the input table fully processed");
		overallProgressPane.add(overallProgress);

		transformProgressPane = new JPanel();
		transformProgressPane.setBorder(new EtchedBorder());
		transformProgressPane.setLayout(new BorderLayout());
		masterPanel.add(transformProgressPane);

		transformProgressTable = new ProgressTable(getNodeModel().transformProgressModel);

		JScrollPane transformProgressScrollPane = new JScrollPane(transformProgressTable);

		transformProgressPane.add(transformProgressScrollPane);
		setComponent(masterPanel);

		getJMenuBar().add(getSMARTSRendererMenu());
		getJMenuBar().add(getAspectRatioMenu());

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
		} else {
			overallProgress.setIndeterminate(false);
			overallProgress.setValue((int) (100.0 * getNodeModel().totalProgress));
		}

	}

	private void initialiseComponents() {
		if (getNodeModel().totalProgress < 0.0) {
			overallProgress.setIndeterminate(true);
		} else {
			overallProgress.setIndeterminate(false);
		}
	}

	private JMenu getAspectRatioMenu() {
		JMenu aspectRatioMenu = new JMenu("Aspect Ratio");
		aspectRatioMenu.setMnemonic(KeyEvent.VK_A);
		aspectRatioMenu.getAccessibleContext().setAccessibleDescription(
				"Menu to change the aspect ratio options for the transform display");
		JCheckBoxMenuItem lockAspRatio = new JCheckBoxMenuItem("Lock aspect ratio",
				transformProgressTable.getTableHeader().lockAspectRatio);
		lockAspRatio.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				transformProgressTable.getTableHeader().lockAspectRatio = lockAspRatio.getState();

			}
		});
		aspectRatioMenu.add(lockAspRatio);
		aspectRatioMenu.addSeparator();

		String[] ar = new String[] { "2:1", "4:3", "16:9", "15:1" };
		ButtonGroup arGroup = new ButtonGroup();
		for (String aspRatio : ar) {
			JRadioButtonMenuItem rb = new JRadioButtonMenuItem(aspRatio);
			rb.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					transformProgressTable
							.setAspectRatio(1.0 * Integer.parseInt(aspRatio.split(":")[0])
									/ Integer.parseInt(aspRatio.split(":")[1]));

				}
			});
			double ratio = 1.0 * Integer.parseInt(aspRatio.split(":")[0])
					/ Integer.parseInt(aspRatio.split(":")[1]);
			if (Double.compare(ratio, transformProgressTable.getTableHeader().aspectRatio) == 0) {
				rb.setSelected(true);
			}
			arGroup.add(rb);
			aspectRatioMenu.add(rb);
		}

		return aspectRatioMenu;
	}

	private JMenu getSMARTSRendererMenu() {
		JMenu smartsMenu = new JMenu("Transform Renderer");
		smartsMenu.setMnemonic(KeyEvent.VK_T);
		smartsMenu.getAccessibleContext().setAccessibleDescription(
				"Menu to change the renderer used for the Transform column");

		for (String renderer : transformProgressTable.getSMARTSRendererNames()) {
			JMenuItem rendMenuItem = new JMenuItem(renderer);
			rendMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					transformProgressTable.setRendererName(e.getActionCommand());

				}
			});
			smartsMenu.add(rendMenuItem);

		}

		String preferredRenderer = transformProgressTable.getPreferredSMARTSRendererName();
		if (preferredRenderer != null) {
			smartsMenu.addSeparator();
			smartsMenu.add(new JMenuItem("Preferred Renderer (" + preferredRenderer + ")"));
		}
		return smartsMenu;
	}
}
