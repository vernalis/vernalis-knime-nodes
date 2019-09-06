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
package com.vernalis.knime.chem.rdkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.knime.bio.types.PdbValue;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.RxnValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmartsValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.node.util.ColumnFilter;
import org.rdkit.knime.types.RDKitMolValue;
import org.rdkit.knime.types.RDKitReactionValue;

import com.vernalis.exceptions.RowExecutionException;

/**
 * A simple enum providing easy access to column type filters for RDKit, based
 * on considerations such as whether the molecule needs to have co-ordinates, is
 * / maybe a reaction etc.
 * 
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public enum RdkitCompatibleColumnFormats implements ColumnFilter {
	@SuppressWarnings("unchecked")
	MOL_WITH_COORDS(RDKitMolValue.class, MolValue.class, SdfValue.class),

	@SuppressWarnings("unchecked")
	MOL_WITH_COORDS_INCL_PDB(RDKitMolValue.class, MolValue.class, SdfValue.class, PdbValue.class),

	@SuppressWarnings("unchecked")
	MOL_ANY(RDKitMolValue.class, MolValue.class, SmilesValue.class, SdfValue.class),

	@SuppressWarnings("unchecked")
	MOL_ANY_INCL_QUERY(RDKitMolValue.class, MolValue.class, SmilesValue.class, SdfValue.class, SmartsValue.class),

	@SuppressWarnings("unchecked")
	MOL_FROM_MOLBLOCK(MolValue.class, SdfValue.class),

	@SuppressWarnings("unchecked")
	RXN_ANY(RDKitReactionValue.class, SmilesValue.class, RxnValue.class, SmartsValue.class),

	@SuppressWarnings("unchecked")
	RXN_Non_RDKit(SmilesValue.class, SmartsValue.class, RxnValue.class),

	@SuppressWarnings("unchecked")
	RXN_Non_RDKit_QUERY(SmartsValue.class, RxnValue.class),

	@SuppressWarnings("unchecked")
	PDB(PdbValue.class);

	private RdkitCompatibleColumnFormats(
			@SuppressWarnings("unchecked") Class<? extends DataValue>... dataValues) {
		if (dataValues == null || dataValues.length == 0) {
			throw new IllegalArgumentException(
					"At least one DataValue type must be supplied");
		}
		m_colFormats = new ArrayList<>();
		Collections.addAll(m_colFormats, dataValues);

	}

	/** The acceptable molecule input formats */
	private ArrayList<Class<? extends DataValue>> m_colFormats;

	/** Get the acceptable column types */
	public ArrayList<Class<? extends DataValue>> getTypes() {
		return m_colFormats;
	}

	/** Get the acceptable column types as an array */
	public Class<? extends DataValue>[] getTypesArray() {
		return getTypes().toArray(new Class[0]);
	}

	@Override
	public boolean includeColumn(DataColumnSpec colSpec) {
		DataType colType = colSpec.getType();
		for (Class<? extends DataValue> valClass : m_colFormats) {
			if (colType.isCompatible(valClass)
					|| colType.isAdaptable(valClass)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String allFilteredMsg() {
		return "No columns of the appropriate type"
				+ (m_colFormats.size() > 1 ? "s" : "")
				+ " (" + m_colFormats.stream().map(clz -> clz.getSimpleName())
						.collect(Collectors.joining(", "))
				+ ") found in input table";
	}

	/**
	 * Utility method to get an RDKit molecule from a DataCell
	 * 
	 * @param cell
	 *            The data cell
	 * @return Molecule, or {@code null} if the molecule could not be created
	 * @throws RowExecutionException
	 *             If an exception was thrown during molecule creation
	 */
	public static final ROMol getRDKitObjectFromCell(DataCell cell)
			throws RowExecutionException {
		ROMol mol = null;
		if (cell.isMissing()) {
			return mol;
		}
		DataType type = cell.getType();
		try {
			if (type.isCompatible(RDKitMolValue.class)) {
				mol = ((RDKitMolValue) cell).readMoleculeValue();
			} else if (type.isCompatible(SmilesValue.class)) {
				RWMol rwMol = RWMol.MolFromSmiles(
						((SmilesValue) cell).getSmilesValue(), 0, false);
				if (rwMol == null) {
					return null;
				}
				RDKFuncs.sanitizeMol(rwMol);
				mol = rwMol;
			} else if (type.isCompatible(MolValue.class)) {
				mol = RWMol.MolFromMolBlock(((MolValue) cell).getMolValue(),
						true, false);
			} else if (type.isCompatible(SdfValue.class)) {
				mol = RWMol.MolFromMolBlock(((SdfValue) cell).getSdfValue(),
						true, false);
			} else if (type.isCompatible(PdbValue.class)) {
				mol = RWMol.MolFromPDBBlock(((PdbValue) cell).getPdbValue(),
						true, false);
			} else {
				throw new RowExecutionException(
						"Cell is not a recognised molecule type");
			}
		} catch (Exception e) {
			if (e instanceof RowExecutionException) {
				throw e;
			}
			String message = e.getMessage();
			if (message == null) {
				Class<?> clz = e.getClass();
				try {
					Method mtd = clz.getMethod("message");
					message = (String) mtd.invoke(e);
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException
						| SecurityException e1) {
					message = "<No Details>";
				}

			}
			if (message.equals("Cell is not a recognised molecule type")) {
				throw new RowExecutionException(message);
			} else {
				throw new RowExecutionException("Error in parsing molecule: "
						+ ((StringValue) cell).getStringValue() + " : "
						+ message);
			}
		}
		if (mol == null) {
			throw new RowExecutionException("Unable to get molecule from cell ("
					+ cell.toString() + ")");
		}
		return mol;
	}
}
