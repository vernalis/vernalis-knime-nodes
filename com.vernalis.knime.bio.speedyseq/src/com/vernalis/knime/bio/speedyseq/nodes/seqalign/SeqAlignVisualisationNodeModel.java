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

import java.io.IOException;
import java.util.Deque;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.knime.base.data.xml.SvgCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.misc.StringParseUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.SEQUENCE_COLUMN_FILTER;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createFirstColNameModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createGapColourModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createGapEmboldenModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createGapSpacingModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createMatchColourModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createMatchEmboldenModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createMismatchColourModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createMismatchEmboldenModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createResiduesPerLineModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqalign.SeqAlignVisualisationNodeDialog.createSecondColNameModel;

public class SeqAlignVisualisationNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	// TODO: Add an option to output html. <text> becomes <p> and <tspan>
	// becomes <span>
	private final SettingsModelString firstColNameMdl =
			registerSettingsModel(createFirstColNameModel());
	private final SettingsModelString secondColNameMdl =
			registerSettingsModel(createSecondColNameModel());
	private final SettingsModelIntegerBounded gapSpacingMdl =
			registerSettingsModel(createGapSpacingModel());
	private final SettingsModelIntegerBounded residuesPerLineMdl =
			registerSettingsModel(createResiduesPerLineModel());
	private final SettingsModelColor matchColourMdl =
			registerSettingsModel(createMatchColourModel());
	private final SettingsModelBoolean matchEmboldenMdl =
			registerSettingsModel(createMatchEmboldenModel());
	private final SettingsModelColor mismatchColourMdl =
			registerSettingsModel(createMismatchColourModel());
	private final SettingsModelBoolean mismatchEmboldenMdl =
			registerSettingsModel(createMismatchEmboldenModel());
	private final SettingsModelColor gapColourMdl =
			registerSettingsModel(createGapColourModel());
	private final SettingsModelBoolean gapEmboldenMdl =
			registerSettingsModel(createGapEmboldenModel());
	/**
	 * The number of lines of text per row of sequence. Currently 3 = 2 sequence
	 * rows + 1 blank row
	 */
	private static final int LINES_PER_ROW = 3;
	/** The height in em units per line of text */
	private static final double HEIGHT_PER_LINE = 1.2;
	/**
	 * The x/y ratio of the font. The value of 47.5/80 was experimentally
	 * determined for Courier New font, in order to calculate the width of the
	 * svg in em units
	 */
	private static final double FONT_ASPECT_RATIO = 47.5 / 80;
	/** The system line separator character(s) */
	private static final String NL = System.lineSeparator();
	/** The tab character */
	private static final char TAB = '\t';
	private static final String STYLE_OPEN_CSS_FONT_FAMILY_FMT =
			"\t<style type=\"text/css\">%n\t\tsvg {%n\t\t\tfont-family: monospace;%n";
	private static final String CSS_WHITE_SPACE_FMT =
			"\t\t\twhite-space: pre;%n";
	private static final String CSS_FILL_FMT = "\t\t\tfill: rgb(%d,%d,%d);%n";
	private static final String CSS_FONT_WEIGHT_FMT =
			"\t\t\tfont-weight: %s;%n";
	private static final String NORMAL = "normal";
	private static final String BOLD = "bold";
	private static final String CLOSE_STYLE = "</style>";
	private static final String TSPAN_CLASS_MISMATCH =
			"<tspan class='mismatch'>";
	private static final String TSPAN_CLASS_GAP = "<tspan class='gap'>";
	/** The close tspan tag */
	private static final String CLOSE_TSPAN = "</tspan>";
	private static final String TEXT_OPEN_TAG_FMT = "<text y='%.1fem'> ";
	/** The close text tag */
	private static final String CLOSE_TEXT = "</text>";
	private static final String CLOSE_SVG = "</svg>";
	private static final String SVG_OPEN_TAG_FMT =
			"<svg xmlns=\"http://www.w3.org/2000/svg\" height='%.2fem' width='%.2fem'>%n";
	private static final String XML_VERSION_1_0_ENCODING_ISO_8859_1 =
			"<?xml version='1.0' encoding='iso-8859-1'?>";

	/**
	 * 
	 */
	protected SeqAlignVisualisationNodeModel() {

	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		// Find the possible columns
		Deque<String> colNames = spec.stream().filter(
				colSpec -> SEQUENCE_COLUMN_FILTER.includeColumn(colSpec))
				.map(colSpec -> colSpec.getName())
				.collect(Collectors.toCollection(LinkedList::new));
		if (colNames.size() < 2) {
			throw new InvalidSettingsException(
					"Need two Sequence columns in input spec");
		}

		// Check that if we have columns selected they are in the list of
		// compatible columns, and are unique
		if (isStringModelFilled(secondColNameMdl)) {
			if (!colNames.contains(secondColNameMdl.getStringValue())) {
				throw new InvalidSettingsException(
						"The selected second sequence column '"
								+ secondColNameMdl.getStringValue()
								+ "' is not available in the input table, or is not of the correct type");
			} else {
				colNames.remove(secondColNameMdl.getStringValue());
			}
		}
		if (isStringModelFilled(firstColNameMdl)) {
			if (!colNames.contains(firstColNameMdl.getStringValue())) {
				if (firstColNameMdl.getStringValue()
						.equals(secondColNameMdl.getStringValue())) {
					throw new InvalidSettingsException(
							"Two different sequence columns must be selected");
				}
				throw new InvalidSettingsException(
						"The selected first sequence column '"
								+ firstColNameMdl.getStringValue()
								+ "' is not available in the input table, or is not of the correct type");
			} else {
				colNames.remove(firstColNameMdl.getStringValue());
			}
		}

		// Now we can guess any missings, without duplication...
		if (!isStringModelFilled(secondColNameMdl)) {
			// Guess..
			secondColNameMdl.setStringValue(colNames.removeLast());
			setWarningMessage("Auto-guessing second column name model - '"
					+ secondColNameMdl.getStringValue() + "' selected");
		}
		if (!isStringModelFilled(firstColNameMdl)) {
			// Guess..
			firstColNameMdl.setStringValue(colNames.removeLast());
			setWarningMessage("Auto-guessing first column name model - '"
					+ firstColNameMdl.getStringValue() + "' selected");
		}

		int firstColIdx =
				spec.findColumnIndex(firstColNameMdl.getStringValue());
		int secondColIdx =
				spec.findColumnIndex(secondColNameMdl.getStringValue());

		DataColumnSpec newColSpec =
				new DataColumnSpecCreator(
						DataTableSpec.getUniqueColumnName(spec,
								firstColNameMdl.getStringValue() + " - "
										+ secondColNameMdl.getStringValue()
										+ " alignment"),
						SvgCellFactory.TYPE).createSpec();
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.append(new SingleCellFactory(true, newColSpec) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell lSeqCell = row.getCell(firstColIdx);
				DataCell rSeqCell = row.getCell(secondColIdx);
				if (lSeqCell.isMissing() || rSeqCell.isMissing()) {
					return DataType.getMissingCell();
				}
				String lSeq = ((StringValue) lSeqCell).getStringValue();
				String rSeq = ((StringValue) rSeqCell).getStringValue();
				final int seqLength = Math.max(lSeq.length(), rSeq.length());
				rSeq = StringParseUtils.rightPad(rSeq, seqLength);
				lSeq = StringParseUtils.rightPad(lSeq, seqLength);
				int numCols;
				int numRows;
				if (residuesPerLineMdl.getIntValue() > 0) {
					numCols = Math.min(seqLength,
							residuesPerLineMdl.getIntValue());
					numRows = (int) Math.ceil((double) seqLength
							/ residuesPerLineMdl.getIntValue());
				} else {
					numCols = seqLength;
					numRows = seqLength > 0 ? 1 : 0;
				}
				numCols += 2;// a space at each end!
				// Now account for extra spaces
				if (gapSpacingMdl.getIntValue() > 0) {
					numCols += numCols / gapSpacingMdl.getIntValue();
				}

				StringBuilder svg =
						new StringBuilder(XML_VERSION_1_0_ENCODING_ISO_8859_1)
								.append(NL);
				Formatter svgFmt = new Formatter(svg);
				svgFmt.format(SVG_OPEN_TAG_FMT,
						numRows * SeqAlignVisualisationNodeModel.LINES_PER_ROW
								* SeqAlignVisualisationNodeModel.HEIGHT_PER_LINE,
						numCols * SeqAlignVisualisationNodeModel.FONT_ASPECT_RATIO);

				// Match format (the default)
				svgFmt.format(STYLE_OPEN_CSS_FONT_FAMILY_FMT);
				svgFmt.format(CSS_FONT_WEIGHT_FMT,
						matchEmboldenMdl.getBooleanValue() ? BOLD : NORMAL);
				svgFmt.format(CSS_WHITE_SPACE_FMT);
				svgFmt.format(CSS_FILL_FMT,
						matchColourMdl.getColorValue().getRed(),
						matchColourMdl.getColorValue().getGreen(),
						matchColourMdl.getColorValue().getBlue());
				svg.append(TAB).append(TAB).append("}").append(NL);

				// Gap format
				svg.append(TAB).append(TAB).append(".gap {").append(NL);
				svgFmt.format(CSS_FONT_WEIGHT_FMT,
						gapEmboldenMdl.getBooleanValue() ? BOLD : NORMAL);
				svgFmt.format(CSS_FILL_FMT,
						gapColourMdl.getColorValue().getRed(),
						gapColourMdl.getColorValue().getGreen(),
						gapColourMdl.getColorValue().getBlue());
				svg.append(TAB).append(TAB).append("}").append(NL);

				// Mismatch format
				svg.append(TAB).append(TAB).append(".mismatch {").append(NL);
				svgFmt.format(CSS_FONT_WEIGHT_FMT,
						mismatchEmboldenMdl.getBooleanValue() ? BOLD : NORMAL);
				svgFmt.format(CSS_FILL_FMT,
						mismatchColourMdl.getColorValue().getRed(),
						mismatchColourMdl.getColorValue().getGreen(),
						mismatchColourMdl.getColorValue().getBlue());
				svg.append(TAB).append(TAB).append("}").append(NL);
				svg.append(TAB).append(CLOSE_STYLE).append(NL);

				// Now add a couple of descs
				svgFmt.format("\t<desc class='%s sequence'>%s</desc>%n",
						"First", lSeq.trim());
				svgFmt.format("\t<desc class='%s sequence'>%s</desc>%n",
						"Second", rSeq.trim());

				// svg.append(TAB)
				// .append("<rect x='0' height='100%' y='0' width='100%'
				// style='fill:none;stroke:red;'/>")
				// .append(NL);

				// Now we add the sequences
				boolean isMisMatch = false;
				boolean isGap = false;
				int line = 0;
				int col = 0;
				StringBuilder lSeqSvg = new StringBuilder(
						String.format(TEXT_OPEN_TAG_FMT, HEIGHT_PER_LINE));
				StringBuilder rSeqSvg = new StringBuilder(
						String.format(TEXT_OPEN_TAG_FMT, 2 * HEIGHT_PER_LINE));

				for (int i = 0; i < seqLength; i++) {
					char lChar = lSeq.charAt(i);
					char rChar = rSeq.charAt(i);
					if (lChar == '-' || rChar == '-') {
						// Now in a gap
						if (!isGap) {
							if (isMisMatch) {
								lSeqSvg.append(CLOSE_TSPAN);
								rSeqSvg.append(CLOSE_TSPAN);
							}
							lSeqSvg.append(TSPAN_CLASS_GAP);
							rSeqSvg.append(TSPAN_CLASS_GAP);
						}
						isGap = true;
						isMisMatch = false;
					} else if (lChar != rChar) {
						// Now in a mismatch
						if (!isMisMatch) {
							if (isGap) {
								lSeqSvg.append(CLOSE_TSPAN);
								rSeqSvg.append(CLOSE_TSPAN);
							}
							lSeqSvg.append(TSPAN_CLASS_MISMATCH);
							rSeqSvg.append(TSPAN_CLASS_MISMATCH);
						}
						isGap = false;
						isMisMatch = true;
					} else if (isMisMatch || isGap) {
						// Now in neither, but need to close
						lSeqSvg.append(CLOSE_TSPAN);
						rSeqSvg.append(CLOSE_TSPAN);
						isMisMatch = false;
						isGap = false;
					}

					// Now we append the sequence characters
					lSeqSvg.append(lChar);
					rSeqSvg.append(rChar);
					col++;

					// Now we check if we need any spaces of a new line
					if (gapSpacingMdl.getIntValue() > 0
							&& (i + 1) % gapSpacingMdl.getIntValue() == 0) {
						lSeqSvg.append(' ');
						rSeqSvg.append(' ');
					}
					if (residuesPerLineMdl.getIntValue() > 0
							&& col % residuesPerLineMdl.getIntValue() == 0) {
						if (isMisMatch || isGap) {
							lSeqSvg.append(CLOSE_TSPAN);
							rSeqSvg.append(CLOSE_TSPAN);
						}
						lSeqSvg.append(CLOSE_TEXT);
						rSeqSvg.append(CLOSE_TEXT);
						svg.append(TAB).append(lSeqSvg).append(NL);
						svg.append(TAB).append(rSeqSvg).append(NL);
						line++;
						col = 0;
						isGap = false;
						isMisMatch = false;
						lSeqSvg = new StringBuilder()
								.append(String.format(TEXT_OPEN_TAG_FMT,
										LINES_PER_ROW * HEIGHT_PER_LINE * line
												+ HEIGHT_PER_LINE));
						rSeqSvg = new StringBuilder()
								.append(String.format(TEXT_OPEN_TAG_FMT,
										LINES_PER_ROW * HEIGHT_PER_LINE * line
												+ 2 * HEIGHT_PER_LINE));
					}
				}
				if (col > 0) {
					if (isMisMatch || isGap) {
						lSeqSvg.append(CLOSE_TSPAN);
						rSeqSvg.append(CLOSE_TSPAN);
					}
					lSeqSvg.append(CLOSE_TEXT);
					rSeqSvg.append(CLOSE_TEXT);
					svg.append(TAB).append(lSeqSvg).append(NL);
					svg.append(TAB).append(rSeqSvg).append(NL);
				}
				svg.append(CLOSE_SVG);
				svgFmt.close();
				try {
					return SvgCellFactory.create(svg.toString());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return rearranger;
	}

}
