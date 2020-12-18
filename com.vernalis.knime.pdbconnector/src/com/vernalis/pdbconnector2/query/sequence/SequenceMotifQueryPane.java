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
package com.vernalis.pdbconnector2.query.sequence;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;

/**
 * {@link AbstractSequenceQueryPane} implementation for the Sequence Motif query
 * type, backed by a {@link SequenceMotifQueryModel}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SequenceMotifQueryPane
		extends AbstractSequenceQueryPane<SequenceMotifQueryModel> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with no {@link FlowVariableModel} button for the sequenc
	 * 
	 * @param model
	 *            the query model
	 */
	public SequenceMotifQueryPane(SequenceMotifQueryModel model) {
		this(model, null);
	}

	/**
	 * Full constructor
	 * 
	 * @param model
	 *            The query model
	 * @param fvm
	 *            The option {@link FlowVariableModel} for the sequence string
	 */
	public SequenceMotifQueryPane(SequenceMotifQueryModel model,
			FlowVariableModel fvm) {
		super(3, model, "Sequence motif", fvm);
		final JPanel componentPanel = new DialogComponentStringSelection(
				getQueryModel().getModeModel(), "Mode",
				Arrays.stream(SequenceMotifType.values()).map(x -> x.getText())
						.collect(Collectors.toList())).getComponentPanel();
		componentPanel.setMinimumSize(getPreferredSize());
		componentPanel.setMaximumSize(getPreferredSize());
		getOptionsBox().add(componentPanel);
		getOptionsBox().add(createHorizontalGlue());
	}

}
