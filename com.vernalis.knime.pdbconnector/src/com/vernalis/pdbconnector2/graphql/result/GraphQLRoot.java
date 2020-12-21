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
package com.vernalis.pdbconnector2.graphql.result;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation serves to indicate that the annotated type is a root level of
 * a query result, e.g.
 * 
 * <pre>
 * &#64;GraphQLRoot("entry")
 * public class QueryResult implements GraphQLResult {
 * 
 * 	public String value;
 * }
 * </pre>
 * 
 * will return the object '{@code value}' when
 * '{@code GraphQLResult#getResultValue("entry.value")}' is called. Without the
 * annotation, this would fail
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface GraphQLRoot {

	/**
	 * @return The values for the annotation
	 */
	public String[] value();
}
