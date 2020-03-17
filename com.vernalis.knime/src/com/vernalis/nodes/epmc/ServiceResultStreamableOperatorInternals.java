/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.epmc;

import org.knime.core.node.streamable.simple.SimpleStreamableOperatorInternals;

/**
 * A simple Streamable Operator Internals class to gather the parameters for
 * saving as a flow variable output during streaming execution
 * 
 * @author S.Roughley
 *
 */
public class ServiceResultStreamableOperatorInternals extends SimpleStreamableOperatorInternals {

	/**
	 * Constructor
	 */
	public ServiceResultStreamableOperatorInternals() {

	}

	/**
	 * Set the hitcount
	 */
	public void setHitCount(long hitCount) {
		this.getConfig().addLong("hitCount", hitCount);
	}

	/**
	 * @return the hit count
	 */
	public long getHitCount() {
		return this.getConfig().getLong("hitCount", -1L);
	}

	/**
	 * Set the page count
	 */
	public void setPageCount(long pageCount) {
		this.getConfig().addLong("pageCount", pageCount);
	}

	/**
	 * @return the page count
	 */
	public long getPageCount() {
		return this.getConfig().getLong("pageCount", -1L);
	}

	/**
	 * Set the query string
	 */
	public void setQueryString(String query) {
		this.getConfig().addString("query", query);
	}

	/**
	 * @return the query string
	 */
	public String getQueryString() {
		return this.getConfig().getString("query", "");
	}

	/**
	 * Set the query url
	 */
	public void setQueryURL(String url) {
		this.getConfig().addString("url", url);
	}

	/**
	 * @return the query url
	 */
	public String getQueryURL() {
		return this.getConfig().getString("url", "");
	}

	/**
	 * Set the result type
	 */
	public void setResultType(String type) {
		this.getConfig().addString("type", type);
	}

	/**
	 * @return the result type
	 */
	public String getResultType() {
		return this.getConfig().getString("type", "");
	}

	/**
	 * Set the query as obtained from the xml
	 */
	public void setQueryFromXML(String query) {
		this.getConfig().addString("XMLQuery", query);
	}

	/**
	 * @return the query as obtained from the xml
	 */
	public String getQueryFromXML() {
		return this.getConfig().getString("XMLQuery", "");
	}

	/**
	 * Set the service version
	 */
	public void setVersion(String version) {
		this.getConfig().addString("version", version);
	}

	/**
	 * @return the service version
	 */
	public String getVersion() {
		return this.getConfig().getString("version", "");
	}
}
