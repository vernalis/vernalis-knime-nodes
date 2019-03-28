/*******************************************************************************
 * Copyright (c) 2016, 2019 Vernalis (R&D) Ltd
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

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * Node dialog for memory monitoring benchmarking loop start nodes
 * 
 * @author s.roughley
 *
 */
public class AbstractMemMonPerfLoopStartNodeDialog
		extends AbstractPerfMonTimingStartNodeDialog {

	public AbstractMemMonPerfLoopStartNodeDialog() {
		super();
		createNewGroup("Memory monitoring");
		addDialogComponent(new DialogComponentNumber(getMonitoringDelayModel(),
				"Interval between memory monitoring checks (ms)", 1000));
		closeCurrentGroup();
	}

	/**
	 * @return Settings Model for the ms interval between memory level checks
	 */
	public static SettingsModelIntegerBounded getMonitoringDelayModel() {
		return new SettingsModelIntegerBounded("Monitoring Interval", 1000, 5,
				Integer.MAX_VALUE);
	}
}
