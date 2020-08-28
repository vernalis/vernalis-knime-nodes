/*******************************************************************************
 * Copyright (c) 2019, Vernalis (R&D) Ltd
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
package com.vernalis.knime.chem.pmi.nodes.plot.kerneldensity;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;

import com.vernalis.knime.chem.util.points.Point;
import com.vernalis.knime.iterators.NestedCounterIterator;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.DoubleSummary;
import com.vernalis.knime.plot.nodes.kerneldensity.BandwidthEstimationMethod;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelDensityFunction;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelEstimator;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelException;
import com.vernalis.knime.plot.nodes.kerneldensity.KernelSymmetry;

public class PmiKernelDensityFunction extends KernelDensityFunction {

	private final boolean showFullTriangle;

	/**
	 * @param gridSize
	 * @param kEst
	 * @param kSymm
	 * @param bandwidthRuleOfThumb
	 * @param h
	 */
	public PmiKernelDensityFunction(int gridSize, KernelEstimator kEst,
			KernelSymmetry kSymm,
			BandwidthEstimationMethod[] bandwidthRuleOfThumb, Double[] h,
			boolean showFullTriangle) {
		super(2, gridSize, kEst, kSymm, bandwidthRuleOfThumb, h);
		this.showFullTriangle = showFullTriangle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.pmi.util.kerneldensity.KernelDensityFunction#
	 * autoRangeKernel(double, boolean)
	 */
	@Override
	public synchronized void autoRangeKernel(double rangeFactor,
			boolean overwrite) throws IllegalStateException {
		if (isClosed) {
			throw new IllegalStateException(
					"The kernel is closed and cannot be re-ranged");
		}
		for (int dim = 0; dim < getNumDimensions(); dim++) {
			if (dimensionBounds[dim] == null || overwrite) {
				if (showFullTriangle) {
					switch (dim) {
					case 0:
						dimensionBounds[dim] = createPoint(2);
						dimensionBounds[dim].setCoordinate(0,
								0.0 - rangeFactor);
						dimensionBounds[dim].setCoordinate(1,
								1.0 + rangeFactor);
						break;
					case 1:
						dimensionBounds[dim] = createPoint(2);
						dimensionBounds[dim].setCoordinate(0,
								0.5 - rangeFactor / 2);
						dimensionBounds[dim].setCoordinate(1,
								1.0 + rangeFactor / 2);
					default:
						break;
					}
				} else {
					final DoubleSummary dimSummary =
							dataSummary.getSummary(dim);
					double margin = dimSummary.getCount() == 1
							? ((1 + rangeFactor)
									* getBandwidthMatrix()[dim][dim])
							: (rangeFactor * (dimSummary.getMax()
									- dimSummary.getMin()));
					dimensionBounds[dim] = createPoint(2);
					dimensionBounds[dim].setCoordinate(0,
							dimSummary.getMin() - margin);
					dimensionBounds[dim].setCoordinate(1,
							dimSummary.getMax() + margin);

				}
			}

		}
	}

	/**
	 * Method to generate the kernel grid points. once this method has been
	 * called, the kernel is closed to new point addition. The grid will not be
	 * recalculated by subsequent calls. In the PMI version, we miss out points
	 * outside the PMI Bounds triangle
	 * 
	 * @param rangeFactor
	 *            The fraction of the actual dimension range to add as a
	 *            'margin'
	 * @throws KernelException
	 *             If there are insufficient data points to estimate a bandwidth
	 * @throws CanceledExecutionException
	 *             If the user cancelled during calculation
	 */
	@Override
	public synchronized void generateKDEGrid(double rangeFactor,
			ExecutionMonitor exec)
			throws KernelException, CanceledExecutionException {
		if (isClosed) {
			// Already done calculation...
			return;
		}
		// Set the range before closing - before, because otherwise we would be
		// able to change the range of a closed kernel:
		autoRangeKernel(rangeFactor);

		// Before going any further we now need
		// Stop anything else being added or changed
		isClosed = true;
		initialiseH();

		// N * H0 * H1 *... * Hd
		double divisor = IntStream.range(0, getNumDimensions())
				.mapToDouble(dim -> H[dim][dim])
				.reduce(data.size(), (a, b) -> a * b);

		double[] stepSizes = Arrays.stream(dimensionBounds)
				.mapToDouble(
						rng -> (rng.getCoordinate(1) - rng.getCoordinate(0))
								/ (gridSize - 1.0))
				.toArray();
		double[] gridMinima = Arrays.stream(dimensionBounds)
				.mapToDouble(rng -> rng.getCoordinate(0)).toArray();

		NestedCounterIterator iter = new NestedCounterIterator(
				ArrayUtils.of(gridSize, numDimensions));

		double progPerGridpoint = 1.0 / (getGridSize() * getGridSize());
		long count = 0l;
		while (iter.hasNext()) {
			if (exec != null) {
				exec.checkCanceled();
				exec.setProgress(count++ * progPerGridpoint);
			}
			// Loop over the grid points
			int[] gridIdx = iter.next();
			Point<?> gridPoint = createPoint(getNumDimensions() + 1);// Extra
			// dimension
			// for
			// kernel
			// intensity
			for (int dim = 0; dim < getNumDimensions(); dim++) {
				gridPoint.setCoordinate(dim,
						gridMinima[dim] + gridIdx[dim] * stepSizes[dim]);
			}

			// Now check whether it adjoins the PMI Triangle
			double x = gridPoint.getCoordinate(0);
			double y = gridPoint.getCoordinate(1);
			if (y - 0.5 * stepSizes[1] > 1
					|| y + 0.5 * stepSizes[1] + x + 0.5 * stepSizes[0] < 1
					|| y + 0.5 * stepSizes[1] < x - 0.5 * stepSizes[0]) {
				// skip this point
				continue;
			}

			double pointIntensity = 0.0;
			for (Point<PointImpl> dataPoint : data) {
				double[] u = IntStream.range(0, getNumDimensions())
						.mapToDouble(dim -> (dataPoint.getCoordinate(dim)
								- gridPoint.getCoordinate(dim)) / H[dim][dim])
						.toArray();
				final double est = kSymm.getEstimate(u, kEst);
				pointIntensity += est;
			}
			pointIntensity /= divisor;
			gridPoint.setCoordinate(getNumDimensions(), pointIntensity);
			addGridPoint(gridPoint);
		}

	}

}
