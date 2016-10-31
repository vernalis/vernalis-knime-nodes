/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.RDKit.Match_Vect;
import org.RDKit.ROMol;

import com.vernalis.knime.swiggc.ISWIGObjectGarbageCollector;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector;

/**
 * A class to hold the start and end atom IDs together, along with the bond ID
 * at time of creation. The object is iterable, iterating through the integer
 * atom IDs in the order start, end
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class BondIdentifier implements Comparable<BondIdentifier>, Iterable<Integer> {
	private final int StartIdx, EndIdx;
	private final long bondIdx;
	private final ISWIGObjectGarbageCollector m_SWIG_GC = new SWIGObjectGarbageCollector();
	private Integer fragmentationIndex = null;

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
	public BondIdentifier(Match_Vect match, ROMol mol) {
		StartIdx = match.get(0).getSecond();
		EndIdx = match.get(1).getSecond();
		bondIdx = m_SWIG_GC.markForCleanup(mol.getBondBetweenAtoms(StartIdx, EndIdx)).getIdx();
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
	public BondIdentifier(int startIdx, int endIdx, long bondIdx) {
		StartIdx = startIdx;
		EndIdx = endIdx;
		this.bondIdx = bondIdx;
	}

	/**
	 * @return The index of the start atom
	 */
	public int getStartIdx() {
		return StartIdx;
	}

	/**
	 * @return The index of the end atom
	 */
	public int getEndIdx() {
		return EndIdx;
	}

	/**
	 * This methods returns the bond idx at the time of creation. <b>NB</b> If
	 * bonds have been deleted from the molecule since the time of creation,
	 * then this pointer will be unreliable at best
	 * 
	 * @see #getBondIdx(ROMol)
	 * @return the bondIdx
	 */
	public long getBondIdx() {
		return bondIdx;
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

	/**
	 * The fragmentation index is a temporary value assigned during molecule
	 * fragmentation
	 * 
	 * @return the fragmentationIndex
	 */
	public Integer getFragmentationIndex() {
		return fragmentationIndex;
	}

	/**
	 * The fragmentation index is a temporary value assigned during molecule
	 * fragmentation
	 * 
	 * @param fragmentationIndex
	 *            the fragmentationIndex to set
	 */
	public void setFragmentationIndex(Integer fragmentationIndex) {
		this.fragmentationIndex = fragmentationIndex;
	}

	/**
	 * Method to check whether two bonds have a common atom.
	 * 
	 * @param bond
	 *            A {@link BondIdentifier} for a second bond
	 * @return {@code true} if at least one of the two atoms is common to both
	 *         bonds
	 */
	public boolean hasSharedAtomWith(BondIdentifier bond) {
		return this.StartIdx == bond.StartIdx || this.StartIdx == bond.EndIdx
				|| this.EndIdx == bond.StartIdx || this.EndIdx == bond.EndIdx;
	}

	/**
	 * Test whether either atom of the bond is the atom indicated
	 * 
	 * @param atomIdx
	 *            The test atom
	 * @return {@code true} if either start or end atom of this bond is the
	 *         index supplied
	 */
	public boolean isToAtomWithIdx(int atomIdx) {
		return (StartIdx == atomIdx || EndIdx == atomIdx);

	}

	/**
	 * Test whether either atom of the bond is the atom indicated
	 * 
	 * @param atomIdx
	 *            The test atom
	 * @return {@code true} if either start or end atom of ths bond is the index
	 *         shown
	 */
	public boolean isToAtomWithIdx(long atomIdx) {
		return (StartIdx == atomIdx || EndIdx == atomIdx);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "(" + StartIdx + "," + EndIdx + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(BondIdentifier o) {
		if (StartIdx != o.StartIdx) {
			return StartIdx - o.StartIdx;
		}
		return EndIdx - o.EndIdx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + EndIdx;
		result = prime * result + StartIdx;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BondIdentifier other = (BondIdentifier) obj;
		if (EndIdx != other.EndIdx)
			return false;
		if (StartIdx != other.StartIdx)
			return false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The iterator iterates over the start/end atoms in order
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new AtomIterator();
	}

	/**
	 * The iterator for the {@link BondIdentifier}. The iterator iterates over
	 * the atom IDs in the order start / end
	 * 
	 * @author s.roughley
	 * 
	 */
	private class AtomIterator implements Iterator<Integer> {
		private int cursor;

		public AtomIterator() {
			cursor = 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return cursor == 0 || cursor == 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public Integer next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}
			cursor++;
			if (cursor == 1) {
				return StartIdx;
			}
			return EndIdx;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			// Does nothing
			throw new UnsupportedOperationException();
		}

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
