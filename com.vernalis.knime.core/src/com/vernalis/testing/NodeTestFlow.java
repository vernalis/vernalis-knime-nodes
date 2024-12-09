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
package com.vernalis.testing;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A marker annotation to indicate the node factory class of the node tested.
 * The annotation is a source annotation so has no compilation effect but allows
 * source-code processing to determine node coverage. Multiple annotations
 * should be provided when a single test covers multiple nodes
 * 
 * If a fully qualified class name is not provided it should be assumed to be in
 * the same package as the test class
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@Retention(SOURCE)
@Target({ TYPE, METHOD })
@Repeatable(NodeTestFlows.class)
public @interface NodeTestFlow {

	/**
	 * @return The node factory class name
	 */
	String value();
}
