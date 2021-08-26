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
package com.vernalis.pdbconnector2.query.text.dialog;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;

import com.vernalis.pdbconnector2.query.QueryModel;
import com.vernalis.pdbconnector2.query.text.dialog.QueryFieldModel.InvalidQueryFieldModel;

/**
 * The {@link QueryModel} implementation for a Query Group. A query group
 * comprises a logic operator, and a mixture of Query Fields (represented by
 * {@link QueryFieldModel}s) and sub-groups (represented by
 * {@link QueryGroupModel}s)
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryGroupModel
		extends AbstractQueryGroupModel<QueryGroupModel, QueryFieldModel>
		implements QueryModel {

	/**
	 * Constructor. An empty group is created
	 */
	public QueryGroupModel() {
		super(false);
	}

	/**
	 * Constructor. A group is added, optionally with a single
	 * {@link QueryFieldModel}
	 * 
	 * @param includeDefaultField
	 *            should a field be added
	 */
	public QueryGroupModel(boolean includeDefaultField) {
		super(includeDefaultField);
	}

	@Override
	protected QueryFieldModel createNewFieldModel() {
		return new QueryFieldModel();
	}

	@Override
	protected QueryFieldModel createNewFieldModel(NodeSettingsRO model)
			throws InvalidSettingsException {
		return new QueryFieldModel(model);
	}

	@Override
	protected QueryGroupModel createNewSubgroupModel() {
		return new QueryGroupModel();
	}

	@Override
	protected QueryFieldModel
			createNewBrokenFieldModel(NodeSettingsRO brokenSettings) {
		return new InvalidQueryFieldModel(brokenSettings);
	}

}
