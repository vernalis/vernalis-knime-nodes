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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class providing methods to run a command on the operating system and capture
 * the results of running the command
 * 
 * @author S. Roughley <s.roughley@vernalis.com>
 * 
 */
public class SystemCommandRunner {
	private String stdOut = null;
	private String stdErr = null;
	private Integer retCode = null;
	private final String command;

	public SystemCommandRunner(String command) {
		this.command = command;

	}

	/**
	 * Method to run the command on the operating System. Will throw an
	 * exception if the command has already been executed
	 * 
	 * @throws CommandExecutionException
	 *             if
	 *             <ul>
	 *             <li>the command has already been run</li>
	 *             <li>an error was thrown during the command execution (in
	 *             which case {@link #getReturnCode()} will return {@code -1})</li>
	 *             <li>an error was thrown during the reading of StdOut from the
	 *             command execution (in which case {@link #getReturnCode()}
	 *             will return {@code -2})</li>
	 *             </ul>
	 *             In the latter two cases, {@link #getStdErr()} will contain
	 *             the exception message
	 * @see #reRun()
	 */
	public void run() throws CommandExecutionException {
		if (retCode != null) {
			throw new CommandExecutionException("Command has already been run!");
		}

		Runtime r = Runtime.getRuntime();
		Process p = null;
		try {
			p = r.exec(command);
			p.waitFor();
		} catch (Exception e) {
			stdErr = "Exception thrown during command execution: "
					+ e.getMessage();
			retCode = -1;
			throw new CommandExecutionException(stdErr, e.getCause());
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		StringBuilder cmdOut = new StringBuilder();
		String line = "";
		try {
			while ((line = reader.readLine()) != null) {
				cmdOut.append(line + "\n");
			}
		} catch (Exception e) {
			stdErr = "Exception thrown reading StdOut: " + e.getMessage();
			retCode = -2;
			throw new CommandExecutionException(stdErr, e.getCause());

		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				;
			}
		}

		stdOut = cmdOut.toString().trim();

		reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		line = "";
		StringBuilder tmp = new StringBuilder();
		try {
			while ((line = reader.readLine()) != null) {
				tmp.append(line + "\n");
			}

		} catch (Exception e) {
			;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
				;
			}
		}
		stdErr = tmp.toString().trim();
		if ("".equals(stdErr)) {
			stdErr = null;
		}
		retCode = p.exitValue();
	}

	/**
	 * Method forcing the command to be re-executed, discarding any previous
	 * results.
	 * 
	 * @see #run() {@link #run()} for further details
	 */
	public void reRun() throws CommandExecutionException {
		retCode = null;
		stdOut = null;
		stdErr = null;
		run();
	}

	/**
	 * Get the StdErr output from running the command
	 * 
	 * @return the stdErr - {@code null} if no Exception was raised (in which
	 *         case it is the result of {@link Exception#getMessage()} ), and
	 *         the stdErr from the Operating System was an empty string.
	 */
	public String getStdErr() {
		return stdErr;
	}

	/**
	 * Get the StdOut output from running the command
	 * 
	 * @return the stdOut - Will be {@code null} if an exception was thrown by
	 *         attempting to run the command, but may contain output even if the
	 *         process did not complete successfully
	 */
	public String getStdOut() {
		return stdOut;
	}

	/**
	 * Get the ExitCode returned by running the command.
	 * 
	 * @return the retCode Either the exit code returned by running the command
	 *         (normally '0' if the command executed successfully) or
	 *         <ul>
	 *         <li> {@code null} if the command has not been run</li>
	 *         <li>{@code -1} if an exception was thrown whilst attempting to
	 *         run the command on the operating system</li>
	 *         <li>{@code -2} if an exception was thrown whilst attempting to
	 *         read the StdOut Stream from the command</li>
	 *         </ul>
	 *         In the latter two cases, {@link #getStdErr()} will return the
	 *         exception message.
	 * @see #run()
	 * @see #reRun()
	 * 
	 */
	public Integer getReturnCode() {
		return retCode;
	}
}