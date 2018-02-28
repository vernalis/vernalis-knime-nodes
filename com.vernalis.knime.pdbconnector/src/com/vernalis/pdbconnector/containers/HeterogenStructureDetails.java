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
 * Implementation of the {@link AbstractHetDetails} class used for SMILES query
 * parsing
 */
public class HeterogenStructureDetails extends AbstractHetDetails
		implements Comparable<HeterogenStructureDetails> {
	private final String StructureID;

	public HeterogenStructureDetails(String xml) throws QueryParsingException {
		super(xml);
		if (xml.matches("\\<ligand.*? structureId=\"(.*?)\".*")) {
			this.StructureID = xml.replaceAll("\\<ligand.*? structureId=\"(.*?)\".*", "$1");
		} else {
			// We must have a structure ID in this case
			throw new QueryParsingException("No structure ID found in " + xml);
		}
	}

	public String getStructureID() {
		return this.StructureID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((StructureID == null) ? 0 : StructureID.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		HeterogenStructureDetails other = (HeterogenStructureDetails) obj;
		if (StructureID == null) {
			if (other.StructureID != null)
				return false;
		} else if (!StructureID.equals(other.StructureID))
			return false;
		return true;
	}

	@Override
	public int compareTo(HeterogenStructureDetails that) {
		// Sort by HetID then by StructureID - Case-Insensitive
		if (this.getHetID().equalsIgnoreCase(that.getHetID())) {
			return this.StructureID.compareToIgnoreCase(that.StructureID);
		} else {
			return this.getHetID().compareToIgnoreCase(that.getHetID());
		}
	}
}
