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

import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.io.FileHelpers.LineBreak;

/**
 * 
 * @author s.roughley
 *
 */
public class AbstractMultiLineObjectLoadFilesNodeDialog
		extends AbstractLoadFilesNodeDialog {

	private static final String NEWLINE_OUTPUT = "Newline output";

	public AbstractMultiLineObjectLoadFilesNodeDialog(String historyID,
			String... fileTypes) {
		super(historyID, fileTypes);
		DialogComponentButtonGroup lbDlg = new DialogComponentButtonGroup(
				createNewlineModel(), NEWLINE_OUTPUT, true, LineBreak.values());
		lbDlg.setToolTipText("Linebreak behaviour in multi-line output");
		addDialogComponent(lbDlg);
	}

	public static SettingsModelString createNewlineModel() {
		return new SettingsModelString(NEWLINE_OUTPUT,
				LineBreak.getDefault().getActionCommand());
	}

}
