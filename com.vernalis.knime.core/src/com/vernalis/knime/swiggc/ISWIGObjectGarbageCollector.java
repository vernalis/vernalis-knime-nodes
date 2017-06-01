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
package com.vernalis.knime.swiggc;

/**
 * @author s.roughley knime@vernalis.com
 *
 */
public interface ISWIGObjectGarbageCollector {

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
	public abstract <T extends Object> T markForCleanup(final T object,
			final int wave, final boolean bRemoveFromOtherWave);

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
	public abstract <T extends Object> T markForCleanup(final T object,
			final int wave);

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
	public abstract <T extends Object> T markForCleanup(final T object,
			boolean bRemoveFromOtherWave);

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
	public abstract <T extends Object> T markForCleanup(final T object);

	/**
	 * Frees resources for all objects that have been registered prior to this
	 * last call using the method {@link #cleanupMarkedObjects()}.
	 */
	public abstract void cleanupMarkedObjects();

	/**
	 * Frees resources for all objects that have been registered prior to this
	 * last call for a certain wave using the method
	 * {@link #cleanupMarkedObjects(int)}.
	 * 
	 * @param wave
	 *            A number that identifies objects registered for a certain
	 *            "wave".
	 */
	public abstract void cleanupMarkedObjects(final int wave);

	/**
	 * Removes all resources for all objects that have been registered prior to
	 * this last call using the method {@link #cleanupMarkedObjects()}, but
	 * delays the cleanup process. It basically moves the objects of interest
	 * into quarantine.
	 * 
	 * @param delayMilliSec
	 *            The delay before actual cleaning in ms
	 */
	public abstract void quarantineAndCleanupMarkedObjects(long delayMilliSec);

	/**
	 * Removes all resources for all objects that have been registered prior to
	 * this last call using the method {@link #cleanupMarkedObjects()}, but
	 * delays the cleanup process. It basically moves the objects of interest
	 * into quarantine for 60 seconds.
	 */
	public abstract void quarantineAndCleanupMarkedObjects();

}
