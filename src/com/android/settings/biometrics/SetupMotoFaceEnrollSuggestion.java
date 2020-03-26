package com.android.settings.biometrics;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.android.settings.Utils;

public class SetupMotoFaceEnrollSuggestion extends Activity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (Utils.isMotoFaceUnlock()) {
            Intent intent = new Intent(this, BiometricEnrollActivity.class);
            intent.putExtra("for_face", true);
            intent.setFlags(33554432);
            startActivity(intent);
        }
        finish();
    }

    public static void enableComponent(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, SetupMotoFaceEnrollSuggestion.class);
        if (packageManager.getComponentEnabledSetting(componentName) != 1) {
            packageManager.setComponentEnabledSetting(componentName, 1, 1);
        }
    }

    public static void disableComponent(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, SetupMotoFaceEnrollSuggestion.class);
        if (packageManager.getComponentEnabledSetting(componentName) != 2) {
            packageManager.setComponentEnabledSetting(componentName, 2, 1);
        }
    }
}
