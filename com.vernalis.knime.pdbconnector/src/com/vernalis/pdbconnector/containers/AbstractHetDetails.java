/*******************************************************************************
 * Copyright (c) 2016,2018 Vernalis (R&D) Ltd
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
 * An abstract container class to contain the data returned for a given HetID If
 * the primary key is a structure ID, then the AbstractStructDetails class
 * should be sub-classed instead
 * <p>
 * 27-Feb-2018 Made lazy instantiation and corrected bug with formatting formula
 * when {@code &lt;formula&gt;} tag is empty - SR.
 * 
 * @author s.roughley
 * 
 */
public abstract class AbstractHetDetails {
	private String xml, HetID, Type = null, ChemName = null, Formula = null, InChiKey = null,
			InChi = null, Smiles = null;
	private Double MWt = null;

	/**
	 * Create the record from the XML string
	 * 
	 * @throws QueryParsingException
	 */
	public AbstractHetDetails(String xml) throws QueryParsingException {
		this.xml = xml;
		if (xml.matches("\\<ligand.*? chemicalID=\"(.*?)\".*")) {
			this.HetID = xml.replaceAll("\\<ligand.*? chemicalID=\"(.*?)\".*", "$1");
		} else {
			// Without a HetID cannot proceed
			throw new QueryParsingException("No Ligand ID found");
		}

	}

	public String getID() {
		return this.HetID;
	}

	public String getHetID() {
		return getID();
	}

	public String getXML() {
		return this.xml;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		if (this.Type == null && xml.matches("\\<ligand.*? type=\"(.*?)\".*")) {
			this.Type = xml.replaceAll("\\<ligand.*? type=\"(.*?)\".*", "$1");
		}
		return Type;
	}

	/**
	 * @return the mWt
	 */
	public Double getMWt() {
		if (this.MWt == null && xml.matches("\\<ligand.*? molecularWeight=\"(.*?)\".*")) {
			this.MWt = Double.parseDouble(
					xml.replaceAll("\\<ligand.*? molecularWeight=\"(.*?)\".*", "$1").trim());
		}
		return MWt;
	}

	/**
	 * @return the chemName
	 */
	public String getChemName() {
		if (this.ChemName == null
				&& xml.matches(".*\\<chemicalName\\>(.*?)\\</chemicalName\\>.*")) {
			this.ChemName = xml.replaceAll(".*\\<chemicalName\\>(.*?)\\</chemicalName\\>.*", "$1");
		}
		return ChemName;
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		if (this.Formula == null && xml.matches(".*\\<formula\\>(.*+)\\</formula\\>.*")) {
			this.Formula = xml.replaceAll(".*\\<formula\\>(.*+)\\</formula\\>.*", "$1");
			formatFormula();
		}
		return Formula;

	}

	/**
	 * @return the inChiKey
	 */
	public String getInChiKey() {
		if (this.InChiKey == null && xml.matches(".*\\<InChIKey\\>(.*?)\\</InChIKey\\>.*")) {
			this.InChiKey = xml.replaceAll(".*\\<InChIKey\\>(.*?)\\</InChIKey\\>.*", "$1");
		}
		return InChiKey;
	}

	/**
	 * @return the inChi
	 */
	public String getInChi() {
		if (this.InChi == null && xml.matches(".*\\<InChI\\>(.*?)\\</InChI\\>.*")) {
			this.InChi = xml.replaceAll(".*\\<InChI\\>(.*?)\\</InChI\\>.*", "$1");
		}
		return InChi;
	}

	/**
	 * @return the smiles
	 */
	public String getSmiles() {
		if (this.Smiles == null && xml.matches(".*\\<smiles\\>(.*?)\\</smiles\\>.*")) {
			this.Smiles = xml.replaceAll(".*\\<smiles\\>(.*?)\\</smiles\\>.*", "$1");
		}
		return Smiles;
	}

	private void formatFormula() {
		assert !this.Formula.isEmpty() : "Formula should never be empty!";

		if (this.Formula.indexOf(" ") >= 0) {
			String[] temp = this.Formula.split(" ");
			this.Formula = "";
			for (int i = 0, l = temp.length; i < l; i++) {
				temp[i] =
						temp[i].substring(0, 1).toUpperCase() + temp[i].substring(1).toLowerCase();
				this.Formula += ((i > 0) ? " " : "") + temp[i];
			}
		} else {
			this.Formula = this.Formula.substring(0, 1).toUpperCase()
					+ this.Formula.substring(1).toLowerCase();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HeterogenDetails [xml=" + xml + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((HetID == null) ? 0 : HetID.hashCode());
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractHetDetails other = (AbstractHetDetails) obj;
		if (HetID == null) {
			if (other.HetID != null)
				return false;
		} else if (!HetID.equals(other.HetID))
			return false;
		return true;
	}

}
