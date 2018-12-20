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
