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
package com.vernalis.knime.bio.speedyseq.nodes.seqalign;

import java.awt.Color;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColorChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ColumnFilter;
import org.knime.core.node.util.DataValueColumnFilter;

public class SeqAlignVisualisationNodeDialog extends DefaultNodeSettingsPane {

	private static final String BOLD = "Bold";
	private static final String MATCH_COLOUR = "Match Colour";
	private static final String RESIDUES_PER_LINE = "Residues per line";
	private static final String SPACE_EVERY_N_RESIDUES =
			"Space every n residues";
	private static final String SECOND_SEQUENCE_COLUMN =
			"Second Sequence Column";
	private static final String FIRST_SEQUENCE_COLUMN = "First Sequence Column";
	@SuppressWarnings("unchecked")
	static final ColumnFilter SEQUENCE_COLUMN_FILTER =
			new DataValueColumnFilter(StringValue.class);
	private static final String MISMATCH_COLOUR = "Mismatch Colour";
	private static final String GAP_COLOUR = "Gap Colour";

	/**
	 * 
	 */
	protected SeqAlignVisualisationNodeDialog() {
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColumnNameSelection(
				createFirstColNameModel(), FIRST_SEQUENCE_COLUMN, 0,
				SEQUENCE_COLUMN_FILTER));
		addDialogComponent(new DialogComponentColumnNameSelection(
				createSecondColNameModel(), SECOND_SEQUENCE_COLUMN, 0,
				SEQUENCE_COLUMN_FILTER));
		setHorizontalPlacement(false);
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentNumber(createGapSpacingModel(),
				SPACE_EVERY_N_RESIDUES, 1));
		addDialogComponent(new DialogComponentNumber(
				createResiduesPerLineModel(), RESIDUES_PER_LINE, 10));
		setHorizontalPlacement(false);
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColorChooser(
				createMatchColourModel(), MATCH_COLOUR, true));
		addDialogComponent(
				new DialogComponentBoolean(createMatchEmboldenModel(), BOLD));
		setHorizontalPlacement(false);
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColorChooser(
				createMismatchColourModel(), MISMATCH_COLOUR, true));
		addDialogComponent(new DialogComponentBoolean(
				createMismatchEmboldenModel(), BOLD));
		setHorizontalPlacement(false);
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentColorChooser(
				createGapColourModel(), GAP_COLOUR, true));
		addDialogComponent(
				new DialogComponentBoolean(createGapEmboldenModel(), BOLD));
		setHorizontalPlacement(false);

	}

	static SettingsModelBoolean createGapEmboldenModel() {
		return new SettingsModelBoolean("Gap " + BOLD, false);
	}

	static SettingsModelBoolean createMismatchEmboldenModel() {
		return new SettingsModelBoolean("Match " + BOLD, false);
	}

	static SettingsModelBoolean createMatchEmboldenModel() {
		return new SettingsModelBoolean("Mismatch " + BOLD, false);
	}

	static SettingsModelColor createGapColourModel() {
		return new SettingsModelColor(GAP_COLOUR, Color.RED);
	}

	static SettingsModelColor createMismatchColourModel() {
		return new SettingsModelColor(MISMATCH_COLOUR, Color.ORANGE);
	}

	static SettingsModelColor createMatchColourModel() {
		return new SettingsModelColor(MATCH_COLOUR, Color.BLACK);
	}

	static SettingsModelIntegerBounded createResiduesPerLineModel() {
		return new SettingsModelIntegerBounded(RESIDUES_PER_LINE, 80, 0, 120);
	}

	static SettingsModelIntegerBounded createGapSpacingModel() {
		return new SettingsModelIntegerBounded(SPACE_EVERY_N_RESIDUES, 10, 0,
				100);
	}

	static SettingsModelString createSecondColNameModel() {
		return new SettingsModelString(SECOND_SEQUENCE_COLUMN, null);
	}

	static SettingsModelString createFirstColNameModel() {
		return new SettingsModelString(FIRST_SEQUENCE_COLUMN, null);
	}

}
