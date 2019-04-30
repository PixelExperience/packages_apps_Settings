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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.wrapper.OverlayManagerWrapper;
import com.android.settings.wrapper.OverlayManagerWrapper.OverlayInfo;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.android.internal.logging.nano.MetricsProto.MetricsEvent.ACTION_THEME;

public class ThemePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_THEME = "theme";

    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final OverlayManagerWrapper mOverlayService;
    private final PackageManager mPackageManager;
    private final String[] mAllAllowedAccents;
    private final String[] mAllowedAccents;
    private final String[] mAllowedAccentsWhenLight;
    private final String[] mAllowedAccentsWhenDark;

    public ThemePreferenceController(Context context) {
        this(context, ServiceManager.getService(Context.OVERLAY_SERVICE) != null
                ? new OverlayManagerWrapper() : null);
    }

    @VisibleForTesting
    ThemePreferenceController(Context context, OverlayManagerWrapper overlayManager) {
        super(context);
        mOverlayService = overlayManager;
        mAllowedAccents = mContext.getResources().getStringArray(R.array.allowed_themes);
        mAllowedAccentsWhenLight = mContext.getResources().getStringArray(R.array.allowed_themes_when_light);
        mAllowedAccentsWhenDark = mContext.getResources().getStringArray(R.array.allowed_themes_when_dark);
        List<String> allAccents = new ArrayList<>();
        Collections.addAll(allAccents, mAllowedAccents);
        Collections.addAll(allAccents, mAllowedAccentsWhenLight);
        Collections.addAll(allAccents, mAllowedAccentsWhenDark);
        mAllAllowedAccents = allAccents.toArray(new String[allAccents.size()]);
        mPackageManager = context.getPackageManager();
        mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    @Override
    public String getPreferenceKey() {
        return KEY_THEME;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_THEME.equals(preference.getKey())) {
            mMetricsFeatureProvider.action(mContext, ACTION_THEME);
        }
        return false;
    }

    @Override
    public void updateState(Preference preference) {
        ListPreference pref = (ListPreference) preference;
        String[] pkgs = getAvailableAccents();
        CharSequence[] labels = new CharSequence[pkgs.length];
        for (int i = 0; i < pkgs.length; i++) {
            try {
                if (pkgs[i].equals("0")){
                    labels[i] = mContext.getString(R.string.default_theme);
                    continue;
                }
                labels[i] = mPackageManager.getApplicationInfo(pkgs[i], 0)
                        .loadLabel(mPackageManager);
            } catch (NameNotFoundException e) {
                labels[i] = pkgs[i];
            }
        }
        pref.setEntries(labels);
        pref.setEntryValues(pkgs);
        String current = getCurrentAccent();
        CharSequence themeLabel = null;

        for (int i = 0; i < pkgs.length; i++) {
            if (TextUtils.equals(pkgs[i], current)) {
                themeLabel = labels[i];
                break;
            }
        }

        if (TextUtils.isEmpty(themeLabel)) {
            themeLabel = mContext.getString(R.string.default_theme);
        }

        pref.setSummary(themeLabel);
        pref.setValue(current);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String current = getCurrentAccent();
        if (Objects.equals(newValue, current)) {
            return true;
        }
        disableAllAccents();
        if (Objects.equals(newValue, "0")) {
            return true;
        }
        mOverlayService.setEnabled((String) newValue, true, UserHandle.myUserId());
        return true;
    }

    private boolean isValidAccent(OverlayInfo oi, boolean darkThemeCheck) {
        if (darkThemeCheck){
            boolean isUsingDarkTheme = isUsingDarkTheme();
            if (!Arrays.asList(mAllowedAccents).contains(oi.packageName) &&
                !Arrays.asList(isUsingDarkTheme ?
                    mAllowedAccentsWhenDark : mAllowedAccentsWhenLight).contains(oi.packageName)){
                return false;
            }
        }else if (!Arrays.asList(mAllAllowedAccents).contains(oi.packageName)){
            return false;
        }
        try {
            PackageInfo pi = mPackageManager.getPackageInfo(oi.packageName, 0);
            return pi != null && !pi.isStaticOverlayPackage();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void disableAllAccents() {
        List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                UserHandle.myUserId());
        for (int i = 0, size = infos.size(); i < size; i++) {
            if (infos.get(i).isEnabled() && isValidAccent(infos.get(i), false)) {
                mOverlayService.setEnabled(infos.get(i).packageName, false, UserHandle.myUserId());
            }
        }
    }

    private String getCurrentAccent() {
        List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                UserHandle.myUserId());
        for (int i = 0, size = infos.size(); i < size; i++) {
            if (infos.get(i).isEnabled() && isValidAccent(infos.get(i), true)) {
                return infos.get(i).packageName;
            }
        }
        return "0";
    }

    @Override
    public boolean isAvailable() {
        if (mOverlayService == null) return false;
        String[] themes = getAvailableAccents();
        return themes != null && themes.length > 1;
    }

    @VisibleForTesting
    String[] getAvailableAccents() {
        List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                UserHandle.myUserId());
        List<String> pkgs = new ArrayList<>(infos.size());
        pkgs.add("0");
        for (int i = 0, size = infos.size(); i < size; i++) {
            if (isValidAccent(infos.get(i), true)) {
                pkgs.add(infos.get(i).packageName);
            }
        }
        return pkgs.toArray(new String[pkgs.size()]);
    }

    private boolean isUsingDarkTheme() {
        List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                UserHandle.myUserId());
        for (int i = 0, size = infos.size(); i < size; i++) {
            if (infos.get(i).isEnabled() && infos.get(i).packageName.equals("com.android.system.theme.dark")) {
                return true;
            }
        }
        return false;
    }
}
