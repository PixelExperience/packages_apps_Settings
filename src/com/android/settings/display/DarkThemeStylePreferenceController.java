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

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settingslib.core.AbstractPreferenceController;

import com.android.settings.R;

/**
 * Setting where user can pick if SystemUI will be light, dark or try to match
 * the wallpaper colors.
 */
public class DarkThemeStylePreferenceController extends AbstractPreferenceController {

    private ListPreference mDarkThemeStylePref;
    private boolean mHasOledScreen;

    private static final String KEY_DARK_THEME_STYLE = "dark_theme_style";

    public DarkThemeStylePreferenceController(Context context) {
        super(context);
        mHasOledScreen = mContext.getResources().getBoolean(com.android.internal.R.bool.config_hasOledDisplay);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_DARK_THEME_STYLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mDarkThemeStylePref = (ListPreference) screen.findPreference(getPreferenceKey());
        int value = Settings.System.getIntForUser(mContext.getContentResolver(),
                        Settings.System.THEME_DARK_STYLE, -1, UserHandle.USER_CURRENT);
        if (value == -1){
            value = mHasOledScreen ? 1 : 0;
        }
        mDarkThemeStylePref.setValue(Integer.toString(value));
    }

}
