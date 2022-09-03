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
public enum PnaAlphabet implements AlphabetSmilesProvider {

	A("NCCN(C(=O)Cn2cnc3c2ncnc3N)CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return T;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}

	},
	/** B = Not A, best SMILES is an 'any' */
	B("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return V;
		}

	},
	C("NCCN(C(=O)Cn2c(=O)nc(N)cc2)CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return G;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** D = Not C, best SMILES is an 'any' */
	D("NCCN(C(=O)C[*])C[C@@H]1O") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return H;
		}

	},
	G("NCCN(C(=O)Cn2cnc3c2nc(N)[nH]c3=O)CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return C;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	/** H = Not G, best SMILES is an 'any' */
	H("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return D;
		}

	},
	/** K = 'Keto' (G, T, U), best SMILES is an 'any' */
	K("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return M;
		}

	},
	/** M = 'Amino' (A or C), best SMILES is an 'any' */
	M("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return K;
		}

	},
	N("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return N;
		}

	},
	/** S = 'Strong' (C or G), best SMILES is an 'any' */
	S("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return S;
		}

	},
	T("NCCN(C(=O)Cn2c(=O)[nH]c(=O)c(C)c2)CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return A;
		}

		@Override
		public boolean isCanonical() {
			return true;
		}
	},
	U("NCCN(C(=O)Cn2c(=O)[nH]c(=O)cc2)CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			// By analogy with T
			return A;
		}

	},
	/** V = Not U, best SMILES is an 'any' */
	V("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return B;
		}

	},
	X("NCCN(C(=O)C[*])CC(=O)") {

		@Override
		public PnaAlphabet getComplement(PnaAlphabet base) {
			return X;
		}

	};

	private final String smi;

	private PnaAlphabet(String smi) {
		this.smi = smi;
	}

	@Override
	public String getSMILES(boolean isLowerCase) {
		return smi;
	}

	@Override
	public String getChainTerminator() {
		return "O";
	}

	@Override
	public String getChainInitiator() {
		return "";
	}

	public abstract PnaAlphabet getComplement(PnaAlphabet base);

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

	public static PnaAlphabet[] getCanonicalBases() {
		return Arrays.stream(PnaAlphabet.values()).filter(x -> x.isCanonical())
				.toArray(x -> new PnaAlphabet[x]);
	}

	public static PnaAlphabet[] getNonambiguityBases() {
		return Arrays.stream(PnaAlphabet.values()).filter(x -> !x.isAmbiguity())
				.toArray(x -> new PnaAlphabet[x]);
	}
}
