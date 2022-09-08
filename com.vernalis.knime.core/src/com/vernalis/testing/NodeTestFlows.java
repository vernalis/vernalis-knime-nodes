package com.vernalis.testing;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * An annotation to collect multiple NodeTestFlow annotations
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@Retention(SOURCE)
@Target({ TYPE, METHOD })
public @interface NodeTestFlows {

	/**
	 * @return the individual test flow annotations
	 */
	NodeTestFlow[] value();
}
