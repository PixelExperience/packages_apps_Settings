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

import android.app.admin.DevicePolicyManager;
import android.app.settings.SettingsEnums;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.face.FaceManager;
import android.os.Bundle;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.biometrics.BiometricEnrollIntroduction;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtilsInternal;

import com.google.android.setupcompat.template.FooterBarMixin;
import com.google.android.setupcompat.template.FooterButton;
import com.google.android.setupcompat.util.WizardManagerHelper;
import com.google.android.setupdesign.span.LinkSpan;
import com.google.android.setupdesign.template.RequireScrollMixin;

import com.android.settings.biometrics.SetupMotoFaceEnrollSuggestion;

public class FaceEnrollIntroduction extends BiometricEnrollIntroduction {

    private static final String TAG = "FaceIntro";

    private FaceManager mFaceManager;
    private FaceFeatureProvider mFaceFeatureProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFaceManager = Utils.getFaceManagerOrNull(this);
        mFaceFeatureProvider = FeatureFactory.getFactory(getApplicationContext())
                .getFaceFeatureProvider();

        mFooterBarMixin = getLayout().getMixin(FooterBarMixin.class);
        if (WizardManagerHelper.isAnySetupWizard(getIntent())) {
            mFooterBarMixin.setSecondaryButton(
                    new FooterButton.Builder(this)
                            .setText(R.string.security_settings_face_enroll_introduction_no_thanks)
                            .setListener(this::onSkipButtonClick)
                            .setButtonType(FooterButton.ButtonType.SKIP)
                            .setTheme(R.style.SudGlifButton_Secondary)
                            .build()
            );
        } else {
            mFooterBarMixin.setSecondaryButton(
                    new FooterButton.Builder(this)
                            .setText(R.string.security_settings_face_enroll_introduction_no_thanks)
                            .setListener(this::onCancelButtonClick)
                            .setButtonType(FooterButton.ButtonType.CANCEL)
                            .setTheme(R.style.SudGlifButton_Secondary)
                            .build()
            );
        }

        FooterButton.Builder nextButtonBuilder = new FooterButton.Builder(this)
                .setText(R.string.security_settings_face_enroll_introduction_agree)
                .setButtonType(FooterButton.ButtonType.NEXT)
                .setTheme(R.style.SudGlifButton_Primary);
        if (maxFacesEnrolled()) {
            nextButtonBuilder.setListener(this::onNextButtonClick);
            mFooterBarMixin.setPrimaryButton(nextButtonBuilder.build());
        } else {
            final FooterButton agreeButton = nextButtonBuilder.build();
            mFooterBarMixin.setPrimaryButton(agreeButton);
            final RequireScrollMixin requireScrollMixin = getLayout().getMixin(
                    RequireScrollMixin.class);
            requireScrollMixin.requireScrollWithButton(this, agreeButton,
                    R.string.security_settings_face_enroll_introduction_more,
                    button -> {
                        onNextButtonClick(button);
                    });
        }

        final TextView footer2 = findViewById(R.id.face_enroll_introduction_footer_part_2);
        final int footer2TextResource =
                mFaceFeatureProvider.isAttentionSupported(getApplicationContext())
                        ? R.string.security_settings_face_enroll_introduction_footer_part_2
                        : R.string.security_settings_face_settings_footer_attention_not_supported;
        footer2.setText(footer2TextResource);
        if (Utils.isMotoFaceUnlock() && this.mHasPassword && this.mToken != null) {
            openMotoFaceUnlock();
        }
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (!Utils.isMotoFaceUnlock()) {
            return;
        }
        if (i != 1) {
            if (i != 4) {
                if (i == 5) {
                    if (i2 == 1 || i2 == -1) {
                        FaceManager faceManager = this.mFaceManager;
                        if (faceManager != null && faceManager.getEnrolledFaces(this.mUserId).size() > 0) {
                            SetupMotoFaceEnrollSuggestion.disableComponent(this);
                        }
                        setResult(1);
                        finish();
                        return;
                    }
                    setResult(0);
                    finish();
                }
            } else if (i2 == -1 && intent != null) {
                openMotoFaceUnlock();
            }
        } else if (i2 == 1) {
            openMotoFaceUnlock();
        }
    }

    private void openMotoFaceUnlock() {
        Intent intent = new Intent();
        intent.putExtra("hw_auth_token", this.mToken);
        int i = this.mUserId;
        if (i != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", i);
        }
        intent.setComponent(new ComponentName("com.motorola.faceunlock", "com.motorola.faceunlock.SetupFaceIntroActivity"));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 5);
        }
    }

    @Override
    protected boolean isDisabledByAdmin() {
        return RestrictedLockUtilsInternal.checkIfKeyguardFeaturesDisabled(
                this, DevicePolicyManager.KEYGUARD_DISABLE_FACE, mUserId) != null;
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.face_enroll_introduction;
    }

    @Override
    protected int getHeaderResDisabledByAdmin() {
        return R.string.security_settings_face_enroll_introduction_title_unlock_disabled;
    }

    @Override
    protected int getHeaderResDefault() {
        return R.string.security_settings_face_enroll_introduction_title;
    }

    @Override
    protected int getDescriptionResDisabledByAdmin() {
        return R.string.security_settings_face_enroll_introduction_message_unlock_disabled;
    }

    @Override
    protected FooterButton getCancelButton() {
        if (mFooterBarMixin != null) {
            return mFooterBarMixin.getSecondaryButton();
        }
        return null;
    }

    @Override
    protected FooterButton getNextButton() {
        if (mFooterBarMixin != null) {
            return mFooterBarMixin.getPrimaryButton();
        }
        return null;
    }

    @Override
    protected TextView getErrorTextView() {
        return findViewById(R.id.error_text);
    }

    private boolean maxFacesEnrolled() {
        if (mFaceManager != null) {
            final int max = getResources().getInteger(
                    com.android.internal.R.integer.config_faceMaxTemplatesPerUser);
            final int numEnrolledFaces = mFaceManager.getEnrolledFaces(mUserId).size();
            return numEnrolledFaces >= max;
        } else {
            return false;
        }
    }

    //TODO: Refactor this to something that conveys it is used for getting a string ID.
    @Override
    protected int checkMaxEnrolled() {
        if (mFaceManager != null) {
            if (maxFacesEnrolled()) {
                return R.string.face_intro_error_max;
            }
        } else {
            return R.string.face_intro_error_unknown;
        }
        return 0;
    }

    @Override
    protected long getChallenge() {
        mFaceManager = Utils.getFaceManagerOrNull(this);
        if (mFaceManager == null) {
            return 0;
        }
        return mFaceManager.generateChallenge();
    }

    @Override
    protected String getExtraKeyForBiometric() {
        return ChooseLockSettingsHelper.EXTRA_KEY_FOR_FACE;
    }

    @Override
    protected Intent getEnrollingIntent() {
        Intent intent = new Intent(this, FaceEnrollEducation.class);
        WizardManagerHelper.copyWizardManagerExtras(getIntent(), intent);
        return intent;
    }

    @Override
    protected int getConfirmLockTitleResId() {
        return R.string.security_settings_face_preference_title;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.FACE_ENROLL_INTRO;
    }

    @Override
    public void onClick(LinkSpan span) {
        // TODO(b/110906762)
    }
}
