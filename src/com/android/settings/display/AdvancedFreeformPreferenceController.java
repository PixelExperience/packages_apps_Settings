/*
 * Copyright (C) 2023 The Android Open Source Project
 *               2023 The Ginkgo
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

package com.android.settings.display;

import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

import com.android.settings.core.BasePreferenceController;

public class AdvancedFreeformPreferenceController extends BasePreferenceController {

    public AdvancedFreeformPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return supportsFreeformMultiWindow(mContext) ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    public static boolean supportsFreeformMultiWindow(Context context) {
        final boolean freeformDevOption = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.DEVELOPMENT_ENABLE_FREEFORM_WINDOWS_SUPPORT, 0) != 0;
        return ActivityTaskManager.supportsMultiWindow(context)
                && (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT)
                || freeformDevOption);
    }

}
