/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd, based on earlier PDB Connector work.
 * 
 * Copyright (c) 2012, 2014 Vernalis (R&D) Ltd and Enspiral Discovery Limited
 * 
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
package com.vernalis.pdbconnector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vernalis.pdbconnector.config.PdbConnectorConfig2;
import com.vernalis.pdbconnector.config.Properties;
import com.vernalis.pdbconnector.config.QueryCategory;
import com.vernalis.pdbconnector.config.QueryOption;
import com.vernalis.pdbconnector.config.QueryParam;
import com.vernalis.pdbconnector.config.QueryParam.eType;

/**
 * Class providing a single static method to render advanced queries as a
 * 'Logical' view, from an Advanced XML Query String
 * 
 * @author S.Roughley
 *
 */
public abstract class XMLQueryRenderer {
	private static List<QueryOption> allOptions = new ArrayList<>();
	static {
		allOptions.add(PdbConnectorConfig2.getInstance().getSimilarity());
		for (QueryCategory qCat : PdbConnectorConfig2.getInstance().getQueryCategories()) {
			allOptions.addAll(qCat.getQueryOptions());
		}
	}

	/**
	 * Method to render advanced queries as a 'Logical' view, from an Advanced
	 * XML Query String
	 * 
	 * @param xml
	 *            The Advanced query XML
	 * @return A logical rendering
	 */
	public static String XMLtoQuery(String xml) {
		String linebreak = (xml.contains("\r")) ? "\r\n" : "\n";
		String query = xml.replace("\r", "").replace("\t", "").replace("\n", "");

		query = query.replace(Properties.COMPOSITE_START, "(\n");
		query = query.replace(Properties.COMPOSITE_END, "\n)");

		query = query.replace(Properties.REFINEMENT_START, "");
		query = query.replace(Properties.REFINEMENT_END, "");
		query = query.replaceAll(Properties.LEVEL_START + "\\d+" + Properties.LEVEL_END, "");
		query = query.replaceAll(Properties.CONJUNCTION_AND, "\n\nAND\n\n");
		query = query.replaceAll(Properties.CONJUNCTION_OR, "\n\nOR\n\n");
		query = query.replaceAll("(?s)<orgPdbQuery>.*?<queryType>", "<orgPdbQuery><queryType>");
		while (query.indexOf("<orgPdbQuery>") >= 0) {
			String type = query.replaceAll("(?s).*?<queryType>(.*?)</queryType>.*", "$1");
			boolean typeFound = false;
			for (QueryOption qOpt : allOptions) {
				if (qOpt.getQueryString().indexOf(type) >= 0) {
					// We have the correct option - now build a regex
					typeFound = true;
					String regex = qOpt.getQueryString();

					List<QueryParam> qParams = qOpt.getParams();
					// Now deal with query parameters with sub-queries

					int replaceIdx = 1;
					for (QueryParam qParam : qParams) {
						if (qParam.getQueryStrings().size() == 0) {
							// We need to increment to skip this parameters
							// argument
							replaceIdx++;
						} else {
							for (String subQuery : qParam.getQueryStrings()) {
								regex = regex.replace("%ARG" + (replaceIdx++) + "%",
										"(" + subQuery + ")?");
							}
						}
					}

					regex = regex.replaceAll("%ARG\\d*%", "(.*?)");
					// Deal with dates which are of form
					// %ARG1%-%ARG2%-%ARG3%
					regex = regex.replace("(.*?)-(.*?)-(.*?)", "(.*?)");
					// }
					Pattern qPatt = Pattern.compile(regex, Pattern.DOTALL);
					Matcher matcher = qPatt.matcher(query);
					matcher.find();

					StringBuilder replacement = new StringBuilder(qOpt.getLabel()).append(":\n");
					int qParamIndex = 0;
					String optionalFilter = null;// An optional String for
					// comparators if
					// followed by a
					// String_COND type
					for (int g = 1; g <= matcher.groupCount(); g++) {
						QueryParam qParam = qParams.get(qParamIndex);
						if (qParam.getType() == eType.STRING_LIST) {
							// Need to check ahead in case followed by
							// String_COND type
							if (qParamIndex + 1 < qParams.size() && qParams.get(qParamIndex + 1)
									.getType() == eType.STRING_COND) {
								// We may need this, or may not...
								optionalFilter = "\t" + qParam.getLabel() + ": " + matcher.group(g)
										+ " \"";
							} else {
								replacement.append("\t").append(qParam.getLabel()).append(": \"");
								replacement.append(
										qParam.getValues().lookupKeyForValue((matcher.group(g))));
								replacement.append("\"");
								if (g < matcher.groupCount()) {
									replacement.append("\n");
								}
							}
							qParamIndex++;
						} else if (qParam.getTypeAsString().toUpperCase().indexOf("_COND") >= 0) {
							if (qParam.getTypeAsString().toUpperCase().indexOf("RANGE") >= 0) {
								// The parameter is a range
								double min = qParam.getMin();
								double max = qParam.getMax();
								String group = matcher.group(g++);
								boolean hasSubType = false;
								if (group != null) {
									min = Double.parseDouble(matcher.group(g++));
									hasSubType = true;
								} else {
									g++;
								}
								group = matcher.group(g++);
								if (group != null) {
									max = Double.parseDouble(matcher.group(g));
									hasSubType = true;
								} // Dont increment second time as the loop will
									// do that!

								if (hasSubType) {
									replacement.append("\t").append(qParam.getLabel()).append(": ");
									replacement.append(min).append(" - ").append(max);
								}
								if (g < matcher.groupCount()) {
									replacement.append("\n");
								}
								qParamIndex++;
							} else {
								// String COND
								assert qParam
										.getType() == eType.STRING_COND : "Expected conditional String";
								String val = "";
								String group = matcher.group(g++);
								boolean hasSubType = false;
								if (group != null) {
									val = matcher.group(g);
									hasSubType = true;
								}
								if (hasSubType) {
									replacement.append(optionalFilter);
									replacement.append(val).append("\"");
								}
								if (g < matcher.groupCount()) {
									replacement.append("\n");
								}
								qParamIndex++;
							}
						} else if (qParam.getTypeAsString().toUpperCase().indexOf("RANGE") >= 0) {
							replacement.append("\t").append(qParam.getLabel()).append(": ");
							replacement.append(matcher.group(g++)).append(" - ")
									.append(matcher.group(g));
							if (g < matcher.groupCount()) {
								replacement.append("\n");
							}
							qParamIndex++;
						} else if (qParam.getTypeAsString().toUpperCase().indexOf("STRING") >= 0) {
							replacement.append("\t").append(qParam.getLabel()).append(": \"");
							replacement.append(matcher.group(g));
							replacement.append("\"");
							if (g < matcher.groupCount()) {
								replacement.append("\n");
							}
							qParamIndex++;
						} else {
							replacement.append("\t").append(qParam.getLabel()).append(": ");
							replacement.append(matcher.group(g));
							if (g < matcher.groupCount()) {
								replacement.append("\n");
							}
							qParamIndex++;
						}
					}
					query = query.replaceFirst(regex, replacement.toString());
					break;
				}
			}
			if (!typeFound) {
				query = query.replaceAll(
						"(?s)(.*?)<orgPdbQuery>.*?<queryType>.*?</queryType>.*?</orgPdbQuery>(.*)",
						"$1\nWARNING: Unknown query type '" + type + "'\n$2");
			}
		}
		query = query.trim();
		if (query.startsWith("("))

		{
			query = query.substring(1, query.length() - 1);
		}

		// Now we indent nested brackets
		String[] lines = query.split("\n");
		StringBuilder retVal = new StringBuilder();
		String indent = "";
		for (String line : lines) {
			if (line.startsWith("(")) {
				retVal.append(indent).append(line).append(linebreak);
				indent += "\t";
			} else if (line.startsWith(")")) {
				indent = indent.substring(1);
				retVal.append(indent).append(line).append(linebreak);
			} else if (line.trim().isEmpty()) {
				retVal.append(linebreak);
			} else {
				retVal.append(indent).append(line).append(linebreak);
			}
		}

		String retValStr = retVal.toString().trim();
		while (retValStr.indexOf(linebreak + linebreak + linebreak) >= 0) {
			retValStr = retValStr.replace(linebreak + linebreak + linebreak, linebreak + linebreak);
		}
		retValStr = retValStr.replaceAll(linebreak + linebreak + "(\t*\\))", linebreak + "$1");
		return retValStr;
	}

	public static String replaceNthOccurence(String inputStr, String searchString,
			String replacementString, int N) {
		String retVal = inputStr;
		int idx = -1;
		for (int i = 0; i <= N; i++) {
			idx = inputStr.indexOf(searchString, idx + 1);
			if (idx < 0) {
				return retVal;
			}
		}
		return retVal.substring(0, idx) + replacementString
				+ retVal.substring(idx + searchString.length());
	}
}
