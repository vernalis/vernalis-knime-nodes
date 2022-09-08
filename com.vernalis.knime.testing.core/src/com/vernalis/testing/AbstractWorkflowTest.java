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
package com.vernalis.testing;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.util.ConvenienceMethods;
import org.knime.core.node.workflow.UnsupportedWorkflowVersionException;
import org.knime.core.util.LockFailedException;
import org.knime.testing.core.TestrunConfiguration;

import nl.esciencecenter.e3dchem.knime.testing.TestFlowRunner;

/**
 * Abstract base test class to run a test workflow.
 * <p>
 * Tests are run using default parameters which can be overriden using JVM
 * properties:
 * <ul>
 * <li>{@code knime.test.dialogs} - should dialogs be tested? (Default:
 * {@code true})</li>
 * <li>{@code knime.test.report.deprecated} - should deprecated nodes be
 * reported? (Default: {@code true})</li>
 * <li>{@code knime.test.memory.leaks} - should memory leaks be tested?
 * (Default: {@code true})</li>
 * <li>{@code knime.test.views} - should node views be opened on load? (Default:
 * {@code false})</li>
 * <li>{@code knime.test.memory.bytes} - Allowed memory use increase in test if
 * memory leaks are being tested? Values can use 'k', 'M' and 'G' as suffix to
 * indicate kilobytes, Megabytes, Gigabytes, e.g. 1M for 1024 kB (Default:
 * {@code 96Mb})</li>
 * </ul>
 * </p>
 * <p>
 * Implementations should indicate node coverage of the test case using the
 * {@link NodeTestFlow} or {@link NodeTestFlows} annotations
 * 
 *
 * @since 07-Sep-2022
 * @since v1.36.0

 * @author S Roughley
 */
public abstract class AbstractWorkflowTest {

	/** Error collector instance */
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	private TestFlowRunner runner;

	/**
	 * Default value of testDialogs from system property
	 * {@code knime.test.dialogs}. Defaults to {@code true}
	 */
	protected static final boolean DEFAULT_TEST_DIALOGS = Boolean
			.parseBoolean(System.getProperty("knime.test.dialogs", "true"));

	/**
	 * Default value of reportDeprecatedNodes from system property
	 * {@code knime.test.report.deprecated}. Defaults to {@code true}
	 */
	protected static final boolean DEFAULT_REPORT_DEPRECATED_NODES =
			Boolean.parseBoolean(System
					.getProperty("knime.test.report.deprecated", "false"));

	/**
	 * Default value of checkMemoryLeaks from system property
	 * {@code knime.test.memory.leaks}. Defaults to {@code true}
	 * 
	 * @see #DEFAULT_ALLOWED_MEMORY_INCREASE
	 */
	protected static final boolean DEFAULT_CHECK_MEMORY_LEAKS =
			Boolean.parseBoolean(
					System.getProperty("knime.test.memory.leaks", "true"));

	/**
	 * Default value of testLoadSaveLoad. Always {@code false} as not supported
	 * by the testing framework
	 */
	protected static final boolean DEFAULT_LOADSAVELOAD = false; // True
																	// currently
																	// breaks
																	// it!

	/**
	 * Default value of textViews from system property {@code knime.test.views}.
	 * Defaults to {@code false}
	 */
	protected static final boolean DEFAULT_TEST_VIEWS = Boolean
			.parseBoolean(System.getProperty("knime.test.views", "false"));

	/**
	 * Default value of testDialogs from system property
	 * {@code knime.test.dialogs}. Defaults to {@code 96M}
	 * 
	 * @see #DEFAULT_CHECK_MEMORY_LEAKS
	 */
	protected static final int DEFAULT_ALLOWED_MEMORY_INCREASE;

	static {
		String envVar = "knime.test.memory.bytes";
		long l = ConvenienceMethods.readSizeSystemProperty(envVar,
				96 * 1024 * 1034 /* 96 Mb */);
		DEFAULT_ALLOWED_MEMORY_INCREASE =
				l > Integer.MAX_VALUE ? 96 * 1024 * 1034 : (int) l;
	}

