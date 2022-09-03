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

public enum TypeAgnosticAlphabet
		implements AlphabetSmilesProvider, ButtonGroupEnumInterface {
	GAP("A 'gap' containing any SMILES Atoms should be inserted") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
		 * AlphabetSmilesProvider#getSMILES(boolean)
		 */
		@Override
		public String getSMILES(boolean isLowerCase) {
			return "[*]";
		}
	},
	BREAK("A break (SMILES '.') should be inserted and a new chain started") {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.knime.bio.speedyseq.nodes.seq2smi.
		 * AlphabetSmilesProvider#getSMILES(boolean)
		 */
		@Override
		public String getSMILES(boolean isLowerCase) {
			return ".";
		}
	},
	SKIP("The character should be skipped") {

		@Override
		public String getSMILES(boolean isLowerCase) {
			return "";
		}
	},
	ANY("The 'Any' symbol for the sequence type should be used") {

		@Override
		public String getSMILES(boolean isLowerCase) {
			// We redirect to the relevant alphabet
			return "";
		}
	},
	TERMINATE("The sequence should be terminated at the preceding residue") {

		@Override
		public String getSMILES(boolean isLowerCase) {
			// We redirect to the relevant alphabet
			return "";
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.bio.speedyseq.AlphabetSmilesProvider#
	 * getOneLetterCode()
	 */
	@Override
	public char getOneLetterCode() {
		return 0;
	}

	private final String tooltip;

	private TypeAgnosticAlphabet(String tooltip) {
		this.tooltip = tooltip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getText()
	 */
	@Override
	public String getText() {
		return name() + " (" + getToolTip() + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getActionCommand()
	 */
	@Override
	public String getActionCommand() {
		return name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#getToolTip()
	 */
	@Override
	public String getToolTip() {
		return tooltip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.util.ButtonGroupEnumInterface#isDefault()
	 */
	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public static TypeAgnosticAlphabet getDefault() {
		return SKIP;
	}

	@Override
	public String getChainTerminator() {
		return "";
	}

	@Override
	public String getChainInitiator() {
		return "";
	}
}
