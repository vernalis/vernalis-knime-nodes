/*******************************************************************************
 * Copyright (c) 2014, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.flowcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;

/**
 * A utility class to provide shared methods for flow control nodes such as if,
 * end if etc. Methods are for checking for, finding, returning active or
 * inactive ports, and for generating the PortObjects to create nodes simply
 * Most methods are duplicated for {@link PortObject}s and
 * {@link PortObjectSpec}s to allow their use in both
 * {@link org.knime.core.node.NodeModel}#configure and #execute methods
 * 
 * @author "Stephen Roughley  <s.roughley@vernalis.com>"
 * 
 */
public class FlowControlHelpers {
	/**
	 * Get a list of the active ports (i.e those which are attached,
	 * configurable and 'active')
	 * 
	 * @param inPorts
	 *            {@link PortObject} Array of inPorts
	 * @return {@link ArrayList} containing the port indices of the active ports
	 */
	public static List<Integer> listActivePortIds(PortObject[] inPorts) {
		List<Integer> activePorts = new ArrayList<Integer>();
		for (int i = 0, l = inPorts.length; i < l; i++) {
			if (inPorts[i] != null
					&& !(inPorts[i] instanceof InactiveBranchPortObject)) {
				activePorts.add(i);
			}
		}
		return activePorts;
	}

	/**
	 * Get a list of the active ports (i.e those which are attached,
	 * configurable and 'active')
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return {@link ArrayList} containing the port indices of the active ports
	 */
	public static List<Integer> listActivePortIds(PortObjectSpec[] inSpecs) {
		List<Integer> activePorts = new ArrayList<Integer>();
		for (int i = 0, l = inSpecs.length; i < l; i++) {
			if (inSpecs[i] != null
					&& !(inSpecs[i] instanceof InactiveBranchPortObjectSpec)) {
				activePorts.add(i);
			}
		}
		return activePorts;
	}

	/**
	 * Count the number of active (i.e those which are attached, configurable
	 * and 'active')
	 * 
	 * @param inPorts
	 *            {@link PortObject} Array of inPorts
	 * @return count of active inPorts
	 */
	public static int countActivePorts(PortObject[] inPorts) {
		return listActivePortIds(inPorts).size();
	}

	/**
	 * Count the number of active (i.e those which are attached, configurable
	 * and 'active')
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return count of active in ports
	 */
	public static int countActivePorts(PortObjectSpec[] inSpecs) {
		return listActivePortIds(inSpecs).size();
	}

