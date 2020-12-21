/*******************************************************************************
 * Copyright (c) 2018,2020 Vernalis (R&D) Ltd
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
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
	 * Wrapper for {@link Arrays#fill(boolean[], boolean)}, returning the filled
	 * array, replaces the following:
	 *
	 * <pre>
	 *     boolean[] arr = new boolean[3];
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
	 *     boolean[] arr = new boolean[3];
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
	public static boolean[] fill(boolean[] a, boolean val) {
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
	 *     boolean[] arr = new boolean[7];
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
	public static boolean[] of(boolean val, int size) {
		return fill(new boolean[size], val);
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
	 * @return A pre-filled array, all containing the <em>same</em> object
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
		final T val = supplier.get();
		@SuppressWarnings("unchecked")
		final T[] a = (T[]) Array.newInstance(val.getClass(), size);
		a[0] = val;
		for (int i = 1; i < a.length; i++) {
			a[i] = supplier.get();
		}
		return a;
	}

	/**
	 * Method to remove any {@code null} elements from an array
	 * 
	 * @param a
	 *            The array
	 * @return The array without {@code null}s
	 */
	public static <T> T[] trimNulls(T[] a) {
		List<T> l = Arrays.stream(a).filter(t -> t != null)
				.collect(Collectors.toList());
		@SuppressWarnings("unchecked")
		T[] retVal = (T[]) Array.newInstance(a.getClass().getComponentType(),
				l.size());
		return l.toArray(retVal);
	}

	/**
	 * Returns a copy of the bit-reverse permuted input array (See
	 * https://en.wikipedia.org/wiki/Bit-reversal_permutation), which is useful
	 * in e.g. Fast-Fourier Transforms
	 * 
	 * @param a
	 *            The array to permute
	 * @return A permuted copy
	 */
	public static <T> T[] bitReversePermute(T[] a) {
		if (Integer.highestOneBit(a.length) != a.length) {
			throw new IllegalArgumentException("Array must be of length 2^k");
		}
		@SuppressWarnings("unchecked")
		final T[] r = (T[]) Array.newInstance(a.getClass().getComponentType(),
				a.length);
		int shift = Integer.numberOfLeadingZeros(a.length) + 1;
		for (int i = 0; i < a.length; i++) {
			r[Integer.reverse(i) >>> shift] = a[i];
		}
		return r;
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(long[], int)} to copy whole array
	 *
	 * <pre>
	 * long[] a = ...
	 * ...
	 * long[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * long[] a = ...
	 * ...
	 * long[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static long[] copy(long[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(int[], int)} to copy whole array
	 *
	 * <pre>
	 * int[] a = ...
	 * ...
	 * int[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * int[] a = ...
	 * ...
	 * int[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static int[] copy(int[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(short[], int)} to copy whole array
	 *
	 * <pre>
	 * short[] a = ...
	 * ...
	 * short[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * short[] a = ...
	 * ...
	 * short[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static short[] copy(short[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(char[], int)} to copy whole array
	 *
	 * <pre>
	 * char[] a = ...
	 * ...
	 * char[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * char[] a = ...
	 * ...
	 * char[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static char[] copy(char[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(byte[], int)} to copy whole array
	 *
	 * <pre>
	 * byte[] a = ...
	 * ...
	 * byte[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * byte[] a = ...
	 * ...
	 * byte[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static byte[] copy(byte[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(boolean[], int)} to copy whole array
	 *
	 * <pre>
	 * boolean[] a = ...
	 * ...
	 * boolean[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * boolean[] a = ...
	 * ...
	 * boolean[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static boolean[] copy(boolean[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(long[], int)} to copy whole array
	 *
	 * <pre>
	 * float[] a = ...
	 * ...
	 * float[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * float[] a = ...
	 * ...
	 * float[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static float[] copy(float[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(long[], int)} to copy whole array
	 *
	 * <pre>
	 * double[] a = ...
	 * ...
	 * double[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * double[] a = ...
	 * ...
	 * double[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static double[] copy(double[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Wrapper for {@link Arrays#copyOf(Object[], int)} to copy whole array
	 *
	 * <pre>
	 * T[] a = ...
	 * ...
	 * T[] b = ArrayUtils.copy(a);
	 * </pre>
	 *
	 * is equivalent to
	 *
	 * <pre>
	 * T[] a = ...
	 * ...
	 * T[b] = Arrays.copyOf(a, a.length);
	 * </pre>
	 *
	 * where T is the class of an object
	 *
	 * @param a
	 *            incoming array
	 * @return new copied array
	 */
	public static <T> T[] copy(T[] a) {
		return Arrays.copyOf(a, a.length);
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static long[] removeEntry(long[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final long[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static int[] removeEntry(int[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final int[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static short[] removeEntry(short[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final short[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static char[] removeEntry(char[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final char[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static byte[] removeEntry(byte[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final byte[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static boolean[] removeEntry(boolean[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final boolean[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static float[] removeEntry(float[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final float[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static double[] removeEntry(double[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final double[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}

	/**
	 * Method to remove an entry from an array and return the new array
	 * 
	 * @param a
	 *            The incoming array
	 * @param index
	 *            The array to copy
	 * @return the newly copied array with one fewer elements
	 */
	public static <T> T[] removeEntry(T[] a, int index) {
		if (index < 0 || index >= a.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		final T[] retVal = Arrays.copyOf(a, a.length - 1);
		if (retVal.length != index) {
			System.arraycopy(a, index + 1, retVal, index,
					retVal.length - index);
		}
		return retVal;
	}
}
