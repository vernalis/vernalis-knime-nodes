/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.nodes.build;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import com.vernalis.pdbconnector2.query.RCSBQueryModel;
import com.vernalis.pdbconnector2.query.chemical.ChemicalQueryPane;
import com.vernalis.pdbconnector2.query.sequence.SequenceMotifQueryPane;
import com.vernalis.pdbconnector2.query.sequence.SequenceQueryPane;
import com.vernalis.pdbconnector2.query.structsim.StructureSimilarityQueryPane;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupPanel;

import static com.vernalis.pdbconnector2.PdbConnector2Helpers.SCROLL_SPEED;

/**
 * The {@link NodeDialogPane} implemenation for the PDB Connector Query Builder
 * node
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class PdbConnector2QueryNodeDialog extends NodeDialogPane {

	private final RCSBQueryModel model = new RCSBQueryModel();

	/**
	 * Constructor
	 */
	public PdbConnector2QueryNodeDialog() {

		final QueryGroupPanel comp = new QueryGroupPanel(model.getTextModel());
		super.addTab("Text / Attribute Query", comp, false);
		comp.setMaximumSize(comp.getParent().getSize());

		final JPanel sequenceTab = new JPanel();
		sequenceTab.setLayout(new BoxLayout(sequenceTab, BoxLayout.Y_AXIS));
		final JScrollPane sequenceScrollPane = new JScrollPane(sequenceTab);
		sequenceScrollPane.getVerticalScrollBar()
				.setUnitIncrement(SCROLL_SPEED);
		super.addTab("Structure & Sequence Query", sequenceScrollPane);
		sequenceTab.add(new SequenceMotifQueryPane(model.getSeqMotifModel()));
		final SequenceQueryPane sqp =
				new SequenceQueryPane(model.getSeqModel());
		sequenceTab.add(sqp);
		final StructureSimilarityQueryPane ssqp =
				new StructureSimilarityQueryPane(model.getStructSimModel());
		ssqp.setMaximumSize(new Dimension(sqp.getMaximumSize().width,
				ssqp.getMaximumSize().height));
		sequenceTab.add(ssqp);

		final JPanel chemTab = new JPanel();
		chemTab.setLayout(new BoxLayout(chemTab, BoxLayout.Y_AXIS));
		final JScrollPane chemScrollPane = new JScrollPane(chemTab);
		chemScrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
		super.addTab("Chemical Query", chemScrollPane);

		final ChemicalQueryPane chemQP =
				new ChemicalQueryPane(3, model.getChemicalQueryModel());
		chemQP.setMaximumSize(new Dimension(sqp.getMaximumSize().width,
				chemQP.getMaximumSize().height));
		chemTab.add(chemQP);

	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings,
			DataTableSpec[] specs) throws NotConfigurableException {

		try {
			model.loadSettings(settings);
		} catch (final InvalidSettingsException e) {
			// Ignore errors here - the NodeModel deals with that and we just
			// skip and stick
			// to defaults....
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings)
			throws InvalidSettingsException {
		model.saveSettingsTo(settings);
	}
}
