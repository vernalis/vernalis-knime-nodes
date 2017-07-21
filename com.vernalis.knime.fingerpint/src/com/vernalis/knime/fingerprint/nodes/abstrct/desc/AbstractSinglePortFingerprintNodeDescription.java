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
package com.vernalis.knime.fingerprint.nodes.abstrct.desc;

import java.util.Map;

import com.vernalis.knime.internal.misc.EitherOr;

/**
 * A convenience type to allow port names and descriptions to be passed in as
 * objects rather than wrapped in arrays first for a 1->1 port node
 * 
 * @author s.roughley
 *
 */
public class AbstractSinglePortFingerprintNodeDescription
		extends AbstractMultiPortFingerprintNodeDescription {
	/**
	 * Constructor
	 * 
	 * @param nodeName
	 *            The name of the node
	 * @param allowSparseBitVector
	 *            Are sparse bit vectors accepted inputs
	 * @param allowDenseBitVector
	 *            Are dense bit vectors accepted inputs
	 * @param allowSparseByteVector
	 *            Are sparse byte vectors accepted inputs
	 * @param allowDenseByteVector
	 *            Are dense byte vectors accepted inputs
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
	public AbstractSinglePortFingerprintNodeDescription(String nodeName,
			boolean allowSparseBitVector, boolean allowDenseBitVector,
			boolean allowSparseByteVector, boolean allowDenseByteVector, String iconName,
			String shortDescription, String[] longDescriptionParagraphs, String inPortName,
			String inPortDescripion, String outPortName, String outPortDescription,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options) {
		super(nodeName, allowSparseBitVector, allowDenseBitVector, allowSparseByteVector,
				allowDenseByteVector, iconName, shortDescription, longDescriptionParagraphs,
				new String[] { inPortName }, new String[] { inPortDescripion },
				new String[] { outPortName }, new String[] { outPortDescription }, options);

	}

}
