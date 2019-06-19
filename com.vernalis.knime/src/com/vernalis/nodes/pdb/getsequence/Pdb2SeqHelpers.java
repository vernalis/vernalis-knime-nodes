/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, 2014, Vernalis (R&D) Ltd
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 * ------------------------------------------------------------------------
 */
package com.vernalis.nodes.pdb.getsequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

/**
 * Utility functions to assist in sequence extraction from PDB files
 * 
 * @author s.roughley
 * 
 */
public class Pdb2SeqHelpers {
	// A Map constant to map 3-letter codes to 1-letter codes
	private static final Map<String, String> AAA_TO_A = createMap();

	private static Map<String, String> createMap() {
		Map<String, String> result = new HashMap<>();

		// Start with standard amino acids
		result.put("ALA", "A");
		result.put("CYS", "C");
		result.put("ASP", "D");
		result.put("GLU", "E");
		result.put("PHE", "F");
		result.put("GLY", "G");
		result.put("HIS", "H");
		result.put("ILE", "I");
		result.put("LYS", "K");
		result.put("LEU", "L");
		result.put("MET", "M");
		result.put("ASN", "N");
		result.put("PRO", "P");
		result.put("GLN", "Q");
		result.put("ARG", "R");
		result.put("SER", "S");
		result.put("THR", "T");
		result.put("VAL", "V");
		result.put("TRP", "W");
		result.put("TYR", "Y");
		result.put("CES", "U");

		// Standard D-amino acids do not have MODRES so include them
		result.put("DAL", "A");
		result.put("DCY", "C");
		result.put("DAS", "D");
		result.put("DGL", "E");
		result.put("DPN", "F");
		result.put("GLY", "G");
		result.put("DHI", "H");
		result.put("DIL", "I");
		result.put("DLY", "K");
		result.put("DLE", "L");
		result.put("MED", "M");
		result.put("DSG", "N");
		result.put("DPR", "P");
		result.put("DGN", "Q");
		result.put("DAR", "R");
		result.put("DSN", "S");
		result.put("DTH", "T");
		result.put("DVA", "V");
		result.put("DTR", "W");
		result.put("DTY", "Y");

		// RNA is unchanged...
		result.put("A", "A");
		result.put("C", "C");
		result.put("G", "G");
		result.put("U", "U");
		result.put("I", "I");
		result.put("T", "T");

		// DNA has a 'D' prefixing the RNA equivalent
		result.put("DA", "A");
		result.put("DC", "C");
		result.put("DG", "G");
		result.put("DU", "U");
		result.put("DI", "I");
		result.put("DT", "T");

		// Finally make sure gaps are retained:
		result.put("?", "?");
		return Collections.unmodifiableMap(result);
	}

	// A map for non-standard amino acids to their parents
	private static final Map<String, String> XXX_TO_A = createMap2();

