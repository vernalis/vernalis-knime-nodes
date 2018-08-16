/*******************************************************************************
 * Copyright (c) 2016, 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.speedysmiles.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
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
	public static final ArrayList<String> organicSMILESSubset =
			new ArrayList<>();

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
	public static final ArrayList<Character> bondSymbols = new ArrayList<>();
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
	public static Set<DataCell> getLargestDataCellByHacComponents(
			String smiles) {
		LinkedHashSet<DataCell> retVal = new LinkedHashSet<>();
		if (smiles == null || "".equals(smiles)) {
			return retVal;
		}
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
	public static DataCell getLongestSmilesStringCell(
			Set<DataCell> smilesCells) {
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
	 * @deprecated
	 */
	@Deprecated
	public static Set<String> getLargestByHacComponents(String smiles) {
		if (smiles == null || "".equals(smiles)) {
			throw new IllegalArgumentException(
					"You must supply a non-empty, non-null String");
		}

		Set<String> retVal = new HashSet<>();
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
	 * Regex to match hydrogens defined explicitly as their own atoms, e.g. in
	 * C([H])([H])[H]
	 */
	static final Pattern H_PATTERN = Pattern.compile("\\[\\d*H\\+?\\-?\\]");
	/**
	 * Regex to match attachment points within []
	 */
	static final Pattern AP_PATTERN = Pattern.compile("\\[\\d*\\*");

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
			if (x == 'c' || x == 'o' || x == 'n' || x == 's' || x == 'b'
					|| x == 'p' || x == 'f' || x == 'k' || x == 'v' || x == 'i'
					|| x == 'y' || x == 'u') {
				cnt++;
				continue;
			}

			// Deal with other atoms
			if (Character.isUpperCase(x))
				cnt++;
		}
		// Now correct for explicit [H]; -ve index in split handles SMILES
		// ending with [H]
		cnt -= H_PATTERN.split(smi, -1).length - 1;

		// And for attachment points ('*' outside of [] will not have been
		// counted, so we only need the [*] cases)
		cnt -= AP_PATTERN.split(smi, -1).length - 1;
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
	public static int countElement(String smi, String elementSymbol,
			String[] counterCases) {
		// TODO: Rewrite using Pattern.split, with Pattern as part of callig
		// Enum
		if (!smi.contains(elementSymbol)
				&& !smi.contains(elementSymbol.toLowerCase())) {
			return 0;
		}
		// -1 forces end of string matches
		int cnt = smi.split("(?<!@)(" + elementSymbol + "|"
				+ elementSymbol.toLowerCase() + ")", -1).length - 1;

		String counterCaseRegex = "(?<!@)("
				+ Arrays.stream(counterCases).collect(Collectors.joining("|"));
		String orgCounterCases = Arrays.stream(counterCases)
				.filter(x -> organicSMILESSubset.contains(x))
				.map(x -> x.toLowerCase()).collect(Collectors.joining("|"));
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
	public static Integer countElement(String SMILES, Pattern elemPatt,
			Pattern counterCasesPatt) {
		if (!elemPatt.matcher(SMILES).find()) {
			return 0;
		}
		// -1 forces end of string matches
		// -1 -(-1) for lengths cancel out
		return elemPatt.split(SMILES, -1).length
				- counterCasesPatt.split(SMILES, -1).length;

	}

	/**
	 * Checks for un-closed bond indexes
	 * 
	 * @param smi
	 *            SMILES String
	 * @return
	 */
	public static boolean hasUnclosedBonds(String smi) {
		ArrayList<Integer> bondIds = new ArrayList<>();
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
					|| organicSMILESSubset
							.contains(String.valueOf(Character.toUpperCase(x)))
					|| ((i + 1) < l && organicSMILESSubset.contains(
							new String(new char[] { Character.toUpperCase(x),
									smi.charAt(i + 1) })))) {

				// Possible starting place to look
				if ((i + 1) < l && organicSMILESSubset.contains(
						new String(new char[] { Character.toUpperCase(x),
								smi.charAt(i + 1) }))) {
					// Skip the second digit if a t character match found
					i++;
				}

				while (++i < l && (Character.isDigit(x = smi.charAt(i))
						|| x == '%' || x == '\\' || x == '/')) {
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
						while (++i < l && j++ < 2
								&& Character.isDigit(x = smi.charAt(i))) {
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
		ArrayList<Integer> bondIds = new ArrayList<>();
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
					|| organicSMILESSubset
							.contains(String.valueOf(Character.toUpperCase(x)))
					|| ((i + 1) < l && organicSMILESSubset.contains(
							new String(new char[] { Character.toUpperCase(x),
									smi.charAt(i + 1) })))) {

				// Possible starting place to look
				if ((i + 1) < l && organicSMILESSubset.contains(
						new String(new char[] { Character.toUpperCase(x),
								smi.charAt(i + 1) }))) {
					// Skip the second digit if a t character match found
					i++;
				}

				while (++i < l && (Character.isDigit(x = smi.charAt(i))
						|| x == '%' || x == '\\' || x == '/')) {
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
						while (++i < l && j++ < 2
								&& Character.isDigit(x = smi.charAt(i))) {
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
	 * Check for a defined double bond geometry
	 * 
	 * @param smi
	 *            SMILES String
	 * @return {@code true} if a defined geometry double bond exists
	 */
	public static boolean hasDefinedDoubleBondGeometry(String smi) {
		return smi.contains("\\") || smi.contains("/");
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
	private static final Pattern HIGHER_STEREOCENTRE_PATTERN =
			Pattern.compile("@((?:SP|TB|OH)\\d*)");

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

		String retVal =
				HIGHER_STEREOCENTRE_PATTERN.matcher(smi).replaceAll("&$1");

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
	private static final Pattern STEREOCENTRE_PATTERN =
			Pattern.compile("[@]+(TH|AL|SP|TB|OH)?");

	/**
	 * @param smi
	 *            the SMILES String
	 * @return The number of possible stereoisomers
	 */
	public static double countPossibleStereoIsomers(String smi) {
		Matcher m = STEREOCENTRE_PATTERN.matcher(smi);
		Double retVal = 1.0;
		while (m.find() && Double.isFinite(retVal)) {
			FlagProvider flag =
					m.group(1) == null ? ChiralCentreCount.UNLABELLED
							: ChiralCentreCount.valueOf(m.group(1));
			int numIsomers = flag.max();
			retVal *= (double) numIsomers;
		}
		return retVal;
	}

	/**
	 * Regex to match any sterecentre designation including simple @ / @@ forms
	 * along with @TH @OH etc with subsquent IDs
	 */
	private static Pattern ALL_STEREOCENTRE_INCL_LABEL_PATTERN =
			Pattern.compile("@+(?:(?:TH|AL|SP|TB|OH)\\d*)?");

	/**
	 * @param smi
	 *            the SMILES String
	 * @return The SMILES String with all stereocentre labels removed
	 */
	public static String stripStereoCentres(String smi) {
		return ALL_STEREOCENTRE_INCL_LABEL_PATTERN.matcher(smi).replaceAll("");

	}

	/**
	 * @param smi
	 *            the SMILES String
	 * @return The SMILES String with all double bonde geometry labels removed
	 */
	public static String stripDoubleBondGeometry(String smi) {
		return smi.replace("\\", "-").replace("/", "-");
	}

	/**
	 * Regex to match any labelled stereocentre (i.e on which is not simply @
	 * or @@)
	 */
	private static final Pattern ALL_LABELLED_STEREOCENTRE_PATTERN =
			Pattern.compile("@(TH|AL|SP|TB|OH)[\\d]+");
	/**
	 * Regex to match any non-labelled stereocentre, i.e. @ or @@ (or higher)
	 */
	private static final Pattern POLY_AT_STEREOCENTRE_PATTERN =
			Pattern.compile("@+");

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
	public static void enumerateLabelledStereoisomers(String SMILES,
			DataRow row, RowOutput rowOutput, ExecutionContext exec)
			throws CanceledExecutionException, InterruptedException,
			ExecutionException {
		if (SMILES == null) {
			// No SMILES - missing cell1
			rowOutput.push(
					new AppendedColumnRow(row, DataType.getMissingCell()));
			return;
		}
		if (!SMILES.contains("@")) {
			// No chiral centres defined - just return the molecule
			rowOutput.push(new AppendedColumnRow(row,
					SmilesCellFactory.createAdapterCell(SMILES)));
			return;
		}

		String maskedSmi =
				POLY_AT_STEREOCENTRE_PATTERN
						.matcher(ALL_LABELLED_STEREOCENTRE_PATTERN
								.matcher(SMILES).replaceAll("&$1£"))
						.replaceAll("&£");
		doLabelledIsomerEnumeration(maskedSmi, row, rowOutput, exec);
	}

	/**
	 * Regex to match any labelled stereocentre which has been masked for
	 * enumeration
	 */
	private static final Pattern MASKED_LABELLED_STEREOCENTRE_PATTERN =
			Pattern.compile("&(TH|AL|SP|TB|OH)?£");

	/**
	 * Regex to match any non-labelled stereocentre which has been masked for
	 * enumeration
	 */
	private static final Pattern MASKED_UNLABELLED_STEREOCENTRE_PATTERN =
			Pattern.compile("&£");

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
	private static void doLabelledIsomerEnumeration(String maskedSmi,
			DataRow row, RowOutput rowOutput, ExecutionContext exec)
			throws InterruptedException, CanceledExecutionException {
		Matcher m = MASKED_LABELLED_STEREOCENTRE_PATTERN.matcher(maskedSmi);
		List<FlagProvider> flags = new ArrayList<>();
		List<Integer> offsets = new ArrayList<>();
		int offSet = 0;
		while (m.find()) {
			FlagProvider flag =
					m.group(1) == null ? ChiralCentreCount.UNLABELLED
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
					newSmi = MASKED_UNLABELLED_STEREOCENTRE_PATTERN
							.matcher(newSmi)
							.replaceFirst((index > 0) ? "@@" : "@");
				} else if (index++ < flag.max()) {
					newSmi = MASKED_LABELLED_STEREOCENTRE_PATTERN
							.matcher(newSmi).replaceFirst("@$1" + index);
				} else {
					// Nonsense value of x - can we precheck
					continue flagset;
				}
			}

			// Check all are now enumerated

			if (!MASKED_LABELLED_STEREOCENTRE_PATTERN.matcher(newSmi).find()) {
				// They are - add the row to the ouput
				rowOutput.push(new AppendedColumnRow(
						new RowKey(row.getKey() + "_" + (subRowIdx++)), row,
						SmilesCellFactory.createAdapterCell(newSmi)));
			} else {
				// Call recursively
				doLabelledIsomerEnumeration(newSmi,
						new DefaultRow(
								new RowKey(row.getKey() + "_" + (subRowIdx++)),
								row),
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
		HashSet<SmilesCell> retVal = new HashSet<>();
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
		HashSet<String> retVal = new HashSet<>();
		if (!smi.contains(".")) {
			retVal.add(smi);
			return retVal;
		}

		retVal.addAll(Arrays.asList(smi.split("\\.")));
		return retVal;
	}

	/**
	 * Convenience method to get a SMILES String from a datacell or adaptor
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
			smi = ((AdapterValue) dataCell).getAdapter(SmilesValue.class)
					.getSmilesValue();
		} else {
			smi = ((SmilesValue) dataCell).getSmilesValue();
		}
		return smi;
	}

	/**
	 * @param smiles
	 *            The incoming SMILES String
	 * @return <code>true</code> if the SMILES contains one or more dummy atoms
	 */
	public static boolean hasDummyAtom(String smiles) {
		return smiles.contains("*");
	}

	/**
	 * @param smiles
	 *            The incoming SMILES String
	 * @return The count of dummy atoms in the SMILES String
	 */
	public static int countDummyAtoms(String smiles) {
		return smiles.split("\\*", -1).length - 1;

	}

	/**
	 * Regex to match a dummy atom {@code [...*...]}
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>{@code ((?<!\\()[\\\\/])?} Deals with matches where stereochemistry
	 * is designated to the * atom</li>
	 * <li>{@code [:\\-=#]?} deals with a possible explicit bond to the dummy
	 * atom</li>
	 * </ul>
	 * </p>
	 */

	private static final Pattern DUMMY_ATOM_ISOTOPE_UNLABELLED =
			Pattern.compile("\\[(\\d+)\\*([^:\\[\\]])*\\]");

	public static String dummyAtomIsotopeToAtomLabel(String smiles) {
		return DUMMY_ATOM_ISOTOPE_UNLABELLED.matcher(smiles)
				.replaceAll("[*$2:$1]");
	}

	private static final Pattern DUMMY_ATOM_NOISOTOPE_NUMERICLABELLED =
			Pattern.compile("\\[\\*([^:\\[\\]])*:(\\d*)\\]");

	public static String dummyAtomAtomLabelToIsotope(String smiles) {
		return DUMMY_ATOM_NOISOTOPE_NUMERICLABELLED.matcher(smiles)
				.replaceAll("[$2*$1]");
	}

	private static final Pattern DUMMY_ATOM_PATTERN =
			Pattern.compile("\\[(\\d*)\\*([^\\[\\]]*)\\]");

	public static String dummyAtomToElementSymbol(String smiles, String repl) {
		return DUMMY_ATOM_PATTERN.matcher(smiles)
				.replaceAll("[$1" + repl + "$2]")
				.replace("*", organicSMILESSubset.contains(repl) ? repl
						: "[" + repl + "]");
	}

	private static final Pattern H_ISOTOPE_PATTERN = Pattern
			.compile("\\[0*(2|3)H((?:@.*)?(?:H\\d*)?(?:(\\+|-)*\\d*)?)\\]");

	public static String applyDTforHIsotopes(String smiles) {
		final Matcher m = H_ISOTOPE_PATTERN.matcher(smiles);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "[" + (m.group(1).equals("2") ? "D" : "T")
					+ m.group(2) + "]");
		}
		m.appendTail(sb);
		return sb.toString().replace("[D]", "D").replace("[T]", "T");

	}

	// D/T dont have isotopic labels!
	private static final Pattern DT_ISOTOPE_PATTERN =
			Pattern.compile("\\[(D|T)((?:@.*)?(?:H\\d*)?(?:(\\+|-)*\\d*)?)\\]");

	public static String replaceDTwithHIsotopes(String smiles) {
		// Start by putting non-bracketed D/T back together
		Matcher m = DT_ISOTOPE_PATTERN
				.matcher(convertIsolatedDTtoBracketedH(smiles));
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, "[" + (m.group(1).equals("D") ? "2" : "3")
					+ "H" + m.group(2) + "]");
		}
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Private method to replace any 'D' or 'T' outwith [] with '[2H]' or '[3H]'
	 * respectively
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return The modified SMILES
	 */
	private static String convertIsolatedDTtoBracketedH(String smiles) {
		StringBuilder sb = new StringBuilder(smiles);
		// NB define length as l as otherwise smiles.length
		// is re-calculated on each iteration, apparently
		for (int i = 0; i < sb.length(); i++) {
			char x = sb.charAt(i);
			if (x == '[') {
				// skip to ]
				while (sb.charAt(i) != ']') {
					i++;
				}
				continue;
			}
			// Deal with isolated D or T
			if (x == 'D') {
				sb.replace(i, i + 1, "[2H]");
				i += 2;
			} else if (x == 'T') {
				sb.replace(i, i + 1, "[3H]");
				i += 2;
			}
		}
		return sb.toString();
	}

	/**
	 * Pattern to group anything other than leading / trailing '.' characters
	 */
	private static final Pattern TERMINAL_DOT_BOND_PATTERN =
			Pattern.compile("^\\.*(.*?)\\.*$");
	/**
	 * Pattern to match to or more '.' characters
	 */
	private static final Pattern MULTI_DOT_PATTERN = Pattern.compile("\\.{2,}");

	/**
	 * SMILES strings can with loose parsing begin or end with the '.' non-bond
	 * character, or contain multiple contiguous such characters, which should
	 * be treated as a single '.'
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return cleaned SMILES
	 */
	public static String cleanUpNonBonds(String smiles) {
		return MULTI_DOT_PATTERN.matcher(
				TERMINAL_DOT_BOND_PATTERN.matcher(smiles).replaceAll("$1"))
				.replaceAll(".");
	}

	/**
	 * Pattern to match leading '0's in isotope labels, ensuring that any
	 * non-zeros are retained, along with 1 final '0' when the isotope label is
	 * 0 (as this is not the same as no isotopic label)
	 */
	private static final Pattern LEADING_ZERO_ISOTOPE_PATTERN =
			Pattern.compile("\\[0+(\\d+?[^\\d])");

	/**
	 * Isotopic labels are parsed as integers, allowing arbitrary leading '0's.
	 * This function cleans any leading 0's. In the case of the label being for
	 * isotope '0', then one '0' is retained, as this has a different meaning to
	 * no isotopic label, e.g. [0013C] is the same as [13C], and [000C] is the
	 * same as [0C], but not the same as [C]
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return The SMILES with leading 0's cleaned
	 */
	public static String cleanUpLeadingIsotopeZeros(String smiles) {
		return LEADING_ZERO_ISOTOPE_PATTERN.matcher(smiles).replaceAll("[$1");
	}

	/**
	 * Pattern to match 2 or more '+' or '-' characters. See
	 * https://stackoverflow.com/a/1660739/6076839 for explanation. In short,
	 * the \\2 is a back-reference which means that all the characters have to
	 * be the same. A lower bound of {1,} is used because group(2) already has 1
	 * character, so we need at least one more. The only place these characters
	 * are valid in 2 or more is within an atom, so we will lazily assume this
	 * is correctly handled
	 */
	private static final Pattern DEPRECATED_CHARGES_PATTERN =
			Pattern.compile("((\\+|-)\\2{1,})");

	/**
	 * Method to replace deprecated multiple-charge specification of 2 or more
	 * contiguous '+' or '-' characters
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return The cleaned SMILES
	 */
	public static String replaceDeprecatedCharges(String smiles) {
		Matcher m = DEPRECATED_CHARGES_PATTERN.matcher(smiles);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			m.appendReplacement(sb, m.group(2) + m.group(1).length());
		}
		m.appendTail(sb);
		return sb.toString();
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
			final SettingsModelString colNameMdl)
			throws InvalidSettingsException {
		DataColumnSpec colSpec =
				inSpec.getColumnSpec(colNameMdl.getStringValue());

		String retVal = null;
		if (colSpec == null) {
			// No column selected, or selected column not found - autoguess!
			for (int i = inSpec.getNumColumns() - 1; i >= 0; i--) {
				// Reverse order to select most recently added
				DataType colType = inSpec.getColumnSpec(i).getType();
				if (colType.isCompatible(SmilesValue.class)
						|| colType.isAdaptable(SmilesValue.class)) {
					colNameMdl
							.setStringValue(inSpec.getColumnSpec(i).getName());
					retVal = "No column selected. "
							+ colNameMdl.getStringValue() + " auto-selected.";
					break;
				}
				// If we are here when i = 0, then no suitable column found
				if (i == 0) {
					throw new InvalidSettingsException(
							"No SMILES column was found.");
				}
			}

		} else {
			// We had a selected column, now lets see if it is a compatible type
			if (!colSpec.getType().isCompatible(SmilesValue.class)) {
				// The column is not compatible with one of the accepted types
				throw new InvalidSettingsException(
						"The column " + colNameMdl.getStringValue()
								+ " is not in the SMILES format");
			}
		}
		return retVal;
	}

	/**
	 * According to the OpenSMILES, 'aromatic bonds are implicit and the
	 * explicit symbol should never be used'. Unfortunately, ':' also is the
	 * indicator of the start of an atom class, so we cannot do a simple replace
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return The cleaned SMILES
	 */
	public static String removeAromaticBonds(String smiles) {
		BitSet aromBonds = new BitSet(smiles.length());
		for (int i = 0; i < smiles.length(); i++) {
			final char c = smiles.charAt(i);
			if (c == '[') {
				// - inside [] is atom class separator
				while (smiles.charAt(i) != ']') {
					// skip over
					i++;
				}
				continue;
			}
			if (c == ':') {
				aromBonds.set(i);
			}
		}
		// Now aromBonds contains flags for aromatic bonds to be removed,
		// working backwards from end to not corrupt indices
		StringBuilder sb = new StringBuilder(smiles);
		for (int i = aromBonds.length(); (i =
				aromBonds.previousSetBit(i - 1)) >= 0;) {
			sb.deleteCharAt(i);
		}
		return sb.toString();
	}

	/**
	 * Set of organic subset aromatic atoms as chars
	 */
	private static final char[] ORGANIC_SUBSET_AROMATICS =
			{ 'b', 'c', 'n', 'o', 'p', 's' };
	/**
	 * Set of aromatic atoms which can only occur bracketed
	 */
	private static final List<String> BRACKET_AROMATICS =
			Arrays.asList(new String[] { "se", "as" });

	/**
	 * According to the OpenSMILES specification, the single bond symbol should
	 * only used when connecting 2 aromatic atoms, as it is otherwise implicit
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return The cleaned SMILES
	 */
	public static String removeUnnecessarySingleBonds(String smiles) {
		BitSet singleBonds = new BitSet(smiles.length());
		// First find all explicit single bonds
		for (int i = 0; i < smiles.length(); i++) {
			final char c = smiles.charAt(i);
			if (c == '[') {
				// - inside [] is -ve charge not single bond
				while (smiles.charAt(i) != ']') {
					// skip over
					i++;
				}
				continue;
			}
			if (c == '-') {
				singleBonds.set(i);
			}
		}

		// Now loop through them all, checking atom after (easier) and before
		// (harder), unsetting those to keep
		for (int i = singleBonds.nextSetBit(0); i >= 0; i =
				singleBonds.nextSetBit(i + 1)) {
			final char c = smiles.charAt(i + 1);
			if (c == '[') {
				// Followed by a bracketed atom
				int j = i + 2;
				char c0;
				while (Character.isDigit(c0 = smiles.charAt(j))) {
					// skip isotope label
					j++;
				}
				if (Arrays.binarySearch(ORGANIC_SUBSET_AROMATICS, c0) < 0
						&& !(Character.isLowerCase(c0)
								&& Character.isLowerCase(smiles.charAt(j + 1))
								&& BRACKET_AROMATICS.contains(
										smiles.substring(j, j + 2)))) {
					// Not aromatic - leave it in
					continue;
				}
			} else if (Arrays.binarySearch(ORGANIC_SUBSET_AROMATICS, c) < 0) {
				// Non-bracketed atom not aromatic - leave it in
				continue;
			}

			// The bond is followed by an aromatic atom
			// Now the harder bit - it is followed by an aromatic atom, but is
			// it preceded by one?
			// Optionally, there are one or more nested brackets e.g. ()(())
			// And there must by a ring closure which should be straight after
			// the preceding atom, before any ()
			int nestingLevel = 0;

			int j = i - 1;
			// Skip past any nested bracket sets
			while (j >= 0 && smiles.charAt(j) == ')') {
				nestingLevel++;
				j--;
				while (nestingLevel > 0) {
					if (smiles.charAt(j) == ')') {
						nestingLevel++;
					} else if (smiles.charAt(j) == '(') {
						nestingLevel--;
					}
					j--;
				}
			}
			// Skip ring closure digit(s)
			while (j >= 0 && Character.isDigit(smiles.charAt(j))) {
				j--;
			}
			// Skip ring closure 2 digit identifier
			if (smiles.charAt(j) == '%') {
				// skip
				j--;
			}
			if (Arrays.binarySearch(ORGANIC_SUBSET_AROMATICS,
					smiles.charAt(j)) >= 0) {
				// Preceded by a organic subset aromatic atom, so we want to
				// keep this one
				singleBonds.clear(i);
				continue;
			}
			if (smiles.charAt(j) == ']') {
				// Now need to work backwards to find the corresponding '['
				while (smiles.charAt(j) != '[') {
					j--;
				}
				// So now back-backtrack (forwardtrack?!) past any isotope label
				j++;
				char c0;
				while (Character.isDigit(c0 = smiles.charAt(j))) {
					// skip isotope label
					j++;
				}
				if (Arrays.binarySearch(ORGANIC_SUBSET_AROMATICS, c0) >= 0
						|| (Character.isLowerCase(c0)
								&& Character.isLowerCase(smiles.charAt(j + 1))
								&& BRACKET_AROMATICS.contains(
										smiles.substring(j, j + 2)))) {
					// [] aromatic, clear the flag
					singleBonds.clear(i);
					continue;
				}
			}
			// If we are here then it wasnt preceded by an aromatic atom, and so
			// it stays in to be marked for removal
		}

		// Now singleBonds contains flags for single bonds to be removed,
		// working backwards from end to not corrupt indices
		StringBuilder sb = new StringBuilder(smiles);
		for (int i = singleBonds.length(); (i =
				singleBonds.previousSetBit(i - 1)) >= 0;) {
			sb.deleteCharAt(i);
		}
		return sb.toString();
	}

	/**
	 * Pattern to match mono-charged atoms with a '1' after the charge sign
	 */
	private static final Pattern MONO_CHARGES_PATTERN =
			Pattern.compile("(\\[[^\\]]+[\\+\\-])1([^\\[]*\\])");

	/**
	 * Mono charged atoms should not have '1' after the + or - charge symbol
	 * 
	 * @param smiles
	 *            The SMILES
	 * @return The cleaned SMILES
	 */
	public static String cleanMonoCharges(String smiles) {
		return MONO_CHARGES_PATTERN.matcher(smiles).replaceAll("$1$2");
	}

	/**
	 * The set of characters which standardly make up bonds
	 */
	private static final char[] BOND_CHARS = { '#', '-', '/', ':', '=', '\\' };

	/**
	 * Method to convert scaffold to SMILES or SMARTS scaffold
	 * 
	 * @param smiles
	 *            The SMILES string to convert
	 * @param useAnyAtom
	 *            Should '*' (or 'a' and 'A') be used in place of 'C'
	 * @param keepExplicitH
	 *            Should any explicitly defined 'H' atoms (e.g. [H]) be kept as
	 *            H or converted to scaffold atoms?
	 * @param keepBondOrders
	 *            Bond orders will be kept if this is selected, otherwise they
	 *            will be converted to implicit bonds
	 * @param keepAromatic
	 *            Aromatic atoms will be kept as aromatic, e.g 'C' or 'c', or
	 *            'A' or 'a'. If this option is selected with the
	 *            {@code useAnyAtom} option, the result will be a SMARTS string,
	 *            as A and a are not valid SMILES ('*' is)
	 * @param keepChirality
	 *            Should chiral specifications be kept or dropped?
	 * @return The scaffold SMILES or SMARTS
	 */
	public static String convertToScaffold(String smiles, boolean useAnyAtom,
			boolean keepExplicitH, boolean keepBondOrders, boolean keepAromatic,
			boolean keepChirality) {
		StringBuilder sb = new StringBuilder();
		char repl = useAnyAtom ? keepAromatic ? 'A' : '*' : 'C';
		char aromRepl = keepAromatic ? useAnyAtom ? 'a' : 'c' : repl;
		for (int i = 0; i < smiles.length(); i++) {
			char c = smiles.charAt(i);
			// System.out.println(i + ":\t" + c);
			if (c == '[') {
				// Bracketed atom
				if (!keepAromatic && !keepExplicitH && !keepChirality) {
					// Doesnt matter what it is...
					sb.append(repl);
				} else {
					// skip isotope label
					i++;
					while (Character.isDigit(smiles.charAt(i))) {
						i++;
					}
					if (keepAromatic
							&& Character.isLowerCase(smiles.charAt(i))) {
						sb.append(aromRepl);
					} else if (keepExplicitH && ((smiles.charAt(i) == 'H'
							|| smiles.charAt(i) == 'T'
							|| smiles.charAt(i) == 'D')
							&& !Character.isLetter(smiles.charAt(i + 1)))) {

						// TODO: Keep these atoms 'properly'
						// TODO: Keep [nnXxHn...] Hs
						sb.append('[').append(smiles.charAt(i)).append(']');
					} else if (keepChirality && (smiles.indexOf('@', i) >= 0
							&& smiles.indexOf('@', i) < smiles.indexOf(']',
									i))) {
						// Chiral atom
						sb.append('[').append(repl);
						// Now figure the chirality, including and TH etc labels
						// and indices, and 'H'...
						i = smiles.indexOf('@', i);
						// 1 or more '@' - Daylight is unclear whether @@@ is
						// allowed for e.g. OH3
						while (smiles.charAt(i) == '@') {
							sb.append('@');
							i++;
						}
						// Either an 'H' or TH AL etc
						while (Character.isUpperCase(smiles.charAt(i))) {
							sb.append(smiles.charAt(i));
							i++;
						}
						// A possible numeric code, e.g. TH1
						while (Character.isDigit(smiles.charAt(i))) {
							sb.append(smiles.charAt(i));
							i++;
						}
						if (smiles.charAt(i) == 'H') {
							sb.append('H');
						}
						sb.append(']');
					} else {
						sb.append(repl);
					}
				}
				i = smiles.indexOf(']', i);
			} else if (keepBondOrders
					&& Arrays.binarySearch(BOND_CHARS, c) >= 0) {
				// A bond (and we are bothered!)
				sb.append(c);
			} else if (Character.isLetter(c) || c == '*') {
				// An unbracketed organic subset - NB could be 2 characters...
				if (Character.isLowerCase(c)) {
					// Must be aromatic - and these are all single char
					sb.append(aromRepl);
				} else if (keepExplicitH && (c == 'D' || c == 'T')
						&& (i + 1 < smiles.length()
								&& !Character.isLetter(smiles.charAt(i + 1)))) {
					// An isolated 'D' or 'T' pseudoatom
					sb.append(c);
				} else if (c == '*') {
					// Always has to stay as '*'
					sb.append('*');
				} else {
					sb.append(repl);
					// Might be a 2-character organic subset
					if (i + 1 < smiles.length()
							&& Character.isLowerCase(smiles.charAt(i + 1))
							&& organicSMILESSubset
									.contains(smiles.substring(i, i + 2))) {
						i++;
					}
				}
			} else if (c == '%' || Character.isDigit(c) || c == '('
					|| c == ')') {
				// Part of a ring closure or branch opening/closure
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
