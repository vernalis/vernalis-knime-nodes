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
package com.vernalis.pdbconnector2.dialogcomponents;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;

import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.pdbconnector2.dialogcomponents.suggester.Suggester;
import com.vernalis.pdbconnector2.dialogcomponents.swing.JSuggestingTextField;

/**
 * A {@link DialogComponent} to allow the input of an array of Strings. The
 * input can be free text, via a {@link Suggester}, or as a {@link JComboBox}
 * dropdown depending on the constructor used. The component is displayed as a
 * table with the inputs in the first column and columns showing '+' or 'X' (add
 * or delete input) columns and a column with a logic operator (AND or OR)
 * 
 * @author S.Roughley knime@vernalis.com
 * 
 * @since 1.28.0
 *
 */
public class DialogComponentStringArrayInput extends DialogComponent {

	/**
	 * A table model backed by {@link SettingsModelStringArray}
	 *
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	private static final class SettingsModelStringArrayTableModel
			extends AbstractTableModel {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private final SettingsModelStringArray model;
		private final List<String> possibleValues;

		public SettingsModelStringArrayTableModel(
				SettingsModelStringArray model,
				Collection<String> possibleValues) {
			this.model = model;
			this.possibleValues =
					possibleValues == null || possibleValues.isEmpty() ? null
							: new ArrayList<>(possibleValues);

			model.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					if (model.getStringArrayValue() == null
							|| model.getStringArrayValue().length == 0) {
						model.setStringArrayValue(new String[] {
								SettingsModelStringArrayTableModel.this.possibleValues == null
										? ""
										: SettingsModelStringArrayTableModel.this.possibleValues
												.get(0) });
					}
					fireTableDataChanged();

				}
			});
			if (model.getStringArrayValue() == null
					|| model.getStringArrayValue().length == 0) {
				appendRow();
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (possibleValues != null && !possibleValues.contains(aValue)) {
				throw new IllegalArgumentException(
						"'" + aValue.toString() + "' is not a valid value");
			}
			if (columnIndex == 0) {
				final String[] newVals = model.getStringArrayValue();
				newVals[rowIndex] = (String) aValue;
				model.setStringArrayValue(newVals);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0:
					return String.class;

				default:
					return Object.class;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (!model.isEnabled()) {
				return false;
			}
			switch (columnIndex) {
				case 0:
					return true;
				case 1:
					// Can delete if more than one row
					return getRowCount() > 1;
				case 2:
					// Can add if last row:
					return rowIndex == getRowCount() - 1;
			}
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0:
					return model.getStringArrayValue()[rowIndex];

				default:
					return null;
			}

		}

		@Override
		public int getRowCount() {
			return model.getStringArrayValue().length;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		public void removeRow(int rowId) {
			model.setStringArrayValue(
					ArrayUtils.removeEntry(model.getStringArrayValue(), rowId));
			fireTableRowsDeleted(rowId, rowId);
		}

		public void appendRow() {
			final String[] newVal = Arrays.copyOf(model.getStringArrayValue(),
					model.getStringArrayValue().length + 1);
			newVal[newVal.length - 1] =
					possibleValues == null ? "" : possibleValues.get(0);
			model.setStringArrayValue(newVal);
			fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
		}

	}

	/**
	 * A TableCellEditor for the individual input cells
	 * 
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	private static final class StringInputColumnTableCellEditor
			extends AbstractCellEditor implements TableCellEditor {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		private final JTextField field;
		private final boolean allowEmptyEntries;

		public StringInputColumnTableCellEditor(Suggester suggester,
				int minSuggestSize, int fieldWidth, boolean allowEmptyEntries) {
			this.allowEmptyEntries = allowEmptyEntries;
			this.field = suggester != null
					? new JSuggestingTextField(suggester, minSuggestSize < 0
							? JSuggestingTextField.DEFAULT_MIN_SUGGEST_SIZE
							: minSuggestSize,
							fieldWidth > 0 ? fieldWidth
									: JSuggestingTextField.DEFAULT_FIELD_WIDTH)
					: new JTextField(fieldWidth > 0 ? fieldWidth
							: JSuggestingTextField.DEFAULT_FIELD_WIDTH);

			this.field.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					StringInputColumnTableCellEditor.this.fireEditingStopped();

				}
			});
		}

		@SuppressWarnings("unused")
		public void setEditorValue(String val) {
			field.setText(val);
		}

		public void silentlySetEditorValue(String val) {
			if (field instanceof JSuggestingTextField) {
				((JSuggestingTextField) field).silentlySetText(val);
			} else {
				field.setText(val);
			}
		}

		@Override
		public Object getCellEditorValue() {
			return field.getText();
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			silentlySetEditorValue((String) value);
			if (!allowEmptyEntries
					&& (value == null || ((String) value).isEmpty())) {
				field.setBackground(Color.RED);
			} else {
				field.setBackground(DEFAULT_BG);
			}
			return field;
		}

	}

	/**
	 * A {@link TableCellEditor} for a '+' button to add a row to the table, an
	 * 'X' button to delete the row or the logic label 'AND' or 'OR'
	 * 
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	private final class PlusButtonTableCellEditor extends AbstractCellEditor
			implements TableCellRenderer, TableCellEditor {

		private static final long serialVersionUID = 1L;
		private final boolean isAnd;

		public PlusButtonTableCellEditor(boolean isAnd) {
			this.isAnd = isAnd;
		}

		@Override
		public Object getCellEditorValue() {
			return null;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			if (column == 1) {
				final JButton b = new JButton("X");
				b.setEnabled(table.getRowCount() > 1);
				b.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						((SettingsModelStringArrayTableModel) table.getModel())
								.removeRow(row);
						PlusButtonTableCellEditor.this.cancelCellEditing();
					}
				});
				b.setPreferredSize(new Dimension(
						table.getColumnModel().getColumn(column)
								.getPreferredWidth(),
						b.getPreferredSize().height));
				return b;
			}
			if (column == 2) {
				if (row == table.getRowCount() - 1) {
					final JButton b = new JButton("+");
					b.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							final SettingsModelStringArrayTableModel model =
									(SettingsModelStringArrayTableModel) table
											.getModel();
							model.appendRow();
							PlusButtonTableCellEditor.this.cancelCellEditing();
						}
					});
					b.setPreferredSize(new Dimension(
							table.getColumnModel().getColumn(column)
									.getPreferredWidth(),
							b.getPreferredSize().height));
					return b;
				} else {

					final JLabel jLabel = new JLabel(isAnd ? "AND" : "OR",
							SwingConstants.CENTER);
					jLabel.setPreferredSize(new Dimension(
							table.getColumnModel().getColumn(column)
									.getPreferredWidth(),
							jLabel.getPreferredSize().height));
					jLabel.setFont(jLabel.getFont().deriveFont(Font.ITALIC));

					return jLabel;
				}
			}
			return null;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return getTableCellEditorComponent(table, value, isSelected, row,
					column);
		}

	}

	private final boolean allowEmptyEntries;

	private final JTable table;

	/*
	 * Constructors for free text input
	 */
	/**
	 * Constructor allowing free text input and unlimited visible rows
	 * 
	 * @param model
	 *            The Settings Model
	 * @param label
	 *            The component label, which may be {@code null} for no label
	 * @param isAnd
	 *            Whether the logic operator is 'AND' or 'OR'
	 * @param fieldWidth
	 *            The width of the input field
	 * @param allowEmptyEntries
	 *            Whether empty input entries are allowed
	 */
	public DialogComponentStringArrayInput(SettingsModelStringArray model,
			String label, boolean isAnd, int fieldWidth,
			boolean allowEmptyEntries) {
		this(model, label, isAnd, fieldWidth, allowEmptyEntries, -1);
	}

