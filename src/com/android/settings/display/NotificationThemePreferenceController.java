/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.content.ContentResolver;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.drawer.SettingsDrawerActivity;

import com.android.settings.R;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.ServiceManager;

public class NotificationThemePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String NOTIFICATION_THEME = "notification_theme";
    private ListPreference mNotificationTheme;

    public NotificationThemePreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return NOTIFICATION_THEME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mNotificationTheme = (ListPreference) screen.findPreference(NOTIFICATION_THEME);
        int notificationTheme = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_THEME, 0);
        int valueIndex = mNotificationTheme.findIndexOfValue(String.valueOf(notificationTheme));
        mNotificationTheme.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mNotificationTheme.setSummary(mNotificationTheme.getEntry());
        mNotificationTheme.setOnPreferenceChangeListener(this);
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNotificationTheme) {
            String value = (String) newValue;
            Settings.System.putInt(mContext.getContentResolver(), Settings.System.NOTIFICATION_THEME, Integer.valueOf(value));
            int valueIndex = mNotificationTheme.findIndexOfValue(value);
            mNotificationTheme.setSummary(mNotificationTheme.getEntries()[valueIndex]);
            try {
                reload();
            }catch (Exception ignored){
            }
        }
        return true;
    }
    private void reload(){
        if (!isUsingDarkTheme()){
            return;
        }
        Intent intent2 = new Intent(Intent.ACTION_MAIN);
        intent2.addCategory(Intent.CATEGORY_HOME);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent2);
        Toast.makeText(mContext, R.string.applying_theme_toast, Toast.LENGTH_SHORT).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
              @Override
              public void run() {
                  Intent intent = new Intent(Intent.ACTION_MAIN);
                  intent.setClassName("com.android.settings",
                        "com.android.settings.Settings$DisplaySettingsActivity");
                  intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                  intent.putExtra(SettingsDrawerActivity.EXTRA_SHOW_MENU, true);
                  mContext.startActivity(intent);
                  Toast.makeText(mContext, R.string.theme_applied_toast, Toast.LENGTH_SHORT).show();
              }
        }, 3000);
    }
    
    // Check for the dark system theme
    private boolean isUsingDarkTheme() {
        IOverlayManager om = IOverlayManager.Stub.asInterface(ServiceManager.getService(mContext.OVERLAY_SERVICE));
        boolean isUsing = false;
        OverlayInfo themeInfo = null;
        try {
            themeInfo = om.getOverlayInfo("com.android.system.theme.dark",
                    UserHandle.USER_CURRENT);
            isUsing = themeInfo != null && themeInfo.isEnabled();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            themeInfo = om.getOverlayInfo("com.android.system.theme.dark.notifications",
                    UserHandle.USER_CURRENT);
            isUsing = isUsing || (themeInfo != null && themeInfo.isEnabled());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return isUsing;
    }
}
