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
package com.vernalis.rcsb.io.helpers;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeFactoryClassMapper;
import org.knime.core.node.NodeModel;

import com.vernalis.rcsb.io.nodes.manip.RCSBmultiDownloadNodeFactory;
import com.vernalis.rcsb.io.nodes.source.RCSBsDownloadNodeFactory;

public class RCSBIONodeFactoryClassMapper extends NodeFactoryClassMapper {

	public RCSBIONodeFactoryClassMapper() {
		super();
	}

	@Override
	public NodeFactory<? extends NodeModel> mapFactoryClassName(String factoryClassName) {
		switch (factoryClassName) {
		case "com.vernalis.nodes.io.rcsb.manip.RCSBmultiDownloadNodeFactory":
			return new RCSBmultiDownloadNodeFactory();
		case "com.vernalis.nodes.io.rcsb.source.RCSBsDownloadNodeFactory":
			return new RCSBsDownloadNodeFactory();
		default:
			return null;
		}
	}

}
