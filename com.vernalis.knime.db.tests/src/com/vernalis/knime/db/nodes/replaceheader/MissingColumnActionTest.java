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
package com.vernalis.knime.db.nodes.replaceheader;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for the {@link MissingColumnAction} class
 *
 * @author S Roughley
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 * @since 07-Sep-2022
 * @since v1.36.0
 */
public class MissingColumnActionTest {

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
	public void testMissingColumnActionSize() throws Exception {

		assertEquals(3, MissingColumnAction.values().length);
	}

	/**
	 * Test the {@link MissingColumnAction#Fail}
	 * {@link MissingColumnAction#getActionCommand()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testFail_GetActionCommandMethod() throws Exception {

		assertEquals("Fail", MissingColumnAction.Fail.getActionCommand());
	}

	/**
	 * Test the {@link MissingColumnAction#Leave_Unchanged}
	 * {@link MissingColumnAction#getActionCommand()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testLeave_Unchanged_GetActionCommandMethod() throws Exception {

		assertEquals("Leave_Unchanged",
				MissingColumnAction.Leave_Unchanged.getActionCommand());
	}

	/**
	 * Test the {@link MissingColumnAction#Omit}
	 * {@link MissingColumnAction#getActionCommand()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testOmit_GetActionCommandMethod() throws Exception {

		assertEquals("Omit", MissingColumnAction.Omit.getActionCommand());
	}

	/**
	 * Test the static method {@link MissingColumnAction#getDefault()}
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testGetDefaultStaticMethod() throws Exception {

		assertEquals(MissingColumnAction.Leave_Unchanged,
				MissingColumnAction.getDefault());
	}

	/**
	 * Test the {@link MissingColumnAction#Fail}
	 * {@link MissingColumnAction#getText()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testFail_GetTextMethod() throws Exception {

		assertEquals("Fail", MissingColumnAction.Fail.getText());
	}

	/**
	 * Test the {@link MissingColumnAction#Leave_Unchanged}
	 * {@link MissingColumnAction#getText()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testLeave_Unchanged_GetTextMethod() throws Exception {

		assertEquals("Leave Unchanged",
				MissingColumnAction.Leave_Unchanged.getText());
	}

	/**
	 * Test the {@link MissingColumnAction#Omit}
	 * {@link MissingColumnAction#getText()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testOmit_GetTextMethod() throws Exception {

		assertEquals("Omit", MissingColumnAction.Omit.getText());
	}

	/**
	 * Test the {@link MissingColumnAction#Fail}
	 * {@link MissingColumnAction#isDefault()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testFail_IsDefaultMethod() throws Exception {

		assertEquals(false, MissingColumnAction.Fail.isDefault());
	}

	/**
	 * Test the {@link MissingColumnAction#Leave_Unchanged}
	 * {@link MissingColumnAction#isDefault()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testLeave_Unchanged_IsDefaultMethod() throws Exception {

		assertEquals(true, MissingColumnAction.Leave_Unchanged.isDefault());
	}

	/**
	 * Test the {@link MissingColumnAction#Omit}
	 * {@link MissingColumnAction#isDefault()} method
	 * 
	 * @throws Exception
	 *             if an error occurred during test execution
	 * 
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	@Test
	public void testOmit_IsDefaultMethod() throws Exception {

		assertEquals(false, MissingColumnAction.Omit.isDefault());
	}

}
