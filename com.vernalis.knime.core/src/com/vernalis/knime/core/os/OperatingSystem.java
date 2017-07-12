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
package com.vernalis.knime.core.os;

import static com.vernalis.knime.core.os.OS.LINUX;
import static com.vernalis.knime.core.os.OS.MAC;
import static com.vernalis.knime.core.os.OS.OTHER;
import static com.vernalis.knime.core.os.OS.WIN;

import java.util.Locale;
/**
 * Singleton class storing information about the operating system
 * 
 * @author s.roughley
 * 
 */
public class OperatingSystem {
	private static OperatingSystem INSTANCE = null;
	private static OS OS = null;
	private static boolean isMac, isWin, isLinux;

	private OperatingSystem() {
		// hide the constructor
	}

	public static OperatingSystem getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new OperatingSystem();
			setOperatingSystem();
		}
		return INSTANCE;
	}

	/**
	 * @return the oS
	 */
	public OS getOS() {
		return OS;
	}

	/**
	 * @return the isMac
	 */
	public boolean isMac() {
		return isMac;
	}

	/**
	 * @return the isWin
	 */
	public boolean isWin() {
		return isWin;
	}

	/**
	 * @return the isLinux
	 */
	public boolean isLinux() {
		return isLinux;
	}

	private static void setOperatingSystem() {
		if (OS == null) {
			String os_name = System.getProperty("os.name").toLowerCase(
					Locale.ENGLISH);
			if (os_name.indexOf("mac") >= 0 || os_name.indexOf("darwin") >= 0) {
				// Mac
				OS = MAC;
				isMac = true;
				isWin = false;
				isLinux = false;
			} else if (os_name.indexOf("win") >= 0) {
				// Windows
				OS = WIN;
				isMac = false;
				isWin = true;
				isLinux = false;
			} else if (os_name.indexOf("nux") >= 0) {
				OS = LINUX;
				isMac = false;
				isWin = false;
				isLinux = true;
			} else {
				OS = OTHER;
				isMac = false;
				isWin = false;
				isLinux = false;
			}
		}
	}
}
