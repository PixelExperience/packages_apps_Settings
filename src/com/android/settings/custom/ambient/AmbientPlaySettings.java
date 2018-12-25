/*
 * Copyright (C) 2018 PixelExperience
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

package com.android.settings.custom.ambient;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import android.content.BroadcastReceiver;

import com.android.settings.custom.ambient.history.AmbientPlayHistoryPreference;

import com.android.internal.util.custom.ambient.play.AmbientPlayHistoryManager;

public class AmbientPlaySettings extends SettingsPreferenceFragment implements CompoundButton.OnCheckedChangeListener {

    private TextView mTextView;

    private SwitchPreference mAmbientRecognitionKeyguardPreference;
    private SwitchPreference mAmbientRecognitionNotificationPreference;
    private AmbientPlayHistoryPreference mAmbientRecognitionHistoryPreference;
    private Preference mAmbientRecognitionSavingOptionsPreference;

    private String AMBIENT_RECOGNITION_KEYGUARD = "ambient_recognition_keyguard";
    private String AMBIENT_RECOGNITION_NOTIFICATION = "ambient_recognition_notification";
    private String AMBIENT_RECOGNITION_HISTORY = "ambient_recognition_history_preference";
    private String AMBIENT_RECOGNITION_SAVING_OPTIONS = "ambient_recognition_saving_options";

    private BroadcastReceiver onSongMatch = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction())) {
                if (mAmbientRecognitionHistoryPreference != null){
                    mAmbientRecognitionHistoryPreference.updateSummary(getActivity());
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_play_settings);
        mAmbientRecognitionKeyguardPreference = (SwitchPreference) findPreference(AMBIENT_RECOGNITION_KEYGUARD);
        mAmbientRecognitionKeyguardPreference.setEnabled(isEnabled());
        mAmbientRecognitionNotificationPreference = (SwitchPreference) findPreference(AMBIENT_RECOGNITION_NOTIFICATION);
        mAmbientRecognitionNotificationPreference.setEnabled(isEnabled());
        mAmbientRecognitionHistoryPreference = (AmbientPlayHistoryPreference) findPreference(AMBIENT_RECOGNITION_HISTORY);
        mAmbientRecognitionSavingOptionsPreference = (Preference) findPreference(AMBIENT_RECOGNITION_SAVING_OPTIONS);
        mAmbientRecognitionSavingOptionsPreference.setEnabled(isEnabled());
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(onSongMatch, new IntentFilter(AmbientPlayHistoryManager.INTENT_SONG_MATCH.getAction()));
        if (mAmbientRecognitionHistoryPreference != null){
            mAmbientRecognitionHistoryPreference.updateSummary(getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(onSongMatch);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SETTINGS;
    }

    private boolean isEnabled() {
        return Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.AMBIENT_RECOGNITION, 0, UserHandle.USER_CURRENT) != 0;
    }

    private void setEnabled(boolean enabled) {
        Settings.System.putIntForUser(getActivity().getContentResolver(),
                Settings.System.AMBIENT_RECOGNITION, enabled ? 1 : 0, UserHandle.USER_CURRENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.ambient_play, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(isEnabled() ? R.string.ambient_play_switch_bar_on : R.string.ambient_play_switch_bar_off));
        View switchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = switchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(isEnabled());
        switchWidget.setOnCheckedChangeListener(this);
        switchBar.setOnClickListener(v -> switchWidget.setChecked(!switchWidget.isChecked()));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        setEnabled(isChecked);
        mTextView.setText(getString(isChecked ? R.string.ambient_play_switch_bar_on : R.string.ambient_play_switch_bar_off));
        mAmbientRecognitionKeyguardPreference.setEnabled(isChecked);
        mAmbientRecognitionNotificationPreference.setEnabled(isChecked);
        mAmbientRecognitionSavingOptionsPreference.setEnabled(isChecked);
    }
}
