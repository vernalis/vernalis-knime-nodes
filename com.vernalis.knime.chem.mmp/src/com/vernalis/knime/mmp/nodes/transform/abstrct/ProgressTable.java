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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;

import org.knime.chem.types.SmartsCellFactory;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.renderer.DataValueRenderer;
import org.knime.core.data.renderer.DataValueRendererFamily;

/**
 * A simple JTable implementation which has only 3 columns, and which specifies
 * a renderer factory for the smarts column. Only the Transform column can be
 * resized, and the cells cannot be selected nor the columns rearranged
 * 
 * @author s.roughley
 *
 */
class ProgressTable extends JTable {

	private static final long serialVersionUID = 1L;

	/** {@link DataColumnSpec} of the Transform Column for the renderer */
	static final DataColumnSpec TRANSFORM_COL_SPEC =
			new DataColumnSpecCreator("rSMARTS", SmartsCellFactory.TYPE).createSpec();
	private DataValueRendererFamily rendererFamily;

	private List<String> rendererNames;

	private String preferredRenderer;

	/**
	 * Constructor
	 * 
	 * @param dm
	 *            The {@link ProgressTableModel} datamodel
	 */
	public ProgressTable(ProgressTableModel dm) {
		super(dm);
		setTableHeader(new ProgressTableHeader(getColumnModel()));
		getTableHeader().setTable(this);
		setCellSelectionEnabled(false);
		setPreferredTransformRenderer();
		getColumn("Progress").setCellRenderer(new ProgressRenderer());
		getColumn("Transform").setPreferredWidth(200);
		setRowHeight(100);
		setAutoCreateColumnsFromModel(false);
		getTableHeader().setReorderingAllowed(false);
		getColumn("Transform Name").setResizable(false);
		getColumn("Transform Name").setResizable(false);
		getColumn("Transform Name").setResizable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#getTableHeader()
	 */
	@Override
	public ProgressTableHeader getTableHeader() {
		return (ProgressTableHeader) super.getTableHeader();
	}

	@SuppressWarnings("deprecation")
	private void setPreferredTransformRenderer() {
		rendererFamily = TRANSFORM_COL_SPEC.getType().getRenderer(TRANSFORM_COL_SPEC);
		rendererNames = Arrays.asList(rendererFamily.getRendererDescriptions());
		preferredRenderer = TRANSFORM_COL_SPEC.getProperties()
				.getProperty(DataValueRenderer.PROPERTY_PREFERRED_RENDERER);
		if (!setRendererName(preferredRenderer)) {
			for (String renderer : rendererNames) {
				if (setRendererName(renderer)) {
					break;
				}
			}
		}
		getColumn("Transform").setCellRenderer(rendererFamily);
	}

	/**
	 * Set the renderer for the transform column
	 * 
	 * @param renderer
	 *            The renderer name
	 * @return <code>true</code> if the name was applied
	 */
	public boolean setRendererName(String renderer) {
		if (rendererNames.contains(renderer)
				&& rendererFamily.accepts(renderer, TRANSFORM_COL_SPEC)) {
			rendererFamily.setActiveRenderer(renderer);
			repaint();
			return true;
		}
		return false;
	}

	/**
	 * @return The renderer family for the SMARTS cells
	 */
	public DataValueRendererFamily getSMARTSRendererFamily() {
		return rendererFamily;
	}

	/**
	 * @return The list of names for the SMARTS cells
	 */
	public List<String> getSMARTSRendererNames() {
		return Collections.unmodifiableList(rendererNames);
	}

	/**
	 * @return The preferred renderer name for the SMARTS cells
	 */
	public String getPreferredSMARTSRendererName() {
		return preferredRenderer;
	}

	/**
	 * Method to set the aspect ratio of the SMARTS column
	 * 
	 * @param aspectRatio
	 *            The aspect ratio
	 */
	public void setAspectRatio(double aspectRatio) {
		getTableHeader().aspectRatio = aspectRatio;
		applyAspectRatio();
	}

	/**
	 * 
	 */
	public void applyAspectRatio() {
		setRowHeight((int) (getColumn("Transform").getWidth() / getTableHeader().aspectRatio));
		repaint();
	}

}
