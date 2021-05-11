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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vernalis.pdbconnector2.dialogcomponents.suggester.RcsbUrlSuggester;
import com.vernalis.pdbconnector2.dialogcomponents.swing.JSuggestingTextField;

/**
 * The {@link AbstractSequenceQueryPane} implementation for a Sequence Query,
 * backed by a {@link SequenceQueryModel}
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class SequenceQueryPane
		extends AbstractSequenceQueryPane<SequenceQueryModel> {

	private static final String SELECT_A_SEQUENCE = "<--Select a sequence-->";
	private static final String SEQUENCE_DROPDOWN_MASK =
			"X: XXXXXXXXXXXXXXXXX...";
	private static final long serialVersionUID = 1L;
	private static final String RCSB_STRUCTURE_QUERY_URL_MASK =
			"https://www.rcsb.org/search/sequence/%s";
	private final Map<String, String> possibleSequences = new TreeMap<>();
	private final JComboBox<String> seqChooser;

	/**
	 * Constructor with no flow variable button for the sequence input
	 * 
	 * @param model
	 *            The model
	 */
	public SequenceQueryPane(SequenceQueryModel model) {
		this(model, null);
	}

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the model
	 * @param fvm
	 *            The optional {@link FlowVariableModel} for the sequence
	 */
	public SequenceQueryPane(SequenceQueryModel model, FlowVariableModel fvm) {
		super(10, model, "Sequence", fvm);

		final JPanel cp0 =
				new DialogComponentNumberEdit(model.getEValueModel(), "E-Value")
						.getComponentPanel();
		cp0.setMinimumSize(cp0.getPreferredSize());
		cp0.setMaximumSize(cp0.getPreferredSize());
		getOptionsBox().add(cp0);

		final JPanel cp1 = new DialogComponentNumber(model.getIdentityModel(),
				"% Identity Threshold", 5).getComponentPanel();
		cp1.setMinimumSize(cp1.getPreferredSize());
		cp1.setMaximumSize(cp1.getPreferredSize());
		getOptionsBox().add(cp1);

		getOptionsBox().add(createHorizontalGlue());

		final Box seqLookupBox = addNewRow();
		seqLookupBox.add(createHorizontalStrut(10));
		seqLookupBox.add(new JLabel("PDB ID"));
		seqLookupBox.add(createHorizontalStrut(5));
		final JSuggestingTextField pdbID = new JSuggestingTextField(
				RcsbUrlSuggester.get(
						"https://www.rcsb.org/search/suggester/rcsb_entry_container_identifiers.entry_id/%s"),
				1, 4);
		pdbID.setMinimumSize(pdbID.getPreferredSize());
		pdbID.setMaximumSize(pdbID.getPreferredSize());
		pdbID.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSequenceDropdown(pdbID.getText());

			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSequenceDropdown(pdbID.getText());

			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateSequenceDropdown(pdbID.getText());

			}
		});
		seqLookupBox.add(pdbID);
		seqLookupBox.add(createHorizontalStrut(5));

		// We set up a JComboBox with a custom renderer to grey out the
		// instruction
		// entry
		// NB The instruction entry is still selectable so as to show it in the
		// dialog,
		// but the selection listener ignores it
		// See https://stackoverflow.com/a/28344521/6076839 for the basis of
		// this render
		seqChooser = new JComboBox<>();
		seqChooser.setRenderer(new DefaultListCellRenderer() {

			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				if (value instanceof JComponent) {
					return (JComponent) value;
				}

				final boolean enabled = value != null
						&& !value.toString().equals(SELECT_A_SEQUENCE);

				super.getListCellRendererComponent(list, value, index,
						isSelected && enabled, cellHasFocus);
				setEnabled(enabled);
				return this;
			}

		});

		seqChooser.setPrototypeDisplayValue(SEQUENCE_DROPDOWN_MASK);
		seqChooser.setMinimumSize(seqChooser.getPreferredSize());
		seqChooser.setMaximumSize(seqChooser.getPreferredSize());

		seqChooser.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (!seqChooser.getSelectedItem()
							.equals(SELECT_A_SEQUENCE)) {
						model.setSequence(possibleSequences
								.get(seqChooser.getSelectedItem()));
						pdbID.silentlyClearText();
						seqChooser.removeAllItems();
					}
				}

			}
		});
		seqLookupBox.add(seqChooser);
		seqLookupBox.add(createHorizontalGlue());

	}

	/**
	 * Method to update the sequence selector with the sequences from a remove
	 * webservice call for the specified PDB ID
	 * 
	 * @param pdbIdTxt
	 *            The current text from the PDB id field
	 */
	protected void updateSequenceDropdown(String pdbIdTxt) {
		if (pdbIdTxt.length() != 4) {
			possibleSequences.clear();
			seqChooser.removeAllItems();
		} else {
			possibleSequences.clear();
			seqChooser.removeAllItems();
			try {
				final URL url =
						new URL(String.format(RCSB_STRUCTURE_QUERY_URL_MASK,
								pdbIdTxt.toUpperCase()));
				final HttpURLConnection conn =
						(HttpURLConnection) url.openConnection();
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					final JsonNode result =
							new ObjectMapper().readTree(conn.getInputStream());
					if (result.has("msg")) {
						return;
					}

					if (!result.isArray()) {
						return;
					}
					final Iterator<JsonNode> iter = result.elements();
					while (iter.hasNext()) {
						final String s = iter.next().asText();
						final String key =
								s.length() > SEQUENCE_DROPDOWN_MASK.length()
										? s.substring(0,
												SEQUENCE_DROPDOWN_MASK.length()
														- 3)
												+ "..."
										: s;
						possibleSequences.put(key, s.split(": ")[1]);

					}
					seqChooser.addItem(SELECT_A_SEQUENCE);
					possibleSequences.keySet()
							.forEach(x -> seqChooser.addItem(x));

				} else {
					return;
				}
			} catch (final Exception e) {
				return;
			}
		}

	}

}
