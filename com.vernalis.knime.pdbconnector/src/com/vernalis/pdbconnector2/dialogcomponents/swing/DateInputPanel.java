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
package com.vernalis.pdbconnector2.dialogcomponents.swing;

import java.awt.Color;
import java.awt.Font;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A simple compound spinner panel to allow date input. The fields interlock
 * with each other such that no spinner can require another spinner value to
 * change to turn over due to the set bounds. For example, a spinner with the
 * lower bound 14-May-2020, currently set to 13-May-2021 will not allow the year
 * to be changed to 2020 by changing the resulting date to 14-May-2020 rather
 * than 13-May-2020. The exception for this behaviour is for months when the day
 * of the month is beyond the normal end of the 'destination' month. For
 * example, A spinner set to 30-Mar-2020 will turn over to 29-Feb-2020 when the
 * month spinner is changed, and then to 28-Feb-2019 if the year spinner is then
 * changed
 *
 *
 * @author S.Roughley knime@vernalis.com
 * @since 1.28.0
 */
public class DateInputPanel extends JPanel
		implements ChangeListener, Changeable {

	private static final long serialVersionUID = 1L;

	/**
	 * The full list of month display names
	 */
	private static final List<String> MONTHS = Collections
			.unmodifiableList(Arrays.asList("Jan", "Feb", "Mar", "Apr", "May",
					"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"));
	private static final Calendar DEFAULT_MIN_DATE;
	static {
		DEFAULT_MIN_DATE = Calendar.getInstance();
		DEFAULT_MIN_DATE.clear();
		// 1st Jan 1900
		DEFAULT_MIN_DATE.set(1900, 0, 1);
	}
	private static final Calendar DEFAULT_MAX_DATE;
	static {
		DEFAULT_MAX_DATE = Calendar.getInstance();
		DEFAULT_MAX_DATE.clear();
		// 31st Dec 3000
		DEFAULT_MAX_DATE.set(3000, 11, 31);
	}

	private final JSpinner daySpinner;
	private final JSpinner monthSpinner;
	private final JSpinner yearSpinner;

	private final Calendar value = Calendar.getInstance();
	private final Calendar minBound = Calendar.getInstance();
	private final Calendar maxBound = Calendar.getInstance();

	// The required display font - some of the JSpinner formatters apply bold
	private final Font font;
	// A flag to stop multiple change events firing when adjusting bounds
	private boolean isAdjustingBounds = false;

	/**
	 * Constructor with default bounds (01-Jan-1900 - 31-Dec-3000) and the
	 * current date
	 */
	public DateInputPanel() {
		this(Calendar.getInstance());
	}

	/**
	 * Constructor with default bounds (01-Jan-1900 - 31-Dec-3000) and the
	 * specified date
	 *
	 * @param date
	 *            the initial displayed date
	 */
	public DateInputPanel(Date date) {
		this(dateToCalendar(date));
	}

	/**
	 * Constructor with specified bounds and date
	 *
	 * @param date
	 *            the initial displayed date
	 * @param minDate
	 *            The minimum bound date (inclusive)
	 * @param maxDate
	 *            The maximum bound date (inclusive)
	 */
	public DateInputPanel(Date date, Date minDate, Date maxDate) {
		this(dateToCalendar(date), dateToCalendar(minDate),
				dateToCalendar(maxDate));
	}

	private static Calendar dateToCalendar(Date date) {
		if (date == null) {
			return null;
		}
		final Calendar retVal = Calendar.getInstance();
		retVal.clear();
		retVal.setTime(date);
		return retVal;
	}

	/**
	 * Constructor with default bounds (01-Jan-1900 - 31-Dec-3000) and the
	 * specified date
	 *
	 * @param date
	 *            the initial displayed date
	 */
	public DateInputPanel(Calendar date) {
		this(date, DEFAULT_MIN_DATE, DEFAULT_MAX_DATE);
	}

	/**
	 * Constructor with specified bounds and date
	 *
	 * @param date
	 *            the initial displayed date
	 * @param minDate
	 *            The minimum bound date (inclusive)
	 * @param maxDate
	 *            The maximum bound date (inclusive)
	 */
	public DateInputPanel(Calendar date, Calendar minDate, Calendar maxDate) {

		// We set supplied dates - NB we only keep the dd/mm/yyyy fields
		value.clear();
		value.set(getCalendarYear(date), getCalendarMonth(date),
				getCalendarDayOfMonth(date));
		minBound.clear();
		if (minDate != null) {
			minBound.set(getCalendarYear(minDate), getCalendarMonth(minDate),
					getCalendarDayOfMonth(minDate));
		} else {
			minBound.set(getCalendarYear(DEFAULT_MIN_DATE),
					getCalendarMonth(DEFAULT_MIN_DATE),
					getCalendarDayOfMonth(DEFAULT_MIN_DATE));
		}
		maxBound.clear();
		if (maxDate != null) {
			maxBound.set(getCalendarYear(maxDate), getCalendarMonth(maxDate),
					getCalendarDayOfMonth(maxDate));
		} else {
			maxBound.set(getCalendarYear(DEFAULT_MAX_DATE),
					getCalendarMonth(DEFAULT_MAX_DATE),
					getCalendarDayOfMonth(DEFAULT_MAX_DATE));
		}

		// Now check the bounds - NB Easier to do this now, which allows for
		// 'null'
		// bounds as arguments
		if (minBound.after(maxBound) || value.before(minBound)
				|| value.after(maxBound)) {
			throw new IllegalArgumentException(
					"The dates supplied must be in the order minDate ("
							+ minBound.getTime() + "), defaultDate ("
							+ value.getTime() + "), maxDate ("
							+ maxBound.getTime() + ") chronologically");
		}

		// Set up the year spinner - its bounds never change
		yearSpinner =
				new JSpinner(new SpinnerNumberModel(getCalendarYear(this.value),
						getCalendarYear(minBound), getCalendarYear(maxBound),
						1));
		yearSpinner.addChangeListener(this);

		final JFormattedTextField yearTextField =
				((JSpinner.DefaultEditor) yearSpinner.getEditor())
						.getTextField();
		yearTextField.setColumns(5);
		font = yearTextField.getFont();
		// No ',' - Why does this make the text bold????
		yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#"));
		// Get the new editor and restore the correct font
		((JSpinner.DefaultEditor) yearSpinner.getEditor()).getTextField()
				.setFont(font);

		// Set up the month spinner - its bounds might sometimes change
		monthSpinner =
				new JSpinner(new SpinnerListModel(getMonthsForCurrentYear()));
		monthSpinner.setValue(MONTHS.get(getCalendarMonth(value)));
		monthSpinner.addChangeListener(this);
		formatMonthSpinner();

		// DaySpinner
		daySpinner = new JSpinner(new SpinnerNumberModel(
				getCalendarDayOfMonth(value), getFirstDayOfSelectedMonth(),
				getLastDayOfSelectedMonth(), 1));
		daySpinner.addChangeListener(this);
		formatDaySpinner();

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(daySpinner);
		add(monthSpinner);
		add(yearSpinner);

	}

	private void formatMonthSpinner() {
		final JFormattedTextField monthTxtFld =
				((JSpinner.ListEditor) monthSpinner.getEditor()).getTextField();
		monthTxtFld.setColumns(3);
		monthTxtFld.setFont(font);
		monthTxtFld.setHorizontalAlignment(JTextField.RIGHT);
	}

	private void formatDaySpinner() {
		final JFormattedTextField dayTxtFld =
				((JSpinner.DefaultEditor) daySpinner.getEditor())
						.getTextField();
		dayTxtFld.setColumns(2);
		dayTxtFld.setFont(font);
	}

	private int getYearFromSpinner() {
		return (int) yearSpinner.getModel().getValue();
	}

	private String getMonthFromSpinner() {
		return (String) monthSpinner.getValue();
	}

	private int getMonthIndexFromSpinner() {
		return MONTHS.indexOf(getMonthFromSpinner());
	}

	private int getDayFromSpinner() {
		return (int) daySpinner.getValue();
	}

	private List<String> getMonthsForCurrentYear() {
		int firstMonthIndex = 0;
		int lastMonthIndex = 12;
		boolean isSubset = false;
		if (getYearFromSpinner() == getCalendarYear(minBound)) {
			firstMonthIndex = getCalendarMonth(minBound);
			// Day spinner will be null during instantiation
			if (daySpinner != null
					&& getDayFromSpinner() < getCalendarDayOfMonth(minBound)) {
				firstMonthIndex++;
			}
			if (firstMonthIndex > 0) {
				isSubset = true;
			}
		}
		if (getYearFromSpinner() == getCalendarYear(maxBound)) {
			lastMonthIndex = getCalendarMonth(maxBound) + 1;
			if (daySpinner != null
					&& getDayFromSpinner() > getCalendarDayOfMonth(maxBound)) {
				lastMonthIndex--;
			}
			if (lastMonthIndex < 12) {
				isSubset = true;
			}
		}
		return isSubset ? MONTHS.subList(firstMonthIndex, lastMonthIndex)
				: MONTHS;
	}

	private int getFirstDayOfSelectedMonth() {
		int retVal = 1;
		if (getYearFromSpinner() == getCalendarYear(minBound)
				&& getMonthFromSpinner()
						.equals(MONTHS.get(getCalendarMonth(minBound)))) {
			retVal = Math.max(retVal, getCalendarDayOfMonth(minBound));
		}
		return retVal;
	}

	private int getLastDayOfSelectedMonth() {
		// Bizarrely, infuriatingly maybe, and certainly bewilderingly, month
		// here is
		// 1-based, whereas in Calendar it is, obviously.. 0-based
		int retVal = YearMonth
				.of(getYearFromSpinner(), getMonthIndexFromSpinner() + 1)
				.lengthOfMonth();
		if (getYearFromSpinner() == getCalendarYear(maxBound)
				&& getMonthFromSpinner()
						.equals(MONTHS.get(getCalendarMonth(maxBound)))) {
			retVal = Math.min(retVal, getCalendarDayOfMonth(maxBound));
		}
		return retVal;
	}

	/**
	 * @param date
	 *            The date
	 * @return The day of the month
	 */
	protected static int getCalendarDayOfMonth(Calendar date) {
		return date.get(Calendar.DATE);
	}

	/**
	 * @param date
	 *            The date
	 * @return The month of the year
	 */
	protected static int getCalendarMonth(Calendar date) {
		return date.get(Calendar.MONTH);
	}

	/**
	 * @param date
	 *            The date
	 * @return The year
	 */
	protected static int getCalendarYear(Calendar date) {
		return date.get(Calendar.YEAR);
	}

	/**
	 * @see #addChangeListener(ChangeListener)
	 */
	@Override
	public void registerChangeListener(ChangeListener l) {
		addChangeListener(l);
	}

	/**
	 * Add a {@link ChangeListener}
	 * 
	 * @param l
	 *            The change listener, which will be fired when the displayed
	 *            date is changed either in the UI, or by a call to
	 *            {@link #setCurrentDate(Calendar)} or
	 *            {@link #setCurrentDate(Date)}. The cause of the
	 *            {@link ChangeEvent} is the set value
	 */
	@Override
	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);

	}

	@Override
	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);

	}

	@Override
	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}

	@Override
	public void setEnabled(boolean enabled) {
		daySpinner.setEnabled(enabled);
		monthSpinner.setEnabled(enabled);
		yearSpinner.setEnabled(enabled);
	}

	/**
	 * @return the current displayed date
	 */
	public Calendar getCurrentCalendarDate() {
		return value;
	}

	/**
	 * @return The current displayed date
	 */
	public Date getCurrentDate() {
		return value.getTime();
	}

	/**
	 * Set the current date
	 * 
	 * @param date
	 *            Set the current stored date, which must not be before the
	 *            minimum date or after the maximum date
	 */
	public void setCurrentDate(Calendar date) {
		final Calendar d = Calendar.getInstance();
		d.clear();
		d.set(getCalendarYear(date), getCalendarMonth(date),
				getCalendarDayOfMonth(date));
		// We update the spinners - only fire a state changed when all three are
		// updated
		if (d.before(minBound) || d.after(maxBound)) {
			throw new IllegalArgumentException("The date (" + d.getTime()
					+ ") is outside the valid bounds range ("
					+ minBound.getTime() + " - " + maxBound.getTime() + ")");
		}
		isAdjustingBounds = true;
		yearSpinner.setValue(getCalendarYear(date));
		monthSpinner.setValue(MONTHS.get(getCalendarMonth(date)));
		// Now allow the state change to fire, which will then update the stored
		// value
		isAdjustingBounds = false;
		daySpinner.setValue(getCalendarDayOfMonth(date));
	}

	/**
	 * @param date
	 *            The date
	 * @see #setCurrentDate(Calendar)
	 */
	public void setCurrentDate(Date date) {
		setCurrentDate(dateToCalendar(date));
	}

	/**
	 * Set the Maximum date value
	 * 
	 * @param date
	 *            The new upper maximum value, which is the highest date which
	 *            can be set in the spinner, and must not be before the current
	 *            'set' date shown by the spinner
	 */
	public void setMaximumDate(Calendar date) {
		final Calendar d = Calendar.getInstance();
		d.clear();
		d.set(getCalendarYear(date), getCalendarMonth(date),
				getCalendarDayOfMonth(date));
		if (d.before(value)) {
			throw new IllegalArgumentException(
					"The maximum cannot invalidate the current value");
		}
		maxBound.clear();
		maxBound.set(getCalendarYear(d), getCalendarMonth(d),
				getCalendarDayOfMonth(d));
		// Now we need to change all the bounds potentially, but as the values
		// dont
		// change we dont fire the changelisteners..
		isAdjustingBounds = true;
		((SpinnerNumberModel) yearSpinner.getModel())
				.setMaximum(getCalendarYear(maxBound));
		updateBounds();
		isAdjustingBounds = false;
	}

	/**
	 * @param date
	 *            The new upper maximum value, which is the highest date which
	 *            can be set in the spinner, and must not be before the current
	 *            'set' date shown by the spinner
	 * @see #setMaximumDate(Calendar)
	 */
	public void setMaximumDate(Date date) {
		setMaximumDate(dateToCalendar(date));
	}

	/**
	 * Set the minimum date
	 * 
	 * @param date
	 *            The new lower minimum value, which is the lowest date which
	 *            can be set in the spinner, and must not be after the current
	 *            'set' date shown by the spinner
	 */
	public void setMinimumDate(Calendar date) {
		final Calendar d = Calendar.getInstance();
		d.clear();
		d.set(getCalendarYear(date), getCalendarMonth(date),
				getCalendarDayOfMonth(date));
		if (d.after(value)) {
			throw new IllegalArgumentException(
					"The minimum cannot invalidate the current value");
		}
		minBound.clear();
		minBound.set(getCalendarYear(d), getCalendarMonth(d),
				getCalendarDayOfMonth(d));
		// Now we need to change all the bounds potentially, but as the values
		// dont
		// change we dont fire the changelisteners..
		isAdjustingBounds = true;
		((SpinnerNumberModel) yearSpinner.getModel())
				.setMinimum(getCalendarYear(minBound));
		updateBounds();
		isAdjustingBounds = false;
	}

	/**
	 * @param date
	 *            The new lower minimum value, which is the lowest date which
	 *            can be set in the spinner, and must not be after the current
	 *            'set' date shown by the spinner
	 * @see #setMinimumDate(Calendar)
	 */
	public void setMinimumDate(Date date) {
		setMinimumDate(dateToCalendar(date));
	}

	/**
	 * Notify the listeners that a change has occurred, passing the value as the
	 * cause
	 */
	protected void fireStateChanged() {
		final Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener) listeners[i + 1])
						.stateChanged(new ChangeEvent(value));
			}
		}
	}

	/**
	 * @param fg
	 *            The foreground colour to apply to the spinner editors
	 */
	public void setEditorForeground(Color fg) {
		((JSpinner.DefaultEditor) daySpinner.getEditor()).getTextField()
				.setForeground(fg);
		((JSpinner.DefaultEditor) monthSpinner.getEditor()).getTextField()
				.setForeground(fg);
		((JSpinner.DefaultEditor) yearSpinner.getEditor()).getTextField()
				.setForeground(fg);
	}

	/**
	 * @param bg
	 *            The background colour to apply to the spinner editors
	 */
	public void setEditorBackground(Color bg) {
		((JSpinner.DefaultEditor) daySpinner.getEditor()).getTextField()
				.setBackground(bg);
		((JSpinner.DefaultEditor) monthSpinner.getEditor()).getTextField()
				.setBackground(bg);
		((JSpinner.DefaultEditor) yearSpinner.getEditor()).getTextField()
				.setBackground(bg);
	}

	/**
	 * Fired by a change to any of the spinner values. If
	 * {@code isAdjustingBounds} is set to {@code true} the method does nothing,
	 * otherwise it makes a call to {@link #updateBounds()}, and then if the
	 * result is a change in the displayed value then it fires a
	 * {@link ChangeEvent} to any registered listeners.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (isAdjustingBounds) {
			// Dont descend into a nested chaos...
			return;
		}
		// Make sure the date spinner is showing the right value
		final long oldTime = value.getTimeInMillis();

		// Now stop any further changes being fired while we check and
		// potentially
		// shuffle the bounds
		isAdjustingBounds = true;
		updateBounds();
		// Reset the flag
		isAdjustingBounds = false;

		// We update the date...
		value.clear();
		value.set(getYearFromSpinner(), getMonthIndexFromSpinner(),
				getDayFromSpinner());
		if (oldTime != value.getTimeInMillis()) {
			// And notify any registered listeners - the source of the new event
			// will be the
			// current value
			fireStateChanged();
		}

	}

	/**
	 * Adjusts the spinner bounds to reflect the new setting (e.g. different
	 * number of days in month, and possible changes to upper or lower bounds
	 * for all 3 spinners based on the bounds and current value)
	 */
	protected void updateBounds() {
		// The year bounds change if setMaximumDate() or setMinimiumDate() are
		// called, and are handled there
		// They also need to change by +/-1 if the month or day of month is on
		// the
		// 'wrong' side of the corresponding bound
		final SpinnerNumberModel yrModel =
				(SpinnerNumberModel) yearSpinner.getModel();
		int maxYear = getCalendarYear(maxBound);
		int minYear = getCalendarYear(minBound);
		if (getMonthIndexFromSpinner() > getCalendarMonth(maxBound)
				|| getMonthIndexFromSpinner() == getCalendarMonth(maxBound)
						&& getDayFromSpinner() > getCalendarDayOfMonth(
								maxBound)) {
			maxYear--;
		}
		if (getMonthIndexFromSpinner() < getCalendarMonth(minBound)
				|| getMonthIndexFromSpinner() == getCalendarMonth(minBound)
						&& getDayFromSpinner() < getCalendarDayOfMonth(
								minBound)) {
			minYear++;
		}
		if (((Integer) yrModel.getMaximum()).intValue() != maxYear) {
			yrModel.setMaximum(maxYear);
		}
		if (((Integer) yrModel.getMinimum()).intValue() != minYear) {
			yrModel.setMinimum(minYear);
		}

		// Handle months which may be restricted at first and last year of
		// bounds
		final SpinnerListModel monthModel =
				(SpinnerListModel) monthSpinner.getModel();
		final List<String> currMonths = monthModel.getList().stream()
				.map(x -> String.class.cast(x)).collect(Collectors.toList());

		final List<String> newMonths = getMonthsForCurrentYear();
		if (!newMonths.equals(currMonths)) {
			final int newMaxMonth =
					MONTHS.indexOf(newMonths.get(newMonths.size() - 1));
			final int newMinMonth = MONTHS.indexOf(newMonths.get(0));
			final String newValue = MONTHS.get(Math.max(newMinMonth,
					Math.min(newMaxMonth, getMonthIndexFromSpinner())));
			final SpinnerListModel newModel = new SpinnerListModel(newMonths);
			// newModel.setValue(newValue);
			monthSpinner.setModel(newModel);
			monthSpinner.setValue(newValue);
			formatMonthSpinner();
		}

		// And the days of the month
		final SpinnerNumberModel dayModel =
				(SpinnerNumberModel) daySpinner.getModel();
		final int maxMonthDay = getLastDayOfSelectedMonth();
		final int minMonthDay = getFirstDayOfSelectedMonth();
		final int currMaxDay = (int) dayModel.getMaximum();
		final int currMinDay = (int) dayModel.getMinimum();
		final int currDay = getDayFromSpinner();
		if (currMaxDay != maxMonthDay || currMinDay != minMonthDay) {
			final int newValue =
					Math.max(minMonthDay, Math.min(maxMonthDay, currDay));
			daySpinner.setModel(new SpinnerNumberModel(newValue, minMonthDay,
					maxMonthDay, 1));
			formatDaySpinner();
		}
	}

}
