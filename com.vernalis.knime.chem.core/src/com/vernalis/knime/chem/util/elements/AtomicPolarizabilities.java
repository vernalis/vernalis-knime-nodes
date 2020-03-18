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
 * This partial list of Atomic Polarizabilities is complied from P.
 * Schwerdtfeger 'Atomic Static Dipole Polarizabilities' in "Atoms, Molecules,
 * and Clusters in Electric Fields: Theoretical Approaches to the Calculation of
 * Electric Polarizability" by George Maroulis (Imperial College Press, 2006),
 * pg 10-15.
 * 
 * https://books.google.co.uk/books?id=dQMm307PYvsC&dq=list+of+atom+polarizabilities
 * 
 * @author s.roughley
 * 
 */
public enum AtomicPolarizabilities {
	H(4.4997515), B(20.5), Br(21.9), C(11.0), Cl(14.7), F(3.76), I(35.1),
	N(7.43), O(6.01), P(24.7), Se(26.24), Si(36.7), S(19.6), Te(37);

	private double polarizability;

	private AtomicPolarizabilities(double polarizability) {
		this.polarizability = polarizability;
	}

	/**
	 * @return The atomic polarizability
	 */
	public double getPolarizability() {
		return polarizability;
	}

	/**
	 * @return the atomic polarizability relative to carbon
	 */
	public double getCarbonScaledPolarizability() {
		return polarizability / C.getPolarizability();
	}
}
