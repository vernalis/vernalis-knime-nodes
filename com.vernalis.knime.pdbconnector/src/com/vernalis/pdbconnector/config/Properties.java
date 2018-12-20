/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.pdbconnector.config;

/**
 * Properties class.
 * 
 * Contains static instances of all string and integer global properties. Property values may be used
 * either to control model behaviour or to control UI dialog layout.
 * 
 * Property values are initialized from configuration XML file by PdbConnectorConfig, or to a suitable
 * internal (hard-coded) default value if not specified in the config file.
 * 
 * @see PdbConnectorConfig2
 */
public class Properties {
	
	/** URL of RCSB search service. */
	public static final String SEARCH_LOCATION;
	
	/** URL of RCSB report service. */
	public static final String REPORT_LOCATION;
	
	/** URL of RCSB ligand image location. */
	public static final String LIGAND_IMG_LOCATION;
	
	/** XML string to start composite query. */
	public static final String COMPOSITE_START;
	
	/** XML string to end composite query. */
	public static final String COMPOSITE_END;
	
	/** XML string to start composite query refinement. */
	public static final String REFINEMENT_START;
	
	/** XML string to end composite query refinement. */
	public static final String REFINEMENT_END;
	
	/** XML prefix for composite query refinement level number. */
	public static final String LEVEL_START;
	
	/** XML suffix for composite query refinement level number. */
	public static final String LEVEL_END;
	
	/** XML string for composite query AND conjunctions. */
	public static final String CONJUNCTION_AND;
	
	/** XML string for composite query OR conjunctions. */
	public static final String CONJUNCTION_OR;
	
	/** Date format string for parsing yyyy-MM-dd values. */
	public static final String YMD_FORMAT;
	
	/** Date format string for parsing yyyy-MM values. */
	public static final String YM_FORMAT;
	
	/** Date format string for parsing yyyy values. */
	public static final String YEAR_FORMAT;
	
	/** Report string prefix before enumerated list of PDB IDs. */
	public static final String REPORT_PDBIDS_URL;
	
	/** Report string prefix before enumerated list of report columns. */
	public static final String REPORT_COLUMNS_URL;
	
	/** Report string snippet to specify CSV output format. */
	public static final String REPORT_CSV_URL;
	
	/** Line delimiter in CSV report output. */
	public static final String REPORT_CSV_LINE_DELIM;
	
	/** Record (field) delimiter in CSV report output. */
	public static final String REPORT_CSV_RECORD_DELIM;
	
	/** Open quote character in CSV report output. */
	public static final String REPORT_CSV_OPEN_QUOTE;
	
	/** Close quote character in CSV report output. */
	public static final String REPORT_CSV_CLOSE_QUOTE;
	
	/** Report string snippet to specify XML output format. */
	public static final String REPORT_XML_URL;
	
	/** Root element name in XML output format. */
	public static final String REPORT_XML_ROOT;
	
	/** Record element name in XML output format. */
	public static final String REPORT_XML_RECORD;
	
	/** KNIME column name for PDB ID field in outport table 0. */
	public static final String PDB_COLUMN_NAME;

	/** UI label for AND conjunctions. */
	public static final String CONJUNCTION_AND_LABEL;
	
	/** UI label for OR conjunctions. */
	public static final String CONJUNCTION_OR_LABEL;

	/** Number of columns in Report Options dialog (excluding category checkbox column). */
	public static final int REPORT_LAYOUT_COLUMNS;
	
	/** Preferred width (in pixels) of each Report Options column. */
	public static final int REPORT_LAYOUT_COL_WIDTH;
	
	/** Default width (in characters) of string query parameter dialogs. */
	public static final int QUERY_PRM_STRING_WIDTH;
	
	/** Default width (in characters) of integer query parameter dialogs. */
	public static final int QUERY_PRM_INTEGER_WIDTH;
	
	/** Default width (in characters) of double query parameter dialogs. */
	public static final int QUERY_PRM_DOUBLE_WIDTH;
	
	/** Default width (in characters) of bigstring query parameter dialogs. */
	public static final int QUERY_PRM_BIGSTRING_WIDTH;
	
	/** Default number of rows for bigstring query parameter dialogs. */
	public static final int QUERY_PRM_BIGSTRING_ROWS;
	
	/** Number of columns in query dialogs (excluding checkbox column). */
	public static final int QUERY_LAYOUT_COLUMNS;
	
	/** Preferred width (in pixels) of each query column. */
	public static final int QUERY_LAYOUT_COL_WIDTH;

