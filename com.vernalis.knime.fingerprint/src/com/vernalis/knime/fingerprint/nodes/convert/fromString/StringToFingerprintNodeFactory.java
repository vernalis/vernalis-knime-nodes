/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.fingerprint.nodes.convert.fromString;

import org.knime.core.node.NodeDialogPane;

import com.vernalis.knime.fingerprint.nodes.abstrct.factory.AbstractStringToFingerprintNodeFactory;
import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractStringToFingerprintNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 *
 */
public class StringToFingerprintNodeFactory extends AbstractStringToFingerprintNodeFactory {

	public StringToFingerprintNodeFactory() {
		// Parameter is ignored - string type is set at configure time
		super(null);
	}

	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new StringToFingerprintNodeDialog();
	}

	@Override
	public AbstractStringToFingerprintNodeModel createNodeModel() {
		return new StringToFingerprintNodeModel();
	}

}