	private final boolean testDialogs;
	private final boolean reportDeprecatedNodes;
	private final boolean checkMemoryLeaks;
	private final boolean loadSaveLoad;
	private final boolean testViews;
	private final int allowedMemoryIncrease;
	private final String workflowPath;

	/**
	 * Constructor applying defaults from system properties:
	 * <ul>
	 * <li>{@code knime.test.dialogs} - should dialogs be tested? (Default:
	 * {@code true})</li>
	 * <li>{@code knime.test.report.deprecated} - should deprecated nodes be
	 * reported? (Default: {@code false})</li>
	 * <li>{@code knime.test.memory.leaks} - should memory leaks be tested?
	 * (Default: {@code true})</li>
	 * <li>{@code knime.test.views} - should node views be opened on load?
	 * (Default: {@code false})</li>
	 * <li>{@code knime.test.memory.bytes} - Allowed memory use increase in test
	 * if memory leaks are being tested? Values can use 'k', 'M' and 'G' as
	 * suffix to indicate kilobytes, Megabytes, Gigabytes, e.g. 1M for 1024 kB
	 * (Default: {@code 96Mb})</li>
	 * </ul>
	 * 
	 * @param workflowPath
	 *            the non-null path to the test workflow within the
	 *            {@code .tests} fragment, e.g. {@code src/knime/MyNodeTestflow}
	 */
	protected AbstractWorkflowTest(String workflowPath) {
		this(workflowPath, 1);
	}

	/**
	 * Constructor applying defaults from system properties:
	 * <ul>
	 * <li>{@code knime.test.dialogs} - should dialogs be tested? (Default:
	 * {@code true})</li>
	 * <li>{@code knime.test.report.deprecated} - should deprecated nodes be
	 * reported? (Default: {@code false})</li>
	 * <li>{@code knime.test.memory.leaks} - should memory leaks be tested?
	 * (Default: {@code true})</li>
	 * <li>{@code knime.test.views} - should node views be opened on load?
	 * (Default: {@code false})</li>
	 * <li>{@code knime.test.memory.bytes} - Allowed memory use increase in test
	 * if memory leaks are being tested? Values can use 'k', 'M' and 'G' as
	 * suffix to indicate kilobytes, Megabytes, Gigabytes, e.g. 1M for 1024 kB
	 * (Default: {@code 96Mb})</li>
	 * <li>{@code knime.test.dialogs} - should dialogs be tested? (Default:
	 * {@code true})</li>
	 * <li>{@code knime.test.dialogs} - should dialogs be tested? (Default:
	 * {@code true})</li>
	 * 
	 * </ul>
	 * 
	 * @param workflowPath
	 *            the non-null path to the test workflow within the
	 *            {@code .tests} fragment, e.g. {@code src/knime/MyNodeTestflow}
	 * @param memLimitMultiplier
	 *            A multiplier for the memory limit for simple use of larger
	 *            memory limits. If &lt;1 then a value of 1 is used
	 */
	protected AbstractWorkflowTest(String workflowPath,
			int memLimitMultiplier) {
		this(null, null, null, null, null,
				validateMultiplier(memLimitMultiplier)
						* DEFAULT_ALLOWED_MEMORY_INCREASE,
				workflowPath);
	}

	private static Integer validateMultiplier(int memLimitMultiplier) {
		return memLimitMultiplier < 1 ? 1 : memLimitMultiplier;
	}

