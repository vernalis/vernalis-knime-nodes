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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.bio.speedyseq.Alphabet;
import com.vernalis.knime.bio.speedyseq.AlphabetSmilesProvider;
import com.vernalis.knime.bio.speedyseq.TypeAgnosticAlphabet;
import com.vernalis.knime.bio.speedyseq.nodes.seq2smi.Seq2SmiNodeDialog.SpecialChars;
import com.vernalis.knime.nodes.AbstractSimpleStreamableFunctionNodeModel;

import static com.vernalis.knime.bio.speedyseq.nodes.seq2smi.Seq2SmiNodeDialog.createAlphabetModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seq2smi.Seq2SmiNodeDialog.createCharModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seq2smi.Seq2SmiNodeDialog.createColNameModel;
import static com.vernalis.knime.bio.speedyseq.nodes.seq2smi.Seq2SmiNodeDialog.createLowerCaseModel;

public class Seq2SmiNodeModel
		extends AbstractSimpleStreamableFunctionNodeModel {

	private final SettingsModelString colNameMdl =
			registerSettingsModel(createColNameModel());
	private final SettingsModelString alphabetMdl =
			registerSettingsModel(createAlphabetModel());
	private final SettingsModelBoolean caseSensitiveMdl =
			createLowerCaseModel();
	private final Map<SpecialChars, SettingsModelString> specialCharModels =
			new EnumMap<>(SpecialChars.class);
	private final Map<Character, TypeAgnosticAlphabet> specialCharBehaviours =
			new HashMap<>();

	/**
	 * 
	 */
	Seq2SmiNodeModel() {
		for (SpecialChars sc : SpecialChars.values()) {
			specialCharModels.put(sc,
					registerSettingsModel(createCharModel(sc.getName())));
		}
	}

	@Override
	protected ColumnRearranger createColumnRearranger(DataTableSpec spec)
			throws InvalidSettingsException {
		// Identify the alphabet to be used
		final Alphabet alphabet;
		try {
			alphabet = Alphabet.valueOf(alphabetMdl.getStringValue());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'" + alphabetMdl.getStringValue()
							+ "' is not a valid alphabet selection");
		}

		specialCharBehaviours.clear();
		try {
			for (SpecialChars sc : SpecialChars.values()) {
				specialCharBehaviours.put(sc.getChar(), TypeAgnosticAlphabet
						.valueOf(specialCharModels.get(sc).getStringValue()));
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new InvalidSettingsException(
					"'Invalid special character behaviour selection - "
							+ e.getMessage());
		}

		// Find and check or guess the column selected
		if (colNameMdl.getStringValue() == null
				|| colNameMdl.getStringValue().isEmpty()) {
			// No column selected - guess last String column
			for (int i = spec.getNumColumns() - 1; i >= 0; i--) {
				DataColumnSpec colSpec = spec.getColumnSpec(i);
				if (colSpec.getType().isCompatible(StringValue.class)) {
					colNameMdl.setStringValue(colSpec.getName());
					setWarningMessage("No column selected - guessed '"
							+ colNameMdl.getStringValue() + "'");
					break;
				}
			}
			if (colNameMdl.getStringValue() == null
					|| colNameMdl.getStringValue().isEmpty()) {
				throw new InvalidSettingsException(
						"No string column found in input table");
			}
		} else {
			// We have a selection, so check it is present and of correct
			// type...
			DataColumnSpec colSpec =
					spec.getColumnSpec(colNameMdl.getStringValue());
			if (colSpec == null) {
				throw new InvalidSettingsException(
						"Selected column '" + colNameMdl.getStringValue()
								+ "' is not available in the input table");
			}
			if (!colSpec.getType().isCompatible(StringValue.class)) {
				throw new InvalidSettingsException(
						"Selected column '" + colNameMdl.getStringValue()
								+ "' is not a string column");
			}
		}

		int seqColIdx = spec.findColumnIndex(colNameMdl.getStringValue());
		ColumnRearranger rearranger = new ColumnRearranger(spec);
		rearranger.append(new SingleCellFactory(true,
				new DataColumnSpecCreator(DataTableSpec.getUniqueColumnName(
						spec, "Sequence SMILES"), SmilesAdapterCell.RAW_TYPE)
								.createSpec()) {

			@Override
			public DataCell getCell(DataRow row) {
				DataCell seqCell = row.getCell(seqColIdx);
				if (seqCell.isMissing()) {
					return DataType.getMissingCell();
				}
				if (!(seqCell instanceof StringValue)) {
					// We should never be able to get here
					setWarningMessage("Non-string cell found (Row: "
							+ row.getKey().getString() + ")");
					return DataType.getMissingCell();
				}
				String seq = ((StringValue) seqCell).getStringValue();
				if (seq.isEmpty()) {
					return DataType.getMissingCell();
				}
				if (!caseSensitiveMdl.getBooleanValue()) {
					seq = seq.toUpperCase();
				}

				try {
					char c = seq.charAt(0);
					AlphabetSmilesProvider smilesProvider = null;

					StringBuilder smiles = new StringBuilder();
					boolean needsInitiator = true;
					sequenceLoop: for (int i = 0; i < seq.length(); i++) {
						c = seq.charAt(i);
						AlphabetSmilesProvider newSmilesProvider;
						if (specialCharBehaviours.containsKey(c)) {
							newSmilesProvider = specialCharBehaviours.get(c);
							switch ((TypeAgnosticAlphabet) newSmilesProvider) {
							case ANY:
								newSmilesProvider =
										alphabet.getAnyResidueCode();
								break;
							case BREAK:
								if (smilesProvider != null) {
									smiles.append(smilesProvider
											.getChainTerminator());
								}
								break;
							case GAP:
								break;
							case SKIP:
								continue sequenceLoop;
							case TERMINATE:
								break sequenceLoop;
							default:
								break;
							}
						} else {
							newSmilesProvider = alphabet.getSmilesProvider(c);
						}

						if (needsInitiator) {
							smiles.append(
									newSmilesProvider.getChainInitiator());
							needsInitiator = false;
						}
						smiles.append(newSmilesProvider
								.getSMILES(caseSensitiveMdl.getBooleanValue()
										&& Character.isLowerCase(c)));
						if (newSmilesProvider == TypeAgnosticAlphabet.BREAK) {
							needsInitiator = true;
						}
						smilesProvider = newSmilesProvider;

					}
					smiles.append(smilesProvider.getChainTerminator());
					return SmilesCellFactory
							.createAdapterCell(smiles.toString());
				} catch (IllegalArgumentException e) {
					// Illegal sequence character encountered
					setWarningMessage("Unsupported sequence character in Row '"
							+ row.getKey().getString() + "'; "
							+ e.getMessage());
					return DataType.getMissingCell();
				}
			}
		});
		return rearranger;
	}

}
