/*******************************************************************************
 * Copyright (c) 2017, 2023, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.filter.abstrct;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.xml.sax.SAXException;

import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

/**
 * Abstract Node Factory for Matched-Molecular Pair Filter/Splitter nodes
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public abstract class AbstractMMPFragmentationFilterSplitterNodeFactory<T, U>
		extends NodeFactory<AbstractMMPFragmentationFilterSplitterNodeModel<T, U>> {
	protected Class<? extends FragmentationUtilsFactory<T, U>> fragUtilsFactory;
	private boolean isSplitter;

	/**
	 * Constructor. The values of the arguments are available to subclasses in
	 * fields of the same names
	 * 
	 * @param fragUtilsFactory
	 *            the {@link FragmentationUtilsFactory} instance for the node
	 * @param isSplitter
	 *            Is the node a splitter?
	 */
	public AbstractMMPFragmentationFilterSplitterNodeFactory(
			Class<? extends FragmentationUtilsFactory<T, U>> fragUtilsFactory, boolean isSplitter) {
		super(true);
		this.fragUtilsFactory = fragUtilsFactory;
		this.isSplitter = isSplitter;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		try {
			return new VernalisDelegateNodeDescription(
					new AbstractMMPFragmentationFilterSplitterNodeDescription<>(
							fragUtilsFactory.getConstructor().newInstance(),
							isSplitter),
					getClass());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Error instantiating Fragmentation Utilities factory", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public AbstractMMPFragmentationFilterSplitterNodeModel<T, U> createNodeModel() {
		try {
			return new AbstractMMPFragmentationFilterSplitterNodeModel<>(
					fragUtilsFactory.getConstructor().newInstance(), isSplitter);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Error instantiating Fragmentation Utilities factory", e);
		}
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
	public NodeView<AbstractMMPFragmentationFilterSplitterNodeModel<T, U>> createNodeView(
			int viewIndex, AbstractMMPFragmentationFilterSplitterNodeModel<T, U> nodeModel) {
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
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		try {
			return new AbstractMMPFragmentationFactoryNodeDialog<>(
					fragUtilsFactory.getConstructor().newInstance(), false, false);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Error instantiating Fragmentation Utilities factory", e);
		}

	}

}
