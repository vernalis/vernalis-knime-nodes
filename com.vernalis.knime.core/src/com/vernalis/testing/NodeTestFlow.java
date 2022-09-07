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
