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
package com.vernalis.knime.core.memory;

import static com.vernalis.knime.core.system.SystemUtils.getPID;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.node.NodeLogger;

import com.vernalis.knime.core.os.OS;
import com.vernalis.knime.core.os.OperatingSystem;
import com.vernalis.knime.core.os.UnsupportedOperatingSystemException;
import com.vernalis.knime.core.system.CommandExecutionException;
import com.vernalis.knime.core.system.SystemCommandRunner;

/**
 * Utility class for methods relating to memory use
 * 
 * @author s.roughley
 * 
 */
public class MemoryUtils {
	/** Linux Result key */
	private static final String RES = "RES";
	/** Windows Result Key */
	private static final String MEM_USAGE = "MEM USAGE";
	/** Windows memory command */
	private static final String WIN_MEM_CMD = "tasklist /FI \"PID eq %PID%\" /FO CSV";
	/** Linux memory command */
	private static final String LINUX_MEM_CMD = "top -n 1 -b -p %PID%";
	/** Constant multiplier for MB */
	private static double MB = 1024.0 * 1024.0;
	/** Regex to extract windows memory result */
	private static final Pattern memPatt = Pattern.compile("([\\d,\\.]+)(K|M|G)?");
	/** Node Logger Instance **/
	private static final NodeLogger logger = NodeLogger.getLogger(MemoryUtils.class);

	private MemoryUtils() {
		// Don't instantiate
	}

	/**
	 * Run a heavy GC. System#gc() calls are repeated at the given interval,
	 * with System#runFinalization() calls interleaved between.
	 * 
	 * @param numberOfGCs
	 *            The total number of Garbage Collection Calls
	 * @param interGCDelayMS
	 *            The delay (ms) between each call
	 * @throws InterruptedException
	 *             If {@link Thread#sleep(long)}) is interrupted
	 */
	public static void runHeavyGC(int numberOfGCs, int interGCDelayMS) throws InterruptedException {
		System.gc();

		for (int i = 1; i < numberOfGCs; i++) {
			Thread.sleep(interGCDelayMS / 2);
			System.runFinalization();
			Thread.sleep(interGCDelayMS / 2);
			System.gc();
		}

	}

	/**
	 * @param os
	 *            The OperatingSystem (Use {@link OperatingSystem#getOS() to
	 *            find}
	 * @return The memory usage of the process in MB
	 * @throws UnsupportedOperatingSystemException
	 *             If os is not windows, mac or linux
	 * @throws CommandExecutionException
	 *             If the system command failed to run
	 */
	public static double getSystemProcessMemory(OS os)
			throws UnsupportedOperatingSystemException, CommandExecutionException {
		SystemCommandRunner scr;
		String delim;
		switch (os) {
		case LINUX:
		case MAC:
			scr = new SystemCommandRunner(LINUX_MEM_CMD.replace("%PID%", "" + getPID()));
			delim = "\\s+";
			break;
		case WIN:
			scr = new SystemCommandRunner(WIN_MEM_CMD.replace("%PID%", "" + getPID()));
			delim = "\\\",\\\"";
			break;
		case OTHER:
		default:
			throw new UnsupportedOperatingSystemException();
		}

		scr.run();

		// Parse the last 2 lines of the std out to the required results
		String[] stdOutLines = scr.getStdOut().toUpperCase().split("\\n");
		HashMap<String, String> returnValues = new HashMap<>();

		String[] propNames = stdOutLines[stdOutLines.length - 2].trim().split(delim);
		String[] propVals = stdOutLines[stdOutLines.length - 1].trim().split(delim);
		for (int i = 0; i < propNames.length; i++) {
			returnValues.put(propNames[i].replace("\"", ""), propVals[i].replace("\"", ""));
		}

		// Now get the relevant property and convert to MB
		// Windows 'MEM USAGE'
		// Linux 'RES'
		double kB;
		switch (os) {
		case WIN:
			returnValues.put(MEM_USAGE, returnValues.get(MEM_USAGE).replaceAll("\\s", ""));
			double multiplier = 1;
			Matcher m = memPatt.matcher(returnValues.get(MEM_USAGE));
			if (m.find()) {
				// Use the following to ensure correct current locale parsing of
				// '.' and ','
				try {
					kB = NumberFormat.getInstance().parse(m.group(1)).doubleValue();
				} catch (ParseException e) {
					logger.error("Number parsing exception while getting process memory: "
							+ e.getMessage());
					throw new CommandExecutionException(
							"Number parsing exception while getting process memory: "
									+ e.getMessage(),
							e);
				}
				switch (m.group(2)) {
				case "M":
					multiplier = 1024;
					break;
				case "G":
					multiplier = 1024 * 1024;
				case "K":
				default:
					break;
				}
			} else {
				kB = -1;
			}
			kB *= multiplier;
			// kB = Long.parseLong(
			// returnValues.get("MEM USAGE").replace(",", "").replace("K",
			// "").trim());
			break;
		case LINUX:
		case MAC:
			kB = Long.parseLong(returnValues.get(RES).trim());
			break;
		default:
			kB = -1;
			break;
		}
		return kB / 1024.0;
	}

	/**
	 * Overloaded version which handles detecting the OS directly
	 * 
	 * @return The memory usage of the process in MB
	 * @throws UnsupportedOperatingSystemException
	 *             If os is not windows, mac or linux
	 * @throws CommandExecutionException
	 *             If the system command failed to run
	 */
	public static double getSystemProcessMemory()
			throws UnsupportedOperatingSystemException, CommandExecutionException {
		OS os = OperatingSystem.getInstance().getOS();
		return getSystemProcessMemory(os);
	}

	/**
	 * @return The amount of free memory within the JVM (in MB)
	 */
	public static double getJVMFreeMemory() {
		return Runtime.getRuntime().freeMemory() / MB;
	}

	/**
	 * @return The total amount of memory currently allocated to the JVM (in MB)
	 */
	public static double getJVMAllocatedMemory() {
		return Runtime.getRuntime().totalMemory() / MB;
	}

	/**
	 * @return The maximum amount of memory available to the JVM (in MB)
	 */
	public static double getJVMMaxAvailableMemory() {
		return Runtime.getRuntime().maxMemory() / MB;
	}

	/**
	 * @return The amount of memory in the JVM which is currently actually used
	 */
	public static double getJVMUsedMemory() {
		return getJVMAllocatedMemory() - getJVMFreeMemory();
	}
}
