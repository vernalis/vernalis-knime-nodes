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
import javax.swing.event.ChangeListener;

/**
 * A special {@link ChangeListener} sub-interface for Query Group chanes
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public interface QueryGroupChangeListener extends ChangeListener {

	@Override
	default void stateChanged(ChangeEvent e) {
		stateChanged(new QueryGroupChangeEvent(e.getSource(), null));

	}

	/**
	 * Invoked when the target listener has changed its state
	 * 
	 * @param e
	 *            The change event, which tracks both the source and type
	 */
	void stateChanged(QueryGroupChangeEvent e);

}
