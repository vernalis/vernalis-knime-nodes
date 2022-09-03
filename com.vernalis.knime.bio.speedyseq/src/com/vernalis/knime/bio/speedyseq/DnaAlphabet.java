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

/**
 * DNA Alphabet. Ambiguity codes and complements are assigned as described at
 * http://www.dnabaser.com/articles/IUPAC%20ambiguity%20codes.html
 * 
 * @author S.Roughley
 *
 */
public enum DnaAlphabet
		implements ComplementableAlphabetSmilesProvider<DnaAlphabet> {
	A("P(=O)([O-])OC[C@H]1O[C@@H](n2cnc3c2ncnc3N)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return T;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
		 * AlphabetSmilesProvider#isCanonical()
		 */
		@Override
		public boolean isCanonical() {
			return true;
		}

	},
	/** B = Not A, best SMILES is an 'any' */
	B("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return V;
		}

	},
	C("P(=O)([O-])OC[C@H]1O[C@@H](n2c(=O)nc(N)cc2)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return G;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
		 * AlphabetSmilesProvider#isCanonical()
		 */
		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** D = Not C, best SMILES is an 'any' */
	D("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return H;
		}

	},
	G("P(=O)([O-])OC[C@H]1O[C@@H](n2cnc3c2nc(N)[nH]c3=O)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return C;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
		 * AlphabetSmilesProvider#isCanonical()
		 */
		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** H = Not G, best SMILES is an 'any' */
	H("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return D;
		}

	},
	I("P(=O)([O-])OC[C@H]1O[C@@H](n2cnc3c2nc[nH]c3=O)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			// https://en.wikipedia.org/wiki/Inosine - prefers C, although can
			// pair anything
			return C;
		}
	},
	/** K = 'Keto' (G, T, U), best SMILES is an 'any' */
	K("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return M;
		}

	},
	/** M = 'Amino' (A or C), best SMILES is an 'any' */
	M("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return K;
		}

	},
	N("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return N;
		}

	},
	R("P(=O)([O-])OC[C@H]1O[C@@H](n2cnc3c2nc([*])nc3[*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return Y;
		}
	},
	/** S = 'Strong' (C or G), best SMILES is an 'any' */
	S("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return S;
		}

	},
	T("P(=O)([O-])OC[C@H]1O[C@@H](n2c(=O)[nH]c(=O)c(C)c2)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return A;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
		 * AlphabetSmilesProvider#isCanonical()
		 */
		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	U("P(=O)([O-])OC[C@H]1O[C@@H](n2c(=O)[nH]c(=O)cc2)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			// By analogy with T
			return A;
		}

	},
	/** V = Not U, best SMILES is an 'any' */
	V("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return B;
		}

	},
	/** W = 'Weak', best SMILES is 'any' */
	W("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return W;
		}
	},
	X("P(=O)([O-])OC[C@H]1O[C@@H]([*])C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return X;
		}

	},
	/** Y = Pyrimidine */
	Y("P(=O)([O-])OC[C@H]1O[C@@H](n2c([*])nc([*])c([*])c2)C[C@@H]1O") {

		@Override
		public DnaAlphabet getComplement(DnaAlphabet base) {
			return R;
		}
	};

	private final String smi;

	private DnaAlphabet(String smi) {
		this.smi = smi;
	}

	@Override
	public String getSMILES(boolean isLowerCase) {
		return isLowerCase ? smi.replace(")C[C@@H]1O", ")[C@H](O)[C@@H]1O")
				: smi;
	}

	@Override
	public String getChainTerminator() {
		return "";
	}

	@Override
	public String getChainInitiator() {
		return "[O-]";
	}

	@Override
	public abstract DnaAlphabet getComplement(DnaAlphabet base);

	public RnaAlphabet getTranscripton(DnaAlphabet base) {
		return RnaAlphabet.valueOf(getComplement(base).name());
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

	public static DnaAlphabet[] getCanonicalBases() {
		return Arrays.stream(DnaAlphabet.values()).filter(x -> x.isCanonical())
				.toArray(x -> new DnaAlphabet[x]);
	}

	public static DnaAlphabet[] getNonambiguityBases() {
		return Arrays.stream(DnaAlphabet.values()).filter(x -> !x.isAmbiguity())
				.toArray(x -> new DnaAlphabet[x]);
	}
}
