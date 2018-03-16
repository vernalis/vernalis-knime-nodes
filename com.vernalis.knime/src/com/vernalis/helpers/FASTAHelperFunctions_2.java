/*
 * ------------------------------------------------------------------------
 *  Copyright (C) 2013, Vernalis (R&D) Ltd
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
package com.vernalis.helpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;

/**
 * Some helper functions to deal with FASTA sequences
 * 
 * @author Stephen Roughley <knime@vernalis.com>
 * 
 */
public class FASTAHelperFunctions_2 {

	/**
	 * Helper function to return a list of the column names based on the
	 * FASTA_Type setting
	 * 
	 * @param FASTA_Type
	 *            Case-insensitive FASTA Type string - currently, accepted
	 *            values are genbank, embl, ddbj (dna database of japan), nbrf,
	 *            protein research foundation, swiss-prot, pdb, patents, geninfo
	 *            backbone id, general database identifier, ncbi reference
	 *            sequence, local sequence identifier, other (no fields added
	 *            from header)
	 * @param IncludeHeader
	 *            If true then the whole header is included as a new column
	 * @param extractSequence
	 *            If true, then the sequence is included as a new column
	 * @return A LinkedHashSet of the column names to be added
	 */
	public static Set<String> ColumnNames(String FASTA_Type, boolean IncludeHeader,
			boolean extractSequence) {

		// Use a set to simplify the default case in the switch of adding the
		// header row if nothing else specified
		// LinkedHashSet retains the order added
		Set<String> ColumnNames = new LinkedHashSet<>();

		if (IncludeHeader) {
			ColumnNames.add("Header");
		}

		String temp = FASTA_Type.toLowerCase();
		if ("genbank".equals(temp) || "embl".equals(temp)
				|| "ddbj (dna database of japan)".equals(temp)) {
			ColumnNames.add("GI number");
			ColumnNames.add("Accession");
			ColumnNames.add("Locus");
		} else if ("nbrf".equals(temp)) {
			ColumnNames.add("Entry");
		} else if ("protein research foundation".equals(temp)) {
			ColumnNames.add("Name");
		} else if ("swiss-prot".equals(temp)) {
			ColumnNames.add("Accession");
			ColumnNames.add("Name");
		} else if ("pdb".equals(temp)) {
			ColumnNames.add("Structure ID");
			ColumnNames.add("Chain");
		} else if ("patents".equals(temp)) {
			ColumnNames.add("Country");
			ColumnNames.add("number");
		} else if ("geninfo backbone id".equals(temp)) {
			ColumnNames.add("Number");
		} else if ("general database identifier".equals(temp)) {
			ColumnNames.add("Database");
			ColumnNames.add("Identifier");
		} else if ("ncbi reference sequence".equals(temp)) {
			ColumnNames.add("Accession");
			ColumnNames.add("Locus");
		} else if ("local sequence identifier".equals(temp)) {
			ColumnNames.add("Identifier");
		} else if (!"other (no fields extracted from header)".equals(temp)) {

			// If somehow we dont have one of the presets, then we just add the
			// header
			ColumnNames.add("Header");
		}

		if (extractSequence) {
			ColumnNames.add("Sequence");
		}
		return ColumnNames;
	}

	/*
	 * Now we have some functions to return the appropriate values from the
	 * header based on the type
	 */

