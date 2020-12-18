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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vernalis.rest.RestClient;

/**
 * A helper class to generate a required query ID and to read the remote search
 * metadata schema
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbConnector2Helpers {

	/**
	 * The scroll speed to use in JScrollPane components
	 */
	public static final int SCROLL_SPEED = 16;

	private PdbConnector2Helpers() {
		// Don't instantiate!
	}

	/**
	 * The URL of the schema
	 */
	public static final String SCHEMA_URL =
			"http://search.rcsb.org/rcsbsearch/v1/metadata/schema";

	/**
	 * Method to read the Metadata Schema
	 *
	 * @return The metadata schema JSON object
	 * @throws IOException
	 *             If there was an error reading the schema
	 */
	public static JsonNode readMetadataSchema() throws IOException {

		try (InputStream is = RestClient.getResultStream(new URL(SCHEMA_URL))) {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.readTree(is);
		}
	}

}
