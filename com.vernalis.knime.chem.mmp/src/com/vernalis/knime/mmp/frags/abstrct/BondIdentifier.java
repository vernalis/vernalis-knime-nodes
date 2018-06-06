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
package com.vernalis.knime.mmp.frags.abstrct;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple container class to hold a matching bond by it's start and end atom
 * IDs, along with the bond ID, and a fragmentation index
 * 
 * @author s.roughley
 *
 */
public class BondIdentifier implements Comparable<BondIdentifier>, Iterable<Integer> {

	protected final int StartIdx;
	protected final int EndIdx;
	protected final long bondIdx;
	private Integer fragmentationIndex = null;
	private BondIdentifier reverse = null;

	/**
	 * Constructor from the atom and bond indices. The fragmentation index is
	 * not set and initialises with the value <code>null</code>
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
	 * Full constructor
	 * 
	 * @param startIdx
	 *            The index of the atom at the start of the bond
	 * @param endIdx
	 *            The index of the atom at the end of the bond
	 * @param bondIdx
	 *            The index of the bond
	 * @param fragmentationIdx
	 *            The index to mark the fragmentation with
	 */
	public BondIdentifier(int startIdx, int endIdx, long bondIdx, Integer fragmentationIdx) {
		this(startIdx, endIdx, bondIdx);
		this.fragmentationIndex = fragmentationIdx;
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
	 * @param idx
	 *            The index at one end of the bond
	 * @return The index at the other end of the bond, or -1 if idx was not at
	 *         either end
	 */
	public int getOtherIdx(int idx) {
		if (idx == StartIdx) {
			return EndIdx;
		} else if (idx == EndIdx) {
			return StartIdx;
		}
		return -1;
	}

	/**
	 * This methods returns the bond idx at the time of creation. <b>NB</b> If
	 * bonds have been deleted from the molecule since the time of creation,
	 * then this pointer will be unreliable at best
	 * 
	 * @return the bondIdx
	 */
	public long getBondIdx() {
		return bondIdx;
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

	@Override
	public String toString() {
		return "(" + StartIdx + "," + EndIdx + ")";
	}

	@Override
	public int compareTo(BondIdentifier o) {
		if (StartIdx != o.StartIdx) {
			return StartIdx - o.StartIdx;
		}
		return EndIdx - o.EndIdx;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + EndIdx;
		result = prime * result + StartIdx;
		return result;
	}

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
	class AtomIterator implements Iterator<Integer> {
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

	/**
	 * @return The 'reverse' {@link BondIdentifier}, i.e. the same bond, but
	 *         with start and end IDs swapped. The reverse is cached and this
	 *         object becomes stored as the reverse of the new object
	 */
	public BondIdentifier getReverse() {
		if (reverse == null) {
			reverse = new BondIdentifier(EndIdx, StartIdx, bondIdx, fragmentationIndex);
			reverse.reverse = this;
		}
		return reverse;
	}

	/**
	 * Method to check if a {@link BondIdentifier} is the reverse of the current
	 * bond - only start and End IDs are checked
	 * 
	 * @param other
	 *            The other {@link BondIdentifier} to check
	 * @return <code>true</code> if the start/end IDs are swapped
	 */
	public boolean isReverse(BondIdentifier other) {
		return other.EndIdx == this.StartIdx && other.StartIdx == this.EndIdx && other.equals(this);
	}
}