package com.vernalis.nodes.fasta;

import java.util.Arrays;
import java.util.Optional;

import org.knime.core.node.util.ButtonGroupEnumInterface;

public enum FastaTypes implements ButtonGroupEnumInterface {
	GenBank("GenBank"), EMBL("EMBL"), DDBJ("DDBJ (DNA Database of Japan)"),
	NBRF("NBRF"), Protein_Research_Foundation("Protein Research Foundation"),
	SWISS_PROT("SWISS-PROT"), PDB("PDB"), Patents("Patents"),
	GenInfo_Backbone_Id("GenInfo Backbone Id"),
	General_Database_Identifier("General Database Identifier"),
	NCBI_Reference_Sequence("NCBI Reference Sequence"),
	Local_Sequence_Identifier("Local Sequence Identifier"),
	Other("Other (No fields extracted from header)");

	private final String legacyName;

	private FastaTypes(String name) {
		this.legacyName = name;
	}

	@Override
	public String getText() {

		return legacyName;
	}

	@Override
	public String getActionCommand() {
		return getText();
	}

	@Override
	public String getToolTip() {
		return getText();
	}

	@Override
	public boolean isDefault() {
		return this == getDefault();
	}

	public static FastaTypes getDefault() {
		return Other;
	}

	public static FastaTypes resolveLegacyName(String legacyName) {
		if (legacyName == null) {
			throw new NullPointerException("A legacyName must be supplied!");
		}
		Optional<FastaTypes> any = Arrays.stream(values())
				.filter(x -> legacyName.equals(x.getActionCommand())).findAny();
		if (any.isPresent()) {
			return any.get();
		}
		throw new IllegalArgumentException(
				"Legacy name '" + legacyName + "' not recognised");
	}
}
