/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.db;

import org.junit.Test;
import org.osgi.framework.Bundle;

import static org.junit.Assert.assertEquals;

/**
 * Test case for the {@link DBNodePlugin} class
 *
 * @author S Roughley
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class DBNodePluginTest {

	/**
	 * Test to check the activator loads and returns the correct bundle symbolic
	 * name
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void idTest() {
		DBNodePlugin activator = DBNodePlugin.getDefault();
		Bundle bundle = activator.getBundle();
		String symbolicName = bundle.getSymbolicName();
		assertEquals("com.vernalis.knime.db", symbolicName);
	}
}
