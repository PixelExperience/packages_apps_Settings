/*
 * Copyright (C) 2018 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.biometrics.face;

import android.content.Intent;
import android.hardware.face.FaceManager;
import android.os.Bundle;

import android.app.Activity;
import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;
import android.util.Log;

import com.android.settings.Utils;
import com.android.settings.core.InstrumentedActivity;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.R;

public class LaunchMotoFaceEnroll extends InstrumentedActivity {

    private static final String TAG = LaunchMotoFaceEnroll.class.getSimpleName();
    private static final int REQUEST_CODE = 180;
    private int mUserId;

    private FaceManager mFaceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this);
        String title = getString(R.string.security_settings_face_preference_title);
        if (mUserId == UserHandle.USER_NULL) {
            helper.launchConfirmationActivity(REQUEST_CODE,
                title, null, null, getChallenge(), true);
        } else {
            helper.launchConfirmationActivity(REQUEST_CODE,
                title, null, null, getChallenge(), mUserId, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            byte[] token = data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
            openMotoFaceUnlock(token);
        }
        finish();
    }

    private void openMotoFaceUnlock(byte[] token) {
        startActivity(getIntent(token, mUserId));
    }

    public static Intent getIntent(byte[] token, int userId) {
        Intent intent = new Intent();
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
        if (userId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, userId);
        }
        intent.setComponent(new ComponentName("com.motorola.faceunlock", "com.motorola.faceunlock.SetupFaceIntroActivity"));
        return intent;
    }

    private long getChallenge() {
        mFaceManager = Utils.getFaceManagerOrNull(this);
        if (mFaceManager == null) {
            return 0;
        }
        return mFaceManager.generateChallenge();
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.FACE_ENROLL_INTRO;
    }
}
