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
package com.vernalis.pdbconnector.containers;

/**
 * A container class to contain the data returned for a given HetID
 * 
 * @author s.roughley
 * 
 */
public class HeterogenDetails extends AbstractHetDetails implements Comparable<HeterogenDetails> {

	/**
	 * Create the record from the XML string
	 * 
	 * @throws QueryParsingException
	 */
	public HeterogenDetails(String xml) throws QueryParsingException {
		super(xml);

	}

	@Override
	public int compareTo(HeterogenDetails that) {
		// Compare solely on Heterogen ID
		return getHetID().compareToIgnoreCase(that.getHetID());
	}

}
