/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.flowcontrol.portcombiner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;

import com.vernalis.knime.flowcontrol.portcombiner.api.PortTypeCombiner;

/**
 * Registry object to provide a few default {@link PortTypeCombiner}s and read
 * the extension point implementations
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
public class PortTypeCombinerRegistry {

	private final Map<PortType, PortTypeCombiner> typeMap = new HashMap<>();
	private static final NodeLogger logger =
			NodeLogger.getLogger(PortTypeCombinerRegistry.class);
	private static final PortTypeRegistry typeReg =
			PortTypeRegistry.getInstance();

	/**
	 * Private constructor
	 */
	private PortTypeCombinerRegistry() {
		// The default options
		typeMap.put(BufferedDataTable.TYPE,
				BufferedDataTablePortCombiner.getInstance());
		typeMap.put(FlowVariablePortObject.TYPE,
				FlowVariablePortTypeCombiner.getInstance());

		// Now the extension point....
		List<IConfigurationElement> config = new ArrayList<>();
		// Handle anything implemented internally before made public
		Collections.addAll(config,
				Platform.getExtensionRegistry().getConfigurationElementsFor(
						"com.vernalis.knime.internal.flowcontrol.porttypecombiner"));
		Collections.addAll(config,
				Platform.getExtensionRegistry().getConfigurationElementsFor(
						"com.vernalis.knime.flowcontrol.porttypecombiner"));
		for (IConfigurationElement elem : config) {
			try {
				Object obj = elem.createExecutableExtension("combiner-class");
				PortTypeCombiner combiner;
				if (!(obj instanceof PortTypeCombiner)) {
					logger.coding(
							"Error loading port type combiner with class '"
									+ elem.getAttribute("combiner-class")
									+ "' - Implementation is not of 'PortTypeCombiner' interface");
					combiner = null;
				} else {
					combiner = (PortTypeCombiner) obj;
				}
				String typeName = elem.getAttribute("portType");
				PortType portType;
				if (typeName == null) {
					portType = null;
					logger.coding(
							"Error loading port type combiner - no port type supplied!");
				} else {
					Optional<Class<? extends PortObject>> typeClass =
							typeReg.getObjectClass(typeName);
					if (typeClass.isPresent()) {
						portType = typeReg.getPortType(typeClass.get());
					} else {
						// we try looking for the name or spec class instead, in
						// case that was supplied..
						Optional<Class<? extends PortObjectSpec>> specClass =
								typeReg.getSpecClass(typeName);
						if (specClass.isPresent()) {
							portType = typeReg.availablePortTypes().stream()
									.filter(pt -> pt.getPortObjectSpecClass()
											.equals(specClass.get()))
									.findFirst().orElse(null);
						} else {
							portType = typeReg.availablePortTypes().stream()
									.filter(pt -> pt.getName().equals(typeName))
									.findFirst().orElse(null);
						}
					}
				}
				if (portType == null) {
					logger.coding(
							"Error loading port type combiner - unable to resolve port type '"
									+ typeName + "'");
				}
				if (combiner != null && portType != null) {
					if (typeMap.containsKey(portType)) {
						logger.warn(
								"Multiple PortObjectCombiner implementations registered for port type '"
										+ portType.getName()
										+ "' replacing previous with"
										+ combiner.toString());
					}
					typeMap.put(portType, combiner);
				}
			} catch (CoreException e) {
				logger.coding(
						"Error loading flow variable condition with class '"
								+ elem.getAttribute("class") + "' - "
								+ e.getMessage(),
						e);
			}
		}
	}

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static PortTypeCombinerRegistry INSTANCE =
				new PortTypeCombinerRegistry();
	}

	/**
	 * Method to get the singleton instance
	 * 
	 * @return The singleton instance of PortTypeCombinerRegistry
	 */
	public static PortTypeCombinerRegistry getInstance() {
		return HoldingClass.INSTANCE;
	}

	/**
	 * @param type
	 *            the port type to combine
	 * 
	 * @return the {@link PortTypeCombiner} for the given type or {@code null}
	 *         if no combiner is mapped for the specified type
	 */
	public PortTypeCombiner getCombiner(PortType type) {
		return typeMap.get(type);
	}

}
