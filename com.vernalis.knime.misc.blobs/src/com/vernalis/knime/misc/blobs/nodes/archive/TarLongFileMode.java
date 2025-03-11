/*******************************************************************************
 * Copyright (c) 2025, Vernalis (R&D) Ltd
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
package com.vernalis.knime.misc.blobs.nodes.archive;

import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * Enum of the options for long file name handling in TAR archives
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public enum TarLongFileMode implements ButtonGroupEnumInterface {

    /** Throw an exception */
	Error("Throw an exception if a name > 100 chars is added",
			TarArchiveOutputStream.LONGFILE_ERROR),

    /** Truncate the filename to 100 chars */
	Truncate("Truncate names > 100 chars",
			TarArchiveOutputStream.LONGFILE_TRUNCATE),

    /**
     * Use the GNU 'oldgnu' variant which may not be extractable on many
     * machines
     */
    GNU("Use GNU Tar variant known as oldgnu; for such names.  Such an archive may not be extractable on "
			+ "many implementations (including OpenBSD, Solaris, Mac OSX)",
			TarArchiveOutputStream.LONGFILE_GNU),

    /**
     * Use the POSIX 1003.1 PAX extended header. Most modern tar implementations
     * can handle this format
     */
	POSIX("Use a PAX extended header as defined by POSIX 1003.1. Most modern tar implementations are able to "
			+ "extract such archives",
			TarArchiveOutputStream.LONGFILE_POSIX);

	private final String tooltip;
	private final int flag;

	private TarLongFileMode(String tooltip, int flag) {
		this.tooltip = tooltip;
		this.flag = flag;
	}

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return name();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return the default behaviour
	 * 
	 * @since 1.38.0
	 */
	public static TarLongFileMode getDefault() {
		return Error;
	}

	@Override
	public String getToolTip() {
		return tooltip;
	}

	/**
	 * @return the flag for the stream setting
	 *
	 * @since 1.38.0
	 */
	public int getFlag() {
		return flag;
	}
}
