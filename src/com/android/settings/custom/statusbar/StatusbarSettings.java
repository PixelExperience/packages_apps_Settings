/*
 * Copyright (C) 2016 The CyanogenMod project
 * Copyright (C) 2017-2018 The LineageOS project
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

package com.android.settings.custom.statusbar;

import android.content.Context;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.view.View;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.provider.SearchIndexableResource;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

public class StatusbarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, Indexable {
    private static final String TAG = "StatusbarSettings";

    private static final String NETWORK_TRAFFIC_CATEGORY = "network_traffic";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;

    private ListPreference mQuickPulldown;
    private PreferenceCategory mNetworkTrafficCategory;
    private DropDownPreference mNetTrafficMode;
    private SwitchPreference mNetTrafficAutohide;
    private DropDownPreference mNetTrafficUnits;
    private SwitchPreference mNetTrafficShowUnits;

    private static List<String> sNonIndexableKeys = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar_settings);
        final ContentResolver resolver = getActivity().getContentResolver();

        mNetworkTrafficCategory = (PreferenceCategory) findPreference(NETWORK_TRAFFIC_CATEGORY);

        // TODO: Check notch

        mQuickPulldown =
                (ListPreference) findPreference(Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        updateQuickPulldownSummary(quickPulldownValue);

        mNetTrafficMode = (DropDownPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_MODE);
        mNetTrafficMode.setOnPreferenceChangeListener(this);
        int mode = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_MODE, 0, UserHandle.USER_CURRENT);
        mNetTrafficMode.setValue(String.valueOf(mode));

        mNetTrafficAutohide = (SwitchPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_AUTOHIDE);
        mNetTrafficAutohide.setOnPreferenceChangeListener(this);

        mNetTrafficUnits = (DropDownPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_UNITS);
        mNetTrafficUnits.setOnPreferenceChangeListener(this);
        int units = Settings.System.getIntForUser(resolver,
                Settings.System.NETWORK_TRAFFIC_UNITS, /* Mbps */ 1, UserHandle.USER_CURRENT);
        mNetTrafficUnits.setValue(String.valueOf(units));

        mNetTrafficShowUnits = (SwitchPreference)
                findPreference(Settings.System.NETWORK_TRAFFIC_SHOW_UNITS);
        mNetTrafficShowUnits.setOnPreferenceChangeListener(this);

        updateNetworkTrafficEnabledStates(mode);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_qs_pulldown_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_qs_pulldown_values_rtl);
        }
        // TODO: Check notch
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mQuickPulldown) {
            updateQuickPulldownSummary(Integer.parseInt((String) newValue));
        }else if (preference == mNetTrafficMode) {
            int mode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_MODE, mode);
            updateNetworkTrafficEnabledStates(mode);
        } else if (preference == mNetTrafficUnits) {
            int units = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_UNITS, units);
        }
        return true;
    }

    private void updateNetworkTrafficEnabledStates(int mode) {
        final boolean enabled = mode != 0;
        mNetTrafficAutohide.setEnabled(enabled);
        mNetTrafficUnits.setEnabled(enabled);
        mNetTrafficShowUnits.setEnabled(enabled);
    }

    private void updateQuickPulldownSummary(int value) {
        String summary="";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_off);
                break;

            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_RIGHT:
                summary = getResources().getString(
                    R.string.status_bar_quick_qs_pulldown_summary,
                    getResources().getString(value == PULLDOWN_DIR_LEFT
                        ? R.string.status_bar_quick_qs_pulldown_summary_left
                        : R.string.status_bar_quick_qs_pulldown_summary_right));
                break;
        }
        mQuickPulldown.setSummary(summary);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    private void removePreferenceAndRemoveFromIndex(PreferenceScreen parent, Preference toRemove){
        if(toRemove instanceof PreferenceCategory) {
            ArrayList<Preference> list = getPreferenceList(toRemove, new ArrayList<Preference>());
            for (Preference p : list) {
                sNonIndexableKeys.add(p.getKey());
            }
        }
        sNonIndexableKeys.add(toRemove.getKey());
        parent.removePreference(toRemove);
    }
    
    private void removePreferenceAndRemoveFromIndex(PreferenceCategory parent, Preference toRemove){
        sNonIndexableKeys.add(toRemove.getKey());
        parent.removePreference(toRemove);
    }

    private ArrayList<Preference> getPreferenceList(Preference p, ArrayList<Preference> list) {
        if(p instanceof PreferenceCategory || p instanceof PreferenceScreen) {
            PreferenceGroup pGroup = (PreferenceGroup) p;
            int pCount = pGroup.getPreferenceCount();
            for(int i = 0; i < pCount; i++) {
                getPreferenceList(pGroup.getPreference(i), list);
            }
        }else{
            list.add(p);
        }
        return list;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                List<SearchIndexableResource> indexables = new ArrayList<>();
                SearchIndexableResource indexable = new SearchIndexableResource(context);
                indexable.xmlResId = R.xml.statusbar_settings;
                indexables.add(indexable);
                return indexables;
            }
            @Override
            public List<String> getNonIndexableKeys(Context context) {
                return sNonIndexableKeys;
            }
        };
}