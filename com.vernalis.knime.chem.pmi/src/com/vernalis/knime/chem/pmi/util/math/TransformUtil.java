/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
 *  This program is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU General Public License, Version 3, as 
 *  published by the Free Software Foundation.
 *  
 *   This program is distributed in the hope that it will be useful, but 
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *   
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>
 ******************************************************************************/
package com.vernalis.knime.chem.pmi.util.math;

/**
 * Utility class to generate the transformation matrix to align a molecule to
 * its inertial reference frame
 * 
 * @author s.roughley
 * 
 */
public class TransformUtil {
	private TransformUtil() {
		// dont insantiate!
	}

	private static int MAX_SWEEPS = 50;

	/**
	 * <p>
	 * Method to generate a rotation matrix (as a single row matrix) to align
	 * the principal axes defined by the supplier covariance matrix to the
	 * co-ordinate axes.
	 * </p>
	 * <p>
	 * This is based on ob_make_rmat from
	 * http://sourceforge.net/p/openbabel/code
	 * /HEAD/tree/openbabel/trunk/src/obutil.cpp
	 * </p>
	 * 
	 * @param inertialTensor
	 *            The inertial tensor (or other weighted covariance matrix)
	 * @return The rotation matrix as a single row matrix, to rotate the
	 *         principal axes to align with the co-ordinate axes
	 * @throws IllegalArgumentException
	 *             if the matrix is not 3x3
	 */
	public static double[] makeRotationMatrix(double[][] inertialTensor)
			throws IllegalArgumentException {
		if (inertialTensor.length != 3 || inertialTensor[0].length != 3) {
			throw new IllegalArgumentException("Inertial Tensor must be a 3x3 matrix");
		}

		double onorm, dnorm;
		double b, dma, q, t, c, s;
		double[] d = new double[3];// Starts of with the diagonals, which will
									// become the eigenvalues
		double atemp, vtemp, dtemp;
		double[][] v = new double[3][3]; // Will hold the eigenvectors
		double[] r1 = new double[3], r2 = new double[3], v1 = new double[3], v2 = new double[3],
				v3 = new double[3];
		// int i, j, k, l;

		for (int j = 0; j < 3; ++j) {
			for (int i = 0; i < 3; ++i) {
				v[i][j] = 0.0;
			}
			v[j][j] = 1.0;
			d[j] = inertialTensor[j][j];
		}

		for (int l = 1; l <= MAX_SWEEPS; ++l) {
			dnorm = 0.0;
			onorm = 0.0;
			for (int j = 0; j < 3; ++j) {
				// dnorm is the sum of the diagonals
				dnorm += Math.abs(d[j]);
				for (int i = 0; i <= j - 1; ++i) {
					// onorm adds the off-diagonals
					onorm += Math.abs(inertialTensor[i][j]);
				}
			}
			if ((onorm / dnorm) <= 1.0e-12) {
				// We've got there...
				break;
			}
			for (int j = 0; j < 3; ++j) {
				for (int i = 0; i <= j - 1; ++i) {
					b = inertialTensor[i][j];
					if (Math.abs(b) > 0.0) {
						dma = d[j] - d[i];
						if (Math.abs(dma) + Math.abs(b) <= Math.abs(dma)) {
							t = b / dma;
						} else {
							q = 0.5 * dma / b;
							t = 1.0 / (Math.abs(q) + Math.sqrt(1.0 + q * q));
							if (q < 0.0) {
								t = -t;
							}
						}
						c = 1.0 / Math.sqrt(t * t + 1.0);
						s = t * c;
						inertialTensor[i][j] = 0.0;
						// DO the Jacobi rotation
						for (int k = 0; k <= i - 1; ++k) {
							atemp = c * inertialTensor[k][i] - s * inertialTensor[k][j];
							inertialTensor[k][j] = s * inertialTensor[k][i]
									+ c * inertialTensor[k][j];
							inertialTensor[k][i] = atemp;
						}
						for (int k = i + 1; k <= j - 1; ++k) {
							atemp = c * inertialTensor[i][k] - s * inertialTensor[k][j];
							inertialTensor[j][k] = s * inertialTensor[i][k]
									+ c * inertialTensor[k][j];
							inertialTensor[i][k] = atemp;
						}
						for (int k = j + 1; k < 3; ++k) {
							atemp = c * inertialTensor[i][k] - s * inertialTensor[j][k];
							inertialTensor[j][k] = s * inertialTensor[i][k]
									+ c * inertialTensor[j][k];
							inertialTensor[i][k] = atemp;
						}
						// And now rotate the unit vector reference frame
						for (int k = 0; k < 3; ++k) {
							vtemp = c * v[k][i] - s * v[k][j];
							v[k][j] = s * v[k][i] + c * v[k][j];
							v[k][i] = vtemp;
						}
						// And finally the diagonals themselves
						dtemp = c * c * d[i] + s * s * d[j] - 2.0 * c * s * b;
						d[j] = s * s * d[i] + c * c * d[j] + 2.0 * c * s * b;
						d[i] = dtemp;
					} /* end if */
				} /* end for i */
			} /* end for j */
		} /* end for l */

		// Now sort the eigenvalues and eigenvectors
		for (int j = 0; j < 3 - 1; ++j) {
			int k = j;
			dtemp = d[k];
			for (int i = j + 1; i < 3; ++i) {
				if (d[i] < dtemp) {
					k = i;
					dtemp = d[k];
				}
			}
			if (k > j) {
				d[k] = d[j];
				d[j] = dtemp;
				for (int i = 0; i < 3; ++i) {
					dtemp = v[i][k];
					v[i][k] = v[i][j];
					v[i][j] = dtemp;
				}
			}
		}

		// Transfer the 1st two eigenvectors held in the matrix to r1 and r2
		for (int i = 0; i < 3; i++) {
			r1[i] = v[i][0];
			r2[i] = v[i][1];
		}

		// The 3rd unit vector - cross product of the 1st two vectors
		v3[0] = r1[1] * r2[2] - r1[2] * r2[1];
		v3[1] = -r1[0] * r2[2] + r1[2] * r2[0];
		v3[2] = r1[0] * r2[1] - r1[1] * r2[0];

		// Normalise!
		s = Math.sqrt(v3[0] * v3[0] + v3[1] * v3[1] + v3[2] * v3[2]);
		v3[0] /= s;
		v3[1] /= s;
		v3[2] /= s;

		// The 2nd unit vector - cross product of v3 and the 1st (r1)
		v2[0] = v3[1] * r1[2] - v3[2] * r1[1];
		v2[1] = -v3[0] * r1[2] + v3[2] * r1[0];
		v2[2] = v2[0] * r1[1] - v3[1] * r1[0];

		// Normalise
		s = Math.sqrt(v2[0] * v2[0] + v2[1] * v2[1] + v2[2] * v2[2]);
		v2[0] /= s;
		v2[1] /= s;
		v2[2] /= s;

		// The 3rd - v1 x v2
		v1[0] = v2[1] * v3[2] - v2[2] * v3[1];
		v1[1] = -v2[0] * v3[2] + v2[2] * v3[0];
		v1[2] = v2[0] * v3[1] - v2[1] * v3[0];

		// Normalise
		s = Math.sqrt(v1[0] * v1[0] + v1[1] * v1[1] + v1[2] * v1[2]);
		v1[0] /= s;
		v1[1] /= s;
		v1[2] /= s;

		double[] retVal = new double[9];
		for (int i = 0; i < 3; i++) {
			retVal[i] = v1[i];
			retVal[i + 3] = v2[i];
			retVal[i + 6] = v3[i];
		}
		return retVal;
	}
}
