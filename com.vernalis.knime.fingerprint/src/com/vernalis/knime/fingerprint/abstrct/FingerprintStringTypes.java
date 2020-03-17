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
 ******************************************************************************/
package com.vernalis.knime.fingerprint.abstrct;

import java.util.ArrayList;
import java.util.Arrays;

import org.knime.core.data.vector.bitvector.BitVectorValue;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * An enum representing the various string types for fingerprints, and providing
 * methods to validate and convert to/from strings to fingerprints
 * 
 * @author s.roughley
 * 
 */
public enum FingerprintStringTypes implements ButtonGroupEnumInterface {
	HEX("Hex", "Hexadeximal", "^[A-Fa-f0-9]+(\\.\\.\\.\\s)*$", "...", (long) Integer.MAX_VALUE * 4,
			(long) Integer.MAX_VALUE * 4) {
		@Override
		public long[] setBitsFromString(String fpString) {
			throw new UnsupportedOperationException(
					"Use the Dense/Sparse BitVector Constuctor with the string");
		}

		@Override
		public long getLengthFromString(String fpString) {
			return fpString.length() * 4;
		}
	},

	BIN("Bin", "Binary", "^[01]+(\\.\\.\\.\\s)*$", "...", Integer.MAX_VALUE,
			(long) Integer.MAX_VALUE) {
		@Override
		public long[] setBitsFromString(String fpString) {
			ArrayList<Long> setBits = new ArrayList<>();
			for (long idx = 0, l = fpString.length(); idx < l; idx++) {
				if (fpString.charAt((int) (l - 1 - idx)) == '1') {
					setBits.add(idx);
				}
			}

			long[] retVal = new long[setBits.size()];
			int idx = 0;
			for (long l : setBits) {
				retVal[idx++] = l;
			}
			Arrays.sort(retVal);
			return retVal;
		}

		@Override
		public long getLengthFromString(String fpString) {
			return fpString.length();
		}
	},

	BITVECTOR_STRING("BitVector String", "Verbose BitVector String",
			"^\\{length=\\s*?(\\d*?),\\s*?" + "set bits=((\\d*?,\\s*?)*\\d*?)(\\.\\.\\.\\s)*\\}$",
			"... }", Long.MAX_VALUE, null) {
		@Override
		public long[] setBitsFromString(String fpString) {
			String[] vals = fpString.replaceAll(".*set bits=(.*?)(... )*\\}", "$1").split(",\\s*");
			long[] retVal = new long[vals.length];
			for (int i = 0; i < vals.length; i++) {
				retVal[i] = Long.parseLong(vals[i]);
			}
			Arrays.sort(retVal);
			return retVal;
		}

		@Override
		public long getLengthFromString(String fpString) {
			return Long.parseLong(fpString.replaceAll(getValidationRegex(), "$1"));
		}
	},

	BYTEVECTOR_STRING("ByteVector String", "Verbose ByteVector String",
			"^\\{((\\d*?,\\s*?)*(\\d+)*)(\\.\\.\\.\\s)*\\}$", "... }", Long.MAX_VALUE, null) {
		@Override
		public long[] setBitsFromString(String fpString) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getLengthFromString(String fpString) {
			return getCountsFromString(fpString).length;
		}

		@Override
		public byte[] getCountsFromString(String fpString) {
			final String countsStr = fpString.replaceAll(getValidationRegex(), "$1").trim();
			String[] vals = countsStr.isEmpty() ? new String[0]
					: countsStr.contains(",") ? countsStr.split(",\\s*")
							: new String[] { countsStr };
			byte[] retVal = new byte[vals.length];
			for (int i = 0; i < vals.length; i++) {
				retVal[i] = Byte.parseByte(vals[i]);
			}
			return retVal;
		}

		@Override
		public boolean isCount() {
			return true;
		}

		@Override
		public boolean isSparseCompatible() {
			return false;
		}

	};

	/** The name */
	private String m_name;

	/** The tool-tip text */
	private String m_tooltip;

	/** A regular expresssion to validate the string matches the format */
	private String m_validationRegex;

	/** #endsWith match string to indicate truncation */
	private String m_truncationEndsWith;

	/**
	 * The maximum fingerprint length which can be encoded in this string format
	 */
	private long m_maxLength;

	/**
	 * The maximum fingerprint cardinality which can be encoded in this string
	 * format
	 */
	private Long m_maxCardinality;

