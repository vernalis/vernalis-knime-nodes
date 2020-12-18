/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.rcsb.io.helpers;

import org.knime.bio.types.PdbCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCellFactory;

/**
 * An enum containing the new urls for PDB downloads. NB Structure Factors are
 * no longer available as a separate download
 * 
 * @author s.roughley
 *
 */
public enum RCSBFileTypes {
	@SuppressWarnings("javadoc")
	PDB {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + ".pdb.gz";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.rcsb.io.helpers.RCSBFileTypes#outputType()
		 */
		@Override
		public DataType getOutputType() {
			return PdbCellFactory.TYPE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.vernalis.rcsb.io.helpers.RCSBFileTypes#getCellFromContent(java.
		 * lang.String)
		 */
		@Override
		public DataCell getCellFromContent(String content) {
			return PdbCellFactory.create(content);
		}
	},

	@SuppressWarnings("javadoc")
	mmCIF {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + ".cif.gz";
		}
	},

	@SuppressWarnings("javadoc")
	Structure_Factors {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + "-sf.cif.gz";
		}
	},

	@SuppressWarnings("javadoc")
	PDBML {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + ".xml.gz";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.vernalis.rcsb.io.helpers.RCSBFileTypes#outputType()
		 */
		@Override
		public DataType getOutputType() {
			return XMLCellFactory.TYPE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.vernalis.rcsb.io.helpers.RCSBFileTypes#getCellFromContent(java.
		 * lang.String)
		 */
		@Override
		public DataCell getCellFromContent(String content) {
			try {
				return XMLCellFactory.create(content);
			} catch (Exception e) {
				return new StringCell(content);
			}
		}
	},

	@SuppressWarnings("javadoc")
	FASTA {

		@Override
		public String getURL(String pdbid) {
			return "http://www.rcsb.org/fasta/entry/" + pdbid + "/download";
		}
	},

	@SuppressWarnings("javadoc")
	NMR_Restraints {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + ".mr.gz";
		}
	},

	@SuppressWarnings("javadoc")
	NMR_Restraints_v2 {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + "_mr.str.gz";
		}
	},
	@SuppressWarnings("javadoc")
	NMR_Chemical_Shifts {

		@Override
		public String getURL(String pdbid) {
			return "https://files.rcsb.org/download/" + pdbid + "_cs.str.gz";
		}
	};

	/**
	 * @param pdbid
	 *            The PDB ID
	 * @return The URL for the file type for the given URL. All URLs are for
	 *         gzipped files
	 */
	public abstract String getURL(String pdbid);

	/**
	 * @return The cell type of the output
	 */
	public DataType getOutputType() {
		return StringCell.TYPE;
	}

	/**
	 * @param content
	 *            The raw text content
	 * @return The {@link DataCell} representation
	 */
	public DataCell getCellFromContent(String content) {
		return new StringCell(content);
	}

	/**
	 * @return The displayable name
	 */
	public String getName() {
		return name().replace("_", " ");
	}

	/**
	 * @return The default type
	 */
	public static RCSBFileTypes getDefault() {
		return PDB;
	}
}
