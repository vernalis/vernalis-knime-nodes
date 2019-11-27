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
package com.vernalis.knime.plot.nodes.kerneldensity;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.xml.sax.SAXException;

import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDescription;
import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane;
import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeFactory;
import com.vernalis.knime.misc.ArrayUtils;

import static com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_OPTIONS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.X_VALUES_COLUMN;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.Y_VALUES_COLUMN;

/**
 * Node Factory implementation for the 2D Kernel Density Plot node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity2DPlotNodeFactory extends
		AbstractDrawableSeriesPlotNodeFactory<KernelDensity2DPlotNodeModel> {

	public KernelDensity2DPlotNodeFactory() {
		super(new String[] { X_VALUES_COLUMN, Y_VALUES_COLUMN },
				ArrayUtils.of(DOUBLE_VALUE_COLUMN_FILTER, 2));

	}

	@Override
	public KernelDensity2DPlotNodeModel createNodeModel() {
		return new KernelDensity2DPlotNodeModel();
	}

	@Override
	public AbstractDrawableSeriesPlotNodeDialogPane createNodeDialogPane() {
		return new KernelDensityPlotNodeDialogPane(true, true);
	}

	@Override
	protected AbstractDrawableSeriesPlotNodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new AbstractKernelDensityPlotNodeDescription(
				"kernel_density2D.png", "2D Kernel Density Plot",
				KERNEL_OPTIONS, getColNames(), this.getClass());
	}

}
