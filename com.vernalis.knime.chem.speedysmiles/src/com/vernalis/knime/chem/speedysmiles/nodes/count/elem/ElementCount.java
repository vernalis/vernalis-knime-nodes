/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.nodes.count.elem;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.vernalis.knime.chem.speedysmiles.helpers.CalculatedProperty;
import com.vernalis.knime.chem.speedysmiles.helpers.SmilesHelpers;

/**
 * Enum containing calculation of element count properties for the SpeedySMILES
 * Element Count node
 * 
 * @author S.Roughley
 *
 */
public enum ElementCount implements CalculatedProperty<Integer> {
	Carbon("C", new String[] { "Cl", "Ca", "Sc", "Cr", "Co", "Cu", "Tc", "Cd",
			"Cs", "Cn", "Ce", "Cm", "Cf" }),

	Nitrogen("N", new String[] { "Ne", "Na", "Mn", "Ni", "Zn", "Nb", "In", "Sn",
			"Rn", "Cn", "Nd", "No" }),

	Oxygen("O", new String[] { "Co", "Mo", "Os", "Po", "Uuo", "Ho", "No" });

	private String[] counterCases;
	private Pattern elemPatt, counterCasesPatt;

	private ElementCount(String elemSymbol, String[] countercases) {
		this.counterCases = countercases;
		elemPatt = Pattern.compile(
				"(?<!@)(" + elemSymbol + "|" + elemSymbol.toLowerCase() + ")");
		String counterCaseRegex = "(?<!@)("
				+ Arrays.stream(counterCases).collect(Collectors.joining("|"));
		String orgCounterCases = Arrays.stream(counterCases)
				.filter(x -> SmilesHelpers.organicSMILESSubset.contains(x))
				.map(x -> x.toLowerCase()).collect(Collectors.joining("|"));
		if (!orgCounterCases.isEmpty()) {
			counterCaseRegex += "|" + orgCounterCases;
		}
		counterCaseRegex += ")";
		counterCasesPatt = Pattern.compile(counterCaseRegex);
	}

	// private static String[] cCountercases = new String[] { "Cl", "Ca", "Sc",
	// "Cr", "Co", "Cu", "Tc",
	// "Cd", "Cs", "Cn", "Ce", "Cm", "Cf" };
	// private static String[] nCountercases = new String[] { "Ne", "Na", "Mn",
	// "Ni", "Zn", "Nb", "In",
	// "Sn", "Rn", "Cn", "Nd", "No" };
	// private static String[] oCountercases = new String[] { "Co", "Mo", "Os",
	// "Po", "Uuo", "Ho",
	// "No" };

	@Override
	public String displayName() {
		return name();
	}

	@Override
	public Integer calculate(String SMILES) {
		return SmilesHelpers.countElement(SMILES, elemPatt, counterCasesPatt);
	}

}
