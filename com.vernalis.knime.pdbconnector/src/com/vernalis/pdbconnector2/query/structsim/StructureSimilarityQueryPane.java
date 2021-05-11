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
package com.vernalis.pdbconnector2.query.structsim;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

import com.vernalis.pdbconnector2.dialogcomponents.DialogComponentStringSuggester;
import com.vernalis.pdbconnector2.dialogcomponents.suggester.RcsbUrlSuggester;
import com.vernalis.pdbconnector2.dialogcomponents.swing.CountClearButtonBox;
import com.vernalis.pdbconnector2.dialogcomponents.swing.SwingUtils;
import com.vernalis.pdbconnector2.query.QueryPanel;
import com.vernalis.pdbconnector2.query.structsim.StructureSimilarityQueryModel.EventType;

/**
 * An implementation of {@link QueryPanel} based on a Swing {@link Box},
 * representing a Structure similarity query input
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class StructureSimilarityQueryPane extends Box
		implements ChangeListener, QueryPanel<StructureSimilarityQueryModel> {

	private static final long serialVersionUID = 1L;

	private final StructureSimilarityQueryModel model;

	private final DialogComponentStringSelection matchUnitTypeDiaC;

	private final DialogComponentStringSelection matchUnitIdDiaC;

	private final CountClearButtonBox countButtons;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the {@link StructureSimilarityQueryModel} for the query
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StructureSimilarityQueryPane(StructureSimilarityQueryModel model) {
		super(BoxLayout.X_AXIS);
		setBorder(BorderFactory.createCompoundBorder(
				new TitledBorder(new EtchedBorder(), "Structure Similarity"),
				new EmptyBorder(5, 5, 5, 0)));
		this.model = model;
		this.model.addChangeListener(this);
		countButtons = new CountClearButtonBox(this);

		add(SwingUtils.forceToPreferredSize(new DialogComponentStringSuggester(
				model.getPdbIdModel(), "PDB ID",
				RcsbUrlSuggester.get(
						"https://www.rcsb.org/search/suggester/rcsb_entry_container_identifiers.entry_id/%s"),
				1, 4)));
		matchUnitTypeDiaC = new DialogComponentStringSelection(
				model.getMatchUnitTypeModel(), "",
				model.getPossibleMatchUnits());
		final JPanel cp0 = matchUnitTypeDiaC.getComponentPanel();
		cp0.setMinimumSize(cp0.getPreferredSize());
		cp0.setMaximumSize(cp0.getPreferredSize());
		add(cp0);
		matchUnitIdDiaC = new DialogComponentStringSelection(
				model.getMatchUnitIdModel(), "", model.getPossibleIDs());
		final JPanel cp1 = matchUnitIdDiaC.getComponentPanel();
		for (final Component c : cp1.getComponents()) {
			if (c instanceof JComboBox) {
				((JComboBox) c).setPrototypeDisplayValue("XXX");
				break;
			}
		}
		SwingUtils.forceToPreferredSize(cp1);
		add(cp1);

		add(SwingUtils.forceToPreferredSize(
				new DialogComponentButtonGroup(model.getSimilarityTypeModel(),
						null, false, StructureSimilarityType.values())));
		add(countButtons);
		add(createGlue());

		setPreferredSize(new Dimension(
				getPreferredSize().width + getInsets().left + getInsets().right,
				cp1.getPreferredSize().height + getInsets().top
						+ getInsets().bottom));
		// Force it to fill the width of any reasonable sized dialog but
		// maintain
		// correct height
		SwingUtils.keepHeightFillWidth(this);

	}

	@Override
	public void stateChanged(ChangeEvent e) {
		switch ((EventType) e.getSource()) {
			case PDBID_CHANGE:
				// Enabled status changes are handled in the model
				// Update the types dropdown list
				if (model.hasQuery()) {
					matchUnitTypeDiaC.replaceListItems(
							model.getPossibleMatchUnits(),
							model.getMatchUnitType().getText());
				}
				// And Keep going...
			case MATCH_UNIT_TYPE_CHANGE:
				// Update the IDs dropdown list
				if (model.hasQuery()) {
					matchUnitIdDiaC.replaceListItems(model.getPossibleIDs(),
							model.getMatchUnitID());
				}
				// And keep going...
			case MATCH_UNIT_ID_CHANGE:
			case SIMILARITY_TYPE_CHANGE:
				// reset the count and clear buttons...
				resetCountClearButtons();

		}

	}

	@Override
	public CountClearButtonBox getButtons() {
		return countButtons;
	}

	@Override
	public StructureSimilarityQueryModel getQueryModel() {
		return model;
	}

}
