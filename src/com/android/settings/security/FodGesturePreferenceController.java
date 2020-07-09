/*
 * Copyright (C) 2020 PixelExperience
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

package com.android.settings.security;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.Utils;

public class FodGesturePreferenceController extends BasePreferenceController {

    public static final String KEY = "fod_gesture";

    private FingerprintManager fpm;

    public FodGesturePreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        fpm = Utils.getFingerprintManagerOrNull(context);
    }

    public FodGesturePreferenceController(Context context) {
        this(context, KEY);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_supportsInDisplayFingerprint)){
            return UNSUPPORTED_ON_DEVICE;
        }
        if (!mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_supportsScreenOffInDisplayFingerprint)){
            return UNSUPPORTED_ON_DEVICE;
        }
        if (fpm != null && (!fpm.isHardwareDetected() || !fpm.hasEnrolledFingerprints())) {
            return UNSUPPORTED_ON_DEVICE;
        }
        return AVAILABLE;
    }
}