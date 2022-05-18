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
package com.vernalis.knime.flowcontrol.nodes.switches.ifcaseswitch;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * An enumeration of the options for node behaviour when the selected variable
 * is out of bounds of the range of output ports
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 *
 */
enum OutOfBoundsBehaviour implements ButtonGroupEnumInterface {

	/**
	 * The node execution should fail
	 */
	FAIL("Fail") {

		@Override
		public Integer getActivePortIndex(int variableValue, int numPorts,
				boolean countNegativesFromLast) {
			int retVal = countNegativesFromLast && variableValue < 0
					? numPorts + variableValue
					: variableValue;
			if (retVal < 0 || retVal >= numPorts) {
				throw new IndexOutOfBoundsException(
						"Index " + variableValue + " out of range ("
								+ (countNegativesFromLast ? -numPorts : 0)
								+ " - " + (numPorts - 1) + ")");
			}
			return retVal;
		}
	},

	/**
	 * The active port should be nearest value to the variable value which is
	 * within the range of ports
	 */
	NEAREST_IN_RANGE("Nearest in range") {

		@Override
		public Integer getActivePortIndex(int variableValue, int numPorts,
				boolean countNegativesFromLast) {
			int retVal = countNegativesFromLast && variableValue < 0
					? numPorts + variableValue
					: variableValue;
			if (retVal < 0) {
				return 0;
			} else if (retVal < numPorts) {
				return retVal;
			} else {
				return numPorts - 1;
			}
		}
	},

	/**
	 * The active port will be the modulo reduction from the number of ports
	 */
	MODULO_REDUCTION("Modulo reduce") {

		@Override
		public Integer getActivePortIndex(int variableValue, int numPorts,
				boolean countNegativesFromLast) {
			int retVal = variableValue % numPorts;
			if (retVal < 0) {
				if (countNegativesFromLast) {
					retVal = numPorts + retVal;
				} else {
					throw new IndexOutOfBoundsException(
							"Index " + variableValue + " < 0");
				}
			}
			return retVal;

		}
	},

	/**
	 * All output ports will be active
	 */
	ALL("All") {

		@Override
		public Integer getActivePortIndex(int variableValue, int numPorts,
				boolean countNegativesFromLast) {
			int retVal = countNegativesFromLast && variableValue < 0
					? numPorts + variableValue
					: variableValue;
			if (retVal < 0) {
				return null;
			} else if (retVal < numPorts) {
				return retVal;
			} else {
				return null;
			}
		}
	};

	private final String text;

	private OutOfBoundsBehaviour(String text) {
		this.text = text;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return getText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * Method to determine the active port index
	 * 
	 * @param variableValue
	 *            the variable value
	 * @param numPorts
	 *            the number of output ports
	 * @param countNegativesFromLast
	 *            should negative indices count backwards from last port
	 * 
	 * @return the active port index, or {@code null} if all ports are active
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the node should fail
	 */
	public abstract Integer getActivePortIndex(int variableValue, int numPorts,
			boolean countNegativesFromLast) throws IndexOutOfBoundsException;

	/**
	 * @return the default option
	 */
	static OutOfBoundsBehaviour getDefault() {
		return FAIL;
	}
}
