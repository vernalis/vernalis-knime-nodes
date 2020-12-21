/**
 * 
 */
package com.vernalis.pdbconnector2.graphql.result;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation indicates to the result parser that the returned intermediate
 * query (which will be an {@code Iterable<? extends GraphQLResult>}) should be
 * iterated to find all subqueries
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 * @see GraphQLResult
 * @see GraphQLFlatten
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface GraphQLIterate {

	/**
	 * @return the value, which defaults to {@code true}
	 */
	public boolean value() default true;
}
