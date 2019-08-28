/*
 * Copyright (C) 2018 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.display;

import static android.provider.Settings.Secure.THEME_MODE;
import static android.provider.Settings.Secure.THEME_MODE_LIGHT;
import static android.provider.Settings.Secure.THEME_MODE_TIME;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.FeatureFlagUtils;

import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

/**
 * Setting where user can pick if SystemUI will be light, dark or try to match
 * the wallpaper colors.
 */
public class SystemUiThemePreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private ListPreference mSystemUiThemePref;
    private ListPreference mDarkThemePref;
    private Preference mThemeTimePref;
    private String KEY_DARK_THEME_STYLE = "dark_theme_style";
    private String KEY_THEME_TIME_SETTINGS = "theme_time_settings";

    public SystemUiThemePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        boolean enabled = FeatureFlagUtils.isEnabled(mContext, "settings_systemui_theme");
        return enabled ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mSystemUiThemePref = (ListPreference) screen.findPreference(getPreferenceKey());
        mDarkThemePref = (ListPreference) screen.findPreference(KEY_DARK_THEME_STYLE);
        mThemeTimePref = (Preference) screen.findPreference(KEY_THEME_TIME_SETTINGS);
        int value = Settings.Secure.getInt(mContext.getContentResolver(), THEME_MODE, 0);
        mSystemUiThemePref.setValue(Integer.toString(value));
        mDarkThemePref.setEnabled(value != THEME_MODE_LIGHT);
        mThemeTimePref.setEnabled(value == THEME_MODE_TIME);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        Settings.Secure.putInt(mContext.getContentResolver(), THEME_MODE, value);
        refreshSummary(preference);
        mDarkThemePref.setEnabled(value != THEME_MODE_LIGHT);
        mThemeTimePref.setEnabled(value == THEME_MODE_TIME);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        int value = Settings.Secure.getInt(mContext.getContentResolver(), THEME_MODE, 0);
        int index = mSystemUiThemePref.findIndexOfValue(Integer.toString(value));
        return mSystemUiThemePref.getEntries()[index];
    }
}
