/*******************************************************************************
 * Copyright (c) 2015, 2017, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp;

import org.RDKit.Match_Vect;
import org.RDKit.ROMol;

import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2;

/**
 * A class to hold the start and end atom IDs together, along with the bond ID
 * at time of creation. The object is iterable, iterating through the integer
 * atom IDs in the order start, end
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * @deprecated Class is no longer required as superclass handles all
 *             requirements
 */
@Deprecated
public class RDKitBondIdentifier extends com.vernalis.knime.mmp.frags.abstrct.BondIdentifier {
	private final SWIGObjectGarbageCollector2 m_SWIG_GC = new SWIGObjectGarbageCollector2();

	/**
	 * Constructor from an RDKit {@link Match_Vect} object and {@link ROMol}
	 * molecule. The user must dispose of the argument objects
	 * 
	 * @param match
	 *            The {@link Match_Vect} containing pointers to the bond. NB if
	 *            this is longer than 2 atoms, only the first two will be used
	 * @param mol
	 *            The {@link ROMol} molecule object
	 */
	@Deprecated
	public RDKitBondIdentifier(Match_Vect match, ROMol mol) {
		super(match.get(0).getSecond(), match.get(1).getSecond(), mol
				.getBondBetweenAtoms(match.get(0).getSecond(), match.get(1).getSecond()).getIdx());
		m_SWIG_GC.cleanupMarkedObjects();
	}

	/**
	 * Constructor from the atom and bond indices
	 * 
	 * @param startIdx
	 *            The index of the atom at the start of the bond
	 * @param endIdx
	 *            The index of the atom at the end of the bond
	 * @param bondIdx
	 *            The index of the bond
	 */
	public RDKitBondIdentifier(int startIdx, int endIdx, long bondIdx) {
		super(startIdx, endIdx, bondIdx);
	}

	/**
	 * This method looks up the bond index from the supplied {@link ROMol}
	 * object, based on the start and end indices
	 * 
	 * @param mol
	 *            The molecule to lookup
	 * @return The current bond index
	 */
	public long getBondIdx(ROMol mol) {
		long retVal = m_SWIG_GC.markForCleanup(mol.getBondBetweenAtoms(StartIdx, EndIdx)).getIdx();
		m_SWIG_GC.cleanupMarkedObjects();
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		m_SWIG_GC.quarantineAndCleanupMarkedObjects();
		super.finalize();
	}
}
