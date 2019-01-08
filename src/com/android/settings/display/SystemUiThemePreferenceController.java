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

import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.FeatureFlagUtils;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

import com.android.settings.fuelgauge.BatterySaverReceiver;

import com.android.settings.R;

/**
 * Setting where user can pick if SystemUI will be light, dark or try to match
 * the wallpaper colors.
 */
public class SystemUiThemePreferenceController extends AbstractPreferenceController
        implements Preference.OnPreferenceChangeListener, LifecycleObserver, OnStart, OnStop, BatterySaverReceiver.BatterySaverListener {

    private ListPreference mSystemUiThemePref;
    private final BatterySaverReceiver mBatteryStateChangeReceiver;
    private final PowerManager mPowerManager;

    private static final String KEY_SYSTEMUI_THEME = "systemui_theme";

    public SystemUiThemePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mBatteryStateChangeReceiver = new BatterySaverReceiver(mContext);
        mBatteryStateChangeReceiver.setBatterySaverListener(this);
    }

    @Override
    public boolean isAvailable() {
        return FeatureFlagUtils.isEnabled(mContext, "settings_systemui_theme");
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SYSTEMUI_THEME;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mSystemUiThemePref = (ListPreference) screen.findPreference(getPreferenceKey());
        int value = Settings.Secure.getInt(mContext.getContentResolver(), THEME_MODE, 0);
        mSystemUiThemePref.setValue(Integer.toString(value));
        updateState();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        Settings.Secure.putInt(mContext.getContentResolver(), THEME_MODE, value);
        refreshSummary(preference);
        return true;
    }

    @Override
    public CharSequence getSummary() {
        if (mPowerManager.isPowerSaveMode()){
            return mContext.getString(R.string.systemui_theme_dark) + " (" + mContext.getString(R.string.battery_tip_early_heads_up_done_title) + ")";
        }else{
            int value = Settings.Secure.getInt(mContext.getContentResolver(), THEME_MODE, 0);
            int index = mSystemUiThemePref.findIndexOfValue(Integer.toString(value));
            return mSystemUiThemePref.getEntries()[index];
        }
    }

    private void updateSummary() {
        if (mSystemUiThemePref != null){
            mSystemUiThemePref.setSummary(getSummary());
        }
    }

    private void updateState() {
        if (mSystemUiThemePref != null){
            mSystemUiThemePref.setEnabled(!mPowerManager.isPowerSaveMode());
        }
    }

    @Override
    public void onStart() {
        mBatteryStateChangeReceiver.setListening(true);
    }

    @Override
    public void onStop() {
        mBatteryStateChangeReceiver.setListening(false);
    }

    @Override
    public void onPowerSaveModeChanged() {
        updateState();
        updateSummary();
    }

    @Override
    public void onBatteryChanged(boolean pluggedIn) {
    }
}
