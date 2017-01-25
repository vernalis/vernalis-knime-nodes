/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.speedysmiles.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.knime.chem.types.SmilesCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.AdapterValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.RowOutput;

import com.vernalis.knime.chem.speedysmiles.nodes.count.chiralcentres.ChiralCentreCount;

/**
 * Utility class containing methods for handling and performing calculations on
 * SMILES Cells and Strings
 * 
 * @author s.roughley
 * 
 */
public class SmilesHelpers {
	private SmilesHelpers() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * A list of the element symbols in the Daylight SMILES definition of
	 * 'organic SMILES atoms' which do not need to be enclosed in [] in their
	 * normal valence state
	 */
	public static ArrayList<String> organicSMILESSubset = new ArrayList<String>();

	static {
		organicSMILESSubset.add("B");
		organicSMILESSubset.add("C");
		organicSMILESSubset.add("N");
		organicSMILESSubset.add("O");
		organicSMILESSubset.add("P");
		organicSMILESSubset.add("S");
		organicSMILESSubset.add("F");
		organicSMILESSubset.add("Cl");
		organicSMILESSubset.add("Br");
		organicSMILESSubset.add("I");
	}

	/**
	 * A list of bond symbols in Daylight SMILES definition
	 */
	public static ArrayList<Character> bondSymbols = new ArrayList<Character>();
	static {
		bondSymbols.add('-');
		bondSymbols.add('=');
		bondSymbols.add(':');
		bondSymbols.add('#');
	}

	/**
	 * Calculate the Largest component by HAC. All components matching this
	 * value are returned in a {@link LinkedHashSet} of {@link DataCell}s
	 * 
	 * @param smiles
	 *            The SMILES string to handle
	 * @return The biggest component(s)
	 * @see #getLargestByHacComponents(String)
	 */
	public static Set<DataCell> getLargestDataCellByHacComponents(String smiles) {
		if (smiles == null || "".equals(smiles)) {
			throw new IllegalArgumentException("You must supply a non-empty, non-null String");
		}

		LinkedHashSet<DataCell> retVal = new LinkedHashSet<>();
		if (smiles.indexOf(".") < 0) {
			// only got 1 component, so this is the biggest
			retVal.add(SmilesCellFactory.createAdapterCell(smiles));
			return retVal;
		}

		// Now the multi-component case
		int maxNumHac = 0;
		String[] smiComps = smiles.split("\\.");
		for (String smi : smiComps) {
			int hacCount = countHAC(smi);
			if (hacCount > maxNumHac) {
				retVal.clear();
				retVal.add(SmilesCellFactory.createAdapterCell(smi));
				maxNumHac = hacCount;
			} else if (hacCount == maxNumHac) {
				retVal.add(SmilesCellFactory.createAdapterCell(smi));
			}
		}
		return retVal;
	}

	/**
	 * Return the longest string SMILES from a set.
	 * 
	 * @param smilesCells
	 *            A {@link Set} of {@link SmilesCell}s
	 * @return The {@link DataCell} containing the longest SMILES String by
	 *         string length
	 */
	public static DataCell getLongestSmilesStringCell(Set<DataCell> smilesCells) {
		DataCell retVal = null;
		int lngth = 0;
		for (DataCell sCell : smilesCells) {
			int length = ((SmilesValue) sCell).getSmilesValue().length();
			if (length > lngth) {
				lngth = length;
				retVal = sCell;
			}
		}
		return retVal;
	}

	/**
	 * Calculate the Largest component by HAC. All components matching this
	 * value are returned in a {@link HashSet} of SMILES Strings
	 * 
	 * @param smiles
	 *            The SMILES string to handle
	 * @return The biggest component(s)
	 * @see #getLargestDataCellByHacComponents(String)
	 */
	public static Set<String> getLargestByHacComponents(String smiles) {
		if (smiles == null || "".equals(smiles)) {
			throw new IllegalArgumentException("You must supply a non-empty, non-null String");
		}

		Set<String> retVal = new HashSet<String>();
		if (smiles.indexOf(".") < 0) {
			// only got 1 component, so this is the biggest
			retVal.add(smiles);
			return retVal;
		}

		// Now the multi-component case
		int maxNumHac = 0;
		String[] smiComps = smiles.split("\\.");
		for (String smi : smiComps) {
			int hacCount = countHAC(smi);
			if (hacCount > maxNumHac) {
				retVal.clear();
				retVal.add(smi);
				maxNumHac = hacCount;
			} else if (hacCount == maxNumHac) {
				retVal.add(smi);
			}
		}
		return retVal;
	}

