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
package com.vernalis.knime.core.nodes.props.abstrct;

import java.util.ArrayList;
import java.util.List;

import org.knime.bio.types.PdbValue;
import org.knime.chem.types.CtabValue;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.node.util.ColumnFilter;
import org.rdkit.knime.types.RDKitMolValue;

/**
 * A flexible {@link ColumnFilter} allowing oe of more column types to be
 * selected
 * 
 * @author s.roughley
 *
 */
public class MoleculeTypeColumnFilter implements ColumnFilter {

	private final List<Class<? extends DataValue>> acceptedTypes =
			new ArrayList<>();
	private final String failMessage;

	/**
	 * Constructor
	 * 
	 * @param acceptsMol
	 *            Should the node accept Mol cells?
	 * @param acceptsSDF
	 *            Should the node accept SDF cells?
	 * @param acceptsCTab
	 *            Should the node accept CTab cells?
	 * @param acceptsSMILES
	 *            Should the node accept SMILES cells?
	 * @param acceptsPDB
	 *            Should the node accept PDB Cells?
	 * @param acceptsRDKit
	 *            Should the node accept RDKit Cells?
	 */
	public MoleculeTypeColumnFilter(boolean acceptsMol, boolean acceptsSDF,
			boolean acceptsCTab, boolean acceptsSMILES, boolean acceptsPDB,
			boolean acceptsRDKit) {
		StringBuilder msgBuilder =
				new StringBuilder("No molecule columns of accepted types (");
		if (acceptsCTab) {
			acceptedTypes.add(CtabValue.class);
			msgBuilder.append("CTab");
		}
		if (acceptsMol) {
			acceptedTypes.add(MolValue.class);
			if (!acceptedTypes.isEmpty()) {
				msgBuilder.append(", ");
			}
			msgBuilder.append("Mol");
		}
		if (acceptsPDB) {
			acceptedTypes.add(PdbValue.class);
			if (!acceptedTypes.isEmpty()) {
				msgBuilder.append(", ");
			}
			msgBuilder.append("PDB");
		}
		if (acceptsRDKit) {
			acceptedTypes.add(RDKitMolValue.class);
			if (!acceptedTypes.isEmpty()) {
				msgBuilder.append(", ");
			}
			msgBuilder.append("RDKit");
		}
		if (acceptsSDF) {
			acceptedTypes.add(SdfValue.class);
			if (!acceptedTypes.isEmpty()) {
				msgBuilder.append(", ");
			}
			msgBuilder.append("SDF");
		}
		if (acceptsSMILES) {
			acceptedTypes.add(SmilesValue.class);
			if (!acceptedTypes.isEmpty()) {
				msgBuilder.append(", ");
			}
			msgBuilder.append("SMILES");
		}
		msgBuilder.append(") available");
		failMessage = msgBuilder.toString();
	}

	@Override
	public boolean includeColumn(DataColumnSpec colSpec) {
		DataType type = colSpec.getType();
		for (Class<? extends DataValue> accType : acceptedTypes) {
			if (type.isCompatible(accType) || type.isAdaptable(accType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String allFilteredMsg() {
		return failMessage;
	}

}
