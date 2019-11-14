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
package com.vernalis.knime.jfcplot.core.nodes;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.util.ColumnFilter;
import org.knime.ext.jfc.node.base.JfcBaseNodeView;
import org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel;
import org.xml.sax.SAXException;

/**
 * Base {@link NodeFactory} implementation for Plot nodes. NB Not a subclass of
 * JfcGenericBaseNodeFactory as this requires the lazy instantiation constructor
 * which is not available in the latter class
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the {@link AbstractDrawableSeriesPlotNodeModel}
 */
public abstract class AbstractDrawableSeriesPlotNodeFactory<T extends AbstractDrawableSeriesPlotNodeModel<?>>
		extends NodeFactory<JfcGenericBaseNodeModel> {

	private final String[] colNames;
	private final ColumnFilter[] colFilters;

	public AbstractDrawableSeriesPlotNodeFactory(String[] colNames,
			ColumnFilter[] columnFilters) {
		super(true);
		this.colNames = colNames;
		this.colFilters = columnFilters;
		init();
	}

	/**
	 * @return the colNames
	 */
	protected final String[] getColNames() {
		return colNames;
	}

	/**
	 * @return the colFilters
	 */
	protected final ColumnFilter[] getColFilters() {
		return colFilters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public JfcBaseNodeView createNodeView(int viewIndex,
			JfcGenericBaseNodeModel nodeModel) {
		// NB AbstractDrawableSeriesPlotNodeModel extends
		// JfcGenericBaseNodeModel so this is ok even though looks a bit odd
		if (viewIndex == 0) {
			return new JfcBaseNodeView(nodeModel);
		} else {
			throw new IllegalStateException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	public abstract T createNodeModel();

	@Override
	public abstract AbstractDrawableSeriesPlotNodeDialogPane createNodeDialogPane();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected abstract AbstractDrawableSeriesPlotNodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException;

}