	/**
	 * Get the index of the first active (i.e those which are attached,
	 * configurable and 'active') in port. Returns -1 if no active ports are
	 * found.
	 * 
	 * @param inPorts
	 *            {@link PortObject} Array of inPorts
	 * @return index of the first active in port
	 */
	public static int getFirstActivePortId(PortObject[] inPorts) {
		for (int i = 0, l = inPorts.length; i < l; i++) {
			if (inPorts[i] != null
					&& !(inPorts[i] instanceof InactiveBranchPortObject)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the index of the first active (i.e those which are attached,
	 * configurable and 'active') in port. Returns -1 if no active ports are
	 * found.
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return index of the first active in port
	 */
	public static int getFirstActivePortId(PortObjectSpec[] inSpecs) {
		for (int i = 0, l = inSpecs.length; i < l; i++) {
			if (inSpecs[i] != null
					&& !(inSpecs[i] instanceof InactiveBranchPortObjectSpec)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the {@link PortObject} of the first active (i.e those which are
	 * attached, configurable and 'active') in port. Returns null if no active
	 * ports are found.
	 * 
	 * @param inPorts
	 *            {@link PortObject} Array of inPorts
	 * @return {@link PortObject} of the first active in port
	 */
	public static PortObject getFirstActivePortObject(PortObject[] inPorts) {
		for (int i = 0, l = inPorts.length; i < l; i++) {
			if (inPorts[i] != null
					&& !(inPorts[i] instanceof InactiveBranchPortObject)) {
				return inPorts[i];
			}
		}
		return null;
	}

	/**
	 * Get the {@link PortObjectSpec} of the first active (i.e those which are
	 * attached, configurable and 'active') in port. Returns null if no active
	 * ports are found.
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return {@link PortObjectSpec} of the first active in port
	 */
	public static PortObjectSpec getFirstActivePortObjectSpec(
			PortObjectSpec[] inSpecs) {
		for (int i = 0, l = inSpecs.length; i < l; i++) {
			if (inSpecs[i] != null
					&& !(inSpecs[i] instanceof InactiveBranchPortObjectSpec)) {
				return inSpecs[i];
			}
		}
		return null;
	}

	/**
	 * Get the index of the last active (i.e those which are attached,
	 * configurable and 'active') in port. Returns -1 if no active ports are
	 * found.
	 * 
	 * @param inPorts
	 *            {@link PortObject} Array of inPorts
	 * @return index of the last active in port
	 */
	public static int getLastActivePortId(PortObject[] inPorts) {
		for (int i = inPorts.length - 1; i >= 0; i--) {
			if (inPorts[i] != null
					&& !(inPorts[i] instanceof InactiveBranchPortObject)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the index of the last active (i.e those which are attached,
	 * configurable and 'active') in port. Returns -1 if no active ports are
	 * found.
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return index of the last active in port
	 */
	public static int getLastActivePortId(PortObjectSpec[] inSpecs) {
		for (int i = inSpecs.length - 1; i >= 0; i--) {
			if (inSpecs[i] != null
					&& !(inSpecs[i] instanceof InactiveBranchPortObjectSpec)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get the {@link PortObject} of the last active (i.e those which are
	 * attached, configurable and 'active') in port. Returns null if no active
	 * ports are found.
	 * 
	 * @param inPorts
	 *            {@link PortObject} Array of inPorts
	 * @return {@link PortObject} of the last active in port
	 */
	public static PortObject getLastActivePortObject(PortObject[] inPorts) {
		for (int i = inPorts.length - 1; i >= 0; i--) {
			if (inPorts[i] != null
					&& !(inPorts[i] instanceof InactiveBranchPortObject)) {
				return inPorts[i];
			}
		}
		return null;
	}

	/**
	 * Get the {@link PortObjectSpec} of the last active (i.e those which are
	 * attached, configurable and 'active') in port. Returns null if no active
	 * ports are found.
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return {@link PortObjectSpec} of the first active in port
	 */
	public static PortObjectSpec getLastActivePortObjectSpec(
			PortObjectSpec[] inSpecs) {
		for (int i = inSpecs.length - 1; i >= 0; i--) {
			if (inSpecs[i] != null
					&& !(inSpecs[i] instanceof InactiveBranchPortObjectSpec)) {
				return inSpecs[i];
			}
		}
		return null;
	}

	/**
	 * Check whether there is an active (i.e those which are attached,
	 * configurable and 'active')in port
	 * 
	 * @param inPorts
	 *            {@link PortObject} array of in ports
	 * @return true if there is an active in port
	 */
	public static boolean hasActivePort(PortObject[] inPorts) {
		return (countActivePorts(inPorts) > 0);
	}

	/**
	 * Check whether there is an active (i.e those which are attached,
	 * configurable and 'active')in port
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return true if there is an active in port
	 */
	public static boolean hasActivePort(PortObjectSpec[] inSpecs) {
		return (countActivePorts(inSpecs) > 0);
	}

	/**
	 * Check whether there is an inactive (i.e those which are attached,
	 * configurable and 'inactive')in port
	 * 
	 * @param inPorts
	 *            {@link PortObject} array of in ports
	 * @return true if there is an inactive in port
	 */
	public static boolean hasInactivePort(PortObject[] inPorts) {
		for (int port = 0, l = inPorts.length; port < l; port++) {
			if (inPorts[port] != null
					&& inPorts[port] instanceof InactiveBranchPortObject) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check whether there is an inactive (i.e those which are attached,
	 * configurable and 'inactive')in port
	 * 
	 * @param inSpecs
	 *            the in specs
	 * @return true if there is an inactive in port
	 */
	public static boolean hasInactivePort(PortObjectSpec[] inSpecs) {
		for (int port = 0, l = inSpecs.length; port < l; port++) {
			if (inSpecs[port] != null
					&& inSpecs[port] instanceof InactiveBranchPortObjectSpec) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility method to create the in ports to an inactive branch end node. If
	 * less than two input ports are specified, two are created. If there are
	 * only 2 input ports, both are required, otherwise only the first is
	 * required and additional ports are optional
	 * 
	 * @param type
	 *            The {@link PortType} of the ports to be created (e.g
	 *            FlowVariablePortObject.TYPE)
	 * @param portCount
	 *            The number of input ports to be created
	 * @return the port type[]
	 */
	public static PortType[] createEndInPorts(PortType type, int portCount) {
		// Make sure at least two ports are required
		portCount = (portCount < 2) ? 2 : portCount;
		PortType[] inPorts = new PortType[portCount];
		// Fill it with optional ports
		Arrays.fill(inPorts, new PortType(type.getPortObjectClass(), true));

		// The first port is never optional!
		inPorts[0] = new PortType(type.getPortObjectClass());
		if (portCount <= 2) {
			// If two ports, then neither are optional!
			inPorts[1] = new PortType(type.getPortObjectClass());
		}
		return inPorts;
	}

	/**
	 * Utility method to create the in ports to an inactive branch end node. If
	 * less than two input ports are specified, two are created. If there are
	 * only 2 input ports, both are required, otherwise only the first is
	 * required and additional ports are optional
	 * 
	 * @param type
	 *            The {@link PortObject} class of the ports to be created (e.g
	 *            FlowVariablePortObject)
	 * @param portCount
	 *            The number of input ports to be created
	 * @return the port type[]
	 */
	public static PortType[] createEndInPorts(Class<? extends PortObject> type,
			int portCount) {
		// Make sure at least two ports are required
		portCount = (portCount < 2) ? 2 : portCount;
		PortType[] inPorts = new PortType[portCount];

		Arrays.fill(inPorts, new PortType(type, true));
		// The first port is never optional!
		inPorts[0] = new PortType(type);
		if (portCount <= 2) {
			inPorts[1] = new PortType(type);
		}
		return inPorts;
	}

	/**
	 * Utility method to create the out port to an inactive branch end node.
	 * 
	 * @param type
	 *            The {@link PortType} of the ports to be created (e.g
	 *            FlowVariablePortObject.TYPE)
	 * @return the port type[]
	 */
	public static PortType[] createEndOutPort(PortType type) {
		PortType[] outPort = new PortType[1];
		outPort[0] = new PortType(type.getPortObjectClass());
		return outPort;
	}

	/**
	 * Utility method to create the out port to an inactive branch end node.
	 * 
	 * @param type
	 *            The {@link PortObject} class of the ports to be created (e.g
	 *            FlowVariablePortObject)
	 * @return the port type[]
	 */
	public static PortType[] createEndOutPort(Class<? extends PortObject> type) {
		PortType[] outPort = new PortType[1];
		outPort[0] = new PortType(type);
		return outPort;
	}

	/**
	 * Utility method to create the out ports to an inactive branch start node.
	 * If less than two input ports are specified, two are created.
	 * 
	 * @param type
	 *            The {@link PortType} of the ports to be created (e.g
	 *            FlowVariablePortObject.TYPE)
	 * @param portCount
	 *            The number of input ports to be created
	 * @return the port type[]
	 */
	public static PortType[] createStartOutPorts(PortType type, int portCount) {
		// Make sure at least two ports are required
		portCount = (portCount < 2) ? 2 : portCount;
		PortType[] outPorts = new PortType[portCount];
		Arrays.fill(outPorts, new PortType(type.getPortObjectClass()));
		return outPorts;
	}

	/**
	 * Utility method to create the out ports to an inactive branch start node.
	 * If less than two input ports are specified, two are created.
	 * 
	 * @param type
	 *            The {@link PortObject} class of the ports to be created (e.g
	 *            FlowVariablePortObject)
	 * @param portCount
	 *            The number of input ports to be created
	 * @return the port type[]
	 */
	public static PortType[] createStartOutPorts(
			Class<? extends PortObject> type, int portCount) {
		// Make sure at least two ports are required
		portCount = (portCount < 2) ? 2 : portCount;
		PortType[] outPorts = new PortType[portCount];
		Arrays.fill(outPorts, new PortType(type));
		return outPorts;
	}

	/**
	 * Utility method to create the in port to an inactive branch start node.
	 * 
	 * @param type
	 *            The {@link PortType} of the ports to be created (e.g
	 *            FlowVariablePortObject.TYPE)
	 * @return the port type[]
	 */
	public static PortType[] createStartInPort(PortType type) {
		PortType[] inPort = new PortType[1];
		inPort[0] = new PortType(type.getPortObjectClass());
		return inPort;
	}

	/**
	 * Utility method to create the in port to an inactive branch start node.
	 * 
	 * @param type
	 *            The {@link PortObject} class of the ports to be created (e.g
	 *            FlowVariablePortObject)
	 * @return the port type[]
	 */
	public static PortType[] createStartInPort(Class<? extends PortObject> type) {
		PortType[] inPort = new PortType[1];
		inPort[0] = new PortType(type);
		return inPort;
	}

	/**
	 * Utility function to pass the active inport {@link PortObjectSpec} to the
	 * active outport, and set the other outports to be
	 * {@link InactiveBranchPortObjectSpec#INSTANCE}.
	 * 
	 * @param inSpecs
	 *            {@link PortObjectSpec} array of the input port specs
	 * @param numPorts
	 *            The total number of ports to create
	 * @param activePortIdx
	 *            The index of the active outport
	 * @return {@link PortObjectSpec} array of the output port specs
	 * @throws InvalidSettingsException
	 *             Thrown if the active port ID < 0 or >= the number of output
	 *             ports
	 */
	public static PortObjectSpec[] createStartOutputPortObjectSpec(
			PortObjectSpec[] inSpecs, int numPorts, int activePortIdx)
			throws InvalidSettingsException {
		if (activePortIdx < 0 || activePortIdx >= numPorts) {
			// Nonsense settings
			throw new InvalidSettingsException(
					"You must select an active port in the range 0 - "
							+ (numPorts - 1));
		}

		PortObjectSpec[] outSpecs = new PortObjectSpec[numPorts];
		for (int port = 0; port < numPorts; port++) {
			outSpecs[port] = InactiveBranchPortObjectSpec.INSTANCE;
		}
		outSpecs[activePortIdx] = inSpecs[0];
		return outSpecs;
	}

	/**
	 * Utility function to pass the active inport {@link PortObject} to the
	 * active outport, and set the other outports to be
	 * {@link InactiveBranchPortObject#INSTANCE}.
	 * 
	 * @param inData
	 *            the in data
	 * @param numPorts
	 *            The total number of ports to create
	 * @param activePortIdx
	 *            The index of the active outport
	 * @return {@link PortObject} array of the output ports
	 * @throws InvalidSettingsException
	 *             Thrown if the active port ID < 0 or >= the number of output
	 *             ports
	 */
	public static PortObject[] createStartOutputPortObject(PortObject[] inData,
			int numPorts, int activePortIdx) throws InvalidSettingsException {
		if (activePortIdx < 0 || activePortIdx >= numPorts) {
			// Nonsense settings
			throw new InvalidSettingsException(
					"You must select an active port in the range 0 - "
							+ (numPorts - 1));
		}

		PortObject[] outData = new PortObject[numPorts];
		for (int port = 0; port < numPorts; port++) {
			outData[port] = InactiveBranchPortObject.INSTANCE;
		}
		outData[activePortIdx] = inData[0];
		return outData;
	}

	/**
	 * Utility function to pass the active inport {@link PortObjectSpec} to the
	 * active outport(s), and set the other outports to be
	 * {@link InactiveBranchPortObjectSpec#INSTANCE}. Takes a list of active
	 * ports but otherwise is the same as
	 * {@link #createStartOutputPortObjectSpec(PortObjectSpec[], int, int)}
	 * 
	 * @param inSpecs
	 *            {@link PortObjectSpec} array of the input port specs
	 * @param numPorts
	 *            The total number of ports to create
	 * @param activePortIdx
	 *            List of indices of active ports
	 * @return {@link PortObjectSpec} array of the output port specs
	 * @throws InvalidSettingsException
	 *             Thrown if the active port ID < 0 or >= the number of output
	 */
	public static PortObjectSpec[] createStartOutputPortObjectSpecs(
			PortObjectSpec[] inSpecs, int numPorts, List<Integer> activePortIdx)
			throws InvalidSettingsException {

		// We are going to assume that there are a sensible number of output
		// ports!
		PortObjectSpec[] outSpecs = new PortObjectSpec[numPorts];
		Arrays.fill(outSpecs, InactiveBranchPortObjectSpec.INSTANCE);

		// Now pass the input port to the active output port(s)
		for (int port : activePortIdx) {
			if (port >= 0 && port < numPorts) {
				outSpecs[port] = inSpecs[0];
			} else {
				throw new InvalidSettingsException(
						"Active ports must be in the range 0 - "
								+ (numPorts - 1));
			}
		}

		return outSpecs;
	}

	/**
	 * Utility function to pass the active inport {@link PortObject} to the
	 * active outport(s), and set the other outports to be
	 * {@link InactiveBranchPortObject#INSTANCE}. Takes a list of active ports
	 * but otherwise is the same as
	 * {@link #createStartOutputPortObject(PortObject[], int, int)}
	 * 
	 * @param inData
	 *            {@link PortObject} array of the input port specs
	 * @param numPorts
	 *            The total number of ports to create
	 * @param activePortIdx
	 *            List of indices of active ports
	 * @return {@link PortObject} array of the output port specs
	 * @throws InvalidSettingsException
	 *             Thrown if the active port ID < 0 or >= the number of output
	 */
	public static PortObject[] createStartOutputPortObjects(
			PortObject[] inData, int numPorts, List<Integer> activePortIdx)
			throws InvalidSettingsException {

		// We are going to assume that there are a sensible number of output
		// ports!
		PortObject[] outData = new PortObject[numPorts];
		Arrays.fill(outData, InactiveBranchPortObject.INSTANCE);

		// Now pass the input port to the active output port(s)
		for (int port : activePortIdx) {
			if (port >= 0 && port < numPorts) {
				outData[port] = inData[0];
			} else {
				throw new InvalidSettingsException(
						"Active ports must be in the range 0 - "
								+ (numPorts - 1));
			}
		}

		return outData;
	}

	/**
	 * Create the output ports for a timed loop end node. NB Adds 1 extra port
	 * for the unprocessed rows
	 * 
	 * @param type
	 *            The Port Type
	 * @param numPorts
	 *            The number of INPUT ports (i.e. one less than the number of
	 *            output ports!)
	 * @return PortType array
	 */
	public static PortType[] createTimedEndPorts(PortType type, int numPorts) {

		PortType[] outPorts = new PortType[numPorts + 1];
		Arrays.fill(outPorts, new PortType(type.getPortObjectClass()));
		return outPorts;

	}

	/**
	 * Utility method to create the in ports to an timed loop end node. If there
	 * are only 2 input ports, both are required, otherwise only the first is
	 * required and additional ports are optional
	 * 
	 * @param type
	 *            The {@link PortType} of the ports to be created (e.g
	 *            FlowVariablePortObject.TYPE)
	 * @param portCount
	 *            The number of input ports to be created
	 * @return the port type[]
	 */
	public static PortType[] createTimedEndOptionalInPorts(PortType type,
			int portCount) {
		PortType[] inPorts = new PortType[portCount];
		// Fill it with optional ports
		Arrays.fill(inPorts, new PortType(type.getPortObjectClass(), true));

		// The first port is never optional!
		inPorts[0] = new PortType(type.getPortObjectClass());
		if (portCount <= 2) {
			// If two ports, then neither are optional!
			inPorts[1] = new PortType(type.getPortObjectClass());
		}
		return inPorts;
	}

	/**
	 * Utility method to create the in ports to a timed loop end node. If less
	 * than two input ports are specified, two are created.
	 * 
	 * @param type
	 *            The {@link PortType} of the ports to be created (e.g
	 *            FlowVariablePortObject.TYPE)
	 * @param portCount
	 *            The number of input ports to be created
	 * @return the port type[]
	 */
	public static PortType[] createTimedEndInPorts(PortType type, int portCount) {
		PortType[] outPorts = new PortType[portCount];
		Arrays.fill(outPorts, new PortType(type.getPortObjectClass()));
		return outPorts;
	}
}
