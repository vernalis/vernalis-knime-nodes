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
package com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.loopstart;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.flowcontrol.nodes.timedloops.abstrct.TimedMissingValuePolicy;

/**
 * Abstract Node Dialog class for Timed Loop start nodes. Provides methods for
 * adding dialog panes and creating settings models.
 * 
 * @author S.Roughley <s.roughley@vernalis.com>
 * 
 */
public abstract class AbstractTimedLoopStartNodeDialog extends
		DefaultNodeSettingsPane {

	/**
	 * Settings model for the missing value policy. Is null unless set by the
	 * {@link #createMissingVariablePanel()} method.
	 */
	protected static SettingsModelString m_onMissing = null;
	/**
	 * Settings model for the missing double value. Is null unless set by the
	 * {@link #createMissingVariablePanel()} method.
	 */
	protected static SettingsModelDouble m_missingDouble = null;
	/**
	 * Settings model for the missing integer value. Is null unless set by the
	 * {@link #createMissingVariablePanel()} method.
	 */
	protected static SettingsModelInteger m_missingInteger = null;
	/**
	 * Settings model for the missing String value. Is null unless set by the
	 * {@link #createMissingVariablePanel()} method.
	 */
	protected static SettingsModelString m_missingString = null;

	/**
	 * Simplest initiation, adding no panels to the dialog.
	 */
	protected AbstractTimedLoopStartNodeDialog() {
		super();
	}

	/**
	 * Simple Start Timed Loop Node Dialog panel. A chunk size panel is created,
	 * and either a run-to-time or run-for-time expiry time panel. Finally a
	 * iteration counter start panel is added.
	 * 
	 * @param isRunToTime
	 *            True if Run-to-time node, or False if Run-for-time
	 */
	protected AbstractTimedLoopStartNodeDialog(boolean isRunToTime) {
		super();
		addChunkSizePanel();
		// Add the appropriate Expiry Time panel
		if (isRunToTime) {
			addRunToTimePanel();
		} else {
			addRunForTimePanel();
		}
		addZerothIterationCountPanel();
	}

	// Dialog creation methods
	/**
	 * Adds a simple dialogue group to ask the user for the number of rows to be
	 * processed in each chunk.
	 */
	protected void addChunkSizePanel() {
		createNewGroup("Chunking");
		addDialogComponent(new DialogComponentNumber(createChunkSizeModel(),
				"Number of rows per chunk", 5));
	}

	/**
	 * Creates a time entry dialogue group for 'Run-to-time' nodes. Should be
	 * followed by a call to {@link #addZerothIterationCountPanel()}.
	 * 
	 * @see #createRunForTimePanel()
	 */
	protected void addRunToTimePanel() {
		createNewGroup("Expiry Time");
		addDialogComponent(new DialogComponentLabel("End time (hh:mm):"));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentNumber(createHourModel(true), "",
				1));
		addDialogComponent(new DialogComponentNumber(createMinuteModel(), ":",
				5));
		setHorizontalPlacement(false);
		addDialogComponent(new DialogComponentBoolean(
				createRunToTomorrowModel(), "Run to tomorrow"));
		addDialogComponent(new DialogComponentBoolean(
				createRunThruWeekendModel(), "Run through weekend"));
	}

	/**
	 * Creates a time entry dialogue group for 'Run-for-time' nodes. Should be
	 * followed by a call to {@link #addZerothIterationCountPanel()}.
	 * 
	 * @see #createRunToTimePanel();
	 */
	protected void addRunForTimePanel() {
		createNewGroup("Expiry Time");
		addDialogComponent(new DialogComponentLabel("Run time (dd:hh:mm:ss):"));
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentNumber(createDayModel(), "", 1));
		addDialogComponent(new DialogComponentNumber(createHourModel(false),
				":", 1));
		addDialogComponent(new DialogComponentNumber(createMinuteModel(), ":",
				5));
		addDialogComponent(new DialogComponentNumber(createSecondModel(), ":",
				5));
		setHorizontalPlacement(false);
	}

	/**
	 * Adds a dialog component for the start value of the iteration counter.
	 * Normally this should immediately follow a call to either
	 * {@link #addRunToTimePanel()} or {@link #addRunForTimePanel()}.
	 */
	protected void addZerothIterationCountPanel() {
		addDialogComponent(new DialogComponentNumber(createZerothIterModel(),
				"Start value of iteration counter", 1));
	}

	/**
	 * Creates a panel to select the behaviour of missing variables, and supply
	 * default values if appropriate.
	 */
	protected void addMissingVariablePanel() {
		createNewGroup("Missing Variable Values");
		// Initialise the Settings Models
		m_onMissing = createOnMissingModel();
		m_missingDouble = createMissingDblModel();
		m_missingString = createMissingStrModel();
		m_missingInteger = createMissingIntModel();

		m_onMissing.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateDefaultsStatus();

			}
		});

		addDialogComponent(new DialogComponentButtonGroup(m_onMissing,
				"Missing Value behaviours", true,
				TimedMissingValuePolicy.values()));

		addDialogComponent(new DialogComponentNumber(m_missingInteger,
				"Default Integer value", 1, 10));
		addDialogComponent(new DialogComponentNumber(m_missingDouble,
				"Default Double Value", 1.0, 10));
		addDialogComponent(new DialogComponentString(m_missingString,
				"Default String Value", true, 13));
		updateDefaultsStatus();

	}

	/**
	 * Update the enabled status of the Settings Models.
	 */
	public static void updateDefaultsStatus() {
		TimedMissingValuePolicy selOption = TimedMissingValuePolicy
				.valueOf(m_onMissing.getStringValue());
		if (selOption == TimedMissingValuePolicy.DEFAULT) {
			m_missingDouble.setEnabled(true);
			m_missingInteger.setEnabled(true);
			m_missingString.setEnabled(true);
		} else {
			m_missingDouble.setEnabled(false);
			m_missingInteger.setEnabled(false);
			m_missingString.setEnabled(false);
		}
	}

	// Default settings model creation methods

	/**
	 * Create a settings model for the initial iteration counter value.
	 * 
	 * @return the settings model integer bounded
	 */
	public static SettingsModelIntegerBounded createZerothIterModel() {
		return new SettingsModelIntegerBounded("Zeroth Iteration", 0, 0,
				Integer.MAX_VALUE);
	}

	/**
	 * Create a settings model for the chunk size.
	 * 
	 * @return the settings model integer bounded
	 */
	public static SettingsModelIntegerBounded createChunkSizeModel() {
		return new SettingsModelIntegerBounded("Rows per chunk", 1, 1,
				Integer.MAX_VALUE);
	}

	/**
	 * Create a settings model for the day value.
	 * 
	 * @return the settings model integer bounded
	 */
	public static SettingsModelIntegerBounded createDayModel() {
		return new SettingsModelIntegerBounded("Days", 0, 0, 14);
	}

	/**
	 * Create a settings model for the hour value.
	 * 
	 * @param isRunToTime
	 *            if true, then the default is 7, otherwise is 0
	 * @return the settings model integer bounded
	 */
	public static SettingsModelIntegerBounded createHourModel(
			final boolean isRunToTime) {
		return new SettingsModelIntegerBounded("Hours", isRunToTime ? 7 : 0, 0,
				24);
	}

	/**
	 * Create a settings model for the minute value.
	 * 
	 * @return the settings model integer bounded
	 */
	public static SettingsModelIntegerBounded createMinuteModel() {
		return new SettingsModelIntegerBounded("Minutes", 0, 0, 59);
	}

	/**
	 * Create a settings model for the seconds value.
	 * 
	 * @return the settings model integer bounded
	 */
	public static SettingsModelIntegerBounded createSecondModel() {
		return new SettingsModelIntegerBounded("Seconds", 0, 0, 59);
	}

	/**
	 * Create a settings model for the run to tomorrow option value.
	 * 
	 * @return the settings model boolean
	 */
	public static SettingsModelBoolean createRunToTomorrowModel() {
		return new SettingsModelBoolean("Run until tomorrow", true);
	}

	/**
	 * Create a settings model for the run through weekend option value.
	 * 
	 * @return the settings model boolean
	 */
	public static SettingsModelBoolean createRunThruWeekendModel() {
		return new SettingsModelBoolean("Run through weekend", true);
	}

	/**
	 * Create a settings model for the missing value behaviour.
	 * 
	 * @return the settings model string
	 */
	public static SettingsModelString createOnMissingModel() {
		return new SettingsModelString("Missing Value", TimedMissingValuePolicy
				.getDefaultMethod().getActionCommand());
	}

	/**
	 * Create a settings model for the default integer value.
	 * 
	 * @return the settings model integer
	 */
	public static SettingsModelInteger createMissingIntModel() {
		return new SettingsModelInteger("Default Integer", 0);
	}

	/**
	 * Create a settings model for the default String value.
	 * 
	 * @return the settings model string
	 */
	public static SettingsModelString createMissingStrModel() {
		return new SettingsModelString("Default String", "");
	}

	/**
	 * Create a settings model for the default double value.
	 * 
	 * @return the settings model double
	 */
	public static SettingsModelDouble createMissingDblModel() {
		return new SettingsModelDouble("Default Double", 0.0);
	}
}
