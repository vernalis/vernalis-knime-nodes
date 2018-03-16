/*******************************************************************************
 * Copyright (c) 2016, Vernalis (R&D) Ltd
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.knime.core.node.NodeLogger;

/**
 * This class provides a container for all SWIG-based objects which need an
 * explicit call to their #delete() method to destroy the underlying C++ object.
 * 
 * It is based heavily on the RDKitCleanupTracker at https://community.knime.
 * org/svn/nodes4knime/trunk/org.rdkit/org.rdkit.knime.nodes/src
 * /org/rdkit/knime/nodes/AbstractRDKitNodeModel.java
 * 
 * @author S Roughley knime@vernalis.com
 * 
 */
// TODO: Can we save some messing around using a HashSet<Object>?
@Deprecated
public class SWIGObjectGarbageCollector extends HashMap<Integer, List<Object>> {

	Map<Object, Integer> refCount = new HashMap<>();

	private static final long serialVersionUID = 3403451572751057864L;

	/** The logger instance. */
	protected static final NodeLogger LOGGER =
			NodeLogger.getLogger(SWIGObjectGarbageCollector.class);

	//
	// Constructors
	//

	/**
	 * Creates a new Garbage collector.
	 */
	public SWIGObjectGarbageCollector() {
		super();
	}

