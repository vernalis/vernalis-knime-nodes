/*******************************************************************************
 * Copyright (c) 2022, Vernalis (R&D) Ltd
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
package com.vernalis.knime.bio.speedyseq;

public interface AlphabetSmilesProvider {

	public String getSMILES(boolean isLowerCase);

	public String getChainTerminator();

	public String getChainInitiator();

	public default boolean isAmbiguity() {
		return getSMILES(false).contains("*");
	}

	public default boolean isCanonical() {
		return false;
	}

	public char getOneLetterCode();
}