	/**
	 * Constructor allowing free text input and unlimited visible rows
	 * 
	 * @param model
	 *            The Settings Model
	 * @param label
	 *            The component label, which may be {@code null} for no label
	 * @param isAnd
	 *            Whether the logic operator is 'AND' or 'OR'
	 * @param fieldWidth
	 *            The width of the input field
	 * @param allowEmptyEntries
	 *            Whether empty input entries are allowed
	 * @param maxVisibleRows
	 *            The maximum number of visible editor rows. -1 means unlimited
	 */
	public DialogComponentStringArrayInput(SettingsModelStringArray model,
			String label, boolean isAnd, int fieldWidth,
			boolean allowEmptyEntries, int maxVisibleRows) {
		this(model, label, isAnd, fieldWidth, allowEmptyEntries, null, -1,
				maxVisibleRows);
	}

	/*
	 * Constructor for Suggester input
	 */

	/**
	 * Constructor allowing a {@link Suggester} input and unlimited visible rows
	 * 
	 * @param model
	 *            The Settings Model
	 * @param label
	 *            The component label, which may be {@code null} for no label
	 * @param isAnd
	 *            Whether the logic operator is 'AND' or 'OR'
	 * @param fieldWidth
	 *            The width of the input field
	 * @param allowEmptyEntries
	 *            Whether empty input entries are allowed
	 * @param fieldSuggester
	 *            The {@link Suggester} to provide interactive suggestions for
	 *            the field value in a dropdown once typing has started
	 * @param minSuggestSize
	 *            The minimum number of characters required before suggestions
	 *            are started
	 * @param maxVisibleRows
	 *            The maximum number of visible editor rows. -1 means unlimited
	 */
	public DialogComponentStringArrayInput(SettingsModelStringArray model,
			String label, boolean isAnd, int fieldWidth,
			boolean allowEmptyEntries, Suggester fieldSuggester,
			int minSuggestSize, int maxVisibleRows) {
		this(model, label, isAnd, fieldWidth, allowEmptyEntries, fieldSuggester,
				minSuggestSize, null, maxVisibleRows);
	}