	static {
		PdbConnectorConfig2 config = PdbConnectorConfig2.getInstance();
		SEARCH_LOCATION = config.getProperty("SEARCH_LOCATION","http://www.rcsb.org/pdb/rest/search");
		REPORT_LOCATION = config.getProperty("REPORT_LOCATION","http://www.rcsb.org/pdb/rest/customReport");
		LIGAND_IMG_LOCATION = config.getProperty("LIGAND_IMG_LOCATION","http://www.pdb.org/pdb/images/");
		COMPOSITE_START = config.getProperty("COMPOSITE_START","<orgPdbCompositeQuery version=\"1.0\">");
		COMPOSITE_END = config.getProperty("COMPOSITE_END", "</orgPdbCompositeQuery>");
		REFINEMENT_START = config.getProperty("REFINEMENT_START","<queryRefinement>");
		REFINEMENT_END = config.getProperty("REFINEMENT_END","</queryRefinement>");
		LEVEL_START = config.getProperty("LEVEL_START","<queryRefinementLevel>");
		LEVEL_END = config.getProperty("LEVEL_END","</queryRefinementLevel>");
		CONJUNCTION_AND = config.getProperty("CONJUNCTION_AND","<conjunctionType>and</conjunctionType>");
		CONJUNCTION_OR = config.getProperty("CONJUNCTION_OR","<conjunctionType>or</conjunctionType>");
		YMD_FORMAT = config.getProperty("YMD_FORMAT","yyyy-MM-dd");
		YM_FORMAT = config.getProperty("YM_FORMAT","yyyy-MM");
		YEAR_FORMAT = config.getProperty("YEAR_FORMAT","yyyy");
		REPORT_PDBIDS_URL = config.getProperty("REPORT_PDBIDS_URL","?pdbids=");
		REPORT_COLUMNS_URL = config.getProperty("REPORT_COLUMNS_URL","&customReportColumns=");
		REPORT_CSV_URL = config.getProperty("REPORT_CSV_URL","&format=csv");
		REPORT_CSV_LINE_DELIM = config.getProperty("REPORT_CSV_LINE_DELIM","<br />");
		REPORT_CSV_RECORD_DELIM = config.getProperty("REPORT_CSV_RECORD_DELIM",",");
		REPORT_CSV_OPEN_QUOTE = config.getProperty("REPORT_CSV_OPEN_QUOTE","\"");
		REPORT_CSV_CLOSE_QUOTE = config.getProperty("REPORT_CSV_CLOSE_QUOTE","\"");
		REPORT_XML_URL = config.getProperty("REPORT_XML_URL","&format=xml");
		REPORT_XML_ROOT = config.getProperty("REPORT_XML_ROOT","dataset");
		REPORT_XML_RECORD = config.getProperty("REPORT_XML_RECORD","record");
		PDB_COLUMN_NAME = config.getProperty("PDB_COLUMN_NAME","PDB ID");
		//UI labels
		CONJUNCTION_AND_LABEL = config.getProperty("CONJUNCTION_AND_LABEL","and");
		CONJUNCTION_OR_LABEL = config.getProperty("CONJUNCTION_OR_LABEL","or");
		//UI layout (integers)
		REPORT_LAYOUT_COLUMNS = config.getPropertyAsInt("REPORT_LAYOUT_COLUMNS", 2);
		REPORT_LAYOUT_COL_WIDTH = config.getPropertyAsInt("REPORT_LAYOUT_COL_WIDTH", 250);
		QUERY_PRM_STRING_WIDTH = config.getPropertyAsInt("QUERY_PRM_STRING_WIDTH", 15);
		QUERY_PRM_INTEGER_WIDTH = config.getPropertyAsInt("QUERY_PRM_INTEGER_WIDTH", 5);
		QUERY_PRM_DOUBLE_WIDTH = config.getPropertyAsInt("QUERY_PRM_DOUBLE_WIDTH", 5);
		QUERY_PRM_BIGSTRING_WIDTH = config.getPropertyAsInt("QUERY_PRM_BIGSTRING_WIDTH", 40);
		QUERY_PRM_BIGSTRING_ROWS = config.getPropertyAsInt("QUERY_PRM_BIGSTRING_ROWS", 3);
		QUERY_LAYOUT_COLUMNS = config.getPropertyAsInt("QUERY_LAYOUT_COLUMNS", 3);
		QUERY_LAYOUT_COL_WIDTH = config.getPropertyAsInt("QUERY_LAYOUT_COL_WIDTH", 200);
	}
}
