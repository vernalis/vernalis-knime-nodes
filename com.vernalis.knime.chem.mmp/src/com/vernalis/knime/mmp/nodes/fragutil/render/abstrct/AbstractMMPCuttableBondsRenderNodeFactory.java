/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.render.abstrct;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.xml.sax.SAXException;

import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractColumnRearrangerFragmentationFactoryNodeModel;

/**
 * Node Factory class Render cuttable bonds nodes
 * 
 * @author s.roughley
 * 
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPCuttableBondsRenderNodeFactory<T, U>
		extends NodeFactory<AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>> {
	private final Class<? extends FragmentationUtilsFactory<T, U>> fragUtilsFactory;

	/**
	 * Constructor
	 * 
	 * @param fragUtilFactory
	 *            the {@link FragmentationUtilsFactory} instance for the node
	 */
	public AbstractMMPCuttableBondsRenderNodeFactory(
			Class<? extends FragmentationUtilsFactory<T, U>> fragUtilFactory) {
		super(true);
		this.fragUtilsFactory = fragUtilFactory;
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
			return new AbstractMMPCuttableBondsRenderNodeDescription<>(
					fragUtilsFactory.getConstructor().newInstance());
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
	public AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U> createNodeModel() {
		try {
			return new AbstractMMPCuttableBondsRenderNodeModel<>(
					fragUtilsFactory.getConstructor().newInstance());
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
	public NodeView<AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>> createNodeView(
			int viewIndex, AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U> nodeModel) {
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
			return new AbstractMMPMatchingBondsRenderNodeDialog<>(
					fragUtilsFactory.getConstructor().newInstance(),
					"Matching bond highlight colour", true, true);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Error instantiating Fragmentation Utilities factory", e);
		}

	}

}
