package com.vernalis.testing;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A marker annotation to indicate that the class or method should not have a
 * test case generated. The annotation is a source annotation so has no
 * compilation effect but allows source-code processing workflows to skip the
 * type or method during automatic test template creation.
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
@Retention(SOURCE)
@Target({ TYPE, METHOD })
public @interface NoTest {

}
