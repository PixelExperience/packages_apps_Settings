/*
 * Copyright (C) 2018 PixelExperience
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.android.settings.custom.ambient.history.utils;

import android.content.Context;
import android.text.format.DateFormat;

import com.android.settings.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    public static String getDaysAgo(Context context, long ts) {
        long days = (new Date().getTime() - ts) / 86400000;
        if (days == 0) {
            return context.getString(R.string.ambient_play_history_today);
        } else if (days == 1) {
            return context.getString(R.string.ambient_play_history_yesterday);
        } else if (days < 7) {
            try {
                return String.format(context.getString(R.string.ambient_play_history_days_ago), days);
            } catch (Exception e) {
                return tsToLocalDate(context, ts);
            }
        } else if (days == 7) {
            return context.getString(R.string.ambient_play_history_last_week);
        } else {
            return tsToLocalDate(context, ts);
        }
    }
}
