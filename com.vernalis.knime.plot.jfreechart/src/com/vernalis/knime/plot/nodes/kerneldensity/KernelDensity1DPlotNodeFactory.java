/*******************************************************************************
 * Copyright (c) 2019, 2023, Vernalis (R&D) Ltd
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

import static com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_OPTIONS;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.VALUES_COLUMN;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.xml.sax.SAXException;

import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDialogPane;
import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeFactory;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

/**
 * Node Factory for the 1D Kernel Density Plot node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensity1DPlotNodeFactory extends
		AbstractDrawableSeriesPlotNodeFactory<KernelDensity1DPlotNodeModel> {

	public KernelDensity1DPlotNodeFactory() {
		super(new String[] { VALUES_COLUMN },
				ArrayUtils.of(DOUBLE_VALUE_COLUMN_FILTER, 1));

	}

	@Override
	public KernelDensity1DPlotNodeModel createNodeModel() {
		return new KernelDensity1DPlotNodeModel();
	}

	@Override
	public AbstractDrawableSeriesPlotNodeDialogPane createNodeDialogPane() {
		return new KernelDensityPlotNodeDialogPane(true);
	}

	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new VernalisDelegateNodeDescription(
				new AbstractKernelDensityPlotNodeDescription(
				"kernel_density1D.png", "1D Kernel Density Plot",
						KERNEL_OPTIONS, getColNames(), this.getClass()),
				getClass());
	}

}
