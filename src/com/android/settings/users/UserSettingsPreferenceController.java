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
 * limitations under the License.
 */
package com.android.settings.users;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.UserManager;
import android.os.UserHandle;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.Utils;

public class UserSettingsPreferenceController extends BasePreferenceController {

    private final UserManager mUm;

    public UserSettingsPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mUm = (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        if (getAvailabilityStatus() == AVAILABLE) {
            final Preference preference = screen.findPreference(getPreferenceKey());
            if (preference != null) {
                preference.setTitle(UserManager.supportsMultipleUsers() ?
                        R.string.user_settings_title : R.string.profile_info_settings_title);
                UserInfo info = mUm.getUserInfo(UserHandle.myUserId());
                preference.setSummary(mContext.getString(R.string.users_summary, info.name));
            }
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return !Utils.isMonkeyRunning()
                ? AVAILABLE
                : UNSUPPORTED_ON_DEVICE;
    }
}
