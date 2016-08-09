package com.vernalis.knime.mmp.fragmentors;

import com.vernalis.knime.mmp.MulticomponentSmilesFragmentParser;

/**
 * A simple exception class which should be thrown by the
 * {@link MulticomponentSmilesFragmentParser} if the smiles string contains a
 * dative bond character ('>' or '<')
 * 
 * @author s.roughley
 *
 */
public class UnenumeratedStereochemistryException extends Exception {
	/**
		 * 
		 */
	private static final long serialVersionUID = 2406414402983464598L;
	private final String smiles;
	private final boolean removeHs;

	/**
	 * Constructer
	 * 
	 * @param message
	 *            The message
	 * @param SMILES
	 *            The SMILES String
	 * @param removeHs
	 *            Should Hs be removed
	 */
	public UnenumeratedStereochemistryException(String message, String SMILES, boolean removeHs) {
		super(message);
		this.smiles = SMILES;
		this.removeHs = removeHs;
	}

	public String getSmiles() {
		return smiles;
	}

	public boolean getRemoveHs() {
		return removeHs;
	}

}
