/*******************************************************************************
 * Copyright (c) 2015, 2017, Vernalis (R&D) Ltd
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License, Version 3, as 
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>
 *******************************************************************************/
package com.vernalis.knime.mmp.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.vernalis.knime.mmp.MatchedPairsMultipleCutsNodePlugin;
import com.vernalis.knime.prefs.fieldeditors.DoubleFieldEditor;

/**
 * Preference page to allow the user to control the parallelisation behaviour of
 * the MMP nodes
 * 
 * @author s.roughley
 * 
 */
public class MatchedPairPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * The default value for {@code Verbose Logging} perference
	 */
	public static final boolean DEFAULT_VERBOSE_LOGGING = false;

	/**
	 * The base of the preference ID strings for the plugin
	 */
	public static final String MMP_PREF_KEY_BASE = "com.vernalis.knime.mmp.";

	/**
	 * The key for the {@code Verbose Logging} preference
	 */
	public static final String MMP_PREF_VERBOSE_LOGGING = MMP_PREF_KEY_BASE + "verbose.logging";

	/**
	 * The key for the {@code Max No. of Threads Ratio} preference
	 */
	public static final String MMP_PREF_MAX_THREADS = MMP_PREF_KEY_BASE + "max.threads.ratio";

	/**
	 * The key for the {@code Fragment Cache Size} preference
	 */
	public static final String MMP_PREF_FRAGMENT_CACHE = MMP_PREF_KEY_BASE + "fragment.cache.size";

	/**
	 * Default size for the fragment cache - this value allows for 250 matching
	 * bonds to be cached without loss
	 */
	public static final int DEFAULT_FRAG_CACHE_SIZE = 500;

	/**
	 * The default value for the {@code Max No. of Threads Ratio} preference
	 */
	public static final double DEFAULT_MAX_THREADS_TO_CORES_RATIO = 1.5;

	/**
	 * The key for the {@code Queue-to-threads ratio} preference
	 */
	public static final String MMP_PREF_QUEUE_TO_THREADS_RATIO =
			MMP_PREF_KEY_BASE + "queue.threads.ratio";

	/**
	 * The default value for the {@code Queue-to-threads ratio} preference
	 */
	public static final int DEFAULT_QUEUE_TO_THREADS_RATIO = 20;

	/**
	 * Flag preventing re-initialisation after possible modification of existing
	 * initialisation
	 */
	private static boolean m_defaultInitialised = false;

	/**
	 * Constructor for the Vernalis Matched Pairs Plugin preference page
	 */
	public MatchedPairPreferencePage() {
		super(GRID);
		setPreferenceStore(MatchedPairsMultipleCutsNodePlugin.getDefault().getPreferenceStore());
		setDescription("Matched Molecular Pair Preferences");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		//

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {

		Composite parent = getFieldEditorParent();
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 6;
		layout.fill = true;
		parent.setLayout(layout);

		BooleanFieldEditor verboseLoggingEditor =
				new BooleanFieldEditor(MMP_PREF_VERBOSE_LOGGING, "Enable Verbose Logging", parent);
		addField(verboseLoggingEditor);

		IntegerFieldEditor cacheSize =
				new IntegerFieldEditor(MMP_PREF_FRAGMENT_CACHE, "Fragmentation Cache Size", parent);
		cacheSize.setValidRange(1, 1000000);
		addField(cacheSize);

		// Add the parallelisation options in a new group
		Group parallelParent = new Group(parent, parent.getStyle());
		parallelParent.setText("Parallel processing options");
		DoubleFieldEditor processorRatio = new DoubleFieldEditor(MMP_PREF_MAX_THREADS,
				"Ratio of Maximum number of threads to processor cores "
						+ "(e.g. for 8 cores, a value of 0.5 will use a maximum of 4 cores)",
				parallelParent);
		processorRatio.setValidRange(0.1, 2.5);
		addField(processorRatio);

		IntegerFieldEditor queueRatio = new IntegerFieldEditor(MMP_PREF_QUEUE_TO_THREADS_RATIO,
				"Ratio of Queue size to Threads Ratio " + "(smaller values will use less memory, "
						+ "but are more likely to have more threads "
						+ "inactive waiting for a slow row to complete)",
				parallelParent);
		queueRatio.setValidRange(1, 5000);
		addField(queueRatio);

	}

	/**
	 * Initialize the default values
	 */
	public synchronized static void initializeDefaultPreferences() {
		if (!m_defaultInitialised) {
			m_defaultInitialised = true;
			try {
				MatchedPairsMultipleCutsNodePlugin plugin =
						MatchedPairsMultipleCutsNodePlugin.getDefault();
				if (plugin != null) {
					final IPreferenceStore prefStore = plugin.getPreferenceStore();
					prefStore.setDefault(MMP_PREF_VERBOSE_LOGGING, DEFAULT_VERBOSE_LOGGING);
					prefStore.setDefault(MMP_PREF_MAX_THREADS, DEFAULT_MAX_THREADS_TO_CORES_RATIO);
					prefStore.setDefault(MMP_PREF_QUEUE_TO_THREADS_RATIO,
							DEFAULT_QUEUE_TO_THREADS_RATIO);
					prefStore.setDefault(MMP_PREF_FRAGMENT_CACHE, DEFAULT_FRAG_CACHE_SIZE);
				}
			} catch (Exception e) {
				;
			}
		}
	}

	/**
	 * @return The threads count based on the current preference value and
	 *         number of available cores
	 */
	public static Integer getThreadsCount() {

		MatchedPairsMultipleCutsNodePlugin plugin = MatchedPairsMultipleCutsNodePlugin.getDefault();
		if (plugin != null) {
			final IPreferenceStore prefStore = plugin.getPreferenceStore();

			return (int) Math.ceil(prefStore.getDouble(MMP_PREF_MAX_THREADS)
					* Runtime.getRuntime().availableProcessors());
		}
		return null;
	}

	/**
	 * @return The total queue size, based on the current preference values and
	 *         number of available cores
	 */
	public static Integer getQueueSize() {
		MatchedPairsMultipleCutsNodePlugin plugin = MatchedPairsMultipleCutsNodePlugin.getDefault();
		if (plugin != null) {
			final IPreferenceStore prefStore = plugin.getPreferenceStore();
			return getThreadsCount() * prefStore.getInt(MMP_PREF_QUEUE_TO_THREADS_RATIO);
		}
		return null;
	}
}
