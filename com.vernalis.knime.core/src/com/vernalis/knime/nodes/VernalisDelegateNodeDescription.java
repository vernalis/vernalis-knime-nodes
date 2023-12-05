/*******************************************************************************
 * Copyright (c) 2023, Vernalis (R&D) Ltd
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
package com.vernalis.knime.nodes;

import java.util.List;
import java.util.Optional;

import org.apache.xmlbeans.XmlException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.knime.core.eclipseUtil.OSGIHelper;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeFactory.NodeType;
import org.knime.node.v41.KnimeNode;
import org.knime.node.v41.KnimeNodeDocument;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A {@link NodeDescription} delegate class which wraps a v4.1 NodeDesciption
 * implementation, and applies the new (post KNIME 4.6) UI methods.
 * Additionally, all {@code get...(int index)} delegate methods wrap a call to
 * the delegate wit a try catch for {@link IndexOutOfBoundsException}, returning
 * {@code null} in the event it is thrown. This is to mitigate a bug in KNIME
 * 5.2.0 in which the port indices are out-by-one in some circumstances
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class VernalisDelegateNodeDescription extends NodeDescription {

	private static final String DEVELOPED_BY_VERNALIS =
			"<p>This node was developed by "
					+ "<a href=\"https://www.vernalis.com\">Vernalis Research</a>.  For feedback and more "
					+ "information, please contact <a href=\"mailto:knime@vernalis.com\">knime@vernalis.com</a></p>";

	private final NodeDescription delegate;
	private final Element element;
	private final KnimeNodeDocument knimeDoc;
	private final KnimeNode node;

	/**
	 * Overloaded constructor in which the supplying bundle information will be
	 * attempted to be inferred from the class of the delegate
	 * 
	 * @param delegate
	 *            the {@link NodeDescription} instance to wrap
	 * @throws XmlException
	 *             if there was an error parsing the XML Description Element to
	 *             a KnimeNodeDocument instance (Needs a v4.1 node description)
	 */
	public VernalisDelegateNodeDescription(NodeDescription delegate)
			throws XmlException {
		this(delegate, null);
	}

	/**
	 * Constructor
	 * 
	 * @param delegate
	 *            the {@link NodeDescription} instance to wrap
	 * @param factoryClass
	 *            the factory class to ensure the supplying package information
	 *            is injected to the node description. If <code>null</code>
	 * @throws XmlException
	 *             if there was an error parsing the XML Description Element to
	 *             a KnimeNodeDocument instance (Needs a v4.1 node description)
	 */
	public VernalisDelegateNodeDescription(NodeDescription delegate,
			Class<?> factoryClass) throws XmlException {
		super();
		this.delegate = delegate;
		this.element = this.delegate.getXMLDescription();

		final NodeList osgiInfoList = element.getElementsByTagName("osgi-info");
		if (osgiInfoList == null || osgiInfoList.getLength() == 0) {
			Class<?> clz =
					factoryClass == null ? delegate.getClass() : factoryClass;
			Element bundleElement =
					element.getOwnerDocument().createElement("osgi-info");
			Bundle bundle = OSGIHelper.getBundle(clz);
			if (bundle != null) {
				Optional<IInstallableUnit> feature =
						OSGIHelper.getFeature(bundle);
				bundleElement.setAttribute("bundle-symbolic-name", feature
						.map(f -> f.getId()).orElse(bundle.getSymbolicName()));
				bundleElement.setAttribute("bundle-name", feature.map(
						f -> f.getProperty(IInstallableUnit.PROP_NAME, null))
						.orElse(bundle.getHeaders().get("Bundle-Name")));
				bundleElement.setAttribute("bundle-vendor", feature
						.map(f -> f.getProperty(IInstallableUnit.PROP_PROVIDER,
								null))
						.orElse(bundle.getHeaders().get("Bundle-Vendor")));
			} else {
				bundleElement.setAttribute("bundle-symbolic-name", "<Unknown>");
				bundleElement.setAttribute("bundle-name", "<Unknown>");
				bundleElement.setAttribute("bundle-vendor", "<Unknown>");
			}
			if (NodeFactory.class.isAssignableFrom(clz)) {
				bundleElement.setAttribute("factory-package",
						clz.getPackage().getName());
			}
			element.appendChild(bundleElement);
		}

		knimeDoc = KnimeNodeDocument.Factory.parse(element);
		node = knimeDoc.getKnimeNode();


	}

	@Override
	public String getIconPath() {
		return delegate.getIconPath();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public Optional<String> getInteractiveViewDescription() {
		return delegate.getInteractiveViewDescription()
				.or(() -> Optional.ofNullable(
						node.getInteractiveView().getDomNode().getNodeValue()));
	}

	@Override
	public Optional<String> getIntro() {
		return delegate.getIntro().or(() -> Optional
				.ofNullable(node.getFullDescription().getIntro().xmlText()))
				.map(str -> str.contains("href=\"mailto:knime@vernalis.com\"")
						? str
						: str + DEVELOPED_BY_VERNALIS);
	}

	@Override
	public List<DialogOptionGroup> getDialogOptionGroups() {
		return !delegate.getDialogOptionGroups().isEmpty()
				? delegate.getDialogOptionGroups()
				: node.getFullDescription().getTabList().stream()
						.map(t -> new DialogOptionGroup(t.getName(),
								t.getDescription() == null ? null
										: t.getDescription().xmlText(),
								t.getOptionList().stream()
										.map(opt -> new DialogOption(
												opt.getName(), opt.xmlText(),
												opt.getOptional()))
										.toList()))
						.toList();

	}

	@Override
	public List<DescriptionLink> getLinks() {

		return !delegate.getLinks().isEmpty() ? delegate.getLinks()
				: node.getFullDescription().getLinkList().stream()
						.map(l -> new DescriptionLink(
								l.getHref().getStringValue(), l.xmlText()))
						.toList();
	}

	@Override
	public Optional<String> getShortDescription() {
		return delegate.getShortDescription().or(() -> {
			final String shortDescription = node.getShortDescription();
			return Optional.ofNullable(shortDescription);
		});
	}

	@Override
	public List<DynamicPortGroupDescription> getDynamicInPortGroups() {
		return !delegate.getDynamicInPortGroups().isEmpty()
				? delegate.getDynamicInPortGroups()
				: node.getPorts().getDynInPortList().stream()
						.map(p -> new DynamicPortGroupDescription(p.getName(),
								p.getGroupIdentifier(), p.xmlText()))
						.toList();
	}

	@Override
	public List<DynamicPortGroupDescription> getDynamicOutPortGroups() {
		return !delegate.getDynamicOutPortGroups().isEmpty()
				? delegate.getDynamicOutPortGroups()
				: node.getPorts().getDynOutPortList().stream()
						.map(p -> new DynamicPortGroupDescription(p.getName(),
								p.getGroupIdentifier(), p.xmlText()))
						.toList();
	}

	@Override
	public boolean isDeprecated() {
		return delegate.isDeprecated();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public String getInportDescription(int index) {
		try {
			return delegate.getInportDescription(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public String getInportName(int index) {
		try {
			return delegate.getInportName(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public String getInteractiveViewName() {
		return delegate.getInteractiveViewName();
	}

	@Override
	public String getNodeName() {
		return delegate.getNodeName();
	}

	@Override
	public String getOutportDescription(int index) {
		try {
			return delegate.getOutportDescription(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public String getOutportName(int index) {
		try {
			return delegate.getOutportName(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public NodeType getType() {
		return delegate.getType();
	}

	@Override
	public int getViewCount() {
		return delegate.getViewCount();
	}

	@Override
	public String getViewDescription(int index) {
		try {
			return delegate.getViewDescription(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public String getViewName(int index) {
		try {
			return delegate.getViewName(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	@Override
	public Element getXMLDescription() {
		return element;
	}

}
