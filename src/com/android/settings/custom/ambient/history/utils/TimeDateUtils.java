package com.android.settings.custom.ambient.history.utils;

import android.content.Context;
import android.text.format.DateFormat;

import com.android.settings.R;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TimeDateUtils {
    public static String tsToLocalTime(Context context, long ts) {
        String timePattern = DateFormat.is24HourFormat(context) ? "HH:mm" : "hh:mm a";
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(ts);
        return new SimpleDateFormat(timePattern, Locale.getDefault()).format(calendar.getTime());
    }

    public static String tsToLocalDate(Context context, long ts) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(ts);
        return DateFormat.getDateFormat(context).format(calendar.getTime());
    }

    public static String getDaysAgo(Context context, long ts, boolean returnInt) {
        String normalDatePattern = ((SimpleDateFormat) DateFormat.getDateFormat(context)).toLocalizedPattern();
        String longDatePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMMdd");
        String longDatePatternWithYear = DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMMMdd");
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(normalDatePattern);
        final LocalDate firstDate = LocalDate.now();
        final LocalDate secondDate = LocalDate.parse(tsToLocalDate(context, ts), formatter);
        int days = Math.round(ChronoUnit.DAYS.between(secondDate, firstDate));
        if (returnInt) {
            return Integer.toString(days);
        }
        if (days == 0) {
            return context.getString(R.string.ambient_play_history_today);
        } else if (days == 1) {
            return context.getString(R.string.ambient_play_history_yesterday);
        } else {
            return secondDate.format(DateTimeFormatter.ofPattern(secondDate.getYear() == firstDate.getYear() ? longDatePattern : longDatePatternWithYear));
        }
    }
}
