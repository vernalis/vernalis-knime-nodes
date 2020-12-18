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
package com.vernalis.pdbconnector2.query;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class loads the static schema held in the {@code /json-config} folder at
 * the plugin root . It should be seen as a temporary fix until using the
 * correct schema call is implemented
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbStaticSchemaLoader {

	private static final NodeLogger logger =
			NodeLogger.getLogger(PdbStaticSchemaLoader.class);
	private static final String JSON_CONFIG =
			"json-config/rcsb-query-schema-last-working.json";
	private final JsonNode json;
	private IOException exception = null;

	/**
	 * Private constructor @throws
	 */
	private PdbStaticSchemaLoader() {
		JsonNode temp;
		try {
			temp = load();
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
			exception = e;
			temp = null;
		}
		json = temp;
	}

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static PdbStaticSchemaLoader INSTANCE =
				new PdbStaticSchemaLoader();
	}

	/**
	 * Method to get the singleton instance
	 * 
	 * @return The singleton instance of PdbStaticSchemaLoader
	 */
	public static PdbStaticSchemaLoader getInstance() {
		return HoldingClass.INSTANCE;
	}

	private JsonNode load() throws IOException {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		IPath jsonPath = new Path(JSON_CONFIG);
		final URL JSON = FileLocator.find(bundle, jsonPath, null);
		if (JSON == null) {
			throw new IOException("Error finding path to " + JSON_CONFIG);
		}
		logger.debug("URL for " + JSON_CONFIG + ": " + JSON.toExternalForm());
		try (InputStream is = JSON.openStream()) {
			return new ObjectMapper().readTree(is);
		}
	}

	/**
	 * @return The loaded JSON contents
	 * @throws IOException
	 *             If there was an error loading the schema
	 */
	public JsonNode getContentJson() throws IOException {
		throwLoadingException();
		return json;
	}

	/**
	 * @return The metadata (query fields)
	 * @throws IOException
	 *             If there was an error loading the schema
	 */
	public JsonNode getQueryMetadata() throws IOException {
		throwLoadingException();
		return getContentJson().get("metadata");
	}

	/**
	 * @return The tabular report groups
	 * @throws IOException
	 *             If there was an error loading the schema
	 */
	public JsonNode getTabularReportGroups() throws IOException {
		throwLoadingException();
		return getContentJson().get("tabularReportGroups");
	}

	/**
	 * @return The query selector items
	 * @throws IOException
	 *             If there was an error loading the schema
	 */
	public JsonNode getSelectorItems() throws IOException {
		throwLoadingException();
		return getContentJson().get("selectorItems");
	}

	/**
	 * @throws IOException
	 *             If there was an error loading the schema
	 */
	private void throwLoadingException() throws IOException {
		if (exception != null) {
			throw exception;
		}
	}

}
