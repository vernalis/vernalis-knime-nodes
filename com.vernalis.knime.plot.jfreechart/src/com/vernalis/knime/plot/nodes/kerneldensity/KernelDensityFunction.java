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
package com.vernalis.knime.plot.nodes.kerneldensity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;

import com.vernalis.knime.chem.util.points.Point;
import com.vernalis.knime.chem.util.points.PointSummariser;
import com.vernalis.knime.iterators.NestedCounterIterator;
import com.vernalis.knime.misc.ArrayUtils;
import com.vernalis.knime.misc.DoubleSummary;

/**
 * This performs a Multivariate (including 1D) kernel density smoothing on a set
 * of points.
 *
 * See http://www.stat.rice.edu/~scottdw/ss.nh.pdf
 * https://en.wikipedia.org/wiki/Kernel_density_estimation
 * https://en.wikipedia.org/wiki/Kernel_(statistics) and
 * https://en.wikipedia.org/wiki/Multivariate_kernel_density_estimation for
 * details
 *
 * Users should construct a {@link KernelDensityFunction}, and then add points
 * using the {@link #acceptPoint(double...)} method. Optionally, the range of
 * coordinates for the grid can be specified manually using
 * {@link #setKernelRange(int, double, double)} and then the
 * {@link KernelDensityFunction} grid can be calculated. Once the grid has been
 * calculated, it is not possible to add further datapoints
 *
 * @author S.Roughley knime@vernalis.com
 *
 */
public class KernelDensityFunction {

	/**
	 * A simple multi-dimensional implementation of {@link Point}
	 * 
	 * @author s.roughley
	 *
	 */
	protected static class PointImpl implements Point<PointImpl> {

		protected final double[] coords;

		/**
		 * Constructor from a set of coordinates
		 * 
		 * @param coords
		 *            The coordinates of the point, which determines the
		 *            dimensions
		 */
		protected PointImpl(double... coords) {
			this.coords = Arrays.copyOf(coords, coords.length);
		}

		/**
		 * Constructor where the number of dimensions is known but the actual
		 * coordinates are not known. Defaults to the cartesian origin
		 * 
		 * @param numDimensions
		 *            The number of dimensions
		 */
		protected PointImpl(int numDimensions) {
			this.coords = new double[numDimensions];
		}

		/**
		 * @param dim
		 *            The dimension
		 * @param val
		 *            The coordinate value
		 */
		@Override
		public void setCoordinate(int dim, double val) {
			coords[dim] = val;
		}

		@Override
		public int getNumDimensions() {
			return coords.length;
		}

		@Override
		public double[] getCoordinates() {
			return ArrayUtils.copy(coords);
		}

		@Override
		public PointImpl normalise() {
			double magnitude = IntStream.range(0, getNumDimensions())
					.mapToDouble(d -> getCoordinate(d)).map(x -> x * x).sum();
			return new PointImpl(
					Arrays.stream(coords).map(c -> c / magnitude).toArray());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Arrays.hashCode(coords);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof PointImpl)) {
				return false;
			}
			PointImpl other = (PointImpl) obj;

