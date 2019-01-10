/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.settings;

import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Telephony;
import android.support.annotation.VisibleForTesting;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.ims.ImsManager;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settings.network.ApnSettings;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

/**
 * Confirm and execute a reset of the network settings to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL RESET EVERYTHING"
 * prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the confirmation screen.
 */
public class ResetNetworkConfirm extends InstrumentedFragment {

    private View mContentView;
    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    @VisibleForTesting boolean mEraseEsim;
    @VisibleForTesting EraseEsimAsyncTask mEraseEsimTask;
    private ResetNetworkAyncTask mResetNetworkTask;
    private ProgressDialog mProgressDialog;

    /**
     * Async task used to erase all the eSIM profiles from the phone. If error happens during
     * erasing eSIM profiles or timeout, an error msg is shown.
     */
    private static class EraseEsimAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;
        private final String mPackageName;

        EraseEsimAsyncTask(Context context, String packageName) {
            mContext = context;
            mPackageName = packageName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return RecoverySystem.wipeEuiccData(mContext, mPackageName);
        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            if (succeeded) {
                Toast.makeText(mContext, R.string.reset_network_complete_toast, Toast.LENGTH_SHORT)
                        .show();
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.reset_esim_error_title)
                        .setMessage(R.string.reset_esim_error_msg)
                        .setPositiveButton(android.R.string.ok, null /* listener */)
                        .show();
            }
        }
    }

    private static class ResetNetworkAyncTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext;
        private String mPackageName;
        private ProgressDialog mProgressDialog;
        private boolean mEraseESimCard;
        private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

        ResetNetworkAyncTask(Context context, String packageName, int subId, boolean eraseESim,
                             ProgressDialog progressDialog) {
            mContext = context;
            mPackageName = packageName;
            mSubId = subId;
            mEraseESimCard = eraseESim;
            mProgressDialog = progressDialog;
        }

        @Override
        protected void onPreExecute() {
            if (null != mProgressDialog) {
                mProgressDialog.show();
            }
        }

        @Override
        protected void onPostExecute(Boolean succeeded) {
            if (null != mProgressDialog && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }

            if (succeeded) {
                Toast.makeText(mContext, R.string.reset_network_complete_toast, Toast.LENGTH_SHORT)
                        .show();
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.reset_esim_error_title)
                        .setMessage(R.string.reset_esim_error_msg)
                        .setPositiveButton(android.R.string.ok, null /* listener */)
                        .show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.factoryReset();
            }

            WifiManager wifiManager = (WifiManager)
                    mContext.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiManager.factoryReset();
            }

            TelephonyManager telephonyManager = (TelephonyManager)
                    mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.factoryReset(mSubId);
            }

            NetworkPolicyManager policyManager = (NetworkPolicyManager)
                    mContext.getSystemService(Context.NETWORK_POLICY_SERVICE);
            if (policyManager != null) {
                String subscriberId = telephonyManager.getSubscriberId(mSubId);
                policyManager.factoryReset(subscriberId);
            }

            BluetoothManager btManager = (BluetoothManager)
                    mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (btManager != null) {
                BluetoothAdapter btAdapter = btManager.getAdapter();
                if (btAdapter != null) {
                    btAdapter.factoryReset();
                    LocalBluetoothManager mLocalBtManager =
                            LocalBluetoothManager.getInstance(mContext, null);
                    if (mLocalBtManager != null) {
                        CachedBluetoothDeviceManager cachedDeviceManager =
                                mLocalBtManager.getCachedDeviceManager();
                        cachedDeviceManager.clearAllDevices();
                    }
                }
            }

            ImsManager.getInstance(mContext,
                    SubscriptionManager.getPhoneId(mSubId)).factoryReset();
            // There has been issues when Sms raw table somehow stores orphan
            // fragments. They lead to garbled message when new fragments come
            // in and combied with those stale ones. In case this happens again,
            // user can reset all network settings which will clean up this table.
            cleanUpSmsRawTable(mContext);
            restoreDefaultApn(mContext);

            if (mEraseESimCard) {
                return RecoverySystem.wipeEuiccData(mContext, mPackageName);
            }

            return true;
        }

        private void cleanUpSmsRawTable(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(Telephony.Sms.CONTENT_URI,
                    "raw/permanentDelete");
            resolver.delete(uri, null, null);
        }

        /**
         * Restore APN settings to default.
         */
        private void restoreDefaultApn(Context context) {
            Uri uri = Uri.parse(ApnSettings.RESTORE_CARRIERS_URI);

            if (SubscriptionManager.isUsableSubIdValue(mSubId)) {
                uri = Uri.withAppendedPath(uri, "subId/" + String.valueOf(mSubId));
            }

            ContentResolver resolver = context.getContentResolver();
            resolver.delete(uri, null, null);
        }
    }

    private ProgressDialog getProgressDialog() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(
                getActivity().getString(R.string.reset_network_progress_title));
        progressDialog.setMessage(
                getActivity().getString(R.string.reset_network_progress_text));
        return progressDialog;
    }

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and reset the network settings to its factory-default state.
     */
    private Button.OnClickListener mFinalClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (Utils.isMonkeyRunning()) {
                return;
            }

            Context context = getActivity();
            mProgressDialog = getProgressDialog();
            mResetNetworkTask = new ResetNetworkAyncTask(context, context.getPackageName(),
                    mSubId, mEraseEsim, mProgressDialog);
            mResetNetworkTask.execute();
        }

    };

    @VisibleForTesting
    void esimFactoryReset(Context context, String packageName) {
        if (mEraseEsim) {
            mEraseEsimTask = new EraseEsimAsyncTask(context, packageName);
            mEraseEsimTask.execute();
        }
    }

    /**
     * Configure the UI for the final confirmation interaction
     */
    private void establishFinalConfirmationState() {
        mContentView.findViewById(R.id.execute_reset_network)
                .setOnClickListener(mFinalClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(
                getActivity(), UserManager.DISALLOW_NETWORK_RESET, UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(),
                UserManager.DISALLOW_NETWORK_RESET, UserHandle.myUserId())) {
            return inflater.inflate(R.layout.network_reset_disallowed_screen, null);
        } else if (admin != null) {
            new ActionDisabledByAdminDialogHelper(getActivity())
                    .prepareDialogBuilder(UserManager.DISALLOW_NETWORK_RESET, admin)
                    .setOnDismissListener(__ -> getActivity().finish())
                    .show();
            return new View(getContext());
        }
        mContentView = inflater.inflate(R.layout.reset_network_confirm, null);
        establishFinalConfirmationState();
        return mContentView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mSubId = args.getInt(PhoneConstants.SUBSCRIPTION_KEY,
                    SubscriptionManager.INVALID_SUBSCRIPTION_ID);
            mEraseEsim = args.getBoolean(MasterClear.ERASE_ESIMS_EXTRA);
        }
    }

    @Override
    public void onDestroy() {
        if (mEraseEsimTask != null) {
            mEraseEsimTask.cancel(true /* mayInterruptIfRunning */);
            mEraseEsimTask = null;
        }

        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        if (null != mResetNetworkTask && !mResetNetworkTask.isCancelled()) {
            mResetNetworkTask.cancel(true /* mayInterruptIfRunning */);
            mResetNetworkTask = null;
        }

        super.onDestroy();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.RESET_NETWORK_CONFIRM;
    }
}
