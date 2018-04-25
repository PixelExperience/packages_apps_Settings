/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecurityPatchPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_SECURITY_PATCH = "security_patch";
    private static final String TAG = "SecurityPatchPref";

    private static String mPatch;
    /* Returns the security patch level via prop override */
    private static String mPatchOverride;
    private final PackageManager mPackageManager;

    public SecurityPatchPreferenceController(Context context) {
        super(context);
        mPackageManager = mContext.getPackageManager();
        mPatch = DeviceInfoUtils.getSecurityPatch();
        mPatchOverride = SystemProperties.get("ro.vendor.override.security_patch", "");
    }

    @Override
    public boolean isAvailable() {
        return !TextUtils.isEmpty(mPatch) ||
              !TextUtils.isEmpty(mPatchOverride);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SECURITY_PATCH;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final Preference pref = screen.findPreference(KEY_SECURITY_PATCH);
        if (pref != null) {
            pref.setSummary(getSecurityPatch());
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), KEY_SECURITY_PATCH)) {
            return false;
        }
        if (mPackageManager.queryIntentActivities(preference.getIntent(), 0).isEmpty()) {
            // Don't send out the intent to stop crash
            Log.w(TAG, "Stop click action on " + KEY_SECURITY_PATCH + ": "
                    + "queryIntentActivities() returns empty");
            return true;
        }
        return false;
    }

    public static String getSecurityPatch() {
        if (!"".equals(mPatchOverride)) {
            try {
                SimpleDateFormat template = new SimpleDateFormat("yyyy-MM-dd");
                Date patchDate = template.parse(mPatchOverride);
                String format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "dMMMMyyyy");
                mPatchOverride = DateFormat.format(format, patchDate).toString();
            } catch (ParseException e) {}
            return mPatchOverride;
        } else {
            return mPatch;
        }
    }
}
