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

public class MaskedSequenceSupplier extends SequenceSupplier {

	private final String mask;

	public MaskedSequenceSupplier(AlphabetSmilesProvider[] values,
			int sequenceLength, boolean caseSensitive, String mask) {
		super(values,
				sequenceLength
						- countNonDash(trimMaskToLength(mask, sequenceLength)),
				caseSensitive);
		this.mask = mask;
	}

	private static String trimMaskToLength(String str, int length) {
		return str.length() > length ? str.substring(0, length) : str;
	}

	private static int countNonDash(String str) {
		return (int) str.chars().filter(x -> (char) x != '-').count();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.bio.speedyseq.SequenceSupplier#
	 * convertStateToString()
	 */
	@Override
	protected String convertStateToString() {
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for (int i = 0; i < length; i++) {
			char c;
			while (idx < mask.length() && (c = mask.charAt(idx)) != '-') {
				sb.append(c);
				idx++;
			}
			boolean lCase = counters[i] < 0;
			int index = lCase ? -counters[i] - 1 : counters[i];
			char chr = values[index].getOneLetterCode();
			sb.append(lCase ? Character.toLowerCase(chr) : chr);
			idx++;
		}

		// Add any remaining mask characters at the end of the pattern
		char c;
		while (idx < mask.length() && (c = mask.charAt(idx)) != '-') {
			sb.append(c);
			idx++;
		}
		return sb.toString();
	}

}
