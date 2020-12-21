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
package com.vernalis.pdbconnector2.dialogcomponents.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A scrollable popup menu. Similar to a JComboBox menu, this has a default
 * limit of 8 items visible in the dropdown which can be changed by calling
 * {@link #setMaxRows(int)}
 *
 * Based loosely on https://stackoverflow.com/a/14167008/6076839 and
 * https://stackoverflow.com/a/58413519/6076839
 *
 * @author Steve
 *
 */
public class JScrollPopupMenu extends JPopupMenu implements ChangeListener {

	private int maxRows = 8;
	private final JScrollBar sBar;
	private final int scrollBarSide;

	/**
	 * Constant for a LEFT scrollbar
	 */
	public static final int SCROLLBAR_LEFT = 1;

	/**
	 * Constant for a RIGHT scrollbar
	 */
	public static final int SCROLLBAR_RIGHT = 0;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor, with the scrollbar positioned on the right of the popup
	 */
	public JScrollPopupMenu() {
		this(SCROLLBAR_RIGHT);
	}

	/**
	 * Constructor allowing choosing of the scrollbar side using
	 * {@value #SCROLLBAR_LEFT} or {@value #SCROLLBAR_RIGHT}
	 *
	 * @param scrollBarSide The side of the menu for the scrollbar
	 *                      (JScrollPopupMenu{@link #SCROLLBAR_LEFT} or
	 *                      {@link JScrollPopupMenu#SCROLLBAR_RIGHT})
	 */
	public JScrollPopupMenu(int scrollBarSide) {
		super();
		if (scrollBarSide > SCROLLBAR_LEFT || scrollBarSide < SCROLLBAR_RIGHT) {
			throw new IllegalArgumentException("Scrollbar side must be 0 or 1");
		}
		this.scrollBarSide = scrollBarSide;
		setLayout(new JScrollPopupMenuLayout());
		sBar = new JScrollBar(JScrollBar.VERTICAL);
		sBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				doLayout();
				repaint();
			}
		});
		sBar.setVisible(false);
		add(sBar);

		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				sBar.setValue(sBar.getValue() + (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL
						? e.getUnitsToScroll() * sBar.getUnitIncrement()
								: (e.getWheelRotation() < 0 ? -1 : 1) * sBar.getBlockIncrement()));
				e.consume();
			}
		});

	}

	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		super.addImpl(comp, constraints, index);
		// Add a change listener on the button to enable mouse behaviour and scrolling
		// when otherwise selection would go out of scrolled view scop
		if (comp instanceof AbstractButton) {
			((AbstractButton) comp).getModel().addChangeListener(this);
		}
		sBar.setVisible(needsScrollbar());
	}

	/**
	 * @return {@code true} if the scrollbar should be displayed (i.e. there are
	 *         more menu items that {@value #maxRows})
	 */
	private boolean needsScrollbar() {
		// Component count includes the scrollbar, upper limit is inclusive
		return getMaxRows() < getComponentCount();
	}

	@Override
	public void remove(int pos) {
		// Offset for scrollbar
		final int actualPos = pos + 1;
		final Component c = getComponent(actualPos);
		// Unregister the change listener
		if (c instanceof AbstractButton) {
			((AbstractButton) c).getModel().removeChangeListener(this);
		}
		super.remove(actualPos);
		sBar.setVisible(needsScrollbar());
	}

	@Override
	public void insert(Action a, int index) {
		// Offset for scrollbar
		super.insert(a, index + 1);
	}

	@Override
	public void insert(Component component, int index) {
		// Offset for scrollbar
		super.insert(component, index + 1);
	}

	/**
	 * @return The maximum number of rows to show
	 */
	public int getMaxRows() {
		return maxRows;
	}

	/**
	 * @param maxRows The maximum number of rows to show (in range 2-50)
	 */
	public void setMaxRows(int maxRows) {
		if (maxRows < 2 || maxRows > 50) {
			throw new IllegalArgumentException("Max rows must be in range 2 - 50");
		}
		this.maxRows = maxRows;
	}

	@Override
	public void show(Component invoker, int x, int y) {

		// Need to work out the scrollbar parameters if it is visible and set the size
		// of the popup accordingly. We are assuming for scrollbar unit scrolling that
		// all items are same height
		if (sBar.isVisible()) {
			int extent = 0;
			int totalHeight = 0;
			int unitHeight = -1;
			int width = 0;

			for (int i = 1; i < getComponentCount(); i++) {
				final Dimension preferredSize = getComponent(i).getPreferredSize();
				width = Math.max(width, preferredSize.width);
				if (unitHeight < 0) {
					unitHeight = preferredSize.height;
				}
				if (i <= getMaxRows()) {
					extent += preferredSize.height;
				}
				totalHeight += preferredSize.height;
			}

			final Insets insets = getInsets();
			final int insetWidth = insets.left + insets.right;
			final int insetHeight = insets.top + insets.bottom;
			sBar.setUnitIncrement(unitHeight);
			sBar.setBlockIncrement(extent);
			sBar.setValues(0, insetHeight + extent, 0, insetHeight + totalHeight);

			width += sBar.getPreferredSize().width + insetWidth;
			final int height = insetHeight + extent;

			setPopupSize(new Dimension(width, height));
		}

		super.show(invoker, x, y);

	}

	@Override
	protected void paintChildren(Graphics g) {
		// Account for any border insets
		final Insets insets = getInsets();
		g.clipRect(insets.left, insets.top, getWidth(), getHeight() - insets.top - insets.bottom);
		super.paintChildren(g);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (!sBar.isVisible()) {
			// We dont have a scrollbar so nothing to do here...
			return;
		}
		// Some of this is adapted from
		// https://stackoverflow.com/questions/58413379/is-there-a-way-to-detect-current-selection-in-jpopupmenu-selectionmodel-changel

		// We need to find the current selected or armed button in the JPopupMenu
		final ButtonModel model = (ButtonModel) e.getSource();
		final AbstractButton selected = Stream.of(getComponents()).filter(c -> c instanceof AbstractButton)
				.map(c -> (AbstractButton) c)
				.filter(b -> (b.getModel().isArmed() || b.getModel().isSelected()) && b.getModel() == model).findAny()
				.orElse(null);
		if (selected != null) {
			// Scroll up or down to get the selected in view
			while (selected.getY() + selected.getHeight() > getHeight() - getInsets().bottom) {
				sBar.setValue(sBar.getValue() + sBar.getUnitIncrement());
			}
			while (selected.getY() < getInsets().top) {
				sBar.setValue(sBar.getValue() - sBar.getUnitIncrement());
			}
		}
		setSelected(selected);
	}

	/**
	 * A Layout which accounts for the JScrollBar being on the right of the popup
	 *
	 * @author Steve
	 *
	 */
	private class JScrollPopupMenuLayout implements LayoutManager {

		@Override
		public void addLayoutComponent(String name, Component comp) {
			//

		}

		@Override
		public void removeLayoutComponent(Component comp) {
			//

		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			int scrollBarExtent = Integer.MAX_VALUE;
			final Dimension retVal = new Dimension();
			for (final Component c : parent.getComponents()) {
				if (!c.isVisible()) {
					continue;
				} else if (c instanceof JScrollBar) {
					scrollBarExtent = ((JScrollBar) c).getVisibleAmount();
				} else {
					final Dimension cDim = c.getPreferredSize();
					if (cDim.width > retVal.width) {
						retVal.width = cDim.width;
					}
					retVal.height += cDim.height;
				}
			}
			final Insets insets = parent.getInsets();
			retVal.height = Math.min(scrollBarExtent, retVal.height + insets.top + insets.bottom);
			return retVal;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			int scrollBarExtent = Integer.MAX_VALUE;
			final Dimension retVal = new Dimension();
			for (final Component c : parent.getComponents()) {
				if (!c.isVisible()) {
					continue;
				} else if (c instanceof JScrollBar) {
					scrollBarExtent = ((JScrollBar) c).getVisibleAmount();
				} else {
					final Dimension cDim = c.getMinimumSize();
					if (cDim.width > retVal.width) {
						retVal.width = cDim.width;
					}
					retVal.height += cDim.height;
				}
			}
			final Insets insets = parent.getInsets();
			retVal.height = Math.min(scrollBarExtent, retVal.height + insets.top + insets.bottom);
			return retVal;
		}

		@Override
		public void layoutContainer(Container parent) {
			final Insets insets = parent.getInsets();
			int x = insets.left;
			int y = insets.top;
			int width = parent.getWidth() - x - insets.right;
			final int height = parent.getHeight() - y - insets.bottom;

			// Layout the Scrollbar and adjust the space for the menu items
			if (sBar.isVisible()) {
				final Dimension sbDim = sBar.getPreferredSize();
				if (scrollBarSide == SCROLLBAR_RIGHT) {
					sBar.setBounds(x + width - sbDim.width, y, sbDim.width, height);
				} else {
					sBar.setBounds(x, y, sbDim.width, height);
					x += sbDim.width;
				}
				width -= sbDim.width;
				y -= sBar.getValue();
			}

			// Now work through the visible menu items
			for (final Component c : parent.getComponents()) {
				if (c.isVisible() && !(c instanceof JScrollBar)) {
					final int cHeight = c.getPreferredSize().height;
					c.setBounds(x, y, width, cHeight);
					y += cHeight;
				}
			}
		}

	}
}