	/*
	 * Constructors for JCombobox input
	 */
	/**
	 * Constructor allowing a {@link JComboBox} dropdown input and unlimited
	 * visible rows
	 * 
	 * @param model
	 *            The Settings Model
	 * @param label
	 *            The component label, which may be {@code null} for no label
	 * @param isAnd
	 *            Whether the logic operator is 'AND' or 'OR'
	 * @param possValues
	 *            The possible values for the dropdown
	 */
	public DialogComponentStringArrayInput(SettingsModelStringArray model,
			String label, boolean isAnd, Collection<String> possValues) {
		this(model, label, isAnd, possValues, -1);
	}

	/**
	 * Constructor allowing a {@link JComboBox} dropdown input and unlimited
	 * visible rows
	 * 
	 * @param model
	 *            The Settings Model
	 * @param label
	 *            The component label, which may be {@code null} for no label
	 * @param isAnd
	 *            Whether the logic operator is 'AND' or 'OR'
	 * @param possValues
	 *            The possible values for the dropdown
	 * @param maxVisibleRows
	 *            The maximum number of visible editor rows. -1 means unlimited
	 */
	public DialogComponentStringArrayInput(SettingsModelStringArray model,
			String label, boolean isAnd, Collection<String> possValues,
			int maxVisibleRows) {
		this(model, label, isAnd, -1, false, null, -1, possValues,
				maxVisibleRows);
	}

