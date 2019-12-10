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
package com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate;

import java.io.IOException;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.knime.core.node.NodeDescription;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.node2012.FullDescriptionDocument.FullDescription;
import org.knime.node2012.InPortDocument.InPort;
import org.knime.node2012.IntroDocument.Intro;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.OutPortDocument.OutPort;
import org.knime.node2012.PortsDocument.Ports;
import org.knime.node2012.TabDocument.Tab;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.vernalis.knime.nodes.NodeDescriptionUtils.TableFactory;

import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.ALLOW_HEAVY_ATOM_MISMATCHES;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.CONFORMER_OUTPUT_FORMAT;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.ENSURE_H_S_ADDED;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.FILTER_BY_ENERGY;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.FILTER_BY_TEMPLATE_RMSD;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.FILTER_CONFORMERS_BY_RMSD;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.FORCEFIELD;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.GEOMETRY_OPTIMISATION;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.IGNORE_H_S;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.ITERATIONS;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.MAX_NUMBER_OF_TRIES_TO_GENERATE_CONFORMER;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.MAX_RELATIVE_ENERGY;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.MAX_TEMPLATE_RMSD;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.MINIMUM_RMSD;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.MOLECULE_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.NUMBER_OF_CONFORMERS;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.OUTPUT_ACTUAL_TEMPLATE_FOR_ROW;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.RANDOM_SEED;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.REMOVE_H_S;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.TEMPLATE_COLUMN;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.TEMPLATE_MOL_BLOCK;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.TEMPLATE_OPTIONS;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.USE_BASIC_KNOWLEDGE_I_E_FLAT_RINGS_ETC;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.USE_EXPERIMENTAL_TORSIONS;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.USE_ROTATABLE_BOND_COUNT_TO_DETERMINE_NUMBER_OF_CONFORMERS;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.USE_TETHERS;
import static com.vernalis.knime.chem.pmi.nodes.confs.rdkitgenerate.RdkitConfgenNodeDialog.USE_UFF_FORCE_FIELD_IF_UNABLE_TO_GENERATE_MMFF;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addBundleInformation;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addDevelopedByVernalis;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.addOptionToTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.createTab;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.insertSubHeading;
import static com.vernalis.knime.nodes.NodeDescriptionUtils.insertURL;

