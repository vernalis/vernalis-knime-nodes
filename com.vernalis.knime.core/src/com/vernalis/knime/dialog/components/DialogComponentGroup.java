/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.dialog.components;

import java.awt.Container;
import java.util.Optional;
import java.util.OptionalInt;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;

/**
 * A class to allow arbitrary nesting of groups of dialog components within a
 * {@link DefaultNodeSettingsPane}. Each group must have at least one component,
 * added at constructor time, and will be drawn within a boxed border.
 * Optionally, a title can be supplied and a direction specified (horizontal or
 * vertical). If no direction is specified, then the direction will be inherited
 * from the parent
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class DialogComponentGroup {

	private static final int DEFAULT_AXIS = BoxLayout.Y_AXIS;
	private final DefaultNodeSettingsPane pane;
	private final Container parent;
	private final JPanel panel;

	/**
	 * Simple constructor with no title and inherited layout orientation
	 * 
	 * @param nodeSettingsPane
	 *            The {@link DefaultNodeSettingsPane} to add the components to
	 * @param firstDiaC
	 *            The first {@link DialogComponent} to go in the grouping
	 */
	public DialogComponentGroup(DefaultNodeSettingsPane nodeSettingsPane,
			DialogComponent firstDiaC) {
		this(nodeSettingsPane, Optional.empty(), OptionalInt.empty(),
				firstDiaC);
	}

	/**
	 * Simple constructor with title and inherited layout orientation
	 * 
	 * @param nodeSettingsPane
	 *            The {@link DefaultNodeSettingsPane} to add the components to
	 * @param title
	 *            The title to display on the border
	 * @param firstDiaC
	 *            The first {@link DialogComponent} to go in the grouping
	 */
	public DialogComponentGroup(DefaultNodeSettingsPane nodeSettingsPane,
			String title, DialogComponent firstDiaC) {
		this(nodeSettingsPane, Optional.ofNullable(title), OptionalInt.empty(),
				firstDiaC);
	}

	/**
	 * Constructor with no title, specifiying layout orientation
	 * 
	 * @param nodeSettingsPane
	 *            The {@link DefaultNodeSettingsPane} to add the components to
	 * @param firstDiaC
	 *            The first {@link DialogComponent} to go in the grouping
	 * @param horizontal
	 *            the layout orientation
	 */
	public DialogComponentGroup(DefaultNodeSettingsPane nodeSettingsPane,
			DialogComponent firstDiaC, boolean horizontal) {
		this(nodeSettingsPane, Optional.empty(),
				OptionalInt
						.of(horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS),
				firstDiaC);
	}

	/**
	 * Full constructor with title, and layout orientation
	 * 
	 * @param nodeSettingsPane
	 *            The {@link DefaultNodeSettingsPane} to add the components to
	 * @param title
	 *            The optional title of the bounding box
	 * @param firstDiaC
	 *            The first {@link DialogComponent} to go in the grouping
	 * @param horizontal
	 *            the layout orientation
	 */
	public DialogComponentGroup(DefaultNodeSettingsPane nodeSettingsPane,
			String title, DialogComponent firstDiaC, boolean horizontal) {
		this(nodeSettingsPane, Optional.ofNullable(title),
				OptionalInt
						.of(horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS),
				firstDiaC);
	}

	/**
	 * Private Full constructor for creation from any of the public constructors
	 * 
	 * @param nodeSettingsPane
	 *            The {@link DefaultNodeSettingsPane} to add the components to
	 * @param title
	 *            The optional title of the bounding box
	 * @param axis
	 *            The axis(e.g. {@code BoxLayout.X_AXIS} or
	 *            {@code BoxLayour.Y_AXIS}
	 * @param firstDiaC
	 *            The first {@link DialogComponent} to go in the grouping
	 */
	private DialogComponentGroup(DefaultNodeSettingsPane nodeSettingsPane,
			Optional<String> title, OptionalInt axis,
			DialogComponent firstDiaC) {
		if (firstDiaC == null) {
			throw new NullPointerException(
					"A non-null DialogComponent must be supplied");
		}
		this.pane = nodeSettingsPane;

		// First we add the dialog component to the NodeSettingsPane - this
		// takes care of settings load/save, and gives us a way into the
		// components
		pane.addDialogComponent(firstDiaC);
		parent = firstDiaC.getComponentPanel().getParent();

		// Now create the panel
		// Start by finding the layout axis
		int layoutAxis;
		if (axis.isPresent()) {
			layoutAxis = axis.getAsInt();
		} else {
			if (parent.getLayout() instanceof BoxLayout) {
				layoutAxis = ((BoxLayout) parent.getLayout()).getAxis();
			} else {
				layoutAxis = DEFAULT_AXIS;
			}
		}

		// And create the border
		Border border = BorderFactory.createEtchedBorder();
		if (title.isPresent()) {
			border = BorderFactory.createTitledBorder(border, title.get());
		}
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, layoutAxis));
		panel.setBorder(border);

		// Add the panel to the dialog
		parent.add(panel);

		// And then add the component into the dialog
		panel.add(firstDiaC.getComponentPanel());
	}

	/**
	 * Private constructor for creation from the
	 * {@link #addSubGroup(DialogComponent, Optional, OptionalInt) method
	 * 
	 * @param pane
	 *            The node settings pane
	 * @param newParent
	 *            The new parent container
	 * @param newPanel
	 *            The new panel
	 */
	private DialogComponentGroup(DefaultNodeSettingsPane pane,
			Container newParent, JPanel newPanel) {
		this.pane = pane;
		this.parent = newParent;
		this.panel = newPanel;
	}

	/**
	 * Method to add a new dialog component to the group
	 * 
	 * @param diaC
	 *            The {@link DialogComponent} to add
	 * @return This group
	 */
	public final DialogComponentGroup addComponent(DialogComponent diaC) {
		if (diaC == null) {
			throw new NullPointerException(
					"A non-null DialogComponent must be supplied");
		}
		pane.addDialogComponent(diaC);
		panel.add(diaC.getComponentPanel());
		return this;
	}

	/**
	 * Method to add new dialog components to the group
	 * 
	 * @param diaC
	 *            The {@link DialogComponent}s to add
	 * @return This group
	 */
	public final DialogComponentGroup addComponents(DialogComponent... diaC) {
		if (diaC == null) {
			throw new NullPointerException(
					"A non-null DialogComponent must be supplied");
		}
		for (DialogComponent dComp : diaC) {
			addComponent(dComp);
		}
		return this;
	}

	/**
	 * Method to create a new {@link DialogComponentGroup} nested within the
	 * current group, with no title and inherited layout
	 * 
	 * @param diaC
	 *            The {@link DialogComponent} to initialised the new group with
	 * @return The new {@link DialogComponentGroup} subgroup
	 */
	public final DialogComponentGroup addSubGroup(DialogComponent diaC) {
		return addSubGroup(diaC, Optional.empty(), OptionalInt.empty());
	}

	/**
	 * Method to create a new {@link DialogComponentGroup} nested within the
	 * current group, with a title and inherited layout
	 * 
	 * @param diaC
	 *            The {@link DialogComponent} to initialised the new group with
	 * @param title
	 *            The title
	 * @return The new {@link DialogComponentGroup} subgroup
	 */
	public final DialogComponentGroup addSubGroup(DialogComponent diaC,
			String title) {
		return addSubGroup(diaC, Optional.ofNullable(title),
				OptionalInt.empty());
	}

	/**
	 * Method to create a new {@link DialogComponentGroup} nested within the
	 * current group, with no title and supplied layout
	 * 
	 * @param diaC
	 *            The {@link DialogComponent} to initialised the new group with
	 * @param horizontal
	 *            The layout orientation
	 * @return The new {@link DialogComponentGroup} subgroup
	 */
	public final DialogComponentGroup addSubGroup(DialogComponent diaC,
			boolean horizontal) {
		return addSubGroup(diaC, Optional.empty(), OptionalInt
				.of(horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
	}

	/**
	 * Method to create a new {@link DialogComponentGroup} nested within the
	 * current group, with a title and supplied layout
	 * 
	 * @param diaC
	 *            The {@link DialogComponent} to initialised the new group with
	 * @param title
	 *            The title
	 * @param horizontal
	 *            The layout orientation
	 * @return The new {@link DialogComponentGroup} subgroup
	 */
	public final DialogComponentGroup addSubGroup(DialogComponent diaC,
			String title, boolean horizontal) {
		return addSubGroup(diaC, Optional.ofNullable(title), OptionalInt
				.of(horizontal ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS));
	}

	private DialogComponentGroup addSubGroup(DialogComponent diaC,
			Optional<String> title, OptionalInt axis) {

		addComponent(diaC);
		Container newParent = diaC.getComponentPanel().getParent();

		// Now create the panel
		// Start by finding the layout axis
		int layoutAxis;
		if (axis.isPresent()) {
			layoutAxis = axis.getAsInt();
		} else {
			if (newParent.getLayout() instanceof BoxLayout) {
				layoutAxis = ((BoxLayout) newParent.getLayout()).getAxis();
			} else {
				layoutAxis = DEFAULT_AXIS;
			}
		}

		// And create the border
		Border border = BorderFactory.createEtchedBorder();
		if (title.isPresent()) {
			border = BorderFactory.createTitledBorder(border, title.get());
		}
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BoxLayout(newPanel, layoutAxis));
		newPanel.setBorder(border);

		// Add the panel to the dialog
		newParent.add(newPanel);

		// And then add the component into the dialog
		newPanel.add(diaC.getComponentPanel());
		return new DialogComponentGroup(this.pane, newParent, newPanel);
	}
}
