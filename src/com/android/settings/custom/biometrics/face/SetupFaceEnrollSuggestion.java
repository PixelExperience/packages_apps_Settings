package com.android.settings.custom.biometrics.face;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.android.settings.biometrics.BiometricEnrollActivity;

import com.android.settings.custom.biometrics.FaceUtils;

public class SetupFaceEnrollSuggestion extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (FaceUtils.isFaceUnlockSupported()) {
            Intent intent = new Intent(this, BiometricEnrollActivity.class);
            intent.putExtra("for_face", true);
            intent.setFlags(33554432);
            startActivity(intent);
        }
        finish();
    }

    public static void disableComponent(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, SetupFaceEnrollSuggestion.class);
        if (packageManager.getComponentEnabledSetting(componentName) != 2) {
            packageManager.setComponentEnabledSetting(componentName, 2, 1);
        }
    }
}
