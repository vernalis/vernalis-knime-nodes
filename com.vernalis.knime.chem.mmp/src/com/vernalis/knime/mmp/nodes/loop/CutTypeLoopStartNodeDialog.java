/*******************************************************************************
 * Copyright (c) 2015, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp.nodes.loop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.vernalis.knime.mmp.FragmentationTypes;

/**
 * The node dialog panel for the Cut type loop start node
 * 
 * @author s.roughley {@literal <knime@vernalis.com>}
 * 
 */
public class CutTypeLoopStartNodeDialog extends DefaultNodeSettingsPane {
	/**
	 * Constructor for the Cut Type loop start node dialog pane
	 */
	public CutTypeLoopStartNodeDialog() {
		super();
		addDialogComponent(new DialogComponentStringListSelection(
				createSelectedCutTypesModel(), "Bond match types",
				createCutTypesList(), true, createCutTypesList().size()));
	}

	/**
	 * @return The settings model for the selected cut types
	 */
	public static SettingsModelStringArray createSelectedCutTypesModel() {
		return new SettingsModelStringArray("Cut Types", createCutTypesList()
				.toArray(new String[0]));
	}

	/**
	 * @return A {@link List} of the action commands of the possible cut types
	 */
	public static Collection<String> createCutTypesList() {
		List<String> retVal = new ArrayList<>();
		for (FragmentationTypes frag : FragmentationTypes.values()) {
			if (frag != FragmentationTypes.USER_DEFINED) {
				retVal.add(frag.getActionCommand());
			}
		}
		return retVal;
	}
}