	/**
	 * Simple method to count components in a SMILES String
	 * 
	 * @param smi
	 *            Non-null SMILES String
	 * @return the number of components
	 */
	public static int countComponents(String smi) {
		if (smi.isEmpty()) {
			return 0;
		}
		if (!smi.contains(".")) {
			return 1;
		}
		return smi.split("\\.").length;
	}

	/**
	 * @param smi
	 *            SMILES string
	 * @return Heavy atom count
	 */
	public static int countHAC(String smi) {
		int cnt = 0;
		// NB define length as l as otherwise smiles.length
		// is re-calculated on each iteration, apparently
		for (int i = 0, l = smi.length(); i < l; i++) {
			char x = smi.charAt(i);
			if (x == '.' || x == '>')
				// skip - but keep counting!
				continue;
			if (x == '[') {
				cnt++;
				// skip to ]
				while (smi.charAt(i) != ']')
					i++;
				continue;
			}

			// Deal with aromatic atoms without []
			// NB some of these are 'impossible' (i.e. halogens, which are
			// assumed valency 1 without []), or not allowed without [] at all
			// Only b, c, n, o, p and s are truly required, hence there
			// positioning at the start of the list
			if (x == 'c' || x == 'o' || x == 'n' || x == 's' || x == 'b' || x == 'p' || x == 'f'
					|| x == 'k' || x == 'v' || x == 'i' || x == 'y' || x == 'u') {
				cnt++;
				continue;
			}

			// Deal with other atoms
			if (Character.isUpperCase(x))
				cnt++;
		}
		// Now correct for explicit [H] - NB this will not catch isotopes,
		// charged H etc
		// TODO: Deal with missed cases
		cnt -= (smi.indexOf("[H]") >= 0) ? smi.split("\\[H\\]").length - 1 : 0;
		return cnt;
	}

	/**
	 * Function to do the actual counting by String splitting
	 * 
	 * @param smi
	 *            The test SMILES string
	 * @param elementSymbol
	 *            The element Symbol to count (in it's normal case form, e.g.
	 *            Cl, Br etc
	 * @param counterCases
	 *            The counter-cases, i.e. those other element symbols containing
	 *            the same symbol
	 * @return The count
	 * @deprecated {@link #countElement(String, Pattern, Pattern)} is preferred
	 *             due to singleton Pattern compilation
	 */
	@Deprecated
	public static int countElement(String smi, String elementSymbol, String[] counterCases) {
		// TODO: Rewrite using Pattern.split, with Pattern as part of callig
		// Enum
		if (!smi.contains(elementSymbol) && !smi.contains(elementSymbol.toLowerCase())) {
			return 0;
		}
		// -1 forces end of string matches
		int cnt = smi.split("(?<!@)(" + elementSymbol + "|" + elementSymbol.toLowerCase() + ")",
				-1).length - 1;

		String counterCaseRegex = "(?<!@)("
				+ Arrays.stream(counterCases).collect(Collectors.joining("|"));
		String orgCounterCases = Arrays.stream(counterCases)
				.filter(x -> organicSMILESSubset.contains(x)).map(x -> x.toLowerCase())
				.collect(Collectors.joining("|"));
		if (!orgCounterCases.isEmpty()) {
			counterCaseRegex += "|" + orgCounterCases;
		}
		counterCaseRegex += ")";
		cnt -= smi.split(counterCaseRegex, -1).length - 1;

		return cnt;
	}

	/**
	 * Function to do the actual counting by String splitting
	 * 
	 * @param smi
	 *            The test SMILES string
	 * @param elemPatt
	 *            The element Symbol to count including lower-case aromatic form
	 *            if appropriate
	 * @param counterCasesPatt
	 *            The counter-cases, i.e. those other element symbols containing
	 *            the same symbol
	 * @return The count
	 */
	public static Integer countElement(String SMILES, Pattern elemPatt, Pattern counterCasesPatt) {
		if (!elemPatt.matcher(SMILES).find()) {
			return 0;
		}
		// -1 forces end of string matches
		// -1 -(-1) for lengths cancel out
		return elemPatt.split(SMILES, -1).length - counterCasesPatt.split(SMILES, -1).length;

	}

