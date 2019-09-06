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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;

import com.vernalis.knime.nodes.propcalc.CalculatedPropertyInterface;

import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.ARTECA_REV_COMP_CHEM;
import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.JIAN_ET_AL_JMGM_2015_58_10_15;
import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.LISTER_ET_AL_INTERNAL_ROTATION_AND_INVERSION;
import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.SAUER_AND_SCHWARTZ;
import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.TANFORD_PHYS_CHEM_OF_MACROMOLECULES;
import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.VOLKENSTEIN_CONFIGURATIONAL_STATISTICS_OF_POLYMERIC_CHAINS_WILEY_NY_1963;
import static com.vernalis.knime.chem.pmi.props.ReferencesConstants.WIRTH_ET_AL_JCAMD;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.ANGSTROM;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.ANGSTROM_SQUARED;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.CUBE_ROOT;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.DALTON;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.DELIMINATOR;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.EPSILON;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.OMEGA;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.PER_ANGSTROM_SQUARED;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.POW_MINUS_ONE;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.SIGMA;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.SQUARED;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.SQUARE_ROOT;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.SUBSCRIPT_1;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.SUBSCRIPT_2;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.SUBSCRIPT_3;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.TIMES;
import static com.vernalis.knime.chem.pmi.props.UnitsTextConstants.UNITLESS;

/**
 * An enum of {@link CalculatedPropertyInterface} bas on the {@link PMI}
 * container
 * 
 * @author s.roughley
 * 
 */
