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
package com.vernalis.nodes.misc.plateids;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.vernalis.knime.dialog.components.SettingsModelIntegerBoundedRerangable;

/**
 * Node dialog for the (Append) Plate Well ID node
 * 
 * @author s.roughley
 *
 */
public class PlateWellIDNodeDialog extends DefaultNodeSettingsPane {

	/**
	 * A simple changelistener to be registered against those settings which
	 * update the Label Format Preview dialog component
	 * 
	 * @author s.roughley
	 *
	 */
	private final class PreviewChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			setLabelText();

		}
	}

	/**
	 * @author s.roughley Change listener to ensure that changes to the skipped
	 *         wells or plate size settings leave wells available on the plate
	 *
	 */
	private final class SkippedWellsChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			String modelKey = null;
			if (e.getSource() instanceof SettingsModelIntegerBoundedRerangable) {
				modelKey =
						((SettingsModelIntegerBoundedRerangable) e.getSource())
								.getKey();
			} else if (e.getSource() instanceof SettingsModelString) {
				modelKey = ((SettingsModelString) e.getSource()).getKey();
			}
			validateSkippedWells(modelKey);

		}

	}

	private static final String FIRST_PLATE_INDEX = "First Plate Index";
	private static final String PLATE_INDEX = "Plate Index";
	private static final String WELLS_TO_SKIP_AT_START =
			"Wells to skip at start of plate(s)";
	private static final String WELLS_TO_SKIP_AT_END =
			"Wells to skip at end of plate(s)";

	private static final String ROW_COLUMN_DELIMINATOR =
			"Row:Column deliminator";
	private static final String PLATE_ROW_DELIMINATOR = "Plate:Row deliminator";
	private static final String PLATE_PREFIX = "Plate prefix";
	private static final String PAD_COLUMN_IDS = "0-pad Column IDs";
	private static final String PAD_ROW_IDS = "<Space>-pad Row IDs";
	private static final String FILL_DIRECTION = "Fill Direction";
	private static final String PLATE_SIZE = "Plate Size";
	private final DialogComponentLabel plateWellPreviewLabel =
			new DialogComponentLabel("");
	private final DialogComponentLabel wellPreviewLabel =
			new DialogComponentLabel("");
	private final SettingsModelString sizeMdl = createSizeModel();
	private final SettingsModelString directionMdl = createDirectionModel();
	private final SettingsModelBoolean rowPadMdl = createRowPadModel();
	private final SettingsModelBoolean colPadMdl = createColPadModel();
	private final SettingsModelString platePrefixMdl = createPlatePrefixMdl();
	private final SettingsModelString plateRowDelimMdl =
			createPlateRowDelimMdl();
	private final SettingsModelString rowColumnSepMdl = createRowColumnSepMdl();
	private final SettingsModelIntegerBoundedRerangable skipStartMdl =
			createSkipStartMdl();
	private final SettingsModelIntegerBoundedRerangable skipEndMdl =
			createSkipEndMdl();

	/**
	 * @param isSource
	 *            If the node is a source node, then the Plate ID behaves
	 *            slightly differently
	 */
	public PlateWellIDNodeDialog(boolean isSource) {

		sizeMdl.addChangeListener(new PreviewChangeListener());
		rowPadMdl.addChangeListener(new PreviewChangeListener());
		colPadMdl.addChangeListener(new PreviewChangeListener());
		platePrefixMdl.addChangeListener(new PreviewChangeListener());
		plateRowDelimMdl.addChangeListener(new PreviewChangeListener());
		rowColumnSepMdl.addChangeListener(new PreviewChangeListener());

		sizeMdl.addChangeListener(new SkippedWellsChangeListener());
		skipStartMdl.addChangeListener(new SkippedWellsChangeListener());
		skipEndMdl.addChangeListener(new SkippedWellsChangeListener());

		addDialogComponent(new DialogComponentButtonGroup(sizeMdl, PLATE_SIZE,
				false, PlateSize.values()));

		addDialogComponent(new DialogComponentButtonGroup(directionMdl,
				FILL_DIRECTION, false, PlateDirection.values()));

		createNewGroup("Blank wells / plates");
		setHorizontalPlacement(true);
		final SettingsModelIntegerBounded firstPlateIndexMdl =
				createFirstPlateIndexModel();
		addDialogComponent(new DialogComponentNumber(firstPlateIndexMdl,
				isSource ? PLATE_INDEX : FIRST_PLATE_INDEX, 1, 4,
				isSource ? createFlowVariableModel(firstPlateIndexMdl) : null));
		addDialogComponent(new DialogComponentNumber(skipStartMdl,
				WELLS_TO_SKIP_AT_START, 1, 4));
		addDialogComponent(new DialogComponentNumber(skipEndMdl,
				WELLS_TO_SKIP_AT_END, 1, 4));
		setHorizontalPlacement(false);
		closeCurrentGroup();

		createNewGroup("Padding & Formatting");
		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentBoolean(rowPadMdl, PAD_ROW_IDS));
		addDialogComponent(
				new DialogComponentBoolean(colPadMdl, PAD_COLUMN_IDS));
		setHorizontalPlacement(false);

		setHorizontalPlacement(true);
		addDialogComponent(new DialogComponentString(platePrefixMdl,
				PLATE_PREFIX, false, 10));
		addDialogComponent(new DialogComponentString(plateRowDelimMdl,
				PLATE_ROW_DELIMINATOR, false, 5));
		addDialogComponent(new DialogComponentString(rowColumnSepMdl,
				ROW_COLUMN_DELIMINATOR, false, 5));
		setHorizontalPlacement(false);
		closeCurrentGroup();

		createNewGroup("Label Format Preview");
		setHorizontalPlacement(true);

		final DialogComponentLabel wellLbl =
				new DialogComponentLabel("Well ID:");
		wellLbl.getComponentPanel().setLayout(new FlowLayout(FlowLayout.LEFT));
		addDialogComponent(wellLbl);

		// wellPreviewLabel.getComponentPanel().setLayout(new
		// FlowLayout(FlowLayout.LEFT));
		addDialogComponent(wellPreviewLabel);

		final DialogComponentLabel plateWellLbl =
				new DialogComponentLabel("Plate / Well ID:");
		plateWellLbl.getComponentPanel()
				.setLayout(new FlowLayout(FlowLayout.LEFT));
		addDialogComponent(plateWellLbl);

		plateWellPreviewLabel.getComponentPanel()
				.setLayout(new FlowLayout(FlowLayout.LEFT));
		addDialogComponent(plateWellPreviewLabel);
		setHorizontalPlacement(false);

		closeCurrentGroup();
		setLabelText();

	}

	static SettingsModelIntegerBounded createFirstPlateIndexModel() {
		return new SettingsModelIntegerBounded(FIRST_PLATE_INDEX, 1, 1,
				Integer.MAX_VALUE);
	}

	static SettingsModelIntegerBoundedRerangable createSkipEndMdl() {
		return new SettingsModelIntegerBoundedRerangable(WELLS_TO_SKIP_AT_END,
				0, 0, PlateSize.getDefault().getWells());
	}

	static SettingsModelIntegerBoundedRerangable createSkipStartMdl() {
		return new SettingsModelIntegerBoundedRerangable(WELLS_TO_SKIP_AT_START,
				0, 0, PlateSize.getDefault().getWells());
	}

	/**
	 * Validates that the number of wells skipped at the start and end of plate
	 * leaves some wells used
	 * 
	 * @param modelKey
	 * 
	 */
	private void validateSkippedWells(String modelKey) {
		PlateSize size = PlateSize.valueOf(sizeMdl.getStringValue());
		if (PLATE_SIZE.equals(modelKey)) {
			// Need to ensure that the bounds and settings are within the new
			// plate size
			if (skipStartMdl.getIntValue() >= size.getWells()) {
				skipStartMdl.setIntValue(size.getWells() - 1);
			}
			skipStartMdl.setBounds(0, size.getWells() - 1);
			if (skipEndMdl.getIntValue() >= size.getWells()) {
				skipEndMdl.setIntValue(size.getWells() - 1);
			}
			skipEndMdl.setBounds(0, size.getWells() - 1);
		} else if (WELLS_TO_SKIP_AT_START.equals(modelKey)
				|| WELLS_TO_SKIP_AT_END.equals(modelKey)) {
			// Need to ensure that the skip end model bounds are within the
			// region between first well+1 and end of plate
			if (size.getWells() - skipStartMdl.getIntValue() <= skipEndMdl
					.getIntValue()) {
				skipEndMdl.setIntValue(
						size.getWells() - skipStartMdl.getIntValue() - 1);
			}
			skipEndMdl.setBounds(0,
					size.getWells() - skipStartMdl.getIntValue() - 1);
		}

	}

	/**
	 */
	private void setLabelText() {
		PlateSize size = PlateSize.valueOf(sizeMdl.getStringValue());
		JLabel plateWellJLabel = ((JLabel) plateWellPreviewLabel
				.getComponentPanel().getComponent(0));
		plateWellJLabel.setText(String.format(
				"%s1%s" /* Plate Prefix - Plate 1 - Plate-Row Deliminator */
						+ (rowPadMdl.getBooleanValue()
								? "%" + size.getMaxRowStringWidth() + "s"
								: "%s") /* Row ID */
						+ "%s" /* Row-Col Deliminator */
						+ (colPadMdl.getBooleanValue()
								? "%0" + size.getMaxColStringWidth() + "d"
								: "%d"),
				platePrefixMdl.getStringValue(),
				plateRowDelimMdl.getStringValue(), "A",
				rowColumnSepMdl.getStringValue(), 1));
		plateWellJLabel.repaint();

		JLabel wellJLabel =
				((JLabel) wellPreviewLabel.getComponentPanel().getComponent(0));
		wellJLabel.setText(String.format(
				"%s%s" /* Row ID - Row-Col Deliminator */
						+ (colPadMdl.getBooleanValue()
								? "%0" + size.getMaxColStringWidth() + "d"
								: "%d"),
				"A", rowColumnSepMdl.getStringValue(), 1));
		wellJLabel.repaint();

	}

	static SettingsModelString createPlatePrefixMdl() {
		return new SettingsModelString(PLATE_PREFIX, "Plate_");
	}

	static SettingsModelString createPlateRowDelimMdl() {
		return new SettingsModelString(PLATE_ROW_DELIMINATOR, ":");
	}

	static SettingsModelString createRowColumnSepMdl() {
		return new SettingsModelString(ROW_COLUMN_DELIMINATOR, ":");
	}

	static SettingsModelBoolean createColPadModel() {
		return new SettingsModelBoolean(PAD_COLUMN_IDS, true);
	}

	static SettingsModelBoolean createRowPadModel() {
		return new SettingsModelBoolean(PAD_ROW_IDS, false);
	}

	static SettingsModelString createDirectionModel() {
		return new SettingsModelString(FILL_DIRECTION,
				PlateDirection.getDefault().getActionCommand());
	}

	static SettingsModelString createSizeModel() {
		return new SettingsModelString(PLATE_SIZE,
				PlateSize.getDefault().getActionCommand());
	}
}