	/**
	 * Checks for un-closed bond indexes
	 * 
	 * @param smi
	 *            SMILES String
	 * @return
	 */
	public static boolean hasUnclosedBonds(String smi) {
		ArrayList<Integer> bondIds = new ArrayList<Integer>();
		for (int i = 0, l = smi.length(); i < l; i++) {
			char x = smi.charAt(i);
			if (x == '[') {
				// skip to ]
				while ((x = smi.charAt(i)) != ']') {
					i++;
				}
			}
			// Only count if immediately after an organicSMILESSubset member or
			// ] or bond symbol (bond symbol added to handle e.g S(=O)=1)
			if (x == ']' || bondSymbols.contains(x)
					|| organicSMILESSubset.contains(String.valueOf(Character.toUpperCase(x)))
					|| ((i + 1) < l && organicSMILESSubset.contains(new String(
							new char[] { Character.toUpperCase(x), smi.charAt(i + 1) })))) {

				// Possible starting place to look
				if ((i + 1) < l && organicSMILESSubset.contains(
						new String(new char[] { Character.toUpperCase(x), smi.charAt(i + 1) }))) {
					// Skip the second digit if a t character match found
					i++;
				}

				while (++i < l && (Character.isDigit(x = smi.charAt(i)) || x == '%' || x == '\\'
						|| x == '/')) {
					// possible intermediate characteres - double bond stereo
					// and bond types, and % = double-digit index marker
					if (Character.isDigit(x)) {
						// If a digit, then each digit is a separate index
						int id = Character.getNumericValue(x);
						if (bondIds.contains(id)) {
							bondIds.remove(new Integer(id));
						} else {
							bondIds.add(id);
						}
					} else if (x == '%') {
						// The next 2 digits only are an index in the Daylight
						// Specification
						// Allow for only 1 being a digit, but dont look for 3
						// or more
						int id = 0;
						int j = 0;
						while (++i < l && j++ < 2 && Character.isDigit(x = smi.charAt(i))) {
							id *= 10;
							id += Character.getNumericValue(x);
						}
						i--;
						if (bondIds.contains(id)) {
							bondIds.remove(new Integer(id));
						} else {
							bondIds.add(id);
						}
					}
					// Otherwise we have a bond type or stereomarker, which we
					// just skip
				}
				// drop i by 1 to start loop again with true next character
				i--;
			}
		}
		return !(bondIds.size() == 0);
	}

	/**
	 * Count the number of rings (as defined by the number of ring closure
	 * bonds) in a structure
	 * 
	 * @param smi
	 *            The SMILES string
	 * @return The number of ring closure bonds, or <code>null</code> if there
	 *         is an unclosed bond
	 */
	public static Integer countRings(String smi) {
		ArrayList<Integer> bondIds = new ArrayList<Integer>();
		int count = 0;
		for (int i = 0, l = smi.length(); i < l; i++) {
			char x = smi.charAt(i);
			if (x == '[') {
				// skip to ]
				while ((x = smi.charAt(i)) != ']') {
					i++;
				}
			}
			// Only count if immediately after an organicSMILESSubset member or
			// ] or bond symbol (bond symbol added to handle e.g S(=O)=1)
			if (x == ']' || bondSymbols.contains(x)
					|| organicSMILESSubset.contains(String.valueOf(Character.toUpperCase(x)))
					|| ((i + 1) < l && organicSMILESSubset.contains(new String(
							new char[] { Character.toUpperCase(x), smi.charAt(i + 1) })))) {

				// Possible starting place to look
				if ((i + 1) < l && organicSMILESSubset.contains(
						new String(new char[] { Character.toUpperCase(x), smi.charAt(i + 1) }))) {
					// Skip the second digit if a t character match found
					i++;
				}

				while (++i < l && (Character.isDigit(x = smi.charAt(i)) || x == '%' || x == '\\'
						|| x == '/')) {
					// possible intermediate characteres - double bond stereo
					// and bond types, and % = double-digit index marker
					if (Character.isDigit(x)) {
						// If a digit, then each digit is a separate index
						int id = Character.getNumericValue(x);
						if (bondIds.contains(id)) {
							bondIds.remove(new Integer(id));
							count++;
						} else {
							bondIds.add(id);
						}
					} else if (x == '%') {
						// The next 2 digits only are an index in the Daylight
						// Specification
						// Allow for only 1 being a digit, but dont look for 3
						// or more
						int id = 0;
						int j = 0;
						while (++i < l && j++ < 2 && Character.isDigit(x = smi.charAt(i))) {
							id *= 10;
							id += Character.getNumericValue(x);
						}
						i--;
						if (bondIds.contains(id)) {
							bondIds.remove(new Integer(id));
							count++;
						} else {
							bondIds.add(id);
						}
					}
					// Otherwise we have a bond type or stereomarker, which we
					// just skip
				}
				// drop i by 1 to start loop again with true next character
				i--;
			}
		}
		if (bondIds.size() > 0) {
			return null;
		}
		return count;
	}

