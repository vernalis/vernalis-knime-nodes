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
package com.vernalis.knime.perfmon;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;

import com.vernalis.knime.perfmon.nodes.loopend.timing.PerfMonTimingEnd2PortNodeFactory;
import com.vernalis.knime.perfmon.nodes.loopend.timing.PerfMonTimingEnd3PortNodeFactory;
import com.vernalis.knime.perfmon.nodes.loopend.timing.PerfMonTimingEndNodeFactory;
import com.vernalis.knime.perfmon.nodes.loopstart.timing.PerfMonTiming2PortStartNodeFactory;
import com.vernalis.knime.perfmon.nodes.loopstart.timing.PerfMonTiming3PortStartNodeFactory;
import com.vernalis.knime.perfmon.nodes.loopstart.timing.PerfMonTimingStartNodeFactory;

/**
 * Factory class mapper to find refactored node factory classes
 * 
 * @author S.Roughley
 *
 */
public class NodeFactoryClassMapper extends org.knime.core.node.NodeFactoryClassMapper {

	public NodeFactoryClassMapper() {

	}

	@Override
	public NodeFactory<? extends NodeModel> mapFactoryClassName(String factoryClassName) {
		switch (factoryClassName) {
		case "com.vernalis.knime.perfmon.nodes.timing.end.PerfMonTimingEndNodeFactory":
			return new PerfMonTimingEndNodeFactory();
		case "com.vernalis.knime.perfmon.nodes.timing.end.PerfMonTimingEnd2PortNodeFactory":
			return new PerfMonTimingEnd2PortNodeFactory();
		case "com.vernalis.knime.perfmon.nodes.timing.end.PerfMonTimingEnd3PortNodeFactory":
			return new PerfMonTimingEnd3PortNodeFactory();
		case "com.vernalis.knime.perfmon.nodes.timing.start.PerfMonTimingStartNodeFactory":
			return new PerfMonTimingStartNodeFactory();
		case "com.vernalis.knime.perfmon.nodes.timing.start.PerfMonTiming2PortStartNodeFactory":
			return new PerfMonTiming2PortStartNodeFactory();
		case "com.vernalis.knime.perfmon.nodes.timing.start.PerfMonTiming3PortStartNodeFactory":
			return new PerfMonTiming3PortStartNodeFactory();
		default:
			return null;
		}
	}
}
