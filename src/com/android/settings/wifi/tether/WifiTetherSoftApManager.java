package com.android.settings.wifi.tether;

import android.net.wifi.WifiManager;
import android.os.Handler;

/**
 * Wrapper for {@link android.net.wifi.WifiManager.SoftApCallback} to pass the robo test
 */
public class WifiTetherSoftApManager {

    private WifiManager mWifiManager;
    private WifiTetherSoftApCallback mWifiTetherSoftApCallback;

    private WifiManager.SoftApCallback mSoftApCallback = new WifiManager.SoftApCallback() {
        @Override
        public void onStateChanged(int state, int failureReason) {
            mWifiTetherSoftApCallback.onStateChanged(state, failureReason);
        }

        @Override
        public void onNumClientsChanged(int numClients) {
            // Do nothing - we don't care about changing anything here.
        }

        @Override
        public void onStaConnected(String Macaddr, int numClients) {
            mWifiTetherSoftApCallback.onNumClientsChanged(numClients);
        }

        @Override
        public void onStaDisconnected(String Macaddr, int numClients) {
            mWifiTetherSoftApCallback.onNumClientsChanged(numClients);
        }
    };
    private Handler mHandler;

    WifiTetherSoftApManager(WifiManager wifiManager,
            WifiTetherSoftApCallback wifiTetherSoftApCallback) {
        mWifiManager = wifiManager;
        mWifiTetherSoftApCallback = wifiTetherSoftApCallback;
        mHandler = new Handler();
    }

    public void registerSoftApCallback() {
        mWifiManager.registerSoftApCallback(mSoftApCallback, mHandler);
    }

    public void unRegisterSoftApCallback() {
        mWifiManager.unregisterSoftApCallback(mSoftApCallback);
    }

    public interface WifiTetherSoftApCallback {
        void onStateChanged(int state, int failureReason);

        void onNumClientsChanged(int numClients);
    }
}
