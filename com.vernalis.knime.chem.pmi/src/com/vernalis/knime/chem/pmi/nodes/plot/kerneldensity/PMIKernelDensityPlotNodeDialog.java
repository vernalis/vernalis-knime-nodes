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
package com.vernalis.knime.chem.pmi.nodes.plot.kerneldensity;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;

import com.vernalis.knime.dialog.components.DialogComponentGroup;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityPlotNodeDialogPane;

import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI1_I1_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI2_I2_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SHOW_FULL_TRIANGLE;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.TRIANGLE_BOUNDS_COLOUR;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.VERTEX_LABEL_COLOUR;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.createShowFullTriangleModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getTriangleColourModel;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.getVertexColourModel;

public class PMIKernelDensityPlotNodeDialog
		extends KernelDensityPlotNodeDialogPane {

	public PMIKernelDensityPlotNodeDialog() {
		super(N_PMI1_I1_I3_COLUMN, N_PMI2_I2_I3_COLUMN, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
	 * KernelDensityPlotNodeDialogPane#addAdditionalPlotOptions()
	 */
	@Override
	protected void addAdditionalPlotOptions() {
		setHorizontalPlacement(false);
		DialogComponentGroup boundsTriangleGroup =
				new DialogComponentGroup(this, "PMI Bounds Triangle",
						new DialogComponentColorChooser(getVertexColourModel(),
								VERTEX_LABEL_COLOUR, true),
						true);

		boundsTriangleGroup.addComponent(new DialogComponentColorChooser(
				getTriangleColourModel(), TRIANGLE_BOUNDS_COLOUR, true));
		boundsTriangleGroup.addComponent(new DialogComponentBoolean(
				createShowFullTriangleModel(), SHOW_FULL_TRIANGLE));

	}

}
