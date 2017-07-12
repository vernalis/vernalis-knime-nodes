/*******************************************************************************
 * Copyright (c) 2016, 2017, Vernalis (R&D) Ltd
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.knime.core.node.NodeLogger;

/**
 * This class provides a container for all SWIG-based objects which need an
 * explicit call to their #delete() method to destroy the underlying C++ object.
 * 
 * It is based heavily on the RDKitCleanupTracker at https://community.knime.
 * org/svn/nodes4knime/trunk/org.rdkit/org.rdkit.knime.nodes/src
 * /org/rdkit/knime/nodes/AbstractRDKitNodeModel.java but is heavily refactored
 * to implement reference counting
 * 
 * @author S Roughley knime@vernalis.com
 * 
 */
public class SWIGObjectGarbageCollector2 extends HashMap<Object, Set<Long>>
		implements ISWIGObjectGarbageCollector {

	private static final long serialVersionUID = 3403451572751057864L;

	/** The logger instance. */
	protected static final NodeLogger LOGGER =
			NodeLogger.getLogger(SWIGObjectGarbageCollector2.class);

	HashMap<Long, Set<Object>> waveLookup = new HashMap<>();

	//
	// Constructors
	//

	/**
	 * Creates a new Garbage collector.
	 */
	public SWIGObjectGarbageCollector2() {
		super();
	}

	/**
	 * Creates a new Garbage collector.
	 * 
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public SWIGObjectGarbageCollector2(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Creates a new Garbage collector.
	 * 
	 * @param initialCapacity
	 */
	public SWIGObjectGarbageCollector2(final int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Creates a copy of an existing Garbage collector object.
	 * 
	 * @param existing
	 *            The existing object. Must not be null.
	 */
	private SWIGObjectGarbageCollector2(final SWIGObjectGarbageCollector2 existing) {
		super(existing);
		waveLookup = new HashMap<>(existing.waveLookup);
	}

	//
	// Public Methods
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.ISWIGObjectGarbageCollector#
	 * markForCleanup (T, int)
	 */
	@Override
	public synchronized <T extends Object> T markForCleanup(final T object, final Long wave) {
		if (object != null) {
			if (!containsKey(object)) {
				// New object
				put(object, new HashSet<>());
			}
			// Add the wave to the object
			get(object).add(wave);

			// Now add to the lookup
			if (!waveLookup.containsKey(wave)) {
				waveLookup.put(wave, new HashSet<>());
			}
			waveLookup.get(wave).add(object);
		}
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.ISWIGObjectGarbageCollector#
	 * markForCleanup (T)
	 */
	@Override
	public synchronized <T extends Object> T markForCleanup(final T object) {
		return markForCleanup(object, 0L);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.ISWIGObjectGarbageCollector#
	 * cleanupMarkedObjects()
	 */
	@Override
	public synchronized void cleanupMarkedObjects() {
		waveLookup.clear();
		// And now run through the keys of the reference counter
		for (Object obj : this.keySet()) {
			cleanUpObject(obj);
		}
		this.clear();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.ISWIGObjectGarbageCollector#
	 * cleanupMarkedObjects(int)
	 */
	@Override
	public synchronized void cleanupMarkedObjects(final Long wave) {
		// Iterator<Entry<Object, Set<Integer>>> iter =
		// this.entrySet().iterator();
		// while (iter.hasNext()) {
		// Entry<Object, Set<Integer>> ent = iter.next();
		// if (ent.getValue().contains(wave)) {
		// ent.getValue().remove(wave);
		// if (ent.getValue().size() == 0) {
		// cleanUpObject(ent.getKey());
		// iter.remove();
		// }
		// }
		// }

		// Loop through all the objects in the wave
		if (!waveLookup.containsKey(wave)) {
			LOGGER.debug("Wave ID " + wave + " not found during native object cleanup");
			return;
		}
		for (Object obj : waveLookup.get(wave)) {
			Set<Long> objWaves = get(obj);
			// Remove the reference to the object from this wave
			objWaves.remove(wave);
			if (get(obj).size() == 0) {
				cleanUpObject(obj);
				remove(obj);
			}
		}
		waveLookup.remove(wave);
	}

	/**
	 * Call the #delete() method of the supplied object - assumes that all
	 * checks have been made and that the object is no longer needed
	 * 
	 * @param objForCleanup
	 *            The object to dispose
	 */
	protected void cleanUpObject(final Object objForCleanup) {
		Class<?> clazz = null;

		try {
			clazz = objForCleanup.getClass();
			final Method method = clazz.getMethod("delete");
			method.invoke(objForCleanup);
		} catch (final NoSuchMethodException excNoSuchMethod) {
			LOGGER.error(
					"An object had been registered for cleanup (delete() call), "
							+ "which does not provide a delete() method."
							+ (clazz == null ? "" : " It's of class " + clazz.getName() + "."),
					excNoSuchMethod.getCause());
		} catch (final SecurityException excSecurity) {
			LOGGER.error(
					"An object had been registered for cleanup (delete() call), "
							+ "which is not accessible for security reasons."
							+ (clazz == null ? "" : " It's of class " + clazz.getName() + "."),
					excSecurity.getCause());
		} catch (final Exception exc) {
			LOGGER.error(
					"Cleaning up a registered object (via delete() call) failed."
							+ (clazz == null ? "" : " It's of class " + clazz.getName() + "."),
					exc.getCause());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.ISWIGObjectGarbageCollector#
	 * quarantineAndCleanupMarkedObjects(long)
	 */
	@Override
	public synchronized void quarantineAndCleanupMarkedObjects(long delayMilliSec) {
		final SWIGObjectGarbageCollector2 quarantineObjects = new SWIGObjectGarbageCollector2(this);
		clear();
		waveLookup.clear();

		if (!quarantineObjects.isEmpty()) {
			// Create the future cleanup task
			final TimerTask futureCleanupTask = new TimerTask() {

				/**
				 * Cleans up all marked objects, which are put into quarantine
				 * for now.
				 */
				@Override
				public void run() {
					quarantineObjects.cleanupMarkedObjects();
				}
			};

			// Schedule the cleanup task for later
			final Timer timer = new Timer("Quarantine SWIG-based Object Cleanup", false);
			timer.schedule(futureCleanupTask, delayMilliSec);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vernalis.knime.internal.swiggc.ISWIGObjectGarbageCollector#
	 * quarantineAndCleanupMarkedObjects()
	 */
	@Override
	public synchronized void quarantineAndCleanupMarkedObjects() {
		// Quarantine for a default delay of 60 seconds
		quarantineAndCleanupMarkedObjects(60000);
	}

	/**
	 * Look up whether an object is in the reference count map, and return the
	 * count. If it is not in the map, then 0 is returned.
	 * 
	 * @param obj
	 *            The query object
	 * @return The count of references in the cleanup object
	 */
	public Integer getReferenceCount(Object obj) {

		if (!this.containsKey(obj)) {
			return 0;
		}
		return this.get(obj).size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SWIGObjectGarbageCollector2 [waveLookup=");
		builder.append(waveLookup);
		builder.append("\n");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

}
