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
package com.vernalis.knime.chem.speedysmiles.nodes.count.chiralcentres;

import java.util.regex.Pattern;

import com.vernalis.knime.chem.speedysmiles.helpers.CalculatedProperty;
import com.vernalis.knime.chem.speedysmiles.helpers.FlagProvider;

/**
 * Enum containing calculation of chiral centre-related properties for the
 * SpeedySMILES Chiral Centre Count node
 * 
 * @author S.Roughley
 *
 */
public enum ChiralCentreCount implements CalculatedProperty<Integer>, FlagProvider {
	ALL("All Absolute Centres") {

		@Override
		public Integer calculate(String SMILES) {

			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					cnt++;
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			// n/a for this type
			return -1L;
		}

		@Override
		public int max() {
			// n/a for this type
			return -1;
		}

		@Override
		public int bits() {
			// n/a for this type
			return -1;
		}

	},

	UNLABELLED("@/@@/@@@ etc Only") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					String nextTwo = "" + SMILES.charAt(i + 1) + SMILES.charAt(i + 2);
					if (!labels.matcher(nextTwo).matches()) {
						cnt++;
					}
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			return 1L;
		}

		@Override
		public int max() {
			return 2;
		}

		@Override
		public int bits() {
			return 1;
		}
	},

	AL("@AL1-2 (Allenyl)") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					if (SMILES.charAt(i + 1) == 'A' && SMILES.charAt(i + 2) == 'L') {
						cnt++;
					}
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			return 1L;
		}

		@Override
		public int max() {
			return 2;
		}

		@Override
		public int bits() {
			return 1;
		}
	},

	OH("@OH1-30 (Octahedral)") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					if (SMILES.charAt(i + 1) == 'O' && SMILES.charAt(i + 2) == 'H') {
						cnt++;
					}
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			return 31L;
		}

		@Override
		public int max() {
			return 30;
		}

		@Override
		public int bits() {
			return 5;
		}
	},

	SP("@SP1-3 (Square Planar)") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					if (SMILES.charAt(i + 1) == 'S' && SMILES.charAt(i + 2) == 'P') {
						cnt++;
					}
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			return 3L;
		}

		@Override
		public int max() {
			return 3;
		}

		@Override
		public int bits() {
			return 2;
		}
	},

	TB("@TB1-20 (Trigonal Bipyramidal") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					if (SMILES.charAt(i + 1) == 'T' && SMILES.charAt(i + 2) == 'B') {
						cnt++;
					}
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			return 31L;
		}

		@Override
		public int max() {
			return 20;
		}

		@Override
		public int bits() {
			return 5;
		}
	},

	TH("@TH1-2 (Tetrahedral)") {

		@Override
		public Integer calculate(String SMILES) {
			int cnt = 0;
			// NB define length as l as otherwise smiles.length
			// is re-calculated on each iteration, apparently
			for (int i = 0, l = SMILES.length(); i < l; i++) {
				char x = SMILES.charAt(i);
				if (x == '@') {
					if (SMILES.charAt(i + 1) == 'T' && SMILES.charAt(i + 2) == 'H') {
						cnt++;
					}
					// skip following
					while (SMILES.charAt(i) == '@') {
						i++;
					}
				}
			}
			return cnt;
		}

		@Override
		public long mask() {
			return 1L;
		}

		@Override
		public int max() {
			return 2;
		}

		@Override
		public int bits() {
			return 1;
		}
	};

	private String displayName;

	private ChiralCentreCount(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String displayName() {
		return displayName;
	}

	private static final Pattern labels = Pattern.compile("(TH|AL|SP|TB|OH)");
}
