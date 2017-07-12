/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 *******************************************************************************/
package com.vernalis.knime.core.system;

import java.lang.management.ManagementFactory;

/**
 * @author s.roughley
 *
 */
public class SystemUtils {
	private SystemUtils() {
		// Dont Instantiate
	}

	/**
	 * @return The System process ID for the JVM
	 */
	public static int getPID() {
		int retVal = -1;
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		if (jvmName.indexOf("@") > 0) {
			retVal = new Integer(jvmName.split("@")[0]);
		}
		return retVal;
	}

}
