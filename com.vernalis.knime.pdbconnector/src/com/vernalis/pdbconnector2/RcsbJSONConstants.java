/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2;

/**
 * A class containing string contants required for parsing and creating JSON
 * Queries and results
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
@SuppressWarnings("javadoc")
public class RcsbJSONConstants {

	private RcsbJSONConstants() {
		// Don't instantiate!
	}

	/*
	 * JSON Parsing constants
	 */
	public static final String INTEGER = "integer";
	public static final String NUMBER = "number";
	public static final String DISPLAY_NAME = "display_name";
	public static final String SUGGESTER = "suggester";
	public static final String SELECT = "select";
	public static final String DATE = "date";
	public static final String INPUT_TYPE = "input_type";
	public static final String OPERATORS = "operators";
	public static final String DEFAULT_OPERATOR = "default_operator";
	public static final String SEARCH_GROUP_PRIORITY = "search_group_priority";
	public static final String EMPTY_STRING = "";
	public static final String SEARCH_GROUP_NAME = "search_group_name";
	public static final String PLACEHOLDER = "placeholder";
	public static final String DESCRIPTION = "description";
	public static final String IS_ITERABLE = "is_iterable";
	public static final String MAX = "max";
	public static final String MIN = "min";
	public static final String ENUMERATION = "enumeration";
	public static final String ENUM_LEN = "enum_len";
	public static final String CHAINS = "asym_ids";
	public static final String ASSEMBLY_IDS = "assembly_ids";
	public static final String MSG = "msg";
	public static final String ASYM_ID = "asym_id";

	/*
	 * Json Creating constants
	 */
	public static final String SERVICE_CHEMICAL = "chemical";
	public static final String MATCH_SUBSET = "match_subset";
	public static final String MATCH_TYPE = "match_type";
	public static final String DESCRIPTOR_TYPE = "descriptor_type";
	public static final String TYPE = "type";
	public static final String NODES = "nodes";
	public static final String LOGICAL_OPERATOR = "logical_operator";
	public static final String TYPE_GROUP = "group";
	public static final String VALUE = "value";
	public static final String OPERATOR = "operator";
	public static final String NEGATION = "negation";
	public static final String ATTRIBUTE = "attribute";
	public static final String PARAMETERS = "parameters";
	public static final String SERVICE_TEXT = "text";
	public static final String SERVICE_KEY = "service";
	public static final String NODE_ID = "node_id";
	public static final String TYPE_TERMINAL = "terminal";
	public static final String TYPE_KEY = "type";
	public static final String ENTRY_ID = "entry_id";
	public static final String SERVICE_STRUCTURE = "structure";
	public static final String TARGET = "target";
	public static final String PATTERN_TYPE = "pattern_type";
	public static final String SERVICE_SEQMOTIF = "seqmotif";
	public static final String SERVICE_SEQUENCE = "sequence";
	public static final String EVALUE_CUTOFF = "evalue_cutoff";
	public static final String IDENTITY_CUTOFF = "identity_cutoff";
}
