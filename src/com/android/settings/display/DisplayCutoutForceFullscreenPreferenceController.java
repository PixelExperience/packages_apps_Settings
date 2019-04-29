/*
 * Copyright (C) 2019 The PixelExperience Project
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

import android.content.Context;
import android.provider.Settings;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class DisplayCutoutForceFullscreenPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin {

    private static final String PREF_KEY = "display_cutout_force_fullscreen_settings";

    public DisplayCutoutForceFullscreenPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_physicalDisplayCutout);
    }

    private boolean isCutoutShowing(){
        return Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.DISPLAY_CUTOUT_HIDDEN, 0,
                UserHandle.USER_CURRENT) == 0;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(PREF_KEY);
        if (pref != null) {
            pref.setEnabled(isCutoutShowing());
        }
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }
}