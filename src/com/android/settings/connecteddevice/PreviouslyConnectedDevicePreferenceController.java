/*
 * Copyright 2018 The Android Open Source Project
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
package com.android.settings.connecteddevice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.bluetooth.BluetoothAdapter;

import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settings.bluetooth.Utils;
import com.android.settings.bluetooth.BluetoothDeviceUpdater;
import com.android.settings.bluetooth.SavedBluetoothDeviceUpdater;
import com.android.settings.connecteddevice.dock.DockUpdater;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class PreviouslyConnectedDevicePreferenceController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback, BluetoothCallback {

    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager manager;
    private Preference mPreference;
    private BluetoothDeviceUpdater mBluetoothDeviceUpdater;
    private DockUpdater mSavedDockUpdater;
    private int mPreferenceSize;

    public PreviouslyConnectedDevicePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);

        mSavedDockUpdater = FeatureFactory.getFactory(
                context).getDockUpdaterFeatureProvider().getSavedDockUpdater(context, this);
        manager = Utils.getLocalBtManager(context);
         if ( manager != null) {
           mLocalAdapter = manager.getBluetoothAdapter();
         }
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
                ? AVAILABLE
                : CONDITIONALLY_UNAVAILABLE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mPreference = screen.findPreference(getPreferenceKey());
            mBluetoothDeviceUpdater.setPrefContext(screen.getContext());
        }
    }

    @Override
    public void onStart() {
        mBluetoothDeviceUpdater.registerCallback();
        mSavedDockUpdater.registerCallback();
        manager.getEventManager().registerCallback(this);
        updatePreferenceOnSizeChanged();
    }

    @Override
    public void onStop() {
        mBluetoothDeviceUpdater.unregisterCallback();
        mSavedDockUpdater.unregisterCallback();
        manager.getEventManager().unregisterCallback(this);

    }

    public void init(DashboardFragment fragment) {
        mBluetoothDeviceUpdater = new SavedBluetoothDeviceUpdater(fragment.getContext(),
                fragment, PreviouslyConnectedDevicePreferenceController.this);
    }

    @Override
    public void onDeviceAdded(Preference preference) {
        mPreferenceSize++;
        updatePreferenceOnSizeChanged();
    }

    @Override
    public void onDeviceRemoved(Preference preference) {
        mPreferenceSize--;
        updatePreferenceOnSizeChanged();
    }

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        updatePreferenceOnSizeChanged();
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        // do nothing
    }

    @Override
    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
       // do nothing
    }

    @Override
    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        // do nothing
    }

    @Override
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        // do nothing
    }

    @Override
    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        // do nothing
    }

    @Override
    public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
        // do nothing
    }

    @Override
    public void onAudioModeChanged() {
       // do nothing
    }

    @VisibleForTesting
    void setBluetoothDeviceUpdater(BluetoothDeviceUpdater bluetoothDeviceUpdater) {
        mBluetoothDeviceUpdater = bluetoothDeviceUpdater;
    }

    @VisibleForTesting
    void setSavedDockUpdater(DockUpdater savedDockUpdater) {
        mSavedDockUpdater = savedDockUpdater;
    }

    @VisibleForTesting
    void setPreferenceSize(int size) {
        mPreferenceSize = size;
    }

    @VisibleForTesting
    void setPreference(Preference preference) {
        mPreference = preference;
    }

    private void updatePreferenceOnSizeChanged() {
        if (isAvailable()) {
            if ((mLocalAdapter != null) &&
                (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON)) {
                mPreference.setVisible(mPreferenceSize != 0);
            } else {
                mPreference.setVisible(false);
            }
        }
    }
}