	/*
	 * Private constuctor covering all bases
	 */
	private DialogComponentStringArrayInput(SettingsModelStringArray model,
			String label, boolean isAnd, int fieldWidth,
			boolean allowEmptyEntries, Suggester fieldSuggester,
			int minSuggestSize, Collection<String> possibleValues,
			int maxVisibleRows) {
		super(model);
		this.allowEmptyEntries = allowEmptyEntries;
		if (label != null && !label.isEmpty()) {
			getComponentPanel().add(new JLabel(label));
		}

		if (model.getStringArrayValue() == null
				|| model.getStringArrayValue().length == 0) {
			model.setStringArrayValue(new String[] { "" });
		}

		table = new JTable(1, 3) {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void configureEnclosingScrollPane() {
				// DO nothing - we dont want to put the header in the top of any
				// scrollpane!
			}

		};

		// Dont allow any reordering/sorting/resizing
		table.getTableHeader().setReorderingAllowed(false);
		table.getTableHeader().setResizingAllowed(false);
		table.setRowSorter(null);
		table.setModel(new SettingsModelStringArrayTableModel(
				(SettingsModelStringArray) getModel(), possibleValues));
		table.setEnabled(true);

		final TableColumnModel columnModel = table.getColumnModel();
		int width = 0;
		final TableColumn c0 = columnModel.getColumn(0);
		if (possibleValues == null || possibleValues.isEmpty()) {
			// Text input - possible with suggestor
			c0.setCellEditor(
					new StringInputColumnTableCellEditor(fieldSuggester,
							minSuggestSize, fieldWidth, allowEmptyEntries));
			c0.setCellRenderer(new DefaultTableCellRenderer() {

				/**
				 *
				 */
				private static final long serialVersionUID = 1L;

				@Override
				protected void setValue(Object value) {
					if (!allowEmptyEntries
							&& (value == null || ((String) value).isEmpty())) {
						setBackground(Color.RED);
					} else {
						setBackground(DEFAULT_BG);
					}
					super.setValue(value);
				}
			});
			c0.setPreferredWidth(
					new JTextField(fieldWidth).getPreferredSize().width);
		} else {
			// JCombobox input
			c0.setCellRenderer(new DefaultTableCellRenderer());
			final JComboBox<String> combo = new JComboBox<>();
			possibleValues.forEach(x -> combo.addItem(x));
			combo.setEditable(false);
			c0.setCellEditor(new DefaultCellEditor(combo));
			c0.setPreferredWidth(combo.getPreferredSize().width);
		}
		width += c0.getPreferredWidth();

		final TableColumn c1 = columnModel.getColumn(1);
		c1.setCellEditor(new PlusButtonTableCellEditor(isAnd));
		c1.setCellRenderer((PlusButtonTableCellEditor) c1.getCellEditor());
		c1.setPreferredWidth(new JButton("X").getPreferredSize().width);
		width += c1.getPreferredWidth();

		final TableColumn c2 = columnModel.getColumn(2);
		c2.setCellEditor(new PlusButtonTableCellEditor(isAnd));
		c2.setCellRenderer((PlusButtonTableCellEditor) c2.getCellEditor());
		c2.setPreferredWidth(Math.max(
				new JLabel(isAnd ? "AND" : "OR").getPreferredSize().width,
				new JButton("+").getPreferredSize().width));
		width += c2.getPreferredWidth();

		if (maxVisibleRows > 0) {
			getComponentPanel().add(new JScrollPane(table,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
			table.setPreferredScrollableViewportSize(new Dimension(width,
					maxVisibleRows * table.getRowHeight()));
		} else {
			getComponentPanel().add(table);
		}
		updateComponent();
		model.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateComponent();

			}
		});

	}

	@Override
	protected void updateComponent() {
		// The component and renderers do it all...
	}

	@Override
	protected void validateSettingsBeforeSave()
			throws InvalidSettingsException {
		if (!allowEmptyEntries) {
			final SettingsModelStringArray model =
					(SettingsModelStringArray) getModel();
			for (final String s : model.getStringArrayValue()) {
				if (s == null || s.isEmpty()) {
					throw new InvalidSettingsException(
							"Empty entries are not allowed");
				}
			}
		}

	}

	@Override
	protected void checkConfigurabilityBeforeLoad(PortObjectSpec[] specs)
			throws NotConfigurableException {
		// Nothing to do here
	}

	@Override
	protected void setEnabledComponents(boolean enabled) {
		table.setEnabled(enabled);

	}

	@Override
	public void setToolTipText(String text) {
		table.setToolTipText(text);

	}

}
