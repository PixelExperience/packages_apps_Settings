package com.google.android.libraries.hats20;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.libraries.hats20.PromptDialogDelegate;
import com.google.android.libraries.hats20.answer.AnswerBeacon;
import com.google.android.libraries.hats20.model.SurveyController;

public final class PlatformPromptDialogFragment extends DialogFragment implements PromptDialogDelegate.DialogFragmentInterface {
    private final PromptDialogDelegate delegate = new PromptDialogDelegate(this);

    public static PlatformPromptDialogFragment newInstance(String str, SurveyController surveyController, AnswerBeacon answerBeacon, Integer num, Integer num2, boolean z) {
        PlatformPromptDialogFragment platformPromptDialogFragment = new PlatformPromptDialogFragment();
        platformPromptDialogFragment.setArguments(PromptDialogDelegate.createArgs(str, surveyController, answerBeacon, num, num2, z));
        return platformPromptDialogFragment;
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return delegate.onCreateView(layoutInflater, viewGroup, bundle);
    }

    public void onStart() {
        super.onStart();
        delegate.onStart();
    }

    public void onResume() {
        super.onResume();
        delegate.onResume();
    }

    public void onPause() {
        super.onPause();
        delegate.onPause();
    }

    public void onDestroy() {
        delegate.onDestroy();
        super.onDestroy();
    }
}
