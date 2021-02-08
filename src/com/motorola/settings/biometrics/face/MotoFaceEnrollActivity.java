package com.motorola.settings.biometrics.face;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.android.settings.biometrics.face.FaceEnrollIntroduction;
import com.motorola.settings.biometrics.FaceUtils;

public class MotoFaceEnrollActivity extends Activity {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (FaceUtils.isFaceUnlockSupported()) {
            Intent faceIntroIntent = getFaceIntroIntent();
            faceIntroIntent.putExtra("for_redo", getIntent().getBooleanExtra("for_redo", false));
            if (getCallingActivity() != null) {
                faceIntroIntent.setFlags(33554432);
            }
            startActivity(faceIntroIntent);
        }
        finish();
    }

    private Intent getFaceIntroIntent() {
        Intent intent = new Intent(this, FaceEnrollIntroduction.class);
        intent.addFlags(268468224);
        return intent;
    }
}
