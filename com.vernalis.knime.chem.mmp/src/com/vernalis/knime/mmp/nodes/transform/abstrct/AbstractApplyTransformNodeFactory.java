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
package com.vernalis.knime.mmp.nodes.transform.abstrct;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.xml.sax.SAXException;

import com.vernalis.knime.mmp.transform.TransformUtilityFactory;

/**
 * The node factory for the Apply Transforms nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the molecule object
 * @param <U>
 *            The type of the query molecule object
 * @param <V>
 *            The type of the reaction/transform object
 */
public class AbstractApplyTransformNodeFactory<T, U, V>
		extends NodeFactory<AbstractApplyTransformNodeModel<T, U, V>> {

	private final TransformUtilityFactory<T, U, V> transUtilFact;

	/**
	 * Constructor
	 * 
	 * @param transUtilFact
	 *            The {@link TransformUtilityFactory} instance for the node
	 */
	public AbstractApplyTransformNodeFactory(TransformUtilityFactory<T, U, V> transUtilFact) {
		super(true);
		this.transUtilFact = transUtilFact;
		init();
	}

	@Override
	public AbstractApplyTransformNodeModel<T, U, V> createNodeModel() {
		return new AbstractApplyTransformNodeModel<>(transUtilFact);
	}

	@Override
	protected int getNrNodeViews() {
		return 2;
	}

	@Override
	public NodeView<AbstractApplyTransformNodeModel<T, U, V>> createNodeView(int viewIndex,
			AbstractApplyTransformNodeModel<T, U, V> nodeModel) {
		return viewIndex == 0 ? new AbstractApplyTransformProgressNodeView<>(nodeModel)
				: new AbstractApplyTransformProgressNodeView2<>(nodeModel);
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractApplyTransformNodeDialog<>(transUtilFact);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new AbstractApplyTransformNodeDescription<>(transUtilFact);
	}

}
