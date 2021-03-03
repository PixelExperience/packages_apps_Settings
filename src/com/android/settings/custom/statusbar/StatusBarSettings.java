/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.custom.statusbar;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.TextUtils;
import android.util.ArraySet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.custom.preference.SecureSettingListPreference;
import com.android.settings.custom.preference.SystemSettingListPreference;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.util.custom.cutout.CutoutUtils;

import java.util.Set;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String CATEGORY_BATTERY = "status_bar_battery_key";
    private static final String CATEGORY_CLOCK = "status_bar_clock_key";
    private static final String CATEGORY_BRIGHTNESS = "status_bar_brightness_category";

    private static final String ICON_BLACKLIST = "icon_blacklist";

    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock";
    private static final String STATUS_BAR_AM_PM = "status_bar_am_pm";
    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";
    private static final String STATUS_BAR_QUICK_QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String STATUS_BAR_QUICK_QS_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";

    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private static final int STATUS_BAR_QS_BRIGHTNESS_NEVER_SHOW = 0;

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private SystemSettingListPreference mQuickPulldown;
    private SystemSettingListPreference mStatusBarClock;
    private SystemSettingListPreference mStatusBarAmPm;
    private SystemSettingListPreference mStatusBarBattery;
    private SystemSettingListPreference mStatusBarBatteryShowPercent;

    private SecureSettingListPreference mStatusBarQsShowBrightnessSlider;
    private SwitchPreference mStatusBarQsShowAutoBrightness;

    private PreferenceCategory mStatusBarBatteryCategory;
    private PreferenceCategory mStatusBarClockCategory;
    private PreferenceCategory mStatusBarBrightnessCategory;

    private static boolean sHasCenteredNotch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);

        sHasCenteredNotch = CutoutUtils.hasCenteredCutout(getActivity());

        mStatusBarAmPm = findPreference(STATUS_BAR_AM_PM);
        mStatusBarClock = findPreference(STATUS_BAR_CLOCK_STYLE);
        mStatusBarClock.setOnPreferenceChangeListener(this);

        mStatusBarClockCategory = getPreferenceScreen().findPreference(CATEGORY_CLOCK);

        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        mStatusBarBattery = findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBattery.setOnPreferenceChangeListener(this);
        enableStatusBarBatteryDependents(mStatusBarBattery.getIntValue(0));

        mStatusBarBatteryCategory = getPreferenceScreen().findPreference(CATEGORY_BATTERY);

        mQuickPulldown = findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        mStatusBarBrightnessCategory = getPreferenceScreen().findPreference(CATEGORY_BRIGHTNESS);
        mStatusBarQsShowBrightnessSlider = mStatusBarBrightnessCategory.findPreference(STATUS_BAR_QUICK_QS_SHOW_BRIGHTNESS_SLIDER);
        mStatusBarQsShowBrightnessSlider.setOnPreferenceChangeListener(this);
        mStatusBarQsShowAutoBrightness = mStatusBarBrightnessCategory.findPreference(STATUS_BAR_QUICK_QS_SHOW_AUTO_BRIGHTNESS);
        if (!getResources().getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available)){
            mStatusBarBrightnessCategory.removePreference(mStatusBarQsShowAutoBrightness);
            mStatusBarQsShowAutoBrightness = null;
        }
        enableStatusBarQsBrightnessDependents(mStatusBarQsShowBrightnessSlider.getIntValue(1));
    }

    @Override
    public void onResume() {
        super.onResume();

        final String curIconBlacklist = Settings.Secure.getString(getContext().getContentResolver(),
                ICON_BLACKLIST);

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "clock")) {
            getPreferenceScreen().removePreference(mStatusBarClockCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarClockCategory);
        }

        if (TextUtils.delimitedStringContains(curIconBlacklist, ',', "battery")) {
            getPreferenceScreen().removePreference(mStatusBarBatteryCategory);
        } else {
            getPreferenceScreen().addPreference(mStatusBarBatteryCategory);
        }

        if (DateFormat.is24HourFormat(getActivity())) {
            mStatusBarAmPm.setEnabled(false);
            mStatusBarAmPm.setSummary(R.string.status_bar_am_pm_info);
        }

        final boolean disallowCenteredClock = sHasCenteredNotch;

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (disallowCenteredClock) {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
            } else {
                mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_rtl);
                mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
            }
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_qs_pulldown_values);
        } else if (disallowCenteredClock) {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries_notch);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values_notch);
        } else {
            mStatusBarClock.setEntries(R.array.status_bar_clock_position_entries);
            mStatusBarClock.setEntryValues(R.array.status_bar_clock_position_values);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        String key = preference.getKey();
        switch (key) {
            case STATUS_BAR_QUICK_QS_PULLDOWN:
                updateQuickPulldownSummary(value);
                break;
            case STATUS_BAR_BATTERY_STYLE:
                enableStatusBarBatteryDependents(value);
                break;
            case STATUS_BAR_QUICK_QS_SHOW_BRIGHTNESS_SLIDER:
                enableStatusBarQsBrightnessDependents(value);
                break;
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);
    }

    private void enableStatusBarQsBrightnessDependents(int qsBrightnessType) {
        if (mStatusBarQsShowAutoBrightness != null){
            mStatusBarQsShowAutoBrightness.setEnabled(qsBrightnessType != STATUS_BAR_QS_BRIGHTNESS_NEVER_SHOW);
        }
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL){
            if (value == PULLDOWN_DIR_LEFT) {
                value = PULLDOWN_DIR_RIGHT;
            }else if (value == PULLDOWN_DIR_RIGHT) {
                value = PULLDOWN_DIR_LEFT;
            }
        }
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;
            case PULLDOWN_DIR_LEFT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary_left_edge);
                break;
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary_right_edge);
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }
}