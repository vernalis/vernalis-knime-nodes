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
package com.vernalis.knime.chem.pmi.nodes.plot.convexhull;

import java.io.IOException;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.TabDocument.Tab;
import org.xml.sax.SAXException;

import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesNodeDialogPane;
import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeDescription;
import com.vernalis.knime.chem.pmi.nodes.plot.abstrct.AbstractPMIDrawableSeriesPlotNodeFactory;

import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.GROUPING_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.HULL_NPMI1_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.HULL_NPMI2_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.HULL_OPTIONS;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.HULL_TRANSPARENCY;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.SHOW_HULL_BOUNDRIES;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.SHOW_LEGEND;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.SHOW_RELATIVE_HULL_AREA_ON_LEGEND;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.THRESHOLD_FOR_AREA_TO_SHOW_HULL_POINTS;
import static com.vernalis.knime.chem.pmi.nodes.plot.convexhull.PMIHullPlotNodeDialogPane.USE_GREYSCALE_COLOUR_PALETTE_IF_NO_COLOUR_MODEL_AVAILABLE;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.createTab;

/**
 * The Node Factory class for the PMI Triangle Convex Hull node
 * 
 * @author s.roughley
 *
 */
public class PMIHullPlotNodeFactory
		extends AbstractPMIDrawableSeriesPlotNodeFactory<PMIHullPlotNodeModel> {

	@Override
	public PMIHullPlotNodeModel createNodeModel() {
		return new PMIHullPlotNodeModel();
	}

	@Override
	public AbstractPMIDrawableSeriesNodeDialogPane createNodeDialogPane() {
		return new PMIHullPlotNodeDialogPane();
	}

	@Override
	protected AbstractPMIDrawableSeriesPlotNodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {

		return new AbstractPMIDrawableSeriesPlotNodeDescription("PMI_Hull.png",
				"PMI Triangle Convex Hull Plot",
				PMIHullPlotNodeFactory.this.getClass(),
				"PMI Table for Convex Hulls") {

			@Override
			public String getViewDescription(int index) {
				if (index == 0) {
					return "PMI Convex Hull plot with optional scatter overlay";
				}
				return null;
			}

			@Override
			protected void prependAdditionalTabbedOptions(
					FullDescription fullDesc) {
				Tab t = createTab(fullDesc, HULL_OPTIONS);
				addOptionToTab(t, HULL_NPMI1_COLUMN,
						"The column containing the normalised-PMI1 (nPMI1, npr1) "
								+ "values for the convex hull(s)");
				addOptionToTab(t, HULL_NPMI2_COLUMN,
						"The column containing the normalised-PMI2 (nPMI2, npr2) "
								+ "values for the convex hull(s)");
				addOptionToTab(t, GROUPING_COLUMN,
						"The optional column containing the group IDs. "
								+ "If no column is selected, all points are included in a single hull");
				addOptionToTab(t, SHOW_HULL_BOUNDRIES,
						"Should a solid bounding line be drawn round each hull");
				addOptionToTab(t, HULL_TRANSPARENCY,
						"The transparency of the fill (0 = no fill; 255 = solid fill)");
				addOptionToTab(t,
						USE_GREYSCALE_COLOUR_PALETTE_IF_NO_COLOUR_MODEL_AVAILABLE,
						"If no colour model for the grouping column is available, "
								+ "then a rainbow colour palette is created. "
								+ "This option allows greyscale in place of colours");
				addOptionToTab(t, SHOW_LEGEND,
						"Show the colour legend for the hulls");
				addOptionToTab(t, SHOW_RELATIVE_HULL_AREA_ON_LEGEND,
						"Should the legend include the area relative to the total PMI triangle");
				addOptionToTab(t, THRESHOLD_FOR_AREA_TO_SHOW_HULL_POINTS,
						"Hulls smaller than this area will have their "
								+ "points plotted to enhance visibility");
			}

			@Override
			protected String getInputPortDescription(int index) {
				if (index == 0) {
					return "A table of normalised PMI values with an optional 'grouping' column. "
							+ "Each group is plotted as a convex hull on the PMI Triangle";
				}
				return null;
			}

			@Override
			protected void addNodeDescription(XmlCursor introCursor) {
				introCursor.insertElementWithText("p",
						"This node generates a PMI plot with the points in convex hulls "
								+ "according to an optional grouping column");
			}
		};
	}

}