	private static Map<String, String> createMap2() {
		Map<String, String> result = new HashMap<>();

		// Start with standard D-amino acids
		result.put("DAL", "ALA");
		result.put("DCY", "CYS");
		result.put("DAS", "ASP");
		result.put("DGL", "GLU");
		result.put("DPN", "PHE");
		result.put("GLY", "GLY");
		result.put("DHI", "HIS");
		result.put("DIL", "ILE");
		result.put("DLY", "LYS");
		result.put("DLE", "LEU");
		result.put("MED", "MET");
		result.put("DSG", "ASN");
		result.put("DPR", "PRO");
		result.put("DGN", "GLN");
		result.put("DAR", "ARG");
		result.put("DSN", "SER");
		result.put("DTH", "THR");
		result.put("DVA", "VAL");
		result.put("DTR", "TRP");
		result.put("DTY", "TYR");

		// Phosphorylateds...
		result.put("ASQ", "ASP");
		result.put("SEP", "SER");
		result.put("TPO", "THR");
		result.put("D11", "THR");
		result.put("PTR", "TYR");

		// Sulphateds...
		result.put("OSE", "SER");
		result.put("TYS", "TYR");

		// Acylateds...
		result.put("ALY", "LYS");
		result.put("OAS", "SER");
		result.put("TH5", "THR");

		// Methylateds...
		result.put("GME", "GLU");
		result.put("M3L", "LYS");
		result.put("MLY", "LYS");
		result.put("MLZ", "LYS");
		result.put("TRG", "LYS");
		result.put("DMH", "ASN");
		result.put("MEN", "ASN");
		result.put("MND", "ASN");
		result.put("MEQ", "GLN");
		result.put("QMM", "GLN");
		result.put("2MR", "ARG");
		result.put("DA2", "ARG");
		result.put("OLT", "THR");
		result.put("XDT", "THR");
		result.put("0A1", "TYR");

		// S-oxidised...
		result.put("0CS", "CYS");
		result.put("2CO", "CYS");
		result.put("CEA", "CYS");
		result.put("CSD", "CYS");
		result.put("CSO", "CYS");
		result.put("OCS", "CYS");
		result.put("00C", "CYS");

		// Finally make sure gaps are retained:
		result.put("?", "?");
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Utility function to return a map of replacements for Post-Translational
	 * Modifications
	 * 
	 * @param PDB
	 *            - String representation of the PDB file
	 * @return A hashmap of the PTM to standard residue replacements
	 */
	public static Map<String, String> getPtmMap(String PDB) {
		Map<String, String> ModRes = new HashMap<>();
		for (String line : PDB.split("\\n")) {
			if (line.startsWith("MODRES ")) {
				ModRes.put(line.substring(12, 15), line.substring(24, 27));
			}
		}
		return ModRes;
	}

	/**
	 * Function to extract SEQRES sequences from PDB file (as String)
	 * 
	 * @param PDB
	 *            The PDB file to extract sequences from
	 * @return Map object containing the Chain IDs as Keys and Sequences as
	 *         Values
	 */
	public static Map<String, String> getSequence(String PDB) {
		Map<String, String> Seqs = new HashMap<>();

		// Some temporary objects for building sequences
		String curChain = "";
		String curSeq = "";
		for (String line : PDB.split("\\n")) {
			if (line.startsWith("SEQRES ")) {
				if (!line.substring(11, 12).equals(curChain)) {
					// This line starts a new sequence
					curSeq = "";
					curChain = line.substring(11, 12);
				}
				curSeq += line.substring(19).trim() + " ";
				// It is simplest just to update the map after each SEQRES line
				// NB SEQRES can have a ' ' if there is only 1 chain, so in this
				// Case, we need to look up the first ATOM record to find the
				// chain
				Seqs.put((" ".equals(curChain)) ? PDB.split("ATOM   ")[1].substring(14, 15)
						: curChain, curSeq.trim());
			}
		}
		return Seqs;
	}

	/**
	 * This function returns the sequence(s) based on the co-ordinate residues
	 * of a PDB file
	 * 
	 * @param PDB
	 *            The PDB file (as a string)
	 * @param includeHETATM
	 *            Should residues listed as HETATM be included?
	 * @param ignoreMODEL
	 *            Should model Nos be ignored?
	 * @return The sequence(s) as a Map of Chain/Sequence pairs - sequences are
	 *         the 3-letter pdb-specified component codes or heterogen IDs
	 */
	public static Map<String, String> getCoordSequence(String PDB, Boolean includeHETATM,
			Boolean ignoreMODEL) throws Exception {
		/** Map object containing the chain as keys and sequences as values */
		Map<String, String> Seqs = new HashMap<>();

		/**
		 * Map object containing the chain as keys and last residue ids as
		 * values
		 */
		Map<String, Integer> lastIDs = new HashMap<>();

		try {
			Integer.parseInt(PDB.split("NUMMDL")[1].split("\\n")[0].trim());
		} catch (Exception e) {
		}

		int modelID = 1; // Default value, applies to most structures

		int curResID = -99999;
		String curChain = "", ChainIDKey = "";
		String resDelim = " ";
		for (String line : PDB.split("\\n")) {
			// Check if the current line is a new MODEL
			if (line.startsWith("MODEL ") && !ignoreMODEL) {
				// New Model
				modelID = Integer.parseInt(line.substring(10).trim());
				curResID = -99999;
			}

			// Now check if it is some sort of co-ordinate
			if (line.startsWith("ATOM   ") || (line.startsWith("HETATM ") && includeHETATM)) {
				// Get the chain and residue ID
				curChain = line.substring(21, 22);
				ChainIDKey = (ignoreMODEL) ? curChain : curChain + "(" + modelID + ")";
				curResID = Integer.parseInt(line.substring(22, 26).trim());

				int deltaResID;
				if (Seqs.containsKey(ChainIDKey)) {
					// Already have this chain/model combo, so need to check if
					// there has been a gap before adding
					deltaResID = curResID - lastIDs.get(ChainIDKey);
					if (deltaResID > 0) {
						// We've moved to a new residue in the chain
						// NB If we've moved backwards then we will ignore the
						// new residue. Not ideal, but then neither is moving
						// backwards!
						// TODO Implement the sequence map as a HashMap<String
						// (=ChainID), HashMap<Integer (=residueID),String
						// (=ResidueName)>> - then sort by resiID, and process
						String ResName = line.substring(17, 20);
						if (!"HOH".equals(ResName)) {
							Seqs.put(ChainIDKey, Seqs.get(ChainIDKey) + resDelim
									+ ((deltaResID > 1) ? "?" + resDelim : "") + ResName);
							// Update the pointer to the current residue ID
							lastIDs.put(ChainIDKey, curResID);
						}
					}
				} else {
					// This is a new chain
					String ResName = line.substring(17, 20);
					if (!"HOH".equals(ResName)) {
						Seqs.put(ChainIDKey, ResName);
						// Update the pointer to the current residue ID
						lastIDs.put(ChainIDKey, curResID);
					}

				}
			}
		}
		return Seqs;
	}

	/**
	 * Convenience wrapper for {@link getCoordSequence} where includeHETATM is
	 * omitted and assumed to take a default value of false - i.e. HETATM lines
	 * are not included in the sequence
	 * 
	 * @param PDB
	 *            The PDB file (as a string)
	 * @return The sequence(s) as a Map of Chain/Sequence pairs - sequences are
	 *         the 3-letter pdb-specified component codes or heterogen IDs
	 */
	public static Map<String, String> getCoordSequence(String PDB) throws Exception {
		return getCoordSequence(PDB, false, false);
	}

	/**
	 * Convert the sequence from SEQRES records to the standardised sequence
	 * with PTMs replaced with their standard 'parent' residues defined in the
	 * PTMMap
	 * 
	 * @param Sequence
	 *            - String of sequence from SEQRES records
	 * @param PTMMap
	 *            - Map of replacements from MODRES records
	 * @return - a string with the sequence replaced with standard residues
	 */
	public static String standardizeSequence(String Sequence, Map<String, String> PTMMap) {
		for (Map.Entry<String, String> entry : PTMMap.entrySet()) {
			Sequence = Sequence.replace(entry.getKey(), entry.getValue());
		}
		return Sequence;
	}

	/**
	 * Convenience wrapper to apply the {@link standardizeSequence} method to a
	 * Map of sequences
	 * 
	 * @param Sequences
	 *            Map of chain/sequence pairs
	 * @param PTMMap
	 *            Map of replacements from MODRES records
	 * @return - a map with the sequences replaced with standard residues
	 */
	public static Map<String, String> standardizeSequences(Map<String, String> Sequences,
			Map<String, String> PTMMap) {
		Map<String, String> Seqs = new HashMap<>();
		for (Map.Entry<String, String> entry : Sequences.entrySet()) {
			Seqs.put(entry.getKey(), standardizeSequence(entry.getValue(), PTMMap));
		}
		return Seqs;
	}

	/**
	 * Convert a sequence from 3-letter to 1-letter codes. Non-standard residues
	 * are returned as 'X'
	 * 
	 * @param Sequence
	 *            3-letter sequence, with ' ' between residues
	 * @return 1-letter sequence
	 */
	public static String convertAAAtoA(String Sequence) {
		String newSeq = "";
		for (String res : Sequence.split(" ")) {
			if (res != null && !"".equals(res)) {
				// Add the relevant 1 letter code - Use X (any) if unknown
				// X will only occur if standardisation failed
				newSeq += (AAA_TO_A.containsKey(res)) ? AAA_TO_A.get(res) : "X";
			}
		}
		return newSeq;
	}

	/**
	 * Convenience wrapper to apply the {@link convertAAAtoA} method to a Map of
	 * sequences
	 * 
	 * @param Sequences
	 *            Map of chain/sequence pairs (ideally standardised)
	 * @return - Map with sequences converted to 1-letter sequences
	 */
	public static Map<String, String> convertAAAtoAs(Map<String, String> Sequences) {
		Map<String, String> Seqs = new HashMap<>();
		for (Map.Entry<String, String> entry : Sequences.entrySet()) {
			Seqs.put(entry.getKey(), convertAAAtoA(entry.getValue()));
		}
		return Seqs;
	}

	/**
	 * Master function which returns the required sequences and chains based on
	 * the user settings from the node
	 * 
	 * @param PDB
	 *            The string value of the pdb cell
	 * @param getSeqres1
	 *            Return the SEQRES sequence in sanitised 1-letter form
	 * @param getSeqres3
	 *            Return the SEQRES sequence in raw 3-letter form
	 * @param getAtom1
	 *            Return the co-ordinates sequence in sanitised 1-letter form
	 * @param getAtom3
	 *            Return the co-ordinates sequence in raw 3-letter form
	 * @param incHetAtom
	 *            Include HETATM records in co-ordinates sequence(s)
	 * @param ignMod
	 *            Ignore MODELs for multi-model structures?
	 * @return An ArrayList of ArrayLists of DataCells containing the results
	 *         specified
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<DataCell>> getResults(String PDB, Boolean getSeqres1,
			Boolean getSeqres3, Boolean getAtom1, Boolean getAtom3, Boolean incHetAtom,
			Boolean ignMod) throws Exception {

		// This List is for the result
		ArrayList<ArrayList<DataCell>> result = new ArrayList<>();

		// This is a set to contain all the chains for the result
		SortedSet<String> Chains = new TreeSet<>();

		// Fetch all the sequences
		Map<String, String> seqres3 = getSequence(PDB);
		Map<String, String> seqres1 = convertAAAtoAs(standardizeSequences(seqres3, getPtmMap(PDB)));

		Map<String, String> atom3 = getCoordSequence(PDB, incHetAtom, ignMod);
		// Here we standardise using the dictionary in case MODRES records are
		// not included
		Map<String, String> atom1 = convertAAAtoAs(standardizeSequences(atom3, XXX_TO_A));

		// Now we need do list the chains based on what is to be included
		if (getSeqres1 || getSeqres3) {
			Chains.addAll(seqres3.keySet());
		}

		// And now deal with the case when there were no SEQRES residues to list
		// the chains
		// We need to extract the chains from the Chain(Model) records in the
		// atom Maps
		// This will also ensure that any Chains omitted from the SEQRES records
		// are included
		Iterator<String> iter = atom3.keySet().iterator();
		while (iter.hasNext()) {
			Chains.add(iter.next().substring(0, 1));
		}

		// Now, we iterate through the models and chains, populating a result
		// set for each one
		// Calculate the number of models
		int numModels;
		try {
			numModels = Integer.parseInt(PDB.split("NUMMDL")[1].split("\\n")[0].trim());
		} catch (Exception e) {
			numModels = 1;
		}
		if (ignMod) {
			// Only loop once
			numModels = 1;

		}

		String temp;
		// Loop through the models
		for (int modelID = 1; modelID <= numModels; modelID++) {
			Iterator<String> iter1 = Chains.iterator();
			while (iter1.hasNext()) {
				String curChain = iter1.next();
				ArrayList<DataCell> outVals = new ArrayList<>();
				// the first column in the output will always be the chain
				// which will always exist!
				outVals.add(new StringCell(curChain));

				if (getSeqres1) {
					temp = seqres1.get(curChain);
					outVals.add((temp == null) ? DataType.getMissingCell() : new StringCell(temp));
				}
				if (getSeqres3) {
					temp = seqres3.get(curChain);
					outVals.add((temp == null) ? DataType.getMissingCell() : new StringCell(temp));
				}

				// If we are not ignoring chains, and are retrieving a co-ords
				// sequence
				// We need a model ID column
				if ((getAtom1 || getAtom3) && !ignMod) {
					outVals.add(new IntCell(modelID));
				}
				if (getAtom1) {
					if (ignMod) {
						temp = atom1.get(curChain);
					} else {
						temp = atom1.get(curChain + "(" + modelID + ")");
					}
					outVals.add((temp == null) ? DataType.getMissingCell() : new StringCell(temp));
				}
				if (getAtom3) {
					if (ignMod) {
						temp = atom3.get(curChain);
					} else {
						temp = atom3.get(curChain + "(" + modelID + ")");
					}
					outVals.add((temp == null) ? DataType.getMissingCell() : new StringCell(temp));
				}

				// Add the results to the return list
				result.add(outVals);
			}
		}

		return result;
	}

	/**
	 * Function to return set of added column names based on user settings
	 * 
	 * @param getSeqres1
	 *            Is SEQRES 1-letter sequence included?
	 * @param getSeqres3
	 *            Is SEQRES 3-letter sequence included?
	 * @param getCoord1
	 *            Is Co-Ordinate block 1-letter sequence included?
	 * @param getCoord3
	 *            Is Co-Ordinate block 3-letter sequence included?
	 * @param ignMods
	 *            Should MODELS be ignored?
	 * @return Set of the added column names
	 */
	public static Set<String> ColumnNames(Boolean getSeqres1, Boolean getSeqres3, Boolean getCoord1,
			Boolean getCoord3, Boolean ignMods) {
		Set<String> ColumnNames = new LinkedHashSet<>();
		ColumnNames.add("Chain");
		if (getSeqres1) {
			ColumnNames.add("SEQRES 1-letter Sequence");
		}
		if (getSeqres3) {
			ColumnNames.add("SEQRES 3-letter Sequence");
		}
		if ((getCoord1 || getCoord3) && !ignMods) {
			ColumnNames.add("Model ID");
		}
		if (getCoord1) {
			ColumnNames.add("Coords 1-letter Sequence");
		}
		if (getCoord3) {
			ColumnNames.add("Coords 3-letter Sequence");
		}
		return ColumnNames;
	}
}
