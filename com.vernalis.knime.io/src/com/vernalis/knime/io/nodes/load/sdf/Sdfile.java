package com.vernalis.knime.io.nodes.load.sdf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.chem.types.MolAdapterCell;
import org.knime.chem.types.MolCellFactory;
import org.knime.chem.types.SdfAdapterCell;
import org.knime.chem.types.SdfCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import com.vernalis.io.MultilineTextObject;

public class Sdfile implements MultilineTextObject {

	private final List<String> lines = new ArrayList<>();
	private String name, userInitials, programName, dimensionality, comments,
			version, molBlock;
	private Integer sf1, irn;
	private Double sf2, energy;
	private int atomCount, bondCount, atomListCount, sTextEntriesCount;
	private boolean isChiral, dateHasTime;
	private Date date;
	private static final SimpleDateFormat sdf_full =
			new SimpleDateFormat("MMddyyHHmm");
	private static final SimpleDateFormat sdf_date =
			new SimpleDateFormat("MMddyy");

	/**
	 * Constructor to use to create empty object for speccreation only purposes
	 */
	public Sdfile() {

	}

	public Sdfile(List<String> lines)
			throws ParseException, IllegalArgumentException {
		this.lines.addAll(lines);
		if (lines.size() < 5) {
			throw new IllegalArgumentException(
					"An sd-file entry must have a minimum of 5 lines");
		}
		// if (!"$$$$".equals(lines.get(lines.size() - 1))) {
		// throw new IllegalArgumentException("SD-File must end with '$$$$'");
		// }
		// Parse 1st header line
		name = nullify(lines.get(0));
		// Parse 2nd header line
		String line2 = lines.get(1);
		userInitials = nullify(subString(line2, 0, 2));
		programName = nullify(subString(line2, 2, 10));
		date = parseDate(subString(line2, 10, 20).trim());
		dimensionality = nullify(subString(line2, 20, 22));
		sf1 = parseNullableInteger(subString(line2, 22, 24));
		sf2 = parseNullableDouble(subString(line2, 24, 34));
		energy = parseNullableDouble(subString(line2, 34, 46));
		irn = parseNullableInteger(
				line2.length() > 46 ? line2.substring(46) : null);
		// Parse 3rd header line
		comments = nullify(lines.get(2));

		// Parse counts line
		String countLine = lines.get(3);
		atomCount = parseInt(subString(countLine, 0, 3));
		bondCount = parseInt(subString(countLine, 3, 6));
		atomListCount = parseInt(subString(countLine, 6, 9));
		isChiral = parseInt(subString(countLine, 12, 15)) == 1;
		sTextEntriesCount = parseInt(subString(countLine, 15, 18));
		version = nullify(
				countLine.length() > 33 ? countLine.substring(33) : null);
		boolean isFirst = true;
		boolean foundMEnd = false;
		StringBuilder sb = new StringBuilder();
		for (String l : lines) {
			if (!isFirst) {
				sb.append("\n");
			} else {
				isFirst = false;
			}
			sb.append(l);
			if (l.toUpperCase().startsWith("M  END")) {
				foundMEnd = true;
				sb.append("\n");// We will replace later!
				break;
			}
		}
		if (!foundMEnd) {
			molBlock = null;
		} else {
			molBlock = sb.toString();
		}
	}

	private String subString(String str, int start, int end) {
		if (str == null) {
			return null;
		}
		if (str.isEmpty()) {
			return str;
		}
		if (str.length() > end) {
			return str.substring(start, end);
		}
		if (str.length() > start) {
			return str.substring(start);
		}
		return null;
	}

	private int parseInt(String string) {
		if (string == null || string.trim().isEmpty()) {
			return 0;
		}
		try {
			return Integer.parseInt(string.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private Double parseNullableDouble(String string) {
		if (string == null || string.trim().isEmpty()) {
			return null;
		}
		try {
			return Double.parseDouble(string.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Integer parseNullableInteger(String string) {
		if (string == null || string.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(string.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private String nullify(String string) {
		return string == null ? null
				: string.trim().isEmpty() ? null : string.trim();
	}

	private synchronized Date parseDate(String dateString) {
		if (dateString == null || dateString.isEmpty()) {
			return null;
		}
		if (dateString.length() == 6) {
			try {
				dateHasTime = false;
				return sdf_date.parse(dateString);
			} catch (ParseException e) {
				return null;
			}
		}
		try {
			dateHasTime = true;
			return sdf_full.parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}

	@Override
	public DataColumnSpec[] getNewColumnSpecs() {
		return new DataColumnSpec[] {
				new DataColumnSpecCreator("SD-File", SdfAdapterCell.RAW_TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Mol Block", MolAdapterCell.RAW_TYPE)
						.createSpec(),
				/* From the Header block Line 1 */
				new DataColumnSpecCreator("Molecule Name", StringCell.TYPE)
						.createSpec(),
				/* From the Header block Line 2 */
				new DataColumnSpecCreator("User Initials", StringCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Program Name", StringCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Date", DateAndTimeCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Dimensionality", StringCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Scaling Factor (1)", IntCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Scaling Factor (2)", DoubleCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Energy", DoubleCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Internal Registry Number",
						IntCell.TYPE).createSpec(),
				/* From the Header block Line 3 */
				new DataColumnSpecCreator("Comments", StringCell.TYPE)
						.createSpec(),
				/* From counts line */
				new DataColumnSpecCreator("Atom Count", IntCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Bond Count", IntCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Atom List Count", IntCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("stext Entries count", IntCell.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Chiral", BooleanCellFactory.TYPE)
						.createSpec(),
				new DataColumnSpecCreator("Molfile Version", StringCell.TYPE)
						.createSpec() };
	}

	@Override
	public DataCell[] getNewCells(String lineSeparator) {

		return new DataCell[] {
				SdfCellFactory.createAdapterCell(lines.stream()
						.collect(Collectors.joining(lineSeparator))),
				MolCellFactory.createAdapterCell("\n".equals(lineSeparator)
						? molBlock : molBlock.replace("\n", lineSeparator)),
				getStringCell(name), getStringCell(userInitials),
				getStringCell(programName), getDateCell(date, dateHasTime),
				getStringCell(dimensionality), getIntCell(sf1),
				getDoubleCell(sf2), getDoubleCell(energy), getIntCell(irn),
				getStringCell(comments), new IntCell(atomCount),
				new IntCell(bondCount), new IntCell(atomListCount),
				new IntCell(sTextEntriesCount),
				BooleanCellFactory.create(isChiral), getStringCell(version) };
	}

	private static DataCell getDoubleCell(Double d) {
		return d == null ? DataType.getMissingCell()
				: new DoubleCell(d.doubleValue());
	}

	private static DataCell getIntCell(Integer i) {
		return i == null ? DataType.getMissingCell()
				: new IntCell(i.intValue());
	}

	private static DataCell getDateCell(Date date, boolean hasTime) {
		return date == null ? DataType.getMissingCell()
				: new DateAndTimeCell(date.getTime(), true, hasTime, false);
	}

	private static DataCell getStringCell(String str) {
		return str == null ? DataType.getMissingCell() : new StringCell(str);

	}

}
