/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
 ******************************************************************************/
package com.vernalis.knime.perfmon.nodes.timing.abstrct;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * @author s.roughley
 *
 */
public class AbstractPerfMonTimingStartNodeDialog extends DefaultNodeSettingsPane {
	private final SettingsModelBoolean m_timeCutout;
	private final SettingsModelIntegerBounded m_maxTime;

	public AbstractPerfMonTimingStartNodeDialog() {
		super();
		addDialogComponent(new DialogComponentNumber(createIterationsModel(),
				"Number of iterations", 1));
		m_timeCutout = createTimeCutoutModel();
		m_maxTime = createMaxTimeModel();
		m_maxTime.setEnabled(m_timeCutout.getBooleanValue());
		m_timeCutout.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				m_maxTime.setEnabled(m_timeCutout.getBooleanValue());

			}
		});
		addDialogComponent(new DialogComponentBoolean(m_timeCutout,
				"Stop tests after timeout period?"));
		addDialogComponent(new DialogComponentNumber(m_maxTime,
				"Maximum total time (s)", 10));
	}

	/**
	 * @return The settings model for the maximum time
	 */
	public static SettingsModelIntegerBounded createMaxTimeModel() {
		return new SettingsModelIntegerBounded("Maximum Time", 300, 0,
				Integer.MAX_VALUE);
	}

	/**
	 * @return The settings model for whether the maximum time is observed
	 */
	public static SettingsModelBoolean createTimeCutoutModel() {
		return new SettingsModelBoolean("Use Cutout period", false);
	}

	/**
	 * @return The settings model for the maximum number of iterations
	 */
	public static SettingsModelIntegerBounded createIterationsModel() {
		return new SettingsModelIntegerBounded("Number of Iterations", 1, 0,
				Integer.MAX_VALUE);
	}
}
