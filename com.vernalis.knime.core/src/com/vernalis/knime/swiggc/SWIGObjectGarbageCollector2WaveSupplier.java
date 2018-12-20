/*******************************************************************************
 * Copyright (c) 2017, Vernalis (R&D) Ltd
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
package com.vernalis.knime.swiggc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A Native object grabage collector which is also capable of supplying new
 * unique wave IDs
 * 
 * @author s.roughley
 * 
 */
@SuppressWarnings("serial")
public class SWIGObjectGarbageCollector2WaveSupplier extends SWIGObjectGarbageCollector2
		implements UniqueWaveIdSupplier {
	protected AtomicLong waveIndex = new AtomicLong(1);

	/**
	 * Constructor
	 */
	public SWIGObjectGarbageCollector2WaveSupplier() {
		super();
	}

	/**
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public SWIGObjectGarbageCollector2WaveSupplier(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * @param initialCapacity
	 */
	public SWIGObjectGarbageCollector2WaveSupplier(int initialCapacity) {
		super(initialCapacity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vernalis.knime.internal.swiggc.UniqueWaveIdSupplier#getNextWaveIndex
	 * ()
	 */
	@Override
	public long getNextWaveIndex() {
		while (this.containsKey(waveIndex.incrementAndGet())) {
			// Ensure we supply an unused idx;
		}
		return waveIndex.get();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.SWIGObjectGarbageCollector2#
	 * cleanupMarkedObjects()
	 */
	@Override
	public synchronized void cleanupMarkedObjects() {
		super.cleanupMarkedObjects();
		waveIndex.set(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.SWIGObjectGarbageCollector2#
	 * cleanupMarkedObjects(int)
	 */
	@Override
	public synchronized void cleanupMarkedObjects(Long wave) {
		super.cleanupMarkedObjects(wave);
		waveIndex.set(wave);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.SWIGObjectGarbageCollector2#
	 * quarantineAndCleanupMarkedObjects(long)
	 */
	@Override
	public synchronized void quarantineAndCleanupMarkedObjects(long delayMilliSec) {
		super.quarantineAndCleanupMarkedObjects(delayMilliSec);
		waveIndex.set(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.SWIGObjectGarbageCollector2#
	 * quarantineAndCleanupMarkedObjects()
	 */
	@Override
	public synchronized void quarantineAndCleanupMarkedObjects() {
		super.quarantineAndCleanupMarkedObjects();
		waveIndex.set(1);
	}

}