	/**
	 * A {@link Enum} to list the string options for fingerprints, and provide
	 * validation parameters
	 * 
	 * @param name
	 *            The name
	 * @param tooltip
	 *            The tool-tip text
	 * @param validationRegex
	 *            A regular expresssion to validate the string matches the
	 *            format
	 * @param truncationEndsWith
	 *            A string at the end of the representation which indicates
	 *            truncation
	 * @param maxLength
	 *            The maximum fingerprint length which can be encoded in this
	 *            string format. Note that internal settings may restrict this
	 *            further.
	 * @param maxCardinality
	 *            The maximum fingerprint cardinality which can be encoded in
	 *            this string format. Can be <code>null</code> if cannot be
	 *            specified.
	 */
	private FingerprintStringTypes(String name, String tooltip, String validationRegex,
			String truncationEndsWith, long maxLength, Long maxCardinality) {
		this.m_name = name;
		this.m_tooltip = tooltip;
		this.m_validationRegex = validationRegex;
		this.m_truncationEndsWith = truncationEndsWith;
		this.m_maxLength = maxLength;
		this.m_maxCardinality = maxCardinality;

	}

	@Override
	public String getText() {
		return m_name;
	}

	@Override
	public String getActionCommand() {
		return this.name();
	}

	@Override
	public String getToolTip() {
		return this.m_tooltip;
	}

	/**
	 * @return the m_validationRegex
	 */
	public String getValidationRegex() {
		return m_validationRegex;
	}

	/** @return the truncation String ending */
	public String getTruncationEnding() {
		return m_truncationEndsWith;
	}

	/**
	 * @return the Maximum Length which can be stored
	 */
	public long getMaxLength() {
		return m_maxLength;
	}

	/**
	 * @return the Maximum Cardinality which can be stored
	 */
	public Long getMaxCardinality() {
		return m_maxCardinality;
	}

	@Override
	public boolean isDefault() {
		return this.equals(FingerprintStringTypes.getDefaultMethod());
	}

	/**
	 * @return The default method
	 */
	public static FingerprintStringTypes getDefaultMethod() {
		return HEX;
	}

	/**
	 * Validate that a supplied string is a viable representation of the current
	 * type
	 * 
	 * @param fpString
	 *            The fingerprint string to validate
	 * @return <code>true</code> if the string is a valid representation
	 */
	public boolean validateString(String fpString) {
		return fpString.matches(m_validationRegex);
	}

	/** Static implementation of the {@link #validateString(String)} method */
	public static boolean validateString(String fpString, FingerprintStringTypes fpType) {
		return fpType.validateString(fpString);
	}

	/**
	 * Check whether a string representation was truncated. The truncation will
	 * be governed by both {@link #getMaxLength()} and
	 * {@link BitVectorValue#MAX_DISPLAY_BITS}.
	 * 
	 * @param fpString
	 * @return
	 */
	public boolean isTruncated(String fpString) {
		return fpString.endsWith(m_truncationEndsWith);
	}

	/** Static implementation of the {@link #isTruncated(String)} method */
	public static boolean isTerminated(String fpString, FingerprintStringTypes fpType) {
		return isTruncated(fpString, fpType);
	}

	/** Static implementation of the {@link #isTruncated(String)} method */
	public static boolean isTruncated(String fpString, FingerprintStringTypes fpType) {
		return fpType.isTruncated(fpString);
	}

	/**
	 * @param fpString
	 *            The string representation of the fingerprint
	 * @return A list of set bit indices (for bitvector fingerprints)
	 * @throws UnsupportedOperationException
	 *             For bytevector types
	 */
	public abstract long[] setBitsFromString(String fpString) throws UnsupportedOperationException;

	/**
	 * @param fpString
	 *            The string representation of the fingerprint
	 * @return The length of the fingerprint
	 */
	public abstract long getLengthFromString(String fpString);

	/**
	 * @param fpString
	 *            The string representation of the fingerprint
	 * @return The counts for the bytevector fingerprint
	 * @throws UnsupportedOperationException
	 *             For bitvector types
	 */
	public byte[] getCountsFromString(String fpString) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return Is the fingerprint type a count (i.e. bytevector)
	 */
	public boolean isCount() {
		return false;
	}

	/**
	 * @return Can the fingerprint be returned as a Sparse representation (bit
	 *         or byte vector)
	 */
	public boolean isSparseCompatible() {
		return true;
	}
}
