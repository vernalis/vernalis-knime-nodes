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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Singleton Look-up for Element properties.
 * </p>
 * <p>
 * Element weights are from
 * <a href="http://introcs.cs.princeton.edu/java/44st/elements.csv"
 * >http://introcs.cs.princeton.edu/java/44st/elements.csv</a>
 * </p>
 * <p>
 * Van der Waals Radii are from "A cartography of van der Waals territories" (s.
 * Alvarez, Dalton Trans., 2013, 42, 8617-8636; DOI:
 * <a href="http://dx.doi.org/10.1039/C3DT50599E">10.1039/C2DT50599E</a>, and
 * are added to the file elements.csv.
 * 
 * @author s.roughley
 * 
 */
public final class Elements {

	private final Map<String, Double> MWts;
	private final Map<String, Double> vdwRadii;
	private double maxVdW;
	private final Map<Integer, String> symbols;

	/**
	 * Hidden constructor
	 */
	private Elements() {
		MWts = new HashMap<>();
		vdwRadii = new HashMap<>();
		maxVdW = 0.0;
		symbols = new HashMap<>();
		readElements();
	}

	/**
	 * Holding class idiom provides automatic lazy instantiation and
	 * synchronization See Joshua Bloch Effective Java Item 71
	 */
	private static final class HoldingClass {

		private static Elements INSTANCE = new Elements();
	}

	/**
	 * @return the singleton instance
	 */
	public static Elements getInstance() {
		return HoldingClass.INSTANCE;
	}

	/**
	 * @param elementSymbol
	 *            The element symbol
	 * @return The van der Waals Radius, or null if none available
	 */
	public double getVdwRadius(String elementSymbol) {
		return vdwRadii.get(elementSymbol);
	}

	/**
	 * @return The maximum van der Waals radius loaded into the list
	 */
	public double getMaxVdW() {
		return maxVdW;
	}

	/**
	 * @param elementSymbol
	 *            The element symbol
	 * @return The Molecular weight of the element
	 */
	public Double getMWt(String elementSymbol) {
		return MWts.get(elementSymbol);
	}

	/**
	 * @param atomicNumber
	 *            The atomic number
	 * @return The element symbol
	 */
	public String getElementSymbol(int atomicNumber) {
		return symbols.get(atomicNumber);
	}

	private void readElements() {
		String line = "";
		BufferedReader br = null;
		try {
			// First find the elements.csv file
			File f = new File("elements.csv");
			if (f.exists()) {
				br = new BufferedReader(new FileReader(f));
			} else {
				URL url = this.getClass().getResource("elements.csv");
				InputStreamReader isr = new InputStreamReader(
						url.openConnection().getInputStream());
				br = new BufferedReader(isr);
			}

			// Now read it into the Map
			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty() || line.startsWith("#")
						|| line.startsWith("//")) {
					// blank or comment line
					continue;
				}
				String[] fields = line.split(",");
				MWts.put(fields[2], Double.parseDouble(fields[3].trim()));
				symbols.put(Integer.parseInt(fields[1].trim()),
						fields[2].trim());
				try {
					vdwRadii.put(fields[2],
							Double.parseDouble(fields[4].trim()));
					maxVdW = Math.max(maxVdW, vdwRadii.get(fields[2]));
				} catch (NumberFormatException
						| ArrayIndexOutOfBoundsException e) {
					// Do nothing - there is no known value for this element
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