	/**
	 * Check for a defined stereocentre
	 * 
	 * @param smi
	 *            SMILES String
	 * @return <code>true</code> if a defined stereocentre exists
	 */
	public static boolean hasDefinedStereoCentres(String smi) {
		// return (smi.indexOf("@") >= 0);
		// Following is ~10% faster
		// for (int i = 0, l = smi.length(); i < l; i++) {
		// char x = smi.charAt(i);
		// if (x == '@') {
		// return true;
		// }
		// }
		// return false;
		// And following faster still
		return smi.contains("@");
	}

	/**
	 * Check for multicomponent SMILES
	 * 
	 * @param smi
	 *            SMILES String
	 * @return <code>true</code> if molecule is multi-component
	 */
	public static boolean isMultiComponent(String smi) {
		// return (smi.indexOf(".") >= 0);
		return smi.contains(".");
	}

	/**
	 * Regex to match higher stereocentres, ie those with indices which can go
	 * beyond 2
	 */
	private static final Pattern HIGHER_STEREOCENTRE_PATTERN = Pattern
			.compile("@((?:SP|TB|OH)\\d*)");

	/**
	 * Invert tetrahedral stereochemistry. NB @TB1 and @OH1 will be converted
	 * to @TH2 and @OH2 and vice versa, if they are listed only as @ and @@.
	 * 
	 * @param smi
	 *            SMILES String
	 * @return Invertes stereochemistry SMILES String
	 */
	public static String invertStereoCentres(String smi) {
		// Need to handle @/@@ and @TH1/@TH2. Use '&' as exchange char in place
		// of '@'
		// The order below takes TH2/@@ to 'TH3/@@@' (non-existant group!), then
		// takes TH1/@ to TH2 (NB temporary use of &TH2 to protect in following
		// swap!), then restores TH3/@@@ to TH1/@

		String retVal = HIGHER_STEREOCENTRE_PATTERN.matcher(smi).replaceAll("&$1");

		// Now convert TH and allenyl 2 to 3
		retVal = retVal.replace("@@", "&&&").replaceAll("@(TH|AL)2", "&$13");

		// And TH/Allenyl 1 to 2
		retVal = retVal.replaceAll("@(TH|AL)1", "&$12").replace("@", "@@");

		// Now convert 3 - 1
		retVal = retVal.replaceAll("&(TH|AL)3", "&$11").replace("&&&", "@");

		// Finally, 'global deprotection'
		retVal = retVal.replace("&", "@");

		return retVal;

	}

	/**
	 * Regex to match any stereocentre
	 */
	private static final Pattern STEREOCENTRE_PATTERN = Pattern.compile("[@]+(TH|AL|SP|TB|OH)?");

	/**
	 * @param smi
	 *            the SMILES String
	 * @return The number of possible stereoisomers
	 */
	public static double countPossibleStereoIsomers(String smi) {
		Matcher m = STEREOCENTRE_PATTERN.matcher(smi);
		Double retVal = 1.0;
		while (m.find() && Double.isFinite(retVal)) {
			FlagProvider flag = m.group(1) == null ? ChiralCentreCount.UNLABELLED
					: ChiralCentreCount.valueOf(m.group(1));
			int numIsomers = flag.max();
			retVal *= (double) numIsomers;
		}
		return retVal;
	}

	/**
	 * Regex to match any labelled stereocentre (i.e on which is not simply @
	 * or @@)
	 */
	private static final Pattern ALL_LABELLED_STEREOCENTRE_PATTERN = Pattern
			.compile("@(TH|AL|SP|TB|OH)[\\d]+");
	/**
	 * Regex to match any non-labelled stereocentre, i.e. @ or @@ (or higher)
	 */
	private static final Pattern POLY_AT_STEREOCENTRE_PATTERN = Pattern.compile("@+");

