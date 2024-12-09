/*******************************************************************************
 * Copyright (c) 2018, 2020, Vernalis (R&D) Ltd
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
package com.vernalis.nodes.fasta;

public class Fasta {

	private final String header, sequence;

	public Fasta(String header, String sequence) {
		this.header = header;
		this.sequence = sequence;
	}

	/**
	 * @return the header
	 */
	public final String getHeader() {
		return header;
	}

	/**
	 * @return the sequence
	 */
	public final String getSequence() {
		return sequence;
	}

}
