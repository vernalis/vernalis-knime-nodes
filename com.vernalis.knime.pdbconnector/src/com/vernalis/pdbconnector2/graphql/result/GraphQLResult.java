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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This interface marks the implementing class as the result of a GraphQL. It
 * contains a default implementation of {@link #getResultValue(String)}. Result
 * fields are queryable using a '.'-delimited path
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 * @see GraphQLAlias
 * @see GraphQLRoot
 * @see GraphQLIterate
 * @see GraphQLFlatten
 */
public interface GraphQLResult {

	/**
	 * This method returns a query result object as follows:
	 * <ul>
	 * <li>If the implementing class is annotated with {@link GraphQLRoot}:
	 * <ul>
	 * <li>If the query path is the same as the annotated value then this object
	 * is returned</li>
	 * <li>Otherwise, if the query path starts with the annotated value, this is
	 * removed from the start of the path before proceeding as below</li>
	 * </ul>
	 * </li>
	 * <li>Methods are checked to see if any method is annotated with
	 * {@link GraphQLAlias}:
	 * <ul>
	 * <li>If the annotation value is the same as the remaining path <i>or</i>
	 * starts the path followed by a '.' then the return value of this method is
	 * used</li>
	 * </ul>
	 * </li>
	 * 
	 * <li>If no matching method is found, a field is selected which either has:
	 * <ul>
	 * <li>the same name as the rest of the path, or starts the path, followed
	 * by a '.'</li>
	 * <li>OR if a field is annotated with {@link JsonProperty} then the
	 * annotation value is used instead of the fieldname</li>
	 * <li>OR is a field is annotated with {@link GraphQLAlias} then that
	 * annotation value is used instead of the fieldname</li>
	 * </ul>
	 * </li>
	 * <li>The obtained value is treated as follows:
	 * <ul>
	 * <li>If the resulting field value is {@code null} then that is returned -
	 * there was no result for the query path</li>
	 * <li>If no path remains, we have reached our destination and the field
	 * value is returned</li>
	 * <li>The used path portion is removed from the start of the path</li>
	 * <li>If any path remains, then:
	 * <ul>
	 * <li>If the field is an instance of {@link GraphQLResult} we recurse until
	 * a result is obtained</li>
	 * <li>If the field is an instance of {@link Iterable} and the elements are
	 * instances of {@link GraphQLResult} then we return a either:
	 * <ul>
	 * <li>If method or field used to obtain the result was annotated with
	 * {@link GraphQLIterate} then the subqueries contained are recursed into a
	 * {@code List<Object>}. If the field or method was also marked with
	 * {@link GraphQLFlatten} then any nested Lists are all flattened to a
	 * single top-level list</li>
	 * <li>Or return a {@link GraphQLResultIterator} containing the field's
	 * iterator and the remaining path for the caller to parse on each value in
	 * turn</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * </li>
	 * <li>Otherwise we have an error and throw an exception</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param path
	 *            '.'-delimited query path
	 * @return The result - see above for details
	 * @throws GraphQLResultException
	 *             If there was an error accessing the field
	 */
	@SuppressWarnings("unchecked")
	public default Object getResultValue(String path)
			throws GraphQLResultException {

		if (path == null || path.isEmpty()) {
			return null;
		}

		// Now handle possible @GraphQLRoot annotation
		String path0 = path;
		if (getClass().isAnnotationPresent(GraphQLRoot.class)) {
			for (String root : getClass().getAnnotation(GraphQLRoot.class)
					.value()) {
				if (path.equals(root)) {
					// The query points to this!
					return this;
				}
				if (path.startsWith(root + ".")) {
					path0 = path.replaceFirst("^\\Q" + root + ".\\E", "");
					break;
				}
			}
		}

		String remainingPath = null;
		Object retVal = null;
		Class<?> retValIterableType = null;
		boolean iterateResult = false;
		boolean flattenIterable = false;

		try {
			// Try to find the value...
			if (remainingPath == null) {
				// Firstly check for any methods annotated accordingly
				Method resultMethod = null;
				for (Method m : getClass().getDeclaredMethods()) {
					m.setAccessible(true);
					// We only use GraphQLAlias here - otherwise a JsonProperty
					// annotation on a method would override a GraphQLAlias
					// annotation on a field
					String prefix = m.isAnnotationPresent(GraphQLAlias.class)
							? prefix =
									m.getAnnotation(GraphQLAlias.class).value()
							: null;
					if (prefix != null) {
						if (path0.equals(prefix)) {
							// Exact match with an alias
							remainingPath = "";
							resultMethod = m;
							break;
						} else if (path0.startsWith(prefix + ".")) {
							remainingPath = path0
									.replaceFirst("^\\Q" + prefix + ".\\E", "");
							resultMethod = m;
							break;
						}
					}
				}
				if (resultMethod != null) {
					try {
						retVal = resultMethod.invoke(this);
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e1) {
						// We will try the fields instead before giving up...
						remainingPath = null;
					}

					// If we have an iterable we need to know what it is of...
					if (retVal instanceof Iterable) {
						Type elementType = resultMethod.getGenericReturnType();
						try {
							retValIterableType =
									elementType instanceof ParameterizedType
											? (Class<?>) ((ParameterizedType) elementType)
													.getActualTypeArguments()[0]
											: Class.forName(
													elementType.getTypeName());

						} catch (ClassNotFoundException e) {
							throw new GraphQLResultException(path);
						}
						iterateResult = resultMethod
								.isAnnotationPresent(GraphQLIterate.class)
										? resultMethod
												.getAnnotation(
														GraphQLIterate.class)
												.value()
										: iterateResult;
						if (iterateResult && resultMethod
								.isAnnotationPresent(GraphQLFlatten.class)) {
							flattenIterable = resultMethod
									.getAnnotation(GraphQLFlatten.class)
									.value();
						}
					}
				}
			}

			// If we didnt find an annotated method then try the fields
			if (remainingPath == null) {
				// Look for the field we want...
				Field resultField = null;

				for (Field f : getClass().getDeclaredFields()) {
					f.setAccessible(true);
					String prefix;
					// GraphQLAlias annotation should be used in preference to
					// fieldname, failing that JsonProperty annotation, failing
					// that, actual field name
					if (f.isAnnotationPresent(GraphQLAlias.class)) {
						prefix = f.getAnnotation(GraphQLAlias.class).value();
					} else if (f.isAnnotationPresent(JsonProperty.class)) {
						prefix = f.getAnnotation(JsonProperty.class).value();
					} else {
						prefix = f.getName();
					}
					if (path0.equals(prefix)) {
						// Exact match with an alias
						remainingPath = "";
						resultField = f;
						break;
					} else if (path0.startsWith(prefix + ".")) {
						remainingPath = path0
								.replaceFirst("^\\Q" + prefix + ".\\E", "");
						resultField = f;
						break;
					}
				}
				if (resultField == null) {
					// We didnt find a match for the query path
					throw new GraphQLResultException(path);
				}

				// Try to get the value
				try {
					retVal = resultField.get(this);
					if (retVal instanceof Iterable) {
						Type elementType = resultField.getGenericType();
						retValIterableType =
								elementType instanceof ParameterizedType
										? (Class<?>) ((ParameterizedType) elementType)
												.getActualTypeArguments()[0]
										: Class.forName(
												elementType.getTypeName());
						iterateResult = resultField
								.isAnnotationPresent(GraphQLIterate.class)
										? resultField
												.getAnnotation(
														GraphQLIterate.class)
												.value()
										: iterateResult;
						if (iterateResult && resultField
								.isAnnotationPresent(GraphQLFlatten.class)) {
							flattenIterable = resultField
									.getAnnotation(GraphQLFlatten.class)
									.value();
						}
					}
				} catch (ClassNotFoundException | SecurityException
						| IllegalArgumentException | IllegalAccessException e) {
					throw new GraphQLResultException(path);
				}
			}

			// Now do something with the obtained value
			if (retVal == null) {
				// No result for the query
				return null;
			}

			if (!remainingPath.isEmpty()) {
				// More layers.... keep digging if we can..
				if (retVal instanceof GraphQLResult) {
					try {
						GraphQLResult subResult = (GraphQLResult) retVal;
						return subResult.getResultValue(remainingPath);
					} catch (GraphQLResultException e) {
						throw new GraphQLResultException(path, e);
					}
				} else if (retVal instanceof Iterable) {
					// We should have an Iterable<? extends GraphQLResult> but
					// check
					// first...
					if (GraphQLResult.class
							.isAssignableFrom(retValIterableType)) {
						if (iterateResult) {
							List<Object> retList = new ArrayList<>();
							Iterator<? extends GraphQLResult> iter =
									((Iterable<? extends GraphQLResult>) retVal)
											.iterator();
							while (iter.hasNext()) {
								GraphQLResult subResult = iter.next();
								try {

									final Object subresultValue = subResult
											.getResultValue(remainingPath);
									if (flattenIterable
											&& subresultValue instanceof Iterable) {
										((Iterable<?>) subresultValue)
												.iterator().forEachRemaining(
														x -> retList.add(x));
									} else {
										retList.add(subresultValue);
									}
								} catch (GraphQLResultException e) {
									throw new GraphQLResultException(path, e);
								}
							}
							return retList;
						} else {
							return new GraphQLResultIterator(
									(Iterable<? extends GraphQLResult>) retVal,
									remainingPath);
						}
					} else {
						throw new GraphQLResultException(path);
					}
				} else {
					// Should have been one of the above...
					throw new GraphQLResultException(path);
				}
			}
			// We have reached our destination...
			return retVal;
		} catch (GraphQLResultException e) {
			throw new GraphQLResultException(
					"Error parsing query '" + path + "'");
		}
	}
}
