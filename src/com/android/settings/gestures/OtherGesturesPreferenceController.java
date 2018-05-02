/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.gestures;

import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.core.PreferenceControllerMixin;

public class OtherGesturesPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {

    private final String KEY_OTHER_GESTURES = "other_gestures";
    private Context mContext;

    public OtherGesturesPreferenceController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_OTHER_GESTURES.equals(preference.getKey())) {
            try {
                String[] customGesturePackage = mContext.getResources().getString(R.string.config_customGesturePackage).split("/");
                String activityName = customGesturePackage[0];
                String className = customGesturePackage[1];
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(activityName, className));
                mContext.startActivity(intent);
            } catch (Exception e){
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return !mContext.getResources().getString(R.string.config_customGesturePackage).equals("");
    }

    @Override
    public String getPreferenceKey() {
        return KEY_OTHER_GESTURES;
    }
}
