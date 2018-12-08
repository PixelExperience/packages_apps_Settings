/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.text.BidiFormatter;
import android.text.TextUtils;

import com.android.settings.R;

import android.os.SystemProperties;

public class BuildNumberDialogController {

    @VisibleForTesting
    static final int BUILD_NUMBER_VALUE_ID = R.id.build_number_value;

    private final FirmwareVersionDialogFragment mDialog;

    public BuildNumberDialogController(FirmwareVersionDialogFragment dialog) {
        mDialog = dialog;
    }

    private String getPixelExperienceVersion(){
        String buildDate = SystemProperties.get("org.pixelexperience.build_date","");
        String buildType = SystemProperties.get("org.pixelexperience.build_type","unofficial").toUpperCase();
        return buildDate.equals("") ? "" : "PixelExperience-" + buildDate + "-" + buildType;
    }

    /**
     * Updates the build number to the dialog.
     */
    public void initialize() {
        
        StringBuilder sb = new StringBuilder();
        sb.append(BidiFormatter.getInstance().unicodeWrap(
                TextUtils.isEmpty(Build.VENDOR.BUILD_NUMBER_OVERRIDE) ? Build.DISPLAY : Build.VENDOR.BUILD_NUMBER_OVERRIDE));
        String pixelExperienceVersion = getPixelExperienceVersion();
        if (!pixelExperienceVersion.equals("")){
            sb.append("\n");
            sb.append(pixelExperienceVersion);
        }
        mDialog.setText(BUILD_NUMBER_VALUE_ID, sb.toString());
    }
}
