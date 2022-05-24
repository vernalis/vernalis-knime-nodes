/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.rdkit.scaffoldkeys.rdkit;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.RDKit.Atom;
import org.RDKit.Atom_Vect;
import org.RDKit.ColourPalette;
import org.RDKit.DrawColour;
import org.RDKit.Int_Vect;
import org.RDKit.Int_Vect_Vect;
import org.RDKit.Match_Vect;
import org.RDKit.Match_Vect_Vect;
import org.RDKit.MolDraw2DSVG;
import org.RDKit.MolDrawOptions;
import org.RDKit.RDKFuncs;
import org.RDKit.ROMol;
import org.RDKit.RWMol;
import org.RDKit.RingInfo;

import com.vernalis.knime.chem.rdkit.scaffoldkeys.abstrct.AbstractScaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.DepictionAtomSet;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;

/**
 * An RDKit implementation of {@link Scaffold}. Off-heap memory should be
 * released by calling {@link #delete()} once the object is finished with
 *
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public class ROMolScaffold extends AbstractScaffold<ROMol>
		implements Scaffold<ROMol> {

	private ROMolScaffold(ROMol mol) {
		super(mol);

	}

	@Override
	public Set<BitSet> findRings(ROMol mol) {
		RingInfo ri = mol.getRingInfo();
		Int_Vect_Vect atomRings = ri.atomRings();

		Set<BitSet> retVal = new LinkedHashSet<>();
		for (int ringIdx = 0; ringIdx < atomRings.size(); ringIdx++) {
			BitSet ring = new BitSet((int) mol.getNumAtoms());
			Int_Vect atomRing = atomRings.get(ringIdx);
			for (int aIdx = 0; aIdx < atomRing.size(); aIdx++) {
				ring.set(atomRing.get(aIdx));
			}
			atomRing.delete();
			retVal.add(ring);
		}
		atomRings.delete();
		ri.delete();
		return retVal;
	}

	/**
	 * Factory method to create an {@link ROMolScaffold} from an RDKit
	 * {@link ROMol}.
	 *
	 * @param mol
	 *            The molecule to create from. The caller is responsible for
	 *            disposal of this object
	 * @param isMurckoScaffold
	 *            TODO
	 *
	 * @return a scaffold
	 *
	 * @since v1.34.0
	 */
	public static ROMolScaffold fromMolecule(ROMol mol,
			boolean isMurckoScaffold) {
		RWMol tmp2;
		if (!isMurckoScaffold) {
			ROMol tmp = RDKFuncs.MurckoDecompose(mol);
			tmp2 = new RWMol(tmp);
			RDKFuncs.sanitizeMol(tmp2);
			tmp.delete();
		} else {
			tmp2 = new RWMol(mol);
		}
		return new ROMolScaffold(tmp2);
	}

	@Override
	public void delete() {
		if (getScaffoldMolecule() != null) {
			getScaffoldMolecule().delete();
		}
		super.delete();
	}

	@Override
	public String getCanonicalSMILES() {
		return getScaffoldMolecule().MolToSmiles();
	}

	@Override
	public ROMol getScaffoldMolecule() throws IllegalStateException {

		final ROMol scaffoldMolecule = super.getScaffoldMolecule();
		if (scaffoldMolecule == null) {
			throw new IllegalStateException("Query molecule has been disposed");
		}
		return scaffoldMolecule;
	}

	@Override
	public BitSet getAllAtomsMatchingSMARTS(final String smarts,
			int... indicesToKeep) throws IllegalStateException {
		RWMol queryMol = RWMol.MolFromSmarts(smarts);
		BitSet matchingAtoms =
				getAllAtomsMatchingQueryMolecule(queryMol, indicesToKeep);
		queryMol.delete();
		return matchingAtoms;
	}

	@Override
	public Collection<BitSet> getAllQueryMoleculeMatches(ROMol queryMol,
			boolean uniquify, int... indicesToKeep) {
		Match_Vect_Vect mvv =
				getScaffoldMolecule().getSubstructMatches(queryMol, uniquify);
		Collection<BitSet> matches =
				uniquify ? new LinkedHashSet<>() : new ArrayList<>();
		for (int i = 0; i < mvv.size(); i++) {
			Match_Vect m0 = mvv.get(i);
			BitSet m = new BitSet((int) getScaffoldMolecule().getNumAtoms());
			if (indicesToKeep.length == 0) {
				for (int j = 0; j < m0.size(); j++) {
					m.set(m0.get(j).getSecond());
				}
			} else {
				for (int j : indicesToKeep) {
					m.set(m0.get(j).getSecond());
				}
			}
			matches.add(m);
		}
		mvv.delete();
		return matches;
	}

	@Override
	public Collection<BitSet> getAllSMARTSMatches(String smarts,
			boolean uniquify, int... indicesToKeep) {
		RWMol queryMol = RWMol.MolFromSmarts(smarts);
		Collection<BitSet> matches =
				getAllQueryMoleculeMatches(queryMol, uniquify, indicesToKeep);
		queryMol.delete();
		return matches;
	}

	@Override
	public int getAtomCount() {
		return (int) getScaffoldMolecule().getNumAtoms();
	}

	@Override
	protected BitSet calculateAtomNeighbours(int atomIndex) {
		BitSet retVal = new BitSet(getAtomCount());
		Atom at = getScaffoldMolecule().getAtomWithIdx(atomIndex);
		Atom_Vect neighbours = getScaffoldMolecule().getAtomNeighbors(at);
		for (int i = 0; i < neighbours.size(); i++) {
			final Atom aNbr = neighbours.get(i);
			retVal.set((int) aNbr.getIdx());
			aNbr.delete();
		}
		at.delete();
		neighbours.delete();
		return retVal;
	}

	@Override
	public boolean canDepict() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String depict(Collection<DepictionAtomSet> highlights)
			throws UnsupportedOperationException {

		ROMol mol = getScaffoldMolecule();
		// Ensure we have a layout for depiction
		if (mol.getNumConformers() == 0) {
			mol.compute2DCoords();
		}

		// Now get the Atom and bond indices to highlight
		Int_Vect atoms = new Int_Vect();
		ColourPalette atomColours = new ColourPalette();
		Int_Vect bonds = new Int_Vect();
		ColourPalette bondColours = new ColourPalette();
		for (DepictionAtomSet highlight : highlights) {

			DrawColour col = new DrawColour(
					highlight.getHighlightColour().getRed() / 255.0,
					highlight.getHighlightColour().getGreen() / 255.0,
					highlight.getHighlightColour().getBlue() / 255.0,
					highlight.getHighlightColour().getAlpha() / 255.0);

			highlight.getHighlightedAtoms().stream().forEach(i -> {
				atomColours.set(i, col);
				atoms.add(i);
			});
			if (highlight.highlightConnectingBonds()) {
				highlight.getHighlightedAtoms().stream().boxed()
						.flatMap(i -> highlight.getHighlightedAtoms().stream()
								.filter(j -> j > i)
								.mapToObj(j -> mol.getBondBetweenAtoms(i, j)))
						.filter(Objects::nonNull).mapToInt(b -> {
							int bIdx = (int) b.getIdx();
							b.delete();
							return bIdx;
						}).forEach(i -> {
							bondColours.set(i, col);
							bonds.add(i);
						});
			}
			col.delete();
		}

		// Some basic settings
		MolDraw2DSVG d2d = new MolDraw2DSVG(300, 300);
		MolDrawOptions dOpts = d2d.drawOptions();
		DrawColour black = new DrawColour(0.0, 0.0, 0.0);
		final ColourPalette atomColourPalette = new ColourPalette() {

			@Override
			public DrawColour get(int arg0) {
				// Colour all atoms in black
				return black;
			}

		};
		dOpts.setAtomColourPalette(atomColourPalette);
		dOpts.setAddStereoAnnotation(true);
		d2d.drawMolecule(mol, "", atoms, bonds, atomColours, bondColours);
		d2d.finishDrawing();
		String retVal = d2d.getDrawingText().replace("svg:", "")
				.replace("xmlns:svg=", "xmlns=");
		d2d.delete();
		dOpts.delete();
		atomColourPalette.delete();
		black.delete();
		atoms.delete();
		atomColours.delete();
		bonds.delete();
		bondColours.delete();
		return retVal;
	}

}
