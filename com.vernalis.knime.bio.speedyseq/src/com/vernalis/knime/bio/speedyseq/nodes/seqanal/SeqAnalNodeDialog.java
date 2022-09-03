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
package com.vernalis.knime.bio.speedyseq.nodes.seqanal;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

import com.vernalis.knime.bio.speedyseq.Alphabet;
import com.vernalis.knime.bio.speedyseq.AlphabetSubsetType;

public class SeqAnalNodeDialog extends DefaultNodeSettingsPane {

	private static final String REPORT_PERCENTAGES = "Report percentages";
	private static final String SEQUENCE_COLUMN = "Sequence Column";
	private static final String CASE_SENSITIVE_PARSING =
			"Case sensitive parsing";
	private static final String SEQUENCE_ALPHABET = "Sequence alphabet";
	private static final String ALPHABET_SUBSET = "Alphabet Subset";
	private static final String REPORT_COUNTS = "Report Counts";
	@SuppressWarnings("unchecked")
	static final ColumnFilter STRING_FILTER =
			new DataValueColumnFilter(StringValue.class);

	public SeqAnalNodeDialog() {
		addDialogComponent(new DialogComponentColumnNameSelection(
				createColNameModel(), SEQUENCE_COLUMN, 0, STRING_FILTER));
		addDialogComponent(new DialogComponentButtonGroup(createAlphabetModel(),
				SEQUENCE_ALPHABET, true, Alphabet.values()));
		addDialogComponent(
				new DialogComponentButtonGroup(createAlphabetSubsetModel(),
						ALPHABET_SUBSET, true, AlphabetSubsetType.values()));
		addDialogComponent(new DialogComponentBoolean(createLowerCaseModel(),
				CASE_SENSITIVE_PARSING));
		addDialogComponent(
				new DialogComponentBoolean(createCountsModel(), REPORT_COUNTS));
		addDialogComponent(new DialogComponentBoolean(createPercentModel(),
				REPORT_PERCENTAGES));

	}

	static SettingsModelBoolean createCountsModel() {
		return new SettingsModelBoolean(REPORT_COUNTS, true);
	}

	static SettingsModelBoolean createPercentModel() {
		return new SettingsModelBoolean(REPORT_PERCENTAGES, true);
	}

	static final SettingsModelBoolean createLowerCaseModel() {
		return new SettingsModelBoolean(CASE_SENSITIVE_PARSING, false);
	}

	static final SettingsModelString createColNameModel() {
		return new SettingsModelString(SEQUENCE_COLUMN, null);
	}

	static final SettingsModelString createAlphabetModel() {
		return new SettingsModelString(SEQUENCE_ALPHABET,
				Alphabet.getDefault().getActionCommand());
	}

	static SettingsModelString createAlphabetSubsetModel() {
		return new SettingsModelString(ALPHABET_SUBSET,
				AlphabetSubsetType.getDefault().getActionCommand());
	}
}
