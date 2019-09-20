/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.plot.nodes.box;

import org.knime.ext.jfc.node.base.JfcBaseNodeDialogPane;
import org.knime.ext.jfc.node.base.JfcBaseNodeFactory;
import org.knime.ext.jfc.node.base.JfcGenericBaseNodeModel;

/**
 * Node factory for the boxplot node
 * 
 * @author S.Roughley
 *
 */
public class BoxplotNodeFactory extends JfcBaseNodeFactory {

	public BoxplotNodeFactory() {

	}

	@Override
	public JfcGenericBaseNodeModel createNodeModel() {
		return new BoxplotNodeModel();
	}

	@Override
	public JfcBaseNodeDialogPane createNodeDialogPane() {
		return new BoxplotNodeDialogPane();
	}

}
