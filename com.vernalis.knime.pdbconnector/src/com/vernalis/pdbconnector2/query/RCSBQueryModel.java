/*******************************************************************************
 * Copyright (c) 2020, 2021, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.InvalidSettingsExceptionCombiner;
import com.vernalis.pdbconnector2.query.chemical.ChemicalQueryModel;
import com.vernalis.pdbconnector2.query.sequence.SequenceMotifQueryModel;
import com.vernalis.pdbconnector2.query.sequence.SequenceQueryModel;
import com.vernalis.pdbconnector2.query.structsim.StructureSimilarityQueryModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupChangeEvent;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupChangeListener;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupModel;

/**
 * A class representing a full advanced query
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class RCSBQueryModel implements QueryModel, PortObjectSpec {

	private static String CFG_KEY_TEXT = "Text";
	private static final String CFG_KEY_VERSION = "modelVersion";

	private final QueryGroupModel textModel = new QueryGroupModel();
	private final SequenceQueryModel seqModel = new SequenceQueryModel();
	private final SequenceMotifQueryModel seqMotifModel =
			new SequenceMotifQueryModel();
	private final StructureSimilarityQueryModel structSimModel =
			new StructureSimilarityQueryModel();
	private final ChemicalQueryModel chemModel = new ChemicalQueryModel();

	private static final int version = 2;
	private final String configKey;
	private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Constructor with {@code null} settings key
	 */
	public RCSBQueryModel() {
		this(null);
	}

	/**
	 * Constructor with settings key
	 * 
	 * @param configKey
	 *            The settings Key
	 */
	public RCSBQueryModel(String configKey) {
		this.configKey = configKey;
		textModel.addChangeListener(new QueryGroupChangeListener() {

			@Override
			public void stateChanged(QueryGroupChangeEvent e) {
				notifyChangeListeners(new ChangeEvent(textModel));

			}
		});
		seqModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(new ChangeEvent(seqModel));

			}
		});
		seqMotifModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(new ChangeEvent(seqMotifModel));

			}
		});
		structSimModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(new ChangeEvent(structSimModel));

			}
		});
		chemModel.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				notifyChangeListeners(new ChangeEvent(chemModel));

			}
		});

	}

	/**
	 * Notify all registered change listeners
	 * 
	 * @param changeEvent
	 *            The {@link ChangeEvent}
	 */
	protected void notifyChangeListeners(ChangeEvent changeEvent) {
		for (final ChangeListener l : listeners) {
			l.stateChanged(changeEvent);
		}

	}

	/**
	 * @return The model for the text query
	 */
	public QueryGroupModel getTextModel() {
		return textModel;
	}

	/**
	 * @return The model for the sequence query
	 */
	public final SequenceQueryModel getSeqModel() {
		return seqModel;
	}

	/**
	 * @return The model for the sequence motif query
	 */
	public final SequenceMotifQueryModel getSeqMotifModel() {
		return seqMotifModel;
	}

	/**
	 * @return The model for the structure similarity query
	 */
	public final StructureSimilarityQueryModel getStructSimModel() {
		return structSimModel;
	}

	/**
	 * @return The model for the chemical query
	 */
	public final ChemicalQueryModel getChemicalQueryModel() {
		return chemModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * saveSettingsTo(org.knime.core.node.NodeSettingsWO)
	 */
	@Override
	public final void saveSettingsTo(NodeSettingsWO settings) {
		if (configKey == null) {
			save(settings);
		} else {
			save(settings.addNodeSettings(configKey));
		}
	}

	/**
	 * Method to save the settings
	 * 
	 * @param settings
	 *            The {@link NodeSettingsWO} object to save the settings to
	 */
	private void save(NodeSettingsWO settings) {
		settings.addInt(CFG_KEY_VERSION, version);
		textModel.saveSettingsTo(settings.addNodeSettings(CFG_KEY_TEXT));
		seqModel.saveSettingsTo(settings);
		seqMotifModel.saveSettingsTo(settings);
		structSimModel.saveSettingsTo(settings);
		chemModel.saveSettingsTo(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * validateSettings(org.knime.core.node.NodeSettingsRO)
	 */
	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		if (configKey == null) {
			validate(settings);
		} else {
			validate(settings.getNodeSettings(configKey));
		}

	}

	/**
	 * Method to validate the saved settings
	 * 
	 * @param settings
	 *            The settings to validate
	 * @throws InvalidSettingsException
	 */
	private void validate(NodeSettingsRO settings)
			throws InvalidSettingsException {
		final InvalidSettingsExceptionCombiner isec =
				new InvalidSettingsExceptionCombiner();

		// Legacy version without this property is v1
		@SuppressWarnings("unused")
		int savedVersion = settings.getInt(CFG_KEY_VERSION, 1);
		try {
			textModel.validateSettings(settings.getNodeSettings(CFG_KEY_TEXT));
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			seqModel.validateSettings(settings);
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			seqMotifModel.validateSettings(settings);
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			structSimModel.validateSettings(settings);
		} catch (final InvalidSettingsException e) {
			isec.add(e);
		}
		try {
			chemModel.validateSettings(settings);
		} catch (InvalidSettingsException e) {
			isec.add(e);
		}
		isec.throwAll();
	}

	@Override
	public void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		if (configKey == null) {
			load(settings);
		} else {
			load(settings.getNodeSettings(configKey));
		}
	}

	/**
	 * Method to load the stored settings
	 * 
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	private void load(NodeSettingsRO settings) throws InvalidSettingsException {
		// Legacy version without this property is v1
		@SuppressWarnings("unused")
		int savedVersion = settings.getInt(CFG_KEY_VERSION, 1);

		textModel.loadSettings(settings.getNodeSettings(CFG_KEY_TEXT));
		seqModel.loadSettings(settings);
		seqMotifModel.loadSettings(settings);
		structSimModel.loadSettings(settings);
		chemModel.loadSettings(settings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener l) {
		if (!listeners.contains(l)) {
			listeners.add(l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	/**
	 * @return The count of individual query types which contain a query
	 */
	public int countQueryTypes() {
		int retVal = 0;
		if (hasTextQuery()) {
			retVal++;
		}
		if (hasSequenceQuery()) {
			retVal++;
		}
		if (hasSequenceMotifQuery()) {
			retVal++;
		}
		if (hasStructureSimilarityQuery()) {
			retVal++;
		}
		if (hasChemicalQuery()) {
			retVal++;
		}
		return retVal;
	}

	/**
	 * @return {@code true} if there is only one query type
	 */
	public boolean isSingleton() {
		return countQueryTypes() == 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * hasQuery()
	 */
	@Override
	public boolean hasQuery() {
		return hasTextQuery() || hasSequenceQuery() || hasSequenceMotifQuery()
				|| hasStructureSimilarityQuery() || hasChemicalQuery();
	}

	@Override
	public boolean hasInvalidQuery() {
		return getTextModel().hasInvalidQuery()
				|| getSeqModel().hasInvalidQuery()
				|| getSeqMotifModel().hasInvalidQuery()
				|| getStructSimModel().hasInvalidQuery()
				|| getChemicalQueryModel().hasInvalidQuery();
	}

	/**
	 * @return whether there is a structure similarity query present
	 */
	public boolean hasStructureSimilarityQuery() {
		return structSimModel.hasQuery();
	}

	/**
	 * @return whether there is a sequence motif query present
	 */
	public boolean hasSequenceMotifQuery() {
		return seqMotifModel.hasQuery();
	}

	/**
	 * @return whether there is a sequence query present
	 */
	public boolean hasSequenceQuery() {
		return seqModel.hasQuery();
	}

	/**
	 * @return whether there is a text query present
	 */
	public boolean hasTextQuery() {
		return textModel.hasQuery();
	}

	/**
	 * @return whether there is a chemical query present
	 */
	public boolean hasChemicalQuery() {
		return chemModel.hasQuery();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * clearQuery()
	 */
	@Override
	public void clearQuery() {
		textModel.clearQuery();
		seqModel.clearQuery();
		seqMotifModel.clearQuery();
		structSimModel.clearQuery();
		chemModel.clearQuery();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * getChangeListeners()
	 */
	@Override
	public List<ChangeListener> getChangeListeners() {
		return Collections.unmodifiableList(listeners);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.knime.core.node.port.PortObjectSpec#getViews()
	 */
	@Override
	public JComponent[] getViews() {
		final JTextArea jsonView = new JTextArea();
		jsonView.setColumns(80);
		jsonView.setName("JSON Query");
		try {
			jsonView.setForeground(Color.BLACK);
			jsonView.setText(new ObjectMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(getQueryNodes(new AtomicInteger())));

			jsonView.setRows(
					Math.min(5, jsonView.getText().split("\n").length));
		} catch (final JsonProcessingException e) {
			jsonView.setForeground(Color.RED);
			jsonView.setText("Error processing query:\n" + e.getMessage());
			jsonView.setRows(5);
		}
		return new JComponent[] { jsonView };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.pdbconnector2.dialogcomponents.swing.QueryModel#
	 * getQueryNodes(java.util.concurrent.atomic.AtomicInteger)
	 */
	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeId) {
		if (!hasQuery()) {
			return null;
		}
		final ObjectNode retVal = new ObjectMapper().createObjectNode();
		retVal.put("type", "group");
		retVal.put("logical_operator", "and");
		final ArrayNode nodes = retVal.putArray("nodes");
		if (hasTextQuery()) {
			nodes.add(textModel.getQueryNodes(nodeId));
		}
		if (hasSequenceQuery()) {
			nodes.add(seqModel.getQueryNodes(nodeId));
		}
		if (hasSequenceMotifQuery()) {
			nodes.add(seqMotifModel.getQueryNodes(nodeId));
		}
		if (hasStructureSimilarityQuery()) {
			nodes.add(structSimModel.getQueryNodes(nodeId));
		}
		if (hasChemicalQuery()) {
			nodes.add(chemModel.getQueryNodes(nodeId));
		}
		return retVal;
	}

}
