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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.knime.core.node.NodeDialogPane;

import com.vernalis.knime.fingerprint.nodes.abstrct.dialog.AbstractDoubleFingerprintNodeDialog;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractDoubleFingerprintSingleOutNodeModel;
import com.vernalis.knime.misc.EitherOr;

/**
 * Abstract Node Factory for the double fingerprint nodes
 * 
 * @author s.roughley
 *
 * @param <T>
 *            The type of the nodemodel (extends
 *            {@link AbstractDoubleFingerprintSingleOutNodeModel}
 */
public abstract class AbstractDoubleFingerprintNodeFactory<T extends AbstractDoubleFingerprintSingleOutNodeModel>
		extends AbstractSingleFingerprintNodeFactory<T> {
	/**
	 * The default node dialog options for the double fingerprint
	 */
	protected static final EitherOr<Map<String, String>, Map<String, Map<String, String>>> DEFAULT_DOUBLE_FP_OPTIONS;
	static {
		Map<String, String> tmp = new LinkedHashMap<>();
		tmp.put("Select the fingerprint column", "Select the incoming fingerprint column");
		tmp.put("Select the second fingerprint column",
				"Select the second incoming fingerprint column");
		tmp.put("Keep input columns", "Keep the input fingerprint columns in the output table?");
		DEFAULT_DOUBLE_FP_OPTIONS = EitherOr.ofLeft(Collections.unmodifiableMap(tmp));
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
	public AbstractDoubleFingerprintNodeFactory(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, String nodeName, String iconName, String shortDescription,
			String[] longDescriptionParagraphs, String inPortName, String inPortDescripion,
			String outPortName, String outPortDescription,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options) {
		super(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector,
				allowDenseByteVector, nodeName, iconName, shortDescription,
				longDescriptionParagraphs, inPortName, inPortDescripion, outPortName,
				outPortDescription, options);

	}

	/**
	 * Overloaded constructor with default options and non-XML node description
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
	public AbstractDoubleFingerprintNodeFactory(boolean allowSparseBitVector,
			boolean allowDenseBitVector, boolean allowSparseByteVector,
			boolean allowDenseByteVector, String nodeName, String iconName, String shortDescription,
			String[] longDescriptionParagraphs, String inPortName, String inPortDescripion,
			String outPortName, String outPortDescription) {
		this(allowSparseBitVector, allowDenseBitVector, allowSparseByteVector, allowDenseByteVector,
				nodeName, iconName, shortDescription, longDescriptionParagraphs, inPortName,
				inPortDescripion, outPortName, outPortDescription, DEFAULT_DOUBLE_FP_OPTIONS);

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
	public AbstractDoubleFingerprintNodeFactory(String nodeName, String iconName,
			String shortDescription, String[] longDescriptionParagraphs, String inPortName,
			String inPortDescripion, String outPortName, String outPortDescription,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options) {
		super(nodeName, iconName, shortDescription, longDescriptionParagraphs, inPortName,
				inPortDescripion, outPortName, outPortDescription, options);

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
	public AbstractDoubleFingerprintNodeFactory(String nodeName, String iconName,
			String shortDescription, String[] longDescriptionParagraphs, String inPortName,
			String inPortDescripion, String outPortName, String outPortDescription) {
		super(nodeName, iconName, shortDescription, longDescriptionParagraphs, inPortName,
				inPortDescripion, outPortName, outPortDescription);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.fingerprint.nodes.abstrct.
	 * AbstractSingleFingerprintNodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new AbstractDoubleFingerprintNodeDialog(allowSparseBitVector, allowDenseBitVector,
				allowSparseByteVector, allowDenseByteVector);
	}

}
