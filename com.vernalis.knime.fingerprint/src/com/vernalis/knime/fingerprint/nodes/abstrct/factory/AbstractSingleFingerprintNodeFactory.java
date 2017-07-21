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
package com.vernalis.knime.fingerprint.nodes.abstrct.factory;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.xml.sax.SAXException;

import com.vernalis.knime.fingerprint.nodes.abstrct.desc.AbstractSinglePortFingerprintNodeDescription;
import com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractSingleFingerprintNodeDialog;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintNodeModel;
import com.vernalis.knime.internal.misc.EitherOr;

/**
 * Abstract Node Factory class implementation for Single Fingerprint nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the Node Model (extends
 *            {@link AbstractSingleFingerprintNodeModel}
 */
public abstract class AbstractSingleFingerprintNodeFactory<T extends AbstractSingleFingerprintNodeModel>
		extends NodeFactory<T> {

	protected final boolean allowSparseBitVector;
	protected final boolean allowDenseBitVector;
	protected final boolean allowSparseByteVector;
	protected final boolean allowDenseByteVector;
	protected final String nodeName;
	protected final String iconName;
	protected final String shortDescription;
	protected final String[] longDescriptionParagraphs;
	protected final String inPortName;
	protected final String inPortDescripion;
	protected final String outPortName;
	protected final String outPortDescription;
	protected final EitherOr<Map<String, String>, Map<String, Map<String, String>>> options;

	/**
	 * The default dialog options for a single fingerprint node
	 */
	protected static final EitherOr<Map<String, String>, Map<String, Map<String, String>>> DEFAULT_SINGLE_FP_OPTIONS;
	static {
		Map<String, String> tmp = new LinkedHashMap<>();
		tmp.put("Select the fingerprint column", "Select the incoming fingerprint column");
		tmp.put("Keep input columns", "Keep the input fingerprint column in the output table?");
		DEFAULT_SINGLE_FP_OPTIONS = EitherOr.ofLeft(Collections.unmodifiableMap(tmp));
	}

	/**
	 * Constructor with control of all options.
	 * 
	 * @param allowSparseBitVector
	 *            Are sparse bit vectors accepted inputs
	 * @param allowDenseBitVector
	 *            Are dense bit vectors accepted inputs
	 * @param allowSparseByteVector
	 *            Are sparse byte vectors accepted inputs
	 * @param allowDenseByteVector
	 *            Are dense byte vectors accepted inputs
	 * @param nodeName
	 *            The name of the node
	 * @param iconName
	 *            The name of the file for the node icon. The file will be
	 *            looked for first in the class resource folder. If this fails
	 *            the file will be looked for in the bundle, with any names
	 *            starting 'com.vernalis' replaced with 'src/com/vernalis'.
	 *            Finally, the default options in the resource folder for this
	 *            abstract implementation will be used
	 * @param shortDescription
	 *            The short description paragraph
	 * @param longDescriptionParagraphs
	 *            The long description paragraphs
	 * @param inPortName
	 *            The names of the input port
	 * @param inPortDescription
	 *            The descriptions of the input port
	 * @param outPortName
	 *            The names of the output port
	 * @param outPortDescription
	 *            The descriptions of the output port
	 * @param options
	 *            The options to be added to the node description. Use an
	 *            {@link EitherOr#ofLeft(Object)} for a map of options added
	 *            directly to the description or an
	 *            {@link EitherOr#ofRight(Object)} for a map of maps of options
	 *            to be added tab-wise
	 */
	public AbstractSingleFingerprintNodeFactory(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, String nodeName, String iconName, String shortDescription,
			String[] longDescriptionParagraphs, String inPortName, String inPortDescripion,
			String outPortName, String outPortDescription,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options) {
		super(true);
		this.allowSparseBitVector = allowSparseBitVector;
		this.allowDenseBitVector = allowDenseBitVector;
		this.allowSparseByteVector = allowSparseByteVector;
		this.allowDenseByteVector = allowDenseByteVector;
		this.nodeName = nodeName;
		if (iconName != null) {
			final URL url = getClass().getResource(iconName);
			if (url != null) {
				this.iconName = url.getFile();
			} else {
				this.iconName = iconName;
			}
		} else {
			this.iconName = null;
		}
		this.shortDescription = shortDescription;
		this.longDescriptionParagraphs = longDescriptionParagraphs;
		this.inPortName = inPortName;
		this.inPortDescripion = inPortDescripion;
		this.outPortName = outPortName;
		this.outPortDescription = outPortDescription;
		this.options = options;
		init();
	}

	/**
	 * Overloaded constructor for a non-XML node description implementation with
	 * all fingerprint types accepted and default options supplied (Fingerprint
	 * columns and keep input column)
	 * 
	 * @param nodeName
	 *            The name of the node
	 * @param iconName
	 *            The name of the file for the node icon. The file will be
	 *            looked for first in the class resource folder. If this fails
	 *            the file will be looked for in the bundle, with any names
	 *            starting 'com.vernalis' replaced with 'src/com/vernalis'.
	 *            Finally, the default options in the resource folder for this
	 *            abstract implementation will be used
	 * @param shortDescription
	 *            The short description paragraph
	 * @param longDescriptionParagraphs
	 *            The long description paragraphs
	 * @param inPortName
	 *            The names of the input port
	 * @param inPortDescription
	 *            The descriptions of the input port
	 * @param outPortName
	 *            The names of the output port
	 * @param outPortDescription
	 *            The descriptions of the output port
	 */
	public AbstractSingleFingerprintNodeFactory(String nodeName, String iconName,
			String shortDescription, String[] longDescriptionParagraphs, String inPortName,
			String inPortDescripion, String outPortName, String outPortDescription) {
		this(true, true, true, true, nodeName, iconName, shortDescription,
				longDescriptionParagraphs, inPortName, inPortDescripion, outPortName,
				outPortDescription, DEFAULT_SINGLE_FP_OPTIONS);
	}

	/**
	 * Overloaded constructor for a non-XML node description implementation with
	 * all fingerprint types accepted
	 * 
	 * @param nodeName
	 *            The name of the node
	 * @param iconName
	 *            The name of the file for the node icon. The file will be
	 *            looked for first in the class resource folder. If this fails
	 *            the file will be looked for in the bundle, with any names
	 *            starting 'com.vernalis' replaced with 'src/com/vernalis'.
	 *            Finally, the default options in the resource folder for this
	 *            abstract implementation will be used
	 * @param shortDescription
	 *            The short description paragraph
	 * @param longDescriptionParagraphs
	 *            The long description paragraphs
	 * @param inPortName
	 *            The names of the input port
	 * @param inPortDescription
	 *            The descriptions of the input port
	 * @param outPortName
	 *            The names of the output port
	 * @param outPortDescription
	 *            The descriptions of the output port
	 * @param options
	 *            The options to be added to the node description. Use an
	 *            {@link EitherOr#ofLeft(Object)} for a map of options added
	 *            directly to the description or an
	 *            {@link EitherOr#ofRight(Object)} for a map of maps of options
	 *            to be added tab-wise
	 */
	public AbstractSingleFingerprintNodeFactory(String nodeName, String iconName,
			String shortDescription, String[] longDescriptionParagraphs, String inPortName,
			String inPortDescripion, String outPortName, String outPortDescription,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options) {
		this(true, true, true, true, nodeName, iconName, shortDescription,
				longDescriptionParagraphs, inPortName, inPortDescripion, outPortName,
				outPortDescription, options);
	}

	/**
	 * Overloaded constructor allowing control of all Node Description and
	 * fingerprint options, except that the default options are supplied
	 * (Fingerprint column and keep input column)
	 * 
	 * @param allowSparseBitVector
	 *            Are sparse bit vectors accepted inputs
	 * @param allowDenseBitVector
	 *            Are dense bit vectors accepted inputs
	 * @param allowSparseByteVector
	 *            Are sparse byte vectors accepted inputs
	 * @param allowDenseByteVector
	 *            Are dense byte vectors accepted inputs
	 * @param nodeName
	 *            The name of the node
	 * @param iconName
	 *            The name of the file for the node icon. The file will be
	 *            looked for first in the class resource folder. If this fails
	 *            the file will be looked for in the bundle, with any names
	 *            starting 'com.vernalis' replaced with 'src/com/vernalis'.
	 *            Finally, the default options in the resource folder for this
	 *            abstract implementation will be used
	 * @param shortDescription
	 *            The short description paragraph
	 * @param longDescriptionParagraphs
	 *            The long description paragraphs
	 * @param inPortName
	 *            The names of the input port
	 * @param inPortDescription
	 *            The descriptions of the input port
	 * @param outPortName
	 *            The names of the output port
	 * @param outPortDescription
	 *            The descriptions of the output port
	 */
	public AbstractSingleFingerprintNodeFactory(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, String nodeName, String iconName, String shortDescription,
			String[] longDescriptionParagraphs, String inPortName, String inPortDescripion,
			String outPortName, String outPortDescription) {
		this(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector, allowDenseByteVector,
				nodeName, iconName, shortDescription, longDescriptionParagraphs, inPortName,
				inPortDescripion, outPortName, outPortDescription, DEFAULT_SINGLE_FP_OPTIONS);
	}

	/**
	 * Overloaded constructor allowing control of input fingerprint types and
	 * requiring an XML-based node description
	 * 
	 * @param allowSparseBitVector
	 *            Does the node accept Sparse Bit Vector columns
	 * @param allowDenseBitVector
	 *            Does the node accept Dense Bit Vector columns
	 * @param allowSparseByteVector
	 *            Does the node accept Sparse Byte Vector columns
	 * @param allowDenseByteVector
	 *            Does the node accept Dense Byte Vector columns
	 */
	public AbstractSingleFingerprintNodeFactory(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector) {
		this(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector, allowDenseByteVector,
				null, null, null, null, null, null, null, null);
	}

	/**
	 * Overloaded constructor for a node accepting any input fingerprint type,
	 * and requiring an XML-based node description
	 */
	public AbstractSingleFingerprintNodeFactory() {
		this(true, true, true, true);
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	public NodeView<T> createNodeView(int viewIndex, T nodeModel) {
		return null;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractSingleFingerprintNodeDialog(allowSparseBitVector, allowDenseBitVector,
				allowSparseByteVector, allowDenseByteVector);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public T createNodeModel() {
		return createNodeModel(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector,
				allowDenseByteVector);
	}

	protected abstract T createNodeModel(boolean allowSparseBitVector2,
			boolean allowDenseBitVector2, boolean allowSparseByteVector2,
			boolean allowDenseByteVector2);

	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		if (nodeName == null) {
			// Try to use the XML
			return super.createNodeDescription();
		}
		return new AbstractSinglePortFingerprintNodeDescription(nodeName, allowSparseBitVector,
				allowDenseBitVector, allowSparseByteVector, allowDenseByteVector, iconName,
				shortDescription, longDescriptionParagraphs, inPortName, inPortDescripion,
				outPortName, outPortDescription, options);
	}
}
