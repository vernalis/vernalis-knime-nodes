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
package com.vernalis.knime.chem.util.points;

public final class SimplePoint extends AbstractPoint<Void> {

	public SimplePoint(double x, double y, double z) {
		super(x, y, z, null);
	}

	public SimplePoint vectorCrossProduct(SimplePoint o)
			throws UnsupportedOperationException {
		return new SimplePoint(getY() * o.getZ() - getZ() * o.getY(),
				getZ() * o.getX() - getX() * o.getZ(),
				getX() * o.getY() - getY() * o.getX());
	}

	public double dotProduct(SimplePoint o) {
		return getX() * o.getX() + getY() * o.getY() + getZ() * o.getZ();
	}

	public SimplePoint normalise() {
		return divideBy(length());
	}

	public SimplePoint plus(SimplePoint o) {
		return new SimplePoint(getX() + o.getX(), getY() + o.getY(),
				getZ() + o.getZ());
	}

	public SimplePoint minus(SimplePoint o) {
		return new SimplePoint(getX() - o.getX(), getY() - o.getY(),
				getZ() - o.getZ());
	}

	@Override
	public SimplePoint divideBy(double divisor) {
		return new SimplePoint(getX() / divisor, getY() / divisor,
				getZ() / divisor);
	}

}
