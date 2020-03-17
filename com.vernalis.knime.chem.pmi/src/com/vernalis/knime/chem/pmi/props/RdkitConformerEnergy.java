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
package com.vernalis.knime.chem.pmi.props;

import org.RDKit.ForceField;
import org.RDKit.ROMol;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;

import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;

/**
 * Enum of {@link CalculatedPropertyInterface} for calculating conformer
 * energies for a molecule using various forcefields
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public enum RdkitConformerEnergy implements CalculatedPropertyInterface<ROMol> {
	MMFF94_Energy(
			"The MMFF94 energy of the conformer") {/*
													 * (non-Javadoc)
													 * 
													 * @see com.vernalis.knime
													 * .internal
													 * .pmi.util.props.
													 * CalculatedPropertyInterface
													 * #calculate(java.lang.
													 * Object)
													 */

		@Override
		public DataCell calculate(ROMol input) {
			if (ForceField.MMFFHasAllMoleculeParams(input)) {
				ForceField ff =
						ForceField.MMFFGetMoleculeForceField(input, "MMFF94");
				ff.initialize();
				double retVal = ff.calcEnergy();
				ff.delete();
				return new DoubleCell(retVal);
			}
			return DataType.getMissingCell();
		};
	},

	MMFF94S_Energy(
			"The MMFF94S energy of the conformer") {/*
													 * (non-Javadoc)
													 * 
													 * @see com.vernalis.
													 * knime.internal
													 * .pmi.util.props.
													 * CalculatedPropertyInterface
													 * # calculate(java.lang
													 * .Object )
													 */

		@Override
		public DataCell calculate(ROMol input) {
			if (ForceField.MMFFHasAllMoleculeParams(input)) {
				ForceField ff =
						ForceField.MMFFGetMoleculeForceField(input, "MMFF94S");
				ff.initialize();
				double retVal = ff.calcEnergy();
				ff.delete();
				return new DoubleCell(retVal);
			}
			return DataType.getMissingCell();
		};
	},
	UFF_Energy(
			"The UFF energy of the conformer") {/*
												 * (non-Javadoc)
												 * 
												 * @see
												 * com.vernalis.knime.internal
												 * .pmi .util .props.
												 * CalculatedPropertyInterface #
												 * calculate(java.lang.Object )
												 */

		@Override
		public DataCell calculate(ROMol input) {
			if (ForceField.UFFHasAllMoleculeParams(input)) {
				ForceField ff = ForceField.UFFGetMoleculeForceField(input);
				ff.initialize();
				double retVal = ff.calcEnergy();
				ff.delete();
				return new DoubleCell(retVal);
			}
			return DataType.getMissingCell();
		};
	};

	private String description;
	private String[] refs;
	private String[] aliases;
	private Number min;
	private Number max;

	private RdkitConformerEnergy(String description) {
		this.description = description;
		this.refs = null;
		this.aliases = null;
		this.min = Double.NEGATIVE_INFINITY;
		this.max = Double.POSITIVE_INFINITY;
	}

	@Override
	public String getName() {
		return this.name().replace("_", " ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.props.CalculatedPropertyInterface
	 * #getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.props.CalculatedPropertyInterface
	 * #getReferences()
	 */
	@Override
	public String[] getReferences() {
		return refs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.props.CalculatedPropertyInterface
	 * #getAliases()
	 */
	@Override
	public String[] getAliases() {
		return aliases;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.props.CalculatedPropertyInterface
	 * #getMinimum()
	 */
	@Override
	public Number getMinimum() {
		return min;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.props.CalculatedPropertyInterface
	 * #getMaximum()
	 */
	@Override
	public Number getMaximum() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.pmi.props.CalculatedPropertyInterface
	 * #getUnits()
	 */
	@Override
	public String getUnits() {
		return "kcal" + UnitsTextConstants.DELIMINATOR + "mol"
				+ UnitsTextConstants.POW_MINUS_ONE;
	}

	@Override
	public DataType getType() {
		return DoubleCell.TYPE;
	}
}