	/**
	 * This function returns a List (ArrayList) containing the values to go into
	 * the columns NB this is not a SET, as there is no requirement for
	 * uniqueness!
	 * 
	 * @param FASTA
	 *            The FASTA file as a string
	 * @param FASTA_Type
	 *            The case-insensitive FASTA type code - currently, accepted
	 *            values are genbank, embl, ddbj (dna database of japan), nbrf,
	 *            protein research foundation, swiss-prot, pdb, patents, geninfo
	 *            backbone id, general database identifier, ncbi reference
	 *            sequence, local sequence identifier, other (no fields added
	 *            from header)
	 * @param IncludeHeader
	 *            If true, the entire header row is included
	 * @param extractSequence
	 *            If true, then sequence is extracted
	 * @return An ArrayList containing the DataCells according to the specified
	 *         options
	 */
	public static DataCell[] ColumnValues(String FASTA, String FASTA_Type, boolean IncludeHeader,
			boolean extractSequence) {

		String FASTAHeader = getHeader(FASTA);
		List<String> ColumnValues = new ArrayList<>();
		String temp = FASTA_Type.toLowerCase();
		if ("".equals(FASTAHeader) || FASTAHeader == null) {
			if (IncludeHeader) {
				ColumnValues.add(null);
			}

			if ("genbank".equals(temp) || "embl".equals(temp)
					|| "ddbj (dna database of japan)".equals(temp)) {
				// These have 3 columns
				ColumnValues.add(null);
				ColumnValues.add(null);
				ColumnValues.add(null);
			} else if ("swiss-prot".equals(temp) || "patents".equals(temp)
					|| "general database identifier".equals(temp)
					|| "ncbi reference sequence".equals(temp) || "pdb".equals(temp)) {
				// These only have 2
				ColumnValues.add(null);
				ColumnValues.add(null);
			} else if ("nbrf".equals(temp) || "protein research foundation".equals(temp)
					|| "geninfo backbone id".equals(temp)
					|| "local sequence identifier".equals(temp)) {
				// And these only have 1
				ColumnValues.add(null);
			} else if (!"other (no fields extracted from header)".equals(temp) && !IncludeHeader) {
				// If somehow we dont have one of the presets, then we just add
				// the header - but only once - so only if we've not added it at
				// the beginning!
				ColumnValues.add(null);
			}

			if (extractSequence) {
				ColumnValues.add(null);
			}
		} else {
			if (IncludeHeader) {
				ColumnValues.add(FASTAHeader.split(">")[1]);
			}

			if ("genbank".equals(temp) || "embl".equals(temp)
					|| "ddbj (dna database of japan)".equals(temp)) {
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[1]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[3]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[4]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
			} else if ("nbrf".equals(temp) || "protein research foundation".equals(temp)) {
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[2]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
			} else if ("swiss-prot".equals(temp) || "patents".equals(temp)
					|| "general database identifier".equals(temp)
					|| "ncbi reference sequence".equals(temp)) {
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[1]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[2]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
			} else if ("geninfo backbone id".equals(temp)
					|| "local sequence identifier".equals(temp)) {
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[1]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
			} else if ("pdb".equals(temp)) {
				// PDB is more complicated!
				if (FASTAHeader.toLowerCase().startsWith("pdb|")) {
					try {
						ColumnValues.add(FASTAHeader.split("\\|")[1]);
					} catch (Exception e) {
						ColumnValues.add(null);
					}
					try {
						ColumnValues.add(FASTAHeader.split("\\|")[2]);
					} catch (Exception e) {
						ColumnValues.add(null);
					}
				} else {
					try {
						ColumnValues.add(getPDBID(FASTAHeader));
					} catch (Exception e) {
						ColumnValues.add(null);
					}
					try {
						ColumnValues.add(getChain(FASTAHeader));
					} catch (Exception e) {
						ColumnValues.add(null);
					}
				}
			} else if (!"other (no fields extracted from header)".equals(temp) && !IncludeHeader) {
				// If somehow we dont have one of the presets, then we just add
				// the header but only once - so only if we've not added it at
				// the beginning!
				try {
					ColumnValues.add(FASTAHeader.split(">")[1]);
				} catch (Exception e) {
					ColumnValues.add(null);
				}
			}
			if (extractSequence) {
				ColumnValues.add(getSequence(FASTA));
			}
		}

		// Now we need to create a DataCell array to return
		DataCell[] ColCells = new DataCell[ColumnValues.size()];
		Iterator<String> itr = ColumnValues.iterator();
		String CellString;
		int i = 0;
		while (itr.hasNext()) {
			CellString = itr.next();
			ColCells[i++] = (CellString == null || "".equals(CellString))
					? DataType.getMissingCell() : new StringCell(CellString);
		}
		return ColCells;
	}

	/**
	 * Utility function to return the whole header line, including the leading
	 * '>' from a single-entry FASTA file
	 * 
	 * @param FASTA
	 *            Single entry FASTA file string
	 * @return The header line, including the leading '>'
	 */
	public static String getHeader(String FASTA) {
		try {
			return ">" + FASTA.split(">")[1].split("\\n")[0];
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Utility function to return the PDB ID from a single-entry FASTA file in
	 * the PDB format
	 * 
	 * @param FASTA
	 *            Single entry FASTA file string
	 * @return The PDB ID
	 */
	public static String getPDBID(String FASTA) {
		try {
			return FASTA.split(">")[1].split(":")[0];
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Utility function to return the Chain ID from a single-entry FASTA file in
	 * the PDB format
	 * 
	 * @param FASTA
	 *            Single entry FASTA file string
	 * @return The Chain ID
	 */
	public static String getChain(String FASTA) {
		try {
			return FASTA.split(":")[1].split("\\|")[0];
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Utility function to return the Sequence from a single-entry FASTA file
	 * 
	 * @param FASTA
	 *            Single entry FASTA file string
	 * @return The sequence
	 */
	public static String getSequence(String FASTA) {
		try {
			String seq = "";
			for (String line : FASTA.split("\\n")) {
				if (!line.startsWith(">")) {
					seq += line;
				}
			}
			return seq.replaceAll("\\s", "");
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Utility function to split a multi-entry FASTA file String into an array
	 * of single entry FASTA file strings
	 * 
	 * @param FASTA
	 *            Multi-entry FASTA file String
	 * @return String[] array of single entry FASTA file strings
	 */
	public static String[] getFASTAs(String FASTA) {
		// Firstly deal with possibility of an empty FASTA Cell
		if (FASTA == null || "".equals(FASTA)) {
			FASTA = ">"; // This ensures the row is propagated in the output
							// table
		}
		try {
			String[] r = FASTA.split(">");
			String[] s = new String[r.length - 1];
			for (int i = 1; i < r.length; i++) {
				// The first entry is a blank, as it is actually the bit
				// preceding the first '>'
				// Put the '>' back at the start to keep them as valid FASTA
				// formats
				s[i - 1] = ">" + r[i];
			}
			return s;
		} catch (Exception e) {
			return null;
		}
	}

}