/**
 * NodeFactory implementation for the 'Conformer Generation' node
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public class RdkitConfgenNodeFactory
		extends NodeFactory<RdkitConfgenNodeModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDescription()
	 */
	@Override
	protected NodeDescription createNodeDescription()
			throws SAXException, IOException, XmlException {
		return new NodeDescription() {

			@Override
			public Element getXMLDescription() {
				KnimeNodeDocument doc = KnimeNodeDocument.Factory.newInstance();
				KnimeNode node = doc.addNewKnimeNode();
				node.setIcon(getIconPath());
				node.setName(getNodeName());
				node.setType(KnimeNode.Type.VISUALIZER);
				node.setShortDescription(
						"This node generates conformers using the RDKit toolkit");
				FullDescription fullDesc = node.addNewFullDescription();
				Intro intro = fullDesc.addNewIntro();

				XmlCursor introCursor = intro.newCursor();
				introCursor.toFirstContentToken();
				introCursor.insertElementWithText("p",
						"This node generates conformers for input molecules using parallel processing, "
								+ "outputting all the generated conformers for each input row as a set of List "
								+ "Cells appended to the input row. Conformers arec returned in ascending "
								+ "energy order, and optional filters for maximum relative energy (kcal/mol) "
								+ "may be added, along with a minimum RMSD filter, which removes "
								+ "'similar' conformers.");
				introCursor.beginElement("p");
				introCursor.insertChars(
						"There are options to use the 'basic knowledge' and 'experimental torsions' "
								+ "knowledge-based conformer generation refinements of Riniker and Landrum "
								+ "('Better Informed Distance Geometry: Using What We Know To Improve "
								+ "Conformation Generation', ");
				introCursor.insertElementWithText("i", "J. Chem. Inf. Model.");
				introCursor.insertChars(", 2015, ");
				introCursor.insertElementWithText("b", "55");
				introCursor.insertChars(", 2562-2574; DOI: ");
				insertURL(introCursor,
						"http://dx.doi.org/10.1021/acs.jcim.5b00654",
						"10.1021/acs.jcim.5b");
				introCursor.insertChars(")");
				introCursor.toEndToken();
				introCursor.toNextToken();

				insertSubHeading(introCursor, "Number of Conformers");
				introCursor.beginElement("p");
				introCursor.insertChars(
						"The number of conformers generated can optionally be determined from "
								+ "the number of rotatable bonds according to the parameters in '"
								+ "Freely Available Conformer Generation Methods: How Good Are They?' (");
				introCursor.insertElementWithText("i", "J. Chem. Inf. Model.");
				introCursor.insertChars(", 2012, ");
				introCursor.insertElementWithText("b", "52");
				introCursor.insertChars(", 1146-1158; DOI:");
				insertURL(introCursor, "http://dx.doi.org/10.1021/ci2004658",
						"10.1021/ci2004658");
				introCursor.insertChars(")");
				introCursor.toEndToken();
				introCursor.toNextToken();

				TableFactory tf =
						new TableFactory("Rotatable Bonds", "Conformers");
				tf.setPreTableText("The values used are:").setPostTableText(
						"When a template is supplied, then the number of rotatable bonds "
								+ "used in this calculation is adjusted to account "
								+ "for the rigid template.");
				tf.addRowToTable("< 8", "50").addRowToTable("8 - 12", "200")
						.addRowToTable("> 12", "300");
				tf.buildTable(introCursor);

				insertSubHeading(introCursor, "Templates");
				introCursor.beginElement("p");
				introCursor.insertChars(
						"Optionally, a template molecule may be supplied. This can be "
								+ "supplied on a row-by-row basis from a second input column, or as a "
								+ "Mol block pasted into the node settings (or supplied from a flow "
								+ "variable). In either case, the following process is applied to the "
								+ "template supplied in order to use it as the basis of a template for "
								+ "conformer generation:");
				introCursor.beginElement("ol");
				introCursor.insertElementWithText("li",
						"If the template is a substructure of the molecule, then it is "
								+ "used as such - where there are multiple possibilities for matching "
								+ "the template to the molecule, the first is used arbitrarily");
				introCursor.insertElementWithText("li",
						"Otherwise, a Maximum Common Substructure (MCS) is generated "
								+ "between the template and the molecule. Currently, this requires "
								+ "atoms to match (otherwise H/non-H matches cause problems for "
								+ "conformer generation). NB: In a future update, this will allow "
								+ "matches between differing heavy atoms as an additional option.");
				introCursor.toEndToken();
				introCursor.toNextToken();
				introCursor.toEndToken();
				introCursor.toNextToken();
				introCursor.insertElementWithText("p",
						"The template can be applied with or without tethers. Tethers add "
								+ "extra points to the forcefield in the positions of the template "
								+ "atoms, and distance constraints from these points to the matched "
								+ "atoms, effectively aligning the generated conformers closely to the "
								+ "actual template orientation. If tethers are not used, simple "
								+ "distance constraints between all pairs of atoms matching the "
								+ "template atoms are added to match the corresponding distances in the "
								+ "template. The conformer will thus not be exactly aligned to the "
								+ "template, although it should be geometrically constrained to be a "
								+ "rotation/translation.");

				introCursor.beginElement("p");
				introCursor.insertChars(
						"Finally, an RMSD filter may be applied to limit the RMSD deviation "
								+ "of the conformers from the template (this ");
				introCursor.insertElementWithText("i", "maximum");
				introCursor.insertChars(
						" RMSD filter should not be confused with the ");
				introCursor.insertElementWithText("i", "minimum");
				introCursor.insertChars(
						" RMSD filter between conformers mentioned above)");
				introCursor.toEndToken();
				introCursor.toNextToken();

				insertSubHeading(introCursor, "Geometry Optimisation");
				introCursor.insertElementWithText("p",
						"Following conformer generation, the conformers undergo geometry "
								+ "optimisation using one of 3 forcefields (UFF, MMFF94, MMFF94S). "
								+ "Optionally, if the forcefield is not parameterised fully for the "
								+ "molecule, the node can default to use the UFF forcefield, which "
								+ "paramaterises most 'organic' molecules. If the UFF forcefield is "
								+ "substituted, a message is sent to the console log. Molecules for "
								+ "which no optimisation was possible have missing values in the "
								+ "conformer energy column. It should be noted that the conformer "
								+ "energy will include any terms for constraining to a template, and "
								+ "explicit Hydrogen contributions.");

				addDevelopedByVernalis(introCursor);
				introCursor.dispose();

				Tab optionsTab = createTab(fullDesc, "Options");
				addOptionToTab(optionsTab, MOLECULE_COLUMN,
						"The input molecule column column");
				addOptionToTab(optionsTab, ENSURE_H_S_ADDED,
						"Hydrogens are added to incoming molecules prior to "
								+ "conformer generation");
				addOptionToTab(optionsTab,
						USE_ROTATABLE_BOND_COUNT_TO_DETERMINE_NUMBER_OF_CONFORMERS,
						"If set, then the number of conformers generated is "
								+ "based on the number of rotatable bonds in the input molecule (see above)");
				addOptionToTab(optionsTab, NUMBER_OF_CONFORMERS,
						"The number of conformers to generate (The actual number returned maybe "
								+ "less than this depending on subsequent filtering options)");
				addOptionToTab(optionsTab,
						MAX_NUMBER_OF_TRIES_TO_GENERATE_CONFORMER,
						"The number of attempts to generate a conformer");
				addOptionToTab(optionsTab, USE_EXPERIMENTAL_TORSIONS,
						"Should the experimental torsions be used");
				addOptionToTab(optionsTab,
						USE_BASIC_KNOWLEDGE_I_E_FLAT_RINGS_ETC,
						"Should 'basic knowledge' be used (i.e. flat rings etc)");
				addOptionToTab(optionsTab, FILTER_CONFORMERS_BY_RMSD,
						"Should conformers be filtered such that no two conformers are within "
								+ "the RMSD threshold of each other?");
				addOptionToTab(optionsTab, MINIMUM_RMSD,
						"The minimum allowed RMSD between conformers");
				addOptionToTab(optionsTab, IGNORE_H_S,
						"Should H's be ignored when calculating RMSD (faster!)");
				addOptionToTab(optionsTab, REMOVE_H_S,
						"Should explicit H's be removed from the output molecules?");
				addOptionToTab(optionsTab, CONFORMER_OUTPUT_FORMAT,
						"The cell type to output (RDKit or Mol)");

				/*
				 * Template Options tab
				 */
				Tab templateTab = createTab(fullDesc, TEMPLATE_OPTIONS);
				addOptionToTab(templateTab, OUTPUT_ACTUAL_TEMPLATE_FOR_ROW,
						"The actual template used for the row, after any MCS calculation "
								+ "is added to the output table");
				addOptionToTab(templateTab, FILTER_BY_TEMPLATE_RMSD,
						"Should conformers be filtered according to their RMSD "
								+ "deviation from the template");
				addOptionToTab(templateTab, MAX_TEMPLATE_RMSD,
						"The maximum permitted RMSD deviation from the template");
				addOptionToTab(templateTab, USE_TETHERS,
						"Should tethers be used when aligning the conformer to "
								+ "the template? See above for details");
				addOptionToTab(templateTab, ALLOW_HEAVY_ATOM_MISMATCHES,
						"Allow template heavy atoms to match different heavy atoms "
								+ "in the molecule, e.g. morpholine / piperazine");
				addOptionToTab(templateTab, TEMPLATE_COLUMN,
						"A column containing the templates");
				addOptionToTab(templateTab, TEMPLATE_MOL_BLOCK,
						"The Mol block of the template used for the entire table.  "
								+ "If a template is supplied this way, then the actual "
								+ "template after MCS may be different for each row");

				/*
				 * Geometry Optimisation tab
				 */
				Tab geomTab = createTab(fullDesc, GEOMETRY_OPTIMISATION);
				addOptionToTab(geomTab, FORCEFIELD,
						"The force field to use for geometry optimisation");
				addOptionToTab(geomTab,
						USE_UFF_FORCE_FIELD_IF_UNABLE_TO_GENERATE_MMFF,
						"Fall back to the UFF if unable to paramaterise "
								+ "selected force field (see above for details)");
				addOptionToTab(geomTab, ITERATIONS,
						"The number of iterations to attempt convergence. "
								+ "Conformers which do not converge will be rejected.  "
								+ "A value of 0 results in no geometry optimisation "
								+ "being attempted.");
				addOptionToTab(geomTab, FILTER_BY_ENERGY,
						"Should the conformers be filtered by relative energy?");
				addOptionToTab(geomTab, MAX_RELATIVE_ENERGY,
						"The maximum relative energy for retained conformations");

				/*
				 * Flow variables tab - hidden setting
				 */
				Tab flowVarTab = createTab(fullDesc, "Flow Variables");
				addOptionToTab(flowVarTab, RANDOM_SEED,
						"Hidden setting only available via flow variables to fix the random "
								+ "seed used for conformer generation for testing purposes.  "
								+ "The default value of -1 gives random behaviour");

				/*
				 * Ports
				 */
				Ports ports = node.addNewPorts();

				InPort inport = ports.addNewInPort();
				inport.setIndex(0);
				inport.setName(getInportName(0));
				inport.newCursor().setTextValue(getInportDescription(0));

				OutPort outport = ports.addNewOutPort();
				outport.setIndex(0);
				outport.setName(getOutportName(0));
				outport.newCursor().setTextValue(getOutportDescription(0));

				addBundleInformation(node, RdkitConfgenNodeFactory.class);
				return (Element) node.getDomNode();
			}

			@Override
			public String getViewName(int index) {
				return null;
			}

			@Override
			public String getViewDescription(int index) {
				return null;
			}

			@Override
			public int getViewCount() {
				return 0;
			}

			@Override
			public NodeType getType() {
				return NodeType.Manipulator;
			}

			@Override
			public String getOutportName(int index) {
				return "Conformers";
			}

			@Override
			public String getOutportDescription(int index) {
				return "The generated conformers";
			}

			@Override
			public String getNodeName() {
				return "Conformer Generation";
			}

			@Override
			public String getInteractiveViewName() {
				return null;
			}

			@Override
			public String getInportName(int index) {
				return "Input molecules";
			}

			@Override
			public String getInportDescription(int index) {
				return "Table of input molecules and optionally templates for conformer generation";
			}

			@Override
			public String getIconPath() {
				return "confs.png";
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeModel()
	 */
	@Override
	public RdkitConfgenNodeModel createNodeModel() {
		return new RdkitConfgenNodeModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#getNrNodeViews()
	 */
	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeView(int,
	 * org.knime.core.node.NodeModel)
	 */
	@Override
	public NodeView<RdkitConfgenNodeModel> createNodeView(int viewIndex,
			RdkitConfgenNodeModel nodeModel) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#hasDialog()
	 */
	@Override
	protected boolean hasDialog() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.NodeFactory#createNodeDialogPane()
	 */
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new RdkitConfgenNodeDialog();
	}

}
