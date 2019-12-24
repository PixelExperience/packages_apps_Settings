package com.google.android.libraries.hats20;

import com.google.android.apps.common.testing.p003ui.espresso.IdlingResource;

public class IdleResourceManager implements IdlingResource {
    private IdlingResource.ResourceCallback espressoIdlingCallback;
    private boolean isMultipleChoiceSelectionAnimating;
    private boolean isThankYouAnimating;

    public void setIsMultipleChoiceSelectionAnimating(boolean z) {
        boolean isIdleNow = isIdleNow();
        isMultipleChoiceSelectionAnimating = z;
        if (!isIdleNow && isIdleNow()) {
            transitionToIdle();
        }
    }

    public void setIsThankYouAnimating(boolean z) {
        boolean isIdleNow = isIdleNow();
        isThankYouAnimating = z;
        if (!isIdleNow && isIdleNow()) {
            transitionToIdle();
        }
    }

    private void transitionToIdle() {
        IdlingResource.ResourceCallback resourceCallback = espressoIdlingCallback;
        if (resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }

    public boolean isIdleNow() {
        return !isMultipleChoiceSelectionAnimating && !isThankYouAnimating;
    }
}
