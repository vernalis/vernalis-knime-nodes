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

import java.util.SortedMap;
import java.util.TreeMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.bio.speedyseq.Alphabet;
import com.vernalis.knime.bio.speedyseq.AlphabetSmilesProvider;
import com.vernalis.knime.bio.speedyseq.AlphabetSubsetType;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.knime.bio.core.BioConstants.SEQUENCE_ALPHABET;
import static com.vernalis.knime.bio.core.BioConstants.SEQUENCE_ALPHABET_SUBSET;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.STRING_FILTER;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.createAlphabetModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.createAlphabetSubsetModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.createColNameModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.createCountsModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.createLowerCaseModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seqanal.SeqAnalNodeDialog.createPercentModel;

public class SeqAnalNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColNameModel());
	private final SettingsModelString alphabetMdl =
			registerSettingsModel(createAlphabetModel());
	private final SettingsModelString alphabetSubsetMdl =
			registerSettingsModel(createAlphabetSubsetModel());
	private final SettingsModelBoolean caseSensitiveMdl =
			registerSettingsModel(createLowerCaseModel());
	private final SettingsModelBoolean percentMdl =
			registerSettingsModel(createPercentModel());
	private final SettingsModelBoolean countsMdl =
			registerSettingsModel(createCountsModel());

	/**
	 * 
	 */
	SeqAnalNodeModel() {

	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {

		if (!percentMdl.getBooleanValue() && !countsMdl.getBooleanValue()) {
			throw new InvalidSettingsException(
					"Must calculate at least one of %ages or counts!");
		}

		// Find / guess column
		int seqColIdx = getValidatedColumnSelectionModelColumnIndex(colNameMdl,
				STRING_FILTER, spec, getLogger());
		DataColumnProperties seqColProps =
				spec.getColumnSpec(seqColIdx).getProperties();

		// Identify the alphabet to be used
		final Alphabet alphabet;
		try {
			alphabet = Alphabet.valueOf(alphabetMdl.getStringValue());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + alphabetMdl.getStringValue()
							+ "' is not a valid alphabet selection");
		}

		try {
			final Alphabet colPropAlphabet =
					Alphabet.valueOf(seqColProps.getProperty(SEQUENCE_ALPHABET,
							alphabetMdl.getStringValue()));
			if (alphabet != colPropAlphabet) {
				setWarningMessage("Selected sequence alphabet '"
						+ alphabet.getActionCommand()
						+ "' differs from column property value ("
						+ colPropAlphabet.getActionCommand() + ")");
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			setWarningMessage("Sequence property '" + SEQUENCE_ALPHABET
					+ "' not a valid alphabet name");
		}

		final AlphabetSubsetType alphabetSubsetType;
		try {
			alphabetSubsetType = AlphabetSubsetType
					.valueOf(alphabetSubsetMdl.getStringValue());
		} catch (Exception e) {
			throw new InvalidSettingsException(
					"Error with unknown Alphabet sub-type setting - "
							+ e.getMessage());
		}

		try {
			final AlphabetSubsetType colPropAlphabetSubsetType =
					AlphabetSubsetType.valueOf(
							seqColProps.getProperty(SEQUENCE_ALPHABET_SUBSET,
									alphabetSubsetMdl.getStringValue()));
			if (alphabetSubsetType != colPropAlphabetSubsetType) {
				setWarningMessage("Selected sequence alphabet subtype '"
						+ alphabetSubsetType.getActionCommand()
						+ "' differs from column property value ("
						+ colPropAlphabetSubsetType.getActionCommand() + ")");
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			setWarningMessage("Sequence property '" + SEQUENCE_ALPHABET_SUBSET
					+ "' not a valid alphabet subset name");
		}

		final AlphabetSmilesProvider[] asps =
				alphabet.getValues(alphabetSubsetType);
		int newColCnt = 1/* Length column */
				+ ((percentMdl.getBooleanValue() && countsMdl.getBooleanValue())
						? 2
						: 1) * (caseSensitiveMdl.getBooleanValue() ? 2 : 1)
						* asps.length;

		DataColumnSpec[] newColSpecs = new DataColumnSpec[newColCnt];
		int colIdx = 0;

		for (AlphabetSmilesProvider asp : asps) {
			if (countsMdl.getBooleanValue()) {
				newColSpecs[colIdx++] = new DataColumnSpecCreator(
						DataTableSpec.getUniqueColumnName(spec,
								"Count " + asp.getOneLetterCode()),
						IntCell.TYPE).createSpec();
			}
			if (percentMdl.getBooleanValue()) {
				newColSpecs[colIdx++] =
						new DataColumnSpecCreator(
								DataTableSpec.getUniqueColumnName(spec,
										"% " + asp.getOneLetterCode()),
								DoubleCell.TYPE).createSpec();
			}
			if (caseSensitiveMdl.getBooleanValue()) {
				if (countsMdl.getBooleanValue()) {
					newColSpecs[colIdx++] = new DataColumnSpecCreator(
							DataTableSpec.getUniqueColumnName(spec,
									"Count " + Character.toLowerCase(
											asp.getOneLetterCode())),
							IntCell.TYPE).createSpec();
				}
				if (percentMdl.getBooleanValue()) {
					newColSpecs[colIdx++] = new DataColumnSpecCreator(
							DataTableSpec.getUniqueColumnName(spec,
									"% " + Character.toLowerCase(
											asp.getOneLetterCode())),
							DoubleCell.TYPE).createSpec();
				}
			}
		}
		newColSpecs[colIdx++] = new DataColumnSpecCreator(
				DataTableSpec.getUniqueColumnName(spec, "Length"), IntCell.TYPE)
						.createSpec();

		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.append(new AbstractCellFactory(true, newColSpecs) {

			@Override
			public DataCell[] getCells(DataRow row) {
				DataCell seqCell = row.getCell(seqColIdx);

				DataCell[] retVal = ArrayUtils.fill(new DataCell[newColCnt],
						DataType.getMissingCell());
				if (seqCell.isMissing()) {
					return retVal;
				}
				if (!(seqCell instanceof StringValue)) {
					// We should never be able to get here
					setWarningMessage("Non-string cell found (Row: "
							+ row.getKey().getString() + ")");
					return retVal;
				}

				// This is the quickest way to count - see
				// https://www.programcreek.com/2013/10/efficient-counter-in-java/
				// The comparator puts in case-insensitive alphabetical order,
				// with lower case following the equivalent upper case, e.g.
				// AaBbCc etc
				SortedMap<Character, int[]> counts =
						new TreeMap<>(/*
										 * new java.util.Comparator<Character>()
										 * {
										 * 
										 * @Override public int
										 * compare(Character a, Character b) {
										 * int retVal = Character.compare(
										 * Character.toUpperCase(a),
										 * Character.toUpperCase(b)); if (retVal
										 * == 0) { return Character.compare(a,
										 * b); } return retVal; } }
										 */);
				for (AlphabetSmilesProvider asp : asps) {
					counts.put(asp.getOneLetterCode(),
							new int[caseSensitiveMdl.getBooleanValue() ? 2
									: 1]);
				}

				int residueCount = 0;
				String seq = ((StringValue) seqCell).getStringValue();

				if (!caseSensitiveMdl.getBooleanValue()) {
					seq = seq.toUpperCase();
				}

				for (int i = 0; i < seq.length(); i++) {
					char c = seq.charAt(i);
					char key = Character.toUpperCase(c);
					int[] rCount = counts.get(key);
					if (rCount != null) {
						// Legal character
						residueCount++;
						if (Character.isLowerCase(c)) {
							rCount[1]++;
						} else {
							rCount[0]++;
						}
					} else if (c != ' ' && c != '*' && c != '.' && c != '+'
							&& c != '-') {
						setWarningMessage(
								"Illegal character encountered in sequence in row '"
										+ row.getKey().getString() + "' - '" + c
										+ "'");
						return retVal;
					}
				}

				int cellIdx = 0;
				for (int[] rCount : counts.values()) {
					for (int rCount0 : rCount) {
						if (countsMdl.getBooleanValue()) {
							retVal[cellIdx++] = new IntCell(rCount0);
						}
						if (percentMdl.getBooleanValue()) {
							retVal[cellIdx++] = new DoubleCell(
									100.0 * rCount0 / residueCount);
						}
					}
				}
				retVal[cellIdx++] = new IntCell(residueCount);
				return retVal;
			}
		});
		return rearranger;
	}

}