	/**
	 * Full constructor allowing manual override of parameters from defaults
	 * specified by system properties. NB the default values are available as
	 * static constants starting {@code DEFAULT_}
	 * 
	 * @param testDialogs
	 *            should the dialogs be tested? {@code null} for default from
	 *            system properties
	 * @param reportDeprecatedNodes
	 *            should deprecated nodes be reported? {@code null} for default
	 *            from system properties
	 * @param checkMemoryLeaks
	 *            should memory leaks be reported? {@code null} for default from
	 *            system properties
	 * @param loadSaveLoad
	 *            should a load-save-load cycle be used? (NB currently will fail
	 *            as not supported!) {@code null} for default from system
	 *            properties
	 * @param testViews
	 *            should node views be opened on workflow load? {@code null} for
	 *            default from system properties
	 * @param allowedMemoryIncrease
	 *            the maximum memory increase allowed (in bytes) if
	 *            {@code checkMemoryLeaks} is set. {@code null} for default from
	 *            system properties. If a fixed multiple of the default is
	 *            required, e.g. for a test known to require larger memory use
	 *            increases, then this can be achieved with e.g.
	 *            {@code 8 * DEFAULT_ALLOWED_MEMORY_INCREASE}
	 * @param workflowPath
	 *            the non-null path to the test workflow within the
	 *            {@code .tests} fragment, e.g. {@code src/knime/MyNodeTestflow}
	 */
	protected AbstractWorkflowTest(Boolean testDialogs,
			Boolean reportDeprecatedNodes, Boolean checkMemoryLeaks,
			Boolean loadSaveLoad, Boolean testViews,
			Integer allowedMemoryIncrease, String workflowPath) {
		this.testDialogs = testDialogs == null ? DEFAULT_TEST_DIALOGS
				: testDialogs.booleanValue();
		this.reportDeprecatedNodes =
				reportDeprecatedNodes == null ? DEFAULT_REPORT_DEPRECATED_NODES
						: reportDeprecatedNodes.booleanValue();
		this.checkMemoryLeaks =
				checkMemoryLeaks == null ? DEFAULT_CHECK_MEMORY_LEAKS
						: checkMemoryLeaks.booleanValue();
		this.loadSaveLoad = loadSaveLoad == null ? DEFAULT_LOADSAVELOAD
				: loadSaveLoad.booleanValue();
		this.testViews = testViews == null ? DEFAULT_TEST_VIEWS
				: testViews.booleanValue();
		this.allowedMemoryIncrease =
				allowedMemoryIncrease == null ? DEFAULT_ALLOWED_MEMORY_INCREASE
						: allowedMemoryIncrease.intValue();
		this.workflowPath = Objects.requireNonNull(workflowPath,
				"workflowPath my not be null!");
	}

	/**
	 * Setup the run configuration
	 */
	@Before
	public void setUp() {
		TestrunConfiguration runConfiguration = new TestrunConfiguration();
		runConfiguration.setTestDialogs(testDialogs);
		runConfiguration.setReportDeprecatedNodes(reportDeprecatedNodes);
		runConfiguration.setCheckMemoryLeaks(checkMemoryLeaks);
		runConfiguration.setAllowedMemoryIncrease(allowedMemoryIncrease);
		runConfiguration.setLoadSaveLoad(loadSaveLoad);
		runConfiguration.setTestViews(testViews);
		runner = new TestFlowRunner(collector, runConfiguration);
	}

	/**
	 * Run the workflow test
	 * 
	 * @throws IOException
	 *             If there was an error reading the workflow
	 * @throws InvalidSettingsException
	 *             If there was a settings error
	 * @throws CanceledExecutionException
	 *             If the execution was cancelled
	 * @throws UnsupportedWorkflowVersionException
	 *             If the workflow version wasnt supported by the current AP
	 * @throws LockFailedException
	 *             If the workflow had a lock error
	 * @throws InterruptedException
	 *             If there was a thread interruption
	 */
	@Test
	public void runTestFlow() throws IOException, InvalidSettingsException,
			CanceledExecutionException, UnsupportedWorkflowVersionException,
			LockFailedException, InterruptedException {
		File workflowDir = new File(workflowPath);
		runner.runTestWorkflow(workflowDir);
	}
}
