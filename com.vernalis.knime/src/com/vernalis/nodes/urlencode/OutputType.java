/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.urlencode;

import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.uri.UriCellFactory;
import org.knime.core.node.util.ButtonGroupEnumInterface;

public enum OutputType implements ButtonGroupEnumInterface {

	String_Column {

		@Override
		public DataType getOutputType(DataType inputType) {
			return StringCell.TYPE;
		}
	},
	URI_Column {

		@Override
		public DataType getOutputType(DataType inputType) {
			return UriCellFactory.TYPE;
		}
	};

	@Override
	public String getText() {
		return name().replace("_", " ");
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return null;
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public abstract DataType getOutputType(DataType inputType);

	public static OutputType getDefault() {
		return String_Column;
	}

}
