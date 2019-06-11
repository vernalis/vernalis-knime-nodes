/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.transform;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.vernalis.knime.mmp.ToolkitException;

/**
 * Utility class containing methods for manipulation of rSMARTs transform
 * strings
 * 
 * @author s.roughley
 *
 */
public class TransformUtils {
	private TransformUtils() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * Method to convert a SMARTS string to one requiring an exact match. Uses
	 * toolkit calls defined in {@link TransformUtilityFactory}
	 * 
	 * @param SMARTS
	 *            The SMARTS String
	 * @param transformUtilFact
	 *            The {@link TransformUtilityFactory} instance
	 * @param rowIndex
	 *            The row index used for native object GC
	 * @return The exact match version of the SMARTS string
	 * @throws ToolkitException
	 *             If the toolkit could not perform the conversion
	 */
	public static <T, U, V> String convertSMARTSToExactMatchSMARTS(String SMARTS,
			TransformUtilityFactory<T, U, V> transformUtilFact, long rowIndex)
			throws ToolkitException {
		U m = transformUtilFact.generateQueryMoleculeFromSMARTS(SMARTS, rowIndex);
		U m2 = transformUtilFact.addHsToQueryMolecule(m, rowIndex);
		return transformUtilFact.getSMARTSFromMolecule(m2);
	}

	/**
	 * Function to convert the SMIRKS representation, with attachment points
	 * labelled by isotopic value (e.g. [2*]), to Reaction SMARTS, with
	 * attachment points labelled by atom label (e.g. ([*:2]). Unlabelled
	 * attachments are labelled '1'. Optionally, the attachment points can be
	 * forced to be attached by a single, acyclic bond (which makes most sense
	 * in view of this being the bond type broken during fragmentation)
	 * 
	 * @param SMIRKS
	 *            The isotopically labelled reaction SMIRKS
	 * @param requireAcyclicSingleBondedAPs
	 *            Should the bonds to APs be required to be single, acyclic?
	 * @return The Reaction SMARTS with correct attachment point labelling
	 */
	public static String convertSmirksToReactionSmarts(String SMIRKS,
			boolean requireAcyclicSingleBondedAPs) {
		String retVal = SMIRKS.replaceAll("\\[([0-9]+)\\*\\]", "[*:$1]").replace("[*]", "[*:1]");
		if (requireAcyclicSingleBondedAPs) {
			String[] rSMARTS = retVal.split(">>");
			for (int i = 0; i < rSMARTS.length; i++) {
				rSMARTS[i] = rSMARTS[i].replaceAll("^(\\[\\*:\\d+\\])", "$1-!@");
				rSMARTS[i] = rSMARTS[i].replaceAll("(\\[\\*:\\d+\\]\\))", "-!@$1");
				rSMARTS[i] = rSMARTS[i].replaceAll("(\\[\\*:\\d+\\])$", "-!@$1");
				rSMARTS[i] = rSMARTS[i].replace("-!@-!@", "-!@");
				retVal = rSMARTS[0] + ">>" + rSMARTS[1];
			}
		}
		return retVal;
	}

	/**
	 * Method to generate a reaction SMARTS which will take an MMP transform
	 * SMARTS and convert it to one which will provide the key (leafs) component
	 * of the molecule to be transformed, rather than the MMP transform product.
	 * 
	 * @param mmpTransformRSMARTS
	 *            The rSMARTS for the Matched-pair transform
	 * @return The rSMARTS for the Incoming molecule -> leaf transform
	 */
	public static String convertRSmartsToLeafGeneration(String mmpTransformRSMARTS) {
		String match = convertRSmartsToMatchSmarts(mmpTransformRSMARTS);
		StringBuilder sb = new StringBuilder(match);
		sb.append(">>");
		Matcher matcher = AP_MATCH.matcher(match);
		boolean isFirst = true;
		while (matcher.find()) {
			if (!isFirst) {
				sb.append(".");
			} else {
				isFirst = false;
			}
			sb.append("[*:");
			sb.append(matcher.group(1));
			sb.append("]-[");
			sb.append(matcher.group(1));
			sb.append("#0]");
		}
		return sb.toString();
	}

	/**
	 * Method to convert an rSMARTS to a SMART matcher for the reactants
	 * 
	 * @param mmpTransformRSMARTS
	 *            The rSMARTS in form {reactants}>>{products}
	 * @return The reactant match (i.e. {reactants} in the above example)
	 */
	public static String convertRSmartsToMatchSmarts(String mmpTransformRSMARTS) {
		return mmpTransformRSMARTS.split(">>")[0];
	}

	/**
	 * Method to enumterate a set of rSMARTS in which each AP atom has had a
	 * chirality arbitrarily assigned systematically to @ or @@
	 * 
	 * 
	 * @param mmpTransformRSMARTS
	 *            The transform rSMARTS
	 * @return A Set of rSMARTS with chirality inserted to the APs
	 */
	public static Set<String> convertRSmartsToChiralProducts(String mmpTransformRSMARTS) {

		final String[] splitRSMARTS = mmpTransformRSMARTS.split(">>");
		Set<String> currentRound = new LinkedHashSet<>();
		Set<String> nextRound = new LinkedHashSet<>();
		nextRound.add(splitRSMARTS[1]);
		Set<String> complete = new LinkedHashSet<>();
		while (!nextRound.isEmpty()) {
			currentRound = new LinkedHashSet<>(nextRound);
			nextRound.clear();
			for (String current : currentRound) {
				String next = current.replaceFirst("\\[\\*:(\\d+)\\]", "[*@:$1]");
				if (AP_MATCH.matcher(next).find()) {
					nextRound.add(next);
					nextRound.add(current.replaceFirst("\\[\\*:(\\d+)\\]", "[*@@:$1]"));
				} else {
					complete.add(next);
					complete.add(current.replaceFirst("\\[\\*:(\\d+)\\]", "[*@@:$1]"));
				}
			}
		}
		return complete.stream().map(x -> splitRSMARTS[0] + ">>" + x).collect(Collectors.toSet());

	}

	private static final Pattern AP_MATCH = Pattern.compile("\\[\\*:(\\d+)\\]");
}
