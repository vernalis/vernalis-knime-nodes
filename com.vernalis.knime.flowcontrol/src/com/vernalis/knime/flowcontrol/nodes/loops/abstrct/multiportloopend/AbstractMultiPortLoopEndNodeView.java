/*******************************************************************************
 * Copyright (c) 2021, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.nodes.loops.abstrct.multiportloopend;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.knime.core.node.NodeView;
import org.knime.core.node.tableview.TableView;

/**
 * NodeView implementation for Loop end nodes using
 * {@link ConcatenatingTablesCollector} to store results. The view provides a
 * tabbed preview of the loop end tables
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 * @since v1.29.0
 *
 */
public class AbstractMultiPortLoopEndNodeView
		extends NodeView<AbstractMultiPortLoopEndNodeModel> {

	private TableView[] tables;
	private JTabbedPane tabs;
	private JPanel comp;
	private static final Dimension dim = new Dimension(650, 450);
	private static final String DEFAULT_TAB_PREFIX = "Table";
	private final String tabPrefix;

	/**
	 * Constructor using the default tab label prefix
	 * 
	 * @param nodeModel
	 *            The NodeModel instance
	 */
	public AbstractMultiPortLoopEndNodeView(
			AbstractMultiPortLoopEndNodeModel nodeModel) {
		this(nodeModel, DEFAULT_TAB_PREFIX);
	}

	/**
	 * Constructor with user-defined tab label prefix
	 * 
	 * @param nodeModel
	 *            The NodeModel instance
	 * @param tabPrefix
	 *            The prefix to use on tab labels
	 */
	public AbstractMultiPortLoopEndNodeView(
			AbstractMultiPortLoopEndNodeModel nodeModel, String tabPrefix) {
		super(nodeModel);
		this.tabPrefix = tabPrefix == null ? "" : tabPrefix.trim();
		setShowNODATALabel(false);
		tables = new TableView[nodeModel.m_NumPorts];
		comp = new JPanel(new BorderLayout());
		tabs = new JTabbedPane();
		// BorderLayout.CENTER ensures the component always wills the JPanel
		comp.add(tabs, BorderLayout.CENTER);

		JPanel buttonBar = new JPanel(new BorderLayout());
		JButton refreshTablesBtn = new JButton("Update Table Previews");
		refreshTablesBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				refreshTablesBtn.setEnabled(false);
				rebuildTabs(getNodeModel());
				refreshTablesBtn.setEnabled(true);
			}
		});
		buttonBar.add(refreshTablesBtn, BorderLayout.CENTER);
		buttonBar.setBorder(BorderFactory.createBevelBorder(3));
		comp.add(buttonBar, BorderLayout.SOUTH);
		setComponent(comp);
		rebuildTabs(nodeModel);
		fixSize();
	}

	@Override
	protected void onClose() {

	}

	@Override
	protected void onOpen() {

	}

	@Override
	protected void modelChanged() {
		tabs.removeAll();
		AbstractMultiPortLoopEndNodeModel nodeModel = getNodeModel();
		rebuildTabs(nodeModel);
	}

	/**
	 * Method to rebuild the tabs. If no preview tables are available (i.e.
	 * before the node has executed for the first time, or when loop execution
	 * is complete) then a 'No data' message will be displayed
	 * 
	 * @param nodeModel
	 *            The node model instance
	 */
	protected void rebuildTabs(AbstractMultiPortLoopEndNodeModel nodeModel) {
		int oldTabCount = tabs.getTabCount();
		if (nodeModel.m_resultContainer != null) {
			if (oldTabCount > 0) {
				tabs.removeAll();
			}
			for (int i = 0; i < tables.length; i++) {
				if (nodeModel.m_resultContainer[i] != null
						&& nodeModel.m_resultContainer[i]
								.getTableSpec() != null) {
					tables[i] = nodeModel.m_resultContainer[i].getPreviewView();
					tabs.insertTab(String.format("%s %d", tabPrefix, i), null,
							tables[i], null, tabs.getTabCount());
				} else {
					tables[i] = null;
				}
			}
		}
		if (oldTabCount == 0 && tabs.getTabCount() > 0) {
			setComponent(comp);
			fixSize();
		} else if (tabs.getTabCount() == 0) {
			JLabel noData = new JLabel("<html><center>No Data!</center></html>",
					SwingConstants.CENTER);
			noData.setPreferredSize(dim);
			setComponent(noData);
			fixSize();

		}
	}

	/**
	 * Method to fix the size of the window, otherwise it is unusable when
	 * initialised without data
	 */
	protected void fixSize() {
		Container frm = getComponent().getParent();
		while (!(frm instanceof JFrame)) {
			frm = frm.getParent();
		}
		frm.invalidate();
		frm.validate();
		frm.setSize(dim);
		frm.repaint();
	}

}
