/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query.text.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;

import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;
import com.vernalis.pdbconnector2.dialogcomponents.swing.SwingUtils;
import com.vernalis.pdbconnector2.query.QueryPanel;

import static com.vernalis.pdbconnector2.PdbConnector2Helpers.SCROLL_SPEED;

/**
 * A {@link JPanel} implementation of {@link QueryPanel} representing a Query
 * Group.
 * 
 * The panel comprises:
 * <ul>
 * <li>A {@link Box} for the query field panels to go in</li>
 * <li>A {@link Box} for any subquery panels to go in</li>
 * <li>A button panel, which contains
 * <ul>
 * <li>If this is not the root parient group, a 'Remove Group' button</li>
 * <li>An 'Add Field' button</li>
 * <li>An 'Add SubGroup' button</li>
 * <li>A query conjunction selector</li>
 * </ul>
 * </li>
 * <li>A count/clear button pair, to count the results for the query group
 * alone, and to clear the query in the group</li>
 * </ul>
 * 
 * Some of the buttons highlight the component border for clarity
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryGroupPanel extends JPanel
		implements Scrollable, ChangeListener, QueryPanel<QueryGroupModel> {

	private final class HighlightBorderMouseListener implements MouseListener {

		private final Color highlightColor;

		/**
		 * @param highlightColor
		 */
		private HighlightBorderMouseListener(Color highlightColor) {
			this.highlightColor = highlightColor;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// Nothing

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// Nothing

		}

		@Override
		public void mouseExited(MouseEvent e) {
			resetBorder();

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			highlightBorder(highlightColor);

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// nothing

		}
	}

	private static final Color BACKGROUND = new JFrame().getBackground();

	private static final long serialVersionUID = 1L;
	/** The fields and components go in here */
	private final Box componentBox;
	/** The fields go in here */
	private final Box fieldPanel;
	private final Box subQueryPanel;
	private final Box buttonPanel;
	private final QueryGroupModel model;
	private final QueryGroupPanel parent;
	private final CountClearButtonBox clearCountButtons;

	/**
	 * Constructor for a root-level pane with no parent group
	 * 
	 * @param model
	 *            The model for the query
	 */
	public QueryGroupPanel(QueryGroupModel model) {
		this(model, null);
	}

	/**
	 * Constructor for a group pane with optional parent group
	 * 
	 * @param model
	 *            The model for the query
	 * @param parent
	 *            the parent group containing this group - {@code null} if this
	 *            is the root level query group
	 */
	public QueryGroupPanel(QueryGroupModel model, QueryGroupPanel parent) {
		super();
		setLayout(new BorderLayout());
		// Because we are in a white background potentially, we need to set
		// these:
		setOpaque(true);
		setBackground(BACKGROUND);
		resetBorder();

		this.model = model;
		this.parent = parent;
		this.model.addChangeListener(this);

		componentBox = new Box(BoxLayout.Y_AXIS);
		if (parent == null) {
			// If this is the top level, then the components go in a scrollpane
			// - otherwise
			// not (the result if they are is an extremely busy chaos!)
			// componentBox.setBackground(Color.white);
			final JScrollPane sPane = new JScrollPane(componentBox,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			sPane.getViewport().setBackground(Color.white);
			sPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
			sPane.setMinimumSize(new Dimension(
					600 + sPane.getInsets().left + sPane.getInsets().right
							+ new JScrollBar(JScrollBar.VERTICAL)
									.getPreferredSize().width,
					75 * 4 + sPane.getInsets().top + sPane.getInsets().bottom));
			add(sPane, BorderLayout.CENTER);
			componentBox.setMinimumSize(new Dimension(600, 4 * 75));
		} else {
			add(componentBox, BorderLayout.CENTER);
		}

		fieldPanel = new Box(BoxLayout.Y_AXIS);
		componentBox.add(fieldPanel);
		for (final QueryFieldModel field : getQueryModel().getFields()) {
			addFieldDialog(field);
		}

		subQueryPanel = new Box(BoxLayout.Y_AXIS);
		componentBox.add(subQueryPanel);
		for (final QueryGroupModel subGroup : getQueryModel().getSubgroups()) {
			addSubgroupDialog(subGroup);
		}
		// Make sure everything stays preferentially at the top
		componentBox.add(Box.createGlue());

		clearCountButtons = new CountClearButtonBox(this);
		clearCountButtons.addCountButtonMouseListener(
				new HighlightBorderMouseListener(Color.GREEN));
		clearCountButtons.addClearButtonMouseListener(
				new HighlightBorderMouseListener(Color.MAGENTA));

		buttonPanel = createButtonBox();
		SwingUtils.keepHeightFillWidth(buttonPanel);
		add(buttonPanel, BorderLayout.SOUTH);

		getQueryModel().addChangeListener(this);
		if (getQueryModel().isEmpty()) {
			getQueryModel().addField(new QueryFieldModel());
		}

	}

	private Box createButtonBox() {
		final Box retVal = new Box(BoxLayout.X_AXIS);
		retVal.add(Box.createGlue());
		if (this.parent != null) {
			final JButton removeGrp = new JButton("Remove Group");
			removeGrp.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					// The QueryGroupChangeListener on the parent model will
					// remove the dialog
					// component
					getParentGroup().getQueryModel()
							.removeGroup(getQueryModel());

				}
			});
			removeGrp.addMouseListener(
					new HighlightBorderMouseListener(Color.RED));
			retVal.add(removeGrp);
			retVal.add(Box.createHorizontalStrut(5));
		}
		final JButton addField = new JButton("Add Field");
		addField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getQueryModel().addField(new QueryFieldModel());
			}
		});
		retVal.add(addField);
		retVal.add(Box.createHorizontalStrut(5));

		final JButton addSubGroup = new JButton("Add SubGroup");
		addSubGroup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getQueryModel().addGroup(new QueryGroupModel());

			}
		});
		retVal.add(addSubGroup);
		// No strut here as CCBB has 5 pixel border
		retVal.add(getButtons());

		retVal.add(
				SwingUtils.forceToPreferredSize(new DialogComponentButtonGroup(
						getQueryModel().getConjunctionModel(), "Conjunction",
						false, QueryGroupConjunction.values())));

		retVal.add(Box.createGlue());
		retVal.setMinimumSize(retVal.getPreferredSize());

		return retVal;
	}

	/**
	 * @return the parent query group, which maybe {@code null}
	 */
	public QueryGroupPanel getParentGroup() {
		return parent;
	}

	private void addSubgroupDialog(QueryGroupModel subGroupModel) {
		subQueryPanel.setVisible(true);
		subQueryPanel.add(new QueryGroupPanel(subGroupModel, this));
		revalidate();

	}

	@Override
	public void revalidate() {
		if (getParentGroup() != null) {
			getParentGroup().revalidate();
		}
		super.revalidate();
	}

	private void addFieldDialog(QueryFieldModel fieldModel) {
		fieldPanel.setVisible(true);
		revalidate();
		fieldPanel.add(new QueryFieldPanel(fieldModel, this));
		// fieldPanel.revalidate();
		revalidate();

	}

	/**
	 * Method to remove a subgroup panel. A call removes the subgroup from the
	 * {@link QueryGroupModel}, firing the change listeners
	 * 
	 * @param queryGroupPanel
	 *            the panel to remove
	 */
	public void removeSubgroup(QueryGroupPanel queryGroupPanel) {
		// Fires changelistener on model which in turn removes the component
		model.removeGroup(queryGroupPanel.getQueryModel());

	}

	/**
	 * Method to remove a subgroup panel. A call removes the subgroup from the
	 * {@link QueryGroupModel}, firing the change listeners. Does not re-fire
	 * the change listeners if the model has already been removed
	 * 
	 * @param queryGroupModel
	 *            the model of the subgruop to remove
	 */
	public void removeSubgroup(QueryGroupModel queryGroupModel) {
		// Won't fire changelistener again if already removed
		model.removeGroup(queryGroupModel);
		for (final Component c : subQueryPanel.getComponents()) {
			if (c instanceof QueryGroupPanel) {
				final QueryGroupPanel qgp = (QueryGroupPanel) c;
				if (qgp.getQueryModel().equals(queryGroupModel)) {
					removeSubgroupDialog(qgp);
					break;
				}
			}
		}
	}

	private void removeSubgroupDialog(QueryGroupPanel queryGroupPanel) {
		subQueryPanel.remove(queryGroupPanel);
		if (!model.hasSubGroups()) {
			subQueryPanel.setVisible(false);
		}
		revalidate();
	}

	/**
	 * Remove a field panel, firing the change listeners, by removing the field
	 * model from the model
	 * 
	 * @param queryFieldPanel
	 *            The query field panel to remove
	 */
	public void removeQueryField(QueryFieldPanel queryFieldPanel) {
		// Fires changelistener on model which in turn removes the component
		model.removeField(queryFieldPanel.getQueryModel());

	}

	/**
	 * Remove a query field from the model, firing change listeners if the field
	 * has not already been removed
	 * 
	 * @param queryFieldModel
	 *            the model of the field to remove
	 */
	public void removeQueryField(QueryFieldModel queryFieldModel) {
		// Won't fire changelistener again if already removed
		model.removeField(queryFieldModel);
		for (final Component c : fieldPanel.getComponents()) {
			if (c instanceof QueryFieldPanel) {
				final QueryFieldPanel qfp = (QueryFieldPanel) c;
				if (qfp.getQueryModel().equals(queryFieldModel)) {
					removeFieldDialog(qfp);
					break;
				}
			}
		}
	}

	private void removeFieldDialog(QueryFieldPanel queryFieldPanel) {
		fieldPanel.remove(queryFieldPanel);
		if (fieldPanel.getComponentCount() == 0) {
			fieldPanel.setVisible(false);
		}
		fieldPanel.revalidate();
	}

	@Override
	public QueryGroupModel getQueryModel() {
		return model;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return fieldPanel.getComponentCount() > 0
				? fieldPanel.getComponent(0).getHeight()
				: subQueryPanel.getComponentCount() > 0
						? ((QueryGroupPanel) subQueryPanel.getComponent(0))
								.getScrollableBlockIncrement(visibleRect,
										orientation, direction)
						: 100;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		final int rh = fieldPanel.getComponentCount() > 0
				? fieldPanel.getComponent(0).getHeight()
				: subQueryPanel.getComponentCount() > 0
						? ((QueryGroupPanel) subQueryPanel.getComponent(0))
								.getScrollableBlockIncrement(visibleRect,
										orientation, direction)
						: 0;
		return rh > 0 ? Math.max(rh, visibleRect.height / rh * rh)
				: visibleRect.height;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	/**
	 * Attempt to ensure that the button controls are visible in any containing
	 * panel
	 */
	public void ensureControlsVisible() {
		final Rectangle bounds = buttonPanel.getBounds();
		bounds.y += getPreferredSize().getHeight() / 2;
		scrollRectToVisible(bounds);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// We need to track field and subgroup changes to update the
		// buttons...
		// Any change in the fields or any of the subgroups must reset the count
		// button
		if (!(e instanceof QueryGroupChangeEvent)) {
			return;
		}
		final QueryGroupChangeEvent e1 = (QueryGroupChangeEvent) e;
		if (e1.getType() == null) {
			return;
		}
		switch (e1.getType()) {
			case FieldAdded:
				addFieldDialog((QueryFieldModel) e1.getSource());
				break;
			case FieldRemoved:
				removeQueryField((QueryFieldModel) e1.getSource());
				break;
			case GroupAdded:
				addSubgroupDialog((QueryGroupModel) e1.getSource());
				break;
			case GroupRemoved:
				removeSubgroup((QueryGroupModel) e1.getSource());
				break;
			case LogicOperatorChange:
				// Nothing to do
				break;
			default:
				break;

		}

	}

	@Override
	public CountClearButtonBox getButtons() {
		return clearCountButtons;
	}

	private void resetBorder() {
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
						new EtchedBorder(EtchedBorder.RAISED),
						new EtchedBorder(EtchedBorder.LOWERED)),
				new EmptyBorder(5, 5, 5, 5)));
	}

	private void highlightBorder(Color highlight) {
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(
						new EtchedBorder(EtchedBorder.RAISED, highlight,
								highlight),
						new EtchedBorder(EtchedBorder.LOWERED)),
				new EmptyBorder(5, 5, 5, 5)));
	}

}
