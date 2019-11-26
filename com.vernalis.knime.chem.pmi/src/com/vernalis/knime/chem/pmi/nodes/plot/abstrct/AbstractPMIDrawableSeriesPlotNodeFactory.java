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
package com.vernalis.knime.chem.pmi.nodes.plot.abstrct;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeFactory;
import org.knime.ext.jfc.node.base.JfcBaseNodeFactory;
import org.xml.sax.SAXException;

/**
 * Base {@link NodeFactory} implementation for PMI Plot nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the {@link AbstractPMIDrawableSeriesPlotNodeModel}
 */
public abstract class AbstractPMIDrawableSeriesPlotNodeFactory<T extends AbstractPMIDrawableSeriesPlotNodeModel<?>>
		extends JfcBaseNodeFactory {

	public AbstractPMIDrawableSeriesPlotNodeFactory() {

	}

	@Override
	public abstract T createNodeModel();

	@Override
	public abstract AbstractPMIDrawableSeriesNodeDialogPane createNodeDialogPane();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected abstract AbstractPMIDrawableSeriesPlotNodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException;

}
