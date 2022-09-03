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
package com.vernalis.knime.bio.speedyseq.nodes.seq2smi;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.bio.speedyseq.Alphabet;
import com.vernalis.knime.bio.speedyseq.TypeAgnosticAlphabet;

public class Seq2SmiNodeDialog extends DefaultNodeSettingsPane {

	static enum SpecialChars {
		SPACE(' '), PERIOD('.'), PLUS('+'), MINUS('-'), ASTERISK('*');

		private final char chr;

		private SpecialChars(char chr) {
			this.chr = chr;
		}

		public String getName() {
			return "<" + name() + "> '" + getChar() + "'";
		}

		public char getChar() {
			return chr;
		}
	}

	private static final String SEQUENCE_COLUMN = "Sequence Column";
	private static final String CASE_SENSITIVE_PARSING =
			"Case sensitive parsing";
	private static final String SELECT_SEQUENCE_ALPHABET =
			"Select sequence alphabet";

	@SuppressWarnings("unchecked")
	public Seq2SmiNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				createColNameModel(), SEQUENCE_COLUMN, 0, StringValue.class));
		addDialogComponent(new DialogComponentButtonGroup(createAlphabetModel(),
				SELECT_SEQUENCE_ALPHABET, true, Alphabet.values()));
		addDialogComponent(new DialogComponentBoolean(createLowerCaseModel(),
				CASE_SENSITIVE_PARSING));

		createNewTab("Special Characters");
		for (SpecialChars sc : SpecialChars.values()) {
			addDialogComponent(new DialogComponentButtonGroup(
					createCharModel(sc.getName()), sc.getName(), true,
					TypeAgnosticAlphabet.values()));
		}
	}

	static SettingsModelString createCharModel(String name) {
		return new SettingsModelString(name,
				TypeAgnosticAlphabet.getDefault().getActionCommand());
	}

	static final SettingsModelBoolean createLowerCaseModel() {
		return new SettingsModelBoolean(CASE_SENSITIVE_PARSING, false);
	}

	static final SettingsModelString createColNameModel() {
		return new SettingsModelString(SEQUENCE_COLUMN, null);
	}

	static final SettingsModelString createAlphabetModel() {
		return new SettingsModelString(SELECT_SEQUENCE_ALPHABET,
				Alphabet.getDefault().getActionCommand());
	}
}
