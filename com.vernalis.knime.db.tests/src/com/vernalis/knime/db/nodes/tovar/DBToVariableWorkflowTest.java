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
package com.vernalis.knime.db.nodes.tovar;

import com.vernalis.testing.AbstractWorkflowTest;
import com.vernalis.testing.NodeTestFlow;

/**
 * Test case to run the test workflow for the 'DB Data To Variable' node
 *
 * @author S Roughley
 *
 * @since 07-Sep-2022
 * @since v1.36.0
 * @since 07-Sep-2022
 * @since v1.36.0
 */
@NodeTestFlow("com.vernalis.knime.db.nodes.tovar.DBDataToVariableNodeFactory")
@NodeTestFlow("com.vernalis.knime.db.nodes.tovar.DBSessionToVariableNodeFactory")
public class DBToVariableWorkflowTest extends AbstractWorkflowTest {

	/**
	 * Public constructor for test class
	 *
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 * @since 07-Sep-2022
	 * @since v1.36.0
	 */
	public DBToVariableWorkflowTest() {
		super("src/knime/DBToVariable");
	}
}
