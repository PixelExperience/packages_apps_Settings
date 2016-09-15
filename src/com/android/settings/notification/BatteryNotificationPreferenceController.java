/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

import static android.provider.Settings.System.BATTERY_LIGHT_ENABLED;

public class BatteryNotificationPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener,
        LifecycleObserver, OnResume, OnPause {

    private static final String KEY_BATTERY_NOTIFICATION = "battery_light_enabled";
    private SettingObserver mSettingObserver;

    public BatteryNotificationPreferenceController(Context context) {
        super(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(KEY_BATTERY_NOTIFICATION);
        if (preference != null) {
            mSettingObserver = new SettingObserver(preference);
        }
    }

    @Override
    public void onResume() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver(), true /* register */);
        }
    }

    @Override
    public void onPause() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver(), false /* register */);
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BATTERY_NOTIFICATION;
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources()
                .getBoolean(com.android.internal.R.bool.config_intrusiveBatteryLed);
    }

    @Override
    public void updateState(Preference preference) {
        try {
            final boolean checked = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.BATTERY_LIGHT_ENABLED, mContext.getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveBatteryLed) ? 1 : 0) == 1;
            ((TwoStatePreference) preference).setChecked(checked);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final boolean val = (Boolean) newValue;
        return Settings.System.putInt(mContext.getContentResolver(),
                BATTERY_LIGHT_ENABLED, val ? 1 : 0);
    }

    class SettingObserver extends ContentObserver {

        private final Uri BATTERY_LIGHT_ENABLED_URI =
                Settings.System.getUriFor(Settings.System.BATTERY_LIGHT_ENABLED);

        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(BATTERY_LIGHT_ENABLED_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (BATTERY_LIGHT_ENABLED_URI.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
