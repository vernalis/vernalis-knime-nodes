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
 *******************************************************************************/
package com.vernalis.knime.jfcplot.core.drawabledataobject;

import java.awt.Shape;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.knime.core.data.property.ShapeFactory;
import org.knime.ext.jfc.node.scatterplot.util.ShapeTranslator;

/**
 * A simple cacheing class to wrap {@link ShapeTranslator}s, which must be
 * initialised with a size parameter, and allow their reuse for creating shape
 * mappings of varying sizes
 * 
 * @author S.Roughley
 *
 */
@SuppressWarnings("serial")
public class ScaleableShapeTranslator
		extends LinkedHashMap<Long, ShapeTranslator> {

	private static final int MAX_ENTRIES = 250;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override
	protected boolean removeEldestEntry(Entry<Long, ShapeTranslator> eldest) {
		// Force the cache to keep within a set number of entries
		return size() > MAX_ENTRIES;
	}

	/**
	 * Method to get a scaled AWT Shape from the KNIME shape and size. Cache's
	 * the ShapeTranslators between calls
	 * 
	 * @param knimeShape
	 *            The KNIME plot shape
	 * @param size
	 *            The size to plot at
	 * @return the AWT Shape
	 */
	public Shape getAWTShape(ShapeFactory.Shape knimeShape, Double size) {
		return this
				.computeIfAbsent(Double.doubleToLongBits(size),
						k -> new ShapeTranslator(
								(float) Double.longBitsToDouble(k)))
				.getAWTShape(knimeShape);
	}
}
