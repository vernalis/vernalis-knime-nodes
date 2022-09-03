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

import java.io.IOException;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.TabDocument.Tab;
import org.xml.sax.SAXException;

import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeDescription;
import com.vernalis.knime.jfcplot.core.nodes.AbstractDrawableSeriesPlotNodeFactory;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.plot.nodes.kerneldensity.AbstractKernelDensityPlotNodeDescription;

import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.DOUBLE_VALUE_COLUMN_FILTER;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI1_I1_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.N_PMI2_I2_I3_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.SHOW_FULL_TRIANGLE;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.TRIANGLE_BOUNDS_COLOUR;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane.VERTEX_LABEL_COLOUR;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeDescription.SHOW_FULL_TRIANGLE_DESCRIPTION;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeDescription.TRIANGLE_BOUND_COLOUR_DESCRIPTION;
import static com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeDescription.VERTEX_LABEL_COLOUR_DESCRIPTION;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.plot.nodes.kerneldensity.KernelConstants.KERNEL_OPTIONS;

public class PMIKernelDensityPlotNodeFactory extends
		AbstractDrawableSeriesPlotNodeFactory<PMIKernelDensityPlotNodeModel> {

	public PMIKernelDensityPlotNodeFactory() {
		super(new String[] { N_PMI1_I1_I3_COLUMN, N_PMI2_I2_I3_COLUMN },
				ArrayUtils.of(DOUBLE_VALUE_COLUMN_FILTER, 2));

	}

	@Override
	public PMIKernelDensityPlotNodeModel createNodeModel() {
		return new PMIKernelDensityPlotNodeModel();
	}

	@Override
	public PMIKernelDensityPlotNodeDialog createNodeDialogPane() {
		return new PMIKernelDensityPlotNodeDialog();
	}

	@Override
	protected AbstractDrawableSeriesPlotNodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new AbstractKernelDensityPlotNodeDescription(
				"kernel_density_pmi.png", "PMI Kernel Density Plot",
				KERNEL_OPTIONS, getColNames(), this.getClass(), false) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
			 * AbstractKernelDensityPlotNodeDescription#getViewDescription(int)
			 */
			@Override
			public String getViewDescription(int index) {
				switch (index) {
					case 0:
						return "View showing the " + getInteractiveViewName();
					default:
						return null;
				}

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
			 * AbstractKernelDensityPlotNodeDescription#getInteractiveViewName()
			 */
			@Override
			public String getInteractiveViewName() {
				return "PMI Triangle Kernel Density Plot";
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
			 * AbstractKernelDensityPlotNodeDescription#createIntroParagraphs(
			 * org.apache.xmlbeans.XmlCursor)
			 */
			@Override
			protected void createIntroParagraphs(XmlCursor introCursor) {
				introCursor.beginElement("p");
				introCursor.insertChars(
						"This node plots a Principal Moments of Intertia (PMI) Triangle ");
				introCursor.beginElement("a");
				introCursor.insertAttributeWithValue("href",
						"https://en.wikipedia.org/wiki/Kernel_(statistics)");
				introCursor.insertChars("Kernel density function");
				introCursor.toEndToken();
				introCursor.toNextToken();
				introCursor.insertChars(
						" based on an incoming data table of normalised Principal Moments of Intertia (nPMI)");
				introCursor.toEndToken();
				introCursor.toNextToken();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
			 * AbstractKernelDensityPlotNodeDescription#getOutportName(int)
			 */
			@Override
			public String getOutportName(int index) {
				switch (index) {
					case 0:
						return getInteractiveViewName();
					default:
						return null;
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
			 * AbstractKernelDensityPlotNodeDescription#getShortDescription()
			 */
			@Override
			protected String getShortDescriptionImpl() {
				return "Node to generate a PMI Triangle Kernel Density Plot";
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.vernalis.knime.internal.pmi.nodes.plot.kerneldensity.
			 * AbstractKernelDensityPlotNodeDescription#
			 * prependAdditionalTabbedOptions(org.knime.node2012.
			 * FullDescriptionDocument.FullDescription)
			 */
			@Override
			protected Tab
					prependAdditionalTabbedOptions(FullDescription fullDesc) {
				// Here we start by adding the superclass options
				Tab tab = super.prependAdditionalTabbedOptions(fullDesc);
				// And now add the PMI specifics
				addOptionToTab(tab, VERTEX_LABEL_COLOUR,
						VERTEX_LABEL_COLOUR_DESCRIPTION);
				addOptionToTab(tab, TRIANGLE_BOUNDS_COLOUR,
						TRIANGLE_BOUND_COLOUR_DESCRIPTION);
				addOptionToTab(tab, SHOW_FULL_TRIANGLE,
						SHOW_FULL_TRIANGLE_DESCRIPTION);
				return tab;
			}

		};
	}

}
