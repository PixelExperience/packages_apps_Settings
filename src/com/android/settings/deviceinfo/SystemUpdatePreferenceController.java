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
package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserManager;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class SystemUpdatePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String TAG = "SysUpdatePrefContr";

    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";

    private final UserManager mUm;

    private static final String OTA_BUILD_TYPE_PROP = "org.pixelexperience.build_type";
    private static final String OTA_APP_PACKAGE = "org.pixelexperience.ota";

    public SystemUpdatePreferenceController(Context context, UserManager um) {
        super(context);
        mUm = um;
    }

    @Override
    public boolean isAvailable() {
        String buildtype = SystemProperties.get(OTA_BUILD_TYPE_PROP,"unofficial");
        if (!mUm.isAdminUser() || !buildtype.equalsIgnoreCase("official")){
            return false;
        }
        try {
            PackageManager pm = mContext.getPackageManager();
            pm.getPackageInfo(OTA_APP_PACKAGE, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SYSTEM_UPDATE_SETTINGS;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        if (isAvailable()) {
            Utils.updatePreferenceToSpecificActivityOrRemove(mContext, screen,
                    KEY_SYSTEM_UPDATE_SETTINGS,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            removePreference(screen, KEY_SYSTEM_UPDATE_SETTINGS);
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_SYSTEM_UPDATE_SETTINGS.equals(preference.getKey())) {
            // TODO: Launch Pixel Experience OTA
        }
        // always return false here because this handler does not want to block other handlers.
        return false;
    }

    @Override
    public void updateState(Preference preference) {
        preference.setSummary(mContext.getString(R.string.about_summary,
                Build.VERSION.RELEASE));
    }
}
