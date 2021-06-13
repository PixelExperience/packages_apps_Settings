/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017-2021 The LineageOS Project
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

package com.android.settings.custom.buttons;

import static com.android.internal.util.custom.globalactions.PowerMenuConstants.*;

import android.content.Context;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import com.android.settings.R;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.util.custom.globalactions.CustomGlobalActions;
import com.android.internal.util.custom.globalactions.PowerMenuConstants;
import com.android.internal.widget.LockPatternUtils;

import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

public class PowerMenuActions extends SettingsPreferenceFragment {
    final static String TAG = "PowerMenuActions";

    private CheckBoxPreference mScreenshotPref;
    private CheckBoxPreference mAirplanePref;
    private CheckBoxPreference mUsersPref;
    private CheckBoxPreference mLockDownPref;
    private CheckBoxPreference mEmergencyPref;

    private CustomGlobalActions mCustomGlobalActions;

    Context mContext;
    private LockPatternUtils mLockPatternUtils;
    private UserManager mUserManager;
    private List<String> mLocalUserConfig = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu_shortcuts);
        mContext = getActivity().getApplicationContext();
        mCustomGlobalActions = CustomGlobalActions.getInstance(mContext);
        mLockPatternUtils = new LockPatternUtils(mContext);
        mUserManager = UserManager.get(mContext);

        for (String action : PowerMenuConstants.getAllActions()) {
            if (action.equals(GLOBAL_ACTION_KEY_SCREENSHOT)) {
                mScreenshotPref = findPreference(GLOBAL_ACTION_KEY_SCREENSHOT);
            } else if (action.equals(GLOBAL_ACTION_KEY_AIRPLANE)) {
                mAirplanePref = findPreference(GLOBAL_ACTION_KEY_AIRPLANE);
            } else if (action.equals(GLOBAL_ACTION_KEY_USERS)) {
                mUsersPref = findPreference(GLOBAL_ACTION_KEY_USERS);
            } else if (action.equals(GLOBAL_ACTION_KEY_LOCKDOWN)) {
                mLockDownPref = findPreference(GLOBAL_ACTION_KEY_LOCKDOWN);
            } else if (action.equals(GLOBAL_ACTION_KEY_EMERGENCY)) {
                mEmergencyPref = findPreference(GLOBAL_ACTION_KEY_EMERGENCY);
            }
        }

        mLocalUserConfig = mCustomGlobalActions.getLocalUserConfig();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mScreenshotPref != null) {
            mScreenshotPref.setChecked(mCustomGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_SCREENSHOT));
        }

        if (mAirplanePref != null) {
            mAirplanePref.setChecked(mCustomGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_AIRPLANE));
        }

        if (mLockDownPref != null) {
            mLockDownPref.setChecked(mCustomGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_LOCKDOWN));
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                getPreferenceScreen().removePreference(findPreference(GLOBAL_ACTION_KEY_USERS));
                mUsersPref = null;
            } else {
                List<UserInfo> users = mUserManager.getUsers();
                boolean enabled = (users.size() > 1);
                mUsersPref.setChecked(mCustomGlobalActions.userConfigContains(
                        GLOBAL_ACTION_KEY_USERS) && enabled);
                mUsersPref.setEnabled(enabled);
            }
        }

        if (mEmergencyPref != null) {
            mEmergencyPref.setChecked(mCustomGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_EMERGENCY));
        }

        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean value;

        if (preference == mScreenshotPref) {
            value = mScreenshotPref.isChecked();
            mCustomGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);

        } else if (preference == mAirplanePref) {
            value = mAirplanePref.isChecked();
            mCustomGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);

        } else if (preference == mUsersPref) {
            value = mUsersPref.isChecked();
            mCustomGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);

        } else if (preference == mLockDownPref) {
            value = mLockDownPref.isChecked();
            mCustomGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_LOCKDOWN);
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.LOCKDOWN_IN_POWER_MENU, value ? 1 : 0, UserHandle.USER_CURRENT);

        } else if (preference == mEmergencyPref) {
            value = mEmergencyPref.isChecked();
            mCustomGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_EMERGENCY);

        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    private void updatePreferences() {
        boolean isKeyguardSecure = mLockPatternUtils.isSecure(UserHandle.myUserId());
        boolean lockdown = Settings.Secure.getIntForUser(
                getContentResolver(), Settings.Secure.LOCKDOWN_IN_POWER_MENU, 0,
                UserHandle.USER_CURRENT) == 1;
        if (mLockDownPref != null) {
            mLockDownPref.setEnabled(isKeyguardSecure);
            if (isKeyguardSecure) {
                mLockDownPref.setChecked(lockdown);
                mLockDownPref.setSummary(null);
            } else {
                mLockDownPref.setChecked(false);
                mLockDownPref.setSummary(R.string.power_menu_shortcut_unavailable);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }
}
