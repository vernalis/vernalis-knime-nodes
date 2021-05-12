/*******************************************************************************
 * Copyright (c) 2016, 2021 Vernalis (R&D) Ltd
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

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.MapNodeFactoryClassMapper;
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
public class NodeFactoryClassMapper extends MapNodeFactoryClassMapper {

	@Override
	protected Map<String, Class<? extends NodeFactory<? extends NodeModel>>>
			getMapInternal() {

		Map<String, Class<? extends NodeFactory<? extends NodeModel>>> retVal =
				new HashMap<>();
		retVal.put(
				"com.vernalis.knime.perfmon.nodes.timing.end.PerfMonTimingEndNodeFactory",
				PerfMonTimingEndNodeFactory.class);
		retVal.put(
				"com.vernalis.knime.perfmon.nodes.timing.end.PerfMonTimingEnd2PortNodeFactory",
				PerfMonTimingEnd2PortNodeFactory.class);
		retVal.put(
				"com.vernalis.knime.perfmon.nodes.timing.end.PerfMonTimingEnd3PortNodeFactory",
				PerfMonTimingEnd3PortNodeFactory.class);
		retVal.put(
				"com.vernalis.knime.perfmon.nodes.timing.start.PerfMonTimingStartNodeFactory",
				PerfMonTimingStartNodeFactory.class);
		retVal.put(
				"com.vernalis.knime.perfmon.nodes.timing.start.PerfMonTiming2PortStartNodeFactory",
				PerfMonTiming2PortStartNodeFactory.class);
		retVal.put(
				"com.vernalis.knime.perfmon.nodes.timing.start.PerfMonTiming3PortStartNodeFactory",
				PerfMonTiming3PortStartNodeFactory.class);

		return retVal;
	}
}
