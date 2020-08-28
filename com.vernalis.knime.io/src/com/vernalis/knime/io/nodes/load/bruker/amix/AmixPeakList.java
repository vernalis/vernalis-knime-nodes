/*******************************************************************************
 * Copyright (c) 2018, 2019 Vernalis (R&D) Ltd
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
package com.vernalis.knime.io.nodes.load.bruker.amix;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.date.DateAndTimeCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import com.vernalis.io.MultilineTextObject;

/**
 * A container / parser object to contain a single spectrum peaklist from an
 * AMIX (Bruker) Peak List file
 * 
 * @author s.roughley
 *
 */
final class AmixPeakList implements MultilineTextObject {

	private final List<String> lines = new ArrayList<>();
	private String title;
	private String user;
	private String host;
	private String spectrum;
	private String date;
	private boolean is2D;
	private final List<AmixPeak> peaks = new ArrayList<>();
	private static final Pattern AMIX_PROPERTY_PATTERN = Pattern
			.compile("! (TITLE|DATE|USER|HOST|spectrum)\\s*(?::|=)\\s*(.*)");

	private static final DataColumnSpec[] NEW_COL_SPECS;
	// This format appears to be written on Windows systems
	private static final SimpleDateFormat AMIX_DATE_FORMAT =
			new SimpleDateFormat("EEE yyyy-MM-dd HH:mm:ss");
	// This format appears to be written on Linux systems
	private static final SimpleDateFormat AMIX_DATE_FORMAT_2 =
			new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	static {
		NEW_COL_SPECS = new DataColumnSpec[11];
		int i = 0;
		NEW_COL_SPECS[i++] =
				new DataColumnSpecCreator("Peak List", StringCell.TYPE)
						.createSpec();
		NEW_COL_SPECS[i++] = new DataColumnSpecCreator("Title", StringCell.TYPE)
				.createSpec();
		NEW_COL_SPECS[i++] =
				new DataColumnSpecCreator("Date", DateAndTimeCellFactory.TYPE)
						.createSpec();
		NEW_COL_SPECS[i++] =
				new DataColumnSpecCreator("User", StringCell.TYPE).createSpec();
		NEW_COL_SPECS[i++] =
				new DataColumnSpecCreator("Host", StringCell.TYPE).createSpec();
		NEW_COL_SPECS[i++] =
				new DataColumnSpecCreator("Spectrum", StringCell.TYPE)
						.createSpec();
		NEW_COL_SPECS[i++] = new DataColumnSpecCreator("w1",
				ListCell.getCollectionType(DoubleCell.TYPE)).createSpec();
		NEW_COL_SPECS[i++] = new DataColumnSpecCreator("w2",
				ListCell.getCollectionType(DoubleCell.TYPE)).createSpec();
		NEW_COL_SPECS[i++] = new DataColumnSpecCreator("Intensity",
				ListCell.getCollectionType(IntCell.TYPE)).createSpec();
		NEW_COL_SPECS[i++] = new DataColumnSpecCreator("Volume",
				ListCell.getCollectionType(IntCell.TYPE)).createSpec();
		NEW_COL_SPECS[i++] = new DataColumnSpecCreator("Annotation",
				ListCell.getCollectionType(StringCell.TYPE)).createSpec();
	}

	/**
	 * Constructor to use to create empty object for speccreation only purposes
	 */
	public AmixPeakList() {

	}

	public AmixPeakList(List<String> lines)
			throws ParseException, IllegalArgumentException {
		this.lines.addAll(lines);
		for (String line : lines) {
			if (line.trim().isEmpty()) {
				continue;
			}
			if (line.trim().startsWith("w")) {
				is2D = line.contains("w2");
				continue;
			}

			if (line.startsWith("!")) {
				// Look for properties
				Matcher m = AMIX_PROPERTY_PATTERN.matcher(line);
				if (m.matches()) {
					switch (m.group(1)) {
					case "TITLE":
						title = m.group(2);
						break;
					case "DATE":
						date = m.group(2);
						break;
					case "USER":
						user = m.group(2);
						break;
					case "HOST":
						host = m.group(2);
						break;
					case "spectrum":
						spectrum = m.group(2);
						break;
					default:
						// Do nothing - not a known property!
					}
				}
			} else {
				peaks.add(is2D ? new Amix2DPeak(line) : new AmixPeak(line));
			}
		}
	}

