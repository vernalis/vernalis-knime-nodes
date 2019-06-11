/*******************************************************************************
 * Copyright (c) 2018, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * A utility class of static helper functions relating to Arrays. Many of these
 * wrap methods from {@link Arrays}
 * 
 * @author S.Roughley
 *
 */
public class ArrayUtils {

	private ArrayUtils() {
		// Utility Class - Do not Instantiate
	}

	/**
	 * Wrapper for {@link Arrays#fill(long[], long)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     long[] arr = new long[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     long[] arr = new long[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static long[] fill(long[] a, long val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(int[], int)}, returning the filled array,
	 * replaces the following:
	 * 
	 * <pre>
	 *     int[] arr = new int[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     int[] arr = new int[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static int[] fill(int[] a, int val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(short[], short)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     short[] arr = new short[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     short[] arr = new short[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static short[] fill(short[] a, short val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(char[], char)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     char[] arr = new char[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     char[] arr = new char[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static char[] fill(char[] a, char val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(byte[], byte)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     byte[] arr = new byte[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     byte[] arr = new byte[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static byte[] fill(byte[] a, byte val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(float[], float)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     float[] arr = new float[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     float[] arr = new float[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static float[] fill(float[] a, float val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(double[], double)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     double[] arr = new double[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     double[] arr = new double[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static double[] fill(double[] a, double val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Wrapper for {@link Arrays#fill(Object[], Object)}, returning the filled
	 * array, replaces the following:
	 * 
	 * <pre>
	 *     SomeType[] arr = new SomeType[3];
	 *     if (condition) {
	 *         Arrays.fill(arr, defaultValue);
	 *         return arr;
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * with
	 * 
	 * <pre>
	 *     SomeType[] arr = new SomeType[3];
	 *     if (condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 *     return arr;
	 * </pre>
	 * 
	 * @param a
	 *            The array to fill
	 * @param val
	 *            The value to fill with
	 * @return The filled array
	 */
	public static <T> T[] fill(T[] a, T val) {
		Arrays.fill(a, val);
		return a;
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     long[] arr = new long[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static long[] of(long val, int size) {
		return fill(new long[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     int[] arr = new int[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static int[] of(int val, int size) {
		return fill(new int[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     short[] arr = new short[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static short[] of(short val, int size) {
		return fill(new short[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     char[] arr = new char[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static char[] of(char val, int size) {
		return fill(new char[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     byte[] arr = new byte[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static byte[] of(byte val, int size) {
		return fill(new byte[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     float[] arr = new float[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static float[] of(float val, int size) {
		return fill(new float[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     double[] arr = new double[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array
	 */
	public static double[] of(double val, int size) {
		return fill(new double[size], val);
	}

	/**
	 * Convenience method to return an array of given size, pre-filled with a
	 * default value. The following code:
	 * 
	 * <pre>
	 *     if(condition) {
	 *         return ArrayUtils.of(defaultValue, 7);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * is equivalent to:
	 * 
	 * <pre>
	 *     SomeType[] arr = new SomeType[7];
	 *     if(condition) {
	 *         return ArrayUtils.fill(arr, defaultValue);
	 *     }
	 *     ...
	 * </pre>
	 * 
	 * @param val
	 *            The value to fill the array with
	 * @param size
	 *            The size of the array
	 * @return A pre-filled array, all containing the same object
	 * @see #of(Supplier, int)
	 */
	public static <T> T[] of(T val, int size) {
		@SuppressWarnings("unchecked")
		final T[] a = (T[]) Array.newInstance(val.getClass(), size);
		return fill(a, val);
	}

	/**
	 * A method to return a new, prefilled array, with each position filled by
	 * an object from a {@link Supplier}
	 * 
	 * @param supplier
	 *            The object Supplier
	 * @param size
	 *            The size of the result array
	 * @return A new, prefilled array
	 * @see #of(Object, int)
	 */
	public static <T> T[] of(Supplier<T> supplier, int size) {
		T val = supplier.get();
		@SuppressWarnings("unchecked")
		final T[] a = (T[]) Array.newInstance(val.getClass(), size);
		a[0] = val;
		for (int i = 1; i < a.length; i++) {
			a[i] = supplier.get();
		}
		return a;
	}
}
