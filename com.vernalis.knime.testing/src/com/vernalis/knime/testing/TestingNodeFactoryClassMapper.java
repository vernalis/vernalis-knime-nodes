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
/**
 * 
 */
package com.vernalis.knime.testing;

import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeFactoryClassMapper;
import org.knime.core.node.NodeModel;

import com.vernalis.knime.testing.nodes.missingvals.EmptyColumnTestNodeFactory;

/**
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since 07-Sep-2022
 */
public class TestingNodeFactoryClassMapper extends NodeFactoryClassMapper {

	@Override
	public NodeFactory<? extends NodeModel>
			mapFactoryClassName(String factoryClassName) {
		switch (factoryClassName) {
			case "com.vernalis.knime.perfmon.nodes.testing.missingvals.EmptyColumnTestNodeFactory":
				// Formerly in the permon plugin
				return new EmptyColumnTestNodeFactory();

			default:
				return null;
		}
	}

}
