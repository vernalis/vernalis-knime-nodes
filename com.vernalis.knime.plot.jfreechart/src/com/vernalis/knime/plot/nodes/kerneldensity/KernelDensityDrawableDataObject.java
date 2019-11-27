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

import java.util.Collection;
import java.util.Collections;

import org.jfree.chart.renderer.PaintScale;

import com.vernalis.knime.jfcplot.core.drawabledataobject.DrawableDataObject;

/**
 * Interface for {@link DrawableDataObject}s rendering kernel densities,
 * provides a number of additional methods. Implementations should ensure that
 * {@link #draw(java.awt.Graphics2D, java.awt.geom.Rectangle2D, org.jfree.chart.axis.ValueAxis, org.jfree.chart.axis.ValueAxis, org.jfree.chart.plot.PlotOrientation)}
 * includes the objects returned by {@link #getAdditionalDDOs()}
 * 
 * @author S.Roughley knime@vernalis.com
 *
 */
public interface KernelDensityDrawableDataObject extends DrawableDataObject {

	/**
	 * @return The {@link KernelDensityFunction} to be drawn
	 */
	public KernelDensityFunction getKernel();

	/**
	 * @return A collection of additional objects to draw on the plot. The
	 *         default implementation returns an empty set.
	 */
	public default Collection<DrawableDataObject> getAdditionalDDOs() {
		return Collections.emptySet();
	}

	/**
	 * @return The paint scale. the default implementation returns {@code null}
	 */
	public default PaintScale getPaintScale() {
		return null;
	}
}
