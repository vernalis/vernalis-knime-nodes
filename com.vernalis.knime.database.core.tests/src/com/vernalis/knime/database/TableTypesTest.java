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
package com.vernalis.knime.database;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test class for the {@link TableTypes} class
 *
 * @author S Roughley
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class TableTypesTest {

	/**
	 * Test the enum has the correct number of values
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testTableTypesSize() throws Exception {

		assertEquals(7, TableTypes.values().length);
	}

	/**
	 * Test the static method {@link TableTypes#getDefault()}
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testGetDefaultStaticMethod() throws Exception {

		assertEquals(TableTypes.TABLE, TableTypes.getDefault());
	}

	/**
	 * Test the static method {@link TableTypes#getDefaults()}
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testGetDefaultsStaticMethod() throws Exception {
		assertArrayEquals(new String[] { "TABLE", "VIEW" },
				TableTypes.getDefaults());
	}

	/**
	 * Test the {@link TableTypes#TABLE} {@link TableTypes#getTypeName()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testTABLE_GetTypeNameMethod() throws Exception {

		assertEquals("TABLE", TableTypes.TABLE.getTypeName());
	}

	/**
	 * Test the {@link TableTypes#VIEW} {@link TableTypes#getTypeName()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testVIEW_GetTypeNameMethod() throws Exception {

		assertEquals("VIEW", TableTypes.VIEW.getTypeName());
	}

	/**
	 * Test the {@link TableTypes#SYSTEM_TABLE} {@link TableTypes#getTypeName()}
	 * method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testSYSTEM_TABLE_GetTypeNameMethod() throws Exception {

		assertEquals("SYSTEM TABLE", TableTypes.SYSTEM_TABLE.getTypeName());
	}

	/**
	 * Test the {@link TableTypes#GLOBAL_TEMPORARY}
	 * {@link TableTypes#getTypeName()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testGLOBAL_TEMPORARY_GetTypeNameMethod() throws Exception {

		assertEquals("GLOBAL TEMPORARY",
				TableTypes.GLOBAL_TEMPORARY.getTypeName());
	}

	/**
	 * Test the {@link TableTypes#LOCAL_TEMPORARY}
	 * {@link TableTypes#getTypeName()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testLOCAL_TEMPORARY_GetTypeNameMethod() throws Exception {

		assertEquals("LOCAL TEMPORARY",
				TableTypes.LOCAL_TEMPORARY.getTypeName());
	}

	/**
	 * Test the {@link TableTypes#ALIAS} {@link TableTypes#getTypeName()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testALIAS_GetTypeNameMethod() throws Exception {

		assertEquals("ALIAS", TableTypes.ALIAS.getTypeName());
	}

	/**
	 * Test the {@link TableTypes#SYNONYM} {@link TableTypes#getTypeName()}
	 * method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testSYNONYM_GetTypeNameMethod() throws Exception {

		assertEquals("SYNONYM", TableTypes.SYNONYM.getTypeName());
	}

}
