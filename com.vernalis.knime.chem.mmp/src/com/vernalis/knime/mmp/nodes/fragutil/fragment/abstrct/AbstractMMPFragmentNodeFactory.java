/*******************************************************************************
 * Copyright (c) 2017, 2018 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.nodes.fragutil.fragment.abstrct;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.xml.sax.SAXException;

import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;

/**
 * Node factory for the MMP Fragment nodes
 * <p>
 * Added version argument to allow version 3 nodes to redirect here and
 * correctly convert settings to maintain behaviour (SDR, 11-May-2018)
 * </p>
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public abstract class AbstractMMPFragmentNodeFactory<T, U>
		extends NodeFactory<AbstractMMPFragmentNodeModel<T, U>> {

	protected boolean isMulticut;
	protected Class<? extends FragmentationUtilsFactory<T, U>> fragUtilsFactory;
	protected int version;

	/**
	 * Constructor. The values of the arguments are available to subclasses in
	 * fields of the same names
	 * 
	 * @param isMulticut
	 *            Is the node a 'multi-cut' node (i.e. performs 1-n cuts), or
	 *            not (i.e. performs only n cuts)?
	 * @param fragUtilsFactory
	 *            The class of the {@link FragmentationUtilsFactory} - we need
	 *            class as each NodeFactory is effectively a singleton, and so
	 *            we need to instantiate a new instance of the
	 *            {@link FragmentationUtilsFactory} for each Node instance
	 *            otherwise we run into issues when multiple node instances are
	 *            running
	 */
	public AbstractMMPFragmentNodeFactory(boolean isMulticut,
			Class<? extends FragmentationUtilsFactory<T, U>> fragUtilsFactory,
			int version) {
		super(true);
		this.isMulticut = isMulticut;
		this.fragUtilsFactory = fragUtilsFactory;
		this.version = version;
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
			return new AbstractMMPFragmentNodeDescription<>(
					fragUtilsFactory.getConstructor().newInstance(), isMulticut,
					version);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"Error instantiating Fragmentation Utilities factory", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public AbstractMMPFragmentNodeModel<T, U> createNodeModel() {
		try {
			return new AbstractMMPFragmentNodeModel<>(isMulticut,
					fragUtilsFactory.getConstructor().newInstance(), version);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"Error instantiating Fragmentation Utilities factory", e);
		}
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
	public NodeView<AbstractMMPFragmentNodeModel<T, U>> createNodeView(
			int viewIndex, AbstractMMPFragmentNodeModel<T, U> nodeModel) {
		return new AbstractMMPFragmentProgressNodeView<>(nodeModel);
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
			return new AbstractMMPFragmentNodeDialog<>(
					fragUtilsFactory.getConstructor().newInstance(), isMulticut,
					version);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"Error instantiating Fragmentation Utilities factory", e);
		}
	}

}