	/**
	 * Method to enumerate isomers of simple (@/@@) and complex (@xxnn, where xx
	 * is a 2-charcter flag and nn is a 1-2 digit number) and add to table
	 * directly to avoid memory issues. The method uses a long as a flag set to
	 * process upto 63 centres at a time, with more complex structures being
	 * processed recursively
	 * 
	 * @param SMILES
	 *            The SMILES String
	 * @param row
	 *            The datarow. The enumerated isomers are added to the end of
	 *            the row
	 * @param rowOutput
	 *            The data container to which rows are added
	 * @param exec
	 *            The execution context to allow the user to cancel
	 * @throws CanceledExecutionException
	 *             if the user cancels
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void enumerateLabelledStereoisomers(String SMILES, DataRow row,
			RowOutput rowOutput, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException, ExecutionException {
		if (SMILES == null) {
			// No SMILES - missing cell1
			rowOutput.push(new AppendedColumnRow(row, DataType.getMissingCell()));
			return;
		}
		if (!SMILES.contains("@")) {
			// No chiral centres defined - just return the molecule
			rowOutput.push(new AppendedColumnRow(row, SmilesCellFactory.createAdapterCell(SMILES)));
			return;
		}

		String maskedSmi = POLY_AT_STEREOCENTRE_PATTERN
				.matcher(ALL_LABELLED_STEREOCENTRE_PATTERN.matcher(SMILES).replaceAll("&$1£"))
				.replaceAll("&£");
		doLabelledIsomerEnumeration(maskedSmi, row, rowOutput, exec);
	}

	/**
	 * Regex to match any labelled stereocentre which has been masked for
	 * enumeration
	 */
	private static final Pattern MASKED_LABELLED_STEREOCENTRE_PATTERN = Pattern
			.compile("&(TH|AL|SP|TB|OH)?£");

	/**
	 * Regex to match any non-labelled stereocentre which has been masked for
	 * enumeration
	 */
	private static final Pattern MASKED_UNLABELLED_STEREOCENTRE_PATTERN = Pattern.compile("&£");

	/**
	 * Private method called potentially recursively with a masked smiles string
	 * by
	 * {@link #enumerateSimpleStereoisomers(String, DataRow, RowOutput, ExecutionContext)}
	 * to perform the enumeration
	 * 
	 * @param maskedSmi
	 * @param row
	 * @param rowOutput
	 * @param exec
	 * @throws InterruptedException
	 * @throws CanceledExecutionException
	 */
	private static void doLabelledIsomerEnumeration(String maskedSmi, DataRow row,
			RowOutput rowOutput, ExecutionContext exec)
			throws InterruptedException, CanceledExecutionException {
		Matcher m = MASKED_LABELLED_STEREOCENTRE_PATTERN.matcher(maskedSmi);
		List<FlagProvider> flags = new ArrayList<>();
		List<Integer> offsets = new ArrayList<>();
		int offSet = 0;
		while (m.find()) {
			FlagProvider flag = m.group(1) == null ? ChiralCentreCount.UNLABELLED
					: ChiralCentreCount.valueOf(m.group(1));
			if (offSet + flag.bits() < 64) {
				flags.add(flag);
				offsets.add(offSet);
			} else {
				break;
			}
			offSet += flag.bits();
		}

		long max = (long) Math.pow(2, offSet);
		long subRowIdx = 0;
		flagset: for (long x = 0; x < max; x++) {
			// Iterate through the combinations
			String newSmi = new String(maskedSmi);
			for (int y = 0; y < flags.size(); y++) {
				FlagProvider flag = flags.get(y);
				int offset = offsets.get(y);
				int index = (int) ((x >> offset) & flag.mask());
				// Now work through the centres
				if (flag == ChiralCentreCount.UNLABELLED) {
					newSmi = MASKED_UNLABELLED_STEREOCENTRE_PATTERN.matcher(newSmi)
							.replaceFirst((index > 0) ? "@@" : "@");
				} else if (index++ < flag.max()) {
					newSmi = MASKED_LABELLED_STEREOCENTRE_PATTERN.matcher(newSmi)
							.replaceFirst("@$1" + index);
				} else {
					// Nonsense value of x - can we precheck
					continue flagset;
				}
			}

			// Check all are now enumerated

			if (!MASKED_LABELLED_STEREOCENTRE_PATTERN.matcher(newSmi).find()) {
				// They are - add the row to the ouput
				rowOutput.push(new AppendedColumnRow(new RowKey(row.getKey() + "_" + (subRowIdx++)),
						row, SmilesCellFactory.createAdapterCell(newSmi)));
			} else {
				// Call recursively
				doLabelledIsomerEnumeration(newSmi,
						new DefaultRow(new RowKey(row.getKey() + "_" + (subRowIdx++)), row),
						rowOutput, exec);
			}
			exec.checkCanceled();
		}

	}

