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
package com.vernalis.knime.chem.util.points;

import static com.vernalis.knime.misc.StringParseUtils.trimRight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.RDKit.Atom;
import org.RDKit.Conformer;
import org.RDKit.Point3D;
import org.RDKit.ROMol;
import org.knime.bio.types.PdbValue;
import org.knime.chem.types.CtabValue;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;

import com.vernalis.knime.chem.util.elements.Elements;
import com.vernalis.knime.iterators.LinewiseStringIterator;

/**
 * This is a utility class containing methods for creating points from various
 * molecule formats, and lists of points from whole molecules
 *
 * @author s.roughley
 *
 */
public class PointFactory {

	private PointFactory() {
	}

	/**
	 * Method to create a {@link SimplePoint} from an RDKit {@link Point3D}
	 * object
	 *
	 * @param point
	 *            The RDKit point
	 * @return the point
	 */
	public static SimplePoint fromROMol(Point3D point) {
		return new SimplePoint(point.getX(), point.getY(), point.getZ());
	}

	/**
	 * Method to create a {@link SimplePoint} from an RDKit {@link ROMol} and
	 * atom index
	 *
	 * @param mol
	 *            The molecule
	 * @param atomIdx
	 *            The atom index
	 * @return The point
	 */
	public static SimplePoint fromROMol(ROMol mol, int atomIdx) {
		final Conformer conformer = mol.getConformer();
		final Point3D pt = conformer.getAtomPos(atomIdx);
		final SimplePoint retVal = fromROMol(pt);
		pt.delete();
		conformer.delete();
		return retVal;
	}

	/**
	 * Method to create an {@link AbstractPoint} from an RDKit {@link ROMol} and
	 * atom index
	 *
	 * @param mol
	 *            The molecule
	 * @param atomIdx
	 *            The atom index
	 * @param propertyFunction
	 *            the function to determine how the point property is determined
	 *            from the {@link ROMol} and atom index
	 * @return The point
	 */
	public static <T> AbstractPoint<T> fromROMol(ROMol mol, int atomIdx,
			BiFunction<ROMol, Integer, T> propertyFunction) {
		final Conformer conformer = mol.getConformer();
		final Point3D pt = conformer.getAtomPos(atomIdx);
		final AbstractPoint<T> retVal = new AbstractPoint<>(pt.getX(),
				pt.getY(), pt.getZ(), propertyFunction.apply(mol, atomIdx));
		conformer.delete();
		pt.delete();
		return retVal;
	}

	public static <T> AbstractPoint<T> fromROMol(ROMol mol, int atomIdx,
			Function<Atom, T> propertyFunction) {
		final Conformer conformer = mol.getConformer();
		final Point3D pt = conformer.getAtomPos(atomIdx);
		final AbstractPoint<T> retVal =
				new AbstractPoint<>(pt.getX(), pt.getY(), pt.getZ(),
						propertyFunction.apply(mol.getAtomWithIdx(atomIdx)));
		conformer.delete();
		pt.delete();
		return retVal;
	}

	/**
	 * @param pdbLine
	 *            A PDB Atom line record
	 * @return The point
	 */
	public static SimplePoint fromPDBLine(String pdbLine) {
		return new SimplePoint(Double.valueOf(pdbLine.substring(30, 38)),
				Double.valueOf(pdbLine.substring(38, 46)),
				Double.valueOf(pdbLine.substring(46, 54)));
	}

	/**
	 * @param pdbLine
	 *            A PDB Atom line record
	 * @param propertyFunction
	 *            Function to get the point property from the atom line
	 * @return The point
	 */
	public static <T> AbstractPoint<T> fromPDBLine(String pdbLine,
			Function<String, T> propertyFunction) {
		return new AbstractPoint<>(Double.valueOf(pdbLine.substring(30, 38)),
				Double.valueOf(pdbLine.substring(38, 46)),
				Double.valueOf(pdbLine.substring(46, 54)),
				propertyFunction.apply(pdbLine));
	}

