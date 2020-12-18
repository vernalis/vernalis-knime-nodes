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
package com.vernalis.pdbconnector2.query.text.dialog;

import javax.swing.event.ChangeEvent;

import com.vernalis.pdbconnector2.query.text.dialog.QueryGroupModel.QueryGroupEventType;

/**
 * A {@link ChangeEvent} subclass which also tracks the type of change event
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public class QueryGroupChangeEvent extends ChangeEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final QueryGroupEventType type;

	/**
	 * Constructor
	 * 
	 * @param source
	 *            The object causing the change
	 * @param type
	 *            The type of event
	 */
	public QueryGroupChangeEvent(Object source, QueryGroupEventType type) {
		super(source);
		this.type = type;
	}

	/**
	 * @return The event type
	 */
	public QueryGroupEventType getType() {
		return type;
	}

}
