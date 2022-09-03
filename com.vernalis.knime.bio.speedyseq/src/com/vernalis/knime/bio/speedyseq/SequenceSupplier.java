/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.bio.speedyseq;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SequenceSupplier implements Iterator<String> {

	protected byte[] counters;
	// private final BitSet uCase;
	protected final AlphabetSmilesProvider[] values;
	protected final int length;
	protected final boolean caseSensitive;
	protected final byte counterStart;

	public SequenceSupplier(AlphabetSmilesProvider[] values, int sequenceLength,
			boolean caseSensitive) {
		this.length = sequenceLength;
		this.caseSensitive = caseSensitive;
		counterStart = (byte) (caseSensitive ? -values.length : 0);
		if (values == null || values.length == 0) {
			this.values = null;
			counters = null;
		} else {
			this.values = Arrays.copyOf(values, values.length);
			counters = new byte[length];
			// uCase=new BitSet(length);
			if (caseSensitive) {
				for (int i = 0; i < length; i++) {
					counters[i] = counterStart;
				}
			}
		}

	}

	@Override
	public boolean hasNext() {
		return counters != null;
	}

	@Override
	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		String retVal = convertStateToString();
		incrementCounters();

		return retVal;
	}

	private void incrementCounters() {
		for (int i = 0; i < length; i++) {
			counters[i]++;
			if (counters[i] < values.length) {
				// Done!
				return;
			}

			// need to change the next thing...
			counters[i] = counterStart;
			/*
			 * if(caseSensitive) { if(!uCase.get(i)){ //If lower case, move to
			 * upper case uCase.set(i); return; } else { //Reset the case flag
			 * and move to the next position uCase.set(i, false); } }
			 */
		}
		// If we are here, then we have exhausted all values...
		counters = null;
	}

	protected String convertStateToString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			boolean lCase = counters[i] < 0;
			int index = lCase ? -counters[i] - 1 : counters[i];
			char chr = values[index].getOneLetterCode();
			sb.append(lCase ? Character.toLowerCase(chr) : chr);
		}
		return sb.toString();
	}

	public double size() {
		int multiplier = caseSensitive ? 2 * values.length : values.length;
		return Math.pow(multiplier, length);
	}
}
