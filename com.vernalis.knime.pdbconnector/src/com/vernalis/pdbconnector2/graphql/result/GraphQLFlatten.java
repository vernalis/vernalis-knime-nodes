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
 * This annotation is used to indicate that an intermediate result of iterable
 * queries should have any iterable results added directly to the parent rather
 * than as nested lists. It is ignored if the method or field is not also
 * annotated with GraphQLIterate
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 * @see GraphQLResult
 * @see GraphQLIterate
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface GraphQLFlatten {

	/**
	 * @return true if nested iterables should be flattened
	 */
	boolean value() default true;
}