public enum PrincipalMomentOfInertiaDerivedProperties
		implements CalculatedPropertyInterface<PMI> {

	PMI_1("First (smallest) Principal Moment of Inertia",
			DALTON + DELIMINATOR + ANGSTROM_SQUARED, null,
			new String[] { "I" + SUBSCRIPT_1 }, 0.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(input.I1());
		}

	},

	PMI_2("Second Principal Moment of Inertia",
			DALTON + DELIMINATOR + ANGSTROM_SQUARED, null,
			new String[] { "I" + SUBSCRIPT_2 }, 0.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(input.I2());
		}

	},

	PMI_3("Third (largest) Principal Moment of Inertia",
			DALTON + DELIMINATOR + ANGSTROM_SQUARED, null,
			new String[] { "I" + SUBSCRIPT_3 }, 0.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(input.I3());
		}

	},

	npr1("First Normalised PMI (i.e. I" + SUBSCRIPT_1 + " / I" + SUBSCRIPT_3
			+ ")", UNITLESS, new String[] { SAUER_AND_SCHWARTZ }, null, 0.0,
			1.0) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.I3() == 0.0) {
				return new DoubleCell(1.0);
			}
			return new DoubleCell(input.I1() / input.I3());
		}

	},

	npr2("Second Normalised PMI (i.e. I" + SUBSCRIPT_2 + " / I" + SUBSCRIPT_3
			+ ")", UNITLESS, new String[] { SAUER_AND_SCHWARTZ }, null, 0.5,
			1.0) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.I3() == 0.0) {
				return new DoubleCell(1.0);
			}
			return new DoubleCell(input.I2() / input.I3());
		}

	},

	Sigma_npr("npr1 + npr2", UNITLESS, new String[] { "" },
			new String[] { "Flatsum", "npr1+npr2", SIGMA + "npr" }, 1.0, 2.0) {

		@Override
		public DataCell calculate(PMI input) {

			return new DoubleCell(((DoubleValue) npr1.calculate(input))
					.getDoubleValue()
					+ ((DoubleValue) npr2.calculate(input)).getDoubleValue());
		}

	},

	Sphericity("npr1 + npr2 - 1", UNITLESS, new String[] { WIRTH_ET_AL_JCAMD },
			new String[] { "S" }, 0.0, 1.0) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(
					((DoubleValue) npr1.calculate(input)).getDoubleValue()
							+ ((DoubleValue) npr2.calculate(input))
									.getDoubleValue()
							- 1.0);
		}

	},

	Rodlikeness("npr2 - npr1", UNITLESS, new String[] { WIRTH_ET_AL_JCAMD },
			new String[] { "R", "Rod-likeness" }, 0.0, 1.0) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(((DoubleValue) npr2.calculate(input))
					.getDoubleValue()
					- ((DoubleValue) npr1.calculate(input)).getDoubleValue());
		}

	},

	Disklikeness("2 - 2" + TIMES + "npr2", UNITLESS,
			new String[] { WIRTH_ET_AL_JCAMD },
			new String[] { "D", "Disc-likeness" }, 0.0, 1.0) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(2.0 - 2.0
					* ((DoubleValue) npr2.calculate(input)).getDoubleValue());
		}

	},

	Asphericity("0.5" + TIMES + "[(I" + SUBSCRIPT_1 + " - I" + SUBSCRIPT_2 + ")"
			+ SQUARED + " + (I" + SUBSCRIPT_1 + " - I" + SUBSCRIPT_3 + ")"
			+ SQUARED + " + (I" + SUBSCRIPT_2 + " - I" + SUBSCRIPT_3 + ")"
			+ SQUARED + "]/[I" + SUBSCRIPT_1 + SQUARED + " + I" + SUBSCRIPT_2
			+ SQUARED + " + I" + SUBSCRIPT_3 + SQUARED + "]; "
			+ "0 corresponds to a spherical top and 1 to a linear molecule.  "
			+ "~0.25 corresponds to prolate (cigar-shaped) molecules, and "
			+ "disc-shaped ~1", UNITLESS, new String[] { ARTECA_REV_COMP_CHEM },
			new String[] { OMEGA + "A" }, 0.0, 1.0) {

		@Override
		public DataCell calculate(PMI input) {

			double numerator = Math.pow(input.I1() - input.I2(), 2.0);
			numerator += Math.pow(input.I1() - input.I3(), 2.0);
			numerator += Math.pow(input.I2() - input.I3(), 2.0);
			double denom = input.I1() * input.I1() + input.I2() * input.I2()
					+ input.I3() * input.I3();
			denom *= 2.0;
			return new DoubleCell(numerator / denom);
		}

	},

	Inertial_Shape_Factor(
			"I" + SUBSCRIPT_2 + " / (I" + SUBSCRIPT_1 + TIMES + "I"
					+ SUBSCRIPT_3 + ");  Undefined for planar molecules",
			DALTON + POW_MINUS_ONE + DELIMINATOR + PER_ANGSTROM_SQUARED,
			new String[] { LISTER_ET_AL_INTERNAL_ROTATION_AND_INVERSION },
			new String[] { "S<sub>I</sub>" }, 0.0, 1.0) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(input.I2() / (input.I1() * input.I3()));
		}

	},

	Molecule_Eccentricity(
			SQUARE_ROOT + "(I" + SUBSCRIPT_1 + SQUARED + " - I" + SUBSCRIPT_3
					+ SQUARED + ") / I" + SUBSCRIPT_2
					+ "; 0 corresponds to a spherical top, and 1 to a linear molecule",
			DALTON + POW_MINUS_ONE + DELIMINATOR + PER_ANGSTROM_SQUARED,
			new String[] { ARTECA_REV_COMP_CHEM }, new String[] { EPSILON },
			0.0, 1.0) {

		@Override
		public DataCell calculate(PMI input) {
			double num = Math.pow(
					input.I1() * input.I1() - input.I3() * input.I3(), 0.5);
			return new DoubleCell(num / input.I2());
		}

	},

	Gyradius("Radius of Gyration", ANGSTROM, new String[] { "" },
			new String[] { "RG", "Radius of Gyration", "Gyradius" }, 0.0,
			Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			if (input.I1() == 0.0) {
				// 2D formula
				return new DoubleCell(Math.pow(
						Math.pow(input.I2() * input.I1(), 0.5) / input.MWt(),
						0.5));
			}
			// 3D formula
			return new DoubleCell(Math.pow(2.0 * Math.PI
					* Math.pow(input.I3() * input.I2() * input.I1(), 1.0 / 3.0)
					/ input.MWt(), 0.5));
		}

	},

	Gyradius_2D(
			"Radius of Gyration (2D) = " + SQUARE_ROOT + "(I" + SUBSCRIPT_1
					+ TIMES + "I" + SUBSCRIPT_2 + ") / MWt",
			ANGSTROM,
			new String[] {
					VOLKENSTEIN_CONFIGURATIONAL_STATISTICS_OF_POLYMERIC_CHAINS_WILEY_NY_1963 },
			new String[] { "RG", "Radius of Gyration", "Gyradius" }, 0.0,
			Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			// 2D formula
			return new DoubleCell(Math.pow(
					Math.pow(input.I2() * input.I1(), 0.5) / input.MWt(), 0.5));

		};
	},

	Gyradius_3D("Radius of Gyration (3D) = " + CUBE_ROOT + "(I" + SUBSCRIPT_1
			+ TIMES + "I" + SUBSCRIPT_2 + TIMES + "I" + SUBSCRIPT_3 + ") / MWt",
			ANGSTROM, new String[] { TANFORD_PHYS_CHEM_OF_MACROMOLECULES },
			new String[] { "RG", "Radius of Gyration", "Gyradius" }, 0.0,
			Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.I1() == 0.0) {
				return DataType.getMissingCell();
			}
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			// 3D formula
			return new DoubleCell(Math.pow(2.0 * Math.PI
					* Math.pow(input.I3() * input.I2() * input.I1(), 1.0 / 3.0)
					/ input.MWt(), 0.5));
		};
	},

	PRG_0("1st Principle Radius of Gyration (R0 = " + SQUARE_ROOT + "(I"
			+ SUBSCRIPT_1 + " / MWt))", ANGSTROM,
			new String[] { JIAN_ET_AL_JMGM_2015_58_10_15 },
			new String[] { "R0" }, 0.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			return new DoubleCell(Math.sqrt(input.I1() / input.MWt()));
		}

	},

	PRG_1("2nd Principle Radius of Gyration (R1 = " + SQUARE_ROOT + "(I"
			+ SUBSCRIPT_2 + " / MWt))", ANGSTROM,
			new String[] { JIAN_ET_AL_JMGM_2015_58_10_15 },
			new String[] { "R1" }, 0.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			return new DoubleCell(Math.sqrt(input.I2() / input.MWt()));
		}

	},

	PRG_2("3rd Principle Radius of Gyration (R2 = " + SQUARE_ROOT + "(I"
			+ SUBSCRIPT_3 + " / MWt))", ANGSTROM,
			new String[] { JIAN_ET_AL_JMGM_2015_58_10_15 },
			new String[] { "R2" }, 0.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			return new DoubleCell(Math.sqrt(input.I3() / input.MWt()));
		}

	},

	Gyradius_Ratio_1("1st Gyradius Ratio (r1 = PRG1 / PRG0)", UNITLESS,
			new String[] { JIAN_ET_AL_JMGM_2015_58_10_15 },
			new String[] { "r1" }, 1.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			if (input.MWt() == null) {
				// We need the MWt
				return DataType.getMissingCell();
			}
			return new DoubleCell(((DoubleValue) PRG_1.calculate(input))
					.getDoubleValue()
					/ ((DoubleValue) PRG_0.calculate(input)).getDoubleValue());
		}

	},

	Gyradius_Ratio_2("2nd Gyradius Ratio (r2 = PRG2 / PRG0)", UNITLESS,
			new String[] { JIAN_ET_AL_JMGM_2015_58_10_15 },
			new String[] { "r2" }, 2.0, Double.POSITIVE_INFINITY) {

		@Override
		public DataCell calculate(PMI input) {
			return new DoubleCell(((DoubleValue) PRG_2.calculate(input))
					.getDoubleValue()
					/ ((DoubleValue) PRG_0.calculate(input)).getDoubleValue());
		}

	};

	private String description, units;
	private String[] refs, aliases;
	private Number min, max;

	/**
	 * Overloaded constructor when there are only a description and units
	 * 
	 * @param description
	 *            A brief description of the property
	 * @param units
	 *            The units of the property
	 */
	private PrincipalMomentOfInertiaDerivedProperties(String description,
			String units) {
		this(description, units, null, null, null, null);
	}

	/**
	 * Overloaded constructor when there are no aliases
	 * 
	 * @param description
	 *            A brief description of the property
	 * @param units
	 *            The units of the property
	 * @param references
	 *            Literature references
	 */
	private PrincipalMomentOfInertiaDerivedProperties(String description,
			String units, String... references) {
		this(description, units, references, null, null, null);
	}

	/**
	 * Overloaded constructor when there is no minimum or maximum value
	 * 
	 * @param description
	 *            A brief description of the property
	 * @param units
	 *            The units of the property
	 * @param references
	 *            Literature references
	 * @param aliases
	 *            Aliases used in the literature
	 */
	private PrincipalMomentOfInertiaDerivedProperties(String description,
			String units, String[] references, String... aliases) {
		this(description, units, references, aliases, null, null);
	}

	/**
	 * Full Constructor
	 * 
	 * @param description
	 *            A brief description of the property
	 * @param units
	 *            The units of the property
	 * @param references
	 *            Literature references
	 * @param aliases
	 *            Aliases used in the literature
	 * @param min
	 *            The minimum possible value
	 * @param max
	 *            The maximum possible value
	 */
	private PrincipalMomentOfInertiaDerivedProperties(String description,
			String units, String[] references, String[] aliases, Number min,
			Number max) {
		this.description = description;
		this.units = units;
		this.refs = references;
		this.aliases = aliases;
		this.min = min;
		this.max = max;
	}

	@Override
	public String getName() {
		return this.name().replace("_", " ");
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String[] getReferences() {
		return refs;
	}

	@Override
	public String[] getAliases() {
		return aliases;
	}

	@Override
	public Number getMinimum() {
		return min;
	}

	@Override
	public Number getMaximum() {
		return max;
	}

	@Override
	public String getUnits() {
		return units;
	}

	@Override
	public DataType getType() {
		return DoubleCell.TYPE;
	}
}
