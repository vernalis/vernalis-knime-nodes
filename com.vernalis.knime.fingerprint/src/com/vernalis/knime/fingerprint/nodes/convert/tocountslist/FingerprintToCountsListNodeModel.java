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
package com.vernalis.knime.fingerprint.nodes.convert.tocountslist;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.vector.bytevector.ByteVectorValue;

import com.vernalis.knime.fingerprint.nodes.abstrct.model.AbstractSingleFingerprintSingleOutputNodeModel;

/**
 * @author "Stephen Roughley knime@vernalis.com"
 * 
 */
public class FingerprintToCountsListNodeModel extends AbstractSingleFingerprintSingleOutputNodeModel {

	public FingerprintToCountsListNodeModel() {
		super(false, false, true, true, ListCell.getCollectionType(IntCell.TYPE));
	}

	@Override
	protected String getResultColumnName() {
		return m_firstColName.getStringValue() + " (List of Counts)";
	}

	@Override
	protected DataCell getByteVectorResultCell(DataCell fp1) throws UnsupportedOperationException {
		ByteVectorValue fp = (ByteVectorValue) fp1;
		List<IntCell> cells = new ArrayList<>();
		for (long idx = 0; idx < fp.length(); idx++) {
			cells.add(new IntCell(fp.get(idx)));
		}
		return CollectionCellFactory.createListCell(cells);
	}

}
