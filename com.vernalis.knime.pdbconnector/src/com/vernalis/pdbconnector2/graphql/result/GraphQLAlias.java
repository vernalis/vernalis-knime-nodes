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
 * This annotation is processed by GraphQLResult to return fields with a name
 * different to their query path
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 * @see GraphQLResult
 * @see GraphQLIterate
 * @see GraphQLAlias
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface GraphQLAlias {

	/**
	 * @return A '.'-delimited query path which should use this field or method
	 */
	public String value();
}
