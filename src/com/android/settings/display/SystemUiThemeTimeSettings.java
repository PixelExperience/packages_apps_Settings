/*
 * Copyright (C) 2018 PixelExperience
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.display;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.theme.AutomaticThemeTimeController;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.text.DateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;

public class SystemUiThemeTimeSettings extends SettingsPreferenceFragment {

    private Preference mSystemUiAutomaticThemeStartTimePreference;
    private Preference mSystemUiAutomaticThemeEndTimePreference;

    private String SYSTEM_UI_THEME_AUTOMATIC_START_TIME = "systemui_theme_time_start_time";
    private String SYSTEM_UI_THEME_AUTOMATIC_END_TIME = "systemui_theme_time_end_time";

    private DateFormat mTimeFormatter;

    private static final int DIALOG_START_TIME = 0;
    private static final int DIALOG_END_TIME = 1;

    private AutomaticThemeTimeController mAutomaticThemeTimeController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.systemui_theme_time_settings);
        mSystemUiAutomaticThemeStartTimePreference = (Preference) findPreference(SYSTEM_UI_THEME_AUTOMATIC_START_TIME);
        mSystemUiAutomaticThemeEndTimePreference = (Preference) findPreference(SYSTEM_UI_THEME_AUTOMATIC_END_TIME);
        mTimeFormatter = android.text.format.DateFormat.getTimeFormat(getContext());
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        mAutomaticThemeTimeController = new AutomaticThemeTimeController(getContext());
        updateSummary();
    }

    private void updateSummary() {
        mSystemUiAutomaticThemeStartTimePreference.setSummary(getFormattedTimeString(mAutomaticThemeTimeController.getCustomStartTime()));
        mSystemUiAutomaticThemeEndTimePreference.setSummary(getFormattedTimeString(mAutomaticThemeTimeController.getCustomEndTime()));
    }

    private String getFormattedTimeString(LocalTime localTime) {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(mTimeFormatter.getTimeZone());
        c.set(Calendar.HOUR_OF_DAY, localTime.getHour());
        c.set(Calendar.MINUTE, localTime.getMinute());
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return mTimeFormatter.format(c.getTime());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (SYSTEM_UI_THEME_AUTOMATIC_END_TIME.equals(preference.getKey())) {
            showDialog(DIALOG_END_TIME);
            return true;
        } else if (SYSTEM_UI_THEME_AUTOMATIC_START_TIME.equals(preference.getKey())) {
            showDialog(DIALOG_START_TIME);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public Dialog onCreateDialog(final int dialogId) {
        if (dialogId == DIALOG_START_TIME || dialogId == DIALOG_END_TIME) {
            final LocalTime initialTime;
            if (dialogId == DIALOG_START_TIME) {
                initialTime = mAutomaticThemeTimeController.getCustomStartTime();
            } else {
                initialTime = mAutomaticThemeTimeController.getCustomEndTime();
            }
            final Context context = getContext();
            final boolean use24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
            return new TimePickerDialog(context, (view, hourOfDay, minute) -> {
                final LocalTime time = LocalTime.of(hourOfDay, minute);
                if (dialogId == DIALOG_START_TIME) {
                    mAutomaticThemeTimeController.setCustomStartTime(time);
                } else {
                    mAutomaticThemeTimeController.setCustomEndTime(time);
                }
                updateSummary();
            }, initialTime.getHour(), initialTime.getMinute(), use24HourFormat);
        }
        return super.onCreateDialog(dialogId);
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }
}