	/**
	 * Creates a new Garbage collector.
	 * 
	 * @param initialCapacity
	 * @param loadFactor
	 */
	public SWIGObjectGarbageCollector(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Creates a new Garbage collector.
	 * 
	 * @param initialCapacity
	 */
	public SWIGObjectGarbageCollector(final int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Creates a copy of an existing Garbage collector object.
	 * 
	 * @param existing
	 *            The existing object. Must not be null.
	 */
	private SWIGObjectGarbageCollector(final SWIGObjectGarbageCollector existing) {
		super(existing);
		this.refCount = new HashMap<>(existing.refCount);
	}

	//
	// Public Methods
	//

	/**
	 * Registers an SWIG based object that is used within a certain block
	 * (wave). $ This object must have a delete() method implemented for freeing
	 * up resources later. The cleanup will happen for all registered objects
	 * when the method {@link #cleanupMarkedObjects(int)} is called with the
	 * same wave. Note: If the last parameter is set to true and the same
	 * rdkitObject was already registered for another wave (or no wave) it will
	 * be removed from the former wave list and will exist only in the wave
	 * specified here. This can be useful for instance, if an object is first
	 * marked as part of a wave and later on it is determined that it needs to
	 * live longer (e.g. without a wave). In this case the first time this
	 * method would be called with a wave id, the second time without wave id
	 * (which would internally be wave = 0).
	 * 
	 * @param <T>
	 *            Any class that implements a delete() method to be called to
	 *            free up resources.
	 * @param object
	 *            An object that should free resources when not used anymore.
	 *            Can be null.
	 * @param wave
	 *            A number that identifies objects registered for a certain
	 *            "wave".
	 * @param bRemoveFromOtherWave
	 *            Checks, if the object was registered before with another wave
	 *            id, and remove it from that former wave. Usually this should
	 *            be set to false for performance reasons.
	 * 
	 * @return The same object that was passed in. Null, if null was passed in.
	 */
	public synchronized <T extends Object> T markForCleanup(final T object, final int wave,
			final boolean bRemoveFromOtherWave) {
		if (object != null) {

			// Remove object from any other list, if desired (cost performance!)
			if (bRemoveFromOtherWave && getReferenceCount(object) > 0) {

				// Loop through all waves to find the object - we create a
				// copy here, because
				// we may remove empty wave lists which may blow up our iterator
				for (final int waveExisting : new HashSet<>(keySet())) {
					final List<Object> list = get(waveExisting);

					if (list.remove(object)) {
						decReferenceCount(object);
						if (list.isEmpty()) {
							remove(waveExisting);
						}
						if (getReferenceCount(object) <= 0) {
							// Stop looping if we know we have already found all
							// other copies
							break;
						}
					}
				}
			}

			// Get the list of the target wave
			List<Object> list = get(wave);

			// Create a wave list, if not found yet
			if (list == null) {
				list = new ArrayList<>();
				put(wave, list);
			}

			// Add the object only once
			if (!list.contains(object)) {
				list.add(object);
				incReferenceCount(object);
			}
		}

		return object;
	}

	/**
	 * Overloaded method, in which the remove from other waves method assumes
	 * the default {@code false}.
	 * 
	 * Registers an SWIG based object that is used within a certain block
	 * (wave). $ This object must have a delete() method implemented for freeing
	 * up resources later. The cleanup will happen for all registered objects
	 * when the method {@link #cleanupMarkedObjects(int)} is called with the
	 * same wave. Note: If the last parameter is set to true and the same
	 * rdkitObject was already registered for another wave (or no wave) it will
	 * be removed from the former wave list and will exist only in the wave
	 * specified here. This can be useful for instance, if an object is first
	 * marked as part of a wave and later on it is determined that it needs to
	 * live longer (e.g. without a wave). In this case the first time this
	 * method would be called with a wave id, the second time without wave id
	 * (which would internally be wave = 0).
	 * 
	 * @param <T>
	 *            Any class that implements a delete() method to be called to
	 *            free up resources.
	 * @param object
	 *            An object that should free resources when not used anymore.
	 *            Can be null.
	 * @param wave
	 *            A number that identifies objects registered for a certain
	 *            "wave".
	 * @return The same object that was passed in. Null, if null was passed in.
	 */
	public synchronized <T extends Object> T markForCleanup(final T object, final int wave) {
		return markForCleanup(object, wave, false);
	}

	/**
	 * Overloaded method, in which the remove from other waves method assumes
	 * the default 'core' wave (={@code 0}).
	 * 
	 * Registers an SWIG based object that is used within a certain block
	 * (wave). $ This object must have a delete() method implemented for freeing
	 * up resources later. The cleanup will happen for all registered objects
	 * when the method {@link #cleanupMarkedObjects(int)} is called with the
	 * same wave. Note: If the last parameter is set to true and the same
	 * rdkitObject was already registered for another wave (or no wave) it will
	 * be removed from the former wave list and will exist only in the wave
	 * specified here. This can be useful for instance, if an object is first
	 * marked as part of a wave and later on it is determined that it needs to
	 * live longer (e.g. without a wave). In this case the first time this
	 * method would be called with a wave id, the second time without wave id
	 * (which would internally be wave = 0).
	 * 
	 * @param <T>
	 *            Any class that implements a delete() method to be called to
	 *            free up resources.
	 * @param object
	 *            An object that should free resources when not used anymore.
	 *            Can be null.
	 * @param wave
	 *            A number that identifies objects registered for a certain
	 *            "wave".
	 * @return The same object that was passed in. Null, if null was passed in.
	 */
	public synchronized <T extends Object> T markForCleanup(final T object,
			boolean bRemoveFromOtherWave) {
		return markForCleanup(object, 0, bRemoveFromOtherWave);
	}

	/**
	 * Overloaded method, in which the remove from other waves method assumes
	 * the default {@code false}, and the default 'core' wave (={@code 0}).
	 * 
	 * Registers an SWIG based object that is used within a certain block
	 * (wave). $ This object must have a delete() method implemented for freeing
	 * up resources later. The cleanup will happen for all registered objects
	 * when the method {@link #cleanupMarkedObjects(int)} is called with the
	 * same wave. Note: If the last parameter is set to true and the same
	 * rdkitObject was already registered for another wave (or no wave) it will
	 * be removed from the former wave list and will exist only in the wave
	 * specified here. This can be useful for instance, if an object is first
	 * marked as part of a wave and later on it is determined that it needs to
	 * live longer (e.g. without a wave). In this case the first time this
	 * method would be called with a wave id, the second time without wave id
	 * (which would internally be wave = 0).
	 * 
	 * @param <T>
	 *            Any class that implements a delete() method to be called to
	 *            free up resources.
	 * @param object
	 *            An object that should free resources when not used anymore.
	 *            Can be null.
	 * @param wave
	 *            A number that identifies objects registered for a certain
	 *            "wave".
	 * @return The same object that was passed in. Null, if null was passed in.
	 */
	public synchronized <T extends Object> T markForCleanup(final T object) {
		return markForCleanup(object, 0, false);
	}

	/**
	 * Frees resources for all objects that have been registered prior to this
	 * last call using the method {@link #cleanupMarkedObjects()}.
	 */
	public synchronized void cleanupMarkedObjects() {
		// // Loop through all waves for cleanup - we create a copy here,
		// because
		// // the cleanupMarkedObjects method will remove items from our map
		// for (final int wave : new HashSet<Integer>(keySet())) {
		// cleanupMarkedObjects(wave);
		// }

		// We can clear the all waves
		this.clear();

		// And now run through the keys of the reference counter
		for (Object obj : refCount.keySet()) {
			cleanUpObject(obj);
		}
		refCount.clear();
	}

	/**
	 * Frees resources for all objects that have been registered prior to this
	 * last call for a certain wave using the method
	 * {@link #cleanupMarkedObjects(int)}.
	 * 
	 * @param wave
	 *            A number that identifies objects registered for a certain
	 *            "wave".
	 */
	public synchronized void cleanupMarkedObjects(final int wave) {
		// Find the right wave list, if not found yet
		final List<Object> list = get(wave);

		// If wave list was found, free all objects in it
		if (list != null) {
			for (final Object objForCleanup : list) {
				decReferenceCount(objForCleanup);
				if (getReferenceCount(objForCleanup) <= 0) {
					// We only actually free the object if we have removed the
					// last reference to it
					cleanUpObject(objForCleanup);
				}
			}

			list.clear();
			remove(wave);
		}
	}

	/**
	 * Call the #delete() method of the supplied object - assusmes that all
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

	/**
	 * Removes all resources for all objects that have been registered prior to
	 * this last call using the method {@link #cleanupMarkedObjects()}, but
	 * delays the cleanup process. It basically moves the objects of interest
	 * into quarantine.
	 * 
	 * @param delayMilliSec
	 *            The delay before actual cleaning in ms
	 */
	public synchronized void quarantineAndCleanupMarkedObjects(long delayMilliSec) {
		final SWIGObjectGarbageCollector quarantineObjects = new SWIGObjectGarbageCollector(this);
		clear();

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

	/**
	 * Removes all resources for all objects that have been registered prior to
	 * this last call using the method {@link #cleanupMarkedObjects()}, but
	 * delays the cleanup process. It basically moves the objects of interest
	 * into quarantine for 60 seconds.
	 */
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
		Integer retVal = refCount.get(obj);
		if (retVal == null) {
			retVal = 0;
		}
		return retVal;
	}

	protected void incReferenceCount(Object obj) {
		if (!refCount.containsKey(obj)) {
			refCount.put(obj, 0);
		}
		int count = refCount.get(obj);
		count++;
		refCount.put(obj, count);
	}

	protected void decReferenceCount(Object obj) {
		if (!refCount.containsKey(obj)) {
			refCount.put(obj, 0);
		} else {
			int count = refCount.get(obj);
			count--;
			refCount.put(obj, count);
		}
	}
}
