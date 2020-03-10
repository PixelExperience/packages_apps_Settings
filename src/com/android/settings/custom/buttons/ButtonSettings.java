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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import androidx.fragment.app.DialogFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.custom.buttons.preference.*;
import com.android.settings.custom.utils.DeviceUtils;
import com.android.settings.custom.utils.TelephonyUtils;

import static com.android.internal.util.custom.hwkeys.DeviceKeysConstants.*;

import com.android.internal.util.custom.NavbarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.android.settings.custom.preference.CustomDialogPreference;
import com.android.settings.gestures.SystemNavigationPreferenceController;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;

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
    private static final String KEY_NAV_MENU_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAV_INVERSE = "navbar_inverse";
    private static final String KEY_NAV_GESTURES = "navbar_gestures";
    private static final String KEY_NAV_COMPACT_LAYOUT = "navigation_bar_compact_layout";
    private static final String KEY_ADDITIONAL_BUTTONS = "additional_buttons";
    private static final String KEY_TORCH_LONG_PRESS_POWER = "torch_long_press_power_gesture";
    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT = "torch_long_press_power_timeout";
    private static final String KEY_POWER_END_CALL = "power_end_call";
    private static final String KEY_HOME_ANSWER_CALL = "home_answer_call";
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
    private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";
    private static final String KEY_EDGE_LONG_SWIPE = "navigation_bar_edge_long_swipe";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_CAMERA = "camera_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_BACKLIGHT = "button_backlight_cat";
    private static final String CATEGORY_NAVBAR = "navbar_key";
    private static final String CATEGORY_POWER = "power_key";
    private static final String CATEGORY_OTHERS = "others_category";

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
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;
    private ListPreference mEdgeLongSwipeAction;
    private SwitchPreference mNavigationMenuArrowKeys;
    private SwitchPreference mNavigationInverse;
    private Preference mNavigationGestures;
    private SwitchPreference mNavigationCompactLayout;
    private Preference mAdditionalButtonsPreference;
    private SwitchPreference mTorchLongPressPower;
    private ListPreference mTorchLongPressPowerTimeout;
    private SwitchPreference mPowerEndCall;
    private SwitchPreference mHomeAnswerCall;
    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreference mVolumeWakeScreen;
    private SwitchPreference mVolumeMusicControls;
    private SwitchPreference mSwapVolumeButtons;
    private PreferenceCategory mNavbarCategory;

    private Handler mHandler;
    
    private boolean mSupportLongPressPowerWhenNonInteractive;
    private boolean mAdditionalButtonsAvailable;

    private static List<String> sNonIndexableKeys = new ArrayList<>();

    private IOverlayManager mOverlayManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.button_settings);

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        final Resources res = getResources();
        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        final int deviceKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = res.getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);

        final boolean hasPowerKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);
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
        final boolean showVolumeWake = (deviceWakeKeys & KEY_MASK_VOLUME) != 0;

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
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);
        final PreferenceCategory cameraCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_CAMERA);
        final PreferenceCategory backlightCat =
                (PreferenceCategory) findPreference(CATEGORY_BACKLIGHT);
        final PreferenceCategory powerCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_POWER);
        final PreferenceCategory othersCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_OTHERS);

        mNavbarCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_NAVBAR);

        // Home button answers calls.
        mHomeAnswerCall = (SwitchPreference) findPreference(KEY_HOME_ANSWER_CALL);

        mHandler = new Handler();

        // Force Navigation bar related options
        mDisableNavigationKeys = (SwitchPreference) findPreference(DISABLE_NAV_KEYS);
        mNavigationInverse = (SwitchPreference) findPreference(KEY_NAV_INVERSE);
        mNavigationMenuArrowKeys = (SwitchPreference) findPreference(KEY_NAV_MENU_ARROW_KEYS);
        mNavigationInverse.setOnPreferenceChangeListener(this);
        mNavigationGestures = (Preference) findPreference(KEY_NAV_GESTURES);
        mNavigationCompactLayout = (SwitchPreference) findPreference(KEY_NAV_COMPACT_LAYOUT);

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
        Action edgeLongSwipeAction = Action.fromSettings(resolver,
                Settings.System.KEY_EDGE_LONG_SWIPE_ACTION,
                Action.NOTHING);

        // Navigation bar home long press
        Action defaultHomeLongPressActionNavbar = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnHomeBehavior));
        Action homeLongPressActionNavbar = Action.fromSettings(resolver,
                Settings.System.KEY_HOME_LONG_PRESS_ACTION_NAVBAR,
                defaultHomeLongPressActionNavbar);
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressActionNavbar);

        // Navigation bar home double tap
        Action defaultHomeDoubleTapActionNavbar = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action homeDoubleTapActionNavbar = Action.fromSettings(resolver,
                Settings.System.KEY_HOME_DOUBLE_TAP_ACTION_NAVBAR,
                defaultHomeDoubleTapActionNavbar);
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapActionNavbar);

        // Navigation bar app switch long press
        Action defaultAppSwitchLongPressActionNavbar = Action.fromIntSafe(res.getInteger(
                com.android.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action appSwitchLongPressActionNavbar = Action.fromSettings(resolver,
                Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION_NAVBAR,
                defaultAppSwitchLongPressActionNavbar);
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressActionNavbar);

        // Edge long swipe gesture
        mEdgeLongSwipeAction = initList(KEY_EDGE_LONG_SWIPE, edgeLongSwipeAction);

        // Only visible on devices that does not have a navigation bar already
        if (NavbarUtils.canDisable(getActivity())) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked());
            mNavigationMenuArrowKeys.setDependency(DISABLE_NAV_KEYS);
            mNavigationInverse.setDependency(DISABLE_NAV_KEYS);
            mNavigationHomeLongPressAction.setDependency(DISABLE_NAV_KEYS);
            mNavigationHomeDoubleTapAction.setDependency(DISABLE_NAV_KEYS);
            mNavigationAppSwitchLongPressAction.setDependency(DISABLE_NAV_KEYS);
            mEdgeLongSwipeAction.setDependency(DISABLE_NAV_KEYS);
            mNavigationGestures.setDependency(DISABLE_NAV_KEYS);
            mNavigationCompactLayout.setDependency(DISABLE_NAV_KEYS);
        } else {
            mNavbarCategory.removePreference(mDisableNavigationKeys);
            mDisableNavigationKeys = null;
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN));
            }

            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                homeCategory.removePreference(mHomeAnswerCall);
                mHomeAnswerCall = null;
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
            prefScreen.removePreference(backlightCat);
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

            mNavigationHomeLongPressAction.setEntries(actionEntriesGo);
            mNavigationHomeLongPressAction.setEntryValues(actionValuesGo);

            mNavigationHomeDoubleTapAction.setEntries(actionEntriesGo);
            mNavigationHomeDoubleTapAction.setEntryValues(actionValuesGo);

            mNavigationAppSwitchLongPressAction.setEntries(actionEntriesGo);
            mNavigationAppSwitchLongPressAction.setEntryValues(actionValuesGo);

            mEdgeLongSwipeAction.setEntries(actionEntriesGo);
            mEdgeLongSwipeAction.setEntryValues(actionValuesGo);
        }
        mAdditionalButtonsAvailable = !getResources().getString(R.string.config_customButtonsPackage).equals("");
        if (mAdditionalButtonsAvailable){
            mAdditionalButtonsPreference = (Preference) findPreference(KEY_ADDITIONAL_BUTTONS);
        }else{
            prefScreen.removePreference(othersCategory);
        }

        // Power button
        mSupportLongPressPowerWhenNonInteractive = getResources().getBoolean(
                com.android.internal.R.bool.config_supportLongPressPowerWhenNonInteractive);
        mPowerEndCall = (SwitchPreference) findPreference(KEY_POWER_END_CALL);
        mTorchLongPressPower = (SwitchPreference) findPreference(KEY_TORCH_LONG_PRESS_POWER);
        mTorchLongPressPowerTimeout = (ListPreference) findPreference(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT);

        if (hasPowerKey) {
            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                powerCategory.removePreference(mPowerEndCall);
                mPowerEndCall = null;
            }
            if (!mSupportLongPressPowerWhenNonInteractive ||
                    !DeviceUtils.deviceSupportsFlashLight(getActivity())) {
                powerCategory.removePreference(mTorchLongPressPower);
                powerCategory.removePreference(mTorchLongPressPowerTimeout);
            }
        } else {
            prefScreen.removePreference(powerCategory);
        }

        // Volume button
        mVolumeWakeScreen = (SwitchPreference) findPreference(Settings.System.VOLUME_WAKE_SCREEN);
        mVolumeMusicControls = (SwitchPreference) findPreference(KEY_VOLUME_MUSIC_CONTROLS);

        if (mVolumeWakeScreen != null) {
            if (mVolumeMusicControls != null) {
                mVolumeMusicControls.setDependency(Settings.System.VOLUME_WAKE_SCREEN);
                mVolumeWakeScreen.setDisableDependentsState(true);
            }
        }

        if (DeviceUtils.hasVolumeRocker(getActivity())) {
            if (!showVolumeWake) {
                volumeCategory.removePreference(findPreference(Settings.System.VOLUME_WAKE_SCREEN));
            }

            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                volumeCategory.removePreference(
                        findPreference(Settings.System.VOLUME_ANSWER_CALL));
            }

            int cursorControlAction = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);

            int swapVolumeKeys = Settings.System.getInt(getContentResolver(),
                    Settings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
            mSwapVolumeButtons = (SwitchPreference)
                    prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
            if (mSwapVolumeButtons != null) {
                mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
            }
        } else {
            prefScreen.removePreference(volumeCategory);
        }

        // Navigation bar modes
        updateNavigationBarModeState();
    }

    private void updateNavigationBarModeState(){
        int removedCount = 0;
        String mode = NavbarUtils.getNavigationBarModeOverlay(getActivity(), mOverlayManager);
        boolean isGesturalMode = !mode.equals(NAV_BAR_MODE_2BUTTON_OVERLAY) && !mode.equals(NAV_BAR_MODE_3BUTTON_OVERLAY);
        if (mode.equals(NAV_BAR_MODE_2BUTTON_OVERLAY)){
            if (mNavigationAppSwitchLongPressAction != null){
                mNavbarCategory.removePreference(mNavigationAppSwitchLongPressAction);
                mNavigationAppSwitchLongPressAction = null;
                removedCount++;
            }
        }else if (!mode.equals(NAV_BAR_MODE_3BUTTON_OVERLAY)){
            if (mNavigationMenuArrowKeys != null){
                mNavbarCategory.removePreference(mNavigationMenuArrowKeys);
                mNavigationMenuArrowKeys = null;
                removedCount++;
            }
            if (mNavigationInverse != null){
                mNavbarCategory.removePreference(mNavigationInverse);
                mNavigationInverse = null;
                removedCount++;
            }
            if (mNavigationHomeLongPressAction != null){
                mNavbarCategory.removePreference(mNavigationHomeLongPressAction);
                mNavigationHomeLongPressAction = null;
                removedCount++;
            }
            if (mNavigationHomeDoubleTapAction != null){
                mNavbarCategory.removePreference(mNavigationHomeDoubleTapAction);
                mNavigationHomeDoubleTapAction = null;
                removedCount++;
            }
            if (mNavigationAppSwitchLongPressAction != null){
                mNavbarCategory.removePreference(mNavigationAppSwitchLongPressAction);
                mNavigationAppSwitchLongPressAction = null;
                removedCount++;
            }
            if (mNavigationCompactLayout != null){
                mNavbarCategory.removePreference(mNavigationCompactLayout);
                mNavigationCompactLayout = null;
                removedCount++;
            }
        }
        if (mDisableNavigationKeys == null){
            removedCount++;
        }
        if (!isGesturalMode && mEdgeLongSwipeAction != null){
            mNavbarCategory.removePreference(mEdgeLongSwipeAction);
            mEdgeLongSwipeAction = null;
            removedCount++;
        }
        if (!SystemNavigationPreferenceController.isGestureAvailable(getActivity())){
            if (mNavigationGestures != null){
                mNavbarCategory.removePreference(mNavigationGestures);
                mNavigationGestures = null;
                removedCount++;
            }
        }else{
            if (mNavigationGestures != null){
                mNavigationGestures.setSummary(SystemNavigationPreferenceController.getPrefSummary(getActivity()));
            }
        }
        if (mNavbarCategory != null && removedCount > 8){
            getPreferenceScreen().removePreference(mNavbarCategory);
            mNavbarCategory = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Power button ends calls.
        if (mPowerEndCall != null) {
            final int incallPowerBehavior = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mPowerEndCall.setChecked(powerButtonEndsCall);
        }

        // Home button answers calls.
        if (mHomeAnswerCall != null) {
            final int incallHomeBehavior = Settings.System.getInt(getContentResolver(),
                    Settings.System.RING_HOME_BUTTON_BEHAVIOR,
                    Settings.System.RING_HOME_BUTTON_BEHAVIOR_DEFAULT);
            final boolean homeButtonAnswersCall =
                (incallHomeBehavior == Settings.System.RING_HOME_BUTTON_BEHAVIOR_ANSWER);
            mHomeAnswerCall.setChecked(homeButtonAnswersCall);
        }

        // Navigation bar modes
        updateNavigationBarModeState();
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        }else if (preference == mNavigationHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_LONG_PRESS_ACTION_NAVBAR);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mNavigationHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_HOME_DOUBLE_TAP_ACTION_NAVBAR);
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
        } else if (preference == mNavigationAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION_NAVBAR);
            return true;
        } else if (preference == mNavigationInverse) {
            mNavigationInverse.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mNavigationInverse.setEnabled(true);
                    }catch(Exception e){
                    }
                }
            }, 1000);
            return true;
        }else if (preference == mVolumeKeyCursorControl) {
            handleListChange(mVolumeKeyCursorControl, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference == mEdgeLongSwipeAction) {
            handleListChange(mEdgeLongSwipeAction, newValue,
                    Settings.System.KEY_EDGE_LONG_SWIPE_ACTION);
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
            if (mHomeAnswerCall != null) {
                mHomeAnswerCall.setEnabled(!navbarEnabled);
            }
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
        }else if(preference == mAdditionalButtonsPreference){
            try {
                String[] customButtonsPackage = getResources().getString(R.string.config_customButtonsPackage).split("/");
                String activityName = customButtonsPackage[0];
                String className = customButtonsPackage[1];
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(activityName, className));
                startActivity(intent);
            } catch (Exception e){
            }
        } else if (preference == mPowerEndCall) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (preference == mHomeAnswerCall) {
            handleToggleHomeButtonAnswersCallPreferenceClick();
            return true;
        }else if (preference == mSwapVolumeButtons) {
            int value;

            if (mSwapVolumeButtons.isChecked()) {
                /* The native inputflinger service uses the same logic of:
                 *   1 - the volume rocker is on one the sides, relative to the natural
                 *       orientation of the display (true for all phones and most tablets)
                 *   2 - the volume rocker is on the top or bottom, relative to the
                 *       natural orientation of the display (true for some tablets)
                 */
                value = getResources().getInteger(
                        R.integer.config_volumeRockerVsDisplayOrientation);
            } else {
                /* Disable the re-orient functionality */
                value = 0;
            }
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
        } 

        return super.onPreferenceTreeClick(preference);
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleToggleHomeButtonAnswersCallPreferenceClick() {
        Settings.System.putInt(getContentResolver(),
                Settings.System.RING_HOME_BUTTON_BEHAVIOR, (mHomeAnswerCall.isChecked()
                        ? Settings.System.RING_HOME_BUTTON_BEHAVIOR_ANSWER
                        : Settings.System.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING));
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
