/*
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

package com.android.settings.custom.buttons;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;

import com.android.settings.core.BasePreferenceController;

public class CustomButtonsPreferenceController extends BasePreferenceController {

    static final String KEY_BUTTONS_CUSTOM = "additional_buttons";

    public CustomButtonsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return !mContext.getResources().getString(R.string.config_customButtonsPackage).equals("") ?
                AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_BUTTONS_CUSTOM.equals(preference.getKey())) {
            try {
                String[] customButtonsPackage = mContext.getResources().getString(R.string.config_customButtonsPackage).split("/");
                String activityName = customButtonsPackage[0];
                String className = customButtonsPackage[1];
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(activityName, className));
                mContext.startActivity(intent);
            } catch (Exception e){
            }
        }
        return false;
    }

}