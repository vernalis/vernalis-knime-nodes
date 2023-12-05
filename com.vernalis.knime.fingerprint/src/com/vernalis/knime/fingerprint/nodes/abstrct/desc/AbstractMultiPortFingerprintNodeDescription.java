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
package com.vernalis.knime.fingerprint.nodes.abstrct.desc;

import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionsToDescription;

import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlCursor;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.knime.core.data.DataType;
import org.knime.core.data.vector.bitvector.DenseBitVectorCell;
import org.knime.core.data.vector.bitvector.SparseBitVectorCell;
import org.knime.core.data.vector.bytevector.DenseByteVectorCell;
import org.knime.core.data.vector.bytevector.SparseByteVectorCell;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.node.v41.FullDescription;
import org.knime.node.v41.InPort;
import org.knime.node.v41.Intro;
import org.knime.node.v41.KnimeNode;
import org.knime.node.v41.KnimeNodeDocument;
import org.knime.node.v41.OutPort;
import org.knime.node.v41.Ports;
import org.knime.node.v41.View;
import org.knime.node.v41.Views;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.w3c.dom.Element;

import com.vernalis.knime.misc.EitherOr;

/**
 * The base abstract class for the fingerprint node descriptions
 * 
 * @author s.roughley
 *
 */
public class AbstractMultiPortFingerprintNodeDescription extends NodeDescription {

	protected final List<DataType> availableColTypes;
	protected final String shortDescription;
	protected final String[] longDescriptionParagraphs;
	protected final String[] inPortNames;
	protected final String[] inPortDescriptions;
	protected final String[] outPortNames;
	protected final String[] outPortDescriptions;
	protected final EitherOr<Map<String, String>, Map<String, Map<String, String>>> options;
	protected final String nodeName;
	protected final String iconName;

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
	 * @param inPortNames
	 *            The names of the input ports
	 * @param inPortDescriptions
	 *            The descriptions of the input ports
	 * @param outPortNames
	 *            The names of the output ports
	 * @param outPortDescriptions
	 *            The descriptions of the output ports
	 * @param options
	 *            The options to be added to the node description. Use an
	 *            {@link EitherOr#ofLeft(Object)} for a map of options added
	 *            directly to the description or an
	 *            {@link EitherOr#ofRight(Object)} for a map of maps of options
	 *            to be added tab-wise
	 */
	public AbstractMultiPortFingerprintNodeDescription(String nodeName,
			boolean allowSparseBitVector, boolean allowDenseBitVector,
			boolean allowSparseByteVector, boolean allowDenseByteVector, String iconName,
			String shortDescription, String[] longDescriptionParagraphs, String[] inPortNames,
			String[] inPortDescriptions, String[] outPortNames, String[] outPortDescriptions,
			EitherOr<Map<String, String>, Map<String, Map<String, String>>> options) {
		super();
		availableColTypes = new ArrayList<>();
		if (allowSparseBitVector) {
			availableColTypes.add(SparseBitVectorCell.TYPE);
		}
		if (allowDenseBitVector) {
			availableColTypes.add(DenseBitVectorCell.TYPE);
		}
		if (allowSparseByteVector) {
			availableColTypes.add(SparseByteVectorCell.TYPE);
		}
		if (allowDenseByteVector) {
			availableColTypes.add(DenseByteVectorCell.TYPE);
		}
		this.shortDescription = shortDescription;
		this.longDescriptionParagraphs = longDescriptionParagraphs;
		this.options = options;
		this.nodeName = nodeName;
		this.iconName = iconName;
		this.outPortNames = outPortNames;
		if (outPortNames.length != outPortDescriptions.length) {
			throw new IllegalArgumentException(
					"Output port names and descriptions must be the same length!");
		}
		this.outPortDescriptions = outPortDescriptions;
		this.inPortDescriptions = inPortDescriptions;
		if (inPortNames.length != inPortDescriptions.length) {
			throw new IllegalArgumentException(
					"Input port names and descriptions must be the same length!");
		}
		this.inPortNames = inPortNames;
	}

	@Override
	public String getIconPath() {
		// First try looking in the resources folder of NodeDescription class
		URL iconURL = getClass().getResource(iconName);
		if (iconURL == null) {
			// Try looking relative to the bundle
			Bundle bundle = FrameworkUtil.getBundle(getClass());
			IPath iconPath;
			if (iconName.startsWith("com.vernalis")) {
				iconPath = new Path("src/" + iconName.replace(".", "/"));
			} else {
				iconPath = new Path(iconName);
			}
			iconURL = FileLocator.find(bundle, iconPath, null);
		}
		if (iconURL == null) {
			return null;
		}
		return iconURL.getFile();
	}

	@Override
	public String getInportDescription(int index) {
		final StringBuilder suffix = new StringBuilder();
		if (index == 0) {
			if (!inPortDescriptions[0].endsWith(".")) {
				suffix.append(".");
			}
			suffix.append(" Acceptable fingerprint formats are ");
			boolean isFirst = true;
			for (DataType colType : availableColTypes) {
				if (isFirst) {
					isFirst = false;
				} else {
					suffix.append(", ");
				}
				suffix.append(colType.getCellClass().getSimpleName().replace("Cell", ""));
			}
			suffix.append(".");
		}
		return inPortDescriptions[index] + suffix.toString();
	}

	@Override
	public String getInportName(int index) {
		return inPortNames[index];
	}

	@Override
	public String getInteractiveViewName() {
		return null;
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public String getOutportDescription(int index) {
		return outPortDescriptions[index];
	}

	@Override
	public String getOutportName(int index) {
		return outPortNames[index];
	}

	@Override
	public NodeType getType() {
		return NodeType.Manipulator;
	}

	@Override
	public int getViewCount() {
		return 0;
	}

	@Override
	public String getViewDescription(int index) {
		return null;
	}

	@Override
	public String getViewName(int index) {
		return null;
	}

	@Override
	public Element getXMLDescription() {
		KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
		KnimeNode node = doc.addNewKnimeNode();
		node.setIcon(getIconPath());
		node.setName(getNodeName());
		node.setType(org.knime.node.v41.NodeType.MANIPULATOR);
		node.setShortDescription(shortDescription);
		FullDescription fullDesc = node.addNewFullDescription();
		Intro intro = fullDesc.addNewIntro();

		XmlCursor introCursor = intro.newCursor();
		introCursor.toFirstContentToken();
		for (String para : longDescriptionParagraphs) {
			introCursor.insertElementWithText("p", para);
		}
		addDevelopedByVernalis(introCursor);
		introCursor.dispose();
		addOptionsToDescription(fullDesc, options);

		Ports ports = node.addNewPorts();
		InPort inport = ports.addNewInPort();
		for (int i = 0; i < inPortNames.length; i++) {
			inport.setIndex(BigInteger.valueOf(i));
			inport.setName(getInportName(i));
			inport.newCursor().setTextValue(getInportDescription(i));
		}
		OutPort outport = ports.addNewOutPort();
		for (int i = 0; i < outPortNames.length; i++) {
			outport.setIndex(BigInteger.valueOf(i));
			outport.setName(getOutportName(i));
			outport.newCursor().setTextValue(getOutportDescription(i));
		}

		if (getViewCount() > 0) {
			Views views = node.addNewViews();
			for (int i = 0; i < getViewCount(); i++) {
				View view = views.addNewView();
				view.setIndex(BigInteger.valueOf(i));
				view.setName(getViewName(i));
				view.newCursor().setTextValue(getViewDescription(i));
			}
		}
		return (Element) node.getDomNode();

	}

}
