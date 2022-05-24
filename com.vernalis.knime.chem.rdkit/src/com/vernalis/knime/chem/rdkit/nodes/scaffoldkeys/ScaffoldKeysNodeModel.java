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
/**
 * 
 */
package com.vernalis.knime.chem.rdkit.nodes.scaffoldkeys;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.RDKit.ROMol;
import org.knime.base.data.xml.SvgCellFactory;
import org.knime.chem.types.SmilesAdapterCell;
import org.knime.chem.types.SmilesCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColor;

import com.vernalis.exceptions.RowExecutionException;
import com.vernalis.knime.chem.rdkit.RdkitCompatibleColumnFormats;
import com.vernalis.knime.chem.rdkit.nodes.abstrct.AbstractVerRDKitColumnRearrangerNodeModel;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.Scaffold;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKey;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.api.ScaffoldKeysFactory;
import com.vernalis.knime.chem.rdkit.scaffoldkeys.rdkit.ROMolRevisedScaffoldKeysFactory;
import com.vernalis.knime.streams.DataCellCollectors;

import static com.vernalis.knime.chem.rdkit.nodes.scaffoldkeys.ScaffoldKeysNodeDialog.createDepictModel;
import static com.vernalis.knime.chem.rdkit.nodes.scaffoldkeys.ScaffoldKeysNodeDialog.createHighlightColourModel;
import static com.vernalis.knime.chem.rdkit.nodes.scaffoldkeys.ScaffoldKeysNodeDialog.createIsMurckoScaffoldModel;

/**
 * @author S.Roughley knime@vernalis.com
 *
 *
 * @since v1.34.0
 */
public class ScaffoldKeysNodeModel
		extends AbstractVerRDKitColumnRearrangerNodeModel<Scaffold<ROMol>> {

	private final SettingsModelBoolean isMurckoScaffoldMdl =
			registerSettingsModel(createIsMurckoScaffoldModel());
	private ScaffoldKeysFactory<ROMol> treeFactory;
	private final SettingsModelBoolean depictKeysMdl;
	private final SettingsModelColor highlightColourMdl;

	/**
	 *
	 * @since v1.34.0
	 */
	public ScaffoldKeysNodeModel() {
		super(null, null, RdkitCompatibleColumnFormats.MOL_ANY, 2);
		treeFactory = ROMolRevisedScaffoldKeysFactory.getInstance();
		if (treeFactory.canDepict()) {
			depictKeysMdl = registerSettingsModel(createDepictModel(), 2,
					mdl -> mdl.setBooleanValue(false));
			// This one doesnt really matter as in legacy mode it will be
			// ignored, but this stops it throwing an error on load
			highlightColourMdl =
					registerSettingsModel(createHighlightColourModel(), 2,
							mdl -> mdl.setColorValue(Color.RED));
			depictKeysMdl.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					highlightColourMdl
							.setEnabled(depictKeysMdl.getBooleanValue());

				}
			});
			highlightColourMdl.setEnabled(depictKeysMdl.getBooleanValue());

		} else {
			depictKeysMdl = null;
			highlightColourMdl = null;
		}
	}

	@Override
	protected String checkSettings(DataTableSpec spec) {

		String retVal = super.checkSettings(spec);

		Map<String, DataType> newCols = new LinkedHashMap<>();
		if (!isMurckoScaffoldMdl.getBooleanValue()) {
			newCols.put("Bemis-Murcko Scaffold", SmilesAdapterCell.RAW_TYPE);
		}

		newCols.put("nAtoms", IntCell.TYPE);

		List<ScaffoldKey<ROMol>> keys = treeFactory.getScaffoldKeys();
		m_columnProperties.clear();
		for (ScaffoldKey<ROMol> key : keys) {
			newCols.put(key.getName(), IntCell.TYPE);
			TreeMap<String, String> props = new TreeMap<>();
			props.put("Key #", String.format("%d", key.getIndex()));
			props.put("Description", key.getDescription());
			m_columnProperties.put(key.getName(), props);

			if (treeFactory.canDepict() && depictKeysMdl.getBooleanValue()
					&& key.canDepict()) {
				newCols.put(key.getName() + " (SVG)", key.isSingletonDepiction()
						? SvgCellFactory.TYPE
						: ListCell.getCollectionType(SvgCellFactory.TYPE));
			}
		}

		m_newColNames = newCols.keySet().toArray(new String[newCols.size()]);
		m_newColTypes = newCols.values().toArray(new DataType[newCols.size()]);

		return retVal;
	}

	@Override
	protected DataCell[] getResultsFromRDKitObject(Scaffold<ROMol> mol,
			long wave) throws RowExecutionException {

		List<DataCell> retVal = new ArrayList<>();

		if (!isMurckoScaffoldMdl.getBooleanValue()) {
			retVal.add(SmilesCellFactory
					.createAdapterCell(mol.getCanonicalSMILES()));
		}

		retVal.add(new IntCell(mol.getAtomCount()));

		List<ScaffoldKey<ROMol>> keys = treeFactory.getScaffoldKeys();
		for (ScaffoldKey<ROMol> key : keys) {
			final int keyVal = key.calculateForScaffold(mol);
			retVal.add(new IntCell(keyVal));
			if (treeFactory.canDepict() && depictKeysMdl.getBooleanValue()
					&& key.canDepict()) {

				DataCell depictCell;
				if (keyVal == 0) {
					depictCell = DataType.getMissingCell();
				} else {
					final String[] svgs = key.depictForScaffold(mol,
							highlightColourMdl.getColorValue());
					if (svgs == null || svgs.length == 0) {
						depictCell = DataType.getMissingCell();
					} else {
						if (key.isSingletonDepiction()) {
							final String svg = svgs[0];
							try {
								depictCell = SvgCellFactory.create(svg);
							} catch (IOException e) {
								getLogger().warn(
										"Error creating SVG Cell for svg '"
												+ svg + "' - "
												+ e.getMessage());
								depictCell = new MissingCell(e.getMessage());
							}
						} else {
							depictCell = Arrays.stream(svgs).map(svg -> {
								try {
									return SvgCellFactory.create(svg);
								} catch (IOException e) {
									getLogger().warn(
											"Error creating SVG Cell for svg '"
													+ svg + "' - "
													+ e.getMessage());
									return new MissingCell(e.getMessage());
								}
							}).collect(DataCellCollectors.toListCell());
						}
					}
				}
				retVal.add(depictCell);
			}
		}

		return retVal.toArray(new DataCell[retVal.size()]);

	}

	@Override
	protected Scaffold<ROMol> getRDKitObjectFromCell(DataCell cell, long wave)
			throws RowExecutionException {

		ROMol mol = getGC().markForCleanup(RdkitCompatibleColumnFormats
				.getRDKitObjectFromCell(cell, true, true), wave);

		return getGC().markForCleanup(treeFactory.getScaffoldFromMolecule(mol,
				isMurckoScaffoldMdl.getBooleanValue()), wave);
	}

}
