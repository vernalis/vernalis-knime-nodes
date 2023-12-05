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
package com.vernalis.knime.mmp.nodes.fragutil.maxcuts.abstrct;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.xmlbeans.XmlException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.xml.sax.SAXException;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.ClosedFactoryException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;
import com.vernalis.knime.mmp.fragutils.FragmentationUtilsFactory;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractColumnRearrangerFragmentationFactoryNodeModel;
import com.vernalis.knime.mmp.nodes.fragutil.abstrct.AbstractMMPFragmentationFactoryNodeDialog;
import com.vernalis.knime.nodes.VernalisDelegateNodeDescription;

/**
 * Node Factory class for the Max Number of Cuts nodes
 * 
 * @author s.roughley
 * @param <T>
 *            The molecule type parameter
 * @param <U>
 *            The matcher type parameter
 */
public class AbstractMMPMaxCutsNodeFactory<T, U> extends
		NodeFactory<AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>> {

	private final Class<? extends FragmentationUtilsFactory<T, U>> fragUtilsFactory;
	private static final String[] newColNames = new String[] { "Maximum Cuts" };
	private static final DataType[] newColTypes =
			new DataType[] { IntCell.TYPE };

	/**
	 * Constructor
	 * 
	 * @param fragUtilFactory
	 *            The {@link FragmentationUtilsFactory} instance for the node
	 */
	public AbstractMMPMaxCutsNodeFactory(
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
			return new VernalisDelegateNodeDescription(
					new AbstractMMPMaxCutsNodeDescription<>(
							fragUtilsFactory.getConstructor().newInstance()),
					getClass());
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
	public AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>
			createNodeModel() {
		// Dont add Hs to the fragmentationFractory by default
		try {
			return new AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>(
					fragUtilsFactory.getConstructor().newInstance(), false,
					false, false, false, newColNames, newColTypes) {

				@Override
				protected DataCell[] getResultColumns(T mol,
						MoleculeFragmentationFactory2<T, U> fragFactory,
						long rowIndex, int length)
						throws RowExecutionException {
					int retVal = 0;
					try {
						retVal = fragFactory.getMaximumNumberOfCuts(
								m_allowTwoCutsToBondValue.getBooleanValue());
						if (retVal < 1 && m_AddHs.getBooleanValue()) {
							// See if adding Hs helps
							fragFactory.close();
							fragFactory = fragUtilityFactory
									.createHAddedFragmentationFactory(mol,
											matcher, false, verboseLogging,
											false, 0, 0, 0.0,
											rowIndex);
							if (fragFactory.canCutNTimes(1, false)) {
								retVal = 1;
							}
						}
					} catch (ClosedFactoryException | ToolkitException e) {
						throw new RowExecutionException("Error evaluting row",
								e);
					}
					return new IntCell[] { new IntCell(retVal) };
				}
			};
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
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U>>
			createNodeView(int viewIndex,
					AbstractColumnRearrangerFragmentationFactoryNodeModel<T, U> nodeModel) {
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
					fragUtilsFactory.getConstructor().newInstance(), false,
					false, false);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"Error instantiating Fragmentation Utilities factory", e);
		}
	}

}