	/**
	 * @param ctabLine
	 *            The cTab line to get the point from
	 * @return The point
	 */
	public static SimplePoint fromCtabLine(String ctabLine) {
		return new SimplePoint(Double.valueOf(ctabLine.substring(0, 10)),
				Double.valueOf(ctabLine.substring(10, 20)),
				Double.valueOf(ctabLine.substring(20, 30)));
	}

	/**
	 * @param ctabLine
	 *            The cTab line to get the point from
	 * @param propertyFunction
	 *            Function to get the point property from the atom line
	 * @return The point
	 */
	public static <T> AbstractPoint<T> fromCtabLine(String ctabLine,
			Function<String, T> propertyFunction) {
		return new AbstractPoint<>(Double.valueOf(ctabLine.substring(0, 10)),
				Double.valueOf(ctabLine.substring(10, 20)),
				Double.valueOf(ctabLine.substring(20, 30)),
				propertyFunction.apply(ctabLine));
	}

	/**
	 * Regex representing a Ctab V3000 atom line
	 */
	private static final Pattern CTAB3000_LINE_PATTERN = Pattern.compile(
			"M  V30 \\s*\\d+\\s+[A-Za-z]+\\s+([+-]?\\d*\\.?\\d*)\\s+([+-]?\\d*\\.?\\d*)\\s+([+-]?\\d*\\.?\\d*).*");

	/**
	 * @param ctab3000Line
	 *            The cTab v3000 line to get the point from
	 * @return The point
	 */
	public static SimplePoint fromCtab3000Line(String ctab3000Line) {
		final Matcher m = CTAB3000_LINE_PATTERN.matcher(ctab3000Line);
		m.find();
		return new SimplePoint(Double.valueOf(m.group(1)),
				Double.valueOf(m.group(2)), Double.valueOf(m.group(3)));
	}

	/**
	 * @param ctab3000Line
	 *            The cTab v3000 line to get the point from
	 * @param propertyFunction
	 *            Function to get the point property from the atom line
	 * @return The point
	 */
	public static <T> AbstractPoint<T> fromCtab3000Line(String ctab3000Line,
			Function<String, T> propertyFunction) {
		final Matcher m = CTAB3000_LINE_PATTERN.matcher(ctab3000Line);
		m.find();
		return new AbstractPoint<>(Double.valueOf(m.group(1)),
				Double.valueOf(m.group(2)), Double.valueOf(m.group(3)),
				propertyFunction.apply(ctab3000Line));
	}

