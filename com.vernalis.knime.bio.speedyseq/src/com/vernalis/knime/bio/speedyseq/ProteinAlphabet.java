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

public enum ProteinAlphabet implements AlphabetSmilesProvider {
	A("N[C@@H](C)C(=O)"), B("N[C@@H](CC(=O)[*])C(=O)"), C("N[C@@H](CS)C(=O)"),
	D("N[C@@H](CC(=O)O)C(=O)"), E("N[C@@H](CCC(=O)O)C(=O)"),
	F("N[C@@H](Cc1ccccc1)C(=O)"), G("NCC(=O)"), H("N[C@@H](Cc1nc[nH]c1)C(=O)"),
	I("N[C@@H]([C@@H](C)CC)C(=O)"), J("N[C@@H]([C@@H]([*])C([*])C)C(=O)"),
	K("N[C@@H](CCCCN)C(=O)"), L("N[C@@H](CC(C)C)C(=O)"),
	M("N[C@@H](CCSC)C(=O)"), N("N[C@@H](CC(=O)N)C(=O)"),
	O("N[C@@H](CCCCNC(=O)[C@H]1[C@H](C)CC=N1)C(=O)") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.vernalis.knime.bio.speedyseq.AlphabetSmilesProvider#
		 * isCanonical()
		 */
		@Override
		public boolean isCanonical() {
			return false;
		}
	},
	P("N1[C@@H](CCC1)C(=O)"), Q("N[C@@H](CCC(=O)N)C(=O)"),
	R("N[C@@H](CCCNC(=N)N)C(=O)"), S("N[C@@H](CO)C(=O)"),
	T("N[C@@H]([C@H](O)C)C(=O)"), U("N[C@@H](C[SeH])C(=O)") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.vernalis.knime.bio.speedyseq.AlphabetSmilesProvider#
		 * isCanonical()
		 */
		@Override
		public boolean isCanonical() {
			return false;
		}
	},
	V("N[C@@H](C(C)C)C(=O)"), W("N[C@@H](Cc1c[nH]c2ccccc12)C(=O)"),
	X("N[C@@H]([*])C(=O)"), Y("N[C@@H](Cc1ccc(O)cc1)C(=O)"),
	Z("N[C@@H](CCC(=O)[*])C(=O)");

	private String smi;

	private ProteinAlphabet(String smi) {
		this.smi = smi;
	}

	@Override
	public String getSMILES(boolean isLowerCase) {
		// If isLowerCase, we invert the 1st (backbone) stereocentre
		return isLowerCase ? smi.replaceFirst("N(1)?\\[C@@", "N$1[C@") : smi;
	}

	@Override
	public String getChainTerminator() {
		return "O";
	}

	@Override
	public String getChainInitiator() {
		return "";
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.bio.speedyseq.AlphabetSmilesProvider#
	 * isCanonical()
	 */
	@Override
	public boolean isCanonical() {
		return !isAmbiguity();
	}

	public static ProteinAlphabet[] getCanonicalResidues() {
		return Arrays.stream(ProteinAlphabet.values())
				.filter(x -> x.isCanonical())
				.toArray(x -> new ProteinAlphabet[x]);
	}

	public static ProteinAlphabet[] getNonambiguityResidues() {
		return Arrays.stream(ProteinAlphabet.values())
				.filter(x -> !x.isAmbiguity())
				.toArray(x -> new ProteinAlphabet[x]);
	}
}