	/**
	 * @return the lines
	 */
	public List<String> getLines() {
		return lines;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the spectrum
	 */
	public String getSpectrum() {
		return spectrum;
	}

	/**
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return an unmodifiable view of the list
	 */
	public List<AmixPeak> getPeaks() {
		return Collections.unmodifiableList(peaks);
	}

	@Override
	public DataColumnSpec[] getNewColumnSpecs() {
		return NEW_COL_SPECS;
	}

	@Override
	public DataCell[] getNewCells(String lineSeparator) {

		DataCell[] retVal = new DataCell[NEW_COL_SPECS.length];
		int i = 0;
		retVal[i++] =
				lines == null || lines.isEmpty() ? DataType.getMissingCell()
						: new StringCell(lines.stream()
								.collect(Collectors.joining(lineSeparator)));
		retVal[i++] = title == null || title.isEmpty()
				? DataType.getMissingCell() : new StringCell(title);
		DataCell dateCell = date == null || date.isEmpty()
				? DataType.getMissingCell() : parseDate(date);

		retVal[i++] = dateCell;
		retVal[i++] = user == null || user.isEmpty() ? DataType.getMissingCell()
				: new StringCell(user);
		retVal[i++] = host == null || host.isEmpty() ? DataType.getMissingCell()
				: new StringCell(host);
		retVal[i++] = spectrum == null || spectrum.isEmpty()
				? DataType.getMissingCell() : new StringCell(spectrum);

		// Now handle the collection cells
		List<DataCell> w1 = new ArrayList<>();
		List<DataCell> w2 = new ArrayList<>();
		List<DataCell> intensity = new ArrayList<>();
		List<DataCell> volume = new ArrayList<>();
		List<DataCell> annotation = new ArrayList<>();
		boolean hasAnnotation = false;
		for (AmixPeak peak : peaks) {
			w1.add(new DoubleCell(peak.w1));
			if (is2D) {
				w2.add(new DoubleCell(((Amix2DPeak) peak).w2));
			}
			intensity.add(new IntCell(peak.intensity));
			volume.add(new IntCell(peak.volume));
			if (peak.annotation == null || peak.annotation.isEmpty()) {
				annotation.add(DataType.getMissingCell());
			} else {
				annotation.add(new StringCell(peak.annotation));
				hasAnnotation = true;
			}
		}
		retVal[i++] = w1.isEmpty() ? DataType.getMissingCell()
				: CollectionCellFactory.createListCell(w1);
		retVal[i++] = w2.isEmpty() ? DataType.getMissingCell()
				: CollectionCellFactory.createListCell(w2);
		retVal[i++] = intensity.isEmpty() ? DataType.getMissingCell()
				: CollectionCellFactory.createListCell(intensity);
		retVal[i++] = volume.isEmpty() ? DataType.getMissingCell()
				: CollectionCellFactory.createListCell(volume);
		retVal[i++] = !hasAnnotation || annotation.isEmpty()
				? DataType.getMissingCell()
				: CollectionCellFactory.createListCell(annotation);
		return retVal;

	}

	@SuppressWarnings("deprecation")
	private static synchronized DataCell parseDate(String date) {
		try {
			return DateAndTimeCellFactory.create(date, AMIX_DATE_FORMAT);
		} catch (IllegalArgumentException e) {
			return DateAndTimeCellFactory.create(date, AMIX_DATE_FORMAT_2);
		}

	}

	/**
	 * Simple container class to contain a single 1D peak. At present, volume is
	 * assumed to be an integer by analogy with intensity as we have no
	 * available examples to indicate otherwise
	 * 
	 * @author s.roughley
	 *
	 */
	private static class AmixPeak {

		final String annotation;
		final double w1;
		final int intensity;
		final int volume;
		private static final Pattern PEAK_LINE_PATTERN = Pattern.compile(
				"\\s*([+\\-]?[\\d]*(?:\\.?[\\d]+))?\\s*\\(\\s*[\\d]+\\)"
						+ "\\s*(?:([+\\-]?[\\d]*(?:\\.?[\\d]+))?\\s*\\(\\s*[\\d]+\\))?"
						+ "\\s*([+\\-]?[\\d]+)\\s*([+\\-]?[\\d]+)\\s*(.*)");

		AmixPeak(String line) throws AmixPeakFormatException {
			assert !line.trim().startsWith("!") : "Line '" + line
					+ "' is a comment line";
			assert !line.trim().isEmpty() : "Empty line";
			assert !line.trim().startsWith("w1") : "Line '" + line
					+ "' is a peak header line";

			Matcher m = PEAK_LINE_PATTERN.matcher(line);
			if (!m.matches()) {
				throw new AmixPeakFormatException(
						"Line '" + line + "' does not match peak line pattern");
			}
			w1 = Double.parseDouble(m.group(1));

			intensity = Integer.parseInt(m.group(3));
			volume = Integer.parseInt(m.group(4));
			annotation = m.group(5).isEmpty() ? null : m.group(5);
		}
	}

	/**
	 * Simple container class to contain a single 2D peak
	 * 
	 * @author s.roughley
	 *
	 */
	private static class Amix2DPeak extends AmixPeak {

		private static final Pattern PEAK_LINE_PATTERN = Pattern.compile(
				"\\s*([+\\-]?[\\d]*(?:\\.?[\\d]+))?\\s*\\(\\s*[\\d]+\\)"
						+ "\\s*([+\\-]?[\\d]*(?:\\.?[\\d]+))?\\s*\\(\\s*[\\d]+\\)"
						+ "\\s*([+\\-]?[\\d]+)\\s*([+\\-]?[\\d]+)\\s*(.*)");
		final double w2;

		public Amix2DPeak(String line) throws AmixPeakFormatException {
			super(line);
			Matcher m = PEAK_LINE_PATTERN.matcher(line);
			if (!m.matches()) {
				throw new AmixPeakFormatException(
						"Line '" + line + "' does not match peak line pattern");
			}
			w2 = Double.parseDouble(m.group(2));
		}

	}

	/**
	 * Exception thrown when parsing an {@link AmixPeak} fails
	 * 
	 * @author s.roughley
	 *
	 */
	@SuppressWarnings("serial")
	private static class AmixPeakFormatException
			extends IllegalArgumentException {

		public AmixPeakFormatException(String message) {
			super(message);
		}

	}
}
