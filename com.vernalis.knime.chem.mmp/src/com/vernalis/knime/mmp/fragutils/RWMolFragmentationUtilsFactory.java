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
package com.vernalis.knime.mmp.fragutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.RDKit.Bond.BondStereo;
import org.RDKit.Bond.BondType;
import org.RDKit.ChemicalReaction;
import org.RDKit.ChemicalReactionException;
import org.RDKit.ChemicalReactionParserException;
import org.RDKit.ExplicitBitVect;
import org.RDKit.GenericRDKitException;
import org.RDKit.Int_Pair;
import org.RDKit.Int_Vect;
import org.RDKit.Match_Vect;
import org.RDKit.Match_Vect_Vect;
import org.RDKit.MolSanitizeException;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.ROMol_Vect;
import org.RDKit.ROMol_Vect_Vect;
import org.RDKit.RWMol;
import org.RDKit.SanitizeFlags;
import org.RDKit.UInt_Vect;
import org.knime.base.data.xml.SvgCellFactory;
import org.knime.chem.types.MolValue;
import org.knime.chem.types.SdfValue;
import org.knime.chem.types.SmilesValue;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.vector.bitvector.DenseBitVector;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.rdkit.knime.types.RDKitMolValue;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.mmp.MolFormats;
import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.ClosedFactoryException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationFactory2;
import com.vernalis.knime.mmp.fragmentors.RWMolFragmentationFactory;
import com.vernalis.knime.mmp.transform.TransformUtilityFactory;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2;

/**
 * RDKit Implementation of the {@link TransformUtilityFactory}
 * 
 * @author s.roughley
 *
 */
