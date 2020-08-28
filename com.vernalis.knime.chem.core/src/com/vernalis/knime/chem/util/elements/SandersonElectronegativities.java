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
package com.vernalis.knime.chem.util.elements;

/**
 * This partial list of Sanderson Electronegativities is taken from
 * http://www.knowledgedoor
 * .com/2/elements_handbook/sanderson_electronegativity.html
 * 
 * @author s.roughley
 * 
 */
public enum SandersonElectronegativities {
	H(2.592),
	/**
	 * For Boron we are assuming Ox. State III
	 */
	B(2.275), Br(3.219), C(2.746), Cl(3.475), F(4.0), I(2.788), N(3.194), O(3.654), P(2.515), Se(3.014),
	/**
	 * For Silicon, assuming Ox. State IV
	 */
	Si(2.138), S(2.957), Te(2.618);

	private double electroneg;

	private SandersonElectronegativities(double electroneg) {
		this.electroneg = electroneg;
	}

	/**
	 * @return The Sanderson Electronegativity
	 */
	public double getElectronegativity() {
		return electroneg;
	}

	/**
	 * @return The C-scaled Sanderson Electronegativity
	 */
	public double getCarbonScaledElectronegativity() {
		return electroneg / C.getElectronegativity();
	}
}
