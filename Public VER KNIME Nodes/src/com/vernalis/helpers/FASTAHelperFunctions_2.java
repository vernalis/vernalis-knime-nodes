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

public class FASTAHelperFunctions_2 {
	/*
	 * Some helper functions to deal with  FASTA sequences
	 */
	
	public static Set<String> ColumnNames (String FASTA_Type, boolean IncludeHeader, 
			boolean extractSequence){
		/*
		 * Helper function to return a list of the column names based on the FASTA_Type setting
		 */
		
		//Use a set to simplify the default case in the switch of adding the header row if nothing else specified
		//LinkedHashSet retains the order added
		Set<String> ColumnNames = new LinkedHashSet<String>();
		
		if (IncludeHeader){
			ColumnNames.add("Header");
		}
		
		switch (FASTA_Type.toLowerCase()){
		case "genbank": case "embl": case "ddbj (dna database of japan)":
			ColumnNames.add("GI number");
			ColumnNames.add("Accession");
			ColumnNames.add("Locus");
			break;
		case "nbrf":
			ColumnNames.add("Entry");
			break;
		case "protein research foundation":
			ColumnNames.add("Name");
			break;
		case "swiss-prot":
			ColumnNames.add("Accession");
			ColumnNames.add("Name");
			break;
		case "pdb":
			ColumnNames.add("Structure ID");
			ColumnNames.add("Chain");
			break;
		case "patents":
			ColumnNames.add("Country");
			ColumnNames.add("number");
			break;
		case "geninfo backbone id":
			ColumnNames.add("Number");
			break;
		case "general database identifier":
			ColumnNames.add("Database");
			ColumnNames.add("Identifier");
			break;
		case "ncbi reference sequence":
			ColumnNames.add("Accession");
			ColumnNames.add("Locus");
			break;
		case "local sequence identifier":
			ColumnNames.add("Identifier");
			break;
		case "other (no fields extracted from header)":
			break;
		default:
			//If somehow we dont have one of the presets, then we just add the header
			ColumnNames.add("Header");
		}
		
		if (extractSequence){
			ColumnNames.add("Sequence");
		}
		return ColumnNames;
	}
	

	/*
	 * Now we have some functions to return the appropriate values from the header based on the type
	 */
	
