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
package com.vernalis.knime.chem.pmi.nodes.plot.scatter;

import java.io.IOException;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.node.v41.FullDescription;
import org.xml.sax.SAXException;

import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane;
import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeDescription;
import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeFactory;
import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeModel;
import com.vernalis.knime.jfcplot.core.drawabledataobject.SimpleShapeDrawableDataObject;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

/**
 * NodeFactory class for the simple PMI Triangle Scatter plot node
 * 
 * @author s.roughley
 *
 */
public class PMIScatterPlotNodeFactory extends
		AbstractPMIDrawableSeriesPlotNodeFactory<AbstractPMIDrawableSeriesPlotNodeModel<SimpleShapeDrawableDataObject>> {

	@Override
	public AbstractPMIDrawableSeriesPlotNodeModel<SimpleShapeDrawableDataObject> createNodeModel() {
		return new AbstractPMIDrawableSeriesPlotNodeModel<>();
	}

	@Override
	public AbstractPMIDrawableSeriesNodeDialogPane createNodeDialogPane() {
		return new AbstractPMIDrawableSeriesNodeDialogPane();
	}

	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {

		return new VernalisDelegateNodeDescription(
				new AbstractPMIDrawableSeriesPlotNodeDescription(
						"PMI_icon2.png",
				"PMI Triangle Scatter Plot",
				PMIScatterPlotNodeFactory.this.getClass()) {

			@Override
			protected void prependAdditionalTabbedOptions(
					FullDescription fullDesc) {
				// Nothing to do

			}

			@Override
			protected void addNodeDescription(XmlCursor introCursor) {
				introCursor.insertElementWithText("p",
						"This node plots a basic PMI Triangle scatter plot, "
								+ "in which each input row is represented by a symbol "
								+ "on the plot within the PMI Bounds box.  The symbol "
								+ "shape, size and colour can be controlled by the "
								+ "appropriate 'Manager' nodes applied to the input table.");
			}

			@Override
			protected String getInputPortDescription(int index) {
				// Nothing to do - only the scatter port
				return null;
			}

			@Override
			public String getViewDescription(int index) {
				if (index == 0) {
					return "PMI Triangle scatter plot";
				}
				return null;
			}
				}, getClass());
	}

}
