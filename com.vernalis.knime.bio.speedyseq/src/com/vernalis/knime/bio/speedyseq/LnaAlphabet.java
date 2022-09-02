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

public enum LnaAlphabet implements AlphabetSmilesProvider {
	A("P(=O)([O-])OC[C@H]14O[C@@H](n2cnc3c2ncnc3N)[C@H](OC4)[C@@H]1O") {

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** B = Not A, best SMILES is an 'any' */
	B("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), C("P(=O)([O-])OC[C@H]14O[C@@H](n2c(=O)nc(N)cc2)[C@H](OC4)[C@@H]1O") {

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** D = Not C, best SMILES is an 'any' */
	D("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), G("P(=O)([O-])OC[C@H]14O[C@@H](n2cnc3c2nc(N)[nH]c3=O)[C@H](OC4)[C@@H]1O") {

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** H = Not G, best SMILES is an 'any' */
	H("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), I("P(=O)([O-])OC[C@H]14O[C@@H](n2cnc3c2nc[nH]c3=O)[C@H](OC4)[C@@H]1O"),
	/** K = 'Keto' (G, T, U), best SMILES is an 'any' */
	K("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"),
	/** M = 'Amino' (A or C), best SMILES is an 'any' */
	M("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), N("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), R("P(=O)([O-])OC[C@H]14O[C@@H](n2cnc3c2nc([*])nc3[*])[C@H](OC4)[C@@H]1O"),
	/** S = 'Strong' (C or G), best SMILES is an 'any' */
	S("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), T("P(=O)([O-])OC[C@H]14O[C@@H](n2c(=O)[nH]c(=O)c(C)c2)[C@H](OC4)[C@@H]1O") {

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	U("P(=O)([O-])OC[C@H]14O[C@@H](n2c(=O)[nH]c(=O)cc2)[C@H](OC4)[C@@H]1O"),
	/** V = Not U, best SMILES is an 'any' */
	V("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O"), X("P(=O)([O-])OC[C@H]14O[C@@H]([*])[C@H](OC4)[C@@H]1O");

	private final String smi;

	private LnaAlphabet(String smi) {
		this.smi = smi;
	}

	@Override
	public String getSMILES(boolean isLowerCase) {
		return smi;
	}

	@Override
	public String getChainTerminator() {
		return "";
	}

	@Override
	public String getChainInitiator() {
		return "[O-]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
	 * AlphabetSmilesProvider#getOneLetterCode()
	 */
	@Override
	public char getOneLetterCode() {
		return name().charAt(0);
	}

	public static LnaAlphabet[] getCanonicalBases() {
		return Arrays.stream(LnaAlphabet.values()).filter(x -> x.isCanonical())
				.toArray(x -> new LnaAlphabet[x]);
	}

	public static LnaAlphabet[] getNonambiguityBases() {
		return Arrays.stream(LnaAlphabet.values()).filter(x -> !x.isAmbiguity())
				.toArray(x -> new LnaAlphabet[x]);
	}
}
