/*
 * Copyright (C) 2016 The CyanogenMod project
 * Copyright (C) 2017-2018 The LineageOS project
 * Copyright (C) 2018 The PixelExperience Project
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

import android.app.ActivityManager;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.custom.buttons.preference.*;

import static com.android.internal.util.custom.hwkeys.DeviceKeysConstants.*;

import com.android.internal.util.custom.NavbarUtils;

import java.util.List;
import java.util.UUID;

import com.android.settings.custom.preference.CustomDialogPreference;

public class ButtonSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ButtonSettings";

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
    private static final String DISABLE_NAV_KEYS = "disable_nav_keys";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_CAMERA = "camera_key";
    private static final String CATEGORY_BACKLIGHT = "key_backlight";

    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private SwitchPreference mCameraWakeScreen;
    private SwitchPreference mCameraSleepOnRelease;
    private SwitchPreference mCameraLaunch;
    private SwitchPreference mDisableNavigationKeys;

    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);

        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;

        final boolean showHomeWake = (deviceWakeKeys & KEY_MASK_HOME) != 0;
        final boolean showBackWake = (deviceWakeKeys & KEY_MASK_BACK) != 0;
        final boolean showMenuWake = (deviceWakeKeys & KEY_MASK_MENU) != 0;
        final boolean showAssistWake = (deviceWakeKeys & KEY_MASK_ASSIST) != 0;
        final boolean showAppSwitchWake = (deviceWakeKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean showCameraWake = (deviceWakeKeys & KEY_MASK_CAMERA) != 0;

        boolean hasAnyBindableKey = false;
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
        final PreferenceCategory cameraCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_CAMERA);

        mHandler = new Handler();

        // Force Navigation bar related options
        mDisableNavigationKeys = (SwitchPreference) findPreference(DISABLE_NAV_KEYS);

        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnHomeBehaviorHwkeys));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnHomeBehaviorHwkeys));
        Action defaultAppSwitchPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_pressOnAppSwitchBehaviorHwkeys));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnAppSwitchBehaviorHwkeys));
        Action defaultAssistPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_pressOnAssistBehaviorHwkeys));
        Action defaultAssistLongPressAction = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnAssistBehaviorHwkeys));
        Action homeLongPressAction = Action.fromSettings(resolver,
                Settings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(resolver,
                Settings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(resolver,
                Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);

        // Only visible on devices that does not have a navigation bar already
        if (NavbarUtils.canDisable(getActivity())) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
        } else {
            prefScreen.removePreference(mDisableNavigationKeys);
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN));
            }

            mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, homeLongPressAction);
            mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, homeDoubleTapAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(homeCategory);
        }

        if (hasBackKey) {
            if (!showBackWake) {
                backCategory.removePreference(findPreference(Settings.System.BACK_WAKE_SCREEN));
                prefScreen.removePreference(backCategory);
            }
        } else {
            prefScreen.removePreference(backCategory);
        }

        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory.removePreference(findPreference(Settings.System.MENU_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_MENU_ACTION, Action.MENU);
            mMenuPressAction = initList(KEY_MENU_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                        Settings.System.KEY_MENU_LONG_PRESS_ACTION,
                        hasAssistKey ? Action.NOTHING : Action.APP_SWITCH);
            mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(menuCategory);
        }

        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory.removePreference(findPreference(Settings.System.ASSIST_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_ASSIST_ACTION, defaultAssistPressAction);
            mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION, defaultAssistLongPressAction);
            mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(assistCategory);
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory.removePreference(findPreference(
                        Settings.System.APP_SWITCH_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(resolver,
                    Settings.System.KEY_APP_SWITCH_ACTION, defaultAppSwitchPressAction);
            mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction);

            mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS, appSwitchLongPressAction);

            hasAnyBindableKey = true;
        } else {
            prefScreen.removePreference(appSwitchCategory);
        }

        if (hasCameraKey) {
            mCameraWakeScreen = (SwitchPreference) findPreference(Settings.System.CAMERA_WAKE_SCREEN);
            mCameraSleepOnRelease =
                    (SwitchPreference) findPreference(Settings.System.CAMERA_SLEEP_ON_RELEASE);
            mCameraLaunch = (SwitchPreference) findPreference(Settings.System.CAMERA_LAUNCH);

            if (!showCameraWake) {
                prefScreen.removePreference(mCameraWakeScreen);
            }
            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(com.android.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease);
            }
        } else {
            prefScreen.removePreference(cameraCategory);
        }

        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) findPreference(KEY_BUTTON_BACKLIGHT);
        if (!backlight.isButtonSupported()) {
            prefScreen.removePreference(backlight);
        }

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !res.getBoolean(
                    com.android.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(Settings.System.CAMERA_WAKE_SCREEN);
            }
        }

        // Override key actions on Go devices in order to hide any unsupported features
        if (ActivityManager.isLowRamDeviceStatic()) {
            String[] actionEntriesGo = res.getStringArray(R.array.hardware_keys_action_entries_go);
            String[] actionValuesGo = res.getStringArray(R.array.hardware_keys_action_values_go);

            if (hasHomeKey) {
                mHomeLongPressAction.setEntries(actionEntriesGo);
                mHomeLongPressAction.setEntryValues(actionValuesGo);

                mHomeDoubleTapAction.setEntries(actionEntriesGo);
                mHomeDoubleTapAction.setEntryValues(actionValuesGo);
            }

            if (hasMenuKey) {
                mMenuPressAction.setEntries(actionEntriesGo);
                mMenuPressAction.setEntryValues(actionValuesGo);

                mMenuLongPressAction.setEntries(actionEntriesGo);
                mMenuLongPressAction.setEntryValues(actionValuesGo);
            }

            if (hasAssistKey) {
                mAssistPressAction.setEntries(actionEntriesGo);
                mAssistPressAction.setEntryValues(actionValuesGo);

                mAssistLongPressAction.setEntries(actionEntriesGo);
                mAssistLongPressAction.setEntryValues(actionValuesGo);
            }

            if (hasAppSwitchKey) {
                mAppSwitchPressAction.setEntries(actionEntriesGo);
                mAppSwitchPressAction.setEntryValues(actionValuesGo);

                mAppSwitchLongPressAction.setEntries(actionEntriesGo);
                mAppSwitchLongPressAction.setEntryValues(actionValuesGo);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleListChange(mMenuPressAction, newValue,
                    Settings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleListChange(mMenuLongPressAction, newValue,
                    Settings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleListChange(mAssistPressAction, newValue,
                    Settings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleListChange(mAssistLongPressAction, newValue,
                    Settings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleListChange(mAppSwitchPressAction, newValue,
                    Settings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        }
        return false;
    }

    private void writeDisableNavkeysOption(boolean enabled) {
        NavbarUtils.setEnabled(getActivity(), enabled);
    }

    private void updateDisableNavkeysOption() {
        mDisableNavigationKeys.setChecked(NavbarUtils.isEnabled(getActivity()));
    }

    private void updateDisableNavkeysCategories(boolean navbarEnabled) {
        final PreferenceScreen prefScreen = getPreferenceScreen();

        /* Disable hw-key options if they're disabled */
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);
        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) prefScreen.findPreference(KEY_BUTTON_BACKLIGHT);

        /* Toggle backlight control depending on navbar state, force it to
           off if enabling */
        if (backlight != null) {
            backlight.setEnabled(!navbarEnabled);
            backlight.updateSummary();
        }

        if (homeCategory != null) {
            homeCategory.setEnabled(!navbarEnabled);
        }
        if (backCategory != null) {
            backCategory.setEnabled(!navbarEnabled);
        }
        if (menuCategory != null) {
            menuCategory.setEnabled(!navbarEnabled);
        }
        if (assistCategory != null) {
            assistCategory.setEnabled(!navbarEnabled);
        }
        if (appSwitchCategory != null) {
            appSwitchCategory.setEnabled(!navbarEnabled);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            writeDisableNavkeysOption(mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(true);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mDisableNavigationKeys.setEnabled(true);
                        updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
                    }catch(Exception e){
                    }
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference.getKey() == null) {
            // Auto-key preferences that don't have a key, so the dialog can find them.
            preference.setKey(UUID.randomUUID().toString());
        }
        DialogFragment f = null;
        if (preference instanceof CustomDialogPreference) {
            f = CustomDialogPreference.CustomPreferenceDialogFragment
                    .newInstance(preference.getKey());
        } else {
            super.onDisplayPreferenceDialog(preference);
            return;
        }
        f.setTargetFragment(this, 0);
        f.show(getFragmentManager(), "dialog_preference");
        onDialogShowing();
    }
}
