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
 * An enum for the options for handling large numbers in TAR archives
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.38.0
 */
public enum TarBigNumericValues implements ButtonGroupEnumInterface {

    /** Error - throw an exception */
	Error("Throw an exception", TarArchiveOutputStream.BIGNUMBER_ERROR),

    /**
     * Use the Jorg Schillings 'star' variant - not supported by all
     * implementations
     */
	Star("Use a variant first introduced by Jorg Schillings star and later adopted "
			+ "by GNU and BSD tar. Not supported by all implementations",
			TarArchiveOutputStream.BIGNUMBER_STAR),

    /**
     * Use POSIX 1003.1 PAX extended header - supported by most modern TAR
     * implementations
     */
	POSIX("Use a PAX extended header as defined by POSIX 1003.1. Most modern tar implementations are able to "
			+ "extract such archives",
			TarArchiveOutputStream.BIGNUMBER_POSIX);

	private final String tooltip;
	private final int flag;

	private TarBigNumericValues(String tooltip, int flag) {
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
	public static TarBigNumericValues getDefault() {
		return Error;
	}

	@Override
	public String getToolTip() {
		return tooltip;
	}

	/**
	 * @return the flag for the stream settings
	 *
	 * @since 1.38.0
	 */
	public int getFlag() {
		return flag;
	}
}
