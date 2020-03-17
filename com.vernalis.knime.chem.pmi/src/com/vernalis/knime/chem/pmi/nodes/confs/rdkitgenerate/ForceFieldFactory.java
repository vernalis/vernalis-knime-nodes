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

import org.RDKit.ForceField;
import org.RDKit.ROMol;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Simple enum to allow the user to select an RDKit forcefield and generate the
 * forcefield for the molecule
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public enum ForceFieldFactory implements ButtonGroupEnumInterface {
	UFF {

		@Override
		public ForceField getForceField(ROMol mol, boolean substituteUFF) {
			if (ForceField.UFFHasAllMoleculeParams(mol)) {
				return ForceField.UFFGetMoleculeForceField(mol);
			}
			NodeLogger.getLogger(ForceFieldFactory.class)
					.info("Unable to generate UFF - missing parameters");
			return null;
		}
	},
	MMFF94 {

		@Override
		public ForceField getForceField(ROMol mol, boolean substituteUFF) {
			ForceField ff = ForceField.MMFFGetMoleculeForceField(mol, "MMFF94");
			if (ff == null && substituteUFF) {
				NodeLogger.getLogger(ForceFieldFactory.class)
						.info("Unable to create MMFF94 - trying UFF ("
								+ mol.MolToSmiles() + ")");
				ff = UFF.getForceField(mol, substituteUFF);
			}
			return ff;
		}
	},
	MMFF94S {

		@Override
		public ForceField getForceField(ROMol mol, boolean substituteUFF) {
			ForceField ff =
					ForceField.MMFFGetMoleculeForceField(mol, "MMFF94S");
			if (ff == null && substituteUFF) {
				NodeLogger.getLogger(ForceFieldFactory.class)
						.info("Unable to create MMFF94S - trying UFF ("
								+ mol.MolToSmiles() + ")");
				ff = UFF.getForceField(mol, substituteUFF);
			}
			return ff;
		}
	};

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public String getToolTip() {
		return name();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return the default forcefield
	 */
	public static ForceFieldFactory getDefault() {
		return UFF;
	}

	/**
	 * Return the RDKit forcefield for the molecule
	 * 
	 * @param mol
	 *            The molecule
	 * @param substituteUFF
	 *            Should UFF be returned if unable to generate the forcefield?
	 * @return The required forcefield
	 */
	public abstract ForceField getForceField(ROMol mol, boolean substituteUFF);
}
