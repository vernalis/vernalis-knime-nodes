/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.nodes.count.charge;

import com.vernalis.knime.chem.speedysmiles.helpers.CalculatedProperty;

/**
 * Enum containing calculation of charge-related properties for the SpeedySMILES
 * ChargeCount node
 * 
 * @author S.Roughley
 *
 */
public enum ChargeCount implements CalculatedProperty<Integer> {
	TOTAL_POSITIVE("Total Positive Charge") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '+') {
					if (Character.isDigit(SMILES.charAt(i + 1))) {
						cnt += Character.getNumericValue(SMILES.charAt(i + 1));
					} else {
						cnt++;
					}
				}
			}
			return cnt;
		}
	},

	TOTAL_NEGATIVE("Total Negative Charge") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			boolean inAtomParentheses = false;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '[') {
					inAtomParentheses = true;
					continue;
				}
				if (x == ']') {
					inAtomParentheses = false;
					continue;
				}
				if (inAtomParentheses && x == '-') {
					if (Character.isDigit(SMILES.charAt(i + 1))) {
						cnt += Character.getNumericValue(SMILES.charAt(i + 1));
					} else {
						cnt++;
					}
				}
			}
			return cnt;
		}
	},

	TOTAL_NET("Total Net Charge") {

		@Override
		public Integer calculate(String SMILES) {
			return TOTAL_POSITIVE.calculate(SMILES) - TOTAL_NEGATIVE.calculate(SMILES);
		}
	},

	TOTAL_GROSS("Total Gross Charge") {

		@Override
		public Integer calculate(String SMILES) {
			return TOTAL_POSITIVE.calculate(SMILES) + TOTAL_NEGATIVE.calculate(SMILES);
		}
	},

	BIGGEST_POSITIVE("Biggest Positive Charge") {

		@Override
		public Integer calculate(String SMILES) {
			int max = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '+') {
					if (Character.isDigit(SMILES.charAt(i + 1))) {
						max = Math.max(max, Character.getNumericValue(SMILES.charAt(i + 1)));
					} else {
						int cnt = 0;
						while (SMILES.charAt(i) == '+') {
							i++;
							cnt++;
						}
						max = Math.max(max, cnt);
					}
				}
			}
			return max;
		}
	},

	BIGGEST_NEGATIVE("Biggest Negative Charge") {

		@Override
		public Integer calculate(String SMILES) {
			int max = 0;
			boolean inAtomParentheses = false;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '[') {
					inAtomParentheses = true;
					continue;
				}
				if (x == ']') {
					inAtomParentheses = false;
					continue;
				}
				if (inAtomParentheses && x == '-') {
					if (Character.isDigit(SMILES.charAt(i + 1))) {
						max = Math.max(max, Character.getNumericValue(SMILES.charAt(i + 1)));
					} else {
						int cnt = 0;
						while (SMILES.charAt(i) == '-') {
							i++;
							cnt++;
						}
						max = Math.max(max, cnt);
					}
				}
			}
			return max;
		}
	},

	BIGGEST_ABSOLUTE("Biggest Absolute Charge") {

		@Override
		public Integer calculate(String SMILES) {
			return Math.max(BIGGEST_POSITIVE.calculate(SMILES), BIGGEST_NEGATIVE.calculate(SMILES));
		}
	};

	private String displayName;

	private ChargeCount(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String displayName() {
		return displayName;
	}

}
