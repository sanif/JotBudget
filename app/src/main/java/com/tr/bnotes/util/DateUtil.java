package com.tr.bnotes.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class DateUtil {
    private static final String CURRENT_YEAR_DATE_FORMAT = "dd MMM";
    private static final String GENERAL_DATE_FORMAT = "dd/MM/yyyy";

    private static final SimpleDateFormat CURRENT_YEAR_DATE_FORMATTER
            = new SimpleDateFormat(CURRENT_YEAR_DATE_FORMAT, Locale.US);
    private static final SimpleDateFormat GENERAL_DATE_FORMATTER
            = new SimpleDateFormat(GENERAL_DATE_FORMAT, Locale.US);

    private DateUtil() {

    }

    /**
     * Formats the date in UI-friendly way
     *
     * @param today calendar with the date set to today
     * @param date date to format
     * @return formatted date
     */
    public static String formatForUI(Calendar today, Calendar date) {
        if (today.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
            if (today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
                return "Today";
            }
            return currentYearDateFormat(date.getTimeInMillis());
        } else {
            return format(date.getTimeInMillis());
        }
    }

    public static String format(long date) {
        return generalDateFormat(date);
    }

    /**
     * @throws IllegalArgumentException if date is not in {@link DateUtil#GENERAL_DATE_FORMAT}
     *                                  format.
     */
    public static long parse(String date) {
        try {
            return generalDateParse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Date is not in '" + GENERAL_DATE_FORMAT + "' pattern");
        }
    }

    // methods ensuring fine-grained locking for non thread-safe SimpleDateFormat
    private static String currentYearDateFormat(long date) {
        synchronized (CURRENT_YEAR_DATE_FORMATTER) {
            return CURRENT_YEAR_DATE_FORMATTER.format(date);
        }
    }

    private static String generalDateFormat(long date) {
        synchronized (GENERAL_DATE_FORMATTER) {
            return GENERAL_DATE_FORMATTER.format(date);
        }
    }

    private static long generalDateParse(String date) throws ParseException {
        synchronized (GENERAL_DATE_FORMATTER) {
            return GENERAL_DATE_FORMATTER.parse(date).getTime();
        }
    }
}