	/**
	 * @param mol
	 *            An RDKit {@link ROMol}
	 * @return A list of points from the mol
	 */
	public static List<SimplePoint> getPointsFromROMol(ROMol mol) {
		final List<SimplePoint> retVal = new ArrayList<>();
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			retVal.add(fromROMol(mol, i));
		}
		return retVal;
	}

	/**
	 * @param mol
	 *            An RDKit {@link ROMol}
	 * @param propertyFunction
	 *            Function to get point property from RDKit {@link Atom}
	 * @return A list of points from the mol
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromROMol(ROMol mol,
			Function<Atom, T> propertyFunction) {
		final List<AbstractPoint<T>> retVal = new ArrayList<>();
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			retVal.add(fromROMol(mol, i, propertyFunction));
		}
		return retVal;
	}

	/**
	 * @param mol
	 *            An RDKit {@link ROMol}
	 * @param propertyFunction
	 *            Function to get point property from RDKit {@link ROMol} and
	 *            atom index
	 * @return A list of points from the mol
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromROMol(ROMol mol,
			BiFunction<ROMol, Integer, T> propertyFunction) {
		final List<AbstractPoint<T>> retVal = new ArrayList<>();
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			retVal.add(fromROMol(mol, i, propertyFunction));
		}
		return retVal;
	}

	/**
	 * @param pdb
	 *            {@link PdbValue} to get points from
	 * @return List of atom points
	 */
	public static List<SimplePoint> getPointsFromPDBString(PdbValue pdb) {
		return getPointsFromPDBString(pdb.getPdbValue());
	}

	/**
	 * @param pdb
	 *            The {@link PdbValue} string to get points from
	 * @param propertyFunction
	 *            Function to get the point property from the atom line
	 * @return List of atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromPDBString(
			PdbValue pdb, Function<String, T> propertyFunction) {
		return getPointsFromPDBString(pdb.getPdbValue(), propertyFunction);
	}

	/**
	 * @param pdb
	 *            PDB String to get points from
	 * @return List of atom points
	 */
	public static List<SimplePoint> getPointsFromPDBString(String pdb) {
		return new LinewiseStringIterator(pdb).getAsStream().filter(
				line -> line.startsWith("ATOM  ") || line.startsWith("HETATM"))
				.map(atom -> fromPDBLine(atom))
				.collect(Collectors.toCollection(ArrayList::new));

	}

	/**
	 * @param pdb
	 *            The PDB String string to get points from
	 * @param propertyFunction
	 *            Function to get the point property from the atom line
	 * @return List of atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromPDBString(String pdb,
			Function<String, T> propertyFunction) {
		return new LinewiseStringIterator(pdb).getAsStream().filter(
				line -> line.startsWith("ATOM  ") || line.startsWith("HETATM"))
				.map(atom -> fromPDBLine(atom, propertyFunction))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * @param mol
	 *            The {@link MolValue} to get the points from
	 * @return List of atom points
	 */
	public static List<SimplePoint> getPointsFromMolString(MolValue mol) {
		return getPointsFromMolString(mol.getMolValue());
	}

	/**
	 * @param mol
	 *            The {@link MolValue} to get the points from
	 * @param propertyFunction
	 *            Function to get the point property from the Mol atom line
	 * @return List of Atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromMolString(
			MolValue mol, Function<String, T> propertyFunction) {
		return getPointsFromMolString(mol.getMolValue(), propertyFunction);
	}

	/**
	 * @param sdf
	 *            The {@link SdfValue} to get the points from
	 * @return List of atom points
	 */
	public static List<SimplePoint> getPointsFromMolString(SdfValue sdf) {
		return getPointsFromMolString(sdf.getSdfValue());
	}

	/**
	 * @param sdf
	 *            The {@link SdfValue} to get the points from
	 * @param propertyFunction
	 *            Function to get the point property from the Mol atom line
	 * @return List of Atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromMolString(
			SdfValue sdf, Function<String, T> propertyFunction) {
		return getPointsFromMolString(sdf.getSdfValue(), propertyFunction);
	}

	/**
	 * @param mol
	 *            The {@link MolValue} to get the points from
	 * @return List of atom points
	 */
	public static List<SimplePoint> getPointsFromMolString(String mol) {
		final List<SimplePoint> retVal = new ArrayList<>();
		final String[] lines = mol.split("\n");
		if (lines[3].contains("V3000")) {
			int lineIdx = 3;
			boolean inAtoms = false;
			while (lineIdx++ < lines.length) {
				String line = lines[lineIdx].trim().toUpperCase();
				if (line.equals("M V30 END ATOM")) {
					break;
				}
				if (line.equalsIgnoreCase("M V30 BEGIN ATOM")) {
					inAtoms = true;
					continue;
				}
				if (!inAtoms) {
					continue;
				}
				// We have an atom
				retVal.add(fromCtab3000Line(line));
				// We dont care about continuation lines
				while (line.endsWith("-")) {
					line = lines[lineIdx++];
				}
			}
		} else {
			// Assume old V2000 format
			final int atomCount = Integer.valueOf(lines[3].substring(0, 3).trim());
			for (int i = 4; i < 4 + atomCount; i++) {
				retVal.add(fromCtabLine(lines[i]));
			}
		}
		return retVal;
	}

	/**
	 * @param sdf
	 *            The {@link SdfValue} to get the points from
	 * @param propertyFunction
	 *            Function to get the point property from the Mol atom line
	 * @return List of Atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromMolString(String mol,
			Function<String, T> propertyFunction) {
		final List<AbstractPoint<T>> retVal = new ArrayList<>();
		final String[] lines = mol.split("\n");
		if (lines[3].contains("V3000")) {
			int lineIdx = 3;
			boolean inAtoms = false;
			while (lineIdx++ < lines.length) {
				String line = lines[lineIdx].trim();
				if (line.equalsIgnoreCase("M  V30 END ATOM")) {
					break;
				}
				if (line.equalsIgnoreCase("M  V30 BEGIN ATOM")) {
					inAtoms = true;
					continue;
				}
				if (!inAtoms) {
					continue;
				}
				// We have an atom
				while (line.endsWith("-")) {
					line = line.substring(0, line.length() - 1)
							+ trimRight(lines[lineIdx++].substring(7));
				}
				retVal.add(fromCtab3000Line(line, propertyFunction));
			}
		} else {
			// Assume old V2000 format
			final int atomCount = Integer.valueOf(lines[3].substring(0, 3).trim());
			for (int i = 4; i < 4 + atomCount; i++) {
				retVal.add(fromCtabLine(lines[i], propertyFunction));
			}
		}
		return retVal;
	}

	/**
	 * @param ctab
	 *            The {@link CtabValue} to get the points from
	 * @return List of Atom points
	 */
	public static List<SimplePoint> getPointsFromCTabString(CtabValue ctab) {
		return getPointsFromCTabString(ctab.getCtabValue());
	}

	/**
	 * @param ctab
	 *            The {@link CtabValue} to get the points from
	 * @param propertyFunction
	 *            Function to get the point property from the Ctab atom line
	 * @return List of Atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromCTabString(
			CtabValue ctab, Function<String, T> propertyFunction) {
		return getPointsFromCTabString(ctab.getCtabValue(), propertyFunction);
	}

	/**
	 * @param ctab
	 *            Thectab string to get the points from
	 * @return List of Atom points
	 */
	public static <T> List<SimplePoint> getPointsFromCTabString(String ctab) {
		final String[] lines = ctab.split("\n");
		final int atomCount = Integer.valueOf(lines[0].substring(0, 3).trim());
		final List<SimplePoint> retVal = new ArrayList<>();
		for (int i = 1; i < 1 + atomCount; i++) {
			retVal.add(fromCtabLine(lines[i]));
		}
		return retVal;
	}

	/**
	 * @param ctab
	 *            The ctab string to get the points from
	 * @param propertyFunction
	 *            Function to get the point property from the Ctab atom line
	 * @return List of Atom points
	 */
	public static <T> List<AbstractPoint<T>> getPointsFromCTabString(
			String cTab, Function<String, T> propertyFunction) {
		final String[] lines = cTab.split("\n");
		final int atomCount = Integer.valueOf(lines[0].substring(0, 3).trim());
		final List<AbstractPoint<T>> retVal = new ArrayList<>();
		for (int i = 1; i < 1 + atomCount; i++) {
			retVal.add(fromCtabLine(lines[i], propertyFunction));
		}
		return retVal;
	}

	/**
	 * Function to get the element symbol from a Mol block (mol, sdf, ctab) atom
	 * line, in original or V3000 format
	 */
	public static Function<String, String> elementSymbolFromMolAtom =
			new Function<String, String>() {

		@Override
		public String apply(String t) {
			if (t.startsWith("M  V30 ")) {
				return t.split("\\s+")[3];
			} else {
				return t.substring(30, 33).trim();
			}
		}
	};

	/**
	 * Function to get the element symbol from a PDB atom line
	 */
	public static Function<String, String> elementSymbolFromPdbAtom =
			new Function<String, String>() {

		@Override
		public String apply(String t) {
			String r = t.substring(76, 78).trim();
			if (r.length() == 2) {
				r = new String(new char[] { r.charAt(0),
						r.charAt(1) < 'a' ? (char) (r.charAt(1) + ' ')
								: r.charAt(1) });
			}
			return r;
		}
	};

	/**
	 * Function to get element atomic weight from element symbol
	 */
	public static Function<String, Double> elementWeightFromSymbol =
			new Function<String, Double>() {

		@Override
		public Double apply(String t) {
			return Elements.getInstance().getMWt(t);
		}
	};

	/**
	 * Function to get element van der waals radius from symbol
	 */
	public static Function<String, Double> elementVdWFromSymbol =
			new Function<String, Double>() {

		@Override
		public Double apply(String t) {
			return Elements.getInstance().getVdwRadius(t);
		}
	};
}
