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
package com.vernalis.knime.nodes.propcalc;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.util.ColumnFilter;
import org.xml.sax.SAXException;

/**
 * Abstract Node Factory implementation for calculated properties nodes
 * 
 * @author s.roughley
 * @param <T>
 *            The NodeModel implementation class
 * @param <U>
 *            The {@link CalculatedPropertyInterface} type parameter
 * 
 */
public abstract class AbstractPropertyCalcNodeFactory<T extends AbstractPropertyCalcNodeModel<U>, U>
		extends NodeFactory<T> {

	private final CalculatedPropertyInterface<U>[] properties;
	private final ColumnFilter acceptedColumnsFilter;
	private final String propertyFilterTitle;
	private final String columnName;
	private final String nodeName;
	private final String iconPath;

	/**
	 * Overloaded constructor for node name same as property dialog name
	 * 
	 * @param columnNameDialogTitle
	 *            The column selector dialog title
	 * @param propertyFilterDialogTitle
	 *            The property dialog component title (and also the node name)
	 * @param iconPath
	 *            The path to the node icon
	 * @param properties
	 *            The properties to calculate
	 * @param acceptedColumnsFilter
	 *            A column filter for the accepted incoming column type(s)
	 */
	public AbstractPropertyCalcNodeFactory(String columnNameDialogTitle,
			String propertyFilterDialogTitle, String iconPath,
			CalculatedPropertyInterface<U>[] properties,
			ColumnFilter acceptedColumnsFilter) {
		this(propertyFilterDialogTitle, columnNameDialogTitle,
				propertyFilterDialogTitle, iconPath, properties,
				acceptedColumnsFilter);
	}

	/**
	 * Full constructor
	 * 
	 * @param nodeName
	 *            The node name
	 * @param columnNameDialogTitle
	 *            The column selector dialog title
	 * @param propertyFilterDialogTitle
	 *            The property dialog component title (and also the node name)
	 * @param iconPath
	 *            The path to the node icon
	 * @param properties
	 *            The properties to calculate
	 * @param acceptedColumnsFilter
	 *            A column filter for the accepted incoming column type(s)
	 */
	public AbstractPropertyCalcNodeFactory(String nodeName,
			String columnNameDialogTitle, String propertyFilterDialogTitle,
			String iconPath, CalculatedPropertyInterface<U>[] properties,
			ColumnFilter acceptedColumnsFilter) {

		super(true);
		this.nodeName = nodeName;
		this.properties = properties;
		this.acceptedColumnsFilter = acceptedColumnsFilter;
		propertyFilterTitle = propertyFilterDialogTitle;
		this.columnName = columnNameDialogTitle;
		this.iconPath = iconPath;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<T> createNodeView(int viewIndex, T nodeModel) {
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new AbstractPropertyCalcNodeDescription(nodeName, columnName,
				propertyFilterTitle, iconPath, properties,
				(Class<? extends NodeFactory<?>>) this.getClass());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractPropertyCalcNodeDialog<>(propertyFilterTitle,
				columnName, properties, acceptedColumnsFilter);
	}

	/**
	 * @return the properties
	 */
	protected CalculatedPropertyInterface<U>[] getProperties() {
		return properties;
	}

	/**
	 * @return the acceptedColumnsFilter
	 */
	protected ColumnFilter getAcceptedColumnsFilter() {
		return acceptedColumnsFilter;
	}

	/**
	 * @return the property chooser name in the dialog
	 */
	protected String getPropertyFilterTitle() {
		return propertyFilterTitle;
	}

	/**
	 * @return the column chooser name in the dialog
	 */
	protected String getColumnName() {
		return columnName;
	}

	/**
	 * @return the node name
	 */
	protected String getFactoryNodeName() {
		return nodeName;
	}

	/**
	 * @return the iconPath
	 */
	protected String getIconPath() {
		return iconPath;
	}

}
