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

import org.knime.core.node.util.ButtonGroupEnumInterface;

public enum Alphabet implements ButtonGroupEnumInterface {
	Protein {

		@Override
		public AlphabetSmilesProvider getSmilesProvider(char c) {
			return ProteinAlphabet.valueOf(String.valueOf(c).toUpperCase());

		}

		@Override
		public AlphabetSmilesProvider getAnyResidueCode() {
			return ProteinAlphabet.X;
		}

		@Override
		public AlphabetSmilesProvider[] getValues(AlphabetSubsetType ast) {
			switch (ast) {
			case All:
				return ProteinAlphabet.values();
			case Canonical:
				return ProteinAlphabet.getCanonicalResidues();
			case Non_ambiguity:
				return ProteinAlphabet.getNonambiguityResidues();
			default:
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.Alphabet#
		 * supportsCaseSensitive()
		 */
		@Override
		public boolean supportsCaseSensitive() {
			return true;
		}

	},
	DNA {

		@Override
		public AlphabetSmilesProvider getSmilesProvider(char c) {
			return DnaAlphabet.valueOf(String.valueOf(c).toUpperCase());

		}

		@Override
		public AlphabetSmilesProvider getAnyResidueCode() {
			return DnaAlphabet.X;
		}

		@Override
		public AlphabetSmilesProvider[] getValues(AlphabetSubsetType ast) {
			switch (ast) {
			case All:
				return DnaAlphabet.values();
			case Canonical:
				return DnaAlphabet.getCanonicalBases();
			case Non_ambiguity:
				return DnaAlphabet.getNonambiguityBases();
			default:
				return null;
			}
		}
	},
	RNA {

		@Override
		public AlphabetSmilesProvider getSmilesProvider(char c) {
			return RnaAlphabet.valueOf(String.valueOf(c).toUpperCase());
		}

		@Override
		public AlphabetSmilesProvider getAnyResidueCode() {
			return RnaAlphabet.X;
		}

		@Override
		public AlphabetSmilesProvider[] getValues(AlphabetSubsetType ast) {
			switch (ast) {
			case All:
				return RnaAlphabet.values();
			case Canonical:
				return RnaAlphabet.getCanonicalBases();
			case Non_ambiguity:
				return RnaAlphabet.getNonambiguityBases();
			default:
				return null;
			}
		}
	},
	LNA {

		@Override
		public AlphabetSmilesProvider getSmilesProvider(char c) {
			return LnaAlphabet.valueOf(String.valueOf(c).toUpperCase());

		}

		@Override
		public AlphabetSmilesProvider getAnyResidueCode() {
			return LnaAlphabet.X;
		}

		@Override
		public AlphabetSmilesProvider[] getValues(AlphabetSubsetType ast) {
			switch (ast) {
			case All:
				return LnaAlphabet.values();
			case Canonical:
				return LnaAlphabet.getCanonicalBases();
			case Non_ambiguity:
				return LnaAlphabet.getNonambiguityBases();
			default:
				return null;
			}
		}
	},
	PNA {

		@Override
		public AlphabetSmilesProvider getSmilesProvider(char c) {

			return PnaAlphabet.valueOf(String.valueOf(c).toUpperCase());

		}

		@Override
		public AlphabetSmilesProvider getAnyResidueCode() {
			return PnaAlphabet.X;
		}

		@Override
		public AlphabetSmilesProvider[] getValues(AlphabetSubsetType ast) {
			switch (ast) {
			case All:
				return PnaAlphabet.values();
			case Canonical:
				return PnaAlphabet.getCanonicalBases();
			case Non_ambiguity:
				return PnaAlphabet.getNonambiguityBases();
			default:
				return null;
			}
		}
	};

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return name();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public static Alphabet getDefault() {
		return Protein;
	}

	public abstract AlphabetSmilesProvider getSmilesProvider(char c)
			throws IllegalArgumentException;

	public abstract AlphabetSmilesProvider getAnyResidueCode();

	public boolean supportsCaseSensitive() {
		return getAnyResidueCode().getSMILES(false)
				.equals(getAnyResidueCode().getSMILES(true));
	}

	public abstract AlphabetSmilesProvider[] getValues(AlphabetSubsetType ast);

}