public class RWMolFragmentationUtilsFactory
		implements TransformUtilityFactory<RWMol, ROMol, ChemicalReaction> {

	/**
	 * The {@link DataType} for the cell renderer
	 */
	public static final DataType RDKIT_RENDERER_CELLTYPE = SvgCellFactory.TYPE;

	/** SWIG Garbage collector for RDKit Objects */
	private final SWIGObjectGarbageCollector2 m_SWIGGC =
			new SWIGObjectGarbageCollector2();

	/**
	 * Whether the changes which affected dative bond type SMILES rendering have
	 * been applied
	 */
	public static final boolean IS_PREDATIVE_BOND_CHANGE =
			isPreDativeFixVersion();

	/**
	 * This is to fix breakage from pull request
	 * https://github.com/rdkit/rdkit/pull/1202
	 */
	public static final boolean IS_PRE_SMILES_DBL_BOND_GEOMETRY_CHANGE =
			isPreDblBondGeomFixVersion();

	/**
	 * Routine to figure if the rdkit version is after 15-May-2017 when changes
	 * breaking dative bonds were added
	 */
	public static boolean isPreDativeFixVersion() {
		RWMol mol = RWMol.MolFromSmiles("CC");
		mol.getBondWithIdx(0).setBondType(BondType.DATIVE);
		String smi = mol.MolToSmiles();
		mol.delete();
		return !smi.contains("-");
	}

	/**
	 * Routine to figure if the rdkit verison is after 15-May-2017 to fix
	 * breakage from pull request https://github.com/rdkit/rdkit/pull/1202
	 */
	private static boolean isPreDblBondGeomFixVersion() {
		RWMol mol = RWMol.MolFromSmiles("CC=CCl");
		RDKFuncs.findPotentialStereoBonds(mol, false);
		boolean retVal =
				mol.getBondWithIdx(1).getStereo() != BondStereo.STEREOANY;
		mol.delete();
		return retVal;
	}

	/**
	 * Wrap layer for RDKit objects to be persisted throughout a call to
	 * {@link #execute(BufferedDataTable[], ExecutionContext)}
	 */
	private static final long GC_LEVEL_EXECUTE = 0;

	@Override
	public RWMol getMolFromCell(DataCell molCell, long rowIndex,
			boolean removeExplicitHs) throws ToolkitException {
		RWMol mol = null;
		DataType type = molCell.getType();
		try {
			if (type.isCompatible(RDKitMolValue.class)) {
				mol = new RWMol(m_SWIGGC.markForCleanup(
						((RDKitMolValue) molCell).readMoleculeValue(),
						rowIndex));
				if (removeExplicitHs) {
					RDKFuncs.removeHs(mol);
				}
			} else if (type.isCompatible(SmilesValue.class)) {
				// Default SMILES sanitization removes Hs, so sanitize if we
				// want to remove, otherwise sanitize separately (which does
				// *not* remove H's!)
				mol = RWMol.MolFromSmiles(
						((SmilesValue) molCell).getSmilesValue(), 0,
						removeExplicitHs);
				if (!removeExplicitHs) {
					RDKFuncs.sanitizeMol(mol);
				}
			} else if (type.isCompatible(MolValue.class)) {
				mol = RWMol.MolFromMolBlock(((MolValue) molCell).getMolValue(),
						true, removeExplicitHs);
			} else if (type.isCompatible(SdfValue.class)) {
				mol = RWMol.MolFromMolBlock(((SdfValue) molCell).getSdfValue(),
						true, removeExplicitHs);
			} else {
				throw new RowExecutionException(
						"Cell is not a recognised molecule type");
			}
		} catch (MolSanitizeException e) {
			// MolSanitizeException returns null for #getMessage()
			throw new ToolkitException("Error in sanitizing molecule: "
					+ ((StringValue) molCell).getStringValue() + " : "
					+ e.what(), e);
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg == null || "".equals(msg)) {
				// Try to do something useful if we have a different RDKit
				// Exception - at least try to report the error type!
				msg = e.getClass().getSimpleName();
				try {
					msg += " : " + ((GenericRDKitException) e).what();
				} catch (Exception e1) {
					// Do nothing
				}
			}
			if (msg.equals("Cell is not a recognised molecule type")) {
				throw new ToolkitException(msg, e);
			} else {
				throw new ToolkitException("Error in parsing molecule: "
						+ ((StringValue) molCell).getStringValue() + " : "
						+ msg, e);
			}
		}
		return m_SWIGGC.markForCleanup(mol, rowIndex);
	}

	@Override
	public RWMol createHAddedMolecule(RWMol mol, long rowIndex)
			throws ToolkitException {
		RWMol mol2 = m_SWIGGC.markForCleanup(new RWMol(mol), rowIndex);
		RDKFuncs.addHs(mol2);
		return mol2;
	}

	@Override
	public boolean moleculeIsEmpty(RWMol mol) {
		return mol == null || "".equals(mol.MolToSmiles(true));
	}

	@Override
	public boolean moleculeIsMultiComponent(RWMol mol) {
		return mol.MolToSmiles().contains(".");
	}

	@Override
	public ROMol getMatcher(String SMARTS) throws ToolkitException {
		return m_SWIGGC.markForCleanup(RWMol.MolFromSmarts(SMARTS),
				GC_LEVEL_EXECUTE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.fragutils.FragmentationUtilsFactory#
	 * postExecuteCleanup()
	 */
	@Override
	public void postExecuteCleanup() {
		// Quarantine to give time for all threads to cancel
		m_SWIGGC.quarantineAndCleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.fragutils.FragmentationUtilsFactory#
	 * nodeReset()
	 */
	@Override
	public void nodeReset() {
		// Quarantine to give time for all threads to cancel
		m_SWIGGC.quarantineAndCleanupMarkedObjects();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.fragutils.FragmentationUtilsFactory#
	 * rowCleanup(long)
	 */
	@Override
	public void rowCleanup(long index) {
		m_SWIGGC.cleanupMarkedObjects(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.mmp.fragutils.FragmentationUtilsFactory#
	 * nodeDispose()
	 */
	@Override
	public void nodeDispose() {
		m_SWIGGC.cleanupMarkedObjects();
	}

	@Override
	public String validateMatcherSmarts(String SMARTS) {
		// TODO: This is a crude fix until QueryBond#getQuery() returns an
		// inspectable query object (RDKit issue #1279
		// https://github.com/rdkit/rdkit/issues/1279)
		if (!SMARTS.contains("!@")) {
			return "Bond must be specified as acyclic";
		}
		ROMol matcher;
		try {
			if (SMARTS.contains(">>")) {
				matcher = getMatcher(SMARTS.split(">>")[0]);
			} else {
				matcher = getMatcher(SMARTS);
			}
		} catch (GenericRDKitException e) {
			return "Unable to generate matcher from SMIRKS - " + e.what();
		} catch (Exception e) {
			return "Unable to generate matcher from SMIRKS - " + e.getMessage();
		}
		if (matcher == null) {
			return "Unable to generate matcher from SMIRKS - unknown reason";
		}
		if (matcher.getNumAtoms() != 2) {
			return "Exactly 2 atoms are required; " + matcher.getNumAtoms()
					+ " found";
		}
		if (matcher.getNumBonds() != 1) {
			return "Exactly 1 bond is required; " + matcher.getNumBonds()
					+ " found";
		}

		if (matcher.getBondWithIdx(0).getBondType() != BondType.SINGLE) {
			// qb.delete();
			if (!(SMARTS.contains("!=") && SMARTS.contains("!#"))) {
				// Allow for !=!# designations
				return "Bond between atoms must be single; "
						+ matcher.getBondWithIdx(0).getBondType() + " found";
			}
		}
		if (!matcher.getRingInfo().isInitialized()) {
			matcher.getRingInfo().initialize();
		}
		if (matcher.getRingInfo().numBondRings(0) > 0) {
			return "Bond must be acyclic";
		}

		return null;
	}

	@Override
	public MoleculeFragmentationFactory2<RWMol, ROMol> createFragmentationFactory(
			RWMol mol, ROMol bondMatch, boolean stripHsAtEnd, boolean isHAdded,
			boolean verboseLog, boolean prochiralAsChiral, Integer maxNumVarAtm,
			Double minCnstToVarAtmRatio, int maxLeafCacheSize)
			throws ClosedFactoryException, ToolkitException {
		return new RWMolFragmentationFactory(mol, bondMatch, stripHsAtEnd,
				isHAdded, verboseLog, prochiralAsChiral, maxNumVarAtm,
				minCnstToVarAtmRatio, maxLeafCacheSize);
	}

	@Override
	public boolean hasExtendedFingerprintOptions() {
		return true;
	}

	@Override
	public String getToolkitName() {
		return "RDKit";
	}

	@Override
	public DataType getRendererType() {
		return RDKIT_RENDERER_CELLTYPE;
	}

	@Override
	public Class<? extends DataValue>[] getInputColumnTypes() {
		return MolFormats.m_RDKitmolFormats.toArray(new Class[0]);
	}

	@Override
	public ChemicalReaction generateReactionFromRSmarts(String rSMARTS,
			boolean allowAdditionalSubstitutionPositions, long rowIndex)
			throws ToolkitException {
		ChemicalReaction rxn = null;
		try {

			if (!allowAdditionalSubstitutionPositions) {
				// We are going to have to dismantle the reactants and product 1
				// by one, add Hs and re-assemble them
				StringBuilder sb = new StringBuilder();
				boolean isFirst = true;
				for (String component : rSMARTS.split(">>")[0].split("\\.")) {
					RWMol comp = RWMol.MolFromSmarts(component);
					RDKFuncs.sanitizeMol(comp,
							SanitizeFlags.SANITIZE_NONE.swigValue());
					RDKFuncs.addHs(comp);
					String component2 = RDKFuncs.MolToSmarts(comp, true);
					comp.delete();
					comp = RWMol.MolFromSmarts(component2, 0, true);
					if (!isFirst) {
						sb.append(".");
					} else {
						isFirst = false;
					}
					sb.append(RDKFuncs.MolToSmarts(comp, true));
					comp.delete();
				}
				sb.append(">>").append(rSMARTS.split(">>")[1]);
				rxn = ChemicalReaction.ReactionFromSmarts(sb.toString(), false);
				// rxn = new ChemicalReaction(sb.toString());
			} else {
				rxn = ChemicalReaction.ReactionFromSmarts(rSMARTS, false);
			}
		} catch (ChemicalReactionException e) {
			throw new ToolkitException(e.what(), e);
		} catch (ChemicalReactionParserException e) {
			throw new ToolkitException(e.what(), e);
		} catch (GenericRDKitException e) {
			throw new ToolkitException(e.what(), e);
		}
		return m_SWIGGC.markForCleanup(rxn, rowIndex);

	}

	@Override
	public String getSMARTSFromMolecule(ROMol queryMol)
			throws ToolkitException {
		return RDKFuncs.MolToSmarts(queryMol, true);
	}

	@Override
	public ROMol addHsToQueryMolecule(ROMol queryMol, long rowIndex)
			throws ToolkitException {
		return m_SWIGGC.markForCleanup(queryMol.addHs(false), rowIndex);
	}

	@Override
	public boolean molMatchesQuery(RWMol mol, ROMol matcher) {
		return mol.hasSubstructMatch(matcher);
	}

	@Override
	public List<Set<Integer>> getMatchAtomSets(RWMol mol, ROMol matcher) {
		List<Set<Integer>> retVal = new ArrayList<>();
		Match_Vect_Vect matches = mol.getSubstructMatches(matcher, false, true);
		for (int i = 0; i < matches.size(); i++) {
			Match_Vect match = matches.get(i);
			Set<Integer> matchAtoms = new TreeSet<>();
			for (int j = 0; j < match.size(); j++) {
				matchAtoms.add(match.get(j).getSecond());
			}
			retVal.add(matchAtoms);
		}
		matches.delete();
		return retVal;
	}

	@Override
	public List<DenseBitVector[]> getEnvironmentFPs(RWMol mol,
			ChemicalReaction toLeafTransform, long index, int fpLength,
			int radius, boolean useBondTypes, boolean useChirality,
			boolean concatenate) throws ToolkitException {
		ROMol_Vect reactant = new ROMol_Vect();
		reactant.add(mol);
		ROMol_Vect_Vect leafProds = toLeafTransform.runReactants(reactant);

		List<DenseBitVector[]> retVal = new ArrayList<>();
		for (int i = 0; i < leafProds.size(); i++) {
			ROMol_Vect leafs = leafProds.get(i);
			DenseBitVector[] fps = new DenseBitVector[(int) leafs.size()];
			for (int j = 0; j < leafs.size(); j++) {
				RWMol leaf = new RWMol(leafs.get(j));
				String leafSmi = leaf.MolToSmiles(true);
				try {
					fps[getAttachmentPointIndex(leafSmi) - 1] =
							getLeafFingerprint(leaf, fpLength, radius,
									useBondTypes, useChirality);
				} catch (ToolkitException e) {
					reactant.delete();
					leafProds.delete();
					throw e;
				} finally {
					leaf.delete();
				}
			}
			if (concatenate) {
				DenseBitVector dbv = fps[0];
				for (int j = 1; j < fps.length; j++) {
					dbv = dbv.concatenate(fps[j]);
				}
				retVal.add(new DenseBitVector[] { dbv });
			} else {
				retVal.add(fps);
			}
		}

		reactant.delete();
		leafProds.delete();
		return retVal;
	}

	@Override
	public DenseBitVector getLeafFingerprint(RWMol mol, int fpLength,
			int radius, boolean useBondTypes, boolean useChirality)
			throws ToolkitException {

		UInt_Vect apIdx = new UInt_Vect();
		ExplicitBitVect ebv;
		Match_Vect matches = null;
		Int_Pair pair = null;
		try {
			RDKFuncs.sanitizeMol(mol);
			// Get the atom ID of the attachment point
			matches = mol.getSubstructMatch(
					MatchedPairsMultipleCutsNodePlugin.AP_QUERY_MOL);
			pair = matches.get(0);
			apIdx.add(pair.getSecond());
			ebv = RDKFuncs.getMorganFingerprintAsBitVect(mol, radius, fpLength,
					null, apIdx, useChirality, useBondTypes);
		} catch (MolSanitizeException e) {
			throw new ToolkitException(e.what(), e);
		} catch (GenericRDKitException e) {
			throw new ToolkitException(e.what(), e);
		} finally {
			apIdx.delete();
			if (pair != null) {
				pair.delete();
			}
			if (matches != null) {
				matches.delete();
			}
		}
		DenseBitVector dbv = new DenseBitVector(ebv.getNumBits());
		Int_Vect onBits = ebv.getOnBits();
		for (int i = 0; i < onBits.size(); i++) {
			dbv.set(onBits.get(i), true);
		}
		onBits.delete();
		ebv.delete();
		return dbv;
	}

	@Override
	public Set<String> getTransformedMoleculesSmiles(RWMol mol,
			ChemicalReaction transform, Set<Integer> okMatchAtoms,
			boolean cleanChirality, long index) throws ToolkitException {
		Set<String> retVal = new HashSet<>();
		for (int i = 0; i < mol.getNumAtoms(); i++) {
			if (!okMatchAtoms.contains(i)) {
				mol.getAtomWithIdx(i).setProp("_protected", "1");
			}
		}

		ROMol_Vect reactant = new ROMol_Vect();
		reactant.add(mol);
		ROMol_Vect_Vect prods = transform.runReactants(reactant);
		for (int i = 0; i < prods.size(); i++) {
			ROMol prod = prods.get(i).get(0);
			if (cleanChirality) {
				RWMol prodrw = new RWMol(prod);
				try {
					prodrw.sanitizeMol();
				} catch (MolSanitizeException e) {
					prodrw.delete();
					prod.delete();
					prods.delete();
					reactant.delete();
					throw new ToolkitException(e.what(), e);
				}
				RDKFuncs.assignStereochemistry(prodrw, true, true);
				prodrw.Kekulize();
				retVal.add(prodrw.MolToSmiles(true, true));
				prodrw.delete();
			} else {
				retVal.add(prod.MolToSmiles(true, true));
			}
			prod.delete();
		}
		prods.delete();
		reactant.delete();
		return retVal;
	}

	@Override
	public ROMol generateQueryMoleculeFromSMARTS(String SMARTS, long rowIndex) {
		RWMol comp = RWMol.MolFromSmarts(SMARTS);
		RDKFuncs.sanitizeMol(comp, SanitizeFlags.SANITIZE_NONE.swigValue());
		RDKFuncs.addHs(comp);
		String component2 = RDKFuncs.MolToSmarts(comp, true);
		comp.delete();
		return m_SWIGGC.markForCleanup(RWMol.MolFromSmarts(component2, 0, true),
				rowIndex);
	}

}
