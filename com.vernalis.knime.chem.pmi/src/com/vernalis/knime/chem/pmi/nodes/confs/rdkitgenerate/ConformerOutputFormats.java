/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate;

import org.knime.chem.types.MolAdapterCell;
import org.knime.chem.types.MolCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.rdkit.knime.types.RDKitAdapterCell;
import org.rdkit.knime.types.RDKitMolCellFactory;

/**
 * Enum for the possible output formats for the node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
enum ConformerOutputFormats implements ButtonGroupEnumInterface {

	Mol {

		@Override
		public DataType getCellType() {
			return MolAdapterCell.RAW_TYPE;
		}

		@Override
		public DataCell getConformerCell(Conformation conf, boolean removeHs) {
			return MolCellFactory.createAdapterCell(removeHs
					? conf.getMinusHMol().MolToMolBlock() : conf.getMolblock());
		}

		@Override
		protected DataCell getTemplate(Conformation conf) {
			return MolCellFactory.createAdapterCell(
					conf.getTemplate().get().MolToMolBlock());
		}
	},
	RDKit {

		@Override
		public DataType getCellType() {
			return RDKitAdapterCell.RAW_TYPE;
		}

		@Override
		public DataCell getConformerCell(Conformation conf, boolean removeHs) {
			return removeHs
					? RDKitMolCellFactory
							.createRDKitAdapterCell(conf.getMinusHMol())
					: RDKitMolCellFactory.createRDKitAdapterCell(conf.getMol(),
							conf.getSmiles());
		}

		@Override
		protected DataCell getTemplate(Conformation conf) {
			return RDKitMolCellFactory
					.createRDKitAdapterCell(conf.getTemplate().get());
		}
	};

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return getText();
	}

	@Override
	public String getToolTip() {
		return getText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public abstract DataType getCellType();

	/**
	 * Method to generate the output cell of the appropriate type from a
	 * {@link Conformation}
	 * 
	 * @param conf
	 *            The conformation
	 * @param removeHs
	 *            Should explicit H's be removed
	 * @return the output cell in the appropriate format representing the
	 *         conformation
	 */
	public abstract DataCell getConformerCell(Conformation conf,
			boolean removeHs);

	/**
	 * @param conf
	 *            The {@link Conformation}
	 * @return A datacell representing the template, of a missing cell if no
	 *         template was present
	 */
	public DataCell getTemplateCell(Conformation conf) {
		if (conf.getTemplate().isPresent()) {
			return getTemplate(conf);
		} else {
			return DataType.getMissingCell();
		}
	}

	protected abstract DataCell getTemplate(Conformation conf);

	/**
	 * @return The default output format
	 */
	public static ConformerOutputFormats getDefault() {
		return Mol;
	}
}
