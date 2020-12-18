/*******************************************************************************
 * Copyright (c) 2020, Vernalis (R&D) Ltd
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
package com.vernalis.pdbconnector2.query;

import java.util.Arrays;

import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 * The scoring algorithm to use. These are not well documented.
 * 
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 *
 */
public enum ScoringType implements ButtonGroupEnumInterface {
	/**
	 * Combined scoring
	 */
	Combined {

		@Override
		public boolean isEnabled(boolean hasText, boolean hasSequence,
				boolean hasSequenceMotif, boolean hasStructure) {
			return true;
		}

		@Override
		public boolean returnsScore(QueryResultType resultType, boolean hasText,
				boolean hasSequence, boolean hasSequenceMotif,
				boolean hasStructure) {
			return false;
		}
	},
	/**
	 * Sequence scoring
	 */
	Sequence {

		@Override
		public boolean isEnabled(boolean hasText, boolean hasSequence,
				boolean hasSequenceMotif, boolean hasStructure) {
			return hasSequence;
		}

		@Override
		public boolean returnsScore(QueryResultType resultType, boolean hasText,
				boolean hasSequence, boolean hasSequenceMotif,
				boolean hasStructure) {
			return false;
		}
	},
	/**
	 * Sequence Motif scoring
	 */
	seqmotif {

		@Override
		public String getText() {
			return "Sequence Motif";
		}

		@Override
		public boolean isEnabled(boolean hasText, boolean hasSequence,
				boolean hasSequenceMotif, boolean hasStructure) {
			return hasSequenceMotif;
		}

		@Override
		public boolean returnsScore(QueryResultType resultType, boolean hasText,
				boolean hasSequence, boolean hasSequenceMotif,
				boolean hasStructure) {
			return false;
		}
	},
	/**
	 * Structural similarity scoring
	 */
	Structure {

		@Override
		public boolean isEnabled(boolean hasText, boolean hasSequence,
				boolean hasSequenceMotif, boolean hasStructure) {
			return hasStructure;
		}

		@Override
		public boolean returnsScore(QueryResultType resultType, boolean hasText,
				boolean hasSequence, boolean hasSequenceMotif,
				boolean hasStructure) {
			return false;
		}
	},
	/**
	 * Text scoring
	 */
	Text {

		@Override
		public boolean isEnabled(boolean hasText, boolean hasSequence,
				boolean hasSequenceMotif, boolean hasStructure) {
			return false;
		}

		@Override
		public boolean returnsScore(QueryResultType resultType, boolean hasText,
				boolean hasSequence, boolean hasSequenceMotif,
				boolean hasStructure) {
			return false;
		}
	};

	@Override
	public String getText() {
		return name();
	}

	@Override
	public String getActionCommand() {
		return name().toLowerCase();
	}

	@Override
	public String getToolTip() {
		return null;
	}

	/**
	 * @param hasText
	 *            Is there a text query?
	 * @param hasSequence
	 *            Is there a sequence query?
	 * @param hasSequenceMotif
	 *            Is there a sequence motif query?
	 * @param hasStructure
	 *            Is there a structure query?
	 * @return whether the return type is enabled, based on the content of the
	 *         query
	 */
	public abstract boolean isEnabled(boolean hasText, boolean hasSequence,
			boolean hasSequenceMotif, boolean hasStructure);

	/**
	 * @param resultType
	 *            The result type
	 * @param hasText
	 *            Is there a text query?
	 * @param hasSequence
	 *            Is there a sequence query?
	 * @param hasSequenceMotif
	 *            Is there a sequence motif query?
	 * @param hasStructure
	 *            Is there a structure query?
	 * @return whether the return type will return a score in the query result
	 */
	public abstract boolean returnsScore(QueryResultType resultType,
			boolean hasText, boolean hasSequence, boolean hasSequenceMotif,
			boolean hasStructure);

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	/**
	 * @return The default scoring type
	 */
	public static ScoringType getDefault() {
		return Combined;
	}

	/**
	 * @param text
	 *            The text to parse
	 * @return the scoring type based on the text argument, which should match
	 *         {@link #getText()} for one of the member else an
	 *         {@link IllegalArgumentException} will be thrown
	 * @throws NullPointerException
	 *             If the supplied text was {@code null}
	 * @throws IllegalArgumentException
	 *             If no corresponding enum value was found
	 */
	public static ScoringType fromText(String text)
			throws NullPointerException, IllegalArgumentException {
		if (text == null) {
			throw new NullPointerException();
		}
		return Arrays.stream(values()).filter(x -> text.equals(x.getText()))
				.findFirst().orElseThrow(IllegalArgumentException::new);
	}
}