			return Arrays.equals(coords, other.coords);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("PointImpl [");
			builder.append(getNumDimensions()).append("D ");
			if (coords != null) {
				builder.append(Arrays.toString(coords).replace("[", "(")
						.replace("]", ")"));
			}
			builder.append("]");
			return builder.toString();
		}

	}

	protected final int numDimensions;
	protected final double[][] H;
	protected final Point<?>[] dimensionBounds;
	protected final List<PointImpl> data = new ArrayList<>();
	protected final PointSummariser<PointImpl> dataSummary =
			new PointSummariser<>();
	protected final List<PointImpl> gridPts = new ArrayList<>();
	protected final PointSummariser<PointImpl> gridSummary =
			new PointSummariser<>();

	protected int gridSize;
	protected double[] gridSteps;

	protected final KernelEstimator kEst;
	protected final KernelSymmetry kSymm;
	protected final BandwidthEstimationMethod[] bandwidthRoT;
	protected volatile boolean isClosed;

	/**
	 * Convenience constructor for a 1D kernel
	 * 
	 * @param gridSize
	 *            The number of points on the grid in each dimension
	 * @param kEst
	 *            The {@link KernelEstimator} to use
	 * @param bandwidthRuleOfThumb
	 *            The estimation method to use for the bandwidth
	 * @param h
	 *            The bandwidth - ignored if the
	 *            {@link BandwidthEstimationMethod} will generate a value
	 * @see KernelDensityFunction#KernelDensityFunction(int, int,
	 *      KernelEstimator, KernelSymmetry, BandwidthEstimationMethod[],
	 *      Double[])
	 */
	public KernelDensityFunction(int gridSize, KernelEstimator kEst,
			BandwidthEstimationMethod bandwidthRuleOfThumb, Double h) {
		this(1, gridSize, kEst, null,
				new BandwidthEstimationMethod[] { bandwidthRuleOfThumb },
				new Double[] { h });
	}

	/**
	 * Full constructor.
	 * 
	 * @param numDimensions
	 *            The number of dimensions to use
	 * @param gridSize
	 *            The number of points on the grid in each dimension
	 * @param kEst
	 *            The {@link KernelEstimator} to use
	 * @param kSymm
	 *            The {@link KernelSymmetry} method to use
	 * @param bandwidthRuleOfThumb
	 *            The estimation methods to use for the bandwidth - one for each
	 *            dimension
	 * @param h
	 *            The bandwidths - one for each dimension - ignored if the
	 *            {@link BandwidthEstimationMethod} will generate a value for
	 *            that dimension
	 */
	public KernelDensityFunction(int numDimensions, int gridSize,
			KernelEstimator kEst, KernelSymmetry kSymm,
			BandwidthEstimationMethod[] bandwidthRuleOfThumb, Double[] h) {

		if (kEst == null) {
			this.kEst = KernelEstimator.getDefault();
		} else {
			this.kEst = kEst;
		}

		if (kSymm == null) {
			this.kSymm = KernelSymmetry.getDefault();
		} else {
			this.kSymm = kSymm;
		}

		if (gridSize < 1) {
			throw new IllegalArgumentException(
					"Must have at least one grid point");
		}
		this.gridSize = gridSize;

		if (numDimensions < 1) {
			throw new IllegalArgumentException(
					"Must have at least one dimension!");
		}
		this.numDimensions = numDimensions;

		if (h != null && h.length != this.numDimensions) {
			throw new IllegalArgumentException(
					"If bandwidths are supplied there must be the same number as dimensions");
		}
		if (bandwidthRuleOfThumb != null
				&& bandwidthRuleOfThumb.length != this.numDimensions) {
			throw new IllegalArgumentException(
					"Bandwidth estimators must be supplied for each dimension");
		}
		this.bandwidthRoT = new BandwidthEstimationMethod[this.numDimensions];
		this.H = new double[this.numDimensions][this.numDimensions];
		for (int dim = 0; dim < this.numDimensions; dim++) {
			this.bandwidthRoT[dim] = bandwidthRuleOfThumb[dim] == null
					? BandwidthEstimationMethod.getDefault()
					: bandwidthRuleOfThumb[dim];
			if (this.bandwidthRoT[dim] == BandwidthEstimationMethod.User_Defined) {
				if (h == null || h[dim] == null) {
					throw new IllegalArgumentException(
							"A user-defined bandwidth must be supplied for dimension "
									+ dim);
				} else {
					this.H[dim][dim] = h[dim];
				}
			}
		}

		dimensionBounds = new PointImpl[this.numDimensions];
		isClosed = false;

	}

	/**
	 * Method to add a new data point
	 * 
	 * @param coords
	 *            The coordinates of the point
	 * @throws IllegalArgumentException
	 *             If the wrong number of dimensions are supplied
	 * @throws IllegalStateException
	 *             If the grid has been calculated and the object closed
	 */
	public void acceptPoint(double... coords)
			throws IllegalStateException, IllegalArgumentException {
		if (isClosed) {
			throw new IllegalStateException("The kernel has been closed");
		}
		if (coords.length != getNumDimensions()) {
			throw new IllegalArgumentException("Must supply "
					+ getNumDimensions() + " coordinates for this kernel");
		}
		synchronized (this) {
			if (!isClosed) {
				final PointImpl pointImpl = new PointImpl(coords);
				data.add(pointImpl);
				dataSummary.accept(pointImpl);
			}
		}

	}

	/**
	 * @return The grid points for the {@link KernelDensityFunction}. Will
	 *         return an empty collection if {@link #generateKDEGrid(double)} or
	 *         {@link #generateKDEGrid(double, ExecutionMonitor)} has not been
	 *         called
	 */
	public final List<Point<?>> getGridPoints() {
		return Collections.unmodifiableList(gridPts);
	}

	/**
	 * @return the number of Dimensions
	 */
	public final int getNumDimensions() {
		return numDimensions;
	}

	/**
	 * @return the number of points in each direction in the grid
	 */
	public int getGridSize() {
		return gridSize;
	}

	/**
	 * @return the bandwidth matrix
	 */
	public double[][] getBandwidthMatrix() {
		return H;
	}

	/**
	 * @return The absolute density value at a point. NB this is calculated from
	 *         the kernel contributions of the datapoint, not from the
	 *         calculated grid. The grid needs to be precalculated in order to
	 *         ensure that the bandwidth matrix is correctly calculated
	 * @throws IllegalArgumentException
	 *             If the wrong number of dimensions are supplied
	 * @throws IllegalStateException
	 *             If the grid has been not been calculated and the object
	 *             closed
	 */
	public double getDensityAtPoint(double... coords)
			throws IllegalStateException, IllegalArgumentException {
		if (!isClosed) {
			throw new IllegalStateException(
					"Need to close and calculate the kernel first!");
		}
		if (coords.length != getNumDimensions()) {
			throw new IllegalArgumentException(
					"Wrong number of coordinates supplied");
		}
		double divisor = IntStream.range(0, getNumDimensions())
				.mapToDouble(dim -> H[dim][dim] * H[dim][dim])
				.reduce(data.size(), (a, b) -> a * b);
		double retVal = 0.0;
		for (Point<PointImpl> dataPoint : data) {
			double[] u = IntStream.range(0, getNumDimensions())
					.mapToDouble(
							dim -> (dataPoint.getCoordinate(dim) - coords[dim])
									/ H[dim][dim])
					.toArray();
			retVal += kSymm.getEstimate(u, kEst);
		}
		retVal /= divisor;
		return retVal;
	}

	/**
	 * @return The maximum value of the intensity of the
	 *         {@link KernelDensityFunction} grid
	 * @throws IllegalStateException
	 *             If the grid has been not been calculated and the object
	 *             closed
	 */
	public double getMaximumIntensity() throws IllegalStateException {
		if (!isClosed) {
			throw new IllegalStateException(
					"Need to close and calculate the kernel first!");
		}
		// The grid points have numDim+1 dimensions, the last being the kernel
		// intensity at that point
		return gridSummary.getSummary(numDimensions).getMax();
	}

	/**
	 * Initialise H0 using the supplied bandwidths or Bandwidth estimators
	 * 
	 * @throws KernelException
	 *             If there are insufficient data points to estimate a bandwidth
	 * @throws IllegalStateException
	 *             If the object has not been closed to new point addition
	 */
	protected synchronized void initialiseH()
			throws KernelException, IllegalStateException {
		if (!isClosed) {
			throw new IllegalStateException(
					"The kernel must be closed to new point addition prior to calculation");
		}

		for (int dim = 0; dim < getNumDimensions(); dim++) {
			if (bandwidthRoT[dim] == BandwidthEstimationMethod.User_Defined) {
				if (H == null || H[dim] == null) {
					throw new IllegalStateException(
							"Missing user-defined bandwidth in dimension "
									+ dim);
				}
				// Nothing to do
				continue;
			}
			if (data.size() < 2) {
				throw new KernelException(
						"Unable to guess bandwidth for single data point kernel");
			}
			H[dim] = new double[getNumDimensions()];
			H[dim][dim] = bandwidthRoT[dim].calculate(getNumDimensions(),
					dataSummary.getSummary(dim).getStandardDeviation(),
					data.size());

		}
	}

	/**
	 * Method to manually set the range of the kernel grid in a specified
	 * dimension
	 * 
	 * @param dim
	 *            The dimension
	 * @param min
	 *            The minimum value for the dimension
	 * @param max
	 *            The maximum value for the dimension
	 * @throws IllegalArgumentException
	 *             If the dimensionis out of range
	 * @throws IllegalStateException
	 *             If the grid has been calculated and the object closed
	 * @see #autoRangeKernel(double)
	 * @see #autoRangeKernel(double, boolean)
	 */
	public synchronized void setKernelRange(int dim, double min, double max)
			throws IllegalStateException, IllegalArgumentException {
		if (isClosed) {
			throw new IllegalStateException(
					"The kernel is closed and cannot be re-ranged");
		}
		if (dim < 0 || dim >= getNumDimensions()) {
			throw new IllegalArgumentException(
					"Dimension must be in range 0-" + getNumDimensions());
		}
		if (max <= min) {
			throw new IllegalArgumentException("Min must be < max");
		}
		if (Double.isNaN(min) || Double.isInfinite(min) || Double.isNaN(max)
				|| Double.isInfinite(max)) {
			throw new IllegalArgumentException(
					"Min and max must both be finite real numbers");
		}
		if (max < dataSummary.getSummary(dim).getMin()) {
			throw new IllegalArgumentException(
					"Max must be >= the minimum data value ("
							+ dataSummary.getSummary(dim).getMin() + ")");
		}
		if (min > dataSummary.getSummary(dim).getMax()) {
			throw new IllegalArgumentException(
					"Min must be <= the maximum data value ("
							+ dataSummary.getSummary(dim).getMax() + ")");
		}

		if (dimensionBounds[dim] == null) {
			dimensionBounds[dim] = new PointImpl(2);
		}
		dimensionBounds[dim].setCoordinate(0, min);
		dimensionBounds[dim].setCoordinate(1, max);
	}

	/**
	 * Overloaded method to auto-range any dimensions not currently ranged
	 * 
	 * @param rangeFactor
	 *            The fraction of the actual dimension range to add as a
	 *            'margin'
	 * @throws IllegalStateException
	 *             If the grid has been calculated and the object closed
	 * @see #autoRangeKernel(double, boolean)
	 * @see #setKernelRange(int, double, double)
	 */
	public synchronized void autoRangeKernel(double rangeFactor)
			throws IllegalStateException {
		this.autoRangeKernel(rangeFactor, false);
	}

	/**
	 * Method to auto-range the kernel grid.
	 * 
	 * @param rangeFactor
	 *            The fraction of the actual dimension range to add as a
	 *            'margin'
	 * @param overwrite
	 *            Should existing ranges be overwritten?
	 * @throws IllegalStateException
	 *             If the grid has been calculated and the object closed
	 * @see #setKernelRange(int, double, double)
	 */
	public synchronized void autoRangeKernel(double rangeFactor,
			boolean overwrite) throws IllegalStateException {
		if (isClosed) {
			throw new IllegalStateException(
					"The kernel is closed and cannot be re-ranged");
		}
		for (int dim = 0; dim < getNumDimensions(); dim++) {
			if (dimensionBounds[dim] == null || overwrite) {
				final DoubleSummary dimSummary = dataSummary.getSummary(dim);
				double margin = dimSummary.getCount() == 1
						? ((1 + rangeFactor) * getBandwidthMatrix()[dim][dim])
						: (rangeFactor
								* (dimSummary.getMax() - dimSummary.getMin()));
				dimensionBounds[dim] =
						new PointImpl(dimSummary.getMin() - margin,
								dimSummary.getMax() + margin);
			}

		}
	}

	/**
	 * @param dim
	 *            The dimension to return the minimum grid value from
	 * @return The minimum grid value in the indicated dimension
	 * @throws IllegalArgumentException
	 *             If the dimension is out of range
	 * @throws IllegalStateException
	 *             if the range has not been calculated or specified in the
	 *             given direction
	 */
	public double getRangeMin(int dim)
			throws IllegalArgumentException, IllegalStateException {
		if (dim < 0 || dim >= getNumDimensions()) {
			throw new IllegalArgumentException(
					"Dimension must be in range 0-" + getNumDimensions());
		}
		if (dimensionBounds[dim] == null) {
			throw new IllegalStateException(
					"Dimension bounds have not been initialised for dimension "
							+ dim);
		}
		return dimensionBounds[dim].getCoordinate(0);
	}

	/**
	 * @param dim
	 *            The dimension to return the maximum grid value from
	 * @return The maximum grid value in the indicated dimension
	 * @throws IllegalArgumentException
	 *             If the dimension is out of range
	 * @throws IllegalStateException
	 *             if the range has not been calculated or specified in the
	 *             given direction
	 */
	public double getRangeMax(int dim)
			throws IllegalArgumentException, IllegalStateException {
		if (dim < 0 || dim >= getNumDimensions()) {
			throw new IllegalArgumentException(
					"Dimension must be in range 0-" + getNumDimensions());
		}
		if (dimensionBounds[dim] == null) {
			throw new IllegalStateException(
					"Dimension bounds have not been initialised for dimension "
							+ dim);
		}
		return dimensionBounds[dim].getCoordinate(1);
	}

	/**
	 * Method to generate the kernel grid points. once this method has been
	 * called, the kernel is closed to new point addition. The grid will not be
	 * recalculated by subsequent calls. This method does not allow user
	 * cancellation
	 * 
	 * @param rangeFactor
	 *            The fraction of the actual dimension range to add as a
	 *            'margin'
	 * @throws KernelException
	 *             If there are insufficient data points to estimate a bandwidth
	 * @see {@link #generateKDEGrid(double, ExecutionMonitor)}
	 */
	public synchronized void generateKDEGrid(double rangeFactor)
			throws KernelException {
		try {
			this.generateKDEGrid(rangeFactor, null);
		} catch (CanceledExecutionException e) {
			// Should be impossible!
			assert false;
		}
	}

	/**
	 * Method to generate the kernel grid points. once this method has been
	 * called, the kernel is closed to new point addition. The grid will not be
	 * recalculated by subsequent calls
	 * 
	 * @param rangeFactor
	 *            The fraction of the actual dimension range to add as a
	 *            'margin'
	 * @throws KernelException
	 *             If there are insufficient data points to estimate a bandwidth
	 * @throws CanceledExecutionException
	 *             If the user cancelled during calculation
	 */
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

	/**
	 * Factory method to keep PointImpl hidden
	 * 
	 * @return a new point with the given number of dimensions
	 */
	protected static Point<?> createPoint(int numDimensions) {
		return new PointImpl(numDimensions);
	}

	/**
	 * Method to add grid point to the kernel, and also track it in the
	 * gridSummary
	 * 
	 * @param gridPoint
	 *            the grid point to add
	 */
	protected void addGridPoint(Point<?> gridPoint) {
		gridPts.add((PointImpl) gridPoint);
		gridSummary.accept((PointImpl) gridPoint);
	}

}
