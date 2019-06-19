/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.io.nodes.abstrct;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.ListSelectionModel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import com.vernalis.io.FileHelpers.LineBreak;
import com.vernalis.io.MultilineTextObject;

/**
 * Node dialog base class for the Load ... Files nodes
 * @author s.roughley
 *
 */
public class AbstractMultiLineObjectLoadFilesNodeDialog<T extends MultilineTextObject>
		extends AbstractLoadFilesNodeDialog {

	private static final String PROPERTIES = "Properties";
	private static final String NEWLINE_OUTPUT = "Newline output";
	protected final T nonReadableObject;
	protected final DialogComponentStringListSelection propertiesDlg;

	public AbstractMultiLineObjectLoadFilesNodeDialog(T nonReadableObject,
			String historyID, String... fileTypes) {
		super(historyID, fileTypes);
		this.nonReadableObject = nonReadableObject;
		DialogComponentButtonGroup lbDlg =
				new DialogComponentButtonGroup(createNewlineModel(),
						NEWLINE_OUTPUT, false, LineBreak.values());
		lbDlg.setToolTipText("Linebreak behaviour in multi-line output");
		addDialogComponent(lbDlg);
		final DataColumnSpec[] newColumnSpecs =
				this.nonReadableObject.getNewColumnSpecs();
		if (getNumProperties(newColumnSpecs) > 1) {
			propertiesDlg = new DialogComponentStringListSelection(
					createPropertySelectionModel(nonReadableObject), PROPERTIES,
					Arrays.stream(newColumnSpecs)
							.map(colSpec -> colSpec.getName())
							.collect(Collectors.toList()),
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true,
					Math.min(getNumProperties(newColumnSpecs), 12));
			setHorizontalPlacement(true);
			addDialogComponent(propertiesDlg);
		} else {
			propertiesDlg = null;
		}
	}

	/**
	 * Method to return the number of properties
	 * @param newColumnSpecs
	 * @return
	 */
	protected int getNumProperties(final DataColumnSpec[] newColumnSpecs) {
		return newColumnSpecs.length;
	}

	static <T extends MultilineTextObject> SettingsModelStringArray createPropertySelectionModel(
			T nonReadableObj) {
		return new SettingsModelStringArray(PROPERTIES,
				Arrays.stream(nonReadableObj.getNewColumnSpecs())
						.map(colSpec -> colSpec.getName())
						.toArray(String[]::new));
	}

	public static SettingsModelString createNewlineModel() {
		return new SettingsModelString(NEWLINE_OUTPUT,
				LineBreak.getDefault().getActionCommand());
	}

}
