/*******************************************************************************
 * Copyright (c) 2017,2021 Vernalis (R&D) Ltd
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
package com.vernalis.knime.mmp.frags.rdkit;

import java.util.Collection;

import org.RDKit.RDKFuncs;
import org.RDKit.ROMol_Vect;
import org.RDKit.RWMol;

import com.vernalis.knime.mmp.ToolkitException;
import com.vernalis.knime.mmp.fragmentors.MoleculeFragmentationException;
import com.vernalis.knime.mmp.frags.abstrct.AbstractFragmentValue;
import com.vernalis.knime.mmp.frags.abstrct.AbstractLeaf;
import com.vernalis.knime.mmp.frags.abstrct.AbstractMulticomponentFragmentationParser;
import com.vernalis.knime.swiggc.SWIGObjectGarbageCollector2WaveSupplier;

/**
 * RDKit toolkit implementation of
 * {@link AbstractMulticomponentFragmentationParser}
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class RWMolMulticomponentFragmentationParser
		extends AbstractMulticomponentFragmentationParser<RWMol> {
	private SWIGObjectGarbageCollector2WaveSupplier gc = null;

	/**
	 * @see AbstractMulticomponentFragmentationParser#AbstractMulticomponentFragmentationParser(Object,
	 *      Collection)
	 */
	public RWMolMulticomponentFragmentationParser(RWMol value, Collection<RWMol> keyComponents)
			throws MoleculeFragmentationException, ToolkitException {
		super(value, keyComponents);
	}

	/**
	 * @see AbstractMulticomponentFragmentationParser#AbstractMulticomponentFragmentationParser(Object,
	 *      Object...)
	 */
	public RWMolMulticomponentFragmentationParser(RWMol value, RWMol... keyComponents)
			throws MoleculeFragmentationException, ToolkitException {
		super(value, keyComponents);
		getGc().cleanupMarkedObjects();
	}

	@Override
	protected RWMolFragmentKey createFragmentKey() {
		return new RWMolFragmentKey();
	}

	@Override
	protected AbstractLeaf<RWMol> createLeafFromMolecule(RWMol comp)
			throws IllegalArgumentException, ToolkitException {
		return new RWMolLeaf(comp);
	}

	@Override
	protected AbstractFragmentValue<RWMol>
			createValueFromMolecule(RWMol value) {
		return new RWMolFragmentValue(value, null);
	}

	@Override
	protected RWMol[] getComponentsFromMultiComponent(RWMol multiCompMol) {
		ROMol_Vect comps =
				getGc().markForCleanup(RDKFuncs
						.getMolFrags(multiCompMol, false/* sanitize frags */,
								null/* frags to mol mapping */,
								null/* Atoms in each frag mapping */,
								false/* copy confs */),
						1L);
		RWMol[] retVal = new RWMol[(int) comps.size()];
		for (int i = 0; i < comps.size(); i++) {
			retVal[i] = (RWMol) getGc().markForCleanup(comps.get(i), 1L);
		}
		return retVal;
	}

	@Override
	protected boolean isMultiComponent(RWMol mol) {
		return mol.MolToSmiles().contains(".");
	}

	private SWIGObjectGarbageCollector2WaveSupplier getGc() {
		if (gc == null) {
			gc = new SWIGObjectGarbageCollector2WaveSupplier();
		}
		return gc;
	}

}
