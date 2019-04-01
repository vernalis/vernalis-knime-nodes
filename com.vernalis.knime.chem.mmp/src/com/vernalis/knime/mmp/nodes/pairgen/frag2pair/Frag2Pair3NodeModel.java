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
package com.vernalis.knime.mmp.nodes.pairgen.frag2pair;

import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;

import com.vernalis.knime.mmp.frags.simple.SimpleFragmentKey;
import com.vernalis.knime.mmp.frags.simple.SimpleFragmentValue;
import com.vernalis.knime.mmp.nodes.pairgen.abstrct.AbstractMatchedPairsFromFragmentsNodeModel;

/**
 * The node model for the Fragments to Pairs nodes
 * 
 * @author s.roughley
 *
 */
public class Frag2Pair3NodeModel extends AbstractMatchedPairsFromFragmentsNodeModel {

	/**
	 * Constructor
	 * 
	 * @param hasSecondInputTable
	 *            Does the node have a second input table (i.e. is a 'Reference'
	 *            node, which generates pairs only between input tables, not
	 *            within individual tables)
	 */
	public Frag2Pair3NodeModel(boolean hasSecondInputTable) {
		super(true, hasSecondInputTable);
	}

	@Override
	protected boolean rowsArePair(DataRow leftRow, DataRow rightRow, SimpleFragmentKey leftKey,
			SimpleFragmentKey rightKey, SimpleFragmentValue leftVal, SimpleFragmentValue rightVal,
			String leftID, String rightID) {
		// We just need to check whether the IDs matter, and if they do that
		// they are different
		return m_AllowSelfTransforms.getBooleanValue() || !leftVal.getID().equals(rightVal.getID());
	}

	@Override
	protected void addImplentationColSpecs(DataTableSpec inTableSpec, List<DataColumnSpec> specs)
			throws InvalidSettingsException {
		// Nothing to do

	}

	@Override
	protected void addImplementationColCells(List<DataCell> cells, DataRow leftRow,
			DataRow rightRow, SimpleFragmentValue leftVal, SimpleFragmentValue rightVal,
			SimpleFragmentKey leftKey, SimpleFragmentKey rightKey) {
		// Nothing to do

	}

}
