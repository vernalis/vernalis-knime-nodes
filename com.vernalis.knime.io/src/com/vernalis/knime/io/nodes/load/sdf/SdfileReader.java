package com.vernalis.knime.io.nodes.load.sdf;

import java.io.Reader;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Pattern;

import com.vernalis.io.AbstractMultilineTextObjectReader;

public class SdfileReader extends AbstractMultilineTextObjectReader<Sdfile> {

	/**
	 * The deliminator pattern signalling the start of a new molecule entry
	 */
	private static final Pattern DELIMINATOR_PATTERN =
			Pattern.compile("\\$\\$\\$\\$");

	public SdfileReader(Reader reader) throws IllegalArgumentException {
		super(reader, DELIMINATOR_PATTERN, true, false,
				false/*
						 * Technically we do, but not all writers seem to put it
						 * there...
						 */);

	}

	@Override
	public Sdfile getObjectFromLines(List<String> lines)
			throws ParseException, IllegalArgumentException {
		return new Sdfile(lines);
	}

}