	/**
	 * Convert a SMILES String with possible multiple components into a SMILES
	 * Cell with each unique (String Uniqueness, not canonicalised) component
	 * represented once only
	 * 
	 * @param smi
	 *            SMILES String
	 * @return {@link SmilesCell} of unique components
	 * 
	 */
	public static DataCell getUniqueComponentsSmilesCell(String smi) {
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		for (String smi2 : getUniqueComponents(smi)) {
			if (!isFirst) {
				sb.append(".");
			} else {
				isFirst = false;
			}
			sb.append(smi2);
		}
		return SmilesCellFactory.create(sb.toString());
	}

	/**
	 * Convert a SMILES String with possible multiple components into a
	 * {@link SetCell} of SMILES Cells with each unique (String Uniqueness, not
	 * canonicalised) component represented once only
	 * 
	 * @param smi
	 *            SMILES String
	 * @return SetCell of Unique components
	 */
	public static DataCell getUniqueComponentsSmilesSetCell(String smi) {
		HashSet<SmilesCell> retVal = new HashSet<SmilesCell>();
		for (String smi2 : getUniqueComponents(smi)) {
			retVal.add((SmilesCell) SmilesCellFactory.create(smi2));
		}
		return CollectionCellFactory.createSetCell(retVal);
	}

	/**
	 * Convert a SMILES String to a {@link HashSet} of unique SMILES components
	 * 
	 * @param smi
	 *            SMILES String
	 * @return {@link HashSet} os component SMILES
	 */
	private static Set<String> getUniqueComponents(String smi) {
		HashSet<String> retVal = new HashSet<String>();
		if (!smi.contains(".")) {
			retVal.add(smi);
			return retVal;
		}

		retVal.addAll(Arrays.asList(smi.split("\\.")));
		return retVal;
	}

	/**
	 * Convenienve method to get a SMILES String from a datacell or adaptor
	 * cell.
	 * 
	 * @param dataCell
	 *            The incoming data cell
	 * @return The SMILES String or <code>null</code> if the cell was a missing
	 *         value
	 */
	public static String getSmilesFromCell(DataCell dataCell) {
		if (dataCell.isMissing()) {
			return null;
		}
		String smi;
		if (dataCell.getType().isAdaptable(SmilesValue.class)) {
			smi = ((AdapterValue) dataCell).getAdapter(SmilesValue.class).getSmilesValue();
		} else {
			smi = ((SmilesValue) dataCell).getSmilesValue();
		}
		return smi;
	}

	/**
	 * Convenience method to check a column selection, or guess one if none is
	 * already made. The selection must be a Smiles Cell, or adaptable to a
	 * Smiles Cell
	 * 
	 * @param inSpec
	 *            The dataTable Spec for the incoming table
	 * @param colNameMdl
	 *            The SettingsModel for the column name
	 * @return <code>null</code>, or information about any automatic column
	 *         selection
	 * @throws InvalidSettingsException
	 *             If not suitable column is found, or the selected column is
	 *             not appropriate
	 */
	public static String findSmilesColumn(final DataTableSpec inSpec,
			final SettingsModelString colNameMdl) throws InvalidSettingsException {
		DataColumnSpec colSpec = inSpec.getColumnSpec(colNameMdl.getStringValue());

		String retVal = null;
		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpec.getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				DataType colType = inSpec.getColumnSpec(i).getType();
				if (colType.isCompatible(SmilesValue.class)
						|| colType.isAdaptable(SmilesValue.class)) {
					colNameMdl.setStringValue(inSpec.getColumnSpec(i).getName());
					retVal = "No column selected. " + colNameMdl.getStringValue()
							+ " auto-selected.";
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					throw new InvalidSettingsException("No SMILES column was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(SmilesValue.class)) {
				// The column is not compatible with one of the accepted types
				throw new InvalidSettingsException("The column " + colNameMdl.getStringValue()
						+ " is not in the SMILES format");
			}
		}
		return retVal;
	}
}
