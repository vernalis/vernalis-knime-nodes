/*******************************************************************************
 * Copyright (c) 2016,2021, Vernalis (R&D) Ltd
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
package com.vernalis.rcsb.io.helpers;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.MapNodeFactoryClassMapper;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeModel;

import com.vernalis.rcsb.io.nodes.manip.RCSBmultiDownload2NodeFactory;

public class RCSBIONodeFactoryClassMapper extends MapNodeFactoryClassMapper {

	@Override
	protected Map<String, Class<? extends NodeFactory<? extends NodeModel>>>
			getMapInternal() {
		Map<String, Class<? extends NodeFactory<? extends NodeModel>>> retVal =
				new HashMap<>();
		retVal.put(
				"com.vernalis.nodes.io.rcsb.manip.RCSBmultiDownloadNodeFactory",
				RCSBmultiDownload2NodeFactory.class);
		retVal.put(
				"com.vernalis.rcsb.io.nodes.manip.RCSBmultiDownloadNodeFactory",
				RCSBmultiDownload2NodeFactory.class);
		retVal.put("com.vernalis.nodes.io.rcsb.source.RCSBsDownloadNodeFactory",
				RCSBmultiDownload2NodeFactory.class);
		retVal.put("com.vernalis.rcsb.io.nodes.source.RCSBsDownloadNodeFactory",
				RCSBmultiDownload2NodeFactory.class);
		return retVal;
	}

}
