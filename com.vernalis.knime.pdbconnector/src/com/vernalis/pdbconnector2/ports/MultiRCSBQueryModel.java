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
package com.vernalis.pdbconnector2.ports;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.util.NonClosableInputStream;
import org.knime.core.data.util.NonClosableOutputStream;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vernalis.pdbconnector2.query.QueryModel;
import com.vernalis.pdbconnector2.query.RCSBQueryModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupConjunction;
import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupModel;

/**
 * A {@link PortObjectSpec} implementation containing one or more
 * {@link RCSBQueryModel}s
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class MultiRCSBQueryModel
		implements QueryModel, PortObjectSpec, Iterable<RCSBQueryModel> {

	private static final String CFGKEY_SUBGROUPS = "subgroups";
	private static final String CFGKEY_QUERIES = "queries";

	/**
	 * The Serializer for the {@link MultiRCSBQueryModel}
	 * 
	 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
	 *
	 */
	public static final class Serializer
			extends PortObjectSpecSerializer<MultiRCSBQueryModel> {

		@Override
		public void savePortObjectSpec(MultiRCSBQueryModel portObjectSpec,
				PortObjectSpecZipOutputStream out) throws IOException {

			final NodeSettings settings = new NodeSettings("model");
			portObjectSpec.saveSettingsTo(settings);

			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			settings.saveToXML(new NonClosableOutputStream.Zip(out));
			out.close();

		}

		@Override
		public MultiRCSBQueryModel loadPortObjectSpec(
				PortObjectSpecZipInputStream in) throws IOException {
			final MultiRCSBQueryModel retVal = new MultiRCSBQueryModel();

			final ZipEntry entry = in.getNextEntry();
			if (!ZIP_ENTRY_NAME.equals(entry.getName())) {
				throw new IOException("Expected zip entry " + ZIP_ENTRY_NAME
						+ ", got " + entry.getName());
			}
			final NodeSettingsRO s = NodeSettings
					.loadFromXML(new NonClosableInputStream.Zip(in));
			try {
				retVal.loadSettings(s);
			} catch (InvalidSettingsException | NullPointerException
					| IllegalArgumentException e) {
				throw new IOException("Unable to load RCSB Advanced PDB Query: "
						+ e.getMessage(), e);
			}
			return retVal;
		}

	}

	private static final String ZIP_ENTRY_NAME = "query.xml";
	private final List<RCSBQueryModel> models = new ArrayList<>();
	private final List<MultiRCSBQueryModel> subMultis = new ArrayList<>();
	private QueryGroupConjunction conjunction =
			QueryGroupConjunction.getDefault();
	private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();

	/**
	 * Constructor
	 */
	public MultiRCSBQueryModel() {
		super();
	}

	@Override
	public void loadSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {

		try {
			conjunction = QueryGroupConjunction.valueOf(
					settings.getString(QueryGroupModel.CFGKEY_OPERATOR));
		} catch (IllegalArgumentException | NullPointerException e) {
			new InvalidSettingsException("Error loading conjuction", e);
		}
		final NodeSettingsRO querySettings =
				settings.getNodeSettings(CFGKEY_QUERIES);
		for (final String key : querySettings) {
			final RCSBQueryModel m = new RCSBQueryModel();
			m.loadSettings(querySettings.getNodeSettings(key));
			addModel(m);
		}
		final NodeSettingsRO multiSettings =
				settings.getNodeSettings(CFGKEY_SUBGROUPS);
		for (final String key : multiSettings) {
			final MultiRCSBQueryModel subGroup = new MultiRCSBQueryModel();
			subGroup.loadSettings(multiSettings.getNodeSettings(key));
			addModel(subGroup);
		}
	}

	@Override
	public void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		try {
			QueryGroupConjunction.valueOf(
					settings.getString(QueryGroupModel.CFGKEY_OPERATOR));
		} catch (IllegalArgumentException | NullPointerException e) {
			new InvalidSettingsException("Error loading conjuction", e);
		}
		final NodeSettingsRO querySettings =
				settings.getNodeSettings(CFGKEY_QUERIES);
		for (final String key : querySettings) {
			new RCSBQueryModel()
					.loadSettings(querySettings.getNodeSettings(key));
		}
		final NodeSettingsRO multiSettings =
				settings.getNodeSettings(CFGKEY_SUBGROUPS);
		for (final String key : multiSettings) {
			new MultiRCSBQueryModel()
					.loadSettings(multiSettings.getNodeSettings(key));
		}
	}

	@Override
	public void saveSettingsTo(NodeSettingsWO settings) {
		final NodeSettingsWO s0 = settings.addNodeSettings(CFGKEY_QUERIES);
		int i = 0;
		for (final RCSBQueryModel m : this) {
			m.saveSettingsTo(s0.addNodeSettings(String.format("%d", i++)));
		}
		settings.addString(QueryGroupModel.CFGKEY_OPERATOR,
				getConjunction().name());
		final NodeSettingsWO s1 = settings.addNodeSettings(CFGKEY_SUBGROUPS);
		i = 0;
		for (final MultiRCSBQueryModel m : subMultis) {
			m.saveSettingsTo(s1.addNodeSettings(String.format("%d", i++)));
		}
	}

	/**
	 * Method to add a new model to the port. The query is only added if it is
	 * not already present, and has a query
	 * 
	 * @param model
	 *            The model to add to the port
	 */
	public void addModel(RCSBQueryModel model) {
		if (!models.contains(model) && model.hasQuery()) {
			if (models.add(model)) {
				notifyChangeListeners(new ChangeEvent(model));
			}
		}
	}

	private void notifyChangeListeners(ChangeEvent changeEvent) {
		for (ChangeListener l : getChangeListeners()) {
			l.stateChanged(changeEvent);
		}

	}

	/**
	 * Method to add a new subquery. If the subquery has no query, nothing is
	 * done. If the subquery has the same {@link QueryGroupConjunction} as this
	 * query or only has 1 query, then it's queries and subgroups are added
	 * directly to this object, otherwise the object is added as a subquery
	 * 
	 * @param subGroup
	 *            The subgroup query to add
	 */
	public void addModel(MultiRCSBQueryModel subGroup) {
		if (subGroup == null || !subGroup.hasQuery()) {
			// do nothing
			return;
		}
		if (subGroup.getConjunction() == getConjunction()
				|| subGroup.getTotalSize() == 1) {
			for (final RCSBQueryModel m : subGroup) {
				addModel(m);
			}
			for (final MultiRCSBQueryModel m : subGroup.getSubMultiqueries()) {
				addModel(m);
			}
		} else {
			subMultis.add(subGroup);
		}

	}

	/**
	 * @return The total number of queries
	 */
	public int getTotalSize() {
		return models.size() + subMultis.size();
	}

	/**
	 * @return An unmodifiable view of the sub-queries
	 */
	public List<MultiRCSBQueryModel> getSubMultiqueries() {
		return Collections.unmodifiableList(subMultis);
	}

	/**
	 * Method to remove a query
	 * 
	 * @param model
	 *            The query to remove
	 */
	public void removeModel(RCSBQueryModel model) {
		if (models.remove(model)) {
			notifyChangeListeners(new ChangeEvent(model));
		}
	}

	@Override
	public JsonNode getQueryNodes(AtomicInteger nodeId) {
		final List<QueryModel> queries = new ArrayList<>();
		models.stream().filter(x -> x.hasQuery()).forEach(queries::add);
		subMultis.stream().filter(x -> x.hasQuery()).forEach(queries::add);
		if (queries.isEmpty()) {
			return null;
		}
		if (queries.size() == 1) {
			return queries.get(0).getQueryNodes(nodeId);
		}
		final ObjectNode retVal = new ObjectMapper().createObjectNode()
				.put("type", "group")
				.put("logical_operator", getConjunction().getActionCommand());
		final ArrayNode nodes = retVal.putArray("nodes");
		for (final QueryModel m : queries) {
			nodes.add(m.getQueryNodes(nodeId));
		}
		return retVal;

	}

	@Override
	public boolean hasQuery() {
		return models.stream().anyMatch(m -> m.hasQuery())
				|| subMultis.stream().anyMatch(m -> m.hasQuery());
	}

	@Override
	public void clearQuery() {
		while (!models.isEmpty()) {
			removeModel(models.get(0));
		}
		while (!subMultis.isEmpty()) {
			removeSubmulti(subMultis.get(0));
		}
	}

	/**
	 * Method to remove a sub-query, or it's constituent parts from this query
	 * if it has the same conjunction
	 * 
	 * @param multiRCSBQueryModel
	 *            The sub-query to remove
	 */
	public void removeSubmulti(MultiRCSBQueryModel multiRCSBQueryModel) {
		if (multiRCSBQueryModel.getConjunction() == getConjunction()
				|| multiRCSBQueryModel.getTotalSize() == 1) {
			for (final RCSBQueryModel m : multiRCSBQueryModel) {
				removeModel(m);
			}
			for (final MultiRCSBQueryModel m : multiRCSBQueryModel
					.getSubMultiqueries()) {
				removeSubmulti(m);
			}
		}
		if (subMultis.remove(multiRCSBQueryModel)) {
			notifyChangeListeners(new ChangeEvent(multiRCSBQueryModel));
		}

	}

	@Override
	public List<ChangeListener> getChangeListeners() {
		return listeners;
	}

	@Override
	public JComponent[] getViews() {
		JTextArea t;
		if (!hasQuery()) {
			t = new JTextArea("No query!");
			t.setForeground(Color.RED);
		} else {
			try {
				t = new JTextArea(new ObjectMapper()
						.writerWithDefaultPrettyPrinter().writeValueAsString(
								getQueryNodes(new AtomicInteger())));
			} catch (final JsonProcessingException e) {
				t = new JTextArea("Error processing query:\n");
				t.append(e.getMessage());
				t.setForeground(Color.RED);
			}
		}
		final JScrollPane retVal = new JScrollPane(t);
		retVal.setName("JSON Query");
		return new JComponent[] { retVal, getResultTableSpec().getViews()[0] };
	}

	@Override
	public Iterator<RCSBQueryModel> iterator() {
		return models.iterator();
	}

	/**
	 * @return The conjunction for the current query level
	 */
	public QueryGroupConjunction getConjunction() {
		return conjunction;
	}

	/**
	 * Method to set the conjunction for the current query level
	 * 
	 * @param conjunction
	 *            The conjunction to set
	 */
	public void setConjunction(QueryGroupConjunction conjunction) {
		this.conjunction = conjunction;

	}

	/**
	 * @return The output table spec resulting from query execution
	 */
	public DataTableSpec getResultTableSpec() {
		final DataTableSpecCreator specFact = new DataTableSpecCreator();
		specFact.addColumns(
				new DataColumnSpecCreator("ID", StringCell.TYPE).createSpec());
		specFact.addColumns(new DataColumnSpecCreator("Score", DoubleCell.TYPE)
				.createSpec());
		return specFact.createSpec();
	}

}
