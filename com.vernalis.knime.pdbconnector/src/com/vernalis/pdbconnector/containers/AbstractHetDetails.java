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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final Pattern HET_ID_PATTERN =
			Pattern.compile("\\<ligand.*? chemicalID=\"(.+?)\".*");
	private static final Pattern SMILES_PATTERN =
			Pattern.compile(".*\\<smiles\\>(.+?)\\</smiles\\>.*");
	private static final Pattern INCHI_PATTERN =
			Pattern.compile(".*\\<InChI\\>(.+?)\\</InChI\\>.*");
	private static final Pattern INCHIKEY_PATTERN =
			Pattern.compile(".*\\<InChIKey\\>(.+?)\\</InChIKey\\>.*");
	private static final Pattern FORMULA_PATTERN =
			Pattern.compile(".*\\<formula\\>(.+?)\\</formula\\>.*");
	private static final Pattern CHEMICAL_NAME_PATTERN =
			Pattern.compile(".*\\<chemicalName\\>(.+?)\\</chemicalName\\>.*");
	private static final Pattern MWT_PATTERN =
			Pattern.compile("\\<ligand.*? molecularWeight=\"(.*?)\".*");
	private static final Pattern LIGAND_PATTERN = Pattern.compile("\\<ligand.*? type=\"(.*?)\".*");
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
		Matcher m = HET_ID_PATTERN.matcher(xml);
		if (m.find()) {
			this.HetID = m.group(1);
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
		if (this.Type == null) {
			Matcher m = LIGAND_PATTERN.matcher(xml);
			if (m.find()) {
				this.Type = m.group(1);
			}
		}
		return Type;
	}

	/**
	 * @return the mWt
	 * @throws QueryParsingException
	 *             If the value cannot be parser as a number
	 */
	public Double getMWt() throws QueryParsingException {
		if (this.MWt == null) {
			Matcher m = MWT_PATTERN.matcher(xml);
			if (m.find()) {
				try {
					this.MWt = Double.parseDouble(m.group(1).trim());
				} catch (Exception e) {
					throw new QueryParsingException("Error parsing MWt from " + xml, e);
				}
			}
		}
		return MWt;

	}

	/**
	 * @return the chemName
	 */
	public String getChemName() {
		if (this.ChemName == null) {
			Matcher m = CHEMICAL_NAME_PATTERN.matcher(xml);
			if (m.find()) {
				this.ChemName = m.group(1);
			}
		}
		return ChemName;
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		if (this.Formula == null) {
			Matcher m = FORMULA_PATTERN.matcher(xml);
			if (m.find()) {
				this.Formula = m.group(1);
				formatFormula();
			}
		}
		return Formula;

	}

	/**
	 * @return the inChiKey
	 */
	public String getInChiKey() {
		if (this.InChiKey == null) {
			Matcher m = INCHIKEY_PATTERN.matcher(xml);
			if (m.find()) {
				this.InChiKey = m.group(1);
			}
		}
		return InChiKey;
	}

	/**
	 * @return the inChi
	 */
	public String getInChi() {
		if (this.InChi == null) {
			Matcher m = INCHI_PATTERN.matcher(xml);
			if (m.find()) {
				this.InChi = m.group(1);
			}
		}
		return InChi;
	}

	/**
	 * @return the smiles
	 */
	public String getSmiles() {
		if (this.Smiles == null) {
			Matcher m = SMILES_PATTERN.matcher(xml);
			if (m.find()) {
				this.Smiles = m.group(1);
			}
		}
		return Smiles;
	}

	private void formatFormula() {
		assert !this.Formula.isEmpty() : "Formula should never be empty!";

		if (this.Formula.contains(" ")) {
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