	public static DataCell[] ColumnValues (String FASTA, String FASTA_Type,
			boolean IncludeHeader, boolean extractSequence){
		
		/*
		 * This function returns a LIST containing the values to go into the columns
		 * NB this is not a SET, as there is no requirement for uniqueness!
		 */
		String FASTAHeader = getHeader(FASTA);
		List<String> ColumnValues = new ArrayList<String>();

		if ("".equals(FASTAHeader) || FASTAHeader == null){
			if (IncludeHeader){
				ColumnValues.add(null);
			}
			switch (FASTA_Type.toLowerCase()){
			case "genbank": case "embl": case "ddbj (dna database of japan)":
				//These have 3 columns
				//NB no break so enough cols added
				ColumnValues.add(null);

			case "swiss-prot": case "patents": case "general database identifier":
			case "ncbi reference sequence": case "pdb":
				//These only have 2
				ColumnValues.add(null);

			case "nbrf": case "protein research foundation":
			case "geninfo backbone id":	case "local sequence identifier":
				//And these only have 1
				ColumnValues.add(null);
				break;
			case "other (no fields extracted from header)":
				//And this one has none
				break;
			default:
				//If somehow we dont have one of the presets, then we just add the header
				if (!IncludeHeader){
					//But only once - so only if we've not added it at the beginning!
					ColumnValues.add(null);
				}
				
			}
			if (extractSequence){
				ColumnValues.add(null);
			}
		}else {
			if (IncludeHeader){
				ColumnValues.add(FASTAHeader.split(">")[1]);
			}

			switch (FASTA_Type.toLowerCase()){
			case "genbank": case "embl": case "ddbj (dna database of japan)":
				try{
					ColumnValues.add(FASTAHeader.split("\\|")[1]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				try{
					ColumnValues.add(FASTAHeader.split("\\|")[3]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				try{
					ColumnValues.add(FASTAHeader.split("\\|")[4]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				break;

			case "nbrf": case "protein research foundation":
				try{
					ColumnValues.add(FASTAHeader.split("\\|")[2]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				break;

			case "swiss-prot": case "patents": case "general database identifier": case "ncbi reference sequence":
				try {
					ColumnValues.add(FASTAHeader.split("\\|")[1]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				try{
					ColumnValues.add(FASTAHeader.split("\\|")[2]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				break;

			case "geninfo backbone id":	case "local sequence identifier":
				try{
					ColumnValues.add(FASTAHeader.split("\\|")[1]);
				}catch (Exception e){
					ColumnValues.add(null);
				}
				break;

			case "pdb":
				//PDB is more complicated!
				if (FASTAHeader.toLowerCase().startsWith("pdb|")){
					try{
						ColumnValues.add(FASTAHeader.split("\\|")[1]);
					}catch (Exception e){
						ColumnValues.add(null);
					}
					try{
						ColumnValues.add(FASTAHeader.split("\\|")[2]);
					}catch (Exception e){
						ColumnValues.add(null);
					}
				} else {
					try{
						ColumnValues.add(getPDBID(FASTAHeader));
					}catch (Exception e){
						ColumnValues.add(null);
					}
					try{
						ColumnValues.add(getChain(FASTAHeader));
					}catch (Exception e){
						ColumnValues.add(null);
					}
				}
				break;
				
			case "other (no fields extracted from header)":
				//And this one has none
				break;

			default:
				//If somehow we dont have one of the presets, then we just add the header
				if (!IncludeHeader){
					//But only once - so only if we've not added it at the beginning!
					try{
						ColumnValues.add(FASTAHeader.split(">")[1]);
					}catch (Exception e){
						ColumnValues.add(null);
					}
				}
				
			}
			if (extractSequence){
				ColumnValues.add(getSequence(FASTA));
			}
		}
		
		//Now we need to create a DataCell array to return
		DataCell[] ColCells = new DataCell[ColumnValues.size()];
		Iterator<String> itr = ColumnValues.iterator();
		String CellString;
		int i = 0;
		while (itr.hasNext()){
			CellString = itr.next();
			ColCells[i++] = (CellString == null || "".equals(CellString)) ?
					DataType.getMissingCell() : new StringCell(CellString);
		}
		return ColCells;
	}

	public static boolean isCorrectType (String FASTAHeader,
			String FASTA_Type){
		/*
		 * This checks against the NCBI list of FASTA header patterns
		 * Feature is disabled as even NCBI appears not to follow the convention strictly!
		 */
		
		String matchPattern="";
		switch (FASTA_Type.toLowerCase()){
		case "genbank":
			matchPattern="gi\\|.*?\\|gb\\|.*?\\|.*?";
		break; 
		case "embl":
			matchPattern="gi\\|.*?\\|emb\\|.*?\\|.*?";
		break; 
		case "ddbj (dna database of japan)":
			matchPattern="gi\\|.*?\\|dbj\\|.*?\\|.*?";
		break; 
		case "swiss-prot":
			matchPattern="sp\\|.*?\\|.*?";
		break; 
		case "patents":
			matchPattern="pat\\|.*?\\|.*?";
		break; 
		case "general database identifier":
			matchPattern="gnl\\|.*?\\|.*?";
		break; 
		case "ncbi reference sequence":
			matchPattern="ref\\|.*?\\|.*?";
		break; 
		case "pdb":
			matchPattern="(pdb\\|.*?\\|.*?|[A-Za-z0-9]{4}:[A-Za-z0-9]{1}\\|PDBID\\|CHAIN\\|SEQUENCE)";
		break; 
		case "nbrf":
			matchPattern="pir\\|\\|.*?";
		break; 
		case "protein research foundation":
			matchPattern="prf\\|\\|.*?";
		break; 
		case "geninfo backbone id":
			matchPattern="bbs\\|.*?\\";
		break; 
		case "local sequence identifier":
			matchPattern="lcl\\|.*?\\";
		case "other (no fields extracted from header)":
			return true;
		}
		
		//Prefix an optional start-of-line character
		matchPattern="(>|)" + matchPattern;
		return FASTAHeader.toLowerCase().matches(matchPattern);
		
	}
	
	public static String getHeader (String FASTA){
		/*
		 * Return the whole header line, including the '>'
		 */
		try {
			return ">" + FASTA.split(">")[1].split("\\n")[0];
		}catch (Exception e){
			return null;
		}
	}
	
	public static String getPDBID (String FASTA){
		/*
		 * Return the PDB ID from a single-entry FASTA file in the PDB format
		 */
		try {
			return FASTA.split(">")[1].split(":")[0];
		}catch (Exception e){
			return null;
		}
	}

	public static String getChain (String FASTA){
		/*
		 * Return the Chain letter from a single-entry FASTA file in the PDB format
		 */
		try{
			return FASTA.split(":")[1].split("\\|")[0];
		}catch (Exception e){
			return null;
		}
	}

	public static String getSequence (String FASTA){
		/*
		 * Return a sequence from a FASTA string
		 */
		try {
			String seq="";
			for (String line : FASTA.split("\\n")){
				if (!line.startsWith(">")){
					seq += line;
				}
			}
			return seq.replaceAll("\\s", "");
		}catch (Exception e){
			return null;
		}
	}

	public static String[] getFASTAs (String FASTA){
		/*
		 * Split a multi-entry FASTA file into an array of single entries
		 */
		//Firstly deal with possibility of an empty FASTA Cell
		if (FASTA==null || "".equals(FASTA)){
			FASTA=">"; //This ensures the row is propagated in the output table
		}
		try{
			String[] r = FASTA.split(">");
			String[] s = new String[r.length - 1];
			for (int i = 1; i< r.length; i++){
				//The first entry is a blank, as it is actually the bit preceding the first '>'
				//Put the '>' back at the start to keep them as valid FASTA formats
				s[i-1] = ">"+r[i];
			}
			return s;
		}catch (Exception e){
			return null;
		}
	}

}
