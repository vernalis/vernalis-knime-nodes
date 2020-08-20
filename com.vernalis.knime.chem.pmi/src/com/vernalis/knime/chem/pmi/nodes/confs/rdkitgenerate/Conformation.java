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

import java.util.Optional;

import org.RDKit.Match_Vect;
import org.RDKit.Match_Vect_Vect;
import org.RDKit.ROMol;
import org.RDKit.Transform3D;

import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

/**
 * A container class for an {@link ROMol} with single conformation which may
 * also store a conformer energy, template and RMSD from template to conformer.
 * <p>
 * H-removed versions are cached along with SMILES and Mol block
 * representations. An optimised method for checking whether another conformer
 * falls within an RMSD of the current conformer is provided.
 * </p>
 * <p>
 * A {@link #delete()} method is also provided as a convenience to allow simple
 * clean-up of wrapped RDKit objects
 * </p>
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public final class Conformation implements Comparable<Conformation> {

	private ROMol mol;
	private ROMol minusHMol = null;
	private Optional<ROMol> template;
	private final double templateRMSD;
	private final double conformerEnergy;
	private final String smiles, molblock;
	private boolean selfSymmetric;
	private Boolean noHselfSymmetric;

	/**
	 * Simple constructor only storing a molecule
	 * 
	 * @param mol
	 *            The molecule
	 */
	public Conformation(ROMol mol) {
		this(mol, 0.0);
	}

	/**
	 * Constructor storing a molecule and associated conformer energy
	 * 
	 * @param mol
	 *            The molecule
	 * @param conformerEnergy
	 *            The energy
	 */
	public Conformation(ROMol mol, double conformerEnergy) {
		this(mol, null, Double.NaN, conformerEnergy);
	}

	/**
	 * Full constructor
	 * 
	 * @param mol
	 *            The molecule
	 * @param template
	 *            The template
	 * @param templateRMSD
	 *            The RMSD to the template
	 * @param conformerEnergy
	 *            The enrgy of the conformation
	 */
	public Conformation(ROMol mol, ROMol template, double templateRMSD,
			double conformerEnergy) {
		this.mol = mol;
		this.smiles = mol.MolToSmiles();
		this.molblock = mol.MolToMolBlock();
		this.template = Optional.ofNullable(template);
		this.templateRMSD =
				this.template.isPresent() ? templateRMSD : Double.NaN;
		this.conformerEnergy = conformerEnergy;
		this.selfSymmetric = checkSelfSymmetry(this.mol);
	}

	/**
	 * @return the mol
	 */
	public final ROMol getMol() {
		return mol;
	}

	/**
	 * @return The H-removed molecule, which is lazily initialized
	 */
	public final ROMol getMinusHMol() {
		if (minusHMol == null) {
			synchronized (Conformation.class) {
				if (minusHMol == null) {
					minusHMol = mol.removeHs(false);
					noHselfSymmetric = checkSelfSymmetry(minusHMol);
				}
			}
		}
		return minusHMol;
	}

	/**
	 * Determines whether the molecule is self-symmetric (i.e. will match itself
	 * in multiple ways)
	 * 
	 * @param ignoreH
	 *            Should explicit hydrogens be ignored?
	 */
	public final boolean isSelfSymmetric(boolean ignoreH) {
		if (ignoreH) {
			if (noHselfSymmetric == null) {
				// Force lazy instantiation to happen
				getMinusHMol();
			}
			return noHselfSymmetric.booleanValue();
		} else {
			return selfSymmetric;
		}
	}

	private boolean checkSelfSymmetry(ROMol mol) {
		// Check if we need to look for symmetry in conformer
		// alignment - saves ~5-10% time
		Match_Vect_Vect mvv = mol.getSubstructMatches(mol, false);
		boolean retVal = mvv.size() > 1;
		mvv.delete();
		return retVal;
	}

	/**
	 * @return the template
	 */
	public final Optional<ROMol> getTemplate() {
		return template;
	}

	/**
	 * @return the templateRMSD
	 */
	public final double getTemplateRMSD() {
		return templateRMSD;
	}

	/**
	 * @return the conformer Energy
	 */
	public final double getConformerEnergy() {
		return conformerEnergy;
	}

	/**
	 * @return the SMILES representation
	 */
	public final String getSmiles() {
		return smiles;
	}

	/**
	 * @return the Mol block representation
	 */
	public final String getMolblock() {
		return molblock;
	}

	/**
	 * Check whether the conformation falls within a threshold of RMSD to a
	 * second conformation
	 * 
	 * @param o
	 *            The other {@link Conformation}
	 * @param threshold
	 *            The threshold
	 * @param ignoreHs
	 *            Should H's be ignored (generally much faster)
	 * @return {@code true} if the RMSD between the two {@link Conformation}s is
	 *         above the threshold
	 */
	public final boolean checkRMSDThreshold(Conformation o, double threshold,
			boolean ignoreHs) {
		if (!this.getSmiles().equals(o.getSmiles())) {
			throw new UnsupportedOperationException(
					"Cannot compare difference molecules");
		}
		ROMol testMol = ignoreHs ? o.getMinusHMol() : o.getMol();
		ROMol refMol = ignoreHs ? getMinusHMol() : getMol();
		if (isSelfSymmetric(ignoreHs)) {
			// Quick check only required
			final Transform3D transform3d = new Transform3D();
			final boolean retVal = testMol.getAlignmentTransform(refMol,
					transform3d, -1, -1) > threshold;
			transform3d.delete();
			return retVal;
		} else {
			// We need to work through each symmetry pair, but we can break out
			// if
			// we are below threshold
			Match_Vect_Vect matches =
					refMol.getSubstructMatches(testMol, false);
			for (int i = 0; i < matches.size(); i++) {
				Match_Vect match = matches.get(i);
				// #getAlignmentTransform() quicker than alignMol as doesn't
				// actually align the conformers, unlike #alignMol()
				final Transform3D transform3d = new Transform3D();
				double dist = testMol.getAlignmentTransform(refMol, transform3d,
						-1, -1, match);
				transform3d.delete();
				if (dist < threshold) {
					// Dont keep going!
					matches.delete();
					return false;
				}
			}
			// If we get here, then we passed!
			matches.delete();
			return true;
		}
	}

	/**
	 * Destroyer method to allow simple use with
	 * {@link SWIGObjectGarbageCollector2WaveSupplier}
	 */
	public void delete() {
		mol.delete();
		mol = null;
		if (template.isPresent()) {
			template.get().delete();
			template = Optional.empty();
		}
		if (minusHMol != null) {
			minusHMol.delete();
			minusHMol = null;
		}
	}

	@Override
	public int compareTo(Conformation o) {
		int retVal = this.smiles.compareTo(o.smiles);
		if (retVal == 0) {
			retVal = Double.compare(conformerEnergy, o.conformerEnergy);
		}
		if (retVal == 0) {
			// Templates come after non-templated - NB This should never
			// happen in node execution, where all should either have or not
			// have a template
			retVal = Boolean.compare(template.isPresent(),
					o.template.isPresent());
		}
		if (retVal == 0 && template.isPresent()) {
			// Both have templates, so we sort by best RMSD to template
			retVal = Double.compare(templateRMSD, o.templateRMSD);
		}
		if (retVal == 0) {
			// This is a last ditch attempt to distinguish pairs of
			// conformers of the same molecule
			retVal = this.molblock.compareTo(o.molblock);
		}
		return retVal;
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
		long temp;
		temp = Double.doubleToLongBits(conformerEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((molblock == null) ? 0 : molblock.hashCode());
		result = prime * result + ((smiles == null) ? 0 : smiles.hashCode());
		temp = Double.doubleToLongBits(templateRMSD);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Conformation)) {
			return false;
		}
		Conformation other = (Conformation) obj;
		if (Double.doubleToLongBits(conformerEnergy) != Double
				.doubleToLongBits(other.conformerEnergy)) {
			return false;
		}
		if (molblock == null) {
			if (other.molblock != null) {
				return false;
			}
		} else if (!molblock.equals(other.molblock)) {
			return false;
		}
		if (smiles == null) {
			if (other.smiles != null) {
				return false;
			}
		} else if (!smiles.equals(other.smiles)) {
			return false;
		}
		if (Double.doubleToLongBits(templateRMSD) != Double
				.doubleToLongBits(other.templateRMSD)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Conformation [");
		builder.append("smiles=").append(smiles);
		builder.append(", E=").append(conformerEnergy);
		if (template.isPresent()) {
			builder.append(", templateRMSD=").append(templateRMSD);
		}
		builder.append("]");
		return builder.toString();
	}

}
